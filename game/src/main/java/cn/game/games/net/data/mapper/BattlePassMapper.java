package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.BattlePass;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BattlePassMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long playerId);

	/**
	 * @mbg.generated
	 */
	int insert(BattlePass row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(BattlePass row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(BattlePass row);

	/**
	 * @mbg.generated
	 */
	BattlePass selectByPrimaryKey(Long playerId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(BattlePass row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(BattlePass row);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<BattlePass> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<BattlePass> records);

	/**
	 * @mbg.generated
	 */
	int batchUpdate(@Param("recordList") List<BattlePass> recordList);
}