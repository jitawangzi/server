package cn.game.protocol.generated.config;
import org.w3c.dom.Element;
import org.apache.commons.lang.StringUtils;

/**
 * 开启表
 * 
 * 工具生成的，不要手动修改
 */
 public class ContentOpenConfig 
{

	/** id（等级） */
	private int id;		
	/** 开放项 */
	private String key;		
	/** 开放项名称 */
	private String name;		
	/** 需求玩家等级 */
	private int playLv;		
	/** 需求主角星级 */
	private int chiefStar;		

	public ContentOpenConfig (Element element)
	{
	
		this.id = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("id")) ? "0"
			: element.getAttribute("id")); // id（等级）
		this.key = element.getAttribute("key"); // 开放项
		this.name = element.getAttribute("name"); // 开放项名称
		this.playLv = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("playLv")) ? "0"
			: element.getAttribute("playLv")); // 需求玩家等级
		this.chiefStar = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("chiefStar")) ? "0"
			: element.getAttribute("chiefStar")); // 需求主角星级
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public String getKey()
	{
		return this.key;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public int getPlayLv()
	{
		return this.playLv;
	}
	
	public int getChiefStar()
	{
		return this.chiefStar;
	}
	
}
