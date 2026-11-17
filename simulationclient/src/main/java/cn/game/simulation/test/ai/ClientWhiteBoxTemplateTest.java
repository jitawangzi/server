package cn.game.simulation.test.ai;

import cn.game.protocol.protobuf.TestMsg.TestConfigQueryRequest_6f000104;
import cn.game.protocol.protobuf.TestMsg.TestConfigQueryResponse_6f000105;

import java.util.Map;

/**
 * 黑白盒一体化测试标准示范场景
 * 演示 Arrange 环境准备、Trace 黑盒录制、双重视角断言、点分路径白盒自省以及强制落盘冷重载五步完整闭环
 *
 * @author shuyongqiang
 */
public class ClientWhiteBoxTemplateTest extends ClientBaseScenarioTest {

	public static void main(String[] args) {
		new ClientWhiteBoxTemplateTest().start();
	}

	@Override
	protected void executeProcess() throws Exception {
		System.out.println("【步骤 1: Arrange】环境准备与状态快照保存");
		resetModules("ItemModule");
		whiteBox.saveSnapshot("template_initial_snapshot");

		System.out.println("【步骤 2: Act & Trace】黑盒发包与白盒 Trace 联合录制");
		TestConfigQueryRequest_6f000104 configReq = TestConfigQueryRequest_6f000104.newBuilder()
				.setOp("tables")
				.build();
		WhiteBoxTestKit.TracedResponse<TestConfigQueryResponse_6f000105> traced = sendAndTrace(configReq);

		System.out.println("【步骤 3: Assert】联合校验协议返回与服务端流水事件");
		traced.assertNoServerErrors();

		System.out.println("【步骤 4: Introspect】深入白盒内部自省模块属性");
		Object heavenlyDaoLevel = inspectModuleField("DevelopModule", "heavenlyDaoLevel");
		assertModuleFieldEquals("DevelopModule", "heavenlyDaoLevel", heavenlyDaoLevel);

		System.out.println("【步骤 5: Cold Reload & Verify】强制落盘与冷重载验证持久化闭环");
		Map<String, Object> reloadData = flushAndColdReload();
		if (!Boolean.TRUE.equals(reloadData.get("reloadedFromDb"))) {
			throw new IllegalStateException("冷重载验证失败：reloadedFromDb 为 false");
		}
		assertModuleFieldEquals("DevelopModule", "heavenlyDaoLevel", heavenlyDaoLevel);

		System.out.println("【恢复环境】恢复快照并清理测试数据");
		whiteBox.restoreSnapshot("template_initial_snapshot");
		whiteBox.deleteSnapshots();
	}
}
