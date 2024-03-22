package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 签到奖励表
 * 
 * 工具生成的，不要手动修改
 */
 public class SignInPrizeConfig {

	/** id -- 月份+日 */
	private final int id;		
	/** 活动id -- 活动id */
	private final int activId;		
	/** 可领取开始日期 -- 日期 */
	private final int starDate;		
	/** 每日奖励 -- 每日奖励 */
	private final List<Entry<Integer,Integer>> dailyPrice;		

	public SignInPrizeConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.activId = Integer.parseInt(element.getAttribute("activId") == null || element.getAttribute("activId").length() == 0 ? "0"
			: element.getAttribute("activId")); // 活动id
		this.starDate = Integer.parseInt(element.getAttribute("starDate") == null || element.getAttribute("starDate").length() == 0 ? "0"
			: element.getAttribute("starDate")); // 可领取开始日期
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
	}
	
	public int getId() {
		return id;
	}
	
	public int getActivId() {
		return activId;
	}
	
	public int getStarDate() {
		return starDate;
	}
	
	public List<Entry<Integer,Integer>> getDailyPrice() {
		return dailyPrice;
	}
	
}
