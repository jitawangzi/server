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
	public final int times;		
	/** buff；类型 1-buff 2-光环型buff 3-瞬发法术 */
	public final int BuffType;		
	/** buff类别 0-中立类buff 1-增益型buff 2-减益型debuff */
	public final int BuffCategory;		
	/** buff存在规则 0-霸占，同一个ID的buff，旧buff占着位置，新buff加不上 1-覆盖，同一个ID的buff，新buff将旧buff覆盖 >1-叠加，同一个ID的buff可以叠加，且叠加上限就是填的数字 */
	public final int BuffCastType;		
	/** buff目标类型 1目标身上 2位置(目标脚下的地面) */
	public final int BuffTargetType;		
	/** Buff效果类型 1-属性加 2-属性减 3-爆炸AOE 4-精神AOE 5-中毒AOE 6-击退 7-瞬加治疗 8-hot治疗 9-城池治疗 10-灼烧DOT 11-刺骨DOT 12-冰冻 13-麻痹 14-混乱 */
	public final int BuffEffectType;		
	/** Buff效果的参数 [1-属性加]       目标属性类型ID;增大百分比（该值/10000用） [2-属性减]       目标属性类型ID;减少百分比（该值/10000用） [3-爆炸]          属性id=415额外爆炸属性d;属性数值|属性id=416额外爆炸属性%;属性%百分比（该值/10000用） [4-精神]          属性id=417额外爆炸属性d;属性数值|属性id=418额外爆炸属性%;属性%百分比（该值/10000用） [5-中毒]          属性id=419额外爆炸属性d;属性数值|属性id=420额外爆炸属性%;属性%百分比（该值/10000用） [6-击退]          击退位移像素 [7-瞬加治疗]    【暂缓制作】西游再做，目标恢复数值hp百分比和次数 [8-hot治疗]      【暂缓制作】西游再做 [9-城池治疗]    属性id=215城池当前生命;每击杀1个怪物,城池瞬间恢复生命d [10-灼烧]         属性id=806怪物当前生命d;每秒损失n点生命;持续n秒 [11-刺骨]         属性id=806怪物当前生命d;每秒损失n点生命;持续n秒 [12-冰冻]         12;持续n秒 [13-麻痹]         13;持续n秒 [14-混乱]         14;持续n秒 */
	public final int[][] BuffParam;		
	/** 造成技能伤害公式用计算方法名字程序来封装方法实现  暂时无用 */
	public final String CalculateFun;		
	/** 技能伤害参数百分比组 */
	public final int[] CalculateParam;		
	/** buff拥有者是否可释放技能  1-是 0-否 */
	public final int Release;		
	/** 是否可叠层  1-是 0-否 */
	public final String Laminate;		
	/** buff在身上特效 */
	public final String BuffEffect;		
	/** buff在身上图标  例如灼烧和冰冻 */
	public final String BuffIcon;		

	public HeroBUFFConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		BuffName = element.getAttribute("BuffName"); // buff名称
		Duration = Integer.parseInt(element.getAttribute("Duration") == null || element.getAttribute("Duration").length() == 0 ? "0"
			: element.getAttribute("Duration")); // 技能持续时间  0 无需计算持续时间 ＞1000（毫秒）
		times = Integer.parseInt(element.getAttribute("times") == null || element.getAttribute("times").length() == 0 ? "0"
			: element.getAttribute("times")); // 有效次数
		BuffType = Integer.parseInt(element.getAttribute("BuffType") == null || element.getAttribute("BuffType").length() == 0 ? "0"
			: element.getAttribute("BuffType")); // buff；类型 1-buff 2-光环型buff 3-瞬发法术
		BuffCategory = Integer.parseInt(element.getAttribute("BuffCategory") == null || element.getAttribute("BuffCategory").length() == 0 ? "0"
			: element.getAttribute("BuffCategory")); // buff类别 0-中立类buff 1-增益型buff 2-减益型debuff
		BuffCastType = Integer.parseInt(element.getAttribute("BuffCastType") == null || element.getAttribute("BuffCastType").length() == 0 ? "0"
			: element.getAttribute("BuffCastType")); // buff存在规则 0-霸占，同一个ID的buff，旧buff占着位置，新buff加不上 1-覆盖，同一个ID的buff，新buff将旧buff覆盖 >1-叠加，同一个ID的buff可以叠加，且叠加上限就是填的数字
		BuffTargetType = Integer.parseInt(element.getAttribute("BuffTargetType") == null || element.getAttribute("BuffTargetType").length() == 0 ? "0"
			: element.getAttribute("BuffTargetType")); // buff目标类型 1目标身上 2位置(目标脚下的地面)
		BuffEffectType = Integer.parseInt(element.getAttribute("BuffEffectType") == null || element.getAttribute("BuffEffectType").length() == 0 ? "0"
			: element.getAttribute("BuffEffectType")); // Buff效果类型 1-属性加 2-属性减 3-爆炸AOE 4-精神AOE 5-中毒AOE 6-击退 7-瞬加治疗 8-hot治疗 9-城池治疗 10-灼烧DOT 11-刺骨DOT 12-冰冻 13-麻痹 14-混乱
		String BuffParamString = element.getAttribute("BuffParam"); // Buff效果的参数 [1-属性加]       目标属性类型ID;增大百分比（该值/10000用） [2-属性减]       目标属性类型ID;减少百分比（该值/10000用） [3-爆炸]          属性id=415额外爆炸属性d;属性数值|属性id=416额外爆炸属性%;属性%百分比（该值/10000用） [4-精神]          属性id=417额外爆炸属性d;属性数值|属性id=418额外爆炸属性%;属性%百分比（该值/10000用） [5-中毒]          属性id=419额外爆炸属性d;属性数值|属性id=420额外爆炸属性%;属性%百分比（该值/10000用） [6-击退]          击退位移像素 [7-瞬加治疗]    【暂缓制作】西游再做，目标恢复数值hp百分比和次数 [8-hot治疗]      【暂缓制作】西游再做 [9-城池治疗]    属性id=215城池当前生命;每击杀1个怪物,城池瞬间恢复生命d [10-灼烧]         属性id=806怪物当前生命d;每秒损失n点生命;持续n秒 [11-刺骨]         属性id=806怪物当前生命d;每秒损失n点生命;持续n秒 [12-冰冻]         12;持续n秒 [13-麻痹]         13;持续n秒 [14-混乱]         14;持续n秒
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
		CalculateFun = element.getAttribute("CalculateFun"); // 造成技能伤害公式用计算方法名字程序来封装方法实现  暂时无用
		String CalculateParamString = element.getAttribute("CalculateParam"); // 技能伤害参数百分比组
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
		BuffEffect = element.getAttribute("BuffEffect"); // buff在身上特效
		BuffIcon = element.getAttribute("BuffIcon"); // buff在身上图标  例如灼烧和冰冻
	}
	

}
