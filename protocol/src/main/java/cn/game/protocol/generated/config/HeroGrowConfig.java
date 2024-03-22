package cn.game.protocol.generated.config;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.apache.commons.lang.StringUtils;

/**
 * 角色星级成长
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroGrowConfig 
{

	/** id */
	private int id;		
	/** 初始：灵巧 */
	private List<Integer> strength_init;		
	/** 初始：知识 */
	private List<Integer> intellect_init;		
	/** 初始：魅力 */
	private List<Integer> perception_init;		
	/** 初始：专注 */
	private List<Integer> decisiveness_init;		
	/** 初始：勇气 */
	private List<Integer> courage_init;		
	/** 成长：灵巧 */
	private List<Integer> strength_growth;		
	/** 成长：知识 */
	private List<Integer> intellect_growth;		
	/** 成长：魅力 */
	private List<Integer> perception_growth;		
	/** 成长：专注 */
	private List<Integer> decisiveness_growth;		
	/** 成长：勇气 */
	private List<Integer> courage_growth;		
	/** 攻击力←灵巧 */
	private List<Float> attack_strength;		
	/** 攻击力←知识 */
	private List<Float> attack_intellect;		
	/** 攻击力←魅力 */
	private List<Float> attack_perception;		
	/** 攻击力←专注 */
	private List<Float> attack_decisiveness;		
	/** 攻击力←勇气 */
	private List<Float> attack_courage;		
	/** 暴击←灵巧 */
	private List<Float> critical_strength;		
	/** 会心←知识 */
	private List<Float> lucky_intellect;		
	/** 破甲←魅力 */
	private List<Float> armor_pene_perception;		
	/** 命中←专注 */
	private List<Float> hit_decisiveness;		
	/** 神勇←勇气 */
	private List<Float> brave_courage;		
	/** 角色初始攻击（播放）速度 */
	private List<Float> aspeed_init;		
	/** 资质 */
	private int aptitude;		
	/** 资质对战斗力的影响系数 */
	private float fc_coef;		
	/** 判断升星消耗碎片数量的类型，关联HeroStar表 */
	private int star_type;		

	public HeroGrowConfig (Element element)
	{
	
		this.id = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("id")) ? "0"
			: element.getAttribute("id")); // id
		String strength_init = element.getAttribute("strength_init"); // 初始：灵巧
		if (strength_init != null && strength_init.length() > 0)
		{
			String[] strength_initStrings = strength_init.split(";"); 
			this.strength_init = new ArrayList<Integer>(strength_initStrings.length) ; 
			for (int i = 0; i < strength_initStrings.length; i++) 
			{
				Integer temp = Integer.valueOf(strength_initStrings[i]);
				this.strength_init.add(temp);
			}
		}
		else 
		{
			this.strength_init = new ArrayList<Integer>(0);
		}
		String intellect_init = element.getAttribute("intellect_init"); // 初始：知识
		if (intellect_init != null && intellect_init.length() > 0)
		{
			String[] intellect_initStrings = intellect_init.split(";"); 
			this.intellect_init = new ArrayList<Integer>(intellect_initStrings.length) ; 
			for (int i = 0; i < intellect_initStrings.length; i++) 
			{
				Integer temp = Integer.valueOf(intellect_initStrings[i]);
				this.intellect_init.add(temp);
			}
		}
		else 
		{
			this.intellect_init = new ArrayList<Integer>(0);
		}
		String perception_init = element.getAttribute("perception_init"); // 初始：魅力
		if (perception_init != null && perception_init.length() > 0)
		{
			String[] perception_initStrings = perception_init.split(";"); 
			this.perception_init = new ArrayList<Integer>(perception_initStrings.length) ; 
			for (int i = 0; i < perception_initStrings.length; i++) 
			{
				Integer temp = Integer.valueOf(perception_initStrings[i]);
				this.perception_init.add(temp);
			}
		}
		else 
		{
			this.perception_init = new ArrayList<Integer>(0);
		}
		String decisiveness_init = element.getAttribute("decisiveness_init"); // 初始：专注
		if (decisiveness_init != null && decisiveness_init.length() > 0)
		{
			String[] decisiveness_initStrings = decisiveness_init.split(";"); 
			this.decisiveness_init = new ArrayList<Integer>(decisiveness_initStrings.length) ; 
			for (int i = 0; i < decisiveness_initStrings.length; i++) 
			{
				Integer temp = Integer.valueOf(decisiveness_initStrings[i]);
				this.decisiveness_init.add(temp);
			}
		}
		else 
		{
			this.decisiveness_init = new ArrayList<Integer>(0);
		}
		String courage_init = element.getAttribute("courage_init"); // 初始：勇气
		if (courage_init != null && courage_init.length() > 0)
		{
			String[] courage_initStrings = courage_init.split(";"); 
			this.courage_init = new ArrayList<Integer>(courage_initStrings.length) ; 
			for (int i = 0; i < courage_initStrings.length; i++) 
			{
				Integer temp = Integer.valueOf(courage_initStrings[i]);
				this.courage_init.add(temp);
			}
		}
		else 
		{
			this.courage_init = new ArrayList<Integer>(0);
		}
		String strength_growth = element.getAttribute("strength_growth"); // 成长：灵巧
		if (strength_growth != null && strength_growth.length() > 0)
		{
			String[] strength_growthStrings = strength_growth.split(";"); 
			this.strength_growth = new ArrayList<Integer>(strength_growthStrings.length) ; 
			for (int i = 0; i < strength_growthStrings.length; i++) 
			{
				Integer temp = Integer.valueOf(strength_growthStrings[i]);
				this.strength_growth.add(temp);
			}
		}
		else 
		{
			this.strength_growth = new ArrayList<Integer>(0);
		}
		String intellect_growth = element.getAttribute("intellect_growth"); // 成长：知识
		if (intellect_growth != null && intellect_growth.length() > 0)
		{
			String[] intellect_growthStrings = intellect_growth.split(";"); 
			this.intellect_growth = new ArrayList<Integer>(intellect_growthStrings.length) ; 
			for (int i = 0; i < intellect_growthStrings.length; i++) 
			{
				Integer temp = Integer.valueOf(intellect_growthStrings[i]);
				this.intellect_growth.add(temp);
			}
		}
		else 
		{
			this.intellect_growth = new ArrayList<Integer>(0);
		}
		String perception_growth = element.getAttribute("perception_growth"); // 成长：魅力
		if (perception_growth != null && perception_growth.length() > 0)
		{
			String[] perception_growthStrings = perception_growth.split(";"); 
			this.perception_growth = new ArrayList<Integer>(perception_growthStrings.length) ; 
			for (int i = 0; i < perception_growthStrings.length; i++) 
			{
				Integer temp = Integer.valueOf(perception_growthStrings[i]);
				this.perception_growth.add(temp);
			}
		}
		else 
		{
			this.perception_growth = new ArrayList<Integer>(0);
		}
		String decisiveness_growth = element.getAttribute("decisiveness_growth"); // 成长：专注
		if (decisiveness_growth != null && decisiveness_growth.length() > 0)
		{
			String[] decisiveness_growthStrings = decisiveness_growth.split(";"); 
			this.decisiveness_growth = new ArrayList<Integer>(decisiveness_growthStrings.length) ; 
			for (int i = 0; i < decisiveness_growthStrings.length; i++) 
			{
				Integer temp = Integer.valueOf(decisiveness_growthStrings[i]);
				this.decisiveness_growth.add(temp);
			}
		}
		else 
		{
			this.decisiveness_growth = new ArrayList<Integer>(0);
		}
		String courage_growth = element.getAttribute("courage_growth"); // 成长：勇气
		if (courage_growth != null && courage_growth.length() > 0)
		{
			String[] courage_growthStrings = courage_growth.split(";"); 
			this.courage_growth = new ArrayList<Integer>(courage_growthStrings.length) ; 
			for (int i = 0; i < courage_growthStrings.length; i++) 
			{
				Integer temp = Integer.valueOf(courage_growthStrings[i]);
				this.courage_growth.add(temp);
			}
		}
		else 
		{
			this.courage_growth = new ArrayList<Integer>(0);
		}
		String attack_strength = element.getAttribute("attack_strength"); // 攻击力←灵巧
		if (attack_strength != null && attack_strength.length() > 0)
		{
			String[] attack_strengthStrings = attack_strength.split(";"); 
			this.attack_strength = new ArrayList<Float>(attack_strengthStrings.length) ; 
			for (int i = 0; i < attack_strengthStrings.length; i++) 
			{
				Float temp = Float.valueOf(attack_strengthStrings[i])/10000f;
				this.attack_strength.add(temp);
			}
		}
		else 
		{
			this.attack_strength = new ArrayList<Float>(0);
		}
		String attack_intellect = element.getAttribute("attack_intellect"); // 攻击力←知识
		if (attack_intellect != null && attack_intellect.length() > 0)
		{
			String[] attack_intellectStrings = attack_intellect.split(";"); 
			this.attack_intellect = new ArrayList<Float>(attack_intellectStrings.length) ; 
			for (int i = 0; i < attack_intellectStrings.length; i++) 
			{
				Float temp = Float.valueOf(attack_intellectStrings[i])/10000f;
				this.attack_intellect.add(temp);
			}
		}
		else 
		{
			this.attack_intellect = new ArrayList<Float>(0);
		}
		String attack_perception = element.getAttribute("attack_perception"); // 攻击力←魅力
		if (attack_perception != null && attack_perception.length() > 0)
		{
			String[] attack_perceptionStrings = attack_perception.split(";"); 
			this.attack_perception = new ArrayList<Float>(attack_perceptionStrings.length) ; 
			for (int i = 0; i < attack_perceptionStrings.length; i++) 
			{
				Float temp = Float.valueOf(attack_perceptionStrings[i])/10000f;
				this.attack_perception.add(temp);
			}
		}
		else 
		{
			this.attack_perception = new ArrayList<Float>(0);
		}
		String attack_decisiveness = element.getAttribute("attack_decisiveness"); // 攻击力←专注
		if (attack_decisiveness != null && attack_decisiveness.length() > 0)
		{
			String[] attack_decisivenessStrings = attack_decisiveness.split(";"); 
			this.attack_decisiveness = new ArrayList<Float>(attack_decisivenessStrings.length) ; 
			for (int i = 0; i < attack_decisivenessStrings.length; i++) 
			{
				Float temp = Float.valueOf(attack_decisivenessStrings[i])/10000f;
				this.attack_decisiveness.add(temp);
			}
		}
		else 
		{
			this.attack_decisiveness = new ArrayList<Float>(0);
		}
		String attack_courage = element.getAttribute("attack_courage"); // 攻击力←勇气
		if (attack_courage != null && attack_courage.length() > 0)
		{
			String[] attack_courageStrings = attack_courage.split(";"); 
			this.attack_courage = new ArrayList<Float>(attack_courageStrings.length) ; 
			for (int i = 0; i < attack_courageStrings.length; i++) 
			{
				Float temp = Float.valueOf(attack_courageStrings[i])/10000f;
				this.attack_courage.add(temp);
			}
		}
		else 
		{
			this.attack_courage = new ArrayList<Float>(0);
		}
		String critical_strength = element.getAttribute("critical_strength"); // 暴击←灵巧
		if (critical_strength != null && critical_strength.length() > 0)
		{
			String[] critical_strengthStrings = critical_strength.split(";"); 
			this.critical_strength = new ArrayList<Float>(critical_strengthStrings.length) ; 
			for (int i = 0; i < critical_strengthStrings.length; i++) 
			{
				Float temp = Float.valueOf(critical_strengthStrings[i])/10000f;
				this.critical_strength.add(temp);
			}
		}
		else 
		{
			this.critical_strength = new ArrayList<Float>(0);
		}
		String lucky_intellect = element.getAttribute("lucky_intellect"); // 会心←知识
		if (lucky_intellect != null && lucky_intellect.length() > 0)
		{
			String[] lucky_intellectStrings = lucky_intellect.split(";"); 
			this.lucky_intellect = new ArrayList<Float>(lucky_intellectStrings.length) ; 
			for (int i = 0; i < lucky_intellectStrings.length; i++) 
			{
				Float temp = Float.valueOf(lucky_intellectStrings[i])/10000f;
				this.lucky_intellect.add(temp);
			}
		}
		else 
		{
			this.lucky_intellect = new ArrayList<Float>(0);
		}
		String armor_pene_perception = element.getAttribute("armor_pene_perception"); // 破甲←魅力
		if (armor_pene_perception != null && armor_pene_perception.length() > 0)
		{
			String[] armor_pene_perceptionStrings = armor_pene_perception.split(";"); 
			this.armor_pene_perception = new ArrayList<Float>(armor_pene_perceptionStrings.length) ; 
			for (int i = 0; i < armor_pene_perceptionStrings.length; i++) 
			{
				Float temp = Float.valueOf(armor_pene_perceptionStrings[i])/10000f;
				this.armor_pene_perception.add(temp);
			}
		}
		else 
		{
			this.armor_pene_perception = new ArrayList<Float>(0);
		}
		String hit_decisiveness = element.getAttribute("hit_decisiveness"); // 命中←专注
		if (hit_decisiveness != null && hit_decisiveness.length() > 0)
		{
			String[] hit_decisivenessStrings = hit_decisiveness.split(";"); 
			this.hit_decisiveness = new ArrayList<Float>(hit_decisivenessStrings.length) ; 
			for (int i = 0; i < hit_decisivenessStrings.length; i++) 
			{
				Float temp = Float.valueOf(hit_decisivenessStrings[i])/10000f;
				this.hit_decisiveness.add(temp);
			}
		}
		else 
		{
			this.hit_decisiveness = new ArrayList<Float>(0);
		}
		String brave_courage = element.getAttribute("brave_courage"); // 神勇←勇气
		if (brave_courage != null && brave_courage.length() > 0)
		{
			String[] brave_courageStrings = brave_courage.split(";"); 
			this.brave_courage = new ArrayList<Float>(brave_courageStrings.length) ; 
			for (int i = 0; i < brave_courageStrings.length; i++) 
			{
				Float temp = Float.valueOf(brave_courageStrings[i])/10000f;
				this.brave_courage.add(temp);
			}
		}
		else 
		{
			this.brave_courage = new ArrayList<Float>(0);
		}
		String aspeed_init = element.getAttribute("aspeed_init"); // 角色初始攻击（播放）速度
		if (aspeed_init != null && aspeed_init.length() > 0)
		{
			String[] aspeed_initStrings = aspeed_init.split(";"); 
			this.aspeed_init = new ArrayList<Float>(aspeed_initStrings.length) ; 
			for (int i = 0; i < aspeed_initStrings.length; i++) 
			{
				Float temp = Float.valueOf(aspeed_initStrings[i])/10000f;
				this.aspeed_init.add(temp);
			}
		}
		else 
		{
			this.aspeed_init = new ArrayList<Float>(0);
		}
		this.aptitude = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("aptitude")) ? "0"
			: element.getAttribute("aptitude")); // 资质
		this.fc_coef = Float.parseFloat(StringUtils.isEmpty(element.getAttribute("fc_coef")) ? "0"
			: element.getAttribute("fc_coef"))/10000f; // 资质对战斗力的影响系数
		this.star_type = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("star_type")) ? "0"
			: element.getAttribute("star_type")); // 判断升星消耗碎片数量的类型，关联HeroStar表
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public List<Integer> getStrength_init()
	{
		return this.strength_init;
	}
	
	public List<Integer> getIntellect_init()
	{
		return this.intellect_init;
	}
	
	public List<Integer> getPerception_init()
	{
		return this.perception_init;
	}
	
	public List<Integer> getDecisiveness_init()
	{
		return this.decisiveness_init;
	}
	
	public List<Integer> getCourage_init()
	{
		return this.courage_init;
	}
	
	public List<Integer> getStrength_growth()
	{
		return this.strength_growth;
	}
	
	public List<Integer> getIntellect_growth()
	{
		return this.intellect_growth;
	}
	
	public List<Integer> getPerception_growth()
	{
		return this.perception_growth;
	}
	
	public List<Integer> getDecisiveness_growth()
	{
		return this.decisiveness_growth;
	}
	
	public List<Integer> getCourage_growth()
	{
		return this.courage_growth;
	}
	
	public List<Float> getAttack_strength()
	{
		return this.attack_strength;
	}
	
	public List<Float> getAttack_intellect()
	{
		return this.attack_intellect;
	}
	
	public List<Float> getAttack_perception()
	{
		return this.attack_perception;
	}
	
	public List<Float> getAttack_decisiveness()
	{
		return this.attack_decisiveness;
	}
	
	public List<Float> getAttack_courage()
	{
		return this.attack_courage;
	}
	
	public List<Float> getCritical_strength()
	{
		return this.critical_strength;
	}
	
	public List<Float> getLucky_intellect()
	{
		return this.lucky_intellect;
	}
	
	public List<Float> getArmor_pene_perception()
	{
		return this.armor_pene_perception;
	}
	
	public List<Float> getHit_decisiveness()
	{
		return this.hit_decisiveness;
	}
	
	public List<Float> getBrave_courage()
	{
		return this.brave_courage;
	}
	
	public List<Float> getAspeed_init()
	{
		return this.aspeed_init;
	}
	
	public int getAptitude()
	{
		return this.aptitude;
	}
	
	public float getFc_coef()
	{
		return this.fc_coef;
	}
	
	public int getStar_type()
	{
		return this.star_type;
	}
	
}
