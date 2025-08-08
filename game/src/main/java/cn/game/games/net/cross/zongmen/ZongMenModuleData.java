package cn.game.games.net.cross.zongmen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.core.cache.CacheType;
import cn.game.games.cache.entity.GuildJoin;
import cn.game.games.net.cross.zongmen.ZongMenConstants.ZongMenEvenType;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.ZongMenMsg;
import cn.game.util.RedisUtil;

/**
 * @ClassName ZongMenModuleData
 *
 * @description: 宗门各个模块管理，代表一个宗门的所有数据
 * @author: ly
 * @create: 2025-02-05 14:53 @Version 1.0
 */
public class ZongMenModuleData  implements ZongMenConstants.ZongMenEventHandler{
	transient Map<ZongMenConstants.ZongMenEvenType, List<ZongMenConstants.ZongMenEventHandler>> eventTypeHandleMaps = new HashMap<>();
	private static ZongMenEvenType[]  eventTypes = new ZongMenEvenType[] {ZongMenEvenType.CROSS_DAY};

	private long zongmenId; 
	/***      宗门操作日志 */
	ZongMenOptLog optLog;
	/***      宗门 成员列表 */
	public Map<Long, ZongMenMember> menMemberMap = new HashMap<>();
	/**  宗门 设置 */
	public ZongMenSetting setting;
	/**  宗门 活跃度 */
	int liveness;
	/** 活跃度低于某个阈值已经持续了多少天 */
	private int livenessLowDay; 
	/** 宗门砍价 */
	ZongMenBargain bargain;

	/*** 宗门 申请列表 */
	List<Long> applyList = new ArrayList<>();

	void registerAllModuleEventHandler() {
		registerEventHandler(optLog);
		registerEventHandler(setting);
		registerEventHandler(bargain);
		menMemberMap.values().forEach(member -> {
			registerEventHandler(member);
		});
		registerEventHandler(this);
	}

	void registerEventHandler(ZongMenConstants.ZongMenEventHandler eventHandler) {
		if (eventHandler == null) {
			return;
		}
		for (ZongMenConstants.ZongMenEvenType eventType : eventHandler.getRegisterEvent()) {
			List<ZongMenConstants.ZongMenEventHandler> handleList;
			if (eventTypeHandleMaps.containsKey(eventType)) {
				handleList = eventTypeHandleMaps.get(eventType);
			} else {
				handleList = new ArrayList<>();
				eventTypeHandleMaps.put(eventType, handleList);
			}
			handleList.add(eventHandler);
		}
	}

	public void handleEvent(ZongMenConstants.ZongMenEvenType evenType, ZongMen info, Object... params) {
		long beginTimer = System.currentTimeMillis();
		List<ZongMenConstants.ZongMenEventHandler> handlers = eventTypeHandleMaps.get(evenType);
		if (handlers == null || handlers.size() == 0) {
			return;
		}
		handlers.forEach(eventHandler -> {
			eventHandler.handleEventType(evenType, info, params);
		});
		long endTimer = System.currentTimeMillis();
		if (endTimer - beginTimer > 50) {
			ZongMenManager.log.error("handleEvent time is too long, type:%s, use:%d", evenType.getDesc(), endTimer - beginTimer);
		}
	}

	public void init(long zongmenId) {
		optLog = new ZongMenOptLog();
		setting = new ZongMenSetting();
		setting.setAutoJoin(2);

		bargain = new ZongMenBargain();
		bargain.setZongMenId(zongmenId);
	}

	public void afterInit(ZongMen info) {
		bargain.init();
	}

	public void addMember(ZongMenMember member, ZongMen info) {
		menMemberMap.put(member.playerId, member);
		registerEventHandler(member);
		RedisUtil.setAsync(CacheType.PLAYER_ID_ZONG_MEN_ID.key(member.playerId), info.getId());
	}

	public void addApply(long playerId) {
		applyList.add(playerId);
	}

	public long getTotalPower() {
		long totalPower = 0;
		for (ZongMenMember member : menMemberMap.values()) {
			totalPower += member.getPower();
		}
		return totalPower;
	}

	public void removeAllMember() {
		List<Long> pidList = new ArrayList<>(menMemberMap.keySet());
		pidList.forEach(playerId -> {
			removeMember(playerId,2);
		});
		menMemberMap.clear();
	}

	/** 
	 * 移除成员
	 * @param playerId
	 * @param quitType 0 自己退出 1 会长踢出 2 宗门解散
	 */
	public void removeMember(long playerId,int quitType) {
		menMemberMap.remove(playerId);
		RedisUtil.deleteAsync(CacheType.PLAYER_ID_ZONG_MEN_ID.key(playerId));
		ZongMenHelper.notifyMsgToPlayer(playerId, ZongMenMsg.notifyQuitZongMen_40000024.newBuilder().build(),
				PbProtocol.notifyQuitZongMen_40000024);
		
		// 给成员发邮件
		ZongMen zongMen = ZongMenManager.getInstance().getZongMen(playerId); 
		int mailId = quitType ==0 ? 0 :  quitType == 1 ? 25 : 26;
		if (mailId > 0) {
			MailHelper.sendMail(playerId, mailId,true,zongMen.getName()); 
		}
		
		GuildJoin guildJoin = new GuildJoin();
		guildJoin.setPlayerId(playerId);
		guildJoin.delete(); 
		ZongMenManager.log.info(" removeMember playerId:{}", playerId);
	}

	public void removeApply(long playerId) {
		applyList.remove(playerId);
	}

	public int getLiveness() {
		return liveness;
	}

	public void setLiveness(int liveness) {
		this.liveness = liveness;
	}

	public ZongMenBargain getBargain() {
		return bargain;
	}

	public void setBargain(ZongMenBargain bargain) {
		this.bargain = bargain;
	}

	public long getZongmenId() {
		return zongmenId;
	}

	@Override
	public ZongMenEvenType[] getRegisterEvent() {
		return eventTypes;
	}
	
	public ZongMenMember getMasterMember(){
		
		for (Entry<Long, ZongMenMember> entry : menMemberMap.entrySet()) {
			if (entry.getValue().getPosition() == ZongMenConstants.ZONG_MEN_POSITION_ZONG_ZHU) {
				return entry.getValue();
			}
		}
		return null ; 
	}

	@Override
	public void handleEventType(ZongMenEvenType type, ZongMen info, Object... params) {
		switch (type) {

		case CROSS_DAY -> {
			// 每天重置活跃度
			if (liveness < GlobalConst.ZongmenDisbandLiveness) {
				livenessLowDay++;
			} else {
				livenessLowDay = 0;
			}
			ZongMen zongMen = ZongMenManager.getInstance().getZongMen(zongmenId); 
			if (livenessLowDay > GlobalConst.ZongmenDisbandDay) {
				// 如果活跃度低于某个值，超过某个天数，就会被解散
				zongMen.dissolveZongMen();
				return ; 
			}
			if (livenessLowDay > GlobalConst.ZongmenDisbandHitDay) {
				// 给会长提示
				MailHelper.sendMail(getMasterMember().getPlayerId(), 27,true, new Object[] {zongMen.getName(),livenessLowDay,GlobalConst.ZongmenDisbandLiveness,(GlobalConst.ZongmenDisbandDay - livenessLowDay),GlobalConst.ZongmenDisbandLiveness}); 
			}
			setLiveness(0);
			
		}
		default -> throw new IllegalArgumentException("Unexpected value: " + type);
		}
	}
}
