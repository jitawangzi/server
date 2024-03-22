package cn.game.protocol.generated.config;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.apache.commons.lang.StringUtils;

/**
 * 战斗日志组表
 * 
 * 工具生成的，不要手动修改
 */
 public class CombatLogGroupConfig 
{

	/** 文本id */
	private int id;		
	/** 章节名称 */
	private List<Integer> textId;		
	/** 章节信息 */
	private String chapterName;		
	/** 章节信息 */
	private List<String> chapterInfo;		

	public CombatLogGroupConfig (Element element)
	{
	
		this.id = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("id")) ? "0"
			: element.getAttribute("id")); // id
		String textId = element.getAttribute("textId"); // 文本id
		if (textId != null && textId.length() > 0)
		{
			String[] textIdStrings = textId.split("\\|"); 
			this.textId = new ArrayList<Integer>(textIdStrings.length) ; 
			for (int i = 0; i < textIdStrings.length; i++) 
			{
				Integer temp = Integer.valueOf(textIdStrings[i]);
				this.textId.add(temp);
			}
		}
		else 
		{
			this.textId = new ArrayList<Integer>();
		}
		this.chapterName = element.getAttribute("chapterName"); // 章节名称
		String chapterInfo = element.getAttribute("chapterInfo"); // 章节信息
		if (chapterInfo != null && chapterInfo.length() > 0)
		{
			String[] chapterInfoStrings = chapterInfo.split("\\|"); 
			this.chapterInfo = new ArrayList<String>(chapterInfoStrings.length) ; 
			for (int i = 0; i < chapterInfoStrings.length; i++) 
			{
				String temp = chapterInfoStrings[i];
				this.chapterInfo.add(temp);
			}
		}
		else 
		{
			this.chapterInfo = new ArrayList<String>();
		}
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public List<Integer> getTextId()
	{
		return this.textId;
	}
	
	public String getChapterName()
	{
		return this.chapterName;
	}
	
	public List<String> getChapterInfo()
	{
		return this.chapterInfo;
	}
	
}
