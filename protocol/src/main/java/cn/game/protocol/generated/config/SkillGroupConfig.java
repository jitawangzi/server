package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 技能组
 * 
 * 工具生成的，不要手动修改
 */
 public class SkillGroupConfig {

	/** 技能ID 每个英雄/怪物仅1个技能 全部技能为主动释放技能 策划约定： -英雄技能1打头 -怪物技能2打头 -Boss技能3打头 id5位=类型1位+国家队标识1位 +职业1位+序列号2位 */
	public final int ID;		
	/** 技能名称 */
	public final String SkillName;		
	/** 技能id组 配置：技能id1|技能id2 调用：SKill#技能 */
	public final int[] SkillGroup;		
	/** 权重 技能 直接根据权重确定使用那个技能 */
	public final int[] SkillWeight;		
	/** 技能使用次数用完就进入CD */
	public final int SkillTimes;		
	/** 技能强制cd时间毫秒 */
	public final int SkillCD;		

	public SkillGroupConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 技能ID 每个英雄/怪物仅1个技能 全部技能为主动释放技能 策划约定： -英雄技能1打头 -怪物技能2打头 -Boss技能3打头 id5位=类型1位+国家队标识1位 +职业1位+序列号2位
		SkillName = element.getAttribute("SkillName"); // 技能名称
		String SkillGroupString = element.getAttribute("SkillGroup"); // 技能id组 配置：技能id1|技能id2 调用：SKill#技能
		if (SkillGroupString != null && SkillGroupString.length() > 0) {
			String[] SkillGroupStrings = SkillGroupString.split(";"); 
			int[] SkillGroupTemp = new int[SkillGroupStrings.length] ; 
			for (int i = 0; i < SkillGroupStrings.length; i++) {
				int temp = Integer.parseInt(SkillGroupStrings[i]);	
				SkillGroupTemp[i] = temp;
			}
			SkillGroup = SkillGroupTemp ;			
		} else {
			SkillGroup = new int[] {};
		}
		String SkillWeightString = element.getAttribute("SkillWeight"); // 权重 技能 直接根据权重确定使用那个技能
		if (SkillWeightString != null && SkillWeightString.length() > 0) {
			String[] SkillWeightStrings = SkillWeightString.split(";"); 
			int[] SkillWeightTemp = new int[SkillWeightStrings.length] ; 
			for (int i = 0; i < SkillWeightStrings.length; i++) {
				int temp = Integer.parseInt(SkillWeightStrings[i]);	
				SkillWeightTemp[i] = temp;
			}
			SkillWeight = SkillWeightTemp ;			
		} else {
			SkillWeight = new int[] {};
		}
		SkillTimes = Integer.parseInt(element.getAttribute("SkillTimes") == null || element.getAttribute("SkillTimes").length() == 0 ? "0"
			: element.getAttribute("SkillTimes")); // 技能使用次数用完就进入CD
		SkillCD = Integer.parseInt(element.getAttribute("SkillCD") == null || element.getAttribute("SkillCD").length() == 0 ? "0"
			: element.getAttribute("SkillCD")); // 技能强制cd时间毫秒
	}
	

}
