package cn.game.games.core.push;

import java.util.function.BiConsumer;

import com.google.protobuf.Message;

/**    
 * 消息推送服务，一般用来广播聊天等消息
 * 2024年9月11日 下午7:29:22
 * @author SYQ
 */
public class PushService {

	private static PushService instance = new PushService();

	private TagSystem tagSystem;
	private MessageQueue messageQueue;

	public static PushService getInstance() {
		return instance;
	}
//	public PushService(BiConsumer<Long, Message> sendToPlayer) {
//		this.tagSystem = new TagSystem();
//		this.messageQueue = new MessageQueue(tagSystem, sendToPlayer);
//	}

	public void init(BiConsumer<Long, Message> sendToPlayer) {
		this.tagSystem = new TagSystem();
		this.messageQueue = new MessageQueue(tagSystem, sendToPlayer);
	}

	private PushService() {
	}

	public void addPlayerTags(long playerId, String... tags) {
		tagSystem.addPlayerTags(playerId, tags);
	}

	public void delPlayerTags(long playerId, String... tags) {
		tagSystem.delPlayerTags(playerId, tags);
	}

	public void pushMessage(Message protoMessage, boolean immediate, String... tags) {
		messageQueue.addMessage(protoMessage, immediate, tags);
	}

	public void pushMessage(Message protoMessage, String... tags) {
		messageQueue.addMessage(protoMessage, true, tags);
	}

	public void stop() {
		messageQueue.stop();
	}
}