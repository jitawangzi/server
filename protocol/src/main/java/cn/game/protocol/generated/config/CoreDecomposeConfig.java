package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 核心分解
 * 
 * 工具生成的，不要手动修改
 */
 public class CoreDecomposeConfig {

	/** 核心id */
	private final int id;		
	/** 分解获得道具 -- 道具id:道具数量|道具id:道具数量... */
	private final List<Entry<Integer,Integer>> decompose;		
	/** 分解消耗道具 -- 道具id:道具数量|道具id:道具数量... */
	private final List<Entry<Integer,Integer>> costItem;		

	public CoreDecomposeConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // 核心id
		String decomposeString = element.getAttribute("decompose"); // 分解获得道具
		if (decomposeString != null && decomposeString.length() > 0) {
			String[] decomposeStrings = decomposeString.split("\\|"); 
			List<Entry<Integer,Integer>> decompose = new ArrayList<Entry<Integer,Integer>>(decomposeStrings.length) ; 
			for (int i = 0; i < decomposeStrings.length; i++) {
			    String[] split = decomposeStrings[i].split(":", 2);
				decompose.add(new Entry<Integer,Integer>()	{
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

			this.decompose = com.google.common.collect.ImmutableList.copyOf(decompose);						
		} else {
			this.decompose = java.util.Collections.emptyList();
		}
		String costItemString = element.getAttribute("costItem"); // 分解消耗道具
		if (costItemString != null && costItemString.length() > 0) {
			String[] costItemStrings = costItemString.split("\\|"); 
			List<Entry<Integer,Integer>> costItem = new ArrayList<Entry<Integer,Integer>>(costItemStrings.length) ; 
			for (int i = 0; i < costItemStrings.length; i++) {
			    String[] split = costItemStrings[i].split(":", 2);
				costItem.add(new Entry<Integer,Integer>()	{
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

			this.costItem = com.google.common.collect.ImmutableList.copyOf(costItem);						
		} else {
			this.costItem = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public List<Entry<Integer,Integer>> getDecompose() {
		return decompose;
	}
	
	public List<Entry<Integer,Integer>> getCostItem() {
		return costItem;
	}
	
}
