package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.Chip;

public interface ChipMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int insert(Chip row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Chip row);

	/**
	 * @mbg.generated
	 */
	Chip selectByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Chip row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Chip row);

	/**
	 * @mbg.generated
	 */
	List<Chip> selectByPlayerId(@Param("playerId") Long playerId);


}
