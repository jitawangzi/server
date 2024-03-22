package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.RoleActionBattle;

public interface RoleActionBattleMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("roleId") Integer roleId,
			@Param("type") Integer type, @Param("subType") Integer subType);

	/**
	 * @mbg.generated
	 */
	int insert(RoleActionBattle row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(RoleActionBattle row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(RoleActionBattle row);

	/**
	 * @mbg.generated
	 */
	RoleActionBattle selectByPrimaryKey(@Param("playerId") Long playerId, @Param("roleId") Integer roleId,
			@Param("type") Integer type, @Param("subType") Integer subType);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(RoleActionBattle row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(RoleActionBattle row);

	/**
	 * @mbg.generated
	 */
	List<RoleActionBattle> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<RoleActionBattle> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<RoleActionBattle> records);


}
