package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 巡逻
 * 
 * 工具生成的，不要手动修改
 */
 public class PatrolConfig {

	/** 要求通关 战役ID */
	public final int ID;		
	/** 每分钟金币收益 */
	public final int IncomeGold;		
	/** 每分钟经验收益 */
	public final int IncomeEXP;		
	/** 每小时掉落ID */
	public final int IncomeRandomID;		

	public PatrolConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 要求通关 战役ID
		IncomeGold = Integer.parseInt(element.getAttribute("IncomeGold") == null || element.getAttribute("IncomeGold").length() == 0 ? "0"
			: element.getAttribute("IncomeGold")); // 每分钟金币收益
		IncomeEXP = Integer.parseInt(element.getAttribute("IncomeEXP") == null || element.getAttribute("IncomeEXP").length() == 0 ? "0"
			: element.getAttribute("IncomeEXP")); // 每分钟经验收益
		IncomeRandomID = Integer.parseInt(element.getAttribute("IncomeRandomID") == null || element.getAttribute("IncomeRandomID").length() == 0 ? "0"
			: element.getAttribute("IncomeRandomID")); // 每小时掉落ID
	}
	

}
