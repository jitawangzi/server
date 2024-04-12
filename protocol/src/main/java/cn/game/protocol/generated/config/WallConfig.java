package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 防线
 * 
 * 工具生成的，不要手动修改
 */
 public class WallConfig {

	/** 防线等级 */
	public final int ID;		
	/** 每级固定属性  213 城池生命上限d 属性id;属性数值 */
	public final int[] WallAttribute;		
	/** 升级固定消耗ID  调用：【消耗表#Consume】 */
	public final int WallConsumeId;		
	/** 防线资源url */
	public final int WallResource;		
	/** 防线技能ID */
	public final int WallSkillID;		
	/** 防线破损 >=生命百分; */
	public final int[] WalDamage;		
	/** 陷阱url */
	public final int TrapResource;		
	/** 陷阱数量 */
	public final int TrapNumber;		
	/** 陷阱技能ID */
	public final int TrapSkillID;		

	public WallConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 防线等级
		String WallAttributeString = element.getAttribute("WallAttribute"); // 每级固定属性  213 城池生命上限d 属性id;属性数值
		if (WallAttributeString != null && WallAttributeString.length() > 0) {
			String[] WallAttributeStrings = WallAttributeString.split(";"); 
			int[] WallAttributeTemp = new int[WallAttributeStrings.length] ; 
			for (int i = 0; i < WallAttributeStrings.length; i++) {
				int temp = Integer.parseInt(WallAttributeStrings[i]);	
				WallAttributeTemp[i] = temp;
			}
			WallAttribute = WallAttributeTemp ;			
		} else {
			WallAttribute = new int[] {};
		}
		WallConsumeId = Integer.parseInt(element.getAttribute("WallConsumeId") == null || element.getAttribute("WallConsumeId").length() == 0 ? "0"
			: element.getAttribute("WallConsumeId")); // 升级固定消耗ID  调用：【消耗表#Consume】
		WallResource = Integer.parseInt(element.getAttribute("WallResource") == null || element.getAttribute("WallResource").length() == 0 ? "0"
			: element.getAttribute("WallResource")); // 防线资源url
		WallSkillID = Integer.parseInt(element.getAttribute("WallSkillID") == null || element.getAttribute("WallSkillID").length() == 0 ? "0"
			: element.getAttribute("WallSkillID")); // 防线技能ID
		String WalDamageString = element.getAttribute("WalDamage"); // 防线破损 >=生命百分;
		if (WalDamageString != null && WalDamageString.length() > 0) {
			String[] WalDamageStrings = WalDamageString.split(";"); 
			int[] WalDamageTemp = new int[WalDamageStrings.length] ; 
			for (int i = 0; i < WalDamageStrings.length; i++) {
				int temp = Integer.parseInt(WalDamageStrings[i]);	
				WalDamageTemp[i] = temp;
			}
			WalDamage = WalDamageTemp ;			
		} else {
			WalDamage = new int[] {};
		}
		TrapResource = Integer.parseInt(element.getAttribute("TrapResource") == null || element.getAttribute("TrapResource").length() == 0 ? "0"
			: element.getAttribute("TrapResource")); // 陷阱url
		TrapNumber = Integer.parseInt(element.getAttribute("TrapNumber") == null || element.getAttribute("TrapNumber").length() == 0 ? "0"
			: element.getAttribute("TrapNumber")); // 陷阱数量
		TrapSkillID = Integer.parseInt(element.getAttribute("TrapSkillID") == null || element.getAttribute("TrapSkillID").length() == 0 ? "0"
			: element.getAttribute("TrapSkillID")); // 陷阱技能ID
	}
	

}
