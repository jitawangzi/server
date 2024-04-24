package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 英雄
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroConfig {

	/** 英雄ID */
	public final int ID;		
	/** 英雄源ID */
	public final int HeroSourceID;		
	/** 品质ID */
	public final int Quality;		
	/** 突破目标ID */
	public final int PromoteTargetID;		
	/** 星级突破消耗 1;职业;品质;数量| */
	public final int[][] StarPromoteConsume;		
	/** 品质突破消耗 2;英雄ID;数量| */
	public final int[][] QualityPromoteConsume;		
	/** 星级突破万能耗材ID */
	public final int StarOmniItemID;		
	/** 品质突破万能耗材ID */
	public final int QualityOmniItemID;		

	public HeroConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 英雄ID
		HeroSourceID = Integer.parseInt(element.getAttribute("HeroSourceID") == null || element.getAttribute("HeroSourceID").length() == 0 ? "0"
			: element.getAttribute("HeroSourceID")); // 英雄源ID
		Quality = Integer.parseInt(element.getAttribute("Quality") == null || element.getAttribute("Quality").length() == 0 ? "0"
			: element.getAttribute("Quality")); // 品质ID
		PromoteTargetID = Integer.parseInt(element.getAttribute("PromoteTargetID") == null || element.getAttribute("PromoteTargetID").length() == 0 ? "0"
			: element.getAttribute("PromoteTargetID")); // 突破目标ID
		String StarPromoteConsumeString = element.getAttribute("StarPromoteConsume"); // 星级突破消耗 1;职业;品质;数量|
		if (StarPromoteConsumeString != null && StarPromoteConsumeString.length() > 0) {
			String[] StarPromoteConsumeStrings = StarPromoteConsumeString.split("\\|"); 
			int[][] StarPromoteConsumeTemp = new int[StarPromoteConsumeStrings.length][] ; 
			for (int i = 0; i < StarPromoteConsumeStrings.length; i++) {
				String[] StarPromoteConsumeStrings2 = StarPromoteConsumeStrings[i].split(";"); 
				int[] array = new int[StarPromoteConsumeStrings2.length];
				for (int j = 0; j < StarPromoteConsumeStrings2.length; j++) {
					int temp = Integer.parseInt(StarPromoteConsumeStrings2[j]);	
					array[j] = temp;
				}
				StarPromoteConsumeTemp[i] = array;
			}
			StarPromoteConsume = StarPromoteConsumeTemp ;			
		} else {
			StarPromoteConsume = new int[][] {};
		}
		String QualityPromoteConsumeString = element.getAttribute("QualityPromoteConsume"); // 品质突破消耗 2;英雄ID;数量|
		if (QualityPromoteConsumeString != null && QualityPromoteConsumeString.length() > 0) {
			String[] QualityPromoteConsumeStrings = QualityPromoteConsumeString.split("\\|"); 
			int[][] QualityPromoteConsumeTemp = new int[QualityPromoteConsumeStrings.length][] ; 
			for (int i = 0; i < QualityPromoteConsumeStrings.length; i++) {
				String[] QualityPromoteConsumeStrings2 = QualityPromoteConsumeStrings[i].split(";"); 
				int[] array = new int[QualityPromoteConsumeStrings2.length];
				for (int j = 0; j < QualityPromoteConsumeStrings2.length; j++) {
					int temp = Integer.parseInt(QualityPromoteConsumeStrings2[j]);	
					array[j] = temp;
				}
				QualityPromoteConsumeTemp[i] = array;
			}
			QualityPromoteConsume = QualityPromoteConsumeTemp ;			
		} else {
			QualityPromoteConsume = new int[][] {};
		}
		StarOmniItemID = Integer.parseInt(element.getAttribute("StarOmniItemID") == null || element.getAttribute("StarOmniItemID").length() == 0 ? "0"
			: element.getAttribute("StarOmniItemID")); // 星级突破万能耗材ID
		QualityOmniItemID = Integer.parseInt(element.getAttribute("QualityOmniItemID") == null || element.getAttribute("QualityOmniItemID").length() == 0 ? "0"
			: element.getAttribute("QualityOmniItemID")); // 品质突破万能耗材ID
	}
	

}
