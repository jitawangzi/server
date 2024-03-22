package cn.game.games.cache.op.face;

import java.util.Set;

/**
 * @Description 特别简单的一组数据，比如就一个id集合的，可以都放到这里
 * @date 2020年11月3日 下午2:18:36
 * @author SYQ
 */
public interface IPlayerOp{
	
	/**
	 * @Description 从db中初始化一些数据
	 * @param unionApplicationIds
	 *            申请过的工会id
	 * @return
	 */
	public int initLoadData(Object ... args);
	
	public boolean addUnionApplication(long unionId);

	public boolean delUnionApplication(long unionId);
	
	/** 
	 * 获取所有完成的线索
	 * @return
	 */
	Set<Integer> getAllClues();
	/** 
	 * 查看某个线索是否完成
	 * @param id
	 * @return
	 */
	boolean isClueFinish(int id);

}
