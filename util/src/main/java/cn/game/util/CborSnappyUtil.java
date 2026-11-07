package cn.game.util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.BitSet;

import org.apache.commons.collections.map.MultiKeyMap;
import org.xerial.snappy.Snappy;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;

/**
 * 基于 Jackson CBOR 与 Snappy 的二进制压缩序列化工具类。
 * 用于玩家模块数据（Modules）等高密度对象的极限体积压缩与高效存取。
 *
 * @author shuyongqiang
 */
public class CborSnappyUtil {

	private static final ObjectMapper objectMapper;

	static {
		objectMapper = new ObjectMapper(new CBORFactory());

		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		objectMapper.disable(SerializationFeature.FAIL_ON_SELF_REFERENCES);
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

		// 必须启用多态类型支持 PolymorphicTypeValidator (LaissezFaireSubTypeValidator.instance, DefaultTyping.NON_FINAL)
		PolymorphicTypeValidator ptv = LaissezFaireSubTypeValidator.instance;
		objectMapper.activateDefaultTyping(ptv, DefaultTyping.NON_FINAL);

		// 注册 BitSet 与 MultiKeyMap 模块
		SimpleModule module = new SimpleModule();
		module.addSerializer(BitSet.class, new JsonUtil.BitSetSerializer());
		module.addDeserializer(BitSet.class, new JsonUtil.BitSetDeserializer());
		module.addSerializer(MultiKeyMap.class, new JsonUtil.MultiKeyMapSerializer());
		module.addDeserializer(MultiKeyMap.class, new JsonUtil.MultiKeyMapDeserializer());
		module.addSerializer(org.apache.commons.collections4.map.MultiKeyMap.class, new JsonUtil.MultiKeyMapSerializer4());
		module.addDeserializer(org.apache.commons.collections4.map.MultiKeyMap.class, new JsonUtil.MultiKeyMapDeserializer4());
		objectMapper.registerModule(module);

		// 注册 Guava 模块与 Protobuf 模块
		objectMapper.registerModule(new GuavaModule());
		objectMapper.registerModule(new ProtobufModule());

		// 注册 JavaTimeModule，格式与项目中 JsonUtil / DateUtil 一致
		JavaTimeModule javaTimeModule = new JavaTimeModule();
		javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DateUtil.pattern_en)));
		javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DateUtil.pattern_en)));
		objectMapper.registerModule(javaTimeModule);
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		// 字段可见性策略与项目中 JsonUtil 保持一致（fieldVisibility ANY，getter/setter/creator/isGetter NONE）
		objectMapper.setVisibility(objectMapper.getSerializationConfig()
				.getDefaultVisibilityChecker()
				.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
				.withGetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withSetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
				.withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));
	}

	private CborSnappyUtil() {}

	/**
	 * 将对象进行 CBOR 序列化并使用 Snappy 压缩
	 *
	 * @param value 要序列化的对象
	 * @return 压缩后的二进制字节数组，如果对象为 null 则返回 null
	 */
	public static byte[] compress(Object value) {
		if (value == null) {
			return null;
		}
		try {
			byte[] cborBytes = objectMapper.writeValueAsBytes(value);
			if (cborBytes == null || cborBytes.length == 0) {
				return null;
			}
			return Snappy.compress(cborBytes);
		} catch (Exception e) {
			throw new RuntimeException("CBOR/Snappy compress error", e);
		}
	}

	/**
	 * 将 Snappy 压缩的 CBOR 字节数组解压并反序列化为对象（根据多态元数据自动还原具体类型）
	 *
	 * @param <T> 目标类型
	 * @param compressedBytes 压缩字节数组
	 * @return 反序列化后的对象，若输入为 null 或空数组则返回 null
	 */
	@SuppressWarnings("unchecked")
	public static <T> T decompress(byte[] compressedBytes) {
		if (compressedBytes == null || compressedBytes.length == 0) {
			return null;
		}
		try {
			byte[] cborBytes = Snappy.uncompress(compressedBytes);
			return (T) objectMapper.readValue(cborBytes, Object.class);
		} catch (Exception e) {
			throw new RuntimeException("CBOR/Snappy decompress error", e);
		}
	}

	/**
	 * 将 Snappy 压缩的 CBOR 字节数组解压并按指定 Class 反序列化
	 *
	 * @param <T> 目标类型
	 * @param compressedBytes 压缩字节数组
	 * @param clazz 目标 Class
	 * @return 反序列化后的对象，若输入为 null 或空数组则返回 null
	 */
	public static <T> T decompress(byte[] compressedBytes, Class<T> clazz) {
		if (compressedBytes == null || compressedBytes.length == 0) {
			return null;
		}
		try {
			byte[] cborBytes = Snappy.uncompress(compressedBytes);
			return objectMapper.readValue(cborBytes, clazz);
		} catch (Exception e) {
			throw new RuntimeException("CBOR/Snappy decompress error", e);
		}
	}

	/**
	 * 将 Snappy 压缩的 CBOR 字节数组解压并按指定 TypeReference 反序列化
	 *
	 * @param <T> 目标类型
	 * @param compressedBytes 压缩字节数组
	 * @param typeReference 目标 TypeReference
	 * @return 反序列化后的对象，若输入为 null 或空数组则返回 null
	 */
	public static <T> T decompress(byte[] compressedBytes, TypeReference<T> typeReference) {
		if (compressedBytes == null || compressedBytes.length == 0) {
			return null;
		}
		try {
			byte[] cborBytes = Snappy.uncompress(compressedBytes);
			return objectMapper.readValue(cborBytes, typeReference);
		} catch (Exception e) {
			throw new RuntimeException("CBOR/Snappy decompress error", e);
		}
	}

	public static ObjectMapper getObjectMapper() {
		return objectMapper;
	}
}