package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.DataFixLog;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DataFixLogMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(int id);

	/**
	 * @mbg.generated
	 */
	int insert(DataFixLog row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(DataFixLog row);

	/**
	 * @mbg.generated
	 */
	DataFixLog selectByPrimaryKey(int id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(DataFixLog row);

	/**
	 * @mbg.generated
	 */
	List<DataFixLog> selectByFixName(@Param("fixName") String fixName);

	/**
	 * @mbg.generated
	 */
	List<DataFixLog> selectAll();

	/**
	 * @mbg.generated
	 */
	List<DataFixLog> getBatchOffset(@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	List<DataFixLog> getBatchCursor(@Param("lastId") int lastId, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	long getTotal();

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<DataFixLog> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<DataFixLog> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<DataFixLog> recordList);

	/**
	 * @mbg.generated
	 */
	Integer getLastIdOfBatch(@Param("lastId") int lastId, @Param("limit") int limit);
}