package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 剧情配置表
 * 
 * 工具生成的，不要手动修改
 */
 public class StoryConfig {

	/** id -- id */
	private final int id;		
	/** 剧本文件 -- 剧本文件 */
	private final String resource;		
	/** 触发条件 -- 触发条件 */
	private final int triggerType;		
	/** 触发条件参数 -- 触发条件参数 */
	private final List<Integer> triggerParameters;		
	/** 触发条件达成次数参数 -- 触发条件达成次数参数 */
	private final int triggerNumPara;		
	/** 是否重复触发 -- 是否重复触发 */
	private final boolean repeat;		
	/** 前置剧情条件 -- 前置剧情条件 */
	private final List<Integer> storyCondition;		
	/** 解锁条件 -- 解锁条件 */
	private final List<Integer> condition;		
	/** 命令列表 -- 命令列表 */
	private final List<Integer> commandList;		
	/** 奖励道具 -- 奖励道具 */
	private final List<Entry<Integer,Integer>> reward;		

	public StoryConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.resource = element.getAttribute("resource"); // 剧本文件
		this.triggerType = Integer.parseInt(element.getAttribute("triggerType") == null || element.getAttribute("triggerType").length() == 0 ? "0"
			: element.getAttribute("triggerType")); // 触发条件
		String triggerParametersString = element.getAttribute("triggerParameters"); // 触发条件参数
		if (triggerParametersString != null && triggerParametersString.length() > 0) {
			String[] triggerParametersStrings = triggerParametersString.split("\\|"); 
			List<Integer> triggerParameters = new ArrayList<Integer>(triggerParametersStrings.length) ; 
			for (int i = 0; i < triggerParametersStrings.length; i++) {
				Integer temp = Integer.parseInt(triggerParametersStrings[i]);
				triggerParameters.add(temp);
			}
			this.triggerParameters = com.google.common.collect.ImmutableList.copyOf(triggerParameters);						
		} else {
			this.triggerParameters = java.util.Collections.emptyList();
		}
		this.triggerNumPara = Integer.parseInt(element.getAttribute("triggerNumPara") == null || element.getAttribute("triggerNumPara").length() == 0 ? "0"
			: element.getAttribute("triggerNumPara")); // 触发条件达成次数参数
		this.repeat = Boolean.parseBoolean(element.getAttribute("repeat") == null || element.getAttribute("repeat").length() == 0 ? "false"
			: element.getAttribute("repeat")); // 是否重复触发
		String storyConditionString = element.getAttribute("storyCondition"); // 前置剧情条件
		if (storyConditionString != null && storyConditionString.length() > 0) {
			String[] storyConditionStrings = storyConditionString.split("\\|"); 
			List<Integer> storyCondition = new ArrayList<Integer>(storyConditionStrings.length) ; 
			for (int i = 0; i < storyConditionStrings.length; i++) {
				Integer temp = Integer.parseInt(storyConditionStrings[i]);
				storyCondition.add(temp);
			}
			this.storyCondition = com.google.common.collect.ImmutableList.copyOf(storyCondition);						
		} else {
			this.storyCondition = java.util.Collections.emptyList();
		}
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
		String commandListString = element.getAttribute("commandList"); // 命令列表
		if (commandListString != null && commandListString.length() > 0) {
			String[] commandListStrings = commandListString.split("\\|"); 
			List<Integer> commandList = new ArrayList<Integer>(commandListStrings.length) ; 
			for (int i = 0; i < commandListStrings.length; i++) {
				Integer temp = Integer.parseInt(commandListStrings[i]);
				commandList.add(temp);
			}
			this.commandList = com.google.common.collect.ImmutableList.copyOf(commandList);						
		} else {
			this.commandList = java.util.Collections.emptyList();
		}
		String rewardString = element.getAttribute("reward"); // 奖励道具
		if (rewardString != null && rewardString.length() > 0) {
			String[] rewardStrings = rewardString.split("\\|"); 
			List<Entry<Integer,Integer>> reward = new ArrayList<Entry<Integer,Integer>>(rewardStrings.length) ; 
			for (int i = 0; i < rewardStrings.length; i++) {
			    String[] split = rewardStrings[i].split(":", 2);
				reward.add(new Entry<Integer,Integer>()	{
					@Override
					public Integer setValue(Integer value) {
						return null;
					}
					@Override
					public Integer getValue() {
						try {
							return Integer.parseInt(split[1]);
						} catch (Exception e) {
							e.printStackTrace();
						}
						throw new NullPointerException();	
					}
					@Override
					public Integer getKey() {
						try {
							return Integer.parseInt(split[0]);
						} catch (Exception e) {
							e.printStackTrace();
						}
						throw new NullPointerException();
					}
				}) ; 
			}

			this.reward = com.google.common.collect.ImmutableList.copyOf(reward);						
		} else {
			this.reward = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public String getResource() {
		return resource;
	}
	
	public int getTriggerType() {
		return triggerType;
	}
	
	public List<Integer> getTriggerParameters() {
		return triggerParameters;
	}
	
	public int getTriggerNumPara() {
		return triggerNumPara;
	}
	
	public boolean getRepeat() {
		return repeat;
	}
	
	public List<Integer> getStoryCondition() {
		return storyCondition;
	}
	
	public List<Integer> getCondition() {
		return condition;
	}
	
	public List<Integer> getCommandList() {
		return commandList;
	}
	
	public List<Entry<Integer,Integer>> getReward() {
		return reward;
	}
	
}
