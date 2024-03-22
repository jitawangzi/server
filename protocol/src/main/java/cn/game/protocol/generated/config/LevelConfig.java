package cn.game.protocol.generated.config;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.apache.commons.lang.StringUtils;

/**
 * 关卡
 * 
 * 工具生成的，不要手动修改
 */
 public class LevelConfig 
{

	/** id */
	private int id;		
	/** 关卡名字 */
	private String name;		
	/** 点击关卡剧情 */
	private int story_id;		
	/** 战斗结束剧情 */
	private int finish_story_id;		
	/** 关卡类型 */
	private int cat;		
	/** 主线可重复 */
	private int repeatable;		
	/** 关卡难度 */
	private int levelLv;		
	/** 需求玩家等级 */
	private int needLv;		
	/** 体力消耗 */
	private int apCost;		
	/** 波数上限 */
	private int waveMax;		
	/** 每波延时 */
	private List<Integer> wave_delay;		
	/** 怪物展示 */
	private List<Integer> monsterShow;		
	/** 玩家获得经验 */
	private int playerExp;		
	/** 玩家获得铜币 */
	private int coin;		
	/** 上阵角色获得经验 */
	private int heroExp;		
	/** 星级血量 */
	private List<Integer> star;		
	/** 关卡首通奖励 */
	private List<Integer> levelDropFirst;		
	/** 关卡奖励 */
	private List<Integer> levelDrop;		

	public LevelConfig (Element element)
	{
	
		this.id = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("id")) ? "0"
			: element.getAttribute("id")); // id
		this.name = element.getAttribute("name"); // 关卡名字
		this.story_id = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("story_id")) ? "0"
			: element.getAttribute("story_id")); // 点击关卡剧情
		this.finish_story_id = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("finish_story_id")) ? "0"
			: element.getAttribute("finish_story_id")); // 战斗结束剧情
		this.cat = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("cat")) ? "0"
			: element.getAttribute("cat")); // 关卡类型
		this.repeatable = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("repeatable")) ? "0"
			: element.getAttribute("repeatable")); // 主线可重复
		this.levelLv = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("levelLv")) ? "0"
			: element.getAttribute("levelLv")); // 关卡难度
		this.needLv = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("needLv")) ? "0"
			: element.getAttribute("needLv")); // 需求玩家等级
		this.apCost = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("apCost")) ? "0"
			: element.getAttribute("apCost")); // 体力消耗
		this.waveMax = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("waveMax")) ? "0"
			: element.getAttribute("waveMax")); // 波数上限
		String wave_delay = element.getAttribute("wave_delay"); // 每波延时
		if (wave_delay != null && wave_delay.length() > 0)
		{
			String[] wave_delayStrings = wave_delay.split(";"); 
			this.wave_delay = new ArrayList<Integer>(wave_delayStrings.length) ; 
			for (int i = 0; i < wave_delayStrings.length; i++) 
			{
				Integer temp = Integer.valueOf(wave_delayStrings[i]);
				this.wave_delay.add(temp);
			}
		}
		else 
		{
			this.wave_delay = new ArrayList<Integer>(0);
		}
		String monsterShow = element.getAttribute("monsterShow"); // 怪物展示
		if (monsterShow != null && monsterShow.length() > 0)
		{
			String[] monsterShowStrings = monsterShow.split(";"); 
			this.monsterShow = new ArrayList<Integer>(monsterShowStrings.length) ; 
			for (int i = 0; i < monsterShowStrings.length; i++) 
			{
				Integer temp = Integer.valueOf(monsterShowStrings[i]);
				this.monsterShow.add(temp);
			}
		}
		else 
		{
			this.monsterShow = new ArrayList<Integer>(0);
		}
		this.playerExp = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("playerExp")) ? "0"
			: element.getAttribute("playerExp")); // 玩家获得经验
		this.coin = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("coin")) ? "0"
			: element.getAttribute("coin")); // 玩家获得铜币
		this.heroExp = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("heroExp")) ? "0"
			: element.getAttribute("heroExp")); // 上阵角色获得经验
		String star = element.getAttribute("star"); // 星级血量
		if (star != null && star.length() > 0)
		{
			String[] starStrings = star.split(";"); 
			this.star = new ArrayList<Integer>(starStrings.length) ; 
			for (int i = 0; i < starStrings.length; i++) 
			{
				Integer temp = Integer.valueOf(starStrings[i]);
				this.star.add(temp);
			}
		}
		else 
		{
			this.star = new ArrayList<Integer>(0);
		}
		String levelDropFirst = element.getAttribute("levelDropFirst"); // 关卡首通奖励
		if (levelDropFirst != null && levelDropFirst.length() > 0)
		{
			String[] levelDropFirstStrings = levelDropFirst.split(";"); 
			this.levelDropFirst = new ArrayList<Integer>(levelDropFirstStrings.length) ; 
			for (int i = 0; i < levelDropFirstStrings.length; i++) 
			{
				Integer temp = Integer.valueOf(levelDropFirstStrings[i]);
				this.levelDropFirst.add(temp);
			}
		}
		else 
		{
			this.levelDropFirst = new ArrayList<Integer>(0);
		}
		String levelDrop = element.getAttribute("levelDrop"); // 关卡奖励
		if (levelDrop != null && levelDrop.length() > 0)
		{
			String[] levelDropStrings = levelDrop.split(";"); 
			this.levelDrop = new ArrayList<Integer>(levelDropStrings.length) ; 
			for (int i = 0; i < levelDropStrings.length; i++) 
			{
				Integer temp = Integer.valueOf(levelDropStrings[i]);
				this.levelDrop.add(temp);
			}
		}
		else 
		{
			this.levelDrop = new ArrayList<Integer>(0);
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
	
	public int getStory_id()
	{
		return this.story_id;
	}
	
	public int getFinish_story_id()
	{
		return this.finish_story_id;
	}
	
	public int getCat()
	{
		return this.cat;
	}
	
	public int getRepeatable()
	{
		return this.repeatable;
	}
	
	public int getLevelLv()
	{
		return this.levelLv;
	}
	
	public int getNeedLv()
	{
		return this.needLv;
	}
	
	public int getApCost()
	{
		return this.apCost;
	}
	
	public int getWaveMax()
	{
		return this.waveMax;
	}
	
	public List<Integer> getWave_delay()
	{
		return this.wave_delay;
	}
	
	public List<Integer> getMonsterShow()
	{
		return this.monsterShow;
	}
	
	public int getPlayerExp()
	{
		return this.playerExp;
	}
	
	public int getCoin()
	{
		return this.coin;
	}
	
	public int getHeroExp()
	{
		return this.heroExp;
	}
	
	public List<Integer> getStar()
	{
		return this.star;
	}
	
	public List<Integer> getLevelDropFirst()
	{
		return this.levelDropFirst;
	}
	
	public List<Integer> getLevelDrop()
	{
		return this.levelDrop;
	}
	
}
