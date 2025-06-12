package cn.game.core.net.protocol;

public interface IProtocol<T> {

	public int getMsgID();

	public String getMsgName();

	/** 
	 * 获取消息体
	 * @return
	 */
	public T getData();

	/** 
	 * 序列化消息体
	 * @return
	 */
	public byte[] serializeData();

	/** 
	 * 反序列化消息体
	 * @param data
	 * @return 
	 * @return
	 */
	public void deserializeData(byte[] data);

	public int getErrorCode();

	public int getSeq();

	public String toString();

}
