package cn.game.protocol.generated.config;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.apache.commons.lang.StringUtils;

/**
 * 日常活动副本开放
 * 
 * 工具生成的，不要手动修改
 */
 public class ChapterDungeonOpenConfig 
{

	/** id */
	private int id;		
	/** 开放等级 */
	private int needLv;		
	/** 开放章节 */
	private List<List<Integer>> openChapter;		

	public ChapterDungeonOpenConfig (Element element)
	{
	
		this.id = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("id")) ? "0"
			: element.getAttribute("id")); // id
		this.needLv = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("needLv")) ? "0"
			: element.getAttribute("needLv")); // 开放等级
		String openChapter = element.getAttribute("openChapter"); // 开放章节
		if (openChapter != null && openChapter.length() > 0)
		{
			String[] openChapterStrings = openChapter.split(";"); 
			this.openChapter = new ArrayList<List<Integer>>(openChapterStrings.length) ; 
			for (int i = 0; i < openChapterStrings.length; i++) 
			{
				String[] openChapterStrings2 = openChapterStrings[i].split(","); 
				List<Integer> list = new ArrayList<Integer>(openChapterStrings2.length) ; 
				for (int j = 0; j < openChapterStrings2.length; j++) 
				{
					Integer temp = Integer.valueOf(openChapterStrings2[j]);
					list.add(temp) ; 
				}
				this.openChapter.add(list);
			}
		}
		else 
		{
			this.openChapter = new ArrayList<List<Integer>>(0);
		}
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public int getNeedLv()
	{
		return this.needLv;
	}
	
	public List<List<Integer>> getOpenChapter()
	{
		return this.openChapter;
	}
	
}
