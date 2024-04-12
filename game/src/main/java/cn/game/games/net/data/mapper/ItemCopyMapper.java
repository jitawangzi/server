package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.ItemCopy;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ItemCopyMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int insert(ItemCopy row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(ItemCopy row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(ItemCopy row);

	/**
	 * @mbg.generated
	 */
	ItemCopy selectByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(ItemCopy row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(ItemCopy row);

	/**
	 * @mbg.generated
	 */
	List<ItemCopy> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<ItemCopy> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<ItemCopy> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<ItemCopy> recordList);
}