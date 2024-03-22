package cn.game.protocol.generated.config;
import org.w3c.dom.Element;
import org.apache.commons.lang.StringUtils;

/**
 * 装备强化/升级
 * 
 * 工具生成的，不要手动修改
 */
 public class EquipLevelConfig 
{

	/** id */
	private int id;		
	/** 品质 */
	private int quarlity;		
	/** 等级 */
	private int level;		
	/** 武器战力系数 */
	private float CPWeapon;		
	/** 装备战力系数 */
	private float CPEquip;		
	/** 至本级需求铜币 */
	private int coin2ThisLv;		
	/** 至本级需求装备经验 */
	private int equipExp2ThisLv;		

	public EquipLevelConfig (Element element)
	{
	
		this.id = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("id")) ? "0"
			: element.getAttribute("id")); // id
		this.quarlity = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("quarlity")) ? "0"
			: element.getAttribute("quarlity")); // 品质
		this.level = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("level")) ? "0"
			: element.getAttribute("level")); // 等级
		this.CPWeapon = Float.parseFloat(StringUtils.isEmpty(element.getAttribute("CPWeapon")) ? "0"
			: element.getAttribute("CPWeapon"))/10000f; // 武器战力系数
		this.CPEquip = Float.parseFloat(StringUtils.isEmpty(element.getAttribute("CPEquip")) ? "0"
			: element.getAttribute("CPEquip"))/10000f; // 装备战力系数
		this.coin2ThisLv = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("coin2ThisLv")) ? "0"
			: element.getAttribute("coin2ThisLv")); // 至本级需求铜币
		this.equipExp2ThisLv = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("equipExp2ThisLv")) ? "0"
			: element.getAttribute("equipExp2ThisLv")); // 至本级需求装备经验
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public int getQuarlity()
	{
		return this.quarlity;
	}
	
	public int getLevel()
	{
		return this.level;
	}
	
	public float getCPWeapon()
	{
		return this.CPWeapon;
	}
	
	public float getCPEquip()
	{
		return this.CPEquip;
	}
	
	public int getCoin2ThisLv()
	{
		return this.coin2ThisLv;
	}
	
	public int getEquipExp2ThisLv()
	{
		return this.equipExp2ThisLv;
	}
	
}
