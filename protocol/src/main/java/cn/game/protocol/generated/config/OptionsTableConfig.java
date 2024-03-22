package cn.game.protocol.generated.config;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.w3c.dom.Element;

/**
 * 剧情选项表
 * 
 * 工具生成的，不要手动修改
 */
 public class OptionsTableConfig {

	/** id */
	private int id;		
	/** 名称 */
	private String name;		
	/** 奖励任务 */
	private int prizeMission;		
	/** 奖励道具 */
	private List<Entry<Integer,Integer>> prizeItem;		

	public OptionsTableConfig (Element element) {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.name = element.getAttribute("name"); // 名称
		this.prizeMission = Integer.parseInt(element.getAttribute("prizeMission") == null || element.getAttribute("prizeMission").length() == 0 ? "0"
			: element.getAttribute("prizeMission")); // 奖励任务
		String prizeItem = element.getAttribute("prizeItem"); // 奖励道具
		if (prizeItem != null && prizeItem.length() > 0) {
			String[] prizeItemStrings = prizeItem.split("\\|"); 
			this.prizeItem = new ArrayList<Entry<Integer,Integer>>(prizeItemStrings.length) ; 
			for (String string : prizeItemStrings) {
			    String[] split = string.split(":");
			    if (split.length != 2) {
					throw new IllegalArgumentException(" 表： " + this.getClass().getSimpleName() + " id  : " + this.id + " 字段: "
							+ "prizeItem" + "需为key-value格式，用冒号分隔");
				} 
				this.prizeItem.add(new Entry<Integer,Integer>()	{
					@Override
					public Integer setValue(Integer value) {
						return null;
					}
					@Override
					public Integer getValue() {
						return Integer.parseInt(split[1]);
					}
					@Override
					public Integer getKey() {
						return Integer.parseInt(split[0]);
					}
				}) ; 
			}
		} else {
			this.prizeItem = new ArrayList<Entry<Integer,Integer>>();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public int getPrizeMission() {
		return prizeMission;
	}
	
	public List<Entry<Integer,Integer>> getPrizeItem() {
		return prizeItem;
	}
	
}
