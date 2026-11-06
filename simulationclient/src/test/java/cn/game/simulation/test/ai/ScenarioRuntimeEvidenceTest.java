package cn.game.simulation.test.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** 核对同一真实玩家、实例和挑战，而非客户端声明的区服。 */
class ScenarioRuntimeEvidenceTest {
    /** @return 仅用于协议判定的固定夹具，非真实发布清单 */
    private ObjectNode identity() {
        ObjectNode node = new ObjectMapper().createObjectNode();
        for (String field : List.of("schemaVersion", "targetId", "buildId", "sourceDigest", "artifactSha256", "configSha256", "protocolSha256", "dataNamespace")) { node.put(field, field); }
        node.put("schemaVersion", "1.0"); node.put("instanceId", "actual-process"); node.put("challenge", "nonce"); node.put("playerId", "123");
        node.put("logicServerId", "logic-2"); node.put("startedAt", "2026-01-01T00:00:00Z");
        return node;
    }

    /** 身份相符时接受实际逻辑区服，不将其混同Game节点名。 */
    @Test void actualIdentityMatches() {
        ScenarioRuntimeEvidence.verify(identity(), identity(), "nonce", "actual-process", "123", "logic-2");
    }

    /** 每个发布身份字段发生变化都应拒绝。 */
    @Test void releaseMismatchFails() {
        for (String field : List.of("schemaVersion", "targetId", "buildId", "sourceDigest", "artifactSha256", "configSha256", "protocolSha256", "dataNamespace")) {
            ObjectNode actual = identity(); actual.put(field, "wrong");
            Assertions.assertThrows(IllegalStateException.class, () -> ScenarioRuntimeEvidence.verify(identity(), actual, "nonce", "actual-process", "123", "logic-2"));
        }
    }

    /** 旧响应、另一实例或另一玩家不得作为本场景的关联证据。 */
    @Test void challengeInstanceAndPlayerAreBound() {
        var client = new cn.game.simulation.client.Client("fixture");
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.sendProtocolAndWait(null, 0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.sendProtocolAndWait(null, 61));
        Assertions.assertThrows(IllegalStateException.class, () -> ScenarioRuntimeEvidence.verify(identity(), identity(), "nonce", "actual-process", "123", "wrong-shard"));
        Assertions.assertThrows(IllegalStateException.class, () -> ScenarioRuntimeEvidence.verify(identity(), identity(), "old-nonce", "actual-process", "123", "logic-2"));
        Assertions.assertThrows(IllegalStateException.class, () -> ScenarioRuntimeEvidence.verify(identity(), identity(), "nonce", "other-process", "123", "logic-2"));
        Assertions.assertThrows(IllegalStateException.class, () -> ScenarioRuntimeEvidence.verify(identity(), identity(), "nonce", "actual-process", "456", "logic-2"));
        ObjectNode missing = identity(); missing.remove("logicServerId");
        Assertions.assertThrows(IllegalStateException.class, () -> ScenarioRuntimeEvidence.verify(identity(), missing, "nonce", "actual-process", "123", "logic-2"));
    }
}
