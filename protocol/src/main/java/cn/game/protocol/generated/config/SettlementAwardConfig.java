package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 爬塔结算表
 * 
 * 工具生成的，不要手动修改
 */
 public class SettlementAwardConfig {

	/** id -- 晋级类型 1-青铜 2-白银 3-黄金 4-铂金 5-钻石 6-大师 7-王者 8-最强王者 */
	private final int id;		
	/** 结算奖励 -- 结算奖励 */
	private final List<Entry<Integer,Integer>> reward;		

	public SettlementAwardConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		String rewardString = element.getAttribute("reward"); // 结算奖励
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
	
	public List<Entry<Integer,Integer>> getReward() {
		return reward;
	}
	
}
