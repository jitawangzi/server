package cn.game.games.net.data.mapper;

import java.util.List;

import cn.game.games.cache.entity.Group;
import org.apache.ibatis.annotations.Param;

public interface GroupMapper {
    /**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int insert(Group row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Group row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Group row);

	/**
	 * @mbg.generated
	 */
	Group selectByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Group row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Group row);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Group> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Group> records);

	/**
	 * @mbg.generated
	 */
	int batchUpdate(@Param("recordList") List<Group> recordList);

	List<Group> selectAll();
}