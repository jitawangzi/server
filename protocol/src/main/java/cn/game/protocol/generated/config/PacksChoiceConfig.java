package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 自选礼包
 * 
 * 工具生成的，不要手动修改
 */
 public class PacksChoiceConfig {

	/** id */
	public final int ID;		
	/** 必含物品 */
	public final int[][] Reward;		
	/** 自选物品1 */
	public final int[][] Reward1;		
	/** 自选物品2 */
	public final int[][] Reward2;		
	/** 自选物品3 */
	public final int[][] Reward3;		
	/** 购买条件ID */
	public final int Condition;		
	/** 折扣显示 万分比 */
	public final int Discount;		
	/** 限购次数 */
	public final int PurchasesNum;		
	/** 售价 */
	public final int[] Price;		

	public PacksChoiceConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // id
		String RewardString = element.getAttribute("Reward"); // 必含物品
		if (RewardString != null && RewardString.length() > 0) {
			String[] RewardStrings = RewardString.split("\\|"); 
			int[][] RewardTemp = new int[RewardStrings.length][] ; 
			for (int i = 0; i < RewardStrings.length; i++) {
				String[] RewardStrings2 = RewardStrings[i].split(";"); 
				int[] array = new int[RewardStrings2.length];
				for (int j = 0; j < RewardStrings2.length; j++) {
					int temp = Integer.parseInt(RewardStrings2[j]);	
					array[j] = temp;
				}
				RewardTemp[i] = array;
			}
			Reward = RewardTemp ;			
		} else {
			Reward = new int[][] {};
		}
		String Reward1String = element.getAttribute("Reward1"); // 自选物品1
		if (Reward1String != null && Reward1String.length() > 0) {
			String[] Reward1Strings = Reward1String.split("\\|"); 
			int[][] Reward1Temp = new int[Reward1Strings.length][] ; 
			for (int i = 0; i < Reward1Strings.length; i++) {
				String[] Reward1Strings2 = Reward1Strings[i].split(";"); 
				int[] array = new int[Reward1Strings2.length];
				for (int j = 0; j < Reward1Strings2.length; j++) {
					int temp = Integer.parseInt(Reward1Strings2[j]);	
					array[j] = temp;
				}
				Reward1Temp[i] = array;
			}
			Reward1 = Reward1Temp ;			
		} else {
			Reward1 = new int[][] {};
		}
		String Reward2String = element.getAttribute("Reward2"); // 自选物品2
		if (Reward2String != null && Reward2String.length() > 0) {
			String[] Reward2Strings = Reward2String.split("\\|"); 
			int[][] Reward2Temp = new int[Reward2Strings.length][] ; 
			for (int i = 0; i < Reward2Strings.length; i++) {
				String[] Reward2Strings2 = Reward2Strings[i].split(";"); 
				int[] array = new int[Reward2Strings2.length];
				for (int j = 0; j < Reward2Strings2.length; j++) {
					int temp = Integer.parseInt(Reward2Strings2[j]);	
					array[j] = temp;
				}
				Reward2Temp[i] = array;
			}
			Reward2 = Reward2Temp ;			
		} else {
			Reward2 = new int[][] {};
		}
		String Reward3String = element.getAttribute("Reward3"); // 自选物品3
		if (Reward3String != null && Reward3String.length() > 0) {
			String[] Reward3Strings = Reward3String.split("\\|"); 
			int[][] Reward3Temp = new int[Reward3Strings.length][] ; 
			for (int i = 0; i < Reward3Strings.length; i++) {
				String[] Reward3Strings2 = Reward3Strings[i].split(";"); 
				int[] array = new int[Reward3Strings2.length];
				for (int j = 0; j < Reward3Strings2.length; j++) {
					int temp = Integer.parseInt(Reward3Strings2[j]);	
					array[j] = temp;
				}
				Reward3Temp[i] = array;
			}
			Reward3 = Reward3Temp ;			
		} else {
			Reward3 = new int[][] {};
		}
		Condition = Integer.parseInt(element.getAttribute("Condition") == null || element.getAttribute("Condition").length() == 0 ? "0"
			: element.getAttribute("Condition")); // 购买条件ID
		Discount = Integer.parseInt(element.getAttribute("Discount") == null || element.getAttribute("Discount").length() == 0 ? "0"
			: element.getAttribute("Discount")); // 折扣显示 万分比
		PurchasesNum = Integer.parseInt(element.getAttribute("PurchasesNum") == null || element.getAttribute("PurchasesNum").length() == 0 ? "0"
			: element.getAttribute("PurchasesNum")); // 限购次数
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
