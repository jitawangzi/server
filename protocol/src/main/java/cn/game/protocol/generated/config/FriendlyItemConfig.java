package cn.game.protocol.generated.config;
import org.w3c.dom.Element;
import org.apache.commons.lang.StringUtils;

/**
 * 亲密度道具
 * 
 * 工具生成的，不要手动修改
 */
 public class FriendlyItemConfig 
{

	/** id */
	private int id;		
	/** 类型 */
	private int type;		
	/** 值 */
	private int value;		

	public FriendlyItemConfig (Element element)
	{
	
		this.id = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("id")) ? "0"
			: element.getAttribute("id")); // id
		this.type = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("type")) ? "0"
			: element.getAttribute("type")); // 类型
		this.value = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("value")) ? "0"
			: element.getAttribute("value")); // 值
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public int getType()
	{
		return this.type;
	}
	
	public int getValue()
	{
		return this.value;
	}
	
}
