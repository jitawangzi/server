package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import cn.game.util.Weightable;
import org.w3c.dom.Element;

/**
 * 礼物卡表
 * 
 * 工具生成的，不要手动修改
 */
 public class GiftCardConfig implements Weightable {

	/** id */
	private int id;		
	/** 名称 */
	private String name;		
	/** 描述 */
	private String desc;		
	/** 图标 */
	private String icon;		
	/** 稀有度 */
	private int quality;		
	/** 效果 */
	private List<Integer> buff;		
	/** 权重 */
	private int weight;		

	public GiftCardConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.name = element.getAttribute("name"); // 名称
		this.desc = element.getAttribute("desc"); // 描述
		this.icon = element.getAttribute("icon"); // 图标
		this.quality = Integer.parseInt(element.getAttribute("quality") == null || element.getAttribute("quality").length() == 0 ? "0"
			: element.getAttribute("quality")); // 稀有度
		String buffString = element.getAttribute("buff"); // 效果
		if (buffString != null && buffString.length() > 0) {
			String[] buffStrings = buffString.split("\\|"); 
			this.buff = new ArrayList<Integer>(buffStrings.length) ; 
			for (int i = 0; i < buffStrings.length; i++) {
				Integer temp = Integer.parseInt(buffStrings[i]);
				this.buff.add(temp);
			}
		} else {
			this.buff = new ArrayList<Integer>();
		}
		this.weight = Integer.parseInt(element.getAttribute("weight") == null || element.getAttribute("weight").length() == 0 ? "0"
			: element.getAttribute("weight")); // 权重
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDesc() {
		return desc;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public int getQuality() {
		return quality;
	}
	
	public List<Integer> getBuff() {
		return buff;
	}
	
	public int getWeight() {
		return weight;
	}
	
	@Override
	public int weight() {
		return this.weight;
	}
}
