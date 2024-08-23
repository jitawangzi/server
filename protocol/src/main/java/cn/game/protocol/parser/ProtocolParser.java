package cn.game.protocol.parser;

public interface ProtocolParser {

	public Object parseFrom(int msgID, byte[] data);

	public int getMsgId(String name);

	public String getMsgName(int id);
}
