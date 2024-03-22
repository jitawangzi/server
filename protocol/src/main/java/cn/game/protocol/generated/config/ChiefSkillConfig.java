package cn.game.protocol.generated.config;
import org.w3c.dom.Element;
import org.apache.commons.lang.StringUtils;

/**
 * 主角技能
 * 
 * 工具生成的，不要手动修改
 */
 public class ChiefSkillConfig 
{

	/** 无意义 */
	private int id;		
	/** 属性类型 */
	private int prof_type;		
	/** 类型 */
	private int type;		
	/** 类型参数 */
	private int typePara;		
	/** 技能id */
	private int skill;		
	/** 需求玩家等级 */
	private int needLv;		
	/** 需求完成任务 */
	private int needQuest;		
	/** 主角角色 */
	private int chiefHero;		
	/** 职业必带 */
	private int profBind;		
	/** 占用cost */
	private int cost;		

	public ChiefSkillConfig (Element element)
	{
	
		this.id = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("id")) ? "0"
			: element.getAttribute("id")); // id
		this.prof_type = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("prof_type")) ? "0"
			: element.getAttribute("prof_type")); // 属性类型
		this.type = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("type")) ? "0"
			: element.getAttribute("type")); // 类型
		this.typePara = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("typePara")) ? "0"
			: element.getAttribute("typePara")); // 类型参数
		this.skill = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("skill")) ? "0"
			: element.getAttribute("skill")); // 技能id
		this.needLv = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("needLv")) ? "0"
			: element.getAttribute("needLv")); // 需求玩家等级
		this.needQuest = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("needQuest")) ? "0"
			: element.getAttribute("needQuest")); // 需求完成任务
		this.chiefHero = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("chiefHero")) ? "0"
			: element.getAttribute("chiefHero")); // 主角角色
		this.profBind = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("profBind")) ? "0"
			: element.getAttribute("profBind")); // 职业必带
		this.cost = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("cost")) ? "0"
			: element.getAttribute("cost")); // 占用cost
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public int getProf_type()
	{
		return this.prof_type;
	}
	
	public int getType()
	{
		return this.type;
	}
	
	public int getTypePara()
	{
		return this.typePara;
	}
	
	public int getSkill()
	{
		return this.skill;
	}
	
	public int getNeedLv()
	{
		return this.needLv;
	}
	
	public int getNeedQuest()
	{
		return this.needQuest;
	}
	
	public int getChiefHero()
	{
		return this.chiefHero;
	}
	
	public int getProfBind()
	{
		return this.profBind;
	}
	
	public int getCost()
	{
		return this.cost;
	}
	
}
