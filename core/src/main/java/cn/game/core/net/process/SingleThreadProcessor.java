package cn.game.core.net.process;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.task.TaskManager;

public class SingleThreadProcessor extends AbstractProcessor {

	public SingleThreadProcessor() {
	}

	@Override
	public void process(final NetClient netClient, final IProtocol protocol) {
		TaskManager.getInstance().addMainTask(() -> {
			super.process(netClient, protocol);
		});
	}

}
