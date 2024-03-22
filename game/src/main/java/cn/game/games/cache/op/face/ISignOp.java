package cn.game.games.cache.op.face;

import cn.game.games.cache.entity.Sign;

public interface ISignOp {

	void initLoadData(Sign sign);
	
	/**
	 * 当月签到情况
	 * @return
	 */
	String seeSign();
	
	/**
	 * 签到
	 * @return
	 */
	boolean sign();
	
	/**
	 * 签到奖励confId
	 * @return
	 */
	int getRewardConfId();
	
	/**
	 * 跨月刷新
	 */
	boolean refresh();
	
	void insert();
	void update();
	
}
