package cn.game.games.cache.op.face;

import java.util.List;

import cn.game.games.cache.base.ICacheOp;
import cn.game.protocol.generated.config.DrawRountineConfig;

public interface IDrawOp {
	/**
	 * 新手卡池当前抽卡次数
	 * 
	 * @return
	 */
	public int getNoviceTimes();

	/**
	 * 普通卡池当前抽卡次数
	 * 
	 * @return
	 */
	public int getCommonTimes();

	/**
	 * up卡池当前抽卡次数
	 * 
	 * @return
	 */
	public int getUpTimes();

	/**
	 * 是否失去了新手卡池抽卡的机会
	 * 
	 * @return
	 */
	public boolean lostNoviceChance();

	/**
	 * 从某卡池抽times次
	 * 
	 * @param pool
	 * @param times
	 * @return 抽中的roleIds
	 */
	public List<Integer> drawByTimesSlow(List<DrawRountineConfig> pool, int times);

}
