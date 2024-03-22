package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 随机奖励组
 * 
 * 工具生成的，不要手动修改
 */
 public class RandomRewardGroupConfig {

	/** id */
	private final int id;		
	/** 组概率 -- 每个grounpid中的概率控制，达到概率可获得这条组的奖励 */
	private final int rate;		
	/** 随机奖励组id -- RandomReward表的groupId */
	private final int randomRewardGroupId;		
	/** 组id */
	private final int groupId;		

	public RandomRewardGroupConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.rate = Integer.parseInt(element.getAttribute("rate") == null || element.getAttribute("rate").length() == 0 ? "0"
			: element.getAttribute("rate")); // 组概率
		this.randomRewardGroupId = Integer.parseInt(element.getAttribute("randomRewardGroupId") == null || element.getAttribute("randomRewardGroupId").length() == 0 ? "0"
			: element.getAttribute("randomRewardGroupId")); // 随机奖励组id
		this.groupId = Integer.parseInt(element.getAttribute("groupId") == null || element.getAttribute("groupId").length() == 0 ? "0"
			: element.getAttribute("groupId")); // 组id
	}
	
	public int getId() {
		return id;
	}
	
	public int getRate() {
		return rate;
	}
	
	public int getRandomRewardGroupId() {
		return randomRewardGroupId;
	}
	
	public int getGroupId() {
		return groupId;
	}
	
}
