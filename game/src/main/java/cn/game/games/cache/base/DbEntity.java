package cn.game.games.cache.base;

import cn.game.games.util.DAO;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;

/**    
 * 这里面的方法，都在多表(每个功能单独表)情况下使用。 
 * 单表不用处理数据库，会忽略这些方法调用。 
 * 单表里面的单独表，用DAO类的方法更新数据。 
 * 2024年4月8日 下午6:45:47
 * @author SYQ
 */
public interface DbEntity {

	public default Future<@Nullable Object> insert() {
//		if (GameServer.getInstance().isSinglePlayerTable()) {
//			return Future.succeededFuture();
//		}
		return DAO.insert(this);
	}

	public default Future<@Nullable Object> insertOrUpdate() {
//		if (GameServer.getInstance().isSinglePlayerTable()) {
//			return Future.succeededFuture();
//		}
		return DAO.insertOrUpdate(this);
	}

	/** 
	 * 只更新基本类型的更新方法。 如果确定只修改了基本类型的数据，建议调用这个方法
	 * @return
	 */
	public default Future<@Nullable Object> update() {
//		if (GameServer.getInstance().isSinglePlayerTable()) {
//			return Future.succeededFuture();
//		}
		return DAO.update(this);
	}

	/** 
	 * 可以更新blob的更新方法，没有blob字段的也可以调用。 
	 * @return
	 */
	public default Future<@Nullable Object> updateWithBlobs() {
//		if (GameServer.getInstance().isSinglePlayerTable()) {
//			return Future.succeededFuture();
//		}
		return DAO.updateWithBLOBs(this);
	}

	public default Future<@Nullable Object> delete() {
//		if (GameServer.getInstance().isSinglePlayerTable()) {
//			return Future.succeededFuture();
//		}
		return DAO.delete(this);
	}

	public default Class<?> getMapperClass() {
		return null ; 
	}

	/** 
	 * 这里为什么要单独抽取出来方法，是因为希望这个方法在客户端eventloop线程执行， 
	 * 而不是在vertx的worker线程池里执行，否则会多个线程同时读写map之类，容易有线程安全问题。 
	 * 这里这类对象都是在同一个进程里读写的。  
	 */
	public default void beforeSave() {
	};

	public Object primaryKey();
}
