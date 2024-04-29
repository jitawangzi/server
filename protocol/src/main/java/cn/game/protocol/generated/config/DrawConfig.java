package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 抽卡
 * 
 * 工具生成的，不要手动修改
 */
 public class DrawConfig {

	/** 卡池id  程序直接写死id 策划改这个 */
	public final int ID;		
	/** 消耗id 单抽消耗id;10连抽消耗id  调用：Consume-Consume#消耗表id */
	public final int[] DrawConsumeId;		
	/** 每日单抽广告次数 */
	public final int DrawAdvNum;		
	/** 每抽出1个获金币数 */
	public final int DrawMoney;		
	/** 抽卡掉落id  调用Rand——RandomGiven#掉落表id */
	public final int DrawRandomId;		

	public DrawConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 卡池id  程序直接写死id 策划改这个
		String DrawConsumeIdString = element.getAttribute("DrawConsumeId"); // 消耗id 单抽消耗id;10连抽消耗id  调用：Consume-Consume#消耗表id
		if (DrawConsumeIdString != null && DrawConsumeIdString.length() > 0) {
			String[] DrawConsumeIdStrings = DrawConsumeIdString.split(";"); 
			int[] DrawConsumeIdTemp = new int[DrawConsumeIdStrings.length] ; 
			for (int i = 0; i < DrawConsumeIdStrings.length; i++) {
				int temp = Integer.parseInt(DrawConsumeIdStrings[i]);	
				DrawConsumeIdTemp[i] = temp;
			}
			DrawConsumeId = DrawConsumeIdTemp ;			
		} else {
			DrawConsumeId = new int[] {};
		}
		DrawAdvNum = Integer.parseInt(element.getAttribute("DrawAdvNum") == null || element.getAttribute("DrawAdvNum").length() == 0 ? "0"
			: element.getAttribute("DrawAdvNum")); // 每日单抽广告次数
		DrawMoney = Integer.parseInt(element.getAttribute("DrawMoney") == null || element.getAttribute("DrawMoney").length() == 0 ? "0"
			: element.getAttribute("DrawMoney")); // 每抽出1个获金币数
		DrawRandomId = Integer.parseInt(element.getAttribute("DrawRandomId") == null || element.getAttribute("DrawRandomId").length() == 0 ? "0"
			: element.getAttribute("DrawRandomId")); // 抽卡掉落id  调用Rand——RandomGiven#掉落表id
	}
	

}
