package cn.game.core.net.process;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;

public class DiscardProcessor implements Processor {


	public DiscardProcessor() {
	}

	@Override
	public void process(final NetClient netClient, final IProtocol protocol) {
		System.out.println(protocol.getData());
	}

}
