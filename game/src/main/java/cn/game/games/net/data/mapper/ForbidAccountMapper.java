package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.ForbidAccount;

public interface ForbidAccountMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(long playerId);

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
	ForbidAccount selectByPrimaryKey(long playerId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(ForbidAccount row);

	/**
	 * @mbg.generated
	 */
	List<ForbidAccount> selectAll();

	/**
	 * @mbg.generated
	 */
	List<ForbidAccount> getBatchOffset(@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	List<ForbidAccount> getBatchCursor(@Param("lastId") long lastId, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	long getTotal();

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
	int updateBatch(@Param("recordList") List<ForbidAccount> recordList);

	/**
	 * @mbg.generated
	 */
	Long getLastIdOfBatch(@Param("lastId") long lastId, @Param("limit") int limit);

}