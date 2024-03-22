package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 合成
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroConflateConfig {

	/** 合成本体ID */
	public final int ID;		
	/** 合成目标 */
	public final int[] target;		
	/** 合成类型 */
	public final int type;		
	/** 耗材类型-1 */
	public final int costType1;		
	/** 耗材-1 */
	public final int[] costParam1;		
	/** 耗材类型-2 */
	public final int costType2;		
	/** 耗材-2 */
	public final int[] costParam2;		
	/** 耗材类型-3 */
	public final int costType3;		
	/** 耗材-3 */
	public final int[] costParam3;		
	/** 万能耗材ID */
	public final int powerfulCostId;		

	public HeroConflateConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 合成本体ID
		String targetString = element.getAttribute("target"); // 合成目标
		if (targetString != null && targetString.length() > 0) {
			String[] targetStrings = targetString.split(";"); 
			int[] targetTemp = new int[targetStrings.length] ; 
			for (int i = 0; i < targetStrings.length; i++) {
				int temp = Integer.parseInt(targetStrings[i]);	
				targetTemp[i] = temp;
			}
			target = targetTemp ;			
		} else {
			target = new int[] {};
		}
		type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 合成类型
		costType1 = Integer.parseInt(element.getAttribute("costType1") == null || element.getAttribute("costType1").length() == 0 ? "0"
			: element.getAttribute("costType1")); // 耗材类型-1
		String costParam1String = element.getAttribute("costParam1"); // 耗材-1
		if (costParam1String != null && costParam1String.length() > 0) {
			String[] costParam1Strings = costParam1String.split(";"); 
			int[] costParam1Temp = new int[costParam1Strings.length] ; 
			for (int i = 0; i < costParam1Strings.length; i++) {
				int temp = Integer.parseInt(costParam1Strings[i]);	
				costParam1Temp[i] = temp;
			}
			costParam1 = costParam1Temp ;			
		} else {
			costParam1 = new int[] {};
		}
		costType2 = Integer.parseInt(element.getAttribute("costType2") == null || element.getAttribute("costType2").length() == 0 ? "0"
			: element.getAttribute("costType2")); // 耗材类型-2
		String costParam2String = element.getAttribute("costParam2"); // 耗材-2
		if (costParam2String != null && costParam2String.length() > 0) {
			String[] costParam2Strings = costParam2String.split(";"); 
			int[] costParam2Temp = new int[costParam2Strings.length] ; 
			for (int i = 0; i < costParam2Strings.length; i++) {
				int temp = Integer.parseInt(costParam2Strings[i]);	
				costParam2Temp[i] = temp;
			}
			costParam2 = costParam2Temp ;			
		} else {
			costParam2 = new int[] {};
		}
		costType3 = Integer.parseInt(element.getAttribute("costType3") == null || element.getAttribute("costType3").length() == 0 ? "0"
			: element.getAttribute("costType3")); // 耗材类型-3
		String costParam3String = element.getAttribute("costParam3"); // 耗材-3
		if (costParam3String != null && costParam3String.length() > 0) {
			String[] costParam3Strings = costParam3String.split(";"); 
			int[] costParam3Temp = new int[costParam3Strings.length] ; 
			for (int i = 0; i < costParam3Strings.length; i++) {
				int temp = Integer.parseInt(costParam3Strings[i]);	
				costParam3Temp[i] = temp;
			}
			costParam3 = costParam3Temp ;			
		} else {
			costParam3 = new int[] {};
		}
		powerfulCostId = Integer.parseInt(element.getAttribute("powerfulCostId") == null || element.getAttribute("powerfulCostId").length() == 0 ? "0"
			: element.getAttribute("powerfulCostId")); // 万能耗材ID
	}
	

}
