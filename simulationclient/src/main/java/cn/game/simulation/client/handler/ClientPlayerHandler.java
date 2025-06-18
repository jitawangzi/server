package cn.game.simulation.client.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PlayerMsg;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerErrorPush_01000099;
import cn.game.protocol.protobuf.PlayerMsg.PlayerLoginResponse_01000002;
import cn.game.simulation.client.Client;


/**
 * 用户处理器
 */
@Component
public class ClientPlayerHandler extends BaseHandler {
	protected Logger systemOutLog = LoggerFactory.getLogger("SystemOut");

	@Override
	protected int getModule() {
		return 0x01;
	}

	@Override
	protected void inititialize() {

		putInvoker(PbProtocol.PlayerLoginResponse_01000002,this::loginResp);
//		putInvoker(PbProtocol.PlayerCreateResponse_01000004, this::createPlayerResp) ; 
		putInvoker(PbProtocol.PlayerHeartbeatResponse_01000006, this::heart) ; 
//		putInvoker(PbProtocol.ServerLoginResponse_01000052, this::pcLoginResp);
//		putInvoker(PbProtocol.PlayerCreateResponse_01000054, this::pcCreateResp);
//		putInvoker(PbProtocol.PlayerLoginResponse_01000056, this::chooseResp);
		putInvoker(PbProtocol.PlayerErrorPush_01000099, this::error);
		putInvoker(PbProtocol.PlayerLogoutPush_01100030, this::logout);
	}
	protected void notImpl(NetClient client, Object message) {

	}

	protected void logout(NetClient client, Object message) {
		System.exit(0);
	}
	protected void error(NetClient client, Object message) {

		PlayerErrorPush_01000099 err = (PlayerErrorPush_01000099) message;
		log.error("发生了错误");

	}
	protected void pcLoginResp(NetClient client, Object message) {
		Client client2 = (Client) client;

		/*		ServerLoginResponse_01000052 resp = (ServerLoginResponse_01000052) message;
				List<PlayerArchiveInfo> archivesList = resp.getArchivesList();
				if (archivesList.isEmpty()) {
					PlayerCreateRequest_01000053.Builder create = PlayerCreateRequest_01000053.newBuilder();
		//			create.setName(System.currentTimeMillis() + "");
					create.setName("存档");
					create.setSessionId(client2.getPassportSessionId() + "");
					client.sendProtocol(create.build());
				} else {
					client2.setArchivesList(archivesList);
					PlayerLoginRequest_01000055.Builder choose = PlayerLoginRequest_01000055.newBuilder();
					choose.setId(Rnd.randomOne(archivesList).getId());
					choose.setSessionId(client2.getPassportSessionId() + "");
					client.sendProtocol(choose.build());
				}*/
	}
	protected void pcCreateResp(NetClient client, Object message) {
		//		PlayerCreateResponse_01000054 resp = (PlayerCreateResponse_01000054) message;
		Client client2 = (Client) client;

//		PlayerArchiveInfo archive = resp.getArchive();
//
//		PCPlayerChooseRequest_01000055.Builder choose = PCPlayerChooseRequest_01000055.newBuilder();
//		choose.setId(archive.getId());
//		choose.setSessionId(client2.getPassportSessionId() + "");
//		client.sendProtocol(choose.build());

//		PlayerAllInfo playerInfo = resp.getPlayerInfo();

//		client2.afterLogin(playerInfo);
		System.out.println("PC角色登陆成功: ");

	}
	protected void chooseResp(NetClient client, Object message) {
		//		PlayerLoginResponse_01000056 resp = (PlayerLoginResponse_01000056) message;
		//		PlayerAllInfo playerInfo = resp.getPlayerInfo();
		//		Client client2 = (Client) client;

		//		client2.afterLogin(playerInfo);
		System.out.println("PC角色登陆成功: ");

	}
	protected void heart(NetClient client, Object message) {
		
	}
	
	protected void loginResp(NetClient client, Object message) {
		PlayerMsg.PlayerLoginResponse_01000002 login = (PlayerLoginResponse_01000002) message;
		Client client2 = (Client) client;

//		if (!login.hasInfo()) {
		if (false) {
			//			cn.game.protocol.protobuf.PlayerMsg.PlayerCreateRequest_01000003.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerCreateRequest_01000003
			//					.newBuilder();

			//			builder.setSessionId(((Client) client).getPassportSessionId() + "");
			////			builder.setServerId("SYQ");
			//			builder.setHead(3);
			//			builder.setName(((Client) client).name);
			//			builder.setIsMan(false);

			System.out.println("没有角色，准备创建: ");

			//			client.sendProtocol(builder.build());
		} else {
			PlayerAllInfo info = login.getInfo();
			client2.afterLogin(info);
//			client2.setInit();
			systemOutLog.info("角色登陆成功: ");
//			LoggerType.SystemOut.logger.info("角色登陆成功: ");
//			systemOutLog.info(info.getItemsList().toString());
//			systemOutLog.info(info.getAssets().toString());
//			System.out.println("角色登陆成功: ");
//			System.out.println(info.getItemsList());
//			System.out.println(info.getAssets());

		}
		long now = System.currentTimeMillis();
		systemOutLog.info("角色完整登陆耗时: " + (now - client2.startTime));
		systemOutLog.info("");
		systemOutLog.info("登陆服HTTP耗时: " + (client2.startConnectTime - client2.startTime));
		systemOutLog.info("创建WS连接耗时: " + (client2.startLoginTime - client2.startConnectTime));
		systemOutLog.info("WS登陆游戏耗时: " + (now - client2.startLoginTime));

//		System.err.println("角色完整登陆耗时: " + (now - client2.startTime));
//		System.err.println();
//		System.err.println("登陆服HTTP耗时: " + (client2.startConnectTime - client2.startTime));
//		System.err.println("创建WS连接耗时: " + (client2.startLoginTime - client2.startConnectTime));
//		System.err.println("WS登陆游戏耗时: " + (now - client2.startLoginTime));

	}

	public void createPlayerResp(NetClient client, Object message) {
		/*
		PlayerCreateResponse_01000004 create = (PlayerCreateResponse_01000004) message;
		Client client2 = (Client) client;
		
		PlayerAllInfo info = create.getInfo();
		LineupInfo.Builder line = LineupInfo.newBuilder();
		line.setLineupId(1);
		LineupPositionInfo.Builder postions = LineupPositionInfo.newBuilder();
		
		client.setPlayerId(info.getPlayer().getId());
		
		// 发心跳
		//		TaskManager.getInstance().scheduleGeneralAtFixedRate(new Runnable() {
		//			
		//			@Override
		//			public void run() {
		//				client.sendProtocol(PlayerHeartbeatRequest_01000005.getDefaultInstance());
		//			}
		//		}, 20000, 20000)  ; 
		client2.setInit();
		*/}

}
