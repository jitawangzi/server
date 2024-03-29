package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.ItemNoStack;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ItemNoStackMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Boolean isStack);

	/**
	 * @mbg.generated
	 */
	int insert(ItemNoStack row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(ItemNoStack row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(ItemNoStack row);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<ItemNoStack> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<ItemNoStack> records);

	/**
	 * @mbg.generated
	 */
	int batchUpdate(@Param("recordList") List<ItemNoStack> recordList);
}