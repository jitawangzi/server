package cn.game.protocol.generated.config;

import cn.game.util.Weightable;
import org.w3c.dom.Element;


/**
 * 黑市
 * 
 * 工具生成的，不要手动修改
 */
 public class HeishiConfig implements Weightable {

	/** 商品id */
	public final int ID;		
	/** 包含物品 商品表ID */
	public final int Item;		
	/** 类型 1=固定位置看广告 2=随机商品 */
	public final int Type;		
	/** 权重 */
	public final int Weight;		

	public HeishiConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 商品id
		Item = Integer.parseInt(element.getAttribute("Item") == null || element.getAttribute("Item").length() == 0 ? "0"
			: element.getAttribute("Item")); // 包含物品 商品表ID
		Type = Integer.parseInt(element.getAttribute("Type") == null || element.getAttribute("Type").length() == 0 ? "0"
			: element.getAttribute("Type")); // 类型 1=固定位置看广告 2=随机商品
		Weight = Integer.parseInt(element.getAttribute("Weight") == null || element.getAttribute("Weight").length() == 0 ? "0"
			: element.getAttribute("Weight")); // 权重
	}
	

	@Override
	public int weight() {
		return this.Weight;
	}
}
