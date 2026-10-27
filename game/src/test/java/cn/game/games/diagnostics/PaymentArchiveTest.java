package cn.game.games.diagnostics;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** 通过真实磁盘字节、独立 JCA 认证及故障注入验证归档，断言不依赖实现自报成功。 */
class PaymentArchiveTest {
    /** 每例只使用自己的私有随机目录和随机测试密钥，不产生生产归档。 */
    private static final class Fixture implements AutoCloseable {
        /** 本例独占目录。 */
        final Path base;
        /** 显式归档根，与 key 分离。 */
        final Path root;
        /** 外部信任锚。 */
        final Path key;
        /** 只在测试内存中使用的随机 key。 */
        final byte[] secret = new byte[32];
        /** 同一观察内容用于精确幂等及竞争。 */
        final ObjectNode content;
        /** @throws Exception 私有路径或夹具准备失败 */
        Fixture() throws Exception {
            Path parent = Path.of(System.getenv("S3_ARCHIVE_TEST_ROOT")).toAbsolutePath();
            base = Files.createTempDirectory(parent, "archive-test-", PaymentArchiveStore.attributes(true));
            root = base.resolve("archive"); key = Files.createFile(base.resolve("key.bin"), PaymentArchiveStore.attributes(false));
            // 密码学测试必须使用安全随机 key，不使用业务伪随机或固定默认密钥。
            new java.security.SecureRandom().nextBytes(secret); Files.write(key, secret);
            content = PaymentArchiveEvidence.collect(41, 91, PaymentEvidenceTest.reader(PaymentEvidenceTest.snapshot(PaymentEvidenceTest.completed())));
        }
        /** @return 固定文件名，不读取实现返回的路径 */
        Path file() { return root.resolve("payment-41-91.json"); }
        /** @return 实际持久化且冷读校验后的摘要 */
        Map<String, Object> seal() throws Exception { return PaymentArchiveStore.seal(root, key, 41, 91, content, new PaymentArchiveStore.Disk() {}); }
        @Override public void close() throws Exception {
            java.util.Arrays.fill(secret, (byte) 0);
            try (var paths = Files.walk(base)) {
                for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) { Files.delete(path); }
            }
        }
    }

    /** 真实写入后重新打开，独立实现规范排序和 JCA HMAC，验证全信封及私密字段隔离。 */
    @Test void diskBytesAuthenticateWithIndependentJcaAndColdRead() throws Exception {
        try (var f = new Fixture()) {
            Assertions.assertEquals("SEALED_AND_VERIFIED", f.seal().get("status"));
            byte[] bytes = Files.readAllBytes(f.file());
            ObjectNode envelope = (ObjectNode) new com.fasterxml.jackson.databind.ObjectMapper().readTree(bytes);
            String signature = envelope.remove("mac").asText();
            Mac mac = Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(f.secret, "HmacSHA256"));
            mac.update("S3C-PAYMENT-ARCHIVE-V1\n".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            Assertions.assertEquals(signature, HexFormat.of().formatHex(mac.doFinal(independentCanonical(envelope))));
            Assertions.assertEquals("secret-sdk", envelope.path("content").path("payItem").path("sdkOrderId").asText());
            Assertions.assertFalse(new String(bytes, java.nio.charset.StandardCharsets.UTF_8).contains(HexFormat.of().formatHex(f.secret)));
            var verified = PaymentArchiveStore.verify(f.root, f.key, 41, 91);
            Assertions.assertEquals("VERIFIED", verified.get("status"));
            Assertions.assertEquals(false, verified.get("canDelete")); Assertions.assertEquals(false, verified.get("canResend"));
        }
    }

    /** @param value 磁盘解析值 @return 测试独立排序并编码，不调用生产规范函数 */
    private static byte[] independentCanonical(JsonNode value) throws Exception {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        Object converted = mapper.convertValue(value, Object.class);
        return mapper.writeValueAsBytes(independentSort(converted));
    }

    /** @param value JSON 原生值 @return 独立递归 TreeMap 与保序列表 */
    private static Object independentSort(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> out = new java.util.TreeMap<>();
            map.forEach((key, item) -> out.put((String) key, independentSort(item))); return out;
        }
        if (value instanceof List<?> list) { return list.stream().map(PaymentArchiveTest::independentSort).toList(); }
        return value;
    }

    /** 改签名、身份、金额、业务结论、购买参数和观察时间，原 MAC 都必须失效。 */
    @Test void signaturePayloadIdentityAndConclusionsCannotBeRewritten() throws Exception {
        try (var f = new Fixture()) {
            f.seal(); byte[] original = Files.readAllBytes(f.file());
            for (String field : new String[] {"mac", "playerId", "orderId", "rmb", "payId", "canDelete", "gameReadStartedAt", "platformAuthenticity", "game"}) {
                ObjectNode envelope = (ObjectNode) PaymentArchiveEvidence.JSON.readTree(original);
                ObjectNode content = (ObjectNode) envelope.path("content");
                switch (field) {
                    case "mac" -> envelope.put("mac", "00".repeat(32));
                    case "rmb", "payId" -> ((ObjectNode) content.path("payItem")).put(field, 99);
                    case "canDelete" -> content.put(field, true);
                    case "platformAuthenticity" -> content.put(field, "PROVEN");
                    case "game" -> ((ObjectNode) content.path("assessment")).put(field, "PROVEN");
                    default -> content.put(field, 99);
                }
                Files.write(f.file(), PaymentArchiveEvidence.JSON.writeValueAsBytes(envelope));
                Assertions.assertThrows(Exception.class, () -> PaymentArchiveStore.verify(f.root, f.key, 41, 91), field);
            }
        }
    }

    /** 错 key、缺 key、key 放入归档目录、未知格式版本均不降级为摘要验证。 */
    @Test void missingWrongEmbeddedKeyAndUnknownVersionFailClosed() throws Exception {
        try (var f = new Fixture()) {
            f.seal(); byte[] original = Files.readAllBytes(f.file());
            Files.write(f.key, new byte[32]);
            Assertions.assertThrows(Exception.class, () -> PaymentArchiveStore.verify(f.root, f.key, 41, 91));
            Files.write(f.key, f.secret);
            Path internal = Files.createFile(f.root.resolve("key.bin"), PaymentArchiveStore.attributes(false)); Files.write(internal, f.secret);
            Assertions.assertThrows(Exception.class, () -> PaymentArchiveStore.verify(f.root, internal, 41, 91));
            ObjectNode envelope = (ObjectNode) PaymentArchiveEvidence.JSON.readTree(original); envelope.put("schemaVersion", 2);
            Files.write(f.file(), PaymentArchiveEvidence.JSON.writeValueAsBytes(envelope));
            Assertions.assertThrows(Exception.class, () -> PaymentArchiveStore.verify(f.root, f.key, 41, 91));
            Files.delete(f.key);
            Assertions.assertThrows(Exception.class, () -> PaymentArchiveStore.verify(f.root, f.key, 41, 91));
        }
    }

    /** 即使 MAC 正确，错期望单号也不可接受；文件重命名不能换单。 */
    @Test void validMacDoesNotAuthorizeDifferentExpectedIdentity() throws Exception {
        try (var f = new Fixture()) {
            f.seal(); byte[] bytes = Files.readAllBytes(f.file());
            Assertions.assertThrows(Exception.class, () -> PaymentArchiveStore.authenticate(bytes, f.secret, 42, 91));
            Assertions.assertThrows(Exception.class, () -> PaymentArchiveStore.authenticate(bytes, f.secret, 41, 92));
            Path renamed = Files.createFile(f.root.resolve("payment-41-92.json"), PaymentArchiveStore.attributes(false)); Files.write(renamed, bytes);
            Assertions.assertThrows(Exception.class, () -> PaymentArchiveStore.verify(f.root, f.key, 41, 92));
        }
    }

    /** 重复键、尾随文档、超深、超大、截断、浮点都不能通过格式或认证门禁。 */
    @Test void ambiguousDeepOversizedAndTruncatedArchivesAreRejected() throws Exception {
        try (var f = new Fixture()) {
            f.seal(); String original = Files.readString(f.file());
            for (String bad : new String[] {original.replace("\"schemaVersion\":1", "\"schemaVersion\":1,\"schemaVersion\":1"),
                    original + "{}", "[".repeat(70) + "0" + "]".repeat(70), " ".repeat(PaymentArchiveStore.MAX_BYTES + 1),
                    original.substring(0, original.length() / 2), original.replace("\"schemaVersion\":1", "\"schemaVersion\":1.0")}) {
                Files.writeString(f.file(), bad);
                Assertions.assertThrows(Exception.class, () -> PaymentArchiveStore.verify(f.root, f.key, 41, 91));
            }
        }
    }

    /** 同一完整观察可幂等验证；内容或 key 变化不得覆写，原文件字节始终不变。 */
    @Test void identicalObservationIsIdempotentAndConflictNeverOverwrites() throws Exception {
        try (var f = new Fixture()) {
            f.seal(); byte[] before = Files.readAllBytes(f.file());
            Assertions.assertEquals("VERIFIED_EXISTING", f.seal().get("status"));
            f.content.put("loginReadEndedAt", f.content.path("loginReadEndedAt").longValue() + 1);
            Assertions.assertThrows(Exception.class, f::seal);
            Assertions.assertArrayEquals(before, Files.readAllBytes(f.file()));
            Files.write(f.key, new byte[32]); Assertions.assertThrows(Exception.class, f::seal);
            Assertions.assertArrayEquals(before, Files.readAllBytes(f.file()));
        }
    }

    /** 多线程同时争抢同一文件，只允许精确内容成功或有界忙失败，最终完整且不覆写。 */
    @Test void concurrentWritersPublishOnlyOneCompleteAuthenticatedFile() throws Exception {
        try (var f = new Fixture(); var workers = Executors.newFixedThreadPool(8)) {
            CountDownLatch ready = new CountDownLatch(8), start = new CountDownLatch(1);
            var futures = new ArrayList<java.util.concurrent.Future<Boolean>>();
            for (int i = 0; i < 8; i++) {
                futures.add(workers.submit(() -> {
                    ready.countDown(); Assertions.assertTrue(start.await(5, TimeUnit.SECONDS));
                    try { f.seal(); return true; }
                    catch (java.io.IOException busy) { PaymentArchiveStore.safeWarn(busy); return false; }
                }));
            }
            try { Assertions.assertTrue(ready.await(5, TimeUnit.SECONDS)); } finally { start.countDown(); }
            int success = 0; for (var future : futures) { if (future.get(10, TimeUnit.SECONDS)) { success++; } }
            Assertions.assertTrue(success > 0);
            Assertions.assertEquals("VERIFIED", PaymentArchiveStore.verify(f.root, f.key, 41, 91).get("status"));
            try (var files = Files.list(f.root)) { Assertions.assertEquals(2, files.count()); }
        }
        // 不同完整观察也实际竞争，最多一个内容获准；失败者不能把胜者内容当成自己的成功。
        try (var f = new Fixture(); var workers = Executors.newFixedThreadPool(2)) {
            ObjectNode changed = f.content.deepCopy(); changed.put("loginReadEndedAt", changed.path("loginReadEndedAt").longValue() + 1);
            CountDownLatch start = new CountDownLatch(1); var results = new ArrayList<java.util.concurrent.Future<ObjectNode>>();
            for (ObjectNode candidate : List.of(f.content, changed)) {
                results.add(workers.submit(() -> {
                    Assertions.assertTrue(start.await(5, TimeUnit.SECONDS));
                    try { PaymentArchiveStore.seal(f.root, f.key, 41, 91, candidate, new PaymentArchiveStore.Disk() {}); return candidate; }
                    catch (java.io.IOException | IllegalArgumentException conflict) { PaymentArchiveStore.safeWarn(conflict); return null; }
                }));
            }
            start.countDown(); ObjectNode winner = null;
            for (var result : results) {
                ObjectNode accepted = result.get(10, TimeUnit.SECONDS);
                if (accepted != null) { Assertions.assertNull(winner); winner = accepted; }
            }
            Assertions.assertNotNull(winner);
            Assertions.assertArrayEquals(independentCanonical(winner), independentCanonical(
                    PaymentArchiveStore.authenticate(Files.readAllBytes(f.file()), f.secret, 41, 91)));
        }
    }

    /** 文件锁被另一进程持有时立即失败，释放后才可正常封存。 */
    @Test void crossProcessLockIsBoundedAndReleasedOnExit() throws Exception {
        try (var f = new Fixture()) {
            f.seal(); Path lock = f.root.resolve("archive.lock"), ready = f.base.resolve("ready");
            Process child = new ProcessBuilder(javaCommand(), "-cp", classpath(), LockHolder.class.getName(), lock.toString(), ready.toString())
                    .redirectErrorStream(true).redirectOutput(f.base.resolve("child.log").toFile()).start();
            try {
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
                while (!Files.exists(ready) && child.isAlive() && System.nanoTime() < deadline) { Thread.sleep(10); }
                Assertions.assertTrue(Files.exists(ready), "子进程必须实际持锁");
                Assertions.assertThrows(java.io.IOException.class, f::seal);
            } finally { child.destroyForcibly(); Assertions.assertTrue(child.waitFor(10, TimeUnit.SECONDS)); }
            Assertions.assertEquals("VERIFIED_EXISTING", f.seal().get("status"));
        }
    }

    /** 测试专用独立进程持锁，不运行服务器或业务。 */
    public static final class LockHolder {
        /** @param args 锁路径和就绪标记，均由本测试创建 */
        public static void main(String[] args) throws Exception {
            try (var channel = java.nio.channels.FileChannel.open(Path.of(args[0]), java.nio.file.StandardOpenOption.WRITE);
                    var lock = channel.lock()) {
                Files.createFile(Path.of(args[1])); Thread.sleep(30_000);
            }
        }
    }

    /** 写入/force/冷读故障都不能报告成功；真实磁盘上半写产物保留，不盲目覆盖修复。 */
    @Test void partialWriteForceAndColdReadFailuresNeverReportSuccessOrRepair() throws Exception {
        for (int mode = 0; mode < 4; mode++) {
            try (var f = new Fixture()) {
                final int fault = mode;
                var disk = new PaymentArchiveStore.Disk() {
                    @Override public void write(java.nio.channels.FileChannel channel, byte[] bytes) throws java.io.IOException {
                        if (fault == 0) { channel.write(java.nio.ByteBuffer.wrap(new byte[] {'{'})); throw new java.io.IOException("DISK_FULL_SECRET"); }
                        PaymentArchiveStore.Disk.super.write(channel, bytes);
                    }
                    @Override public void force(java.nio.channels.FileChannel channel) throws java.io.IOException {
                        if (fault == 1) { throw new java.io.IOException("FORCE_FAILED_SECRET"); }
                        PaymentArchiveStore.Disk.super.force(channel);
                    }
                    @Override public byte[] read(Path path) throws java.io.IOException {
                        if (fault == 2) { throw new java.io.IOException("READ_FAILED_SECRET"); }
                        if (fault == 3) { return "{}".getBytes(java.nio.charset.StandardCharsets.UTF_8); }
                        return PaymentArchiveStore.Disk.super.read(path);
                    }
                };
                Assertions.assertThrows(Exception.class, () -> PaymentArchiveStore.seal(f.root, f.key, 41, 91, f.content, disk));
                Assertions.assertTrue(Files.exists(f.file()));
                byte[] before = Files.readAllBytes(f.file());
                if (fault == 0) {
                    Assertions.assertArrayEquals(new byte[] {'{'}, before);
                    Assertions.assertThrows(Exception.class, f::seal);
                } else {
                    // force 失败后的磁盘可能完整；本轮仍失败，后续只能显式冷读验证，不能冒充原轮成功。
                    Assertions.assertEquals("VERIFIED", PaymentArchiveStore.verify(f.root, f.key, 41, 91).get("status"));
                }
                Assertions.assertArrayEquals(before, Files.readAllBytes(f.file()));
            }
        }
    }

    /** 普通读取失败、超限、重复键和金额/归属冲突不得产生归档或回执。 */
    @Test void unsafeOrUnreadableSourcesProduceNoArchive() throws Exception {
        String raw = PaymentEvidenceTest.snapshot(PaymentEvidenceTest.completed());
        for (String bad : new String[] {"{", "{}{}", raw.replace("\"rmb\":12", "\"rmb\":13"),
                raw.replace("\"playerId\":41", "\"playerId\":42"), raw.replace("\"orderId\":91", "\"orderId\":92"),
                raw.replace("\"rmb\":12", "\"rmb\":12,\"rmb\":12"), " ".repeat(PaymentEvidenceCli.MAX_BYTES + 1),
                raw.replace("\"isFinish\":true", "\"isFinish\":true,\"futureSchemaPrivacy\":\"secret\""),
                raw.replace("\"sdkOrderId\":\"secret-sdk\"", "\"sdkOrderId\":{\"privateAccount\":\"secret\"}"),
                "[".repeat(70) + "0" + "]".repeat(70)}) {
            try (var f = new Fixture()) {
                Assertions.assertThrows(Exception.class, () -> PaymentArchiveStore.archive(f.root, f.key, 41, 91, PaymentEvidenceTest.reader(bad)));
                Assertions.assertFalse(Files.exists(f.root));
            }
        }
        try (var f = new Fixture()) {
            var failed = new PaymentEvidenceCli.Reader() {
                @Override public String game(long id) throws Exception { throw new java.sql.SQLTimeoutException("secret"); }
                @Override public PaymentEvidenceCli.LoginOrder login(long id) throws Exception { throw new java.sql.SQLException("secret"); }
            };
            Assertions.assertThrows(Exception.class, () -> PaymentArchiveStore.archive(f.root, f.key, 41, 91, failed));
            Assertions.assertFalse(Files.exists(f.root));
        }
    }

    /** 旧 finish、未知 proof 及执行中订单只能成为风险证据，绝不升级为业务完成。 */
    @Test void historicalAndUnknownProofsRetainRiskWithoutDeleteOrResendAuthority() throws Exception {
        for (String state : new String[] {"legacy", "unknown", "executing"}) {
            try (var f = new Fixture()) {
                var item = PaymentEvidenceTest.completed();
                String raw = PaymentEvidenceTest.snapshot(item);
                if (state.equals("legacy")) {
                    item = new cn.game.games.net.game.module.recharge.PayItem(); item.setOrderId(91); item.setRmb(12); item.setFinish(true);
                    raw = PaymentEvidenceTest.snapshot(item);
                } else if (state.equals("unknown")) { raw = raw.replace("\"version\":1", "\"version\":99"); }
                else { raw = raw.replace("REWARDS_COMPLETED", "EXECUTING"); }
                f.content.setAll(PaymentArchiveEvidence.collect(41, 91, PaymentEvidenceTest.reader(raw)));
                f.seal(); var result = PaymentArchiveStore.verify(f.root, f.key, 41, 91);
                Assertions.assertNotEquals("PERSISTED_V1_PROOF_CONSISTENT", result.get("gameEvidence"));
                Assertions.assertEquals(false, result.get("canDelete")); Assertions.assertEquals(false, result.get("canResend"));
                Assertions.assertEquals("NOT_PROVEN", result.get("transactionResolution"));
                Assertions.assertEquals("RISK_ONLY", result.get("evidenceClass"));
            }
        }
    }

    /** 只保留指定 PayItem 和最小 Login，不保留账号、其他订单或整个模块；源 JSON 不变。 */
    @Test void collectionPreservesSourceAndExcludesOtherPlayerData() throws Exception {
        try (var f = new Fixture()) {
            String source = PaymentEvidenceTest.snapshot(PaymentEvidenceTest.completed());
            source = source.replace("\"payItems\":", "\"privateAccount\":\"PRIVATE_ACCOUNT_MARKER\",\"payItems\":");
            String original = source;
            ObjectNode content = PaymentArchiveEvidence.collect(41, 91, PaymentEvidenceTest.reader(source));
            Assertions.assertEquals(original, source);
            Assertions.assertFalse(content.toString().contains("PRIVATE_ACCOUNT_MARKER"));
            Assertions.assertFalse(content.toString().contains("guideMap"));
            Assertions.assertEquals(12, content.path("payItem").path("rmb").intValue());
            Assertions.assertEquals(1200, content.path("loginOrder").path("price").intValue());
        }
    }

    /** 显式根、点段、归档文件目录替换及 key 路径边界都必须拒绝。 */
    @Test void pathsAndNonRegularTargetsFailClosed() throws Exception {
        try (var f = new Fixture()) {
            Assertions.assertThrows(Exception.class, () -> PaymentArchiveStore.seal(Path.of("relative"), f.key, 41, 91, f.content, new PaymentArchiveStore.Disk() {}));
            Assertions.assertThrows(Exception.class, () -> PaymentArchiveStore.seal(f.base.resolve("..\\escape"), f.key, 41, 91, f.content, new PaymentArchiveStore.Disk() {}));
            if (System.getProperty("os.name").startsWith("Windows")) {
                Assertions.assertThrows(Exception.class, () -> PaymentArchiveStore.seal(Path.of("\\\\localhost\\share\\archive"), f.key, 41, 91, f.content, new PaymentArchiveStore.Disk() {}));
                Assertions.assertThrows(Exception.class, () -> PaymentArchiveStore.seal(f.base.resolve("root:stream"), f.key, 41, 91, f.content, new PaymentArchiveStore.Disk() {}));
            }
            f.seal(); Files.delete(f.file()); Files.createDirectory(f.file(), PaymentArchiveStore.attributes(true));
            Assertions.assertThrows(Exception.class, f::seal);
            Assertions.assertThrows(Exception.class, () -> PaymentArchiveStore.verify(f.root, f.key, 41, 91));
        }
    }

    /** Windows junction（或 POSIX symlink）必须被实际拒绝，测试不以跳过替代。 */
    @Test void reparseRedirectedRootCannotEscapeControlledDirectory() throws Exception {
        try (var f = new Fixture()) {
            Path outside = Files.createDirectory(f.base.resolve("outside"), PaymentArchiveStore.attributes(true));
            Path link = f.base.resolve("redirect");
            if (System.getProperty("os.name").startsWith("Windows")) {
                Process command = new ProcessBuilder("cmd.exe", "/c", "mklink", "/J", link.toString(), outside.toString())
                        .redirectErrorStream(true).redirectOutput(f.base.resolve("junction.log").toFile()).start();
                Assertions.assertTrue(command.waitFor(10, TimeUnit.SECONDS)); Assertions.assertEquals(0, command.exitValue());
            } else { Files.createSymbolicLink(link, outside); }
            try {
                Assertions.assertThrows(Exception.class, () -> PaymentArchiveStore.seal(link, f.key, 41, 91, f.content, new PaymentArchiveStore.Disk() {}));
                try (var files = Files.list(outside)) { Assertions.assertEquals(0, files.count()); }
            } finally { Files.delete(link); }
        }
    }

    /** 新 JVM 冷读 CLI 的 stdout 不包含 SDK 单号、key、账号、正文或输入路径；失败退出非零。 */
    @Test void standaloneVerifyHasRedactedStdoutAndNonzeroFailureExit() throws Exception {
        try (var f = new Fixture()) {
            f.seal();
            for (boolean valid : new boolean[] {true, false}) {
                Path output = f.base.resolve(valid ? "stdout.json" : "failure.json");
                Process child = new ProcessBuilder(javaCommand(), "-Duser.name=archive-test-invalid-user", "-Dlog4j.configurationFile=" + Path.of("target/classes/payment-evidence-log4j2.xml").toAbsolutePath().toUri(),
                        "-cp", classpath(), PaymentArchiveCli.class.getName(), "verify", "41", valid ? "91" : "92", f.root.toString(), f.key.toString())
                        .redirectOutput(output.toFile()).redirectError(f.base.resolve("stderr.log").toFile()).start();
                Assertions.assertTrue(child.waitFor(15, TimeUnit.SECONDS)); Assertions.assertEquals(valid ? 0 : 1, child.exitValue());
                String text = Files.readString(output); JsonNode result = PaymentArchiveEvidence.JSON.readTree(text);
                Assertions.assertEquals(valid ? "VERIFIED" : "FAILED_RETAIN_SOURCE", result.path("status").asText());
                for (String secret : new String[] {"secret-sdk", f.root.toString(), f.key.toString(), HexFormat.of().formatHex(f.secret), "payItem", "loginOrder"}) {
                    Assertions.assertFalse(text.contains(secret));
                }
            }
        }
    }

    /** @return 当前 JDK 的子进程可执行路径 */
    private static String javaCommand() { return Path.of(System.getProperty("java.home"), "bin", "java").toString(); }
    /** @return 当前 Surefire 提供的真实测试 classpath，子进程不使用历史业务 JAR */
    private static String classpath() { return System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")); }
}
