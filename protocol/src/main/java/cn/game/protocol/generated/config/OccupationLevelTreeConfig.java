package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 职介天赋树
 * 
 * 工具生成的，不要手动修改
 */
 public class OccupationLevelTreeConfig {

	/** id -- id */
	private final int id;		
	/** 类型 -- 类型 */
	private final int type;		
	/** 解锁消耗 -- 解锁消耗 */
	private final List<Entry<Integer,Integer>> cost;		
	/** 限制等级 -- 限制等级 */
	private final int levelLimit;		
	/** 角色id -- 角色id */
	private final int roleId;		
	/** 奖励属性 -- 奖励属性 */
	private final List<Entry<Integer,Integer>> rewardAtt;		

	public OccupationLevelTreeConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 类型
		String costString = element.getAttribute("cost"); // 解锁消耗
		if (costString != null && costString.length() > 0) {
			String[] costStrings = costString.split("\\|"); 
			List<Entry<Integer,Integer>> cost = new ArrayList<Entry<Integer,Integer>>(costStrings.length) ; 
			for (int i = 0; i < costStrings.length; i++) {
			    String[] split = costStrings[i].split(":", 2);
				cost.add(new Entry<Integer,Integer>()	{
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

			this.cost = com.google.common.collect.ImmutableList.copyOf(cost);						
		} else {
			this.cost = java.util.Collections.emptyList();
		}
		this.levelLimit = Integer.parseInt(element.getAttribute("levelLimit") == null || element.getAttribute("levelLimit").length() == 0 ? "0"
			: element.getAttribute("levelLimit")); // 限制等级
		this.roleId = Integer.parseInt(element.getAttribute("roleId") == null || element.getAttribute("roleId").length() == 0 ? "0"
			: element.getAttribute("roleId")); // 角色id
		String rewardAttString = element.getAttribute("rewardAtt"); // 奖励属性
		if (rewardAttString != null && rewardAttString.length() > 0) {
			String[] rewardAttStrings = rewardAttString.split("\\|"); 
			List<Entry<Integer,Integer>> rewardAtt = new ArrayList<Entry<Integer,Integer>>(rewardAttStrings.length) ; 
			for (int i = 0; i < rewardAttStrings.length; i++) {
			    String[] split = rewardAttStrings[i].split(":", 2);
				rewardAtt.add(new Entry<Integer,Integer>()	{
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

			this.rewardAtt = com.google.common.collect.ImmutableList.copyOf(rewardAtt);						
		} else {
			this.rewardAtt = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getType() {
		return type;
	}
	
	public List<Entry<Integer,Integer>> getCost() {
		return cost;
	}
	
	public int getLevelLimit() {
		return levelLimit;
	}
	
	public int getRoleId() {
		return roleId;
	}
	
	public List<Entry<Integer,Integer>> getRewardAtt() {
		return rewardAtt;
	}
	
}
