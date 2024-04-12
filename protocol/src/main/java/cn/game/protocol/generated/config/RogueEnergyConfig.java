package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 能量控制
 * 
 * 工具生成的，不要手动修改
 */
 public class RogueEnergyConfig {

	/** ID */
	public final int ID;		
	/** 能量触发组ID */
	public final int EnergyTriggerGroup;		
	/** 等级 */
	public final int Level;		
	/** 所需能量 */
	public final int Energy;		
	/** 肉鸽类型 */
	public final int RogeType;		
	/** 权重分配 */
	public final int[][] RogeWeight;		

	public RogueEnergyConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		EnergyTriggerGroup = Integer.parseInt(element.getAttribute("EnergyTriggerGroup") == null || element.getAttribute("EnergyTriggerGroup").length() == 0 ? "0"
			: element.getAttribute("EnergyTriggerGroup")); // 能量触发组ID
		Level = Integer.parseInt(element.getAttribute("Level") == null || element.getAttribute("Level").length() == 0 ? "0"
			: element.getAttribute("Level")); // 等级
		Energy = Integer.parseInt(element.getAttribute("Energy") == null || element.getAttribute("Energy").length() == 0 ? "0"
			: element.getAttribute("Energy")); // 所需能量
		RogeType = Integer.parseInt(element.getAttribute("RogeType") == null || element.getAttribute("RogeType").length() == 0 ? "0"
			: element.getAttribute("RogeType")); // 肉鸽类型
		String RogeWeightString = element.getAttribute("RogeWeight"); // 权重分配
		if (RogeWeightString != null && RogeWeightString.length() > 0) {
			String[] RogeWeightStrings = RogeWeightString.split("\\|"); 
			int[][] RogeWeightTemp = new int[RogeWeightStrings.length][] ; 
			for (int i = 0; i < RogeWeightStrings.length; i++) {
				String[] RogeWeightStrings2 = RogeWeightStrings[i].split(";"); 
				int[] array = new int[RogeWeightStrings2.length];
				for (int j = 0; j < RogeWeightStrings2.length; j++) {
					int temp = Integer.parseInt(RogeWeightStrings2[j]);	
					array[j] = temp;
				}
				RogeWeightTemp[i] = array;
			}
			RogeWeight = RogeWeightTemp ;			
		} else {
			RogeWeight = new int[][] {};
		}
	}
	

}
