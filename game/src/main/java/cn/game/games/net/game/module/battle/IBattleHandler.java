package cn.game.games.net.game.module.battle;

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
	 * @param type
	 *            DungeonTypeEnum的id
	 * @param dungeonId
	 * @param id
	 * @param lineupId
	 * @param uid
	 */
	int battleStart(long playerId, int type, int dungeonId, int id, int lineupId, long uid);
	
	int battleEnd(long playerId, BattleFieldEndRequest_13000003 request, BattleFieldEndResponse_13000004.Builder resp);

	int getType();
}
