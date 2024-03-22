package cn.game.protocol.generated.config;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.w3c.dom.Element;

/**
 * 剧情配置表
 * 
 * 工具生成的，不要手动修改
 */
 public class PlotEventsConfig {

	/** id */
	private int id;		
	/** 名称 */
	private String name;		
	/** 剧本文件 */
	private int txtId;		
	/** 触发条件 */
	private List<Integer> trigger;		
	/** 是否重复触发 */
	private boolean orTrigger;		
	/** 解锁条件 */
	private List<Integer> condition;		
	/** 命令列表 */
	private List<Integer> commandList;		
	/** 奖励道具 */
	private List<Entry<Integer,Integer>> prizeItem;		

	public PlotEventsConfig (Element element) {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.name = element.getAttribute("name"); // 名称
		this.txtId = Integer.parseInt(element.getAttribute("txtId") == null || element.getAttribute("txtId").length() == 0 ? "0"
			: element.getAttribute("txtId")); // 剧本文件
		String trigger = element.getAttribute("trigger"); // 触发条件
		if (trigger != null && trigger.length() > 0) {
			String[] triggerStrings = trigger.split("\\|"); 
			this.trigger = new ArrayList<Integer>(triggerStrings.length) ; 
			for (int i = 0; i < triggerStrings.length; i++) {
				Integer temp = Integer.parseInt(triggerStrings[i]);
				this.trigger.add(temp);
			}
		} else {
			this.trigger = new ArrayList<Integer>();
		}
		this.orTrigger = Boolean.parseBoolean(element.getAttribute("orTrigger") == null || element.getAttribute("orTrigger").length() == 0 ? "false"
			: element.getAttribute("orTrigger")); // 是否重复触发
		String condition = element.getAttribute("condition"); // 解锁条件
		if (condition != null && condition.length() > 0) {
			String[] conditionStrings = condition.split("\\|"); 
			this.condition = new ArrayList<Integer>(conditionStrings.length) ; 
			for (int i = 0; i < conditionStrings.length; i++) {
				Integer temp = Integer.parseInt(conditionStrings[i]);
				this.condition.add(temp);
			}
		} else {
			this.condition = new ArrayList<Integer>();
		}
		String commandList = element.getAttribute("commandList"); // 命令列表
		if (commandList != null && commandList.length() > 0) {
			String[] commandListStrings = commandList.split("\\|"); 
			this.commandList = new ArrayList<Integer>(commandListStrings.length) ; 
			for (int i = 0; i < commandListStrings.length; i++) {
				Integer temp = Integer.parseInt(commandListStrings[i]);
				this.commandList.add(temp);
			}
		} else {
			this.commandList = new ArrayList<Integer>();
		}
		String prizeItem = element.getAttribute("prizeItem"); // 奖励道具
		if (prizeItem != null && prizeItem.length() > 0) {
			String[] prizeItemStrings = prizeItem.split("\\|"); 
			this.prizeItem = new ArrayList<Entry<Integer,Integer>>(prizeItemStrings.length) ; 
			for (String string : prizeItemStrings) {
			    String[] split = string.split(":");
			    if (split.length != 2) {
					throw new IllegalArgumentException(" 表： " + this.getClass().getSimpleName() + " id  : " + this.id + " 字段: "
							+ "prizeItem" + "需为key-value格式，用冒号分隔");
				} 
				this.prizeItem.add(new Entry<Integer,Integer>()	{
					@Override
					public Integer setValue(Integer value) {
						return null;
					}
					@Override
					public Integer getValue() {
						return Integer.parseInt(split[1]);
					}
					@Override
					public Integer getKey() {
						return Integer.parseInt(split[0]);
					}
				}) ; 
			}
		} else {
			this.prizeItem = new ArrayList<Entry<Integer,Integer>>();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public int getTxtId() {
		return txtId;
	}
	
	public List<Integer> getTrigger() {
		return trigger;
	}
	
	public boolean getOrTrigger() {
		return orTrigger;
	}
	
	public List<Integer> getCondition() {
		return condition;
	}
	
	public List<Integer> getCommandList() {
		return commandList;
	}
	
	public List<Entry<Integer,Integer>> getPrizeItem() {
		return prizeItem;
	}
	
}
