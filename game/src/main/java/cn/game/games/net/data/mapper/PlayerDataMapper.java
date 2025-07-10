package cn.game.games.net.data.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.SimplePlayer;

public interface PlayerDataMapper {
    /**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(long playerId);

	/**
	 * @mbg.generated
	 */
	int insert(PlayerData row);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(PlayerData row);

	/**
	 * @mbg.generated
	 */
	PlayerData selectByPrimaryKey(long playerId);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(PlayerData row);

	/**
	 * @mbg.generated
	 */
	List<PlayerData> selectByUid(@Param("uid") long uid);

	/**
	 * @mbg.generated
	 */
	List<PlayerData> selectAll();

	/**
	 * @mbg.generated
	 */
	List<PlayerData> getBatchOffset(@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	List<PlayerData> getBatchCursor(@Param("lastId") long lastId, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	long getTotal();

	/**
	 * @mbg.generated
	 */
	int insertBatch(List<PlayerData> records);

	/**
	 * @mbg.generated
	 */
	int deleteBatch(List<PlayerData> records);

	/**
	 * @mbg.generated
	 */
	int updateBatch(@Param("recordList") List<PlayerData> recordList);

	/**
	 * @mbg.generated
	 */
	Long getLastIdOfBatch(@Param("lastId") long lastId, @Param("limit") int limit);

	List<Player> selectPlayersByUid(Long uid);

	int discardPlayer(Long id);

	List<Long> selectAllId();

	Long selectOffLineTime(Long id);

	Long selectMaxId();

	SimplePlayer selectSimplePlayer(Long id);

	List<SimplePlayer> selectSimplePlayers(HashMap<String, Object> hashMap);

	List<SimplePlayer> searchPlayers(HashMap<String, Object> hashMap);

	List<SimplePlayer> selectSimplePlayersLimit(HashMap<String, Object> hashMap);

	Long selectIdByName(String name);

	/**
	 * @Description 执行一条sql语句
	 * @param sql
	 * @return
	 */
	List<Map> executeSql(@Param("sql") String sql);
}