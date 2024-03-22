package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 英雄升级消耗
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroLvConfig {

	/** 等级ID  升级不根据英雄走 升级根据装英雄的槽位走 */
	public final int ID;		
	/** 升级所需金币数量 */
	public final int UpgradeCoin;		
	/** 不同职业所需 不同升级消耗道具个数  配置： id1;id2;id3;id4;id5;id6  消耗道具（直接写死）id201102;201103;201104;201105;201106;201107 */
	public final int[] ConsumptionQuantity;		

	public HeroLvConfig (Element element) throws Exception {
	
		this.ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 等级ID  升级不根据英雄走 升级根据装英雄的槽位走
		this.UpgradeCoin = Integer.parseInt(element.getAttribute("UpgradeCoin") == null || element.getAttribute("UpgradeCoin").length() == 0 ? "0"
			: element.getAttribute("UpgradeCoin")); // 升级所需金币数量
		String ConsumptionQuantityString = element.getAttribute("ConsumptionQuantity"); // 不同职业所需 不同升级消耗道具个数  配置： id1;id2;id3;id4;id5;id6  消耗道具（直接写死）id201102;201103;201104;201105;201106;201107
		if (ConsumptionQuantityString != null && ConsumptionQuantityString.length() > 0) {
			String[] ConsumptionQuantityStrings = ConsumptionQuantityString.split(";"); 
			int[] ConsumptionQuantity = new int[ConsumptionQuantityStrings.length] ; 
			for (int i = 0; i < ConsumptionQuantityStrings.length; i++) {
				int temp = Integer.parseInt(ConsumptionQuantityStrings[i]);
				ConsumptionQuantity[i] = temp;
			}
			this.ConsumptionQuantity = ConsumptionQuantity ;			
		} else {
			this.ConsumptionQuantity = new int[] {};
		}
	}
	

}
