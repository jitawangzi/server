package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 坐标组
 * 
 * 工具生成的，不要手动修改
 */
 public class CoordinateGroupConfig {

	/** 坐标组ID */
	public final int ID;		
	/** 坐标偏移公式 */
	public final String[] PointsName;		
	/** X偏移量 */
	public final int[] AddX;		
	/** Y偏移量 */
	public final int[] AddY;		

	public CoordinateGroupConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 坐标组ID
		String PointsNameString = element.getAttribute("PointsName"); // 坐标偏移公式
		if (PointsNameString != null && PointsNameString.length() > 0) {
			String[] PointsNameStrings = PointsNameString.split(";"); 
			String[] PointsNameTemp = new String[PointsNameStrings.length] ; 
			for (int i = 0; i < PointsNameStrings.length; i++) {
				String temp = PointsNameStrings[i];	
				PointsNameTemp[i] = temp;
			}
			PointsName = PointsNameTemp ;			
		} else {
			PointsName = new String[] {};
		}
		String AddXString = element.getAttribute("AddX"); // X偏移量
		if (AddXString != null && AddXString.length() > 0) {
			String[] AddXStrings = AddXString.split(";"); 
			int[] AddXTemp = new int[AddXStrings.length] ; 
			for (int i = 0; i < AddXStrings.length; i++) {
				int temp = Integer.parseInt(AddXStrings[i]);	
				AddXTemp[i] = temp;
			}
			AddX = AddXTemp ;			
		} else {
			AddX = new int[] {};
		}
		String AddYString = element.getAttribute("AddY"); // Y偏移量
		if (AddYString != null && AddYString.length() > 0) {
			String[] AddYStrings = AddYString.split(";"); 
			int[] AddYTemp = new int[AddYStrings.length] ; 
			for (int i = 0; i < AddYStrings.length; i++) {
				int temp = Integer.parseInt(AddYStrings[i]);	
				AddYTemp[i] = temp;
			}
			AddY = AddYTemp ;			
		} else {
			AddY = new int[] {};
		}
	}
	

}
