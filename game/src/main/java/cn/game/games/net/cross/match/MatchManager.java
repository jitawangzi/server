package cn.game.games.net.cross.match;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import cn.game.games.cache.entity.Player;

public class MatchManager {

	private static MatchManager  instace = new MatchManager(); 
	
	private MatchManager(){} ; 
	
	private ConcurrentMap<Long, MatchRoom> rooms = new ConcurrentHashMap<>() ; 
	private	AtomicLong roomId = new AtomicLong(1); 
	
	public static MatchManager getInstance(){
		
		return instace; 
	}
	
	public MatchRoom newRoom(Player player){
		
		MatchRoom room = new MatchRoom(player,roomId.getAndIncrement()) ; 
		rooms.put(room.getId(),room) ; 
		return room ; 

 	}    
	
	public MatchRoom match(Player player){
		
		for (MatchRoom matchRoom : rooms.values()) {
			if (matchRoom.matchPlayer(player)) {
				return matchRoom; 
			}
		}
		
		return newRoom(player); 
		
	}
	
	public MatchRoom finish(Player player,int score){
		
//		MatchRoom room = this.rooms.get(player.getData().getMatchRoomId()) ; 
//		if (room!=null) {
//			
//			boolean addFinishId = room.addFinishId(player); 
//			if (addFinishId) {
//				return MatchManager.getInstance().removeRoom(player.getData().getMatchRoomId()) ; 
//			}else {
//				return room ; 
//			}
//		}
		return null ; 
		
	}
	
	public MatchRoom ready(Player player){
		
//		MatchRoom matchRoom = rooms.get(player.getData().getMatchRoomId()); 
//		matchRoom.ready(player); 
//		return matchRoom ; 
		return null ; 
		
	}
	
	public MatchRoom exit(Player player){
//		MatchRoom matchRoom = rooms.get(player.getData().getMatchRoomId()); 
//		if (matchRoom!=null) {
//			matchRoom.removePlayer(player.getData().getId()); 
//			if (matchRoom.size()==0) {
//				MatchManager.getInstance().removeRoom(matchRoom.getId()) ; 
//				return null ; 
//			}
//			return matchRoom ; 
//		}
		return null ; 
		
	}
	public MatchRoom removeRoom(long id){
		return this.rooms.remove(id) ; 
	}
	public MatchRoom getRoom(long id){
		return this.rooms.get(id) ; 
	}
}
