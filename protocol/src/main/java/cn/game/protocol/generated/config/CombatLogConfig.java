package cn.game.protocol.generated.config;
import org.w3c.dom.Element;
import org.apache.commons.lang.StringUtils;

/**
 * 战斗日志子表
 * 
 * 工具生成的，不要手动修改
 */
 public class CombatLogConfig 
{

	/** 关卡名称 */
	private int id;		
	/** 文本内容 */
	private String levelName;		

	public CombatLogConfig (Element element)
	{
	
		this.id = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("id")) ? "0"
			: element.getAttribute("id")); // id
		this.levelName = element.getAttribute("levelName"); // 关卡名称
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public String getLevelName()
	{
		return this.levelName;
	}
	
}
