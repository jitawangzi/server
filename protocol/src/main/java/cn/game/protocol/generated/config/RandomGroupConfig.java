package cn.game.protocol.generated.config;

import cn.game.util.Weightable;
import org.w3c.dom.Element;


/**
 * 掉落组
 * 
 * 工具生成的，不要手动修改
 */
 public class RandomGroupConfig implements Weightable {

	/** 掉落组索引 */
	public final int ID;		
	/** 掉落组ID */
	public final int RandomGroupID;		
	/** 道具ID */
	public final int AssetID;		
	/** 数量 */
	public final int Several;		
	/** 权重 */
	public final int Weight;		

	public RandomGroupConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 掉落组索引
		RandomGroupID = Integer.parseInt(element.getAttribute("RandomGroupID") == null || element.getAttribute("RandomGroupID").length() == 0 ? "0"
			: element.getAttribute("RandomGroupID")); // 掉落组ID
		AssetID = Integer.parseInt(element.getAttribute("AssetID") == null || element.getAttribute("AssetID").length() == 0 ? "0"
			: element.getAttribute("AssetID")); // 道具ID
		Several = Integer.parseInt(element.getAttribute("Several") == null || element.getAttribute("Several").length() == 0 ? "0"
			: element.getAttribute("Several")); // 数量
		Weight = Integer.parseInt(element.getAttribute("Weight") == null || element.getAttribute("Weight").length() == 0 ? "0"
			: element.getAttribute("Weight")); // 权重
	}
	

	@Override
	public int weight() {
		return this.Weight;
	}
}
