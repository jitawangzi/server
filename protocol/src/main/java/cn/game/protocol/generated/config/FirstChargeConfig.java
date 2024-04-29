package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 首充
 * 
 * 工具生成的，不要手动修改
 */
 public class FirstChargeConfig {

	/** ID */
	public final int ID;		
	/** 购买顺序 */
	public final int Order;		
	/** 包含物品 */
	public final int[][] Rewards;		
	/** 可选取数量 */
	public final int Cnt;		
	/** 必得物品 */
	public final int[][] Rewards2;		
	/** 售价 */
	public final int[] Price;		

	public FirstChargeConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		Order = Integer.parseInt(element.getAttribute("Order") == null || element.getAttribute("Order").length() == 0 ? "0"
			: element.getAttribute("Order")); // 购买顺序
		String RewardsString = element.getAttribute("Rewards"); // 包含物品
		if (RewardsString != null && RewardsString.length() > 0) {
			String[] RewardsStrings = RewardsString.split("\\|"); 
			int[][] RewardsTemp = new int[RewardsStrings.length][] ; 
			for (int i = 0; i < RewardsStrings.length; i++) {
				String[] RewardsStrings2 = RewardsStrings[i].split(";"); 
				int[] array = new int[RewardsStrings2.length];
				for (int j = 0; j < RewardsStrings2.length; j++) {
					int temp = Integer.parseInt(RewardsStrings2[j]);	
					array[j] = temp;
				}
				RewardsTemp[i] = array;
			}
			Rewards = RewardsTemp ;			
		} else {
			Rewards = new int[][] {};
		}
		Cnt = Integer.parseInt(element.getAttribute("Cnt") == null || element.getAttribute("Cnt").length() == 0 ? "0"
			: element.getAttribute("Cnt")); // 可选取数量
		String Rewards2String = element.getAttribute("Rewards2"); // 必得物品
		if (Rewards2String != null && Rewards2String.length() > 0) {
			String[] Rewards2Strings = Rewards2String.split("\\|"); 
			int[][] Rewards2Temp = new int[Rewards2Strings.length][] ; 
			for (int i = 0; i < Rewards2Strings.length; i++) {
				String[] Rewards2Strings2 = Rewards2Strings[i].split(";"); 
				int[] array = new int[Rewards2Strings2.length];
				for (int j = 0; j < Rewards2Strings2.length; j++) {
					int temp = Integer.parseInt(Rewards2Strings2[j]);	
					array[j] = temp;
				}
				Rewards2Temp[i] = array;
			}
			Rewards2 = Rewards2Temp ;			
		} else {
			Rewards2 = new int[][] {};
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
	}
	

}
