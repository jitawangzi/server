package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.GmMail;

import java.sql.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface GmMailMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Integer id);
	/**
	 * @mbg.generated
	 */
	int insert(GmMail row);
	/**
	 * @mbg.generated
	 */
	int insertSelective(GmMail row);
	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(GmMail row);
	/**
	 * @mbg.generated
	 */
	GmMail selectByPrimaryKey(Integer id);
	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(GmMail row);
	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeyWithBLOBs(GmMail row);
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
	List<GmMail> getBatchCursor(@Param("lastId") Integer lastId, @Param("limit") int limit);
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
	Integer getLastIdOfBatch(@Param("lastId") Integer lastId, @Param("limit") int limit);
	List<GmMail> selectGlobalMailList();
	List<GmMail> selectGmMailList(
			@Param("startTimer")Date startTimer,
			@Param("endTimer")Date endTimer,
			@Param("mailOptType")Integer mailOptType
	,@Param("title")String title, @Param("context") String context, @Param("status")Integer status
    ,@Param("limit")int limit, @Param("offset")int offset
	);
}