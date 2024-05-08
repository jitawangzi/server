package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 怪物刷新
 * 
 * 工具生成的，不要手动修改
 */
 public class MonsterAppearConfig {

	/** 索引 顺延 */
	public final int ID;		
	/** 战役ID 使用 Battle#战役 表中的ID */
	public final int BattleID;		
	/** 用于区分同一场战疫不同刷怪组先后激活顺序，ID一样则代表同时刷出 */
	public final int Round;		
	/** 开始刷怪时间ms；刷怪持续时长ms */
	public final int[] RoundStartTime;		
	/** 取 Monster#怪物属性 表中的ID 一波怪物只能刷一个ID的怪物 */
	public final String MonsterID;		
	/** 刷怪数量 */
	public final int EnemyCount;		
	/** 预警  0无预警 1怪潮预警 2BOSS预警 */
	public final int Waring;		
	/** 特殊处理  0不处理 1清屏 */
	public final int SpecialOpt;		
	/** 能量  每击杀1个怪给的能量数 */
	public final int EnergyPoint;		

	public MonsterAppearConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 索引 顺延
		BattleID = Integer.parseInt(element.getAttribute("BattleID") == null || element.getAttribute("BattleID").length() == 0 ? "0"
			: element.getAttribute("BattleID")); // 战役ID 使用 Battle#战役 表中的ID
		Round = Integer.parseInt(element.getAttribute("Round") == null || element.getAttribute("Round").length() == 0 ? "0"
			: element.getAttribute("Round")); // 用于区分同一场战疫不同刷怪组先后激活顺序，ID一样则代表同时刷出
		String RoundStartTimeString = element.getAttribute("RoundStartTime"); // 开始刷怪时间ms；刷怪持续时长ms
		if (RoundStartTimeString != null && RoundStartTimeString.length() > 0) {
			String[] RoundStartTimeStrings = RoundStartTimeString.split(";"); 
			int[] RoundStartTimeTemp = new int[RoundStartTimeStrings.length] ; 
			for (int i = 0; i < RoundStartTimeStrings.length; i++) {
				int temp = Integer.parseInt(RoundStartTimeStrings[i]);	
				RoundStartTimeTemp[i] = temp;
			}
			RoundStartTime = RoundStartTimeTemp ;			
		} else {
			RoundStartTime = new int[] {};
		}
		MonsterID = element.getAttribute("MonsterID"); // 取 Monster#怪物属性 表中的ID 一波怪物只能刷一个ID的怪物
		EnemyCount = Integer.parseInt(element.getAttribute("EnemyCount") == null || element.getAttribute("EnemyCount").length() == 0 ? "0"
			: element.getAttribute("EnemyCount")); // 刷怪数量
		Waring = Integer.parseInt(element.getAttribute("Waring") == null || element.getAttribute("Waring").length() == 0 ? "0"
			: element.getAttribute("Waring")); // 预警  0无预警 1怪潮预警 2BOSS预警
		SpecialOpt = Integer.parseInt(element.getAttribute("SpecialOpt") == null || element.getAttribute("SpecialOpt").length() == 0 ? "0"
			: element.getAttribute("SpecialOpt")); // 特殊处理  0不处理 1清屏
		EnergyPoint = Integer.parseInt(element.getAttribute("EnergyPoint") == null || element.getAttribute("EnergyPoint").length() == 0 ? "0"
			: element.getAttribute("EnergyPoint")); // 能量  每击杀1个怪给的能量数
	}
	

}
