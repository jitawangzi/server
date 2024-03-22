package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 抽卡汇总表
 * 
 * 工具生成的，不要手动修改
 */
 public class DrawGroupConfig {

	/** id -- id */
	private final int id;		
	/** 卡池类型 -- 卡池类型 */
	private final int cardType;		
	/** 十连必中 -- 保底稀有度 */
	private final int floorsQuality;		
	/** 累计保底 -- 累计保底 */
	private final List<Entry<Integer,Integer>> accumulate;		
	/** 保底角色 -- 保底角色 */
	private final List<Entry<Integer,Integer>> floorsRole;		
	/** 消耗资源 -- 消耗资源 */
	private final List<Entry<Integer,Integer>> costRes;		
	/** 等级限制 -- 等级限制 */
	private final int limit;		

	public DrawGroupConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.cardType = Integer.parseInt(element.getAttribute("cardType") == null || element.getAttribute("cardType").length() == 0 ? "0"
			: element.getAttribute("cardType")); // 卡池类型
		this.floorsQuality = Integer.parseInt(element.getAttribute("floorsQuality") == null || element.getAttribute("floorsQuality").length() == 0 ? "0"
			: element.getAttribute("floorsQuality")); // 十连必中
		String accumulateString = element.getAttribute("accumulate"); // 累计保底
		if (accumulateString != null && accumulateString.length() > 0) {
			String[] accumulateStrings = accumulateString.split("\\|"); 
			List<Entry<Integer,Integer>> accumulate = new ArrayList<Entry<Integer,Integer>>(accumulateStrings.length) ; 
			for (int i = 0; i < accumulateStrings.length; i++) {
			    String[] split = accumulateStrings[i].split(":", 2);
				accumulate.add(new Entry<Integer,Integer>()	{
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

			this.accumulate = com.google.common.collect.ImmutableList.copyOf(accumulate);						
		} else {
			this.accumulate = java.util.Collections.emptyList();
		}
		String floorsRoleString = element.getAttribute("floorsRole"); // 保底角色
		if (floorsRoleString != null && floorsRoleString.length() > 0) {
			String[] floorsRoleStrings = floorsRoleString.split("\\|"); 
			List<Entry<Integer,Integer>> floorsRole = new ArrayList<Entry<Integer,Integer>>(floorsRoleStrings.length) ; 
			for (int i = 0; i < floorsRoleStrings.length; i++) {
			    String[] split = floorsRoleStrings[i].split(":", 2);
				floorsRole.add(new Entry<Integer,Integer>()	{
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

			this.floorsRole = com.google.common.collect.ImmutableList.copyOf(floorsRole);						
		} else {
			this.floorsRole = java.util.Collections.emptyList();
		}
		String costResString = element.getAttribute("costRes"); // 消耗资源
		if (costResString != null && costResString.length() > 0) {
			String[] costResStrings = costResString.split("\\|"); 
			List<Entry<Integer,Integer>> costRes = new ArrayList<Entry<Integer,Integer>>(costResStrings.length) ; 
			for (int i = 0; i < costResStrings.length; i++) {
			    String[] split = costResStrings[i].split(":", 2);
				costRes.add(new Entry<Integer,Integer>()	{
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

			this.costRes = com.google.common.collect.ImmutableList.copyOf(costRes);						
		} else {
			this.costRes = java.util.Collections.emptyList();
		}
		this.limit = Integer.parseInt(element.getAttribute("limit") == null || element.getAttribute("limit").length() == 0 ? "0"
			: element.getAttribute("limit")); // 等级限制
	}
	
	public int getId() {
		return id;
	}
	
	public int getCardType() {
		return cardType;
	}
	
	public int getFloorsQuality() {
		return floorsQuality;
	}
	
	public List<Entry<Integer,Integer>> getAccumulate() {
		return accumulate;
	}
	
	public List<Entry<Integer,Integer>> getFloorsRole() {
		return floorsRole;
	}
	
	public List<Entry<Integer,Integer>> getCostRes() {
		return costRes;
	}
	
	public int getLimit() {
		return limit;
	}
	
}
