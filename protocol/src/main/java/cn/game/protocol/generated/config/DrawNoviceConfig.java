package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 抽卡新手组
 * 
 * 工具生成的，不要手动修改
 */
 public class DrawNoviceConfig {

	/** id -- id */
	private final int id;		
	/** 奖励列表 -- 奖励列表 */
	private final List<Integer> awardList;		
	/** 权重 -- 权重 */
	private final int weight;		

	public DrawNoviceConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		String awardListString = element.getAttribute("awardList"); // 奖励列表
		if (awardListString != null && awardListString.length() > 0) {
			String[] awardListStrings = awardListString.split("\\|"); 
			List<Integer> awardList = new ArrayList<Integer>(awardListStrings.length) ; 
			for (int i = 0; i < awardListStrings.length; i++) {
				Integer temp = Integer.parseInt(awardListStrings[i]);
				awardList.add(temp);
			}
			this.awardList = com.google.common.collect.ImmutableList.copyOf(awardList);						
		} else {
			this.awardList = java.util.Collections.emptyList();
		}
		this.weight = Integer.parseInt(element.getAttribute("weight") == null || element.getAttribute("weight").length() == 0 ? "0"
			: element.getAttribute("weight")); // 权重
	}
	
	public int getId() {
		return id;
	}
	
	public List<Integer> getAwardList() {
		return awardList;
	}
	
	public int getWeight() {
		return weight;
	}
	
}
