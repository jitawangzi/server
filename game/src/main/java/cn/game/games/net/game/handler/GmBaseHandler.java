package cn.game.games.net.game.handler;

import cn.game.core.base.ServerContext;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.gm.GmSecurityPolicy;
import cn.game.games.net.game.manager.PlayerManager;

/**
 * 0x77后台协议处理器基类，按统一GmSecurityPolicy校验RunMode与玩家isGm权限
 *
 * @author shuyongqiang
 */
public abstract class GmBaseHandler extends GameBaseHandler {

	@Override
	public boolean checkExt(NetClient client, IProtocol<?> protocol) {
		long playerId = client.getPlayerId();
		Player player = playerId > 0 ? PlayerManager.getInstance().getPlayer(playerId) : null;
		return GmSecurityPolicy.check0x77Access(player, ServerContext.getInstance().getRunMode());
	}
}