package cn.game.games.net.game.helper;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.net.data.mapper.PlayerDataMapper;

/** 事务边界测试：真实专用写入器配会话替身，验证显式提交及异常分类，不连接数据库。 */
class PlayerSnapshotWriterTest {
    /** 两条 Mapper 操作均必须经过显式提交，返回前关闭会话。 */
    @Test
    void insertAndUpdateConfirmOnlyAfterExplicitCommit() throws Exception {
        for (boolean insert : List.of(true, false)) {
            SessionFixture f = new SessionFixture();
            var result = new PlayerSnapshotWriter(f::factory, Runnable::run).write(new PlayerData(), insert);
            Assertions.assertEquals(PlayerSaveCoordinator.Outcome.COMMITTED, result.result().outcome());
            Assertions.assertEquals(List.of(insert ? "insert" : "update", "commit", "close"), f.calls);
        }
    }
    /** commit 未返回前真实结果保持 pending，即使调用者等待已超时。 */
    @Test
    void callerTimeoutDoesNotCompleteActualTransaction() throws Exception {
        SessionFixture f = new SessionFixture(); f.commitEntered = new CountDownLatch(1); f.commitRelease = new CountDownLatch(1);
        try (var io = Executors.newVirtualThreadPerTaskExecutor()) {
            var actual = new PlayerSnapshotWriter(f::factory, io).write(new PlayerData(), false);
            Assertions.assertTrue(f.commitEntered.await(5, TimeUnit.SECONDS));
            Assertions.assertThrows(java.util.concurrent.TimeoutException.class,
                    () -> actual.toCompletionStage().toCompletableFuture().get(1, TimeUnit.MILLISECONDS));
            Assertions.assertFalse(actual.isComplete());
            f.commitRelease.countDown();
            Assertions.assertEquals(PlayerSaveCoordinator.Outcome.COMMITTED,
                    actual.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS).outcome());
        } finally { f.commitRelease.countDown(); }
    }
    /** 零行不伪装持久化成功；回滚确认前不认定未写入。 */
    @Test
    void zeroRowsRequireRollbackAndNeverConfirmSaved() {
        SessionFixture f = new SessionFixture(); f.rows = 0;
        var result = new PlayerSnapshotWriter(f::factory, Runnable::run).write(new PlayerData(), false).result();
        Assertions.assertEquals(PlayerSaveCoordinator.Outcome.NOT_WRITTEN, result.outcome());
        Assertions.assertEquals(List.of("update", "rollback", "close"), f.calls);
        SessionFixture rollback = new SessionFixture(); rollback.rows = 0; rollback.failAt = "rollback";
        Assertions.assertEquals(PlayerSaveCoordinator.Outcome.UNKNOWN,
                new PlayerSnapshotWriter(rollback::factory, Runnable::run).write(new PlayerData(), false).result().outcome());
    }
    /** Mapper、提交异常均可能已产生数据库副作用；会话关闭不能将 UNKNOWN 转成安全失败。 */
    @Test
    void mapperAndCommitExceptionsRemainUnknown() {
        for (String point : List.of("update", "commit")) {
            SessionFixture f = new SessionFixture(); f.failAt = point;
            var result = new PlayerSnapshotWriter(f::factory, Runnable::run).write(new PlayerData(), false).result();
            Assertions.assertEquals(PlayerSaveCoordinator.Outcome.UNKNOWN, result.outcome());
            Assertions.assertSame(f.failure, result.cause());
            Assertions.assertEquals("close", f.calls.getLast());
        }
    }
    /** 提交已确认后的关闭异常不撤销确认，后继仍可按真实提交结果推进。 */
    @Test
    void closeFailureDoesNotEraseConfirmedCommit() {
        SessionFixture f = new SessionFixture(); f.failAt = "close";
        Assertions.assertEquals(PlayerSaveCoordinator.Outcome.COMMITTED,
                new PlayerSnapshotWriter(f::factory, Runnable::run).write(new PlayerData(), false).result().outcome());
    }
    /** 未受理、配置失败和自动提交不进入 Mapper，明确未写入。 */
    @Test
    void preExecutionFailuresAreKnownNotWritten() {
        SessionFixture f = new SessionFixture();
        var rejection = new PlayerSnapshotWriter(f::factory, action -> { throw new RejectedExecutionException("closed"); });
        Assertions.assertEquals(PlayerSaveCoordinator.Outcome.NOT_WRITTEN, rejection.write(new PlayerData(), false).result().outcome());
        Assertions.assertTrue(f.calls.isEmpty());
        var badFactory = new PlayerSnapshotWriter(() -> { throw new IllegalStateException("no factory"); }, Runnable::run);
        Assertions.assertEquals(PlayerSaveCoordinator.Outcome.NOT_WRITTEN, badFactory.write(new PlayerData(), false).result().outcome());
        f.refuseAutoCommit = true;
        Assertions.assertEquals(PlayerSaveCoordinator.Outcome.NOT_WRITTEN,
                new PlayerSnapshotWriter(f::factory, Runnable::run).write(new PlayerData(), false).result().outcome());
        Assertions.assertEquals(List.of("close"), f.calls);
    }
    /** 使用真实 MyBatis 会话和 Spring/JDBC 事务工厂，覆盖连接池默认自动提交为 true 的配置。 */
    @Test
    void realFactoriesCommitDefaultAutoCommitConnection() throws Exception {
        for (boolean spring : List.of(true, false)) {
            RealSessionFixture f = new RealSessionFixture(spring);
            var result = new PlayerSnapshotWriter(f::factory, Runnable::run).write(new PlayerData(), false).result();
            Assertions.assertEquals(PlayerSaveCoordinator.Outcome.COMMITTED, result.outcome());
            Assertions.assertTrue(f.calls.indexOf("autoCommit=false") < f.calls.indexOf("sql"));
            Assertions.assertTrue(f.calls.indexOf("sql") < f.calls.indexOf("commit"));
            Assertions.assertTrue(f.calls.indexOf("commit") < f.calls.indexOf("close"));
            Assertions.assertTrue(f.autoCommit, "归还连接时还原自动提交");
        }
    }
    /** 真实 Spring 事务管理器在 commit 抛错时不能报告已确认或未执行。 */
    @Test
    void realSpringCommitFailureIsUnknown() throws Exception {
        RealSessionFixture f = new RealSessionFixture(true); f.failCommit = true;
        var result = new PlayerSnapshotWriter(f::factory, Runnable::run).write(new PlayerData(), false).result();
        Assertions.assertEquals(PlayerSaveCoordinator.Outcome.UNKNOWN, result.outcome());
        Assertions.assertTrue(f.calls.contains("commit"));
    }
    /** 真实 Spring 零行分支只能回滚，不得在返回未写入之前提交。 */
    @Test
    void realSpringZeroRowsRollsBack() throws Exception {
        RealSessionFixture f = new RealSessionFixture(true); f.rows = 0;
        var result = new PlayerSnapshotWriter(f::factory, Runnable::run).write(new PlayerData(), false).result();
        Assertions.assertEquals(PlayerSaveCoordinator.Outcome.NOT_WRITTEN, result.outcome());
        Assertions.assertTrue(f.calls.contains("rollback"));
        Assertions.assertFalse(f.calls.contains("commit"));
    }
    /** 真实配置/会话/映射，仅 JDBC 连接和语句为替身。 */
    private static final class RealSessionFixture {
        /** 是否使用现有 Spring 工厂。 */
        final boolean spring;
        /** 连接池默认值及可控结果。 */
        boolean autoCommit = true, failCommit;
        /** 驱动网络状态。 */
        int network;
        /** 更新影响行数。 */
        int rows = 1;
        /** 数据库边界轨迹。 */
        final List<String> calls = new ArrayList<>();
        /** @param spring 是否使用 SpringManaged 工厂 */
        RealSessionFixture(boolean spring) { this.spring = spring; }
        /** @return 真实工厂，使用生产 Mapper XML */
        SqlSessionFactory factory() {
            Connection connection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[]{Connection.class},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "getNetworkTimeout": return network;
                            case "setNetworkTimeout": network = (int) args[1]; return null;
                            case "getAutoCommit": return autoCommit;
                            case "setAutoCommit": autoCommit = (boolean) args[0]; calls.add("autoCommit=" + autoCommit); return null;
                            case "commit": calls.add("commit"); if (failCommit) { throw new java.sql.SQLException("commit response lost"); } return null;
                            case "rollback": calls.add("rollback"); return null;
                            case "close": calls.add("close"); return null;
                            case "isClosed", "isReadOnly": return false;
                            case "getTransactionIsolation": return Connection.TRANSACTION_READ_COMMITTED;
                            case "getWarnings": return null;
                            case "clearWarnings", "setReadOnly", "setTransactionIsolation": return null;
                            case "toString": return "test connection";
                            case "hashCode": return 1;
                            case "equals": return proxy == args[0];
                            case "prepareStatement":
                                return Proxy.newProxyInstance(java.sql.PreparedStatement.class.getClassLoader(), new Class<?>[]{java.sql.PreparedStatement.class},
                                        (statement, operation, params) -> {
                                            if (operation.getName().startsWith("set") || operation.getName().equals("close")) { return null; }
                                            return switch (operation.getName()) {
                                                case "execute" -> { Assertions.assertFalse(autoCommit); calls.add("sql"); yield false; }
                                                case "getUpdateCount" -> rows;
                                                case "getQueryTimeout" -> 0;
                                                case "getConnection" -> proxy;
                                                case "getMoreResults", "isClosed" -> false;
                                                case "toString" -> "test statement";
                                                default -> throw new AssertionError(operation.getName());
                                            };
                                        });
                            default: throw new AssertionError(method.getName());
                        }
                    });
            DataSource dataSource = (DataSource) Proxy.newProxyInstance(PaymentDatabaseBoundary.Source.class.getClassLoader(), new Class<?>[]{PaymentDatabaseBoundary.Source.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "getConnection" -> connection;
                        case "acquisitionMillis" -> 1;
                        case "mysqlDriver" -> false;
                        case "discard" -> null;
                        case "toString" -> "test datasource";
                        case "hashCode" -> 2;
                        case "equals" -> proxy == args[0];
                        default -> throw new AssertionError(method.getName());
                    });
            Configuration config = new Configuration(new Environment("test", spring
                    ? new org.mybatis.spring.transaction.SpringManagedTransactionFactory() : new JdbcTransactionFactory(), dataSource));
            String resource = "mapping/PlayerDataMapper.xml";
            try (var stream = getClass().getClassLoader().getResourceAsStream(resource)) {
                new org.apache.ibatis.builder.xml.XMLMapperBuilder(stream, config, resource, config.getSqlFragments()).parse();
            } catch (java.io.IOException failure) {
                org.slf4j.LoggerFactory.getLogger(getClass()).error("读取真实 Mapper 失败", failure);
                throw new IllegalStateException(failure);
            }
            return new org.apache.ibatis.session.defaults.DefaultSqlSessionFactory(config);
        }
    }
    /** 仅替换数据库边界；没有测试自己的提交逻辑。 */
    private static final class SessionFixture {
        /** 操作轨迹。 */
        final List<String> calls = new ArrayList<>();
        /** 可控影响行数。 */
        int rows = 1;
        /** 违规自动提交配置。 */
        boolean autoCommit = true, refuseAutoCommit;
        /** 可核对的连接原状态。 */
        int network, isolation = Connection.TRANSACTION_READ_COMMITTED;
        boolean readOnly;
        /** 指定失败阶段。 */
        String failAt;
        /** 固定原因，验证异常身份。 */
        final RuntimeException failure = new IllegalStateException("database connection failure");
        /** commit 闩锁，可控等待发生点。 */
        CountDownLatch commitEntered, commitRelease;
        /** @return 不建立数据库连接的会话工厂 */
        SqlSessionFactory factory() {
            Connection connection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[]{Connection.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "getAutoCommit" -> autoCommit;
                        case "setAutoCommit" -> { if (!refuseAutoCommit) { autoCommit = (boolean) args[0]; } yield null; }
                        case "getNetworkTimeout" -> network;
                        case "setNetworkTimeout" -> { network = (int) args[1]; yield null; }
                        case "getTransactionIsolation" -> isolation;
                        case "setTransactionIsolation" -> { isolation = (int) args[0]; yield null; }
                        case "isReadOnly" -> readOnly;
                        case "setReadOnly" -> { readOnly = (boolean) args[0]; yield null; }
                        case "commit", "rollback", "close" -> null;
                        default -> throw new AssertionError(method.getName());
                    });
            DataSource ds = (DataSource) Proxy.newProxyInstance(PaymentDatabaseBoundary.Source.class.getClassLoader(), new Class<?>[]{PaymentDatabaseBoundary.Source.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "getConnection" -> connection;
                        case "acquisitionMillis" -> 1;
                        case "mysqlDriver" -> false;
                        case "discard" -> null;
                        default -> throw new AssertionError(method.getName());
                    });
            Configuration configuration = new Configuration(new Environment("test", new JdbcTransactionFactory(), ds));
            Connection[] bounded = new Connection[1];
            PlayerDataMapper mapper = (PlayerDataMapper) Proxy.newProxyInstance(PlayerDataMapper.class.getClassLoader(), new Class<?>[]{PlayerDataMapper.class},
                    (proxy, method, args) -> { visit(method.getName().equals("insert") ? "insert" : "update"); return rows; });
            SqlSession session = (SqlSession) Proxy.newProxyInstance(SqlSession.class.getClassLoader(), new Class<?>[]{SqlSession.class},
                    (proxy, method, args) -> {
                        return switch (method.getName()) {
                            case "getConnection" -> bounded[0];
                            case "getMapper" -> mapper;
                            case "commit", "rollback", "close" -> {
                                visit(method.getName());
                                if (method.getName().equals("commit") && commitEntered != null) {
                                    commitEntered.countDown();
                                    Assertions.assertTrue(commitRelease.await(5, TimeUnit.SECONDS));
                                }
                                if (method.getName().equals("commit")) { bounded[0].commit(); }
                                if (method.getName().equals("rollback")) { bounded[0].rollback(); }
                                yield null;
                            }
                            default -> throw new AssertionError(method.getName());
                        };
                    });
            return (SqlSessionFactory) Proxy.newProxyInstance(SqlSessionFactory.class.getClassLoader(), new Class<?>[]{SqlSessionFactory.class},
                    (proxy, method, args) -> {
                        if (method.getName().equals("getConfiguration")) { return configuration; }
                        Assertions.assertEquals("openSession", method.getName());
                        bounded[0] = (Connection) args[0];
                        return session;
                    });
        }
        /** @param operation 记录到达的事务步骤并可控抛错 */
        void visit(String operation) { calls.add(operation); if (operation.equals(failAt)) { throw failure; } }
    }
}
