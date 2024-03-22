package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.PlayerTest;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PlayerTestMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int insert(PlayerTest row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(PlayerTest row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(PlayerTest row);

	/**
	 * @mbg.generated
	 */
	PlayerTest selectByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(PlayerTest row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeyWithBLOBs(PlayerTest row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(PlayerTest row);

	/**
	 * @mbg.generated
	 */
	List<PlayerTest> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<PlayerTest> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<PlayerTest> records);
}