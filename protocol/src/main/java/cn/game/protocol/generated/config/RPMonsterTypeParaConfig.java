package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 怪物类型系数
 * 
 * 工具生成的，不要手动修改
 */
 public class RPMonsterTypeParaConfig {

	/** 怪物类型 -- 和Monster表的type对应 1-普通 2-精英 3-BOSS 4-特殊 */
	private final int id;		
	/** 角色经验 */
	private final int roleExpPara;		
	/** 金币 */
	private final int coinPara;		
	/** 结晶 */
	private final int crystallizePara;		
	/** 情报 */
	private final int intelligencePara;		
	/** 钛合金 -- 装备材料 0默认不产出 */
	private final int titanium;		
	/** 石墨烯 -- 装备材料 0默认不产出 */
	private final int graphene;		
	/** 碳纤维 -- 装备材料 0默认不产出 */
	private final int carbonFibre;		
	/** 寄合质 -- 装备材料 0默认不产出 */
	private final int zygote;		
	/** 异导物 -- 装备材料 0默认不产出 */
	private final int heteroderivative;		
	/** 晋升点数 -- 主城的晋升点数产出系数 0默认不产出 */
	private final int PromotionPoint;		

	public RPMonsterTypeParaConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // 怪物类型
		this.roleExpPara = Integer.parseInt(element.getAttribute("roleExpPara") == null || element.getAttribute("roleExpPara").length() == 0 ? "0"
			: element.getAttribute("roleExpPara")); // 角色经验
		this.coinPara = Integer.parseInt(element.getAttribute("coinPara") == null || element.getAttribute("coinPara").length() == 0 ? "0"
			: element.getAttribute("coinPara")); // 金币
		this.crystallizePara = Integer.parseInt(element.getAttribute("crystallizePara") == null || element.getAttribute("crystallizePara").length() == 0 ? "0"
			: element.getAttribute("crystallizePara")); // 结晶
		this.intelligencePara = Integer.parseInt(element.getAttribute("intelligencePara") == null || element.getAttribute("intelligencePara").length() == 0 ? "0"
			: element.getAttribute("intelligencePara")); // 情报
		this.titanium = Integer.parseInt(element.getAttribute("titanium") == null || element.getAttribute("titanium").length() == 0 ? "0"
			: element.getAttribute("titanium")); // 钛合金
		this.graphene = Integer.parseInt(element.getAttribute("graphene") == null || element.getAttribute("graphene").length() == 0 ? "0"
			: element.getAttribute("graphene")); // 石墨烯
		this.carbonFibre = Integer.parseInt(element.getAttribute("carbonFibre") == null || element.getAttribute("carbonFibre").length() == 0 ? "0"
			: element.getAttribute("carbonFibre")); // 碳纤维
		this.zygote = Integer.parseInt(element.getAttribute("zygote") == null || element.getAttribute("zygote").length() == 0 ? "0"
			: element.getAttribute("zygote")); // 寄合质
		this.heteroderivative = Integer.parseInt(element.getAttribute("heteroderivative") == null || element.getAttribute("heteroderivative").length() == 0 ? "0"
			: element.getAttribute("heteroderivative")); // 异导物
		this.PromotionPoint = Integer.parseInt(element.getAttribute("PromotionPoint") == null || element.getAttribute("PromotionPoint").length() == 0 ? "0"
			: element.getAttribute("PromotionPoint")); // 晋升点数
	}
	
	public int getId() {
		return id;
	}
	
	public int getRoleExpPara() {
		return roleExpPara;
	}
	
	public int getCoinPara() {
		return coinPara;
	}
	
	public int getCrystallizePara() {
		return crystallizePara;
	}
	
	public int getIntelligencePara() {
		return intelligencePara;
	}
	
	public int getTitanium() {
		return titanium;
	}
	
	public int getGraphene() {
		return graphene;
	}
	
	public int getCarbonFibre() {
		return carbonFibre;
	}
	
	public int getZygote() {
		return zygote;
	}
	
	public int getHeteroderivative() {
		return heteroderivative;
	}
	
	public int getPromotionPoint() {
		return PromotionPoint;
	}
	
}
