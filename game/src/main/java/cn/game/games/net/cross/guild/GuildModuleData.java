package cn.game.games.net.cross.guild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.core.cache.CacheType;
import cn.game.games.cache.entity.GuildJoin;
import cn.game.games.net.cross.guild.GuildConstants.GuildEvenType;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.GuildMsg;
import cn.game.protocol.protobuf.GuildMsg.GuildApplyProcessedPush_40100001;
import cn.game.util.RedisUtil;

/**
 * @ClassName GuildModuleData
 *
 * @description: 公会各个模块管理，代表一个公会的所有数据
 * @author: ly
 * @create: 2025-02-05 14:53 @Version 1.0
 */
public class GuildModuleData  implements GuildConstants.GuildEventHandler{
	transient Map<GuildConstants.GuildEvenType, List<GuildConstants.GuildEventHandler>> eventTypeHandleMaps = new HashMap<>();
	private static GuildEvenType[]  eventTypes = new GuildEvenType[] {GuildEvenType.CROSS_DAY};

	private long guildId; 
	/***      公会操作日志 */
	GuildOptLog optLog;
	/***      公会 成员列表 */
	public Map<Long, GuildMember> menMemberMap = new HashMap<>();
	/**  公会 设置 */
	public GuildSetting setting;
	/**  公会 活跃度 */
	int liveness;
	/** 活跃度低于某个阈值已经持续了多少天 */
	private int livenessLowDay; 
	/** 公会砍价 */
	GuildBargain bargain;
	/** 创始人名字 */
	private String creatorName; 

	/*** 公会 申请列表 */
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

	void registerEventHandler(GuildConstants.GuildEventHandler eventHandler) {
		if (eventHandler == null) {
			return;
		}
		for (GuildConstants.GuildEvenType eventType : eventHandler.getRegisterEvent()) {
			List<GuildConstants.GuildEventHandler> handleList;
			if (eventTypeHandleMaps.containsKey(eventType)) {
				handleList = eventTypeHandleMaps.get(eventType);
			} else {
				handleList = new ArrayList<>();
				eventTypeHandleMaps.put(eventType, handleList);
			}
			handleList.add(eventHandler);
		}
	}

	public void handleEvent(GuildConstants.GuildEvenType evenType, Guild info, Object... params) {
		long beginTimer = System.currentTimeMillis();
		List<GuildConstants.GuildEventHandler> handlers = eventTypeHandleMaps.get(evenType);
		if (handlers == null || handlers.size() == 0) {
			return;
		}
		handlers.forEach(eventHandler -> {
			eventHandler.handleEventType(evenType, info, params);
		});
		long endTimer = System.currentTimeMillis();
		if (endTimer - beginTimer > 50) {
			GuildManager.log.error("handleEvent time is too long, type:{}, use:{}", evenType.getDesc(), endTimer - beginTimer);
		}
	}

	public void init(long guildId,int joinType,String creatorName) {
		this.guildId = guildId;
		this.creatorName = creatorName;
		optLog = new GuildOptLog();
		setting = new GuildSetting();
		setting.setAutoJoin(joinType);

		bargain = new GuildBargain();
		bargain.setGuildId(guildId);
	}

	public void afterInit(Guild info) {
		bargain.init();
	}

	public void addMember(GuildMember member, Guild info) {
		menMemberMap.put(member.playerId, member);
		registerEventHandler(member);
//		RedisUtil.setAsync(CacheType.PLAYER_ID_ZONG_MEN_ID.key(member.playerId), info.getId());
	}

	public void addApply(long playerId) {
		applyList.add(playerId);
	}

	public long getTotalPower() {
		long totalPower = 0;
		for (GuildMember member : menMemberMap.values()) {
			totalPower += member.getPower();
		}
		return totalPower;
	}

	/** 
	 * 
	 * @param quitType 2 会长主动解散 3 公会活跃度低强制解散
	 */
	public void removeAllMember(int quitType) {
		List<Long> pidList = new ArrayList<>(menMemberMap.keySet());
		pidList.forEach(playerId -> {
			removeMember(playerId,quitType);
		});
		menMemberMap.clear();
	}

	/** 
	 * 移除成员
	 * @param playerId
	 * @param quitType 0 自己退出 1 会长踢出 2 会长主动解散 3 公会活跃度低强制解散
	 */
	public void removeMember(long playerId,int quitType) {
		menMemberMap.remove(playerId);
//		RedisUtil.deleteAsync(CacheType.PLAYER_ID_ZONG_MEN_ID.key(playerId));
		
		GuildHelper.notifyMsgToPlayer(playerId, GuildMsg.GuildQuitPush_40000024.newBuilder().setQuitType(quitType).build(),
				PbProtocol.GuildQuitPush_40000024);
		
		// 给成员发邮件
		Guild guild = GuildManager.getInstance().getGuild(guildId); 
		int mailId = quitType == 1 ? 25 : quitType == 3 ? 26 : 0;
		if (mailId > 0) {
			MailHelper.sendMail(playerId, mailId,true,guild.getName()); 
		}
		
		GuildJoin guildJoin = new GuildJoin();
		guildJoin.setPlayerId(playerId);
		guildJoin.delete(); 
		GuildManager.log.info(" removeMember playerId:{}", playerId);
	}

	public void removeApply(long playerId) {
		applyList.remove(playerId);
		GuildHelper.sendMsgToPlayer(playerId, GuildApplyProcessedPush_40100001.newBuilder().setGuildId(guildId).setPlayerId(playerId).build());
	}

	public int getLiveness() {
		return liveness;
	}

	public void setLiveness(int liveness) {
		this.liveness = liveness;
	}

	public GuildBargain getBargain() {
		return bargain;
	}

	public void setBargain(GuildBargain bargain) {
		this.bargain = bargain;
	}

	public long getGuildId() {
		return guildId;
	}
	

	public String getCreatorName() {
		return creatorName;
	}

	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}

	@Override
	public GuildEvenType[] getRegisterEvent() {
		return eventTypes;
	}
	
	public GuildMember getMasterMember(){
		
		for (Entry<Long, GuildMember> entry : menMemberMap.entrySet()) {
			if (entry.getValue().getPosition() == GuildConstants.ZONG_MEN_POSITION_ZONG_ZHU) {
				return entry.getValue();
			}
		}
		return null ; 
	}

	@Override
	public void handleEventType(GuildEvenType type, Guild info, Object... params) {
		switch (type) {

		case CROSS_DAY -> {
			// 每天重置活跃度
			if (liveness < GlobalConst.GuildDisbandLiveness) {
				livenessLowDay++;
			} else {
				livenessLowDay = 0;
			}
			Guild guild = GuildManager.getInstance().getGuild(guildId); 
			if (livenessLowDay > GlobalConst.GuildDisbandDay) {
				// 如果活跃度低于某个值，超过某个天数，就会被解散
				guild.dissolveGuild(3);
				return ; 
			}
			if (livenessLowDay > GlobalConst.GuildDisbandHitDay) {
				// 给会长提示
				MailHelper.sendMail(getMasterMember().getPlayerId(), 27,true, new Object[] {guild.getName(),livenessLowDay,GlobalConst.GuildDisbandLiveness,(GlobalConst.GuildDisbandDay - livenessLowDay),GlobalConst.GuildDisbandLiveness}); 
			}
			setLiveness(0);
			
		}
		default -> throw new IllegalArgumentException("Unexpected value: " + type);
		}
	}
}
