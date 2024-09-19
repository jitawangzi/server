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
	int updateByPrimaryKey(GmMail row);
	/**
	 * @mbg.generated
	 */
	List<GmMail> selectByOptFlagIndex(@Param("optFlag") Byte optFlag);
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
	List<GmMail> selectGlobalMailList(@Param("time") Date time);
	List<GmMail> selectGmMailList(@Param("mailOptType")int mailOptType, @Param("startTime") Date startTime, @Param("endTime") Date endTime
	,@Param("title")String title, @Param("context") String context, @Param("status")int status
    ,@Param("limit")int limit, @Param("offset")int offset
	);
}