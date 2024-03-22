package cn.game.protocol.generated.config;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.w3c.dom.Element;
import org.apache.commons.lang.StringUtils;

/**
 * 装备套装
 * 
 * 工具生成的，不要手动修改
 */
 public class EquipSuitConfig 
{

	/** id */
	private int id;		
	/** 套装id */
	private int suitId;		
	/** 套装件数 */
	private int suitNumb;		
	/** 套装属性 */
	private List<Entry<Integer,Integer>> suitAtr;		
	/** 套装技能 */
	private int suitSkill;		

	public EquipSuitConfig (Element element)
	{
	
		this.id = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("id")) ? "0"
			: element.getAttribute("id")); // id
		this.suitId = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("suitId")) ? "0"
			: element.getAttribute("suitId")); // 套装id
		this.suitNumb = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("suitNumb")) ? "0"
			: element.getAttribute("suitNumb")); // 套装件数
		String suitAtr = element.getAttribute("suitAtr"); // 套装属性
		if (suitAtr != null && suitAtr.length() > 0)
		{
			String[] suitAtrStrings = suitAtr.split(";"); 
			this.suitAtr = new ArrayList<Entry<Integer,Integer>>(suitAtrStrings.length) ; 
			for (String string : suitAtrStrings)
			{
			    String[] split = string.split(",");
			    if (split.length != 2) {
					throw new IllegalArgumentException(" 表： " + this.getClass().getSimpleName() + " id  : " + this.id + " 字段: "
							+ "suitAtr" + "需为key-value格式，用逗号分隔");
				} 
				this.suitAtr.add(new Entry<Integer,Integer>()
				{
					@Override
					public Integer setValue(Integer value)
					{
						return null;
					}
					@Override
					public Integer getValue()
					{
						return Integer.valueOf(split[1]);
					}
					@Override
					public Integer getKey()
					{
						return Integer.valueOf(split[0]);
					}
				}) ; 
			}
		}
		else 
		{
			this.suitAtr = new ArrayList<Entry<Integer,Integer>>(0);
		}
		this.suitSkill = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("suitSkill")) ? "0"
			: element.getAttribute("suitSkill")); // 套装技能
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public int getSuitId()
	{
		return this.suitId;
	}
	
	public int getSuitNumb()
	{
		return this.suitNumb;
	}
	
	public List<Entry<Integer,Integer>> getSuitAtr()
	{
		return this.suitAtr;
	}
	
	public int getSuitSkill()
	{
		return this.suitSkill;
	}
	
}
