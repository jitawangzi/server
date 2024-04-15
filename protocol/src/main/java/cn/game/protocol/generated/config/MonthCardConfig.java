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
	/** 获得条件id */
	public final int[] ConditionID;		
	/** 售价 */
	public final int[] Price;		
	/** 有效期 默认=天数 0=永久 */
	public final int Expiration;		
	/** 购买奖励 */
	public final int[][] PurchaseRewards;		
	/** 每日领取 */
	public final int[] DailyRewards;		
	/** 福利-1 */
	public final int[] Benefit1;		
	/** 福利-2 */
	public final int[] Benefit2;		
	/** 福利-3 */
	public final int[] Benefit3;		
	/** 福利-4 */
	public final int[] Benefit4;		
	/** 福利-5 */
	public final int[] Benefit5;		

	public MonthCardConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 月卡id
		FuncName = element.getAttribute("FuncName"); // 月卡功能名
		String ConditionIDString = element.getAttribute("ConditionID"); // 获得条件id
		if (ConditionIDString != null && ConditionIDString.length() > 0) {
			String[] ConditionIDStrings = ConditionIDString.split(";"); 
			int[] ConditionIDTemp = new int[ConditionIDStrings.length] ; 
			for (int i = 0; i < ConditionIDStrings.length; i++) {
				int temp = Integer.parseInt(ConditionIDStrings[i]);	
				ConditionIDTemp[i] = temp;
			}
			ConditionID = ConditionIDTemp ;			
		} else {
			ConditionID = new int[] {};
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
		String Benefit1String = element.getAttribute("Benefit1"); // 福利-1
		if (Benefit1String != null && Benefit1String.length() > 0) {
			String[] Benefit1Strings = Benefit1String.split(";"); 
			int[] Benefit1Temp = new int[Benefit1Strings.length] ; 
			for (int i = 0; i < Benefit1Strings.length; i++) {
				int temp = Integer.parseInt(Benefit1Strings[i]);	
				Benefit1Temp[i] = temp;
			}
			Benefit1 = Benefit1Temp ;			
		} else {
			Benefit1 = new int[] {};
		}
		String Benefit2String = element.getAttribute("Benefit2"); // 福利-2
		if (Benefit2String != null && Benefit2String.length() > 0) {
			String[] Benefit2Strings = Benefit2String.split(";"); 
			int[] Benefit2Temp = new int[Benefit2Strings.length] ; 
			for (int i = 0; i < Benefit2Strings.length; i++) {
				int temp = Integer.parseInt(Benefit2Strings[i]);	
				Benefit2Temp[i] = temp;
			}
			Benefit2 = Benefit2Temp ;			
		} else {
			Benefit2 = new int[] {};
		}
		String Benefit3String = element.getAttribute("Benefit3"); // 福利-3
		if (Benefit3String != null && Benefit3String.length() > 0) {
			String[] Benefit3Strings = Benefit3String.split(";"); 
			int[] Benefit3Temp = new int[Benefit3Strings.length] ; 
			for (int i = 0; i < Benefit3Strings.length; i++) {
				int temp = Integer.parseInt(Benefit3Strings[i]);	
				Benefit3Temp[i] = temp;
			}
			Benefit3 = Benefit3Temp ;			
		} else {
			Benefit3 = new int[] {};
		}
		String Benefit4String = element.getAttribute("Benefit4"); // 福利-4
		if (Benefit4String != null && Benefit4String.length() > 0) {
			String[] Benefit4Strings = Benefit4String.split(";"); 
			int[] Benefit4Temp = new int[Benefit4Strings.length] ; 
			for (int i = 0; i < Benefit4Strings.length; i++) {
				int temp = Integer.parseInt(Benefit4Strings[i]);	
				Benefit4Temp[i] = temp;
			}
			Benefit4 = Benefit4Temp ;			
		} else {
			Benefit4 = new int[] {};
		}
		String Benefit5String = element.getAttribute("Benefit5"); // 福利-5
		if (Benefit5String != null && Benefit5String.length() > 0) {
			String[] Benefit5Strings = Benefit5String.split(";"); 
			int[] Benefit5Temp = new int[Benefit5Strings.length] ; 
			for (int i = 0; i < Benefit5Strings.length; i++) {
				int temp = Integer.parseInt(Benefit5Strings[i]);	
				Benefit5Temp[i] = temp;
			}
			Benefit5 = Benefit5Temp ;			
		} else {
			Benefit5 = new int[] {};
		}
	}
	

}
