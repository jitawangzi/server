package cn.game.util;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 基础数据类
 *
 **/
public class ExcelData {

	@ExcelProperty("id")
	private Integer id;
	@ExcelProperty("版本")
	private Integer version;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

}