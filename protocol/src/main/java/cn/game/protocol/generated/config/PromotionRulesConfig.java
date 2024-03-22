package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 爬塔晋升规则表
 * 
 * 工具生成的，不要手动修改
 */
 public class PromotionRulesConfig {

	/** id -- id */
	private final int id;		
	/** 晋级 -- 前X名(不包含) */
	private final int riseInRank;		
	/** 降级 -- 后X名(不包含) */
	private final int reduceInRank;		

	public PromotionRulesConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.riseInRank = Integer.parseInt(element.getAttribute("riseInRank") == null || element.getAttribute("riseInRank").length() == 0 ? "0"
			: element.getAttribute("riseInRank")); // 晋级
		this.reduceInRank = Integer.parseInt(element.getAttribute("reduceInRank") == null || element.getAttribute("reduceInRank").length() == 0 ? "0"
			: element.getAttribute("reduceInRank")); // 降级
	}
	
	public int getId() {
		return id;
	}
	
	public int getRiseInRank() {
		return riseInRank;
	}
	
	public int getReduceInRank() {
		return reduceInRank;
	}
	
}
