package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.Quest;

public interface QuestMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("id") Integer id);

	/**
	 * @mbg.generated
	 */
	int insert(Quest row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Quest row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Quest row);

	/**
	 * @mbg.generated
	 */
	Quest selectByPrimaryKey(@Param("playerId") Long playerId, @Param("id") Integer id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Quest row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeyWithBLOBs(Quest row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Quest row);

	/**
	 * @mbg.generated
	 */
	List<Quest> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Quest> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Quest> records);

	int batchUpdateUsers(List<Quest> records);

}
