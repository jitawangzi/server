import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Example {

	public static class MyObject {
		private String name;

		// 该属性只进行反序列化，不进行序列化
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private String onlyDeserialize;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getOnlyDeserialize() {
			return onlyDeserialize;
		}

		@JsonSetter("onlyDeserialize")
		public void setOnlyDeserialize(String onlyDeserialize) {
			this.onlyDeserialize = onlyDeserialize;
		}
	}

	public static void main(String[] args) throws IOException {
		ObjectMapper mapper = new ObjectMapper();

		// JSON 字符串包含 onlyDeserialize 属性
		String json = "{\"name\":\"Test\",\"onlyDeserialize\":\"This will only be deserialized\"}";

		// 反序列化 JSON 字符串到 MyObject 实例
		MyObject obj = mapper.readValue(json, MyObject.class);

		// 输出反序列化后的对象
		System.out.println("Name: " + obj.getName());
		System.out.println("Only Deserialize: " + obj.getOnlyDeserialize());

		// 序列化 MyObject 实例到 JSON 字符串
		String serializedJson = mapper.writeValueAsString(obj);
		System.out.println("Serialized JSON: " + serializedJson);
	}
}
