package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.Friend;

public interface FriendMapper {
    /**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") Long playerId, @Param("friendId") Long friendId);

	/**
	 * @mbg.generated
	 */
	int insert(Friend row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(Friend row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Friend row);

	/**
	 * @mbg.generated
	 */
	Friend selectByPrimaryKey(@Param("playerId") Long playerId, @Param("friendId") Long friendId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(Friend row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Friend row);

	/**
	 * @mbg.generated
	 */
	List<Friend> selectByPlayerId(@Param("playerId") Long playerId);

	/**
	 * @mbg.generated
	 */
	List<Friend> selectAll();

	/**
	 * @mbg.generated
	 */
	List<Friend> getBatch(@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<Friend> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<Friend> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<Friend> recordList);

	int selectFriendLocalCount(@Param("playerId") Long playerId, @Param("serverId") String serverId);

	int selectFriendOtherCount(@Param("playerId") Long playerId, @Param("serverId") String serverId);
	
	Long selectBlack(@Param("playerId") Long playerId, @Param("friendId") Long friendId, @Param("relation") Byte relation);

}
