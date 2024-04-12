package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 资产表
 * 
 * 工具生成的，不要手动修改
 */
 public class AssetConfig {

	/** 物品ID */
	public final int ID;		
	/** 物品英文名 */
	public final String Name;		
	/** 物品名称 */
	public final String Desc;		
	/** 物品类型 */
	public final int Type;		
	/** 品质 */
	public final int Quality;		
	/** 物品tips */
	public final String Tips;		
	/** 图标Icon 文件名 */
	public final String Icon;		

	public AssetConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 物品ID
		Name = element.getAttribute("Name"); // 物品英文名
		Desc = element.getAttribute("Desc"); // 物品名称
		Type = Integer.parseInt(element.getAttribute("Type") == null || element.getAttribute("Type").length() == 0 ? "0"
			: element.getAttribute("Type")); // 物品类型
		Quality = Integer.parseInt(element.getAttribute("Quality") == null || element.getAttribute("Quality").length() == 0 ? "0"
			: element.getAttribute("Quality")); // 品质
		Tips = element.getAttribute("Tips"); // 物品tips
		Icon = element.getAttribute("Icon"); // 图标Icon 文件名
	}
	

}
