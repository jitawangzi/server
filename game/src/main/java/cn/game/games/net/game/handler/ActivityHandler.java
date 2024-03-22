package cn.game.games.net.game.handler;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.net.game.manager.ActivityStateManager;
import cn.game.protocol.protobuf.ActivityMsg.ActivityListResponse_11000002;

/**
 * 活动处理器
 */
public class ActivityHandler extends BaseHandler {

	@Override
	protected int getModule() {

		return 0x11;
	}

	@Override
	protected void inititialize() {

//		putInvoker(PbProtocol.ActivityListRequest_11000001, (client, message) -> {
//			list(client, message);
//		});
//		putInvoker(PbProtocol.ActivityInfoRequest_11000003, (client, message) -> {
//			info(client, message);
//		});
	}

	private void list(NetClient client, Object message) {
		ActivityListResponse_11000002.Builder resp = ActivityListResponse_11000002.newBuilder();

		resp.addAllId(ActivityStateManager.getInstance().getShowIds());
		client.sendProtocol(resp);
	}

	private void info(NetClient client, Object message) {
		/*ActivityInfoRequest_11000003 req = (ActivityInfoRequest_11000003) message;
		ActivityInfoResponse_11000004.Builder resp = ActivityInfoResponse_11000004.newBuilder();
		int id = req.getId();
		ActivityBase activityBase = ActivityManager.getInstance().getActivityBase(id, client.getPlayerId());
		if (activityBase != null) {
			ActivityInfo buildActivityInfo = activityBase.buildActivityInfo();
			resp.setActivity(buildActivityInfo);
		}
		client.sendProtocol(resp);*/
	}
}
