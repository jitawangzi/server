package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.MonthCard;

public interface MonthCardMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("monthCardId") Integer monthCardId);

	/**
	 * @mbg.generated
	 */
	int insert(MonthCard row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(MonthCard row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(MonthCard row);

	/**
	 * @mbg.generated
	 */
	MonthCard selectByPrimaryKey(@Param("playerId") Long playerId, @Param("monthCardId") Integer monthCardId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(MonthCard row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(MonthCard row);

	/**
	 * @mbg.generated
	 */
	List<MonthCard> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<MonthCard> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<MonthCard> records);

	/**
	 * @mbg.generated
	 */
	int batchUpdate(@Param("recordList") List<MonthCard> recordList);

}
