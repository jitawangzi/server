package cn.game.core.net.socket.handler;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;

public interface Handler {
	
	public void dispatch(NetClient client, IProtocol<?> protocol);

}
