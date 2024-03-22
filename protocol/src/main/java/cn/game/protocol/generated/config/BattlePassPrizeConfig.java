package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 通行证奖励
 * 
 * 工具生成的，不要手动修改
 */
 public class BattlePassPrizeConfig {

	/** id -- 活动id+等级 */
	private final int id;		
	/** 等级 -- 等级 */
	private final int level;		
	/** 活动id -- 活动id */
	private final int actId;		
	/** 经验值 -- 经验值 */
	private final int unlockCost;		
	/** 等级购买价格 -- 等级购买价格 */
	private final int levelPrice;		
	/** 金色勋章奖励id -- 金色奖励 */
	private final List<Entry<Integer,Integer>> goldMedal;		
	/** 银色勋章奖励id -- 银色奖励 */
	private final List<Entry<Integer,Integer>> silverMedal;		

	public BattlePassPrizeConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.level = Integer.parseInt(element.getAttribute("level") == null || element.getAttribute("level").length() == 0 ? "0"
			: element.getAttribute("level")); // 等级
		this.actId = Integer.parseInt(element.getAttribute("actId") == null || element.getAttribute("actId").length() == 0 ? "0"
			: element.getAttribute("actId")); // 活动id
		this.unlockCost = Integer.parseInt(element.getAttribute("unlockCost") == null || element.getAttribute("unlockCost").length() == 0 ? "0"
			: element.getAttribute("unlockCost")); // 经验值
		this.levelPrice = Integer.parseInt(element.getAttribute("levelPrice") == null || element.getAttribute("levelPrice").length() == 0 ? "0"
			: element.getAttribute("levelPrice")); // 等级购买价格
		String goldMedalString = element.getAttribute("goldMedal"); // 金色勋章奖励id
		if (goldMedalString != null && goldMedalString.length() > 0) {
			String[] goldMedalStrings = goldMedalString.split("\\|"); 
			List<Entry<Integer,Integer>> goldMedal = new ArrayList<Entry<Integer,Integer>>(goldMedalStrings.length) ; 
			for (int i = 0; i < goldMedalStrings.length; i++) {
			    String[] split = goldMedalStrings[i].split(":", 2);
				goldMedal.add(new Entry<Integer,Integer>()	{
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

			this.goldMedal = com.google.common.collect.ImmutableList.copyOf(goldMedal);						
		} else {
			this.goldMedal = java.util.Collections.emptyList();
		}
		String silverMedalString = element.getAttribute("silverMedal"); // 银色勋章奖励id
		if (silverMedalString != null && silverMedalString.length() > 0) {
			String[] silverMedalStrings = silverMedalString.split("\\|"); 
			List<Entry<Integer,Integer>> silverMedal = new ArrayList<Entry<Integer,Integer>>(silverMedalStrings.length) ; 
			for (int i = 0; i < silverMedalStrings.length; i++) {
			    String[] split = silverMedalStrings[i].split(":", 2);
				silverMedal.add(new Entry<Integer,Integer>()	{
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

			this.silverMedal = com.google.common.collect.ImmutableList.copyOf(silverMedal);						
		} else {
			this.silverMedal = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getLevel() {
		return level;
	}
	
	public int getActId() {
		return actId;
	}
	
	public int getUnlockCost() {
		return unlockCost;
	}
	
	public int getLevelPrice() {
		return levelPrice;
	}
	
	public List<Entry<Integer,Integer>> getGoldMedal() {
		return goldMedal;
	}
	
	public List<Entry<Integer,Integer>> getSilverMedal() {
		return silverMedal;
	}
	
}
