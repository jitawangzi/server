package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.Draw;
import java.util.List;

public interface DrawMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long playerId);

	/**
	 * @mbg.generated
	 */
	int insert(Draw row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Draw row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Draw row);

	/**
	 * @mbg.generated
	 */
	Draw selectByPrimaryKey(Long playerId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Draw row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Draw row);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Draw> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Draw> records);
}