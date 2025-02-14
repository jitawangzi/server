package cn.game.games.net.cross.zongmen;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.task.SchedulerService;
import cn.game.games.cache.entity.Zongmen;
import cn.game.games.cache.id.IdCache;
import cn.game.games.net.data.mapper.ZongmenMapper;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.enume.RankType;
import cn.game.util.DateUtil;
import cn.game.util.RedisUtil;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * @ClassName ZongMenManager
 * @description: 宗门管理
 * @author: ly
 * @create: 2025-02-05 14:45 @Version 1.0
 */
public class ZongMenManager {
    private static final ZongMenManager  Instance = new ZongMenManager();
     static Logger log	= LoggerFactory.getLogger(ZongMenManager.class);
    long lastCrossDayTimer;
    /**当前服务器的所有宗门*/
    private Map<Long,ZongMenInfo> zongMenInfoMap = new ConcurrentHashMap<>();
    private ZongMenManager() {

    }
    AtomicInteger zongMenAutoIncrementNum = new AtomicInteger(0);
    public static ZongMenManager getInstance(){ return Instance;}

    public void init() {
        lastCrossDayTimer = System.currentTimeMillis();
        //加载宗门数据
        loadAllData();
        //启动定时器 定期存储 宗门数据
        SchedulerService.getInstance().scheduleAtFixedRate(saveAllZongMenData(),ZongMenConstants.SAVE_ZONG_MEN_DATA_PERIOD_TIMER, TimeUnit.SECONDS);
        //启动定时器 定期触发宗门 时间相关事件
        SchedulerService.getInstance().scheduleAtFixedRate(timeCrossCheck(),1, TimeUnit.SECONDS);
    }

    private Runnable timeCrossCheck() {
        return ()->{
            long now = System.currentTimeMillis();
            if (!DateUtil.isSameDay(now, lastCrossDayTimer)){
                lastCrossDayTimer = now;
                //跨天触发 宗门事件
                zongMenInfoMap.values().forEach(zongMenInfo ->{
                    ServerContext.getInstance().getProcessor().process(zongMenInfo.getId(),()->{
                        zongMenInfo.handleEvent(ZongMenConstants.ZongMenEvenType.CROSS_DAY);
                        zongMenInfo.checkZongZhuTransfer(now);
                    });
                });
            }
        };
    }

    private Runnable saveAllZongMenData() {
        return ()->{
            long now = System.currentTimeMillis();
            int saveNum = 0;
            for (ZongMenInfo info : zongMenInfoMap.values()) {
                    if (now - info.getSaveDataTimer() >= ZongMenConstants.SAVE_ZONG_MEN_DATA_TIMER){
                        saveNum++;
                        ServerContext.getInstance().getProcessor().process(info.getId(), () ->{
                            info.setSaveDataTimer(now);
                            info.updateModuleData();
                            DAO.update(info.getData());
                            saveZongMenTotalPowerRank(info);
                        });
                    }
            }
            log.info(String.format("saveAllZongMenData use:%d, saveNum:%d, totalNum:%d",System.currentTimeMillis() - now, saveNum,zongMenInfoMap.size()));

        };
    }


    public void loadAllData(){
        log.info(String.format("开始加载所有的宗门"));
        long beginTimer = System.currentTimeMillis();
        DAO.execute(ZongmenMapper.class,"selectByServerNodeIdIndex", ServerContext.getInstance().getServerId())
                        .onSuccess(res ->{
                            int num = 0;
                            if (res != null){
                                List<Zongmen> list = (List<Zongmen>) res;
                                list.forEach(zongmen ->{
                                    ZongMenInfo info = new ZongMenInfo(zongmen);
                                    zongMenInfoMap.put(info.getId(),info);
                                    info.setSaveDataTimer(System.currentTimeMillis() + RandomUtils.nextInt((int) ZongMenConstants.SAVE_ZONG_MEN_DATA_TIMER));

                                });
                                num = list.size();
                            }
                            zongMenAutoIncrementNum.set(num + 1);
                            log.info(String.format("开始加载所有的宗门结束, use:%d, 数量:%d", System.currentTimeMillis() - beginTimer,num));
                        })
                        .onFailure(err ->{
                            log.error(String.format("加载所有的宗门 出错 "));
                            err.printStackTrace();
                        });
    }

	public void loadZongmenList(List<Zongmen> list) {
		int num = 0;
		list.forEach(zongmen -> {
			if (IdCache.initServerId(DistributedObjectType.ZONGMEN, zongmen.getId())) {
				ZongMenInfo info = new ZongMenInfo(zongmen);
				zongMenInfoMap.put(info.getId(), info);
				info.setSaveDataTimer(System.currentTimeMillis() + RandomUtils.nextInt((int) ZongMenConstants.SAVE_ZONG_MEN_DATA_TIMER));
			}
		});
		num = list.size();
		zongMenAutoIncrementNum.set(num + 1);
	}

//	public static void initAllData() {
//		ZongmenMapper mapper = SpringContextLoader.getContext().getBean(ZongmenMapper.class);
//		int total = mapper.getTotal();
//		int pageSize = 100;
//		int totalPages = (total + pageSize - 1) / pageSize;
//
//		for (int page = 0; page < totalPages; page++) {
//			int offset = page * pageSize;
//			CrossServerInterface crossServerInterface = CrossServer.getInstance().getCrossServerInterface();
//			crossServerInterface.loadDataDistributed(offset, totalPages);
//		}
//	}

    public void saveSimpleData(ZongMenInfo zongMen){
        String redisKey = CacheType.ZONG_MEN_SIMPLE_DATA.key(zongMen.getId());
        RedisUtil.setAsync(redisKey,zongMen.toSimpleZongMen());
    }

    public ZongMenInfo getZongMenInfo(long zongMenId) {
        return zongMenInfoMap.get(zongMenId);
    }

    /**
     * 创建宗门
     * @param name 宗门名称
     * @param createPlayerId 门主pid
     * @param createPlayerName 门主名称
     * @param power 门主战力
     * @return 新的宗门
     */
	public Future<ZongMenInfo> createZongMen(String name, long createPlayerId, String createPlayerName, int power, String serverId) {
		long newZongMenId = ZongMenHelper.createZongMenId();
        //创建宗门
        ZongMenInfo zongMenInfo = new ZongMenInfo();
        //宗门初始化
		zongMenInfo.init(serverId, newZongMenId, name, createPlayerId, createPlayerName, power);
        zongMenInfo.updateModuleData();
        Promise<ZongMenInfo> promise = Promise.promise();
        DAO.insert(zongMenInfo.getData()).onSuccess( res ->{
            if (res != null){
                //保存 simple data
                saveSimpleData(zongMenInfo);
                //存储 redis name--id map
                saveRedisNameIdMap(name,newZongMenId);
                //保存宗门战斗力排行榜
                saveZongMenTotalPowerRank(zongMenInfo);
                zongMenInfoMap.put(newZongMenId,zongMenInfo);
				// 宗门所在服务器
				saveZongMenServerId(newZongMenId);
                promise.complete(zongMenInfo);
            } else {
                promise.complete(null);
            }
        } ).onFailure(err ->{
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
		return zongMenInfoMap.keySet();
	}

	void saveZongMenTotalPowerRank(ZongMenInfo zongMenInfo) {
        RankService.getInstance().setScoreAsync(zongMenInfo.getData().getCreateServerId()+"", RankType.ZongMen,zongMenInfo.getId(),zongMenInfo.callTotalPower());
    }

     void saveRedisNameIdMap(String name, long newZongMenId) {
        String key = CacheType.ZONG_MEN_NAME_ID.key(name);
        RedisUtil.setAsync(key,newZongMenId);
    }

	private void saveZongMenServerId(long id) {
		IdCache.getZongMenServerId(id);
	}

	public void clearZongMenServerId(long id) {
		RedisUtil.deleteAsync(CacheType.ZONG_MEN_SERVER_ID.key(id));
	}
}
