package cn.game.games.cache.op.face;

import java.util.Collection;
import java.util.List;

import cn.game.games.cache.base.ICacheOp;
import cn.game.games.cache.entity.EventGame;


/**    
 * 事件
 * @date 2022年5月24日 上午11:38:03
 * @author SYQ
 */
public interface IGameEventOp{

	//初始数据,
	public void initLoadData(List<EventGame> events);

	/** 
	 * 一般由前端触发的事件
	 * @param id
	 */
	public void trigger(int id);

	/** 
	 * 一般由后端自己创建的游戏事件
	 * @param id
	 */
	public void create(int id);

	/** 
	 * 更新事件阶段
	 * @param id
	 */
	public void updateStage(int id, int stage);

	/** 
	 * 完成某个事件
	 * @param id
	 */
	public void finish(int id);
	/** 
	 * 放弃某个事件
	 * @param id
	 */
	public void giveup(int id);

	/** 
	 * 删除某个事件
	 * @param id
	 * @param finished 删除事件的时候，这个事件是否需要是完成的状态。
	 */
	public void delete(int id, boolean finished);

	public EventGame get(int id);

	public boolean isFinish(int id);

	/** 
	 * 某事件是否进行中（有事件，并且没完成）
	 * @param id
	 * @return
	 */
	public boolean isRunning(int id);

	/** 
	 * 某事件是否完成过
	 * @param id
	 * @return
	 */
	public boolean hasCompleted(int id);
	/** 
	 * 是否是主动放弃的事件
	 * @param id
	 * @return
	 */
	public boolean isGiveup(int id);

	public void doAction(int actionId);

	public Collection<EventGame> list();

	/** 
	 * 是否可以投放某个事件
	 * @param id
	 * @return
	 */
	boolean canPut(int id);

	/** 
	 * 取消未完成的事件
	 * @param type  1 跨地图  2进入第二阶段
	 */
	@Deprecated
	void cancel(int type);

	/** 
	 * 清除事件过程中产生的交互物、任务等等
	 * @param id
	 * @param eventGame
	 */
	void clearEventGenarateObjects(int id, EventGame eventGame);

}
