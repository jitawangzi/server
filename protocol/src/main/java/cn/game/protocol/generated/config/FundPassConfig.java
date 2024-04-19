package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 基金通行证
 * 
 * 工具生成的，不要手动修改
 */
 public class FundPassConfig {

	/** 索引 */
	public final int ID;		
	/** 活动ID */
	public final int ActivityiD;		
	/** 售价 */
	public final int[] Price;		
	/** 奖励 */
	public final int BundleID;		

	public FundPassConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 索引
		ActivityiD = Integer.parseInt(element.getAttribute("ActivityiD") == null || element.getAttribute("ActivityiD").length() == 0 ? "0"
			: element.getAttribute("ActivityiD")); // 活动ID
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
		BundleID = Integer.parseInt(element.getAttribute("BundleID") == null || element.getAttribute("BundleID").length() == 0 ? "0"
			: element.getAttribute("BundleID")); // 奖励
	}
	

}
