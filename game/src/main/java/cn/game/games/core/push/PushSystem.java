package cn.game.games.core.push;

import java.util.function.BiConsumer;

import com.google.protobuf.Message;

public class PushSystem {
	private TagSystem tagSystem;
	private MessageQueue messageQueue;

	public PushSystem(BiConsumer<Long, Message> sendToPlayer) {
		this.tagSystem = new TagSystem();
		this.messageQueue = new MessageQueue(tagSystem, sendToPlayer);
	}

	public PushSystem() {
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