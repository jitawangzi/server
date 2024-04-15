package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 抽奖
 * 
 * 工具生成的，不要手动修改
 */
 public class LotteryConfig {

	/** 抽奖ID */
	public final int ID;		
	/** 抽奖消耗ID */
	public final int[] LotteryConsumptionID;		
	/** 普通掉落ID */
	public final int NormalDropID;		
	/** 小次数 */
	public final int GuaranteesCnt1;		
	/** 小保底掉落ID */
	public final int GuaranteesDropID1;		
	/** 大次数 */
	public final int GuaranteesCnt2;		
	/** 大保底掉落ID */
	public final int GuaranteesDropID2;		

	public LotteryConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 抽奖ID
		String LotteryConsumptionIDString = element.getAttribute("LotteryConsumptionID"); // 抽奖消耗ID
		if (LotteryConsumptionIDString != null && LotteryConsumptionIDString.length() > 0) {
			String[] LotteryConsumptionIDStrings = LotteryConsumptionIDString.split(";"); 
			int[] LotteryConsumptionIDTemp = new int[LotteryConsumptionIDStrings.length] ; 
			for (int i = 0; i < LotteryConsumptionIDStrings.length; i++) {
				int temp = Integer.parseInt(LotteryConsumptionIDStrings[i]);	
				LotteryConsumptionIDTemp[i] = temp;
			}
			LotteryConsumptionID = LotteryConsumptionIDTemp ;			
		} else {
			LotteryConsumptionID = new int[] {};
		}
		NormalDropID = Integer.parseInt(element.getAttribute("NormalDropID") == null || element.getAttribute("NormalDropID").length() == 0 ? "0"
			: element.getAttribute("NormalDropID")); // 普通掉落ID
		GuaranteesCnt1 = Integer.parseInt(element.getAttribute("GuaranteesCnt1") == null || element.getAttribute("GuaranteesCnt1").length() == 0 ? "0"
			: element.getAttribute("GuaranteesCnt1")); // 小次数
		GuaranteesDropID1 = Integer.parseInt(element.getAttribute("GuaranteesDropID1") == null || element.getAttribute("GuaranteesDropID1").length() == 0 ? "0"
			: element.getAttribute("GuaranteesDropID1")); // 小保底掉落ID
		GuaranteesCnt2 = Integer.parseInt(element.getAttribute("GuaranteesCnt2") == null || element.getAttribute("GuaranteesCnt2").length() == 0 ? "0"
			: element.getAttribute("GuaranteesCnt2")); // 大次数
		GuaranteesDropID2 = Integer.parseInt(element.getAttribute("GuaranteesDropID2") == null || element.getAttribute("GuaranteesDropID2").length() == 0 ? "0"
			: element.getAttribute("GuaranteesDropID2")); // 大保底掉落ID
	}
	

}
