package cn.game.games.net.common.module.activity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.config.ActivityCrossWorldBossConfig;
import cn.game.protocol.generated.config.SecretscriptConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.ActivityCrossWorldBossManager;
import cn.game.protocol.generated.manager.ActivityManager;
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
import cn.game.protocol.generated.manager.MailManager;
import cn.game.protocol.generated.manager.PlayerFigureManager;
import cn.game.protocol.generated.manager.SecretscriptManager;
import cn.game.protocol.generated.manager.SoulPetManager;
import cn.game.protocol.manual.GoodsTypeEnum;

/**
 * 跨服世界Boss静态配置启动期与热重载合法性校验器。
 * <p>
 * 职责边界：在配置全量加载或 XML 热重载后执行跨表引用与数值合法性校验，
 * 并按 {@link ActivityCrossWorldBossManager#currentVersion()} 维护线程安全的不可变 {@code zoneOverrides} 解析缓存；
 * 不向生成类 {@code ActivityCrossWorldBossConfig} 或 {@code ActivityCrossWorldBossManager} 注入任何业务状态。
 * </p>
 *
 * @author shuyongqiang
 */
public final class CrossWorldBossConfigValidator {

	/** 校验器日志器 */
	private static final Logger log = LoggerFactory.getLogger(CrossWorldBossConfigValidator.class);

	/** 目标配置表名（用于构造统一异常上下文） */
	private static final String TABLE_NAME = "ActivityCrossWorldBoss";

	/** 里程碑万分比上限（100.00%） */
	private static final int MAX_MILESTONE_THRESHOLD = 10000;

	/** 物品 ID 提取商品类型编号的除数（对齐 ItemHelper.getGoodsType） */
	private static final int GOODS_TYPE_DIVISOR = 100000;

	/** 静态战区覆盖缓存写入互斥锁 */
	private static final Object CACHE_LOCK = new Object();

	/** 按配置管理器版本号隔离的战区覆盖只读缓存 */
	private static volatile ZoneOverrideSnapshot zoneOverrideSnapshot = new ZoneOverrideSnapshot(-1L);

	/**
	 * 私有构造，禁止实例化工具校验器
	 */
	private CrossWorldBossConfigValidator() {
	}

	/**
	 * 执行跨服世界Boss全表结构、数值边界、奖励物品与跨表引用校验，并刷新当前版本的战区覆盖缓存。
	 * <p>
	 * 调用前提：{@link ActivityManager}、{@link ActivityCrossWorldBossManager}、{@link MailManager} 与各资源表已完成加载。
	 * </p>
	 *
	 * @throws IllegalStateException 任一配置项违反结构、数值范围或跨表引用约束时抛出
	 */
	public static void validateAll() {
		// 校验 Activity 表中已启用的跨服世界Boss活动是否在业务表中有对应配置行
		for (ActivityConfig actCfg : ActivityManager.instance().list()) {
			if (actCfg != null && actCfg.type == ActivityTypeEnum.CrossWorldBoss.ID && !actCfg.disable) {
				if (ActivityCrossWorldBossManager.instance().getNullable(actCfg.ID) == null) {
					throw illegalConfig(actCfg.ID, "ID", actCfg.ID,
							"Activity表中已启用跨服世界Boss活动但缺少对应配置行");
				}
			}
		}

		// 逐行校验业务配置合法性并预先构建本版本的战区覆盖映射
		long currentVersion = ActivityCrossWorldBossManager.instance().currentVersion();
		ZoneOverrideSnapshot nextSnapshot = new ZoneOverrideSnapshot(currentVersion);
		for (ActivityCrossWorldBossConfig cfg : ActivityCrossWorldBossManager.instance().list()) {
			if (cfg == null) {
				continue;
			}
			validateConfigRow(cfg);
			Map<String, Integer> parsedOverrides = parseZoneOverrides(cfg);
			nextSnapshot.entries.put(cfg.ID, new ZoneOverrideEntry(cfg.zoneOverrides, parsedOverrides));
		}

		synchronized (CACHE_LOCK) {
			zoneOverrideSnapshot = nextSnapshot;
		}
		log.info("跨服世界Boss静态配置校验通过, configCount={}, version={}",
				ActivityCrossWorldBossManager.instance().list().size(), currentVersion);
	}

	/**
	 * 解析并获取指定配置行的静态战区覆盖映射（serverId -> groupId）。
	 * <p>
	 * 按 {@link ActivityCrossWorldBossManager#currentVersion()} 复用线程安全不可变映射；
	 * 若版本发生热重载变化或配置原文不一致，则按当前配置重新解析并刷新缓存。
	 * </p>
	 *
	 * @param cfg 跨服世界Boss配置行，允许为 null
	 * @return 不可变 {@code Map<String, Integer>}，入参为 null 或未配置覆盖时返回空 Map
	 * @throws IllegalStateException {@code zoneOverrides} 格式非法、区服重复或战区号不大于 0 时抛出
	 */
	public static Map<String, Integer> resolveZoneOverrides(ActivityCrossWorldBossConfig cfg) {
		if (cfg == null) {
			return Collections.emptyMap();
		}
		long currentVersion = ActivityCrossWorldBossManager.instance().currentVersion();
		ZoneOverrideSnapshot snapshot = zoneOverrideSnapshot;
		if (snapshot.version == currentVersion) {
			ZoneOverrideEntry cached = snapshot.entries.get(cfg.ID);
			if (cached != null && Objects.equals(cached.rawZoneOverrides, cfg.zoneOverrides)) {
				return cached.parsedMap;
			}
		}

		// 缓存未命中或版本切换时在同步块内双重检查后构建并写入不可变映射
		Map<String, Integer> parsed = parseZoneOverrides(cfg);
		synchronized (CACHE_LOCK) {
			ZoneOverrideSnapshot latest = zoneOverrideSnapshot;
			if (latest.version != currentVersion) {
				latest = new ZoneOverrideSnapshot(currentVersion);
				zoneOverrideSnapshot = latest;
			}
			ZoneOverrideEntry existing = latest.entries.get(cfg.ID);
			if (existing != null && Objects.equals(existing.rawZoneOverrides, cfg.zoneOverrides)) {
				return existing.parsedMap;
			}
			latest.entries.put(cfg.ID, new ZoneOverrideEntry(cfg.zoneOverrides, parsed));
		}
		return parsed;
	}

	/**
	 * 校验奖励或消耗物品 ID 是否在宿主已支持的 {@link GoodsTypeEnum} 资源表中真实存在。
	 * <p>
	 * 覆盖资源、道具、神将、头像框、宠物、装备、皮肤、合成神将、头像、形象、宝石、防线皮肤、神通、合成装备与仙友。
	 * </p>
	 *
	 * @param itemId 物品或资产 ID
	 * @return ID 大于 0 且在对应配置表中存在时返回 true，否则返回 false
	 */
	public static boolean isValidRewardId(int itemId) {
		if (itemId <= 0) {
			return false;
		}
		GoodsTypeEnum goodsType = GoodsTypeEnum.getNullable(itemId / GOODS_TYPE_DIVISOR);
		if (goodsType == null) {
			return false;
		}
		switch (goodsType) {
		case Resource:
			return Asset.getNullable(itemId) != null;
		case Item:
			return ItemManager.instance().getNullable(itemId) != null;
		case Hero:
			return HeroManager.instance().getNullable(itemId) != null;
		case HeadBox:
			return HeadBoxManager.instance().getNullable(itemId) != null;
		case Pet:
			return SoulPetManager.instance().getNullable(itemId) != null;
		case Equipment:
			return EquipManager.instance().getNullable(itemId) != null;
		case HeroSkin:
			return HeroSkinManager.instance().getNullable(itemId) != null;
		case HCHero:
			return HCHeroManager.instance().getNullable(itemId) != null;
		case HeadPortrait:
			return HeadPortraitManager.instance().getNullable(itemId) != null;
		case Figure:
			return PlayerFigureManager.instance().getNullable(itemId) != null;
		case Gem:
			return GemManager.instance().getNullable(itemId) != null;
		case DefenceSkin:
			return DefenceSkinManager.instance().getNullable(itemId) != null;
		case Secretscript:
			return hasSecretscriptId(itemId);
		case Merge_Equip:
			return HCEquipManager.instance().getNullable(itemId) != null
					|| EquipManager.instance().getNullable(itemId) != null;
		case FairyFriend:
			return FairyFriendManager.instance().getNullable(itemId) != null;
		default:
			return false;
		}
	}

	/**
	 * 检查神通配置表（唯一索引型表，无单主键 getNullable 方法）中是否存在指定配置 ID
	 *
	 * @param itemId 神通配置 ID
	 * @return 存在返回 true，否则返回 false
	 */
	private static boolean hasSecretscriptId(int itemId) {
		for (SecretscriptConfig cfg : SecretscriptManager.instance().list()) {
			if (cfg != null && cfg.ID == itemId) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 校验单行 {@link ActivityCrossWorldBossConfig} 的全部字段与跨表引用
	 *
	 * @param cfg 配置行对象
	 */
	private static void validateConfigRow(ActivityCrossWorldBossConfig cfg) {
		if (cfg.ID <= 0) {
			throw illegalConfig(cfg.ID, "ID", cfg.ID, "活动ID必须大于0");
		}

		// 校验基础数值与战区容量边界
		if (cfg.maxHp <= 0L) {
			throw illegalConfig(cfg.ID, "maxHp", cfg.maxHp, "Boss最大血量必须大于0");
		}
		if (cfg.groupSize <= 0) {
			throw illegalConfig(cfg.ID, "groupSize", cfg.groupSize, "每战区默认区服数必须大于0");
		}
		if (cfg.dailyFreeTimes < 0) {
			throw illegalConfig(cfg.ID, "dailyFreeTimes", cfg.dailyFreeTimes, "每日免费挑战次数不能为负");
		}
		if (cfg.dailyBuyLimit < 0) {
			throw illegalConfig(cfg.ID, "dailyBuyLimit", cfg.dailyBuyLimit, "每日最大可购次数不能为负");
		}
		if (cfg.dailyFreeTimes + cfg.dailyBuyLimit <= 0) {
			throw illegalConfig(cfg.ID, "dailyFreeTimes+dailyBuyLimit",
					cfg.dailyFreeTimes + "+" + cfg.dailyBuyLimit, "每日免费次数与可购次数之和必须大于0");
		}
		if (cfg.maxSingleDamage <= 0L || cfg.maxSingleDamage > cfg.maxHp) {
			throw illegalConfig(cfg.ID, "maxSingleDamage", cfg.maxSingleDamage,
					"单次上报最大伤害必须满足 0 < maxSingleDamage <= maxHp(" + cfg.maxHp + ")");
		}
		if (cfg.minParticipateDamage < 0L) {
			throw illegalConfig(cfg.ID, "minParticipateDamage", cfg.minParticipateDamage, "最低参与伤害门槛不能为负");
		}

		// 校验阶梯购买消耗数组结构与消耗物合法性
		if (cfg.dailyBuyLimit > 0 && (cfg.buyCosts == null || cfg.buyCosts.length == 0)) {
			throw illegalConfig(cfg.ID, "buyCosts", "empty", "允许购买次数(dailyBuyLimit=" + cfg.dailyBuyLimit + ")时buyCosts不能为空");
		}
		if (cfg.buyCosts != null) {
			for (int i = 0; i < cfg.buyCosts.length; i++) {
				validateRewardPairs(cfg.ID, "buyCosts[" + i + "]", cfg.buyCosts[i]);
			}
		}

		// 校验血量削减里程碑严格递增且奖励档位一一对应
		if (cfg.milestoneThresholds == null || cfg.milestoneThresholds.length == 0) {
			throw illegalConfig(cfg.ID, "milestoneThresholds", "empty", "里程碑阈值数组不能为空");
		}
		if (cfg.milestoneRewards == null || cfg.milestoneThresholds.length != cfg.milestoneRewards.length) {
			throw illegalConfig(cfg.ID, "milestoneRewards",
					cfg.milestoneRewards == null ? 0 : cfg.milestoneRewards.length,
					"里程碑奖励档位数量必须与milestoneThresholds长度(" + cfg.milestoneThresholds.length + ")一致");
		}
		int prevThreshold = 0;
		for (int i = 0; i < cfg.milestoneThresholds.length; i++) {
			int threshold = cfg.milestoneThresholds[i];
			if (threshold <= 0 || threshold > MAX_MILESTONE_THRESHOLD || threshold <= prevThreshold) {
				throw illegalConfig(cfg.ID, "milestoneThresholds[" + i + "]", threshold,
						"里程碑阈值必须严格递增且位于 (0, 10000] 范围内");
			}
			prevThreshold = threshold;
			validateRewardPairs(cfg.ID, "milestoneRewards[" + i + "]", cfg.milestoneRewards[i]);
		}

		// 校验最后一击奖励列表
		if (cfg.killRewards == null || cfg.killRewards.length == 0) {
			throw illegalConfig(cfg.ID, "killRewards", "empty", "最后一击奖励不能为空");
		}
		for (int i = 0; i < cfg.killRewards.length; i++) {
			validateRewardPairs(cfg.ID, "killRewards[" + i + "]", cfg.killRewards[i]);
		}

		// 校验伤害排行区间连续性、对应奖励及结算邮件模板存在性
		if (cfg.rankRanges == null || cfg.rankRanges.length == 0) {
			throw illegalConfig(cfg.ID, "rankRanges", "empty", "排行分段配置不能为空");
		}
		if (cfg.rankRewards == null || cfg.rankRanges.length != cfg.rankRewards.length) {
			throw illegalConfig(cfg.ID, "rankRewards",
					cfg.rankRewards == null ? 0 : cfg.rankRewards.length,
					"排行奖励档位数量必须与rankRanges长度(" + cfg.rankRanges.length + ")一致");
		}
		for (int i = 0; i < cfg.rankRanges.length; i++) {
			int[] range = cfg.rankRanges[i];
			if (range == null || range.length != 2) {
				throw illegalConfig(cfg.ID, "rankRanges[" + i + "]",
						range == null ? "null" : "len=" + range.length, "每个排行区间必须恰好包含[minRank, maxRank]");
			}
			int minRank = range[0];
			int maxRank = range[1];
			if (minRank < 1 || minRank > maxRank) {
				throw illegalConfig(cfg.ID, "rankRanges[" + i + "]", minRank + ";" + maxRank,
						"排行区间必须满足 1 <= minRank <= maxRank");
			}
			if (i == 0 && minRank != 1) {
				throw illegalConfig(cfg.ID, "rankRanges[0]", minRank + ";" + maxRank, "首个排行区间必须从第1名开始");
			}
			if (i > 0 && cfg.rankRanges[i - 1][1] + 1 != minRank) {
				throw illegalConfig(cfg.ID, "rankRanges[" + i + "]", minRank + ";" + maxRank,
						"相邻排行区间必须连续不重叠不留缝，期望起始名次=" + (cfg.rankRanges[i - 1][1] + 1));
			}
			validateRewardPairs(cfg.ID, "rankRewards[" + i + "]", cfg.rankRewards[i]);
		}
		if (cfg.rankMailId <= 0 || MailManager.instance().getNullable(cfg.rankMailId) == null) {
			throw illegalConfig(cfg.ID, "rankMailId", cfg.rankMailId, "伤害榜结算邮件ID在Mail表中不存在");
		}
	}

	/**
	 * 解析并校验 {@code zoneOverrides} 字段格式
	 *
	 * @param cfg 配置行对象
	 * @return 解析后的不可变映射
	 */
	private static Map<String, Integer> parseZoneOverrides(ActivityCrossWorldBossConfig cfg) {
		if (cfg.zoneOverrides == null || cfg.zoneOverrides.trim().isEmpty()) {
			return Collections.emptyMap();
		}
		String[] segments = cfg.zoneOverrides.split(";", -1);
		Map<String, Integer> map = new LinkedHashMap<>();
		for (String rawSegment : segments) {
			String segment = rawSegment.trim();
			if (segment.isEmpty()) {
				throw illegalConfig(cfg.ID, "zoneOverrides", cfg.zoneOverrides, "静态战区覆盖包含空分段");
			}
			String[] pair = segment.split(":", -1);
			if (pair.length != 2) {
				throw illegalConfig(cfg.ID, "zoneOverrides", segment, "静态战区覆盖项必须符合 serverId:groupId 格式");
			}
			String serverId = pair[0].trim();
			if (serverId.isEmpty()) {
				throw illegalConfig(cfg.ID, "zoneOverrides", segment, "静态战区覆盖项的 serverId 不能为空");
			}
			int groupId;
			try {
				groupId = Integer.parseInt(pair[1].trim());
			} catch (NumberFormatException e) {
				log.warn("解析跨服世界Boss静态战区覆盖失败: id={}, segment={}", cfg.ID, segment, e);
				throw illegalConfig(cfg.ID, "zoneOverrides", segment, "groupId 必须为合法整数", e);
			}
			if (groupId <= 0) {
				throw illegalConfig(cfg.ID, "zoneOverrides", segment, "groupId 必须大于0");
			}
			if (map.put(serverId, groupId) != null) {
				throw illegalConfig(cfg.ID, "zoneOverrides", serverId, "静态战区覆盖存在重复的 serverId");
			}
		}
		return Collections.unmodifiableMap(map);
	}

	/**
	 * 校验扁平偶数长度 {@code [itemId, count, ...]} 物品对数组
	 *
	 * @param configId  配置行 ID
	 * @param fieldName 字段名（含下标）
	 * @param pairs     物品与数量扁平数组
	 */
	private static void validateRewardPairs(int configId, String fieldName, int[] pairs) {
		if (pairs == null || pairs.length < 2 || pairs.length % 2 != 0) {
			throw illegalConfig(configId, fieldName, pairs == null ? "null" : "len=" + pairs.length,
					"物品配置组必须为长度大于等于2的偶数数组");
		}
		for (int i = 0; i < pairs.length; i += 2) {
			int itemId = pairs[i];
			int count = pairs[i + 1];
			if (itemId <= 0 || !isValidRewardId(itemId)) {
				throw illegalConfig(configId, fieldName, itemId, "物品ID不存在或不属于已支持的资源类型");
			}
			if (count <= 0) {
				throw illegalConfig(configId, fieldName, count, "物品数量必须大于0");
			}
		}
	}

	/**
	 * 构造统一格式的配置校验异常
	 *
	 * @param configId  配置行 ID
	 * @param fieldName 字段名
	 * @param value     非法值
	 * @param reason    失败说明
	 * @return IllegalStateException 实例
	 */
	private static IllegalStateException illegalConfig(int configId, String fieldName, Object value, String reason) {
		return new IllegalStateException(
				"配置表[" + TABLE_NAME + "]校验失败: ID=" + configId + ", 字段=" + fieldName + ", 非法值=" + value + " (" + reason + ")");
	}

	/**
	 * 构造携带根因异常的统一配置校验异常
	 *
	 * @param configId  配置行 ID
	 * @param fieldName 字段名
	 * @param value     非法值
	 * @param reason    失败说明
	 * @param cause     原始异常
	 * @return IllegalStateException 实例
	 */
	private static IllegalStateException illegalConfig(int configId, String fieldName, Object value, String reason,
			Throwable cause) {
		return new IllegalStateException(
				"配置表[" + TABLE_NAME + "]校验失败: ID=" + configId + ", 字段=" + fieldName + ", 非法值=" + value + " (" + reason + ")",
				cause);
	}

	/**
	 * 按配置版本隔离的缓存快照
	 */
	private static final class ZoneOverrideSnapshot {
		/** 关联的配置管理器版本号 */
		private final long version;
		/** 按活动配置 ID 索引的不可变战区覆盖映射 */
		private final ConcurrentHashMap<Integer, ZoneOverrideEntry> entries = new ConcurrentHashMap<>();

		private ZoneOverrideSnapshot(long version) {
			this.version = version;
		}
	}

	/**
	 * 单行配置解析缓存条目（保留原始串用于在单测无版本自增场景下防陈旧命中）
	 */
	private static final class ZoneOverrideEntry {
		/** 缓存构建时的原始配置串 */
		private final String rawZoneOverrides;
		/** 解析后的不可变战区覆盖映射 */
		private final Map<String, Integer> parsedMap;

		private ZoneOverrideEntry(String rawZoneOverrides, Map<String, Integer> parsedMap) {
			this.rawZoneOverrides = rawZoneOverrides;
			this.parsedMap = parsedMap;
		}
	}
}
