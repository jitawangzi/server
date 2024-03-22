package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.Item;

public interface ItemMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int insert(Item row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Item row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Item row);

	/**
	 * @mbg.generated
	 */
	Item selectByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Item row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Item row);

	/**
	 * @mbg.generated
	 */
	List<Item> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Item> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Item> records);
}