package cn.game.core.runtime;

import java.nio.file.Path;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.game.util.runtime.FrozenApollo;

/** Game/Login 共用的存储前屏障；独立期望只用于比较，实际摘要来自本 JVM 固定的输入。 */
public final class RuntimeStartup {
    /** 工具类不创建实例。 */
    private RuntimeStartup() {}

    /** @param targetId 入口已解析的真实节点标识；任何核验错误都在存储初始化之前传播。 */
    public static void verify(String targetId) throws Exception {
        if (!FrozenApollo.enabled()) { return; }
        if (!"test".equalsIgnoreCase(System.getProperty("server.run.mode"))) {
            throw new IllegalStateException("RUNTIME_IDENTITY_REQUIRES_TEST_MODE");
        }
        String expectedPath = System.getProperty("runtime.expected", "");
        if (expectedPath.isBlank()) { throw new IllegalStateException("RUNTIME_EXPECTED_REQUIRED"); }
        Map<String, String> expected = new ObjectMapper().readValue(Path.of(expectedPath).toFile(), new TypeReference<Map<String, String>>() {});
        FrozenApollo.INSTANCE.freeze();
        var inventory = new RuntimeIdentityInventory(Path.of(System.getProperty("game.runtimeIdentity.root", "")), true);
        Map<String, String> actual = inventory.capture(targetId);
        if (!actual.equals(expected)) { throw new IllegalStateException("RUNTIME_STARTUP_EXPECTATION_MISMATCH"); }
        FrozenApollo.INSTANCE.assertUnchanged();
    }
}
