package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 单充活动
 * 
 * 工具生成的，不要手动修改
 */
 public class SingleChargeActivityConfig {

	/** 索引 */
	public final int ID;		
	/** 活动ID */
	public final int ActivityiD;		
	/** 顺序 */
	public final int Type;		
	/** 前置条件 */
	public final int[] Preconditions;		
	/** 售价 */
	public final int[] Price;		
	/** 礼包ID组 礼包表 */
	public final int[] BundleID;		

	public SingleChargeActivityConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 索引
		ActivityiD = Integer.parseInt(element.getAttribute("ActivityiD") == null || element.getAttribute("ActivityiD").length() == 0 ? "0"
			: element.getAttribute("ActivityiD")); // 活动ID
		Type = Integer.parseInt(element.getAttribute("Type") == null || element.getAttribute("Type").length() == 0 ? "0"
			: element.getAttribute("Type")); // 顺序
		String PreconditionsString = element.getAttribute("Preconditions"); // 前置条件
		if (PreconditionsString != null && PreconditionsString.length() > 0) {
			String[] PreconditionsStrings = PreconditionsString.split(";"); 
			int[] PreconditionsTemp = new int[PreconditionsStrings.length] ; 
			for (int i = 0; i < PreconditionsStrings.length; i++) {
				int temp = Integer.parseInt(PreconditionsStrings[i]);	
				PreconditionsTemp[i] = temp;
			}
			Preconditions = PreconditionsTemp ;			
		} else {
			Preconditions = new int[] {};
		}
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
		String BundleIDString = element.getAttribute("BundleID"); // 礼包ID组 礼包表
		if (BundleIDString != null && BundleIDString.length() > 0) {
			String[] BundleIDStrings = BundleIDString.split(";"); 
			int[] BundleIDTemp = new int[BundleIDStrings.length] ; 
			for (int i = 0; i < BundleIDStrings.length; i++) {
				int temp = Integer.parseInt(BundleIDStrings[i]);	
				BundleIDTemp[i] = temp;
			}
			BundleID = BundleIDTemp ;			
		} else {
			BundleID = new int[] {};
		}
	}
	

}
