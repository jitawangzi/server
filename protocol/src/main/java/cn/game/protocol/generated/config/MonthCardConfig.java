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
	public final String name;		
	/** 显示名称 */
	public final String viewName;		
	/** 获得条件id */
	public final int condition;		
	/** 售价 1=货币；货币id；数量 2=充值；数量 3=看广告 */
	public final int[] cost;		
	/** 有效期 默认=天数 0=永久 */
	public final int effectiveDays;		
	/** 购买奖励 */
	public final int[][] buyReward;		
	/** 每日领取 */
	public final int[][] dayReward;		

	public MonthCardConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 月卡id
		name = element.getAttribute("name"); // 月卡功能名
		viewName = element.getAttribute("viewName"); // 显示名称
		condition = Integer.parseInt(element.getAttribute("condition") == null || element.getAttribute("condition").length() == 0 ? "0"
			: element.getAttribute("condition")); // 获得条件id
		String costString = element.getAttribute("cost"); // 售价 1=货币；货币id；数量 2=充值；数量 3=看广告
		if (costString != null && costString.length() > 0) {
			String[] costStrings = costString.split(";"); 
			int[] costTemp = new int[costStrings.length] ; 
			for (int i = 0; i < costStrings.length; i++) {
				int temp = Integer.parseInt(costStrings[i]);	
				costTemp[i] = temp;
			}
			cost = costTemp ;			
		} else {
			cost = new int[] {};
		}
		effectiveDays = Integer.parseInt(element.getAttribute("effectiveDays") == null || element.getAttribute("effectiveDays").length() == 0 ? "0"
			: element.getAttribute("effectiveDays")); // 有效期 默认=天数 0=永久
		String buyRewardString = element.getAttribute("buyReward"); // 购买奖励
		if (buyRewardString != null && buyRewardString.length() > 0) {
			String[] buyRewardStrings = buyRewardString.split("\\|"); 
			int[][] buyRewardTemp = new int[buyRewardStrings.length][] ; 
			for (int i = 0; i < buyRewardStrings.length; i++) {
				String[] buyRewardStrings2 = buyRewardStrings[i].split(";"); 
				int[] array = new int[buyRewardStrings2.length];
				for (int j = 0; j < buyRewardStrings2.length; j++) {
					int temp = Integer.parseInt(buyRewardStrings2[j]);	
					array[j] = temp;
				}
				buyRewardTemp[i] = array;
			}
			buyReward = buyRewardTemp ;			
		} else {
			buyReward = new int[][] {};
		}
		String dayRewardString = element.getAttribute("dayReward"); // 每日领取
		if (dayRewardString != null && dayRewardString.length() > 0) {
			String[] dayRewardStrings = dayRewardString.split("\\|"); 
			int[][] dayRewardTemp = new int[dayRewardStrings.length][] ; 
			for (int i = 0; i < dayRewardStrings.length; i++) {
				String[] dayRewardStrings2 = dayRewardStrings[i].split(";"); 
				int[] array = new int[dayRewardStrings2.length];
				for (int j = 0; j < dayRewardStrings2.length; j++) {
					int temp = Integer.parseInt(dayRewardStrings2[j]);	
					array[j] = temp;
				}
				dayRewardTemp[i] = array;
			}
			dayReward = dayRewardTemp ;			
		} else {
			dayReward = new int[][] {};
		}
	}
	

}
