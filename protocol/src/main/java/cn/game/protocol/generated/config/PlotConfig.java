package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 剧情
 * 
 * 工具生成的，不要手动修改
 */
 public class PlotConfig {

	/** ID */
	public final int ID;		
	/** 下一引导 */
	public final int Next;		
	/** 引导条件 1=刚进入战斗；战役ID */
	public final int[] Condition;		
	/** 引导提示界面 */
	public final int Tip;		
	/** 面板角色资源 不填则表示只有面板，无需角色图，一般用在非强制引导中 */
	public final String Res;		
	/** 面板角色名称 */
	public final String Name;		
	/** 面板显示内容 */
	public final String Content;		
	/** 是否遮黑 0=不遮黑，一般用于非强制引导 1=遮黑，用镂空控制 */
	public final boolean Mask;		
	/** 面板镂空形状 1=圆形 2=矩形 */
	public final int HollowShape;		
	/** 面板区域坐标 1=圆心坐标，半径 2=两点坐标 */
	public final int[][] AreaCoordinates;		
	/** 闪烁资源 1=点击大手+相应光圈特效 2=流光特效 */
	public final int FlashingRes;		
	/** 闪烁位置 */
	public final int[] FlashingPosition;		
	/** 是否强制 1=强制引导必须按照引导点下去 0=非强制引导可以直接点击任意位置消除掉 */
	public final boolean Compulsion;		

	public PlotConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		Next = Integer.parseInt(element.getAttribute("Next") == null || element.getAttribute("Next").length() == 0 ? "0"
			: element.getAttribute("Next")); // 下一引导
		String ConditionString = element.getAttribute("Condition"); // 引导条件 1=刚进入战斗；战役ID
		if (ConditionString != null && ConditionString.length() > 0) {
			String[] ConditionStrings = ConditionString.split(";"); 
			int[] ConditionTemp = new int[ConditionStrings.length] ; 
			for (int i = 0; i < ConditionStrings.length; i++) {
				int temp = Integer.parseInt(ConditionStrings[i]);	
				ConditionTemp[i] = temp;
			}
			Condition = ConditionTemp ;			
		} else {
			Condition = new int[] {};
		}
		Tip = Integer.parseInt(element.getAttribute("Tip") == null || element.getAttribute("Tip").length() == 0 ? "0"
			: element.getAttribute("Tip")); // 引导提示界面
		Res = element.getAttribute("Res"); // 面板角色资源 不填则表示只有面板，无需角色图，一般用在非强制引导中
		Name = element.getAttribute("Name"); // 面板角色名称
		Content = element.getAttribute("Content"); // 面板显示内容
		Mask = Boolean.parseBoolean(element.getAttribute("Mask") == null || element.getAttribute("Mask").length() == 0 ? "false"
			: element.getAttribute("Mask")); // 是否遮黑 0=不遮黑，一般用于非强制引导 1=遮黑，用镂空控制
		HollowShape = Integer.parseInt(element.getAttribute("HollowShape") == null || element.getAttribute("HollowShape").length() == 0 ? "0"
			: element.getAttribute("HollowShape")); // 面板镂空形状 1=圆形 2=矩形
		String AreaCoordinatesString = element.getAttribute("AreaCoordinates"); // 面板区域坐标 1=圆心坐标，半径 2=两点坐标
		if (AreaCoordinatesString != null && AreaCoordinatesString.length() > 0) {
			String[] AreaCoordinatesStrings = AreaCoordinatesString.split("\\|"); 
			int[][] AreaCoordinatesTemp = new int[AreaCoordinatesStrings.length][] ; 
			for (int i = 0; i < AreaCoordinatesStrings.length; i++) {
				String[] AreaCoordinatesStrings2 = AreaCoordinatesStrings[i].split(";"); 
				int[] array = new int[AreaCoordinatesStrings2.length];
				for (int j = 0; j < AreaCoordinatesStrings2.length; j++) {
					int temp = Integer.parseInt(AreaCoordinatesStrings2[j]);	
					array[j] = temp;
				}
				AreaCoordinatesTemp[i] = array;
			}
			AreaCoordinates = AreaCoordinatesTemp ;			
		} else {
			AreaCoordinates = new int[][] {};
		}
		FlashingRes = Integer.parseInt(element.getAttribute("FlashingRes") == null || element.getAttribute("FlashingRes").length() == 0 ? "0"
			: element.getAttribute("FlashingRes")); // 闪烁资源 1=点击大手+相应光圈特效 2=流光特效
		String FlashingPositionString = element.getAttribute("FlashingPosition"); // 闪烁位置
		if (FlashingPositionString != null && FlashingPositionString.length() > 0) {
			String[] FlashingPositionStrings = FlashingPositionString.split(";"); 
			int[] FlashingPositionTemp = new int[FlashingPositionStrings.length] ; 
			for (int i = 0; i < FlashingPositionStrings.length; i++) {
				int temp = Integer.parseInt(FlashingPositionStrings[i]);	
				FlashingPositionTemp[i] = temp;
			}
			FlashingPosition = FlashingPositionTemp ;			
		} else {
			FlashingPosition = new int[] {};
		}
		Compulsion = Boolean.parseBoolean(element.getAttribute("Compulsion") == null || element.getAttribute("Compulsion").length() == 0 ? "false"
			: element.getAttribute("Compulsion")); // 是否强制 1=强制引导必须按照引导点下去 0=非强制引导可以直接点击任意位置消除掉
	}
	

}
