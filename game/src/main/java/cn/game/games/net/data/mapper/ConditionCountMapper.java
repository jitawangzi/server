package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.ConditionCount;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ConditionCountMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("conditionType") Integer conditionType);

	/**
	 * @mbg.generated
	 */
	int insert(ConditionCount row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(ConditionCount row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(ConditionCount row);

	/**
	 * @mbg.generated
	 */
	ConditionCount selectByPrimaryKey(@Param("playerId") Long playerId, @Param("conditionType") Integer conditionType);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(ConditionCount row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(ConditionCount row);

	/**
	 * @mbg.generated
	 */
	List<ConditionCount> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<ConditionCount> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<ConditionCount> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<ConditionCount> recordList);
}