package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 基金通行证奖励
 * 
 * 工具生成的，不要手动修改
 */
 public class FundPassRewardsConfig {

	/** ID */
	public final int ID;		
	/** 索引 */
	public final int Index;		
	/** 等级 */
	public final int Lv;		
	/** 解锁消耗 0=自动解锁不消耗 1=货币；货币id；数量 2=玩家等级 3=开启主线章节 */
	public final int[] UnlockCost;		
	/** 免费奖励 */
	public final int[] FreeRewards;		
	/** 付费奖励 */
	public final int[][] PaidRewards;		

	public FundPassRewardsConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		Index = Integer.parseInt(element.getAttribute("Index") == null || element.getAttribute("Index").length() == 0 ? "0"
			: element.getAttribute("Index")); // 索引
		Lv = Integer.parseInt(element.getAttribute("Lv") == null || element.getAttribute("Lv").length() == 0 ? "0"
			: element.getAttribute("Lv")); // 等级
		String UnlockCostString = element.getAttribute("UnlockCost"); // 解锁消耗 0=自动解锁不消耗 1=货币；货币id；数量 2=玩家等级 3=开启主线章节
		if (UnlockCostString != null && UnlockCostString.length() > 0) {
			String[] UnlockCostStrings = UnlockCostString.split(";"); 
			int[] UnlockCostTemp = new int[UnlockCostStrings.length] ; 
			for (int i = 0; i < UnlockCostStrings.length; i++) {
				int temp = Integer.parseInt(UnlockCostStrings[i]);	
				UnlockCostTemp[i] = temp;
			}
			UnlockCost = UnlockCostTemp ;			
		} else {
			UnlockCost = new int[] {};
		}
		String FreeRewardsString = element.getAttribute("FreeRewards"); // 免费奖励
		if (FreeRewardsString != null && FreeRewardsString.length() > 0) {
			String[] FreeRewardsStrings = FreeRewardsString.split(";"); 
			int[] FreeRewardsTemp = new int[FreeRewardsStrings.length] ; 
			for (int i = 0; i < FreeRewardsStrings.length; i++) {
				int temp = Integer.parseInt(FreeRewardsStrings[i]);	
				FreeRewardsTemp[i] = temp;
			}
			FreeRewards = FreeRewardsTemp ;			
		} else {
			FreeRewards = new int[] {};
		}
		String PaidRewardsString = element.getAttribute("PaidRewards"); // 付费奖励
		if (PaidRewardsString != null && PaidRewardsString.length() > 0) {
			String[] PaidRewardsStrings = PaidRewardsString.split("\\|"); 
			int[][] PaidRewardsTemp = new int[PaidRewardsStrings.length][] ; 
			for (int i = 0; i < PaidRewardsStrings.length; i++) {
				String[] PaidRewardsStrings2 = PaidRewardsStrings[i].split(";"); 
				int[] array = new int[PaidRewardsStrings2.length];
				for (int j = 0; j < PaidRewardsStrings2.length; j++) {
					int temp = Integer.parseInt(PaidRewardsStrings2[j]);	
					array[j] = temp;
				}
				PaidRewardsTemp[i] = array;
			}
			PaidRewards = PaidRewardsTemp ;			
		} else {
			PaidRewards = new int[][] {};
		}
	}
	

}
