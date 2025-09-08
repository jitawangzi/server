package cn.game.games.core;

import java.io.Serializable;

import cn.game.games.cache.entity.Player;
import cn.game.protocol.generated.manager.VirtualServerManager;

@Deprecated
public class BasePlayer implements Serializable {

	public long id; // id
	public String name; // 名字
	public int level; // 等级
	public long unionId;
	/** 所在服务器id，并不是真正的在哪个服务器，只是加一个标签 */
	public String serverId = "";
	/** 所在服务器名，并不是真正的在哪个服务器，只是加一个标签 */
	public String serverName = "";

	/**
	 * @param player
	 *            根据在线的player对象，构造实例
	 */
	public BasePlayer(Player player) {
		this.id = player.getData().getPlayerId();
		this.name = player.getData().getName();
		this.level = player.getLevel();
		this.serverId = player.getServerId();
		this.serverName = VirtualServerManager.instance().get(this.serverId).name;
	}

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

	public long getUnionId() {
		return unionId;
	}

	public void setUnionId(long unionId) {
		this.unionId = unionId;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}


}
