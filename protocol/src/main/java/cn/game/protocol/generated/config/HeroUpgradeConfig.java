package cn.game.protocol.generated.config;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.w3c.dom.Element;
import org.apache.commons.lang.StringUtils;

/**
 * 角色突破
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroUpgradeConfig 
{

	/** 元素类型 * 100 + 突破等级 */
	private int id;		
	/** 元素类型 */
	private int type;		
	/** 突破等级 */
	private int grade;		
	/** coinToNextGrade */
	private int coin2NextGr;		
	/** 突破消耗道具 */
	private List<Entry<Integer,Integer>> upgradeConsume;		
	/** damageDoneToElement */
	private float dmD2E1;		
	/** 对元素2伤害提高% */
	private float dmD2E2;		
	/** 对元素3伤害提高% */
	private float dmD2E3;		
	/** 对元素4伤害提高% */
	private float dmD2E4;		
	/** 对元素5伤害提高% */
	private float dmD2E5;		
	/** 对元素6伤害提高% */
	private float dmD2E6;		
	/** 突破战力系数 */
	private float CPUpgrade;		

	public HeroUpgradeConfig (Element element)
	{
	
		this.id = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("id")) ? "0"
			: element.getAttribute("id")); // 索引
		this.type = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("type")) ? "0"
			: element.getAttribute("type")); // 元素类型
		this.grade = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("grade")) ? "0"
			: element.getAttribute("grade")); // 突破等级
		this.coin2NextGr = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("coin2NextGr")) ? "0"
			: element.getAttribute("coin2NextGr")); // 突破下级铜币
		String upgradeConsume = element.getAttribute("upgradeConsume"); // 突破消耗道具
		if (upgradeConsume != null && upgradeConsume.length() > 0)
		{
			String[] upgradeConsumeStrings = upgradeConsume.split(";"); 
			this.upgradeConsume = new ArrayList<Entry<Integer,Integer>>(upgradeConsumeStrings.length) ; 
			for (String string : upgradeConsumeStrings)
			{
			    String[] split = string.split(",");
			    if (split.length != 2) {
					throw new IllegalArgumentException(" 表： " + this.getClass().getSimpleName() + " id  : " + this.id + " 字段: "
							+ "upgradeConsume" + "需为key-value格式，用逗号分隔");
				} 
				this.upgradeConsume.add(new Entry<Integer,Integer>()
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
			this.upgradeConsume = new ArrayList<Entry<Integer,Integer>>(0);
		}
		this.dmD2E1 = Float.parseFloat(StringUtils.isEmpty(element.getAttribute("dmD2E1")) ? "0"
			: element.getAttribute("dmD2E1"))/10000f; // 对元素1伤害提高%
		this.dmD2E2 = Float.parseFloat(StringUtils.isEmpty(element.getAttribute("dmD2E2")) ? "0"
			: element.getAttribute("dmD2E2"))/10000f; // 对元素2伤害提高%
		this.dmD2E3 = Float.parseFloat(StringUtils.isEmpty(element.getAttribute("dmD2E3")) ? "0"
			: element.getAttribute("dmD2E3"))/10000f; // 对元素3伤害提高%
		this.dmD2E4 = Float.parseFloat(StringUtils.isEmpty(element.getAttribute("dmD2E4")) ? "0"
			: element.getAttribute("dmD2E4"))/10000f; // 对元素4伤害提高%
		this.dmD2E5 = Float.parseFloat(StringUtils.isEmpty(element.getAttribute("dmD2E5")) ? "0"
			: element.getAttribute("dmD2E5"))/10000f; // 对元素5伤害提高%
		this.dmD2E6 = Float.parseFloat(StringUtils.isEmpty(element.getAttribute("dmD2E6")) ? "0"
			: element.getAttribute("dmD2E6"))/10000f; // 对元素6伤害提高%
		this.CPUpgrade = Float.parseFloat(StringUtils.isEmpty(element.getAttribute("CPUpgrade")) ? "0"
			: element.getAttribute("CPUpgrade"))/10000f; // 突破战力系数
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public int getType()
	{
		return this.type;
	}
	
	public int getGrade()
	{
		return this.grade;
	}
	
	public int getCoin2NextGr()
	{
		return this.coin2NextGr;
	}
	
	public List<Entry<Integer,Integer>> getUpgradeConsume()
	{
		return this.upgradeConsume;
	}
	
	public float getDmD2E1()
	{
		return this.dmD2E1;
	}
	
	public float getDmD2E2()
	{
		return this.dmD2E2;
	}
	
	public float getDmD2E3()
	{
		return this.dmD2E3;
	}
	
	public float getDmD2E4()
	{
		return this.dmD2E4;
	}
	
	public float getDmD2E5()
	{
		return this.dmD2E5;
	}
	
	public float getDmD2E6()
	{
		return this.dmD2E6;
	}
	
	public float getCPUpgrade()
	{
		return this.CPUpgrade;
	}
	
}
