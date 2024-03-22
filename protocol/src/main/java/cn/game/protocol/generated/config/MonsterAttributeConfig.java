package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 怪物属性
 * 
 * 工具生成的，不要手动修改
 */
 public class MonsterAttributeConfig {

	/** 怪物ID */
	private final int ID;		
	/** 英雄职业及固定站位 1-骑士[前排1] 2-战士[前排2] 3-刺客[前排3] 4-法师[后排1] 5-牧师[后排2] 6-射手[后排3] */
	private final int Career;		
	/** 初始属性  读取AttrEffectConfig#属性Id表 */
	private final int[][] InitialAttribute;		
	/** 每级成长属性 */
	private final int[][] Growth;		

	public MonsterAttributeConfig (Element element) throws Exception {
	
		this.ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 怪物ID
		this.Career = Integer.parseInt(element.getAttribute("Career") == null || element.getAttribute("Career").length() == 0 ? "0"
			: element.getAttribute("Career")); // 英雄职业及固定站位 1-骑士[前排1] 2-战士[前排2] 3-刺客[前排3] 4-法师[后排1] 5-牧师[后排2] 6-射手[后排3]
		String InitialAttributeString = element.getAttribute("InitialAttribute"); // 初始属性  读取AttrEffectConfig#属性Id表
		if (InitialAttributeString != null && InitialAttributeString.length() > 0) {
			String[] InitialAttributeStrings = InitialAttributeString.split("\\|"); 
			int[][] InitialAttribute = new int[InitialAttributeStrings.length][] ; 
			for (int i = 0; i < InitialAttributeStrings.length; i++) {
				String[] InitialAttributeStrings2 = InitialAttributeStrings[i].split(";"); 
				int[] array = new int[InitialAttributeStrings2.length];
				for (int j = 0; j < InitialAttributeStrings2.length; j++) {
					int temp = Integer.parseInt(InitialAttributeStrings2[j]);
					array[j] = temp;
				}
				InitialAttribute[i] = array;
				
			}
			this.InitialAttribute = InitialAttribute ;			
		} else {
			this.InitialAttribute = new int[][] {};
		}
		String GrowthString = element.getAttribute("Growth"); // 每级成长属性
		if (GrowthString != null && GrowthString.length() > 0) {
			String[] GrowthStrings = GrowthString.split("\\|"); 
			int[][] Growth = new int[GrowthStrings.length][] ; 
			for (int i = 0; i < GrowthStrings.length; i++) {
				String[] GrowthStrings2 = GrowthStrings[i].split(";"); 
				int[] array = new int[GrowthStrings2.length];
				for (int j = 0; j < GrowthStrings2.length; j++) {
					int temp = Integer.parseInt(GrowthStrings2[j]);
					array[j] = temp;
				}
				Growth[i] = array;
				
			}
			this.Growth = Growth ;			
		} else {
			this.Growth = new int[][] {};
		}
	}
	
	public int getID() {
		return ID;
	}
	
	public int getCareer() {
		return Career;
	}
	
	public int[][] getInitialAttribute() {
		return InitialAttribute;
	}
	
	public int[][] getGrowth() {
		return Growth;
	}
	
}
