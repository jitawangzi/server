package cn.game.core.net.protocol.bytes;

public class DefaultByteProtocol extends BaseByteProtocol{
	
	public DefaultByteProtocol(){
	}
	public DefaultByteProtocol(int msgID,byte[] datas){
		super(msgID, datas) ; 
	}
	
}
