package cn.game.games.net.game.helper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Supplier;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.util.SpringContextLoader;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * 玩家聚合快照的专用事务边界。独立线程打开非自动提交会话，显式 commit 后才报告确认。
 * 不复用 DAO 的等待超时包装；进入 Mapper 后的任何异常都保守报告 UNKNOWN。
 * 不修改共享 MyBatis 配置，也不把连接关闭或 rollback 请求当作库侧终止证明。
 */
final class PlayerSnapshotWriter {
    /** 事务失败及资源关闭日志。 */
    private static final Logger log = LoggerFactory.getLogger(PlayerSnapshotWriter.class);
    /** 独立数据库线程，不占用玩家邮箱的执行权。 */
    private static final Executor io = Executors.newVirtualThreadPerTaskExecutor();
    /** 生产或测试会话工厂。 */
    private final Supplier<SqlSessionFactory> factories;
    /** 异步实际执行入口。 */
    private final Executor executor;
    /** 从现有 Spring 容器取得同一玩家数据库工厂，首次写入时才访问配置。 */
    PlayerSnapshotWriter() { this(() -> SpringContextLoader.getContext().getBean(SqlSessionFactory.class), io); }
    /** @param factories 会话工厂 @param executor 独立执行器 */
    PlayerSnapshotWriter(Supplier<SqlSessionFactory> factories, Executor executor) {
        this.factories = factories; this.executor = executor;
    }
    /** @param data 封存参数 @param insert 创建或更新 @return 不带等待超时的真实事务结果 */
    Future<PlayerSaveCoordinator.WriteResult> write(PlayerData data, boolean insert) {
        Promise<PlayerSaveCoordinator.WriteResult> actual = Promise.promise();
        try {
            executor.execute(() -> actual.complete(execute(data, insert)));
        } catch (RejectedExecutionException failure) {
            log.warn("保存执行器未受理 playerId={}", data.getPlayerId(), failure);
            actual.complete(new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.NOT_WRITTEN, failure));
        }
        return actual.future();
    }
    /** 仅在专用执行线程中使用的实际调用阶段。 */
    private static final class Attempt {
        /** 已开始 Mapper 调用后，异常不能再宣称未执行。 */
        boolean enteredMapper;
    }
    /** @param data 独占参数 @param insert 创建或更新 @return 仅显式事务提交成功才确认 */
    private PlayerSaveCoordinator.WriteResult execute(PlayerData data, boolean insert) {
        Attempt attempt = new Attempt();
        try {
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                throw new IllegalStateException("专用保存线程不能加入外部事务");
            }
            SqlSessionFactory factory = factories.get();
            var environment = factory.getConfiguration().getEnvironment();
            var transactions = environment.getTransactionFactory();
            int rows;
            if (transactions instanceof SpringManagedTransactionFactory) {
                // SpringManaged 忽略 openSession 的 autoCommit 参数，必须先由同数据源的事务管理器绑定连接。
                TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(environment.getDataSource()));
                rows = transaction.execute(status -> {
                    SqlSession session = factory.openSession(ExecutorType.SIMPLE, false);
                    try {
                        int affected = invoke(session, data, insert, attempt);
                        if (affected != 1) { status.setRollbackOnly(); }
                        return affected;
                    } finally { close(session, data.getPlayerId()); }
                });
                // 模板返回时数据库 commit/rollback 已结束，不能在模板回调返回时提前通知成功。
            } else if (transactions instanceof JdbcTransactionFactory) {
                SqlSession session = factory.openSession(ExecutorType.SIMPLE, false);
                try {
                    rows = invoke(session, data, insert, attempt);
                    if (rows == 1) { session.commit(true); }
                    else { session.rollback(true); }
                } finally { close(session, data.getPlayerId()); }
            } else {
                throw new IllegalStateException("玩家保存要求支持显式提交的 JDBC/Spring 事务工厂");
            }
            return rows == 1 ? new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.COMMITTED, null)
                    : new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.NOT_WRITTEN,
                            new IllegalStateException("玩家快照影响行数必须为1，实际=" + rows));
        } catch (Throwable failure) {
            log.error("玩家快照事务未确认 playerId={} enteredMapper={}", data.getPlayerId(), attempt.enteredMapper, failure);
            return new PlayerSaveCoordinator.WriteResult(attempt.enteredMapper
                    ? PlayerSaveCoordinator.Outcome.UNKNOWN : PlayerSaveCoordinator.Outcome.NOT_WRITTEN, failure);
        }
    }
    /** @param session 专用会话 @param data 封存参数 @param insert 操作 @param attempt 调用阶段 @return 实际影响行数 */
    private int invoke(SqlSession session, PlayerData data, boolean insert, Attempt attempt) {
        try {
            if (session.getConnection().getAutoCommit()) { throw new IllegalStateException("玩家保存会话必须关闭自动提交"); }
        } catch (java.sql.SQLException failure) {
            log.warn("无法核对保存连接的自动提交状态 playerId={}", data.getPlayerId(), failure);
            throw new IllegalStateException("保存连接状态未知，未进入 Mapper", failure);
        }
        PlayerDataMapper mapper = session.getMapper(PlayerDataMapper.class);
        attempt.enteredMapper = true;
        return insert ? mapper.insert(data) : mapper.updateByPrimaryKey(data);
    }
    /** @param session 待关闭会话 @param playerId 玩家；关闭异常不充当提交或终止证明 */
    private void close(SqlSession session, long playerId) {
        try { session.close(); }
        catch (Throwable failure) {
            log.warn("保存会话关闭失败，不能据此推断事务终止 playerId={}", playerId, failure);
        }
    }
}
