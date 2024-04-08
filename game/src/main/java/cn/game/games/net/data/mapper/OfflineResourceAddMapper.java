package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.OfflineResourceAdd;

public interface OfflineResourceAddMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Integer id);

	/**
	 * @mbg.generated
	 */
	int insert(OfflineResourceAdd row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(OfflineResourceAdd row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(OfflineResourceAdd row);

	/**
	 * @mbg.generated
	 */
	OfflineResourceAdd selectByPrimaryKey(Integer id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(OfflineResourceAdd row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(OfflineResourceAdd row);

	/**
	 * @mbg.generated
	 */
	List<OfflineResourceAdd> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<OfflineResourceAdd> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<OfflineResourceAdd> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<OfflineResourceAdd> recordList);


}
