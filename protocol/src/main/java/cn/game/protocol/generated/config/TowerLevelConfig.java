package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 爬塔关卡层表
 * 
 * 工具生成的，不要手动修改
 */
 public class TowerLevelConfig {

	/** id -- id */
	private final int id;		
	/** 背景资源 -- 背景资源 */
	private final String res;		
	/** 战斗id -- 战斗id */
	private final List<Integer> battleLevel;		
	/** 通关结算 -- 通关结算 */
	private final int clearanceScore;		
	/** 时间结算 -- 时间结算 */
	private final List<Integer> timeScore;		
	/** 无伤结算 -- 无伤结算 */
	private final int hurtScore;		

	public TowerLevelConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.res = element.getAttribute("res"); // 背景资源
		String battleLevelString = element.getAttribute("battleLevel"); // 战斗id
		if (battleLevelString != null && battleLevelString.length() > 0) {
			String[] battleLevelStrings = battleLevelString.split("\\|"); 
			List<Integer> battleLevel = new ArrayList<Integer>(battleLevelStrings.length) ; 
			for (int i = 0; i < battleLevelStrings.length; i++) {
				Integer temp = Integer.parseInt(battleLevelStrings[i]);
				battleLevel.add(temp);
			}
			this.battleLevel = com.google.common.collect.ImmutableList.copyOf(battleLevel);						
		} else {
			this.battleLevel = java.util.Collections.emptyList();
		}
		this.clearanceScore = Integer.parseInt(element.getAttribute("clearanceScore") == null || element.getAttribute("clearanceScore").length() == 0 ? "0"
			: element.getAttribute("clearanceScore")); // 通关结算
		String timeScoreString = element.getAttribute("timeScore"); // 时间结算
		if (timeScoreString != null && timeScoreString.length() > 0) {
			String[] timeScoreStrings = timeScoreString.split("\\|"); 
			List<Integer> timeScore = new ArrayList<Integer>(timeScoreStrings.length) ; 
			for (int i = 0; i < timeScoreStrings.length; i++) {
				Integer temp = Integer.parseInt(timeScoreStrings[i]);
				timeScore.add(temp);
			}
			this.timeScore = com.google.common.collect.ImmutableList.copyOf(timeScore);						
		} else {
			this.timeScore = java.util.Collections.emptyList();
		}
		this.hurtScore = Integer.parseInt(element.getAttribute("hurtScore") == null || element.getAttribute("hurtScore").length() == 0 ? "0"
			: element.getAttribute("hurtScore")); // 无伤结算
	}
	
	public int getId() {
		return id;
	}
	
	public String getRes() {
		return res;
	}
	
	public List<Integer> getBattleLevel() {
		return battleLevel;
	}
	
	public int getClearanceScore() {
		return clearanceScore;
	}
	
	public List<Integer> getTimeScore() {
		return timeScore;
	}
	
	public int getHurtScore() {
		return hurtScore;
	}
	
}
