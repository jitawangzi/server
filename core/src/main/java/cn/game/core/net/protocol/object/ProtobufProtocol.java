package cn.game.core.net.protocol.object;

import com.google.protobuf.Message;

import cn.game.protocol.protobuf.PbProtocol;

public class ProtobufProtocol extends BaseObjectProtocol<Message> {

	public ProtobufProtocol(int msgID, Message message) {
		super(msgID, message);
	}
	public ProtobufProtocol(int msgID, Message message, int seq) {
		super(msgID, message, seq);
	}

	public ProtobufProtocol(){
	}

	@Override
	public byte[] serializeData() {
		return data.toByteArray();
	}

	@Override
	public void deserializeData(byte[] data) {
		this.data = PbProtocol.getInstance().parseFrom(msgID, data);
	}

}
