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

	/**
	 * 根据主键动态更新指定字段 <p>通过Map参数指定要更新的字段名和对应的值，只更新Map中包含的字段</p>
	 * @param playerId  player_id（主键）
	 * @param params  要更新的字段Map，key为数据库字段名，value为新值
	 * @return  更新的记录数
	 * @note   1. 字段名必须与数据库列名一致 2. 字段值类型需要与数据库字段类型兼容 3. 主键字段不会被更新 4. 如果params为空或不包含任何有效字段，将不执行更新操作
	 * @mbg.generated
	 */
	int updateColumnsByPrimaryKey(@Param("playerId") long playerId, @Param("params") java.util.Map<String, Object> params);

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
	 * 更新某个玩家的宗门信息
	 * @param playerId
	 * @param unionId
	 * @param unionName
	 * @return
	 */
	int updatePlayerUnion(@Param("playerId") long playerId, @Param("unionId") long unionId, @Param("unionName") String unionName);

	/**
	 * @Description 执行一条sql语句
	 * @param sql
	 * @return
	 */
	List<Map> executeSql(@Param("sql") String sql);
}