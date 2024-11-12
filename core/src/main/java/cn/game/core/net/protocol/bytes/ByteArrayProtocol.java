package cn.game.core.net.protocol.bytes;

public class ByteArrayProtocol extends BaseByteArrayProtocol{
	
	public ByteArrayProtocol(){
	}
	public ByteArrayProtocol(int msgID,byte[] datas){
		super(msgID, datas) ; 
	}

	public ByteArrayProtocol(int msgID, byte[] data, int seq) {
		super(msgID, data, seq);
	}

	public ByteArrayProtocol(int msgID, byte[] data, int seq, int errorCode) {
		super(msgID, data, seq, errorCode);
	}
	
}
