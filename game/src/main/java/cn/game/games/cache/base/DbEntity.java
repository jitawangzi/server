package cn.game.games.cache.base;

import cn.game.games.util.DAO;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;

/**    
 * 数据库实体接口，所有数据库实体类都需要实现这个接口。
 * 这个接口定义了一些基本的数据库操作方法，如插入、更新、删除等。
 * 这些方法都是异步的，返回一个Future对象，表示操作的结果。
 * 具体实现由DAO类提供，DAO类负责与数据库进行交互。
 * 2024年4月8日 下午6:45:47
 * @author SYQ
 */
public interface DbEntity {

	public default Future<@Nullable Object> insert() {
		return DAO.insert(this);
	}

	public default Future<@Nullable Object> insertOrUpdate() {
		return DAO.insertOrUpdate(this);
	}

	/** 
	 * 更新整行数据
	 * @return
	 */
	public default Future<@Nullable Object> update() {
		return DAO.update(this);
	}

	public default Future<@Nullable Object> delete() {
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
