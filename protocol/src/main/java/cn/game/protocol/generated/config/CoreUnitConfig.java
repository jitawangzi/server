package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 核心道具
 * 
 * 工具生成的，不要手动修改
 */
 public class CoreUnitConfig {

	/** 核心id -- 百千万三位表示核心套装编号，个位表示核心单元编号 */
	private final int id;		
	/** 核心名称 */
	private final String name;		
	/** 核心品质 -- 1-C级绿色； 2-B级蓝色； 3-A级紫色； 4-S级橙色； */
	private final int quality;		
	/** 核心位置 -- 1-左； 2-右； 3-中； */
	private final int position;		
	/** 突破上限 -- C-2； B-3； A-4； S-5； */
	private final int BreakThrough;		
	/** 初始属性 -- 属性id|属性值 */
	private final List<List<Integer>> InitialAttribute;		
	/** 成长属性 -- 属性id|属性值 */
	private final List<List<Integer>> GrowthAttribute;		
	/** 特效 -- 单件特殊效果，读取skill表 */
	private final int effect;		
	/** 套装id */
	private final int SuitId;		
	/** 倾向标记 -- 5-爆发 6-续航 7-辅助 8-护卫 9-反击 10-生存 11-控制 12-治疗 */
	private final List<Integer> tag;		

	public CoreUnitConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // 核心id
		this.name = element.getAttribute("name"); // 核心名称
		this.quality = Integer.parseInt(element.getAttribute("quality") == null || element.getAttribute("quality").length() == 0 ? "0"
			: element.getAttribute("quality")); // 核心品质
		this.position = Integer.parseInt(element.getAttribute("position") == null || element.getAttribute("position").length() == 0 ? "0"
			: element.getAttribute("position")); // 核心位置
		this.BreakThrough = Integer.parseInt(element.getAttribute("BreakThrough") == null || element.getAttribute("BreakThrough").length() == 0 ? "0"
			: element.getAttribute("BreakThrough")); // 突破上限
		String InitialAttributeString = element.getAttribute("InitialAttribute"); // 初始属性
		if (InitialAttributeString != null && InitialAttributeString.length() > 0) {
			String[] InitialAttributeStrings = InitialAttributeString.split("\\|"); 
			List<List<Integer>> InitialAttribute = new ArrayList<List<Integer>>(InitialAttributeStrings.length) ; 
			for (int i = 0; i < InitialAttributeStrings.length; i++) {
				String[] InitialAttributeStrings2 = InitialAttributeStrings[i].split(":"); 
				List<Integer> list = new ArrayList<Integer>(InitialAttributeStrings2.length) ; 
				for (int j = 0; j < InitialAttributeStrings2.length; j++) {
					Integer temp = Integer.parseInt(InitialAttributeStrings2[j]);
					list.add(temp) ; 
				}
				InitialAttribute.add(list);
			}
			this.InitialAttribute = com.google.common.collect.ImmutableList.copyOf(InitialAttribute);						
		} else {
			this.InitialAttribute = java.util.Collections.emptyList();
		}
		String GrowthAttributeString = element.getAttribute("GrowthAttribute"); // 成长属性
		if (GrowthAttributeString != null && GrowthAttributeString.length() > 0) {
			String[] GrowthAttributeStrings = GrowthAttributeString.split("\\|"); 
			List<List<Integer>> GrowthAttribute = new ArrayList<List<Integer>>(GrowthAttributeStrings.length) ; 
			for (int i = 0; i < GrowthAttributeStrings.length; i++) {
				String[] GrowthAttributeStrings2 = GrowthAttributeStrings[i].split(":"); 
				List<Integer> list = new ArrayList<Integer>(GrowthAttributeStrings2.length) ; 
				for (int j = 0; j < GrowthAttributeStrings2.length; j++) {
					Integer temp = Integer.parseInt(GrowthAttributeStrings2[j]);
					list.add(temp) ; 
				}
				GrowthAttribute.add(list);
			}
			this.GrowthAttribute = com.google.common.collect.ImmutableList.copyOf(GrowthAttribute);						
		} else {
			this.GrowthAttribute = java.util.Collections.emptyList();
		}
		this.effect = Integer.parseInt(element.getAttribute("effect") == null || element.getAttribute("effect").length() == 0 ? "0"
			: element.getAttribute("effect")); // 特效
		this.SuitId = Integer.parseInt(element.getAttribute("SuitId") == null || element.getAttribute("SuitId").length() == 0 ? "0"
			: element.getAttribute("SuitId")); // 套装id
		String tagString = element.getAttribute("tag"); // 倾向标记
		if (tagString != null && tagString.length() > 0) {
			String[] tagStrings = tagString.split("\\|"); 
			List<Integer> tag = new ArrayList<Integer>(tagStrings.length) ; 
			for (int i = 0; i < tagStrings.length; i++) {
				Integer temp = Integer.parseInt(tagStrings[i]);
				tag.add(temp);
			}
			this.tag = com.google.common.collect.ImmutableList.copyOf(tag);						
		} else {
			this.tag = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public int getQuality() {
		return quality;
	}
	
	public int getPosition() {
		return position;
	}
	
	public int getBreakThrough() {
		return BreakThrough;
	}
	
	public List<List<Integer>> getInitialAttribute() {
		return InitialAttribute;
	}
	
	public List<List<Integer>> getGrowthAttribute() {
		return GrowthAttribute;
	}
	
	public int getEffect() {
		return effect;
	}
	
	public int getSuitId() {
		return SuitId;
	}
	
	public List<Integer> getTag() {
		return tag;
	}
	
}
