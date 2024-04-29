package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 赠送卡
 * 
 * 工具生成的，不要手动修改
 */
 public class GiftCardConfig {

	/** id */
	public final int ID;		
	/** 卡池id */
	public final int DrawId;		
	/** 赠送卡牌的品质  4-紫 5-金 6-红 */
	public final int GiftCardQuality;		
	/** 必赠送神将抽卡次数 */
	public final int[] GiftCardCount;		
	/** 必赠卡牌掉落id  调用Rand-RandomGiven#掉落表id */
	public final int[] GiftCardRandomId;		

	public GiftCardConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // id
		DrawId = Integer.parseInt(element.getAttribute("DrawId") == null || element.getAttribute("DrawId").length() == 0 ? "0"
			: element.getAttribute("DrawId")); // 卡池id
		GiftCardQuality = Integer.parseInt(element.getAttribute("GiftCardQuality") == null || element.getAttribute("GiftCardQuality").length() == 0 ? "0"
			: element.getAttribute("GiftCardQuality")); // 赠送卡牌的品质  4-紫 5-金 6-红
		String GiftCardCountString = element.getAttribute("GiftCardCount"); // 必赠送神将抽卡次数
		if (GiftCardCountString != null && GiftCardCountString.length() > 0) {
			String[] GiftCardCountStrings = GiftCardCountString.split(";"); 
			int[] GiftCardCountTemp = new int[GiftCardCountStrings.length] ; 
			for (int i = 0; i < GiftCardCountStrings.length; i++) {
				int temp = Integer.parseInt(GiftCardCountStrings[i]);	
				GiftCardCountTemp[i] = temp;
			}
			GiftCardCount = GiftCardCountTemp ;			
		} else {
			GiftCardCount = new int[] {};
		}
		String GiftCardRandomIdString = element.getAttribute("GiftCardRandomId"); // 必赠卡牌掉落id  调用Rand-RandomGiven#掉落表id
		if (GiftCardRandomIdString != null && GiftCardRandomIdString.length() > 0) {
			String[] GiftCardRandomIdStrings = GiftCardRandomIdString.split(";"); 
			int[] GiftCardRandomIdTemp = new int[GiftCardRandomIdStrings.length] ; 
			for (int i = 0; i < GiftCardRandomIdStrings.length; i++) {
				int temp = Integer.parseInt(GiftCardRandomIdStrings[i]);	
				GiftCardRandomIdTemp[i] = temp;
			}
			GiftCardRandomId = GiftCardRandomIdTemp ;			
		} else {
			GiftCardRandomId = new int[] {};
		}
	}
	

}
