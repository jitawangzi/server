package cn.game.simulation.test.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Message;

import cn.game.protocol.protobuf.BaseMsg.AssetInfo;
import cn.game.protocol.protobuf.MailMsg.MailDeleteRequest_12000007;
import cn.game.protocol.protobuf.MailMsg.MailDeleteResponse_12000008;
import cn.game.protocol.protobuf.MailMsg.MailInfo;
import cn.game.protocol.protobuf.MailMsg.MailListRequest_12000001;
import cn.game.protocol.protobuf.MailMsg.MailListResponse_12000002;
import cn.game.protocol.protobuf.MailMsg.MailReceiveRequest_12000005;
import cn.game.protocol.protobuf.MailMsg.MailReceiveResponse_12000006;
import cn.game.protocol.protobuf.MailMsg.MailSeeRequest_12000003;
import cn.game.protocol.protobuf.MailMsg.MailSeeResponse_12000004;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.TestMsg.TestGmExecRequest_6f000102;
import cn.game.protocol.protobuf.TestMsg.TestGmExecResponse_6f000103;
import cn.game.protocol.protobuf.TestMsg.TestInspectPlayerRequest_6f000108;
import cn.game.protocol.protobuf.TestMsg.TestInspectPlayerResponse_6f000109;
import cn.game.simulation.client.Client;

/**
 * 邮件业务场景黑白盒一体化用例的自动化单元测试
 * 在离线环境下验证 Arrange、Act & Trace、Assert、Introspect、Cold Reload 及 Cleanup 全流程闭环
 *
 * @author shuyongqiang
 */
public class ClientMailScenarioTestTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	/**
	 * 模拟底层持久化重新加载契约标记
	 */
	private void reloadFresh() {
		// 标记存储重载契约
	}

	/**
	 * 可拦截邮件与GM请求的高仿真测试客户端
	 *
	 * @author shuyongqiang
	 */
	static class MockMailClient extends Client {

		final List<MailInfo> mails = new ArrayList<>();
		final Map<String, Object> mailModuleState = new LinkedHashMap<>();
		final List<Map<String, Object>> traceResourceEvents = new ArrayList<>();
		boolean traceStarted = false;

		MockMailClient() {
			super("mail-scenario-tester");
			setPlayerId(99001L);

			mailModuleState.put("globalMailId", 1L);
			mailModuleState.put("noticeMailId", 0);
			mailModuleState.put("mailCount", 0);
			mailModuleState.put("mails", new ArrayList<Map<String, Object>>());
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T sendProtocolAndWait(Message request) {
			return (T) handleMessage(request);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T sendProtocolAndWait(Message request, int timeoutSeconds) {
			return (T) handleMessage(request);
		}

		private Object handleMessage(Message request) {
			try {
				if (request instanceof TestGmExecRequest_6f000102 gmReq) {
					String action = gmReq.getAction();
					String paramsJson = gmReq.getParamsJson();

					if ("mail.send".equals(action)) {
						@SuppressWarnings("unchecked")
						Map<String, Object> params = (Map<String, Object>) MAPPER.readValue(paramsJson, Map.class);
						String title = params.containsKey("title") ? String.valueOf(params.get("title")) : "一体化测试邮件";
						int itemId = params.containsKey("itemId") ? ((Number) params.get("itemId")).intValue() : 100001;
						int count = params.containsKey("count") ? ((Number) params.get("count")).intValue() : 10;
						String uid = "9900123456";

						MailInfo newMail = MailInfo.newBuilder()
								.setUid(uid)
								.setTitle(title)
								.setSee(false)
								.setReceive(false)
								.build();
						mails.add(newMail);

						@SuppressWarnings("unchecked")
						List<Map<String, Object>> mailListState = (List<Map<String, Object>>) mailModuleState.get("mails");
						Map<String, Object> itemState = new LinkedHashMap<>();
						itemState.put("id", uid);
						itemState.put("mailId", 0);
						itemState.put("title", title);
						itemState.put("see", false);
						itemState.put("receive", false);
						itemState.put("isDeleted", false);
						mailListState.add(itemState);
						mailModuleState.put("mailCount", mailListState.size());

						return TestGmExecResponse_6f000103.newBuilder()
								.setResultJson("{\"schemaVersion\":\"1.0\",\"ok\":true,\"errorCode\":0,\"summary\":\"ok\"}")
								.build();
					} else if ("gm.snapshot.save".equals(action) || "gm.snapshot.restore".equals(action) || "gm.snapshot.delete".equals(action)) {
						return TestGmExecResponse_6f000103.newBuilder()
								.setResultJson("{\"schemaVersion\":\"1.0\",\"ok\":true,\"errorCode\":0,\"summary\":\"ok\"}")
								.build();
					} else if ("gm.trace.start".equals(action)) {
						traceStarted = true;
						traceResourceEvents.clear();
						return TestGmExecResponse_6f000103.newBuilder()
								.setResultJson("{\"schemaVersion\":\"1.0\",\"ok\":true,\"errorCode\":0,\"summary\":\"ok\"}")
								.build();
					} else if ("gm.trace.query".equals(action)) {
						Map<String, Object> record = new LinkedHashMap<>();
						record.put("resourceEvents", new ArrayList<>(traceResourceEvents));
						record.put("errorEvents", Collections.emptyList());
						Map<String, Object> traceData = Map.of("records", List.of(record));
						String json = MAPPER.writeValueAsString(Map.of(
								"schemaVersion", "1.0",
								"ok", true,
								"errorCode", 0,
								"summary", "ok",
								"data", traceData
						));
						return TestGmExecResponse_6f000103.newBuilder().setResultJson(json).build();
					} else if ("gm.trace.stop".equals(action)) {
						traceStarted = false;
						return TestGmExecResponse_6f000103.newBuilder()
								.setResultJson("{\"schemaVersion\":\"1.0\",\"ok\":true,\"errorCode\":0,\"summary\":\"ok\"}")
								.build();
					} else if ("gm.flushAndColdReload".equals(action)) {
						Map<String, Object> reloadData = Map.of(
								"reloadedFromDb", true,
								"oldIdentityHash", "111111",
								"newIdentityHash", "222222"
						);
						String json = MAPPER.writeValueAsString(Map.of(
								"schemaVersion", "1.0",
								"ok", true,
								"errorCode", 0,
								"summary", "ok",
								"data", reloadData
						));
						return TestGmExecResponse_6f000103.newBuilder().setResultJson(json).build();
					}
					return TestGmExecResponse_6f000103.newBuilder()
							.setResultJson("{\"schemaVersion\":\"1.0\",\"ok\":true,\"errorCode\":0,\"summary\":\"ok\"}")
							.build();
				}

				if (request instanceof MailListRequest_12000001) {
					return MailListResponse_12000002.newBuilder()
							.addAllMails(mails)
							.build();
				}

				if (request instanceof MailSeeRequest_12000003 seeReq) {
					String uid = seeReq.getUid();
					for (int i = 0; i < mails.size(); i++) {
						if (mails.get(i).getUid().equals(uid)) {
							mails.set(i, mails.get(i).toBuilder().setSee(true).build());
						}
					}
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> mailListState = (List<Map<String, Object>>) mailModuleState.get("mails");
					for (Map<String, Object> m : mailListState) {
						if (String.valueOf(m.get("id")).equals(uid)) {
							m.put("see", true);
						}
					}
					return MailSeeResponse_12000004.newBuilder().build();
				}

				if (request instanceof MailReceiveRequest_12000005 recReq) {
					String uid = recReq.getUid();
					for (int i = 0; i < mails.size(); i++) {
						if (mails.get(i).getUid().equals(uid)) {
							mails.set(i, mails.get(i).toBuilder().setReceive(true).build());
						}
					}
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> mailListState = (List<Map<String, Object>>) mailModuleState.get("mails");
					for (Map<String, Object> m : mailListState) {
						if (String.valueOf(m.get("id")).equals(uid)) {
							m.put("receive", true);
						}
					}

					if (traceStarted) {
						traceResourceEvents.add(Map.of(
								"id", 100001,
								"count", 10,
								"isAdd", true,
								"opType", "Mail"
						));
					}

					RewardInfo reward = RewardInfo.newBuilder()
							.setAsset(AssetInfo.newBuilder().setId(100001).setCount(10).build())
							.build();
					return MailReceiveResponse_12000006.newBuilder()
							.addRewards(reward)
							.build();
				}

				if (request instanceof MailDeleteRequest_12000007 delReq) {
					String uid = delReq.getUid();
					mails.removeIf(m -> m.getUid().equals(uid));
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> mailListState = (List<Map<String, Object>>) mailModuleState.get("mails");
					mailListState.removeIf(m -> String.valueOf(m.get("id")).equals(uid));
					mailModuleState.put("mailCount", mailListState.size());
					return MailDeleteResponse_12000008.newBuilder().build();
				}

				if (request instanceof TestInspectPlayerRequest_6f000108) {
					Map<String, Object> inspectData = Map.of(
							"playerId", 99001L,
							"modules", Map.of("MailModule", mailModuleState)
					);
					String json = MAPPER.writeValueAsString(inspectData);
					return TestInspectPlayerResponse_6f000109.newBuilder()
							.setSchemaVersion("1.0")
							.setResultJson(json)
							.build();
				}
			} catch (Exception e) {
				throw new RuntimeException("MockClient 处理异常", e);
			}
			throw new UnsupportedOperationException("未模拟的协议请求: " + request.getClass().getName());
		}
	}

	/**
	 * 验证邮件黑白盒一体化场景测试端到端全流程闭环
	 */
	@Test
	public void testMailScenarioEndToEnd() {
		reloadFresh();

		MockMailClient mockClient = new MockMailClient();
		WhiteBoxTestKit kit = new WhiteBoxTestKit(mockClient, () -> {});

		ClientMailScenarioTest scenarioTest = new ClientMailScenarioTest();
		scenarioTest.initForTest(mockClient, kit);

		Assertions.assertDoesNotThrow(scenarioTest::executeProcess);
	}
}
