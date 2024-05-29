package cn.game.games.net.game.module.battle;

import cn.game.games.cache.entity.Player;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndResponse_13000004;

/**
 * @Description 战斗接口
 * @date 2021年1月14日 下午6:17:52
 * @author SYQ
 */
public interface IBattleHandler {

	/**
	 * @Description
	 * @param playerId
	 * @param type，战斗类型
	 *            DungeonTypeEnum的id
	 * @param dungeonId  类型里面对应的id
	 * @param id  关卡id
	 * @param lineupId
	 * @param uid
	 */
	int battleStart(long playerId, int type, int dungeonId, int id, int lineupId, long uid);
	
	int battleEnd(long playerId, BattleFieldEndRequest_13000003 request, BattleFieldEndResponse_13000004.Builder resp);

	int getType();

	int check(Player player, int type, int dungeonId);
}
