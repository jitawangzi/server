package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 装备表
 * 
 * 工具生成的，不要手动修改
 */
 public class EquipmentConfig {

	/** id -- id */
	private final int id;		
	/** 名称 */
	private final String name;		
	/** 图标 */
	private final String icon;		
	/** 等级 -- 代表饰品的强度 */
	private final int level;		
	/** 类型 -- 1-固有装备 2-饰品 */
	private final int type;		
	/** 稀有度 -- 代表饰品的稀有度 1-白 2-绿 3-蓝 4-紫 5-橙 固有装备初始为1 */
	private final int quality;		
	/** 饰品初始属性 -- 读取AttributeTypeEnum表 格式=类型:初始数值 */
	private final List<Entry<Integer,Integer>> attribute;		
	/** 模板类型 -- 固有装备模板类型 1-一类武器 2-一类护具 3-二类武器 4-二类护具 5-三类武器 6-三类护具 7-四类武器 8-四类护具 */
	private final int attributeTemplateType;		
	/** 效果 -- 指定buff中的装备初始效果 */
	private final List<Integer> buff;		
	/** 固定词缀产生的效果 -- 读取buff 固定装备每4，7，10级时升级一次buff效果 格式-效1:词缀2|词缀3:词缀4... （即固定词缀产生的效果） */
	private final List<List<Integer>> buffs;		
	/** 词缀组 -- 1-子库1 2-子库2 3-子库3 。。。 */
	private final int group;		
	/** 角色id -- 固定装备 :所属的角色 饰品 :只有该角色才可以使用 */
	private final List<Integer> roleId;		
	/** 职业 -- 1-守护 2-先锋 3-异能 4-突袭 5-祈愿 饰品 :只有该职业才可以使用 */
	private final List<Integer> occupation;		
	/** 阵营 -- 1-马戏团 2-深水重工 3-昨日联盟 4-群星乐园 5-风铃群落 6-七神海社 饰品 :只有该阵营才可以使用 */
	private final List<Integer> camp;		

	public EquipmentConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.name = element.getAttribute("name"); // 名称
		this.icon = element.getAttribute("icon"); // 图标
		this.level = Integer.parseInt(element.getAttribute("level") == null || element.getAttribute("level").length() == 0 ? "0"
			: element.getAttribute("level")); // 等级
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 类型
		this.quality = Integer.parseInt(element.getAttribute("quality") == null || element.getAttribute("quality").length() == 0 ? "0"
			: element.getAttribute("quality")); // 稀有度
		String attributeString = element.getAttribute("attribute"); // 饰品初始属性
		if (attributeString != null && attributeString.length() > 0) {
			String[] attributeStrings = attributeString.split("\\|"); 
			List<Entry<Integer,Integer>> attribute = new ArrayList<Entry<Integer,Integer>>(attributeStrings.length) ; 
			for (int i = 0; i < attributeStrings.length; i++) {
			    String[] split = attributeStrings[i].split(":", 2);
				attribute.add(new Entry<Integer,Integer>()	{
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

			this.attribute = com.google.common.collect.ImmutableList.copyOf(attribute);						
		} else {
			this.attribute = java.util.Collections.emptyList();
		}
		this.attributeTemplateType = Integer.parseInt(element.getAttribute("attributeTemplateType") == null || element.getAttribute("attributeTemplateType").length() == 0 ? "0"
			: element.getAttribute("attributeTemplateType")); // 模板类型
		String buffString = element.getAttribute("buff"); // 效果
		if (buffString != null && buffString.length() > 0) {
			String[] buffStrings = buffString.split("\\|"); 
			List<Integer> buff = new ArrayList<Integer>(buffStrings.length) ; 
			for (int i = 0; i < buffStrings.length; i++) {
				Integer temp = Integer.parseInt(buffStrings[i]);
				buff.add(temp);
			}
			this.buff = com.google.common.collect.ImmutableList.copyOf(buff);						
		} else {
			this.buff = java.util.Collections.emptyList();
		}
		String buffsString = element.getAttribute("buffs"); // 固定词缀产生的效果
		if (buffsString != null && buffsString.length() > 0) {
			String[] buffsStrings = buffsString.split("\\|"); 
			List<List<Integer>> buffs = new ArrayList<List<Integer>>(buffsStrings.length) ; 
			for (int i = 0; i < buffsStrings.length; i++) {
				String[] buffsStrings2 = buffsStrings[i].split(":"); 
				List<Integer> list = new ArrayList<Integer>(buffsStrings2.length) ; 
				for (int j = 0; j < buffsStrings2.length; j++) {
					Integer temp = Integer.parseInt(buffsStrings2[j]);
					list.add(temp) ; 
				}
				buffs.add(list);
			}
			this.buffs = com.google.common.collect.ImmutableList.copyOf(buffs);						
		} else {
			this.buffs = java.util.Collections.emptyList();
		}
		this.group = Integer.parseInt(element.getAttribute("group") == null || element.getAttribute("group").length() == 0 ? "0"
			: element.getAttribute("group")); // 词缀组
		String roleIdString = element.getAttribute("roleId"); // 角色id
		if (roleIdString != null && roleIdString.length() > 0) {
			String[] roleIdStrings = roleIdString.split("\\|"); 
			List<Integer> roleId = new ArrayList<Integer>(roleIdStrings.length) ; 
			for (int i = 0; i < roleIdStrings.length; i++) {
				Integer temp = Integer.parseInt(roleIdStrings[i]);
				roleId.add(temp);
			}
			this.roleId = com.google.common.collect.ImmutableList.copyOf(roleId);						
		} else {
			this.roleId = java.util.Collections.emptyList();
		}
		String occupationString = element.getAttribute("occupation"); // 职业
		if (occupationString != null && occupationString.length() > 0) {
			String[] occupationStrings = occupationString.split("\\|"); 
			List<Integer> occupation = new ArrayList<Integer>(occupationStrings.length) ; 
			for (int i = 0; i < occupationStrings.length; i++) {
				Integer temp = Integer.parseInt(occupationStrings[i]);
				occupation.add(temp);
			}
			this.occupation = com.google.common.collect.ImmutableList.copyOf(occupation);						
		} else {
			this.occupation = java.util.Collections.emptyList();
		}
		String campString = element.getAttribute("camp"); // 阵营
		if (campString != null && campString.length() > 0) {
			String[] campStrings = campString.split("\\|"); 
			List<Integer> camp = new ArrayList<Integer>(campStrings.length) ; 
			for (int i = 0; i < campStrings.length; i++) {
				Integer temp = Integer.parseInt(campStrings[i]);
				camp.add(temp);
			}
			this.camp = com.google.common.collect.ImmutableList.copyOf(camp);						
		} else {
			this.camp = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public int getLevel() {
		return level;
	}
	
	public int getType() {
		return type;
	}
	
	public int getQuality() {
		return quality;
	}
	
	public List<Entry<Integer,Integer>> getAttribute() {
		return attribute;
	}
	
	public int getAttributeTemplateType() {
		return attributeTemplateType;
	}
	
	public List<Integer> getBuff() {
		return buff;
	}
	
	public List<List<Integer>> getBuffs() {
		return buffs;
	}
	
	public int getGroup() {
		return group;
	}
	
	public List<Integer> getRoleId() {
		return roleId;
	}
	
	public List<Integer> getOccupation() {
		return occupation;
	}
	
	public List<Integer> getCamp() {
		return camp;
	}
	
}
