package cn.game.login.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.login.cache.entity.Notice;

public interface NoticeMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Integer id);

	/**
	 * @mbg.generated
	 */
	int insert(Notice row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Notice row);

	/**
	 * @mbg.generated
	 */
	Notice selectByPrimaryKey(Integer id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Notice row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeyWithBLOBs(Notice row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Notice row);

	/**
	 * @mbg.generated
	 */
	List<Notice> selectAll();

	/**
	 * @mbg.generated
	 */
	List<Notice> getBatchOffset(@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	List<Notice> getBatchCursor(@Param("lastId") Integer lastId, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Notice record);
}