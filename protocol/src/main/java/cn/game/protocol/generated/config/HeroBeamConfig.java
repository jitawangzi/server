package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 范围
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroBeamConfig {

	/** StatusTypeID */
	public final int ID;		
	/** 范围类型 1-全屏 2-圆 3-矩形 4-扇形 5-线 */
	public final int RangeType;		
	/** 范围参数750*1334 1 无参数 2-半径像素 3-长，宽 4-角度;半径 5-长度 */
	public final int[] RangeParam;		
	/** 造成技能伤害公式用计算方法名字程序来封装方法实现 */
	public final String CalculateFun;		
	/** 技能伤害参数百分比组 */
	public final int[] CalculateParam;		
	/** 技能持续时间（毫秒） */
	public final int Duration;		
	/** 表现类型 1-单次多伤害 2-多次单伤害 */
	public final int ShowType;		
	/** 表现类型1参数 多次伤害时间间隔  表现类型2参数 时间间隔 */
	public final int[] r;		

	public HeroBeamConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // StatusTypeID
		RangeType = Integer.parseInt(element.getAttribute("RangeType") == null || element.getAttribute("RangeType").length() == 0 ? "0"
			: element.getAttribute("RangeType")); // 范围类型 1-全屏 2-圆 3-矩形 4-扇形 5-线
		String RangeParamString = element.getAttribute("RangeParam"); // 范围参数750*1334 1 无参数 2-半径像素 3-长，宽 4-角度;半径 5-长度
		if (RangeParamString != null && RangeParamString.length() > 0) {
			String[] RangeParamStrings = RangeParamString.split(";"); 
			int[] RangeParamTemp = new int[RangeParamStrings.length] ; 
			for (int i = 0; i < RangeParamStrings.length; i++) {
				int temp = Integer.parseInt(RangeParamStrings[i]);	
				RangeParamTemp[i] = temp;
			}
			RangeParam = RangeParamTemp ;			
		} else {
			RangeParam = new int[] {};
		}
		CalculateFun = element.getAttribute("CalculateFun"); // 造成技能伤害公式用计算方法名字程序来封装方法实现
		String CalculateParamString = element.getAttribute("CalculateParam"); // 技能伤害参数百分比组
		if (CalculateParamString != null && CalculateParamString.length() > 0) {
			String[] CalculateParamStrings = CalculateParamString.split(";"); 
			int[] CalculateParamTemp = new int[CalculateParamStrings.length] ; 
			for (int i = 0; i < CalculateParamStrings.length; i++) {
				int temp = Integer.parseInt(CalculateParamStrings[i]);	
				CalculateParamTemp[i] = temp;
			}
			CalculateParam = CalculateParamTemp ;			
		} else {
			CalculateParam = new int[] {};
		}
		Duration = Integer.parseInt(element.getAttribute("Duration") == null || element.getAttribute("Duration").length() == 0 ? "0"
			: element.getAttribute("Duration")); // 技能持续时间（毫秒）
		ShowType = Integer.parseInt(element.getAttribute("ShowType") == null || element.getAttribute("ShowType").length() == 0 ? "0"
			: element.getAttribute("ShowType")); // 表现类型 1-单次多伤害 2-多次单伤害
		String rString = element.getAttribute("r"); // 表现类型1参数 多次伤害时间间隔  表现类型2参数 时间间隔
		if (rString != null && rString.length() > 0) {
			String[] rStrings = rString.split(";"); 
			int[] rTemp = new int[rStrings.length] ; 
			for (int i = 0; i < rStrings.length; i++) {
				int temp = Integer.parseInt(rStrings[i]);	
				rTemp[i] = temp;
			}
			r = rTemp ;			
		} else {
			r = new int[] {};
		}
	}
	

}
