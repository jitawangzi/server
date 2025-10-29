package cn.game.games.net.game.handler;

import cn.game.core.exception.LogicException;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.manual.ErrorMsgEnum;

public abstract class GmBaseHandler extends GameBaseHandler {

	@Override
	public boolean checkExt(NetClient client, IProtocol<?> protocol) {
		long playerId = client.getPlayerId();
		if (playerId <= 0) {
			return true;
		}
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		if (player != null) {
			if (!player.getData().getIsGm()) {
				throw new LogicException(ErrorMsgEnum.gm_not) ; 
			}
		}
		return true;
	}
}
