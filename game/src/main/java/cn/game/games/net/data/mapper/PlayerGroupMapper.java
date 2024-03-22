package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.PlayerGroup;

public interface PlayerGroupMapper {

	/**
	 * @mbggenerated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("groupId") Long groupId);

	/**
	 * @mbggenerated
	 */
	int insert(PlayerGroup record);

	/**
	 * @mbggenerated
	 */
	int insertSelective(PlayerGroup record);

	/**
	 * @mbggenerated
	 */
	PlayerGroup selectByPrimaryKey(@Param("playerId") Long playerId, @Param("groupId") Long groupId);

	/**
	 * @mbggenerated
	 */
	int updateByPrimaryKeySelective(PlayerGroup record);

	/**
	 * @mbggenerated
	 */
	int updateByPrimaryKey(PlayerGroup record);

	List<PlayerGroup> selectAll();
}