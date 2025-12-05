package cn.game.login.net.clientpacket.vertx;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.excel.util.DateUtils;
import com.google.protobuf.InvalidProtocolBufferException;

import cn.game.core.base.ActiveServerListManager;
import cn.game.core.base.ServerContext;
import cn.game.core.base.ServerList;
import cn.game.core.base.ServerListManager;
import cn.game.core.base.VirtualServerRegistry.VirtualServerView;
import cn.game.core.zookeeper.server.ValidServerService;
import cn.game.login.cache.entity.User;
import cn.game.protocol.protobuf.Account.AccountErrorCode;
import cn.game.protocol.protobuf.Account.AccountLogicServerList;
import cn.game.protocol.protobuf.Account.AccountLogicServerListResponse;
import cn.game.protocol.protobuf.Account.HttpResult;
import cn.game.protocol.protobuf.Account.LogicServerInfo;
import cn.game.util.DateUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;

@VertxRoute("/account/logic_server_list")
public class VertxLogicServerListReq implements BaseVertxHandler {

	protected static final Logger log = LoggerFactory.getLogger(VertxLogicServerListReq.class);

	@Override
	public void handle(RoutingContext context) {
		HttpServerResponse response = context.response().putHeader("content-type", "application/octet-stream");
		SocketAddress remoteAddress = context.request().remoteAddress();
		byte[] bytes = context.body().buffer().getBytes();
		AccountLogicServerList from = null;
		try {
			from = AccountLogicServerList.parseFrom(bytes);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		String passportSessionId = from.getPassportSessionId();
		int page = from.getPage(); 
		int pageSize = from.getPageSize(); 
		AccountLogicServerListResponse.Builder resp = AccountLogicServerListResponse.newBuilder();
		// 查询用户
		User user = UserHelper.getUserBySessionId(passportSessionId); 
		if (user == null) {
			HttpResult httpResult = HttpResult.newBuilder()
					.setErrorMsg("可能未登陆")
					.setErrorCode(AccountErrorCode.PASSPORT_SESSION_ERROR)
					.build();
			response.end(Buffer.buffer(resp.setResult(httpResult).build().toByteArray()));
			return;
		}
		
		ValidServerService validGameService = ServerContext.getInstance().getValidGameService(); 
		List<VirtualServerView> validServers = validGameService.getValidServerList(); 
		List<VirtualServerView> subList = validServers.subList((page - 1) * pageSize, pageSize);
		
		int status = ServerList.STATUS_MAINTANCE; 
		Collection<ServerList> serversList = ServerListManager.getInstance().getServerList();
		for (ServerList serverList : serversList) {
			if (serverList.getStatus() == ServerList.STATUS_RUN) {
				status = ServerList.STATUS_RUN ; 
				break ; 
			}
		}

		LocalDateTime now = LocalDateTime.now(); 
		LogicServerInfo.Builder serverBuilder = LogicServerInfo.newBuilder(); 
		for (VirtualServerView virtualServerView : subList) {
			serverBuilder.setIsNew(DateUtil.isSameDay(virtualServerView.getOpenTime(), now)); 
			serverBuilder.setSeq(virtualServerView.getSeq()); 
			serverBuilder.setName(virtualServerView.getName()); 
			serverBuilder.setOpenTime(DateUtil.toEpochSecond(virtualServerView.getOpenTime())); 
			serverBuilder.setServerId(virtualServerView.getID()); 
			serverBuilder.setStatus(status); 
			
			resp.addLogicServerList(serverBuilder.build()); 
		}
		response.end(Buffer.buffer(resp.build().toByteArray()));
	}
}
