package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.FriendApplication;

public interface FriendApplicationMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("applyPlayerId") Long applyPlayerId);

	/**
	 * @mbg.generated
	 */
	int insert(FriendApplication row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(FriendApplication row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(FriendApplication row);

	/**
	 * @mbg.generated
	 */
	FriendApplication selectByPrimaryKey(@Param("playerId") Long playerId, @Param("applyPlayerId") Long applyPlayerId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(FriendApplication row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(FriendApplication row);

	/**
	 * @mbg.generated
	 */
	List<FriendApplication> selectByApplyid(@Param("applyPlayerId") Long applyPlayerId);

	/**
	 * @mbg.generated
	 */
	List<FriendApplication> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	List<FriendApplication> selectAll();

	/**
	 * @mbg.generated
	 */
	List<FriendApplication> getBatchOffset(@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	List<FriendApplication> getBatchCursor(@Param("lastPlayerId") Long lastPlayerId, @Param("lastApplyPlayerId") Long lastApplyPlayerId,
			@Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	long getTotal();

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<FriendApplication> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<FriendApplication> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<FriendApplication> recordList);

	int deletePlayerData(@Param("playerId") Long playerId);


}
