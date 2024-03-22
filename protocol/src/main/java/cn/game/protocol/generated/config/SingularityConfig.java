package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 奇点属性表
 * 
 * 工具生成的，不要手动修改
 */
 public class SingularityConfig {

	/** id -- id */
	private final int id;		
	/** 稀有度 -- 稀有度 */
	private final int quality;		
	/** 合成目标 -- 合成目标 */
	private final int composeTarget;		
	/** 合成消耗材料 -- 合成消耗材料 */
	private final List<Entry<Integer,Integer>> composeCost;		
	/** 重构消耗 -- 重构消耗 */
	private final List<Entry<Integer,Integer>> resetCost;		
	/** 主属性类型 -- 主属性类型 */
	private final int mainType;		
	/** 主属性范围 -- 主属性范围 */
	private final List<Integer> mainTypeRange;		
	/** 负属性类型 -- 负属性类型 */
	private final int lossType;		
	/** 负属性范围 -- 负属性范围 */
	private final List<Integer> lossTypeRange;		

	public SingularityConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.quality = Integer.parseInt(element.getAttribute("quality") == null || element.getAttribute("quality").length() == 0 ? "0"
			: element.getAttribute("quality")); // 稀有度
		this.composeTarget = Integer.parseInt(element.getAttribute("composeTarget") == null || element.getAttribute("composeTarget").length() == 0 ? "0"
			: element.getAttribute("composeTarget")); // 合成目标
		String composeCostString = element.getAttribute("composeCost"); // 合成消耗材料
		if (composeCostString != null && composeCostString.length() > 0) {
			String[] composeCostStrings = composeCostString.split("\\|"); 
			List<Entry<Integer,Integer>> composeCost = new ArrayList<Entry<Integer,Integer>>(composeCostStrings.length) ; 
			for (int i = 0; i < composeCostStrings.length; i++) {
			    String[] split = composeCostStrings[i].split(":", 2);
				composeCost.add(new Entry<Integer,Integer>()	{
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

			this.composeCost = com.google.common.collect.ImmutableList.copyOf(composeCost);						
		} else {
			this.composeCost = java.util.Collections.emptyList();
		}
		String resetCostString = element.getAttribute("resetCost"); // 重构消耗
		if (resetCostString != null && resetCostString.length() > 0) {
			String[] resetCostStrings = resetCostString.split("\\|"); 
			List<Entry<Integer,Integer>> resetCost = new ArrayList<Entry<Integer,Integer>>(resetCostStrings.length) ; 
			for (int i = 0; i < resetCostStrings.length; i++) {
			    String[] split = resetCostStrings[i].split(":", 2);
				resetCost.add(new Entry<Integer,Integer>()	{
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

			this.resetCost = com.google.common.collect.ImmutableList.copyOf(resetCost);						
		} else {
			this.resetCost = java.util.Collections.emptyList();
		}
		this.mainType = Integer.parseInt(element.getAttribute("mainType") == null || element.getAttribute("mainType").length() == 0 ? "0"
			: element.getAttribute("mainType")); // 主属性类型
		String mainTypeRangeString = element.getAttribute("mainTypeRange"); // 主属性范围
		if (mainTypeRangeString != null && mainTypeRangeString.length() > 0) {
			String[] mainTypeRangeStrings = mainTypeRangeString.split("\\|"); 
			List<Integer> mainTypeRange = new ArrayList<Integer>(mainTypeRangeStrings.length) ; 
			for (int i = 0; i < mainTypeRangeStrings.length; i++) {
				Integer temp = Integer.parseInt(mainTypeRangeStrings[i]);
				mainTypeRange.add(temp);
			}
			this.mainTypeRange = com.google.common.collect.ImmutableList.copyOf(mainTypeRange);						
		} else {
			this.mainTypeRange = java.util.Collections.emptyList();
		}
		this.lossType = Integer.parseInt(element.getAttribute("lossType") == null || element.getAttribute("lossType").length() == 0 ? "0"
			: element.getAttribute("lossType")); // 负属性类型
		String lossTypeRangeString = element.getAttribute("lossTypeRange"); // 负属性范围
		if (lossTypeRangeString != null && lossTypeRangeString.length() > 0) {
			String[] lossTypeRangeStrings = lossTypeRangeString.split("\\|"); 
			List<Integer> lossTypeRange = new ArrayList<Integer>(lossTypeRangeStrings.length) ; 
			for (int i = 0; i < lossTypeRangeStrings.length; i++) {
				Integer temp = Integer.parseInt(lossTypeRangeStrings[i]);
				lossTypeRange.add(temp);
			}
			this.lossTypeRange = com.google.common.collect.ImmutableList.copyOf(lossTypeRange);						
		} else {
			this.lossTypeRange = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getQuality() {
		return quality;
	}
	
	public int getComposeTarget() {
		return composeTarget;
	}
	
	public List<Entry<Integer,Integer>> getComposeCost() {
		return composeCost;
	}
	
	public List<Entry<Integer,Integer>> getResetCost() {
		return resetCost;
	}
	
	public int getMainType() {
		return mainType;
	}
	
	public List<Integer> getMainTypeRange() {
		return mainTypeRange;
	}
	
	public int getLossType() {
		return lossType;
	}
	
	public List<Integer> getLossTypeRange() {
		return lossTypeRange;
	}
	
}
