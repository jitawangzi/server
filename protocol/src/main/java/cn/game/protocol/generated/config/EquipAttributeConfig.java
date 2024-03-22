package cn.game.protocol.generated.config;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.apache.commons.lang.StringUtils;

/**
 * 装备词条
 * 
 * 工具生成的，不要手动修改
 */
 public class EquipAttributeConfig 
{

	/** 索引id */
	private int id;		
	/** 初始类型 */
	private int initType;		
	/** 强化必成长 */
	private int growSure;		
	/** 属性id */
	private int atr;		
	/** 初始属性 */
	private List<Integer> atrInit;		
	/** 取消属性 波动成长 */
	private List<Integer> atrGrow;		

	public EquipAttributeConfig (Element element)
	{
	
		this.id = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("id")) ? "0"
			: element.getAttribute("id")); // 索引id
		this.initType = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("initType")) ? "0"
			: element.getAttribute("initType")); // 初始类型
		this.growSure = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("growSure")) ? "0"
			: element.getAttribute("growSure")); // 强化必成长
		this.atr = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("atr")) ? "0"
			: element.getAttribute("atr")); // 属性id
		String atrInit = element.getAttribute("atrInit"); // 初始属性
		if (atrInit != null && atrInit.length() > 0)
		{
			String[] atrInitStrings = atrInit.split(";"); 
			this.atrInit = new ArrayList<Integer>(atrInitStrings.length) ; 
			for (int i = 0; i < atrInitStrings.length; i++) 
			{
				Integer temp = Integer.valueOf(atrInitStrings[i]);
				this.atrInit.add(temp);
			}
		}
		else 
		{
			this.atrInit = new ArrayList<Integer>(0);
		}
		String atrGrow = element.getAttribute("atrGrow"); // 属性成长
		if (atrGrow != null && atrGrow.length() > 0)
		{
			String[] atrGrowStrings = atrGrow.split(";"); 
			this.atrGrow = new ArrayList<Integer>(atrGrowStrings.length) ; 
			for (int i = 0; i < atrGrowStrings.length; i++) 
			{
				Integer temp = Integer.valueOf(atrGrowStrings[i]);
				this.atrGrow.add(temp);
			}
		}
		else 
		{
			this.atrGrow = new ArrayList<Integer>(0);
		}
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public int getInitType()
	{
		return this.initType;
	}
	
	public int getGrowSure()
	{
		return this.growSure;
	}
	
	public int getAtr()
	{
		return this.atr;
	}
	
	public List<Integer> getAtrInit()
	{
		return this.atrInit;
	}
	
	public List<Integer> getAtrGrow()
	{
		return this.atrGrow;
	}
	
}
