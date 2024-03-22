package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 主城Buff表
 * 
 * 工具生成的，不要手动修改
 */
 public class MainCityBuffConfig {

	/** id */
	private int id;		
	/** 选项描述 */
	private String des;		
	/** 回合参数 */
	private int round;		
	/** 枚举 */
	private int mainCityBuffEnum;		
	/** ID参数 */
	private int idParam;		
	/** 数量参数 */
	private int numParam;		

	public MainCityBuffConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.des = element.getAttribute("des"); // 选项描述
		this.round = Integer.parseInt(element.getAttribute("round") == null || element.getAttribute("round").length() == 0 ? "0"
			: element.getAttribute("round")); // 回合参数
		this.mainCityBuffEnum = Integer.parseInt(element.getAttribute("mainCityBuffEnum") == null || element.getAttribute("mainCityBuffEnum").length() == 0 ? "0"
			: element.getAttribute("mainCityBuffEnum")); // 枚举
		this.idParam = Integer.parseInt(element.getAttribute("idParam") == null || element.getAttribute("idParam").length() == 0 ? "0"
			: element.getAttribute("idParam")); // ID参数
		this.numParam = Integer.parseInt(element.getAttribute("numParam") == null || element.getAttribute("numParam").length() == 0 ? "0"
			: element.getAttribute("numParam")); // 数量参数
	}
	
	public int getId() {
		return id;
	}
	
	public String getDes() {
		return des;
	}
	
	public int getRound() {
		return round;
	}
	
	public int getMainCityBuffEnum() {
		return mainCityBuffEnum;
	}
	
	public int getIdParam() {
		return idParam;
	}
	
	public int getNumParam() {
		return numParam;
	}
	
}
