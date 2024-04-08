package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.StoreData;

public interface StoreDataMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long playerId);

	/**
	 * @mbg.generated
	 */
	int insert(StoreData row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(StoreData row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(StoreData row);

	/**
	 * @mbg.generated
	 */
	StoreData selectByPrimaryKey(Long playerId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(StoreData row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeyWithBLOBs(StoreData row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(StoreData row);

	/**
	 * @mbg.generated
	 */
	List<StoreData> selectByItemRoleid(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<StoreData> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<StoreData> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<StoreData> recordList);
}