package cn.game.games.net.game.module.zongmen;

import java.util.ArrayList;
import java.util.List;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.cross.zongmen.ZongMenHelper;
import cn.game.games.net.cross.zongmen.service.ZongmenServiceInterface;
import cn.game.games.net.game.GameServer;
import cn.game.games.net.game.module.player.pointreward.PointRewardType;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.protocol.generated.config.QuestConfig;
import cn.game.protocol.generated.enume.QuestTypeEnum;
import cn.game.protocol.generated.manager.QuestManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PlayerMsg;
import cn.game.protocol.protobuf.ZongMenMsg;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenShowInfo;

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
	/** 宗门贡献值 */
	long contribute;

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
			refreshZongMenTask();
		}
		}
	}

	public void refreshZongMenTask() {
		if (player.getZongMenId() == 0)
			return;
		List<QuestConfig> zongMenTaskList = QuestManager.instance().getTypeList(QuestTypeEnum.ZongMen.ID);
		QuestModule questModule = player.getQuestModule();
		for (QuestConfig config : zongMenTaskList) {
			questModule.remove(config.ID);
			questModule.open(config.ID, true);
			log.info(String.format("玩家[%d] 刷新宗门任务 宗门ID[%d] 任务ID[%d]", player.getPlayerId(), player.getZongMenId(), config.ID));
		}
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
				join(player.getZongMenId(), player.getZongMenName(),0);
			} else {
				// 已经有宗门了,并且没有变化
			}
		}
	}

	/** 
	 * 初次新加入一个工会
	 */
	private void initFirstTime() {
		
        refreshZongMenTask();
        player.getShopModule().refreshShopByShopType(17);
	}

	/** 
	 * 由一个工会退出后，  加入到另外一个工会时
	 */
	private void change() {
//        refreshZongMenTask();
//        player.getShopModule().refreshZongMenShop(17);
	}

	private void getZongMenInfo() {
		ZongMenMsg.getZongMenInfoRequest_40000021 request = ZongMenMsg.getZongMenInfoRequest_40000021.newBuilder().build();
		ZongMenHandler.sendMsgToZongMenServer(player, request).onSuccess(msg -> {
			// 玩家宗门 可能被解散了
			if (msg.errorCode == ErrorMsgEnum.zong_men_not_exist.ID) {
				clearZongMen();
			} else if (msg.errorCode == ErrorMsgEnum.ok.ID) {
				ZongMenMsg.getZongMenInfoResponse_40000022 response = (ZongMenMsg.getZongMenInfoResponse_40000022) msg.response;
//				if (StringUtils.isEmpty(zongMenName)) {
//					setZongMenInfo(response.getInfo());
//				}
			}
		}).onFailure(err -> {
			err.printStackTrace();
		});
	}

	@Deprecated
	public void clearZongMen() {
//		applyJoinList.clear();
//		player.getShopModule().clearZongMenShop();
		contribute = 0;
		// 退出宗门 暂停宗门任务进度
		QuestModule questModule = player.getQuestModule();
		List<QuestConfig> zongMenTaskList = QuestManager.instance().getTypeList(QuestTypeEnum.ZongMen.ID);
		for (QuestConfig config : zongMenTaskList) {
			questModule.remove(config.ID);
		}
		// 清除宗门活跃度领取记录
		player.getPointRewardModule().clearActiveRewardList(PointRewardType.QUEST, QuestTypeEnum.ZongMen.ID);
	}

	public void kickZongMen(ZongMenMsg.notifyQuitZongMen_40000024 quitZongMenMsg) {
		clearZongMen();
		player.getGameClient().sendProtocol(quitZongMenMsg);
	}

	public void joinZongMen(ZongMenMsg.notifyJoinZongMen_40000044 req) {
		join(req.getZongMen().getId(), req.getZongMen().getName(), req.getZongMen().getLevel());
	}

	public void joinAndPush(ZongMenMsg.notifyJoinZongMen_40000044 req) {
		joinZongMen(req);
		player.getGameClient().sendProtocol(req);
	}

	public void quit() {
		if (lastId == 0) {
			return;
		}
		lastId = 0;
		contribute = 0;

	}

	public void join(long zongmenId,String zongmenName,int level) {
		if (zongmenId == lastId) {
			return;
		}
		player.getData().setUnionId(zongmenId);
		player.getData().setUnionName(zongmenName);
		
		if (level == 0) {
			ZongmenServiceInterface zongmenProxy = GameServer.getInstance().getZongmenProxy(zongmenId); 
			ZongMenShowInfo zongmenShowInfo = zongmenProxy.getZongmenShowInfo(zongmenId); 
			level= zongmenShowInfo.getSimpleInfo().getLevel(); 
		}
		
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
		player.handleEvent(EventTypeEnum.ZongMenJoin, zongmenId, zongmenName, level,isFirstJoin);
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

	public long getContribute() {
		return contribute;
	}

	public void setContribute(long contribute) {
		this.contribute = contribute;
	}

	public boolean subContribute(long value) {
		if (this.contribute > value) {
			this.contribute -= value;
			ZongMenHelper.sendMsgToZongMenServer(player,
					ZongMenMsg.ZongMenUpdateContributeValueReq_40000057.newBuilder().setIsAdd(false).setValue((int) contribute).build());
			return true;
		} else {
			return false;
		}
	}

	public void addContribute(int value) {
		if (value < 0) {
			return;
		}
		this.contribute += value;
	}

	public long getLastId() {
		return lastId;
	}

	public void setLastId(long lastId) {
		this.lastId = lastId;
	}

}
