package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.ShopItem;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ShopItemMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int insert(ShopItem row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(ShopItem row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(ShopItem row);

	/**
	 * @mbg.generated
	 */
	ShopItem selectByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(ShopItem row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(ShopItem row);

	/**
	 * @mbg.generated
	 */
	List<ShopItem> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<ShopItem> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<ShopItem> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<ShopItem> recordList);
}