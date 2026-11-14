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
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.helper.TestHelper;
import cn.game.games.net.game.remote.GameServerInterface;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.RewardMsg.RewardPush_55000501;

/**
 * 道具与资源发放/扣除GM指令分组
 *
 * @author shuyongqiang
 */
@Component
@GmGroup(name = "item", description = "道具与资源调试指令")
public class ItemGm extends AbstractGm {

	@GmAction(name = "item", description = "添加道具或资源", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void item(Player player,
			@GmParam(description = "道具或资源ID", required = false, defaultValue = "0") int id,
			@GmParam(description = "数量", required = false, defaultValue = "0") int count) {
		List<RewardInfo> items = TestHelper.addItems(player, id, count);
		PlayerHelper.sendProtocol(player.getPlayerId(), RewardPush_55000501.newBuilder().addAllRewards(items).build());
	}

	@GmAction(name = "itemdel", description = "扣除指定玩家道具或资源", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void itemdel(Player player,
			@GmParam(description = "目标玩家ID(0代表当前玩家)", required = false, defaultValue = "0") long targetPlayerId,
			@GmParam(description = "道具或资源ID", required = false, defaultValue = "0") int id,
			@GmParam(description = "扣除数量", required = false, defaultValue = "0") int count) {
		long pid = targetPlayerId == 0 ? player.getPlayerId() : targetPlayerId;
		GameServerInterface playerProxy = ServerHelper.getPlayerProxy(pid);
		playerProxy.delResources(pid, id, count);
	}
}