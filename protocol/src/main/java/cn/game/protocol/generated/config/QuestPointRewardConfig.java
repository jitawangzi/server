package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 任务积分奖励
 * 
 * 工具生成的，不要手动修改
 */
 public class QuestPointRewardConfig {

	/** 任务类型： QuestTypeEnum表id */
	public final int ID;		
	/** 积分类型 Asset表id */
	public final int PointType;		
	/** 奖励需要的积分数量段 */
	public final int[] Stage;		
	/** 积分段对应的奖励 */
	public final int[][] Reward;		

	public QuestPointRewardConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 任务类型： QuestTypeEnum表id
		PointType = Integer.parseInt(element.getAttribute("PointType") == null || element.getAttribute("PointType").length() == 0 ? "0"
			: element.getAttribute("PointType")); // 积分类型 Asset表id
		String StageString = element.getAttribute("Stage"); // 奖励需要的积分数量段
		if (StageString != null && StageString.length() > 0) {
			String[] StageStrings = StageString.split(";"); 
			int[] StageTemp = new int[StageStrings.length] ; 
			for (int i = 0; i < StageStrings.length; i++) {
				int temp = Integer.parseInt(StageStrings[i]);	
				StageTemp[i] = temp;
			}
			Stage = StageTemp ;			
		} else {
			Stage = new int[] {};
		}
		String RewardString = element.getAttribute("Reward"); // 积分段对应的奖励
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
	}
	

}
