package cn.game.games.net.game.gm;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.util.file.ResourceListener;

/**
 * 静态配表查询服务单元测试（覆盖3种配表形态、分页、模糊与集合过滤及O(1)查询）
 *
 * @author shuyongqiang
 */
public class ConfigQueryServiceTest {

	static class SampleShopRow {
		public int ID;
		public String name;
		public int price;
		public int[] tags;
		public int[][] costs;
		public Map<Integer, Integer> extra;

		SampleShopRow(int id, String name, int price, int[] tags, int[][] costs, Map<Integer, Integer> extra) {
			this.ID = id;
			this.name = name;
			this.price = price;
			this.tags = tags;
			this.costs = costs;
			this.extra = extra;
		}
	}

	static class SampleMapManager extends ResourceListener {
		private final Map<Integer, SampleShopRow> map = new LinkedHashMap<>();
		final AtomicInteger directGetCount = new AtomicInteger(0);

		SampleMapManager(int count) {
			for (int i = 1; i <= count; i++) {
				map.put(i, new SampleShopRow(
						i,
						"item-" + i,
						i * 10,
						new int[] { 1, i },
						new int[][] { { 100, i * 5 } },
						Map.of(1, i * 2)));
			}
		}

		public Collection<SampleShopRow> list() {
			return map.values();
		}

		public SampleShopRow getNullable(int id) {
			directGetCount.incrementAndGet();
			return map.get(id);
		}

		@Override
		public void load() {
		}

		@Override
		public String name() {
			return "SampleShop";
		}
	}

	static class SampleGroupItem {
		public int ID;
		public int groupId;
		public int weight;

		SampleGroupItem(int id, int groupId, int weight) {
			this.ID = id;
			this.groupId = groupId;
			this.weight = weight;
		}
	}

	static class SampleGroupedManager extends ResourceListener {
		private final Map<Integer, List<SampleGroupItem>> groups = new LinkedHashMap<>();
		final AtomicInteger groupedGetCount = new AtomicInteger(0);

		SampleGroupedManager() {
			groups.put(10, List.of(new SampleGroupItem(1001, 10, 50), new SampleGroupItem(1002, 10, 50)));
			groups.put(20, List.of(new SampleGroupItem(2001, 20, 100)));
		}

		public Collection<List<SampleGroupItem>> list() {
			return groups.values();
		}

		public List<SampleGroupItem> getSampleGroupIdList(int groupId) {
			groupedGetCount.incrementAndGet();
			return groups.get(groupId);
		}

		@Override
		public void load() {
		}

		@Override
		public String name() {
			return "SampleGrouped";
		}
	}

	static class SampleGlobalConst extends ResourceListener {
		public static final String xmlFileName = "GlobalConst.xml";
		public static final SampleGlobalConst instance = new SampleGlobalConst();
		public static int MaxHeroLevel = 120;
		public static int[] InitialItems = new int[] { 100001, 10 };

		@Override
		public void load() {
		}

		@Override
		public String name() {
			return "GlobalConst";
		}
	}

	@Test
	void verifyThreeTableKindsPaginationAndErrorProtection() {
		Map<String, ResourceListener> resources = new LinkedHashMap<>();
		SampleMapManager shopMgr = new SampleMapManager(65);
		SampleGroupedManager groupedMgr = new SampleGroupedManager();
		resources.put("SampleShop", shopMgr);
		resources.put("SampleGrouped", groupedMgr);
		resources.put("GlobalConst", SampleGlobalConst.instance);

		ConfigQueryService service = new ConfigQueryService(() -> resources);
		ConfigGm configGm = new ConfigGm(service);

		// 识别3种配表形态并携带 schemaVersion
		Map<String, Object> tablesResult = configGm.tables();
		Assertions.assertEquals("1.0", tablesResult.get("schemaVersion"));
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> tables = (List<Map<String, Object>>) tablesResult.get("tables");
		Assertions.assertEquals(3, tables.size());
		Map<String, String> kinds = new LinkedHashMap<>();
		Map<String, Integer> rowCounts = new LinkedHashMap<>();
		for (Map<String, Object> t : tables) {
			kinds.put((String) t.get("table"), (String) t.get("kind"));
			rowCounts.put((String) t.get("table"), (Integer) t.get("rowCount"));
		}
		Assertions.assertEquals("MAP_BY_ID", kinds.get("SampleShop"));
		Assertions.assertEquals("GROUPED_LIST", kinds.get("SampleGrouped"));
		Assertions.assertEquals("GLOBAL_CONST", kinds.get("GlobalConst"));
		Assertions.assertEquals(65, rowCounts.get("SampleShop"));
		Assertions.assertEquals(3, rowCounts.get("SampleGrouped"));
		Assertions.assertEquals(2, rowCounts.get("GlobalConst"));

		// 默认分页 pageSize=50 + 数组与Map字段结构化序列化
		Map<String, Object> page1 = service.listRows("SampleShop", Map.of(), 1, null);
		Assertions.assertEquals("1.0", page1.get("schemaVersion"));
		Assertions.assertEquals(50, page1.get("pageSize"));
		Assertions.assertEquals(65, page1.get("total"));
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> rows = (List<Map<String, Object>>) page1.get("rows");
		Assertions.assertEquals(50, rows.size());
		Assertions.assertInstanceOf(List.class, rows.get(0).get("tags"));
		Assertions.assertInstanceOf(List.class, rows.get(0).get("costs"));
		Assertions.assertInstanceOf(Map.class, rows.get(0).get("extra"));

		// pageSize=201 拒绝（禁止静默截断）
		GmResult oversized = configGm.list("SampleShop", null, 1, 201);
		Assertions.assertFalse(oversized.isOk());
		Assertions.assertEquals(ErrorMsgEnum.gm_cmd_param.getId(), oversized.getErrorCode());

		// 字符串子串模糊过滤、集合/数组元素过滤与未知字段报错
		Map<String, Object> fuzzyFiltered = service.listRows("SampleShop", Map.of("name", "item-1"), 1, 50);
		Assertions.assertEquals(11, fuzzyFiltered.get("total"));
		Map<String, Object> exactFiltered = service.listRows("SampleShop", Map.of("name", "item-12"), 1, 20);
		Assertions.assertEquals(1, exactFiltered.get("total"));
		Map<String, Object> arrayFiltered = service.listRows("SampleShop", Map.of("tags", "12"), 1, 20);
		Assertions.assertEquals(1, arrayFiltered.get("total"));
		Map<String, Object> nestedArrayFiltered = service.listRows("SampleShop", Map.of("costs", "60"), 1, 20);
		Assertions.assertEquals(1, nestedArrayFiltered.get("total"));

		GmResult badFilter = configGm.list("SampleShop", "{\"unknownField\":\"x\"}", 1, 20);
		Assertions.assertFalse(badFilter.isOk());
		Assertions.assertEquals(ErrorMsgEnum.gm_cmd_param.getId(), badFilter.getErrorCode());

		// O(1) 单条配置查询走 getNullable
		GmResult singleGet = configGm.get("SampleShop", 12, null);
		Assertions.assertTrue(singleGet.isOk());
		Assertions.assertEquals(1, shopMgr.directGetCount.get());
		@SuppressWarnings("unchecked")
		Map<String, Object> singleData = (Map<String, Object>) singleGet.getData();
		Assertions.assertEquals("1.0", singleData.get("schemaVersion"));
		Assertions.assertEquals("item-12", singleData.get("name"));

		// GROUPED_LIST 展平查询、get(10) 与缺失分组报错
		Map<String, Object> groupedList = service.listRows("SampleGrouped", Map.of(), 1, 50);
		Assertions.assertEquals(3, groupedList.get("total"));
		GmResult groupedGet = configGm.get("SampleGrouped", 10, null);
		Assertions.assertTrue(groupedGet.isOk());
		Assertions.assertEquals(1, groupedMgr.groupedGetCount.get());
		GmResult missingGroupId = configGm.get("SampleGrouped", 999, null);
		Assertions.assertFalse(missingGroupId.isOk());
		Assertions.assertEquals(ErrorMsgEnum.config_data_not_found.getId(), missingGroupId.getErrorCode());

		// GLOBAL_CONST 查询与按键获取
		GmResult constGet = configGm.get("GlobalConst", null, "MaxHeroLevel");
		Assertions.assertTrue(constGet.isOk());
		@SuppressWarnings("unchecked")
		Map<String, Object> constData = (Map<String, Object>) constGet.getData();
		Assertions.assertEquals("1.0", constData.get("schemaVersion"));
		Assertions.assertEquals(120, constData.get("value"));

		// 未知表或不存在ID返回 config_data_not_found
		GmResult missingTable = configGm.get("NoSuchTable", 1, null);
		Assertions.assertFalse(missingTable.isOk());
		Assertions.assertEquals(ErrorMsgEnum.config_data_not_found.getId(), missingTable.getErrorCode());

		GmResult missingId = configGm.get("SampleShop", 99999, null);
		Assertions.assertFalse(missingId.isOk());
		Assertions.assertEquals(ErrorMsgEnum.config_data_not_found.getId(), missingId.getErrorCode());
	}
}