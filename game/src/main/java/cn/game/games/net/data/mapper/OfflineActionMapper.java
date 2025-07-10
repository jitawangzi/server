package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.OfflineAction;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OfflineActionMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(long id);

	/**
	 * @mbg.generated
	 */
	int insert(OfflineAction row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(OfflineAction row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(OfflineAction row);

	/**
	 * @mbg.generated
	 */
	OfflineAction selectByPrimaryKey(long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(OfflineAction row);

	/**
	 * @mbg.generated
	 */
	List<OfflineAction> selectByPlayerId(@Param("playerId") long playerId);

	/**
	 * @mbg.generated
	 */
	List<OfflineAction> selectAll();

	/**
	 * @mbg.generated
	 */
	List<OfflineAction> getBatchOffset(@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	List<OfflineAction> getBatchCursor(@Param("lastId") long lastId, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	long getTotal();

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<OfflineAction> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<OfflineAction> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<OfflineAction> recordList);

	/**
	 * @mbg.generated
	 */
	Long getLastIdOfBatch(@Param("lastId") long lastId, @Param("limit") int limit);
}