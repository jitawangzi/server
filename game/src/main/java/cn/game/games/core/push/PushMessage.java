package cn.game.games.core.push;

import java.util.List;

import com.google.protobuf.Message;

public class PushMessage {
	Message protoMessage;
	boolean immediate;
	List<String> tags;
//	long timestamp;

	public PushMessage(Message protoMessage, boolean immediate, List<String> tags) {
		this.protoMessage = protoMessage;
		this.immediate = immediate;
		this.tags = tags;
//		this.timestamp = System.currentTimeMillis();
	}

	public PushMessage(Message protoMessage, List<String> tags) {
		this(protoMessage, true, tags);
	}
}