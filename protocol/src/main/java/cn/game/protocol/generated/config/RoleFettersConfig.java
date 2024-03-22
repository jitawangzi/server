package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 好感羁绊
 * 
 * 工具生成的，不要手动修改
 */
 public class RoleFettersConfig implements Comparable<RoleFettersConfig> {

	/** id -- id */
	private final int id;		
	/** 类型 -- 1-默认通用 2-特殊羁绊 */
	private final int type;		
	/** 好感值 */
	private final int favorValue;		
	/** 名称 */
	private final String name;		
	/** 效果 -- 读取buff表ID */
	private final int[] buffIds;		
	/** 角色1 */
	private final int roleId1;		
	/** 角色2 */
	private final int roleId2;		

	public RoleFettersConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 类型
		this.favorValue = Integer.parseInt(element.getAttribute("favorValue") == null || element.getAttribute("favorValue").length() == 0 ? "0"
			: element.getAttribute("favorValue")); // 好感值
		this.name = element.getAttribute("name"); // 名称
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
		this.roleId1 = Integer.parseInt(element.getAttribute("roleId1") == null || element.getAttribute("roleId1").length() == 0 ? "0"
			: element.getAttribute("roleId1")); // 角色1
		this.roleId2 = Integer.parseInt(element.getAttribute("roleId2") == null || element.getAttribute("roleId2").length() == 0 ? "0"
			: element.getAttribute("roleId2")); // 角色2
	}
	
	public int getId() {
		return id;
	}
	
	public int getType() {
		return type;
	}
	
	public int getFavorValue() {
		return favorValue;
	}
	
	public String getName() {
		return name;
	}
	
	public int[] getBuffIds() {
		return buffIds;
	}
	
	public int getRoleId1() {
		return roleId1;
	}
	
	public int getRoleId2() {
		return roleId2;
	}
	
	@Override
	public int compareTo(RoleFettersConfig o) {
		if (this.getFavorValue() > o.getFavorValue()) {
			return 1 ; 
		}else if (this.getFavorValue() < o.getFavorValue()) {
			return -1 ; 
		}
		return 0;
	}
}
