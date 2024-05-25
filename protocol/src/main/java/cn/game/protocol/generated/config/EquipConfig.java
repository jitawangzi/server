package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 装备表
 * 
 * 工具生成的，不要手动修改
 */
 public class EquipConfig {

	/** 装备ID  物品5打头6位 51—装备 52—特殊空格 */
	public final int ID;		
	/** 总类型 1-货币 2-物品 3-装备 4-铁哥们 5-好友 6-宠物 */
	public final int TotalType;		
	/** 物品类型 1-武器 2-防具 3-战中特殊空格 4-乾坤袋 */
	public final int ItemType;		
	/** 品质 1-白色 2-绿色 3-蓝色 4-紫色 5-金色 6-红色 7-彩色 8-永恒 9-唯一 */
	public final int Quality;		
	/** 装备同组  id一致代表是一种装备的不同等级 */
	public final int EquipGroup;		
	/** 装备等级 */
	public final int EquipLv;		
	/** 章节解锁  章节数字 ≥该章节，就可以解锁该装备 */
	public final int ChapterUnlock;		
	/** 装备类型  二进制 9宫格，有填1，无填0 */
	public final String EquipType;		
	/** 升级  升级所需碎片id；升级所需碎片数量|升级所需金币物品id；所需金币数量 */
	public final int[][] EquipUpgrade;		
	/** 【纯界面显示】 目标  1-最近目标 2-随机目标 3-自身 */
	public final int CTarget;		
	/** 增加HP上限  属性id；属性数值 */
	public final int[] CHp;		
	/** 基础伤害  属性id；属性数值 */
	public final int[] CDamage;		
	/** 是否需要看广告才能用 */
	public final int EquipAdvertisement;		
	/** 战中合成 每次刷新的权重  1、【当前装备库】先检索目前已上阵的装备 2、【当前波次装备刷新总个数】再按照Battle#战役中每波次合成时取随机数，生成该波次的刷新装备数 3、【每装备的刷新权重】 */
	public final int EquipRefreshWeight;		

	public EquipConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 装备ID  物品5打头6位 51—装备 52—特殊空格
		TotalType = Integer.parseInt(element.getAttribute("TotalType") == null || element.getAttribute("TotalType").length() == 0 ? "0"
			: element.getAttribute("TotalType")); // 总类型 1-货币 2-物品 3-装备 4-铁哥们 5-好友 6-宠物
		ItemType = Integer.parseInt(element.getAttribute("ItemType") == null || element.getAttribute("ItemType").length() == 0 ? "0"
			: element.getAttribute("ItemType")); // 物品类型 1-武器 2-防具 3-战中特殊空格 4-乾坤袋
		Quality = Integer.parseInt(element.getAttribute("Quality") == null || element.getAttribute("Quality").length() == 0 ? "0"
			: element.getAttribute("Quality")); // 品质 1-白色 2-绿色 3-蓝色 4-紫色 5-金色 6-红色 7-彩色 8-永恒 9-唯一
		EquipGroup = Integer.parseInt(element.getAttribute("EquipGroup") == null || element.getAttribute("EquipGroup").length() == 0 ? "0"
			: element.getAttribute("EquipGroup")); // 装备同组  id一致代表是一种装备的不同等级
		EquipLv = Integer.parseInt(element.getAttribute("EquipLv") == null || element.getAttribute("EquipLv").length() == 0 ? "0"
			: element.getAttribute("EquipLv")); // 装备等级
		ChapterUnlock = Integer.parseInt(element.getAttribute("ChapterUnlock") == null || element.getAttribute("ChapterUnlock").length() == 0 ? "0"
			: element.getAttribute("ChapterUnlock")); // 章节解锁  章节数字 ≥该章节，就可以解锁该装备
		EquipType = element.getAttribute("EquipType"); // 装备类型  二进制 9宫格，有填1，无填0
		String EquipUpgradeString = element.getAttribute("EquipUpgrade"); // 升级  升级所需碎片id；升级所需碎片数量|升级所需金币物品id；所需金币数量
		if (EquipUpgradeString != null && EquipUpgradeString.length() > 0) {
			String[] EquipUpgradeStrings = EquipUpgradeString.split("\\|"); 
			int[][] EquipUpgradeTemp = new int[EquipUpgradeStrings.length][] ; 
			for (int i = 0; i < EquipUpgradeStrings.length; i++) {
				String[] EquipUpgradeStrings2 = EquipUpgradeStrings[i].split(";"); 
				int[] array = new int[EquipUpgradeStrings2.length];
				for (int j = 0; j < EquipUpgradeStrings2.length; j++) {
					int temp = Integer.parseInt(EquipUpgradeStrings2[j]);	
					array[j] = temp;
				}
				EquipUpgradeTemp[i] = array;
			}
			EquipUpgrade = EquipUpgradeTemp ;			
		} else {
			EquipUpgrade = new int[][] {};
		}
		CTarget = Integer.parseInt(element.getAttribute("CTarget") == null || element.getAttribute("CTarget").length() == 0 ? "0"
			: element.getAttribute("CTarget")); // 【纯界面显示】 目标  1-最近目标 2-随机目标 3-自身
		String CHpString = element.getAttribute("CHp"); // 增加HP上限  属性id；属性数值
		if (CHpString != null && CHpString.length() > 0) {
			String[] CHpStrings = CHpString.split(";"); 
			int[] CHpTemp = new int[CHpStrings.length] ; 
			for (int i = 0; i < CHpStrings.length; i++) {
				int temp = Integer.parseInt(CHpStrings[i]);	
				CHpTemp[i] = temp;
			}
			CHp = CHpTemp ;			
		} else {
			CHp = new int[] {};
		}
		String CDamageString = element.getAttribute("CDamage"); // 基础伤害  属性id；属性数值
		if (CDamageString != null && CDamageString.length() > 0) {
			String[] CDamageStrings = CDamageString.split(";"); 
			int[] CDamageTemp = new int[CDamageStrings.length] ; 
			for (int i = 0; i < CDamageStrings.length; i++) {
				int temp = Integer.parseInt(CDamageStrings[i]);	
				CDamageTemp[i] = temp;
			}
			CDamage = CDamageTemp ;			
		} else {
			CDamage = new int[] {};
		}
		EquipAdvertisement = Integer.parseInt(element.getAttribute("EquipAdvertisement") == null || element.getAttribute("EquipAdvertisement").length() == 0 ? "0"
			: element.getAttribute("EquipAdvertisement")); // 是否需要看广告才能用
		EquipRefreshWeight = Integer.parseInt(element.getAttribute("EquipRefreshWeight") == null || element.getAttribute("EquipRefreshWeight").length() == 0 ? "0"
			: element.getAttribute("EquipRefreshWeight")); // 战中合成 每次刷新的权重  1、【当前装备库】先检索目前已上阵的装备 2、【当前波次装备刷新总个数】再按照Battle#战役中每波次合成时取随机数，生成该波次的刷新装备数 3、【每装备的刷新权重】
	}
	

}
