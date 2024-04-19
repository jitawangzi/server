package cn.game.protocol.generated.config;

import cn.game.util.Weightable;
import org.w3c.dom.Element;


/**
 * 随机属性表
 * 
 * 工具生成的，不要手动修改
 */
 public class RandomAttributeConfig implements Weightable {

	/** 序列号 */
	public final int ID;		
	/** 属性id组  是一个属性组的配成一样数值 */
	public final int RandomAttributeId;		
	/** 属性id  调用属性表 装备和宝石共用 */
	public final int AttributeId;		
	/** 权重 */
	public final int Weight;		
	/** 数值min  需要注意，百分比属性需要/10000来用 */
	public final int ValueMin;		
	/** 数值max  需要注意，百分比属性需要/10000来用 */
	public final int ValueMax;		

	public RandomAttributeConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 序列号
		RandomAttributeId = Integer.parseInt(element.getAttribute("RandomAttributeId") == null || element.getAttribute("RandomAttributeId").length() == 0 ? "0"
			: element.getAttribute("RandomAttributeId")); // 属性id组  是一个属性组的配成一样数值
		AttributeId = Integer.parseInt(element.getAttribute("AttributeId") == null || element.getAttribute("AttributeId").length() == 0 ? "0"
			: element.getAttribute("AttributeId")); // 属性id  调用属性表 装备和宝石共用
		Weight = Integer.parseInt(element.getAttribute("Weight") == null || element.getAttribute("Weight").length() == 0 ? "0"
			: element.getAttribute("Weight")); // 权重
		ValueMin = Integer.parseInt(element.getAttribute("ValueMin") == null || element.getAttribute("ValueMin").length() == 0 ? "0"
			: element.getAttribute("ValueMin")); // 数值min  需要注意，百分比属性需要/10000来用
		ValueMax = Integer.parseInt(element.getAttribute("ValueMax") == null || element.getAttribute("ValueMax").length() == 0 ? "0"
			: element.getAttribute("ValueMax")); // 数值max  需要注意，百分比属性需要/10000来用
	}
	

	@Override
	public int weight() {
		return this.Weight;
	}
}
