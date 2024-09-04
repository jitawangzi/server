package cn.game.simulation.util;

import com.alibaba.excel.annotation.ExcelProperty;

public class ExcelRowData {
	@ExcelProperty("序号")
	private Integer index;

	@ExcelProperty("模块")
	private String module;

	@ExcelProperty("协议")
	private String protocol;

	@ExcelProperty("协议号")
	private String protocolId;

	@ExcelProperty("权重")
	private Float weight;

	@ExcelProperty("描述")
	private String description;

	// Public no-arg constructor
	public ExcelRowData() {
	}

	// Getters and setters
	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getProtocolId() {
		return protocolId;
	}

	public void setProtocolId(String protocolId) {
		this.protocolId = protocolId;
	}

	public Float getWeight() {
		return weight;
	}

	public void setWeight(Float weight) {
		this.weight = weight;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
