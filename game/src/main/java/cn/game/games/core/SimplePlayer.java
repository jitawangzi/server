package cn.game.games.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.game.protocol.protobuf.GmMsg;
import com.google.protobuf.InvalidProtocolBufferException;

import cn.game.games.cache.entity.Hero;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.module.develop.secretscript.Secretscript;
import cn.game.protocol.generated.config.NPCConfig;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.protobuf.BaseMsg.PlayerShowInfo;
import cn.game.protocol.protobuf.BaseMsg.SimplePlayerInfo;
import cn.game.protocol.protobuf.BattleMsg;

/**
 * 玩家的简单数据，一般用来显示用
 * 2020年11月2日 下午1:46:12
 * @author SYQ
 */
public class SimplePlayer implements Serializable {

	/**  */
	private static final long serialVersionUID = 1L;
	public long id; // id
	public String name; // 名字
	public int level; // 等级
	public int combatEffectiveness; // 战力
	public int head; // 头像
	public int headFrame; // 头像
	public byte gender; // 性别： 1男2女

	public String unionName;
	public long unionId;
	public long offlineTime;
	public boolean online = true;
	public long createTimer;
	public long lastLoginTimer;
	public int recharge; // 充值金额
	/** 所在服务器id，并不是真正的在哪个服务器，只是加一个标签 */
	public String serverId = "";
	/** 所在服务器名，并不是真正的在哪个服务器，只是加一个标签 */
	public String serverName = "";
	/** 被点赞数量 */
	public int praisedCount;
	public Object accountAdChannel;

	/** 关卡进度 */
	public int battleId;

	public List<Hero> heros;
	/**
	 * 前端需要的战斗相关的属性
	 */
	byte[] battleAttrs;
	/**
	 * 玩家阵容数据 目前只有PVP 玩法 有需要存储
	 */
	private Map<Integer, Map<Integer, List<String>>> lineupMaps = new HashMap<Integer, Map<Integer, List<String>>>();
	/**
	 * 玩家神通 阵容数据
	 */
	Map<Integer,Map<Integer,Integer>> secretscripMap = new HashMap<Integer,Map<Integer,Integer>>();
	List<Secretscript> secretscripInfos = new ArrayList<>();

	public SimplePlayer(long id, String name, int level, int combatEffectiveness, int head, int headFrame, byte gender,
			String unionName, long offLinetime) {
		this.id = id;
		this.name = name;
		this.level = level;
		this.combatEffectiveness = combatEffectiveness;
		this.head = head;
		this.headFrame = head;
		this.gender = gender;
		this.unionName = unionName;
		this.offlineTime = offLinetime;
	}
	/**
	 * @param player
	 *            根据在线的player对象，构造实例
	 */
	public SimplePlayer(Player player) {
		this.id = player.getData().getPlayerId();
		this.name = player.getData().getName();
		this.level = player.getData().getLevel();
		this.combatEffectiveness = player.getAttrModule().getPower();
		this.head = player.getData().getHead();
		this.headFrame = player.getData().getHeadFrame();
		this.gender = (byte) (player.getData().getGender().booleanValue() == true ? 1 : 0);
		this.offlineTime = player.getData().getOfflineTime();
		this.online = player.isOnline();
		this.level = player.getLevel();
		this.lastLoginTimer = player.getLastLoginTimer();
		this.createTimer =  player.getCreateTimer();
		this.battleId = player.getChapterModule().getMainBattleHighest();
		this.heros = new ArrayList<>(player.getHeroModule().getBattleHeroList());
		this.battleAttrs = player.getAttrModule().buildBattleAttrs().toByteArray();
		//存储 大道争锋阵容
		if (player.getChapterModule().getLineups(DungeonTypeEnum.CHAPTER_TYPE_DA_DAO.getId()) != null){
			lineupMaps.put(DungeonTypeEnum.CHAPTER_TYPE_DA_DAO.getId(),player.getChapterModule().getLineups(DungeonTypeEnum.CHAPTER_TYPE_DA_DAO.getId()));
		} else {
			lineupMaps.put(DungeonTypeEnum.CHAPTER_TYPE_DA_DAO.getId(),player.getChapterModule().getLineups(DungeonTypeEnum.BattleChapter.getId()));
		}
        //存储 神通阵容
        secretscripMap.put(DungeonTypeEnum.CHAPTER_TYPE_DA_DAO.getId(),player.getSecretscriptModule().getSecretscriptPosMap());
		secretscripInfos.addAll(player.getSecretscriptModule().getSecretscriptInfos());
	}

	public SimplePlayer(SimplePlayerInfo simplePlayerInfo) {
		SimplePlayer simplePlayer = new SimplePlayer();
		simplePlayer.setId(Long.parseLong(simplePlayerInfo.getId()));
		simplePlayer.setName(simplePlayerInfo.getName());
		simplePlayer.setLevel(simplePlayerInfo.getLevel());
		simplePlayer.setHead(simplePlayerInfo.getHead());
		simplePlayer.setOnline(simplePlayerInfo.getOnline());
		simplePlayer.setOfflineTime(simplePlayerInfo.getOfflineTime());
		simplePlayer.setServerId(simplePlayerInfo.getServerId());
	}
	public SimplePlayer initDataEx() {
//		setServerId(ServerContext.getInstance().getServerId());
		return this;
	}
	public static SimplePlayer makeByNpcConfig(NPCConfig npcConfig) {
		SimplePlayer simplePlayer = new SimplePlayer();
		simplePlayer.setId(npcConfig.ID);
		simplePlayer.setName(npcConfig.Name);
		simplePlayer.setOnline(true);
		simplePlayer.setHeadFrame(400006);
//		simplePlayer.setLevel(npcConfig.lv);
		simplePlayer.setHead(Integer.parseInt(npcConfig.Icon));
		return simplePlayer;
	}
	public SimplePlayerInfo toSimplePlayerInfo() {

		SimplePlayerInfo.Builder builder = SimplePlayerInfo.newBuilder();

		builder.setId(id + "");
		builder.setLevel(level);
		builder.setName(name);
		builder.setOnline(isOnline());
		builder.setOfflineTime((int) (getOfflineTime() / 1000));
		builder.setHead(head);
		builder.setHeadFrame(headFrame);
		builder.setServerId(serverId);
		builder.setCombatEffectiveness(combatEffectiveness);

		return builder.build();
	}

	public GmMsg.GmPlayerInfo toGmPlayerInfo() {
		GmMsg.GmPlayerInfo.Builder builder = GmMsg.GmPlayerInfo.newBuilder();
		builder.setPlayerId(getId()+"");
		builder.setName(getName());
		builder.setLevel(getLevel());
		builder.setCreateTime((int) (getCreateTimer()/1000));
		builder.setLastLoginTime((int) (getLastLoginTimer()/1000));
		builder.setServerId(getServerId());
		builder.setChargeCumulation(recharge);
		if (offlineTime < lastLoginTimer){
			builder.setIsOnline(true);
		} else {
			builder.setIsOnline(false);
		}
        return builder.build();
    }

	public PlayerShowInfo toShowInfo() {

		PlayerShowInfo.Builder showInfo = PlayerShowInfo.newBuilder();
		showInfo.setBattleId(battleId);
		if (unionName != null) {
			showInfo.setGuild(unionName);
		}
		for (Hero hero : heros) {
			showInfo.addHeros(hero.toHeroInfo());
		}
		return showInfo.build();
	}

	public SimplePlayer() {
	};

	public long getId() {

		return id;
	}

	public void setId(long id) {

		this.id = id;
	}

	public String getName() {

		return name;
	}

	public void setName(String name) {

		this.name = name;
	}

	public int getLevel() {

		return level;
	}

	public void setLevel(int level) {

		this.level = level;
	}


	public int getCombatEffectiveness() {
		return combatEffectiveness;
	}

	public void setCombatEffectiveness(int combatEffectiveness) {
		this.combatEffectiveness = combatEffectiveness;
	}
	public byte getGender() {

		return gender;
	}

	public void setGender(byte gender) {

		this.gender = gender;
	}

	public String getUnionName() {

		return unionName;
	}

	public void setUnionName(String unionName) {

		this.unionName = unionName;
	}

	public long getUnionId() {

		return unionId;
	}

	public void setUnionId(long unionId) {

		this.unionId = unionId;
	}

	public long getOfflineTime() {
		return offlineTime;
	}

	public void setOfflineTime(long offlineTime) {
		this.offlineTime = offlineTime;
	}


	public int getHead() {
		return head;
	}

	public void setHead(int head) {
		this.head = head;
	}

	public int getHeadFrame() {
		return headFrame;
	}

	public void setHeadFrame(int headFrame) {
		this.headFrame = headFrame;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public int getPraisedCount() {
		return praisedCount;
	}

	public void setPraisedCount(int praisedCount) {
		this.praisedCount = praisedCount;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimplePlayer other = (SimplePlayer) obj;
		return other.getId() == this.id;
	}

	public Object getAccountId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getClientDeviceId() {
		// TODO Auto-generated method stub
		return null;
	}

	public BattleMsg.PlayerBattleAttrs getPlayerBattleAttrs() {
		try {
			return  BattleMsg.PlayerBattleAttrs.parseFrom(battleAttrs);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Map<Integer, Map<Integer, List<String>>> getLineupMaps() {
		return lineupMaps;
	}

	public void setLineupMaps(Map<Integer, Map<Integer, List<String>>> lineupMaps) {
		this.lineupMaps = lineupMaps;
	}

	public Map<Integer, Map<Integer, Integer>> getSecretscripMap() {
		return secretscripMap;
	}

	public void setSecretscripMap(Map<Integer, Map<Integer, Integer>> secretscripMap) {
		this.secretscripMap = secretscripMap;
	}

	public List<Secretscript> getSecretscripInfos() {
		return secretscripInfos;
	}

	public void setSecretscripInfos(List<Secretscript> secretscripInfos) {
		this.secretscripInfos = secretscripInfos;
	}

	public long getCreateTimer() {
		return createTimer;
	}

	public void setCreateTimer(long createTimer) {
		this.createTimer = createTimer;
	}

	public long getLastLoginTimer() {
		return lastLoginTimer;
	}

	public void setLastLoginTimer(long lastLoginTimer) {
		this.lastLoginTimer = lastLoginTimer;
	}

	public int getRecharge() {
		return recharge;
	}

	public void setRecharge(int recharge) {
		this.recharge = recharge;
	}

	public BattleMsg.BattleSecretscriptInfo toSecretscriptPbInfo(DungeonTypeEnum dungeonTypeEnum ) {
        BattleMsg.BattleSecretscriptInfo.Builder builder = BattleMsg.BattleSecretscriptInfo.newBuilder();
		builder.putAllSecretscriptPosMap(secretscripMap.get(dungeonTypeEnum.getId()));
		secretscripInfos.forEach(secretscript -> {
            builder.addSecretscriptList(secretscript.toProtoInfo());
        });
        return builder.build();
    }

}
