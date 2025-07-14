package cn.game.games.net.game.handler;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.manager.PlayerManager;

public abstract class GameBaseHandler extends BaseHandler {

	@Override
	public boolean checkFunctionOpen(NetClient client, IProtocol<?> protocol) {
		long playerId = client.getPlayerId();
		if (playerId <= 0) {
			return true;
		}
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		return player != null && player.isFuncOpen(getInitialUI());
	}

}
