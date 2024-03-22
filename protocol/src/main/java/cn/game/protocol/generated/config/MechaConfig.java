package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 机甲属性
 * 
 * 工具生成的，不要手动修改
 */
 public class MechaConfig {

	/** id -- id */
	private final int id;		
	/** 阵营名称 */
	private final String CampName;		
	/** 奖励属性 */
	private final List<Entry<Integer,Integer>> RewardAttribute;		
	/** 消耗机甲单元 */
	private final List<Integer> ExpendMechaUnit;		
	/** 外观效果 */
	private final String effect;		

	public MechaConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.CampName = element.getAttribute("CampName"); // 阵营名称
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
		String ExpendMechaUnitString = element.getAttribute("ExpendMechaUnit"); // 消耗机甲单元
		if (ExpendMechaUnitString != null && ExpendMechaUnitString.length() > 0) {
			String[] ExpendMechaUnitStrings = ExpendMechaUnitString.split("\\|"); 
			List<Integer> ExpendMechaUnit = new ArrayList<Integer>(ExpendMechaUnitStrings.length) ; 
			for (int i = 0; i < ExpendMechaUnitStrings.length; i++) {
				Integer temp = Integer.parseInt(ExpendMechaUnitStrings[i]);
				ExpendMechaUnit.add(temp);
			}
			this.ExpendMechaUnit = com.google.common.collect.ImmutableList.copyOf(ExpendMechaUnit);						
		} else {
			this.ExpendMechaUnit = java.util.Collections.emptyList();
		}
		this.effect = element.getAttribute("effect"); // 外观效果
	}
	
	public int getId() {
		return id;
	}
	
	public String getCampName() {
		return CampName;
	}
	
	public List<Entry<Integer,Integer>> getRewardAttribute() {
		return RewardAttribute;
	}
	
	public List<Integer> getExpendMechaUnit() {
		return ExpendMechaUnit;
	}
	
	public String getEffect() {
		return effect;
	}
	
}
