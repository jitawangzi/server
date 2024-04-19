package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 瞬发
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroDisplacementConfig {

	/** ID */
	public final int ID;		
	/** 轨迹类型 1卡牌冲过去 2卡牌瞬移+闪现 3卡牌原地 4目标位置(多个) */
	public final int trackType;		
	/** 位移速度  (XXX像素/秒) */
	public final int Speed;		
	/** 击中敌方伤害类型 0直接伤害 1范围个数 2范围半径 3击退 */
	public final int[] HitType;		
	/** 0直接伤害 无 1范围个数 当前目标周围得目标 2范围半径 半径范围内得目标 3击退距离像素 */
	public final int HitTypeParam;		
	/** 攻击次数   多少个炸 */
	public final int ComboCount;		
	/** 造成技能伤害公式用计算方法名字程序来封装方法实现  击退不用写 */
	public final String CalculateFun;		
	/** 技能伤害参数百分比组 */
	public final int[] CalculateParam;		

	public HeroDisplacementConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		trackType = Integer.parseInt(element.getAttribute("trackType") == null || element.getAttribute("trackType").length() == 0 ? "0"
			: element.getAttribute("trackType")); // 轨迹类型 1卡牌冲过去 2卡牌瞬移+闪现 3卡牌原地 4目标位置(多个)
		Speed = Integer.parseInt(element.getAttribute("Speed") == null || element.getAttribute("Speed").length() == 0 ? "0"
			: element.getAttribute("Speed")); // 位移速度  (XXX像素/秒)
		String HitTypeString = element.getAttribute("HitType"); // 击中敌方伤害类型 0直接伤害 1范围个数 2范围半径 3击退
		if (HitTypeString != null && HitTypeString.length() > 0) {
			String[] HitTypeStrings = HitTypeString.split(";"); 
			int[] HitTypeTemp = new int[HitTypeStrings.length] ; 
			for (int i = 0; i < HitTypeStrings.length; i++) {
				int temp = Integer.parseInt(HitTypeStrings[i]);	
				HitTypeTemp[i] = temp;
			}
			HitType = HitTypeTemp ;			
		} else {
			HitType = new int[] {};
		}
		HitTypeParam = Integer.parseInt(element.getAttribute("HitTypeParam") == null || element.getAttribute("HitTypeParam").length() == 0 ? "0"
			: element.getAttribute("HitTypeParam")); // 0直接伤害 无 1范围个数 当前目标周围得目标 2范围半径 半径范围内得目标 3击退距离像素
		ComboCount = Integer.parseInt(element.getAttribute("ComboCount") == null || element.getAttribute("ComboCount").length() == 0 ? "0"
			: element.getAttribute("ComboCount")); // 攻击次数   多少个炸
		CalculateFun = element.getAttribute("CalculateFun"); // 造成技能伤害公式用计算方法名字程序来封装方法实现  击退不用写
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
	}
	

}
