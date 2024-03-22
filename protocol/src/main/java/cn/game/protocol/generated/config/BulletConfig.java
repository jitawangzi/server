package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 子弹
 * 
 * 工具生成的，不要手动修改
 */
 public class BulletConfig {

	/** ID */
	public final int ID;		
	/** 技能持续时间  0 无需计算持续时间 ＞1000（毫秒） */
	public final int Duration;		
	/** 轨迹类型 1直线 2屏幕反弹 */
	public final int trackType;		
	/** 轨迹类型参数 1直线 无 2屏幕反弹 反弹次数 */
	public final int trackParame;		
	/** 子弹发射数量 */
	public final int bulletTimes;		
	/** 子弹 飞行速度  毫秒 */
	public final int bulletSpeed;		
	/** 子弹 击中敌方  可穿透敌人次数 */
	public final int APenetrate;		
	/** 子弹击中敌方伤害类型 0直接伤害 1爆炸 2分裂 3连击 */
	public final int[] HitType;		
	/** 伤害类型参数 0直接伤害 无 1爆炸 圆形半径(10000 全屏) 2分裂 角度 个数 分裂子弹ID 3连击 连击敌人数量 */
	public final int[][] HitTypeParam;		
	/** 造成技能伤害公式 */
	public final String CalculateFun;		
	/** 技能伤害参数百分比组 */
	public final int[] CalculateParam;		
	/** 子弹 是否最终回到手里  1-是 0-否 */
	public final int AReturn;		
	/** 施法前摇时间  毫秒 */
	public final int CastingTime;		
	/** 子弹特效延迟飞出时间  毫秒 */
	public final int BulletDelayTime;		
	/** 子弹特效文件prefab */
	public final String BulletEffect;		
	/** 子弹爆炸特效文件prefab */
	public final String HitEffect;		

	public BulletConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		Duration = Integer.parseInt(element.getAttribute("Duration") == null || element.getAttribute("Duration").length() == 0 ? "0"
			: element.getAttribute("Duration")); // 技能持续时间  0 无需计算持续时间 ＞1000（毫秒）
		trackType = Integer.parseInt(element.getAttribute("trackType") == null || element.getAttribute("trackType").length() == 0 ? "0"
			: element.getAttribute("trackType")); // 轨迹类型 1直线 2屏幕反弹
		trackParame = Integer.parseInt(element.getAttribute("trackParame") == null || element.getAttribute("trackParame").length() == 0 ? "0"
			: element.getAttribute("trackParame")); // 轨迹类型参数 1直线 无 2屏幕反弹 反弹次数
		bulletTimes = Integer.parseInt(element.getAttribute("bulletTimes") == null || element.getAttribute("bulletTimes").length() == 0 ? "0"
			: element.getAttribute("bulletTimes")); // 子弹发射数量
		bulletSpeed = Integer.parseInt(element.getAttribute("bulletSpeed") == null || element.getAttribute("bulletSpeed").length() == 0 ? "0"
			: element.getAttribute("bulletSpeed")); // 子弹 飞行速度  毫秒
		APenetrate = Integer.parseInt(element.getAttribute("APenetrate") == null || element.getAttribute("APenetrate").length() == 0 ? "0"
			: element.getAttribute("APenetrate")); // 子弹 击中敌方  可穿透敌人次数
		String HitTypeString = element.getAttribute("HitType"); // 子弹击中敌方伤害类型 0直接伤害 1爆炸 2分裂 3连击
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
		String HitTypeParamString = element.getAttribute("HitTypeParam"); // 伤害类型参数 0直接伤害 无 1爆炸 圆形半径(10000 全屏) 2分裂 角度 个数 分裂子弹ID 3连击 连击敌人数量
		if (HitTypeParamString != null && HitTypeParamString.length() > 0) {
			String[] HitTypeParamStrings = HitTypeParamString.split("\\|"); 
			int[][] HitTypeParamTemp = new int[HitTypeParamStrings.length][] ; 
			for (int i = 0; i < HitTypeParamStrings.length; i++) {
				String[] HitTypeParamStrings2 = HitTypeParamStrings[i].split(";"); 
				int[] array = new int[HitTypeParamStrings2.length];
				for (int j = 0; j < HitTypeParamStrings2.length; j++) {
					int temp = Integer.parseInt(HitTypeParamStrings2[j]);	
					array[j] = temp;
				}
				HitTypeParamTemp[i] = array;
				
			}
			HitTypeParam = HitTypeParamTemp ;			
		} else {
			HitTypeParam = new int[][] {};
		}
		CalculateFun = element.getAttribute("CalculateFun"); // 造成技能伤害公式
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
		AReturn = Integer.parseInt(element.getAttribute("AReturn") == null || element.getAttribute("AReturn").length() == 0 ? "0"
			: element.getAttribute("AReturn")); // 子弹 是否最终回到手里  1-是 0-否
		CastingTime = Integer.parseInt(element.getAttribute("CastingTime") == null || element.getAttribute("CastingTime").length() == 0 ? "0"
			: element.getAttribute("CastingTime")); // 施法前摇时间  毫秒
		BulletDelayTime = Integer.parseInt(element.getAttribute("BulletDelayTime") == null || element.getAttribute("BulletDelayTime").length() == 0 ? "0"
			: element.getAttribute("BulletDelayTime")); // 子弹特效延迟飞出时间  毫秒
		BulletEffect = element.getAttribute("BulletEffect"); // 子弹特效文件prefab
		HitEffect = element.getAttribute("HitEffect"); // 子弹爆炸特效文件prefab
	}
	

}
