package cn.game.games.net.game.module.quest;

import java.util.function.Consumer;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.PlayerEventHandler;
import cn.game.protocol.generated.config.ConditionConfig;

public interface Condition extends PlayerEventHandler {

	/**
	 * 完成数量统计
	 * 
	 * @return 数量
	 */
	public long getFinishCount();

	/**
	 * 需求数量， 通常完成数量等于需求数量时表示任务完成
	 * 
	 * @return 需求数量
	 */
	public int getRequireCount();

	/**
	 * 需求id
	 * 
	 * @return
	 */
	public int getRequireId();


	/**
	 * 获得需求索引<br>
	 * 需求唯一标示
	 * 
	 * @return 需求索引
	 */
	byte getIndex();

	/**
	 * 判断需求是否已达成，如果达成了会设置为达成状态
	 * 
	 * @return true:达成<br>
	 *         false:未达成
	 */
	boolean isAchieve();

	/**
	 * 设置需求已达成,需要确认确实条件已经达到了，可以直接完成的情况
	 */
	void setAchieve();

	/** 
	 * 此条件完成时的操作
	 */
	void finishAction();

	/** 
	 * 需求的数量变更时的操作
	 */
	void updateAction();

	/**
	 * 转化为存储字符用于存储
	 * 
	 * @return 存储用字符串
	 */
	String toSaveString();

	/**
	 * 初始条件需求
	 * 
	 * @param condition
	 * @param index
	 * @param quest
	 */
	void init(long playerId, int condition, byte index, Consumer<Condition> updateAction, Consumer<Condition> achieveAction);

	void init(Player player);

	void registerEvent();

	void unregisterEvent();

	/** 
	 * 获取扩展参数，某些可选的参数，如果没有配置，返回0
	 * @param index
	 * @return
	 */
	int getParam(int index);

	/** 
	 * 获取第一个扩展参数，大多数情况扩展参数只有一个
	 * 特别注意如果没有配置扩展参数，会返回0
	 * @return
	 */
	int getParam();

	/** 
	 * 获取这个条件的所有扩展参数
	 * @return
	 */
	int[] getExtParam();

	int getCondition();

	/** 
	 * 直接增加计数，一般是客户端发起的更新。 
	 * @param count
	 */
	void addCount(int count);
	
	/**
	 * 获取条件当前的数值 (Stateless check)
	 * @param player
	 * @param config
	 * @return
	 */
	long getValue(Player player, ConditionConfig config);

}
