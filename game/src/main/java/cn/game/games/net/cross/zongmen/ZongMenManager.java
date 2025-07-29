package cn.game.games.net.cross.zongmen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.math.RandomUtils;
import org.redisson.api.RFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.cache.id.IdCache;
import cn.game.core.task.SchedulerService;
import cn.game.core.util.AsyncUtils;
import cn.game.games.cache.entity.ZongmenData;
import cn.game.games.net.data.mapper.ZongmenDataMapper;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.protobuf.ZongMenMsg;
import cn.game.util.DateUtil;
import cn.game.util.RedisUtil;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * @ClassName ZongMenManager
 * @description: 宗门管理
 * @author: ly
 * @create: 2025-02-05 14:45 @Version 1.0
 */
public class ZongMenManager {
	private static final ZongMenManager Instance = new ZongMenManager();
	static Logger log = LoggerFactory.getLogger(ZongMenManager.class);
	long lastCrossDayTimer;
	/**当前服务器的所有宗门*/
	private Map<Long, ZongMen> zongMenMap = new ConcurrentHashMap<>();

	private ZongMenManager() {

	}

	public static ZongMenManager getInstance() {
		return Instance;
	}

	public void init() {
		lastCrossDayTimer = System.currentTimeMillis();
		// 加载宗门数据
//        loadAllData();
		// 启动定时器 定期存储 宗门数据
		SchedulerService.getInstance()
				.scheduleAtFixedRate(() -> saveAllZongMenData(false), ZongMenConstants.SAVE_ZONG_MEN_DATA_PERIOD_TIMER, TimeUnit.SECONDS);
		// 启动定时器 定期触发宗门 时间相关事件
		SchedulerService.getInstance().scheduleAtFixedRate(timeCrossCheck(), 1, TimeUnit.SECONDS);
	}

	private Runnable timeCrossCheck() {
		return () -> {
			long now = System.currentTimeMillis();
			if (!DateUtil.isSameDay(now, lastCrossDayTimer)) {
				lastCrossDayTimer = now;
				// 跨天触发 宗门事件
				zongMenMap.values().forEach(zongMenInfo -> {
					ServerContext.getInstance().getProcessor().process(zongMenInfo.getId(), () -> {
						zongMenInfo.handleEvent(ZongMenConstants.ZongMenEvenType.CROSS_DAY);
						zongMenInfo.checkZongZhuTransfer(now);
					});
				});
			}
		};
	}

	public Future<Void> saveAllZongMenData(boolean isForce) {
		long now = System.currentTimeMillis();
		List<Future<Void>> saveFutures = new ArrayList<>();
		int saveNum = 0;
		for (ZongMen info : zongMenMap.values()) {
			if (isForce || now - info.getSaveDataTimer() >= ZongMenConstants.SAVE_ZONG_MEN_DATA_TIMER) {
				saveNum++;
				// 为每个info创建一个Future
				Promise<Void> promise = Promise.promise();
				saveFutures.add(promise.future());

				// 投递到eventloop执行
				ServerContext.getInstance().getProcessor().process(info.getId(), () -> {
					info.setSaveDataTimer(now);
					info.updateModuleData();
					Future<@Nullable Object> updateWithBLOBs = DAO.update(info.getData());
					CompletionStage<Boolean> saveZongMenTotalPowerRank = saveZongMenTotalPowerRank(info);
					RFuture<Void> saveSimpleData = saveSimpleData(info);

					// 等待三个异步操作全部完成
					List<Future<Object>> futures = AsyncUtils.toVertxFutures(updateWithBLOBs, saveZongMenTotalPowerRank, saveSimpleData);
					Future.join(futures).onComplete(ar -> {
						if (ar.succeeded()) {
							promise.complete();
						} else {
							log.error("Save info id=" + info.getId() + " failed", ar.cause());
							promise.fail(ar.cause());
						}
						log.info(String.format("update zong men data id:%d, name:%s, memberNum:%d", info.getId(), info.getName(),
								info.getModule().menMemberMap.size()));
					});
				});
			}
		}
		log.info(String.format("saveAllZongMenData use:%d, saveNum:%d, totalNum:%d", System.currentTimeMillis() - now, saveNum,
				zongMenMap.size()));

		if (saveFutures.isEmpty()) {
			return Future.succeededFuture();
		}
		// 等待所有保存操作全部完成
		return Future.join(saveFutures).mapEmpty();
	}

	@Deprecated
	public void loadAllData() {
		log.info(String.format("开始加载所有的宗门"));
		long beginTimer = System.currentTimeMillis();
		DAO.execute(ZongmenDataMapper.class, "selectByServerNodeIdIndex", ServerContext.getInstance().getServerId()).onSuccess(res -> {
			int num = 0;
			if (res != null) {
				List<ZongmenData> list = (List<ZongmenData>) res;
				list.forEach(zongmen -> {
					ZongMen info = new ZongMen(zongmen);
					zongMenMap.put(info.getId(), info);
					info.setSaveDataTimer(
							System.currentTimeMillis() + RandomUtils.nextInt((int) ZongMenConstants.SAVE_ZONG_MEN_DATA_TIMER));

				});
				num = list.size();
			}
			log.info(String.format("开始加载所有的宗门结束, use:%d, 数量:%d", System.currentTimeMillis() - beginTimer, num));
		}).onFailure(err -> {
			log.error(String.format("加载所有的宗门 出错 "));
			err.printStackTrace();
		});
	}

	/** 
	 * 加载宗门数据
	 * @param list
	 */
	public void loadZongmenList(List<ZongmenData> list) {
		list.forEach(zongmen -> {
			if (IdCache.initServerId(DistributedObjectType.ZONGMEN, zongmen.getId())) {
				ZongMen info = new ZongMen(zongmen);
				zongMenMap.put(info.getId(), info);
				info.setSaveDataTimer(System.currentTimeMillis() + RandomUtils.nextInt((int) ZongMenConstants.SAVE_ZONG_MEN_DATA_TIMER));
			}
		});
	}

	public RFuture<Void> saveSimpleData(ZongMen zongMen) {
		String redisKey = CacheType.ZONG_MEN_SIMPLE_DATA.key(zongMen.getId());
		return RedisUtil.setAsync(redisKey, zongMen.toSimpleZongMen());
	}

	public ZongMen getZongMen(long zongMenId) {
		return zongMenMap.get(zongMenId);
	}

	/**
	 * 创建宗门
	 * @param req 创建宗门的一些参数
	 * @param createPlayerId 门主pid
	 * @return 新的宗门
	 */
	public Future<ZongMen> createZongMen(ZongMenMsg.createZongMenRequest_40000005 req, long createPlayerId) {
		long newZongMenId = ZongMenHelper.createZongMenId();

		boolean trySetName = ZongMenHelper.trySetName(req.getName(), createPlayerId); 
		if (!trySetName) {
			return Future.failedFuture("宗门名称已存在，请重新输入名称");
		}
		// 创建宗门
		ZongMen zongMenInfo = new ZongMen();
		// 宗门初始化
		zongMenInfo.init(req,newZongMenId, createPlayerId);
		zongMenInfo.updateModuleData();
		Promise<ZongMen> promise = Promise.promise();
		DAO.insert(zongMenInfo.getData()).onSuccess(res -> {
			if (res != null) {
				// 保存 simple data
				saveSimpleData(zongMenInfo);
				// 存储 redis name--id map
//				saveRedisNameIdMap(zongMenInfo.getName(), zongMenInfo.getId());
				// 保存宗门战斗力排行榜
				saveZongMenTotalPowerRank(zongMenInfo);
				zongMenMap.put(zongMenInfo.getId(), zongMenInfo);
				// 宗门所在服务器
				saveZongMenServerId(zongMenInfo.getId());
				promise.complete(zongMenInfo);
			} else {
				promise.complete(null);
			}
		}).onFailure(err -> {
			err.printStackTrace();
			promise.complete(null);
		});
		return promise.future();
	}

	/** 
	 * 获取所有宗门id
	 * @return
	 */
	public Collection<Long> getAllZongMenIds() {
		return zongMenMap.keySet();
	}

	CompletionStage<Boolean> saveZongMenTotalPowerRank(ZongMen zongMenInfo) {
		return RankService.getInstance()
				.setScoreAsync(zongMenInfo.getData().getServerId(), RankType.ZongMen, zongMenInfo.getId(), zongMenInfo.callTotalPower());
	}

	private RFuture<Void> saveZongMenServerId(long id) {
		return IdCache.setServerId(DistributedObjectType.ZONGMEN, id);
	}

	public void clearZongMenServerId(long id) {
		RedisUtil.deleteAsync(CacheType.ZONG_MEN_SERVER_ID.key(id));
	}

	public void delZongMen(long id) {
		zongMenMap.remove(id);
	}
}
