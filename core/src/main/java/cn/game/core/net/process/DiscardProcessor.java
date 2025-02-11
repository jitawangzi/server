package cn.game.core.net.process;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;

public class DiscardProcessor extends AbstractProcessor {


	public DiscardProcessor() {
	}

	@Override
	public void process(final NetClient netClient, final IProtocol protocol) {
		log.info("discard msg[{}] from {}", "0x" + Integer.toHexString(protocol.getMsgID()), netClient);
	}

}
