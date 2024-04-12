package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 子弹
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroBulletConfig {

	/** ID */
	public final int ID;		
	/** 技能持续时间  0 无需计算持续时间 ＞1000（毫秒） */
	public final int Duration;		
	/** 轨迹类型 1直线 2屏幕反弹 3回旋 4散射 5散射回旋 */
	public final int TrackType;		
	/** 轨迹类型参数 1直线 填0 2屏幕反弹 填反弹次数 3回旋极限距离 4散射最大扇形夹角 5散射最大扇形夹角;回旋极限距离 */
	public final int[] TrackParame;		
	/** 子弹 击中敌方  可穿透敌人次数 */
	public final int APenetrate;		
	/** 子弹发射数量 */
	public final int BulletTimes;		
	/** 子弹 飞行速度  每秒XX像素 */
	public final int BulletSpeed;		
	/** 子弹击中敌方伤害类型 0直接伤害 1爆炸 2分裂 3怪物之间弹射 4施加buff */
	public final int[] HitType;		
	/** 伤害类型参数 0直接伤害 无 1爆炸 圆形半径(10000 全屏) 2分裂 角度 个数 分裂子弹ID 3弹射敌人数量 4 buffID */
	public final int[][] HitTypeParam;		
	/** 造成技能伤害公式 */
	public final String CalculateFun;		
	/** 伤害属性id 调用：AttrEffectConfig#属性Id表id */
	public final int[] AttrEffectConfigId;		
	/** 技能伤害参数百分比组  【多子弹配置】        子弹1伤害%;子弹2伤害%;...;子弹n伤害% 【多子弹分裂配置】  分裂子弹1伤害%;分裂子弹2伤害%;...;分裂子弹n伤害% 【多子弹穿透配置】  穿透子弹1伤害%;穿透子弹2伤害%;...;穿透子弹n伤害% */
	public final int[] CalculateParam;		
	/** 子弹特效延迟飞出时间  毫秒 */
	public final int BulletDelayTime;		
	/** 子弹特效文件prefab */
	public final String BulletEffect;		
	/** 子弹爆炸特效文件prefab */
	public final String HitEffect;		

	public HeroBulletConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		Duration = Integer.parseInt(element.getAttribute("Duration") == null || element.getAttribute("Duration").length() == 0 ? "0"
			: element.getAttribute("Duration")); // 技能持续时间  0 无需计算持续时间 ＞1000（毫秒）
		TrackType = Integer.parseInt(element.getAttribute("TrackType") == null || element.getAttribute("TrackType").length() == 0 ? "0"
			: element.getAttribute("TrackType")); // 轨迹类型 1直线 2屏幕反弹 3回旋 4散射 5散射回旋
		String TrackParameString = element.getAttribute("TrackParame"); // 轨迹类型参数 1直线 填0 2屏幕反弹 填反弹次数 3回旋极限距离 4散射最大扇形夹角 5散射最大扇形夹角;回旋极限距离
		if (TrackParameString != null && TrackParameString.length() > 0) {
			String[] TrackParameStrings = TrackParameString.split(";"); 
			int[] TrackParameTemp = new int[TrackParameStrings.length] ; 
			for (int i = 0; i < TrackParameStrings.length; i++) {
				int temp = Integer.parseInt(TrackParameStrings[i]);	
				TrackParameTemp[i] = temp;
			}
			TrackParame = TrackParameTemp ;			
		} else {
			TrackParame = new int[] {};
		}
		APenetrate = Integer.parseInt(element.getAttribute("APenetrate") == null || element.getAttribute("APenetrate").length() == 0 ? "0"
			: element.getAttribute("APenetrate")); // 子弹 击中敌方  可穿透敌人次数
		BulletTimes = Integer.parseInt(element.getAttribute("BulletTimes") == null || element.getAttribute("BulletTimes").length() == 0 ? "0"
			: element.getAttribute("BulletTimes")); // 子弹发射数量
		BulletSpeed = Integer.parseInt(element.getAttribute("BulletSpeed") == null || element.getAttribute("BulletSpeed").length() == 0 ? "0"
			: element.getAttribute("BulletSpeed")); // 子弹 飞行速度  每秒XX像素
		String HitTypeString = element.getAttribute("HitType"); // 子弹击中敌方伤害类型 0直接伤害 1爆炸 2分裂 3怪物之间弹射 4施加buff
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
		String HitTypeParamString = element.getAttribute("HitTypeParam"); // 伤害类型参数 0直接伤害 无 1爆炸 圆形半径(10000 全屏) 2分裂 角度 个数 分裂子弹ID 3弹射敌人数量 4 buffID
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
		String AttrEffectConfigIdString = element.getAttribute("AttrEffectConfigId"); // 伤害属性id 调用：AttrEffectConfig#属性Id表id
		if (AttrEffectConfigIdString != null && AttrEffectConfigIdString.length() > 0) {
			String[] AttrEffectConfigIdStrings = AttrEffectConfigIdString.split(";"); 
			int[] AttrEffectConfigIdTemp = new int[AttrEffectConfigIdStrings.length] ; 
			for (int i = 0; i < AttrEffectConfigIdStrings.length; i++) {
				int temp = Integer.parseInt(AttrEffectConfigIdStrings[i]);	
				AttrEffectConfigIdTemp[i] = temp;
			}
			AttrEffectConfigId = AttrEffectConfigIdTemp ;			
		} else {
			AttrEffectConfigId = new int[] {};
		}
		String CalculateParamString = element.getAttribute("CalculateParam"); // 技能伤害参数百分比组  【多子弹配置】        子弹1伤害%;子弹2伤害%;...;子弹n伤害% 【多子弹分裂配置】  分裂子弹1伤害%;分裂子弹2伤害%;...;分裂子弹n伤害% 【多子弹穿透配置】  穿透子弹1伤害%;穿透子弹2伤害%;...;穿透子弹n伤害%
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
		BulletDelayTime = Integer.parseInt(element.getAttribute("BulletDelayTime") == null || element.getAttribute("BulletDelayTime").length() == 0 ? "0"
			: element.getAttribute("BulletDelayTime")); // 子弹特效延迟飞出时间  毫秒
		BulletEffect = element.getAttribute("BulletEffect"); // 子弹特效文件prefab
		HitEffect = element.getAttribute("HitEffect"); // 子弹爆炸特效文件prefab
	}
	

}
