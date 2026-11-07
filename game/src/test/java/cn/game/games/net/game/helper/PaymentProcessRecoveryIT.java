package cn.game.games.net.game.helper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.net.game.module.recharge.PayItem;
import cn.game.games.net.game.module.recharge.PayType;
import cn.game.games.net.game.module.player.CountingModule;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.helper.ManagerHelper;
import io.vertx.core.json.JsonObject;

/**
 * 真实数据库与两独立 JVM 的充值验收。预期固定来自 Recharge 配置 ID=1 的 60 钻石，
 * 不用被测输出计算预期；故障点由子进程实际边界报告，终止使用强制退出而非业务异常。
 */
@Isolated
class PaymentProcessRecoveryIT {
    /** 失败时保留节点日志及本轮身份，便于复核。 */
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PaymentProcessRecoveryIT.class);
    /** 父进程只做准备、冷读和断言，不执行业务奖励。 */
    @BeforeAll static void metadata() { ClassManager.getInstance().init(); ManagerHelper.init(); }

    /** 同单重复经过跨进程正式传输，原节点和重启节点都不能再次增加资产。 */
    @Test void duplicateCrossProcessShipmentAndCommittedRestart() throws Exception {
        try (Case c = new Case("duplicate")) {
            c.open("none");
            Assertions.assertTrue(c.ship(8_000).getBoolean("success"));
            Assertions.assertTrue(c.ship(8_000).getBoolean("success"));
            c.cold(160, 1, true); c.observe(160, true);
            JsonObject old = c.state("target");
            Assertions.assertEquals(2, old.getInteger("received")); Assertions.assertEquals(1, old.getInteger("business"));
            c.restart(); c.cold(160, 1, true); c.observe(160, true);
            Assertions.assertTrue(c.ship(8_000).getBoolean("success"));
            Assertions.assertEquals(0, c.state("target").getInteger("business"));
            c.cold(160, 1, true); c.observe(160, true); c.pass();
        }
    }

    /** 远端未提交时等待超时，原任务仍完成；同一订单重试不得当作新交易。 */
    @Test void timedOutRequestCompletesAndRetryDoesNotRepeatReward() throws Exception {
        try (Case c = new Case("timeout")) {
            c.open("before_save");
            JsonObject timeout = c.ship(800);
            c.awaitGate("before_save", 160); Assertions.assertEquals("TIMEOUT", timeout.getString("replyFailure"));
            c.cold(100, 0, false); Files.writeString(c.directory.resolve("release"), "release");
            Assertions.assertTrue(c.ship(8_000).getBoolean("success"));
            Assertions.assertEquals(1, c.state("target").getInteger("business"));
            c.cold(160, 1, true); c.observe(160, true); c.pass();
        }
    }

    /** 已提交但响应尚未发送即崩溃，新进程冷读完成事实后可以成功去重。 */
    @Test void committedResponseLostThenCrashRecoversWithoutRewardReplay() throws Exception {
        try (Case c = new Case("lost_response")) {
            c.open("after_commit");
            Assertions.assertEquals("TIMEOUT", c.ship(800).getString("replyFailure"));
            c.awaitGate("after_commit", 160); c.cold(160, 1, true);
            c.restart(); c.observe(160, true); Assertions.assertTrue(c.ship(8_000).getBoolean("success"));
            Assertions.assertEquals(0, c.state("target").getInteger("business"));
            c.cold(160, 1, true); c.observe(160, true); c.pass();
        }
    }

    /** 占位后奖励前终止，重启保留待核对订单，不能静默返回成功或自动补发。 */
    @Test void crashAfterClaimBeforeRewardRemainsBlocked() throws Exception { crashUncommitted("before_reward", 100); }
    /** 奖励在内存完成但尚未保存，崩溃只恢复旧资产，独立占位阻止重发。 */
    @Test void crashAfterRewardBeforeSaveRemainsBlocked() throws Exception { crashUncommitted("before_save", 160); }
    /** 实际 UPDATE 和凭证 INSERT 后、commit 前终止，数据库回滚两者且保留占位。 */
    @Test void crashInsideTransactionBeforeCommitRemainsBlocked() throws Exception { crashUncommitted("before_commit", 160); }

    /** @param point 确切终止点 @param memoryDiamonds 该点内存资产预期 */
    private void crashUncommitted(String point, long memoryDiamonds) throws Exception {
        try (Case c = new Case(point)) {
            c.open(point); Assertions.assertEquals("TIMEOUT", c.ship(800).getString("replyFailure"));
            c.awaitGate(point, memoryDiamonds); c.cold(100, 0, false);
            c.restart(); c.observe(100, false);
            for (int retry = 0; retry < 2; retry++) {
                JsonObject rejected = c.ship(8_000);
                Assertions.assertFalse(rejected.containsKey("failure"), "必须收到真实业务拒绝，传输超时不能冒充拒绝");
                Assertions.assertFalse(rejected.containsKey("replyFailure"));
                Assertions.assertFalse(rejected.getBoolean("success"));
            }
            Assertions.assertEquals(2, c.state("target").getInteger("received"));
            Assertions.assertEquals(0, c.state("target").getInteger("business"));
            c.cold(100, 0, false); c.observe(100, false); c.pass();
        }
    }

    /** 一次场景拥有独立玩家、凭证表、ZK命名空间和进程；禁止复用存量业务数据。 */
    private static final class Case implements AutoCloseable {
        /** 原有真实数据库 fixture 负责生产 Mapper、唯一玩家身份和条件清理。 */
        final PlayerSaveDatabaseIT.DatabaseFixture db = new PlayerSaveDatabaseIT.DatabaseFixture(false);
        /** 本场景全部临时标识都来自新随机值。 */
        final String suffix = UUID.randomUUID().toString().replace("-", "");
        final String claim = "s3d_claim_" + suffix, receipt = "s3d_receipt_" + suffix;
        final Path directory;
        final JsonObject spec;
        /** 生命周期内创建的子进程、表和观测，不通过全机进程扫描结束服务。 */
        final java.util.Map<String, Process> processes = new java.util.HashMap<>();
        final java.util.Map<String, JsonObject> ready = new java.util.HashMap<>();
        final List<String> tables = new ArrayList<>();
        final List<JsonObject> observations = new ArrayList<>();
        boolean seeded, passed;

        /** @param name 场景名；不从外部输入表名或SQL */
        Case(String name) throws Exception {
            directory = Files.createDirectory(Path.of(System.getenv("PAYMENT_PROCESS_ROOT")).resolve(name + "_" + suffix));
            spec = new JsonObject().put("case", name).put("runId", db.runId).put("suffix", suffix).put("playerId", db.id)
                    .put("orderId", 91L).put("sdkOrderId", "process_" + suffix).put("zkRoot", "payment_process_" + suffix)
                    .put("sender", "payment_sender_" + suffix).put("target", "payment_target_" + suffix);
            Files.writeString(directory.resolve("scenario.json"), spec.encodePrettily());
        }

        /** @param gate 目标进程本轮暂停点；准备只写本轮新建数据 */
        void open(String gate) throws Exception {
            db.seed(); seeded = true;
            String sql;
            try (var input = getClass().getResourceAsStream("/db/migration/payment-durable-dedup-v1.sql")) {
                sql = new String(input.readAllBytes(), StandardCharsets.UTF_8).replaceAll("(?m)^--.*$", "")
                        .replace("t_payment_fulfillment_claim", claim).replace("t_payment_fulfillment_receipt", receipt)
                        .replace(") ENGINE=InnoDB", ") ENGINE=InnoDB COMMENT='process_" + suffix + "'");
            }
            String[] statements = sql.trim().split(";"); Assertions.assertEquals(2, statements.length);
            for (int i = 0; i < 2; i++) {
                try (var connection = db.source.getConnection(); var statement = connection.createStatement()) { statement.execute(statements[i]); }
                tables.add(i == 0 ? claim : receipt);
            }
            Player seed = new Player(db.read()); seed.getCurrencyModule().setCount(100001, 100);
            Assertions.assertEquals(0, seed.getModule(CountingModule.class).getCumulativeCount(ConditionTypeEnum.AccumulatedRecharge));
            Assertions.assertEquals(0, seed.getModule(CountingModule.class).getCumulativeCount(ConditionTypeEnum.RechargeCnt));
            Assertions.assertEquals(0, seed.getPlayerModule().getExpLevelMap().getValue(Asset.VIPExp.ID));
            Assertions.assertEquals(0, seed.getCurrencyModule().getCount(Asset.VIPExp.ID));
            PayItem item = new PayItem(); item.setOrderId(91); item.setPayId(1); item.setPayType(PayType.Recharge);
            item.setRmb(6); item.setSdkGoodsId(1001);
            seed.getPlayerModule().addPayItems(item);
            Assertions.assertEquals(PlayerSaveCoordinator.Outcome.COMMITTED,
                    PaymentDeliveryLifecycleTest.await(db.writer.write(PlayerDataSnapshot.capture(seed), false)).outcome());
            start("target", gate); start("sender", "none");
            Assertions.assertNotEquals(ready.get("target").getLong("pid"), ready.get("sender").getLong("pid"));
        }

        /** @param role 独立进程角色 @param gate 暂停点 */
        void start(String role, String gate) throws Exception {
            Files.deleteIfExists(directory.resolve(role + "-ready.json"));
            String classpath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
            var args = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin", "java.exe").toString(),
                    "-Xmx512m", "-Dio.netty.tryReflectionSetAccessible=true", "--add-opens=java.base/java.lang=ALL-UNNAMED",
                    "-cp", classpath, PaymentProcessNode.class.getName(), directory.toString(), role, gate));
            Process process = new ProcessBuilder(args).redirectErrorStream(true)
                    .redirectOutput(directory.resolve(role + "-" + UUID.randomUUID() + ".log").toFile()).start();
            processes.put(role, process);
            Path file = directory.resolve(role + "-ready.json");
            long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(60);
            while (!Files.exists(file)) {
                Assertions.assertTrue(process.isAlive(), "子进程退出，检查 " + directory);
                Assertions.assertTrue(System.nanoTime() < until, "节点未就绪 " + directory); Thread.sleep(50);
            }
            JsonObject identity = new JsonObject(Files.readString(file));
            Assertions.assertEquals(process.pid(), identity.getLong("pid")); ready.put(role, identity);
            observations.add(identity.copy().put("event", "ready"));
        }

        /** @param role 控制节点 @param path 固定回环控制请求 @return 实际节点返回 */
        JsonObject request(String role, String path) throws Exception {
            try (HttpClient client = HttpClient.newHttpClient()) {
                var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + ready.get(role).getInteger("port") + path))
                        .timeout(Duration.ofSeconds(15)).GET().build();
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                Assertions.assertEquals(200, response.statusCode()); return new JsonObject(response.body());
            }
        }

        /** @param timeout 实际EventBus等待毫秒 @return 发货结果 */
        JsonObject ship(long timeout) throws Exception {
            JsonObject response = request("sender", "/ship?timeout=" + timeout);
            observations.add(response.copy().put("event", "reply")); return response;
        }
        /** @param role 节点角色 @return 真实进程计数 */
        JsonObject state(String role) throws Exception { return request(role, "/state"); }

        /** @param point 预期故障点 @param diamonds 该点真实内存资产 */
        void awaitGate(String point, long diamonds) throws Exception {
            Path file = directory.resolve("gate.json"); long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
            while (!Files.exists(file)) { Assertions.assertTrue(System.nanoTime() < until, "故障点未到达 " + directory); Thread.sleep(20); }
            JsonObject marker = new JsonObject(Files.readString(file));
            Assertions.assertEquals(point, marker.getString("point")); Assertions.assertEquals(diamonds, marker.getLong("diamonds"));
            Assertions.assertEquals(processes.get("target").pid(), marker.getLong("pid"));
            observations.add(marker.put("event", "fault_gate"));
        }

        /** @param diamonds 持久资产 @param receipts 完成凭证数 @param complete 热订单完成状态 */
        void cold(long diamonds, int receipts, boolean complete) throws Exception {
            Player cold = new Player(db.read());
            Assertions.assertEquals(diamonds, cold.getCurrencyModule().getCount(100001));
            Assertions.assertEquals(complete, cold.getPlayerModule().getPayItems(91).isFinish());
            Assertions.assertEquals(complete, cold.getPlayerModule().getPayItems(91).hasCommittedCompletionProof(db.id, spec.getString("sdkOrderId")));
            var lookup = new PaymentDedupStore(() -> db.factory, suffix).lookup(db.id, 91, spec.getString("sdkOrderId"), cold.getPlayerModule().getPayItems(91));
            Assertions.assertEquals(complete ? PaymentDedupStore.Lookup.COMPLETED : PaymentDedupStore.Lookup.CLAIMED, lookup);
            Assertions.assertEquals(complete ? 6 : 0, cold.getModule(CountingModule.class).getCumulativeCount(ConditionTypeEnum.AccumulatedRecharge));
            Assertions.assertEquals(complete ? 1 : 0, cold.getModule(CountingModule.class).getCumulativeCount(ConditionTypeEnum.RechargeCnt));
            Assertions.assertEquals(complete ? 1 : 0, cold.getPlayerModule().getExpLevelMap().getValue(Asset.VIPExp.ID));
            Assertions.assertEquals(0, cold.getCurrencyModule().getCount(Asset.VIPExp.ID));
            try (var connection = db.source.getConnection(); var statement = connection.createStatement()) {
                try (var rows = statement.executeQuery("SELECT COUNT(*) FROM " + receipt)) { Assertions.assertTrue(rows.next()); Assertions.assertEquals(receipts, rows.getInt(1)); }
                try (var rows = statement.executeQuery("SELECT COUNT(*) FROM " + claim)) { Assertions.assertTrue(rows.next()); Assertions.assertEquals(1, rows.getInt(1)); }
            }
            observations.add(new JsonObject().put("event", "cold").put("diamonds", diamonds).put("receipts", receipts).put("complete", complete));
        }

        /** @param diamonds 预期资产 @param complete 完成状态；此响应来自新业务JVM自己的Player */
        void observe(long diamonds, boolean complete) throws Exception {
            JsonObject actual = request("target", "/observe");
            Assertions.assertEquals(ready.get("target").getString("instance"), actual.getString("instance"));
            Assertions.assertEquals(diamonds, actual.getLong("diamonds"));
            Assertions.assertEquals(complete ? 6 : 0, actual.getInteger("recharge"));
            Assertions.assertEquals(complete ? 1 : 0, actual.getInteger("rechargeCount"));
            Assertions.assertEquals(complete ? 1 : 0, actual.getInteger("vipLevel"));
            Assertions.assertEquals(0, actual.getLong("vipExp"));
            Assertions.assertEquals(complete, actual.getBoolean("finish")); Assertions.assertEquals(complete, actual.getBoolean("proof"));
            observations.add(actual.put("event", "target_observation"));
        }

        /** 强制终止原进程并确认退出，新的进程重新从数据库构造 Player。 */
        void restart() throws Exception {
            JsonObject old = state("target"); stop("target");
            // 强杀不会注销ZK临时成员；先等待真实租约退出，不用盲目重发交易探测路由。
            long began = System.nanoTime(), until = began + TimeUnit.SECONDS.toNanos(45);
            while (state("sender").getJsonArray("members").contains(old.getString("clusterNodeId"))) {
                Assertions.assertTrue(System.nanoTime() < until, "旧节点仍在实际集群视图中"); Thread.sleep(100);
            }
            observations.add(new JsonObject().put("event", "old_member_removed").put("oldClusterNodeId", old.getString("clusterNodeId"))
                    .put("waitMillis", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - began)));
            Files.deleteIfExists(directory.resolve("gate.json")); Files.deleteIfExists(directory.resolve("release"));
            start("target", "none");
            Assertions.assertNotEquals(old.getString("instance"), ready.get("target").getString("instance"));
            Assertions.assertEquals(0, state("target").getInteger("business"));
        }

        /** @param role 仅结束本对象创建的 Process */
        void stop(String role) throws Exception {
            Process process = processes.get(role);
            if (process == null) { return; }
            process.destroyForcibly(); Assertions.assertTrue(process.waitFor(15, TimeUnit.SECONDS), "进程未退出，不能清理数据");
            processes.remove(role);
            observations.add(new JsonObject().put("event", "forced_exit").put("role", role).put("pid", process.pid()).put("exit", process.exitValue()));
        }

        /** 所有场景断言完成后才允许清理专用玩家。 */
        void pass() { passed = true; }

        @Override public void close() throws Exception {
            List<Throwable> failures = new ArrayList<>();
            boolean cleanupPassed = false;
            try {
                for (String role : List.of("target", "sender")) {
                    try { stop(role); } catch (Throwable failure) { log.error("子进程停止失败 role={}", role, failure); failures.add(failure); }
                }
                if (!failures.isEmpty() || !processes.isEmpty()) { throw new IllegalStateException("OWNED_PROCESS_STILL_RUNNING"); }
                db.close();
                if (seeded) { db.clean(); }
                {
                    for (String table : tables) {
                        try (var connection = db.source.getConnection(); var check = connection.prepareStatement(
                                "SELECT table_comment FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name=?")) {
                            check.setString(1, table);
                            try (var rows = check.executeQuery()) { Assertions.assertTrue(rows.next()); Assertions.assertEquals("process_" + suffix, rows.getString(1)); }
                            try (var drop = connection.createStatement()) { drop.execute("DROP TABLE " + table); }
                        }
                    }
                }
                JsonObject zk = new JsonObject(PaymentProcessNode.configuration().getProperty("zk"));
                try (var curator = org.apache.curator.framework.CuratorFrameworkFactory.newClient(zk.getString("zookeeperHosts", "127.0.0.1"),
                        new org.apache.curator.retry.RetryOneTime(500))) {
                    curator.start(); Assertions.assertTrue(curator.blockUntilConnected(5, TimeUnit.SECONDS));
                    String path = "/" + spec.getString("zkRoot");
                    Assertions.assertEquals("/payment_process_" + suffix, path);
                    if (curator.checkExists().forPath(path) != null) { curator.delete().deletingChildrenIfNeeded().forPath(path); }
                    Assertions.assertNull(curator.checkExists().forPath(path));
                }
                cleanupPassed = true;
            } catch (Throwable failure) {
                log.error("本场景清理未完成，保留身份与证据", failure); failures.add(failure);
            } finally {
                Files.writeString(directory.resolve("evidence.json"), spec.copy().put("scenarioPassed", passed).put("cleanupPassed", cleanupPassed)
                        .put("passed", passed && cleanupPassed).put("cleanupErrors", failures.stream().map(f -> f.getClass().getSimpleName()).toList())
                        .put("observations", observations).encodePrettily());
                log.info("PAYMENT_PROCESS_CASE name={} passed={} cleanup={} evidence={}", spec.getString("case"), passed, cleanupPassed, directory);
            }
            if (!failures.isEmpty()) { Exception aggregate = new Exception("SCENARIO_CLEANUP_FAILED"); failures.forEach(aggregate::addSuppressed); throw aggregate; }
        }
    }
}
