package cn.game.games.net.cross.match;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.manager.GameClientManager;

public class MatchRoom implements Serializable{

	/**  */
	private static final long serialVersionUID = 7096622149963478978L;
	public static final int PERSON_SIZE = 2 ; 
	private Map<Long, Player> players = new HashMap<>() ; 
	private int level ; 
	private long createTime ; 
	private long id ; 
	private List<Long> readyIds = new ArrayList<>() ; 
	private List<Long> finishIds = new ArrayList<>() ; 
	
	
	public MatchRoom(Player firstPlayer,long id){
		createTime = System.currentTimeMillis() ; 
		level = firstPlayer.getData().getLevel();
		this.id = id ; 
		players.put(firstPlayer.getData().getPlayerId(), firstPlayer);
	}
	
	public int size(){
		return this.players.size() ; 
	}
	
	public void clear(){
		this.players.clear(); 
		this.level = 1 ; 
		this.createTime = 0 ; 
		this.readyIds.clear();  
	}
	
	public boolean matchPlayer(Player player){
		
		synchronized (players) {
			if (players.size()>=PERSON_SIZE) {
				return false; 
			}
			if (players.isEmpty()) {
				players.put(player.getData().getPlayerId(), player) ; 
				level = player.getData().getLevel(); 
				return true ; 
			}
			int second = (int) ((System.currentTimeMillis()-createTime)/1000) ; 
			int l = second / 5 ; 

			if (player.getData().getLevel()>=level-l*5&&player.getData().getLevel()<=level+l*5) {
				players.put(player.getData().getPlayerId(), player) ; 
				return true ; 
			}
		}
		return false; 
	}
	
	public Player removePlayer(long id){
		return this.players.remove(id) ; 
	}
	
	public Collection<Player> getPlayers(){
		return this.players.values() ; 
	}

	public long getId() {
		return id;
	}
	public boolean addFinishId(Player player){
		
		synchronized (finishIds) {
			finishIds.add(player.getData().getPlayerId()) ;
			if (finishIds.size()==players.size()) {
				return true ; 
			}
		}
		return false ; 
	}
	
	public boolean ready(Player player){
		
		synchronized (readyIds) {
			readyIds.add(player.getData().getPlayerId()) ; 
			if (readyIds.size()==PERSON_SIZE) {
				return true ; 
			}
		}
		return false ; 
		
	}
	
	public void broadcast(Object message){
		
		broadcast(message, 0);
	}
	public void broadcast(Object message,long notContain){

		for (Player player2 : players.values()) {
			GameClient gameClient = GameClientManager.getInstance().getGameClientByPlayer(player2.getPlayerId()) ; 
			if (gameClient!=null) {
				if (notContain>0&&player2.getPlayerId()==notContain) {
					continue ; 
				}
				gameClient.sendProtocol(message); ; 
			}
		}
		
	}
	
}
