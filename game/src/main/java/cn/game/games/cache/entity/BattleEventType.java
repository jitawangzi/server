package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class BattleEventType implements Serializable, DbEntity {
    /**
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 章节随机事件类型
	 * @mbg.generated
	 */
	private Integer eventType;
	/**
	 * 此类型的事件，每天产生过的次数
	 * @mbg.generated
	 */
	private Integer count;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @mbg.generated
	 */
	public Long getPlayerId() {
		return playerId;
	}

	/**
	 * @mbg.generated
	 */
	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getEventType() {
		return eventType;
	}

	/**
	 * @mbg.generated
	 */
	public void setEventType(Integer eventType) {
		this.eventType = eventType;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getCount() {
		return count;
	}

	/**
	 * @mbg.generated
	 */
	public void setCount(Integer count) {
		this.count = count;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.BattleEventTypeMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return new Object[] { playerId, eventType };
	}

	public static BattleEventType valueOf(long playerId, int eventType, int count) {
		
    	BattleEventType battleEventType = new BattleEventType() ; 
    	battleEventType.setPlayerId(playerId);
    	battleEventType.setEventType(eventType);
    	battleEventType.setCount(count);
    	
    	return battleEventType ; 
    	
    }
}