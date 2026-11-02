package cn.game.games.net.game.helper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import cn.game.games.diagnostics.PaymentEnablementCli;
import cn.game.games.net.game.module.recharge.PayItem;

/**
 * 真实隔离表DDL变体必须在claim/业务变更前拒绝；生产新表名不执行DDL。
 * 每例仅占用随机玩家和带comment的随机表，退出等待写线程并以新连接验证精确清理。
 */
@Isolated
class PaymentSchemaDatabaseIT {
    /** 仅固定归属标记和冷查结果进入私有日志。 */
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PaymentSchemaDatabaseIT.class);

    /** 装配真实发货Handler所需的模块元数据。 */
    @BeforeAll static void metadata() throws Exception { PaymentDeliveryLifecycleTest.metadata(); }
    /** 恢复被测试设施临时替换的依赖。 */
    @AfterAll static void restore() throws Exception { PaymentDeliveryLifecycleTest.restore(); }

    /** 缺失字段、错误整数类型/有符号性都不能让奖励开始，包括receipt才使用的字段。 */
    @Test void missingAndWrongSignedIntegerColumnsRejectBeforeClaimAndReward() throws Exception {
        try (var f = new Fixture()) {
            f.open();
            for (String ddl : List.of("receipt DROP COLUMN proof", "claim DROP COLUMN player_id", "receipt DROP COLUMN version",
                    "claim MODIFY order_id INT NOT NULL", "receipt MODIFY player_id BIGINT UNSIGNED NOT NULL",
                    "claim MODIFY player_id BIGINT UNSIGNED NOT NULL", "receipt MODIFY version SMALLINT NOT NULL",
                    "receipt MODIFY version INT UNSIGNED NOT NULL")) { f.variant(ddl); }
        }
    }

    /** 精确SDK长度、大小写比较、token/hash字符集长度及正文容量不能默默漂移。 */
    @Test void stringLengthCharsetCollationAndTextCapacityRejectBeforeMutation() throws Exception {
        try (var f = new Fixture()) {
            f.open();
            for (String ddl : List.of(
                    "claim MODIFY sdk_order_id VARCHAR(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL",
                    "receipt MODIFY sdk_order_id VARCHAR(192) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL",
                    "receipt MODIFY sdk_order_id VARCHAR(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL",
                    "claim MODIFY sdk_order_id VARCHAR(191) CHARACTER SET ascii COLLATE ascii_bin NOT NULL",
                    "claim MODIFY token CHAR(35) CHARACTER SET ascii COLLATE ascii_bin NOT NULL",
                    "receipt MODIFY token CHAR(36) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL",
                    "receipt MODIFY proof_sha256 CHAR(63) CHARACTER SET ascii COLLATE ascii_bin NOT NULL",
                    "receipt MODIFY proof_sha256 CHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL",
                    "claim MODIFY intent TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL",
                    "receipt MODIFY proof TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL")) { f.variant(ddl); }
        }
    }

    /** 可空、默认值、必填额外列及生成列不属于V1契约，不能依赖SQL mode或晚期失败兜底。 */
    @Test void nullabilityDefaultsExtrasAndGeneratedColumnsRejectBeforeMutation() throws Exception {
        try (var f = new Fixture()) {
            f.open();
            for (String ddl : List.of("receipt MODIFY version INT NULL", "claim MODIFY player_id BIGINT NOT NULL DEFAULT 0",
                    "receipt MODIFY token CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'private-value'",
                    "receipt ADD required_extra INT NOT NULL", "claim ADD optional_extra INT NULL",
                    "receipt ADD computed_extra BIGINT GENERATED ALWAYS AS (player_id+1) STORED",
                    "receipt MODIFY version INT GENERATED ALWAYS AS (1) STORED")) { f.variant(ddl); }
        }
    }

    /** 复合/前缀SDK唯一键不能当独立完整键，额外唯一键也不能让合法新单在奖后失败。 */
    @Test void compositePrefixMissingAndAdditionalUniqueConstraintsReject() throws Exception {
        try (var f = new Fixture()) {
            f.open();
            for (String ddl : List.of("claim DROP INDEX uk_payment_claim_sdk",
                    "receipt DROP INDEX uk_payment_receipt_sdk, ADD UNIQUE KEY altered_sdk (sdk_order_id,player_id)",
                    "claim DROP INDEX uk_payment_claim_sdk, ADD UNIQUE KEY altered_sdk (sdk_order_id(190))",
                    "receipt ADD UNIQUE KEY extra_player (player_id)",
                    "claim DROP PRIMARY KEY, ADD PRIMARY KEY (order_id,player_id)")) { f.variant(ddl); }
        }
    }

    /** 玩家表事务性用随机影子表验证，不对实际t_player_data执行ALTER；新表非事务引擎也拒绝。 */
    @Test void allThreeTablesRequireTransactionalBaseTables() throws Exception {
        try (var f = new Fixture()) {
            f.open(); f.createShadow("MyISAM");
            try (var c = f.db.source.getConnection()) {
                Assertions.assertTrue(PaymentDedupSchema.inspect(c, f.shadow, f.claim, f.receipt).blockers().contains("player:REQUIRE_INNODB_BASE_TABLE"));
            }
            f.variant("claim ENGINE=MyISAM"); f.variant("receipt ENGINE=MyISAM");
        }
    }

    /** 精确迁移结构允许真实原子提交和冷读，SDK大小写和191字符完整唯一键均按声明生效。 */
    @Test void correctSchemaCommitsAndReadonlyReportDoesNotAuthorizeEnablement() throws Exception {
        try (var f = new Fixture()) {
            f.open(); String signature = f.inspect().signature(); Assertions.assertTrue(f.inspect().valid());
            var before = f.db.read().getModules();
            var report = PaymentEnablementCli.run(new String[0], f::inspect);
            Assertions.assertEquals(true, report.get("localSchemaReady")); Assertions.assertEquals(false, report.get("canEnable"));
            Assertions.assertEquals(2, PaymentEnablementCli.exitCode(report)); Assertions.assertEquals(signature, report.get("schemaSignature"));
            Assertions.assertEquals(before, f.db.read().getModules()); f.assertEmpty();
            f.standalone(); Assertions.assertEquals(before, f.db.read().getModules()); f.assertEmpty();
            try (var shipment = f.shipment()) {
                shipment.ship(); shipment.captured(); shipment.flush();
                Assertions.assertEquals(List.of(true), shipment.replies); Assertions.assertEquals(1, shipment.awards.get());
                Assertions.assertEquals(1, shipment.charges.get()); Assertions.assertEquals(1, f.count(f.receipt));
                Assertions.assertEquals(1, f.db.reloadFresh().getPlayerModule().getGuideMap().get(700));
            }
            for (int i = 0; i < 2; i++) {
                PayItem item = PaymentDurableDedupTest.item(); item.setOrderId(f.db.id + i + 1);
                String sdk = "x".repeat(190) + (i == 0 ? "A" : "a");
                f.store.claim(PaymentDedupStore.intent(f.db.id, item, sdk));
            }
            Assertions.assertEquals(3, f.count(f.claim)); Assertions.assertEquals(signature, f.inspect().signature());
        }
    }

    /** 已通过的观察不缓存：先有效lookup，再修改receipt字段，claim自身必须再次检查且不写入。 */
    @Test void schemaChangeAfterLookupIsRecheckedBeforeClaimWithoutCache() throws Exception {
        try (var f = new Fixture()) {
            f.open(); Assertions.assertEquals(PaymentDedupStore.Lookup.ABSENT, f.store.lookup(f.db.id, f.db.id, "sdk-91", null));
            String before = f.inspect().signature(); f.execute("ALTER TABLE " + f.receipt + " DROP COLUMN proof");
            Assertions.assertNotEquals(before, f.inspect().signature());
            PayItem item = PaymentDurableDedupTest.item(); item.setOrderId(f.db.id);
            Assertions.assertThrows(java.sql.SQLException.class, () -> f.store.claim(PaymentDedupStore.intent(f.db.id, item, "sdk-91")));
            f.reject();
        }
    }

    /** 显示宽度、显式列顺序及非唯一索引不改变写入契约，SDK索引名称也不参与业务身份。 */
    @Test void declaredSafeMetadataVariationsStillCommit() throws Exception {
        try (var f = new Fixture()) {
            f.open();
            f.execute("ALTER TABLE " + f.claim + " MODIFY order_id BIGINT(20) NOT NULL, ADD INDEX ordinary_player (player_id)");
            f.execute("ALTER TABLE " + f.receipt + " MODIFY version INT(11) NOT NULL, MODIFY token CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL FIRST, RENAME INDEX uk_payment_receipt_sdk TO renamed_sdk");
            Assertions.assertTrue(f.inspect().valid(), f.inspect().blockers().toString());
            try (var shipment = f.shipment()) {
                shipment.ship(); shipment.captured(); shipment.flush();
                Assertions.assertEquals(List.of(true), shipment.replies); Assertions.assertEquals(1, f.count(f.receipt));
            }
        }
    }

    /** 结构中的额外外键或触发器可能改变INSERT语义，预检不得仅凭列和原有唯一键放行。 */
    @Test void foreignKeyAndTriggerWriteRestrictionsAreRejected() throws Exception {
        try (var f = new Fixture()) {
            f.open(); f.createShadow("InnoDB");
            f.execute("ALTER TABLE " + f.receipt + " ADD CONSTRAINT fk_owned_player FOREIGN KEY (player_id) REFERENCES " + f.shadow + "(id)");
            f.reject(); f.resetTables();
            f.execute("CREATE TRIGGER schema_" + f.suffix + " BEFORE INSERT ON " + f.receipt + " FOR EACH ROW SET NEW.version=2");
            Assertions.assertTrue(f.inspect().blockers().contains("receipt:TRIGGER_UNSUPPORTED")); f.reject();
        }
    }

    /** 归属集合及持久comment同时确认后才允许精确DROP，异常也保留可诊断清理证据。 */
    private static final class Fixture implements AutoCloseable {
        /** 真实Game玩家、Mapper和生产事务写入器。 */
        final PlayerSaveDatabaseIT.DatabaseFixture db = new PlayerSaveDatabaseIT.DatabaseFixture(false);
        /** 本例的受控随机表名，与生产新表名完全隔离。 */
        final String suffix = UUID.randomUUID().toString().replace("-", "");
        /** 两张迁移副本及仅验证玩家表引擎的影子表。 */
        final String claim = "s3d_claim_" + suffix, receipt = "s3d_receipt_" + suffix, shadow = "s3e_player_" + suffix;
        /** 表创建成功后才登记归属。 */
        final java.util.Set<String> created = new java.util.HashSet<>();
        /** 每次lookup/claim均走生产结构检查。 */
        final PaymentDedupStore store = new PaymentDedupStore(() -> db.factory, suffix);
        /** 只有实际成功占有才清理玩家行。 */
        boolean seeded;

        /** @throws Exception 本轮显式配置不可用时不跳过 */
        Fixture() throws Exception {}

        /** 进入资源作用域后才创建本轮行/表。 */
        void open() throws Exception {
            db.seed(); seeded = true; resetTables(); log.info("PAYMENT_SCHEMA_FIXTURE_OPENED suffix={}", suffix);
        }

        /** 从权威DDL重新建立同结构表，绝不执行生产表名DDL。 */
        void resetTables() throws Exception {
            for (String table : List.of(claim, receipt)) { if (created.contains(table)) { drop(table); } }
            String sql;
            try (InputStream in = getClass().getResourceAsStream("/db/migration/payment-durable-dedup-v1.sql")) { sql = new String(in.readAllBytes(), StandardCharsets.UTF_8); }
            sql = sql.replaceAll("(?m)^--.*$", "").replace("t_payment_fulfillment_claim", claim).replace("t_payment_fulfillment_receipt", receipt)
                    .replace(") ENGINE=InnoDB", ") ENGINE=InnoDB COMMENT='schema_" + suffix + "'");
            String[] statements = sql.trim().split(";"); Assertions.assertEquals(2, statements.length);
            for (int i = 0; i < 2; i++) { execute(statements[i]); created.add(i == 0 ? claim : receipt); }
        }

        /** @param ddl 代码内固定故障；第一段为claim或receipt角色 */
        void variant(String ddl) throws Exception {
            resetTables(); int space = ddl.indexOf(' '); String table = ddl.startsWith("claim ") ? claim : receipt;
            execute("ALTER TABLE " + table + " " + ddl.substring(space + 1)); reject();
        }

        /** @return 真实Handler与本轮独有订单ID */
        PaymentDeliveryLifecycleTest.Fixture shipment() throws Exception {
            var f = new PaymentDeliveryLifecycleTest.Fixture(db.player().getData(), true, db.writer::write);
            var field = cn.game.games.net.game.module.player.PlayerModule.class.getDeclaredField("payItems"); field.setAccessible(true);
            ((java.util.Map<?, ?>) field.get(f.player.getPlayerModule())).clear();
            PayItem item = PaymentDurableDedupTest.item(); item.setOrderId(db.id); f.player.getPlayerModule().addPayItems(item);
            f.orderId = db.id; f.durableStore = store; return f;
        }

        /** 先检查报告，再走真实发货，冷核对没有claim、奖励、Charge或聚合保存。 */
        void reject() throws Exception {
            var report = inspect(); Assertions.assertFalse(report.valid()); Assertions.assertThrows(java.sql.SQLException.class, report::requireValid);
            byte[] before = db.read().getModules();
            try (var f = shipment()) {
                f.ship(); f.flush(); f.assertRejectedBeforeBusiness(); Assertions.assertEquals(0, db.gate.commits.get());
                Assertions.assertNull(f.player.getPlayerModule().getPayItems(db.id).getSdkOrderId());
                Assertions.assertEquals(0, f.player.getPlayerModule().getPayItems(db.id).getFinishTime());
            }
            Assertions.assertArrayEquals(before, db.read().getModules()); assertEmpty();
        }

        /** @return 只执行生产结构检查的新只读连接观察 */
        PaymentDedupSchema.Report inspect() throws Exception {
            try (var c = db.source.getConnection()) { c.setReadOnly(true); return PaymentDedupSchema.inspect(c, "t_player_data", claim, receipt); }
        }

        /** @param engine 测试内部指定的真实引擎 */
        void createShadow(String engine) throws Exception {
            execute("CREATE TABLE " + shadow + " (id BIGINT NOT NULL PRIMARY KEY) ENGINE=" + engine + " COMMENT='schema_" + suffix + "'"); created.add(shadow);
        }

        /** 新JVM走固定生产表名和真实只读配置，仅观察元数据，正常缺表也必须输出完整脱敏阻断报告。 */
        void standalone() throws Exception {
            var root = java.nio.file.Path.of(System.getenv("S3_ARCHIVE_TEST_ROOT"));
            var stdout = root.resolve("schema-cli-" + suffix + ".json"); var stderr = root.resolve("schema-cli-" + suffix + ".log");
            var command = List.of(java.nio.file.Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-Xmx128m",
                    "-Dlog4j.configurationFile=" + java.nio.file.Path.of("target/classes/payment-evidence-log4j2.xml").toAbsolutePath().toUri(),
                    "-cp", System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")), PaymentEnablementCli.class.getName());
            Process process = new ProcessBuilder(command).redirectOutput(stdout.toFile()).redirectError(stderr.toFile()).start();
            try {
                Assertions.assertTrue(process.waitFor(20, java.util.concurrent.TimeUnit.SECONDS));
                String text = java.nio.file.Files.readString(stdout); var report = new com.fasterxml.jackson.databind.ObjectMapper().readTree(text);
                Assertions.assertEquals(report.path("localSchemaReady").booleanValue() ? 2 : 3, process.exitValue());
                Assertions.assertEquals(1, report.path("contractVersion").intValue()); Assertions.assertEquals(3, report.path("externalBlockers").size());
                for (String flag : List.of("canEnable", "canDelete", "canResend")) { Assertions.assertFalse(report.path(flag).booleanValue()); }
                for (String secret : List.of(db.runId, "jdbc:", System.getenv("S2_DB_CONFIG"), "dbpass")) { Assertions.assertFalse(text.contains(secret)); }
            } finally {
                if (process.isAlive()) { process.destroyForcibly(); Assertions.assertTrue(process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)); }
            }
        }

        /** @param sql 代码内部的受控DDL；不接受CLI表名 */
        void execute(String sql) throws Exception { try (var c = db.source.getConnection(); var s = c.createStatement()) { s.setQueryTimeout(5); s.execute(sql); } }
        /** @param table 本例表 @return 新连接冷读计数 */
        int count(String table) throws Exception {
            Assertions.assertTrue(created.contains(table));
            try (var c = db.source.getConnection(); var s = c.createStatement(); var r = s.executeQuery("SELECT COUNT(*) FROM " + table)) { Assertions.assertTrue(r.next()); return r.getInt(1); }
        }
        /** 两张新表不能出现任何本例占位/凭证。 */
        void assertEmpty() throws Exception { Assertions.assertEquals(0, count(claim)); Assertions.assertEquals(0, count(receipt)); }

        /** @param table 仅实际创建且数据库comment匹配的精确表 */
        void drop(String table) throws Exception {
            Assertions.assertTrue(created.contains(table));
            try (Connection c = db.source.getConnection(); var s = c.prepareStatement("SELECT table_comment FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name=?")) {
                s.setString(1, table); try (var r = s.executeQuery()) { Assertions.assertTrue(r.next()); Assertions.assertEquals("schema_" + suffix, r.getString(1)); }
            }
            execute("DROP TABLE " + table); created.remove(table);
        }

        @Override public void close() throws Exception {
            db.close();
            try { if (seeded) { db.clean(); } }
            finally {
                for (String table : List.of(receipt, claim, shadow)) { if (created.contains(table)) { drop(table); } }
                try (var c = db.source.getConnection(); var s = c.prepareStatement("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name IN (?,?,?)")) {
                    s.setString(1, claim); s.setString(2, receipt); s.setString(3, shadow);
                    try (var r = s.executeQuery()) { Assertions.assertTrue(r.next()); Assertions.assertEquals(0, r.getInt(1)); }
                }
                log.info("PAYMENT_SCHEMA_FIXTURE_COLD_EMPTY suffix={} game=1 tables=0", suffix);
            }
        }
    }
}
