package cn.game.games.net.game.module.chat;

import java.io.Serializable;
import java.util.List;

import cn.game.games.core.SimplePlayer;
import cn.game.games.cache.entity.Group;

public class GroupAllInfo implements Serializable {
	private static final long serialVersionUID = 1277L;
	
	private Group group;
	List<SimplePlayer> simplePlayers;
	
	public Group getGroup() {
		return group;
	}
	public void setGroup(Group group) {
		this.group = group;
	}
	public List<SimplePlayer> getSimplePlayers() {
		return simplePlayers;
	}
	public void setSimplePlayers(List<SimplePlayer> simplePlayers) {
		this.simplePlayers = simplePlayers;
	}
	
	public GroupAllInfo() {
		super();
	}
	
	public GroupAllInfo(Group group, List<SimplePlayer> simplePlayers) {
		super();
		this.group = group;
		this.simplePlayers = simplePlayers;
	}
		
}
