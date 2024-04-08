package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.RoleAction;

public interface RoleActionMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("roleId") Integer roleId,
			@Param("type") Integer type, @Param("subType") Integer subType);

	/**
	 * @mbg.generated
	 */
	int insert(RoleAction row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(RoleAction row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(RoleAction row);

	/**
	 * @mbg.generated
	 */
	RoleAction selectByPrimaryKey(@Param("playerId") Long playerId, @Param("roleId") Integer roleId,
			@Param("type") Integer type, @Param("subType") Integer subType);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(RoleAction row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(RoleAction row);

	/**
	 * @mbg.generated
	 */
	List<RoleAction> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<RoleAction> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<RoleAction> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<RoleAction> recordList);


}
