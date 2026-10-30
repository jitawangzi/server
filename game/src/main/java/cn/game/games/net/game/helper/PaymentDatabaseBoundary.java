package cn.game.games.net.game.helper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import com.zaxxer.hikari.HikariDataSource;
import cn.game.core.execute.TimingMetrics;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * 支付及玩家保存的连接级预算。同步执行实际 JDBC 调用，不用取消等待者伪装数据库终止。
 * 只接受已知有限获取路径；不改共享池、工厂或 URL。失败连接直接废弃，恢复核对通过才可归还。
 * 网络预算约束驱动等待，不是服务端事务终止证明；调度、DNS、驱动缺陷及操作系统停顿不具备硬实时保证。
 */
public final class PaymentDatabaseBoundary {
    /** 失败与废弃日志，不输出连接配置。 */
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PaymentDatabaseBoundary.class);

    /** 毫秒预算；获取连接兼容当前 Game Hikari 的60000毫秒，剩余总预算包含结构检查及清理。 */
    public record Limits(int acquire, int network, int query, int total) {
        /** 所有预算必须有限，总预算必须给获取之后的执行留下余量。 */
        public Limits {
            if (acquire < 1 || network < 1 || query < 1 || total <= acquire || total > 300000
                    || network > total || query > total) { throw new IllegalArgumentException("PAYMENT_DB_BUDGET_INVALID"); }
        }
        /** @return 本地进程参数；不读取或修改 Apollo，非法配置拒绝写入 */
        public static Limits configured() {
            return new Limits(value("acquire", 65000), value("network", 5000), value("query", 5000), value("total", 75000));
        }
        /** @param name 固定参数后缀 @param fallback 默认毫秒 @return 严格正整数配置 */
        private static int value(String name, int fallback) {
            String raw = System.getProperty("payment.db." + name + "Millis");
            if (raw == null) { return fallback; }
            if (!raw.matches("[1-9][0-9]{0,8}")) { throw new IllegalArgumentException("PAYMENT_DB_BUDGET_INVALID:" + name); }
            return Integer.parseInt(raw);
        }
    }

    /** 独立适配器必须证明获取上限及废弃语义；当前只由隔离故障测试使用，不自动信任路由数据源。 */
    public interface Source extends DataSource {
        /** @return 获取连接的已验证最大毫秒，不包含任何调用方 Future 包装 */
        int acquisitionMillis();
        /** @param connection 本适配器发出的连接；必须使其永久不可复用 @throws SQLException 废弃失败 */
        void discard(Connection connection) throws SQLException;
        /** @return 是否为实际Connector/J连接；纯内存故障替身显式不访问驱动属性 */
        default boolean mysqlDriver() { return false; }
    }

    /** 工具类不创建实例。 */
    private PaymentDatabaseBoundary() {}

    /** @param source 原工厂数据源 @param limits 本次不可变预算 @return 独占、自动恢复或废弃的连接 @throws SQLException 不支持或获取失败 */
    public static Connection open(DataSource source, Limits limits) throws SQLException {
        return open(source, limits, System.nanoTime());
    }

    /** @param source 原数据源 @param limits 预算 @param started 整条诊断起点，不在每次查询时重置 @return 预算连接 @throws SQLException 超期或不支持 */
    public static Connection open(DataSource source, Limits limits, long started) throws SQLException {
        if (System.nanoTime() - started >= TimeUnit.MILLISECONDS.toNanos(limits.total() - limits.acquire())) {
            throw new SQLTimeoutException("PAYMENT_DB_INSUFFICIENT_ACQUISITION_BUDGET");
        }
        long acquire;
        if (source.getClass() == HikariDataSource.class) {
            HikariDataSource pool = (HikariDataSource) source;
            // 未启动池首次初始化可同步重试建连，不能把 connectionTimeout 当作初始化上限。
            if (pool.getHikariPoolMXBean() == null) { throw new SQLException("PAYMENT_DB_POOL_NOT_STARTED"); }
            // Hikari在借用超时外还可能等待一次存活校验，必须把validationTimeout纳入有限获取预算。
            acquire = pool.getConnectionTimeout() + pool.getValidationTimeout();
        } else if (source.getClass() == DriverManagerDataSource.class) {
            DriverManagerDataSource direct = (DriverManagerDataSource) source;
            validateUrl(direct.getUrl(), direct.getConnectionProperties(), limits.acquire());
            acquire = limits.acquire();
        } else if (source instanceof Source supported) { acquire = supported.acquisitionMillis(); }
        else { throw new SQLException("PAYMENT_DB_UNSUPPORTED_DATASOURCE:finite acquisition adapter required"); }
        if (acquire < 1 || acquire > limits.acquire()) { throw new SQLException("PAYMENT_DB_ACQUIRE_EXCEEDS_BUDGET"); }
        TimingMetrics.Span acquisition = TimingMetrics.start(TimingMetrics.Stage.CONNECTION);
        Connection connection;
        boolean acquired = false;
        try { connection = source.getConnection(); acquired = true; }
        finally { acquisition.end(acquired ? TimingMetrics.Outcome.SUCCESS : TimingMetrics.Outcome.FAILURE); }
        try {
            if (source instanceof HikariDataSource) { connection.unwrap(com.mysql.cj.jdbc.JdbcConnection.class); }
            return new Lease(connection, source, limits, started).proxy();
        }
        catch (Throwable failure) {
            log.warn("数据库连接边界不受支持，禁止执行SQL", failure);
            discard(source, connection);
            if (failure instanceof SQLException sql) { throw sql; }
            throw new SQLException("PAYMENT_DB_UNSUPPORTED_CONNECTION", failure);
        }
    }

    /**
     * 用驱动实际解析结果验证优先级，不改 URL，不覆盖显式非法配置。
     * @param url 原始单主机 MySQL URL @param properties 提供给驱动的私有属性 @param maximum 最大网络毫秒
     * @throws SQLException 未配置有限等待、URL覆盖或扩展套接字/重连路径不受支持
     */
    public static void validateUrl(String url, Properties properties, int maximum) throws SQLException {
        if (url == null || !url.startsWith("jdbc:mysql://")) { throw new SQLException("PAYMENT_DB_MYSQL_SINGLE_HOST_REQUIRED"); }
        try {
            var explicit = com.mysql.cj.conf.ConnectionUrl.getConnectionUrlInstance(url, new Properties()).getMainHost().getHostProperties();
            var parsed = com.mysql.cj.conf.ConnectionUrl.getConnectionUrlInstance(url, properties == null ? new Properties() : properties);
            var values = parsed.getMainHost().getHostProperties();
            // 本地8.0.26中Properties优先于URL；仍单独拒绝URL显式无限或超预算值，不靠覆盖掩盖它。
            for (String key : new String[]{"connectTimeout", "socketTimeout"}) {
                String value = explicit.get(key);
                if (value != null && (!value.matches("[1-9][0-9]{0,8}") || Integer.parseInt(value) > maximum)) {
                    throw new SQLException("PAYMENT_DB_EXPLICIT_URL_TIMEOUT_INVALID:" + key);
                }
            }
            if (parsed.getHostsList().size() != 1) { throw new SQLException("PAYMENT_DB_MYSQL_SINGLE_HOST_REQUIRED"); }
            for (String key : new String[]{"connectTimeout", "socketTimeout"}) {
                String value = values.get(key);
                if (value == null || !value.matches("[1-9][0-9]{0,8}") || Integer.parseInt(value) > maximum) {
                    throw new SQLException("PAYMENT_DB_FINITE_URL_TIMEOUT_REQUIRED:" + key);
                }
            }
            for (String key : new String[]{"autoReconnect", "autoReconnectForPools", "reconnectAtTxEnd"}) {
                if (!"false".equalsIgnoreCase(values.getOrDefault(key, "false"))) { throw new SQLException("PAYMENT_DB_RECONNECT_UNSUPPORTED"); }
            }
            for (String key : new String[]{"socketFactory", "propertiesTransform", "useConfigs", "connectionLifecycleInterceptors", "queryInterceptors", "socksProxyHost"}) {
                if (values.containsKey(key)) { throw new SQLException("PAYMENT_DB_CUSTOM_CONNECT_PATH_UNSUPPORTED"); }
            }
        } catch (RuntimeException failure) {
            log.warn("数据库URL预算无法验证，拒绝连接", failure);
            throw new SQLException("PAYMENT_DB_URL_BUDGET_INVALID");
        }
    }

    /** @param source 原数据源 @param c 不得复用的连接 @throws SQLException 无法证明废弃 */
    private static void discard(DataSource source, Connection c) throws SQLException {
        if (source instanceof Source supported) { supported.discard(c); }
        else if (source instanceof HikariDataSource pool) {
            if (!c.isClosed()) {
                var physical = c.unwrap(com.mysql.cj.jdbc.JdbcConnection.class);
                // 在物理关闭前从池摘除；摘除后不能再close代理触发Hikari对空PoolEntry的状态恢复。
                pool.evictConnection(c);
                physical.abortInternal();
            } else { pool.evictConnection(c); }
        } else {
            // Connector/J abortInternal 本地强制关闭，不执行可能提交事务的自动提交恢复。
            c.unwrap(com.mysql.cj.jdbc.JdbcConnection.class).abortInternal();
            c.close();
        }
    }

    /** 每个连接只携带一个单调截止点；所有语句和提交消耗同一预算。 */
    private static final class Lease {
        /** 原连接、来源及不可变预算。 */
        final Connection raw;
        final DataSource source;
        final Limits limits;
        /** 总截止点及归还时必须恢复的原始状态。 */
        final long deadline;
        final int network, isolation;
        final boolean autoCommit, readOnly;
        /** 连接私有查询取消策略，避免超时另起可能无限建连的KILL QUERY连接。 */
        final com.mysql.cj.conf.RuntimeProperty<Boolean> killOnTimeout;
        final Boolean oldKillOnTimeout;
        /** 任意SQL异常、超期或未结束事务均禁止归还。 */
        boolean unsafe, closed, transaction;
        /** @param raw 原连接 @param source 原数据源 @param limits 毫秒预算 @param started 获取之前起点 @throws SQLException 不支持网络预算 */
        Lease(Connection raw, DataSource source, Limits limits, long started) throws SQLException {
            this.raw = raw; this.source = source; this.limits = limits;
            deadline = started + TimeUnit.MILLISECONDS.toNanos(limits.total());
            // Connector/J的只读/隔离状态读取可能查询服务端，先安装网络预算再读取原状态。
            network = raw.getNetworkTimeout(); arm();
            autoCommit = raw.getAutoCommit(); readOnly = raw.isReadOnly(); isolation = raw.getTransactionIsolation();
            if (!autoCommit) { throw new SQLException("PAYMENT_DB_PREEXISTING_TRANSACTION_UNSUPPORTED"); }
            if (!(source instanceof Source adapter) || adapter.mysqlDriver()) {
                var properties = raw.unwrap(com.mysql.cj.jdbc.JdbcConnection.class).getPropertySet();
                if (!properties.getBooleanProperty(com.mysql.cj.conf.PropertyKey.enableQueryTimeouts).getValue()) {
                    throw new SQLException("PAYMENT_DB_QUERY_TIMEOUT_DISABLED");
                }
                killOnTimeout = properties.getBooleanProperty(com.mysql.cj.conf.PropertyKey.queryTimeoutKillsConnection);
                oldKillOnTimeout = killOnTimeout.getValue(); killOnTimeout.setValue(true);
                if (!killOnTimeout.getValue()) { throw new SQLException("PAYMENT_DB_QUERY_TIMEOUT_UNSUPPORTED"); }
            } else { killOnTimeout = null; oldKillOnTimeout = null; }
            arm();
        }
        /** @return 始终至少1毫秒；截止后不再启动任何新命令 @throws SQLTimeoutException 总预算耗尽 */
        int remaining() throws SQLTimeoutException {
            long nanos = deadline - System.nanoTime();
            if (nanos <= 0) { unsafe = true; throw new SQLTimeoutException("PAYMENT_DB_TOTAL_EXPIRED"); }
            return (int) Math.max(1, TimeUnit.NANOSECONDS.toMillis(nanos));
        }
        /** @throws SQLException 驱动必须实际支持并保留网络超时设置 */
        void arm() throws SQLException {
            int timeout = Math.min(limits.network(), remaining());
            raw.setNetworkTimeout(Runnable::run, timeout);
            if (raw.getNetworkTimeout() != timeout) { throw new SQLException("PAYMENT_DB_NETWORK_TIMEOUT_UNSUPPORTED"); }
        }
        /** @return 包装原连接，close 只在此处恢复或废弃，不将底层池连接泄露给事务框架 */
        Connection proxy() {
            return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[]{Connection.class}, (p, m, a) -> {
                String name = m.getName();
                if (name.equals("close")) { finish(); return null; }
                if (name.equals("equals")) { return p == a[0]; }
                if (name.equals("hashCode")) { return System.identityHashCode(p); }
                if (name.equals("toString")) { return "payment-budget-connection"; }
                if (name.equals("unwrap")) { if (((Class<?>) a[0]).isInstance(p)) { return p; } throw new SQLException("PAYMENT_DB_UNWRAP_FORBIDDEN"); }
                if (name.equals("isWrapperFor")) { return ((Class<?>) a[0]).isInstance(p); }
                if (closed) { throw new SQLException("PAYMENT_DB_CONNECTION_CLOSED"); }
                try {
                    if (unsafe && (name.equals("commit") || name.startsWith("prepare") || name.equals("createStatement")
                            || (name.equals("setAutoCommit") && Boolean.TRUE.equals(a[0]) && transaction))) {
                        throw new SQLException("PAYMENT_DB_FAILED_CONNECTION_NOT_REUSABLE");
                    }
                    if (name.equals("commit") || name.equals("rollback") || name.equals("setAutoCommit")
                            || name.equals("setReadOnly") || name.equals("setTransactionIsolation") || name.equals("isReadOnly")
                            || name.equals("getTransactionIsolation") || name.equals("getAutoCommit") || name.startsWith("prepare") || name.equals("createStatement")) { arm(); }
                    if (name.equals("setAutoCommit") && Boolean.FALSE.equals(a[0])) { transaction = true; }
                    TimingMetrics.Span commit = name.equals("commit") || name.equals("rollback")
                            ? TimingMetrics.start(TimingMetrics.Stage.SQL) : null;
                    Object value;
                    boolean success = false;
                    try { value = m.invoke(raw, a); success = true; }
                    finally { if (commit != null) { commit.end(success ? TimingMetrics.Outcome.SUCCESS : TimingMetrics.Outcome.FAILURE); } }
                    if (name.equals("commit") || name.equals("rollback")) { transaction = false; remaining(); }
                    if (value instanceof Statement statement) { return statement(statement, (Connection) p); }
                    return value;
                } catch (InvocationTargetException failure) {
                    unsafe = true; log.warn("数据库调用未确认，连接将废弃 method={}", name, failure.getCause()); throw failure.getCause();
                }
            });
        }
        /** @param rawStatement 原语句 @param connection 同一包装连接 @return 执行时设置剩余网络及查询预算的语句 */
        Object statement(Statement rawStatement, Connection connection) {
            Class<?> type = rawStatement instanceof java.sql.CallableStatement ? java.sql.CallableStatement.class
                    : rawStatement instanceof java.sql.PreparedStatement ? java.sql.PreparedStatement.class : Statement.class;
            return Proxy.newProxyInstance(Statement.class.getClassLoader(), new Class<?>[]{type}, (p, m, a) -> {
                if (m.getName().equals("getConnection")) { return connection; }
                try {
                    if (m.getName().startsWith("execute")) {
                        arm();
                        int seconds = Math.max(1, (Math.min(limits.query(), remaining()) + 999) / 1000);
                        int existing = rawStatement.getQueryTimeout();
                        // 保留schema等调用方已经指定的更短期限，统一预算只能收紧，不能放宽。
                        rawStatement.setQueryTimeout(existing > 0 ? Math.min(existing, seconds) : seconds);
                    }
                    TimingMetrics.Span sql = m.getName().startsWith("execute") ? TimingMetrics.start(TimingMetrics.Stage.SQL) : null;
                    Object value;
                    boolean success = false;
                    try { value = m.invoke(rawStatement, a); success = true; }
                    finally { if (sql != null) { sql.end(success ? TimingMetrics.Outcome.SUCCESS : TimingMetrics.Outcome.FAILURE); } }
                    if (m.getName().startsWith("execute")) { remaining(); }
                    if (value instanceof java.sql.ResultSet result) { return resultSet(result); }
                    return value;
                } catch (InvocationTargetException failure) {
                    unsafe = true; log.warn("数据库语句未确认，连接将废弃", failure.getCause()); throw failure.getCause();
                }
            });
        }
        /** @param rows 原结果集 @return 每次推进结果行也消耗同一总预算的包装 */
        Object resultSet(java.sql.ResultSet rows) {
            return Proxy.newProxyInstance(java.sql.ResultSet.class.getClassLoader(), new Class<?>[]{java.sql.ResultSet.class}, (p, m, a) -> {
                try {
                    if (m.getName().equals("next") || m.getName().startsWith("get")) { arm(); }
                    return m.invoke(rows, a);
                } catch (InvocationTargetException failure) {
                    unsafe = true; log.warn("数据库结果读取未确认", failure.getCause()); throw failure.getCause();
                }
            });
        }
        /** @throws SQLException 恢复不能核对时废弃且报告失败，不归还带改动状态的连接 */
        void finish() throws SQLException {
            if (closed) { return; }
            closed = true;
            try {
                if (unsafe || transaction) { discard(source, raw); return; }
                arm();
                raw.setReadOnly(readOnly); raw.setTransactionIsolation(isolation); raw.setAutoCommit(autoCommit);
                if (killOnTimeout != null) {
                    killOnTimeout.setValue(oldKillOnTimeout);
                    if (!oldKillOnTimeout.equals(killOnTimeout.getValue())) { throw new SQLException("PAYMENT_DB_QUERY_POLICY_RESTORE_NOT_PROVEN"); }
                }
                if (raw.getAutoCommit() != autoCommit || raw.isReadOnly() != readOnly || raw.getTransactionIsolation() != isolation) {
                    throw new SQLException("PAYMENT_DB_RESTORE_NOT_PROVEN");
                }
                // 服务端状态核对仍使用有限网络预算；最后才还原可能为0的原socket设置。
                raw.setNetworkTimeout(Runnable::run, network);
                if (raw.getNetworkTimeout() != network) { throw new SQLException("PAYMENT_DB_NETWORK_RESTORE_NOT_PROVEN"); }
                if (source.getClass() == DriverManagerDataSource.class) { discard(source, raw); }
                else { raw.close(); }
            } catch (SQLException | RuntimeException failure) {
                log.warn("数据库连接恢复失败，禁止回池", failure);
                try { discard(source, raw); }
                catch (SQLException discardFailure) { log.error("数据库连接废弃失败，需人工处置", discardFailure); failure.addSuppressed(discardFailure); }
                throw failure;
            }
        }
    }
}
