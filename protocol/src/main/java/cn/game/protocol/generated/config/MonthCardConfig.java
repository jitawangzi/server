package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 月卡
 * 
 * 工具生成的，不要手动修改
 */
 public class MonthCardConfig {

	/** 月卡id */
	public final int ID;		
	/** 月卡功能名 */
	public final String FuncName;		
	/** 售价 */
	public final int[] Price;		
	/** 有效期 默认=天数 0=永久 */
	public final int Expiration;		
	/** 购买奖励 */
	public final int[][] PurchaseRewards;		
	/** 每日领取 */
	public final int[] DailyRewards;		
	/** 福利 */
	public final int[][] Benefit1;		

	public MonthCardConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 月卡id
		FuncName = element.getAttribute("FuncName"); // 月卡功能名
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
		Expiration = Integer.parseInt(element.getAttribute("Expiration") == null || element.getAttribute("Expiration").length() == 0 ? "0"
			: element.getAttribute("Expiration")); // 有效期 默认=天数 0=永久
		String PurchaseRewardsString = element.getAttribute("PurchaseRewards"); // 购买奖励
		if (PurchaseRewardsString != null && PurchaseRewardsString.length() > 0) {
			String[] PurchaseRewardsStrings = PurchaseRewardsString.split("\\|"); 
			int[][] PurchaseRewardsTemp = new int[PurchaseRewardsStrings.length][] ; 
			for (int i = 0; i < PurchaseRewardsStrings.length; i++) {
				String[] PurchaseRewardsStrings2 = PurchaseRewardsStrings[i].split(";"); 
				int[] array = new int[PurchaseRewardsStrings2.length];
				for (int j = 0; j < PurchaseRewardsStrings2.length; j++) {
					int temp = Integer.parseInt(PurchaseRewardsStrings2[j]);	
					array[j] = temp;
				}
				PurchaseRewardsTemp[i] = array;
			}
			PurchaseRewards = PurchaseRewardsTemp ;			
		} else {
			PurchaseRewards = new int[][] {};
		}
		String DailyRewardsString = element.getAttribute("DailyRewards"); // 每日领取
		if (DailyRewardsString != null && DailyRewardsString.length() > 0) {
			String[] DailyRewardsStrings = DailyRewardsString.split(";"); 
			int[] DailyRewardsTemp = new int[DailyRewardsStrings.length] ; 
			for (int i = 0; i < DailyRewardsStrings.length; i++) {
				int temp = Integer.parseInt(DailyRewardsStrings[i]);	
				DailyRewardsTemp[i] = temp;
			}
			DailyRewards = DailyRewardsTemp ;			
		} else {
			DailyRewards = new int[] {};
		}
		String Benefit1String = element.getAttribute("Benefit1"); // 福利
		if (Benefit1String != null && Benefit1String.length() > 0) {
			String[] Benefit1Strings = Benefit1String.split("\\|"); 
			int[][] Benefit1Temp = new int[Benefit1Strings.length][] ; 
			for (int i = 0; i < Benefit1Strings.length; i++) {
				String[] Benefit1Strings2 = Benefit1Strings[i].split(";"); 
				int[] array = new int[Benefit1Strings2.length];
				for (int j = 0; j < Benefit1Strings2.length; j++) {
					int temp = Integer.parseInt(Benefit1Strings2[j]);	
					array[j] = temp;
				}
				Benefit1Temp[i] = array;
			}
			Benefit1 = Benefit1Temp ;			
		} else {
			Benefit1 = new int[][] {};
		}
	}
	

}
