package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import cn.game.protocol.generated.enume.GameEventTriggerEnum;
import org.w3c.dom.Element;

/**
 * 游戏事件表
 * 
 * 工具生成的，不要手动修改
 */
 public class GameEventConfig {

	/** id -- id */
	private final int id;		
	/** 条件关系 -- 0-完成全部即可达成 1-完成任意一个即可达成 */
	private final boolean conditionalOr;		
	/** 开启条件 -- 一般服务端验证用 */
	private final List<Integer> openConditions;		
	/** 是否可重复触发 -- 0-不可重复 1-可重复 */
	private final boolean isRepeated;		
	/** 事件类型 -- 1-通用事件 2-探索事件 3-序章事件 4- */
	private final int eventType;		
	/** 生成物id -- 投放事件的时候，首先会生成这里的物体，之后可能会根据这些物体来触发事件，如事件npc id等 */
	private final int[] genarateIds;		
	/** 生成数量扩展 */
	private final int genarateCount;		
	/** 触发类型 */
	private final GameEventTriggerEnum triggerType;		
	/** 参数 */
	private final int[] triggerParams;		
	/** 是否不记录 -- 完成后不记录该事件（多用于子事件） 0-记录 1-不记录 */
	private final boolean isNotRecord;		

	public GameEventConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.conditionalOr = Boolean.parseBoolean(element.getAttribute("conditionalOr") == null || element.getAttribute("conditionalOr").length() == 0 ? "false"
			: element.getAttribute("conditionalOr")); // 条件关系
		String openConditionsString = element.getAttribute("openConditions"); // 开启条件
		if (openConditionsString != null && openConditionsString.length() > 0) {
			String[] openConditionsStrings = openConditionsString.split("\\|"); 
			List<Integer> openConditions = new ArrayList<Integer>(openConditionsStrings.length) ; 
			for (int i = 0; i < openConditionsStrings.length; i++) {
				Integer temp = Integer.parseInt(openConditionsStrings[i]);
				openConditions.add(temp);
			}
			this.openConditions = com.google.common.collect.ImmutableList.copyOf(openConditions);						
		} else {
			this.openConditions = java.util.Collections.emptyList();
		}
		this.isRepeated = Boolean.parseBoolean(element.getAttribute("isRepeated") == null || element.getAttribute("isRepeated").length() == 0 ? "false"
			: element.getAttribute("isRepeated")); // 是否可重复触发
		this.eventType = Integer.parseInt(element.getAttribute("eventType") == null || element.getAttribute("eventType").length() == 0 ? "0"
			: element.getAttribute("eventType")); // 事件类型
		String genarateIdsString = element.getAttribute("genarateIds"); // 生成物id
		if (genarateIdsString != null && genarateIdsString.length() > 0) {
			String[] genarateIdsStrings = genarateIdsString.split("\\|"); 
			int[] genarateIds = new int[genarateIdsStrings.length] ; 
			for (int i = 0; i < genarateIdsStrings.length; i++) {
				int temp = Integer.parseInt(genarateIdsStrings[i]);
				genarateIds[i] = temp;
			}
			this.genarateIds = genarateIds ;			
		} else {
			this.genarateIds = new int[] {};
		}
		this.genarateCount = Integer.parseInt(element.getAttribute("genarateCount") == null || element.getAttribute("genarateCount").length() == 0 ? "0"
			: element.getAttribute("genarateCount")); // 生成数量扩展
		this.triggerType = GameEventTriggerEnum.get(Integer.parseInt(element.getAttribute("triggerType")));	// 触发类型
		String triggerParamsString = element.getAttribute("triggerParams"); // 参数
		if (triggerParamsString != null && triggerParamsString.length() > 0) {
			String[] triggerParamsStrings = triggerParamsString.split("\\|"); 
			int[] triggerParams = new int[triggerParamsStrings.length] ; 
			for (int i = 0; i < triggerParamsStrings.length; i++) {
				int temp = Integer.parseInt(triggerParamsStrings[i]);
				triggerParams[i] = temp;
			}
			this.triggerParams = triggerParams ;			
		} else {
			this.triggerParams = new int[] {};
		}
		this.isNotRecord = Boolean.parseBoolean(element.getAttribute("isNotRecord") == null || element.getAttribute("isNotRecord").length() == 0 ? "false"
			: element.getAttribute("isNotRecord")); // 是否不记录
	}
	
	public int getId() {
		return id;
	}
	
	public boolean getConditionalOr() {
		return conditionalOr;
	}
	
	public List<Integer> getOpenConditions() {
		return openConditions;
	}
	
	public boolean getIsRepeated() {
		return isRepeated;
	}
	
	public int getEventType() {
		return eventType;
	}
	
	public int[] getGenarateIds() {
		return genarateIds;
	}
	
	public int getGenarateCount() {
		return genarateCount;
	}
	
	public GameEventTriggerEnum getTriggerType() {
		return triggerType;
	}
	
	public int[] getTriggerParams() {
		return triggerParams;
	}
	
	public boolean getIsNotRecord() {
		return isNotRecord;
	}
	
}
