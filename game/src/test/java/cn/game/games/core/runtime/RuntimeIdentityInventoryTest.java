package cn.game.games.core.runtime;

import cn.game.core.runtime.RuntimeIdentityInventory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** 验证身份绑定实际文件及字段边界，避免仅测固定样例成功。 */
class RuntimeIdentityInventoryTest {
    /** 每个用例独有的文件根。 */
    @TempDir Path directory;

    /** 首次身份复查发生在修改、实际加载、原字节恢复之后，仍必须拒绝。 */
    @Test void resourceLoadThenRestoreBeforeFirstProbeRevokesIdentity() throws Exception {
        var watcher = cn.game.util.file.WatchServiceManager.getInstance();
        var inventory = new RuntimeIdentityInventory(directory, false);
        Path file = Files.writeString(directory.resolve("BoundaryFixture.xml"), "original");
        var loaded = new java.util.concurrent.atomic.AtomicReference<String>();
        watcher.register(new cn.game.util.file.ResourceListener() {
            @Override public String name() { return "BoundaryFixture"; }
            @Override public void load() {
                try { loaded.set(Files.readString(file)); }
                catch (java.io.IOException e) {
                    org.slf4j.LoggerFactory.getLogger(RuntimeIdentityInventoryTest.class).warn("读取资源回归夹具失败", e);
                    throw new java.io.UncheckedIOException(e);
                }
            }
        });
        Files.writeString(file, "changed");
        watcher.dispatchChange(java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY, file.getFileName());
        Assertions.assertEquals("changed", loaded.get());
        Files.writeString(file, "original");
        Assertions.assertEquals("RUNTIME_RESOURCE_CHANGED", Assertions.assertThrows(IllegalStateException.class,
                () -> inventory.capture("game_test")).getMessage());
        Assertions.assertEquals("changed", loaded.get());
    }

    /** CREATE 在解析失败前撤销；OVERFLOW 丢失事件也不能继续证明身份。 */
    @Test void resourceCreateFailureAndOverflowRevokeIdentity() throws Exception {
        var watcher = cn.game.util.file.WatchServiceManager.getInstance();
        var inventory = new RuntimeIdentityInventory(directory, false);
        watcher.register(new cn.game.util.file.ResourceListener() {
            @Override public String name() { return "BoundaryFail"; }
            @Override public void load() { throw new IllegalStateException("fixture parse failure"); }
        });
        Assertions.assertThrows(IllegalStateException.class, () -> watcher.dispatchChange(
                java.nio.file.StandardWatchEventKinds.ENTRY_CREATE, Path.of("BoundaryFail.xml")));
        Assertions.assertEquals("RUNTIME_RESOURCE_CHANGED", Assertions.assertThrows(IllegalStateException.class,
                () -> inventory.capture("game_test")).getMessage());
        var next = new RuntimeIdentityInventory(directory, false);
        watcher.dispatchChange(java.nio.file.StandardWatchEventKinds.OVERFLOW, null);
        Assertions.assertEquals("RUNTIME_RESOURCE_CHANGED", Assertions.assertThrows(IllegalStateException.class,
                () -> next.capture("game_test")).getMessage());
    }

    /** 相同名称但不同字节的制品不能共用摘要。 */
    @Test void artifactContentChangesIdentity() throws Exception {
        Path classes = Files.createDirectory(directory.resolve("classes"));
        Path file = Files.writeString(classes.resolve("Example.class"), "first");
        String first = RuntimeIdentityInventory.digestPaths(directory, List.of(classes));
        Files.writeString(file, "other");
        Assertions.assertNotEquals(first, RuntimeIdentityInventory.digestPaths(directory, List.of(classes)));
    }

    /** 类路径顺序改变会影响加载结果，必须进入身份。 */
    @Test void classpathOrderChangesIdentity() throws Exception {
        Path first = Files.writeString(directory.resolve("first.jar"), "same");
        Path second = Files.writeString(directory.resolve("second.jar"), "same");
        Assertions.assertNotEquals(RuntimeIdentityInventory.digestPaths(directory, List.of(first, second)),
                RuntimeIdentityInventory.digestPaths(directory, List.of(second, first)));
    }

    /** 未声明在独立快照中的文件不可加入运行身份。 */
    @Test void externalClasspathIsRejected() throws Exception {
        Path root = Files.createDirectory(directory.resolve("root"));
        Path external = Files.writeString(directory.resolve("other.jar"), "outside");
        Assertions.assertThrows(IllegalStateException.class, () -> RuntimeIdentityInventory.digestPaths(root, List.of(external)));
    }

    /** 空类目录不能冒充有效构建输入。 */
    @Test void emptyArtifactsAreRejected() throws Exception {
        Path empty = Files.createDirectory(directory.resolve("empty"));
        Assertions.assertThrows(IllegalStateException.class, () -> RuntimeIdentityInventory.digestPaths(directory, List.of(empty)));
    }

    /** 配置迭代顺序无关，但键值边界和真实值必须绑定。 */
    @Test void configurationEncodingIsUnambiguous() throws Exception {
        Map<String, String> forward = new LinkedHashMap<>(); forward.put("a", "bc"); forward.put("d", "e");
        Map<String, String> reverse = new LinkedHashMap<>(); reverse.put("d", "e"); reverse.put("a", "bc");
        Assertions.assertEquals(RuntimeIdentityInventory.digestValues(forward), RuntimeIdentityInventory.digestValues(reverse));
        Assertions.assertNotEquals(RuntimeIdentityInventory.digestValues(Map.of("a", "bc")), RuntimeIdentityInventory.digestValues(Map.of("ab", "c")));
        Assertions.assertNotEquals(RuntimeIdentityInventory.digestValues(forward), RuntimeIdentityInventory.digestValues(Map.of("a", "changed", "d", "e")));
    }

    /** 未启用的进程和不规范挑战均不能返回看似有效身份。 */
    @Test void disabledIdentityAndInvalidChallengesFailClosed() {
        Assertions.assertThrows(IllegalStateException.class, () -> RuntimeIdentityInventory.assertHotUpdateDisabled(true));
        RuntimeIdentityInventory.assertHotUpdateDisabled(false);
        Assertions.assertFalse(RuntimeIdentity.validChallenge(null));
        Assertions.assertFalse(RuntimeIdentity.validChallenge("../untrusted"));
        Assertions.assertFalse(RuntimeIdentity.validChallenge("a".repeat(33)));
        Assertions.assertTrue(RuntimeIdentity.validChallenge("a".repeat(32)));
        Assertions.assertThrows(IllegalStateException.class, () -> RuntimeIdentity.observe("a".repeat(32)));
    }

    /** Game 清单必须在读取配置与制品前应用命令行节点覆盖，不能保留环境默认节点名。 */
    @Test void gameManifestAppliesStartupNodeBeforeCollectingInputs() throws Exception {
        assertManifestStartupNode("game", "game.server.id", "game_test");
    }

    /** Login 清单遵循自己的启动节点键，不能遗漏登录服或错误覆盖游戏服节点。 */
    @Test void loginManifestAppliesStartupNodeBeforeCollectingInputs() throws Exception {
        assertManifestStartupNode("login", "login.server.id", "login_test");
    }

    /**
     * 用缺失制品目录终止后续采集，直接验证真实清单入口在配置读取前的启动参数语义。
     * @param app Apollo 应用名
     * @param key 该应用启动入口使用的节点属性名
     * @param target 命令行显式指定的节点名
     */
    private void assertManifestStartupNode(String app, String key, String target) throws Exception {
        String otherKey = app.equals("game") ? "login.server.id" : "game.server.id";
        Map<String, String> previous = new LinkedHashMap<>();
        for (String property : List.of("app.id", "game.runtimeIdentity.root", key, otherKey)) {
            previous.put(property, System.getProperty(property));
        }
        Path output = directory.resolve("expected.json");
        try {
            System.setProperty("app.id", app);
            System.setProperty("game.runtimeIdentity.root", directory.resolve("missing-artifacts").toString());
            System.setProperty(key, "apollo_default_node");
            System.setProperty(otherKey, "other_node");
            Assertions.assertThrows(java.nio.file.NoSuchFileException.class,
                    () -> RuntimeIdentityManifest.main(new String[] { target, output.toString() }));
            Assertions.assertEquals(target, System.getProperty(key));
            Assertions.assertEquals("other_node", System.getProperty(otherKey));
            Assertions.assertFalse(Files.exists(output));
        } finally {
            previous.forEach((property, value) -> {
                if (value == null) { System.clearProperty(property); }
                else { System.setProperty(property, value); }
            });
        }
    }
}
