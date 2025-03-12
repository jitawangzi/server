package cn.game.core.net.mq;

import cn.game.core.net.rpc.RPCService;

public class RocketMQRPCService<T> implements RPCService<T> {
//	private static final Logger log = LoggerFactory.getLogger(VertxRPCService.class);

	private T wrappedService;

	private String serverId;

	public RocketMQRPCService(T wrappedService) {
		this.wrappedService = wrappedService;
	}
	public RocketMQRPCService(T wrappedService, String serverId) {
		this.wrappedService = wrappedService;
		this.serverId = serverId;
	}

	@Override
	public void init() {
	}

	@Override
	public boolean shutdown() {
		
		log.info("{} start shutdown.... ",this.getClass().getSimpleName());
		return true;
	}

}
