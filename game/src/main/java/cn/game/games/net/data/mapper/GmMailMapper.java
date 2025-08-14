package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.GmMail;

import java.sql.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface GmMailMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(int id);
	/**
	 * @mbg.generated
	 */
	int insert(GmMail row);
	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(GmMail row);
	/**
	 * @mbg.generated
	 */
	GmMail selectByPrimaryKey(int id);
	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(GmMail row);
	/**
	 * @mbg.generated
	 */
	List<GmMail> selectAll();
	/**
	 * @mbg.generated
	 */
	List<GmMail> getBatchOffset(@Param("offset") int offset, @Param("limit") int limit);
	/**
	 * @mbg.generated
	 */
	List<GmMail> getBatchCursor(@Param("lastId") int lastId, @Param("limit") int limit);
	/**
	 * @mbg.generated
	 */
	long getTotal();
	/**
	 * @mbg.generated
	 */
	int insertBatch(List<GmMail> records);
	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<GmMail> records);
	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<GmMail> recordList);
	/**
	 * @mbg.generated
	 */
	Integer getLastIdOfBatch(@Param("lastId") int lastId, @Param("limit") int limit);
	/**
	 * 根据主键动态更新指定字段 <p>通过Map参数指定要更新的字段名和对应的值，只更新Map中包含的字段</p>
	 * @param id  id（主键）
	 * @param params  要更新的字段Map，key为数据库字段名，value为新值
	 * @return  更新的记录数
	 * @note   1. 字段名必须与数据库列名一致 2. 字段值类型需要与数据库字段类型兼容 3. 主键字段不会被更新 4. 如果params为空或不包含任何有效字段，将不执行更新操作
	 * @mbg.generated
	 */
	int updateColumnsByPrimaryKey(@Param("id") int id, @Param("params") java.util.Map<String, Object> params);

	List<GmMail> selectGlobalMailList();

	List<GmMail> selectGmMailList(
			@Param("startTimer")Date startTimer,
			@Param("endTimer")Date endTimer,
			@Param("mailOptType")Integer mailOptType
	,@Param("title")String title, @Param("context") String context, @Param("status")Integer status
    ,@Param("limit")int limit, @Param("offset")int offset
	);
}