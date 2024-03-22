package cn.game.games.cache.base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class PlayerCacheChangeOp extends BaseShareOp {

	 
	//变化的角色id列表
	private List<Long> playerIds;
	
	//playerId => 实体pk_op => 变化实体列表
	private Map<Long, Map<String, PlayerEntity>> changes;
	
	
	@Override
	
	public void init() {
		playerIds = new ArrayList<>();
		changes = new LinkedHashMap<>();		
	}

	/**
	 * 添加一个变化数据
	 * @param o
	 */
	public void addChange(PlayerEntity o){
		
		 
		long playerId = o.getPlayerId();
		Map<String, PlayerEntity> map = changes.get(playerId);
		
		//加到变化角色列表
		if(map == null || map.size() == 0){
			playerIds.add(playerId);
		}
			
		//加到具体角色列表
		if(map == null){
			map = new LinkedHashMap<>();
			changes.put(playerId, map);
		}
		map.put(o.pk() + "_" + o.getOp(), o);
	}
	
	/**
	 * 删除一个变化实体
	 * @param o
	 */
	public void removeChange(PlayerEntity o) {
		
		
		Map<String, PlayerEntity> list = changes.get(o.getPlayerId());
		if(list == null || list.size() == 0)
			return;
		
		list.remove(o.pk() + "_" + o.getOp());
		if(list.size() == 0)
			playerIds.remove(o.getPlayerId());
		
		o.setOp(CacheEntity.OP_UNCHANGE);
	}	
	
	/**
	 * 获取一部分变化数据
	 * @param popCount 最大获取数量
	 * @return
	 */
	public List<PlayerEntity> pop(int popCount){
		
		List<PlayerEntity> retList = new ArrayList<>(popCount);
		if(playerIds.size() == 0)
			return retList;
		
		 
		Map<String, PlayerEntity> dataList = null;
		while(retList.size() < popCount){
			
			if(playerIds.size() == 0)
				break;
			
			//顺序取一个变化的角色
			long playerId = playerIds.get(0);
			dataList = changes.get(playerId);
			
			
			//把角色的变化数据放入返回列表
			if(dataList != null && dataList.size() > 0){
				
				String key;
				for(Iterator<String> iter=dataList.keySet().iterator(); iter.hasNext();){
					key = iter.next();
					retList.add(dataList.get(key));
					iter.remove();
					
					if(retList.size() >= popCount)
						break;
				}
			}
			
			
			//此角色无变化数据了，删除角色id
			if(dataList == null || dataList.size() == 0){
				playerIds.remove(0);
			}
		}
		
		return retList;
	}
	
	public int getPlayerCount(){
		return playerIds.size();
	}
	
	/**
	 * 检查一个角色是否有变化的数据
	 */
	public boolean hasChange(long playerId){
		Map<String, PlayerEntity> m = changes.get(playerId);
		if(m == null || m.size() == 0)
			return false;
		else
			return true;
	}
	
//	@Override
//	public void afterCommit(ShareEntity entity, byte op) {
//		
//	}

	
}
