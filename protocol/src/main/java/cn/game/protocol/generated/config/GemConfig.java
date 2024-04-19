package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 宝石
 * 
 * 工具生成的，不要手动修改
 */
 public class GemConfig {

	/** 物品ID */
	public final int ID;		
	/** 总类型 1-货币 2-物品 3-英雄 4-经验 5-能量 6-英雄武器 7-英雄装备 8-英雄时装 9-英雄宝石 10-龙 */
	public final int TotalType;		
	/** 宝石装备类型 1-头盔 2-披风 3-衣服 4-项链 5-戒指 6-靴子 */
	public final int ItemType;		
	/** 品质 1-白色 2-绿色 3-蓝色 4-紫色 5-金色 6-红色 7-彩色 8-永恒 9-唯一 */
	public final int Quality;		
	/** 物品名称 */
	public final String Name;		
	/** 物品tips */
	public final String Tips;		
	/** 图标Icon 文件名 */
	public final String Icon;		
	/** 合成   合成数量;合成后的宝石id */
	public final int[] GemCompound;		
	/** 【附加属性】id 调用RandomAttribute随机属性表id  【宝石生成时】 每个宝石只有固定1条属性 [第1次随机]每次生成宝石时，会在所有属性中根据权重，随机生成1条属性 [第2次随机]属性数值=[属性min，属性max]中随机生成1个数值给该宝石 */
	public final int RandomAttributeId;		

	public GemConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 物品ID
		TotalType = Integer.parseInt(element.getAttribute("TotalType") == null || element.getAttribute("TotalType").length() == 0 ? "0"
			: element.getAttribute("TotalType")); // 总类型 1-货币 2-物品 3-英雄 4-经验 5-能量 6-英雄武器 7-英雄装备 8-英雄时装 9-英雄宝石 10-龙
		ItemType = Integer.parseInt(element.getAttribute("ItemType") == null || element.getAttribute("ItemType").length() == 0 ? "0"
			: element.getAttribute("ItemType")); // 宝石装备类型 1-头盔 2-披风 3-衣服 4-项链 5-戒指 6-靴子
		Quality = Integer.parseInt(element.getAttribute("Quality") == null || element.getAttribute("Quality").length() == 0 ? "0"
			: element.getAttribute("Quality")); // 品质 1-白色 2-绿色 3-蓝色 4-紫色 5-金色 6-红色 7-彩色 8-永恒 9-唯一
		Name = element.getAttribute("Name"); // 物品名称
		Tips = element.getAttribute("Tips"); // 物品tips
		Icon = element.getAttribute("Icon"); // 图标Icon 文件名
		String GemCompoundString = element.getAttribute("GemCompound"); // 合成   合成数量;合成后的宝石id
		if (GemCompoundString != null && GemCompoundString.length() > 0) {
			String[] GemCompoundStrings = GemCompoundString.split(";"); 
			int[] GemCompoundTemp = new int[GemCompoundStrings.length] ; 
			for (int i = 0; i < GemCompoundStrings.length; i++) {
				int temp = Integer.parseInt(GemCompoundStrings[i]);	
				GemCompoundTemp[i] = temp;
			}
			GemCompound = GemCompoundTemp ;			
		} else {
			GemCompound = new int[] {};
		}
		RandomAttributeId = Integer.parseInt(element.getAttribute("RandomAttributeId") == null || element.getAttribute("RandomAttributeId").length() == 0 ? "0"
			: element.getAttribute("RandomAttributeId")); // 【附加属性】id 调用RandomAttribute随机属性表id  【宝石生成时】 每个宝石只有固定1条属性 [第1次随机]每次生成宝石时，会在所有属性中根据权重，随机生成1条属性 [第2次随机]属性数值=[属性min，属性max]中随机生成1个数值给该宝石
	}
	

}
