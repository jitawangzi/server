package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 主线章节
 * 
 * 工具生成的，不要手动修改
 */
 public class BattleChapterConfig {

	/** id */
	private final int id;		
	/** 解锁条件 */
	private final List<Integer> condition;		
	/** 章节星数 */
	private final List<Integer> star;		
	/** 章节奖励 -- Reward表的id */
	private final List<Integer> starRewardId;		

	public BattleChapterConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		String conditionString = element.getAttribute("condition"); // 解锁条件
		if (conditionString != null && conditionString.length() > 0) {
			String[] conditionStrings = conditionString.split("\\|"); 
			List<Integer> condition = new ArrayList<Integer>(conditionStrings.length) ; 
			for (int i = 0; i < conditionStrings.length; i++) {
				Integer temp = Integer.parseInt(conditionStrings[i]);
				condition.add(temp);
			}
			this.condition = com.google.common.collect.ImmutableList.copyOf(condition);						
		} else {
			this.condition = java.util.Collections.emptyList();
		}
		String starString = element.getAttribute("star"); // 章节星数
		if (starString != null && starString.length() > 0) {
			String[] starStrings = starString.split("\\|"); 
			List<Integer> star = new ArrayList<Integer>(starStrings.length) ; 
			for (int i = 0; i < starStrings.length; i++) {
				Integer temp = Integer.parseInt(starStrings[i]);
				star.add(temp);
			}
			this.star = com.google.common.collect.ImmutableList.copyOf(star);						
		} else {
			this.star = java.util.Collections.emptyList();
		}
		String starRewardIdString = element.getAttribute("starRewardId"); // 章节奖励
		if (starRewardIdString != null && starRewardIdString.length() > 0) {
			String[] starRewardIdStrings = starRewardIdString.split("\\|"); 
			List<Integer> starRewardId = new ArrayList<Integer>(starRewardIdStrings.length) ; 
			for (int i = 0; i < starRewardIdStrings.length; i++) {
				Integer temp = Integer.parseInt(starRewardIdStrings[i]);
				starRewardId.add(temp);
			}
			this.starRewardId = com.google.common.collect.ImmutableList.copyOf(starRewardId);						
		} else {
			this.starRewardId = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public List<Integer> getCondition() {
		return condition;
	}
	
	public List<Integer> getStar() {
		return star;
	}
	
	public List<Integer> getStarRewardId() {
		return starRewardId;
	}
	
}
