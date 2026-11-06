package cn.game.simulation.test.ai;

import cn.game.protocol.protobuf.PlayerMsg;

/** 使用真实登录连接执行无奖励副作用的心跳；前后处理节点由场景基类自动核验。 */
public final class ClientRuntimeIdentityTest extends ClientBaseScenarioTest {
    /** @param args 此场景不接收命令参数，运行配置由入口注入 */
    public static void main(String[] args) { new ClientRuntimeIdentityTest().start(); }

    @Override
    protected void executeProcess() {
        Object response = sendAndWait(PlayerMsg.PlayerHeartbeatRequest_01000005.getDefaultInstance());
        if (!(response instanceof PlayerMsg.PlayerHeartbeatResponse_01000006)) {
            throw new IllegalStateException("RUNTIME_SCENARIO_HEARTBEAT_RESPONSE_INVALID");
        }
    }
}
