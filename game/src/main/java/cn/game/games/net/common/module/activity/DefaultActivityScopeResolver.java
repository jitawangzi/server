package cn.game.games.net.common.module.activity;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.ServerContext;
import cn.game.core.base.VirtualServerRegistry.VirtualServerView;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.module.activity.ActivityBase;
import cn.game.games.net.game.module.activity.ActivityHelper;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.config.ActivityCrossGuildWarConfig;
import cn.game.protocol.generated.config.ActivityCrossWorldBossConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.manager.ActivityCrossGuildWarManager;
import cn.game.protocol.generated.manager.ActivityCrossWorldBossManager;

/**
 * 默认活动作用域解析器
 * <p>
 * 策略：
 * 1. {@code OPENTYPE_SERVER_OPEN_DAY} (10) -> {@code serverId}（校验非空）
 * 2. 战区分组跨服活动（如 {@code ActivityTypeEnum.CrossWorldBoss}）-> 按非生产沙盒、静态覆盖、区服序号分桶及默认兜底解析 {@code group_<id>}
 * 3. 其他 {@code OPENTYPE_DATE} (0) 全局活动 -> {@code "all"}
 * </p>
 *
 * @author shuyongqiang
 */
public class DefaultActivityScopeResolver implements ActivityScopeResolver {

	/** 日志器 */
	private static final Logger log = LoggerFactory.getLogger(DefaultActivityScopeResolver.class);

	/** 跨服战区作用域前缀 */
	public static final String ZONE_SCOPE_PREFIX = "group_";

	/** 默认每个战区容纳的逻辑区服数量 */
	public static final int DEFAULT_ZONE_GROUP_SIZE = 4;

	/** 自动化验收沙盒虚拟区服标识（非生产环境下固定映射到独立沙盒战区） */
	public static final String SANDBOX_SERVER_ID = "sandbox_cwb";

	/** 自动化验收沙盒隔离战区键（与线上 group_1 物理隔离） */
	public static final String SANDBOX_SCOPE_KEY = "group_9999";

	/** 跨服宗门争霸自动化验收沙盒虚拟区服标识 */
	public static final String SANDBOX_CGW_SERVER_ID = "sandbox_cgw";

	/** 跨服宗门争霸自动化验收沙盒战区编号 */
	public static final int SANDBOX_CGW_GROUP_ID = 9998;

	/** 跨服宗门争霸自动化验收沙盒隔离战区键（与 CrossWorldBoss 的 group_9999 及线上 group_1 物理隔离） */
	public static final String SANDBOX_CGW_SCOPE_KEY = "group_9998";

	/** 单例实例 */
	private static final DefaultActivityScopeResolver INSTANCE = new DefaultActivityScopeResolver();

	/**
	 * 获取默认作用域解析器单例
	 *
	 * @return 默认解析器实例
	 */
	public static DefaultActivityScopeResolver getInstance() {
		return INSTANCE;
	}

	/**
	 * 判断活动配置是否属于按战区分组的跨服活动
	 *
	 * @param config 活动配置
	 * @return 属于战区分组跨服活动返回 true，否则返回 false
	 */
	public static boolean isZoneGroupedActivity(ActivityConfig config) {
		return config != null && (config.type == ActivityTypeEnum.CrossWorldBoss.ID
				|| config.type == ActivityTypeEnum.CrossGuildWar.ID);
	}

	/**
	 * 根据活动配置和逻辑区服 ID 解析跨服作用域键
	 *
	 * @param config   活动配置，允许为 null
	 * @param serverId 逻辑区服 ID
	 * @return 作用域键（如 {@code "all"}、具体 {@code serverId} 或 {@code "group_<id>"}）
	 * @throws IllegalArgumentException 按开服天数开启的活动缺少有效 {@code serverId} 时抛出
	 */
	@Override
	public String resolveScopeKey(ActivityConfig config, String serverId) {
		if (config == null) {
			return GLOBAL_SCOPE_KEY;
		}
		if (config.openType == ActivityHelper.OPENTYPE_SERVER_OPEN_DAY) {
			if (serverId == null || serverId.trim().isEmpty()) {
				throw new IllegalArgumentException("serverId cannot be empty for OPENTYPE_SERVER_OPEN_DAY activity: " + config.ID);
			}
			return serverId.trim();
		}
		if (isZoneGroupedActivity(config)) {
			return resolveZoneScopeKey(config, serverId, null);
		}
		return GLOBAL_SCOPE_KEY;
	}

	/**
	 * 解析指定活动当前应开启的全部去重作用域集合
	 *
	 * @param config  活动配置
	 * @param servers 当前有效逻辑区服视图集合
	 * @return 去重后的有序作用域键集合
	 */
	@Override
	public Set<String> resolveActiveScopeKeys(ActivityConfig config, Collection<VirtualServerView> servers) {
		if (config == null) {
			return Collections.singleton(GLOBAL_SCOPE_KEY);
		}
		if (config.openType == ActivityHelper.OPENTYPE_SERVER_OPEN_DAY) {
			Set<String> result = new LinkedHashSet<>();
			if (servers != null) {
				for (VirtualServerView view : servers) {
					if (view == null || view.ID == null || view.ID.trim().isEmpty()) {
						continue;
					}
					if (ActivityBase.canOpenForServer(config, view.ID)) {
						result.add(resolveScopeKey(config, view.ID));
					}
				}
			}
			return result;
		}
		if (isZoneGroupedActivity(config)) {
			if (servers == null || servers.isEmpty()) {
				log.warn("战区跨服活动无有效逻辑服视图，兜底开启默认战区 group_1: configId={}", config.ID);
				return Collections.singleton(ZONE_SCOPE_PREFIX + "1");
			}
			Set<String> result = new LinkedHashSet<>();
			for (VirtualServerView view : servers) {
				if (view == null || view.ID == null || view.ID.trim().isEmpty()) {
					continue;
				}
				result.add(resolveZoneScopeKey(config, view.ID, view));
			}
			if (result.isEmpty()) {
				log.warn("战区跨服活动无有效逻辑服视图，兜底开启默认战区 group_1: configId={}", config.ID);
				return Collections.singleton(ZONE_SCOPE_PREFIX + "1");
			}
			return result;
		}
		return Collections.singleton(GLOBAL_SCOPE_KEY);
	}

	/**
	 * 按四级优先级解析战区作用域键
	 *
	 * @param config       活动配置
	 * @param serverId     逻辑区服 ID
	 * @param providedView 巡检阶段已持有的逻辑服视图（可为 null，未提供时从 ServerHelper 查找）
	 * @return 战区作用域键
	 */
	private String resolveZoneScopeKey(ActivityConfig config, String serverId, VirtualServerView providedView) {
		String trimmedServerId = serverId != null ? serverId.trim() : null;

		// 优先级 0：非生产环境下的验收沙盒区服固定映射到各自隔离战区，防止污染线上 group_1
		if (ServerContext.getInstance().getRunMode() != null
				&& !ServerContext.getInstance().getRunMode().isProduction()) {
			if (config.type == ActivityTypeEnum.CrossGuildWar.ID && SANDBOX_CGW_SERVER_ID.equals(trimmedServerId)) {
				return SANDBOX_CGW_SCOPE_KEY;
			}
			if (config.type == ActivityTypeEnum.CrossWorldBoss.ID && SANDBOX_SERVER_ID.equals(trimmedServerId)) {
				return SANDBOX_SCOPE_KEY;
			}
		}

		int groupId = resolveZoneGroupId(config, trimmedServerId, providedView);
		return ZONE_SCOPE_PREFIX + groupId;
	}

	/**
	 * 计算战区整数编号（优先级 1~3）
	 *
	 * @param config       活动配置
	 * @param serverId     已去空白的逻辑区服 ID（允许为 null）
	 * @param providedView 已持有的区服视图（允许为 null）
	 * @return 大于 0 的战区编号
	 */
	private int resolveZoneGroupId(ActivityConfig config, String serverId, VirtualServerView providedView) {
		if (config.type == ActivityTypeEnum.CrossGuildWar.ID) {
			ActivityCrossGuildWarConfig warCfg = ActivityCrossGuildWarManager.instance().getNullable(config.ID);
			if (serverId != null && !serverId.isEmpty()) {
				Map<String, Integer> overrides = CrossGuildWarConfigValidator.resolveZoneOverrides(warCfg);
				Integer overrideGroupId = overrides.get(serverId);
				if (overrideGroupId != null && overrideGroupId > 0) {
					return overrideGroupId;
				}
			}
			VirtualServerView view = providedView;
			if (view == null && serverId != null && !serverId.isEmpty()) {
				Map<String, VirtualServerView> openMap = ServerHelper.getServerOpenMap();
				view = openMap != null ? openMap.get(serverId) : null;
			}
			if (view != null && view.seq != null && view.seq > 0) {
				int groupSize = (warCfg != null && warCfg.groupSize > 0) ? warCfg.groupSize : DEFAULT_ZONE_GROUP_SIZE;
				return (view.seq - 1) / groupSize + 1;
			}
			log.warn("跨服宗门争霸战区分组缺少有效区服序号，兜底归入 group_1: configId={}, serverId={}, seq={}",
					config.ID, serverId, view == null ? null : view.seq);
			return 1;
		}

		ActivityCrossWorldBossConfig bossCfg = ActivityCrossWorldBossManager.instance().getNullable(config.ID);

		// 优先级 1：读取按版本缓存的静态覆盖映射，优先满足合服或人工指定战区分组
		if (serverId != null && !serverId.isEmpty()) {
			Map<String, Integer> overrides = CrossWorldBossConfigValidator.resolveZoneOverrides(bossCfg);
			Integer overrideGroupId = overrides.get(serverId);
			if (overrideGroupId != null && overrideGroupId > 0) {
				return overrideGroupId;
			}
		}

		// 优先级 2：按 VirtualServerView.seq 自动分桶，不改动 core/VirtualServerRegistry 与 ZK 结构
		VirtualServerView view = providedView;
		if (view == null && serverId != null && !serverId.isEmpty()) {
			Map<String, VirtualServerView> openMap = ServerHelper.getServerOpenMap();
			view = openMap != null ? openMap.get(serverId) : null;
		}
		if (view != null && view.seq != null && view.seq > 0) {
			int groupSize = (bossCfg != null && bossCfg.groupSize > 0) ? bossCfg.groupSize : DEFAULT_ZONE_GROUP_SIZE;
			return (view.seq - 1) / groupSize + 1;
		}

		// 优先级 3：缺少有效区服序号时安全兜底归入 group_1 并记录告警，保证业务不中断
		log.warn("跨服战区分组缺少有效区服序号，兜底归入 group_1: configId={}, serverId={}, seq={}",
				config.ID, serverId, view == null ? null : view.seq);
		return 1;
	}
}
