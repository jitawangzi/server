package cn.game.games.net.data.mapper;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.BattleChapter;

import java.util.List;

public interface BattleChapterMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("chapterId") Integer chapterId);

	/**
	 * @mbg.generated
	 */
	int insert(BattleChapter row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(BattleChapter row);

	/**
	 * @mbg.generated
	 */
	BattleChapter selectByPrimaryKey(@Param("playerId") Long playerId, @Param("chapterId") Integer chapterId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(BattleChapter row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(BattleChapter row);

	/**
	 * @mbg.generated
	 */
	List<BattleChapter> selectByPlayerId(@Param("playerId") Long playerId);
}