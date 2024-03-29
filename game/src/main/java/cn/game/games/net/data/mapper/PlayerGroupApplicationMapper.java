package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.PlayerGroupApplication;

public interface PlayerGroupApplicationMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("groupId") Long groupId);

	/**
	 * @mbg.generated
	 */
	int insert(PlayerGroupApplication row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(PlayerGroupApplication row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(PlayerGroupApplication row);

	/**
	 * @mbg.generated
	 */
	PlayerGroupApplication selectByPrimaryKey(@Param("playerId") Long playerId, @Param("groupId") Long groupId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(PlayerGroupApplication row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(PlayerGroupApplication row);

	/**
	 * @mbg.generated
	 */
	List<PlayerGroupApplication> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<PlayerGroupApplication> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<PlayerGroupApplication> records);

	/**
	 * @mbg.generated
	 */
	int batchUpdate(@Param("recordList") List<PlayerGroupApplication> recordList);

	List<PlayerGroupApplication> selectAll();
}