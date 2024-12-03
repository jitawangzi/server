package cn.game.games.net.game.module.chat;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.google.protobuf.ProtocolStringList;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.push.PushService;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.BaseMsg.SimplePlayerInfo;
import cn.game.protocol.protobuf.ChatMsg.ChatMessageInfo;
import cn.game.protocol.protobuf.ChatMsg.ChatMessagePush_31010001;
import cn.game.protocol.protobuf.ChatMsg.ChatRequest_31000001;
import cn.game.protocol.protobuf.ChatMsg.ChatResponse_31000002;
import cn.game.protocol.protobuf.ChatMsg.ChatType;
import cn.game.protocol.protobuf.ChatMsg.ServerChatMessagePush_31000010;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.util.DateUtil;
import cn.game.util.ServerType;
import io.vertx.core.Future;

/**
 * 聊天处理器
 */

@Component
public class ChatHandler extends BaseHandler {

	@Override
	protected int getModule() {
		return 0x31;
	}

	@Override
	protected void inititialize() {
		putInvoker(PbProtocol.ChatRequest_31000001, this::chat);
		putInvoker(PbProtocol.ServerChatMessagePush_31000010, this::chatPush);
	}
	
	protected void chatPush(NetClient client, Object message) {

		ServerChatMessagePush_31000010 req = (ServerChatMessagePush_31000010) message;
		ChatResponse_31000002.Builder resp = ChatResponse_31000002.newBuilder();
		ChatType chatType = req.getChatType(); // 聊天类型
		String content = req.getContent();
		String serverId = req.getServerId();
		ProtocolStringList atPlayerIdsList = req.getAtPlayerIdsList();
		SimplePlayerInfo sendPlayer = req.getSendPlayer();
//		String sendServerId = sendPlayer.getServerId();
//		Set<Long> atPlayerIdsSet = atPlayerIdsList.stream().map(Long::valueOf).collect(Collectors.toSet());
		
		ChatMessageInfo atMe = null;
		ChatMessageInfo notAtMe = null;
		ChatMessageInfo.Builder chatBuilder = ChatMessageInfo.newBuilder();
		chatBuilder.setChatType(chatType);
		chatBuilder.setContent(content);
		chatBuilder.setSendPlayer(sendPlayer);
		chatBuilder.setAtMe(true);
		atMe = chatBuilder.build();
		chatBuilder.setAtMe(false);
		notAtMe = chatBuilder.build();

		ChatMessagePush_31010001 atMeMessage = ChatMessagePush_31010001.newBuilder().addMessageInfo(atMe).build();
		ChatMessagePush_31010001 notAtMeMessage = ChatMessagePush_31010001.newBuilder().addMessageInfo(notAtMe).build();

		switch (chatType) {
		case WORLD_CHAT: {
			// 推送给所有在线玩家
//			PushService.getInstance().pushMessage(notAtMeMessage, false, sendPlayer.getServerId());
			PushService.getInstance().pushMessage(notAtMeMessage, false);
//			Collection<Player> players = PlayerManager.getInstance().getAllPlayer().values();
//			for (Player player : players) {
//				if (!player.getData().getServerId().equals(sendServerId)) {
//					continue;
//				}
//				if (atPlayerIdsSet.contains(player.getPlayerId())) {
//					player.getGameClient().sendProtocol(atMeMessage);
//				} else {
//					player.getGameClient().sendProtocol(notAtMeMessage);
//				}
//			}
			break;
		}
		case MARQUEE: {
			PushService.getInstance().pushMessage(notAtMeMessage, false, serverId);
			break;
		}
		case UNINON_CHAT: {

			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + chatType);
		}

	}
	protected void chat(NetClient client, Object message) {

		long sendPlayerId = client.getPlayerId();
		Player sendPlayer = PlayerManager.getInstance().getPlayer(sendPlayerId);
		ChatRequest_31000001 req = (ChatRequest_31000001) message;
		ChatResponse_31000002.Builder resp = ChatResponse_31000002.newBuilder();
		ChatType chatType = req.getChatType(); // 聊天类型
		String content = req.getContent();
		if (StringUtils.isEmpty(content)) {
			client.sendProtocol(resp, ErrorMsgEnum.request_parameter_null.getId());
			return;
		}
		if (PlayerManager.getInstance().isForbidChat(sendPlayerId)){
			client.sendProtocol(resp, ErrorMsgEnum.chat_fail_player_is_forbid.getId());
			return;
		}
//		content = KeywordFilter.getInstance().filter(content);
		ProtocolStringList atPlayerIdsList = req.getAtPlayerIdsList();
		String targetPlayerId = req.getTargetPlayerId();

		Future<Boolean> checkFuture =  PlayerHelper.checkContextData(sendPlayer,content);
		checkFuture.onSuccess( b -> {
			if(!b) {
				client.sendProtocol(resp, ErrorMsgEnum.we_chat_context_check_fail.getId());
				return;
			}
			int errorCode = 0;
			switch (chatType) {
				case PRIVATE_CHAT: {
					if (targetPlayerId == null || targetPlayerId.isEmpty()) {
						errorCode = ErrorMsgEnum.player_not_exist.getId();
						break;
					}
					if (!PlayerManager.getInstance().isOnline(Long.valueOf(targetPlayerId))) {
						errorCode = ErrorMsgEnum.player_not_online.getId();
					}
					ChatMessageInfo.Builder messageBuilder = ChatMessageInfo.newBuilder();
					messageBuilder.setChatType(chatType);
					messageBuilder.setContent(content);
					messageBuilder.setSendPlayer(sendPlayer.buildSimplePlayerInfo());
					PlayerHelper.sendOnlinePlayer(Long.valueOf(targetPlayerId), ChatMessagePush_31010001.newBuilder().addMessageInfo(messageBuilder.build()).build());
					break;
				}
				case SYSTEM_CHAT: {
					errorCode = ErrorMsgEnum.request_parameter_error.getId();
					break;
				}
				case WORLD_CHAT: {
					int lastChatTime = sendPlayer.getPlayerModule().getLastChatTime();
					if (DateUtil.currentTimeSeconds() - lastChatTime < 5) {
						errorCode = ErrorMsgEnum.operation_too_fast.getId();
						break;
					}
					ServerChatMessagePush_31000010.Builder messageBuilder = ServerChatMessagePush_31000010.newBuilder();
					messageBuilder.setChatType(chatType);
					messageBuilder.setContent(content);
					messageBuilder.setSendPlayer(sendPlayer.buildSimplePlayerInfo());
					messageBuilder.addAllAtPlayerIds(atPlayerIdsList);
					VxHolder.broadcastRemoteServer(ServerType.Game, messageBuilder.build());
					sendPlayer.getPlayerModule().setLastChatTime(DateUtil.currentTimeSeconds());
					break;
				}
				case UNINON_CHAT: {

					break;
				}
				default:
					throw new IllegalArgumentException("Unexpected value: " + chatType);
			}

			client.sendProtocol(resp, errorCode);
		}).onFailure(err ->{
			err.printStackTrace();
			client.sendProtocol(resp, ErrorMsgEnum.we_chat_context_check_fail.getId());
		});


	}
}

