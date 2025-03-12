package cn.game.core.net.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface RPCService<T> {

	public static final Logger log = LoggerFactory.getLogger(RPCService.class);

	void init();

//	void start();

	boolean shutdown();

}
