package cn.game.util;

import com.google.protobuf.Message;

import java.lang.reflect.Method;

/**    
 * 通用的对象序列化/反序列化工具，通过对象类型，使用不同的序列化方案
 * 2025年8月4日 18:04:04
 * @author SYQ
 */
public class SerializationUtil {

	/**
	 * 自动分流序列化（标准）
	 */
	public static <T> byte[] serialize(T obj) {
		if (obj == null)
			return null;
		if (isProtobufMessage(obj.getClass())) {
			return ((Message) obj).toByteArray();
		} else {
			return KryoUtils.serialize(obj);
		}
	}

	/**
	 * 自动分流序列化（带版本）,只针对 kryo方式
	 */
	public static <T> byte[] serializeWithVersion(T obj) {
		if (obj == null)
			return null;
		if (isProtobufMessage(obj.getClass())) {
			// Protobuf本身没有"版本"序列化的说法，直接原生
			return ((Message) obj).toByteArray();
		} else {
			return KryoUtils.serializeClassAndObjectWithVersion(obj);
		}
	}

	/**
	 * 自动分流反序列化（标准）
	 */
	@SuppressWarnings("unchecked")
	public static <T> T deserialize(byte[] data, Class<T> clazz) {
		if (data == null || clazz == null)
			return null;
		if (isProtobufMessage(clazz)) {
			try {
				Method parseFrom = clazz.getMethod("parseFrom", byte[].class);
				return (T) parseFrom.invoke(null, data);
			} catch (Exception e) {
				throw new RuntimeException("Failed to deserialize protobuf message: " + clazz.getName(), e);
			}
		} else {
			return KryoUtils.deserialize(data, clazz);
		}
	}

	/**
	 * 自动分流反序列化（带版本）
	 */
	@SuppressWarnings("unchecked")
	public static <T> T deserializeWithVersion(byte[] data, Class<T> clazz) {
		if (data == null || clazz == null)
			return null;
		if (isProtobufMessage(clazz)) {
			try {
				Method parseFrom = clazz.getMethod("parseFrom", byte[].class);
				return (T) parseFrom.invoke(null, data);
			} catch (Exception e) {
				throw new RuntimeException("Failed to deserialize protobuf message: " + clazz.getName(), e);
			}
		} else {
			// 你可以根据自己的KryoUtils版本化序列化API调整
			Object obj = KryoUtils.deserializeClassAndObjectWithVersion(data);
			return clazz.isInstance(obj) ? (T) obj : null;
		}
	}

	/**
	 * 自动分流（writeClassAndObject）序列化
	 */
	public static byte[] serializeClassAndObject(Object obj) {
		if (obj == null)
			return null;
		if (isProtobufMessage(obj.getClass())) {
			return ((Message) obj).toByteArray();
		} else {
			return KryoUtils.serializeClassAndObject(obj);
		}
	}

	/**
	 * 自动分流（writeClassAndObject）序列化，带版本
	 */
	public static byte[] serializeClassAndObjectWithVersion(Object obj) {
		if (obj == null)
			return null;
		if (isProtobufMessage(obj.getClass())) {
			return ((Message) obj).toByteArray();
		} else {
			return KryoUtils.serializeClassAndObjectWithVersion(obj);
		}
	}

	/**
	 * 自动分流反序列化（writeClassAndObject），返回Object
	 * 注意：protobuf类型无法自动判断Class，只能用在非protobuf对象
	 */
	public static Object deserializeClassAndObject(byte[] data) {
		return KryoUtils.deserializeClassAndObject(data);
	}

	/**
	 * 自动分流反序列化（writeClassAndObject），带版本
	 * 注意：protobuf类型无法自动判断Class，只能用在非protobuf对象
	 */
	public static Object deserializeClassAndObjectWithVersion(byte[] data) {
		return KryoUtils.deserializeClassAndObjectWithVersion(data);
	}

	/** 判断是否为protobuf消息类型 */
	private static boolean isProtobufMessage(Class<?> clazz) {
		return Message.class.isAssignableFrom(clazz);
	}
}