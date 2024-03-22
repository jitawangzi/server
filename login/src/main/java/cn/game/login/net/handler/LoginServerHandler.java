package cn.game.login.net.handler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

import cn.game.core.base.ActiveServerListManager;
import cn.game.core.cache.CacheType;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.login.cache.entity.User;
import cn.game.protocol.manual.OldErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.ServerMsg.GameStatusPublish_7d000017;
import cn.game.protocol.protobuf.ServerMsg.LoginPlayerUidRequest_7d000018;
import cn.game.protocol.protobuf.ServerMsg.LoginPlayerUidResponse_7d000019;
import cn.game.util.RedissonUtil;
import cn.game.util.ServerType;

/**
 * 服务器之间的消息处理器
 */
@Component
public class LoginServerHandler extends BaseHandler {

	@Override
	protected int getModule() {
		return 0x7d;
	}
	@Override
	protected void inititialize() {

		putInvoker(PbProtocol.GameStatusPublish_7d000017, this::gameStatus);
		putInvoker(PbProtocol.LoginPlayerUidRequest_7d000018, this::uid);

	}

	protected void uid(NetClient client, Object message) {
		LoginPlayerUidRequest_7d000018 request = (LoginPlayerUidRequest_7d000018) message;
		String passportSessionId = request.getPassportSessionId();

		// 查询用户
		RedissonUtil.getAndRunAsync(CacheType.PASSPORT_SESSION.key(passportSessionId), retU -> {
			if (StringUtils.isEmpty((String) retU)) {
				client.sendProtocol(LoginPlayerUidResponse_7d000019.getDefaultInstance(),
						OldErrorMsgEnum.session_not_exist.getId());
				return;
			}
			User u = JSON.parseObject((String) retU, User.class);
			client.sendProtocol(LoginPlayerUidResponse_7d000019.newBuilder().setUid(u.getId()));
		});
	}
	protected void gameStatus(NetClient client, Object message) {
		GameStatusPublish_7d000017 request = (GameStatusPublish_7d000017) message;
		String serverId = request.getServerId();
		int onlinePlayerCount = request.getOnlinePlayerCount();
		ActiveServerListManager.getInstance().setPlayerCount(serverId, onlinePlayerCount);
		ActiveServerListManager.getInstance().addServer(serverId, ServerType.Game.name());
	}
}
