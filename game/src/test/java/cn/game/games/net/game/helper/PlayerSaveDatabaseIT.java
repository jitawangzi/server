package cn.game.games.net.game.helper;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.games.net.game.module.player.PlayerModule;
import cn.game.games.net.game.module.recharge.PayItem;
import cn.game.games.net.game.module.recharge.PayType;
import cn.game.util.DateUtil;
import io.vertx.core.Future;

/**
 * 显式选择的真实库验收；S2_DB_CONFIG 指向本地凭据文件，不随普通单测运行。
 * 仅创建本轮唯一玩家行，不注册账号、不启动服务，也不把存储层验收冒充登录端到端。
 */
@Isolated
class PlayerSaveDatabaseIT {
    /** 记录精确测试归属和清理结果，不记录连接凭据。 */
    private static final Logger log = LoggerFactory.getLogger(PlayerSaveDatabaseIT.class);

    /** 准备生产模块类型，避免启动服务器和共享连接池。 */
    @BeforeAll static void metadata() { ClassManager.getInstance().init(); }

    /** 唯一两库夹具经真实只读采集、持久归档和冷读验证；新连接核对源行未变及精确清理为空。 */
    @Test void trustedArchiveReadsBothDatabasesWithoutMutatingSource() throws Exception {
        Properties config = new Properties();
        try (InputStream input = Files.newInputStream(Path.of(System.getenv("S2_DB_CONFIG")))) { config.load(input); }
        Path runtime = Files.createTempDirectory(Path.of(System.getenv("S3_ARCHIVE_TEST_ROOT")), "archive-db-");
        Path key = runtime.resolve("key.bin"), archive = runtime.resolve("archive");
        byte[] secret = new byte[32]; new java.security.SecureRandom().nextBytes(secret); Files.write(key, secret);
        java.util.Arrays.fill(secret, (byte) 0);
        try (DatabaseFixture db = new DatabaseFixture(false)) {
            db.seed(); boolean loginInserted = false;
            try {
                try (var connection = archiveLoginConnection(config); var insert = connection.prepareStatement(
                        "INSERT INTO t_pay_order (id,game_server_id,channel_code,channel_label,user_id,player_id,third_uid,env,pay_state,is_deliver,create_date,create_time,debug_text,third_order_id,price,item_name,callback) VALUES (?,'s3c-test','s3c-test','s3c-test',?,?,?,1,2,1,?,?,?, ?,1200,'s3c-test','{}')")) {
                    insert.setLong(1, db.id); insert.setLong(2, db.id); insert.setLong(3, db.id); insert.setString(4, db.runId);
                    insert.setString(5, DateUtil.nowDateStr()); insert.setString(6, DateUtil.nowTimeStr());
                    insert.setString(7, db.runId); insert.setString(8, db.runId);
                    Assertions.assertEquals(1, insert.executeUpdate()); loginInserted = true;
                }
                Player player = db.player(); PayItem item = new PayItem();
                item.setOrderId(db.id); item.setRmb(12); item.setPayType(PayType.FirstCharge); item.setSdkOrderId(db.runId);
                item.finish(); item.setCompletionState("REWARDS_COMPLETED"); item.prepareCompletionProof(db.id); item.confirmCompletionProof();
                player.getPlayerModule().addPayItems(item);
                Assertions.assertEquals(PlayerSaveCoordinator.Outcome.COMMITTED,
                        await(db.writer.write(PlayerDataSnapshot.capture(player), false)).outcome());
                String gameBefore = db.read().getModules(); List<String> loginBefore = archiveLoginSnapshot(config, db.id);
                var sealed = cn.game.games.diagnostics.PaymentArchiveCli.run(new String[] {"archive", Long.toString(db.id), Long.toString(db.id), archive.toString(), key.toString()});
                Assertions.assertEquals("SEALED_AND_VERIFIED", sealed.get("status"), sealed.toString());
                Path artifact = archive.resolve("payment-" + db.id + "-" + db.id + ".json");
                Assertions.assertTrue(Files.size(artifact) > 0);
                var cold = cn.game.games.diagnostics.PaymentArchiveCli.run(new String[] {"verify", Long.toString(db.id), Long.toString(db.id), archive.toString(), key.toString()});
                Assertions.assertEquals("VERIFIED", cold.get("status")); Assertions.assertEquals(false, cold.get("canDelete"));
                Assertions.assertEquals(gameBefore, db.read().getModules());
                Assertions.assertEquals(loginBefore, archiveLoginSnapshot(config, db.id));
                db.clean();
            } finally {
                if (loginInserted) {
                    try (var connection = archiveLoginConnection(config); var delete = connection.prepareStatement(
                            "DELETE FROM t_pay_order WHERE id=? AND player_id=? AND debug_text=? AND third_uid=? AND third_order_id=?")) {
                        delete.setLong(1, db.id); delete.setLong(2, db.id); delete.setString(3, db.runId);
                        delete.setString(4, db.runId); delete.setString(5, db.runId);
                        Assertions.assertEquals(1, delete.executeUpdate());
                    }
                    Assertions.assertTrue(archiveLoginSnapshot(config, db.id).isEmpty(), "新连接冷查本轮Login夹具必须为空");
                    log.info("TRUSTED_ARCHIVE_LOGIN_FIXTURE_COLD_EMPTY");
                }
            }
            log.info("TRUSTED_ARCHIVE_FIXTURE_COLD_EMPTY game=1 login=1");
        } finally {
            // 路径只来自本轮私有临时目录；不保留测试密钥或把测试产物当生产归档。
            try (var paths = Files.walk(runtime)) {
                for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) { Files.delete(path); }
            }
        }
    }

    /** @param config 临时受限配置 @return 仅本夹具的独立 Login 连接，不使用业务连接池 */
    private static Connection archiveLoginConnection(Properties config) throws SQLException {
        Properties properties = new Properties(); properties.setProperty("user", config.getProperty("login.dbuser"));
        properties.setProperty("password", config.getProperty("login.dbpass")); properties.setProperty("connectTimeout", "2000");
        properties.setProperty("socketTimeout", "5000"); properties.setProperty("autoReconnect", "false");
        return java.sql.DriverManager.getConnection(config.getProperty("login.dburl"), properties);
    }

    /** @param config 临时配置 @param id 本轮唯一订单 @return 每个字段的冷读值，含 null；不输出正文 */
    private static List<String> archiveLoginSnapshot(Properties config, long id) throws SQLException {
        try (var connection = archiveLoginConnection(config); var select = connection.prepareStatement("SELECT * FROM t_pay_order WHERE id=?")) {
            select.setLong(1, id);
            try (var row = select.executeQuery()) {
                List<String> values = new ArrayList<>();
                if (row.next()) { for (int i = 1; i <= row.getMetaData().getColumnCount(); i++) { values.add(row.getString(i)); } }
                return values;
            }
        }
    }

    /** 真实库写入十天以前的凭证，冷读后执行清理，再经本地 CLI 重复核对并确认整行不变。 */
    @Test void agedPaymentEvidenceSurvivesColdCleanupAndReadOnlyCli() throws Exception {
        try (DatabaseFixture db = new DatabaseFixture(false)) {
            db.seed();
            Player player = db.player();
            PayItem item = new PayItem(); item.setOrderId(db.id); item.setRmb(12); item.setPayType(PayType.FirstCharge);
            item.setSdkOrderId("fixture-sdk"); item.finish(); item.setCompletionState("REWARDS_COMPLETED");
            item.setFinishTime(DateUtil.currentTimeMillis() - 11 * DateUtil.DAY_MILLIS);
            item.prepareCompletionProof(db.id); item.confirmCompletionProof();
            player.getPlayerModule().addPayItems(item);
            Assertions.assertEquals(PlayerSaveCoordinator.Outcome.COMMITTED,
                    await(db.writer.write(PlayerDataSnapshot.capture(player), false)).outcome());
            Player cold = db.reloadFresh(); cold.getPlayerModule().initFromDbAfter();
            Assertions.assertTrue(cold.getPlayerModule().getPayItems(db.id).hasCommittedCompletionProof(db.id, "fixture-sdk"));
            String before = db.read().getModules();
            for (int i = 0; i < 2; i++) {
                var result = cn.game.games.diagnostics.PaymentEvidenceCli.run(new String[] {Long.toString(db.id), Long.toString(db.id)});
                Assertions.assertEquals("PERSISTED_V1_PROOF_CONSISTENT", result.get("game"), result.toString());
                Assertions.assertEquals("ORDER_NOT_FOUND", result.get("login"), result.toString());
                Assertions.assertEquals("NOT_PROVEN", result.get("transactionResolution"));
                Assertions.assertEquals(before, db.read().getModules());
            }
            db.clean();
        }
    }

    /** 原快照真实提交后，用新会话冷读、新玩家对象和新协调器验证重复通知不再发奖或保存。 */
    @Test void completedPaymentColdReloadDeduplicatesWithoutReplaying() throws Exception {
        PaymentDeliveryLifecycleTest.metadata();
        try {
            for (boolean spring : List.of(false, true)) {
                try (DatabaseFixture db = new DatabaseFixture(spring)) {
                    db.seed(); db.gate.hold = true;
                    try (var original = new PaymentDeliveryLifecycleTest.Fixture(db.player().getData(), true, db.writer::write)) {
                        original.ship(); original.captured(); Assertions.assertTrue(db.gate.entered.await(10, TimeUnit.SECONDS));
                        Assertions.assertTrue(original.replies.isEmpty());
                        Assertions.assertFalse(original.player.getPlayerModule().getPayItems(91).hasCommittedCompletionProof(db.id, "sdk-91"));
                        db.gate.release.countDown(); original.flush();
                        Assertions.assertEquals(List.of(true), original.replies);
                    }
                    Player cold = db.reloadFresh();
                    Assertions.assertEquals(1, cold.getPlayerModule().getGuideMap().get(700));
                    Assertions.assertTrue(cold.getPlayerModule().getPayItems(91).hasCommittedCompletionProof(db.id, "sdk-91"));
                    try (var restarted = new PaymentDeliveryLifecycleTest.Fixture(cold.getData(), true,
                            (row, insert) -> { throw new AssertionError("冷读去重不能再次保存"); })) {
                        restarted.player.getModules().put(PlayerModule.class.getName(), cold.getPlayerModule());
                        restarted.ship(); restarted.flush(); restarted.ship(); restarted.flush();
                        Assertions.assertEquals(List.of(true, true), restarted.replies);
                        Assertions.assertEquals(0, restarted.awards.get()); Assertions.assertEquals(0, restarted.charges.get());
                        Assertions.assertFalse(restarted.captured.future().isComplete());
                        Assertions.assertEquals(1, restarted.player.getPlayerModule().getGuideMap().get(700));
                    }
                    Assertions.assertEquals(1, db.gate.completedCommits.get());
                    db.clean();
                }
            }
        } finally { PaymentDeliveryLifecycleTest.restore(); }
    }

    /** 实际离线发货在真实事务确认后才回成功并清理；冷读核对订单与本例奖励标记。 */
    @Test void offlineDeliveryCommitsBeforeReplyAndCleanup() throws Exception {
        PaymentDeliveryLifecycleTest.metadata();
        try {
            for (boolean spring : List.of(false, true)) {
                try (DatabaseFixture db = new DatabaseFixture(spring)) {
                    db.seed(); db.gate.hold = true;
                    try (var f = new PaymentDeliveryLifecycleTest.Fixture(db.player().getData(), false, db.writer::write)) {
                        f.ship(); f.captured(); Assertions.assertTrue(db.gate.entered.await(10, TimeUnit.SECONDS));
                        Assertions.assertTrue(f.replies.isEmpty()); Assertions.assertSame(f.player, f.owner);
                        db.gate.release.countDown(); f.flush();
                        Assertions.assertEquals(List.of(true), f.replies); Assertions.assertNull(f.owner);
                        Assertions.assertEquals(1, f.awards.get()); Assertions.assertEquals(1, f.charges.get());
                        Player cold = db.reloadFresh();
                        Assertions.assertEquals(1, cold.getPlayerModule().getGuideMap().get(700));
                        PayItem order = cold.getPlayerModule().getPayItems(91);
                        Assertions.assertNotNull(order); Assertions.assertTrue(order.isFinish());
                        Assertions.assertEquals("sdk-91", order.getSdkOrderId());
                        Assertions.assertFalse(cold.getPlayerModule().execPayCallback(91));
                    }
                    db.clean();
                }
            }
        } finally { PaymentDeliveryLifecycleTest.restore(); }
    }

    /** 真实发布器经生产保存通道和事务写入器确认后推单，新 Player 必须恢复完整付款关联。 */
    @Test void payableOrderPublicationColdReloadsAssociation() throws Exception {
        for (boolean spring : List.of(false, true)) {
            try (DatabaseFixture db = new DatabaseFixture(spring)) {
                db.seed();
                try (var f = new PaymentOrderPublisherTest.Fixture(db.player(), db.writer::write)) {
                    Future<Boolean> payment = f.start();
                    f.order(); f.captured(); f.flush();
                    Assertions.assertEquals(1, f.client.published());
                    Assertions.assertFalse(payment.isComplete(), "发布订单不等于付款成功");
                    Player cold = db.reloadFresh();
                    PayItem item = cold.getPlayerModule().getPayItems(91);
                    Assertions.assertNotNull(item);
                    Assertions.assertEquals(91, item.getOrderId()); Assertions.assertEquals(12, item.getRmb());
                    Assertions.assertEquals(PayType.FirstCharge, item.getPayType()); Assertions.assertEquals(73, item.getPayId());
                    Assertions.assertEquals(1007, item.getSdkGoodsId()); Assertions.assertEquals(List.of(17, 18), item.getPaySubIds());
                    Assertions.assertFalse(item.isFinish()); Assertions.assertFalse(cold.getPlayerModule().execPayCallback(91));
                    Assertions.assertFalse(f.saves.status(db.id).observedDirty());
                }
                db.clean();
            }
        }
    }

    /** 两种生产支持的事务都须提交后才能在新会话和新对象中读到快照。 */
    @Test void committedSnapshotsColdReloadWithBothTransactionFactories() throws Exception {
        for (boolean spring : List.of(false, true)) {
            try (DatabaseFixture f = new DatabaseFixture(spring)) {
                f.seed();
                Player player = f.player();
                PlayerData snapshot = PlayerDataSnapshot.capture(player);
                player.getData().setExp(999);
                player.getModule(PlayerModule.class).getGuideMap().put(7, 999);
                Assertions.assertEquals(PlayerSaveCoordinator.Outcome.COMMITTED, await(f.writer.write(snapshot, false)).outcome());
                Player cold = f.reloadFresh();
                Assertions.assertNotSame(player, cold);
                Assertions.assertNotSame(player.getModule(PlayerModule.class), cold.getModule(PlayerModule.class));
                Assertions.assertEquals(42, cold.getData().getExp());
                Assertions.assertEquals(43, cold.getModule(PlayerModule.class).getGuideMap().get(7));
                f.clean();
            }
        }
    }

    /** 等待视图超时期间，真实事务尚未提交，B 不得越过 A，最终冷读必须是 B。 */
    @Test void timeoutKeepsActualDatabaseWritesOrdered() throws Exception {
        for (boolean spring : List.of(false, true)) {
            try (DatabaseFixture f = new DatabaseFixture(spring)) {
                f.seed();
                Player player = f.player();
                List<Runnable> deadlines = new ArrayList<>();
                PlayerSaveCoordinator saves = coordinator(f, deadlines);
                saves.register(f.id, player, () -> {});
                f.gate.hold = true;
                var a = saves.submit(player, PlayerDataSnapshot.capture(player), false);
                Assertions.assertTrue(f.gate.entered.await(10, TimeUnit.SECONDS), "A 必须进入真实 commit 边界");
                player.getData().setExp(84);
                player.getModule(PlayerModule.class).getGuideMap().put(7, 85);
                var b = saves.submit(player, PlayerDataSnapshot.capture(player), false);
                deadlines.getFirst().run();
                Assertions.assertThrows(java.util.concurrent.ExecutionException.class, () -> await(a.result()));
                Assertions.assertEquals("WAIT_TIMEOUT", Assertions.assertInstanceOf(PlayerSaveCoordinator.SaveFailure.class, a.result().cause()).code);
                Assertions.assertEquals(1, saves.status(f.id).queued());
                Assertions.assertFalse(saves.status(f.id).workerExited());
                Assertions.assertEquals(1, f.gate.commits.get());
                Assertions.assertEquals(1, f.submissions.get(), "B 必须尚未提交给写入器，而不只是排在其线程池里");
                Assertions.assertEquals(0, f.read().getExp(), "另一个连接只能看到此前已提交的初始行");
                f.gate.release.countDown();
                await(b.result());
                Player cold = f.reloadFresh();
                Assertions.assertEquals(84, cold.getData().getExp());
                Assertions.assertEquals(85, cold.getModule(PlayerModule.class).getGuideMap().get(7));
                Assertions.assertEquals(b.generation(), saves.status(f.id).lastConfirmedGeneration());
                Assertions.assertFalse(saves.status(f.id).observedDirty());
                f.clean();
            }
        }
    }

    /** 在真实 commit 返回后注入丢失回执，UNKNOWN 不得重写，核对实际结束与冷读后显式恢复。 */
    @Test void committedButLostAcknowledgementRequiresExplicitReconciliation() throws Exception {
        for (boolean spring : List.of(false, true)) {
            try (DatabaseFixture f = new DatabaseFixture(spring)) {
                f.seed();
                Player player = f.player();
                PlayerSaveCoordinator saves = coordinator(f, new ArrayList<>());
                saves.register(f.id, player, () -> {});
                f.gate.loseAcknowledgement = true;
                var a = saves.submit(player, PlayerDataSnapshot.capture(player), false);
                Assertions.assertThrows(java.util.concurrent.ExecutionException.class, () -> await(a.result()));
                Assertions.assertEquals("UNKNOWN", saves.status(f.id).state());
                Assertions.assertTrue(saves.status(f.id).writeProtected());
                Assertions.assertTrue(saves.status(f.id).workerExited());
                Assertions.assertEquals(1, f.gate.completedCommits.get(), "故障发生在真实驱动 commit 返回之后");
                Assertions.assertEquals(42, f.reloadFresh().getData().getExp());
                Assertions.assertThrows(PlayerSaveCoordinator.SaveFailure.class, () -> saves.beginMutation(f.id, player));
                var status = saves.status(f.id);
                saves.reconcile(status.coordinatorId(), f.id, a.generation(), PlayerSaveCoordinator.Resolution.COMMITTED,
                        new PlayerSaveCoordinator.RecoveryEvidence("database-fixture", "configured-test-database", f.runId,
                                "driver commit returned; isolated writer exited; no automatic retry", "new connection cold read matched snapshot A"));
                Assertions.assertFalse(saves.status(f.id).writeProtected());
                Assertions.assertEquals(1, f.gate.completedCommits.get());
                f.clean();
            }
        }
    }

    /** @param f 独立数据库夹具 @param deadlines 可控等待超时动作 @return 连接真实写入器的协调器 */
    private static PlayerSaveCoordinator coordinator(DatabaseFixture f, List<Runnable> deadlines) {
        return new PlayerSaveCoordinator(2, 30_000, (row, insert) -> {
            f.submissions.incrementAndGet(); return f.writer.write(row, insert);
        },
                (action, delay) -> { deadlines.add(action); return () -> {}; }, DateUtil::currentTimeMillis);
    }

    /** @param future 待确认实际结果 @return 完成值，不把调用者等待超时当成事务结束 */
    private static <T> T await(Future<T> future) throws Exception {
        return future.toCompletionStage().toCompletableFuture().get(15, TimeUnit.SECONDS);
    }

    /** 每个场景独占一个随机主键；失败保留数据与归属日志，成功才条件删除。 */
    static final class DatabaseFixture implements AutoCloseable {
        /** 本轮归属标识。 */
        final String runId = "s2_" + UUID.randomUUID().toString().replace("-", "");
        /** 与普通分配 ID 空间分离的正整数，INSERT 冲突立即失败。 */
        final long id = (UUID.randomUUID().getMostSignificantBits() & 0x1fffffffffffffffL) | 0x4000000000000000L;
        /** 只使用本测试的连接，不使用共享业务池。 */
        final DriverManagerDataSource source;
        /** 仅拦截本场景的事务提交边界。 */
        final CommitGate gate;
        /** 使用生产 XML 的独立映射工厂。 */
        final SqlSessionFactory factory;
        /** 同一场景最多一个写入工作线程。 */
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        /** 生产事务写入器。 */
        final PlayerSnapshotWriter writer;
        /** 协调器实际交给写入器的数量，独立于线程池是否开始执行。 */
        final AtomicInteger submissions = new AtomicInteger();

        /** @param spring 是否验证 SpringManaged 事务；缺少显式配置时报错，不跳过验收 */
        DatabaseFixture(boolean spring) throws Exception {
            String config = System.getenv("S2_DB_CONFIG");
            Assertions.assertNotNull(config, "显式设置 S2_DB_CONFIG 后运行本集成测试");
            Properties properties = new Properties();
            try (InputStream input = Files.newInputStream(Path.of(config))) { properties.load(input); }
            source = new DriverManagerDataSource();
            source.setDriverClassName(properties.getProperty("driverClassName"));
            source.setUrl(properties.getProperty("dburl"));
            source.setUsername(properties.getProperty("dbuser"));
            source.setPassword(properties.getProperty("dbpass"));
            Properties limits = new Properties();
            limits.setProperty("connectTimeout", "3000");
            limits.setProperty("socketTimeout", "20000");
            limits.setProperty("autoReconnect", "false");
            source.setConnectionProperties(limits);
            gate = new CommitGate(source);
            Configuration mapping = new Configuration(new Environment("s2-fixture",
                    spring ? new SpringManagedTransactionFactory() : new JdbcTransactionFactory(), gate));
            try (InputStream xml = PlayerSaveDatabaseIT.class.getResourceAsStream("/mapping/PlayerDataMapper.xml")) {
                new XMLMapperBuilder(xml, mapping, "mapping/PlayerDataMapper.xml", mapping.getSqlFragments()).parse();
            }
            factory = new SqlSessionFactoryBuilder().build(mapping);
            writer = new PlayerSnapshotWriter(() -> factory, executor);
        }

        /** 资源进入 try-with-resources 后才执行可失败的建行，异常仍能收尾执行器。 */
        void seed() throws Exception {
            log.info("数据库测试登记 runId={} playerId={} uid={} transactions={}", runId, id, id,
                    factory.getConfiguration().getEnvironment().getTransactionFactory().getClass().getSimpleName());
            // 直接 INSERT 是唯一占有动作；从不先删除、更新或复用已有玩家。
            PlayerData seed = data();
            seed.setExp(0);
            var inserted = await(writer.write(seed, true));
            Assertions.assertEquals(PlayerSaveCoordinator.Outcome.COMMITTED, inserted.outcome(), "fixture 插入必须确认成功");
            gate.commits.set(0);
            gate.completedCommits.set(0);
        }

        /** @return 本轮专用聚合行，全部身份字段保持可核对 */
        private PlayerData data() {
            PlayerData data = new PlayerData();
            data.setPlayerId(id); data.setUid(id); data.setAccountId(runId); data.setDeviceId(runId);
            data.setChannelId("s2-test"); data.setServerId("s2-test"); data.setName(runId.substring(0, 15));
            data.setRegion(""); data.setUnionName("");
            data.setCreateDate("2026-10-25 00:00:00"); data.setLoginDate("2026-10-25 00:00:00");
            data.setModules("{}"); data.setExp(42);
            return data;
        }

        /** @return 仅装配实际 PlayerModule 的在线测试对象，不执行登录副作用 */
        Player player() {
            PlayerData data = data();
            Player player = new Player() { @Override public PlayerData getData() { return data; } };
            player.setPlayerId(id);
            PlayerModule module = new PlayerModule();
            module.getGuideMap().put(7, 43);
            player.getModules().put(PlayerModule.class.getName(), module);
            return player;
        }

        /** @return 新会话读取的真实提交行，禁用会话间缓存 */
        PlayerData read() {
            try (var session = factory.openSession()) {
                PlayerData row = session.getMapper(PlayerDataMapper.class).selectByPrimaryKey(id);
                Assertions.assertNotNull(row);
                Assertions.assertEquals(runId, row.getAccountId());
                Assertions.assertEquals(id, row.getUid());
                return row;
            }
        }

        /** @return 通过生产构造函数恢复的新 Player，不复用在线模块或 JSON 往返替身 */
        Player reloadFresh() { return new Player(read()); }

        /** 仅在全部断言通过后删除本轮归属匹配行，不进行全表或账号范围清理。 */
        void clean() throws SQLException {
            try (Connection connection = source.getConnection();
                    var statement = connection.prepareStatement("DELETE FROM t_player_data WHERE player_id=? AND uid=? AND account_id=? AND device_id=?")) {
                statement.setLong(1, id); statement.setLong(2, id); statement.setString(3, runId); statement.setString(4, runId);
                Assertions.assertEquals(1, statement.executeUpdate(), "只清理本轮精确归属行");
            }
            log.info("数据库测试清理完成 runId={} playerId={}", runId, id);
            try (var session = factory.openSession()) {
                Assertions.assertNull(session.getMapper(PlayerDataMapper.class).selectByPrimaryKey(id), "精确清理后冷查必须为空");
            }
            log.info("PAYMENT_GAME_FIXTURE_COLD_EMPTY");
        }

        @Override public void close() throws InterruptedException {
            gate.release.countDown();
            gate.receiptRelease.countDown();
            executor.shutdown();
            Assertions.assertTrue(executor.awaitTermination(20, TimeUnit.SECONDS), "实际写线程未退出，保留 fixture，不进入下一轮");
        }
    }

    /** 仅包装真实连接的 commit；SQL、事务提交和冷读仍由实际数据库执行。 */
    static final class CommitGate extends DelegatingDataSource implements PaymentDatabaseBoundary.Source {
        /** A 到达真实提交边界。 */
        final CountDownLatch entered = new CountDownLatch(1);
        /** 由测试放行 A，任何退出路径都放行。 */
        final CountDownLatch release = new CountDownLatch(1);
        /** 已进入提交边界的数量。 */
        final AtomicInteger commits = new AtomicInteger();
        /** 驱动真实 commit 已成功返回的数量。 */
        final AtomicInteger completedCommits = new AtomicInteger();
        /** 是否在首笔提交前暂停。 */
        volatile boolean hold;
        /** 是否在首笔真实提交后注入回执丢失。 */
        volatile boolean loseAcknowledgement;
        /** 新持久去重验收只在完成凭证事务的提交处阻塞，避免阻塞发奖前占位提交。 */
        volatile boolean holdReceipt, failReceiptInsert;
        /** 真实提交之后丢失确认，只用于新凭证 UNKNOWN 验收。 */
        volatile boolean loseReceiptAcknowledgement;
        /** 查询异常只能拒绝，不能转换成凭证不存在。 */
        volatile boolean failEvidenceRead;
        /** 新凭证场景的独立提交屏障，旧回归不使用。 */
        final CountDownLatch receiptEntered = new CountDownLatch(1), receiptRelease = new CountDownLatch(1);
        /** 真实驱动完成凭证事务提交的证据。 */
        final CountDownLatch receiptCommitted = new CountDownLatch(1);

        /** @param source 测试私有数据源 */
        CommitGate(DriverManagerDataSource source) { super(source); }

        /** 隔离新场景可声明更短的已验证私有获取上限。 */
        int acquisitionMillis = 60000;
        @Override public boolean mysqlDriver() { return true; }
        @Override public int acquisitionMillis() { return acquisitionMillis; }
        @Override public void discard(Connection connection) throws SQLException {
            connection.unwrap(com.mysql.cj.jdbc.JdbcConnection.class).abortInternal();
            connection.close();
        }
        @Override public Connection getConnection() throws SQLException {
            Connection actual = super.getConnection();
            java.util.concurrent.atomic.AtomicBoolean receipt = new java.util.concurrent.atomic.AtomicBoolean();
            return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[] {Connection.class},
                    (proxy, method, args) -> {
                        if (failEvidenceRead && method.getName().equals("prepareStatement") && args[0] instanceof String sql
                                && sql.startsWith("SELECT intent,token,player_id,order_id,sdk_order_id FROM s3d_claim_")) {
                            throw new SQLException("受控持久凭证查询故障");
                        }
                        if (method.getName().equals("prepareStatement") && args[0] instanceof String sql
                                && sql.startsWith("INSERT INTO s3d_receipt_")) {
                            receipt.set(true);
                            if (failReceiptInsert) { throw new SQLException("受控凭证插入故障"); }
                        }
                        boolean commit = method.getName().equals("commit");
                        if (commit && receipt.get() && holdReceipt) {
                            receiptEntered.countDown();
                            if (!receiptRelease.await(15, TimeUnit.SECONDS)) { throw new SQLException("凭证提交屏障未释放"); }
                        }
                        int number = commit ? commits.incrementAndGet() : 0;
                        if (commit && hold && number == 1) {
                            entered.countDown();
                            if (!release.await(15, TimeUnit.SECONDS)) { throw new SQLException("测试提交屏障未释放"); }
                        }
                        Object result;
                        try { result = method.invoke(actual, args); }
                        catch (InvocationTargetException failure) {
                            log.warn("真实数据库调用失败 method={}", method.getName(), failure.getCause());
                            throw failure.getCause();
                        }
                        if (commit) {
                            completedCommits.incrementAndGet();
                            if (receipt.get()) {
                                receiptCommitted.countDown();
                                if (loseReceiptAcknowledgement) { throw new SQLException("凭证事务真实提交后回执丢失"); }
                            }
                            if (loseAcknowledgement && number == 1) { throw new SQLException("真实提交完成后注入回执丢失"); }
                        }
                        return result;
                    });
        }
    }
}
