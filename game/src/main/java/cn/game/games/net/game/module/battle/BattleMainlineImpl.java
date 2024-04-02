package cn.game.games.net.game.module.battle;

import java.util.List;

import cn.game.games.cache.entity.Player;
import cn.game.games.cache.op.impl.ChapterOp;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.enume.DungeonTypeEnum;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndResponse_13000004;

/**
 * @Description 主线战斗
 */
public class BattleMainlineImpl implements IBattleHandler {

	@Override
	public int battleStart(long playerId, int type, int dungeonId, int id, int lineupId, long uid) {
//		MainlineNpcConfig mainlineNpcConfig = MainlineNpcManager.getInstance().getMainlineNpcConfigNullable(dungeonId);
//		MainlineOp mainlineOp = player.getModule(MainlineOp.class);

		//主线npc
//		if (uid == 0 && mainlineNpcConfig != null) {
//			BattleLevelConfig battleLevelConfig = BattleLevelManager.getInstance().getBattleLevelConfig(mainlineNpcConfig.getBattlelevel());
//			if (battleLevelConfig == null) {
//				return ErrorMsgEnum.config_data_not_found.getId();
//			}
//		}
//		
		//主线怪物战斗
		if (id != 0 && uid != 0) {
			// 合法检验
//			boolean isLegal = mainlineOp.checkMonsterLegal(uid, id);
//			if (!isLegal) {
//				return ErrorMsgEnum.player_check_error.getId();
//			}
		}
		
		
		return 0;
	}

	@Override
	public int battleEnd(long playerId, boolean win, List<Integer> starList, BattleFieldEndResponse_13000004.Builder resp) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		ChapterOp chapterOp = player.getModule(ChapterOp.class);
		int id = chapterOp.getAttackingId();
		int dungeonId = chapterOp.getAttackingDungeonId();
		long attackingUid = chapterOp.getAttackingUid();
//		MainlineOp mainlineOp = player.getModule(MainlineOp.class);
		
//		if (win) {
//			//主线npc(不发uid)
//			if (attackingUid == 0) {
//				MainlineMap mainlineMap = mainlineOp.getCurMainlineMap();
//				MainlineObject object = mainlineMap.getNpcs().get(dungeonId);
//				object.setInteracted(true);
//				mainlineOp.updateNpc(mainlineMap.getMap());
//			}
//			
//			// 主线怪物战斗(uid=怪物uid)
//			if (attackingUid != 0) {
//				int confId = mainlineOp.killMonster(attackingUid);
//				MainlineMonsterConfig monsterConf = MainlineMonsterManager.getInstance().getMainlineMonsterConfig(confId);
//				if (monsterConf == null) {
//					return ErrorMsgEnum.config_data_not_found.getId();
//				}
//				List<Entry<Integer, Integer>> monsterReward = monsterConf.getMonsterReward();
//				
////				2020.06.08策划因为版本问题,临时修改了怪物奖励规则,以后还可能用权重随机奖励的方式
////				int reward = Rnd.randomKey(monsterReward);
////				resp.addAllDungeonRewards(PbBuilder.buildRewardInfo(PlayerHelper.addRewards(playerId, reward)));
//				
//				List<RewardItem> addResources = PlayerHelper.addResources(playerId, monsterReward);
//				resp.addAllDungeonRewards(PbBuilder.buildRewardInfo(addResources));
//				
//				//在主线中战斗时，每场战斗胜利时，每名上阵角色好感度+1，每日有上限，具体由配置决定
//				RoleOp roleOp = player.getModule(RoleOp.class);
//				roleOp.addAllRoleIntimacy(1);
//
//				EventHelper.handleEvent(playerId, new GameEvent(EventTypeEnum.HitMainlineMonster, confId, mainlineOp
//						.getCurMainlineMap().getMap()));
//
//			}	
//		
//		}else {//战斗失败
		
			//打怪失败
//			if (attackingUid != 0) {
//				mainlineOp.battleFailReturnLastCity();
//			}
//			
			
			
//		}

		chapterOp.addBattleLevelPass(id, starList);

		return 0;
	}

	@Override
	public int getType() {
		return DungeonTypeEnum.MainlineBattle.getId();
	}

}
