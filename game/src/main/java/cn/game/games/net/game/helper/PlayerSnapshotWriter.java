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
import org.springframework.jdbc.datasource.ConnectionHolder;
import java.sql.Connection;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.util.SpringContextLoader;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import cn.game.core.execute.TimingMetrics;

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
    /** 每次操作开始前验证本地预算，测试可注入更短有限值。 */
    private final Supplier<PaymentDatabaseBoundary.Limits> limits;
    /** 从现有 Spring 容器取得同一玩家数据库工厂，首次写入时才访问配置。 */
    PlayerSnapshotWriter() { this(() -> SpringContextLoader.getContext().getBean(SqlSessionFactory.class), io); }
    /** @param factories 会话工厂 @param executor 独立执行器 */
    PlayerSnapshotWriter(Supplier<SqlSessionFactory> factories, Executor executor) {
        this(factories, executor, PaymentDatabaseBoundary.Limits::configured);
    }
    /** @param factories 原工厂 @param executor 实际IO执行器 @param limits 有限预算来源 */
    PlayerSnapshotWriter(Supplier<SqlSessionFactory> factories, Executor executor, Supplier<PaymentDatabaseBoundary.Limits> limits) {
        this.factories = factories; this.executor = executor; this.limits = limits;
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
        /** commit已真实确认后，资源恢复故障不撤销已提交事实。 */
        boolean committed;
    }
    /** @param data 独占参数 @param insert 创建或更新 @return 仅显式事务提交成功才确认 */
    private PlayerSaveCoordinator.WriteResult execute(PlayerData data, boolean insert) {
        Attempt attempt = new Attempt();
        TimingMetrics.Span transactionTiming = TimingMetrics.start(TimingMetrics.Stage.TRANSACTION);
        try {
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                throw new IllegalStateException("专用保存线程不能加入外部事务");
            }
            SqlSessionFactory factory = factories.get();
            var environment = factory.getConfiguration().getEnvironment();
            var transactions = environment.getTransactionFactory();
            int rows;
            try (Connection bounded = PaymentDatabaseBoundary.open(environment.getDataSource(), limits.get())) {
                if (transactions instanceof SpringManagedTransactionFactory) {
                    // SpringManaged 忽略 openSession 的 autoCommit 参数，必须先由同数据源的事务管理器绑定连接。
                    TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(environment.getDataSource()));
                    if (TransactionSynchronizationManager.hasResource(environment.getDataSource())) {
                        throw new IllegalStateException("保存线程已有数据源绑定");
                    }
                    // 预先绑定原数据源身份，事务管理器与原工厂取得同一预算连接；不替换共享工厂的数据源。
                    TransactionSynchronizationManager.bindResource(environment.getDataSource(), new ConnectionHolder(bounded));
                    try {
                        rows = transaction.execute(status -> {
                            SqlSession session = factory.openSession(ExecutorType.SIMPLE, false);
                            try {
                                int affected = invoke(session, data, insert, attempt);
                                if (affected != 1) { status.setRollbackOnly(); }
                                return affected;
                            } finally { close(session, data.getPlayerId()); }
                        });
                        if (rows == 1) { attempt.committed = true; }
                    } finally { TransactionSynchronizationManager.unbindResource(environment.getDataSource()); }
                    // 模板返回时数据库 commit/rollback 已结束，不能在模板回调返回时提前通知成功。
                } else if (transactions instanceof JdbcTransactionFactory) {
                    bounded.setAutoCommit(false);
                    SqlSession session = factory.openSession(bounded);
                    try {
                        rows = invoke(session, data, insert, attempt);
                        if (rows == 1) { session.commit(true); attempt.committed = true; }
                        else { session.rollback(true); }
                    } finally { close(session, data.getPlayerId()); }
                } else {
                    throw new IllegalStateException("玩家保存要求支持显式提交的 JDBC/Spring 事务工厂");
                }
            }
            return rows == 1 ? new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.COMMITTED, null)
                    : new PlayerSaveCoordinator.WriteResult(PlayerSaveCoordinator.Outcome.NOT_WRITTEN,
                            new IllegalStateException("玩家快照影响行数必须为1，实际=" + rows));
        } catch (Throwable failure) {
            log.error("玩家快照边界失败 playerId={} enteredMapper={} committed={}", data.getPlayerId(), attempt.enteredMapper, attempt.committed, failure);
            return new PlayerSaveCoordinator.WriteResult(attempt.committed ? PlayerSaveCoordinator.Outcome.COMMITTED : attempt.enteredMapper
                    ? PlayerSaveCoordinator.Outcome.UNKNOWN : PlayerSaveCoordinator.Outcome.NOT_WRITTEN, failure);
        } finally { transactionTiming.end(attempt.committed ? TimingMetrics.Outcome.SUCCESS : TimingMetrics.Outcome.FAILURE); }
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
        int rows = insert ? mapper.insert(data) : mapper.updateByPrimaryKey(data);
        var completion = PlayerDataSnapshot.durablePayment(data);
        if (rows == 1 && completion != null) {
            try {
                completion.claim().store().insertCompletion(session.getConnection(), factories.get(), completion);
            } catch (java.sql.SQLException failure) {
                log.warn("履约凭证插入失败，玩家聚合事务不得提交 playerId={}", data.getPlayerId(), failure);
                throw new IllegalStateException("PAYMENT_DURABLE_INSERT_FAILED", failure);
            }
        }
        return rows;
    }
    /** @param session 待关闭会话 @param playerId 玩家；关闭异常不充当提交或终止证明 */
    private void close(SqlSession session, long playerId) {
        try { session.close(); }
        catch (Throwable failure) {
            log.warn("保存会话关闭失败，不能据此推断事务终止 playerId={}", playerId, failure);
        }
    }
}
