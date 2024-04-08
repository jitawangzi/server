package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.Buff;

public interface BuffMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int insert(Buff row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Buff row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Buff row);

	/**
	 * @mbg.generated
	 */
	Buff selectByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Buff row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Buff row);

	/**
	 * @mbg.generated
	 */
	List<Buff> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Buff> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Buff> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<Buff> recordList);
}