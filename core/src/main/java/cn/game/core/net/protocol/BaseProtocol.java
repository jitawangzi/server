package cn.game.core.net.protocol;

public abstract class BaseProtocol<T> implements IProtocol<T> {
	
	protected int messageLength;

	protected int msgID;
	
	protected int errorCode;

	protected int seq;

	public BaseProtocol() {
	}
	
	public BaseProtocol(int msgID) {
		setMsgID(msgID);
	}
	public BaseProtocol(T data) {
		setData(data);
	}
	public BaseProtocol(int msgID, T data) {
		this(msgID, data, 0, 0);
	}
	public BaseProtocol(int msgID, T data, int seq) {
		this(msgID, data, seq, 0);
	}
	public BaseProtocol(int msgID, T data, int seq, int errorCode) {
		setMsgID(msgID);
		setData(data);
		setSeq(seq);
		setErrorCode(errorCode);
	}

	public abstract void setData(T data);

	public void setMsgID(int msgID) {
		this.msgID = msgID;
	}

	@Override
	public int getMsgID() {
	
		return msgID;
	}
	
	@Override
	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public int getMessageLength() {
		return messageLength;
	}

	@Override
	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

}
