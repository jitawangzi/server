package cn.game.util;

import java.nio.ByteBuffer;

import org.apache.mina.core.buffer.IoBuffer;

public class GameCrypt
{
	public static final GameCrypt	gameCrypt	= new GameCrypt();
	private final byte[]			inKey		= new byte[16];
	private final byte[]			outKey		= new byte[16];
	private boolean					isEnabled;

	/**
	 * @Title: decrypt
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param buf
	 * @param type
	 * @param size
	 * @param position
	 * @return void 返回类型
	 */
	public void decrypt(final IoBuffer buf, final int type, final int size, final int position)
	{
		byte encriptKey = (byte) ((byte) size ^ (byte) (size >>> 8) ^ (byte) type ^ (byte) (type >>> 8));
		byte nextencriptKey;
		for (int i = 0; i < size; i++)
		{
			nextencriptKey = buf.get(i + position);
			buf.put(i + position, (byte) (nextencriptKey ^ encriptKey));
			encriptKey = nextencriptKey;
		}
	}

	/**
	 * @Title: decrypt
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param buf
	 * @param type
	 * @param size
	 * @param position
	 * @return void 返回类型
	 */
	public void decrypt(final byte[] buf, final short type, final short size, final int position)
	{
		byte encriptKey = (byte) ((byte) size ^ (byte) (size >>> 8) ^ (byte) type ^ (byte) (type >>> 8));
		byte nextencriptKey;
		for (int i = 0; i < size; i++)
		{
			nextencriptKey = buf[i + position];
			buf[i + position] = (byte) (nextencriptKey ^ encriptKey);
			encriptKey = nextencriptKey;
		}
	}

	/**
	 * @Title: encrypt
	 * @Description: 加密
	 * @param buf
	 * @param size
	 * @return void 返回类型
	 */
	public void encrypt(final ByteBuffer buf, final int size)
	{

	}

	/**
	 * @Title: encrypt
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param buf
	 * @param type
	 * @param size
	 * @param position
	 * @return void 返回类型
	 */
	public void encrypt(final IoBuffer buf, final short type, final short size, final int position)
	{
		byte encriptKey = (byte) ((byte) size ^ (byte) (size >>> 8) ^ (byte) type ^ (byte) (type >>> 8));
		byte key;
		for (int i = 0; i < size; i++)
		{
			key = (byte) (buf.get(i + position) ^ encriptKey);
			buf.put(i + position, key);
			encriptKey = key;
		}
	}

	/**
	 * @Title: encrypt
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param buf
	 * @param type
	 * @param size
	 * @param position
	 * @return void 返回类型
	 */
	public void encrypt(final byte[] buf, final short type, final short size, final int position)
	{
		byte encriptKey = (byte) ((byte) size ^ (byte) (size >>> 8) ^ (byte) type ^ (byte) (type >>> 8));
		byte key;
		for (int i = 0; i < size; i++)
		{
			key = (byte) (buf[i + position] ^ encriptKey);
			buf[i + position] = key;
			encriptKey = key;
		}
	}

	/**
	 * @Title: checkPacket
	 * @Description: 检查数据包头
	 * @param packetHeader
	 * @return boolean 返回类型
	 */
	public boolean checkPacket(int packetHeader)
	{
		boolean ret = true;
		return ret;
	}

	/**
	 * @Title: getPacketHeader
	 * @Description: 数据包头长度加密
	 * @param length
	 * @return
	 * @return byte[] 返回类型
	 */
	public byte[] getPacketHeader(int length)
	{

		byte[] data = new byte[4];
		data[0] = (byte) (length & 0xff);
		data[0] = (byte) (length >> 8 & 0xff);
		data[0] = (byte) (length >> 16 & 0xff);
		data[0] = (byte) (length >> 24 & 0xff);
		return null;
	}
}
