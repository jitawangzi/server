package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 源质强化
 * 
 * 工具生成的，不要手动修改
 */
 public class OriginStrengthenConfig {

	/** 源质id -- 品质*1000+强化等级 */
	private final int id;		
	/** 强化消耗道具 -- 道具id:道具数量|道具id:道具数量... */
	private final List<Entry<Integer,Integer>> StrengthenExpendItem;		

	public OriginStrengthenConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // 源质id
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
	}
	
	public int getId() {
		return id;
	}
	
	public List<Entry<Integer,Integer>> getStrengthenExpendItem() {
		return StrengthenExpendItem;
	}
	
}
