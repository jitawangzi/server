package cn.game.core.net.process;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;

public interface Processor {
	public void process(NetClient gameClient,IProtocol protocol) ; 
}
