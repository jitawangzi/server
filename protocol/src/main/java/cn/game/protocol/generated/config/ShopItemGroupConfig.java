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
	/** 商店类型 1=普通商店，所见即所得 2=随机商店，每个商品位随机（商品Id;权重） */
	public final int ShopType;		
	/** 重置类型 0=不重置 1=每几天重置1次（参数填【1=每1天重置一次】，参数填【7=每7天重置一次】，默认重置时间是：0点0分1秒） */
	public final int ResetType;		
	/** 重置参数 */
	public final int ResetParameter;		
	/** 商品id-1 商品id;权重| */
	public final int[][] ID1;		
	/** 商品id-2 */
	public final int[][] ID2;		
	/** 商品id-3 */
	public final int[][] ID3;		
	/** 商品id-4 */
	public final int[][] ID4;		
	/** 商品id-5 */
	public final int[][] ID5;		
	/** 商品id-6 */
	public final int[][] ID6;		

	public ShopItemGroupConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 商品组id
		ShopType = Integer.parseInt(element.getAttribute("ShopType") == null || element.getAttribute("ShopType").length() == 0 ? "0"
			: element.getAttribute("ShopType")); // 商店类型 1=普通商店，所见即所得 2=随机商店，每个商品位随机（商品Id;权重）
		ResetType = Integer.parseInt(element.getAttribute("ResetType") == null || element.getAttribute("ResetType").length() == 0 ? "0"
			: element.getAttribute("ResetType")); // 重置类型 0=不重置 1=每几天重置1次（参数填【1=每1天重置一次】，参数填【7=每7天重置一次】，默认重置时间是：0点0分1秒）
		ResetParameter = Integer.parseInt(element.getAttribute("ResetParameter") == null || element.getAttribute("ResetParameter").length() == 0 ? "0"
			: element.getAttribute("ResetParameter")); // 重置参数
		String ID1String = element.getAttribute("ID1"); // 商品id-1 商品id;权重|
		if (ID1String != null && ID1String.length() > 0) {
			String[] ID1Strings = ID1String.split("\\|"); 
			int[][] ID1Temp = new int[ID1Strings.length][] ; 
			for (int i = 0; i < ID1Strings.length; i++) {
				String[] ID1Strings2 = ID1Strings[i].split(";"); 
				int[] array = new int[ID1Strings2.length];
				for (int j = 0; j < ID1Strings2.length; j++) {
					int temp = Integer.parseInt(ID1Strings2[j]);	
					array[j] = temp;
				}
				ID1Temp[i] = array;
			}
			ID1 = ID1Temp ;			
		} else {
			ID1 = new int[][] {};
		}
		String ID2String = element.getAttribute("ID2"); // 商品id-2
		if (ID2String != null && ID2String.length() > 0) {
			String[] ID2Strings = ID2String.split("\\|"); 
			int[][] ID2Temp = new int[ID2Strings.length][] ; 
			for (int i = 0; i < ID2Strings.length; i++) {
				String[] ID2Strings2 = ID2Strings[i].split(";"); 
				int[] array = new int[ID2Strings2.length];
				for (int j = 0; j < ID2Strings2.length; j++) {
					int temp = Integer.parseInt(ID2Strings2[j]);	
					array[j] = temp;
				}
				ID2Temp[i] = array;
			}
			ID2 = ID2Temp ;			
		} else {
			ID2 = new int[][] {};
		}
		String ID3String = element.getAttribute("ID3"); // 商品id-3
		if (ID3String != null && ID3String.length() > 0) {
			String[] ID3Strings = ID3String.split("\\|"); 
			int[][] ID3Temp = new int[ID3Strings.length][] ; 
			for (int i = 0; i < ID3Strings.length; i++) {
				String[] ID3Strings2 = ID3Strings[i].split(";"); 
				int[] array = new int[ID3Strings2.length];
				for (int j = 0; j < ID3Strings2.length; j++) {
					int temp = Integer.parseInt(ID3Strings2[j]);	
					array[j] = temp;
				}
				ID3Temp[i] = array;
			}
			ID3 = ID3Temp ;			
		} else {
			ID3 = new int[][] {};
		}
		String ID4String = element.getAttribute("ID4"); // 商品id-4
		if (ID4String != null && ID4String.length() > 0) {
			String[] ID4Strings = ID4String.split("\\|"); 
			int[][] ID4Temp = new int[ID4Strings.length][] ; 
			for (int i = 0; i < ID4Strings.length; i++) {
				String[] ID4Strings2 = ID4Strings[i].split(";"); 
				int[] array = new int[ID4Strings2.length];
				for (int j = 0; j < ID4Strings2.length; j++) {
					int temp = Integer.parseInt(ID4Strings2[j]);	
					array[j] = temp;
				}
				ID4Temp[i] = array;
			}
			ID4 = ID4Temp ;			
		} else {
			ID4 = new int[][] {};
		}
		String ID5String = element.getAttribute("ID5"); // 商品id-5
		if (ID5String != null && ID5String.length() > 0) {
			String[] ID5Strings = ID5String.split("\\|"); 
			int[][] ID5Temp = new int[ID5Strings.length][] ; 
			for (int i = 0; i < ID5Strings.length; i++) {
				String[] ID5Strings2 = ID5Strings[i].split(";"); 
				int[] array = new int[ID5Strings2.length];
				for (int j = 0; j < ID5Strings2.length; j++) {
					int temp = Integer.parseInt(ID5Strings2[j]);	
					array[j] = temp;
				}
				ID5Temp[i] = array;
			}
			ID5 = ID5Temp ;			
		} else {
			ID5 = new int[][] {};
		}
		String ID6String = element.getAttribute("ID6"); // 商品id-6
		if (ID6String != null && ID6String.length() > 0) {
			String[] ID6Strings = ID6String.split("\\|"); 
			int[][] ID6Temp = new int[ID6Strings.length][] ; 
			for (int i = 0; i < ID6Strings.length; i++) {
				String[] ID6Strings2 = ID6Strings[i].split(";"); 
				int[] array = new int[ID6Strings2.length];
				for (int j = 0; j < ID6Strings2.length; j++) {
					int temp = Integer.parseInt(ID6Strings2[j]);	
					array[j] = temp;
				}
				ID6Temp[i] = array;
			}
			ID6 = ID6Temp ;			
		} else {
			ID6 = new int[][] {};
		}
	}
	

}
