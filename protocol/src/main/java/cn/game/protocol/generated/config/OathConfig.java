package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 誓约
 * 
 * 工具生成的，不要手动修改
 */
 public class OathConfig {

	/** id -- 角色id，与role表相同 */
	private final int id;		
	/** 奖励属性 -- 属性id|属性值。。。 */
	private final List<Entry<Integer,Integer>> RewardAttribute;		

	public OathConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		String RewardAttributeString = element.getAttribute("RewardAttribute"); // 奖励属性
		if (RewardAttributeString != null && RewardAttributeString.length() > 0) {
			String[] RewardAttributeStrings = RewardAttributeString.split("\\|"); 
			List<Entry<Integer,Integer>> RewardAttribute = new ArrayList<Entry<Integer,Integer>>(RewardAttributeStrings.length) ; 
			for (int i = 0; i < RewardAttributeStrings.length; i++) {
			    String[] split = RewardAttributeStrings[i].split(":", 2);
				RewardAttribute.add(new Entry<Integer,Integer>()	{
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

			this.RewardAttribute = com.google.common.collect.ImmutableList.copyOf(RewardAttribute);						
		} else {
			this.RewardAttribute = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public List<Entry<Integer,Integer>> getRewardAttribute() {
		return RewardAttribute;
	}
	
}
