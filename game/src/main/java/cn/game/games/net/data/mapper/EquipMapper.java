package cn.game.games.net.data.mapper;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.Equip;

public interface EquipMapper {
	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(long id);

	/**
	 * @mbg.generated
	 */
	int insert(Equip row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Equip row);

	/**
	 * @mbg.generated
	 */
	Equip selectByPrimaryKey(long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Equip row);

	/**
	 * @mbg.generated
	 */
	List<Equip> selectByPlayerId(@Param("playerId") long playerId);

	/**
	 * @mbg.generated
	 */
	List<Equip> selectAll();

	/**
	 * @mbg.generated
	 */
	List<Equip> getBatchOffset(@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	List<Equip> getBatchCursor(@Param("lastId") long lastId, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	long getTotal();

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Equip> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Equip> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<Equip> recordList);

	/**
	 * @mbg.generated
	 */
	Long getLastIdOfBatch(@Param("lastId") long lastId, @Param("limit") int limit);

	int deleteByIds(ArrayList<Long> ids);
}
