package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * buff表
 * 
 * 工具生成的，不要手动修改
 */
 public class BUFFConfig {

	/** ID */
	public final int ID;		
	/** buff名称 */
	public final String BuffName;		
	/** buff说明  目前所有buff无需优先级显示 无需判定同时存在时buff顶替或替换规则 同类型Buff直接替换？ */
	public final String BuffTips;		
	/** buff类型 1增加技能伤害 2吸血 3状态 4治疗 5防护盾 6属性变化 */
	public final int BuffType;		
	/** 1伤害（增加活降低） 伤害百分比 2吸血 目标hp得百分比给自己 3状态  目标状态 1冰冻2石化 4治疗  目标恢复数值hp百分比和次数 5防护罩 目标防护罩数值抵消伤害 6属性变化 目标属性类型（需要定义属性） 变化百分比 */
	public final int[] BuffParam;		
	/** 技能持续时间  0 无需计算持续时间 ＞1000（毫秒） */
	public final int Duration;		
	/** 有效次数 */
	public final int times;		
	/** 是否可释放技能  1-是 0-否 */
	public final int Release;		
	/** 是否可叠层  1-是 0-否 */
	public final String Laminate;		
	/** buff在身上特效  例如治疗、吸血、护盾 */
	public final String BuffEffect;		
	/** buff在身上图标  例如流血和冰冻 */
	public final String BuffIcon;		

	public BUFFConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		BuffName = element.getAttribute("BuffName"); // buff名称
		BuffTips = element.getAttribute("BuffTips"); // buff说明  目前所有buff无需优先级显示 无需判定同时存在时buff顶替或替换规则 同类型Buff直接替换？
		BuffType = Integer.parseInt(element.getAttribute("BuffType") == null || element.getAttribute("BuffType").length() == 0 ? "0"
			: element.getAttribute("BuffType")); // buff类型 1增加技能伤害 2吸血 3状态 4治疗 5防护盾 6属性变化
		String BuffParamString = element.getAttribute("BuffParam"); // 1伤害（增加活降低） 伤害百分比 2吸血 目标hp得百分比给自己 3状态  目标状态 1冰冻2石化 4治疗  目标恢复数值hp百分比和次数 5防护罩 目标防护罩数值抵消伤害 6属性变化 目标属性类型（需要定义属性） 变化百分比
		if (BuffParamString != null && BuffParamString.length() > 0) {
			String[] BuffParamStrings = BuffParamString.split(";"); 
			int[] BuffParamTemp = new int[BuffParamStrings.length] ; 
			for (int i = 0; i < BuffParamStrings.length; i++) {
				int temp = Integer.parseInt(BuffParamStrings[i]);	
				BuffParamTemp[i] = temp;
			}
			BuffParam = BuffParamTemp ;			
		} else {
			BuffParam = new int[] {};
		}
		Duration = Integer.parseInt(element.getAttribute("Duration") == null || element.getAttribute("Duration").length() == 0 ? "0"
			: element.getAttribute("Duration")); // 技能持续时间  0 无需计算持续时间 ＞1000（毫秒）
		times = Integer.parseInt(element.getAttribute("times") == null || element.getAttribute("times").length() == 0 ? "0"
			: element.getAttribute("times")); // 有效次数
		Release = Integer.parseInt(element.getAttribute("Release") == null || element.getAttribute("Release").length() == 0 ? "0"
			: element.getAttribute("Release")); // 是否可释放技能  1-是 0-否
		Laminate = element.getAttribute("Laminate"); // 是否可叠层  1-是 0-否
		BuffEffect = element.getAttribute("BuffEffect"); // buff在身上特效  例如治疗、吸血、护盾
		BuffIcon = element.getAttribute("BuffIcon"); // buff在身上图标  例如流血和冰冻
	}
	

}
