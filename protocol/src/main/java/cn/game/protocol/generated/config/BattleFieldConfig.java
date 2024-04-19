package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 关卡
 * 
 * 工具生成的，不要手动修改
 */
 public class BattleFieldConfig {

	/** 关卡ID */
	public final int ID;		
	/** 场景资源 */
	public final String SceneRes;		
	/** 推荐等级 */
	public final int Level;		
	/** 推荐战力 */
	public final int AtkValue;		
	/** 关卡限时 */
	public final int LimitedTime;		
	/** 胜利类型 */
	public final int WinCondition;		
	/** 玩法类型 */
	public final int PlayType;		
	/** 玩法类型参数 */
	public final int PlayTypeParameter;		
	/** 小怪1 */
	public final int[][] monster1;		
	/** 小怪2 */
	public final int[][] monster2;		
	/** 精英1 */
	public final int[][] elite1;		
	/** 精英2 */
	public final int[][] elite2;		
	/** BOSS1 */
	public final int[][] boss1;		
	/** BOSS2 */
	public final int[][] boss2;		
	/** 肉鸽能量ID */
	public final int RogeEnergyID;		
	/** 难度提升条件 */
	public final int HardCondtion;		
	/** 难度提升天数 */
	public final int HardType;		
	/** 提升属性 */
	public final int[][] MonsterAttExt;		

	public BattleFieldConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 关卡ID
		SceneRes = element.getAttribute("SceneRes"); // 场景资源
		Level = Integer.parseInt(element.getAttribute("Level") == null || element.getAttribute("Level").length() == 0 ? "0"
			: element.getAttribute("Level")); // 推荐等级
		AtkValue = Integer.parseInt(element.getAttribute("AtkValue") == null || element.getAttribute("AtkValue").length() == 0 ? "0"
			: element.getAttribute("AtkValue")); // 推荐战力
		LimitedTime = Integer.parseInt(element.getAttribute("LimitedTime") == null || element.getAttribute("LimitedTime").length() == 0 ? "0"
			: element.getAttribute("LimitedTime")); // 关卡限时
		WinCondition = Integer.parseInt(element.getAttribute("WinCondition") == null || element.getAttribute("WinCondition").length() == 0 ? "0"
			: element.getAttribute("WinCondition")); // 胜利类型
		PlayType = Integer.parseInt(element.getAttribute("PlayType") == null || element.getAttribute("PlayType").length() == 0 ? "0"
			: element.getAttribute("PlayType")); // 玩法类型
		PlayTypeParameter = Integer.parseInt(element.getAttribute("PlayTypeParameter") == null || element.getAttribute("PlayTypeParameter").length() == 0 ? "0"
			: element.getAttribute("PlayTypeParameter")); // 玩法类型参数
		String monster1String = element.getAttribute("monster1"); // 小怪1
		if (monster1String != null && monster1String.length() > 0) {
			String[] monster1Strings = monster1String.split("\\|"); 
			int[][] monster1Temp = new int[monster1Strings.length][] ; 
			for (int i = 0; i < monster1Strings.length; i++) {
				String[] monster1Strings2 = monster1Strings[i].split(";"); 
				int[] array = new int[monster1Strings2.length];
				for (int j = 0; j < monster1Strings2.length; j++) {
					int temp = Integer.parseInt(monster1Strings2[j]);	
					array[j] = temp;
				}
				monster1Temp[i] = array;
			}
			monster1 = monster1Temp ;			
		} else {
			monster1 = new int[][] {};
		}
		String monster2String = element.getAttribute("monster2"); // 小怪2
		if (monster2String != null && monster2String.length() > 0) {
			String[] monster2Strings = monster2String.split("\\|"); 
			int[][] monster2Temp = new int[monster2Strings.length][] ; 
			for (int i = 0; i < monster2Strings.length; i++) {
				String[] monster2Strings2 = monster2Strings[i].split(";"); 
				int[] array = new int[monster2Strings2.length];
				for (int j = 0; j < monster2Strings2.length; j++) {
					int temp = Integer.parseInt(monster2Strings2[j]);	
					array[j] = temp;
				}
				monster2Temp[i] = array;
			}
			monster2 = monster2Temp ;			
		} else {
			monster2 = new int[][] {};
		}
		String elite1String = element.getAttribute("elite1"); // 精英1
		if (elite1String != null && elite1String.length() > 0) {
			String[] elite1Strings = elite1String.split("\\|"); 
			int[][] elite1Temp = new int[elite1Strings.length][] ; 
			for (int i = 0; i < elite1Strings.length; i++) {
				String[] elite1Strings2 = elite1Strings[i].split(";"); 
				int[] array = new int[elite1Strings2.length];
				for (int j = 0; j < elite1Strings2.length; j++) {
					int temp = Integer.parseInt(elite1Strings2[j]);	
					array[j] = temp;
				}
				elite1Temp[i] = array;
			}
			elite1 = elite1Temp ;			
		} else {
			elite1 = new int[][] {};
		}
		String elite2String = element.getAttribute("elite2"); // 精英2
		if (elite2String != null && elite2String.length() > 0) {
			String[] elite2Strings = elite2String.split("\\|"); 
			int[][] elite2Temp = new int[elite2Strings.length][] ; 
			for (int i = 0; i < elite2Strings.length; i++) {
				String[] elite2Strings2 = elite2Strings[i].split(";"); 
				int[] array = new int[elite2Strings2.length];
				for (int j = 0; j < elite2Strings2.length; j++) {
					int temp = Integer.parseInt(elite2Strings2[j]);	
					array[j] = temp;
				}
				elite2Temp[i] = array;
			}
			elite2 = elite2Temp ;			
		} else {
			elite2 = new int[][] {};
		}
		String boss1String = element.getAttribute("boss1"); // BOSS1
		if (boss1String != null && boss1String.length() > 0) {
			String[] boss1Strings = boss1String.split("\\|"); 
			int[][] boss1Temp = new int[boss1Strings.length][] ; 
			for (int i = 0; i < boss1Strings.length; i++) {
				String[] boss1Strings2 = boss1Strings[i].split(";"); 
				int[] array = new int[boss1Strings2.length];
				for (int j = 0; j < boss1Strings2.length; j++) {
					int temp = Integer.parseInt(boss1Strings2[j]);	
					array[j] = temp;
				}
				boss1Temp[i] = array;
			}
			boss1 = boss1Temp ;			
		} else {
			boss1 = new int[][] {};
		}
		String boss2String = element.getAttribute("boss2"); // BOSS2
		if (boss2String != null && boss2String.length() > 0) {
			String[] boss2Strings = boss2String.split("\\|"); 
			int[][] boss2Temp = new int[boss2Strings.length][] ; 
			for (int i = 0; i < boss2Strings.length; i++) {
				String[] boss2Strings2 = boss2Strings[i].split(";"); 
				int[] array = new int[boss2Strings2.length];
				for (int j = 0; j < boss2Strings2.length; j++) {
					int temp = Integer.parseInt(boss2Strings2[j]);	
					array[j] = temp;
				}
				boss2Temp[i] = array;
			}
			boss2 = boss2Temp ;			
		} else {
			boss2 = new int[][] {};
		}
		RogeEnergyID = Integer.parseInt(element.getAttribute("RogeEnergyID") == null || element.getAttribute("RogeEnergyID").length() == 0 ? "0"
			: element.getAttribute("RogeEnergyID")); // 肉鸽能量ID
		HardCondtion = Integer.parseInt(element.getAttribute("HardCondtion") == null || element.getAttribute("HardCondtion").length() == 0 ? "0"
			: element.getAttribute("HardCondtion")); // 难度提升条件
		HardType = Integer.parseInt(element.getAttribute("HardType") == null || element.getAttribute("HardType").length() == 0 ? "0"
			: element.getAttribute("HardType")); // 难度提升天数
		String MonsterAttExtString = element.getAttribute("MonsterAttExt"); // 提升属性
		if (MonsterAttExtString != null && MonsterAttExtString.length() > 0) {
			String[] MonsterAttExtStrings = MonsterAttExtString.split("\\|"); 
			int[][] MonsterAttExtTemp = new int[MonsterAttExtStrings.length][] ; 
			for (int i = 0; i < MonsterAttExtStrings.length; i++) {
				String[] MonsterAttExtStrings2 = MonsterAttExtStrings[i].split(";"); 
				int[] array = new int[MonsterAttExtStrings2.length];
				for (int j = 0; j < MonsterAttExtStrings2.length; j++) {
					int temp = Integer.parseInt(MonsterAttExtStrings2[j]);	
					array[j] = temp;
				}
				MonsterAttExtTemp[i] = array;
			}
			MonsterAttExt = MonsterAttExtTemp ;			
		} else {
			MonsterAttExt = new int[][] {};
		}
	}
	

}
