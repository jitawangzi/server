package cn.game.games.net.game.module.battle.impl;

import java.time.LocalTime;
import java.util.List;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.battle.ChapterModule;
import cn.game.games.net.game.module.battle.LingPoBattle;
import cn.game.games.net.game.module.battle.XiYouBattleHandler;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndResponse_13000004;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**    
 * 灵魄之战
 * 2024年7月31日 下午2:21:06
 * @author SYQ
 */
public class LingPoImpl extends XiYouBattleHandler {

	@Override
	public int battleStart(long playerId, int type, int dungeonId, int id, int lineupId, long uid) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		LingPoBattle lingPoBattle = chapterModule.getLingPoBattle();
		if (dungeonId != lingPoBattle.getBattleId()) {
			return ErrorMsgEnum.request_parameter_error.getId();
		}
		LocalTime now = LocalTime.now();
		LocalTime start = LocalTime.of(23, 30);

		if (now.isAfter(start)) {
			return ErrorMsgEnum.not_open.getId();
		}
		if (lingPoBattle.isAdsGetBattleTimes()) {
			return ErrorMsgEnum.not_watch_ads.getId();
		}
		if (lingPoBattle.getBattleTimes() >= 2) {
			return ErrorMsgEnum.times_limit.getId();
		}
		return 0;
	}

	@Override
	public int battleEnd(long playerId, BattleFieldEndRequest_13000003 request, BattleFieldEndResponse_13000004.Builder resp) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		int attackingType = chapterModule.getAttackingType();
		BattleConfig battleConfig = BattleManager.instance().get(chapterModule.getAttackingDungeonId());
		LingPoBattle lingPoBattle = chapterModule.getLingPoBattle();
		int pointAdd = 0;
		if (request.getWin()) {
			pointAdd = GlobalConst.SpiritBattleVicpoint;
		} else {
			int[][] spiritBattleFailpoint = GlobalConst.SpiritBattleFailpoint;
			for (int i = spiritBattleFailpoint.length - 1; i >= 0; i--) {
				if (request.getBattleTime() >= spiritBattleFailpoint[i][0]) {
					pointAdd = spiritBattleFailpoint[i][1];
					break;
				}
			}
		}

		List<RewardInfo> reward = PlayerHelper.addResources(player, Asset.SpiritBattlePoint.ID, pointAdd, OpType.LingPoBattle);
		resp.addAllRewards(reward);
		lingPoBattle.battleEnd();
		return 0;
	}

	@Override
	public int getType() {
		return DungeonTypeEnum.LingPo.getId();
	}

}
