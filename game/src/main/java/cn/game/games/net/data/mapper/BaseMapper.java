package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.Base;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BaseMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Integer intField1);

	/**
	 * @mbg.generated
	 */
	int insert(Base row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Base row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Base row);

	/**
	 * @mbg.generated
	 */
	Base selectByPrimaryKey(Integer intField1);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Base row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Base row);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Base> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Base> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<Base> recordList);
}