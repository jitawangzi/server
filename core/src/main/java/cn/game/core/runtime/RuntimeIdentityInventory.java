package cn.game.core.runtime;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;

import cn.game.protocol.protobuf.PbProtocol;

/**
 * 从本进程实际类路径及 Apollo 客户端读取启动身份，不读取测试调用者的期望清单。
 * 仅支持显式准备的独立制品目录；配置更新后永久拒绝本次运行的严格身份观察。
 */
public final class RuntimeIdentityInventory {
    /** 参与启动和业务参数加载的配置空间，不保存其明文到报告。 */
    private static final List<String> NAMESPACES = List.of("application", "config", "jdbc", "redisson", "zookeeper");
    /** 配置曾发生更新时不能通过恢复原字节把旧运行重新标成已核验。 */
    private final AtomicBoolean changed = new AtomicBoolean();
    /** 独立制品目录，所有实际类路径条目必须在此目录中。 */
    private final Path root;
    /** 记录启动前的实际 XML 加载代数；文件恢复不能恢复旧代数。 */
    private final long resourceVersion = cn.game.util.file.WatchServiceManager.getInstance().resourceVersion();

    /**
     * 注册变化观察；必须在业务加载配置之前创建。
     * @param root 本次不可原地重建的制品根目录
     * @param watch 是否注册运行期间的 Apollo 变化监听
     */
    public RuntimeIdentityInventory(Path root, boolean watch) throws Exception {
        this.root = root.toRealPath();
        if (cn.game.util.runtime.FrozenApollo.enabled()) {
            cn.game.util.runtime.FrozenApollo.INSTANCE.assertUnchanged();
        }
        if (watch) {
            for (String namespace : NAMESPACES) {
                ConfigService.getConfig(namespace).addChangeListener(event -> changed.set(true));
            }
            for (String name : springFiles()) {
                ConfigService.getConfigFile(name, ConfigFileFormat.XML).addChangeListener(event -> changed.set(true));
            }
        }
    }

    /**
     * 采集本进程实际输入；配置正文只进入摘要计算。
     * @param targetId 实际进程节点标识，来自服务启动参数
     * @return 可公开给本地测试的白名单字段
     */
    public Map<String, String> capture(String targetId) throws Exception {
        if (resourceVersion != cn.game.util.file.WatchServiceManager.getInstance().resourceVersion()) { throw new IllegalStateException("RUNTIME_RESOURCE_CHANGED"); }
        if (changed.get()) { throw new IllegalStateException("RUNTIME_CONFIG_CHANGED"); }
        if (cn.game.util.runtime.FrozenApollo.enabled()) { cn.game.util.runtime.FrozenApollo.INSTANCE.assertUnchanged(); }
        if (targetId == null || !targetId.matches("[A-Za-z0-9_.-]{1,100}")) {
            throw new IllegalArgumentException("RUNTIME_TARGET_INVALID");
        }
        Properties build = new Properties();
        try (InputStream in = RuntimeIdentityInventory.class.getResourceAsStream("/runtime-build.properties")) {
            if (in == null) { throw new IllegalStateException("RUNTIME_BUILD_METADATA_MISSING"); }
            build.load(in);
        }
        String source = build.getProperty("sourceDigest", "");
        String buildId = build.getProperty("buildId", "");
        if (!source.matches("[0-9a-f]{64}") || !buildId.matches("[A-Za-z0-9_.-]{1,120}")) {
            throw new IllegalStateException("RUNTIME_BUILD_METADATA_INVALID");
        }
        List<Path> classpath = new ArrayList<>();
        for (String entry : System.getProperty("java.class.path").split(Pattern.quote(java.io.File.pathSeparator))) {
            if (entry.isBlank()) { throw new IllegalStateException("RUNTIME_CLASSPATH_INVALID"); }
            classpath.add(Path.of(entry));
        }
        Map<String, String> config = new TreeMap<>();
        if (cn.game.util.runtime.FrozenApollo.enabled()) { config.putAll(cn.game.util.runtime.FrozenApollo.INSTANCE.identityValues()); }
        for (String namespace : NAMESPACES) {
            var loaded = ConfigService.getConfig(namespace);
            for (String key : loaded.getPropertyNames()) { config.put(namespace + "/" + key, loaded.getProperty(key, "")); }
        }
        assertHotUpdateDisabled(ConfigService.getConfig("config").getBooleanProperty("hot.update", false));
        for (String name : springFiles()) {
            String content = ConfigService.getConfigFile(name, ConfigFileFormat.XML).getContent();
            if (content == null || content.isBlank()) { throw new IllegalStateException("RUNTIME_SPRING_CONFIG_MISSING"); }
            config.put("spring/" + name, content);
        }
        for (String key : List.of("app.id", "env", "apollo.cluster", "server.run.mode", "defaultNetworkIp", "vertx.zookeeper.hosts")) {
            config.put("process/" + key, System.getProperty(key, ""));
        }
        Map<String, String> data = new TreeMap<>();
        String jdbc = ConfigService.getConfig("jdbc").getProperty("dburl", "");
        if (jdbc.isBlank()) { throw new IllegalStateException("RUNTIME_DATABASE_CONFIG_MISSING"); }
        data.put("jdbc", jdbc);
        data.put("redis", ConfigService.getConfig("redisson").getProperty("redisson", ""));
        data.put("zookeeper", ConfigService.getConfig("zookeeper").getProperty("zk", ""));
        Map<String, String> result = new LinkedHashMap<>();
        result.put("schemaVersion", "1.0");
        result.put("targetId", targetId);
        result.put("buildId", buildId);
        result.put("sourceDigest", source);
        result.put("artifactSha256", digestPaths(root, classpath));
        result.put("protocolSha256", digestPaths(root, List.of(Path.of(PbProtocol.class.getProtectionDomain().getCodeSource().getLocation().toURI()))));
        result.put("configSha256", digestValues(config));
        result.put("dataNamespace", "storage-sha256:" + digestValues(data));
        if (resourceVersion != cn.game.util.file.WatchServiceManager.getInstance().resourceVersion()) { throw new IllegalStateException("RUNTIME_RESOURCE_CHANGED"); }
        if (changed.get()) { throw new IllegalStateException("RUNTIME_CONFIG_CHANGED"); }
        if (cn.game.util.runtime.FrozenApollo.enabled()) { cn.game.util.runtime.FrozenApollo.INSTANCE.assertUnchanged(); }
        return result;
    }

    /** 严格身份不能覆盖工作目录中的动态类重定义，必须在启动前拒绝启用热更新。 */
    public static void assertHotUpdateDisabled(boolean enabled) {
        if (enabled) { throw new IllegalStateException("RUNTIME_HOT_UPDATE_UNSUPPORTED"); }
    }

    /** @return 实际 Spring 加载器声明的 XML 名称，拒绝空配置。 */
    private static List<String> springFiles() {
        String value = ConfigService.getAppConfig().getProperty("springArgs", "");
        List<String> files = new ArrayList<>();
        for (String item : value.split(",")) {
            if (!item.matches("[A-Za-z0-9_-]+\\.xml")) { throw new IllegalStateException("RUNTIME_SPRING_ARGS_INVALID"); }
            files.add(item.substring(0, item.length() - 4));
        }
        return files;
    }

    /**
     * 绑定路径顺序、相对名称和文件原字节，拒绝逃出制品目录及链接。
     * @param root 允许读取的制品根
     * @param paths 按实际类加载顺序排列的条目
     * @return SHA-256 小写摘要
     */
    public static String digestPaths(Path root, List<Path> paths) throws Exception {
        Path base = root.toRealPath();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        int index = 0;
        for (Path path : paths) {
            Path absolute = path.toAbsolutePath().normalize();
            if (Files.isSymbolicLink(absolute) || !absolute.toRealPath().startsWith(base)) {
                throw new IllegalStateException("RUNTIME_ARTIFACT_OUTSIDE_SNAPSHOT");
            }
            List<Path> files;
            if (Files.isDirectory(absolute)) {
                try (var walk = Files.walk(absolute)) {
                    files = walk.filter(p -> !Files.isDirectory(p)).sorted().toList();
                }
            } else { files = List.of(absolute); }
            if (files.isEmpty()) { throw new IllegalStateException("RUNTIME_ARTIFACT_EMPTY"); }
            update(digest, Integer.toString(index++));
            update(digest, base.relativize(absolute).toString().replace('\\', '/'));
            for (Path file : files) {
                if (Files.isSymbolicLink(file) || !file.toRealPath().startsWith(base) || !Files.isRegularFile(file)) {
                    throw new IllegalStateException("RUNTIME_ARTIFACT_INVALID");
                }
                update(digest, absolute.equals(file) ? "" : absolute.relativize(file).toString().replace('\\', '/'));
                MessageDigest content = MessageDigest.getInstance("SHA-256");
                try (InputStream in = Files.newInputStream(file)) {
                    byte[] buffer = new byte[65536];
                    int length;
                    while ((length = in.read(buffer)) != -1) { content.update(buffer, 0, length); }
                }
                digest.update(content.digest());
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    /** @param values 无序键值集合 @return 与遍历顺序无关且有边界编码的摘要 */
    public static String digestValues(Map<String, String> values) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (var entry : new TreeMap<>(values).entrySet()) { update(digest, entry.getKey()); update(digest, entry.getValue()); }
        return HexFormat.of().formatHex(digest.digest());
    }

    /** @param digest 累积器 @param value 使用长度前缀避免字段拼接歧义的值 */
    private static void update(MessageDigest digest, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        digest.update(java.nio.ByteBuffer.allocate(4).putInt(bytes.length).array());
        digest.update(bytes);
    }
}
