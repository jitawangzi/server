package cn.game.games.net.data.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import cn.game.games.cache.entity.Lineup;

public interface LineupMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("lineupId") Integer lineupId);

	/**
	 * @mbg.generated
	 */
	int insert(Lineup row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Lineup row);

	/**
	 * @mbg.generated
	 */
	Lineup selectByPrimaryKey(@Param("playerId") Long playerId, @Param("lineupId") Integer lineupId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Lineup row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Lineup row);

	/**
	 * @mbg.generated
	 */
	List<Lineup> selectByPlayerId(@Param("playerId") Long playerId);


}
