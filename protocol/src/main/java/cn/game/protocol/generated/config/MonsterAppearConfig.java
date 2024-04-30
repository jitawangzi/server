package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 怪物刷新
 * 
 * 工具生成的，不要手动修改
 */
 public class MonsterAppearConfig {

	/** 索引 */
	public final int ID;		
	/** 刷怪组ID */
	public final int GroupIDs;		
	/** 循环类型 1定时刷怪 2剩余补怪 3定时刷怪 */
	public final int LoopType;		
	/** 循环参数 1定时刷怪   每隔N秒就刷怪；到M秒结束刷怪 2剩余补怪   当场上怪物剩余≤N个就开始刷怪；到M秒结束刷怪 3定时刷怪   在N秒时开始刷怪 */
	public final int[] LoopParms;		
	/** 刷怪参数  调用Battle表中BattleField#关卡 */
	public final String EnemyType;		
	/** 刷怪数量 */
	public final int EnemyCount;		
	/** 刷怪速率（秒）  刷怪数量100 速率5 =100个怪在5秒内刷完 */
	public final int Time;		
	/** 预警  0无预警 1怪潮预警 2BOSS预警 */
	public final int Waring;		
	/** 特殊处理  0不处理 1清屏 */
	public final int SpecialOpt;		
	/** 能量  每击杀1个怪给的能量数 */
	public final int EnergyPoint;		
	/** 彩蛋触发器  触发类型；触发概率/10000 0=无触发 1=刷怪前触发 2=击杀后触发 */
	public final int[] Egg;		

	public MonsterAppearConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 索引
		GroupIDs = Integer.parseInt(element.getAttribute("GroupIDs") == null || element.getAttribute("GroupIDs").length() == 0 ? "0"
			: element.getAttribute("GroupIDs")); // 刷怪组ID
		LoopType = Integer.parseInt(element.getAttribute("LoopType") == null || element.getAttribute("LoopType").length() == 0 ? "0"
			: element.getAttribute("LoopType")); // 循环类型 1定时刷怪 2剩余补怪 3定时刷怪
		String LoopParmsString = element.getAttribute("LoopParms"); // 循环参数 1定时刷怪   每隔N秒就刷怪；到M秒结束刷怪 2剩余补怪   当场上怪物剩余≤N个就开始刷怪；到M秒结束刷怪 3定时刷怪   在N秒时开始刷怪
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
		EnemyType = element.getAttribute("EnemyType"); // 刷怪参数  调用Battle表中BattleField#关卡
		EnemyCount = Integer.parseInt(element.getAttribute("EnemyCount") == null || element.getAttribute("EnemyCount").length() == 0 ? "0"
			: element.getAttribute("EnemyCount")); // 刷怪数量
		Time = Integer.parseInt(element.getAttribute("Time") == null || element.getAttribute("Time").length() == 0 ? "0"
			: element.getAttribute("Time")); // 刷怪速率（秒）  刷怪数量100 速率5 =100个怪在5秒内刷完
		Waring = Integer.parseInt(element.getAttribute("Waring") == null || element.getAttribute("Waring").length() == 0 ? "0"
			: element.getAttribute("Waring")); // 预警  0无预警 1怪潮预警 2BOSS预警
		SpecialOpt = Integer.parseInt(element.getAttribute("SpecialOpt") == null || element.getAttribute("SpecialOpt").length() == 0 ? "0"
			: element.getAttribute("SpecialOpt")); // 特殊处理  0不处理 1清屏
		EnergyPoint = Integer.parseInt(element.getAttribute("EnergyPoint") == null || element.getAttribute("EnergyPoint").length() == 0 ? "0"
			: element.getAttribute("EnergyPoint")); // 能量  每击杀1个怪给的能量数
		String EggString = element.getAttribute("Egg"); // 彩蛋触发器  触发类型；触发概率/10000 0=无触发 1=刷怪前触发 2=击杀后触发
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
	}
	

}
