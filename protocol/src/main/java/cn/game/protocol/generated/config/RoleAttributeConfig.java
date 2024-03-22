package cn.game.protocol.generated.config;

import cn.game.protocol.generated.enume.AttributeSubTypeEnum;
import cn.game.protocol.generated.enume.AttributeTypeEnum;
import org.w3c.dom.Element;

/**
 * 角色属性
 * 
 * 工具生成的，不要手动修改
 */
 public class RoleAttributeConfig {

	/** id */
	private final int id;		
	/** 属性类型 -- 读AttributeTypeEnum表 */
	private final AttributeTypeEnum type;		
	/** 子类型 -- 1 总值 2 当前值 3 总值当前值 4 降低减免% 5 增长减免% 6 临时值 7 降低免疫概率 8 增长免疫概率 */
	private final AttributeSubTypeEnum subType;		

	public RoleAttributeConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.type = AttributeTypeEnum.get(Integer.parseInt(element.getAttribute("type")));	// 属性类型
		this.subType = AttributeSubTypeEnum.get(Integer.parseInt(element.getAttribute("subType")));	// 子类型
	}
	
	public int getId() {
		return id;
	}
	
	public AttributeTypeEnum getType() {
		return type;
	}
	
	public AttributeSubTypeEnum getSubType() {
		return subType;
	}
	
}
