package cn.game.protocol.generated.config;

import cn.game.protocol.generated.enume.RoleTagEnum;
import java.util.ArrayList;
import java.util.List;
import cn.game.util.Weightable;
import org.w3c.dom.Element;

/**
 * 标签库
 * 
 * 工具生成的，不要手动修改
 */
 public class RoleTagConfig implements Weightable {

	/** id -- #san标签 */
	private final int id;		
	/** 标签名称 */
	private final String name;		
	/** 标签描述 -- SAN一档/二档/三档 挡位越高，SAN值越高 */
	private final String desc;		
	/** 类型 -- 1-SAN标签 2-基础标签 3-历程标签 4-可继承标签 20-代表毁灭者特性 */
	private final int type;		
	/** 类型2 -- 1-SAN标签 2-基础常驻标签 3-类别1 4-类别2 5-类别3 6-环境类 7-可继承类 */
	private final int type2;		
	/** 类型3 -- 读取RoleTagEnum表 */
	private final RoleTagEnum type3;		
	/** 达成条件 -- 读取条件ID 默认是并列关系 */
	private final List<Integer> condition;		
	/** buff -- 读取Buff表 */
	private final List<Integer> buff;		
	/** 权重值 -- 不随机的标签填写0 */
	private final int weight;		
	/** 图标 */
	private final String icon;		
	/** 职业 -- 1-守护 2-先锋 3-异能 4-突袭 5-祈愿 */
	private final int occupation;		

	public RoleTagConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.name = element.getAttribute("name"); // 标签名称
		this.desc = element.getAttribute("desc"); // 标签描述
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 类型
		this.type2 = Integer.parseInt(element.getAttribute("type2") == null || element.getAttribute("type2").length() == 0 ? "0"
			: element.getAttribute("type2")); // 类型2
		this.type3 = RoleTagEnum.get(Integer.parseInt(element.getAttribute("type3")));	// 类型3
		String conditionString = element.getAttribute("condition"); // 达成条件
		if (conditionString != null && conditionString.length() > 0) {
			String[] conditionStrings = conditionString.split("\\|"); 
			List<Integer> condition = new ArrayList<Integer>(conditionStrings.length) ; 
			for (int i = 0; i < conditionStrings.length; i++) {
				Integer temp = Integer.parseInt(conditionStrings[i]);
				condition.add(temp);
			}
			this.condition = com.google.common.collect.ImmutableList.copyOf(condition);						
		} else {
			this.condition = java.util.Collections.emptyList();
		}
		String buffString = element.getAttribute("buff"); // buff
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
		this.weight = Integer.parseInt(element.getAttribute("weight") == null || element.getAttribute("weight").length() == 0 ? "0"
			: element.getAttribute("weight")); // 权重值
		this.icon = element.getAttribute("icon"); // 图标
		this.occupation = Integer.parseInt(element.getAttribute("occupation") == null || element.getAttribute("occupation").length() == 0 ? "0"
			: element.getAttribute("occupation")); // 职业
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDesc() {
		return desc;
	}
	
	public int getType() {
		return type;
	}
	
	public int getType2() {
		return type2;
	}
	
	public RoleTagEnum getType3() {
		return type3;
	}
	
	public List<Integer> getCondition() {
		return condition;
	}
	
	public List<Integer> getBuff() {
		return buff;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public int getOccupation() {
		return occupation;
	}
	
	@Override
	public int weight() {
		return this.weight;
	}
}
