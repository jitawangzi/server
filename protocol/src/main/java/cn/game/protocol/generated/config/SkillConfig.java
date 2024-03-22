package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 技能
 * 
 * 工具生成的，不要手动修改
 */
 public class SkillConfig {

	/** 状态id 每个技能是由多个状态组成 策划约定： -每个状态的对象可能不同 id7位 = 技能5位+序列号2位 */
	public final int ID;		
	/** 技能目标 10-仅自己 11-友方队友 12-友方前排队友 13-友方后排队友 14-友方生命最少队友  20-离我方最近敌人 21-全体敌人 22-BOSS  23-血量最低敌人 24-攻击最高敌人 */
	public final int TargetType;		
	/** 附加技能id组skill技能 */
	public final int[] EccectGroup;		
	/** 技能触发类型 类型： 1-几率型（几率） 2-次数累加（次数） */
	public final int EffectCondition;		
	/** 技能触发类型参数几率都是按10000来算几率 次数是个数 */
	public final int[] ConditionParam;		
	/** 状态类型 1-光束 2-子弹 3-位移 4-效果(buff/debuff) */
	public final int StatusType;		
	/** 状态ID 根据前1列StatusType值 StatusType=1 读【Beam#范围】第1列 StatusType=2 读【Bullet#子弹】第1列 StatusType=3 读【Displacement#位移】第1列 StatusType=4 读【Effect#效果】第1列 */
	public final int StatusTypeID;		
	/** 动作使用那个动作 */
	public final String ActionName;		
	/** 特效是否则有外挂特效显示 */
	public final String ActionEffect;		

	public SkillConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 状态id 每个技能是由多个状态组成 策划约定： -每个状态的对象可能不同 id7位 = 技能5位+序列号2位
		TargetType = Integer.parseInt(element.getAttribute("TargetType") == null || element.getAttribute("TargetType").length() == 0 ? "0"
			: element.getAttribute("TargetType")); // 技能目标 10-仅自己 11-友方队友 12-友方前排队友 13-友方后排队友 14-友方生命最少队友  20-离我方最近敌人 21-全体敌人 22-BOSS  23-血量最低敌人 24-攻击最高敌人
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
			: element.getAttribute("EffectCondition")); // 技能触发类型 类型： 1-几率型（几率） 2-次数累加（次数）
		String ConditionParamString = element.getAttribute("ConditionParam"); // 技能触发类型参数几率都是按10000来算几率 次数是个数
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
		StatusType = Integer.parseInt(element.getAttribute("StatusType") == null || element.getAttribute("StatusType").length() == 0 ? "0"
			: element.getAttribute("StatusType")); // 状态类型 1-光束 2-子弹 3-位移 4-效果(buff/debuff)
		StatusTypeID = Integer.parseInt(element.getAttribute("StatusTypeID") == null || element.getAttribute("StatusTypeID").length() == 0 ? "0"
			: element.getAttribute("StatusTypeID")); // 状态ID 根据前1列StatusType值 StatusType=1 读【Beam#范围】第1列 StatusType=2 读【Bullet#子弹】第1列 StatusType=3 读【Displacement#位移】第1列 StatusType=4 读【Effect#效果】第1列
		ActionName = element.getAttribute("ActionName"); // 动作使用那个动作
		ActionEffect = element.getAttribute("ActionEffect"); // 特效是否则有外挂特效显示
	}
	

}
