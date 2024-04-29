package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 地图
 * 
 * 工具生成的，不要手动修改
 */
 public class MapConfig {

	/** 地图ID */
	public final int ID;		
	/** 【章节宝箱1】 通关战役id;奖励掉落id */
	public final int[] OnlyWinRan;		
	/** 【章节宝箱2】 战役id;坚持分钟数;奖励掉落id */
	public final int[] OnlyWin50Ran;		
	/** 【章节宝箱3】 通关战役id;奖励掉落id */
	public final int[] OnlyWin100Ran;		

	public MapConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 地图ID
		String OnlyWinRanString = element.getAttribute("OnlyWinRan"); // 【章节宝箱1】 通关战役id;奖励掉落id
		if (OnlyWinRanString != null && OnlyWinRanString.length() > 0) {
			String[] OnlyWinRanStrings = OnlyWinRanString.split(";"); 
			int[] OnlyWinRanTemp = new int[OnlyWinRanStrings.length] ; 
			for (int i = 0; i < OnlyWinRanStrings.length; i++) {
				int temp = Integer.parseInt(OnlyWinRanStrings[i]);	
				OnlyWinRanTemp[i] = temp;
			}
			OnlyWinRan = OnlyWinRanTemp ;			
		} else {
			OnlyWinRan = new int[] {};
		}
		String OnlyWin50RanString = element.getAttribute("OnlyWin50Ran"); // 【章节宝箱2】 战役id;坚持分钟数;奖励掉落id
		if (OnlyWin50RanString != null && OnlyWin50RanString.length() > 0) {
			String[] OnlyWin50RanStrings = OnlyWin50RanString.split(";"); 
			int[] OnlyWin50RanTemp = new int[OnlyWin50RanStrings.length] ; 
			for (int i = 0; i < OnlyWin50RanStrings.length; i++) {
				int temp = Integer.parseInt(OnlyWin50RanStrings[i]);	
				OnlyWin50RanTemp[i] = temp;
			}
			OnlyWin50Ran = OnlyWin50RanTemp ;			
		} else {
			OnlyWin50Ran = new int[] {};
		}
		String OnlyWin100RanString = element.getAttribute("OnlyWin100Ran"); // 【章节宝箱3】 通关战役id;奖励掉落id
		if (OnlyWin100RanString != null && OnlyWin100RanString.length() > 0) {
			String[] OnlyWin100RanStrings = OnlyWin100RanString.split(";"); 
			int[] OnlyWin100RanTemp = new int[OnlyWin100RanStrings.length] ; 
			for (int i = 0; i < OnlyWin100RanStrings.length; i++) {
				int temp = Integer.parseInt(OnlyWin100RanStrings[i]);	
				OnlyWin100RanTemp[i] = temp;
			}
			OnlyWin100Ran = OnlyWin100RanTemp ;			
		} else {
			OnlyWin100Ran = new int[] {};
		}
	}
	

}
