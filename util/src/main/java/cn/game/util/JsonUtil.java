package cn.game.util;

import java.io.IOException;
import java.util.BitSet;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
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

/**    
 * 对jackson的一个封装
 * @date 2024年3月1日 下午5:14:33
 * @author SYQ
 */
public class JsonUtil {
	private static ObjectMapper objectMapper = new ObjectMapper();

	static {
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		objectMapper.disable(SerializationFeature.FAIL_ON_SELF_REFERENCES);
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

		SimpleModule module = new SimpleModule();
		module.addSerializer(BitSet.class, new BitSetSerializer());
		module.addDeserializer(BitSet.class, new BitSetDeserializer());
		objectMapper.registerModule(module);
        objectMapper.registerModule(new GuavaModule()); // 注册 Guava 模块

		// 写入类名
		PolymorphicTypeValidator ptv = LaissezFaireSubTypeValidator.instance;
		objectMapper.activateDefaultTyping(ptv, DefaultTyping.NON_FINAL);
//
		objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
				.withFieldVisibility(JsonAutoDetect.Visibility.ANY).withGetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withSetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
				.withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));
	}

	public static String toJsonString(Object value) {
		// 将 Map 对象序列化成 JSON 字符串
		try {
			String json = objectMapper.writeValueAsString(value);
			return json;
		} catch (JsonProcessingException e) {
			throw new RuntimeException("json 序列化异常:" + value, e);
		}
	}

	public static <T> T parseObject(String value, Class<T> valueType) {
		try {
			return objectMapper.readValue(value, valueType);
		} catch (JsonMappingException e) {
			throw new RuntimeException("json 反序列化异常:" + value, e);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("json 反序列化异常:" + value, e);
		}
	}

	// 自定义 BitSet 序列化器
	public static class BitSetSerializer extends JsonSerializer<BitSet> {
		@Override
		public void serialize(BitSet value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException, JsonProcessingException {
			gen.writeBinary(value.toByteArray());
		}

		@Override
		public void serializeWithType(BitSet value, JsonGenerator g, SerializerProvider provider,
				TypeSerializer typeSer) throws IOException {
	        serialize(value, g, provider);
		}
	}

	// 自定义 BitSet 反序列化器
	public static class BitSetDeserializer extends JsonDeserializer<BitSet> {
		@Override
		public BitSet deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			return BitSet.valueOf(p.getBinaryValue());
		}

		public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer)
				throws IOException, JacksonException {
			return deserialize(p, ctxt);
		}
	}
}
