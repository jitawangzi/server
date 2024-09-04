package cn.game.simulation.client.handler;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.protocol.generated.config.GameEventActionConfig;
import cn.game.protocol.generated.manager.GameEventActionManager;
import cn.game.protocol.protobuf.GameEventMsg.GameEventStageRequest_23000003;
import cn.game.simulation.client.Client;


@Component
public class ClientGameEventHandler extends BaseHandler {

	@Override
	protected int getModule() {
		return 0x23;
	}

	@Override
	protected void inititialize() {

//		putInvoker(PbProtocol.GameEventTriggerResponse_23000002, this::trigger);
	}
	protected void trigger(NetClient client, Object message) {
		Client c = (Client) client;
//		Message lastRequest = c.getLastRequest();
//		Message curRequest = c.getCurRequest();
		int id = 0;
//		if (curRequest instanceof GameEventTriggerRequest_23000001) {
//			GameEventTriggerRequest_23000001 request = (GameEventTriggerRequest_23000001) curRequest;
//			id = request.getId();
//		}
//		if (curRequest instanceof TestGameEventTriggerRequest_6f000105) {
//			TestGameEventTriggerRequest_6f000105 request = (TestGameEventTriggerRequest_6f000105) curRequest;
//			id = request.getId();
//		}
		int actionCount = 8;
		List<GameEventActionConfig> actions = GameEventActionManager.getInstance().getEventIdList(id);
		if (actions != null) {
//			actionCount = actions.size();
		}
		// 触发事件后，开启各个阶段。 
		for (int i = 0; i < actionCount; i++) {
			GameEventStageRequest_23000003.Builder builder = GameEventStageRequest_23000003.newBuilder();
			builder.setStage(i);
			builder.setId(id);
			client.sendProtocol(builder.build());
		}
	}

}
