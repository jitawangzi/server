package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 随机奖励
 * 
 * 工具生成的，不要手动修改
 */
 public class RandomAwardConfig {

	/** 奖励ID */
	private final int ID;		
	/** 随机物品个数  每次在所有随机的物品总个数中，随机出来的个数 */
	private final int[] RandomNumber;		
	/** 是否每次随机出的物品可重复 1-是 0-否 */
	private final int[] RandomRepetition;		
	/** 随机物品ID组   物品id1,...,物品idN */
	private final int[] RandItem;		
	/** 随机物品 最小数量min  物品id1个数min,...,物品idN个数min */
	private final int[] RandItemMinCount;		
	/** 随机物品 最大数量max  物品id1个数max,...,物品idN个数max  数量不做随机的则与前列写相同数值 */
	private final int[] RandItemMaxCount;		
	/** 随机物品 权重 */
	private final int[] RandItemWeight;		

	public RandomAwardConfig (Element element) throws Exception {
	
		this.ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 奖励ID
		String RandomNumberString = element.getAttribute("RandomNumber"); // 随机物品个数  每次在所有随机的物品总个数中，随机出来的个数
		if (RandomNumberString != null && RandomNumberString.length() > 0) {
			String[] RandomNumberStrings = RandomNumberString.split(";"); 
			int[] RandomNumber = new int[RandomNumberStrings.length] ; 
			for (int i = 0; i < RandomNumberStrings.length; i++) {
				int temp = Integer.parseInt(RandomNumberStrings[i]);
				RandomNumber[i] = temp;
			}
			this.RandomNumber = RandomNumber ;			
		} else {
			this.RandomNumber = new int[] {};
		}
		String RandomRepetitionString = element.getAttribute("RandomRepetition"); // 是否每次随机出的物品可重复 1-是 0-否
		if (RandomRepetitionString != null && RandomRepetitionString.length() > 0) {
			String[] RandomRepetitionStrings = RandomRepetitionString.split(";"); 
			int[] RandomRepetition = new int[RandomRepetitionStrings.length] ; 
			for (int i = 0; i < RandomRepetitionStrings.length; i++) {
				int temp = Integer.parseInt(RandomRepetitionStrings[i]);
				RandomRepetition[i] = temp;
			}
			this.RandomRepetition = RandomRepetition ;			
		} else {
			this.RandomRepetition = new int[] {};
		}
		String RandItemString = element.getAttribute("RandItem"); // 随机物品ID组   物品id1,...,物品idN
		if (RandItemString != null && RandItemString.length() > 0) {
			String[] RandItemStrings = RandItemString.split(";"); 
			int[] RandItem = new int[RandItemStrings.length] ; 
			for (int i = 0; i < RandItemStrings.length; i++) {
				int temp = Integer.parseInt(RandItemStrings[i]);
				RandItem[i] = temp;
			}
			this.RandItem = RandItem ;			
		} else {
			this.RandItem = new int[] {};
		}
		String RandItemMinCountString = element.getAttribute("RandItemMinCount"); // 随机物品 最小数量min  物品id1个数min,...,物品idN个数min
		if (RandItemMinCountString != null && RandItemMinCountString.length() > 0) {
			String[] RandItemMinCountStrings = RandItemMinCountString.split(";"); 
			int[] RandItemMinCount = new int[RandItemMinCountStrings.length] ; 
			for (int i = 0; i < RandItemMinCountStrings.length; i++) {
				int temp = Integer.parseInt(RandItemMinCountStrings[i]);
				RandItemMinCount[i] = temp;
			}
			this.RandItemMinCount = RandItemMinCount ;			
		} else {
			this.RandItemMinCount = new int[] {};
		}
		String RandItemMaxCountString = element.getAttribute("RandItemMaxCount"); // 随机物品 最大数量max  物品id1个数max,...,物品idN个数max  数量不做随机的则与前列写相同数值
		if (RandItemMaxCountString != null && RandItemMaxCountString.length() > 0) {
			String[] RandItemMaxCountStrings = RandItemMaxCountString.split(";"); 
			int[] RandItemMaxCount = new int[RandItemMaxCountStrings.length] ; 
			for (int i = 0; i < RandItemMaxCountStrings.length; i++) {
				int temp = Integer.parseInt(RandItemMaxCountStrings[i]);
				RandItemMaxCount[i] = temp;
			}
			this.RandItemMaxCount = RandItemMaxCount ;			
		} else {
			this.RandItemMaxCount = new int[] {};
		}
		String RandItemWeightString = element.getAttribute("RandItemWeight"); // 随机物品 权重
		if (RandItemWeightString != null && RandItemWeightString.length() > 0) {
			String[] RandItemWeightStrings = RandItemWeightString.split(";"); 
			int[] RandItemWeight = new int[RandItemWeightStrings.length] ; 
			for (int i = 0; i < RandItemWeightStrings.length; i++) {
				int temp = Integer.parseInt(RandItemWeightStrings[i]);
				RandItemWeight[i] = temp;
			}
			this.RandItemWeight = RandItemWeight ;			
		} else {
			this.RandItemWeight = new int[] {};
		}
	}
	
	public int getID() {
		return ID;
	}
	
	public int[] getRandomNumber() {
		return RandomNumber;
	}
	
	public int[] getRandomRepetition() {
		return RandomRepetition;
	}
	
	public int[] getRandItem() {
		return RandItem;
	}
	
	public int[] getRandItemMinCount() {
		return RandItemMinCount;
	}
	
	public int[] getRandItemMaxCount() {
		return RandItemMaxCount;
	}
	
	public int[] getRandItemWeight() {
		return RandItemWeight;
	}
	
}
