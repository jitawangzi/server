package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 进阶训练
 * 
 * 工具生成的，不要手动修改
 */
 public class RoutineTrainingConfig {

	/** id -- 第三位表示训练类型 */
	private final int id;		
	/** 可用职业 -- 1-守护 2-先锋 3-异能 4-突袭 5-祈愿 6-黯灭 */
	private final int profession;		
	/** 开启时间 */
	private final List<Integer> openTime;		
	/** 等级要求 */
	private final int level;		
	/** 关卡序列 */
	private final List<Integer> battle;		

	public RoutineTrainingConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.profession = Integer.parseInt(element.getAttribute("profession") == null || element.getAttribute("profession").length() == 0 ? "0"
			: element.getAttribute("profession")); // 可用职业
		String openTimeString = element.getAttribute("openTime"); // 开启时间
		if (openTimeString != null && openTimeString.length() > 0) {
			String[] openTimeStrings = openTimeString.split("\\|"); 
			List<Integer> openTime = new ArrayList<Integer>(openTimeStrings.length) ; 
			for (int i = 0; i < openTimeStrings.length; i++) {
				Integer temp = Integer.parseInt(openTimeStrings[i]);
				openTime.add(temp);
			}
			this.openTime = com.google.common.collect.ImmutableList.copyOf(openTime);						
		} else {
			this.openTime = java.util.Collections.emptyList();
		}
		this.level = Integer.parseInt(element.getAttribute("level") == null || element.getAttribute("level").length() == 0 ? "0"
			: element.getAttribute("level")); // 等级要求
		String battleString = element.getAttribute("battle"); // 关卡序列
		if (battleString != null && battleString.length() > 0) {
			String[] battleStrings = battleString.split("\\|"); 
			List<Integer> battle = new ArrayList<Integer>(battleStrings.length) ; 
			for (int i = 0; i < battleStrings.length; i++) {
				Integer temp = Integer.parseInt(battleStrings[i]);
				battle.add(temp);
			}
			this.battle = com.google.common.collect.ImmutableList.copyOf(battle);						
		} else {
			this.battle = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getProfession() {
		return profession;
	}
	
	public List<Integer> getOpenTime() {
		return openTime;
	}
	
	public int getLevel() {
		return level;
	}
	
	public List<Integer> getBattle() {
		return battle;
	}
	
}
