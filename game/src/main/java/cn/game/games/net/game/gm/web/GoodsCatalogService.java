package cn.game.games.net.game.gm.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.protocol.generated.config.DefenceSkinConfig;
import cn.game.protocol.generated.config.EquipConfig;
import cn.game.protocol.generated.config.FairyFriendConfig;
import cn.game.protocol.generated.config.GemConfig;
import cn.game.protocol.generated.config.HCEquipConfig;
import cn.game.protocol.generated.config.HCHeroConfig;
import cn.game.protocol.generated.config.HeadBoxConfig;
import cn.game.protocol.generated.config.HeadPortraitConfig;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.config.HeroSkinConfig;
import cn.game.protocol.generated.config.ItemConfig;
import cn.game.protocol.generated.config.PlayerFigureConfig;
import cn.game.protocol.generated.config.SecretscriptConfig;
import cn.game.protocol.generated.config.SoulPetConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.DefenceSkinManager;
import cn.game.protocol.generated.manager.EquipManager;
import cn.game.protocol.generated.manager.FairyFriendManager;
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
import cn.game.protocol.generated.manager.SoulPetManager;
import cn.game.protocol.manual.GoodsTypeEnum;

/**
 * 全物品大类与配表条目只读目录服务，为 GM 控制台 2 级联动选品器提供统一数据源
 *
 * @author shuyongqiang
 */
public class GoodsCatalogService {

	private static final Logger log = LoggerFactory.getLogger(GoodsCatalogService.class);

	public static final int DEFAULT_ITEM_LIMIT = 2000;
	public static final int MAX_ITEM_LIMIT = 5000;

	/**
	 * 物品大类元数据定义
	 */
	private static final class CategoryDef {
		final GoodsTypeEnum goodsType;
		final String label;
		final String tableName;
		final boolean supportsSet;
		final boolean supportsDel;
		final String hint;

		CategoryDef(GoodsTypeEnum goodsType, String label, String tableName, boolean supportsSet, boolean supportsDel, String hint) {
			this.goodsType = goodsType;
			this.label = label;
			this.tableName = tableName;
			this.supportsSet = supportsSet;
			this.supportsDel = supportsDel;
			this.hint = hint;
		}
	}

	private static final List<CategoryDef> CATEGORIES = List.of(
			new CategoryDef(GoodsTypeEnum.Resource, "玩家资源 (货币/体力/经验)", "Asset", true, true, "普通货币/体力支持发放、精确设置与扣除；经验类仅建议发放或用设等级/VIP"),
			new CategoryDef(GoodsTypeEnum.Item, "道具 (材料/碎片/宝箱/券)", "Item", true, true, "可堆叠道具支持发放、精确设置与扣除；自动开启/折算道具(ItemType=5~8)仅支持发放"),
			new CategoryDef(GoodsTypeEnum.Hero, "英雄 (神将)", "Hero", false, false, "非堆叠模块，支持按配置ID直接发放"),
			new CategoryDef(GoodsTypeEnum.Equipment, "装备", "Equip", false, false, "非堆叠模块，支持按配置ID发放"),
			new CategoryDef(GoodsTypeEnum.Gem, "宝石", "Gem", false, false, "非堆叠模块，支持按配置ID发放"),
			new CategoryDef(GoodsTypeEnum.Pet, "灵宠", "SoulPet", false, false, "支持按配置ID直接发放"),
			new CategoryDef(GoodsTypeEnum.Secretscript, "神通", "Secretscript", false, false, "支持按配置ID直接发放"),
			new CategoryDef(GoodsTypeEnum.FairyFriend, "仙友", "FairyFriend", false, false, "支持按配置ID直接发放"),
			new CategoryDef(GoodsTypeEnum.HeroSkin, "英雄皮肤", "HeroSkin", false, false, "唯一解锁类，支持按配置ID发放"),
			new CategoryDef(GoodsTypeEnum.DefenceSkin, "防线皮肤", "DefenceSkin", false, false, "支持按配置ID发放"),
			new CategoryDef(GoodsTypeEnum.HeadBox, "头像框", "HeadBox", false, false, "唯一解锁类，支持按配置ID发放"),
			new CategoryDef(GoodsTypeEnum.HeadPortrait, "头像", "HeadPortrait", false, false, "唯一解锁类，支持按配置ID发放"),
			new CategoryDef(GoodsTypeEnum.Figure, "玩家形象", "PlayerFigure", false, false, "唯一解锁类，支持按配置ID发放"),
			new CategoryDef(GoodsTypeEnum.HCHero, "合成英雄", "HCHero", false, false, "支持按配置ID发放"),
			new CategoryDef(GoodsTypeEnum.Merge_Equip, "合并装备", "HCEquip", false, false, "支持按配置ID发放")
	);

	/**
	 * 统一查询入口：不传 typeId 返回大类目录列表，传入 typeId 返回该大类下的配表条目
	 *
	 * @param typeId 物品大类 ID（对应 GoodsTypeEnum.getId()），为空或 <=0 时仅返回大类目录
	 * @param keyword 可选关键字（匹配 ID、中文名称或备注）
	 * @param limit 返回条目上限（默认 2000，上限 5000）
	 * @return 目录视图 Map
	 */
	public Map<String, Object> queryCatalog(Integer typeId, String keyword, Integer limit) {
		if (typeId == null || typeId <= 0) {
			return listCategories();
		}
		int effectiveLimit = DEFAULT_ITEM_LIMIT;
		if (limit != null && limit > 0) {
			effectiveLimit = Math.min(limit, MAX_ITEM_LIMIT);
		}
		return listItems(typeId, keyword, effectiveLimit);
	}

	/**
	 * 获取全部物品大类摘要及各配置表当前条目数
	 *
	 * @return 大类列表响应体
	 */
	public Map<String, Object> listCategories() {
		List<Map<String, Object>> list = new ArrayList<>(CATEGORIES.size());
		for (CategoryDef def : CATEGORIES) {
			int count = countByType(def.goodsType);
			list.add(toCategoryMap(def, count));
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("simulationEnabled", true);
		result.put("categories", list);
		return result;
	}

	/**
	 * 按物品大类与可选关键字查询对应配置表条目
	 *
	 * @param typeId GoodsTypeEnum ID
	 * @param keyword 模糊搜索关键字（按 ID / 名称 / 备注匹配）
	 * @param limit 返回数量上限
	 * @return 条目列表响应体
	 */
	public Map<String, Object> listItems(int typeId, String keyword, int limit) {
		CategoryDef def = findCategory(typeId);
		if (def == null) {
			Map<String, Object> empty = new LinkedHashMap<>();
			empty.put("simulationEnabled", true);
			empty.put("category", null);
			empty.put("total", 0);
			empty.put("truncated", false);
			empty.put("items", Collections.emptyList());
			return empty;
		}
		List<Map<String, Object>> allItems = loadItemsByType(def);
		allItems.sort(Comparator.comparingInt(m -> ((Number) m.get("id")).intValue()));

		String kw = keyword != null ? keyword.trim().toLowerCase(Locale.ROOT) : "";
		List<Map<String, Object>> matched = new ArrayList<>();
		for (Map<String, Object> item : allItems) {
			if (kw.isEmpty() || matchesKeyword(item, kw)) {
				matched.add(item);
			}
		}

		int total = matched.size();
		boolean truncated = total > limit;
		List<Map<String, Object>> sliced = truncated ? new ArrayList<>(matched.subList(0, limit)) : matched;

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("simulationEnabled", true);
		result.put("category", toCategoryMap(def, allItems.size()));
		result.put("total", total);
		result.put("truncated", truncated);
		result.put("items", sliced);
		return result;
	}

	private CategoryDef findCategory(int typeId) {
		for (CategoryDef def : CATEGORIES) {
			if (def.goodsType.getId() == typeId) {
				return def;
			}
		}
		return null;
	}

	private Map<String, Object> toCategoryMap(CategoryDef def, int count) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("typeId", def.goodsType.getId());
		map.put("typeKey", def.goodsType.name());
		map.put("label", def.label);
		map.put("table", def.tableName);
		map.put("count", count);
		map.put("supportsSet", def.supportsSet);
		map.put("supportsDel", def.supportsDel);
		map.put("hint", def.hint);
		return map;
	}

	private int countByType(GoodsTypeEnum type) {
		try {
			switch (type) {
			case Resource:
				return Asset.values().length;
			case Item:
				return safeSize(ItemManager.instance() != null ? ItemManager.instance().list() : null);
			case Hero:
				return safeSize(HeroManager.instance() != null ? HeroManager.instance().list() : null);
			case Equipment:
				return safeSize(EquipManager.instance() != null ? EquipManager.instance().list() : null);
			case Gem:
				return safeSize(GemManager.instance() != null ? GemManager.instance().list() : null);
			case Pet:
				return safeSize(SoulPetManager.instance() != null ? SoulPetManager.instance().list() : null);
			case Secretscript:
				return safeSize(SecretscriptManager.instance() != null ? SecretscriptManager.instance().list() : null);
			case FairyFriend:
				return safeSize(FairyFriendManager.instance() != null ? FairyFriendManager.instance().list() : null);
			case HeroSkin:
				return safeSize(HeroSkinManager.instance() != null ? HeroSkinManager.instance().list() : null);
			case DefenceSkin:
				return safeSize(DefenceSkinManager.instance() != null ? DefenceSkinManager.instance().list() : null);
			case HeadBox:
				return safeSize(HeadBoxManager.instance() != null ? HeadBoxManager.instance().list() : null);
			case HeadPortrait:
				return safeSize(HeadPortraitManager.instance() != null ? HeadPortraitManager.instance().list() : null);
			case Figure:
				return safeSize(PlayerFigureManager.instance() != null ? PlayerFigureManager.instance().list() : null);
			case HCHero:
				return safeSize(HCHeroManager.instance() != null ? HCHeroManager.instance().list() : null);
			case Merge_Equip:
				return safeSize(HCEquipManager.instance() != null ? HCEquipManager.instance().list() : null);
			default:
				return 0;
			}
		} catch (Throwable t) {
			log.warn("统计物品大类配置条目数失败: type={}", type, t);
			return 0;
		}
	}

	private List<Map<String, Object>> loadItemsByType(CategoryDef def) {
		List<Map<String, Object>> items = new ArrayList<>();
		try {
			switch (def.goodsType) {
			case Resource:
				for (Asset asset : Asset.values()) {
					boolean isExp = asset.Type == 2;
					String subLabel = asset.Type == 1 ? "货币" : (asset.Type == 2 ? "经验" : (asset.Type == 3 ? "体力" : ("Type=" + asset.Type)));
					String note = isExp ? "经验类不支持 item.set，请用发放或设等级/VIP" : safeStr(asset.Tips);
					items.add(buildItem(asset.ID, nonBlank(asset.Desc, asset.Name, "资源#" + asset.ID),
							subLabel, asset.Quality, !isExp, !isExp, note));
				}
				break;
			case Item:
				if (ItemManager.instance() != null && ItemManager.instance().list() != null) {
					for (ItemConfig cfg : ItemManager.instance().list()) {
						boolean autoOpen = cfg.ItemType >= 5 && cfg.ItemType <= 8;
						String note = autoOpen ? "自动开启/折算道具(ItemType=" + cfg.ItemType + ")，仅支持发放" : safeStr(cfg.Tips);
						items.add(buildItem(cfg.ID, nonBlank(cfg.Name, null, "道具#" + cfg.ID),
								itemTypeLabel(cfg.ItemType), cfg.Quality, !autoOpen, !autoOpen, note));
					}
				}
				break;
			case Hero:
				if (HeroManager.instance() != null && HeroManager.instance().list() != null) {
					for (HeroConfig cfg : HeroManager.instance().list()) {
						String heroTypeLabel = cfg.HeroType == 1 ? "神将" : (cfg.HeroType == 2 ? "临时卡" : "召唤物");
						items.add(buildItem(cfg.ID, nonBlank(cfg.name, null, "英雄#" + cfg.ID),
								heroTypeLabel, cfg.InitialQuality, false, false, safeStr(cfg.HeroCharacteristic)));
					}
				}
				break;
			case Equipment:
				if (EquipManager.instance() != null && EquipManager.instance().list() != null) {
					for (EquipConfig cfg : EquipManager.instance().list()) {
						String sub = "部位" + cfg.pos + " Lv." + cfg.level;
						items.add(buildItem(cfg.ID, nonBlank(cfg.name, null, "装备#" + cfg.ID),
								sub, cfg.quality, false, false, ""));
					}
				}
				break;
			case Gem:
				if (GemManager.instance() != null && GemManager.instance().list() != null) {
					for (GemConfig cfg : GemManager.instance().list()) {
						String sub = "部位" + cfg.pos;
						items.add(buildItem(cfg.ID, nonBlank(cfg.name, null, "宝石#" + cfg.ID),
								sub, cfg.quality, false, false, ""));
					}
				}
				break;
			case Pet:
				if (SoulPetManager.instance() != null && SoulPetManager.instance().list() != null) {
					for (SoulPetConfig cfg : SoulPetManager.instance().list()) {
						items.add(buildItem(cfg.ID, nonBlank(cfg.Name, null, "灵宠#" + cfg.ID),
								"灵宠", 0, false, false, ""));
					}
				}
				break;
			case Secretscript:
				if (SecretscriptManager.instance() != null && SecretscriptManager.instance().list() != null) {
					for (SecretscriptConfig cfg : SecretscriptManager.instance().list()) {
						String sub = nonBlank(cfg.SecretscriptTag, null, "星级" + cfg.SecretscriptStar);
						items.add(buildItem(cfg.ID, nonBlank(cfg.SecretscriptName, null, "神通#" + cfg.ID),
								sub, cfg.SecretscriptQuality, false, false, safeStr(cfg.SecretscriptDescription)));
					}
				}
				break;
			case FairyFriend:
				if (FairyFriendManager.instance() != null && FairyFriendManager.instance().list() != null) {
					for (FairyFriendConfig cfg : FairyFriendManager.instance().list()) {
						items.add(buildItem(cfg.ID, nonBlank(cfg.Name, null, "仙友#" + cfg.ID),
								"仙友", 0, false, false, ""));
					}
				}
				break;
			case HeroSkin:
				if (HeroSkinManager.instance() != null && HeroSkinManager.instance().list() != null) {
					for (HeroSkinConfig cfg : HeroSkinManager.instance().list()) {
						String heroName = resolveHeroName(cfg.HeroID);
						String display = heroName.isEmpty() ? ("英雄皮肤#" + cfg.ID) : (heroName + " 皮肤#" + cfg.ID);
						items.add(buildItem(cfg.ID, display, "HeroID=" + cfg.HeroID, cfg.Quality, false, false, ""));
					}
				}
				break;
			case DefenceSkin:
				if (DefenceSkinManager.instance() != null && DefenceSkinManager.instance().list() != null) {
					for (DefenceSkinConfig cfg : DefenceSkinManager.instance().list()) {
						items.add(buildItem(cfg.ID, nonBlank(cfg.Name, null, "防线皮肤#" + cfg.ID),
								"防线皮肤", 0, false, false, ""));
					}
				}
				break;
			case HeadBox:
				if (HeadBoxManager.instance() != null && HeadBoxManager.instance().list() != null) {
					for (HeadBoxConfig cfg : HeadBoxManager.instance().list()) {
						String period = cfg.Time > 0 ? ("限时" + cfg.Time + "秒") : "永久";
						items.add(buildItem(cfg.ID, "头像框#" + cfg.ID + " (" + period + ")", period, 0, false, false, ""));
					}
				}
				break;
			case HeadPortrait:
				if (HeadPortraitManager.instance() != null && HeadPortraitManager.instance().list() != null) {
					for (HeadPortraitConfig cfg : HeadPortraitManager.instance().list()) {
						String heroName = resolveHeroName(cfg.ConditionHero);
						String display = heroName.isEmpty() ? ("头像#" + cfg.ID) : (heroName + " 头像#" + cfg.ID);
						items.add(buildItem(cfg.ID, display, cfg.ConditionHero > 0 ? ("HeroID=" + cfg.ConditionHero) : "通用头像", cfg.Quality, false, false, ""));
					}
				}
				break;
			case Figure:
				if (PlayerFigureManager.instance() != null && PlayerFigureManager.instance().list() != null) {
					for (PlayerFigureConfig cfg : PlayerFigureManager.instance().list()) {
						String heroName = resolveHeroName(cfg.ConditionHero);
						String display = heroName.isEmpty() ? ("玩家形象#" + cfg.ID) : (heroName + " 形象#" + cfg.ID);
						String sub = cfg.Times > 0 ? ("限时" + cfg.Times + "秒") : "永久";
						items.add(buildItem(cfg.ID, display, sub, cfg.Quality, false, false, ""));
					}
				}
				break;
			case HCHero:
				if (HCHeroManager.instance() != null && HCHeroManager.instance().list() != null) {
					for (HCHeroConfig cfg : HCHeroManager.instance().list()) {
						items.add(buildItem(cfg.ID, nonBlank(cfg.Name, null, "合成英雄#" + cfg.ID),
								"合成英雄", 0, false, false, ""));
					}
				}
				break;
			case Merge_Equip:
				if (HCEquipManager.instance() != null && HCEquipManager.instance().list() != null) {
					for (HCEquipConfig cfg : HCEquipManager.instance().list()) {
						String display = "合并装备#" + cfg.ID + " (组" + cfg.EquipGroup + " Lv." + cfg.EquipLv + ")";
						items.add(buildItem(cfg.ID, display, "Type=" + cfg.ItemType, cfg.Quality, false, false, ""));
					}
				}
				break;
			default:
				break;
			}
		} catch (Throwable t) {
			log.warn("加载物品大类配置条目失败: type={}", def.goodsType, t);
		}
		return items;
	}

	private Map<String, Object> buildItem(int id, String name, String subType, int quality, boolean canSet, boolean canDel, String note) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("id", id);
		map.put("name", name);
		map.put("subType", subType != null ? subType : "");
		map.put("quality", quality);
		map.put("qualityName", qualityLabel(quality));
		map.put("canSet", canSet);
		map.put("canDel", canDel);
		map.put("note", note != null ? note : "");
		return map;
	}

	private boolean matchesKeyword(Map<String, Object> item, String kwLower) {
		String idStr = String.valueOf(item.get("id"));
		if (idStr.contains(kwLower)) {
			return true;
		}
		String name = String.valueOf(item.getOrDefault("name", "")).toLowerCase(Locale.ROOT);
		if (name.contains(kwLower)) {
			return true;
		}
		String subType = String.valueOf(item.getOrDefault("subType", "")).toLowerCase(Locale.ROOT);
		if (subType.contains(kwLower)) {
			return true;
		}
		String note = String.valueOf(item.getOrDefault("note", "")).toLowerCase(Locale.ROOT);
		return note.contains(kwLower);
	}

	private String resolveHeroName(int heroId) {
		if (heroId <= 0 || HeroManager.instance() == null) {
			return "";
		}
		try {
			HeroConfig hero = HeroManager.instance().getNullable(heroId);
			return hero != null && hero.name != null ? hero.name.trim() : "";
		} catch (Throwable t) {
			log.warn("解析关联英雄名称失败: heroId={}", heroId, t);
			return "";
		}
	}

	private int safeSize(Collection<?> col) {
		return col != null ? col.size() : 0;
	}

	private String safeStr(String s) {
		return s != null ? s.trim() : "";
	}

	private String nonBlank(String primary, String secondary, String fallback) {
		if (primary != null && !primary.isBlank()) {
			return primary.trim();
		}
		if (secondary != null && !secondary.isBlank()) {
			return secondary.trim();
		}
		return fallback;
	}

	private String itemTypeLabel(int itemType) {
		switch (itemType) {
		case 1:
			return "英雄突破";
		case 2:
			return "英雄升级";
		case 3:
			return "请神道具";
		case 4:
			return "任选宝箱";
		case 5:
			return "巡逻经验(自动开)";
		case 6:
			return "巡逻金币(自动开)";
		case 7:
			return "随机道具(自动开)";
		case 8:
			return "掉落道具(自动开)";
		case 9:
			return "一般道具";
		case 10:
			return "挑战券";
		case 11:
			return "神将碎片";
		case 12:
			return "灵宠碎片";
		case 13:
			return "手操卡";
		default:
			return "ItemType=" + itemType;
		}
	}

	private String qualityLabel(int quality) {
		switch (quality) {
		case 1:
			return "白";
		case 2:
			return "绿";
		case 3:
			return "蓝";
		case 4:
			return "紫";
		case 5:
			return "金";
		case 6:
			return "红";
		case 7:
			return "彩";
		case 8:
			return "永恒";
		case 9:
			return "唯一";
		default:
			return "";
		}
	}
}
