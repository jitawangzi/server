package cn.game.games.net.game.module.battle.impl;

import cn.game.games.cache.entity.Player;
import cn.game.games.cache.op.impl.ClimbingTowerOp;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.battle.ChapterModule;
import cn.game.games.net.game.module.battle.IBattleHandler;
import cn.game.protocol.generated.enume.DungeonTypeEnum;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndResponse_13000004;

/**
 * 爬塔战斗
 */
public class BattleClimbingTowerImpl implements IBattleHandler {

	@Override
	public int battleStart(long playerId, int type, int dungeonId, int id, int lineupId, long uid) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		ClimbingTowerOp climbTowerOp= player.getModule(ClimbingTowerOp.class);
		int checkRes = climbTowerOp.checkBattle(id);
		if (checkRes != 0) {
			return checkRes;
		}
		return 0;
	}

	@Override
	public int battleEnd(long playerId, boolean win, int killMonsterCount, int hpPercent, BattleFieldEndResponse_13000004.Builder resp) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		ChapterModule chapterOp = player.getModule(ChapterModule.class);
		int id = chapterOp.getAttackingId();
		ClimbingTowerOp climbTowerOp= player.getModule(ClimbingTowerOp.class);
		int checkRes = climbTowerOp.checkBattle(id);
		if (checkRes != 0) {
			return checkRes;
		}
		
		if (win) {
			climbTowerOp.battleWin();
		} 
		return 0;
	}

	@Override
	public int getType() {
		return DungeonTypeEnum.ClimbingTowerBattle.getId();
	}

}
