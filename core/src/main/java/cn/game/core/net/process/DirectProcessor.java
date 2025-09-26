package cn.game.core.net.process;

import java.util.concurrent.Callable;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;

public class DirectProcessor extends AbstractProcessor {

	public DirectProcessor() {
	}
	
	
	@Override
	public void process(long objectId, NetClient netClient, IProtocol<?> protocol) {
		super.process(netClient, protocol); 
	}
	
	@Override
	public void process(Runnable task) {
		task.run(); 
	}
	
	@Override
	public void process(long objectId, Runnable task) {
		task.run();
	}
	
	@Override
	public void process(long objectId, Runnable task,boolean fast) {
		task.run();
	}
	@Override
	public <T> T process(long objectId, Callable<T> supplier) {
		try {
			return supplier.call();
		} catch (Exception e) {
			throw new RuntimeException("Error processing callable for objectId: " + objectId, e);
		} 
	}
}
