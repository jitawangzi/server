package cn.game.games.net.cross.guild;

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
import cn.game.games.cache.entity.GuildData;
import cn.game.games.net.data.mapper.GuildDataMapper;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.protobuf.GuildMsg;
import cn.game.util.DateUtil;
import cn.game.util.RedisUtil;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * @ClassName GuildManager
 * @description: 公会管理
 * @author: ly
 * @create: 2025-02-05 14:45 @Version 1.0
 */
public class GuildManager {
	private static final GuildManager Instance = new GuildManager();
	static Logger log = LoggerFactory.getLogger(GuildManager.class);
	long lastCrossDayTimer;
	/**当前服务器的所有公会*/
	private Map<Long, Guild> guildMap = new ConcurrentHashMap<>();

	private GuildManager() {

	}

	public static GuildManager getInstance() {
		return Instance;
	}

	public void init() {
		lastCrossDayTimer = System.currentTimeMillis();
		// 加载公会数据
//        loadAllData();
		// 启动定时器 定期存储 公会数据
		SchedulerService.getInstance()
				.scheduleAtFixedRate(() -> saveAllGuildData(false), GuildConstants.SAVE_ZONG_MEN_DATA_PERIOD_TIMER, TimeUnit.SECONDS);
		// 启动定时器 定期触发公会 时间相关事件
		SchedulerService.getInstance().scheduleAtFixedRate(timeCrossCheck(), 1, TimeUnit.SECONDS);
	}

	private Runnable timeCrossCheck() {
		return () -> {
			long now = System.currentTimeMillis();
			if (!DateUtil.isSameDay(now, lastCrossDayTimer)) {
				lastCrossDayTimer = now;
				// 跨天触发 公会事件
				guildMap.values().forEach(guildInfo -> {
					ServerContext.getInstance().getProcessor().process(guildInfo.getId(), () -> {
						guildInfo.handleEvent(GuildConstants.GuildEvenType.CROSS_DAY);
						guildInfo.checkZongZhuTransfer(now);
					});
				});
			}
		};
	}

	public Future<Void> saveAllGuildData(boolean isForce) {
		long now = System.currentTimeMillis();
		List<Future<Void>> saveFutures = new ArrayList<>();
		int saveNum = 0;
		for (Guild info : guildMap.values()) {
			if (isForce || now - info.getSaveDataTimer() >= GuildConstants.SAVE_ZONG_MEN_DATA_TIMER) {
				saveNum++;
				// 为每个info创建一个Future
				Promise<Void> promise = Promise.promise();
				saveFutures.add(promise.future());

				// 投递到虚拟线程执行
				ServerContext.getInstance().getProcessor().process(info.getId(), () -> {
					info.setSaveDataTimer(now);
					info.updateModuleData();
					Future<@Nullable Object> updateWithBLOBs = DAO.update(info.getData());
					CompletionStage<Boolean> saveGuildTotalPowerRank = saveGuildTotalPowerRank(info);
					RFuture<Void> saveSimpleData = saveSimpleData(info);

					// 等待三个异步操作全部完成
					List<Future<Object>> futures = AsyncUtils.toVertxFutures(updateWithBLOBs, saveGuildTotalPowerRank, saveSimpleData);
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
		log.info(String.format("saveAllGuildData use:%d, saveNum:%d, totalNum:%d", System.currentTimeMillis() - now, saveNum,
				guildMap.size()));

		if (saveFutures.isEmpty()) {
			return Future.succeededFuture();
		}
		// 等待所有保存操作全部完成
		return Future.join(saveFutures).mapEmpty();
	}

	@Deprecated
	public void loadAllData() {
		log.info(String.format("开始加载所有的公会"));
		long beginTimer = System.currentTimeMillis();
		DAO.execute(GuildDataMapper.class, "selectByServerNodeIdIndex", ServerContext.getInstance().getServerId()).onSuccess(res -> {
			int num = 0;
			if (res != null) {
				List<GuildData> list = (List<GuildData>) res;
				list.forEach(guild -> {
					Guild info = new Guild(guild);
					guildMap.put(info.getId(), info);
					info.setSaveDataTimer(
							System.currentTimeMillis() + RandomUtils.nextInt((int) GuildConstants.SAVE_ZONG_MEN_DATA_TIMER));

				});
				num = list.size();
			}
			log.info(String.format("开始加载所有的公会结束, use:%d, 数量:%d", System.currentTimeMillis() - beginTimer, num));
		}).onFailure(err -> {
			log.error(String.format("加载所有的公会 出错 "));
			err.printStackTrace();
		});
	}

	/** 
	 * 加载公会数据
	 * @param list
	 */
	public void loadGuildList(List<GuildData> list) {
		list.forEach(guild -> {
			if (IdCache.initServerId(DistributedObjectType.GUILD, guild.getId())) {
				Guild info = new Guild(guild);
				guildMap.put(info.getId(), info);
				info.setSaveDataTimer(System.currentTimeMillis() + RandomUtils.nextInt((int) GuildConstants.SAVE_ZONG_MEN_DATA_TIMER));
			}
		});
	}

	public RFuture<Void> saveSimpleData(Guild guild) {
		String redisKey = CacheType.ZONG_MEN_SIMPLE_DATA.key(guild.getId());
		return RedisUtil.setAsync(redisKey, guild.toSimpleGuild());
	}

	public Guild getGuild(long guildId) {
		return guildMap.get(guildId);
	}

	/**
	 * 创建公会
	 * @param req 创建公会的一些参数
	 * @param createPlayerId 门主pid
	 * @return 新的公会
	 */
	public Future<Guild> createGuild(long createPlayerId,String name,String notice,String declaration,int icon,int joinType) {
		Promise<Guild> promise = Promise.promise();
		try {
			long newGuildId = GuildHelper.createGuildId();

			boolean trySetName = GuildHelper.trySetName(name, newGuildId); 
			if (!trySetName) {
				return Future.failedFuture("公会名称已存在，请重新输入名称");
			}
			// 创建公会
			Guild guildInfo = new Guild();
			// 公会初始化
			guildInfo.init(name,notice,declaration,icon,joinType, newGuildId, createPlayerId);
			guildInfo.updateModuleData();
			DAO.insert(guildInfo.getData()).onSuccess(res -> {
				if (res != null) {
					// 保存 simple data
					saveSimpleData(guildInfo);
					// 存储 redis name--id map
//					saveRedisNameIdMap(guildInfo.getName(), guildInfo.getId());
					// 保存公会战斗力排行榜
					saveGuildTotalPowerRank(guildInfo);
					guildMap.put(guildInfo.getId(), guildInfo);
					// 公会所在服务器
					saveGuildServerId(guildInfo.getId());
					promise.complete(guildInfo);
				} else {
					promise.complete(null);
				}
			}).onFailure(err -> {
				promise.fail(err);
			});
		} catch (Exception e) {
			log.error("",e);
			promise.fail(e);
		}
		return promise.future();
	}

	/** 
	 * 获取所有公会id
	 * @return
	 */
	public Collection<Long> getAllGuildIds() {
		return guildMap.keySet();
	}

	CompletionStage<Boolean> saveGuildTotalPowerRank(Guild guildInfo) {
		return RankService.getInstance()
				.setScoreAsync(guildInfo.getData().getServerId(), RankType.Guild, guildInfo.getId(), guildInfo.callTotalPower());
	}

	private CompletionStage<Boolean> saveGuildServerId(long id) {
		return IdCache.trySetServerIdAsync(DistributedObjectType.GUILD, id);
	}

	public void clearGuildServerId(long id) {
		RedisUtil.deleteAsync(CacheType.ZONG_MEN_SERVER_ID.key(id));
	}

	public void delGuild(long id) {
		guildMap.remove(id);
	}

	public Collection<Guild> getAllGuild() {
		return guildMap.values();
	}
}
