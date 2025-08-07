package cn.game.games.net.game.module.zongmen;

import java.util.ArrayList;
import java.util.List;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.cross.zongmen.service.ZongmenServiceInterface;
import cn.game.games.net.game.GameServer;
import cn.game.games.net.game.module.player.pointreward.PointRewardType;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.protocol.generated.config.QuestConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.QuestTypeEnum;
import cn.game.protocol.generated.manager.QuestManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PlayerMsg;
import cn.game.protocol.protobuf.ZongMenMsg;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenPersonalInfo;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenShowInfo;
import cn.game.util.IntMapWrapper;

/**
 * @ClassName ZongMenModule
 *
 * @description: 玩家的宗门数据
 * @author: ly
 * @create: 2025-02-06 17:18 @Version 1.0
 */
public class ZongMenModule extends BasePlayerModule {

	/** 上一个宗门的id */
	private long lastId;
	/** 宗门反复加入次数 * */
	int disbandCount;

	/**
	 * 下次加入宗门的时间 第二次及后续解散时，宗主需要1小时才可加入其它宗门（ZongmenSuzerainCD）
	 * 第二次及后续退出时，需要1小时才可加入其它宗门（ZongmenMemberCD）*
	 */
	long nextJoinTimer;
	/** 加入时间 */
	long joinTime;
	/** 申请过加入宗门列表 */
	List<Long> applyJoinList = new ArrayList<>();
	private boolean inited = false;
	
	/** 每日捐献次数记录 */
	private IntMapWrapper donateMap = new IntMapWrapper();
	/** 当日砍价次数 */
	private int bargainCount;
	/** 砍价后是否购买 */
	private boolean isBargainBuy;
	

	public int getDisbandCount() {
		return disbandCount;
	}

	public void setDisbandCount(int disbandCount) {
		this.disbandCount = disbandCount;
	}

	public long getNextJoinTimer() {
		return nextJoinTimer;
	}

	public void setNextJoinTimer(long nextJoinTimer) {
		this.nextJoinTimer = nextJoinTimer;
	}

	public long getJoinTime() {
		return joinTime;
	}

	@Override
	public void buildPlayerAllInfo(PlayerMsg.PlayerAllInfo.Builder builder) {
		// 宗门信息
		builder.setZongMenId(player.getZongMenId());
		if (player.getZongMenName() != null) {
			builder.setZongMenName(player.getZongMenName());
		}
		builder.addAllApplyZongMenIds(applyJoinList);
		builder.setZongMenQuitCount(disbandCount);
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return new EventTypeEnum[] { EventTypeEnum.LoginFinish, EventTypeEnum.NewDay };
	}

	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {
		case LoginFinish -> {
			checkZongMen();
		}
		case NewDay -> { // 跨天刷新宗门任务
			initZongMenTask();
			donateMap.clear();
			// 砍价重置为1次
			player.getCurrencyModule().setCount(Asset.ZongMenBargain.ID, 1);
			bargainCount = 0;
			isBargainBuy = false;
		}
		case GetItem -> {
			// 可能更新公会资源
			if (player.getZongMenId() > 0) {
				int id = event.get(0);
				int count = event.get(1);
				if (id == Asset.ZongMenExp.ID || id == Asset.ZongMenPoint.ID) {
					ZongmenServiceInterface zongmenProxy = GameServer.getInstance().getZongmenProxy(player.getZongMenId()); 
					zongmenProxy.addZongmenAsset(player.getZongMenId(), playerId, id, count) ;
				}else if (id == Asset.ZongMenContribute.ID) {
					
				}
			}; 
		}
		}
	}

	public void initZongMenTask() {
		QuestModule questModule = player.getQuestModule();
		questModule.refreshQuest(QuestTypeEnum.ZongMen);
	}

	private void checkZongMen() {
		// 未加入宗门 检测是否有宗门
		/*	if (player.getZongMenId() == 0) {
				RedisLocalCache.getInstance().getAsync(CacheType.PLAYER_ID_ZONG_MEN_ID.key(player.getPlayerId())).onSuccess(msg -> {
					if (msg == null) {
						return;
					}
					long zongMenId = Long.parseLong(msg + "");
					setZongMenId(zongMenId);
					getZongMenInfo();
					refreshZongMenTask();
					player.getShopModule().refreshZongMenShop();
					log.info(String.format("玩家[%d]登录成功，离线期间被审批加入宗门  宗门ID[%d]", player.getPlayerId(), zongMenId));
				}).onFailure(err -> {
					err.printStackTrace();
				});
			} else {
				getZongMenInfo();
			}*/
		// 没有宗门
		if (player.getZongMenId() == 0) {
			// 离线期间被退出了
			if (lastId > 0) {
				quit();
			} else {
				// 一直没有,忽略
			}
		} else {
			// 有宗门
			if (player.getZongMenId() != lastId) {
				join(player.getZongMenId(), player.getZongMenName());
			} else {
				// 已经有宗门了,并且没有变化
			}
		}
	}

	/** 
	 * 初次新加入一个工会
	 */
	private void initFirstTime() {
		
        initZongMenTask();
        player.getShopModule().refreshShopByShopType(17);
	}

	/** 
	 * 由一个工会退出后，  加入到另外一个工会时
	 */
	private void change() {
//        refreshZongMenTask();
//        player.getShopModule().refreshZongMenShop(17);
	}


	public void kickZongMen(ZongMenMsg.notifyQuitZongMen_40000024 quitZongMenMsg) {
		quit();
		player.getGameClient().sendProtocol(quitZongMenMsg);
	}

	public void joinZongMen(ZongMenMsg.notifyJoinZongMen_40000044 req) {
		join(req.getZongMen().getId(), req.getZongMen().getName());
	}

	public void joinAndPush(ZongMenMsg.notifyJoinZongMen_40000044 req) {
		joinZongMen(req);
		player.getGameClient().sendProtocol(req);
	}

	/** 
	 * 退出一个公会
	 */
	public void quit() {
		if (lastId == 0) {
			return;
		}
		lastId = 0;
		player.getCurrencyModule().setCount(Asset.ZongMenContribute.ID, 0);
	}

	public void join(long zongmenId,String zongmenName) {
		if (zongmenId == lastId) {
			return;
		}
		player.getData().setUnionId(zongmenId);
		player.getData().setUnionName(zongmenName);
		
		boolean isFirstJoin = true;
		// 之前有加入过宗门，不是第一次加入
		if (lastId > 0) {
			change();
			isFirstJoin = false;
		} else {
			// 初次加入
			initFirstTime();
		}
		
		lastId = zongmenId; 
		applyJoinList.clear(); 
		inited = true;
		player.handleEvent(EventTypeEnum.ZongMenJoin, zongmenId, zongmenName,isFirstJoin);
	}

	public List<Long> getApplyJoinList() {
		return applyJoinList;
	}

	public void setApplyJoinList(List<Long> applyJoinList) {
		this.applyJoinList = applyJoinList;
	}

	public void removeApplyJoinList(Long zongMenId) {
		applyJoinList.remove(zongMenId);
	}
	public long getLastId() {
		return lastId;
	}

	public void setLastId(long lastId) {
		this.lastId = lastId;
	}
	
	public int getLevel() {
		if (player.getZongMenId() == 0) {
			return 0;
		}
		ZongmenServiceInterface zongmenProxy = GameServer.getInstance().getZongmenProxy(player.getZongMenId());
		ZongMenShowInfo zongmenShowInfo = zongmenProxy.getZongmenShowInfo(player.getZongMenId());
		return zongmenShowInfo.getSimpleInfo().getLevel();
	}

	public IntMapWrapper getDonateMap() {
		return donateMap;
	}
	
	public ZongMenPersonalInfo toPersonalInfo() {
		ZongMenPersonalInfo.Builder builder = ZongMenPersonalInfo.newBuilder();
		builder.setIsBargainBuy(isBargainBuy);
		builder.setBargainCount(bargainCount);
		builder.putAllDonate(donateMap.getMap()) ; 
		
		return builder.build();
	}

	public boolean isInited() {
		return inited;
	}

	public int getBargainCount() {
		return bargainCount;
	}

	public boolean isBargainBuy() {
		return isBargainBuy;
	}

	public void setBargainCount(int bargainCount) {
		this.bargainCount = bargainCount;
	}

	public void setBargainBuy(boolean isBargainBuy) {
		this.isBargainBuy = isBargainBuy;
	}
	
	
}
