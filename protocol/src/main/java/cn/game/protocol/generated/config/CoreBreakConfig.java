package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 核心突破
 * 
 * 工具生成的，不要手动修改
 */
 public class CoreBreakConfig {

	/** 核心id -- 品质*1000+突破等级 */
	private final int id;		
	/** 突破消耗道具 -- 道具id:道具数量|道具id:道具数量... */
	private final List<Entry<Integer,Integer>> BreakExpendItem;		
	/** 突破消耗道具汇总 -- 道具id:道具数量|道具id:道具数量... */
	private final List<Entry<Integer,Integer>> BreakExpendItemTotal;		

	public CoreBreakConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // 核心id
		String BreakExpendItemString = element.getAttribute("BreakExpendItem"); // 突破消耗道具
		if (BreakExpendItemString != null && BreakExpendItemString.length() > 0) {
			String[] BreakExpendItemStrings = BreakExpendItemString.split("\\|"); 
			List<Entry<Integer,Integer>> BreakExpendItem = new ArrayList<Entry<Integer,Integer>>(BreakExpendItemStrings.length) ; 
			for (int i = 0; i < BreakExpendItemStrings.length; i++) {
			    String[] split = BreakExpendItemStrings[i].split(":", 2);
				BreakExpendItem.add(new Entry<Integer,Integer>()	{
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

			this.BreakExpendItem = com.google.common.collect.ImmutableList.copyOf(BreakExpendItem);						
		} else {
			this.BreakExpendItem = java.util.Collections.emptyList();
		}
		String BreakExpendItemTotalString = element.getAttribute("BreakExpendItemTotal"); // 突破消耗道具汇总
		if (BreakExpendItemTotalString != null && BreakExpendItemTotalString.length() > 0) {
			String[] BreakExpendItemTotalStrings = BreakExpendItemTotalString.split("\\|"); 
			List<Entry<Integer,Integer>> BreakExpendItemTotal = new ArrayList<Entry<Integer,Integer>>(BreakExpendItemTotalStrings.length) ; 
			for (int i = 0; i < BreakExpendItemTotalStrings.length; i++) {
			    String[] split = BreakExpendItemTotalStrings[i].split(":", 2);
				BreakExpendItemTotal.add(new Entry<Integer,Integer>()	{
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

			this.BreakExpendItemTotal = com.google.common.collect.ImmutableList.copyOf(BreakExpendItemTotal);						
		} else {
			this.BreakExpendItemTotal = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public List<Entry<Integer,Integer>> getBreakExpendItem() {
		return BreakExpendItem;
	}
	
	public List<Entry<Integer,Integer>> getBreakExpendItemTotal() {
		return BreakExpendItemTotal;
	}
	
}
