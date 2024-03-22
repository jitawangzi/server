package cn.game.protocol.generated.config;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.apache.commons.lang.StringUtils;

/**
 * 日常活动副本
 * 
 * 工具生成的，不要手动修改
 */
 public class ChapterDungeonConfig 
{

	/** id */
	private int id;		
	/** 类型 */
	private int type;		
	/** 开放等级 */
	private int needLv;		
	/** Level表 */
	private List<Integer> level;		

	public ChapterDungeonConfig (Element element)
	{
	
		this.id = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("id")) ? "0"
			: element.getAttribute("id")); // id
		this.type = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("type")) ? "0"
			: element.getAttribute("type")); // 类型
		this.needLv = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("needLv")) ? "0"
			: element.getAttribute("needLv")); // 开放等级
		String level = element.getAttribute("level"); // Level表
		if (level != null && level.length() > 0)
		{
			String[] levelStrings = level.split(";"); 
			this.level = new ArrayList<Integer>(levelStrings.length) ; 
			for (int i = 0; i < levelStrings.length; i++) 
			{
				Integer temp = Integer.valueOf(levelStrings[i]);
				this.level.add(temp);
			}
		}
		else 
		{
			this.level = new ArrayList<Integer>(0);
		}
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public int getType()
	{
		return this.type;
	}
	
	public int getNeedLv()
	{
		return this.needLv;
	}
	
	public List<Integer> getLevel()
	{
		return this.level;
	}
	
}
