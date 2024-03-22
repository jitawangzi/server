package cn.game.games.net.data.mapper;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.Equip;

public interface EquipMapper {
	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int insert(Equip row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Equip row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Equip row);

	/**
	 * @mbg.generated
	 */
	Equip selectByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Equip row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeyWithBLOBs(Equip row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Equip row);

	/**
	 * @mbg.generated
	 */
	List<Equip> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Equip> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Equip> records);

	int deleteByIds(ArrayList<Long> ids);
}
