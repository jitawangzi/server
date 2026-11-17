package cn.game.simulation.test.ai;

import java.util.List;
import java.util.Map;

import cn.game.protocol.protobuf.MailMsg.MailDeleteRequest_12000007;
import cn.game.protocol.protobuf.MailMsg.MailInfo;
import cn.game.protocol.protobuf.MailMsg.MailListRequest_12000001;
import cn.game.protocol.protobuf.MailMsg.MailListResponse_12000002;
import cn.game.protocol.protobuf.MailMsg.MailReceiveRequest_12000005;
import cn.game.protocol.protobuf.MailMsg.MailReceiveResponse_12000006;
import cn.game.protocol.protobuf.MailMsg.MailSeeRequest_12000003;
import cn.game.protocol.protobuf.MailMsg.MailSeeResponse_12000004;
import cn.game.simulation.client.Client;

/**
 * 邮件模块黑白盒一体化业务测试用例
 * 演示环境准备(GM发信与快照)、黑盒发包、服务端白盒Trace录制与资源流水断言、
 * 模块内部状态自省(see/receive)以及数据强制落盘与冷重载完整闭环
 *
 * @author shuyongqiang
 */
public class ClientMailScenarioTest extends ClientBaseScenarioTest {

	public static void main(String[] args) {
		new ClientMailScenarioTest().start();
	}

	/**
	 * 供单元测试注入桩客户端与 WhiteBox 实例的辅助入口
	 *
	 * @param testClient 桩客户端
	 * @param kit 白盒工具箱
	 */
	public void initForTest(Client testClient, WhiteBoxTestKit kit) {
		this.client = testClient;
		this.whiteBox = kit;
	}

	@Override
	protected void executeProcess() {
		System.out.println(">>> [ClientMailScenarioTest] 开始执行邮件模块黑白盒一体化场景测试");

		// 步骤 1: Arrange - 环境准备与保存测试前快照
		System.out.println("【步骤 1: Arrange】保存环境快照，并通过 GM 指令发送一封带附件的测试邮件");
		whiteBox.saveSnapshot("mail_scenario_before");

		// 发送测试邮件：元宝(100001) x 10
		int rewardItemId = 100001;
		int rewardItemCount = 10;
		String mailTitle = "一体化测试邮件";
		gm("mail.send", Map.of(
				"title", mailTitle,
				"content", "黑白盒一体化全流程自动化验收测试邮件",
				"itemId", rewardItemId,
				"count", rewardItemCount
		)).requireOk();

		// 步骤 2: Act & Trace - 拉取邮件列表并定位目标邮件
		System.out.println("【步骤 2: Act & Trace】拉取邮件列表，定位新建测试邮件");
		MailListResponse_12000002 listResp = sendAndWait(MailListRequest_12000001.getDefaultInstance());
		List<MailInfo> mailsList = listResp.getMailsList();
		if (mailsList.isEmpty()) {
			throw new IllegalStateException("未能查询到测试邮件，邮件列表为空");
		}

		MailInfo targetMail = null;
		for (MailInfo info : mailsList) {
			if (mailTitle.equals(info.getTitle())) {
				targetMail = info;
				break;
			}
		}
		if (targetMail == null) {
			targetMail = mailsList.get(mailsList.size() - 1);
		}
		String targetUid = targetMail.getUid();
		System.out.println("成功定位测试邮件: uid=" + targetUid + ", title=" + targetMail.getTitle());

		// 步骤 2.1: 查看邮件并录制 Trace
		System.out.println("【步骤 2.1: Act & Trace】发送查看邮件请求并录制白盒 Trace");
		MailSeeRequest_12000003 seeReq = MailSeeRequest_12000003.newBuilder()
				.setUid(targetUid)
				.build();
		WhiteBoxTestKit.TracedResponse<MailSeeResponse_12000004> tracedSee = sendAndTrace(seeReq);
		tracedSee.assertNoServerErrors();

		// 步骤 2.2: 领取附件并录制 Trace
		System.out.println("【步骤 2.2: Act & Trace】发送领取附件请求并录制白盒 Trace");
		MailReceiveRequest_12000005 receiveReq = MailReceiveRequest_12000005.newBuilder()
				.setUid(targetUid)
				.build();
		WhiteBoxTestKit.TracedResponse<MailReceiveResponse_12000006> tracedReceive = sendAndTrace(receiveReq);

		// 步骤 3: Assert - 校验协议返回与白盒流水断言
		System.out.println("【步骤 3: Assert】联合断言协议返回奖励与服务端资源变化流水");
		tracedReceive.assertNoServerErrors();
		MailReceiveResponse_12000006 receiveResp = tracedReceive.getResponse();
		if (receiveResp.getRewardsCount() <= 0) {
			throw new IllegalStateException("领取邮件返回奖励列表为空");
		}
		// 校验资源流水净增额
		tracedReceive.assertResourceDelta(rewardItemId, rewardItemCount, "Mail");
		System.out.println("资源流水净额断言通过: itemId=" + rewardItemId + ", delta=+" + rewardItemCount);

		// 步骤 4: Introspect - 模块内部状态自省
		System.out.println("【步骤 4: Introspect】自省 MailModule 内部内存状态");
		Object mailCount = inspectModuleField("MailModule", "mailCount");
		if (mailCount instanceof Number && ((Number) mailCount).intValue() <= 0) {
			throw new IllegalStateException("自省 MailModule 邮件总数异常: mailCount=" + mailCount);
		}
		Map<String, Object> mailModuleData = whiteBox.inspectModule("MailModule");
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> introspectMails = (List<Map<String, Object>>) mailModuleData.get("mails");
		if (introspectMails == null || introspectMails.isEmpty()) {
			throw new IllegalStateException("自省 MailModule.mails 列表为空");
		}
		Map<String, Object> introspectTarget = null;
		for (Map<String, Object> item : introspectMails) {
			if (String.valueOf(item.get("id")).equals(targetUid)) {
				introspectTarget = item;
				break;
			}
		}
		if (introspectTarget == null) {
			throw new IllegalStateException("自省未找到目标邮件: uid=" + targetUid);
		}
		if (!Boolean.TRUE.equals(introspectTarget.get("see"))) {
			throw new IllegalStateException("自省目标邮件 see 状态不为 true: " + introspectTarget);
		}
		if (!Boolean.TRUE.equals(introspectTarget.get("receive"))) {
			throw new IllegalStateException("自省目标邮件 receive 状态不为 true: " + introspectTarget);
		}
		System.out.println("白盒自省断言通过: see=true, receive=true");

		// 步骤 5: Cold Reload & Verify - 强制落盘与冷重载
		System.out.println("【步骤 5: Cold Reload & Verify】强制落盘并执行冷重载，验证 DB 重新加载后状态持久化");
		Map<String, Object> reloadResult = flushAndColdReload();
		if (!Boolean.TRUE.equals(reloadResult.get("reloadedFromDb"))) {
			throw new IllegalStateException("冷重载未确认从 DB 重新加载");
		}
		// 再次自省冷重载后的状态
		Map<String, Object> reloadedMailData = whiteBox.inspectModule("MailModule");
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> reloadedMails = (List<Map<String, Object>>) reloadedMailData.get("mails");
		Map<String, Object> reloadedTarget = null;
		if (reloadedMails != null) {
			for (Map<String, Object> item : reloadedMails) {
				if (String.valueOf(item.get("id")).equals(targetUid)) {
					reloadedTarget = item;
					break;
				}
			}
		}
		if (reloadedTarget == null) {
			throw new IllegalStateException("冷重载后在 DB 数据中未找到目标邮件: uid=" + targetUid);
		}
		if (!Boolean.TRUE.equals(reloadedTarget.get("see")) || !Boolean.TRUE.equals(reloadedTarget.get("receive"))) {
			throw new IllegalStateException("冷重载后目标邮件状态持久化异常: " + reloadedTarget);
		}
		System.out.println("冷重载持久化断言通过: 实体已从数据库重新加载且状态一致");

		// 步骤 6: Cleanup - 业务清理与快照恢复
		System.out.println("【步骤 6: Cleanup】删除已读已领取的测试邮件，并恢复初始快照");
		MailDeleteRequest_12000007 delReq = MailDeleteRequest_12000007.newBuilder()
				.setUid(targetUid)
				.build();
		sendAndWait(delReq);

		// 再次拉取验证已删除
		MailListResponse_12000002 afterDelList = sendAndWait(MailListRequest_12000001.getDefaultInstance());
		for (MailInfo m : afterDelList.getMailsList()) {
			if (targetUid.equals(m.getUid())) {
				throw new IllegalStateException("测试邮件删除失败，列表中仍存在: uid=" + targetUid);
			}
		}
		System.out.println("测试邮件删除成功");

		// 恢复快照
		whiteBox.restoreSnapshot("mail_scenario_before");
		whiteBox.deleteSnapshots();
		System.out.println(">>> [ClientMailScenarioTest] 邮件模块黑白盒一体化场景测试全部通过 (PASS)");
	}
}
