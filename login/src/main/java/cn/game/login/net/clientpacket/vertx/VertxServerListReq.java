package cn.game.login.net.clientpacket.vertx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.InvalidProtocolBufferException;

import cn.game.core.base.ActiveServerListManager;
import cn.game.core.base.ServerContext;
import cn.game.core.base.ServerList;
import cn.game.core.base.ServerListManager;
import cn.game.core.cache.CacheType;
import cn.game.login.cache.entity.User;
import cn.game.protocol.custom.ServerItem;
import cn.game.protocol.protobuf.Account.AccountErrorCode;
import cn.game.protocol.protobuf.Account.AccountServerList;
import cn.game.protocol.protobuf.Account.AccountServerListResponse;
import cn.game.protocol.protobuf.Account.HttpResult;
import cn.game.protocol.protobuf.Account.ServerInfo;
import cn.game.util.RedissonUtil;
import cn.game.util.ServerType;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class VertxServerListReq implements Handler<RoutingContext> {

	protected static final Logger log = LoggerFactory.getLogger(VertxServerListReq.class);

	@Override
	public void handle(RoutingContext context) {
//		ServerListResp resp = new ServerListResp();
//		HttpServerRequest request = context.request();

//		JsonObject bodyAsJson = context.getBodyAsJson();
//		String passportSessionId = bodyAsJson.getString("passport_session_id");
//		log.info("服务器列表，sessionId: " + passportSessionId);
		HttpServerResponse response = context.response().putHeader("content-type", "application/octet-stream");
		byte[] bytes = context.getBody().getBytes();
		AccountServerList from = null;
		try {
			from = AccountServerList.parseFrom(bytes);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		String passportSessionId = from.getPassportSessionId();
		AccountServerListResponse.Builder resp = AccountServerListResponse.newBuilder();
		// 查询用户
		RedissonUtil.getAndRunAsync(CacheType.PASSPORT_SESSION.key(passportSessionId), retU -> {
			if (StringUtils.isEmpty((String) retU)) {
				HttpResult httpResult = HttpResult.newBuilder().setErrorMsg("可能未登陆")
						.setErrorCode(AccountErrorCode.PASSPORT_SESSION_ERROR).build();
				response.end(Buffer.buffer(resp.setResult(httpResult).build().toByteArray()));
				return;
			}
			User u = JSON.parseObject((String) retU, User.class);
			RedissonUtil.getAndRunAsync(CacheType.PLAYER_SERVER_ID.key(u.getId()), serverId -> {
				String myServerId = (String) serverId;
				Collection<ServerList> serversList = ServerListManager.getInstance().getServerList();
				Collection<String> activeServerSet = ActiveServerListManager.getInstance()
						.getServerSet(ServerType.Game);

				List<ServerInfo> serverItems = new ArrayList<>();
				List<ServerItem> myServerItems = new ArrayList<>();
				List<ServerList> myServerList = new ArrayList<>();
				if (u.getServers() != null && u.getServers().length() > 0) {
					String[] split = u.getServers().split(",");
					for (String string : split) {
//						if (type != Integer.parseInt(string)) {
//							continue;
//						}
//						myServerList.add();
					}
				}
				ServerInfo item;

//				java.util.Collections.sort(serversList);
				for (ServerList server : serversList) {
//					if (type != server.getType()) {
//						continue;
//					}
					int status = server.getStatus();
					if (!activeServerSet.contains(server.getServerId())) {
						if (ServerContext.getInstance().getRunMode().isTest()) {
							status = ServerList.STATUS_SHUTDOWN;
						} else {
							continue;
						}
					}
					boolean isGm = false;
					if (u != null) {
						// 非gm跳过不开放的服务器
//						if (u.getIsGm() == 0 && server.getStatus() != ServerList.STATUS_RUN) {
//							continue;
//						}
						isGm = u.getIsGm();
					}
					item = formatProto(server, status, isGm);
					serverItems.add(item);
					// TODO我在哪个server
				}

				if (myServerList != null) {
					for (ServerList serverList : myServerList) {
						myServerItems.add(format(serverList, true));
					}
				}
				response.end(Buffer.buffer(resp.addAllServers(serverItems).build().toByteArray()));
			}); 
		});
	}

	private ServerItem format(ServerList server, boolean isGm) {
		ServerItem item = new ServerItem();
		item.setServerId(server.getServerId());
		item.setName(server.getName());
		item.setIp(server.getIp());
//		item.setInternalIp(server.getScenseInternalIp());
		item.setPort(server.getPort());
//		item.setInternalPort(server.getScenseInternalPort());
		item.setStatus(server.getStatus());

		switch (server.getStatus()) {
		// 开启
		case ServerList.STATUS_RUN:
			// TODO 需要区分4种状态

			item.setVisible(1);
			item.setCanEnter(1);
				item.setName(item.getName());
			item.setNameColor(0x00ff00);
			break;
		// 维护
		case ServerList.STATUS_MAINTANCE:
			item.setVisible(1);
			if (isGm)
				item.setCanEnter(1);
			else
					item.setCanEnter(0);

				item.setName(item.getName());
			item.setNameColor(0xefefef);
			break;

		// 新服
		case ServerList.STATUS_NEW_SERVER:
			if (isGm)
				item.setVisible(1);
			else
				item.setVisible(0);

			if (isGm)
				item.setCanEnter(1);
			else
				item.setCanEnter(0);

				item.setName(item.getName());
			item.setNameColor(0xffffff);
			break;

		// 停机
		case ServerList.STATUS_SHUTDOWN:
			item.setVisible(1);
			item.setCanEnter(0);
				item.setName(item.getName());
			item.setNameColor(0xefefefe);
			break;
		default:

		}
		return item;
	}

	private ServerInfo formatProto(ServerList server, int status, boolean isGmint) {
		ServerInfo.Builder item = ServerInfo.newBuilder();
		item.setServerId(server.getServerId());
		item.setName(server.getName());
		item.setIp(server.getIp());
//		item.setInternalIp(server.getScenseInternalIp());
		item.setPort(server.getPort());
//		item.setInternalPort(server.getScenseInternalPort());
		item.setStatus(status);
		item.setVersion(server.getVersion());

		switch (server.getStatus()) {

		// 开启，
		case ServerList.STATUS_RUN:
			// TODO 需要区分4种状态

//			item.setVisible(1);
//			item.setCanEnter(1);
//			item.setNameColor(0x00ff00);
			break;
		// 维护，可能启动中，老的版本服务器。
		case ServerList.STATUS_MAINTANCE:
//			item.setVisible(1);
//			if (isGm)
//				item.setCanEnter(1);
//			else
//				item.setCanEnter(0);

//			item.setNameColor(0xefefef);
			break;

		// 新服
		case ServerList.STATUS_NEW_SERVER:
//			if (isGm)
//				item.setVisible(1);
//			else
//				item.setVisible(0);
//
//			if (isGm)
//				item.setCanEnter(1);
//			else
//				item.setCanEnter(0);

//			item.setNameColor(0xffffff);
			break;

		// 停机，服务器关了，一般不发给客户端
		case ServerList.STATUS_SHUTDOWN:
//			item.setVisible(1);
//			item.setCanEnter(0);
			item.setName(item.getName());
			break;
		default:
		}
		return item.build();
	}
}
