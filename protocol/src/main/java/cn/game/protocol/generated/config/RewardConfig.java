package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 奖励表
 * 
 * 工具生成的，不要手动修改
 */
 public class RewardConfig {

	/** id */
	private final int id;		
	/** 奖励1 */
	private final List<Entry<Integer,Integer>> info;		

	public RewardConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		String infoString = element.getAttribute("info"); // 奖励1
		if (infoString != null && infoString.length() > 0) {
			String[] infoStrings = infoString.split("\\|"); 
			List<Entry<Integer,Integer>> info = new ArrayList<Entry<Integer,Integer>>(infoStrings.length) ; 
			for (int i = 0; i < infoStrings.length; i++) {
			    String[] split = infoStrings[i].split(":", 2);
				info.add(new Entry<Integer,Integer>()	{
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

			this.info = com.google.common.collect.ImmutableList.copyOf(info);						
		} else {
			this.info = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public List<Entry<Integer,Integer>> getInfo() {
		return info;
	}
	
}
