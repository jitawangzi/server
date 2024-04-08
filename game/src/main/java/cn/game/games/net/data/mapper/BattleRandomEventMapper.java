package cn.game.games.net.data.mapper;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.BattleRandomEvent;

import java.util.List;

public interface BattleRandomEventMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int insert(BattleRandomEvent row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(BattleRandomEvent row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(BattleRandomEvent row);

	/**
	 * @mbg.generated
	 */
	BattleRandomEvent selectByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(BattleRandomEvent row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(BattleRandomEvent row);

	/**
	 * @mbg.generated
	 */
	List<BattleRandomEvent> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<BattleRandomEvent> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<BattleRandomEvent> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<BattleRandomEvent> recordList);


}
