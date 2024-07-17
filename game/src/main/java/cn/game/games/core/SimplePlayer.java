package cn.game.games.core;

import java.io.Serializable;

import cn.game.core.base.ServerContext;
import cn.game.games.cache.entity.Player;

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
	boolean online;

	public int combat;
	/** 所在服务器id，并不是真正的在哪个服务器，只是加一个标签 */
	public String serverId;
	/** 所在服务器名，并不是真正的在哪个服务器，只是加一个标签 */
	public String serverName;
	/** 被点赞数量 */
	public int praisedCount;
	public Object accountAdChannel;

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
	 * @param unionName
	 */
	public SimplePlayer(Player player) {
		this.id = player.getData().getPlayerId();
		this.name = player.getData().getName();
		this.level = player.getData().getLevel();
		// TODO 计算战斗力
//		this.combatEffectiveness = player.getData().getPower();
		this.head = player.getData().getHead();
		this.headFrame = player.getData().getHeadFrame();
		this.gender = (byte) (player.getData().getGender().booleanValue() == true ? 1 : 0);
		this.offlineTime = player.getData().getOfflineTime();
		this.online = true;
	}
	public SimplePlayer initDataEx() {
		setServerId(ServerContext.getInstance().getServerId());
		return this;
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
	public int getCombat() {
		return combat;
	}

	public void setCombat(int combat) {
		this.combat = combat;
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
}
