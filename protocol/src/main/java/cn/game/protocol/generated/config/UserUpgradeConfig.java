package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 升级
 * 
 * 工具生成的，不要手动修改
 */
 public class UserUpgradeConfig extends ExpConfig {

	/** 等级 */
	public final int ID;		
	/** 玩家升级所需经验 */
	public final int experience;		
	/** 奖励货币或卡牌或物品1id;奖励1数量|...|货币或卡牌或物品Nid;奖励N数量 */
	public final int[][] LvReward;		
	/** 提升战力 */
	public final int Combatpower;		

	public UserUpgradeConfig (Element element) throws Exception {
	
		super(element);
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 等级
		experience = Integer.parseInt(element.getAttribute("experience") == null || element.getAttribute("experience").length() == 0 ? "0"
			: element.getAttribute("experience")); // 玩家升级所需经验
		String LvRewardString = element.getAttribute("LvReward"); // 奖励货币或卡牌或物品1id;奖励1数量|...|货币或卡牌或物品Nid;奖励N数量
		if (LvRewardString != null && LvRewardString.length() > 0) {
			String[] LvRewardStrings = LvRewardString.split("\\|"); 
			int[][] LvRewardTemp = new int[LvRewardStrings.length][] ; 
			for (int i = 0; i < LvRewardStrings.length; i++) {
				String[] LvRewardStrings2 = LvRewardStrings[i].split(";"); 
				int[] array = new int[LvRewardStrings2.length];
				for (int j = 0; j < LvRewardStrings2.length; j++) {
					int temp = Integer.parseInt(LvRewardStrings2[j]);	
					array[j] = temp;
				}
				LvRewardTemp[i] = array;
			}
			LvReward = LvRewardTemp ;			
		} else {
			LvReward = new int[][] {};
		}
		Combatpower = Integer.parseInt(element.getAttribute("Combatpower") == null || element.getAttribute("Combatpower").length() == 0 ? "0"
			: element.getAttribute("Combatpower")); // 提升战力
	}
	

}
