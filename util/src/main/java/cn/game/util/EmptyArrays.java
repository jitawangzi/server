package cn.game.util;

import java.util.HashMap;
import java.util.Map;

public class EmptyArrays {

	private EmptyArrays() {
	}

	// 一维数组常量
	public static final int[] EMPTY_INT_ARRAY = new int[0];
	public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
	public static final short[] EMPTY_SHORT_ARRAY = new short[0];
	public static final long[] EMPTY_LONG_ARRAY = new long[0];
	public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
	public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
	public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
	public static final char[] EMPTY_CHAR_ARRAY = new char[0];

	public static final Integer[] EMPTY_INTEGER_OBJECT_ARRAY = new Integer[0];
	public static final Byte[] EMPTY_BYTE_OBJECT_ARRAY = new Byte[0];
	public static final Short[] EMPTY_SHORT_OBJECT_ARRAY = new Short[0];
	public static final Long[] EMPTY_LONG_OBJECT_ARRAY = new Long[0];
	public static final Float[] EMPTY_FLOAT_OBJECT_ARRAY = new Float[0];
	public static final Double[] EMPTY_DOUBLE_OBJECT_ARRAY = new Double[0];
	public static final Boolean[] EMPTY_BOOLEAN_OBJECT_ARRAY = new Boolean[0];
	public static final Character[] EMPTY_CHARACTER_OBJECT_ARRAY = new Character[0];

	public static final String[] EMPTY_STRING_ARRAY = new String[0];
	public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

	// 二维数组常量
	public static final int[][] EMPTY_INT_2D_ARRAY = new int[0][];
	public static final byte[][] EMPTY_BYTE_2D_ARRAY = new byte[0][];
	public static final short[][] EMPTY_SHORT_2D_ARRAY = new short[0][];
	public static final long[][] EMPTY_LONG_2D_ARRAY = new long[0][];
	public static final float[][] EMPTY_FLOAT_2D_ARRAY = new float[0][];
	public static final double[][] EMPTY_DOUBLE_2D_ARRAY = new double[0][];
	public static final boolean[][] EMPTY_BOOLEAN_2D_ARRAY = new boolean[0][];
	public static final char[][] EMPTY_CHAR_2D_ARRAY = new char[0][];

	public static final Integer[][] EMPTY_INTEGER_OBJECT_2D_ARRAY = new Integer[0][];
	public static final Byte[][] EMPTY_BYTE_OBJECT_2D_ARRAY = new Byte[0][];
	public static final Short[][] EMPTY_SHORT_OBJECT_2D_ARRAY = new Short[0][];
	public static final Long[][] EMPTY_LONG_OBJECT_2D_ARRAY = new Long[0][];
	public static final Float[][] EMPTY_FLOAT_OBJECT_2D_ARRAY = new Float[0][];
	public static final Double[][] EMPTY_DOUBLE_OBJECT_2D_ARRAY = new Double[0][];
	public static final Boolean[][] EMPTY_BOOLEAN_OBJECT_2D_ARRAY = new Boolean[0][];
	public static final Character[][] EMPTY_CHARACTER_OBJECT_2D_ARRAY = new Character[0][];

	public static final String[][] EMPTY_STRING_2D_ARRAY = new String[0][];
	public static final Object[][] EMPTY_OBJECT_2D_ARRAY = new Object[0][];

	private static final Map<String, String> EMPTY_ARRAY_CONSTANTS = new HashMap<>();
	private static final Map<String, String> EMPTY_2D_ARRAY_CONSTANTS = new HashMap<>();

	static {
		// 一维数组映射
		EMPTY_ARRAY_CONSTANTS.put("int", "EmptyArrays.EMPTY_INT_ARRAY");
		EMPTY_ARRAY_CONSTANTS.put("byte", "EmptyArrays.EMPTY_BYTE_ARRAY");
		EMPTY_ARRAY_CONSTANTS.put("short", "EmptyArrays.EMPTY_SHORT_ARRAY");
		EMPTY_ARRAY_CONSTANTS.put("long", "EmptyArrays.EMPTY_LONG_ARRAY");
		EMPTY_ARRAY_CONSTANTS.put("float", "EmptyArrays.EMPTY_FLOAT_ARRAY");
		EMPTY_ARRAY_CONSTANTS.put("double", "EmptyArrays.EMPTY_DOUBLE_ARRAY");
		EMPTY_ARRAY_CONSTANTS.put("boolean", "EmptyArrays.EMPTY_BOOLEAN_ARRAY");
		EMPTY_ARRAY_CONSTANTS.put("char", "EmptyArrays.EMPTY_CHAR_ARRAY");

		EMPTY_ARRAY_CONSTANTS.put("Integer", "EmptyArrays.EMPTY_INTEGER_OBJECT_ARRAY");
		EMPTY_ARRAY_CONSTANTS.put("Byte", "EmptyArrays.EMPTY_BYTE_OBJECT_ARRAY");
		EMPTY_ARRAY_CONSTANTS.put("Short", "EmptyArrays.EMPTY_SHORT_OBJECT_ARRAY");
		EMPTY_ARRAY_CONSTANTS.put("Long", "EmptyArrays.EMPTY_LONG_OBJECT_ARRAY");
		EMPTY_ARRAY_CONSTANTS.put("Float", "EmptyArrays.EMPTY_FLOAT_OBJECT_ARRAY");
		EMPTY_ARRAY_CONSTANTS.put("Double", "EmptyArrays.EMPTY_DOUBLE_OBJECT_ARRAY");
		EMPTY_ARRAY_CONSTANTS.put("Boolean", "EmptyArrays.EMPTY_BOOLEAN_OBJECT_ARRAY");
		EMPTY_ARRAY_CONSTANTS.put("Character", "EmptyArrays.EMPTY_CHARACTER_OBJECT_ARRAY");
		EMPTY_ARRAY_CONSTANTS.put("String", "EmptyArrays.EMPTY_STRING_ARRAY");
		EMPTY_ARRAY_CONSTANTS.put("Object", "EmptyArrays.EMPTY_OBJECT_ARRAY");

		// 二维数组映射
		EMPTY_2D_ARRAY_CONSTANTS.put("int", "EmptyArrays.EMPTY_INT_2D_ARRAY");
		EMPTY_2D_ARRAY_CONSTANTS.put("byte", "EmptyArrays.EMPTY_BYTE_2D_ARRAY");
		EMPTY_2D_ARRAY_CONSTANTS.put("short", "EmptyArrays.EMPTY_SHORT_2D_ARRAY");
		EMPTY_2D_ARRAY_CONSTANTS.put("long", "EmptyArrays.EMPTY_LONG_2D_ARRAY");
		EMPTY_2D_ARRAY_CONSTANTS.put("float", "EmptyArrays.EMPTY_FLOAT_2D_ARRAY");
		EMPTY_2D_ARRAY_CONSTANTS.put("double", "EmptyArrays.EMPTY_DOUBLE_2D_ARRAY");
		EMPTY_2D_ARRAY_CONSTANTS.put("boolean", "EmptyArrays.EMPTY_BOOLEAN_2D_ARRAY");
		EMPTY_2D_ARRAY_CONSTANTS.put("char", "EmptyArrays.EMPTY_CHAR_2D_ARRAY");

		EMPTY_2D_ARRAY_CONSTANTS.put("Integer", "EmptyArrays.EMPTY_INTEGER_OBJECT_2D_ARRAY");
		EMPTY_2D_ARRAY_CONSTANTS.put("Byte", "EmptyArrays.EMPTY_BYTE_OBJECT_2D_ARRAY");
		EMPTY_2D_ARRAY_CONSTANTS.put("Short", "EmptyArrays.EMPTY_SHORT_OBJECT_2D_ARRAY");
		EMPTY_2D_ARRAY_CONSTANTS.put("Long", "EmptyArrays.EMPTY_LONG_OBJECT_2D_ARRAY");
		EMPTY_2D_ARRAY_CONSTANTS.put("Float", "EmptyArrays.EMPTY_FLOAT_OBJECT_2D_ARRAY");
		EMPTY_2D_ARRAY_CONSTANTS.put("Double", "EmptyArrays.EMPTY_DOUBLE_OBJECT_2D_ARRAY");
		EMPTY_2D_ARRAY_CONSTANTS.put("Boolean", "EmptyArrays.EMPTY_BOOLEAN_OBJECT_2D_ARRAY");
		EMPTY_2D_ARRAY_CONSTANTS.put("Character", "EmptyArrays.EMPTY_CHARACTER_2D_ARRAY");
		EMPTY_2D_ARRAY_CONSTANTS.put("String", "EmptyArrays.EMPTY_STRING_2D_ARRAY");
		EMPTY_2D_ARRAY_CONSTANTS.put("Object", "EmptyArrays.EMPTY_OBJECT_2D_ARRAY");
	}

	public static String getEmptyArray(String type, int dimensions) {
		if (dimensions == 1) {
			String constant = EMPTY_ARRAY_CONSTANTS.get(type);
			return constant != null ? constant : "new " + type + "[] {}";
		} else if (dimensions == 2) {
			String constant = EMPTY_2D_ARRAY_CONSTANTS.get(type);
			return constant != null ? constant : "new " + type + "[0][]";
		}
		return "new " + type + "[0]" + "[]".repeat(dimensions - 1);
	}
}