package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 战区
 * 
 * 工具生成的，不要手动修改
 */
 public class BattleZoneConfig {

	/** id */
	private final int id;		
	/** 星数 */
	private final List<Integer> star;		
	/** 奖励id -- Reward表的id */
	private final List<Integer> rewardId;		
	/** 世界地图id -- 所属世界地图id */
	private final int worldMapId;		
	/** 主线事件 */
	private final List<Integer> mainEvent;		

	public BattleZoneConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		String starString = element.getAttribute("star"); // 星数
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
		String rewardIdString = element.getAttribute("rewardId"); // 奖励id
		if (rewardIdString != null && rewardIdString.length() > 0) {
			String[] rewardIdStrings = rewardIdString.split("\\|"); 
			List<Integer> rewardId = new ArrayList<Integer>(rewardIdStrings.length) ; 
			for (int i = 0; i < rewardIdStrings.length; i++) {
				Integer temp = Integer.parseInt(rewardIdStrings[i]);
				rewardId.add(temp);
			}
			this.rewardId = com.google.common.collect.ImmutableList.copyOf(rewardId);						
		} else {
			this.rewardId = java.util.Collections.emptyList();
		}
		this.worldMapId = Integer.parseInt(element.getAttribute("worldMapId") == null || element.getAttribute("worldMapId").length() == 0 ? "0"
			: element.getAttribute("worldMapId")); // 世界地图id
		String mainEventString = element.getAttribute("mainEvent"); // 主线事件
		if (mainEventString != null && mainEventString.length() > 0) {
			String[] mainEventStrings = mainEventString.split("\\|"); 
			List<Integer> mainEvent = new ArrayList<Integer>(mainEventStrings.length) ; 
			for (int i = 0; i < mainEventStrings.length; i++) {
				Integer temp = Integer.parseInt(mainEventStrings[i]);
				mainEvent.add(temp);
			}
			this.mainEvent = com.google.common.collect.ImmutableList.copyOf(mainEvent);						
		} else {
			this.mainEvent = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public List<Integer> getStar() {
		return star;
	}
	
	public List<Integer> getRewardId() {
		return rewardId;
	}
	
	public int getWorldMapId() {
		return worldMapId;
	}
	
	public List<Integer> getMainEvent() {
		return mainEvent;
	}
	
}
