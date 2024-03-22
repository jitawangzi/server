package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 道具消耗品子表
 * 
 * 工具生成的，不要手动修改
 */
 public class ItemConsumablesConfig {

	/** id -- id第四位是道具类型 */
	private final int id;		
	/** 指定对象 -- 1-选定一个角色 2-选定当前队伍 */
	private final int target;		
	/** 队伍限制 -- 只对队伍使用的道具限制 */
	private final int teamLimit;		
	/** buff表id */
	private final List<Integer> buffIds;		
	/** 使用条件 */
	private final int[] condition;		

	public ItemConsumablesConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.target = Integer.parseInt(element.getAttribute("target") == null || element.getAttribute("target").length() == 0 ? "0"
			: element.getAttribute("target")); // 指定对象
		this.teamLimit = Integer.parseInt(element.getAttribute("teamLimit") == null || element.getAttribute("teamLimit").length() == 0 ? "0"
			: element.getAttribute("teamLimit")); // 队伍限制
		String buffIdsString = element.getAttribute("buffIds"); // buff表id
		if (buffIdsString != null && buffIdsString.length() > 0) {
			String[] buffIdsStrings = buffIdsString.split("\\|"); 
			List<Integer> buffIds = new ArrayList<Integer>(buffIdsStrings.length) ; 
			for (int i = 0; i < buffIdsStrings.length; i++) {
				Integer temp = Integer.parseInt(buffIdsStrings[i]);
				buffIds.add(temp);
			}
			this.buffIds = com.google.common.collect.ImmutableList.copyOf(buffIds);						
		} else {
			this.buffIds = java.util.Collections.emptyList();
		}
		String conditionString = element.getAttribute("condition"); // 使用条件
		if (conditionString != null && conditionString.length() > 0) {
			String[] conditionStrings = conditionString.split("\\|"); 
			int[] condition = new int[conditionStrings.length] ; 
			for (int i = 0; i < conditionStrings.length; i++) {
				int temp = Integer.parseInt(conditionStrings[i]);
				condition[i] = temp;
			}
			this.condition = condition ;			
		} else {
			this.condition = new int[] {};
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getTarget() {
		return target;
	}
	
	public int getTeamLimit() {
		return teamLimit;
	}
	
	public List<Integer> getBuffIds() {
		return buffIds;
	}
	
	public int[] getCondition() {
		return condition;
	}
	
}
