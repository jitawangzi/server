package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 掉落
 * 
 * 工具生成的，不要手动修改
 */
 public class RandomGivenConfig {

	/** 掉落ID */
	public final int ID;		
	/** 必掉物品组 物品id;个数 以下方便策划看： 100001 元宝      100002 金币    100201 玩家经验 100301 体力      202001 串串（神将升级） 203001 请神符   100206潜力升级  100207潜力突破 */
	public final int[][] MustGiven;		
	/** 随机次数  表示最后能掉出多少个物品 数量min;数量max */
	public final int[] RandomNumber;		
	/** 掉落组id——组合  自定义掉落id */
	public final int[] RandomParameterGroupId;		
	/** 每个掉落组id的权重 */
	public final int[] RandomParameterWeight;		

	public RandomGivenConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 掉落ID
		String MustGivenString = element.getAttribute("MustGiven"); // 必掉物品组 物品id;个数 以下方便策划看： 100001 元宝      100002 金币    100201 玩家经验 100301 体力      202001 串串（神将升级） 203001 请神符   100206潜力升级  100207潜力突破
		if (MustGivenString != null && MustGivenString.length() > 0) {
			String[] MustGivenStrings = MustGivenString.split("\\|"); 
			int[][] MustGivenTemp = new int[MustGivenStrings.length][] ; 
			for (int i = 0; i < MustGivenStrings.length; i++) {
				String[] MustGivenStrings2 = MustGivenStrings[i].split(";"); 
				int[] array = new int[MustGivenStrings2.length];
				for (int j = 0; j < MustGivenStrings2.length; j++) {
					int temp = Integer.parseInt(MustGivenStrings2[j]);	
					array[j] = temp;
				}
				MustGivenTemp[i] = array;
			}
			MustGiven = MustGivenTemp ;			
		} else {
			MustGiven = new int[][] {};
		}
		String RandomNumberString = element.getAttribute("RandomNumber"); // 随机次数  表示最后能掉出多少个物品 数量min;数量max
		if (RandomNumberString != null && RandomNumberString.length() > 0) {
			String[] RandomNumberStrings = RandomNumberString.split(";"); 
			int[] RandomNumberTemp = new int[RandomNumberStrings.length] ; 
			for (int i = 0; i < RandomNumberStrings.length; i++) {
				int temp = Integer.parseInt(RandomNumberStrings[i]);	
				RandomNumberTemp[i] = temp;
			}
			RandomNumber = RandomNumberTemp ;			
		} else {
			RandomNumber = new int[] {};
		}
		String RandomParameterGroupIdString = element.getAttribute("RandomParameterGroupId"); // 掉落组id——组合  自定义掉落id
		if (RandomParameterGroupIdString != null && RandomParameterGroupIdString.length() > 0) {
			String[] RandomParameterGroupIdStrings = RandomParameterGroupIdString.split(";"); 
			int[] RandomParameterGroupIdTemp = new int[RandomParameterGroupIdStrings.length] ; 
			for (int i = 0; i < RandomParameterGroupIdStrings.length; i++) {
				int temp = Integer.parseInt(RandomParameterGroupIdStrings[i]);	
				RandomParameterGroupIdTemp[i] = temp;
			}
			RandomParameterGroupId = RandomParameterGroupIdTemp ;			
		} else {
			RandomParameterGroupId = new int[] {};
		}
		String RandomParameterWeightString = element.getAttribute("RandomParameterWeight"); // 每个掉落组id的权重
		if (RandomParameterWeightString != null && RandomParameterWeightString.length() > 0) {
			String[] RandomParameterWeightStrings = RandomParameterWeightString.split(";"); 
			int[] RandomParameterWeightTemp = new int[RandomParameterWeightStrings.length] ; 
			for (int i = 0; i < RandomParameterWeightStrings.length; i++) {
				int temp = Integer.parseInt(RandomParameterWeightStrings[i]);	
				RandomParameterWeightTemp[i] = temp;
			}
			RandomParameterWeight = RandomParameterWeightTemp ;			
		} else {
			RandomParameterWeight = new int[] {};
		}
	}
	

}
