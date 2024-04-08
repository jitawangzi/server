package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.DayOperation;

public interface DayOperationMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("type") Integer type);

	/**
	 * @mbg.generated
	 */
	int insert(DayOperation row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(DayOperation row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(DayOperation row);

	/**
	 * @mbg.generated
	 */
	DayOperation selectByPrimaryKey(@Param("playerId") Long playerId, @Param("type") Integer type);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(DayOperation row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(DayOperation row);

	/**
	 * @mbg.generated
	 */
	List<DayOperation> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<DayOperation> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<DayOperation> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<DayOperation> recordList);

	int updateByPlayerId(Long playerId);

	int deleteByPlayerId(Long playerId);

}
