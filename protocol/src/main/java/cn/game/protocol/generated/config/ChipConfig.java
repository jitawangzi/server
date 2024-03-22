package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 芯片
 * 
 * 工具生成的，不要手动修改
 */
 public class ChipConfig {

	/** 芯片id -- 前四位表示芯片id，后两位表示强化等级 */
	private final int id;		
	/** 芯片颜色 -- 1-红色； 2-黄色； 3-蓝色； 4-橙色； */
	private final int color;		
	/** 芯片类型 -- 1-主要芯片； 2-次要芯片； */
	private final int type;		
	/** 稀有度 -- 1绿色 2蓝色 3紫色 4橙色 */
	private final int quality;		
	/** 芯片技能 */
	private final int skill;		
	/** 洗练消耗道具 -- 道具id:道具数量|道具id:道具数量... */
	private final List<Entry<Integer,Integer>> BreakExpendItem;		
	/** 分解获得道具 -- 道具id:道具数量|道具id:道具数量... */
	private final List<Entry<Integer,Integer>> decompose;		
	/** 分解消耗道具 -- 道具id:道具数量|道具id:道具数量... */
	private final List<Entry<Integer,Integer>> costItem;		

	public ChipConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // 芯片id
		this.color = Integer.parseInt(element.getAttribute("color") == null || element.getAttribute("color").length() == 0 ? "0"
			: element.getAttribute("color")); // 芯片颜色
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 芯片类型
		this.quality = Integer.parseInt(element.getAttribute("quality") == null || element.getAttribute("quality").length() == 0 ? "0"
			: element.getAttribute("quality")); // 稀有度
		this.skill = Integer.parseInt(element.getAttribute("skill") == null || element.getAttribute("skill").length() == 0 ? "0"
			: element.getAttribute("skill")); // 芯片技能
		String BreakExpendItemString = element.getAttribute("BreakExpendItem"); // 洗练消耗道具
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
	
	public int getColor() {
		return color;
	}
	
	public int getType() {
		return type;
	}
	
	public int getQuality() {
		return quality;
	}
	
	public int getSkill() {
		return skill;
	}
	
	public List<Entry<Integer,Integer>> getBreakExpendItem() {
		return BreakExpendItem;
	}
	
	public List<Entry<Integer,Integer>> getDecompose() {
		return decompose;
	}
	
	public List<Entry<Integer,Integer>> getCostItem() {
		return costItem;
	}
	
}
