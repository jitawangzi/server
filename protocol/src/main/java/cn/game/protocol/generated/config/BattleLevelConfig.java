package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 战区关卡
 * 
 * 工具生成的，不要手动修改
 */
 public class BattleLevelConfig {

	/** id -- 列传10001 剧情20001 探索怪物40001 */
	private final int id;		
	/** 通关条件 -- 1-消灭全部敌人 */
	private final int passCondition;		
	/** 通关条件参数 */
	private final List<Integer> passParameter;		
	/** 关卡类型 -- 1-主线 2-支线 */
	private final int type;		
	/** 解锁条件 */
	private final List<Integer> condition;		
	/** 能量消耗 */
	private final int energyExpend;		
	/** 怪物序列 */
	private final List<Integer> monsterSequence;		
	/** 星级条件 -- 1-通关 2-阵亡人数 3-剩余血量百分比 */
	private final List<List<Integer>> starLevelCondition;		
	/** 星级奖励 -- ResourceEnum的id */
	private final List<Entry<Integer,Integer>> starLevelReward;		
	/** 积分奖励 */
	private final List<Integer> scoreReward;		
	/** 晋升点数 -- 用于升级角色的晋升等级 */
	private final int PromotionPoint;		
	/** 常规奖励 */
	private final List<Entry<Integer,Integer>> baseReward;		
	/** 特殊奖励 -- 根据战斗类型特殊奖励意义不同，具体根据DungeonTypeEnum决定 */
	private final List<Entry<Integer,Integer>> specialReward;		
	/** 随机奖励 -- 读取RandomRewardGroup-->groupId */
	private final int[] randomReward;		
	/** 章节id */
	private final int battleChapterId;		
	/** 扩展参数 -- 部分系统中的战斗需要使用的参数 */
	private final List<Integer> extParameter;		

	public BattleLevelConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.passCondition = Integer.parseInt(element.getAttribute("passCondition") == null || element.getAttribute("passCondition").length() == 0 ? "0"
			: element.getAttribute("passCondition")); // 通关条件
		String passParameterString = element.getAttribute("passParameter"); // 通关条件参数
		if (passParameterString != null && passParameterString.length() > 0) {
			String[] passParameterStrings = passParameterString.split("\\|"); 
			List<Integer> passParameter = new ArrayList<Integer>(passParameterStrings.length) ; 
			for (int i = 0; i < passParameterStrings.length; i++) {
				Integer temp = Integer.parseInt(passParameterStrings[i]);
				passParameter.add(temp);
			}
			this.passParameter = com.google.common.collect.ImmutableList.copyOf(passParameter);						
		} else {
			this.passParameter = java.util.Collections.emptyList();
		}
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 关卡类型
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
		this.energyExpend = Integer.parseInt(element.getAttribute("energyExpend") == null || element.getAttribute("energyExpend").length() == 0 ? "0"
			: element.getAttribute("energyExpend")); // 能量消耗
		String monsterSequenceString = element.getAttribute("monsterSequence"); // 怪物序列
		if (monsterSequenceString != null && monsterSequenceString.length() > 0) {
			String[] monsterSequenceStrings = monsterSequenceString.split("\\|"); 
			List<Integer> monsterSequence = new ArrayList<Integer>(monsterSequenceStrings.length) ; 
			for (int i = 0; i < monsterSequenceStrings.length; i++) {
				Integer temp = Integer.parseInt(monsterSequenceStrings[i]);
				monsterSequence.add(temp);
			}
			this.monsterSequence = com.google.common.collect.ImmutableList.copyOf(monsterSequence);						
		} else {
			this.monsterSequence = java.util.Collections.emptyList();
		}
		String starLevelConditionString = element.getAttribute("starLevelCondition"); // 星级条件
		if (starLevelConditionString != null && starLevelConditionString.length() > 0) {
			String[] starLevelConditionStrings = starLevelConditionString.split("\\|"); 
			List<List<Integer>> starLevelCondition = new ArrayList<List<Integer>>(starLevelConditionStrings.length) ; 
			for (int i = 0; i < starLevelConditionStrings.length; i++) {
				String[] starLevelConditionStrings2 = starLevelConditionStrings[i].split(":"); 
				List<Integer> list = new ArrayList<Integer>(starLevelConditionStrings2.length) ; 
				for (int j = 0; j < starLevelConditionStrings2.length; j++) {
					Integer temp = Integer.parseInt(starLevelConditionStrings2[j]);
					list.add(temp) ; 
				}
				starLevelCondition.add(list);
			}
			this.starLevelCondition = com.google.common.collect.ImmutableList.copyOf(starLevelCondition);						
		} else {
			this.starLevelCondition = java.util.Collections.emptyList();
		}
		String starLevelRewardString = element.getAttribute("starLevelReward"); // 星级奖励
		if (starLevelRewardString != null && starLevelRewardString.length() > 0) {
			String[] starLevelRewardStrings = starLevelRewardString.split("\\|"); 
			List<Entry<Integer,Integer>> starLevelReward = new ArrayList<Entry<Integer,Integer>>(starLevelRewardStrings.length) ; 
			for (int i = 0; i < starLevelRewardStrings.length; i++) {
			    String[] split = starLevelRewardStrings[i].split(":", 2);
				starLevelReward.add(new Entry<Integer,Integer>()	{
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

			this.starLevelReward = com.google.common.collect.ImmutableList.copyOf(starLevelReward);						
		} else {
			this.starLevelReward = java.util.Collections.emptyList();
		}
		String scoreRewardString = element.getAttribute("scoreReward"); // 积分奖励
		if (scoreRewardString != null && scoreRewardString.length() > 0) {
			String[] scoreRewardStrings = scoreRewardString.split("\\|"); 
			List<Integer> scoreReward = new ArrayList<Integer>(scoreRewardStrings.length) ; 
			for (int i = 0; i < scoreRewardStrings.length; i++) {
				Integer temp = Integer.parseInt(scoreRewardStrings[i]);
				scoreReward.add(temp);
			}
			this.scoreReward = com.google.common.collect.ImmutableList.copyOf(scoreReward);						
		} else {
			this.scoreReward = java.util.Collections.emptyList();
		}
		this.PromotionPoint = Integer.parseInt(element.getAttribute("PromotionPoint") == null || element.getAttribute("PromotionPoint").length() == 0 ? "0"
			: element.getAttribute("PromotionPoint")); // 晋升点数
		String baseRewardString = element.getAttribute("baseReward"); // 常规奖励
		if (baseRewardString != null && baseRewardString.length() > 0) {
			String[] baseRewardStrings = baseRewardString.split("\\|"); 
			List<Entry<Integer,Integer>> baseReward = new ArrayList<Entry<Integer,Integer>>(baseRewardStrings.length) ; 
			for (int i = 0; i < baseRewardStrings.length; i++) {
			    String[] split = baseRewardStrings[i].split(":", 2);
				baseReward.add(new Entry<Integer,Integer>()	{
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

			this.baseReward = com.google.common.collect.ImmutableList.copyOf(baseReward);						
		} else {
			this.baseReward = java.util.Collections.emptyList();
		}
		String specialRewardString = element.getAttribute("specialReward"); // 特殊奖励
		if (specialRewardString != null && specialRewardString.length() > 0) {
			String[] specialRewardStrings = specialRewardString.split("\\|"); 
			List<Entry<Integer,Integer>> specialReward = new ArrayList<Entry<Integer,Integer>>(specialRewardStrings.length) ; 
			for (int i = 0; i < specialRewardStrings.length; i++) {
			    String[] split = specialRewardStrings[i].split(":", 2);
				specialReward.add(new Entry<Integer,Integer>()	{
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

			this.specialReward = com.google.common.collect.ImmutableList.copyOf(specialReward);						
		} else {
			this.specialReward = java.util.Collections.emptyList();
		}
		String randomRewardString = element.getAttribute("randomReward"); // 随机奖励
		if (randomRewardString != null && randomRewardString.length() > 0) {
			String[] randomRewardStrings = randomRewardString.split("\\|"); 
			int[] randomReward = new int[randomRewardStrings.length] ; 
			for (int i = 0; i < randomRewardStrings.length; i++) {
				int temp = Integer.parseInt(randomRewardStrings[i]);
				randomReward[i] = temp;
			}
			this.randomReward = randomReward ;			
		} else {
			this.randomReward = new int[] {};
		}
		this.battleChapterId = Integer.parseInt(element.getAttribute("battleChapterId") == null || element.getAttribute("battleChapterId").length() == 0 ? "0"
			: element.getAttribute("battleChapterId")); // 章节id
		String extParameterString = element.getAttribute("extParameter"); // 扩展参数
		if (extParameterString != null && extParameterString.length() > 0) {
			String[] extParameterStrings = extParameterString.split("\\|"); 
			List<Integer> extParameter = new ArrayList<Integer>(extParameterStrings.length) ; 
			for (int i = 0; i < extParameterStrings.length; i++) {
				Integer temp = Integer.parseInt(extParameterStrings[i]);
				extParameter.add(temp);
			}
			this.extParameter = com.google.common.collect.ImmutableList.copyOf(extParameter);						
		} else {
			this.extParameter = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getPassCondition() {
		return passCondition;
	}
	
	public List<Integer> getPassParameter() {
		return passParameter;
	}
	
	public int getType() {
		return type;
	}
	
	public List<Integer> getCondition() {
		return condition;
	}
	
	public int getEnergyExpend() {
		return energyExpend;
	}
	
	public List<Integer> getMonsterSequence() {
		return monsterSequence;
	}
	
	public List<List<Integer>> getStarLevelCondition() {
		return starLevelCondition;
	}
	
	public List<Entry<Integer,Integer>> getStarLevelReward() {
		return starLevelReward;
	}
	
	public List<Integer> getScoreReward() {
		return scoreReward;
	}
	
	public int getPromotionPoint() {
		return PromotionPoint;
	}
	
	public List<Entry<Integer,Integer>> getBaseReward() {
		return baseReward;
	}
	
	public List<Entry<Integer,Integer>> getSpecialReward() {
		return specialReward;
	}
	
	public int[] getRandomReward() {
		return randomReward;
	}
	
	public int getBattleChapterId() {
		return battleChapterId;
	}
	
	public List<Integer> getExtParameter() {
		return extParameter;
	}
	
}
