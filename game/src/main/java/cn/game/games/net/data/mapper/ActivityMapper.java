package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.Activity;

public interface ActivityMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("id") Integer id);

	/**
	 * @mbg.generated
	 */
	int insert(Activity row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Activity row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Activity row);

	/**
	 * @mbg.generated
	 */
	Activity selectByPrimaryKey(@Param("playerId") Long playerId, @Param("id") Integer id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Activity row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeyWithBLOBs(Activity row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Activity row);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Activity> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Activity> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<Activity> recordList);

	List<Activity> selectByPlayerId(Long playerId);

}
