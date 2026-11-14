package cn.game.games.net.game.gm.command;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.gm.AbstractGm;
import cn.game.games.net.game.gm.GmAction;
import cn.game.games.net.game.gm.GmGroup;
import cn.game.games.net.game.gm.GmParam;
import cn.game.games.net.game.gm.GmScope;
import cn.game.games.net.game.gm.GmSecurityTier;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.RewardMsg.RewardPush_55000501;

/**
 * 英雄与招募调试GM指令分组
 *
 * @author shuyongqiang
 */
@Component
@GmGroup(name = "hero", description = "英雄与招募调试指令")
public class HeroGm extends AbstractGm {

	@GmAction(name = "hero", description = "发放英雄或资源", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void hero(Player player,
			@GmParam(description = "英雄/资源配置ID", required = false, defaultValue = "0") int id,
			@GmParam(description = "数量", required = false, defaultValue = "0") int count) {
		List<RewardInfo> resources = PlayerHelper.addResources(player, id, count, OpType.Test);
		PlayerHelper.sendProtocol(player.getPlayerId(), RewardPush_55000501.newBuilder().addAllRewards(resources).build());
	}

	@GmAction(name = "herolv", description = "设置指定配置ID英雄等级", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void herolv(Player player,
			@GmParam(description = "英雄配置ID", required = false, defaultValue = "0") int id,
			@GmParam(description = "目标等级", required = false, defaultValue = "0") int level) {
		int size = player.getHeroModule().list().size();
		for (int i = 0; i < size; i++) {
			var hero = player.getHeroModule().list().stream().toList().get(i);
			if (hero.getConfigId() == id) {
				hero.setLevel(level);
			}
		}
	}

	@GmAction(name = "qingshen", description = "刷新请神/英雄招募池", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void qingshen(Player player) {
		player.getDrawModule().getHeroRecruit().refresh();
	}
}