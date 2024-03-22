package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 宝箱空缺率
 * 
 * 工具生成的，不要手动修改
 */
 public class RPBoxVacancyRateParaConfig {

	/** id */
	private final int id;		
	/** 最小值 -- 最小值>= */
	private final float rateMin;		
	/** 最大值 -- 最大值<= */
	private final float rateMax;		
	/** 系数 -- 空缺率系数变化 */
	private final float para;		

	public RPBoxVacancyRateParaConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.rateMin = Float.parseFloat(element.getAttribute("rateMin") == null || element.getAttribute("rateMin").length() == 0 ? "0"
			: element.getAttribute("rateMin")); // 最小值
		this.rateMax = Float.parseFloat(element.getAttribute("rateMax") == null || element.getAttribute("rateMax").length() == 0 ? "0"
			: element.getAttribute("rateMax")); // 最大值
		this.para = Float.parseFloat(element.getAttribute("para") == null || element.getAttribute("para").length() == 0 ? "0"
			: element.getAttribute("para")); // 系数
	}
	
	public int getId() {
		return id;
	}
	
	public float getRateMin() {
		return rateMin;
	}
	
	public float getRateMax() {
		return rateMax;
	}
	
	public float getPara() {
		return para;
	}
	
}
