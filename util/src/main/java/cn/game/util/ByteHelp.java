package cn.game.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**   
 * 二进制转换帮助类
 * 2017年4月23日 上午10:24:48
 * @author SYQ
 */
public class ByteHelp {

	public static String hexChar = "0123456789ABCDEF";

	private static byte[] array(byte bit,long number,boolean bigEndian){
		
		byte[] targets = new byte[bit];
		for (int i = 0; i < bit; i++) {
			int offset = bigEndian?i * 8:(targets.length - 1 - i) * 8;
//			targets[i] = (byte) ((number >>> offset) & 0xff);
			targets[i] = (byte) ((number >>> offset));
		}
		return targets;

	}
	
	private static byte[] array(byte bit,long number){
		
		return array(bit, number, true);
	}
	
	
	public static byte[] toByteArray(long l) {

		return array((byte)8, l, false) ; 

	}
	
	public static byte[] toByteArrayB(long l) {

		return array((byte)8, l) ; 

	}

	public static byte[] toByteArray(int number) {

		return array((byte)4, number, false) ; 

	}
	public static byte[] toByteArrayB(int number) {

		return array((byte)4, number,true) ; 

	}
	public static byte[] toByteArray(short number) {

		return array((byte)2, number,false) ; 

	}
	public static byte[] toByteArrayB(short number) {

		return array((byte)2, number,true) ; 

	}

	static public long makeLong(byte b7, byte b6, byte b5, byte b4, byte b3, byte b2, byte b1, byte b0) {
		return ((((long) b7) << 56) | (((long) b6 & 0xff) << 48) | (((long) b5 & 0xff) << 40) | (((long) b4 & 0xff) << 32) | (((long) b3
				& 0xff) << 24) | (((long) b2 & 0xff) << 16) | (((long) b1 & 0xff) << 8) | (((long) b0 & 0xff)));
	}

	static public long makeLong(byte[] bytes) {
		return makeLong(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7]);
	}

	static public long makeLongB(byte[] bytes) {
		return makeLong(bytes[7], bytes[6], bytes[5], bytes[4], bytes[3], bytes[2], bytes[1], bytes[0]);
	}
	static public short makeShort(byte[] bytes) {
		return makeShort(bytes[0], bytes[1]);
	}

	static public short makeShort(byte b2,byte b1) {
		
		return  (short) ((((b2 & 0xff) << 8)) |((b1 & 0xff))); 
		}
	
	static public short makeShortB(byte[] bytes) {
		return makeShort(bytes[1], bytes[0]);
	}

	static private int makeInt(byte b3, byte b2, byte b1, byte b0) {
		return (((b3) << 24) | ((b2 & 0xff) << 16) | ((b1 & 0xff) << 8) | ((b0 & 0xff)));
	}

	static public int makeInt(byte[] bytes) {
		return makeInt(bytes[0], bytes[1], bytes[2], bytes[3]);
	}

	static public int makeIntB(byte[] bytes) {
		return makeInt(bytes[3], bytes[2], bytes[1], bytes[0]);
	}
	static public String toString(byte[] data) {

		boolean isText = true;
		for (int i = 0; i < data.length; i++) {
			if (data[i] < 32 || data[i] > 127) {
				isText = false;
				break;
			}
		}
		if (isText)
			return new String(data);
		else
			return strhex(data);
	}
	public static String strhex(byte[] data) {

		StringBuilder b = new StringBuilder();
		for (int nbr = 0; nbr < data.length; nbr++) {
			int b1 = data[nbr] >>> 4 & 0xf;
			int b2 = data[nbr] & 0xf;
			b.append(hexChar.charAt(b1));
			b.append(hexChar.charAt(b2));
		}
		return b.toString();
	}

	/** 
	 * 输出一个二维数组的前n位
	 * @param a
	 * @param length
	 * @return
	 */
	public static String toString(byte[] a, int length) {

		if (a == null)
			return "null";
		int iMax = length > a.length - 1 ? a.length - 1 : length;
		if (iMax == -1)
			return "[]";

		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = 0;; i++) {
			b.append(a[i]);
			if (i == iMax)
				return b.append(']').toString();
			b.append(", ");
		}
	}

	/**
	 * 更新一个给定的int值，将参数中指定的位更新给1，通常来记录连续多个boolean值
	 * @param value
	 * @param index 里面的值应该小于32
	 * @return
	 */
	public static int modifyBit(int value, List<Integer> index) {

		int ret = value;
		if (index.isEmpty()) {
			return ret;
		}
		for (Integer integer : index) {
			if (integer >= 32) {
				throw new IllegalArgumentException("参数错误，不能大于32 ： " + index);
			}
			ret = ret | 1 << integer;
		}
		return ret;
	}

	/**
	 * 将value的bit位更新为1
	 * @param value
	 * @param bit
	 * @return new  value
	 */
	public static int modifyBit(int value, int bit) {

		return value | 1 << bit;
	}
	public static int modifyBit(Collection<? extends Number> list) {
		return modifyBit(0, list);
	}
	public static int modifyBit(int value, Collection<? extends Number> list) {
		for (Number number : list) {

			value = value | 1 << number.intValue();
		}
		return value;
	}
	public static byte modifyBit(byte value, Collection<? extends Number> list) {
		for (Number number : list) {

			value = (byte) (value | 1 << number.byteValue());
		}
		return value;
	}

	/** 
	 * 获取一个int型数字所有位数为1的集合
	 * @param value
	 * @return
	 */
	public static List<Integer> binary1List(int value) {
		List<Integer> ret = new ArrayList<>();
		for (int i = 0; i < 32; i++) {
			if (((value >> i) & 1) == 1) {
				ret.add(i);
			}
		}
		return ret;
	}

	/**
	 * 计算一个int的二进制值里有几个1
	 * @param value
	 * @return
	 */
	public static int binary1Count(int value) {

		int ret = 0;
		for (int i = 0; i < 32; i++) {
			if (((value >> i) & 1) == 1) {
				ret++;
			}
		}
		return ret;

	}

	/**
	 * 判断某个数的 某位是否为1
	 * @param value	
	 * @param index
	 * @return
	 */
	public static boolean isOne(int value, int index) {
		return ((value >> index) & 1) == 1;
	}

	public static String toBinaryStringWithZero(long data) {
		return String.format("%64s", Long.toBinaryString(data)).replace(' ', '0');
	}
	public static String toBinaryStringWithZero(int data) {
		return String.format("%32s", Integer.toBinaryString(data)).replace(' ', '0');
	}
	public static String toBinaryStringWithZero(byte data) {
		return String.format("%8s", Integer.toBinaryString(data)).replace(' ', '0');
	}
	public static void main(String[] args) {
		long x = 33323223232L;
		System.out.println(toBinaryStringWithZero(x));
		System.out.println(toBinaryStringWithZero(22L));
		System.out.println(toBinaryStringWithZero(x << 22));

		System.out.println(toBinaryStringWithZero(2 | x << 22));

	}
}
