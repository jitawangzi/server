package cn.game.games.diagnostics;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 单文件、不可覆盖的本地证据仓库；独立 HMAC key 是显式信任锚，摘要本身不是防篡改。
 * 同权限管理员、密钥泄漏和目录被同身份恶意替换不在防护范围；不宣称跨库或断电原子性。
 */
public final class PaymentArchiveStore {
    /** 自包含文件最大 4 MiB；完整玩家快照从不进入此文件。 */
    static final int MAX_BYTES = PaymentEvidenceCli.MAX_BYTES;
    /** 单个归档根只允许一个立即取得的进程锁，不无限等待或自动接管。 */
    private static final String LOCK = "archive.lock";
    /** 认证域和规范化规则随版本固定，不与其他 HMAC 用途混用。 */
    private static final byte[] DOMAIN = "S3C-PAYMENT-ARCHIVE-V1\n".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
    /** 独立故障注入边界，生产始终使用真实文件通道和重新打开的冷读。 */
    interface Disk {
        /** @param channel 新创建文件 @param bytes 完整规范字节；失败时保留半写产物 */
        default void write(FileChannel channel, byte[] bytes) throws IOException {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            while (buffer.hasRemaining()) { if (channel.write(buffer) <= 0) { throw new IOException("WRITE_STALLED"); } }
        }
        /** @param channel 已写入的文件通道 */
        default void force(FileChannel channel) throws IOException { channel.force(true); }
        /** @param path 固定受控文件名 @return 新通道冷读的有界字节 */
        default byte[] read(Path path) throws IOException { return boundedRead(path, MAX_BYTES); }
    }
    /** 无故障注入的真实文件操作。 */
    private static final Disk DISK = new Disk() {};

    /** 工具类不创建实例。 */
    private PaymentArchiveStore() {}

    /** @param root 显式绝对归档根 @param keyFile 根外部的私有 32 字节 key @param player 期望玩家 @param order 期望订单 @param reader 有界只读源 @return 脱敏回执 */
    public static Map<String, Object> archive(Path root, Path keyFile, long player, long order, PaymentEvidenceCli.Reader reader) throws Exception {
        return seal(root, keyFile, player, order, PaymentArchiveEvidence.collect(player, order, reader), DISK);
    }

    /** @param root 显式根 @param keyFile 外置信任锚 @param player 期望玩家 @param order 期望订单 @return 冷读认证结果，绝不授予业务操作权限 */
    public static Map<String, Object> verify(Path root, Path keyFile, long player, long order) throws Exception {
        return receipt("VERIFIED", readAuthenticated(root, keyFile, player, order));
    }

    /** @param root 显式根 @param keyFile 外部私有密钥 @param player 玩家 @param order 订单 @return 本次真实冷读认证的普通树，不能作为后续写入授权 */
    static JsonNode readAuthenticated(Path root, Path keyFile, long player, long order) throws Exception {
        Path directory = directory(root, false);
        byte[] key = key(keyFile, directory);
        try {
            return authenticate(DISK.read(artifact(directory, player, order)), key, player, order);
        } finally { java.util.Arrays.fill(key, (byte) 0); }
    }

    /** @param root 根 @param keyFile 独立 key @param player 玩家 @param order 订单 @param content 精确观察内容 @param disk 真实 IO 或故障边界 @return 冷读后回执 */
    static Map<String, Object> seal(Path root, Path keyFile, long player, long order, ObjectNode content, Disk disk) throws Exception {
        PaymentArchiveEvidence.validate(content, player, order);
        Path directory = directory(root, true);
        byte[] key = key(keyFile, directory);
        try {
            ObjectNode envelope = PaymentArchiveEvidence.JSON.createObjectNode();
            envelope.put("schemaVersion", 1); envelope.put("algorithm", "HMAC-SHA256");
            envelope.put("keyId", HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(key)));
            envelope.set("content", content.deepCopy());
            envelope.put("mac", HexFormat.of().formatHex(mac(key, canonical(envelope))));
            byte[] bytes = canonical(envelope);
            PaymentArchiveEvidence.require(bytes.length <= MAX_BYTES);
            Path target = artifact(directory, player, order);
            Path lockPath = directory.resolve(LOCK);
            if (!Files.exists(lockPath, LinkOption.NOFOLLOW_LINKS)) {
                try (var created = FileChannel.open(lockPath, Set.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE), attributes(false))) {
                    created.force(true);
                } catch (java.nio.file.FileAlreadyExistsException race) {
                    safeWarn(race);
                }
            }
            regular(lockPath);
            try (var lockChannel = FileChannel.open(lockPath, StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS)) {
                java.nio.channels.FileLock lock;
                try { lock = lockChannel.tryLock(); }
                catch (OverlappingFileLockException busy) { safeWarn(busy); throw new IOException("ARCHIVE_BUSY"); }
                if (lock == null) { throw new IOException("ARCHIVE_BUSY"); }
                try (lock) {
                    if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                        regular(target);
                        JsonNode old = authenticate(disk.read(target), key, player, order);
                        // 仅精确内容相同才幂等成功；不能把旧观察的回执当成新观察已持久化。
                        PaymentArchiveEvidence.require(MessageDigest.isEqual(canonical(old), canonical(content)));
                        return receipt("VERIFIED_EXISTING", old);
                    }
                    try (var channel = FileChannel.open(target, Set.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE), attributes(false))) {
                        disk.write(channel, bytes); disk.force(channel);
                    }
                    regular(target);
                    byte[] cold = disk.read(target);
                    JsonNode verified = authenticate(cold, key, player, order);
                    PaymentArchiveEvidence.require(MessageDigest.isEqual(bytes, cold)
                            && MessageDigest.isEqual(canonical(content), canonical(verified)));
                    return receipt("SEALED_AND_VERIFIED", verified);
                }
            }
        } finally { java.util.Arrays.fill(key, (byte) 0); }
    }

    /** @param bytes 磁盘有界字节 @param key 外部密钥 @param player 显式期望玩家 @param order 显式期望订单 @return 认证且结构核对后的内容 */
    static JsonNode authenticate(byte[] bytes, byte[] key, long player, long order) throws Exception {
        PaymentArchiveEvidence.require(bytes.length > 0 && bytes.length <= MAX_BYTES);
        JsonNode parsed = PaymentArchiveEvidence.JSON.readTree(bytes);
        PaymentArchiveEvidence.require(parsed.isObject() && PaymentArchiveEvidence.names(parsed).equals(
                Set.of("schemaVersion", "algorithm", "keyId", "content", "mac")));
        ObjectNode envelope = (ObjectNode) parsed;
        PaymentArchiveEvidence.require(envelope.path("schemaVersion").isInt() && envelope.path("schemaVersion").intValue() == 1
                && envelope.path("algorithm").asText().equals("HMAC-SHA256")
                && envelope.path("mac").isTextual() && envelope.path("mac").textValue().matches("[0-9a-f]{64}")
                && envelope.path("keyId").isTextual() && envelope.path("keyId").textValue().equals(
                        HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(key))));
        byte[] expected = HexFormat.of().parseHex(envelope.remove("mac").textValue());
        PaymentArchiveEvidence.require(MessageDigest.isEqual(expected, mac(key, canonical(envelope))));
        JsonNode content = envelope.path("content");
        PaymentArchiveEvidence.validate(content, player, order);
        return content;
    }

    /** @param key 本地独立密钥 @param bytes 规范化的完整信封（除 mac） @return JCA HMAC-SHA256 */
    private static byte[] mac(byte[] key, byte[] bytes) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(key, "HmacSHA256")); mac.update(DOMAIN); return mac.doFinal(bytes);
    }

    /** @param node 普通 JSON 树 @return V1 规范 UTF-8：对象键按 Java String 顺序、数组保序、整数十进制；不接受浮点和二进制 */
    static byte[] canonical(JsonNode node) throws Exception {
        return PaymentArchiveEvidence.JSON.writeValueAsBytes(sorted(node));
    }

    /** @param node 有界解析树 @return 递归排序对象，拒绝不在 V1 中的数值类型 */
    private static JsonNode sorted(JsonNode node) {
        if (node.isObject()) {
            ObjectNode out = PaymentArchiveEvidence.JSON.createObjectNode();
            var names = new java.util.TreeSet<String>(); node.fieldNames().forEachRemaining(names::add);
            for (String name : names) { out.set(name, sorted(node.get(name))); } return out;
        }
        if (node.isArray()) {
            var out = PaymentArchiveEvidence.JSON.createArrayNode(); for (JsonNode item : node) { out.add(sorted(item)); } return out;
        }
        PaymentArchiveEvidence.require(node.isNull() || node.isTextual() || node.isBoolean() || (node.isIntegralNumber() && node.canConvertToLong()));
        return node;
    }

    /** @param status 固定成功码 @param content 已验证内容 @return 不包含单号、正文、凭据、key 或账号的摘要 */
    private static Map<String, Object> receipt(String status, JsonNode content) {
        JsonNode assessment = content.path("assessment");
        boolean consistent = assessment.path("game").asText().equals("PERSISTED_V1_PROOF_CONSISTENT")
                && assessment.path("login").asText().equals("OBSERVED")
                && assessment.path("association").asText().equals("PLAYER_ORDER_AMOUNT_MATCH")
                && assessment.path("sdkBinding").asText().equals("MATCH");
        return Map.of("status", status, "archiveIntegrity", "HMAC_SHA256_VERIFIED", "source", "OFFLINE_STORAGE_NON_ATOMIC",
                "gameEvidence", content.path("assessment").path("game").asText(), "platformAuthenticity", "NOT_PROVEN",
                "transactionResolution", "NOT_PROVEN", "canDelete", false, "canResend", false,
                "evidenceClass", consistent ? "LOCAL_V1_FIELD_CONSISTENCY_ONLY" : "RISK_ONLY");
    }

    /** @param root 显式绝对目录 @param create 是否可创建根本身（不创建父级） @return 无链接且当前身份私有的目录 */
    private static Path directory(Path root, boolean create) throws IOException {
        absolute(root);
        if (!Files.exists(root, LinkOption.NOFOLLOW_LINKS) && create) {
            noLinks(root.getParent());
            try { Files.createDirectory(root, attributes(true)); }
            catch (java.nio.file.FileAlreadyExistsException race) { safeWarn(race); }
        }
        noLinks(root); PaymentArchiveEvidence.require(Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS));
        privateAccess(root); return root;
    }

    /** @param directory 受控根 @param player 正整数玩家 @param order 正整数订单 @return 无调用方自由文件名的目标 */
    static Path artifact(Path directory, long player, long order) {
        PaymentArchiveEvidence.require(player > 0 && order > 0); return directory.resolve("payment-" + player + "-" + order + ".json");
    }

    /** @param path 受控文件 @return 不返回内容，仅验证常规文件、无链接及私有权限 */
    private static void regular(Path path) throws IOException {
        noLinks(path); PaymentArchiveEvidence.require(Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)); privateAccess(path);
    }

    /** @param keyFile 显式独立密钥文件 @param root 归档根 @return 精确 32 字节，不缓存或输出 */
    private static byte[] key(Path keyFile, Path root) throws IOException {
        absolute(keyFile); PaymentArchiveEvidence.require(!keyFile.startsWith(root)); regular(keyFile);
        byte[] bytes = boundedRead(keyFile, 32);
        if (bytes.length != 32) { java.util.Arrays.fill(bytes, (byte) 0); throw new IOException("INVALID_KEY"); }
        return bytes;
    }

    /** @param path 无链接文件 @param max 字节上限 @return 重新打开读入的字节，增长或截断均由认证拒绝 */
    private static byte[] boundedRead(Path path, int max) throws IOException {
        regular(path);
        try (var channel = FileChannel.open(path, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS)) {
            if (channel.size() > max) { throw new IOException("INPUT_LIMIT"); }
            var out = new java.io.ByteArrayOutputStream(); ByteBuffer buffer = ByteBuffer.allocate(8192);
            while (channel.read(buffer) != -1) {
                buffer.flip(); if (out.size() + buffer.remaining() > max) { throw new IOException("INPUT_LIMIT"); }
                out.write(buffer.array(), 0, buffer.remaining()); buffer.clear();
            }
            return out.toByteArray();
        }
    }

    /** @param path 调用方显式绝对路径，不接受点段或隐式工作目录 */
    private static void absolute(Path path) {
        PaymentArchiveEvidence.require(path != null && path.isAbsolute());
        boolean windows = path.getFileSystem().supportedFileAttributeViews().contains("acl");
        if (windows) { PaymentArchiveEvidence.require(path.getRoot().toString().matches("[A-Za-z]:\\\\")); }
        for (Path part : path) {
            PaymentArchiveEvidence.require(!part.toString().equals(".") && !part.toString().equals("..")
                    && (!windows || !part.toString().contains(":")));
        }
    }

    /** @param path 已存在路径；逐级拒绝符号链接、junction/reparse 重定向，不追随归档中的任意路径 */
    private static void noLinks(Path path) throws IOException {
        absolute(path); Path current = path.getRoot();
        for (Path part : path) {
            current = current.resolve(part);
            PaymentArchiveEvidence.require(!Files.isSymbolicLink(current)
                    && current.toRealPath().equals(current.toRealPath(LinkOption.NOFOLLOW_LINKS))
                    && !Files.readAttributes(current, java.nio.file.attribute.BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).isOther());
        }
    }

    /** @return 当前 Java 执行身份，不借用其他执行者的 owner 或 ACL */
    private static UserPrincipal identity() throws IOException {
        // user.name 可被 JVM 参数覆盖，沙箱中也可能保留宿主值；必须从原生执行令牌取身份。
        String name;
        if (Path.of(".").getFileSystem().supportedFileAttributeViews().contains("acl")) {
            var token = new com.sun.security.auth.module.NTSystem();
            name = token.getDomain() + "\\" + token.getName();
        } else { name = new com.sun.security.auth.module.UnixSystem().getUsername(); }
        return Path.of(".").getFileSystem().getUserPrincipalLookupService().lookupPrincipalByName(name);
    }

    /** @param directory 是否创建目录 @return 创建时原子应用的私有 ACL/权限，不修改现有文件 ACL */
    static FileAttribute<?>[] attributes(boolean directory) throws IOException {
        if (Path.of(".").getFileSystem().supportedFileAttributeViews().contains("acl")) {
            AclEntry entry = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(identity())
                    .setPermissions(java.util.EnumSet.allOf(AclEntryPermission.class)).build();
            return new FileAttribute<?>[] {new FileAttribute<List<AclEntry>>() {
                @Override public String name() { return "acl:acl"; }
                @Override public List<AclEntry> value() { return List.of(entry); }
            }};
        }
        return new FileAttribute<?>[] {PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString(directory ? "rwx------" : "rw-------"))};
    }

    /** @param path 已存在目录或文件，拒绝其他身份的允许项，不通过修改 ACL 修复 */
    private static void privateAccess(Path path) throws IOException {
        UserPrincipal user = identity(); PaymentArchiveEvidence.require(Files.getOwner(path, LinkOption.NOFOLLOW_LINKS).equals(user));
        var acl = Files.getFileAttributeView(path, AclFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        if (acl != null) {
            for (AclEntry entry : acl.getAcl()) {
                PaymentArchiveEvidence.require(entry.type() != AclEntryType.ALLOW || entry.principal().equals(user));
            }
        } else {
            for (var permission : Files.getPosixFilePermissions(path, LinkOption.NOFOLLOW_LINKS)) {
                PaymentArchiveEvidence.require(!permission.name().startsWith("GROUP_") && !permission.name().startsWith("OTHERS_"));
            }
        }
    }

    /** @param failure 预期竞争异常，脱敏后保留栈帧，不输出路径、正文或配置消息 */
    static void safeWarn(Exception failure) {
        var safe = new IllegalStateException("PAYMENT_ARCHIVE_" + failure.getClass().getSimpleName()); safe.setStackTrace(failure.getStackTrace());
        org.slf4j.LoggerFactory.getLogger(PaymentArchiveStore.class).warn("支付归档未完成或遇到竞争，保留原始产物并等待显式核查", safe);
    }
}
