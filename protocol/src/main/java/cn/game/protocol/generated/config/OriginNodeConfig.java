package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 源质节点
 * 
 * 工具生成的，不要手动修改
 */
 public class OriginNodeConfig {

	/** 源质id -- 每个角色对应9个源质id */
	private final int id;		
	/** 源质品质 -- 1-普通源质 2-高级源质 当id为2时，读取附加效果 */
	private final int type;		
	/** 普通属性1 -- id:值|id:值... */
	private final List<Entry<Integer,Integer>> attributeOne;		
	/** 普通属性2 -- id:值|id:值... */
	private final List<Entry<Integer,Integer>> attributeTwo;		
	/** 附加效果 -- id:值|id:值... */
	private final List<Entry<Integer,Integer>> specialEffects;		
	/** 槽位 */
	private final int slot;		
	/** 角色id */
	private final int roleId;		

	public OriginNodeConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // 源质id
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 源质品质
		String attributeOneString = element.getAttribute("attributeOne"); // 普通属性1
		if (attributeOneString != null && attributeOneString.length() > 0) {
			String[] attributeOneStrings = attributeOneString.split("\\|"); 
			List<Entry<Integer,Integer>> attributeOne = new ArrayList<Entry<Integer,Integer>>(attributeOneStrings.length) ; 
			for (int i = 0; i < attributeOneStrings.length; i++) {
			    String[] split = attributeOneStrings[i].split(":", 2);
				attributeOne.add(new Entry<Integer,Integer>()	{
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

			this.attributeOne = com.google.common.collect.ImmutableList.copyOf(attributeOne);						
		} else {
			this.attributeOne = java.util.Collections.emptyList();
		}
		String attributeTwoString = element.getAttribute("attributeTwo"); // 普通属性2
		if (attributeTwoString != null && attributeTwoString.length() > 0) {
			String[] attributeTwoStrings = attributeTwoString.split("\\|"); 
			List<Entry<Integer,Integer>> attributeTwo = new ArrayList<Entry<Integer,Integer>>(attributeTwoStrings.length) ; 
			for (int i = 0; i < attributeTwoStrings.length; i++) {
			    String[] split = attributeTwoStrings[i].split(":", 2);
				attributeTwo.add(new Entry<Integer,Integer>()	{
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

			this.attributeTwo = com.google.common.collect.ImmutableList.copyOf(attributeTwo);						
		} else {
			this.attributeTwo = java.util.Collections.emptyList();
		}
		String specialEffectsString = element.getAttribute("specialEffects"); // 附加效果
		if (specialEffectsString != null && specialEffectsString.length() > 0) {
			String[] specialEffectsStrings = specialEffectsString.split("\\|"); 
			List<Entry<Integer,Integer>> specialEffects = new ArrayList<Entry<Integer,Integer>>(specialEffectsStrings.length) ; 
			for (int i = 0; i < specialEffectsStrings.length; i++) {
			    String[] split = specialEffectsStrings[i].split(":", 2);
				specialEffects.add(new Entry<Integer,Integer>()	{
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

			this.specialEffects = com.google.common.collect.ImmutableList.copyOf(specialEffects);						
		} else {
			this.specialEffects = java.util.Collections.emptyList();
		}
		this.slot = Integer.parseInt(element.getAttribute("slot") == null || element.getAttribute("slot").length() == 0 ? "0"
			: element.getAttribute("slot")); // 槽位
		this.roleId = Integer.parseInt(element.getAttribute("roleId") == null || element.getAttribute("roleId").length() == 0 ? "0"
			: element.getAttribute("roleId")); // 角色id
	}
	
	public int getId() {
		return id;
	}
	
	public int getType() {
		return type;
	}
	
	public List<Entry<Integer,Integer>> getAttributeOne() {
		return attributeOne;
	}
	
	public List<Entry<Integer,Integer>> getAttributeTwo() {
		return attributeTwo;
	}
	
	public List<Entry<Integer,Integer>> getSpecialEffects() {
		return specialEffects;
	}
	
	public int getSlot() {
		return slot;
	}
	
	public int getRoleId() {
		return roleId;
	}
	
}
