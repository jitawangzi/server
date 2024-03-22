package cn.game.games.cache.op.face;

import java.util.Collection;
import java.util.List;

import cn.game.games.cache.base.ICacheOp;
import cn.game.games.cache.entity.Story;

public interface IStoryOp {

	/**
	 * @Description 初始化剧情
	 * @param items
	 */
	void initLoadData(List<Story> storys);
	
	boolean update(int id, int count, boolean finish);

	Story get(int id);

	boolean finish(int id);

	/**
	 * 剧情是否都完成了
	 * 
	 * @param ids
	 * @return
	 */
	boolean isFinish(List<Integer> ids);

	Collection<Story> list();

}
