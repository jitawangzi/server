package cn.game.games.net.common.module.activity;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.config.ActivityCrossGuildWarConfig;
import cn.game.protocol.generated.config.ItemConfig;
import cn.game.protocol.generated.config.RandomGivenConfig;
import cn.game.protocol.generated.config.RandomGroupConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.ActivityCrossGuildWarManager;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.generated.manager.ItemManager;
import cn.game.protocol.generated.manager.MailManager;
import cn.game.protocol.generated.manager.RandomGivenManager;
import cn.game.protocol.generated.manager.RandomGroupManager;
import cn.game.protocol.manual.GoodsTypeEnum;

/**
 * 跨服宗门争霸静态配置启动期与热重载合法性校验器。
 * <p>
 * 职责边界：在配置全量加载或 XML 热重载后执行跨表引用、数组对齐、战区覆盖与高危公会资产阻断校验，
 * 并按 {@link ActivityCrossGuildWarManager#currentVersion()} 维护线程安全的不可变 {@code zoneOverrides} 解析缓存；
 * 不向生成类 {@code ActivityCrossGuildWarConfig} 或 {@code ActivityCrossGuildWarManager} 注入任何业务状态。
 * </p>
 *
 * @author shuyongqiang
 */
public final class CrossGuildWarConfigValidator {

	/** 校验器日志器 */
	private static final Logger log = LoggerFactory.getLogger(CrossGuildWarConfigValidator.class);

	/** 目标配置表名（用于构造统一异常上下文） */
	private static final String TABLE_NAME = "ActivityCrossGuildWar";

	/** 物品 ID 提取商品类型编号的除数（对齐 ItemHelper.getGoodsType） */
	private static final int GOODS_TYPE_DIVISOR = 100000;

	/** 静态战区覆盖缓存写入互斥锁 */
	private static final Object CACHE_LOCK = new Object();

	/** 按配置管理器版本号隔离的战区覆盖只读缓存 */
	private static volatile ZoneOverrideSnapshot zoneOverrideSnapshot = new ZoneOverrideSnapshot(-1L);

	/**
	 * 私有构造，禁止实例化工具校验器
	 */
	private CrossGuildWarConfigValidator() {
	}

	/**
	 * 执行跨服宗门争霸全表结构、数值边界、奖励物品与跨表引用校验，并刷新当前版本的战区覆盖缓存。
	 * <p>
	 * 调用前提：{@link ActivityManager}、{@link ActivityCrossGuildWarManager}、{@link MailManager} 与各资源表已完成加载。
	 * </p>
	 *
	 * @throws IllegalStateException 任一配置项违反结构、数值范围、高危公会资产阻断或跨表引用约束时抛出
	 */
	public static void validateAll() {
		// 校验 Activity 表中已启用的跨服宗门争霸活动是否在业务表中有对应配置行
		for (ActivityConfig actCfg : ActivityManager.instance().list()) {
			if (actCfg != null && actCfg.type == ActivityTypeEnum.CrossGuildWar.ID && !actCfg.disable) {
				if (ActivityCrossGuildWarManager.instance().getNullable(actCfg.ID) == null) {
					throw illegalConfig(actCfg.ID, "ID", actCfg.ID,
							"Activity表中已启用跨服宗门争霸活动但缺少对应配置行");
				}
			}
		}

		// 逐行校验业务配置合法性并预先构建本版本的战区覆盖映射
		long currentVersion = ActivityCrossGuildWarManager.instance().currentVersion();
		ZoneOverrideSnapshot nextSnapshot = new ZoneOverrideSnapshot(currentVersion);
		for (ActivityCrossGuildWarConfig cfg : ActivityCrossGuildWarManager.instance().list()) {
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
		log.info("跨服宗门争霸静态配置校验通过, configCount={}, version={}",
				ActivityCrossGuildWarManager.instance().list().size(), currentVersion);
	}

	/**
	 * 解析并获取指定配置行的静态战区覆盖映射（serverId -> groupId）。
	 * <p>
	 * 按 {@link ActivityCrossGuildWarManager#currentVersion()} 复用线程安全不可变映射；
	 * 若版本发生热重载变化或配置原文不一致，则按当前配置重新解析并刷新缓存。
	 * </p>
	 *
	 * @param cfg 跨服宗门争霸配置行，允许为 null
	 * @return 不可变 {@code Map<String, Integer>}，入参为 null 或未配置覆盖时返回空 Map
	 * @throws IllegalStateException {@code zoneOverrides} 格式非法、区服重复或战区号不大于 0 时抛出
	 */
	public static Map<String, Integer> resolveZoneOverrides(ActivityCrossGuildWarConfig cfg) {
		if (cfg == null) {
			return Collections.emptyMap();
		}
		long currentVersion = ActivityCrossGuildWarManager.instance().currentVersion();
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
	 * 校验奖励或消耗物品 ID 是否在宿主已支持的资源表中真实存在（直接委托复用 {@link CrossWorldBossConfigValidator#isValidRewardId}）。
	 *
	 * @param itemId 物品或资产 ID
	 * @return ID 大于 0 且在对应配置表中存在时返回 true，否则返回 false
	 */
	public static boolean isValidRewardId(int itemId) {
		return CrossWorldBossConfigValidator.isValidRewardId(itemId);
	}

	/**
	 * 判断物品 ID 是否属于或会间接掉落禁止在跨服宗门争霸奖励/消耗中配置的跨服公会资产（100701~100703）。
	 * <p>
	 * 防止后续 {@code PlayerHelper.addResources} 或开启宝箱/掉落规则触发 {@code GuildModule} 的 {@code GetItem} 监听器向 Cross Guild 发起跨服 RPC。
	 * </p>
	 *
	 * @param itemId 物品或资产 ID
	 * @return 直接或间接包含 100701（GuildPoint）、100702（GuildExp）或 100703（GuildContribute）时返回 true
	 */
	public static boolean isCrossGuildUnsafeAsset(int itemId) {
		return isCrossGuildUnsafeAsset(itemId, new HashSet<>(), new HashSet<>());
	}

	/**
	 * 带环路保护地递归判定物品是否直接或间接包含跨服公会资产（100701~100703）
	 *
	 * @param itemId         物品或资产 ID
	 * @param visitedItemIds 已访问物品 ID 集合（防递归环路）
	 * @param visitedDropIds 已访问掉落规则 ID 集合（防递归环路）
	 * @return 命中直接或间接公会资产时返回 true
	 */
	private static boolean isCrossGuildUnsafeAsset(int itemId, Set<Integer> visitedItemIds,
			Set<Integer> visitedDropIds) {
		if (itemId == Asset.GuildPoint.ID
				|| itemId == Asset.GuildExp.ID
				|| itemId == Asset.GuildContribute.ID) {
			return true;
		}
		if (itemId <= 0 || !visitedItemIds.add(itemId)) {
			return false;
		}
		GoodsTypeEnum goodsType = GoodsTypeEnum.getNullable(itemId / GOODS_TYPE_DIVISOR);
		if (goodsType != GoodsTypeEnum.Item) {
			return false;
		}
		ItemConfig itemCfg = ItemManager.instance().getNullable(itemId);
		if (itemCfg == null) {
			return false;
		}
		if (itemCfg.ItemType == 4 && itemCfg.Para1 != null) {
			for (int[] row : itemCfg.Para1) {
				if (row != null && row.length > 0
						&& isCrossGuildUnsafeAsset(row[0], visitedItemIds, visitedDropIds)) {
					return true;
				}
			}
		} else if (itemCfg.ItemType == 7 && itemCfg.Para != null) {
			for (int subItemId : itemCfg.Para) {
				if (isCrossGuildUnsafeAsset(subItemId, visitedItemIds, visitedDropIds)) {
					return true;
				}
			}
		} else if (itemCfg.ItemType == 8 && itemCfg.Para != null) {
			for (int givenId : itemCfg.Para) {
				if (isUnsafeRandomGiven(givenId, visitedItemIds, visitedDropIds)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 递归检查 {@link RandomGivenConfig} 必掉与随机掉落组是否包含跨服公会资产（100701~100703）
	 *
	 * @param givenId        RandomGiven 配置 ID
	 * @param visitedItemIds 已访问物品 ID 集合
	 * @param visitedDropIds 已访问掉落规则 ID 集合
	 * @return 命中不安全资产时返回 true
	 */
	private static boolean isUnsafeRandomGiven(int givenId, Set<Integer> visitedItemIds,
			Set<Integer> visitedDropIds) {
		if (givenId <= 0 || !visitedDropIds.add(givenId)) {
			return false;
		}
		RandomGivenConfig givenCfg = RandomGivenManager.instance().getNullable(givenId);
		if (givenCfg == null) {
			return false;
		}
		if (givenCfg.MustGiven != null) {
			for (int[] row : givenCfg.MustGiven) {
				if (row == null) {
					continue;
				}
				for (int i = 0; i < row.length; i += 2) {
					if (isCrossGuildUnsafeAsset(row[i], visitedItemIds, visitedDropIds)) {
						return true;
					}
				}
			}
		}
		if (givenCfg.RandomParameterGroupId != null) {
			for (int groupId : givenCfg.RandomParameterGroupId) {
				if (isUnsafeRandomGroup(groupId, visitedItemIds, visitedDropIds)) {
					return true;
				}
			}
		}
		if (givenCfg.FixedNumRandomDrop != null) {
			for (int[] row : givenCfg.FixedNumRandomDrop) {
				if (row != null && row.length > 0
						&& isUnsafeRandomGroup(row[0], visitedItemIds, visitedDropIds)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 递归检查 {@link RandomGroupConfig} 掉落组条目是否包含跨服公会资产（100701~100703）
	 *
	 * @param groupId        掉落组 ID
	 * @param visitedItemIds 已访问物品 ID 集合
	 * @param visitedDropIds 已访问掉落规则 ID 集合
	 * @return 命中不安全资产时返回 true
	 */
	private static boolean isUnsafeRandomGroup(int groupId, Set<Integer> visitedItemIds,
			Set<Integer> visitedDropIds) {
		if (groupId <= 0) {
			return false;
		}
		List<RandomGroupConfig> groupConfigs = RandomGroupManager.instance().getRandomGroupIDList(groupId);
		if (groupConfigs == null) {
			return false;
		}
		for (RandomGroupConfig groupCfg : groupConfigs) {
			if (groupCfg != null && isCrossGuildUnsafeAsset(groupCfg.AssetID, visitedItemIds, visitedDropIds)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 校验单行 {@link ActivityCrossGuildWarConfig} 的全部字段、数组对齐与跨表引用
	 *
	 * @param cfg 配置行对象
	 */
	private static void validateConfigRow(ActivityCrossGuildWarConfig cfg) {
		if (cfg.ID <= 0) {
			throw illegalConfig(cfg.ID, "ID", cfg.ID, "活动ID必须大于0");
		}
		if (cfg.groupSize <= 0) {
			throw illegalConfig(cfg.ID, "groupSize", cfg.groupSize, "每战区默认区服数必须大于0");
		}
		if (cfg.dailyFreeTimes < 0) {
			throw illegalConfig(cfg.ID, "dailyFreeTimes", cfg.dailyFreeTimes, "每日免费出征次数不能为负");
		}
		if (cfg.dailyBuyLimit < 0) {
			throw illegalConfig(cfg.ID, "dailyBuyLimit", cfg.dailyBuyLimit, "每日最大可购次数不能为负");
		}
		if (cfg.dailyFreeTimes + cfg.dailyBuyLimit <= 0) {
			throw illegalConfig(cfg.ID, "dailyFreeTimes+dailyBuyLimit",
					cfg.dailyFreeTimes + "+" + cfg.dailyBuyLimit, "每日免费次数与可购次数之和必须大于0");
		}
		if (cfg.maxSingleScore <= 0L) {
			throw illegalConfig(cfg.ID, "maxSingleScore", cfg.maxSingleScore, "单次上报最大合法战功必须大于0");
		}
		if (cfg.minParticipateScore < 0L) {
			throw illegalConfig(cfg.ID, "minParticipateScore", cfg.minParticipateScore, "最低个人战功门槛不能为负");
		}

		// 校验阶梯购买消耗数组长度覆盖 dailyBuyLimit 且消耗物合法
		if (cfg.dailyBuyLimit > 0 && (cfg.buyCosts == null || cfg.buyCosts.length < cfg.dailyBuyLimit)) {
			throw illegalConfig(cfg.ID, "buyCosts",
					cfg.buyCosts == null ? "null" : "len=" + cfg.buyCosts.length,
					"允许购买次数(dailyBuyLimit=" + cfg.dailyBuyLimit + ")时buyCosts长度必须>=dailyBuyLimit");
		}
		if (cfg.buyCosts != null) {
			for (int i = 0; i < cfg.buyCosts.length; i++) {
				validateRewardPairs(cfg.ID, "buyCosts[" + i + "]", cfg.buyCosts[i]);
			}
		}

		// 校验可争夺据点数组长度对齐、ID 去重、名称非空白及门槛/加分边界
		if (cfg.strongholdIds == null || cfg.strongholdIds.length == 0) {
			throw illegalConfig(cfg.ID, "strongholdIds", "empty", "据点ID列表不能为空");
		}
		int strongholdCount = cfg.strongholdIds.length;
		if (cfg.strongholdNames == null || cfg.strongholdNames.length != strongholdCount) {
			throw illegalConfig(cfg.ID, "strongholdNames",
					cfg.strongholdNames == null ? 0 : cfg.strongholdNames.length,
					"据点名称数量必须与strongholdIds长度(" + strongholdCount + ")一致");
		}
		if (cfg.strongholdOccupyMinScores == null || cfg.strongholdOccupyMinScores.length != strongholdCount) {
			throw illegalConfig(cfg.ID, "strongholdOccupyMinScores",
					cfg.strongholdOccupyMinScores == null ? 0 : cfg.strongholdOccupyMinScores.length,
					"据点占领门槛数量必须与strongholdIds长度(" + strongholdCount + ")一致");
		}
		if (cfg.strongholdBonusScores == null || cfg.strongholdBonusScores.length != strongholdCount) {
			throw illegalConfig(cfg.ID, "strongholdBonusScores",
					cfg.strongholdBonusScores == null ? 0 : cfg.strongholdBonusScores.length,
					"据点霸主加分数量必须与strongholdIds长度(" + strongholdCount + ")一致");
		}
		Set<Integer> seenStrongholdIds = new HashSet<>();
		for (int i = 0; i < strongholdCount; i++) {
			int strongholdId = cfg.strongholdIds[i];
			if (strongholdId <= 0) {
				throw illegalConfig(cfg.ID, "strongholdIds[" + i + "]", strongholdId, "据点ID必须大于0");
			}
			if (!seenStrongholdIds.add(strongholdId)) {
				throw illegalConfig(cfg.ID, "strongholdIds[" + i + "]", strongholdId, "据点ID存在重复配置");
			}
			String strongholdName = cfg.strongholdNames[i];
			if (strongholdName == null || strongholdName.trim().isEmpty()) {
				throw illegalConfig(cfg.ID, "strongholdNames[" + i + "]", strongholdName, "据点名称不能为空白");
			}
			if (cfg.strongholdOccupyMinScores[i] <= 0L) {
				throw illegalConfig(cfg.ID, "strongholdOccupyMinScores[" + i + "]",
						cfg.strongholdOccupyMinScores[i], "据点占领门槛必须大于0");
			}
			if (cfg.strongholdBonusScores[i] < 0L) {
				throw illegalConfig(cfg.ID, "strongholdBonusScores[" + i + "]",
						cfg.strongholdBonusScores[i], "据点霸主加分不能为负");
			}
		}

		// 校验个人里程碑与宗门里程碑严格递增且奖励档位一一对应
		validateMilestones(cfg.ID, "personalMilestoneThresholds", cfg.personalMilestoneThresholds,
				"personalMilestoneRewards", cfg.personalMilestoneRewards);
		validateMilestones(cfg.ID, "guildMilestoneThresholds", cfg.guildMilestoneThresholds,
				"guildMilestoneRewards", cfg.guildMilestoneRewards);

		// 校验宗门排行区间连续性、对应奖励及终榜结算邮件模板存在性
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
			throw illegalConfig(cfg.ID, "rankMailId", cfg.rankMailId, "终榜结算邮件ID在Mail表中不存在");
		}
	}

	/**
	 * 校验里程碑阈值严格递增且与奖励档位一一对应
	 *
	 * @param configId       配置行 ID
	 * @param thresholdField 阈值字段名
	 * @param thresholds     阈值数组
	 * @param rewardField    奖励字段名
	 * @param rewards        奖励二维数组
	 */
	private static void validateMilestones(int configId, String thresholdField, long[] thresholds,
			String rewardField, int[][] rewards) {
		if (thresholds == null || thresholds.length == 0) {
			throw illegalConfig(configId, thresholdField, "empty", "里程碑阈值数组不能为空");
		}
		if (rewards == null || thresholds.length != rewards.length) {
			throw illegalConfig(configId, rewardField, rewards == null ? 0 : rewards.length,
					"里程碑奖励档位数量必须与" + thresholdField + "长度(" + thresholds.length + ")一致");
		}
		long prevThreshold = 0L;
		for (int i = 0; i < thresholds.length; i++) {
			long threshold = thresholds[i];
			if (threshold <= 0L || threshold <= prevThreshold) {
				throw illegalConfig(configId, thresholdField + "[" + i + "]", threshold,
						"里程碑阈值必须大于0且严格递增");
			}
			prevThreshold = threshold;
			validateRewardPairs(configId, rewardField + "[" + i + "]", rewards[i]);
		}
	}

	/**
	 * 解析并校验 {@code zoneOverrides} 字段格式
	 *
	 * @param cfg 配置行对象
	 * @return 解析后的不可变映射
	 */
	private static Map<String, Integer> parseZoneOverrides(ActivityCrossGuildWarConfig cfg) {
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
				log.warn("解析跨服宗门争霸静态战区覆盖失败: id={}, segment={}", cfg.ID, segment, e);
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
	 * 校验扁平偶数长度 {@code [itemId, count, ...]} 物品对数组，并阻断 100701~100703 跨服公会资产
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
			if (isCrossGuildUnsafeAsset(itemId)) {
				throw illegalConfig(configId, fieldName, itemId,
						"禁止配置跨服公会资产(100701~100703)，防止触发GuildModule跨服RPC");
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
