package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.Story;

public interface StoryMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("story") Integer story);

	/**
	 * @mbg.generated
	 */
	int insert(Story row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Story row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Story row);

	/**
	 * @mbg.generated
	 */
	Story selectByPrimaryKey(@Param("playerId") Long playerId, @Param("story") Integer story);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Story row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Story row);

	/**
	 * @mbg.generated
	 */
	List<Story> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Story> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Story> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<Story> recordList);

}
