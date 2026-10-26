package cn.game.login.net.clientpacket.vertx.wechat;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import cn.game.login.cache.entity.PayOrder;
import cn.game.login.mapper.PayOrderMapper;
import cn.game.util.DateUtil;

/** 真实库验证生产合并 SQL；只 INSERT 唯一隔离订单，不创建账号或调用支付平台。 */
class PaymentOrderDatabaseIT {
    /** 并发通知均读取未完成快照；成功后迟到失败与重复成功不得回退状态或重写完成时间。 */
    @Test void concurrentReceiptsAndLateFailurePreserveCompletion() throws Exception {
        for (boolean affectedRows : List.of(false, true)) {
            try (var db = new Fixture(affectedRows)) {
                db.seed();
                CountDownLatch ready = new CountDownLatch(8), start = new CountDownLatch(1);
                try (var workers = Executors.newFixedThreadPool(8)) {
                    var results = new java.util.ArrayList<java.util.concurrent.Future<?>>();
                    for (int index = 0; index < 8; index++) {
                        boolean success = index % 2 == 0;
                        results.add(workers.submit(() -> {
                            PayOrder stale = db.read();
                            ready.countDown();
                            Assertions.assertTrue(start.await(10, TimeUnit.SECONDS));
                            db.record(stale, success);
                            return null;
                        }));
                    }
                    try { Assertions.assertTrue(ready.await(10, TimeUnit.SECONDS)); }
                    finally { start.countDown(); }
                    for (var result : results) { result.get(20, TimeUnit.SECONDS); }
                }
                PayOrder complete = db.read();
                Assertions.assertEquals((byte) 2, complete.getPayState());
                Assertions.assertTrue(complete.getIsDeliver());
                Assertions.assertNotNull(complete.getCompleteDate());
                Assertions.assertNotNull(complete.getCompleteTime());
                // 使用旧快照并故意指定不同时间，验证生产 SQL 不覆盖已提交终态。
                for (boolean success : List.of(false, true)) {
                    PayOrder stale = db.receipt();
                    stale.setPayDate("19990101"); stale.setPayTime("000000");
                    stale.setCompleteDate("19990101"); stale.setCompleteTime("000000");
                    try (var session = db.factory.openSession(true)) {
                        session.getMapper(PayOrderMapper.class).mergePaymentReceipt(stale, success);
                    }
                    PayOrder cold = db.read();
                    Assertions.assertTrue(cold.getIsDeliver());
                    Assertions.assertEquals(complete.getPayState(), cold.getPayState());
                    Assertions.assertEquals(complete.getPayDate(), cold.getPayDate());
                    Assertions.assertEquals(complete.getPayTime(), cold.getPayTime());
                    Assertions.assertEquals(complete.getCompleteDate(), cold.getCompleteDate());
                    Assertions.assertEquals(complete.getCompleteTime(), cold.getCompleteTime());
                    db.record(db.receipt(), success);
                }
            }
        }
    }

    /** 错玩家、金额、平台单号、交易号均不能修改真实订单；失败通知只能登记支付事实。 */
    @Test void conflictingReceiptsCannotClaimOrDowngradeOrder() throws Exception {
        try (var db = new Fixture(true)) {
            db.seed(); db.record(db.receipt(), false);
            Assertions.assertFalse(db.read().getIsDeliver());
            Assertions.assertEquals((byte) 2, db.read().getPayState());
            for (int mode = 0; mode < 6; mode++) {
                PayOrder conflict = db.receipt();
                switch (mode) {
                    case 0 -> conflict.setPlayerId(db.id + 1);
                    case 1 -> conflict.setPrice(1201);
                    case 2 -> conflict.setThirdOrderId("conflicting-sdk-order");
                    case 3 -> conflict.setThirdUid("conflicting-account");
                    case 4 -> conflict.setTransactionId("conflicting-transaction");
                    default -> conflict.setMchOrderNo("conflicting-merchant-order");
                }
                Assertions.assertThrows(IllegalStateException.class, () -> db.record(conflict, true));
                Assertions.assertFalse(db.read().getIsDeliver());
                Assertions.assertEquals(db.marker, db.read().getThirdOrderId());
            }
            db.record(db.receipt(), true);
            db.record(db.receipt(), false);
            Assertions.assertTrue(db.read().getIsDeliver());
        }
    }

    /** 未知支付状态必须保持待核对，不能只更新交付位后向平台宣布完成。 */
    @Test void unknownPaymentStateCannotPublishDelivery() throws Exception {
        try (var db = new Fixture(true)) {
            db.seed((byte) 0);
            Assertions.assertThrows(IllegalStateException.class, () -> db.record(db.receipt(), true));
            Assertions.assertEquals((byte) 0, db.read().getPayState());
            Assertions.assertFalse(db.read().getIsDeliver());
        }
    }

    /** 不同支付流水争抢未绑定订单时只允许一个通过发货前预占，不能都先去 Game。 */
    @Test void concurrentConflictingBindingsPermitOnlyOneReceipt() throws Exception {
        try (var db = new Fixture(true)) {
            db.seed();
            CountDownLatch ready = new CountDownLatch(8), start = new CountDownLatch(1);
            java.util.concurrent.atomic.AtomicInteger accepted = new java.util.concurrent.atomic.AtomicInteger();
            try (var workers = Executors.newFixedThreadPool(8)) {
                var results = new java.util.ArrayList<java.util.concurrent.Future<?>>();
                for (int index = 0; index < 8; index++) {
                    int sequence = index;
                    results.add(workers.submit(() -> {
                        PayOrder row = db.receipt(); row.setTransactionId(db.marker + sequence); row.setMchOrderNo(db.marker + sequence);
                        ready.countDown(); Assertions.assertTrue(start.await(10, TimeUnit.SECONDS));
                        try { db.record(row, false); accepted.incrementAndGet(); }
                        catch (IllegalStateException conflict) {
                            org.slf4j.LoggerFactory.getLogger(PaymentOrderDatabaseIT.class).warn("预期的并发流水冲突，拒绝发货", conflict);
                        }
                        return null;
                    }));
                }
                try { Assertions.assertTrue(ready.await(10, TimeUnit.SECONDS)); } finally { start.countDown(); }
                for (var result : results) { result.get(20, TimeUnit.SECONDS); }
            }
            Assertions.assertEquals(1, accepted.get());
            PayOrder stored = db.read(); Assertions.assertFalse(stored.getIsDeliver());
            db.record(stored, true); Assertions.assertTrue(db.read().getIsDeliver());
        }
    }

    /** 私有数据源和唯一主键；检查现有表结构后写入，按主键、玩家与标识精确清理。 */
    private static final class Fixture implements AutoCloseable {
        /** 专用测试主键，不复用任何业务行。 */
        final long id = (UUID.randomUUID().getMostSignificantBits() & 0x1fffffffffffffffL) | 0x4000000000000000L;
        /** 清理时核对本轮所有权。 */
        final String marker = "s3a_" + UUID.randomUUID().toString().replace("-", "");
        /** 每次操作创建新会话，避免缓存冒充数据库冷读。 */
        final SqlSessionFactory factory;
        /** INSERT 成功后才获得删除权限，冲突不清理已有行。 */
        boolean inserted;

        /** @param affectedRows 是否让相同值 UPDATE 返回零，覆盖 MySQL 两种影响行数配置 */
        Fixture(boolean affectedRows) throws Exception {
            String config = System.getenv("S2_DB_CONFIG");
            Assertions.assertNotNull(config, "必须显式提供 S2_DB_CONFIG，不能跳过真实库验收");
            Properties properties = new Properties();
            try (InputStream input = Files.newInputStream(Path.of(config))) { properties.load(input); }
            var source = new DriverManagerDataSource();
            source.setDriverClassName(properties.getProperty("driverClassName"));
            source.setUrl(properties.getProperty("dburl"));
            source.setUsername(properties.getProperty("dbuser")); source.setPassword(properties.getProperty("dbpass"));
            Properties limits = new Properties();
            limits.setProperty("connectTimeout", "3000"); limits.setProperty("socketTimeout", "20000");
            limits.setProperty("autoReconnect", "false"); limits.setProperty("useAffectedRows", String.valueOf(affectedRows));
            source.setConnectionProperties(limits);
            // 表结构只读检查；不迁移表，也不查询业务行。
            try (Connection connection = source.getConnection()) {
                var columns = new HashSet<String>();
                try (var rows = connection.getMetaData().getColumns(connection.getCatalog(), null, "t_pay_order", null)) {
                    while (rows.next()) { columns.add(rows.getString("COLUMN_NAME").toLowerCase(java.util.Locale.ROOT)); }
                }
                Assertions.assertTrue(columns.containsAll(List.of("id", "player_id", "price", "third_order_id",
                        "transaction_id", "pay_state", "is_deliver", "debug_text", "complete_date", "complete_time")),
                        "现有订单表必须包含生产合并与隔离清理所需字段");
            }
            var mapping = new Configuration(new Environment("s3a-login-fixture", new JdbcTransactionFactory(), source));
            try (InputStream xml = getClass().getResourceAsStream("/mapping/PayOrderMapper.xml")) {
                new XMLMapperBuilder(xml, mapping, "mapping/PayOrderMapper.xml", mapping.getSqlFragments()).parse();
            }
            factory = new SqlSessionFactoryBuilder().build(mapping);
        }

        /** 仅 INSERT 本例订单；不存在业务账号、玩家或外部订单关联。 */
        void seed() {
            seed((byte) 1);
        }

        /** @param state 本例初始支付状态；用于未知状态的保守处理验证 */
        void seed(byte state) {
            PayOrder row = receipt(); row.setThirdOrderId(null); row.setTransactionId(null); row.setMchOrderNo(null);
            row.setPayState(state);
            try (var session = factory.openSession(true)) {
                Assertions.assertEquals(1, session.getMapper(PayOrderMapper.class).insert(row)); inserted = true;
            }
        }

        /** @return 带本轮身份的旧订单快照，不从真实业务订单复制数据 */
        PayOrder receipt() {
            var row = new PayOrder(); row.setId(id); row.setPlayerId(id); row.setUserId(id);
            row.setGameServerId("s3a-test"); row.setChannelCode("s3a-test"); row.setChannelLabel("s3a-test");
            row.setThirdUid(marker); row.setEnv((byte) 1); row.setPayState((byte) 1); row.setIsDeliver(false);
            row.setCreateDate(DateUtil.nowDateStr()); row.setCreateTime(DateUtil.nowTimeStr());
            row.setDebugText(marker); row.setPrice(1200); row.setItemName("s3a-test");
            row.setThirdOrderId(marker); row.setTransactionId(marker); row.setMchOrderNo(marker);
            row.setCallback("{\"fixture\":\"" + marker + "\"}");
            return row;
        }

        /** @return 独立会话读取的本轮订单 */
        PayOrder read() {
            try (var session = factory.openSession(true)) { return session.getMapper(PayOrderMapper.class).selectByPrimaryKey(id); }
        }

        /** @param row 原回执快照 @param success 只有 Game 提交后才可传 true */
        void record(PayOrder row, boolean success) {
            try (var session = factory.openSession(true)) { PaymentOrderState.record(session.getMapper(PayOrderMapper.class), row, success); }
        }

        @Override public void close() throws Exception {
            if (!inserted) { return; }
            try (var connection = factory.getConfiguration().getEnvironment().getDataSource().getConnection()) {
                connection.setAutoCommit(true);
                try (var statement = connection.prepareStatement("delete from t_pay_order where id=? and player_id=? and debug_text=? and third_uid=?")) {
                    statement.setLong(1, id); statement.setLong(2, id); statement.setString(3, marker); statement.setString(4, marker);
                    Assertions.assertEquals(1, statement.executeUpdate(), "只清理当前 fixture 的唯一订单");
                }
                Assertions.assertNull(read(), "fixture 必须清理完毕");
            }
        }
    }
}
