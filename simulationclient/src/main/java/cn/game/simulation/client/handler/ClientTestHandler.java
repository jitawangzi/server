package cn.game.simulation.client.handler;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;

/**
 * 用户处理器
 */
@Component
public class ClientTestHandler extends BaseHandler {

	@Override
	protected int getModule() {
		return 0x7f;
	}

	@Override
	protected void inititialize() {

//		putInvoker(PbProtocol.TestAddItemResponse_7f000009, new Invoker() {
//			@Override
//			public void invoke(NetClient client, Object message) throws InvalidProtocolBufferException {
//				addItemResp(client, message);
//			}
//
//		});
	}

	protected void addItemResp(NetClient client, Object message) {
		// TODO Auto-generated method stub

	}
}
