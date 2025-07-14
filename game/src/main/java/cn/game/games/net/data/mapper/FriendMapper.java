package cn.game.games.net.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.Friend;

public interface FriendMapper {
    /**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(@Param("playerId") long playerId, @Param("friendId") long friendId);

	/**
	 * @mbg.generated
	 */
	int insert(Friend row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(Friend row);

	/**
	 * @mbg.generated
	 */
	Friend selectByPrimaryKey(@Param("playerId") long playerId, @Param("friendId") long friendId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(Friend row);

	/**
	 * @mbg.generated
	 */
	List<Friend> selectByPlayerId(@Param("playerId") long playerId);

	/**
	 * @mbg.generated
	 */
	List<Friend> selectAll();

	/**
	 * @mbg.generated
	 */
	List<Friend> getBatchOffset(@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	List<Friend> getBatchCursor(@Param("lastPlayerId") long lastPlayerId, @Param("lastFriendId") long lastFriendId,
			@Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	long getTotal();

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

	/**
	 * 根据主键动态更新指定字段 <p>通过Map参数指定要更新的字段名和对应的值，只更新Map中包含的字段</p>
	 * @param playerId  玩家id（主键）
	 * @param friendId  好友id（主键）
	 * @param params  要更新的字段Map，key为数据库字段名，value为新值
	 * @return  更新的记录数
	 * @note   1. 字段名必须与数据库列名一致 2. 字段值类型需要与数据库字段类型兼容 3. 主键字段不会被更新 4. 如果params为空或不包含任何有效字段，将不执行更新操作
	 * @mbg.generated
	 */
	int updateColumnsByPrimaryKey(@Param("playerId") long playerId, @Param("friendId") long friendId,
			@Param("params") java.util.Map<String, Object> params);

	/**
	 * @mbg.generated
	 */
	int updateFriendGifted(@Param("playerId") long playerId, @Param("friendId") long friendId, @Param("gifted") boolean gifted);

	int selectFriendLocalCount(@Param("playerId") Long playerId, @Param("serverId") String serverId);

	int selectFriendOtherCount(@Param("playerId") Long playerId, @Param("serverId") String serverId);
	
	Long selectBlack(@Param("playerId") Long playerId, @Param("friendId") Long friendId, @Param("relation") Byte relation);

	int deletePlayerData(@Param("playerId") Long playerId);

}
