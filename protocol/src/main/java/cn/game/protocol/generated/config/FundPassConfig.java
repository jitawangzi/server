package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 基金通行证
 * 
 * 工具生成的，不要手动修改
 */
 public class FundPassConfig {

	/** ID */
	public final int ID;		
	/** 活动ID */
	public final int ActivityiD;		
	/** 是否需要经验升级 */
	public final boolean Exp;		
	/** 售价 */
	public final int[] Price;		

	public FundPassConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		ActivityiD = Integer.parseInt(element.getAttribute("ActivityiD") == null || element.getAttribute("ActivityiD").length() == 0 ? "0"
			: element.getAttribute("ActivityiD")); // 活动ID
		Exp = Boolean.parseBoolean(element.getAttribute("Exp") == null || element.getAttribute("Exp").length() == 0 ? "false"
			: element.getAttribute("Exp")); // 是否需要经验升级
		String PriceString = element.getAttribute("Price"); // 售价
		if (PriceString != null && PriceString.length() > 0) {
			String[] PriceStrings = PriceString.split(";"); 
			int[] PriceTemp = new int[PriceStrings.length] ; 
			for (int i = 0; i < PriceStrings.length; i++) {
				int temp = Integer.parseInt(PriceStrings[i]);	
				PriceTemp[i] = temp;
			}
			Price = PriceTemp ;			
		} else {
			Price = new int[] {};
		}
	}
	

}
