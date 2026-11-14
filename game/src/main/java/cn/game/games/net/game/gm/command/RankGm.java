package cn.game.games.net.game.gm.command;

import org.springframework.stereotype.Component;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.gm.AbstractGm;
import cn.game.games.net.game.gm.GmAction;
import cn.game.games.net.game.gm.GmGroup;
import cn.game.games.net.game.gm.GmParam;
import cn.game.games.net.game.gm.GmScope;
import cn.game.games.net.game.gm.GmSecurityTier;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.protocol.generated.enume.RankType;

/**
 * 排行榜调试GM指令分组
 *
 * @author shuyongqiang
 */
@Component
@GmGroup(name = "rank", description = "排行榜调试指令")
public class RankGm extends AbstractGm {

	@GmAction(name = "rank", description = "设置玩家指定排行榜分数", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void rank(Player player,
			@GmParam(description = "排行榜类型ID", required = false, defaultValue = "0") int rankType,
			@GmParam(description = "目标分数", required = false, defaultValue = "0") int score) {
		RankService.getInstance().setScoreAsync(player.getServerId(), RankType.get(rankType), player.getPlayerId(), score);
	}

	@GmAction(name = "rankds", description = "设置大圣擂台赛季与日榜分数", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void rankds(Player player,
			@GmParam(description = "目标分数", required = false, defaultValue = "0") int score) {
		RankService.getInstance().setScoreAsync(player.getServerId(), RankType.DaShengLeiTaiSeason, player.getPlayerId(), score);
		RankService.getInstance().setScoreAsync(player.getServerId(), RankType.DaShengLeiTaiDay, player.getPlayerId(), score);
	}

	@GmAction(name = "rankreward", description = "触发指定排行榜结算发奖", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void rankreward(Player player,
			@GmParam(description = "排行榜类型ID", required = false, defaultValue = "0") int rankType) {
		RankService.getInstance().rewardGm(rankType);
	}
}