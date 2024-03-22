package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 奖励表
 * 
 * 工具生成的，不要手动修改
 */
 public class AwardConfig {

	/** 奖励ID */
	private final int ID;		
	/** 奖励组  配置：奖励类型;奖励id|奖励类型;奖励id|奖励类型;奖励id  奖励类型 1-必给物品 2-随机物品  奖励id-调用：MustAward#必给奖励、RandomAward#随机奖励 中id */
	private final int AwardGroup;		

	public AwardConfig (Element element) throws Exception {
	
		this.ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 奖励ID
		this.AwardGroup = Integer.parseInt(element.getAttribute("AwardGroup") == null || element.getAttribute("AwardGroup").length() == 0 ? "0"
			: element.getAttribute("AwardGroup")); // 奖励组  配置：奖励类型;奖励id|奖励类型;奖励id|奖励类型;奖励id  奖励类型 1-必给物品 2-随机物品  奖励id-调用：MustAward#必给奖励、RandomAward#随机奖励 中id
	}
	
	public int getID() {
		return ID;
	}
	
	public int getAwardGroup() {
		return AwardGroup;
	}
	
}
