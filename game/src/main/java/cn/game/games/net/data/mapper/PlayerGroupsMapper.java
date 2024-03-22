package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.PlayerGroups;

public interface PlayerGroupsMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("groupId") Long groupId);

	/**
	 * @mbg.generated
	 */
	int insert(PlayerGroups row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(PlayerGroups row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(PlayerGroups row);

	/**
	 * @mbg.generated
	 */
	PlayerGroups selectByPrimaryKey(@Param("playerId") Long playerId, @Param("groupId") Long groupId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(PlayerGroups row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(PlayerGroups row);

	/**
	 * @mbg.generated
	 */
	List<PlayerGroups> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<PlayerGroups> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<PlayerGroups> records);
}