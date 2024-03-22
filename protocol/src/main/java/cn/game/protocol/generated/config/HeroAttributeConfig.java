package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 英雄属性
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroAttributeConfig {

	/** 属性ID */
	public final int ID;		
	/** 初始属性 属性ID；属性值 */
	public final int[][] InitialAttribute;		
	/** 每级成长属性属性 属性ID；属性增量 */
	public final int[][] Growth;		
	/** 品质属性 突破所带来的属性提升 属性ID；属性值 */
	public final int[][] QualityAttribute;		
	/** 0星小进度个数  999表示无需升级 */
	public final int StarProgressBar0;		
	/** 0星进度加成  每小进度加成 */
	public final int[][] StarProgressAttribute0;		
	/** 0星永久属性加成  属性id;数值 */
	public final int[][] StarPermanentAttribute0;		
	/** 1星小进度个数  999表示无需升级 */
	public final int StarProgressBar1;		
	/** 1星进度加成  每小进度加成 */
	public final int[][] StarProgressAttribute1;		
	/** 1星永久属性加成  属性id;数值 */
	public final int[][] StarPermanentAttribute1;		
	/** 2星小进度个数  999表示无需升级 */
	public final int StarProgressBar2;		
	/** 2星进度加成  每小进度加成 */
	public final int[][] StarProgressAttribute2;		
	/** 2星永久属性加成  属性id;数值 */
	public final int[][] StarPermanentAttribute2;		
	/** 3星小进度个数  999表示无需升级 */
	public final int StarProgressBar3;		
	/** 3星进度加成  每小进度加成 */
	public final int[][] StarProgressAttribute3;		
	/** 3星永久属性加成  属性id;数值 */
	public final int[][] StarPermanentAttribute3;		
	/** 4星小进度个数  999表示无需升级 */
	public final int StarProgressBar4;		
	/** 4星进度加成  每小进度加成 */
	public final int[][] StarProgressAttribute4;		
	/** 4星永久属性加成  属性id;数值 */
	public final int[][] StarPermanentAttribute4;		
	/** 5星小进度个数  999表示无需升级 */
	public final int StarProgressBar5;		
	/** 5星进度加成  每小进度加成 */
	public final int[][] StarProgressAttribute5;		
	/** 5星永久属性加成  属性id;数值 */
	public final int[][] StarPermanentAttribute5;		

	public HeroAttributeConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 属性ID
		String InitialAttributeString = element.getAttribute("InitialAttribute"); // 初始属性 属性ID；属性值
		if (InitialAttributeString != null && InitialAttributeString.length() > 0) {
			String[] InitialAttributeStrings = InitialAttributeString.split("\\|"); 
			int[][] InitialAttributeTemp = new int[InitialAttributeStrings.length][] ; 
			for (int i = 0; i < InitialAttributeStrings.length; i++) {
				String[] InitialAttributeStrings2 = InitialAttributeStrings[i].split(";"); 
				int[] array = new int[InitialAttributeStrings2.length];
				for (int j = 0; j < InitialAttributeStrings2.length; j++) {
					int temp = Integer.parseInt(InitialAttributeStrings2[j]);	
					array[j] = temp;
				}
				InitialAttributeTemp[i] = array;
			}
			InitialAttribute = InitialAttributeTemp ;			
		} else {
			InitialAttribute = new int[][] {};
		}
		String GrowthString = element.getAttribute("Growth"); // 每级成长属性属性 属性ID；属性增量
		if (GrowthString != null && GrowthString.length() > 0) {
			String[] GrowthStrings = GrowthString.split("\\|"); 
			int[][] GrowthTemp = new int[GrowthStrings.length][] ; 
			for (int i = 0; i < GrowthStrings.length; i++) {
				String[] GrowthStrings2 = GrowthStrings[i].split(";"); 
				int[] array = new int[GrowthStrings2.length];
				for (int j = 0; j < GrowthStrings2.length; j++) {
					int temp = Integer.parseInt(GrowthStrings2[j]);	
					array[j] = temp;
				}
				GrowthTemp[i] = array;
			}
			Growth = GrowthTemp ;			
		} else {
			Growth = new int[][] {};
		}
		String QualityAttributeString = element.getAttribute("QualityAttribute"); // 品质属性 突破所带来的属性提升 属性ID；属性值
		if (QualityAttributeString != null && QualityAttributeString.length() > 0) {
			String[] QualityAttributeStrings = QualityAttributeString.split("\\|"); 
			int[][] QualityAttributeTemp = new int[QualityAttributeStrings.length][] ; 
			for (int i = 0; i < QualityAttributeStrings.length; i++) {
				String[] QualityAttributeStrings2 = QualityAttributeStrings[i].split(";"); 
				int[] array = new int[QualityAttributeStrings2.length];
				for (int j = 0; j < QualityAttributeStrings2.length; j++) {
					int temp = Integer.parseInt(QualityAttributeStrings2[j]);	
					array[j] = temp;
				}
				QualityAttributeTemp[i] = array;
			}
			QualityAttribute = QualityAttributeTemp ;			
		} else {
			QualityAttribute = new int[][] {};
		}
		StarProgressBar0 = Integer.parseInt(element.getAttribute("StarProgressBar0") == null || element.getAttribute("StarProgressBar0").length() == 0 ? "0"
			: element.getAttribute("StarProgressBar0")); // 0星小进度个数  999表示无需升级
		String StarProgressAttribute0String = element.getAttribute("StarProgressAttribute0"); // 0星进度加成  每小进度加成
		if (StarProgressAttribute0String != null && StarProgressAttribute0String.length() > 0) {
			String[] StarProgressAttribute0Strings = StarProgressAttribute0String.split("\\|"); 
			int[][] StarProgressAttribute0Temp = new int[StarProgressAttribute0Strings.length][] ; 
			for (int i = 0; i < StarProgressAttribute0Strings.length; i++) {
				String[] StarProgressAttribute0Strings2 = StarProgressAttribute0Strings[i].split(";"); 
				int[] array = new int[StarProgressAttribute0Strings2.length];
				for (int j = 0; j < StarProgressAttribute0Strings2.length; j++) {
					int temp = Integer.parseInt(StarProgressAttribute0Strings2[j]);	
					array[j] = temp;
				}
				StarProgressAttribute0Temp[i] = array;
			}
			StarProgressAttribute0 = StarProgressAttribute0Temp ;			
		} else {
			StarProgressAttribute0 = new int[][] {};
		}
		String StarPermanentAttribute0String = element.getAttribute("StarPermanentAttribute0"); // 0星永久属性加成  属性id;数值
		if (StarPermanentAttribute0String != null && StarPermanentAttribute0String.length() > 0) {
			String[] StarPermanentAttribute0Strings = StarPermanentAttribute0String.split("\\|"); 
			int[][] StarPermanentAttribute0Temp = new int[StarPermanentAttribute0Strings.length][] ; 
			for (int i = 0; i < StarPermanentAttribute0Strings.length; i++) {
				String[] StarPermanentAttribute0Strings2 = StarPermanentAttribute0Strings[i].split(";"); 
				int[] array = new int[StarPermanentAttribute0Strings2.length];
				for (int j = 0; j < StarPermanentAttribute0Strings2.length; j++) {
					int temp = Integer.parseInt(StarPermanentAttribute0Strings2[j]);	
					array[j] = temp;
				}
				StarPermanentAttribute0Temp[i] = array;
			}
			StarPermanentAttribute0 = StarPermanentAttribute0Temp ;			
		} else {
			StarPermanentAttribute0 = new int[][] {};
		}
		StarProgressBar1 = Integer.parseInt(element.getAttribute("StarProgressBar1") == null || element.getAttribute("StarProgressBar1").length() == 0 ? "0"
			: element.getAttribute("StarProgressBar1")); // 1星小进度个数  999表示无需升级
		String StarProgressAttribute1String = element.getAttribute("StarProgressAttribute1"); // 1星进度加成  每小进度加成
		if (StarProgressAttribute1String != null && StarProgressAttribute1String.length() > 0) {
			String[] StarProgressAttribute1Strings = StarProgressAttribute1String.split("\\|"); 
			int[][] StarProgressAttribute1Temp = new int[StarProgressAttribute1Strings.length][] ; 
			for (int i = 0; i < StarProgressAttribute1Strings.length; i++) {
				String[] StarProgressAttribute1Strings2 = StarProgressAttribute1Strings[i].split(";"); 
				int[] array = new int[StarProgressAttribute1Strings2.length];
				for (int j = 0; j < StarProgressAttribute1Strings2.length; j++) {
					int temp = Integer.parseInt(StarProgressAttribute1Strings2[j]);	
					array[j] = temp;
				}
				StarProgressAttribute1Temp[i] = array;
			}
			StarProgressAttribute1 = StarProgressAttribute1Temp ;			
		} else {
			StarProgressAttribute1 = new int[][] {};
		}
		String StarPermanentAttribute1String = element.getAttribute("StarPermanentAttribute1"); // 1星永久属性加成  属性id;数值
		if (StarPermanentAttribute1String != null && StarPermanentAttribute1String.length() > 0) {
			String[] StarPermanentAttribute1Strings = StarPermanentAttribute1String.split("\\|"); 
			int[][] StarPermanentAttribute1Temp = new int[StarPermanentAttribute1Strings.length][] ; 
			for (int i = 0; i < StarPermanentAttribute1Strings.length; i++) {
				String[] StarPermanentAttribute1Strings2 = StarPermanentAttribute1Strings[i].split(";"); 
				int[] array = new int[StarPermanentAttribute1Strings2.length];
				for (int j = 0; j < StarPermanentAttribute1Strings2.length; j++) {
					int temp = Integer.parseInt(StarPermanentAttribute1Strings2[j]);	
					array[j] = temp;
				}
				StarPermanentAttribute1Temp[i] = array;
			}
			StarPermanentAttribute1 = StarPermanentAttribute1Temp ;			
		} else {
			StarPermanentAttribute1 = new int[][] {};
		}
		StarProgressBar2 = Integer.parseInt(element.getAttribute("StarProgressBar2") == null || element.getAttribute("StarProgressBar2").length() == 0 ? "0"
			: element.getAttribute("StarProgressBar2")); // 2星小进度个数  999表示无需升级
		String StarProgressAttribute2String = element.getAttribute("StarProgressAttribute2"); // 2星进度加成  每小进度加成
		if (StarProgressAttribute2String != null && StarProgressAttribute2String.length() > 0) {
			String[] StarProgressAttribute2Strings = StarProgressAttribute2String.split("\\|"); 
			int[][] StarProgressAttribute2Temp = new int[StarProgressAttribute2Strings.length][] ; 
			for (int i = 0; i < StarProgressAttribute2Strings.length; i++) {
				String[] StarProgressAttribute2Strings2 = StarProgressAttribute2Strings[i].split(";"); 
				int[] array = new int[StarProgressAttribute2Strings2.length];
				for (int j = 0; j < StarProgressAttribute2Strings2.length; j++) {
					int temp = Integer.parseInt(StarProgressAttribute2Strings2[j]);	
					array[j] = temp;
				}
				StarProgressAttribute2Temp[i] = array;
			}
			StarProgressAttribute2 = StarProgressAttribute2Temp ;			
		} else {
			StarProgressAttribute2 = new int[][] {};
		}
		String StarPermanentAttribute2String = element.getAttribute("StarPermanentAttribute2"); // 2星永久属性加成  属性id;数值
		if (StarPermanentAttribute2String != null && StarPermanentAttribute2String.length() > 0) {
			String[] StarPermanentAttribute2Strings = StarPermanentAttribute2String.split("\\|"); 
			int[][] StarPermanentAttribute2Temp = new int[StarPermanentAttribute2Strings.length][] ; 
			for (int i = 0; i < StarPermanentAttribute2Strings.length; i++) {
				String[] StarPermanentAttribute2Strings2 = StarPermanentAttribute2Strings[i].split(";"); 
				int[] array = new int[StarPermanentAttribute2Strings2.length];
				for (int j = 0; j < StarPermanentAttribute2Strings2.length; j++) {
					int temp = Integer.parseInt(StarPermanentAttribute2Strings2[j]);	
					array[j] = temp;
				}
				StarPermanentAttribute2Temp[i] = array;
			}
			StarPermanentAttribute2 = StarPermanentAttribute2Temp ;			
		} else {
			StarPermanentAttribute2 = new int[][] {};
		}
		StarProgressBar3 = Integer.parseInt(element.getAttribute("StarProgressBar3") == null || element.getAttribute("StarProgressBar3").length() == 0 ? "0"
			: element.getAttribute("StarProgressBar3")); // 3星小进度个数  999表示无需升级
		String StarProgressAttribute3String = element.getAttribute("StarProgressAttribute3"); // 3星进度加成  每小进度加成
		if (StarProgressAttribute3String != null && StarProgressAttribute3String.length() > 0) {
			String[] StarProgressAttribute3Strings = StarProgressAttribute3String.split("\\|"); 
			int[][] StarProgressAttribute3Temp = new int[StarProgressAttribute3Strings.length][] ; 
			for (int i = 0; i < StarProgressAttribute3Strings.length; i++) {
				String[] StarProgressAttribute3Strings2 = StarProgressAttribute3Strings[i].split(";"); 
				int[] array = new int[StarProgressAttribute3Strings2.length];
				for (int j = 0; j < StarProgressAttribute3Strings2.length; j++) {
					int temp = Integer.parseInt(StarProgressAttribute3Strings2[j]);	
					array[j] = temp;
				}
				StarProgressAttribute3Temp[i] = array;
			}
			StarProgressAttribute3 = StarProgressAttribute3Temp ;			
		} else {
			StarProgressAttribute3 = new int[][] {};
		}
		String StarPermanentAttribute3String = element.getAttribute("StarPermanentAttribute3"); // 3星永久属性加成  属性id;数值
		if (StarPermanentAttribute3String != null && StarPermanentAttribute3String.length() > 0) {
			String[] StarPermanentAttribute3Strings = StarPermanentAttribute3String.split("\\|"); 
			int[][] StarPermanentAttribute3Temp = new int[StarPermanentAttribute3Strings.length][] ; 
			for (int i = 0; i < StarPermanentAttribute3Strings.length; i++) {
				String[] StarPermanentAttribute3Strings2 = StarPermanentAttribute3Strings[i].split(";"); 
				int[] array = new int[StarPermanentAttribute3Strings2.length];
				for (int j = 0; j < StarPermanentAttribute3Strings2.length; j++) {
					int temp = Integer.parseInt(StarPermanentAttribute3Strings2[j]);	
					array[j] = temp;
				}
				StarPermanentAttribute3Temp[i] = array;
			}
			StarPermanentAttribute3 = StarPermanentAttribute3Temp ;			
		} else {
			StarPermanentAttribute3 = new int[][] {};
		}
		StarProgressBar4 = Integer.parseInt(element.getAttribute("StarProgressBar4") == null || element.getAttribute("StarProgressBar4").length() == 0 ? "0"
			: element.getAttribute("StarProgressBar4")); // 4星小进度个数  999表示无需升级
		String StarProgressAttribute4String = element.getAttribute("StarProgressAttribute4"); // 4星进度加成  每小进度加成
		if (StarProgressAttribute4String != null && StarProgressAttribute4String.length() > 0) {
			String[] StarProgressAttribute4Strings = StarProgressAttribute4String.split("\\|"); 
			int[][] StarProgressAttribute4Temp = new int[StarProgressAttribute4Strings.length][] ; 
			for (int i = 0; i < StarProgressAttribute4Strings.length; i++) {
				String[] StarProgressAttribute4Strings2 = StarProgressAttribute4Strings[i].split(";"); 
				int[] array = new int[StarProgressAttribute4Strings2.length];
				for (int j = 0; j < StarProgressAttribute4Strings2.length; j++) {
					int temp = Integer.parseInt(StarProgressAttribute4Strings2[j]);	
					array[j] = temp;
				}
				StarProgressAttribute4Temp[i] = array;
			}
			StarProgressAttribute4 = StarProgressAttribute4Temp ;			
		} else {
			StarProgressAttribute4 = new int[][] {};
		}
		String StarPermanentAttribute4String = element.getAttribute("StarPermanentAttribute4"); // 4星永久属性加成  属性id;数值
		if (StarPermanentAttribute4String != null && StarPermanentAttribute4String.length() > 0) {
			String[] StarPermanentAttribute4Strings = StarPermanentAttribute4String.split("\\|"); 
			int[][] StarPermanentAttribute4Temp = new int[StarPermanentAttribute4Strings.length][] ; 
			for (int i = 0; i < StarPermanentAttribute4Strings.length; i++) {
				String[] StarPermanentAttribute4Strings2 = StarPermanentAttribute4Strings[i].split(";"); 
				int[] array = new int[StarPermanentAttribute4Strings2.length];
				for (int j = 0; j < StarPermanentAttribute4Strings2.length; j++) {
					int temp = Integer.parseInt(StarPermanentAttribute4Strings2[j]);	
					array[j] = temp;
				}
				StarPermanentAttribute4Temp[i] = array;
			}
			StarPermanentAttribute4 = StarPermanentAttribute4Temp ;			
		} else {
			StarPermanentAttribute4 = new int[][] {};
		}
		StarProgressBar5 = Integer.parseInt(element.getAttribute("StarProgressBar5") == null || element.getAttribute("StarProgressBar5").length() == 0 ? "0"
			: element.getAttribute("StarProgressBar5")); // 5星小进度个数  999表示无需升级
		String StarProgressAttribute5String = element.getAttribute("StarProgressAttribute5"); // 5星进度加成  每小进度加成
		if (StarProgressAttribute5String != null && StarProgressAttribute5String.length() > 0) {
			String[] StarProgressAttribute5Strings = StarProgressAttribute5String.split("\\|"); 
			int[][] StarProgressAttribute5Temp = new int[StarProgressAttribute5Strings.length][] ; 
			for (int i = 0; i < StarProgressAttribute5Strings.length; i++) {
				String[] StarProgressAttribute5Strings2 = StarProgressAttribute5Strings[i].split(";"); 
				int[] array = new int[StarProgressAttribute5Strings2.length];
				for (int j = 0; j < StarProgressAttribute5Strings2.length; j++) {
					int temp = Integer.parseInt(StarProgressAttribute5Strings2[j]);	
					array[j] = temp;
				}
				StarProgressAttribute5Temp[i] = array;
			}
			StarProgressAttribute5 = StarProgressAttribute5Temp ;			
		} else {
			StarProgressAttribute5 = new int[][] {};
		}
		String StarPermanentAttribute5String = element.getAttribute("StarPermanentAttribute5"); // 5星永久属性加成  属性id;数值
		if (StarPermanentAttribute5String != null && StarPermanentAttribute5String.length() > 0) {
			String[] StarPermanentAttribute5Strings = StarPermanentAttribute5String.split("\\|"); 
			int[][] StarPermanentAttribute5Temp = new int[StarPermanentAttribute5Strings.length][] ; 
			for (int i = 0; i < StarPermanentAttribute5Strings.length; i++) {
				String[] StarPermanentAttribute5Strings2 = StarPermanentAttribute5Strings[i].split(";"); 
				int[] array = new int[StarPermanentAttribute5Strings2.length];
				for (int j = 0; j < StarPermanentAttribute5Strings2.length; j++) {
					int temp = Integer.parseInt(StarPermanentAttribute5Strings2[j]);	
					array[j] = temp;
				}
				StarPermanentAttribute5Temp[i] = array;
			}
			StarPermanentAttribute5 = StarPermanentAttribute5Temp ;			
		} else {
			StarPermanentAttribute5 = new int[][] {};
		}
	}
	

}
