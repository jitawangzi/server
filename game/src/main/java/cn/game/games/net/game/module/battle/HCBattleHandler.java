package cn.game.games.net.game.module.battle;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.generated.config.HCBattleConfig;
import cn.game.protocol.generated.manager.HCBattleManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;

/**    
 * HCBattle表的战斗
 * @date 2024年5月29日 上午10:32:23
 * @author SYQ
 */
public abstract class HCBattleHandler implements IBattleHandler {
	@Override
	public int check(Player player, int type, int dungeonId) {

		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		HCBattleConfig battleConfig = HCBattleManager.instance().get(dungeonId);
		if (battleConfig.preBattle > 0 && !chapterModule.isBattlePass(battleConfig.preBattle)) {
			return ErrorMsgEnum.BattleLevel_pre.getId();
		}
		if (!chapterModule.checkChapterTimes(dungeonId)) {
			return ErrorMsgEnum.times_limit.getId();
		}

		if (!PlayerHelper.delResources(player, battleConfig.cost, OpType.BattleStart)) {
			return ErrorMsgEnum.resource_not_enough.getId();
		}
		return 0;
	}
}
