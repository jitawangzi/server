package cn.game.core.net.protocol.object;

public class ObjectProtocol extends BaseObjectProtocol<Object> {

	public ObjectProtocol() {
		super();
	}

	public ObjectProtocol(int msgID, Object data) {
		super(msgID, data);
	}
}
