package cn.game.games.net.game.handler;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerErrorPush_01000099;

public abstract class GameBaseHandler extends BaseHandler {

	/** 
	 * 对应的业务功能,主要用来判断功能是否开启
	 * @return
	 */
	protected InitialUI getInitialUI() {
		return null;
	}
	@Override
	public boolean checkExt(NetClient client, IProtocol<?> protocol) {
		long playerId = client.getPlayerId();
		if (playerId <= 0) {
			return true;
		}
		if (getInitialUI() == null) {
			return true;
		}
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		if (player != null) {
			if (player.isIslogouting()) {
				client.sendProtocol(PlayerErrorPush_01000099.getDefaultInstance(), ErrorMsgEnum.player_logouting.getId());
				return false;
			}
			if (!player.isFuncOpen(getInitialUI())) {
				client.sendProtocol(PlayerErrorPush_01000099.getDefaultInstance(), ErrorMsgEnum.func_not_open.getId());
				return false;
			}
		}
		return true;
	}

}
