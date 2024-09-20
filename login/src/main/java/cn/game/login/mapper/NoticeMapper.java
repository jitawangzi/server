package cn.game.login.mapper;

import java.util.List;

import cn.game.login.cache.entity.Notice;
import org.apache.ibatis.annotations.Param;

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
	int insertOrUpdate(Notice row);

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
	int insertBatch(List<Notice> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Notice> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<Notice> recordList);

	public List<Notice> selectAll();
}