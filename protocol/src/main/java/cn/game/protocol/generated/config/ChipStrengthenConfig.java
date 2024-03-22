package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 芯片升级
 * 
 * 工具生成的，不要手动修改
 */
 public class ChipStrengthenConfig {

	/** id -- 前四位表示芯片id，后两位表示强化等级 */
	private final int id;		
	/** 等级 */
	private final int level;		
	/** 强化消耗道具 -- 道具id:道具数量|道具id:道具数量... */
	private final List<Entry<Integer,Integer>> StrengthenExpendItem;		
	/** 强化消耗道具汇总 -- 道具id:道具数量|道具id:道具数量... */
	private final List<Entry<Integer,Integer>> StrengthenExpendItemTotal;		

	public ChipStrengthenConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.level = Integer.parseInt(element.getAttribute("level") == null || element.getAttribute("level").length() == 0 ? "0"
			: element.getAttribute("level")); // 等级
		String StrengthenExpendItemString = element.getAttribute("StrengthenExpendItem"); // 强化消耗道具
		if (StrengthenExpendItemString != null && StrengthenExpendItemString.length() > 0) {
			String[] StrengthenExpendItemStrings = StrengthenExpendItemString.split("\\|"); 
			List<Entry<Integer,Integer>> StrengthenExpendItem = new ArrayList<Entry<Integer,Integer>>(StrengthenExpendItemStrings.length) ; 
			for (int i = 0; i < StrengthenExpendItemStrings.length; i++) {
			    String[] split = StrengthenExpendItemStrings[i].split(":", 2);
				StrengthenExpendItem.add(new Entry<Integer,Integer>()	{
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

			this.StrengthenExpendItem = com.google.common.collect.ImmutableList.copyOf(StrengthenExpendItem);						
		} else {
			this.StrengthenExpendItem = java.util.Collections.emptyList();
		}
		String StrengthenExpendItemTotalString = element.getAttribute("StrengthenExpendItemTotal"); // 强化消耗道具汇总
		if (StrengthenExpendItemTotalString != null && StrengthenExpendItemTotalString.length() > 0) {
			String[] StrengthenExpendItemTotalStrings = StrengthenExpendItemTotalString.split("\\|"); 
			List<Entry<Integer,Integer>> StrengthenExpendItemTotal = new ArrayList<Entry<Integer,Integer>>(StrengthenExpendItemTotalStrings.length) ; 
			for (int i = 0; i < StrengthenExpendItemTotalStrings.length; i++) {
			    String[] split = StrengthenExpendItemTotalStrings[i].split(":", 2);
				StrengthenExpendItemTotal.add(new Entry<Integer,Integer>()	{
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

			this.StrengthenExpendItemTotal = com.google.common.collect.ImmutableList.copyOf(StrengthenExpendItemTotal);						
		} else {
			this.StrengthenExpendItemTotal = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getLevel() {
		return level;
	}
	
	public List<Entry<Integer,Integer>> getStrengthenExpendItem() {
		return StrengthenExpendItem;
	}
	
	public List<Entry<Integer,Integer>> getStrengthenExpendItemTotal() {
		return StrengthenExpendItemTotal;
	}
	
}
