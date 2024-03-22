package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import cn.game.protocol.generated.enume.MissionTypeEnum;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 主线任务表
 * 
 * 工具生成的，不要手动修改
 */
 public class MainlineMissionConfig extends MissionConfig {

	/** 起始命令id -- 起始命令id */
	private final List<Integer> startCommand;		
	/** 是否隐藏条件提示 -- 是否隐藏条件提示 */
	private final boolean conditionHiding;		
	/** 条件命令id -- 条件命令id */
	private final List<Integer> conditionalCommand;		
	/** 是否禁用传送 -- 是否禁用传送 */
	private final boolean ifDelivery;		
	/** 结束命令id -- 结束命令id */
	private final List<Integer> endCommand;		

	public MainlineMissionConfig (Element element) throws Exception {
	
		super(element);
		String startCommandString = element.getAttribute("startCommand"); // 起始命令id
		if (startCommandString != null && startCommandString.length() > 0) {
			String[] startCommandStrings = startCommandString.split("\\|"); 
			List<Integer> startCommand = new ArrayList<Integer>(startCommandStrings.length) ; 
			for (int i = 0; i < startCommandStrings.length; i++) {
				Integer temp = Integer.parseInt(startCommandStrings[i]);
				startCommand.add(temp);
			}
			this.startCommand = com.google.common.collect.ImmutableList.copyOf(startCommand);						
		} else {
			this.startCommand = java.util.Collections.emptyList();
		}
		this.conditionHiding = Boolean.parseBoolean(element.getAttribute("conditionHiding") == null || element.getAttribute("conditionHiding").length() == 0 ? "false"
			: element.getAttribute("conditionHiding")); // 是否隐藏条件提示
		String conditionalCommandString = element.getAttribute("conditionalCommand"); // 条件命令id
		if (conditionalCommandString != null && conditionalCommandString.length() > 0) {
			String[] conditionalCommandStrings = conditionalCommandString.split("\\|"); 
			List<Integer> conditionalCommand = new ArrayList<Integer>(conditionalCommandStrings.length) ; 
			for (int i = 0; i < conditionalCommandStrings.length; i++) {
				Integer temp = Integer.parseInt(conditionalCommandStrings[i]);
				conditionalCommand.add(temp);
			}
			this.conditionalCommand = com.google.common.collect.ImmutableList.copyOf(conditionalCommand);						
		} else {
			this.conditionalCommand = java.util.Collections.emptyList();
		}
		this.ifDelivery = Boolean.parseBoolean(element.getAttribute("ifDelivery") == null || element.getAttribute("ifDelivery").length() == 0 ? "false"
			: element.getAttribute("ifDelivery")); // 是否禁用传送
		String endCommandString = element.getAttribute("endCommand"); // 结束命令id
		if (endCommandString != null && endCommandString.length() > 0) {
			String[] endCommandStrings = endCommandString.split("\\|"); 
			List<Integer> endCommand = new ArrayList<Integer>(endCommandStrings.length) ; 
			for (int i = 0; i < endCommandStrings.length; i++) {
				Integer temp = Integer.parseInt(endCommandStrings[i]);
				endCommand.add(temp);
			}
			this.endCommand = com.google.common.collect.ImmutableList.copyOf(endCommand);						
		} else {
			this.endCommand = java.util.Collections.emptyList();
		}
	}
	
	public List<Integer> getStartCommand() {
		return startCommand;
	}
	
	public boolean getConditionHiding() {
		return conditionHiding;
	}
	
	public List<Integer> getConditionalCommand() {
		return conditionalCommand;
	}
	
	public boolean getIfDelivery() {
		return ifDelivery;
	}
	
	public List<Integer> getEndCommand() {
		return endCommand;
	}
	
}
