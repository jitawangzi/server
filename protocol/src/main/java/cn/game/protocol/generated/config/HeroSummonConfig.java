package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 召唤
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroSummonConfig {

	/** ID */
	public final int ID;		
	/** 召唤物的ID可能多个数组 */
	public final int[] SummonedID;		
	/** 召唤物出生便宜位置位置 [][x,y] */
	public final int[][] PosOffset;		
	/** 主人死亡时，召唤物是否消失 1-消失 0-不消失 */
	public final int DispearAsOwner;		
	/** 继承属性ID 组合 */
	public final int[] inheritAttribute;		
	/** 召唤物属性继承比例 (填写的是万分比) */
	public final int[] inheritParam;		
	/** 存活时间 (单位:毫秒) (召唤物存活不受时间影响时，填0) */
	public final int[] LiveTime;		

	public HeroSummonConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		String SummonedIDString = element.getAttribute("SummonedID"); // 召唤物的ID可能多个数组
		if (SummonedIDString != null && SummonedIDString.length() > 0) {
			String[] SummonedIDStrings = SummonedIDString.split(";"); 
			int[] SummonedIDTemp = new int[SummonedIDStrings.length] ; 
			for (int i = 0; i < SummonedIDStrings.length; i++) {
				int temp = Integer.parseInt(SummonedIDStrings[i]);	
				SummonedIDTemp[i] = temp;
			}
			SummonedID = SummonedIDTemp ;			
		} else {
			SummonedID = new int[] {};
		}
		String PosOffsetString = element.getAttribute("PosOffset"); // 召唤物出生便宜位置位置 [][x,y]
		if (PosOffsetString != null && PosOffsetString.length() > 0) {
			String[] PosOffsetStrings = PosOffsetString.split("\\|"); 
			int[][] PosOffsetTemp = new int[PosOffsetStrings.length][] ; 
			for (int i = 0; i < PosOffsetStrings.length; i++) {
				String[] PosOffsetStrings2 = PosOffsetStrings[i].split(";"); 
				int[] array = new int[PosOffsetStrings2.length];
				for (int j = 0; j < PosOffsetStrings2.length; j++) {
					int temp = Integer.parseInt(PosOffsetStrings2[j]);	
					array[j] = temp;
				}
				PosOffsetTemp[i] = array;
			}
			PosOffset = PosOffsetTemp ;			
		} else {
			PosOffset = new int[][] {};
		}
		DispearAsOwner = Integer.parseInt(element.getAttribute("DispearAsOwner") == null || element.getAttribute("DispearAsOwner").length() == 0 ? "0"
			: element.getAttribute("DispearAsOwner")); // 主人死亡时，召唤物是否消失 1-消失 0-不消失
		String inheritAttributeString = element.getAttribute("inheritAttribute"); // 继承属性ID 组合
		if (inheritAttributeString != null && inheritAttributeString.length() > 0) {
			String[] inheritAttributeStrings = inheritAttributeString.split(";"); 
			int[] inheritAttributeTemp = new int[inheritAttributeStrings.length] ; 
			for (int i = 0; i < inheritAttributeStrings.length; i++) {
				int temp = Integer.parseInt(inheritAttributeStrings[i]);	
				inheritAttributeTemp[i] = temp;
			}
			inheritAttribute = inheritAttributeTemp ;			
		} else {
			inheritAttribute = new int[] {};
		}
		String inheritParamString = element.getAttribute("inheritParam"); // 召唤物属性继承比例 (填写的是万分比)
		if (inheritParamString != null && inheritParamString.length() > 0) {
			String[] inheritParamStrings = inheritParamString.split(";"); 
			int[] inheritParamTemp = new int[inheritParamStrings.length] ; 
			for (int i = 0; i < inheritParamStrings.length; i++) {
				int temp = Integer.parseInt(inheritParamStrings[i]);	
				inheritParamTemp[i] = temp;
			}
			inheritParam = inheritParamTemp ;			
		} else {
			inheritParam = new int[] {};
		}
		String LiveTimeString = element.getAttribute("LiveTime"); // 存活时间 (单位:毫秒) (召唤物存活不受时间影响时，填0)
		if (LiveTimeString != null && LiveTimeString.length() > 0) {
			String[] LiveTimeStrings = LiveTimeString.split(";"); 
			int[] LiveTimeTemp = new int[LiveTimeStrings.length] ; 
			for (int i = 0; i < LiveTimeStrings.length; i++) {
				int temp = Integer.parseInt(LiveTimeStrings[i]);	
				LiveTimeTemp[i] = temp;
			}
			LiveTime = LiveTimeTemp ;			
		} else {
			LiveTime = new int[] {};
		}
	}
	

}
