package cn.game.games.net.cross.activity.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.cache.id.IdCache;
import cn.game.core.event.EventHandler;
import cn.game.core.event.ServerEventTypeEnum;
import cn.game.core.id.IdUtil;
import cn.game.core.task.SchedulerService;
import cn.game.games.cache.entity.GameActivity;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.core.event.server.ServerEvent;
import cn.game.games.core.event.server.ServerEventBus;
import cn.game.games.net.common.module.activity.ActivityScopeResolver;
import cn.game.games.net.common.module.activity.DefaultActivityScopeResolver;
import cn.game.games.net.cross.activity.CrossActivityBase;
import cn.game.games.net.cross.activity.CrossActivityDirectory;
import cn.game.games.net.cross.activity.CrossActivityStore;
import cn.game.games.net.cross.activity.CrossGlobalActivityManager;
import cn.game.games.net.cross.activity.DefaultCrossActivityStore;
import cn.game.games.net.cross.activity.RedisCrossActivityDirectory;
import cn.game.games.net.cross.activity.dto.CrossActivityActionResult;
import cn.game.games.net.cross.activity.dto.CrossActivityCreateResult;
import cn.game.games.net.cross.activity.dto.CrossActivitySnapshot;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.manager.ActivityStateManager;
import cn.game.games.net.game.module.activity.ActivityBase;
import cn.game.games.net.game.module.activity.ActivityFactory;
import cn.game.games.net.game.module.activity.ActivityHelper;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.protobuf.ActivityMsg.ActivityState;
import cn.game.util.DateUtil;
import cn.game.util.JsonUtil;
import cn.game.util.LockUtil;
import cn.game.util.RedisUtil;

/**
 * 跨服活动服务核心实现
 * 涵盖：
 * 1. 同 activityUid 邮箱串行执行统一调度、防重入死锁与销毁内存回收
 * 2. Leader 节点防重创建协议、唯一索引旧行清理与 Redis 路由读写
 * 3. 存储 SPI 隔离、定时脏写与停服 saveAll(true)
 * 4. 故障重载、冷启动自愈与原子占坑防裂脑
 * 5. 全事件监听响应（NewDay/ActivityOpenTime/ActivityShutDownTime/ActivityDestoryTime）
 * 6. Game 侧零副作用只读检查与玩家 RPC 钩子分发
 * 
 * @author shuyongqiang
 */
public class CrossActivityService implements EventHandler<ServerEventTypeEnum, ServerEvent> {

	private static final Logger log = LoggerFactory.getLogger(CrossActivityService.class);

	private static final CrossActivityService INSTANCE = new CrossActivityService();

	public static final long CREATE_LOCK_WAIT_SECONDS = 3L;
	public static final long CREATE_LOCK_LEASE_SECONDS = 15L;

	private final CrossGlobalActivityManager manager = CrossGlobalActivityManager.getInstance();
	private final CrossActivityDirectory directory = RedisCrossActivityDirectory.getInstance();
	private final ActivityScopeResolver scopeResolver = DefaultActivityScopeResolver.getInstance();
	private volatile CrossActivityStore store = new DefaultCrossActivityStore();

	private volatile boolean initialized = false;

	/** 邮箱线程上下文标识，防同邮箱重入死锁 */
	private static final ThreadLocal<Long> CURRENT_MAILBOX_UID = new ThreadLocal<>();
	private final ConcurrentHashMap<Long, ReentrantLock> mailboxLocks = new ConcurrentHashMap<>();

	/** 本地测试无 Redis 互斥锁兜底 */
	private final ConcurrentHashMap<String, ReentrantLock> localCreateLocks = new ConcurrentHashMap<>();

	private final ServerEventTypeEnum[] eventTypes = new ServerEventTypeEnum[] {
			ServerEventTypeEnum.NewDay,
			ServerEventTypeEnum.ActivityOpenTime,
			ServerEventTypeEnum.ActivityShutDownTime,
			ServerEventTypeEnum.ActivityDestoryTime
	};

	public static CrossActivityService getInstance() {
		return INSTANCE;
	}

	public void setStore(CrossActivityStore store) {
		this.store = (store != null ? store : new DefaultCrossActivityStore());
	}

	public CrossActivityStore getStore() {
		return store;
	}

	public synchronized void init() {
		if (initialized) {
			return;
		}
		initialized = true;
		SchedulerService.getInstance().scheduleAtFixedRate(this::periodicDirtySave, 5, 5, TimeUnit.SECONDS);

		// DC-16: Cross 独立零点日切保底调度
		scheduleCrossMidnightCheck();

		if (ServerEventBus.getInstance() != null) {
			ServerEventBus.getInstance().register(this);
		}
	}

	private void scheduleCrossMidnightCheck() {
		try {
			LocalDateTime now = DateUtil.nowDateTime();
			LocalDateTime tomorrowMidnight = now.toLocalDate().plusDays(1).atStartOfDay();
			long initialDelayMillis = Math.max(1000L, Duration.between(now, tomorrowMidnight).toMillis() + 200L);
			SchedulerService.getInstance().scheduleAtFixedRate(this::onNewDay, initialDelayMillis,
					24 * 3600 * 1000L, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			log.error("Failed to schedule Cross midnight check", e);
		}
	}

	@Override
	public ServerEventTypeEnum[] getEventTypes() {
		return eventTypes;
	}

	@Override
	public void handleEvent(ServerEvent event) {
		if (event == null || event.getType() == null) {
			return;
		}
		switch (event.getType()) {
		case NewDay:
			onNewDay();
			break;
		case ActivityOpenTime: {
			int configId = event.getIntParameter(0);
			onActivityOpenEvent(configId);
			break;
		}
		case ActivityShutDownTime: {
			int configId = event.getIntParameter(0);
			onActivityShutDownEvent(configId);
			break;
		}
		case ActivityDestoryTime: {
			int configId = event.getIntParameter(0);
			onActivityDestroyEvent(configId);
			break;
		}
		default:
			break;
		}
	}

	private void onActivityOpenEvent(int configId) {
		if (!ServerContext.getInstance().isLeader()) {
			return;
		}
		ActivityConfig config = ActivityManager.instance().getNullable(configId);
		if (config != null && config.isCross && config.isMultiplayer) {
			onLeaderScheduleCheckForConfig(config);
		}
	}

	private void onActivityShutDownEvent(int configId) {
		for (CrossActivityBase act : manager.getAllActivities()) {
			if (act.getId() == configId && act.getState() == ActivityState.START_VALUE) {
				runInActivityMailbox(act.getUid(), () -> {
					act.end();
					return null;
				});
			}
		}
	}

	private void onActivityDestroyEvent(int configId) {
		for (CrossActivityBase act : manager.getAllActivities()) {
			if (act.getId() == configId) {
				destroyActivity(act.getUid(), configId, act.getScopeKey());
			}
		}
	}

	/**
	 * Game 侧只读查询：当前服务器是否托管该活动 UID（零副作用保证）
	 */
	public boolean containsActivity(long activityUid) {
		if (!initialized) {
			return false;
		}
		return manager.containsActivity(activityUid);
	}

	/**
	 * Game 侧只读查询：当前服务器托管的所有活动 UID（零副作用保证）
	 */
	public List<Long> getAllActivityIds() {
		if (!initialized) {
			return Collections.emptyList();
		}
		return manager.getAllActivityIds();
	}

	/**
	 * 统一邮箱串行调度入口（防重入死锁）
	 */
	public <T> T runInActivityMailbox(long activityUid, Callable<T> task) {
		if (activityUid <= 0) {
			try {
				return task.call();
			} catch (Exception e) {
				if (e instanceof RuntimeException re) {
					throw re;
				}
				throw new RuntimeException(e);
			}
		}
		if (Objects.equals(CURRENT_MAILBOX_UID.get(), activityUid)) {
			try {
				return task.call();
			} catch (Exception e) {
				if (e instanceof RuntimeException re) {
					throw re;
				}
				throw new RuntimeException(e);
			}
		}
		ReentrantLock lock = mailboxLocks.computeIfAbsent(activityUid, k -> new ReentrantLock(true));
		lock.lock();
		Long prev = CURRENT_MAILBOX_UID.get();
		CURRENT_MAILBOX_UID.set(activityUid);
		try {
			return task.call();
		} catch (Exception e) {
			if (e instanceof RuntimeException re) {
				throw re;
			}
			throw new RuntimeException(e);
		} finally {
			if (prev != null) {
				CURRENT_MAILBOX_UID.set(prev);
			} else {
				CURRENT_MAILBOX_UID.remove();
			}
			lock.unlock();
		}
	}

	public void markDirty(long activityUid) {
		CrossActivityBase act = manager.getActivity(activityUid);
		if (act != null) {
			act.markDirty();
		}
	}

	/**
	 * Leader 选主监听触发活动周期巡检与创建
	 */
	public void onLeaderScheduleCheck() {
		if (!ServerContext.getInstance().isLeader()) {
			return;
		}
		for (ActivityConfig config : ActivityManager.instance().list()) {
			if (!config.isCross || !config.isMultiplayer) {
				continue;
			}
			if (!ClassManager.getInstance().hasServerActivityClass(config.type)) {
				continue;
			}
			onLeaderScheduleCheckForConfig(config);
		}
	}

	private void onLeaderScheduleCheckForConfig(ActivityConfig config) {
		if (config.openType == ActivityHelper.OPENTYPE_SERVER_OPEN_DAY) {
			// 开服天数活动：按有效逻辑区服分别计算 scopeKey 与开启检查，严禁误用 Cross 物理节点 ID
			String[] serverIds = ServerHelper.getServerIds();
			if (serverIds != null && serverIds.length > 0) {
				for (String logicServerId : serverIds) {
					if (logicServerId == null || logicServerId.isEmpty()) {
						continue;
					}
					if (!ActivityBase.canOpenForServer(config, logicServerId)) {
						continue;
					}
					String scopeKey = scopeResolver.resolveScopeKey(config, logicServerId);
					ensureActivityLeader(config, scopeKey, logicServerId);
				}
			}
		} else {
			// 普通全局跨服活动
			if (!manager.canOpen(config)) {
				return;
			}
			String scopeKey = scopeResolver.resolveScopeKey(config, null);
			ensureActivityLeader(config, scopeKey, null);
		}
	}

	/**
	 * 计算活动的真实销毁时间（支持开服天数模式）
	 */
	private long calcActivityDestroyTime(ActivityConfig config, String serverId) {
		if (config == null) {
			return 0L;
		}
		if (config.openType == ActivityHelper.OPENTYPE_SERVER_OPEN_DAY) {
			if (serverId != null && !serverId.isEmpty()) {
				return ActivityBase.calcServerOpenDayDestroyTime(config, serverId);
			}
			return 0L;
		}
		return ActivityStateManager.getInstance().getDestroyTime(config.ID);
	}

	/**
	 * Leader 6 步防重创建协议
	 */
	public void ensureActivityLeader(ActivityConfig config, String scopeKey, String serverId) {
		// 步骤 2: 快速预检
		if (directory.getActiveUid(config.ID, scopeKey).isPresent()) {
			return;
		}

		// 步骤 3: 分布式锁
		String lockKey = CacheType.CROSS_ACTIVITY_CREATE_LOCK.key(config.ID, scopeKey);
		RLock redisLock = null;
		ReentrantLock localLock = null;
		boolean locked = false;

		if (RedisUtil.getRedis() != null) {
			redisLock = LockUtil.tryLockSync(CREATE_LOCK_WAIT_SECONDS, CREATE_LOCK_LEASE_SECONDS, TimeUnit.SECONDS, lockKey);
			locked = (redisLock != null);
		} else {
			localLock = localCreateLocks.computeIfAbsent(lockKey, k -> new ReentrantLock());
			try {
				locked = localLock.tryLock(CREATE_LOCK_WAIT_SECONDS, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				locked = false;
			}
		}

		if (!locked) {
			log.warn("Failed to acquire create lock for activity configId={}, scopeKey={}", config.ID, scopeKey);
			return;
		}

		try {
			// 步骤 4: 锁内双检与残留死行清理
			Optional<Long> activeUidOpt = directory.getActiveUid(config.ID, scopeKey);
			if (activeUidOpt.isPresent()) {
				long activeUid = activeUidOpt.get();
				var idManager = IdCache.getManager(DistributedObjectType.CROSS_ACTIVITY);
				String routeServerId = idManager != null ? idManager.getServerId(activeUid) : null;
				if (routeServerId != null && !routeServerId.isEmpty()) {
					return;
				}
			}

			long targetUid = 0L;
			List<GameActivity> oldEntities = store.selectListByScopeAndConfig(scopeKey, config.ID);
			if (oldEntities != null && !oldEntities.isEmpty()) {
				for (GameActivity oldEntity : oldEntities) {
					long now = DateUtil.currentTimeMillis();
					long destroyTime = calcActivityDestroyTime(config, serverId != null ? serverId : scopeKey);
					boolean isActive = oldEntity.getState() != ActivityState.NONE_VALUE
							&& (destroyTime <= 0 || now < destroyTime);

					if (isActive && targetUid == 0L) {
						// 存在活跃旧实体，复用其 UID，后续由目标节点加载挂载并写入真实路由
						targetUid = oldEntity.getId();
					} else {
						// 残留僵尸行清理：清理目录、Redis 路由与存储
						long oldUid = oldEntity.getId();
						directory.removeIfMatch(config.ID, scopeKey, oldUid);
						cleanupRouteAndCache(oldUid);
						manager.removeActivity(oldUid);
						store.delete(oldUid);
					}
				}
			}

			// 步骤 5: 负载均衡分派（若有活跃旧实体则复用其 UID 由目标节点挂载，否则生成新 UID）
			if (targetUid == 0L) {
				try {
					targetUid = IdUtil.getId();
				} catch (Exception e) {
					log.error("Failed to generate uid for cross activity configId={}, scopeKey={}", config.ID, scopeKey, e);
					throw new IllegalStateException("Failed to generate cross activity UID", e);
				}
			}
			CrossActivityServiceInterface proxy = ServerHelper.getCrossActivityBalanceProxy();
			CrossActivityCreateResult res = proxy.createOrEnsureActivity(targetUid, config.ID, scopeKey, serverId);
			if (res != null && !res.isSuccess()) {
				log.error("Failed to create activity remotely: configId={}, error={}", config.ID, res.getErrorMsg());
			}
		} finally {
			if (redisLock != null && redisLock.isHeldByCurrentThread()) {
				redisLock.unlock();
			}
			if (localLock != null && localLock.isHeldByCurrentThread()) {
				localLock.unlock();
			}
		}
	}

	/**
	 * 目标节点创建或恢复跨服活动实例
	 */
	public CrossActivityCreateResult createOrEnsureActivity(long suggestedUid, int configId, String scopeKey, String serverId) {
		return runInActivityMailbox(suggestedUid, () -> {
			CrossActivityBase existing = manager.getActivity(configId, scopeKey);
			if (existing != null) {
				return CrossActivityCreateResult.success(existing.getUid(), configId, scopeKey, ServerContext.getInstance().getServerId());
			}

			ActivityConfig config = ActivityManager.instance().getNullable(configId);
			if (config == null) {
				return CrossActivityCreateResult.fail(configId, scopeKey, "Config not found: " + configId);
			}

			CrossActivityBase act = null;
			long actualUid = suggestedUid;
			long now = DateUtil.currentTimeMillis();

			// 检查 DB 中是否已有持久化实体
			List<GameActivity> oldEntities = store.selectListByScopeAndConfig(scopeKey, configId);
			if (oldEntities != null) {
				for (GameActivity old : oldEntities) {
					long destroyTime = calcActivityDestroyTime(config, old.getServerId() != null ? old.getServerId() : scopeKey);
					boolean isActive = old.getState() != ActivityState.NONE_VALUE
							&& (destroyTime <= 0 || now < destroyTime);
					if (isActive && act == null) {
						ActivityBase baseAct;
						if (StringUtils.isNotEmpty(old.getParams())) {
							baseAct = JsonUtil.parseObjectWithType(old.getParams());
						} else {
							baseAct = ActivityFactory.createServerActivityBase(config.type);
						}
						if (baseAct instanceof CrossActivityBase cab) {
							act = cab;
						} else {
							act = new CrossActivityBase();
							act.init(config.ID, null, false);
						}
						actualUid = old.getId();
						act.setUid(actualUid);
						act.setServerId(serverId != null ? serverId : scopeKey);
						act.setScopeKey(scopeKey);
						act.afterLoadFromDb();
					} else {
						store.delete(old.getId());
					}
				}
			}

			if (act == null) {
				ActivityBase baseAct = ActivityFactory.createServerActivityBase(config.type);
				if (baseAct instanceof CrossActivityBase cab) {
					act = cab;
				} else {
					act = new CrossActivityBase();
				}
				act.setUid(actualUid);
				act.setServerId(serverId != null ? serverId : scopeKey);
				act.setScopeKey(scopeKey);
				act.init(configId, null, true);

				saveActivityToDb(act);
			}

			// BLOCKER 1: 写入真实 Redis 路由并更新本地 IdCache
			IdCache.initServerId(DistributedObjectType.CROSS_ACTIVITY, actualUid);
			directory.putActive(configId, scopeKey, actualUid);
			manager.addActivity(act);

			manager.scheduleEndTask(act, act.getEndTime() - now);
			manager.scheduleDestroyTask(act, act.getDestroyTime() - now);

			return CrossActivityCreateResult.success(actualUid, configId, scopeKey, ServerContext.getInstance().getServerId());
		});
	}

	public CrossActivitySnapshot queryActivitySnapshot(long activityUid, long playerId) {
		return runInActivityMailbox(activityUid, () -> {
			CrossActivityBase act = manager.getActivity(activityUid);
			if (act == null) {
				return null;
			}
			CrossActivitySnapshot snapshot = new CrossActivitySnapshot();
			snapshot.setActivityUid(act.getUid());
			snapshot.setConfigId(act.getId());
			snapshot.setScopeKey(act.getScopeKey());
			snapshot.setState(act.getState());
			snapshot.setStartTime(act.getStartTime());
			snapshot.setEndTime(act.getEndTime());
			snapshot.setDestroyTime(act.getDestroyTime());
			snapshot.setServerId(ServerContext.getInstance().getServerId());
			return snapshot;
		});
	}

	public CrossActivityActionResult executePlayerAction(long activityUid, long playerId, String actionType, String payloadJson) {
		return runInActivityMailbox(activityUid, () -> {
			CrossActivityBase act = manager.getActivity(activityUid);
			if (act == null) {
				return CrossActivityActionResult.fail(cn.game.protocol.manual.ErrorMsgEnum.activity_not_found.ID, "Activity not found");
			}
			return act.executePlayerAction(playerId, actionType, payloadJson);
		});
	}

	public CrossActivitySnapshot joinActivity(long activityUid, long playerId, String serverId, Map<String, Object> extraData) {
		return runInActivityMailbox(activityUid, () -> {
			CrossActivityBase act = manager.getActivity(activityUid);
			if (act == null) {
				return null;
			}
			boolean joined = act.join(playerId, serverId, extraData);
			if (!joined) {
				log.warn("Player {} failed to join cross activity uid={}", playerId, activityUid);
			}
			return queryActivitySnapshot(activityUid, playerId);
		});
	}

	public void periodicDirtySave() {
		for (CrossActivityBase act : manager.getAllActivities()) {
			if (act.isDirty()) {
				try {
					runInActivityMailbox(act.getUid(), () -> {
						if (act.isDirty()) {
							saveActivityToDb(act);
							act.clearDirty();
							act.setLastSaveTime(DateUtil.currentTimeMillis());
						}
						return null;
					});
				} catch (Exception e) {
					log.error("Failed periodicDirtySave for activity uid={}", act.getUid(), e);
				}
			}
		}
	}

	public void saveAll(boolean force) {
		for (CrossActivityBase act : manager.getAllActivities()) {
			try {
				runInActivityMailbox(act.getUid(), () -> {
					if (force || act.isDirty()) {
						saveActivityToDb(act);
						act.clearDirty();
						act.setLastSaveTime(DateUtil.currentTimeMillis());
					}
					return null;
				});
			} catch (Exception e) {
				log.error("Failed saveAll for activity uid={}", act.getUid(), e);
			}
		}
	}

	public void loadActivitiesFromDb(List<GameActivity> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		long now = DateUtil.currentTimeMillis();
		for (GameActivity entity : list) {
			if (entity == null) {
				continue;
			}
			long uid = entity.getId();
			try {
				runInActivityMailbox(uid, () -> {
					ActivityConfig config = ActivityManager.instance().getNullable(entity.getConfigId());
					if (config == null || !config.isCross) {
						destroyActivity(uid, entity.getConfigId(), entity.getServerId());
						return null;
					}
					long destroyTime = calcActivityDestroyTime(config, entity.getServerId());
					if (entity.getState() == ActivityState.NONE_VALUE || (destroyTime > 0 && now >= destroyTime)) {
						destroyActivity(uid, config.ID, entity.getServerId());
						return null;
					}

					// BLOCKER 1: 原子占坑防裂脑，若被其他节点占坑则放弃加载
					boolean claimed = IdCache.initServerId(DistributedObjectType.CROSS_ACTIVITY, uid);
					if (!claimed) {
						log.warn("Cross activity uid={} already claimed by another server node, skip loading", uid);
						return null;
					}

					ActivityBase baseAct;
					if (StringUtils.isNotEmpty(entity.getParams())) {
						baseAct = JsonUtil.parseObjectWithType(entity.getParams());
					} else {
						baseAct = ActivityFactory.createServerActivityBase(config.type);
					}
					CrossActivityBase act;
					if (baseAct instanceof CrossActivityBase cab) {
						act = cab;
					} else {
						act = new CrossActivityBase();
						act.init(config.ID, null, false);
					}
					act.setUid(uid);
					act.setServerId(entity.getServerId());
					act.setScopeKey(entity.getServerId());
					act.afterLoadFromDb();
					manager.addActivity(act);

					directory.putActive(config.ID, act.getScopeKey(), uid);

					// BLOCKER 6: 恢复时停机已过 endTime 但未到 destroyTime，补调 end() 推进终态
					if (entity.getState() == ActivityState.START_VALUE && now >= act.getEndTime()) {
						act.end();
					} else {
						manager.scheduleEndTask(act, act.getEndTime() - now);
					}
					manager.scheduleDestroyTask(act, act.getDestroyTime() - now);
					return null;
				});
			} catch (Exception e) {
				log.error("Failed to recover activity uid={}", uid, e);
			}
		}
	}

	public void destroyActivity(long activityUid, int configId, String scopeKey) {
		try {
			runInActivityMailbox(activityUid, () -> {
				CrossActivityBase act = manager.removeActivity(activityUid);
				if (act != null) {
					// MAJOR 6: 显式触发子类销毁钩子
					act.destroy();
				}
				int cid = act != null ? act.getId() : configId;
				String scope = act != null ? act.getScopeKey() : scopeKey;
				if (cid > 0 && scope != null) {
					directory.removeIfMatch(cid, scope, activityUid);
				}
				cleanupRouteAndCache(activityUid);
				store.delete(activityUid);
				return null;
			});
		} finally {
			// BLOCKER 6: 彻底从 mailboxLocks 移除该 UID 锁，消除内存泄漏
			mailboxLocks.remove(activityUid);
		}
	}

	public void destroyActivity(long activityUid) {
		destroyActivity(activityUid, 0, null);
	}

	private void cleanupRouteAndCache(long activityUid) {
		var idManager = IdCache.getManager(DistributedObjectType.CROSS_ACTIVITY);
		if (idManager != null) {
			idManager.clearServerId(activityUid);
			idManager.invalidateServerId(activityUid);
		}
		// BLOCKER 1: 显式从 Redis 清除路由键
		if (RedisUtil.getRedis() != null) {
			try {
				RedisUtil.delete(IdCache.generateRedisKey(DistributedObjectType.CROSS_ACTIVITY, activityUid));
			} catch (Exception e) {
				log.error("Failed to delete Redis route key for uid={}", activityUid, e);
			}
		}
	}

	public void onNewDay() {
		// 1. 推进当前活跃活动的跨天
		for (CrossActivityBase act : manager.getAllActivities()) {
			try {
				runInActivityMailbox(act.getUid(), () -> {
					act.newDay();
					return null;
				});
			} catch (Exception e) {
				log.error("Failed onNewDay for activity uid={}", act.getUid(), e);
			}
		}

		// 2. Leader 节点做周期巡检（开服天数跨天新开启与过期清理）
		if (ServerContext.getInstance().isLeader()) {
			try {
				onLeaderScheduleCheck();
			} catch (Exception e) {
				log.error("Failed onLeaderScheduleCheck onNewDay", e);
			}
		}
	}

	private void saveActivityToDb(CrossActivityBase act) {
		GameActivity entity = new GameActivity();
		entity.setId(act.getUid());
		entity.setServerId(act.getScopeKey());
		entity.setConfigId(act.getId());
		entity.setState((byte) act.getState());
		entity.setCreateTime(act.getStartTime() > 0 ? act.getStartTime() : DateUtil.currentTimeMillis());
		entity.setParams(act.toSaveString());
		store.save(entity);
	}
}
