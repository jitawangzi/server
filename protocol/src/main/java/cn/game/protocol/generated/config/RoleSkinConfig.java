package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 皮肤表
 * 
 * 工具生成的，不要手动修改
 */
 public class RoleSkinConfig {

	/** id -- id */
	private final int id;		
	/** roleId -- roleId */
	private final int roleId;		
	/** 皮肤类型 -- 1-普通 2-限定 3-特殊 */
	private final int skinType;		
	/** 皮肤消耗 -- 物品id：数量 */
	private final List<Entry<Integer,Integer>> skinCostNumber;		

	public RoleSkinConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.roleId = Integer.parseInt(element.getAttribute("roleId") == null || element.getAttribute("roleId").length() == 0 ? "0"
			: element.getAttribute("roleId")); // roleId
		this.skinType = Integer.parseInt(element.getAttribute("skinType") == null || element.getAttribute("skinType").length() == 0 ? "0"
			: element.getAttribute("skinType")); // 皮肤类型
		String skinCostNumberString = element.getAttribute("skinCostNumber"); // 皮肤消耗
		if (skinCostNumberString != null && skinCostNumberString.length() > 0) {
			String[] skinCostNumberStrings = skinCostNumberString.split("\\|"); 
			List<Entry<Integer,Integer>> skinCostNumber = new ArrayList<Entry<Integer,Integer>>(skinCostNumberStrings.length) ; 
			for (int i = 0; i < skinCostNumberStrings.length; i++) {
			    String[] split = skinCostNumberStrings[i].split(":", 2);
				skinCostNumber.add(new Entry<Integer,Integer>()	{
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

			this.skinCostNumber = com.google.common.collect.ImmutableList.copyOf(skinCostNumber);						
		} else {
			this.skinCostNumber = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getRoleId() {
		return roleId;
	}
	
	public int getSkinType() {
		return skinType;
	}
	
	public List<Entry<Integer,Integer>> getSkinCostNumber() {
		return skinCostNumber;
	}
	
}
