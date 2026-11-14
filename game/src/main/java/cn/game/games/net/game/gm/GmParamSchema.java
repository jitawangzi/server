package cn.game.games.net.game.gm;

import java.util.List;

/**
 * GM动作参数元信息描述模型
 *
 * @author shuyongqiang
 */
public class GmParamSchema {

	private String name;
	private String javaType;
	private GmParamType paramType;
	private boolean required;
	private String defaultValue;
	private String description;
	private String example;
	private String ref;
	private List<String> options;
	private List<String> enumValues;

	public GmParamSchema() {
	}

	public GmParamSchema(String name, String javaType, GmParamType paramType, boolean required, String defaultValue,
			String description, String example, List<String> enumValues) {
		this(name, javaType, paramType, required, defaultValue, description, example, "", enumValues);
	}

	public GmParamSchema(String name, String javaType, GmParamType paramType, boolean required, String defaultValue,
			String description, String example, String ref, List<String> options) {
		this.name = name;
		this.javaType = javaType;
		this.paramType = paramType;
		this.required = required;
		this.defaultValue = defaultValue;
		this.description = description;
		this.example = example;
		this.ref = ref;
		this.options = options;
		this.enumValues = options;
	}

	public String getName() {
		return name;
	}

	public String getJavaType() {
		return javaType;
	}

	public GmParamType getParamType() {
		return paramType;
	}

	public boolean isRequired() {
		return required;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public String getDescription() {
		return description;
	}

	public String getExample() {
		return example;
	}

	public String getRef() {
		return ref;
	}

	public List<String> getOptions() {
		return options;
	}

	public List<String> getEnumValues() {
		return enumValues;
	}
}