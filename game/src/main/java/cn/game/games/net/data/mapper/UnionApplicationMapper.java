package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.UnionApplication;

public interface UnionApplicationMapper {

    /**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("unionId") Long unionId);

	/**
	 * @mbg.generated
	 */
	int insert(UnionApplication row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(UnionApplication row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(UnionApplication row);

	/**
	 * @mbg.generated
	 */
	UnionApplication selectByPrimaryKey(@Param("playerId") Long playerId, @Param("unionId") Long unionId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(UnionApplication row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(UnionApplication row);

	/**
	 * @mbg.generated
	 */
	List<UnionApplication> selectByApplyid(@Param("unionId") Long unionId);

	/**
	 * @mbg.generated
	 */
	List<UnionApplication> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<UnionApplication> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<UnionApplication> records);

	/**
	 * @mbg.generated
	 */
	int batchUpdate(@Param("recordList") List<UnionApplication> recordList);

	List<UnionApplication> selectAll();
}
