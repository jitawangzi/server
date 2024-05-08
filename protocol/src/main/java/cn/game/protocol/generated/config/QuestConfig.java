package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import cn.game.protocol.generated.enume.QuestTypeEnum;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 任务
 * 
 * 工具生成的，不要手动修改
 */
 public class QuestConfig {

	/** id -- 任务id */
	private final int id;		
	/** 开启任务id -- 开启任务id */
	private final List<Integer> openTaskId;		
	/** 分组 -- 分组id */
	private final int groupId;		
	/** 类型 -- 类型 */
	private final QuestTypeEnum type;		
	/** 完成条件 -- 完成条件 */
	private final List<Integer> condition;		
	/** 任务奖励方式 -- 0-全部领取 1-多选1 2-多选2 */
	private final int chooseRewardType;		
	/** 任务奖励 -- 任务奖励 */
	private final List<Entry<Integer,Integer>> reward;		
	/** 解锁等级 -- 解锁等级 */
	private final int level;		
	/** 结束等级 -- 结束等级 */
	private final int endLevel;		
	/** 周积分 -- 周积分 */
	private final int weekScore;		
	/** BP积分 -- BP积分 */
	private final List<Entry<Integer,Integer>> bpScore;		
	/** 挑战积分 -- 挑战积分 */
	private final int challengeScore;		
	/** 跳转id -- 跳转id */
	private final int jump;		
	/** 接取方式 -- 接取方式 */
	private final List<Integer> accessMode;		
	/** 条件关系 -- 条件关系 */
	private final boolean conditionalOr;		
	/** 交付方式 -- 交付方式 */
	private final List<Integer> modeOfDelivery;		
	/** 是否刷新 -- 刷新类型 */
	private final boolean refreshType;		
	/** 是否前端出发 -- 是否前端更新进度 0-不需要 1-需要前端更新 */
	private final boolean isClentUpdate;		

	public QuestConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		String openTaskIdString = element.getAttribute("openTaskId"); // 开启任务id
		if (openTaskIdString != null && openTaskIdString.length() > 0) {
			String[] openTaskIdStrings = openTaskIdString.split("\\|"); 
			List<Integer> openTaskId = new ArrayList<Integer>(openTaskIdStrings.length) ; 
			for (int i = 0; i < openTaskIdStrings.length; i++) {
				Integer temp = Integer.parseInt(openTaskIdStrings[i]);
				openTaskId.add(temp);
			}
			this.openTaskId = com.google.common.collect.ImmutableList.copyOf(openTaskId);						
		} else {
			this.openTaskId = java.util.Collections.emptyList();
		}
		this.groupId = Integer.parseInt(element.getAttribute("groupId") == null || element.getAttribute("groupId").length() == 0 ? "0"
			: element.getAttribute("groupId")); // 分组
		this.type = QuestTypeEnum.get(Integer.parseInt(element.getAttribute("type")));	// 类型
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
		this.chooseRewardType = Integer.parseInt(element.getAttribute("chooseRewardType") == null || element.getAttribute("chooseRewardType").length() == 0 ? "0"
			: element.getAttribute("chooseRewardType")); // 任务奖励方式
		String rewardString = element.getAttribute("reward"); // 任务奖励
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
		this.level = Integer.parseInt(element.getAttribute("level") == null || element.getAttribute("level").length() == 0 ? "0"
			: element.getAttribute("level")); // 解锁等级
		this.endLevel = Integer.parseInt(element.getAttribute("endLevel") == null || element.getAttribute("endLevel").length() == 0 ? "0"
			: element.getAttribute("endLevel")); // 结束等级
		this.weekScore = Integer.parseInt(element.getAttribute("weekScore") == null || element.getAttribute("weekScore").length() == 0 ? "0"
			: element.getAttribute("weekScore")); // 周积分
		String bpScoreString = element.getAttribute("bpScore"); // BP积分
		if (bpScoreString != null && bpScoreString.length() > 0) {
			String[] bpScoreStrings = bpScoreString.split("\\|"); 
			List<Entry<Integer,Integer>> bpScore = new ArrayList<Entry<Integer,Integer>>(bpScoreStrings.length) ; 
			for (int i = 0; i < bpScoreStrings.length; i++) {
			    String[] split = bpScoreStrings[i].split(":", 2);
				bpScore.add(new Entry<Integer,Integer>()	{
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

			this.bpScore = com.google.common.collect.ImmutableList.copyOf(bpScore);						
		} else {
			this.bpScore = java.util.Collections.emptyList();
		}
		this.challengeScore = Integer.parseInt(element.getAttribute("challengeScore") == null || element.getAttribute("challengeScore").length() == 0 ? "0"
			: element.getAttribute("challengeScore")); // 挑战积分
		this.jump = Integer.parseInt(element.getAttribute("jump") == null || element.getAttribute("jump").length() == 0 ? "0"
			: element.getAttribute("jump")); // 跳转id
		String accessModeString = element.getAttribute("accessMode"); // 接取方式
		if (accessModeString != null && accessModeString.length() > 0) {
			String[] accessModeStrings = accessModeString.split("\\|"); 
			List<Integer> accessMode = new ArrayList<Integer>(accessModeStrings.length) ; 
			for (int i = 0; i < accessModeStrings.length; i++) {
				Integer temp = Integer.parseInt(accessModeStrings[i]);
				accessMode.add(temp);
			}
			this.accessMode = com.google.common.collect.ImmutableList.copyOf(accessMode);						
		} else {
			this.accessMode = java.util.Collections.emptyList();
		}
		this.conditionalOr = Boolean.parseBoolean(element.getAttribute("conditionalOr") == null || element.getAttribute("conditionalOr").length() == 0 ? "false"
			: element.getAttribute("conditionalOr")); // 条件关系
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
		this.refreshType = Boolean.parseBoolean(element.getAttribute("refreshType") == null || element.getAttribute("refreshType").length() == 0 ? "false"
			: element.getAttribute("refreshType")); // 是否刷新
		this.isClentUpdate = Boolean.parseBoolean(element.getAttribute("isClentUpdate") == null || element.getAttribute("isClentUpdate").length() == 0 ? "false"
			: element.getAttribute("isClentUpdate")); // 是否前端出发
	}
	
	public int getId() {
		return id;
	}
	
	public List<Integer> getOpenTaskId() {
		return openTaskId;
	}
	
	public int getGroupId() {
		return groupId;
	}
	
	public QuestTypeEnum getType() {
		return type;
	}
	
	public List<Integer> getCondition() {
		return condition;
	}
	
	public int getChooseRewardType() {
		return chooseRewardType;
	}
	
	public List<Entry<Integer,Integer>> getReward() {
		return reward;
	}
	
	public int getLevel() {
		return level;
	}
	
	public int getEndLevel() {
		return endLevel;
	}
	
	public int getWeekScore() {
		return weekScore;
	}
	
	public List<Entry<Integer,Integer>> getBpScore() {
		return bpScore;
	}
	
	public int getChallengeScore() {
		return challengeScore;
	}
	
	public int getJump() {
		return jump;
	}
	
	public List<Integer> getAccessMode() {
		return accessMode;
	}
	
	public boolean getConditionalOr() {
		return conditionalOr;
	}
	
	public List<Integer> getModeOfDelivery() {
		return modeOfDelivery;
	}
	
	public boolean getRefreshType() {
		return refreshType;
	}
	
	public boolean getIsClentUpdate() {
		return isClentUpdate;
	}
	
}
