package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 英雄
 * 
 * 工具生成的，不要手动修改
 */
 public class Hero1Config {

	/** 字典表ID 对参数唯一标识字段。  1-9999为界面显示文字。 10000-19999为显示规则。 20000-29999带数据显示的文字。 30000-39999为消息反馈提示。 40000-49999为系统错误码 字符配置以玩法模块为基础，尽量保证一个模块放在一起。 */
	private final int ID;		
	/** 配置文本字段。 若有不确定的字段用相应字符串代替。 比如：%s：字符串；%d：整型：%f：浮点。 若有不同颜色的需求，需要按照通过16进制颜色编码进行标识。 在相应字段前中括号内填上颜色标识，在字段后中括号内填上“-”。 比如：是否消耗[FFFFFF]%d[-]金钱升级？ [b]可以加粗字体，例如：[b]等级 \n换行符 */
	private final String Value;		

	public Hero1Config (Element element) throws Exception {
	
		this.ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 字典表ID 对参数唯一标识字段。  1-9999为界面显示文字。 10000-19999为显示规则。 20000-29999带数据显示的文字。 30000-39999为消息反馈提示。 40000-49999为系统错误码 字符配置以玩法模块为基础，尽量保证一个模块放在一起。
		this.Value = element.getAttribute("Value"); // 配置文本字段。 若有不确定的字段用相应字符串代替。 比如：%s：字符串；%d：整型：%f：浮点。 若有不同颜色的需求，需要按照通过16进制颜色编码进行标识。 在相应字段前中括号内填上颜色标识，在字段后中括号内填上“-”。 比如：是否消耗[FFFFFF]%d[-]金钱升级？ [b]可以加粗字体，例如：[b]等级 \n换行符
	}
	
	public int getID() {
		return ID;
	}
	
	public String getValue() {
		return Value;
	}
	
}
