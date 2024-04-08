package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.Role;

public interface RoleMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int insert(Role row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Role row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Role row);

	/**
	 * @mbg.generated
	 */
	Role selectByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Role row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeyWithBLOBs(Role row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Role row);

	/**
	 * @mbg.generated
	 */
	List<Role> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Role> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Role> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<Role> recordList);


}
