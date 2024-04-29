package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 英雄升级exp
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroLvConfig {

	/** 英雄等级 */
	public final int ID;		
	/** 英雄升级消耗物品数量  固定消耗物品id调用：Initial表—GlobalConst#常量表—HeroLvItem行物品id */
	public final int LvConsumeItem;		
	/** 金币  固定消耗 */
	public final int LvConsumeMoney;		

	public HeroLvConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 英雄等级
		LvConsumeItem = Integer.parseInt(element.getAttribute("LvConsumeItem") == null || element.getAttribute("LvConsumeItem").length() == 0 ? "0"
			: element.getAttribute("LvConsumeItem")); // 英雄升级消耗物品数量  固定消耗物品id调用：Initial表—GlobalConst#常量表—HeroLvItem行物品id
		LvConsumeMoney = Integer.parseInt(element.getAttribute("LvConsumeMoney") == null || element.getAttribute("LvConsumeMoney").length() == 0 ? "0"
			: element.getAttribute("LvConsumeMoney")); // 金币  固定消耗
	}
	

}
