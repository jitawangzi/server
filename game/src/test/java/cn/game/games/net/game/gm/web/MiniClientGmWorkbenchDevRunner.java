package cn.game.games.net.game.gm.web;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.game.core.base.ServerContext;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.socket.controller.DispatcherImpl;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.core.event.server.ServerEventBus;
import cn.game.games.net.game.gm.GmDispatcher;
import cn.game.games.net.game.gm.RequestTraceService;
import cn.game.games.net.game.gm.command.FriendGm;
import cn.game.games.net.game.gm.command.MailGm;
import cn.game.games.net.game.gm.command.PlayerGm;
import cn.game.games.net.game.gm.command.QuestGm;
import cn.game.games.cache.entity.Friend;
import cn.game.games.cache.entity.FriendApplication;
import cn.game.games.cache.entity.Mail;
import cn.game.games.net.game.module.friend.FriendModule;
import cn.game.games.net.game.module.mail.MailModule;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.gm.proto.ProtocolSimulationService;
import cn.game.games.net.game.gm.proto.VirtualGameClient;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PlayerMsg.PlayerNameRequest_01000011;
import cn.game.protocol.protobuf.PlayerMsg.PlayerNameResponse_01000012;
import cn.game.protocol.protobuf.PlayerMsg.PlayerResetPush_01100016;
import cn.game.util.ObjUtil;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;

/**
 * 本地开发与浏览器验收专用的 GmWebConsoleServer 独立启动器
 *
 * @author shuyongqiang
 */
public class MiniClientGmWorkbenchDevRunner {

	public static final long TEST_PLAYER_ID = 10001L;

	public static void main(String[] args) {
		try {
			System.setProperty(ServerContext.SERVER_RUN_MODE, "DEV");
			ServerContext.getInstance().setRunMode();
			if (ServerContext.getInstance().getEventBus() == null) {
				ServerContext.getInstance().setEventBus(ServerEventBus.getInstance());
			}
			cn.game.protocol.generated.config.GlobalConst.FriendMax = 50;

			ClassManager.getInstance().init();

			GmDispatcher dispatcher = GmDispatcher.getInstance();
			if (!dispatcher.hasCommand("player.time.status")) {
				dispatcher.registerBean(new PlayerGm());
			}
			if (!dispatcher.hasCommand("friend.add")) {
				dispatcher.registerBean(new FriendGm());
			}
			if (!dispatcher.hasCommand("mail.send")) {
				dispatcher.registerBean(new MailGm());
			}
			if (!dispatcher.hasCommand("quest.quest")) {
				dispatcher.registerBean(new QuestGm());
			}

			// 初始化一个模拟在线玩家
			PlayerData data = new PlayerData();
			ObjUtil.setDefaultValue(data);
			data.setPlayerId(TEST_PLAYER_ID);
			data.setUid(TEST_PLAYER_ID);
			data.setName("BrowserDevPlayer");
			data.setServerId("game_dev_1");
			data.setLevel(45);

			Player player = new Player(data);
			player.setOnline(true);
			player.setIslogouting(false);
			player.setGameClient(new VirtualGameClient(TEST_PLAYER_ID));

			// 初始化初始好友、邮件与任务测试数据
			try {
				FriendModule friendModule = player.getModule(FriendModule.class);
				if (friendModule != null) {
					friendModule.addFriend(10002L, "game_dev_1", Friend.FRIEND);
					friendModule.addApplication(FriendApplication.valueOf(TEST_PLAYER_ID, 10003L, "game_dev_1"));
				}
				MailModule mailModule = player.getMailModule();
				if (mailModule != null) {
					Mail initMail = new Mail();
					initMail.setId(1001L);
					initMail.setPlayerId(TEST_PLAYER_ID);
					initMail.setMailId(0);
					initMail.setTitle("欢迎进入GM工作台");
					initMail.setContent("欢迎使用MiniClient工作台，已为您发放初始测试道具！");
					initMail.setSender("系统管理员");
					initMail.setType((byte) 1);
					initMail.setAttachmentList(java.util.List.of(new Goods(100002, 8888)));
					initMail.setCreateTime((int) (System.currentTimeMillis() / 1000));
					mailModule.sendOnline(initMail, false);
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}

			registerPlayer(player);

			// 注册模拟 Dispatcher
			DispatcherImpl mockDispatcher = new DispatcherImpl() {
				@Override
				public void dispatch(NetClient client, IProtocol protocol) {
					Object rawMsg = protocol.getData();
					if (rawMsg instanceof PlayerNameRequest_01000011 nameReq) {
						String newName = nameReq.getName();
						if (newName != null && !newName.isEmpty()) {
							player.getData().setName(newName);
						}
						PlayerNameResponse_01000012 resp = PlayerNameResponse_01000012.newBuilder().build();
						client.sendProtocol(resp);
						player.getGameClient().sendProtocol(PlayerResetPush_01100016.newBuilder().setType(1).build());
						RequestTraceService.recordResourceEvent(TEST_PLAYER_ID, 1, 100L, true, "Name changed to " + newName);
					} else {
						int reqMsgId = protocol.getMsgID();
						int respMsgId = reqMsgId + 1;
						Parser<?> parser = PbProtocol.parsersMap.get(respMsgId);
						if (parser != null) {
							try {
								String respClassName = PbProtocol.idNameMap.get(respMsgId);
								Class<?> respClass = Class.forName("cn.game.protocol.protobuf." + respClassName.substring(0, respClassName.indexOf('_') - 3) + "Msg$" + respClassName);
								Method getDefault = respClass.getMethod("getDefaultInstance");
								Message defaultResp = (Message) getDefault.invoke(null);
								client.sendProtocol(defaultResp);
							} catch (Throwable ignored) {
								client.sendProtocol(PlayerNameResponse_01000012.newBuilder().build());
							}
						}
						RequestTraceService.recordResourceEvent(TEST_PLAYER_ID, 1, 50L, true, "Simulated protocol executed: " + protocol.getMsgID());
					}
				}
			};

			Field field = ProtocolSimulationService.class.getDeclaredField("explicitDispatcher");
			field.setAccessible(true);
			field.set(ProtocolSimulationService.getInstance(), mockDispatcher);

			GmWebConsoleServer server = GmWebConsoleServer.getInstance();
			int port = server.start("127.0.0.1", 17010).toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);

			System.out.println("=================================================================");
			System.out.println("  GM Web Console 已在 http://localhost:" + port + "/gm-console 成功启动！");
			System.out.println("=================================================================");

			Thread.sleep(Long.MAX_VALUE);
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}

	private static void registerPlayer(Player player) {
		PlayerManager.getInstance().initAdd(player);
	}
}
