package cn.game.protocol.generated.config;

import cn.game.protocol.generated.enume.MissionTypeEnum;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 成就表
 * 
 * 工具生成的，不要手动修改
 */
 public class AchievementMissionConfig extends MissionConfig {

	/** 类型 -- 成就任务 */
	private final MissionTypeEnum type;		
	/** 交付方式 -- 1-自动交付 */
	private final List<Integer> modeOfDelivery;		
	/** 条件关系 -- 0-完成全部即可达成 1-完成任意一个即可达成 */
	private final boolean conditionalOr;		
	/** 完成条件 -- 完成条件 关联Condition */
	private final List<Integer> condition;		

	public AchievementMissionConfig (Element element) throws Exception {
	
		super(element);
		this.type = MissionTypeEnum.get(Integer.parseInt(element.getAttribute("type")));	// 类型
		String modeOfDeliveryString = element.getAttribute("modeOfDelivery"); // 交付方式
		if (modeOfDeliveryString != null && modeOfDeliveryString.length() > 0) {
			String[] modeOfDeliveryStrings = modeOfDeliveryString.split("\\|"); 
			List<Integer> modeOfDelivery = new ArrayList<Integer>(modeOfDeliveryStrings.length) ; 
			for (int i = 0; i < modeOfDeliveryStrings.length; i++) {
				Integer temp = Integer.parseInt(modeOfDeliveryStrings[i]);
				modeOfDelivery.add(temp);
			}
			this.modeOfDelivery = com.google.common.collect.ImmutableList.copyOf(modeOfDelivery);						
		} else {
			this.modeOfDelivery = java.util.Collections.emptyList();
		}
		this.conditionalOr = Boolean.parseBoolean(element.getAttribute("conditionalOr") == null || element.getAttribute("conditionalOr").length() == 0 ? "false"
			: element.getAttribute("conditionalOr")); // 条件关系
		String conditionString = element.getAttribute("condition"); // 完成条件
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
	}
	
	public MissionTypeEnum getType() {
		return type;
	}
	
	public List<Integer> getModeOfDelivery() {
		return modeOfDelivery;
	}
	
	public boolean getConditionalOr() {
		return conditionalOr;
	}
	
	public List<Integer> getCondition() {
		return condition;
	}
	
}
