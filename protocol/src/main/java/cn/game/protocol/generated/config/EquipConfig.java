package cn.game.protocol.generated.config;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.w3c.dom.Element;
import org.apache.commons.lang.StringUtils;

/**
 * 装备
 * 
 * 工具生成的，不要手动修改
 */
 public class EquipConfig 
{

	/** id */
	private int id;		
	/** 名称 */
	private String name;		
	/** 描述 */
	private String des;		
	/** 子类型 */
	private int subType;		
	/** 职业 */
	private int prof;		
	/** 品质 */
	private int quarlity;		
	/** 吞噬经验 */
	private int asExp;		
	/** 套装id */
	private int suit;		
	/** 初始战斗力 */
	private int combatPowerInit;		
	/** 战斗力成长 */
	private int combatPowerGrow;		
	/** 初始属性条数 */
	private List<Entry<Integer,Integer>> atrInitNumb;		
	/** 单条随机属性组1 */
	private List<Entry<Integer,Integer>> atrGroup1;		
	/** 单条随机属性组2 */
	private List<Entry<Integer,Integer>> atrGroup2;		
	/** 单条随机属性组3 */
	private List<Entry<Integer,Integer>> atrGroup3;		
	/** 不定条数随机属性组 */
	private List<Entry<Integer,Integer>> atrExtraGroup;		
	/** 强化上限 */
	private int maxLv;		
	/** 阶段成长等级 */
	private List<Integer> phaseLv;		
	/** 最大属性条数 */
	private int atrLimit;		
	/** 阶段成长新属性概率 */
	private int atrNewOdd;		
	/** 阶段成长属性强化 */
	private List<Entry<Integer,Integer>> atrStrength;		

	public EquipConfig (Element element)
	{
	
		this.id = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("id")) ? "0"
			: element.getAttribute("id")); // id
		this.name = element.getAttribute("name"); // 名称
		this.des = element.getAttribute("des"); // 描述
		this.subType = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("subType")) ? "0"
			: element.getAttribute("subType")); // 子类型
		this.prof = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("prof")) ? "0"
			: element.getAttribute("prof")); // 职业
		this.quarlity = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("quarlity")) ? "0"
			: element.getAttribute("quarlity")); // 品质
		this.asExp = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("asExp")) ? "0"
			: element.getAttribute("asExp")); // 吞噬经验
		this.suit = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("suit")) ? "0"
			: element.getAttribute("suit")); // 套装id
		this.combatPowerInit = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("combatPowerInit")) ? "0"
			: element.getAttribute("combatPowerInit")); // 初始战斗力
		this.combatPowerGrow = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("combatPowerGrow")) ? "0"
			: element.getAttribute("combatPowerGrow")); // 战斗力成长
		String atrInitNumb = element.getAttribute("atrInitNumb"); // 初始属性条数
		if (atrInitNumb != null && atrInitNumb.length() > 0)
		{
			String[] atrInitNumbStrings = atrInitNumb.split(";"); 
			this.atrInitNumb = new ArrayList<Entry<Integer,Integer>>(atrInitNumbStrings.length) ; 
			for (String string : atrInitNumbStrings)
			{
			    final String[] split = string.split(",");
			    if (split.length != 2) {
					throw new IllegalArgumentException(" 表： " + this.getClass().getSimpleName() + " id  : " + this.id + " 字段: "
							+ "atrInitNumb" + "需为key-value格式，用逗号分隔");
				} 
				this.atrInitNumb.add(new Entry<Integer,Integer>()
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
			this.atrInitNumb = new ArrayList<Entry<Integer,Integer>>(0);
		}
		String atrGroup1 = element.getAttribute("atrGroup1"); // 单条随机属性组1
		if (atrGroup1 != null && atrGroup1.length() > 0)
		{
			String[] atrGroup1Strings = atrGroup1.split(";"); 
			this.atrGroup1 = new ArrayList<Entry<Integer,Integer>>(atrGroup1Strings.length) ; 
			for (String string : atrGroup1Strings)
			{
			    String[] split = string.split(",");
			    if (split.length != 2) {
					throw new IllegalArgumentException(" 表： " + this.getClass().getSimpleName() + " id  : " + this.id + " 字段: "
							+ "atrGroup1" + "需为key-value格式，用逗号分隔");
				} 
				this.atrGroup1.add(new Entry<Integer,Integer>()
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
			this.atrGroup1 = new ArrayList<Entry<Integer,Integer>>(0);
		}
		String atrGroup2 = element.getAttribute("atrGroup2"); // 单条随机属性组2
		if (atrGroup2 != null && atrGroup2.length() > 0)
		{
			String[] atrGroup2Strings = atrGroup2.split(";"); 
			this.atrGroup2 = new ArrayList<Entry<Integer,Integer>>(atrGroup2Strings.length) ; 
			for (String string : atrGroup2Strings)
			{
			    String[] split = string.split(",");
			    if (split.length != 2) {
					throw new IllegalArgumentException(" 表： " + this.getClass().getSimpleName() + " id  : " + this.id + " 字段: "
							+ "atrGroup2" + "需为key-value格式，用逗号分隔");
				} 
				this.atrGroup2.add(new Entry<Integer,Integer>()
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
			this.atrGroup2 = new ArrayList<Entry<Integer,Integer>>(0);
		}
		String atrGroup3 = element.getAttribute("atrGroup3"); // 单条随机属性组3
		if (atrGroup3 != null && atrGroup3.length() > 0)
		{
			String[] atrGroup3Strings = atrGroup3.split(";"); 
			this.atrGroup3 = new ArrayList<Entry<Integer,Integer>>(atrGroup3Strings.length) ; 
			for (String string : atrGroup3Strings)
			{
			    String[] split = string.split(",");
			    if (split.length != 2) {
					throw new IllegalArgumentException(" 表： " + this.getClass().getSimpleName() + " id  : " + this.id + " 字段: "
							+ "atrGroup3" + "需为key-value格式，用逗号分隔");
				} 
				this.atrGroup3.add(new Entry<Integer,Integer>()
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
			this.atrGroup3 = new ArrayList<Entry<Integer,Integer>>(0);
		}
		String atrExtraGroup = element.getAttribute("atrExtraGroup"); // 不定条数随机属性组
		if (atrExtraGroup != null && atrExtraGroup.length() > 0)
		{
			String[] atrExtraGroupStrings = atrExtraGroup.split(";"); 
			this.atrExtraGroup = new ArrayList<Entry<Integer,Integer>>(atrExtraGroupStrings.length) ; 
			for (String string : atrExtraGroupStrings)
			{
			    String[] split = string.split(",");
			    if (split.length != 2) {
					throw new IllegalArgumentException(" 表： " + this.getClass().getSimpleName() + " id  : " + this.id + " 字段: "
							+ "atrExtraGroup" + "需为key-value格式，用逗号分隔");
				} 
				this.atrExtraGroup.add(new Entry<Integer,Integer>()
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
			this.atrExtraGroup = new ArrayList<Entry<Integer,Integer>>(0);
		}
		this.maxLv = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("maxLv")) ? "0"
			: element.getAttribute("maxLv")); // 强化上限
		String phaseLv = element.getAttribute("phaseLv"); // 阶段成长等级
		if (phaseLv != null && phaseLv.length() > 0)
		{
			String[] phaseLvStrings = phaseLv.split(";"); 
			this.phaseLv = new ArrayList<Integer>(phaseLvStrings.length) ; 
			for (int i = 0; i < phaseLvStrings.length; i++) 
			{
				Integer temp = Integer.valueOf(phaseLvStrings[i]);
				this.phaseLv.add(temp);
			}
		}
		else 
		{
			this.phaseLv = new ArrayList<Integer>(0);
		}
		this.atrLimit = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("atrLimit")) ? "0"
			: element.getAttribute("atrLimit")); // 最大属性条数
		this.atrNewOdd = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("atrNewOdd")) ? "0"
			: element.getAttribute("atrNewOdd")); // 阶段成长新属性概率
		String atrStrength = element.getAttribute("atrStrength"); // 阶段成长属性强化
		if (atrStrength != null && atrStrength.length() > 0)
		{
			String[] atrStrengthStrings = atrStrength.split(";"); 
			this.atrStrength = new ArrayList<Entry<Integer,Integer>>(atrStrengthStrings.length) ; 
			for (String string : atrStrengthStrings)
			{
			    String[] split = string.split(",");
			    if (split.length != 2) {
					throw new IllegalArgumentException(" 表： " + this.getClass().getSimpleName() + " id  : " + this.id + " 字段: "
							+ "atrStrength" + "需为key-value格式，用逗号分隔");
				} 
				this.atrStrength.add(new Entry<Integer,Integer>()
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
			this.atrStrength = new ArrayList<Entry<Integer,Integer>>(0);
		}
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String getDes()
	{
		return this.des;
	}
	
	public int getSubType()
	{
		return this.subType;
	}
	
	public int getProf()
	{
		return this.prof;
	}
	
	public int getQuarlity()
	{
		return this.quarlity;
	}
	
	public int getAsExp()
	{
		return this.asExp;
	}
	
	public int getSuit()
	{
		return this.suit;
	}
	
	public int getCombatPowerInit()
	{
		return this.combatPowerInit;
	}
	
	public int getCombatPowerGrow()
	{
		return this.combatPowerGrow;
	}
	
	public List<Entry<Integer,Integer>> getAtrInitNumb()
	{
		return this.atrInitNumb;
	}
	
	public List<Entry<Integer,Integer>> getAtrGroup1()
	{
		return this.atrGroup1;
	}
	
	public List<Entry<Integer,Integer>> getAtrGroup2()
	{
		return this.atrGroup2;
	}
	
	public List<Entry<Integer,Integer>> getAtrGroup3()
	{
		return this.atrGroup3;
	}
	
	public List<Entry<Integer,Integer>> getAtrExtraGroup()
	{
		return this.atrExtraGroup;
	}
	
	public int getMaxLv()
	{
		return this.maxLv;
	}
	
	public List<Integer> getPhaseLv()
	{
		return this.phaseLv;
	}
	
	public int getAtrLimit()
	{
		return this.atrLimit;
	}
	
	public int getAtrNewOdd()
	{
		return this.atrNewOdd;
	}
	
	public List<Entry<Integer,Integer>> getAtrStrength()
	{
		return this.atrStrength;
	}
	
}
