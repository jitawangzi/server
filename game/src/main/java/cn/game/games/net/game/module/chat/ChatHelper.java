package cn.game.games.net.game.module.chat;

import cn.game.core.net.vertx.VxHolder;
import cn.game.protocol.protobuf.ChatMsg.ChatMessageInfo;
import cn.game.protocol.protobuf.ChatMsg.ChatType;
import cn.game.protocol.protobuf.ChatMsg.ServerChatMessagePush_31000010;
import cn.game.util.ServerType;

public class ChatHelper {

	/** 
	 * 广播跑马灯消息
	 * @param content
	 * @param 给哪个服广播
	 */
	public static void marquee(String content, String serverId) {

		ChatMessageInfo.Builder chatBuilder = ChatMessageInfo.newBuilder();
		chatBuilder.setChatType(ChatType.MARQUEE);
		chatBuilder.setContent(content);

		ServerChatMessagePush_31000010.Builder messageBuilder = ServerChatMessagePush_31000010.newBuilder();
		messageBuilder.setChatType(ChatType.MARQUEE);
		messageBuilder.setContent(content);
		messageBuilder.setServerId(serverId);
		VxHolder.broadcastRemoteServer(ServerType.Game, messageBuilder.build());

	}
}
