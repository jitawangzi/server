package cn.game.core.net.protocol.object;

import com.google.protobuf.ByteString;

public class ByteStringProtocol extends BaseObjectProtocol<ByteString>
{
	
	public ByteStringProtocol(){
	}

	public ByteStringProtocol(int msgID, ByteString datas) {
		super(msgID, datas) ; 
	}
	
	@Override
	public byte[] serializeData() {
		return data.toByteArray();
	}

	@Override
	public void deserializeData(byte[] data) {
		this.data = ByteString.copyFrom(data);
	}

}
