package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 战区事件
 * 
 * 工具生成的，不要手动修改
 */
 public class BattleEventConfig {

	/** id */
	private final int id;		
	/** 事件类型 -- 1-经验事件 2-金币事件 3-核心事件 4-芯片事件 5-礼物事件 6-卡卷事件 */
	private final int type;		
	/** 事件关卡 */
	private final int level;		
	/** 等级区间 -- type*100+rankInterval */
	private final int rankInterval;		
	/** 固定奖励 */
	private final List<Entry<Integer,Integer>> reward;		
	/** 随机奖励 */
	private final int randomReward;		

	public BattleEventConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 事件类型
		this.level = Integer.parseInt(element.getAttribute("level") == null || element.getAttribute("level").length() == 0 ? "0"
			: element.getAttribute("level")); // 事件关卡
		this.rankInterval = Integer.parseInt(element.getAttribute("rankInterval") == null || element.getAttribute("rankInterval").length() == 0 ? "0"
			: element.getAttribute("rankInterval")); // 等级区间
		String rewardString = element.getAttribute("reward"); // 固定奖励
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
		this.randomReward = Integer.parseInt(element.getAttribute("randomReward") == null || element.getAttribute("randomReward").length() == 0 ? "0"
			: element.getAttribute("randomReward")); // 随机奖励
	}
	
	public int getId() {
		return id;
	}
	
	public int getType() {
		return type;
	}
	
	public int getLevel() {
		return level;
	}
	
	public int getRankInterval() {
		return rankInterval;
	}
	
	public List<Entry<Integer,Integer>> getReward() {
		return reward;
	}
	
	public int getRandomReward() {
		return randomReward;
	}
	
}
