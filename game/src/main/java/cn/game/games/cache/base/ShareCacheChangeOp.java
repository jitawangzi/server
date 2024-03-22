package cn.game.games.cache.base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class ShareCacheChangeOp extends BaseShareOp {

	 
	private Map<String, ShareEntity> changes;
	
	@Override
	public void init() {
		changes = new LinkedHashMap<>();		
	}

	public void addChange(ShareEntity o){
		changes.put(o.pk(), o);
	}
	
	public void removeChange(ShareEntity o) {
		changes.remove(o.pk());
	}
	
	public List<ShareEntity> pop(int count){
		
		List<ShareEntity> list = new ArrayList<>();
		
		//ShareEntity o;
		String key;
		for(Iterator<String> iter=changes.keySet().iterator(); iter.hasNext(); ){
			key = iter.next();
			list.add(changes.get(key));
			iter.remove();
			
			if(list.size() >= count)
				break;
		}
		
		return list;
	}
	
	public int getChangeCount(){
		return changes.size();
	}

	
	
//	@Override
//	public void afterCommit(ShareEntity entity, byte op) {		
//	}
}
