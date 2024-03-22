package cn.game.protocol.generated.enume;

/**
 * 游戏事件触发类型
 * 
 * 工具生成的，不要手动修改
 */
public enum GameEventTriggerEnum{

	/** 无 */
	Null(99,"Null","无"),
	/** 与生成的资源交互 */
	NPCTalk(1,"NPCTalk","与生成的资源交互"),
	/** 到达范围 */
	ArrivedPlace(2,"ArrivedPlace","到达范围"),
	/** 进入探索地图 */
	EnterExploreMap(3,"EnterExploreMap","进入探索地图"),
	/** 进入战斗 */
	EnterBattle(4,"EnterBattle","进入战斗"),
	/** 进入当前地图 */
	EnterCurrentMap(5,"EnterCurrentMap","进入当前地图"),
	/** 投放就触发 */
	DirectTriggerEvent(6,"DirectTriggerEvent","投放就触发"),
	/** 进入界面 */
	EnterUI(7,"EnterUI","进入界面"),
	/** 离开界面 */
	LeaveUI(8,"LeaveUI","离开界面"),
	/** 首次进入二阶段 */
	FirstPhaseII(9,"FirstPhaseII","首次进入二阶段"),
	/** 首次星脉能量充满 */
	FirstEnergyFull(10,"FirstEnergyFull","首次星脉能量充满"),
	/** 首次策略点满 */
	FirstStrategyFull(11,"FirstStrategyFull","首次策略点满"),
	/** 首次等待进入boss战斗（倒计时为0） */
	FirstConfront(12,"FirstConfront","首次等待进入boss战斗（倒计时为0）"),
	/** 战斗完成 */
	WaitBattleFinish(13,"WaitBattleFinish","战斗完成"),
	/** 点击编队-调整队员按钮后触发 */
	AdjustTeamButton(14,"AdjustTeamButton","点击编队-调整队员按钮后触发"),
	/** 污染值大于x后触发 */
	PollutionValueRange(15,"PollutionValueRange","污染值大于x后触发"),
	/** 探索中补给有变化后触发 */
	SupplyChange(16,"SupplyChange","探索中补给有变化后触发"),
	/** 任意一场战斗种选择技能时触发 */
	ClickSkill(17,"ClickSkill","任意一场战斗种选择技能时触发"),
	/** 任意一场战斗中出现暴击(敌我双方) */
	TriggerCriticalStrike(18,"TriggerCriticalStrike","任意一场战斗中出现暴击(敌我双方)"),
	/** 任意一场战斗中出现异常状态(敌我双方) */
	TriggerException(19,"TriggerException","任意一场战斗中出现异常状态(敌我双方)"),
	/** 等待的事件开启时触发 */
	WaitEventOpenTrigger(20,"WaitEventOpenTrigger","等待的事件开启时触发"),
	/** 进入里世界 */
	EnterInWorld(21,"EnterInWorld","进入里世界"),
	/** 遇到boss战斗（倒计时为0） */
	RunIntoAnyBOSS(22,"RunIntoAnyBOSS","遇到boss战斗（倒计时为0）"),
	/** BOSS战斗完成 */
	FinishedAnyBOSS(23,"FinishedAnyBOSS","BOSS战斗完成"),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 名称 */
	private String desc ; 

	private GameEventTriggerEnum(int id, String name, String desc) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static GameEventTriggerEnum get(int id) {
		GameEventTriggerEnum[] values = GameEventTriggerEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【GameEventTriggerEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static GameEventTriggerEnum getNullable(int id) {
		GameEventTriggerEnum[] values = GameEventTriggerEnum.values();
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
