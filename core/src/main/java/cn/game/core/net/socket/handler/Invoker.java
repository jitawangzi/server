package cn.game.core.net.socket.handler;

import cn.game.core.net.client.NetClient;

public interface Invoker {

	void invoke(NetClient client, Object message);
	
}
