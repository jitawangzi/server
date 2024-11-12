package cn.game.core.net.protocol.bytes;

import cn.game.core.net.protocol.BaseProtocol;

public abstract class BaseByteArrayProtocol extends BaseProtocol<byte[]> {

	protected byte[] datas;

	public BaseByteArrayProtocol() {
	}

	public BaseByteArrayProtocol(int msgID, byte[] datas) {
		super(msgID, datas);
	}

	public BaseByteArrayProtocol(int msgID, byte[] data, int seq) {
		super(msgID, data, seq);
	}
	public BaseByteArrayProtocol(int msgID, byte[] data, int seq, int errorCode) {
		super(msgID, data, seq, errorCode);
	}

	@Override
	public void setData(byte[] datas) {
		this.datas = datas;
	}

	@Override
	public String getMsgName() {
		return null;
	}
	@Override
	public byte[] getData() {
		return datas;
	}

	@Override
	public byte[] serializeData() {
		return datas;
	}

	@Override
	public void deserializeData(byte[] data) {
		this.datas = data;
	}

}
