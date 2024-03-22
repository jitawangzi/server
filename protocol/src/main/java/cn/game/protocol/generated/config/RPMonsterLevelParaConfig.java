package cn.game.protocol.generated.config;

import cn.game.protocol.generated.enume.ResourceEnum;
import org.w3c.dom.Element;

/**
 * 怪物等级基础系数
 * 
 * 工具生成的，不要手动修改
 */
 public class RPMonsterLevelParaConfig {

	/** id */
	private final int id;		
	/** id */
	private final ResourceEnum type;		
	/** min -- 怪物等级 min */
	private final int levelMin;		
	/** max -- 怪物等级 max */
	private final int levelMax;		
	/** 基础值 -- 基础值 */
	private final int base;		

	public RPMonsterLevelParaConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.type = ResourceEnum.get(Integer.parseInt(element.getAttribute("type")));	// id
		this.levelMin = Integer.parseInt(element.getAttribute("levelMin") == null || element.getAttribute("levelMin").length() == 0 ? "0"
			: element.getAttribute("levelMin")); // min
		this.levelMax = Integer.parseInt(element.getAttribute("levelMax") == null || element.getAttribute("levelMax").length() == 0 ? "0"
			: element.getAttribute("levelMax")); // max
		this.base = Integer.parseInt(element.getAttribute("base") == null || element.getAttribute("base").length() == 0 ? "0"
			: element.getAttribute("base")); // 基础值
	}
	
	public int getId() {
		return id;
	}
	
	public ResourceEnum getType() {
		return type;
	}
	
	public int getLevelMin() {
		return levelMin;
	}
	
	public int getLevelMax() {
		return levelMax;
	}
	
	public int getBase() {
		return base;
	}
	
}
