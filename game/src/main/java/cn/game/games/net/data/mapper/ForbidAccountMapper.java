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

	/**
	 * 根据主键动态更新指定字段 <p>通过Map参数指定要更新的字段名和对应的值，只更新Map中包含的字段</p>
	 * @param playerId  封禁的玩家id（主键）
	 * @param params  要更新的字段Map，key为数据库字段名，value为新值
	 * @return  更新的记录数
	 * @note   1. 字段名必须与数据库列名一致 2. 字段值类型需要与数据库字段类型兼容 3. 主键字段不会被更新 4. 如果params为空或不包含任何有效字段，将不执行更新操作
	 * @mbg.generated
	 */
	int updateColumnsByPrimaryKey(@Param("playerId") long playerId, @Param("params") java.util.Map<String, Object> params);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(ForbidAccount record);

}