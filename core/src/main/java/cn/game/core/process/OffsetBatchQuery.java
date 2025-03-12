package cn.game.core.process;

import java.util.List;

/**    
 * 基于 offset 的分页查询，如果用在数据库中，注意查询的时候数据不能增加和删除
 * 否则数据会错乱
 * 2025年3月3日 16:37:17
 * @author SYQ
 * @param <T>
 */
public interface OffsetBatchQuery<T> {
	List<T> query(int offset, int limit);
}