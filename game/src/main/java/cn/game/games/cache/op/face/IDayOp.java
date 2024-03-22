package cn.game.games.cache.op.face;

import java.util.List;

import cn.game.games.cache.base.ICacheOp;
import cn.game.games.cache.entity.DayOperation;
import cn.game.games.net.game.module.award.OpType;


public interface IDayOp{

	//初始数据,
	public void initLoadData(List<DayOperation> dayOperations);
	
	public int getCount(OpType type) ; 
	
	public void addCount(OpType type) ; 

	public void addCount(OpType type,int count) ; 
	
	public void insert(DayOperation dayOperation) ; 
	
	public void update(DayOperation dayOperation) ; 
	
	public boolean reset() ; 


}
