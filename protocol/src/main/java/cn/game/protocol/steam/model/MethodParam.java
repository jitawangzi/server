package cn.game.protocol.steam.model;

import java.text.MessageFormat;

public class MethodParam {

	private String name;
	private String type;
	private boolean optional;
	private String description;
	private boolean isArray;
	private boolean isDefault0;
	private boolean isBool;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		if (this.name.indexOf("[0]") > -1) {
			this.name = this.name.replace("[0]", "");
			if (this.type != null) {
				this.type += "[]";
			}
			isArray = true;
		}
	}

	public String getType() {
		return type;
	}

	/** 
	 * 转成java用的类型
	 * @param type
	 */
	public void setType(String type) {
		if (type.indexOf("int32") > -1) {
			type = "int";
			isDefault0 = true;
		} else if (type.indexOf("int64") > -1) {
			type = "long";
			isDefault0 = true;
		} else if (type.equalsIgnoreCase("string")) {
			type = "String";
		} else if (type.equalsIgnoreCase("rawbinary")) {
			type = "byte[]";
		} else if (type.equalsIgnoreCase("bool")) {
			type = "boolean";
			isBool = true;
		} else if (type.equalsIgnoreCase("{message}")) {
			type = "JsonObject";
		} else if (type.equalsIgnoreCase("float")) {
			type = "float";
		} else if (type.equalsIgnoreCase("{enum}")) {
			isDefault0 = true;
			type = "int";
		}else {
			throw new IllegalArgumentException(MessageFormat.format("不支持的数据类型[{0}]", type));
		}

		if (isArray || this.name != null && this.name.indexOf("[0]") > -1) {
			type += "[]";
			isArray = true;
		}
		this.type = type;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isArray() {
		return isArray;
	}

	public void setArray(boolean isArray) {
		this.isArray = isArray;
	}

	public boolean isDefault0() {
		return isDefault0;
	}

	public void setDefault0(boolean isDefault0) {
		this.isDefault0 = isDefault0;
	}

	public boolean isBool() {
		return isBool;
	}

	public void setBool(boolean isBool) {
		this.isBool = isBool;
	}

}
