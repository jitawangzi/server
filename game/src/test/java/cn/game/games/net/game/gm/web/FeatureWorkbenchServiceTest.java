package cn.game.games.net.game.gm.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import cn.game.games.net.game.gm.ProtoCatalogService;
import cn.game.games.net.game.gm.ProtoCatalogService.ProtoFileMeta;

/**
 * 全服功能模块目录严格 //@MessageModule 解析、十六进制映射、标题与 Schema 测试
 * (TC-02-MODULE-CATALOG-DISCOVERY, DC-02, EX-02)
 *
 * @author shuyongqiang
 */
public class FeatureWorkbenchServiceTest {

	@Test
	@SuppressWarnings("unchecked")
	public void testModuleCatalogStrictlyFollowsMessageModuleHex() {
		ProtoCatalogService catalogService = new ProtoCatalogService();
		Map<String, ProtoFileMeta> fileMetas = catalogService.getProtoFileMetas();
		Assertions.assertFalse(fileMetas.isEmpty(), "ProtoFileMeta 不应为空");

		FeatureWorkbenchService service = new FeatureWorkbenchService(catalogService);
		Map<String, Object> fullCatalog = service.buildCatalog(null);
		Assertions.assertNotNull(fullCatalog);
		Assertions.assertEquals("1.0", fullCatalog.get("schemaVersion"));
		Assertions.assertNotNull(fullCatalog.get("runMode"));
		Assertions.assertTrue(fullCatalog.containsKey("simulationEnabled"));

		List<Map<String, Object>> modules = (List<Map<String, Object>>) fullCatalog.get("modules");
		Assertions.assertNotNull(modules);
		Assertions.assertEquals(fileMetas.size(), modules.size(), "模块列表数量必须严格等于含 //@MessageModule 的 .proto 文件数");

		Map<Integer, Map<String, Object>> byId = new LinkedHashMap<>();
		for (Map<String, Object> mod : modules) {
			int modId = (int) mod.get("moduleId");
			String title = String.valueOf(mod.get("title"));
			Assertions.assertFalse(title.contains("option optimize_for"), "模块标题不得被 //option 注释污染: " + title);
			Assertions.assertFalse(title.startsWith("import "), "模块标题不得被 //import 注释污染: " + title);
			Assertions.assertNull(byId.put(modId, mod), "不允许存在重复 moduleId: " + mod.get("moduleIdHex"));
		}

		// 1. 验证无 //@MessageModule 的协议文件（Player 0x01 / Gm 0x77 / Server 0x7d）不会进入模块列表
		Assertions.assertNull(byId.get(0x01), "PlayerMsg.proto 未声明 //@MessageModule，不应列入工作台模块列表");
		Assertions.assertNull(byId.get(0x77), "GmMsg.proto 未声明 //@MessageModule，不应列入工作台模块列表");
		Assertions.assertNull(byId.get(0x7d), "ServerMsg.proto 未声明 //@MessageModule，不应列入工作台模块列表");

		// 2. 验证十六进制 //@MessageModule 不发生十进制错位（如 Develop 0x25=37 vs Draw 0x37=55 vs Pet 0x19=25）
		Map<String, Object> develop = byId.get(0x25);
		Assertions.assertNotNull(develop, "DevelopMsg.proto (//@MessageModule 25) 必须映射至 0x25");
		Assertions.assertEquals("develop", develop.get("moduleKey"));
		Assertions.assertEquals("0x25", develop.get("moduleIdHex"));
		Assertions.assertTrue(String.valueOf(develop.get("title")).contains("养成"), "Develop 标题应取自中文注释: " + develop.get("title"));
		List<Map<String, Object>> devReqs = (List<Map<String, Object>>) develop.get("requests");
		Assertions.assertTrue(devReqs.stream().anyMatch(r -> "DevelopPotentialLvUpRequest_25000001".equals(r.get("msgName"))),
				"0x25 必须包含 DevelopPotentialLvUpRequest_25000001");
		Map<String, Object> devReq = devReqs.stream()
				.filter(r -> "DevelopPotentialLvUpRequest_25000001".equals(r.get("msgName")))
				.findFirst().orElseThrow();
		Map<String, Object> devReqSchema = (Map<String, Object>) devReq.get("reqSchema");
		Assertions.assertNotNull(devReqSchema, "Request 必须携带 reqSchema 供前端动态渲染");
		Assertions.assertFalse(((List<?>) devReqSchema.get("fields")).isEmpty(), "reqSchema.fields 不应为空");

		Map<String, Object> draw = byId.get(0x37);
		Assertions.assertNotNull(draw, "DrawMsg.proto (//@MessageModule 37) 必须映射至 0x37");
		Assertions.assertEquals("draw", draw.get("moduleKey"));
		List<Map<String, Object>> drawReqs = (List<Map<String, Object>>) draw.get("requests");
		Assertions.assertTrue(drawReqs.stream().anyMatch(r -> "DrawListRequest_37000001".equals(r.get("msgName"))));

		Map<String, Object> activity = byId.get(0x11);
		Assertions.assertNotNull(activity, "ActivityMsg.proto (//@MessageModule 11) 必须映射至 0x11");
		Assertions.assertEquals("活动协议", activity.get("title"));

		Map<String, Object> mail = byId.get(0x12);
		Assertions.assertNotNull(mail, "MailMsg.proto (//@MessageModule 12) 必须映射至 0x12");
		List<Map<String, Object>> mailReqs = (List<Map<String, Object>>) mail.get("requests");
		Assertions.assertTrue(mailReqs.stream().anyMatch(r -> "MailListRequest_12000001".equals(r.get("msgName"))));

		Map<String, Object> battle = byId.get(0x13);
		Assertions.assertNotNull(battle, "BattleMsg.proto (//@MessageModule 13) 必须映射至 0x13");
		Assertions.assertEquals("章节关卡协议", battle.get("title"));

		Map<String, Object> item = byId.get(0x0b);
		Assertions.assertNotNull(item, "ItemMsg.proto (//@MessageModule 0b) 必须映射至 0x0b");
		Assertions.assertEquals("item", item.get("moduleKey"));

		Map<String, Object> testMod = byId.get(0x6f);
		Assertions.assertNotNull(testMod, "TestMsg.proto (//@MessageModule 6f) 必须映射至 0x6f");
		Assertions.assertEquals("test", testMod.get("moduleKey"));

		// 3. 静态检查 isSimulatable 黑名单方法
		Assertions.assertFalse(FeatureWorkbenchService.isSimulatable(FeatureWorkbenchService.CMD_LOGOUT_REQ));
		Assertions.assertFalse(FeatureWorkbenchService.isSimulatable(FeatureWorkbenchService.CMD_LOGIN_REQ));
		Assertions.assertFalse(FeatureWorkbenchService.isSimulatable(FeatureWorkbenchService.CMD_HEARTBEAT_REQ));
		Assertions.assertFalse(FeatureWorkbenchService.isSimulatable(FeatureWorkbenchService.CMD_RECONNECT_REQ));
		Assertions.assertFalse(FeatureWorkbenchService.isSimulatable(0x7d000001));
		Assertions.assertTrue(FeatureWorkbenchService.isSimulatable(0x01000011));

		// 4. 关键词过滤测试 ("mail")
		Map<String, Object> filteredCatalog = service.buildCatalog("mail");
		List<Map<String, Object>> filteredModules = (List<Map<String, Object>>) filteredCatalog.get("modules");
		Assertions.assertNotNull(filteredModules);
		Assertions.assertFalse(filteredModules.isEmpty(), "过滤 mail 应能匹配到邮件相关模块");
		Assertions.assertTrue(filteredModules.stream().anyMatch(m -> Integer.valueOf(0x12).equals(m.get("moduleId"))));
	}
}