package cn.game.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TestByteHelp {

	@Test
	@DisplayName("test toByteArray for long/int/short big and little endian")
	void testToByteArray() {
		long l = 0x0102030405060708L;
		byte[] little = ByteHelp.toByteArray(l);
		byte[] big = ByteHelp.toByteArrayB(l);

		assertArrayEquals(new byte[] { 8, 7, 6, 5, 4, 3, 2, 1 }, little);
		assertArrayEquals(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 }, big);

		int i = 0x01020304;
		assertArrayEquals(new byte[] { 4, 3, 2, 1 }, ByteHelp.toByteArray(i));
		assertArrayEquals(new byte[] { 1, 2, 3, 4 }, ByteHelp.toByteArrayB(i));

		short s = (short) 0x0102;
		assertArrayEquals(new byte[] { 2, 1 }, ByteHelp.toByteArray(s));
		assertArrayEquals(new byte[] { 1, 2 }, ByteHelp.toByteArrayB(s));
	}

	@Test
	void testMakeLong() {
		byte[] arr = { 1, 2, 3, 4, 5, 6, 7, 8 };
		long l = ByteHelp.makeLong(arr);
		long lB = ByteHelp.makeLongB(arr);

		assertEquals(0x0102030405060708L, l);
		assertEquals(0x0807060504030201L, lB);

		assertThrows(IllegalArgumentException.class, () -> ByteHelp.makeLong(new byte[] { 1, 2, 3 }));
	}

	@Test
	void testMakeShort() {
		byte[] arr = { 1, 2 };
		assertEquals((short) 0x0102, ByteHelp.makeShort(arr));
		assertEquals((short) 0x0201, ByteHelp.makeShortB(arr));
		assertEquals((short) 0x0102, ByteHelp.makeShort((byte) 1, (byte) 2));
		assertThrows(IllegalArgumentException.class, () -> ByteHelp.makeShort(new byte[] { 1 }));
	}

	@Test
	void testMakeInt() {
		byte[] arr = { 1, 2, 3, 4 };
		assertEquals(0x01020304, ByteHelp.makeInt(arr));
		assertEquals(0x04030201, ByteHelp.makeIntB(arr));
	}

	@Test
	void testToStringByteArrayAsText() {
		byte[] textBytes = "hello".getBytes(StandardCharsets.US_ASCII);
		assertEquals("hello", ByteHelp.toString(textBytes));
	}

	@Test
	void testToStringByteArrayAsHex() {
		byte[] data = new byte[] { (byte) 0xF0, 0x0A };
		assertEquals("F00A", ByteHelp.toString(data));
	}

	@Test
	void testStrHex() {
		byte[] data = { 0x12, 0x34, (byte) 0xAB, (byte) 0xFF };
		assertEquals("1234ABFF", ByteHelp.strhex(data));
	}

	@Test
	void testToStringWithLength() {
		byte[] arr = { 1, 2, 3, 4, 5 };
		assertEquals("[1, 2, 3]", ByteHelp.toString(arr, 3));
		assertEquals("[1, 2, 3, 4, 5]", ByteHelp.toString(arr, 5));
		assertEquals("[]", ByteHelp.toString(arr, 0));
		assertEquals("null", ByteHelp.toString(null, 3));
	}

	@Test
	void testModifyBitList() {
		int value = 0;
		List<Integer> idxs = Arrays.asList(1, 3, 5);
		int modified = ByteHelp.modifyBit(value, idxs);
		assertEquals(0b101010, modified);

		assertEquals(value, ByteHelp.modifyBit(value, Collections.emptyList()));

		List<Integer> invalid = Arrays.asList(33);
		assertThrows(IllegalArgumentException.class, () -> ByteHelp.modifyBit(value, invalid));
		List<Integer> negative = Arrays.asList(-1);
		assertThrows(IllegalArgumentException.class, () -> ByteHelp.modifyBit(value, negative));
	}

	@Test
	void testModifyBitSingle() {
		int value = 0b100;
		assertEquals(0b1100, ByteHelp.modifyBit(value, 2));
		assertEquals(0b10100, ByteHelp.modifyBit(value, 4));
	}

	@Test
	void testModifyBitCollectionInt() {
		Collection<Integer> list = Arrays.asList(0, 1, 3);
		assertEquals(0b1011, ByteHelp.modifyBit(0, list));
		assertEquals(0b1011, ByteHelp.modifyBit(list));
	}

	@Test
	void testModifyBitByteCollection() {
		Collection<Number> list = Arrays.asList(0, 2, 7);
		byte result = ByteHelp.modifyBit((byte) 0, list);
		assertEquals((byte) 0b10000101, result);
	}

	@Test
	void testBinary1List() {
		int value = 0b101010;
		List<Integer> ones = ByteHelp.binary1List(value);
		assertEquals(Arrays.asList(1, 3, 5), ones);
	}

	@Test
	void testBinary1Count() {
		assertEquals(0, ByteHelp.binary1Count(0));
		assertEquals(1, ByteHelp.binary1Count(1));
		assertEquals(3, ByteHelp.binary1Count(0b1011));
		assertEquals(32, ByteHelp.binary1Count(-1));
	}

	@Test
	void testIsOne() {
		assertTrue(ByteHelp.isOne(0b1000, 3));
		assertFalse(ByteHelp.isOne(0b1000, 2));
	}

	@Test
	void testToBinaryStringWithZeroLong() {
		assertEquals("0000000000000000000000000000000000000000000000000000000000001010", ByteHelp.toBinaryStringWithZero(10L));
		assertEquals(64, ByteHelp.toBinaryStringWithZero(Long.MAX_VALUE).length());
	}

	@Test
	void testToBinaryStringWithZeroInt() {
		assertEquals("00000000000000000000000000001010", ByteHelp.toBinaryStringWithZero(10));
		assertEquals(32, ByteHelp.toBinaryStringWithZero(Integer.MAX_VALUE).length());
	}

	@Test
	void testToBinaryStringWithZeroByte() {
		assertEquals("00001010", ByteHelp.toBinaryStringWithZero((byte) 10));
		assertEquals("10000000", ByteHelp.toBinaryStringWithZero((byte) -128));
		assertEquals(8, ByteHelp.toBinaryStringWithZero((byte) -1).length());
	}
}