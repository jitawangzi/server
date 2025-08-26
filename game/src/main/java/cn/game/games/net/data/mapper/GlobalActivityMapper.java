package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.GlobalActivity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface GlobalActivityMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(long id);

	/**
	 * @mbg.generated
	 */
	int insert(GlobalActivity row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(GlobalActivity row);

	/**
	 * @mbg.generated
	 */
	GlobalActivity selectByPrimaryKey(long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(GlobalActivity row);

	/**
	 * @mbg.generated
	 */
	GlobalActivity selectByTGlobalActivityServerIdIDX(@Param("serverId") String serverId, @Param("configId") int configId);

	/**
	 * @mbg.generated
	 */
	List<GlobalActivity> selectAll();

	/**
	 * @mbg.generated
	 */
	List<GlobalActivity> getBatchOffset(@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	List<GlobalActivity> getBatchCursor(@Param("lastId") long lastId, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	long getTotal();

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<GlobalActivity> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<GlobalActivity> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<GlobalActivity> recordList);

	/**
	 * @mbg.generated
	 */
	Long getLastIdOfBatch(@Param("lastId") long lastId, @Param("limit") int limit);

	/**
	 * 根据主键动态更新指定字段 <p>通过Map参数指定要更新的字段名和对应的值，只更新Map中包含的字段</p>
	 * @param id  唯一id（主键）
	 * @param params  要更新的字段Map，key为数据库字段名，value为新值
	 * @return  更新的记录数
	 * @note   1. 字段名必须与数据库列名一致 2. 字段值类型需要与数据库字段类型兼容 3. 主键字段不会被更新 4. 如果params为空或不包含任何有效字段，将不执行更新操作
	 * @mbg.generated
	 */
	int updateColumnsByPrimaryKey(@Param("id") long id, @Param("params") java.util.Map<String, Object> params);
}