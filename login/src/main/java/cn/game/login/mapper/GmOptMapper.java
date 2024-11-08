package cn.game.login.mapper;

import cn.game.login.cache.entity.GmOpt;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface GmOptMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Integer id);

	/**
	 * @mbg.generated
	 */
	int insert(GmOpt row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(GmOpt row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(GmOpt row);

	/**
	 * @mbg.generated
	 */
	GmOpt selectByPrimaryKey(Integer id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(GmOpt row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeyWithBLOBs(GmOpt row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(GmOpt row);

	/**
	 * @mbg.generated
	 */
	List<GmOpt> selectAll();

	/**
	 * @mbg.generated
	 */
	List<GmOpt> getBatch(@Param("offset") int offset, @Param("limit") int limit);

	List<GmOpt> selectByPage(@Param("begin") int begin, @Param("end") int end);

	int count();
}