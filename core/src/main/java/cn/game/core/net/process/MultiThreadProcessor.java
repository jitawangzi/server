package cn.game.core.net.process;



import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;

public class MultiThreadProcessor extends AbstractProcessor {

	private static ExecutorService executorService = Executors.newFixedThreadPool(64);


	public MultiThreadProcessor() {
	}

	@Override
	public void process(final NetClient netClient, final IProtocol<?> protocol) {
		executorService.execute(() -> super.process(netClient, protocol));
	}

	@Override
	public void process(Runnable task) {
		executorService.execute(task);
	}

}
