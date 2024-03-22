package cn.game.games.net.data.mapper;

import java.util.List;
import cn.game.games.cache.entity.ClimbingTower;

public interface ClimbingTowerMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long playerId);

	/**
	 * @mbg.generated
	 */
	int insert(ClimbingTower row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(ClimbingTower row);

	/**
	 * @mbg.generated
	 */
	ClimbingTower selectByPrimaryKey(Long playerId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(ClimbingTower row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(ClimbingTower row);

	List<ClimbingTower> selectAll();
}