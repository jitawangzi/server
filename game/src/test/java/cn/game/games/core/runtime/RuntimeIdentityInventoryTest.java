package cn.game.games.core.runtime;

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
}
