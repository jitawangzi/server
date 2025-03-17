package cn.game.core.db;

import java.util.List;

/**    
 * 数据库分页处理接口
 * 2025年3月17日 11:13:46
 * @author SYQ
 * @param <T>
 * @param <ID>
 */
public interface GenericDataLoader<T, ID extends Number> {

	/** 
	 * 查询分页参数
	 * @param lastId
	 * @param limit
	 * @return
	 */
	ID getLastIdOfBatch(ID lastId, int limit);

	List<T> getBatch(ID lastId, int limit);

	void processData(List<T> data);

}