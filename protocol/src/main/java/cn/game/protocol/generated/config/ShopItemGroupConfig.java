package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 商品组
 * 
 * 工具生成的，不要手动修改
 */
 public class ShopItemGroupConfig {

	/** 商品组id */
	public final int ID;		
	/** 商店类型 */
	public final int shopType;		
	/** 重置类型 */
	public final int resetType;		
	/** 重置参数 */
	public final int resetParam;		
	/** 商品id-1 商品id;权重| */
	public final int[][] item1;		
	/** 商品id-2 */
	public final int[][] item2;		
	/** 商品id-3 */
	public final int[][] item3;		
	/** 商品id-4 */
	public final int[][] item4;		
	/** 商品id-5 */
	public final int[][] item5;		
	/** 商品id-6 */
	public final int[][] item6;		

	public ShopItemGroupConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 商品组id
		shopType = Integer.parseInt(element.getAttribute("shopType") == null || element.getAttribute("shopType").length() == 0 ? "0"
			: element.getAttribute("shopType")); // 商店类型
		resetType = Integer.parseInt(element.getAttribute("resetType") == null || element.getAttribute("resetType").length() == 0 ? "0"
			: element.getAttribute("resetType")); // 重置类型
		resetParam = Integer.parseInt(element.getAttribute("resetParam") == null || element.getAttribute("resetParam").length() == 0 ? "0"
			: element.getAttribute("resetParam")); // 重置参数
		String item1String = element.getAttribute("item1"); // 商品id-1 商品id;权重|
		if (item1String != null && item1String.length() > 0) {
			String[] item1Strings = item1String.split("\\|"); 
			int[][] item1Temp = new int[item1Strings.length][] ; 
			for (int i = 0; i < item1Strings.length; i++) {
				String[] item1Strings2 = item1Strings[i].split(";"); 
				int[] array = new int[item1Strings2.length];
				for (int j = 0; j < item1Strings2.length; j++) {
					int temp = Integer.parseInt(item1Strings2[j]);	
					array[j] = temp;
				}
				item1Temp[i] = array;
			}
			item1 = item1Temp ;			
		} else {
			item1 = new int[][] {};
		}
		String item2String = element.getAttribute("item2"); // 商品id-2
		if (item2String != null && item2String.length() > 0) {
			String[] item2Strings = item2String.split("\\|"); 
			int[][] item2Temp = new int[item2Strings.length][] ; 
			for (int i = 0; i < item2Strings.length; i++) {
				String[] item2Strings2 = item2Strings[i].split(";"); 
				int[] array = new int[item2Strings2.length];
				for (int j = 0; j < item2Strings2.length; j++) {
					int temp = Integer.parseInt(item2Strings2[j]);	
					array[j] = temp;
				}
				item2Temp[i] = array;
			}
			item2 = item2Temp ;			
		} else {
			item2 = new int[][] {};
		}
		String item3String = element.getAttribute("item3"); // 商品id-3
		if (item3String != null && item3String.length() > 0) {
			String[] item3Strings = item3String.split("\\|"); 
			int[][] item3Temp = new int[item3Strings.length][] ; 
			for (int i = 0; i < item3Strings.length; i++) {
				String[] item3Strings2 = item3Strings[i].split(";"); 
				int[] array = new int[item3Strings2.length];
				for (int j = 0; j < item3Strings2.length; j++) {
					int temp = Integer.parseInt(item3Strings2[j]);	
					array[j] = temp;
				}
				item3Temp[i] = array;
			}
			item3 = item3Temp ;			
		} else {
			item3 = new int[][] {};
		}
		String item4String = element.getAttribute("item4"); // 商品id-4
		if (item4String != null && item4String.length() > 0) {
			String[] item4Strings = item4String.split("\\|"); 
			int[][] item4Temp = new int[item4Strings.length][] ; 
			for (int i = 0; i < item4Strings.length; i++) {
				String[] item4Strings2 = item4Strings[i].split(";"); 
				int[] array = new int[item4Strings2.length];
				for (int j = 0; j < item4Strings2.length; j++) {
					int temp = Integer.parseInt(item4Strings2[j]);	
					array[j] = temp;
				}
				item4Temp[i] = array;
			}
			item4 = item4Temp ;			
		} else {
			item4 = new int[][] {};
		}
		String item5String = element.getAttribute("item5"); // 商品id-5
		if (item5String != null && item5String.length() > 0) {
			String[] item5Strings = item5String.split("\\|"); 
			int[][] item5Temp = new int[item5Strings.length][] ; 
			for (int i = 0; i < item5Strings.length; i++) {
				String[] item5Strings2 = item5Strings[i].split(";"); 
				int[] array = new int[item5Strings2.length];
				for (int j = 0; j < item5Strings2.length; j++) {
					int temp = Integer.parseInt(item5Strings2[j]);	
					array[j] = temp;
				}
				item5Temp[i] = array;
			}
			item5 = item5Temp ;			
		} else {
			item5 = new int[][] {};
		}
		String item6String = element.getAttribute("item6"); // 商品id-6
		if (item6String != null && item6String.length() > 0) {
			String[] item6Strings = item6String.split("\\|"); 
			int[][] item6Temp = new int[item6Strings.length][] ; 
			for (int i = 0; i < item6Strings.length; i++) {
				String[] item6Strings2 = item6Strings[i].split(";"); 
				int[] array = new int[item6Strings2.length];
				for (int j = 0; j < item6Strings2.length; j++) {
					int temp = Integer.parseInt(item6Strings2[j]);	
					array[j] = temp;
				}
				item6Temp[i] = array;
			}
			item6 = item6Temp ;			
		} else {
			item6 = new int[][] {};
		}
	}
	

}
