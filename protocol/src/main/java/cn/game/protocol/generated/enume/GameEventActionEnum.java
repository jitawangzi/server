package cn.game.protocol.generated.enume;

/**
 * 游戏事件行为类型表
 * 
 * 工具生成的，不要手动修改
 */
public enum GameEventActionEnum{

	/** 播放剧情 */
	PlayPlot(1,"PlayPlot","播放剧情",false),
	/** 接取任务 */
	AcceptMission(2,"AcceptMission","接取任务",true),
	/** 播放队伍对话 */
	PlayTeamDialog(3,"PlayTeamDialog","播放队伍对话",false),
	/** 等待任务可交付 */
	WaitMissionCompleted(4,"WaitMissionCompleted","等待任务可交付",false),
	/** 执行命令 */
	ExecuteCommand(5,"ExecuteCommand","执行命令",true),
	/** 等待战斗完成 */
	WaitBattleFinish(6,"WaitBattleFinish","等待战斗完成",false),
	/** 领取任务奖励 */
	ReceiveMissionReward(7,"ReceiveMissionReward","领取任务奖励",false),
	/** 等待任务结束 */
	WaitMissionFinished(8,"WaitMissionFinished","等待任务结束",false),
	/** 弹出引导 */
	OpenGuideTip(9,"OpenGuideTip","弹出引导",false),
	/** 进入boss战斗 */
	EnterBossBattle(10,"EnterBossBattle","进入boss战斗",true),
	/** 等待进入战斗 */
	WaitEnterBattle(11,"WaitEnterBattle","等待进入战斗",false),
	/** 等待进入地点NPC范围 */
	WaitInArea(12,"WaitInArea","等待进入地点NPC范围",false),
	/** 等待进入某个地图 */
	WaitMap(13,"WaitMap","等待进入某个地图",false),
	/** 等待交互完成 */
	WaitInteractorFinish(14,"WaitInteractorFinish","等待交互完成",false),
	/** 等待战斗完成(不填表示等待任意战斗id完成,多填表示等待任意一个id完成即可继续) */
	WaitBossBattleFinish(15,"WaitBossBattleFinish","等待战斗完成(不填表示等待任意战斗id完成,多填表示等待任意一个id完成即可继续)",false),
	/** 进入某个地图 */
	EnterMap(16,"EnterMap","进入某个地图",false),
	/** 离开探索，不能弹评价 */
	ExitExplore(17,"ExitExplore","离开探索，不能弹评价",false),
	/** 进入主界面 */
	EnterMainMenu(18,"EnterMainMenu","进入主界面",false),
	/** 等待全部事件完成 */
	WaitAllEventsComplete(19,"WaitAllEventsComplete","等待全部事件完成",false),
	/** 等待任意一个事件完成 */
	WaitAnyEventComplete(20,"WaitAnyEventComplete","等待任意一个事件完成",false),
	/** 走向玩家 */
	GoPlayer(21,"GoPlayer","走向玩家",false),
	/** 离开UI */
	LeaveUI(22,"LeaveUI","离开UI",false),
	/** 弹出功能解锁提示 */
	OpenFeatureUnlockTips(23,"OpenFeatureUnlockTips","弹出功能解锁提示",false),
	/** 等待离开UI */
	WaitLeaveUI(24,"WaitLeaveUI","等待离开UI",false),
	/** 弹出toast */
	OpenToast(25,"OpenToast","弹出toast",false),
	/** 延时行为/秒 */
	DelayedSecond(26,"DelayedSecond","延时行为/秒",false),
	/** 通讯器提示 */
	MessengerPrompt(27,"MessengerPrompt","通讯器提示",false),
	/** 回收任务事件 */
	RecycleEvent(28,"RecycleEvent","回收任务事件",true),
	/** 手动领取任务奖励(多选1的情况) */
	ReceiveRewardsManually(29,"ReceiveRewardsManually","手动领取任务奖励(多选1的情况)",false),
	/** 等待剧情播完 */
	WaitAnyStoryFinish(30,"WaitAnyStoryFinish","等待剧情播完",false),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 名称 */
	private String desc ; 
	/** 是否需要服务器处理 */
	private boolean isServerProcess ; 

	private GameEventActionEnum(int id, String name, String desc, boolean isServerProcess) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
		this.isServerProcess = isServerProcess; 
	}
	
	public static GameEventActionEnum get(int id) {
		GameEventActionEnum[] values = GameEventActionEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【GameEventActionEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static GameEventActionEnum getNullable(int id) {
		GameEventActionEnum[] values = GameEventActionEnum.values();
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
	public boolean getIsServerProcess(){
		return this.isServerProcess;
	}
}
