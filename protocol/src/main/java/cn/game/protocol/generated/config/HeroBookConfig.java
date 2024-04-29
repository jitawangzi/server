package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 英雄图鉴
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroBookConfig {

	/** 图鉴id */
	public final int ID;		
	/** 图鉴名称 */
	public final String HeroBookName;		
	/** 图鉴包含英雄id组  其他配置说明：Initial——GlobalConst#常量表 HeroBookAward【图鉴】拥有&每次突破，每个神将固定奖励元宝数 HeroBookStar【图鉴】不同品质的1颗星加成属性id */
	public final int[] HeroBookCardIdGroup;		
	/** 图鉴主页信纸UI资源路径 */
	public final String BookArtResource;		

	public HeroBookConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 图鉴id
		HeroBookName = element.getAttribute("HeroBookName"); // 图鉴名称
		String HeroBookCardIdGroupString = element.getAttribute("HeroBookCardIdGroup"); // 图鉴包含英雄id组  其他配置说明：Initial——GlobalConst#常量表 HeroBookAward【图鉴】拥有&每次突破，每个神将固定奖励元宝数 HeroBookStar【图鉴】不同品质的1颗星加成属性id
		if (HeroBookCardIdGroupString != null && HeroBookCardIdGroupString.length() > 0) {
			String[] HeroBookCardIdGroupStrings = HeroBookCardIdGroupString.split(";"); 
			int[] HeroBookCardIdGroupTemp = new int[HeroBookCardIdGroupStrings.length] ; 
			for (int i = 0; i < HeroBookCardIdGroupStrings.length; i++) {
				int temp = Integer.parseInt(HeroBookCardIdGroupStrings[i]);	
				HeroBookCardIdGroupTemp[i] = temp;
			}
			HeroBookCardIdGroup = HeroBookCardIdGroupTemp ;			
		} else {
			HeroBookCardIdGroup = new int[] {};
		}
		BookArtResource = element.getAttribute("BookArtResource"); // 图鉴主页信纸UI资源路径
	}
	

}
