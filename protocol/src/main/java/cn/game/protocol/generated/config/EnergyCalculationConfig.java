package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 能量计算
 * 
 * 工具生成的，不要手动修改
 */
 public class EnergyCalculationConfig {

	/** 索引 顺延 */
	public final int ID;		

	public EnergyCalculationConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 索引 顺延
	}
	

}
