package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 礼物卡
 * 
 * 工具生成的，不要手动修改
 */
 public class RoleGiftCardConfig {

	/** id */
	private int id;		
	/** 名称 */
	private String name;		
	/** 图片 */
	private String icon;		
	/** 类别 */
	private int type;		
	/** 描述 */
	private String desc;		
	/** 效果 */
	private int[] buffId;		
	/** COST值 */
	private int cost;		
	/** 是否给自己 */
	private boolean ifMine;		

	public RoleGiftCardConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.name = element.getAttribute("name"); // 名称
		this.icon = element.getAttribute("icon"); // 图片
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 类别
		this.desc = element.getAttribute("desc"); // 描述
		String buffIdString = element.getAttribute("buffId"); // 效果
		if (buffIdString != null && buffIdString.length() > 0) {
			String[] buffIdStrings = buffIdString.split("\\|"); 
			this.buffId = new int[buffIdStrings.length] ; 
			for (int i = 0; i < buffIdStrings.length; i++) {
				int temp = Integer.parseInt(buffIdStrings[i]);
				this.buffId[i] = temp;
			}
		} else {
			this.buffId = new int[] {};
		}
		this.cost = Integer.parseInt(element.getAttribute("cost") == null || element.getAttribute("cost").length() == 0 ? "0"
			: element.getAttribute("cost")); // COST值
		this.ifMine = Boolean.parseBoolean(element.getAttribute("ifMine") == null || element.getAttribute("ifMine").length() == 0 ? "false"
			: element.getAttribute("ifMine")); // 是否给自己
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
	
	public String getDesc() {
		return desc;
	}
	
	public int[] getBuffId() {
		return buffId;
	}
	
	public int getCost() {
		return cost;
	}
	
	public boolean getIfMine() {
		return ifMine;
	}
	
}
