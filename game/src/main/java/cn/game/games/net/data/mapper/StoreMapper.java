package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.Store;

public interface StoreMapper {
    /**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("goodsId") Integer goodsId);

	/**
	 * @mbg.generated
	 */
	int insert(Store row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Store row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Store row);

	/**
	 * @mbg.generated
	 */
	Store selectByPrimaryKey(@Param("playerId") Long playerId, @Param("goodsId") Integer goodsId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Store row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Store row);

	/**
	 * @mbg.generated
	 */
	List<Store> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Store> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Store> records);

	int deleteByPlayerId(Long playerId);

}
