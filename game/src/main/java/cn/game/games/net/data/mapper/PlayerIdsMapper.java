package cn.game.games.net.data.mapper;

import cn.game.games.cache.entity.PlayerIds;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PlayerIdsMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("type") Integer type,
			@Param("configId") Integer configId);

	/**
	 * @mbg.generated
	 */
	int insert(PlayerIds row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(PlayerIds row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(PlayerIds row);

	/**
	 * @mbg.generated
	 */
	PlayerIds selectByPrimaryKey(@Param("playerId") Long playerId, @Param("type") Integer type,
			@Param("configId") Integer configId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(PlayerIds row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(PlayerIds row);

	/**
	 * @mbg.generated
	 */
	List<PlayerIds> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<PlayerIds> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<PlayerIds> records);
}