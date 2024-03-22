package cn.game.protocol.generated.enume;

/**
 * 命令枚举
 * 
 * 工具生成的，不要手动修改
 */
public enum GameCommandEnum{

	/** 全员增加属性值（负数减少） */
	AddValueAttribute(1,"AddValueAttribute","全员增加属性值（负数减少）"),
	/** 全员增加属性值百分比（负数减少） */
	AddPercentAttribute(2,"AddPercentAttribute","全员增加属性值百分比（负数减少）"),
	/** 奖励物品 */
	RewardItem(3,"RewardItem","奖励物品"),
	/** 添加NPC */
	AddNpc(4,"AddNpc","添加NPC"),
	/** 删除NPC */
	DelNpc(5,"DelNpc","删除NPC"),
	/** 增加怪物 */
	AddMonster(6,"AddMonster","增加怪物"),
	/** 删除怪物 */
	DelMonster(7,"DelMonster","删除怪物"),
	/** 禁止生成野怪 */
	ForbidFieldMonster(8,"ForbidFieldMonster","禁止生成野怪"),
	/** 禁止生成精英怪 */
	ForbidEliteMonster(9,"ForbidEliteMonster","禁止生成精英怪"),
	/** 取消禁止生成野怪 */
	RemoveForbidFieldMonster(10,"RemoveForbidFieldMonster","取消禁止生成野怪"),
	/** 取消禁止生成精英怪 */
	RemoveForbidEliteMonster(11,"RemoveForbidEliteMonster","取消禁止生成精英怪"),
	/** 替换场景资源地图id */
	ReplaceMapResourc(12,"ReplaceMapResourc","替换场景资源地图id"),
	/** 传送至地点 */
	Transform(13,"Transform","传送至地点"),
	/** 生成交互物 */
	Addlnteractor(14,"Addlnteractor","生成交互物"),
	/** 删除交互物 */
	Dellnteractor(15,"Dellnteractor","删除交互物"),
	/** 禁止进入某地图（0527未实现） */
	LockArea(22,"LockArea","禁止进入某地图（0527未实现）"),
	/** 解除禁止进入某地图（0527未实现） */
	UnlockArea(23,"UnlockArea","解除禁止进入某地图（0527未实现）"),
	/** 播放某个一般物体或可交互物体的动画 */
	PlayAnimation(16,"PlayAnimation","播放某个一般物体或可交互物体的动画"),
	/** 触发一场战斗 */
	BattleLevel(17,"BattleLevel","触发一场战斗"),
	/** 添加场景遮罩文件 */
	AddMapMask(18,"AddMapMask","添加场景遮罩文件"),
	/** 弹出提示文本 */
	PromptText(19,"PromptText","弹出提示文本"),
	/** 位置触发式剧情 */
	PlotInPos(20,"PlotInPos","位置触发式剧情"),
	/** 在某场景的某位播放指定动画 */
	PlayAnimationInMap(21,"PlayAnimationInMap","在某场景的某位播放指定动画"),
	/** 生成物体(具体地图具体位置) */
	AddExploreObjectPosition(107,"AddExploreObjectPosition","生成物体(具体地图具体位置)"),
	/** 生成物体(玩家范围生成) */
	AddExploreObjectRange(109,"AddExploreObjectRange","生成物体(玩家范围生成)"),
	/** 生成物体(以某个具体id范围生成) */
	AddExploreObjectCoordinate(111,"AddExploreObjectCoordinate","生成物体(以某个具体id范围生成)"),
	/** 删除背包中探索的剧情道具 */
	DelExploreItem(105,"DelExploreItem","删除背包中探索的剧情道具"),
	/** 删除地图中指定枚举类型的id */
	DelExploreObject(106,"DelExploreObject","删除地图中指定枚举类型的id"),
	/** 新增buff */
	AddBuff(102,"AddBuff","新增buff"),
	/** 新增天气 */
	addWeather(110,"addWeather","新增天气"),
	/** 随机扣除一种道具类型的道具 */
	DeductionRandomItem(120,"DeductionRandomItem","随机扣除一种道具类型的道具"),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 名称 */
	private String desc ; 

	private GameCommandEnum(int id, String name, String desc) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static GameCommandEnum get(int id) {
		GameCommandEnum[] values = GameCommandEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【GameCommandEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static GameCommandEnum getNullable(int id) {
		GameCommandEnum[] values = GameCommandEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		return null;
	}

	public int getId(){
		return this.id;
	}
	public String getName(){
		return this.name;
	}
	public String getDesc(){
		return this.desc;
	}
}
