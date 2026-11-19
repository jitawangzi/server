package cn.game.games.net.game.gm.proto;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.Parser;

import cn.game.protocol.protobuf.PbProtocol;

/**
 * 纯基于 protobuf-java 3.12.2 与 Jackson 的结构化 JSON 编解码器 (DC-03, DR-02, BR-04, EX-03)
 * 规避跨大版本 protobuf-java-util 依赖冲突，支持双向转换、snake_case/camelCase 兼容与路径级校验报错。
 *
 * @author shuyongqiang
 */
public class ProtoJsonCodec {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private ProtoJsonCodec() {}

	/**
	 * 将输入的 JSON 字符串或 Map 载荷反序列化为具体的 Protobuf Message 对象
	 *
	 * @param parser     Protobuf 解析器
	 * @param descriptor 目标消息 Descriptor
	 * @param payloadObj JSON 字符串或 Map 对象
	 * @return 强类型 Message 实例
	 */
	@SuppressWarnings("unchecked")
	public static Message fromJson(Parser<?> parser, Descriptors.Descriptor descriptor, Object payloadObj) {
		if (descriptor == null) {
			throw new IllegalArgumentException("Target protobuf descriptor cannot be null");
		}
		Map<String, Object> map;
		if (payloadObj == null) {
			map = Collections.emptyMap();
		} else if (payloadObj instanceof String str) {
			String trimmed = str.trim();
			if (trimmed.isEmpty() || "{}".equals(trimmed)) {
				map = Collections.emptyMap();
			} else {
				try {
					map = MAPPER.readValue(trimmed, LinkedHashMap.class);
				} catch (Exception e) {
					throw new IllegalArgumentException("Payload is not a valid JSON object: " + e.getMessage(), e);
				}
			}
		} else if (payloadObj instanceof Map<?, ?> m) {
			map = (Map<String, Object>) m;
		} else {
			throw new IllegalArgumentException("Unsupported payload type: " + payloadObj.getClass().getName());
		}

		Message defaultInstance;
		try {
			defaultInstance = (Message) parser.parseFrom(new byte[0]);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to obtain default message instance: " + e.getMessage(), e);
		}

		Message.Builder builder = defaultInstance.newBuilderForType();
		populateMessage(builder, descriptor, map, "");
		return builder.build();
	}

	@SuppressWarnings("unchecked")
	private static void populateMessage(Message.Builder builder, Descriptors.Descriptor descriptor, Map<String, Object> map, String pathPrefix) {
		Map<String, FieldDescriptor> fieldLookup = new LinkedHashMap<>();
		for (FieldDescriptor field : descriptor.getFields()) {
			fieldLookup.put(field.getName().toLowerCase(), field);
			fieldLookup.put(field.getJsonName().toLowerCase(), field);
		}

		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String rawKey = entry.getKey();
			Object value = entry.getValue();
			if (value == null) {
				continue;
			}
			String currentPath = pathPrefix.isEmpty() ? rawKey : pathPrefix + "." + rawKey;

			FieldDescriptor field = fieldLookup.get(rawKey.toLowerCase());
			if (field == null) {
				throw new IllegalArgumentException("Unknown field at path [" + currentPath + "] for message [" + descriptor.getFullName() + "]");
			}

			if (field.isMapField()) {
				if (!(value instanceof Map<?, ?> valMap)) {
					throw new IllegalArgumentException("Expected Map at path [" + currentPath + "], but got " + value.getClass().getSimpleName());
				}
				Descriptors.Descriptor entryDescriptor = field.getMessageType();
				FieldDescriptor keyField = entryDescriptor.findFieldByNumber(1);
				FieldDescriptor valField = entryDescriptor.findFieldByNumber(2);

				for (Map.Entry<?, ?> mapEntry : valMap.entrySet()) {
					Message.Builder entryBuilder = builder.newBuilderForField(field);
					Object parsedKey = parseScalarOrEnum(keyField, mapEntry.getKey(), currentPath + ".key");
					Object parsedVal = parseValue(entryBuilder, valField, mapEntry.getValue(), currentPath + "[" + mapEntry.getKey() + "]");
					if (parsedKey != null) {
						entryBuilder.setField(keyField, parsedKey);
					}
					if (parsedVal != null) {
						entryBuilder.setField(valField, parsedVal);
					}
					builder.addRepeatedField(field, entryBuilder.build());
				}
			} else if (field.isRepeated()) {
				if (!(value instanceof List<?> list)) {
					throw new IllegalArgumentException("Expected List at path [" + currentPath + "], but got " + value.getClass().getSimpleName());
				}
				for (int i = 0; i < list.size(); i++) {
					Object element = list.get(i);
					if (element != null) {
						Object parsedElement = parseValue(builder, field, element, currentPath + "[" + i + "]");
						builder.addRepeatedField(field, parsedElement);
					}
				}
			} else {
				Object parsed = parseValue(builder, field, value, currentPath);
				if (parsed != null) {
					builder.setField(field, parsed);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static Object parseValue(Message.Builder parentBuilder, FieldDescriptor field, Object value, String path) {
		if (field.getJavaType() == FieldDescriptor.JavaType.MESSAGE) {
			if (!(value instanceof Map<?, ?> subMap)) {
				throw new IllegalArgumentException("Expected Object at path [" + path + "], but got " + value.getClass().getSimpleName());
			}
			Message.Builder subBuilder = parentBuilder.newBuilderForField(field);
			populateMessage(subBuilder, field.getMessageType(), (Map<String, Object>) subMap, path);
			return subBuilder.build();
		}
		return parseScalarOrEnum(field, value, path);
	}

	private static Object parseScalarOrEnum(FieldDescriptor field, Object value, String path) {
		try {
			switch (field.getType()) {
				case INT32:
				case SINT32:
				case SFIXED32: {
					long v = parseLong(value, path);
					if (v < Integer.MIN_VALUE || v > Integer.MAX_VALUE) {
						throw new IllegalArgumentException("Value out of int32 range at path [" + path + "]: " + value);
					}
					return (int) v;
				}
				case UINT32:
				case FIXED32: {
					long v = parseLong(value, path);
					if (v < 0 || v > 0xFFFFFFFFL) {
						throw new IllegalArgumentException("Value out of uint32 range at path [" + path + "]: " + value);
					}
					return (int) v;
				}
				case INT64:
				case SINT64:
				case SFIXED64: {
					return parseLong(value, path);
				}
				case UINT64:
				case FIXED64: {
					return parseUnsignedLong(value, path);
				}
				case FLOAT: {
					if (value instanceof Number n) {
						return n.floatValue();
					}
					return Float.parseFloat(value.toString());
				}
				case DOUBLE: {
					if (value instanceof Number n) {
						return n.doubleValue();
					}
					return Double.parseDouble(value.toString());
				}
				case BOOL: {
					if (value instanceof Boolean b) {
						return b;
					}
					return Boolean.parseBoolean(value.toString());
				}
				case STRING: {
					return value.toString();
				}
				case BYTES: {
					if (value instanceof byte[] bytes) {
						return ByteString.copyFrom(bytes);
					}
					String str = value.toString();
					try {
						return ByteString.copyFrom(Base64.getDecoder().decode(str));
					} catch (IllegalArgumentException e) {
						return ByteString.copyFromUtf8(str);
					}
				}
				case ENUM: {
					Descriptors.EnumDescriptor enumDesc = field.getEnumType();
					if (value instanceof Number n) {
						EnumValueDescriptor ev = enumDesc.findValueByNumber(n.intValue());
						if (ev != null) {
							return ev;
						}
						throw new IllegalArgumentException("Unknown enum number [" + n + "] for [" + enumDesc.getName() + "] at path [" + path + "]");
					}
					String name = value.toString().trim();
					EnumValueDescriptor ev = enumDesc.findValueByName(name);
					if (ev == null) {
						for (EnumValueDescriptor candidate : enumDesc.getValues()) {
							if (candidate.getName().equalsIgnoreCase(name)) {
								ev = candidate;
								break;
							}
						}
					}
					if (ev != null) {
						return ev;
					}
					try {
						int num = Integer.parseInt(name);
						ev = enumDesc.findValueByNumber(num);
						if (ev != null) {
							return ev;
						}
					} catch (NumberFormatException ignored) {}
					throw new IllegalArgumentException("Unknown enum value [" + name + "] for [" + enumDesc.getName() + "] at path [" + path + "]");
				}
				default:
					throw new IllegalArgumentException("Unsupported field type [" + field.getType() + "] at path [" + path + "]");
			}
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to parse field at path [" + path + "]: " + e.getMessage(), e);
		}
	}

	private static long parseLong(Object value, String path) {
		if (value instanceof Number n) {
			return n.longValue();
		}
		try {
			return Long.parseLong(value.toString().trim());
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid integer at path [" + path + "]: " + value);
		}
	}

	private static long parseUnsignedLong(Object value, String path) {
		if (value instanceof Number n) {
			return n.longValue();
		}
		try {
			String str = value.toString().trim();
			BigInteger bi = new BigInteger(str);
			if (bi.signum() < 0 || bi.compareTo(new BigInteger("18446744073709551615")) > 0) {
				throw new IllegalArgumentException("Value out of uint64 range at path [" + path + "]: " + value);
			}
			return bi.longValue();
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid unsigned 64-bit integer at path [" + path + "]: " + value);
		}
	}

	/**
	 * 将 Message 对象递归转化为带有完整字段名与可读值的 LinkedHashMap
	 *
	 * @param message Protobuf 消息实例
	 * @return 结构化 Map 结果
	 */
	public static Map<String, Object> toMap(MessageOrBuilder message) {
		if (message == null) {
			return Collections.emptyMap();
		}
		Map<String, Object> result = new LinkedHashMap<>();
		Descriptors.Descriptor descriptor = message.getDescriptorForType();

		for (FieldDescriptor field : descriptor.getFields()) {
			String name = field.getName();
			if (field.isMapField()) {
				Map<String, Object> mapValue = new LinkedHashMap<>();
				int count = message.getRepeatedFieldCount(field);
				for (int i = 0; i < count; i++) {
					Message entryMsg = (Message) message.getRepeatedField(field, i);
					FieldDescriptor kf = entryMsg.getDescriptorForType().findFieldByNumber(1);
					FieldDescriptor vf = entryMsg.getDescriptorForType().findFieldByNumber(2);
					Object k = convertFieldValue(entryMsg.getField(kf));
					Object v = convertFieldValue(entryMsg.getField(vf));
					mapValue.put(String.valueOf(k), v);
				}
				result.put(name, mapValue);
			} else if (field.isRepeated()) {
				int count = message.getRepeatedFieldCount(field);
				List<Object> list = new ArrayList<>(count);
				for (int i = 0; i < count; i++) {
					list.add(convertFieldValue(message.getRepeatedField(field, i)));
				}
				result.put(name, list);
			} else {
				if (message.hasField(field)) {
					result.put(name, convertFieldValue(message.getField(field)));
				} else {
					result.put(name, convertFieldValue(field.getDefaultValue()));
				}
			}
		}
		return result;
	}

	private static Object convertFieldValue(Object val) {
		if (val == null) {
			return null;
		}
		if (val instanceof MessageOrBuilder mob) {
			return toMap(mob);
		}
		if (val instanceof EnumValueDescriptor evd) {
			return evd.getName();
		}
		if (val instanceof ByteString bs) {
			return Base64.getEncoder().encodeToString(bs.toByteArray());
		}
		if (val instanceof byte[] bytes) {
			return Base64.getEncoder().encodeToString(bytes);
		}
		return val;
	}
}
