package cn.game.protocol.generated.config;
import org.w3c.dom.Element;

/**
 * 战斗结算各资源基础值
 * 
 * 工具生成的，不要手动修改
 */
 public class ProduceBattleSettlementBaseConfig {

	/** id */
	private int id;		
	/** id */
	private int type;		
	/** min */
	private int levelMin;		
	/** max */
	private int levelMax;		
	/** 基础值 */
	private int para;		

	public ProduceBattleSettlementBaseConfig (Element element) {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // id
		this.levelMin = Integer.parseInt(element.getAttribute("levelMin") == null || element.getAttribute("levelMin").length() == 0 ? "0"
			: element.getAttribute("levelMin")); // min
		this.levelMax = Integer.parseInt(element.getAttribute("levelMax") == null || element.getAttribute("levelMax").length() == 0 ? "0"
			: element.getAttribute("levelMax")); // max
		this.para = Integer.parseInt(element.getAttribute("para") == null || element.getAttribute("para").length() == 0 ? "0"
			: element.getAttribute("para")); // 基础值
	}
	
	public int getId() {
		return id;
	}
	
	public int getType() {
		return type;
	}
	
	public int getLevelMin() {
		return levelMin;
	}
	
	public int getLevelMax() {
		return levelMax;
	}
	
	public int getPara() {
		return para;
	}
	
}
