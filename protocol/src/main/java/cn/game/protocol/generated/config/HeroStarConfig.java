package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 英雄星级
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroStarConfig {

	/** 星级ID   每个星级中有10个小进度 星级提升有肉鸽/技能&属性提升 星级小进度提升仅属性提升 */
	public final int ID;		
	/** 英雄品质 1-白色 2-绿色 3-蓝色 4-紫色 5-金色 6-红色 7-彩色 8-永恒 9-唯一 */
	public final int Quality;		
	/** 星级小图标文件 */
	public final String StarIcon;		
	/** 小进度个数  999表示无需升级 */
	public final int ProgressBar;		
	/** 小进度属性 数值加成 每小进度加成 */
	public final int[][] ProgressAttribute;		
	/** 小进度提升消耗 所需属性及个数 配置：id;个数|id;个数 其中：物品id1打头          货币id2打头 */
	public final int[] ProgressItem;		
	/** 永久属性加成  配置：属性id;数值|属性id;数值 */
	public final int[] PermanentAttribute;		

	public HeroStarConfig (Element element) throws Exception {
	
		this.ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 星级ID   每个星级中有10个小进度 星级提升有肉鸽/技能&属性提升 星级小进度提升仅属性提升
		this.Quality = Integer.parseInt(element.getAttribute("Quality") == null || element.getAttribute("Quality").length() == 0 ? "0"
			: element.getAttribute("Quality")); // 英雄品质 1-白色 2-绿色 3-蓝色 4-紫色 5-金色 6-红色 7-彩色 8-永恒 9-唯一
		this.StarIcon = element.getAttribute("StarIcon"); // 星级小图标文件
		this.ProgressBar = Integer.parseInt(element.getAttribute("ProgressBar") == null || element.getAttribute("ProgressBar").length() == 0 ? "0"
			: element.getAttribute("ProgressBar")); // 小进度个数  999表示无需升级
		String ProgressAttributeString = element.getAttribute("ProgressAttribute"); // 小进度属性 数值加成 每小进度加成
		if (ProgressAttributeString != null && ProgressAttributeString.length() > 0) {
			String[] ProgressAttributeStrings = ProgressAttributeString.split("\\|"); 
			int[][] ProgressAttribute = new int[ProgressAttributeStrings.length][] ; 
			for (int i = 0; i < ProgressAttributeStrings.length; i++) {
				String[] ProgressAttributeStrings2 = ProgressAttributeStrings[i].split(";"); 
				int[] array = new int[ProgressAttributeStrings2.length];
				for (int j = 0; j < ProgressAttributeStrings2.length; j++) {
					int temp = Integer.parseInt(ProgressAttributeStrings2[j]);
					array[j] = temp;
				}
				ProgressAttribute[i] = array;
				
			}
			this.ProgressAttribute = ProgressAttribute ;			
		} else {
			this.ProgressAttribute = new int[][] {};
		}
		String ProgressItemString = element.getAttribute("ProgressItem"); // 小进度提升消耗 所需属性及个数 配置：id;个数|id;个数 其中：物品id1打头          货币id2打头
		if (ProgressItemString != null && ProgressItemString.length() > 0) {
			String[] ProgressItemStrings = ProgressItemString.split(";"); 
			int[] ProgressItem = new int[ProgressItemStrings.length] ; 
			for (int i = 0; i < ProgressItemStrings.length; i++) {
				int temp = Integer.parseInt(ProgressItemStrings[i]);
				ProgressItem[i] = temp;
			}
			this.ProgressItem = ProgressItem ;			
		} else {
			this.ProgressItem = new int[] {};
		}
		String PermanentAttributeString = element.getAttribute("PermanentAttribute"); // 永久属性加成  配置：属性id;数值|属性id;数值
		if (PermanentAttributeString != null && PermanentAttributeString.length() > 0) {
			String[] PermanentAttributeStrings = PermanentAttributeString.split(";"); 
			int[] PermanentAttribute = new int[PermanentAttributeStrings.length] ; 
			for (int i = 0; i < PermanentAttributeStrings.length; i++) {
				int temp = Integer.parseInt(PermanentAttributeStrings[i]);
				PermanentAttribute[i] = temp;
			}
			this.PermanentAttribute = PermanentAttribute ;			
		} else {
			this.PermanentAttribute = new int[] {};
		}
	}
	

}
