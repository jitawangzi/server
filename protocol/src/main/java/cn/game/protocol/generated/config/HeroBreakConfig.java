package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 英雄突破
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroBreakConfig {

	/** 突破id */
	public final int ID;		
	/** 当前品质 1-白 2-绿 3-蓝 4-紫 5-金 6-红 7-彩 8-永恒 9-唯一 */
	public final int InitialQuality;		
	/** 星级  星级上限5星 */
	public final int Star;		
	/** 消耗 同名卡数量 */
	public final int SameConsumeNum;		
	/** 消耗 同职业数量 */
	public final int CareerConsumeNum;		
	/** 重生返还的道具  战士蛋仔物品id;刺客蛋仔物品id;法师蛋仔物品id;牧师蛋仔物品id;射手蛋仔物品id  返还只会同品质 */
	public final int[] RebirthReturnItem;		
	/** 等级上限 */
	public final int LevelMax;		
	/** 突破一次性增加 属性id  属性%  调用AttributeVlalue#属性数值表id */
	public final int BreakOneTime;		

	public HeroBreakConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 突破id
		InitialQuality = Integer.parseInt(element.getAttribute("InitialQuality") == null || element.getAttribute("InitialQuality").length() == 0 ? "0"
			: element.getAttribute("InitialQuality")); // 当前品质 1-白 2-绿 3-蓝 4-紫 5-金 6-红 7-彩 8-永恒 9-唯一
		Star = Integer.parseInt(element.getAttribute("Star") == null || element.getAttribute("Star").length() == 0 ? "0"
			: element.getAttribute("Star")); // 星级  星级上限5星
		SameConsumeNum = Integer.parseInt(element.getAttribute("SameConsumeNum") == null || element.getAttribute("SameConsumeNum").length() == 0 ? "0"
			: element.getAttribute("SameConsumeNum")); // 消耗 同名卡数量
		CareerConsumeNum = Integer.parseInt(element.getAttribute("CareerConsumeNum") == null || element.getAttribute("CareerConsumeNum").length() == 0 ? "0"
			: element.getAttribute("CareerConsumeNum")); // 消耗 同职业数量
		String RebirthReturnItemString = element.getAttribute("RebirthReturnItem"); // 重生返还的道具  战士蛋仔物品id;刺客蛋仔物品id;法师蛋仔物品id;牧师蛋仔物品id;射手蛋仔物品id  返还只会同品质
		if (RebirthReturnItemString != null && RebirthReturnItemString.length() > 0) {
			String[] RebirthReturnItemStrings = RebirthReturnItemString.split(";"); 
			int[] RebirthReturnItemTemp = new int[RebirthReturnItemStrings.length] ; 
			for (int i = 0; i < RebirthReturnItemStrings.length; i++) {
				int temp = Integer.parseInt(RebirthReturnItemStrings[i]);	
				RebirthReturnItemTemp[i] = temp;
			}
			RebirthReturnItem = RebirthReturnItemTemp ;			
		} else {
			RebirthReturnItem = new int[] {};
		}
		LevelMax = Integer.parseInt(element.getAttribute("LevelMax") == null || element.getAttribute("LevelMax").length() == 0 ? "0"
			: element.getAttribute("LevelMax")); // 等级上限
		BreakOneTime = Integer.parseInt(element.getAttribute("BreakOneTime") == null || element.getAttribute("BreakOneTime").length() == 0 ? "0"
			: element.getAttribute("BreakOneTime")); // 突破一次性增加 属性id  属性%  调用AttributeVlalue#属性数值表id
	}
	

}
