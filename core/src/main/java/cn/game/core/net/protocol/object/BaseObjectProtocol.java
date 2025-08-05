package cn.game.core.net.protocol.object;

import cn.game.core.net.protocol.BaseProtocol;
import cn.game.util.KryoUtils;

public abstract class BaseObjectProtocol<T> extends BaseProtocol<T> {

	protected T data;

	public BaseObjectProtocol() {
	}

	public BaseObjectProtocol(int msgID, T data) {
		super(msgID, data);
	}
	public BaseObjectProtocol(int msgID, T data, int seq) {
		super(msgID, data, seq);
	}
	public BaseObjectProtocol(int msgID, T data, int seq, int errorCode) {
		super(msgID, data, seq, errorCode);
	}

	@Override
	public void setData(T data) {
		this.data = data;
	}

	@Override
	public String getMsgName() {
		return null;
	}
	@Override
	public T getData() {
		return data;
	}
	@Override
	public byte[] serializeData() {
		return KryoUtils.serializeClassAndObject(data);
	}

	@Override
	public void deserializeData(byte[] data) {
		this.data = (T) KryoUtils.deserializeClassAndObject(data);
	}

	@Override
	public String toString() {
		return "BaseObjectProtocol{" + "msgID=" + msgID + ", errorCode=" + errorCode + ", seq=" + seq + ", data="
				+ (data != null ? data.getClass().getName() : "null") + '}';
	}

}
