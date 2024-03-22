package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import cn.game.util.Weightable;
import org.w3c.dom.Element;

/**
 * 策略卡表
 * 
 * 工具生成的，不要手动修改
 */
 public class StrategyCardConfig implements Weightable {

	/** id -- id */
	private final int id;		
	/** 名称 -- 名称 */
	private final String name;		
	/** 类型 -- 类型 */
	private final int type;		
	/** 贵重程度 -- 贵重程度 */
	private final int level;		
	/** cost值 -- cost值 */
	private final int cost;		
	/** 效果 -- 效果 */
	private final List<Integer> buffIds;		
	/** 售卖价格 -- 售卖价格 */
	private final List<Entry<Integer,Integer>> price;		
	/** 权重 -- 在探索结算时策略卡的权重 */
	private final int weight;		

	public StrategyCardConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.name = element.getAttribute("name"); // 名称
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 类型
		this.level = Integer.parseInt(element.getAttribute("level") == null || element.getAttribute("level").length() == 0 ? "0"
			: element.getAttribute("level")); // 贵重程度
		this.cost = Integer.parseInt(element.getAttribute("cost") == null || element.getAttribute("cost").length() == 0 ? "0"
			: element.getAttribute("cost")); // cost值
		String buffIdsString = element.getAttribute("buffIds"); // 效果
		if (buffIdsString != null && buffIdsString.length() > 0) {
			String[] buffIdsStrings = buffIdsString.split("\\|"); 
			List<Integer> buffIds = new ArrayList<Integer>(buffIdsStrings.length) ; 
			for (int i = 0; i < buffIdsStrings.length; i++) {
				Integer temp = Integer.parseInt(buffIdsStrings[i]);
				buffIds.add(temp);
			}
			this.buffIds = com.google.common.collect.ImmutableList.copyOf(buffIds);						
		} else {
			this.buffIds = java.util.Collections.emptyList();
		}
		String priceString = element.getAttribute("price"); // 售卖价格
		if (priceString != null && priceString.length() > 0) {
			String[] priceStrings = priceString.split("\\|"); 
			List<Entry<Integer,Integer>> price = new ArrayList<Entry<Integer,Integer>>(priceStrings.length) ; 
			for (int i = 0; i < priceStrings.length; i++) {
			    String[] split = priceStrings[i].split(":", 2);
				price.add(new Entry<Integer,Integer>()	{
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

			this.price = com.google.common.collect.ImmutableList.copyOf(price);						
		} else {
			this.price = java.util.Collections.emptyList();
		}
		this.weight = Integer.parseInt(element.getAttribute("weight") == null || element.getAttribute("weight").length() == 0 ? "0"
			: element.getAttribute("weight")); // 权重
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public int getType() {
		return type;
	}
	
	public int getLevel() {
		return level;
	}
	
	public int getCost() {
		return cost;
	}
	
	public List<Integer> getBuffIds() {
		return buffIds;
	}
	
	public List<Entry<Integer,Integer>> getPrice() {
		return price;
	}
	
	public int getWeight() {
		return weight;
	}
	
	@Override
	public int weight() {
		return this.weight;
	}
}
