package cn.game.games.net.game.module.award;

/**   
 * @Description 操作类型定义
 * @date 2016-6-25 下午5:44:22
 * @author SYQ
 */
public enum OpType
{
	/** 元宝刷新寻访的武将 */
	FIND_RESET(1),
	/** 购买体力 */
	POWER_BUY(2),
	/** 竞技场战斗 */
	ARENA_BATTLE(3),
	/** 购买竞技场挑战次数 */
	ARENA_BUY(4),
	/** 元宝购买铜钱 */
	COIN_BUY(5),
	/** 个人商店刷新*/
	MARKET_RESET(8),
	
	 ; 
	
	int type ; 
	OpType(int type){
		this.type = type ; 
	}
	public int getType() {
		return type;
	}
	
	/**
	 * 根据操作码查找对应的操作类型
	 */
	public static OpType getOpType(int value)
	{
		for (OpType type : OpType.values())
		{
			if (type.type == value) { return type; }
		}
		return null;
	}
}
