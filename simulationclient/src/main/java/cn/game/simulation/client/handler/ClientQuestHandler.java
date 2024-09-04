package cn.game.simulation.client.handler;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.protocol.protobuf.PbProtocol;

@Component
public class ClientQuestHandler extends BaseHandler {

	@Override
	protected int getModule() {
		return 0x20;
	}

	@Override
	protected void inititialize() {

		putInvoker(PbProtocol.QuestGroupPush_20100008, this::groupPush);
		putInvoker(PbProtocol.QuestPush_20200008, this::missionPush);
		putInvoker(PbProtocol.QuestListResponse_20000002, this::missionPush);
	}
	protected void groupPush(NetClient client, Object message) {
		
	}
	protected void missionPush(NetClient client, Object message) {

	}

}
