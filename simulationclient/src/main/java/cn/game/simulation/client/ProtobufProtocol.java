package cn.game.simulation.client;

public class ProtobufProtocol {
	public int msgID ; 
	public Object datas ; 
	public int callback ; 
	public byte type ; 
	public ProtobufProtocol(){
	}
	public ProtobufProtocol(int msgID,Object datas){
		this.msgID = msgID ; 
		this.datas = datas ; 
	}
	public int getMsgID()
	{
		return msgID;
	}
	public void setMsgID(int msgID)
	{
		this.msgID = msgID;
	}
	public Object getDatas()
	{
		return datas;
	}
	public void setDatas(Object datas)
	{
		this.datas = datas;
	}
	
	
}
