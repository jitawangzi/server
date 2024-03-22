package cn.game.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * 一些字符处理的常用函数集合
 * 
 * @author ChenQX
 */
public class Num
{
	/** 十进制的正则表达式判定,至少一个0到9数字组成的字符串 */
	private static Pattern	decimalistPattern	= Pattern.compile("[0-9]+");
	/** 十六进制的正则表达式判定,0x或者0X起头后跟至少一位的十六进制的值组成的字符串 */
	private static Pattern	hexPattern			= Pattern.compile("^[0][xX][0-9a-fA-F]+$");
	/** 十六进制流的正则表达式判定,0x或者0X起头后跟至少一位的十六进制的值组成的字符串,可以使用()[]{}但无意义 */
	private static Pattern	hexStreamPattern	= Pattern.compile("^[0][xX][0-9a-fA-F\\(\\)\\[\\]\\{\\}\\<\\>]+$");

	/**
	 * 判定一个点(x,y)是否在点(a,b)一个范围是field内
	 * 
	 * @param a
	 * @param b
	 * @param x
	 * @param y
	 * @param field
	 * @return 当且仅当返回true时在范围内
	 */
	public static boolean inField(int a, int b, int x, int y, int field)
	{
		if (Math.abs(a - x) < field && Math.abs(b - y) < field) return true;
		return false;
	}

	/**
	 * 获得一个字符串表示的int值,如果str为空或者不为数值形式则返回默认值,str可以是以0xhhh形式出现的16进制值
	 * 
	 * @param defaultValue
	 * @param str
	 * @return
	 */
	public static int getIntValue(int defaultValue, String str)
	{
		try
		{
			if (str == null) return defaultValue;
			if (decimalistPattern.matcher(str).matches())
			{
				return Integer.parseInt(str);
			} else if (hexPattern.matcher(str).matches())
			{
				return getHexIntValue(str);
			} else
			{
				return Integer.parseInt(str);
			}
		} catch (Exception e)
		{
			return defaultValue;
		}
	}

	/**
	 * 获得一个字符串表示的long值,如果str为空或者不为数值形式则返回默认值,str可以是以0xhhh形式出现的16进制值
	 * 
	 * @param defaultValue
	 * @param str
	 * @return
	 */
	public static long getLongValue(long defaultValue, String str)
	{
		try
		{
			if (str == null) return defaultValue;
			if (decimalistPattern.matcher(str).matches())
			{
				return Long.parseLong(str);
			} else if (hexPattern.matcher(str).matches())
			{
				return getHexLongValue(str);
			} else
			{
				return defaultValue;
			}
		} catch (Exception e)
		{
			return defaultValue;
		}
	}

	/**
	 * 将16进制值的String转为int值,格式为0x或者0X后面加上至少一位的16进制值
	 * 
	 * @param str
	 *            16进制值
	 * @return int 值
	 * @throws NumberFormatException
	 *             ，字符串不是有效的16进制表示
	 */
	public static int getHexIntValue(String str)
	{
		if (!hexPattern.matcher(str).matches()) { throw new NumberFormatException(str); }
		int value = 0;
		for (int i = 2; i < str.length(); i++)
		{
			value = (value << 4) + (getValue(str.charAt(i)) & 0x0000000f);
		}
		return value;
	}

	/**
	 * 将16进制值的String转为字节流,格式为0x或者0X后面加上至少一位的16进制值
	 * 
	 * @param str
	 *            16进制值
	 * @return InputStream 值
	 * @throws NumberFormatException
	 *             ，字符串不是有效的16进制表示
	 */
	public static InputStream getHexStream(String str)
	{
		if (str == null || str.trim().equals("")) { return null; }
		if (!hexStreamPattern.matcher(str).matches()) { throw new NumberFormatException(str); }
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte value = 0;
		byte isAdd = 0;
		for (int i = 2; i < str.length(); i++)
		{
			int charValue = getValue(str.charAt(i));
			if (charValue < 0) continue;
			isAdd++;
			value = (byte) ((value << 4) + charValue);
			if (isAdd >= 2)
			{
				bos.write(value);
				value = 0;
				isAdd = 0;
			}
		}
		if (isAdd != 0)
		{
			bos.write(value);
		}
		return new ByteArrayInputStream(bos.toByteArray());
	}

	/**
	 * 将16进制值的String转为字节组,格式为0x或者0X后面加上至少一位的16进制值
	 * 
	 * @param str
	 *            16进制值
	 * @return byte[] 值
	 * @throws NumberFormatException
	 *             ，字符串不是有效的16进制表示
	 */
	public static byte[] getHexStreamBytes(String str)
	{
		if (!hexStreamPattern.matcher(str).matches()) { throw new NumberFormatException(str); }
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte value = 0;
		byte isAdd = 0;
		for (int i = 2; i < str.length(); i++)
		{
			int charValue = getValue(str.charAt(i));
			if (charValue < 0) continue;
			isAdd++;
			value = (byte) ((value << 4) + charValue);
			if (isAdd >= 2)
			{
				bos.write(value);
				value = 0;
				isAdd = 0;
			}
		}
		if (isAdd != 0)
		{
			bos.write(value);
		}
		return bos.toByteArray();
	}

	/**
	 * 将16进制值的String转为long值,格式为0x或者0X后面加上至少一位的16进制值
	 * 
	 * @param str
	 *            16进制值
	 * @return long 值
	 * @throws NumberFormatException
	 *             ，字符串不是有效的16进制表示
	 */
	public static long getHexLongValue(String str)
	{
		if (!hexPattern.matcher(str).matches()) { throw new NumberFormatException(str); }
		long value = 0;
		for (int i = 2; i < str.length(); i++)
		{
			value = (value << 4) + getValue(str.charAt(i));
		}
		return value;
	}

	/**
	 * 将char所代表的16进制值转为int值
	 * 
	 * @param value
	 *            char
	 * @return int 当返回值小于0时则char非有效的16进制值
	 */
	private static int getValue(char value)
	{
		if (value >= 0x0061 && value <= 0x0066)
		{
			return (0x000a + value - 0x0061);
		} else if (value >= 0x0041 && value <= 0x0046)
		{
			return (0x000a + value - 0x0041);
		} else if (value >= 0x0030 && value <= 0x0039) { return (0x0000 + value - 0x0030); }
		return -1;
	}

	public static long getLongValue(byte[] data)
	{
		return (((long) data[0] << 56) + ((long) (data[1] & 255) << 48) + ((long) (data[2] & 255) << 40)
				+ ((long) (data[3] & 255) << 32) + ((long) (data[4] & 255) << 24) + ((data[5] & 255) << 16)
				+ ((data[6] & 255) << 8) + ((data[7] & 255) << 0));
	}

	/**
	 * 将btye数组转换为16进制字符串
	 */
	public static String getHexStrByByte(byte[] data) throws Exception
	{

		try
		{
			// System.out.println("length:"+data.length);
			StringBuffer hexStr = new StringBuffer("0x");
			for (byte b : data)
			{

				// System.out.println("  byte:"+b);
				byte hig = (byte) ((b & 0x000000f0) >>> 4);
				byte low = (byte) (b & 0x0000000f);
				// System.out.println("higHex:"+Integer.toHexString(hig));
				// System.out.println("lowHex:"+Integer.toHexString(low));
				hexStr.append(Integer.toHexString(hig)).append(Integer.toHexString(low));

			}
			return hexStr.toString();
		} catch (Exception e)
		{

			e.printStackTrace();
			throw e;
		}

	}
}
