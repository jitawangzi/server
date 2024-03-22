package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 主线交互物体表
 * 
 * 工具生成的，不要手动修改
 */
 public class MainlineObjectConfig {

	/** id -- id */
	private final int id;		
	/** 地图id -- 地图id */
	private final int mapId;		
	/** 名称 -- 名称 */
	private final String name;		
	/** 枚举类型 -- 类型 */
	private final int enumType;		
	/** 资源 -- 资源 */
	private final String resource;		
	/** 限制等级 -- 限制等级 */
	private final int limitLevel;		
	/** 触发条件 -- 触发条件 */
	private final List<Integer> triggerConditions;		
	/** 交互后的命令 -- 交互后的命令 */
	private final List<Integer> command;		
	/** 是否可重复交互 -- 是否可重复交互 */
	private final boolean isRepeat;		
	/** 交互后是否消失 -- 交互后是否消失 */
	private final boolean isDisappear;		
	/** 刷新类型 -- 刷新类型 */
	private final int refreshType;		
	/** 刷新时间（单位:小时) -- 刷新时间（单位:小时) */
	private final int refreshTime;		
	/** 交互按钮文本 -- 交互按钮文本 */
	private final String btnTxt;		

	public MainlineObjectConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.mapId = Integer.parseInt(element.getAttribute("mapId") == null || element.getAttribute("mapId").length() == 0 ? "0"
			: element.getAttribute("mapId")); // 地图id
		this.name = element.getAttribute("name"); // 名称
		this.enumType = Integer.parseInt(element.getAttribute("enumType") == null || element.getAttribute("enumType").length() == 0 ? "0"
			: element.getAttribute("enumType")); // 枚举类型
		this.resource = element.getAttribute("resource"); // 资源
		this.limitLevel = Integer.parseInt(element.getAttribute("limitLevel") == null || element.getAttribute("limitLevel").length() == 0 ? "0"
			: element.getAttribute("limitLevel")); // 限制等级
		String triggerConditionsString = element.getAttribute("triggerConditions"); // 触发条件
		if (triggerConditionsString != null && triggerConditionsString.length() > 0) {
			String[] triggerConditionsStrings = triggerConditionsString.split("\\|"); 
			List<Integer> triggerConditions = new ArrayList<Integer>(triggerConditionsStrings.length) ; 
			for (int i = 0; i < triggerConditionsStrings.length; i++) {
				Integer temp = Integer.parseInt(triggerConditionsStrings[i]);
				triggerConditions.add(temp);
			}
			this.triggerConditions = com.google.common.collect.ImmutableList.copyOf(triggerConditions);						
		} else {
			this.triggerConditions = java.util.Collections.emptyList();
		}
		String commandString = element.getAttribute("command"); // 交互后的命令
		if (commandString != null && commandString.length() > 0) {
			String[] commandStrings = commandString.split("\\|"); 
			List<Integer> command = new ArrayList<Integer>(commandStrings.length) ; 
			for (int i = 0; i < commandStrings.length; i++) {
				Integer temp = Integer.parseInt(commandStrings[i]);
				command.add(temp);
			}
			this.command = com.google.common.collect.ImmutableList.copyOf(command);						
		} else {
			this.command = java.util.Collections.emptyList();
		}
		this.isRepeat = Boolean.parseBoolean(element.getAttribute("isRepeat") == null || element.getAttribute("isRepeat").length() == 0 ? "false"
			: element.getAttribute("isRepeat")); // 是否可重复交互
		this.isDisappear = Boolean.parseBoolean(element.getAttribute("isDisappear") == null || element.getAttribute("isDisappear").length() == 0 ? "false"
			: element.getAttribute("isDisappear")); // 交互后是否消失
		this.refreshType = Integer.parseInt(element.getAttribute("refreshType") == null || element.getAttribute("refreshType").length() == 0 ? "0"
			: element.getAttribute("refreshType")); // 刷新类型
		this.refreshTime = Integer.parseInt(element.getAttribute("refreshTime") == null || element.getAttribute("refreshTime").length() == 0 ? "0"
			: element.getAttribute("refreshTime")); // 刷新时间（单位:小时)
		this.btnTxt = element.getAttribute("btnTxt"); // 交互按钮文本
	}
	
	public int getId() {
		return id;
	}
	
	public int getMapId() {
		return mapId;
	}
	
	public String getName() {
		return name;
	}
	
	public int getEnumType() {
		return enumType;
	}
	
	public String getResource() {
		return resource;
	}
	
	public int getLimitLevel() {
		return limitLevel;
	}
	
	public List<Integer> getTriggerConditions() {
		return triggerConditions;
	}
	
	public List<Integer> getCommand() {
		return command;
	}
	
	public boolean getIsRepeat() {
		return isRepeat;
	}
	
	public boolean getIsDisappear() {
		return isDisappear;
	}
	
	public int getRefreshType() {
		return refreshType;
	}
	
	public int getRefreshTime() {
		return refreshTime;
	}
	
	public String getBtnTxt() {
		return btnTxt;
	}
	
}
