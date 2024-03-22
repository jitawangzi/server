package cn.game.protocol.generated.config;
import org.w3c.dom.Element;
import org.apache.commons.lang.StringUtils;

/**
 * 账号/玩家等级
 * 
 * 工具生成的，不要手动修改
 */
 public class PlayerLevelConfig 
{

	/** id（等级） */
	private int id;		
	/** 至本级需求经验 */
	private int playExp2ThisLv;		
	/** 职业cost */
	private int cost;		
	/** 队伍槽位最大数量 */
	private int troopSlots;		
	/** 解锁职业 */
	private int UnlockProf;		

	public PlayerLevelConfig (Element element)
	{
	
		this.id = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("id")) ? "0"
			: element.getAttribute("id")); // id（等级）
		this.playExp2ThisLv = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("playExp2ThisLv")) ? "0"
			: element.getAttribute("playExp2ThisLv")); // 至本级需求经验
		this.cost = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("cost")) ? "0"
			: element.getAttribute("cost")); // 职业cost
		this.troopSlots = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("troopSlots")) ? "0"
			: element.getAttribute("troopSlots")); // 队伍槽位最大数量
		this.UnlockProf = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("UnlockProf")) ? "0"
			: element.getAttribute("UnlockProf")); // 解锁职业
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public int getPlayExp2ThisLv()
	{
		return this.playExp2ThisLv;
	}
	
	public int getCost()
	{
		return this.cost;
	}
	
	public int getTroopSlots()
	{
		return this.troopSlots;
	}
	
	public int getUnlockProf()
	{
		return this.UnlockProf;
	}
	
}
