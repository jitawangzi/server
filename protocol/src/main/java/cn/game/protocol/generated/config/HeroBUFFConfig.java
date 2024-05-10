package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * buff表
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroBUFFConfig {

	/** ID */
	public final int ID;		
	/** buff名称 */
	public final String BuffName;		
	/** 技能持续时间  0 无需计算持续时间 ＞1000（毫秒） */
	public final int Duration;		
	/** 有效次数 */
	public final int Times;		
	/** buff；类型 1-buff 2-光环型buff 3-瞬发法术 */
	public final int BuffType;		
	/** buff类别 0-中立类buff 1-增益型buff 2-减益型debuff */
	public final int BuffCategory;		
	/** buff存在规则 0-霸占，同一个ID的buff，旧buff占着位置，新buff加不上 1-覆盖，同一个ID的buff，新buff将旧buff覆盖 >1-叠加，同一个ID的buff可以叠加，且叠加上限就是填的数字 */
	public final int BuffCastType;		
	/** buff目标类型 1目标身上 2位置(目标脚下的地面) */
	public final int[] BuffTargetType;		
	/** Buff效果类型 1-属性变化的buff（加属性） 2-状态buff  3-伤害buff 4-治疗 */
	public final int BuffEffectType;		
	/** Buff效果的参数 1-给目标加属性id;属性数值|给目标加属性%id;属性数值  2-类型（1冰冻2眩晕） 3-每秒损失N点血（流血麻痹） 4-Calculate_buff_hp=每次治疗量=受治疗目标生命*该buff影响治疗的%/有效次数time */
	public final int[][] BuffParam;		
	/** 造成技能伤害公式用计算方法名字程序来封装方法实现 伤害buff=Calculate_buff_hurt 治疗buff=Calculate_buff_hp */
	public final String CalculateFun;		
	/** 技能伤害参数百分比组  4治疗=总回复hp%/技能持续时间Duration */
	public final int[] CalculateParam;		
	/** buff拥有者是否可释放技能  1-是 0-否 */
	public final int Release;		
	/** 是否可叠层  1-是 0-否 */
	public final String Laminate;		

	public HeroBUFFConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		BuffName = element.getAttribute("BuffName"); // buff名称
		Duration = Integer.parseInt(element.getAttribute("Duration") == null || element.getAttribute("Duration").length() == 0 ? "0"
			: element.getAttribute("Duration")); // 技能持续时间  0 无需计算持续时间 ＞1000（毫秒）
		Times = Integer.parseInt(element.getAttribute("Times") == null || element.getAttribute("Times").length() == 0 ? "0"
			: element.getAttribute("Times")); // 有效次数
		BuffType = Integer.parseInt(element.getAttribute("BuffType") == null || element.getAttribute("BuffType").length() == 0 ? "0"
			: element.getAttribute("BuffType")); // buff；类型 1-buff 2-光环型buff 3-瞬发法术
		BuffCategory = Integer.parseInt(element.getAttribute("BuffCategory") == null || element.getAttribute("BuffCategory").length() == 0 ? "0"
			: element.getAttribute("BuffCategory")); // buff类别 0-中立类buff 1-增益型buff 2-减益型debuff
		BuffCastType = Integer.parseInt(element.getAttribute("BuffCastType") == null || element.getAttribute("BuffCastType").length() == 0 ? "0"
			: element.getAttribute("BuffCastType")); // buff存在规则 0-霸占，同一个ID的buff，旧buff占着位置，新buff加不上 1-覆盖，同一个ID的buff，新buff将旧buff覆盖 >1-叠加，同一个ID的buff可以叠加，且叠加上限就是填的数字
		String BuffTargetTypeString = element.getAttribute("BuffTargetType"); // buff目标类型 1目标身上 2位置(目标脚下的地面)
		if (BuffTargetTypeString != null && BuffTargetTypeString.length() > 0) {
			String[] BuffTargetTypeStrings = BuffTargetTypeString.split(";"); 
			int[] BuffTargetTypeTemp = new int[BuffTargetTypeStrings.length] ; 
			for (int i = 0; i < BuffTargetTypeStrings.length; i++) {
				int temp = Integer.parseInt(BuffTargetTypeStrings[i]);	
				BuffTargetTypeTemp[i] = temp;
			}
			BuffTargetType = BuffTargetTypeTemp ;			
		} else {
			BuffTargetType = new int[] {};
		}
		BuffEffectType = Integer.parseInt(element.getAttribute("BuffEffectType") == null || element.getAttribute("BuffEffectType").length() == 0 ? "0"
			: element.getAttribute("BuffEffectType")); // Buff效果类型 1-属性变化的buff（加属性） 2-状态buff  3-伤害buff 4-治疗
		String BuffParamString = element.getAttribute("BuffParam"); // Buff效果的参数 1-给目标加属性id;属性数值|给目标加属性%id;属性数值  2-类型（1冰冻2眩晕） 3-每秒损失N点血（流血麻痹） 4-Calculate_buff_hp=每次治疗量=受治疗目标生命*该buff影响治疗的%/有效次数time
		if (BuffParamString != null && BuffParamString.length() > 0) {
			String[] BuffParamStrings = BuffParamString.split("\\|"); 
			int[][] BuffParamTemp = new int[BuffParamStrings.length][] ; 
			for (int i = 0; i < BuffParamStrings.length; i++) {
				String[] BuffParamStrings2 = BuffParamStrings[i].split(";"); 
				int[] array = new int[BuffParamStrings2.length];
				for (int j = 0; j < BuffParamStrings2.length; j++) {
					int temp = Integer.parseInt(BuffParamStrings2[j]);	
					array[j] = temp;
				}
				BuffParamTemp[i] = array;
			}
			BuffParam = BuffParamTemp ;			
		} else {
			BuffParam = new int[][] {};
		}
		CalculateFun = element.getAttribute("CalculateFun"); // 造成技能伤害公式用计算方法名字程序来封装方法实现 伤害buff=Calculate_buff_hurt 治疗buff=Calculate_buff_hp
		String CalculateParamString = element.getAttribute("CalculateParam"); // 技能伤害参数百分比组  4治疗=总回复hp%/技能持续时间Duration
		if (CalculateParamString != null && CalculateParamString.length() > 0) {
			String[] CalculateParamStrings = CalculateParamString.split(";"); 
			int[] CalculateParamTemp = new int[CalculateParamStrings.length] ; 
			for (int i = 0; i < CalculateParamStrings.length; i++) {
				int temp = Integer.parseInt(CalculateParamStrings[i]);	
				CalculateParamTemp[i] = temp;
			}
			CalculateParam = CalculateParamTemp ;			
		} else {
			CalculateParam = new int[] {};
		}
		Release = Integer.parseInt(element.getAttribute("Release") == null || element.getAttribute("Release").length() == 0 ? "0"
			: element.getAttribute("Release")); // buff拥有者是否可释放技能  1-是 0-否
		Laminate = element.getAttribute("Laminate"); // 是否可叠层  1-是 0-否
	}
	

}
