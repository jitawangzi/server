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
	/** 掉落展示 */
	public final int[] Show;		
	/** 必掉物品组 */
	public final int[][] MustGiven;		
	/** 随机次数 数量下限;数量上限 */
	public final int[] RandomNumber;		
	/** 掉落参数 */
	public final int[][] RandomParameter;		

	public RandomGivenConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 掉落ID
		String ShowString = element.getAttribute("Show"); // 掉落展示
		if (ShowString != null && ShowString.length() > 0) {
			String[] ShowStrings = ShowString.split(";"); 
			int[] ShowTemp = new int[ShowStrings.length] ; 
			for (int i = 0; i < ShowStrings.length; i++) {
				int temp = Integer.parseInt(ShowStrings[i]);	
				ShowTemp[i] = temp;
			}
			Show = ShowTemp ;			
		} else {
			Show = new int[] {};
		}
		String MustGivenString = element.getAttribute("MustGiven"); // 必掉物品组
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
		String RandomNumberString = element.getAttribute("RandomNumber"); // 随机次数 数量下限;数量上限
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
		String RandomParameterString = element.getAttribute("RandomParameter"); // 掉落参数
		if (RandomParameterString != null && RandomParameterString.length() > 0) {
			String[] RandomParameterStrings = RandomParameterString.split("\\|"); 
			int[][] RandomParameterTemp = new int[RandomParameterStrings.length][] ; 
			for (int i = 0; i < RandomParameterStrings.length; i++) {
				String[] RandomParameterStrings2 = RandomParameterStrings[i].split(";"); 
				int[] array = new int[RandomParameterStrings2.length];
				for (int j = 0; j < RandomParameterStrings2.length; j++) {
					int temp = Integer.parseInt(RandomParameterStrings2[j]);	
					array[j] = temp;
				}
				RandomParameterTemp[i] = array;
			}
			RandomParameter = RandomParameterTemp ;			
		} else {
			RandomParameter = new int[][] {};
		}
	}
	

}
