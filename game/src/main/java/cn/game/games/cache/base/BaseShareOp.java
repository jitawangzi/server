package cn.game.games.cache.base;


/**
 * 非角色的全局数据的缓存操作父类
 * @author syq
 *
 */
public abstract class BaseShareOp extends BaseOp{

	
//	public abstract void afterCommit(ShareEntity entity, byte op);
	
	protected void commit(CacheEntity cacheEntity, byte op) {	
		
		ShareEntity o = (ShareEntity)cacheEntity;
		ShareCacheChangeOp changeOp = ShareCacheFactory.getCache(ShareCacheChangeOp.class);
		
		
		if(op == CacheEntity.OP_INSERT){
			
			//新数据可以插入
			if(o.getOp() == CacheEntity.OP_UNCHANGE){
				o.setOp(CacheEntity.OP_INSERT);				
				changeOp.addChange(o);
			}
			
		}else if(op == CacheEntity.OP_UPDATE){
			
			//修改数据, 如果数据当前状态为insert, update, del则不需要重复操作
			if(o.getOp() == CacheEntity.OP_UNCHANGE){
				o.setOp(CacheEntity.OP_UPDATE);
				changeOp.addChange(o);
			}
			
		}else if(op == CacheEntity.OP_DEL){
			
			//正在insert的数据，直接取消持久化
			if(cacheEntity.getOp() == CacheEntity.OP_INSERT){
				changeOp.removeChange(o);
				
			//其他状态数据可删除
			}else{
				o.setOp(CacheEntity.OP_DEL);
				changeOp.addChange(o);
			}
		}	
	}
	
 
	
}
