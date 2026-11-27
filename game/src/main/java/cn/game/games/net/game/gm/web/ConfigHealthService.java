package cn.game.games.net.game.gm.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.net.common.module.activity.CrossGuildWarConfigValidator;
import cn.game.games.net.common.module.activity.CrossWorldBossConfigValidator;
import cn.game.games.net.game.gm.ConfigQueryService;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.config.FundPassRewardsConfig;
import cn.game.protocol.generated.config.ItemConfig;
import cn.game.protocol.generated.config.SecretscriptConfig;
import cn.game.protocol.generated.config.ShopItemConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.generated.manager.DefenceSkinManager;
import cn.game.protocol.generated.manager.EquipManager;
import cn.game.protocol.generated.manager.FairyFriendManager;
import cn.game.protocol.generated.manager.FundPassManager;
import cn.game.protocol.generated.manager.FundPassRewardsManager;
import cn.game.protocol.generated.manager.GemManager;
import cn.game.protocol.generated.manager.HCEquipManager;
import cn.game.protocol.generated.manager.HCHeroManager;
import cn.game.protocol.generated.manager.HeadBoxManager;
import cn.game.protocol.generated.manager.HeadPortraitManager;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.generated.manager.HeroSkinManager;
import cn.game.protocol.generated.manager.ItemManager;
import cn.game.protocol.generated.manager.PlayerFigureManager;
import cn.game.protocol.generated.manager.SecretscriptManager;
import cn.game.protocol.generated.manager.ShopItemManager;
import cn.game.protocol.generated.manager.SoulPetManager;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 静态配表目录浏览、受控热重载与静态引用完整性一键体检服务
 *
 * @author shuyongqiang
 */
public class ConfigHealthService {

	private static final Logger log = LoggerFactory.getLogger(ConfigHealthService.class);

	private final ConfigQueryService configQueryService = new ConfigQueryService();

	/**
	 * 获取全部已注册配表摘要及当前热更版本号
	 *
	 * @return 配表目录概览 Map
	 */
	public Map<String, Object> getSummary() {
		List<Map<String, Object>> tables = configQueryService.listTables();
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("resourceVersion", WatchServiceManager.getInstance().resourceVersion());
		result.put("tableCount", tables.size());
		result.put("tables", tables);
		return result;
	}

	/**
	 * 触发单表或全量配表热重载
	 *
	 * @param tableName 配置表名称，传 "ALL" 或 "*" 重载全部已注册表
	 * @return 重载结果明细 Map
	 */
	public Map<String, Object> reloadTables(String tableName) {
		long startNs = System.nanoTime();
		Map<String, ResourceListener> resources = WatchServiceManager.getInstance().getResources();
		String target = tableName != null ? tableName.trim() : "";

		List<Map<String, Object>> results = new ArrayList<>();
		int successCount = 0;
		int failCount = 0;

		boolean reloadAll = target.isEmpty() || "ALL".equalsIgnoreCase(target) || "*".equals(target);
		boolean bumped = false;
		for (Map.Entry<String, ResourceListener> entry : resources.entrySet()) {
			String name = entry.getKey();
			ResourceListener listener = entry.getValue();
			if (listener == null) {
				continue;
			}
			if (!reloadAll && !name.equalsIgnoreCase(target)) {
				continue;
			}
			if (!bumped) {
				WatchServiceManager.getInstance().bumpResourceVersion();
				bumped = true;
			}
			long t0 = System.nanoTime();
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("table", name);
			try {
				listener.reload();
				row.put("ok", true);
				row.put("message", "reloaded");
				successCount++;
			} catch (Throwable t) {
				log.error("热重载配表失败: table={}", name, t);
				row.put("ok", false);
				row.put("message", t.getClass().getSimpleName() + ": " + t.getMessage());
				failCount++;
			}
			row.put("elapsedMs", Math.max(0L, (System.nanoTime() - t0) / 1_000_000L));
			results.add(row);
		}

		Map<String, Object> summary = new LinkedHashMap<>();
		summary.put("ok", failCount == 0 && !results.isEmpty());
		summary.put("target", reloadAll ? "ALL" : target);
		summary.put("matchedCount", results.size());
		summary.put("successCount", successCount);
		summary.put("failCount", failCount);
		summary.put("resourceVersion", WatchServiceManager.getInstance().resourceVersion());
		summary.put("elapsedMs", Math.max(0L, (System.nanoTime() - startNs) / 1_000_000L));
		summary.put("results", results);
		if (results.isEmpty()) {
			summary.put("message", "未找到匹配的配置表监听器: " + target);
		}
		return summary;
	}

	/**
	 * 执行核心业务配表静态引用完整性体检（商品/基金奖励/自选宝箱/活动配置）
	 *
	 * @return 体检报告 Map
	 */
	public Map<String, Object> validateIntegrity() {
		long startNs = System.nanoTime();
		List<String> checkedTables = new ArrayList<>();
		List<Map<String, Object>> issues = new ArrayList<>();

		checkShopItems(checkedTables, issues);
		checkFundPassRewards(checkedTables, issues);
		checkItems(checkedTables, issues);
		checkActivities(checkedTables, issues);

		int errorCount = 0;
		int warnCount = 0;
		for (Map<String, Object> issue : issues) {
			if ("ERROR".equals(issue.get("severity"))) {
				errorCount++;
			} else {
				warnCount++;
			}
		}

		Map<String, Object> report = new LinkedHashMap<>();
		report.put("ok", errorCount == 0);
		report.put("resourceVersion", WatchServiceManager.getInstance().resourceVersion());
		report.put("checkedTables", checkedTables);
		report.put("issueCount", issues.size());
		report.put("errorCount", errorCount);
		report.put("warnCount", warnCount);
		report.put("issues", issues);
		report.put("elapsedMs", Math.max(0L, (System.nanoTime() - startNs) / 1_000_000L));
		return report;
	}

	private void checkShopItems(List<String> checkedTables, List<Map<String, Object>> issues) {
		if (ShopItemManager.instance() == null || ShopItemManager.instance().list() == null) {
			return;
		}
		checkedTables.add("ShopItem");
		Collection<ShopItemConfig> rows = ShopItemManager.instance().list();
		for (ShopItemConfig cfg : rows) {
			if (cfg == null) {
				continue;
			}
			if (cfg.Item == null || cfg.Item.length == 0) {
				addIssue(issues, "WARN", "ShopItem", cfg.ID, "商品包含物品 Item 为空");
			} else {
				for (int[] pair : cfg.Item) {
					if (pair == null || pair.length < 2) {
						addIssue(issues, "ERROR", "ShopItem", cfg.ID, "Item 二维配置长度不足2");
						continue;
					}
					if (!existsGoodsId(pair[0])) {
						addIssue(issues, "ERROR", "ShopItem", cfg.ID, "Item 引用了不存在的物品/资源ID: " + pair[0]);
					}
					if (pair[1] <= 0) {
						addIssue(issues, "WARN", "ShopItem", cfg.ID, "Item 发放数量 <= 0 (id=" + pair[0] + ", count=" + pair[1] + ")");
					}
				}
			}
			if (cfg.PurchaseParameter != null && cfg.PurchaseParameter.length >= 2 && cfg.PurchaseParameter[0] == 1) {
				int costId = cfg.PurchaseParameter[1];
				if (!existsGoodsId(costId)) {
					addIssue(issues, "ERROR", "ShopItem", cfg.ID, "PurchaseParameter 引用了不存在的消耗货币/道具ID: " + costId);
				}
			}
		}
	}

	private void checkFundPassRewards(List<String> checkedTables, List<Map<String, Object>> issues) {
		if (FundPassRewardsManager.instance() == null || FundPassRewardsManager.instance().list() == null) {
			return;
		}
		checkedTables.add("FundPassRewards");
		for (FundPassRewardsConfig cfg : FundPassRewardsManager.instance().list()) {
			if (cfg == null) {
				continue;
			}
			if (FundPassManager.instance() != null && FundPassManager.instance().getNullable(cfg.Index) == null) {
				addIssue(issues, "ERROR", "FundPassRewards", cfg.ID, "Index 引用了不存在的 FundPass ID: " + cfg.Index);
			}
			if (cfg.Reward != null) {
				for (int[] r : cfg.Reward) {
					if (r != null && r.length >= 2 && !existsGoodsId(r[0])) {
						addIssue(issues, "ERROR", "FundPassRewards", cfg.ID, "Reward 引用了不存在的物品/资源ID: " + r[0]);
					}
				}
			}
		}
	}

	private void checkItems(List<String> checkedTables, List<Map<String, Object>> issues) {
		if (ItemManager.instance() == null || ItemManager.instance().list() == null) {
			return;
		}
		checkedTables.add("Item");
		for (ItemConfig cfg : ItemManager.instance().list()) {
			if (cfg == null) {
				continue;
			}
			if (cfg.ItemType == 4 && cfg.Para1 != null) {
				for (int[] choice : cfg.Para1) {
					if (choice != null && choice.length >= 1 && !existsGoodsId(choice[0])) {
						addIssue(issues, "ERROR", "Item", cfg.ID, "任选宝箱 Para1 引用了不存在的物品ID: " + choice[0]);
					}
				}
			}
			if (cfg.ItemType == 7 && cfg.Para != null) {
				for (int subId : cfg.Para) {
					if (subId > 0 && !existsGoodsId(subId)) {
						addIssue(issues, "ERROR", "Item", cfg.ID, "随机道具 Para 引用了不存在的物品ID: " + subId);
					}
				}
			}
		}
	}

	private void checkActivities(List<String> checkedTables, List<Map<String, Object>> issues) {
		if (ActivityManager.instance() == null || ActivityManager.instance().list() == null) {
			return;
		}
		checkedTables.add("Activity");
		for (ActivityConfig cfg : ActivityManager.instance().list()) {
			if (cfg == null || cfg.disable) {
				continue;
			}
			if (cfg.rewards != null) {
				for (int[] r : cfg.rewards) {
					if (r != null && r.length >= 1 && r[0] > 0 && !existsGoodsId(r[0])) {
						addIssue(issues, "WARN", "Activity", cfg.ID, "rewards 引用了未知物品ID: " + r[0]);
					}
				}
			}
		}
		try {
			CrossWorldBossConfigValidator.validateAll();
			checkedTables.add("ActivityCrossWorldBoss");
		} catch (Throwable t) {
			addIssue(issues, "ERROR", "ActivityCrossWorldBoss", 0, "跨服世界Boss配置校验异常: " + t.getMessage());
		}
		try {
			CrossGuildWarConfigValidator.validateAll();
			checkedTables.add("ActivityCrossGuildWar");
		} catch (Throwable t) {
			addIssue(issues, "ERROR", "ActivityCrossGuildWar", 0, "跨服仙会战配置校验异常: " + t.getMessage());
		}
	}

	/**
	 * 判断物品或货币 ID 在当前已加载静态配置中是否存在
	 *
	 * @param id 物品/资源 ID
	 * @return true 表示存在
	 */
	public boolean existsGoodsId(int id) {
		if (id <= 0) {
			return false;
		}
		if (Asset.getNullable(id) != null) {
			return true;
		}
		int goodsTypeId = id / 100000;
		GoodsTypeEnum goodsType = GoodsTypeEnum.getNullable(goodsTypeId);
		if (goodsType == null) {
			return false;
		}
		switch (goodsType) {
		case Item:
			return ItemManager.instance() != null && ItemManager.instance().getNullable(id) != null;
		case Hero:
			return HeroManager.instance() != null && HeroManager.instance().getNullable(id) != null;
		case Equipment:
			return EquipManager.instance() != null && EquipManager.instance().getNullable(id) != null;
		case Gem:
			return GemManager.instance() != null && GemManager.instance().getNullable(id) != null;
		case Pet:
			return SoulPetManager.instance() != null && SoulPetManager.instance().getNullable(id) != null;
		case Secretscript:
			if (SecretscriptManager.instance() == null) {
				return false;
			}
			for (SecretscriptConfig cfg : SecretscriptManager.instance().list()) {
				if (cfg != null && (cfg.ID == id || cfg.SecretscriptMark == id)) {
					return true;
				}
			}
			return false;
		case FairyFriend:
			return FairyFriendManager.instance() != null && FairyFriendManager.instance().getNullable(id) != null;
		case HeroSkin:
			return HeroSkinManager.instance() != null && HeroSkinManager.instance().getNullable(id) != null;
		case DefenceSkin:
			return DefenceSkinManager.instance() != null && DefenceSkinManager.instance().getNullable(id) != null;
		case HeadBox:
			return HeadBoxManager.instance() != null && HeadBoxManager.instance().getNullable(id) != null;
		case HeadPortrait:
			return HeadPortraitManager.instance() != null && HeadPortraitManager.instance().getNullable(id) != null;
		case Figure:
			return PlayerFigureManager.instance() != null && PlayerFigureManager.instance().getNullable(id) != null;
		case HCHero:
			return HCHeroManager.instance() != null && HCHeroManager.instance().getNullable(id) != null;
		case Merge_Equip:
			return HCEquipManager.instance() != null && HCEquipManager.instance().getNullable(id) != null;
		default:
			return false;
		}
	}

	private void addIssue(List<Map<String, Object>> issues, String severity, String table, int id, String detail) {
		if (issues.size() >= 300) {
			return;
		}
		Map<String, Object> issue = new LinkedHashMap<>();
		issue.put("severity", severity);
		issue.put("table", table);
		issue.put("id", id);
		issue.put("detail", detail);
		issues.add(issue);
	}
}
