package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 
 * 
 * 工具生成的，不要手动修改
 */
 public class TestConfig {

	/** ID */
	public final int ID;		
	/** 奖励物品 */
	public final int Reward;		
	/** 奖励物品 */
	public final int[] Rewards1;		
	/** 奖励物品 */
	public final int[][] Rewards;		
	/** 物品名字 */
	public final String Name;		
	/** 奖励物品的数量 */
	public final String[] RewardCount;		
	/** 奖励物品加数量(只客户端支持类型) */
	public final String[][] ReardList;		
	/** 浮点型数据测试 */
	public final int fMateId;		

	public TestConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		Reward = Integer.parseInt(element.getAttribute("Reward") == null || element.getAttribute("Reward").length() == 0 ? "0"
			: element.getAttribute("Reward")); // 奖励物品
		String Rewards1String = element.getAttribute("Rewards1"); // 奖励物品
		if (Rewards1String != null && Rewards1String.length() > 0) {
			String[] Rewards1Strings = Rewards1String.split(";"); 
			int[] Rewards1Temp = new int[Rewards1Strings.length] ; 
			for (int i = 0; i < Rewards1Strings.length; i++) {
				int temp = Integer.parseInt(Rewards1Strings[i]);	
				Rewards1Temp[i] = temp;
			}
			Rewards1 = Rewards1Temp ;			
		} else {
			Rewards1 = new int[] {};
		}
		String RewardsString = element.getAttribute("Rewards"); // 奖励物品
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
		Name = element.getAttribute("Name"); // 物品名字
		String RewardCountString = element.getAttribute("RewardCount"); // 奖励物品的数量
		if (RewardCountString != null && RewardCountString.length() > 0) {
			String[] RewardCountStrings = RewardCountString.split(";"); 
			String[] RewardCountTemp = new String[RewardCountStrings.length] ; 
			for (int i = 0; i < RewardCountStrings.length; i++) {
				String temp = RewardCountStrings[i];	
				RewardCountTemp[i] = temp;
			}
			RewardCount = RewardCountTemp ;			
		} else {
			RewardCount = new String[] {};
		}
		String ReardListString = element.getAttribute("ReardList"); // 奖励物品加数量(只客户端支持类型)
		if (ReardListString != null && ReardListString.length() > 0) {
			String[] ReardListStrings = ReardListString.split("\\|"); 
			String[][] ReardListTemp = new String[ReardListStrings.length][] ; 
			for (int i = 0; i < ReardListStrings.length; i++) {
				String[] ReardListStrings2 = ReardListStrings[i].split(";"); 
				String[] array = new String[ReardListStrings2.length];
				for (int j = 0; j < ReardListStrings2.length; j++) {
					String temp = ReardListStrings2[j];	
					array[j] = temp;
				}
				ReardListTemp[i] = array;
			}
			ReardList = ReardListTemp ;			
		} else {
			ReardList = new String[][] {};
		}
		fMateId = Integer.parseInt(element.getAttribute("fMateId") == null || element.getAttribute("fMateId").length() == 0 ? "0"
			: element.getAttribute("fMateId")); // 浮点型数据测试
	}
	

}
