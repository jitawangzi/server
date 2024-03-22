package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 礼物卡
 * 
 * 工具生成的，不要手动修改
 */
 public class RoleGiftCardsConfig {

	/** id -- id */
	private final int id;		
	/** 名称 */
	private final String name;		
	/** 图片 */
	private final String icon;		
	/** 类别 -- 1-攻击型 2-防御型 3-恢复型 */
	private final int type;		
	/** 目标 -- 1-单体 2-全体 3-仅自身 */
	private final int target;		
	/** COST值 */
	private final int cost;		
	/** 简述 -- 简述字数不超过18个字 */
	private final String sketch;		
	/** 描述 */
	private final String desc;		
	/** 是否下个区域生效 -- 0-不生效 1-生效 */
	private final boolean isNextAreaEffect;		
	/** 效果 -- 读取buff表ID */
	private final int[] buffIds;		

	public RoleGiftCardsConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.name = element.getAttribute("name"); // 名称
		this.icon = element.getAttribute("icon"); // 图片
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 类别
		this.target = Integer.parseInt(element.getAttribute("target") == null || element.getAttribute("target").length() == 0 ? "0"
			: element.getAttribute("target")); // 目标
		this.cost = Integer.parseInt(element.getAttribute("cost") == null || element.getAttribute("cost").length() == 0 ? "0"
			: element.getAttribute("cost")); // COST值
		this.sketch = element.getAttribute("sketch"); // 简述
		this.desc = element.getAttribute("desc"); // 描述
		this.isNextAreaEffect = Boolean.parseBoolean(element.getAttribute("isNextAreaEffect") == null || element.getAttribute("isNextAreaEffect").length() == 0 ? "false"
			: element.getAttribute("isNextAreaEffect")); // 是否下个区域生效
		String buffIdsString = element.getAttribute("buffIds"); // 效果
		if (buffIdsString != null && buffIdsString.length() > 0) {
			String[] buffIdsStrings = buffIdsString.split("\\|"); 
			int[] buffIds = new int[buffIdsStrings.length] ; 
			for (int i = 0; i < buffIdsStrings.length; i++) {
				int temp = Integer.parseInt(buffIdsStrings[i]);
				buffIds[i] = temp;
			}
			this.buffIds = buffIds ;			
		} else {
			this.buffIds = new int[] {};
		}
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public int getType() {
		return type;
	}
	
	public int getTarget() {
		return target;
	}
	
	public int getCost() {
		return cost;
	}
	
	public String getSketch() {
		return sketch;
	}
	
	public String getDesc() {
		return desc;
	}
	
	public boolean getIsNextAreaEffect() {
		return isNextAreaEffect;
	}
	
	public int[] getBuffIds() {
		return buffIds;
	}
	
}
