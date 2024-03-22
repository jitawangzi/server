package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 主线剧情
 * 
 * 工具生成的，不要手动修改
 */
 public class BattlePlotConfig {

	/** id -- 意识海10000 剧情关20000 */
	private final int id;		
	/** 解锁条件 */
	private final List<Integer> condition;		
	/** 剧情 */
	private final String plot;		
	/** 战区事件id */
	private final int battleChapterId;		

	public BattlePlotConfig (Element element) throws Exception {
	
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
		this.plot = element.getAttribute("plot"); // 剧情
		this.battleChapterId = Integer.parseInt(element.getAttribute("battleChapterId") == null || element.getAttribute("battleChapterId").length() == 0 ? "0"
			: element.getAttribute("battleChapterId")); // 战区事件id
	}
	
	public int getId() {
		return id;
	}
	
	public List<Integer> getCondition() {
		return condition;
	}
	
	public String getPlot() {
		return plot;
	}
	
	public int getBattleChapterId() {
		return battleChapterId;
	}
	
}
