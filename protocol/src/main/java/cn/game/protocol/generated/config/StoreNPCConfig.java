package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 商店npc
 * 
 * 工具生成的，不要手动修改
 */
 public class StoreNPCConfig {

	/** id -- id */
	private final int id;		
	/** 好感 -- 好感 */
	private final int liking;		
	/** 好感值 -- 好感值 */
	private final int likingValue;		
	/** 每日奖励 -- 每日奖励 */
	private final List<Entry<Integer,Integer>> dailyPrice;		
	/** 等级奖励 -- 等级奖励 */
	private final List<Entry<Integer,Integer>> levelPrice;		
	/** 文本资源 -- 文本资源 */
	private final List<Integer> textId;		
	/** 服装资源 -- 服装资源 */
	private final List<String> clothingRes;		
	/** 动作资源 -- 动作资源 */
	private final List<String> actionRes;		

	public StoreNPCConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.liking = Integer.parseInt(element.getAttribute("liking") == null || element.getAttribute("liking").length() == 0 ? "0"
			: element.getAttribute("liking")); // 好感
		this.likingValue = Integer.parseInt(element.getAttribute("likingValue") == null || element.getAttribute("likingValue").length() == 0 ? "0"
			: element.getAttribute("likingValue")); // 好感值
		String dailyPriceString = element.getAttribute("dailyPrice"); // 每日奖励
		if (dailyPriceString != null && dailyPriceString.length() > 0) {
			String[] dailyPriceStrings = dailyPriceString.split("\\|"); 
			List<Entry<Integer,Integer>> dailyPrice = new ArrayList<Entry<Integer,Integer>>(dailyPriceStrings.length) ; 
			for (int i = 0; i < dailyPriceStrings.length; i++) {
			    String[] split = dailyPriceStrings[i].split(":", 2);
				dailyPrice.add(new Entry<Integer,Integer>()	{
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

			this.dailyPrice = com.google.common.collect.ImmutableList.copyOf(dailyPrice);						
		} else {
			this.dailyPrice = java.util.Collections.emptyList();
		}
		String levelPriceString = element.getAttribute("levelPrice"); // 等级奖励
		if (levelPriceString != null && levelPriceString.length() > 0) {
			String[] levelPriceStrings = levelPriceString.split("\\|"); 
			List<Entry<Integer,Integer>> levelPrice = new ArrayList<Entry<Integer,Integer>>(levelPriceStrings.length) ; 
			for (int i = 0; i < levelPriceStrings.length; i++) {
			    String[] split = levelPriceStrings[i].split(":", 2);
				levelPrice.add(new Entry<Integer,Integer>()	{
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

			this.levelPrice = com.google.common.collect.ImmutableList.copyOf(levelPrice);						
		} else {
			this.levelPrice = java.util.Collections.emptyList();
		}
		String textIdString = element.getAttribute("textId"); // 文本资源
		if (textIdString != null && textIdString.length() > 0) {
			String[] textIdStrings = textIdString.split("\\|"); 
			List<Integer> textId = new ArrayList<Integer>(textIdStrings.length) ; 
			for (int i = 0; i < textIdStrings.length; i++) {
				Integer temp = Integer.parseInt(textIdStrings[i]);
				textId.add(temp);
			}
			this.textId = com.google.common.collect.ImmutableList.copyOf(textId);						
		} else {
			this.textId = java.util.Collections.emptyList();
		}
		String clothingResString = element.getAttribute("clothingRes"); // 服装资源
		if (clothingResString != null && clothingResString.length() > 0) {
			String[] clothingResStrings = clothingResString.split("\\|"); 
			List<String> clothingRes = new ArrayList<String>(clothingResStrings.length) ; 
			for (int i = 0; i < clothingResStrings.length; i++) {
				String temp = clothingResStrings[i];
				clothingRes.add(temp);
			}
			this.clothingRes = com.google.common.collect.ImmutableList.copyOf(clothingRes);						
		} else {
			this.clothingRes = java.util.Collections.emptyList();
		}
		String actionResString = element.getAttribute("actionRes"); // 动作资源
		if (actionResString != null && actionResString.length() > 0) {
			String[] actionResStrings = actionResString.split("\\|"); 
			List<String> actionRes = new ArrayList<String>(actionResStrings.length) ; 
			for (int i = 0; i < actionResStrings.length; i++) {
				String temp = actionResStrings[i];
				actionRes.add(temp);
			}
			this.actionRes = com.google.common.collect.ImmutableList.copyOf(actionRes);						
		} else {
			this.actionRes = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getLiking() {
		return liking;
	}
	
	public int getLikingValue() {
		return likingValue;
	}
	
	public List<Entry<Integer,Integer>> getDailyPrice() {
		return dailyPrice;
	}
	
	public List<Entry<Integer,Integer>> getLevelPrice() {
		return levelPrice;
	}
	
	public List<Integer> getTextId() {
		return textId;
	}
	
	public List<String> getClothingRes() {
		return clothingRes;
	}
	
	public List<String> getActionRes() {
		return actionRes;
	}
	
}
