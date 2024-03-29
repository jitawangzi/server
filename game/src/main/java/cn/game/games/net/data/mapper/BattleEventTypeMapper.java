package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.BattleEventType;

public interface BattleEventTypeMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("eventType") Integer eventType);

	/**
	 * @mbg.generated
	 */
	int insert(BattleEventType row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(BattleEventType row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(BattleEventType row);

	/**
	 * @mbg.generated
	 */
	BattleEventType selectByPrimaryKey(@Param("playerId") Long playerId, @Param("eventType") Integer eventType);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(BattleEventType row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(BattleEventType row);

	/**
	 * @mbg.generated
	 */
	List<BattleEventType> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<BattleEventType> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<BattleEventType> records);

	/**
	 * @mbg.generated
	 */
	int batchUpdate(@Param("recordList") List<BattleEventType> recordList);

}
