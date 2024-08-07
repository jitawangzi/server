package cn.game.games.net.game.module.battle.impl;

import java.util.List;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.ResultObject;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.battle.ChapterModule;
import cn.game.games.net.game.module.battle.LingPoBattle;
import cn.game.games.net.game.module.battle.XiYouBattleHandler;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndResponse_13000004.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**    
 * 失落真经
 * 2024年8月7日 上午11:27:41
 * @author SYQ
 */
public class ShiLuoZhenJingImpl extends XiYouBattleHandler {

	@Override
	public int getType() {
		return DungeonTypeEnum.ShiLuoZhenJing.getId();
	}

	@Override
	public int battleStart(long playerId, int type, int dungeonId, int id, int lineupId, long uid) {

		Player player = PlayerManager.getInstance().getPlayer(playerId);

		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		LingPoBattle lingPoBattle = chapterModule.getLingPoBattle();
		if (dungeonId != lingPoBattle.getBattleId()) {
			return ErrorMsgEnum.request_parameter_error.getId();
		}

		return 0;
	}

	@Override
	public int battleEnd(long playerId, BattleFieldEndRequest_13000003 request, Builder resp) {
		return 0;
	}

	@Override
	public ResultObject<List<RewardInfo>> quickEnd(long playerId, int typeId) {
		return null;
	}

}
