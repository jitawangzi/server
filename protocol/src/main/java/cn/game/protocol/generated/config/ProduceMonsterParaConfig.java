package cn.game.protocol.generated.config;
import org.w3c.dom.Element;

/**
 * 怪物产出的各资源参数
 * 
 * 工具生成的，不要手动修改
 */
 public class ProduceMonsterParaConfig {

	/** 怪物id */
	private int id;		
	/** 角色经验 */
	private int roleExp;		
	/** 金币 */
	private int coin;		
	/** 结晶 */
	private int crystallize;		
	/** 情报 */
	private int intelligence;		
	/** 钛合金 */
	private int titanium;		
	/** 石墨烯 */
	private int graphene;		
	/** 碳纤维 */
	private int carbonFibre;		
	/** 寄合质 */
	private int zygote;		
	/** 异导物 */
	private int heteroderivative;		

	public ProduceMonsterParaConfig (Element element) {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // 怪物id
		this.roleExp = Integer.parseInt(element.getAttribute("roleExp") == null || element.getAttribute("roleExp").length() == 0 ? "0"
			: element.getAttribute("roleExp")); // 角色经验
		this.coin = Integer.parseInt(element.getAttribute("coin") == null || element.getAttribute("coin").length() == 0 ? "0"
			: element.getAttribute("coin")); // 金币
		this.crystallize = Integer.parseInt(element.getAttribute("crystallize") == null || element.getAttribute("crystallize").length() == 0 ? "0"
			: element.getAttribute("crystallize")); // 结晶
		this.intelligence = Integer.parseInt(element.getAttribute("intelligence") == null || element.getAttribute("intelligence").length() == 0 ? "0"
			: element.getAttribute("intelligence")); // 情报
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
	}
	
	public int getId() {
		return id;
	}
	
	public int getRoleExp() {
		return roleExp;
	}
	
	public int getCoin() {
		return coin;
	}
	
	public int getCrystallize() {
		return crystallize;
	}
	
	public int getIntelligence() {
		return intelligence;
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
	
}
