package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 灵武升级表
 * 
 * 工具生成的，不要手动修改
 */
 public class SoulWeaponExpConfig {

	/** id -- id */
	private final int id;		
	/** 材料消耗 -- 材料消耗 */
	private final List<Entry<Integer,Integer>> itemCost;		
	/** 限制指挥官等级 -- 限制指挥官等级 */
	private final int levelLimit;		

	public SoulWeaponExpConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		String itemCostString = element.getAttribute("itemCost"); // 材料消耗
		if (itemCostString != null && itemCostString.length() > 0) {
			String[] itemCostStrings = itemCostString.split("\\|"); 
			List<Entry<Integer,Integer>> itemCost = new ArrayList<Entry<Integer,Integer>>(itemCostStrings.length) ; 
			for (int i = 0; i < itemCostStrings.length; i++) {
			    String[] split = itemCostStrings[i].split(":", 2);
				itemCost.add(new Entry<Integer,Integer>()	{
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

			this.itemCost = com.google.common.collect.ImmutableList.copyOf(itemCost);						
		} else {
			this.itemCost = java.util.Collections.emptyList();
		}
		this.levelLimit = Integer.parseInt(element.getAttribute("levelLimit") == null || element.getAttribute("levelLimit").length() == 0 ? "0"
			: element.getAttribute("levelLimit")); // 限制指挥官等级
	}
	
	public int getId() {
		return id;
	}
	
	public List<Entry<Integer,Integer>> getItemCost() {
		return itemCost;
	}
	
	public int getLevelLimit() {
		return levelLimit;
	}
	
}
