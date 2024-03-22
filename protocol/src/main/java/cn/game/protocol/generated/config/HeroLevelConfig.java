package cn.game.protocol.generated.config;
import org.w3c.dom.Element;
import org.apache.commons.lang.StringUtils;

/**
 * 角色升级
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroLevelConfig 
{

	/** id（等级） */
	private int id;		
	/** 至本级需求突破等级 */
	private int needUpgradeLv;		
	/** 至本级需求经验 */
	private int exp2ThisLv;		

	public HeroLevelConfig (Element element)
	{
	
		this.id = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("id")) ? "0"
			: element.getAttribute("id")); // id（等级）
		this.needUpgradeLv = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("needUpgradeLv")) ? "0"
			: element.getAttribute("needUpgradeLv")); // 至本级需求突破等级
		this.exp2ThisLv = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("exp2ThisLv")) ? "0"
			: element.getAttribute("exp2ThisLv")); // 至本级需求经验
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public int getNeedUpgradeLv()
	{
		return this.needUpgradeLv;
	}
	
	public int getExp2ThisLv()
	{
		return this.exp2ThisLv;
	}
	
}
