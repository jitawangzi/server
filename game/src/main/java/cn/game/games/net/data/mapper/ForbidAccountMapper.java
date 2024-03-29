package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.ForbidAccount;

public interface ForbidAccountMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long playerId);

	/**
	 * @mbg.generated
	 */
	int insert(ForbidAccount row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(ForbidAccount row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(ForbidAccount row);

	/**
	 * @mbg.generated
	 */
	ForbidAccount selectByPrimaryKey(Long playerId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(ForbidAccount row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(ForbidAccount row);

	/**
	 * @mbg.generated
	 */
	List<ForbidAccount> selectByRoleName(@Param("name") String name);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<ForbidAccount> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<ForbidAccount> records);

	/**
	 * @mbg.generated
	 */
	int batchUpdate(@Param("recordList") List<ForbidAccount> recordList);

	List<ForbidAccount> selectAll();
}