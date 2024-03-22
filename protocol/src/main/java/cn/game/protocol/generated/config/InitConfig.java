package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 初始化
 * 
 * 工具生成的，不要手动修改
 */
 public class InitConfig {

	/** 初始化 */
	private final int ID;		
	/** 初始卡牌 初始给予卡牌id及星级 配置： 卡牌1id;卡牌2id;...;卡牌nid  卡牌id调用Hero#英雄 星级id调用HeroStar#英雄星级 */
	private final int[] HeroID;		
	/** 初始货币  配置： 货币id;个数|货币id;个数   货币id调用Money#货币表第1列id */
	private final int[][] MoneyID;		
	/** 初始开启系统id  调用功能开启表 */
	private final int[] FunctionID;		
	/** 初始任务ID */
	private final int QuestID;		

	public InitConfig (Element element) throws Exception {
	
		this.ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 初始化
		String HeroIDString = element.getAttribute("HeroID"); // 初始卡牌 初始给予卡牌id及星级 配置： 卡牌1id;卡牌2id;...;卡牌nid  卡牌id调用Hero#英雄 星级id调用HeroStar#英雄星级
		if (HeroIDString != null && HeroIDString.length() > 0) {
			String[] HeroIDStrings = HeroIDString.split(";"); 
			int[] HeroID = new int[HeroIDStrings.length] ; 
			for (int i = 0; i < HeroIDStrings.length; i++) {
				int temp = Integer.parseInt(HeroIDStrings[i]);
				HeroID[i] = temp;
			}
			this.HeroID = HeroID ;			
		} else {
			this.HeroID = new int[] {};
		}
		String MoneyIDString = element.getAttribute("MoneyID"); // 初始货币  配置： 货币id;个数|货币id;个数   货币id调用Money#货币表第1列id
		if (MoneyIDString != null && MoneyIDString.length() > 0) {
			String[] MoneyIDStrings = MoneyIDString.split("\\|"); 
			int[][] MoneyID = new int[MoneyIDStrings.length][] ; 
			for (int i = 0; i < MoneyIDStrings.length; i++) {
				String[] MoneyIDStrings2 = MoneyIDStrings[i].split(";"); 
				int[] array = new int[MoneyIDStrings2.length];
				for (int j = 0; j < MoneyIDStrings2.length; j++) {
					int temp = Integer.parseInt(MoneyIDStrings2[j]);
					array[j] = temp;
				}
				MoneyID[i] = array;
				
			}
			this.MoneyID = MoneyID ;			
		} else {
			this.MoneyID = new int[][] {};
		}
		String FunctionIDString = element.getAttribute("FunctionID"); // 初始开启系统id  调用功能开启表
		if (FunctionIDString != null && FunctionIDString.length() > 0) {
			String[] FunctionIDStrings = FunctionIDString.split(";"); 
			int[] FunctionID = new int[FunctionIDStrings.length] ; 
			for (int i = 0; i < FunctionIDStrings.length; i++) {
				int temp = Integer.parseInt(FunctionIDStrings[i]);
				FunctionID[i] = temp;
			}
			this.FunctionID = FunctionID ;			
		} else {
			this.FunctionID = new int[] {};
		}
		this.QuestID = Integer.parseInt(element.getAttribute("QuestID") == null || element.getAttribute("QuestID").length() == 0 ? "0"
			: element.getAttribute("QuestID")); // 初始任务ID
	}
	
	public int getID() {
		return ID;
	}
	
	public int[] getHeroID() {
		return HeroID;
	}
	
	public int[][] getMoneyID() {
		return MoneyID;
	}
	
	public int[] getFunctionID() {
		return FunctionID;
	}
	
	public int getQuestID() {
		return QuestID;
	}
	
}
