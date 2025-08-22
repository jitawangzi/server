package cn.game.util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.BitSet;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**    
 * 对jackson的一个封装
 * 2024年3月1日 下午5:14:33
 * @author SYQ
 */
public class JsonUtil {
	private static ObjectMapper objectMapper = new ObjectMapper();
	/** 启用类型信息 */
	private static ObjectMapper objectMapperWithType = new ObjectMapper();

	static {
		configureMapper(objectMapper);
		configureMapper(objectMapperWithType);

		// 启用类型信息
		PolymorphicTypeValidator ptv = LaissezFaireSubTypeValidator.instance;
		objectMapperWithType.activateDefaultTyping(ptv, DefaultTyping.NON_FINAL);
	}

	private static void configureMapper(ObjectMapper mapper) {

		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		mapper.disable(SerializationFeature.FAIL_ON_SELF_REFERENCES);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//		mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
		// 忽略不存在的属性
//		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		SimpleModule module = new SimpleModule();
		module.addSerializer(BitSet.class, new BitSetSerializer());
		module.addDeserializer(BitSet.class, new BitSetDeserializer());
		module.addSerializer(MultiKeyMap.class, new MultiKeyMapSerializer());
		module.addDeserializer(MultiKeyMap.class, new MultiKeyMapDeserializer());
		module.addSerializer(org.apache.commons.collections4.map.MultiKeyMap.class, new MultiKeyMapSerializer4());
		module.addDeserializer(org.apache.commons.collections4.map.MultiKeyMap.class, new MultiKeyMapDeserializer4());
		mapper.registerModule(module);
		mapper.registerModule(new GuavaModule()); // 注册 Guava 模块
		mapper.registerModule(new com.hubspot.jackson.datatype.protobuf.ProtobufModule()); // 注册 Guava 模块
	    // 注册 JavaTimeModule，并自定义格式
	    JavaTimeModule javaTimeModule = new JavaTimeModule();
	    javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DateUtil.pattern_en)));
	    mapper.registerModule(javaTimeModule);
	    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		
		mapper.setVisibility(objectMapper.getSerializationConfig()
				.getDefaultVisibilityChecker()
				.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
				.withGetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withSetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
				.withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));

	}

	/** 
	 * 对象转成json字符串，默认不带类型信息的序列化（常用）
	 * @param value
	 * @return
	 */
	public static String toJsonString(Object value) {
		try {
			String json = objectMapper.writeValueAsString(value);
			return json;
		} catch (JsonProcessingException e) {
			throw new RuntimeException("json 序列化异常:" + value, e);
		}
	}

	/** 
	 * 对象转成json字符串，带有类的类型信息
	 * 方便反序列化时获取精确类型。 （特殊场景使用）
	 * @param value
	 * @return
	 */
	public static String toJsonStringWithType(Object value) {
		try {
			String json = objectMapperWithType.writeValueAsString(value);
			return json;
		} catch (JsonProcessingException e) {
			throw new RuntimeException("json 序列化异常:" + value, e);
		}
	}

	@Deprecated
	static final Gson gson = new Gson();

	public static String toJsonStr(Object val) {
		return gson.toJson(val).toString();
	}

	/** 
	 * 解析json字符串成对象，需要注意正确的对象类型
	 * @param <T>
	 * @param value
	 * @param valueType
	 * @return
	 */
	public static <T> T parseObject(String value, Class<T> valueType) {
		try {
			return objectMapper.readValue(value, valueType);
		} catch (JsonMappingException e) {
			throw new RuntimeException("json 反序列化异常:" + value, e);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("json 反序列化异常:" + value, e);
		}
	}

	public static <T> T parseObject(String value, TypeReference<T> typeReference) {
		try {
			return objectMapper.readValue(value, typeReference);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("json 反序列化异常:" + value, e);
		}
	}

	/**
	 * 反序列化带有类型信息的json字符串
	 * @param value json字符串
	 * @return 反序列化后的对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parseObjectWithType(String value) {
		try {
			return (T) objectMapperWithType.readValue(value, Object.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("json 反序列化异常:" + value, e);
		}
	}

	public static JsonObject parserJson(String jsonStr) throws Exception {
		return gson.fromJson(jsonStr, JsonObject.class);
	}

	// 自定义 BitSet 序列化器
	public static class BitSetSerializer extends JsonSerializer<BitSet> {
		@Override
		public void serialize(BitSet value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
			gen.writeBinary(value.toByteArray());
		}

		@Override
		public void serializeWithType(BitSet value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer)
				throws IOException {
			serialize(value, g, provider);
		}
	}

	// 自定义 BitSet 反序列化器
	public static class BitSetDeserializer extends JsonDeserializer<BitSet> {
		@Override
		public BitSet deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			return BitSet.valueOf(p.getBinaryValue());
		}

		public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer)
				throws IOException, JacksonException {
			return deserialize(p, ctxt);
		}
	}

	public static class MultiKeyMapSerializer extends JsonSerializer<MultiKeyMap> {

		@Override
		public void serialize(MultiKeyMap value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			gen.writeStartArray();
			for (Object key : value.keySet()) {
				MultiKey mKey = (MultiKey) key;
				gen.writeObject(mKey.getKeys());
				gen.writeObject(value.get(key));
			}
			gen.writeEndArray();
		}

		@Override
		public void serializeWithType(MultiKeyMap value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer)
				throws IOException {
			serialize(value, gen, provider);
		}
	}

	public static class MultiKeyMapDeserializer extends JsonDeserializer<MultiKeyMap> {

		@Override
		public MultiKeyMap deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
			MultiKeyMap multiKeyMap = new MultiKeyMap();

			// 遍历JSON数组中的元素
			while (p.nextToken() != JsonToken.END_ARRAY) {
				// 读取当前元素的键和值
				Object[] keys = p.readValueAs(Object[].class);
				p.nextToken();
				Object value = p.readValueAs(Object.class);

				// 创建 MultiKey 对象并添加到 MultiKeyMap 中
				MultiKey mKey = new MultiKey(keys);
				multiKeyMap.put(mKey, value);
			}
			return multiKeyMap;
		}

		public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer)
				throws IOException, JacksonException {
			return deserialize(p, ctxt);
		}
	}

	public static class MultiKeyMapSerializer4<K, V> extends JsonSerializer<org.apache.commons.collections4.map.MultiKeyMap<K, V>> {

		@Override
		public void serialize(org.apache.commons.collections4.map.MultiKeyMap<K, V> value, JsonGenerator gen,
				SerializerProvider serializers) throws IOException {
			gen.writeStartArray();
			for (org.apache.commons.collections4.keyvalue.MultiKey<? extends K> key : value.keySet()) {
				gen.writeObject(key.getKeys());
				gen.writeObject(value.get(key));
			}
			gen.writeEndArray();
		}

		@Override
		public void serializeWithType(org.apache.commons.collections4.map.MultiKeyMap<K, V> value, JsonGenerator gen,
				SerializerProvider provider, TypeSerializer typeSer) throws IOException {
			serialize(value, gen, provider);
		}
	}

	public static class MultiKeyMapDeserializer4<K, V> extends JsonDeserializer<org.apache.commons.collections4.map.MultiKeyMap<K, V>> {

		@Override
		public org.apache.commons.collections4.map.MultiKeyMap<K, V> deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException {
			org.apache.commons.collections4.map.MultiKeyMap<K, V> multiKeyMap = new org.apache.commons.collections4.map.MultiKeyMap<K, V>();

			// 遍历JSON数组中的元素
			while (p.nextToken() != JsonToken.END_ARRAY) {
				// 读取当前元素的键和值
				Object[] keys = p.readValueAs(Object[].class);
				p.nextToken();
				Object value = p.readValueAs(Object.class);

				// 创建 MultiKey 对象并添加到 MultiKeyMap 中
				org.apache.commons.collections4.keyvalue.MultiKey<? extends K> mKey = new org.apache.commons.collections4.keyvalue.MultiKey(
						keys);
				multiKeyMap.put(mKey, (V) value);
			}
			return multiKeyMap;
		}

		public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer)
				throws IOException, JacksonException {
			return deserialize(p, ctxt);
		}
	}

	public static ObjectMapper getObjectMapper() {
		return objectMapper;
	}
}
