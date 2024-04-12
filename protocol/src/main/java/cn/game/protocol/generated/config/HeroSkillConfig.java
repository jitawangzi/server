package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 技能
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroSkillConfig {

	/** 状态id 每个技能是由多个状态组成 */
	public final int ID;		
	/** 技能目标 - 10-仅自己 - 11-队友中的随机1人      (队友的意思就是不包括自己) - 12-前排队友中的随机1人 - 13-后排队友中的随机1人 - 14-当前生命值百分比最低的队友 - 15-当前生命值数值最低的队友 - 16-全体队友 - 17-已经死亡的队友中的随机1人 - 18-己方随机1人          (己方的意思就是包括自己) - 19-己方前排全体 - 20-己方后排全体 - 21-己方全体 - 22-带有护盾的己方 - 40-距离自己最近的怪物 - 41-全体怪物 - 42-除BOSS外的其余怪物 - 43-主技能造成伤害的怪物 - 44-地面怪物 */
	public final int TargetType;		
	/** 技能属性  1-战士  2-刺客  3-法师  4-牧师  5-射手 */
	public final int[] Attribute;		
	/** 状态类型 0-本身无效过只是为了挂接附加技能组 1-光束 2-子弹 3-位移 4-效果(buff/debuff) 5-Summon 召唤（暂不制作） */
	public final int StatusType;		
	/** 状态ID 根据前1列StatusType值 StatusType=1 读【Beam#范围】第1列 StatusType=2 读【Bullet#子弹】第1列 StatusType=3 读【Displacement#位移】第1列 StatusType=4 读【BUFF#效果】第1列 StatusType=5 读【Summon#召唤】第1列 */
	public final int StatusTypeID;		
	/** 攻击距离(常见是给怪物用的) -1 无限距离 */
	public final int AttackDis;		
	/** 动作使用那个动作 */
	public final String ActionName;		
	/** 特效是否有外挂特效显示  1-是 0-否 */
	public final String ActionEffect;		
	/** 附加技能id组skill技能 */
	public final int[] EccectGroup;		
	/** 附加技能触发类型 类型： 1-几率型（几率） 2-次数累加（次数）   5-- 自身生命值低于几%时触发触发几次 6- 队友死亡时触发 7- - 队友复活时触发 8-- BOSS登场时触发 9- 位置离出生位置大于距离 10-自身CD结束后触发 */
	public final int EffectCondition;		
	/** 技能触发类型参数 1几率都是按10000来算几率  2次数是个数 ----------条件 条件类型的参数定义  5-- 自身生命值低于几%时触发触发几次  3000;1 6- 队友死亡时触发 无 7- - 队友复活时触发 无 8-- BOSS登场时触发 无 9- 位置离出生位置大于距离 距离 触发次数  300;1 10-自身结束CD后触发  无 */
	public final int[] ConditionParam;		
	/** 附加技能触发时间点 1-施法时触发 2-命中时触发 */
	public final int TriggerTiming;		

	public HeroSkillConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 状态id 每个技能是由多个状态组成
		TargetType = Integer.parseInt(element.getAttribute("TargetType") == null || element.getAttribute("TargetType").length() == 0 ? "0"
			: element.getAttribute("TargetType")); // 技能目标 - 10-仅自己 - 11-队友中的随机1人      (队友的意思就是不包括自己) - 12-前排队友中的随机1人 - 13-后排队友中的随机1人 - 14-当前生命值百分比最低的队友 - 15-当前生命值数值最低的队友 - 16-全体队友 - 17-已经死亡的队友中的随机1人 - 18-己方随机1人          (己方的意思就是包括自己) - 19-己方前排全体 - 20-己方后排全体 - 21-己方全体 - 22-带有护盾的己方 - 40-距离自己最近的怪物 - 41-全体怪物 - 42-除BOSS外的其余怪物 - 43-主技能造成伤害的怪物 - 44-地面怪物
		String AttributeString = element.getAttribute("Attribute"); // 技能属性  1-战士  2-刺客  3-法师  4-牧师  5-射手
		if (AttributeString != null && AttributeString.length() > 0) {
			String[] AttributeStrings = AttributeString.split(";"); 
			int[] AttributeTemp = new int[AttributeStrings.length] ; 
			for (int i = 0; i < AttributeStrings.length; i++) {
				int temp = Integer.parseInt(AttributeStrings[i]);	
				AttributeTemp[i] = temp;
			}
			Attribute = AttributeTemp ;			
		} else {
			Attribute = new int[] {};
		}
		StatusType = Integer.parseInt(element.getAttribute("StatusType") == null || element.getAttribute("StatusType").length() == 0 ? "0"
			: element.getAttribute("StatusType")); // 状态类型 0-本身无效过只是为了挂接附加技能组 1-光束 2-子弹 3-位移 4-效果(buff/debuff) 5-Summon 召唤（暂不制作）
		StatusTypeID = Integer.parseInt(element.getAttribute("StatusTypeID") == null || element.getAttribute("StatusTypeID").length() == 0 ? "0"
			: element.getAttribute("StatusTypeID")); // 状态ID 根据前1列StatusType值 StatusType=1 读【Beam#范围】第1列 StatusType=2 读【Bullet#子弹】第1列 StatusType=3 读【Displacement#位移】第1列 StatusType=4 读【BUFF#效果】第1列 StatusType=5 读【Summon#召唤】第1列
		AttackDis = Integer.parseInt(element.getAttribute("AttackDis") == null || element.getAttribute("AttackDis").length() == 0 ? "0"
			: element.getAttribute("AttackDis")); // 攻击距离(常见是给怪物用的) -1 无限距离
		ActionName = element.getAttribute("ActionName"); // 动作使用那个动作
		ActionEffect = element.getAttribute("ActionEffect"); // 特效是否有外挂特效显示  1-是 0-否
		String EccectGroupString = element.getAttribute("EccectGroup"); // 附加技能id组skill技能
		if (EccectGroupString != null && EccectGroupString.length() > 0) {
			String[] EccectGroupStrings = EccectGroupString.split(";"); 
			int[] EccectGroupTemp = new int[EccectGroupStrings.length] ; 
			for (int i = 0; i < EccectGroupStrings.length; i++) {
				int temp = Integer.parseInt(EccectGroupStrings[i]);	
				EccectGroupTemp[i] = temp;
			}
			EccectGroup = EccectGroupTemp ;			
		} else {
			EccectGroup = new int[] {};
		}
		EffectCondition = Integer.parseInt(element.getAttribute("EffectCondition") == null || element.getAttribute("EffectCondition").length() == 0 ? "0"
			: element.getAttribute("EffectCondition")); // 附加技能触发类型 类型： 1-几率型（几率） 2-次数累加（次数）   5-- 自身生命值低于几%时触发触发几次 6- 队友死亡时触发 7- - 队友复活时触发 8-- BOSS登场时触发 9- 位置离出生位置大于距离 10-自身CD结束后触发
		String ConditionParamString = element.getAttribute("ConditionParam"); // 技能触发类型参数 1几率都是按10000来算几率  2次数是个数 ----------条件 条件类型的参数定义  5-- 自身生命值低于几%时触发触发几次  3000;1 6- 队友死亡时触发 无 7- - 队友复活时触发 无 8-- BOSS登场时触发 无 9- 位置离出生位置大于距离 距离 触发次数  300;1 10-自身结束CD后触发  无
		if (ConditionParamString != null && ConditionParamString.length() > 0) {
			String[] ConditionParamStrings = ConditionParamString.split(";"); 
			int[] ConditionParamTemp = new int[ConditionParamStrings.length] ; 
			for (int i = 0; i < ConditionParamStrings.length; i++) {
				int temp = Integer.parseInt(ConditionParamStrings[i]);	
				ConditionParamTemp[i] = temp;
			}
			ConditionParam = ConditionParamTemp ;			
		} else {
			ConditionParam = new int[] {};
		}
		TriggerTiming = Integer.parseInt(element.getAttribute("TriggerTiming") == null || element.getAttribute("TriggerTiming").length() == 0 ? "0"
			: element.getAttribute("TriggerTiming")); // 附加技能触发时间点 1-施法时触发 2-命中时触发
	}
	

}
