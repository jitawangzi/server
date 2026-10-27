package cn.game.games.net.game.helper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.module.player.PlayerModule;
import cn.game.games.net.game.module.recharge.PayItem;

/**
 * 使用当前 Apollo 配置和真实 Game 事务验收；每例独占随机玩家和两张与迁移同结构的临时表。
 * 只改自己的 fixture，finally 条件清理并从新连接冷查为空；不部署生产 schema、不调用支付平台。
 */
@Isolated
class PaymentDurableDedupDatabaseIT {
    /** 精确清理证据只输出固定标记和计数，不输出连接值或凭证正文。 */
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PaymentDurableDedupDatabaseIT.class);

    /** 初始化生产模块元数据和隔离统计依赖。 */
    @BeforeAll static void metadata() throws Exception { PaymentDeliveryLifecycleTest.metadata(); }
    /** 恢复测试前依赖。 */
    @AfterAll static void restore() throws Exception { PaymentDeliveryLifecycleTest.restore(); }

    /** JDBC 和 Spring 使用真实同一事务，邮箱并发通知只执行一次奖励、Charge 和完成 INSERT。 */
    @Test void atomicJdbcAndSpringCommitBeforeSuccess() throws Exception {
        for (boolean spring : List.of(false, true)) {
            try (var c = new Case(spring)) {
                c.open(); c.db.gate.holdReceipt = true;
                try (var f = c.shipment(true)) {
                    f.ship(); f.captured(); Assertions.assertTrue(c.db.gate.receiptEntered.await(10, TimeUnit.SECONDS));
                    Assertions.assertTrue(f.replies.isEmpty()); Assertions.assertEquals(0, c.receipts());
                    Assertions.assertEquals("{}", c.db.read().getModules(), "提交前新连接只能看到原fixture字节");
                    f.ship(); c.db.gate.receiptRelease.countDown(); f.flush();
                    Assertions.assertEquals(List.of(true, true), f.replies);
                    Assertions.assertEquals(1, f.awards.get()); Assertions.assertEquals(1, f.charges.get());
                    Assertions.assertEquals(1, c.receipts());
                    Player cold = c.db.reloadFresh();
                    Assertions.assertEquals(1, cold.getPlayerModule().getGuideMap().get(700));
                    Assertions.assertTrue(cold.getPlayerModule().getPayItems(91).hasCommittedCompletionProof(c.db.id, "sdk-91"));
                    Assertions.assertFalse(c.db.read().getModules().contains("durablePayment"));
                    Assertions.assertEquals(PaymentDedupStore.Lookup.COMPLETED, c.store.lookup(c.db.id, 91, "sdk-91", cold.getPlayerModule().getPayItems(91)));
                }
            }
        }
    }

    /** 移除仅属于测试 fixture 的热记录并真实保存，重启/冷加载后没有归档目录仍成功去重。 */
    @Test void coldWithoutHotOrderDeduplicatesWithoutArchiveOrSideEffects() throws Exception {
        try (var c = new Case(false)) {
            c.open();
            try (var f = c.shipment(true)) { f.ship(); f.captured(); f.flush(); Assertions.assertEquals(List.of(true), f.replies); }
            Player cold = c.db.reloadFresh();
            // 仅在本轮唯一测试玩家构造未来清理后的数据库状态，不增加生产清理入口。
            var field = PlayerModule.class.getDeclaredField("payItems"); field.setAccessible(true);
            ((java.util.Map<?, ?>) field.get(cold.getPlayerModule())).clear();
            Assertions.assertEquals(PlayerSaveCoordinator.Outcome.COMMITTED,
                    PaymentDeliveryLifecycleTest.await(c.db.writer.write(PlayerDataSnapshot.capture(cold), false)).outcome());
            Player restarted = c.db.reloadFresh(); Assertions.assertNull(restarted.getPlayerModule().getPayItems(91));
            try (var f = new PaymentDeliveryLifecycleTest.Fixture(restarted.getData(), true,
                    (row, insert) -> { throw new AssertionError("幂等重试不得保存"); })) {
                f.durableStore = c.store; f.player.getModules().put(PlayerModule.class.getName(), restarted.getPlayerModule());
                f.ship(); f.flush(); f.ship(); f.flush();
                Assertions.assertEquals(List.of(true, true), f.replies); Assertions.assertEquals(0, f.awards.get());
                Assertions.assertEquals(0, f.charges.get()); Assertions.assertFalse(f.captured.future().isComplete());
                Assertions.assertEquals(1, f.player.getPlayerModule().getGuideMap().get(700));
            }
        }
    }

    /** Mapper 已 UPDATE 后凭证 INSERT 故障，两种事务工厂都回滚聚合，原对象禁止自动保存和重放。 */
    @Test void receiptInsertFailureRollsBackAggregateAndFreezesOwner() throws Exception {
        for (boolean spring : List.of(false, true)) {
            try (var c = new Case(spring)) {
                c.open(); c.db.gate.failReceiptInsert = true;
                try (var f = c.shipment(true)) {
                    f.ship(); f.flush();
                    Assertions.assertEquals(List.of(false), f.replies); Assertions.assertEquals(1, f.awards.get());
                    Assertions.assertEquals(0, c.receipts()); Assertions.assertEquals("{}", c.db.read().getModules());
                    Assertions.assertTrue(f.saves.status(f.id()).writeProtected());
                    Assertions.assertThrows(PlayerSaveCoordinator.SaveFailure.class,
                            () -> PlayerHelper.saveClientCache(f.id(), id -> f.owner, () -> true, f.saves, tasks -> { throw new AssertionError(); }));
                    c.db.gate.failReceiptInsert = false; f.ship(); f.flush();
                    Assertions.assertEquals(List.of(false, false), f.replies); Assertions.assertEquals(1, f.awards.get());
                    Assertions.assertEquals(PaymentDedupStore.Lookup.CLAIMED, c.store.lookup(c.db.id, 91, "sdk-91", null));
                }
            }
        }
    }

    /** 完成表唯一键冲突发生在聚合 UPDATE 之后，原完成事实不覆盖，第二份聚合必须回滚。 */
    @Test void immutableReceiptConflictRollsBackSecondSnapshot() throws Exception {
        for (boolean spring : List.of(false, true)) {
            try (var c = new Case(spring)) {
                c.open();
                PayItem item = PaymentDurableDedupTest.item(); item.setSdkOrderId("sdk-91");
                var claim = c.store.claim(PaymentDedupStore.intent(c.db.id, item, "sdk-91"));
                item.finish(); item.setCompletionState("REWARDS_COMPLETED"); item.prepareCompletionProof(c.db.id);
                Player player = c.db.player(); player.getPlayerModule().addPayItems(item);
                player.getPlayerModule().setDurablePayment(PaymentDedupStore.completed(claim, item));
                var first = PlayerDataSnapshot.capture(player);
                Assertions.assertEquals(PlayerSaveCoordinator.Outcome.COMMITTED, PaymentDeliveryLifecycleTest.await(c.db.writer.write(first, false)).outcome());
                String original = c.db.read().getModules();
                player.getData().setExp(999); player.getPlayerModule().getGuideMap().put(700, 99);
                Assertions.assertEquals(PlayerSaveCoordinator.Outcome.UNKNOWN,
                        PaymentDeliveryLifecycleTest.await(c.db.writer.write(PlayerDataSnapshot.capture(player), false)).outcome());
                Assertions.assertEquals(42, c.db.read().getExp()); Assertions.assertEquals(original, c.db.read().getModules());
                Assertions.assertEquals(1, c.receipts());
            }
        }
    }

    /** 相同订单/不同参数、相同平台单/不同订单并发争用真实唯一键；失败者不能取得 token。 */
    @Test void concurrentClaimsRejectSameOrderAndSdkWithDifferentArguments() throws Exception {
        try (var c = new Case(false)) {
            c.open();
            var a = PaymentDurableDedupTest.item(); var b = PaymentDurableDedupTest.item(); b.setRmb(13);
            CountDownLatch start = new CountDownLatch(1); AtomicInteger won = new AtomicInteger(), lost = new AtomicInteger();
            try (var pool = Executors.newVirtualThreadPerTaskExecutor()) {
                var tasks = new java.util.ArrayList<java.util.concurrent.Future<?>>();
                for (PayItem item : List.of(a, b)) {
                    tasks.add(pool.submit(() -> {
                        try { start.await(); c.store.claim(PaymentDedupStore.intent(c.db.id, item, "sdk-91")); won.incrementAndGet(); }
                        catch (java.sql.SQLException conflict) { log.warn("预期唯一键竞争失败", conflict); lost.incrementAndGet(); }
                        catch (InterruptedException failure) { log.warn("竞争测试被中断", failure); Thread.currentThread().interrupt(); throw new IllegalStateException(failure); }
                    }));
                }
                start.countDown(); for (var task : tasks) { task.get(15, TimeUnit.SECONDS); }
            }
            Assertions.assertEquals(1, won.get()); Assertions.assertEquals(1, lost.get());
            b.setOrderId(92);
            Assertions.assertThrows(java.sql.SQLException.class, () -> c.store.claim(PaymentDedupStore.intent(c.db.id, b, "sdk-91")));
            Assertions.assertEquals(0, c.receipts());
        }
    }

    /** 身份、参数、版本、正文摘要任一异常拒绝；不将其他键的完成记录作为成功。 */
    @Test void wrongIdentityArgumentsVersionsAndCorruptionFailClosed() throws Exception {
        try (var c = new Case(false)) {
            c.open();
            try (var f = c.shipment(true)) { f.ship(); f.captured(); f.flush(); Assertions.assertEquals(List.of(true), f.replies); }
            Assertions.assertThrows(RuntimeException.class, () -> c.store.lookup(c.db.id + 1, 91, "sdk-91", null));
            Assertions.assertThrows(RuntimeException.class, () -> c.store.lookup(c.db.id, 92, "sdk-91", null));
            Assertions.assertThrows(RuntimeException.class, () -> c.store.lookup(c.db.id, 91, "another-sdk", null));
            List<java.util.function.Consumer<PayItem>> changes = List.of(item -> item.setRmb(13), item -> item.setPayId(999),
                    item -> item.setSdkGoodsId(999), item -> item.setPayType(cn.game.games.net.game.module.recharge.PayType.Recharge),
                    item -> item.setAddId(999), item -> item.setAddCount(999), item -> item.getPaySubIds().add(999));
            for (var change : changes) {
                PayItem hot = c.db.reloadFresh().getPlayerModule().getPayItems(91); change.accept(hot);
                Assertions.assertThrows(RuntimeException.class, () -> c.store.lookup(c.db.id, 91, "sdk-91", hot));
            }
            c.execute("UPDATE " + c.receipt + " SET version=2 WHERE order_id=91");
            Assertions.assertThrows(RuntimeException.class, () -> c.store.lookup(c.db.id, 91, "sdk-91", null));
            c.execute("UPDATE " + c.receipt + " SET version=1,proof_sha256=REPEAT('0',64) WHERE order_id=91");
            Assertions.assertThrows(RuntimeException.class, () -> c.store.lookup(c.db.id, 91, "sdk-91", null));
            c.execute("UPDATE " + c.receipt + " SET proof=REPLACE(proof,'\"version\":1','\"version\":2'),proof_sha256=SHA2(proof,256) WHERE order_id=91");
            Assertions.assertThrows(RuntimeException.class, () -> c.store.lookup(c.db.id, 91, "sdk-91", null));
        }
    }

    /** 旧 V1 与历史 finish 均不自动回填，持久占位在新协调器中仍不允许重新发奖。 */
    @Test void legacyAndRestartedPendingOrdersRemainBlocked() throws Exception {
        try (var c = new Case(false)) {
            c.open();
            for (boolean proof : List.of(false, true)) {
                try (var f = c.shipment(true)) {
                    PayItem item = f.player.getPlayerModule().getPayItems(91); item.setSdkOrderId("sdk-91"); item.finish();
                    if (proof) { item.setCompletionState("REWARDS_COMPLETED"); item.prepareCompletionProof(c.db.id); item.confirmCompletionProof(); }
                    f.ship(); f.flush(); f.assertRejectedBeforeBusiness();
                }
            }
            c.store.claim(PaymentDedupStore.intent(c.db.id, PaymentDurableDedupTest.item(), "sdk-91"));
            try (var restarted = c.shipment(true)) { restarted.ship(); restarted.flush(); restarted.assertRejectedBeforeBusiness(); }
            Assertions.assertEquals(0, c.receipts());
        }
    }

    /** 默认关闭时缺新表保持旧支付可用；显式开启后缺表绝不回退旧凭证或放行奖励。 */
    @Test void disabledAndMissingTableDeploymentBoundaries() throws Exception {
        try (var c = new Case(false)) {
            c.open(); c.drop(c.receipt);
            try (var enabled = c.shipment(true)) { enabled.ship(); enabled.flush(); enabled.assertRejectedBeforeBusiness(); }
            try (var disabled = c.shipment(true)) {
                disabled.durableStore = null; disabled.ship(); disabled.captured(); disabled.flush();
                Assertions.assertEquals(List.of(true), disabled.replies); Assertions.assertEquals(1, disabled.awards.get());
            }
        }
    }

    /** 表存在但独立唯一约束丢失也属于依赖不可用，不等到奖后才发现。 */
    @Test void missingUniqueConstraintRejectsBeforeReward() throws Exception {
        try (var c = new Case(false)) {
            c.open(); c.execute("ALTER TABLE " + c.claim + " DROP INDEX uk_payment_claim_sdk");
            try (var f = c.shipment(true)) { f.ship(); f.flush(); f.assertRejectedBeforeBusiness(); }
        }
    }

    /** 保存等待超时不结束真实事务；迟到提交保留原失败与对象隔离，新进程可冷读有效凭证。 */
    @Test void timeoutLateCommitDoesNotThawOriginalOwner() throws Exception {
        try (var c = new Case(false)) {
            c.open(); c.db.gate.holdReceipt = true;
            try (var f = c.shipment(true)) {
                f.ship(); f.captured(); Assertions.assertTrue(c.db.gate.receiptEntered.await(10, TimeUnit.SECONDS));
                f.deadlines.getFirst().run(); f.flush(); Assertions.assertEquals(List.of(false), f.replies);
                Assertions.assertTrue(f.saves.status(f.id()).writeProtected());
                c.db.gate.receiptRelease.countDown(); Assertions.assertTrue(c.db.gate.receiptCommitted.await(10, TimeUnit.SECONDS));
                f.ship(); f.flush(); Assertions.assertEquals(List.of(false, false), f.replies);
                Assertions.assertEquals(1, f.awards.get()); Assertions.assertTrue(f.saves.status(f.id()).writeProtected());
                Assertions.assertEquals(PaymentDedupStore.Lookup.COMPLETED, c.store.lookup(c.db.id, 91, "sdk-91", null));
            }
        }
    }

    /** 真实提交后回执丢失保持 UNKNOWN；两个库内事实已提交，不能在原进程自动解锁。 */
    @Test void lostCommitAcknowledgementRemainsUnknown() throws Exception {
        try (var c = new Case(true)) {
            c.open(); c.db.gate.loseReceiptAcknowledgement = true;
            try (var f = c.shipment(true)) {
                f.ship(); f.flush(); Assertions.assertEquals(List.of(false), f.replies);
                Assertions.assertEquals("UNKNOWN", f.saves.status(f.id()).state());
                Assertions.assertEquals(1, c.receipts()); Assertions.assertEquals(1, c.db.reloadFresh().getPlayerModule().getGuideMap().get(700));
                f.ship(); f.flush(); Assertions.assertEquals(List.of(false, false), f.replies); Assertions.assertEquals(1, f.awards.get());
            }
        }
    }

    /** 奖励部分失败后仅留下占位，后续保存入口也被隔离；不自动补偿或重放外部副作用。 */
    @Test void partialRewardFailureFreezesWithoutInventingReceipt() throws Exception {
        try (var c = new Case(false)) {
            c.open();
            try (var f = c.shipment(true)) {
                f.reward = () -> { f.award(); return io.vertx.core.Future.failedFuture("injected partial reward"); };
                f.ship(); f.flush(); Assertions.assertEquals(List.of(false), f.replies);
                Assertions.assertEquals(1, f.awards.get()); Assertions.assertEquals(0, f.charges.get());
                Assertions.assertTrue(f.saves.status(f.id()).writeProtected()); Assertions.assertEquals(0, c.receipts());
                Assertions.assertThrows(PlayerSaveCoordinator.SaveFailure.class, () -> f.saves.preparing(f.player, f.id()));
                Assertions.assertEquals("{}", c.db.read().getModules());
            }
        }
    }

    /** 查询故障不会被解释为不存在，也不会触及购买事实、奖励或保存。 */
    @Test void queryFailureNeverBecomesPermissionToReward() throws Exception {
        try (var c = new Case(false)) {
            c.open(); c.db.gate.failEvidenceRead = true;
            try (var f = c.shipment(true)) { f.ship(); f.flush(); f.assertRejectedBeforeBusiness(); }
            Assertions.assertEquals(0, c.receipts());
        }
    }

    /** 新凭证覆盖真实离线奖励与原清理顺序，提交之前不回复、不释放临时对象。 */
    @Test void offlineShipmentCommitsReceiptBeforeReplyAndRelease() throws Exception {
        try (var c = new Case(true)) {
            c.open(); c.db.gate.holdReceipt = true;
            try (var f = c.shipment(false)) {
                f.ship(); f.captured(); Assertions.assertTrue(c.db.gate.receiptEntered.await(10, TimeUnit.SECONDS));
                Assertions.assertSame(f.player, f.owner); Assertions.assertTrue(f.replies.isEmpty());
                c.db.gate.receiptRelease.countDown(); f.flush();
                Assertions.assertEquals(List.of(true), f.replies); Assertions.assertNull(f.owner);
                Assertions.assertEquals(1, f.awards.get()); Assertions.assertEquals(1, f.charges.get());
                Assertions.assertEquals(PaymentDedupStore.Lookup.COMPLETED, c.store.lookup(c.db.id, 91, "sdk-91", null));
                Assertions.assertEquals(1, c.db.reloadFresh().getPlayerModule().getGuideMap().get(700));
            }
        }
    }

    /** 每个测试方法自己的表/玩家归属，不开放任意业务表名。 */
    private static final class Case implements AutoCloseable {
        /** 独立真实 Game 数据库 fixture；连接值只来自本轮临时 JavaProperties。 */
        final PlayerSaveDatabaseIT.DatabaseFixture db;
        /** 与迁移同构的两张独占临时表。 */
        final String suffix = UUID.randomUUID().toString().replace("-", "");
        final String claim = "s3d_claim_" + suffix, receipt = "s3d_receipt_" + suffix;
        /** 只有实际创建后才允许清理。 */
        final java.util.Set<String> created = new java.util.HashSet<>();
        /** 生产存储仅注入包内已验证的随机表后缀。 */
        final PaymentDedupStore store;
        /** 是否已成功占有本轮玩家。 */
        boolean seeded;

        /** @param spring 是否使用实际 SpringManaged 事务 */
        Case(boolean spring) throws Exception {
            db = new PlayerSaveDatabaseIT.DatabaseFixture(spring); store = new PaymentDedupStore(() -> db.factory, suffix);
        }

        /** 资源已进入 try-with-resources 后才建行/表，异常也精确清理。 */
        void open() throws Exception {
            db.seed(); seeded = true;
            String sql;
            try (InputStream in = getClass().getResourceAsStream("/db/migration/payment-durable-dedup-v1.sql")) {
                sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            sql = sql.replaceAll("(?m)^--.*$", "").replace("t_payment_fulfillment_claim", claim)
                    .replace("t_payment_fulfillment_receipt", receipt).replace(") ENGINE=InnoDB", ") ENGINE=InnoDB COMMENT='s3d_" + suffix + "'");
            String[] statements = sql.trim().split(";"); Assertions.assertEquals(2, statements.length);
            for (int i = 0; i < 2; i++) { execute(statements[i]); created.add(i == 0 ? claim : receipt); }
        }

        /** @param online 起始连接状态 @return 正式发货 Handler、真实邮箱和生产事务写入器 */
        PaymentDeliveryLifecycleTest.Fixture shipment(boolean online) {
            var f = new PaymentDeliveryLifecycleTest.Fixture(db.player().getData(), online, db.writer::write);
            f.durableStore = store; return f;
        }

        /** @param sql 仅本测试内部构造的 SQL，不接受外部表名或业务输入 @throws Exception 执行失败 */
        void execute(String sql) throws Exception {
            try (var c = db.source.getConnection(); var s = c.createStatement()) { s.setQueryTimeout(5); s.execute(sql); }
        }

        /** @return 新连接已提交凭证数 @throws Exception 冷查询失败 */
        int receipts() throws Exception {
            try (var c = db.source.getConnection(); var s = c.createStatement(); var r = s.executeQuery("SELECT COUNT(*) FROM " + receipt)) {
                Assertions.assertTrue(r.next()); return r.getInt(1);
            }
        }

        /** @param table 本轮创建表；核对持久 comment 后才允许 DROP @throws Exception 身份不符或清理失败 */
        void drop(String table) throws Exception {
            Assertions.assertTrue(created.contains(table));
            try (Connection c = db.source.getConnection(); var s = c.prepareStatement(
                    "SELECT table_comment FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name=?")) {
                s.setString(1, table);
                try (var r = s.executeQuery()) {
                    Assertions.assertTrue(r.next()); Assertions.assertEquals("s3d_" + suffix, r.getString(1));
                }
            }
            execute("DROP TABLE " + table); created.remove(table);
        }

        @Override public void close() throws Exception {
            // 先排除本轮后台提交，再清理自己的玩家/表，失败不跨越归属边界。
            db.close();
            try { if (seeded) { db.clean(); } }
            finally {
                for (String table : List.copyOf(created)) { drop(table); }
                try (var c = db.source.getConnection(); var s = c.prepareStatement(
                        "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name IN (?,?)")) {
                    s.setString(1, claim); s.setString(2, receipt);
                    try (var r = s.executeQuery()) { Assertions.assertTrue(r.next()); Assertions.assertEquals(0, r.getInt(1)); }
                }
                log.info("PAYMENT_DURABLE_FIXTURE_COLD_EMPTY game=1 tables=2");
            }
        }
    }
}
