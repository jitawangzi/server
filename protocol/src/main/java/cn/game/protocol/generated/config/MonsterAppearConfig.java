package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 定时刷怪
 * 
 * 工具生成的，不要手动修改
 */
 public class MonsterAppearConfig {

	/** 索引 */
	public final int ID;		
	/** 刷怪组ID */
	public final int GroupIDs;		
	/** 循环类型 */
	public final int LoopType;		
	/** 循环参数 */
	public final int[] LoopParms;		
	/** 刷怪参数 */
	public final String EnemyType;		
	/** 刷怪数量 */
	public final int EnemyCount;		
	/** 刷怪位置 */
	public final int[] EnemyPos;		
	/** 刷怪速率 */
	public final int Time;		
	/** 预警 */
	public final int Waring;		
	/** 特殊处理 */
	public final int SpecialOpt;		
	/** 能量价值 */
	public final int EnergyPoint;		
	/** 彩蛋触发器 */
	public final int[] Egg;		
	/** 麻吉触发器 */
	public final int[] UniqueSkills;		

	public MonsterAppearConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 索引
		GroupIDs = Integer.parseInt(element.getAttribute("GroupIDs") == null || element.getAttribute("GroupIDs").length() == 0 ? "0"
			: element.getAttribute("GroupIDs")); // 刷怪组ID
		LoopType = Integer.parseInt(element.getAttribute("LoopType") == null || element.getAttribute("LoopType").length() == 0 ? "0"
			: element.getAttribute("LoopType")); // 循环类型
		String LoopParmsString = element.getAttribute("LoopParms"); // 循环参数
		if (LoopParmsString != null && LoopParmsString.length() > 0) {
			String[] LoopParmsStrings = LoopParmsString.split(";"); 
			int[] LoopParmsTemp = new int[LoopParmsStrings.length] ; 
			for (int i = 0; i < LoopParmsStrings.length; i++) {
				int temp = Integer.parseInt(LoopParmsStrings[i]);	
				LoopParmsTemp[i] = temp;
			}
			LoopParms = LoopParmsTemp ;			
		} else {
			LoopParms = new int[] {};
		}
		EnemyType = element.getAttribute("EnemyType"); // 刷怪参数
		EnemyCount = Integer.parseInt(element.getAttribute("EnemyCount") == null || element.getAttribute("EnemyCount").length() == 0 ? "0"
			: element.getAttribute("EnemyCount")); // 刷怪数量
		String EnemyPosString = element.getAttribute("EnemyPos"); // 刷怪位置
		if (EnemyPosString != null && EnemyPosString.length() > 0) {
			String[] EnemyPosStrings = EnemyPosString.split(";"); 
			int[] EnemyPosTemp = new int[EnemyPosStrings.length] ; 
			for (int i = 0; i < EnemyPosStrings.length; i++) {
				int temp = Integer.parseInt(EnemyPosStrings[i]);	
				EnemyPosTemp[i] = temp;
			}
			EnemyPos = EnemyPosTemp ;			
		} else {
			EnemyPos = new int[] {};
		}
		Time = Integer.parseInt(element.getAttribute("Time") == null || element.getAttribute("Time").length() == 0 ? "0"
			: element.getAttribute("Time")); // 刷怪速率
		Waring = Integer.parseInt(element.getAttribute("Waring") == null || element.getAttribute("Waring").length() == 0 ? "0"
			: element.getAttribute("Waring")); // 预警
		SpecialOpt = Integer.parseInt(element.getAttribute("SpecialOpt") == null || element.getAttribute("SpecialOpt").length() == 0 ? "0"
			: element.getAttribute("SpecialOpt")); // 特殊处理
		EnergyPoint = Integer.parseInt(element.getAttribute("EnergyPoint") == null || element.getAttribute("EnergyPoint").length() == 0 ? "0"
			: element.getAttribute("EnergyPoint")); // 能量价值
		String EggString = element.getAttribute("Egg"); // 彩蛋触发器
		if (EggString != null && EggString.length() > 0) {
			String[] EggStrings = EggString.split(";"); 
			int[] EggTemp = new int[EggStrings.length] ; 
			for (int i = 0; i < EggStrings.length; i++) {
				int temp = Integer.parseInt(EggStrings[i]);	
				EggTemp[i] = temp;
			}
			Egg = EggTemp ;			
		} else {
			Egg = new int[] {};
		}
		String UniqueSkillsString = element.getAttribute("UniqueSkills"); // 麻吉触发器
		if (UniqueSkillsString != null && UniqueSkillsString.length() > 0) {
			String[] UniqueSkillsStrings = UniqueSkillsString.split(";"); 
			int[] UniqueSkillsTemp = new int[UniqueSkillsStrings.length] ; 
			for (int i = 0; i < UniqueSkillsStrings.length; i++) {
				int temp = Integer.parseInt(UniqueSkillsStrings[i]);	
				UniqueSkillsTemp[i] = temp;
			}
			UniqueSkills = UniqueSkillsTemp ;			
		} else {
			UniqueSkills = new int[] {};
		}
	}
	

}
