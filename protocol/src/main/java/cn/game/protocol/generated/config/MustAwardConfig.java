package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 必给奖励
 * 
 * 工具生成的，不要手动修改
 */
 public class MustAwardConfig {

	/** 奖励ID */
	private final int ID;		
	/** 必给物品 ID组 物品ID */
	private final int[] AwardSnGroup;		
	/** 必给物品 数量组 */
	private final int[] AwardCountGroup;		

	public MustAwardConfig (Element element) throws Exception {
	
		this.ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 奖励ID
		String AwardSnGroupString = element.getAttribute("AwardSnGroup"); // 必给物品 ID组 物品ID
		if (AwardSnGroupString != null && AwardSnGroupString.length() > 0) {
			String[] AwardSnGroupStrings = AwardSnGroupString.split(";"); 
			int[] AwardSnGroup = new int[AwardSnGroupStrings.length] ; 
			for (int i = 0; i < AwardSnGroupStrings.length; i++) {
				int temp = Integer.parseInt(AwardSnGroupStrings[i]);
				AwardSnGroup[i] = temp;
			}
			this.AwardSnGroup = AwardSnGroup ;			
		} else {
			this.AwardSnGroup = new int[] {};
		}
		String AwardCountGroupString = element.getAttribute("AwardCountGroup"); // 必给物品 数量组
		if (AwardCountGroupString != null && AwardCountGroupString.length() > 0) {
			String[] AwardCountGroupStrings = AwardCountGroupString.split(";"); 
			int[] AwardCountGroup = new int[AwardCountGroupStrings.length] ; 
			for (int i = 0; i < AwardCountGroupStrings.length; i++) {
				int temp = Integer.parseInt(AwardCountGroupStrings[i]);
				AwardCountGroup[i] = temp;
			}
			this.AwardCountGroup = AwardCountGroup ;			
		} else {
			this.AwardCountGroup = new int[] {};
		}
	}
	
	public int getID() {
		return ID;
	}
	
	public int[] getAwardSnGroup() {
		return AwardSnGroup;
	}
	
	public int[] getAwardCountGroup() {
		return AwardCountGroup;
	}
	
}
