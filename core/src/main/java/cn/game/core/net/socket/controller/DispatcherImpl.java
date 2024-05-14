package cn.game.core.net.socket.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.core.net.socket.handler.Handler;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerErrorPush_01000099;
import cn.game.util.Config;
import cn.game.util.HexUtil;

@Component
public class DispatcherImpl implements Dispatcher {
	protected Logger log = LoggerFactory.getLogger(BaseHandler.class);
	private static final Map<Integer, Handler> MODULE_HANDLERS = new HashMap<Integer, Handler>();

	@Override
	public void put(int module, Handler handler) {
		if (handler != null) {
			if (MODULE_HANDLERS.containsKey(module)) {
				throw new RuntimeException(String.format("Error: duplicated key [%d]", module));
			}
			MODULE_HANDLERS.put(module, handler);
		}
	}
	@Override
	public void dispatch(NetClient client, IProtocol protocol) {

		int module = protocol.getMsgID() >> 24;
		if (Config.isModuleDisabled(module)) {
			client.sendProtocol(PlayerErrorPush_01000099.newBuilder().setError("该功能暂不可用").build(), ErrorMsgEnum.unknown.getId());
			return;
		}
		Handler handler = MODULE_HANDLERS.get(module);
		if (handler != null) {
			handler.dispatch(client, protocol);
			return;
		}
		log.error(String.format("No handler for module [%s] msgID[%s]", Integer.toHexString(module),
				HexUtil.toHexString(protocol.getMsgID())));
	}
}
