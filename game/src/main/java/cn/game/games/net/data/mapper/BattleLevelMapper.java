package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.BattleLevel;

public interface BattleLevelMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("levelId") Integer levelId);

	/**
	 * @mbg.generated
	 */
	int insert(BattleLevel row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(BattleLevel row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(BattleLevel row);

	/**
	 * @mbg.generated
	 */
	BattleLevel selectByPrimaryKey(@Param("playerId") Long playerId, @Param("levelId") Integer levelId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(BattleLevel row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(BattleLevel row);

	/**
	 * @mbg.generated
	 */
	List<BattleLevel> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<BattleLevel> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<BattleLevel> records);


}
