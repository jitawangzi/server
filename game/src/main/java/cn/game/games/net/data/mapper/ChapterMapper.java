package cn.game.games.net.data.mapper;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.Chapter;

import java.util.List;

public interface ChapterMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("chapterId") Integer chapterId);

	/**
	 * @mbg.generated
	 */
	int insert(Chapter row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Chapter row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Chapter row);

	/**
	 * @mbg.generated
	 */
	Chapter selectByPrimaryKey(@Param("playerId") Long playerId, @Param("chapterId") Integer chapterId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Chapter row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Chapter row);

	/**
	 * @mbg.generated
	 */
	List<Chapter> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Chapter> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Chapter> records);


}
