package cn.game.games.net.data.mapper;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.QuestChallenge;

import java.util.List;

public interface QuestChallengeMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("id") Integer id, @Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insert(QuestChallenge row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(QuestChallenge row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(QuestChallenge row);

	/**
	 * @mbg.generated
	 */
	QuestChallenge selectByPrimaryKey(@Param("id") Integer id, @Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(QuestChallenge row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(QuestChallenge row);

	/**
	 * @mbg.generated
	 */
	List<QuestChallenge> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<QuestChallenge> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<QuestChallenge> records);

}
