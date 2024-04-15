package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 龙技能
 * 
 * 工具生成的，不要手动修改
 */
 public class DragonSkillConfig {

	/** 龙id */
	public final int ID;		
	/** 龙技能名称 */
	public final String Name;		
	/** 龙技能tips */
	public final String Tips;		
	/** 图标Icon 文件名  同技能图标 */
	public final String Icon;		
	/** 解锁等级 取用户等级解锁 */
	public final int[] UnlockingLv;		
	/** 升级属性奖励    暴击伤害%加成  除以10000用 */
	public final int[] UpgradeAttributeAward;		
	/** 激活肉鸽id组   配置：等级；激活的肉鸽id1|...|等级；激活肉鸽idn  调用：Almost-技能&肉鸽——Roguelike#肉鸽表 */
	public final int[][] RoguelikeId;		
	/** 龙技能每级升级增量 消耗id  调用：【Almost配置表_通用_消耗表】—【消耗表#Consume】中ID  该级升级所需=lv*本列值 */
	public final int[] ConsumeId;		

	public DragonSkillConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 龙id
		Name = element.getAttribute("Name"); // 龙技能名称
		Tips = element.getAttribute("Tips"); // 龙技能tips
		Icon = element.getAttribute("Icon"); // 图标Icon 文件名  同技能图标
		String UnlockingLvString = element.getAttribute("UnlockingLv"); // 解锁等级 取用户等级解锁
		if (UnlockingLvString != null && UnlockingLvString.length() > 0) {
			String[] UnlockingLvStrings = UnlockingLvString.split(";"); 
			int[] UnlockingLvTemp = new int[UnlockingLvStrings.length] ; 
			for (int i = 0; i < UnlockingLvStrings.length; i++) {
				int temp = Integer.parseInt(UnlockingLvStrings[i]);	
				UnlockingLvTemp[i] = temp;
			}
			UnlockingLv = UnlockingLvTemp ;			
		} else {
			UnlockingLv = new int[] {};
		}
		String UpgradeAttributeAwardString = element.getAttribute("UpgradeAttributeAward"); // 升级属性奖励    暴击伤害%加成  除以10000用
		if (UpgradeAttributeAwardString != null && UpgradeAttributeAwardString.length() > 0) {
			String[] UpgradeAttributeAwardStrings = UpgradeAttributeAwardString.split(";"); 
			int[] UpgradeAttributeAwardTemp = new int[UpgradeAttributeAwardStrings.length] ; 
			for (int i = 0; i < UpgradeAttributeAwardStrings.length; i++) {
				int temp = Integer.parseInt(UpgradeAttributeAwardStrings[i]);	
				UpgradeAttributeAwardTemp[i] = temp;
			}
			UpgradeAttributeAward = UpgradeAttributeAwardTemp ;			
		} else {
			UpgradeAttributeAward = new int[] {};
		}
		String RoguelikeIdString = element.getAttribute("RoguelikeId"); // 激活肉鸽id组   配置：等级；激活的肉鸽id1|...|等级；激活肉鸽idn  调用：Almost-技能&肉鸽——Roguelike#肉鸽表
		if (RoguelikeIdString != null && RoguelikeIdString.length() > 0) {
			String[] RoguelikeIdStrings = RoguelikeIdString.split("\\|"); 
			int[][] RoguelikeIdTemp = new int[RoguelikeIdStrings.length][] ; 
			for (int i = 0; i < RoguelikeIdStrings.length; i++) {
				String[] RoguelikeIdStrings2 = RoguelikeIdStrings[i].split(";"); 
				int[] array = new int[RoguelikeIdStrings2.length];
				for (int j = 0; j < RoguelikeIdStrings2.length; j++) {
					int temp = Integer.parseInt(RoguelikeIdStrings2[j]);	
					array[j] = temp;
				}
				RoguelikeIdTemp[i] = array;
			}
			RoguelikeId = RoguelikeIdTemp ;			
		} else {
			RoguelikeId = new int[][] {};
		}
		String ConsumeIdString = element.getAttribute("ConsumeId"); // 龙技能每级升级增量 消耗id  调用：【Almost配置表_通用_消耗表】—【消耗表#Consume】中ID  该级升级所需=lv*本列值
		if (ConsumeIdString != null && ConsumeIdString.length() > 0) {
			String[] ConsumeIdStrings = ConsumeIdString.split(";"); 
			int[] ConsumeIdTemp = new int[ConsumeIdStrings.length] ; 
			for (int i = 0; i < ConsumeIdStrings.length; i++) {
				int temp = Integer.parseInt(ConsumeIdStrings[i]);	
				ConsumeIdTemp[i] = temp;
			}
			ConsumeId = ConsumeIdTemp ;			
		} else {
			ConsumeId = new int[] {};
		}
	}
	

}
