package cn.game.protocol.generated.enume;

/**
 * 系统提示文本
 * 
 * 工具生成的，不要手动修改
 */
public enum TextMsgEnum{

	/** 行动开始，祝好运。 */
	noLastFloorStart(1,"noLastFloorStart","行动开始，祝好运。"),
	/** 这是最后一个区域，祝好运 */
	lastFloorStart(2,"lastFloorStart","这是最后一个区域，祝好运"),
	/** 已摧毁全部掩射器，准备进入映射域歼灭领主。 */
	firstPhaseEnd(3,"firstPhaseEnd","已摧毁全部掩射器，准备进入映射域歼灭领主。"),
	/** 检查队伍状态，准备与领主战斗。 */
	noLastFloorBossComeOn(4,"noLastFloorBossComeOn","检查队伍状态，准备与领主战斗。"),
	/** 这是本区域最后的战斗，请确保做好万全的准备。 */
	lastFloorBossComeOn(5,"lastFloorBossComeOn","这是本区域最后的战斗，请确保做好万全的准备。"),
	/** 营地已准备完毕，好好休息一下吧。 */
	noLastCanComeInCamp(6,"noLastCanComeInCamp","营地已准备完毕，好好休息一下吧。"),
	/** 做得很好，我们已经取得这一区域的胜利，随时可以返回主城。 */
	lastCanComeInCity(7,"lastCanComeInCity","做得很好，我们已经取得这一区域的胜利，随时可以返回主城。"),
	/** 获得新线索 */
	GetNewClue(101,"GetNewClue","获得新线索"),
	/** 你们好像触发了某种警报...请注意！ */
	PromptInformation01(102,"PromptInformation01","你们好像触发了某种警报...请注意！"),
	/** 检测到飞弹来袭，目标：你所在的地图，剩余时间15回合。请尽快离开！ */
	PromptInformation02(103,"PromptInformation02","检测到飞弹来袭，目标：你所在的地图，剩余时间15回合。请尽快离开！"),
	/** 侦测到高浓度环晶颗粒，侵蚀度上升速度加快，请注意！ */
	PromptInformation03(104,"PromptInformation03","侦测到高浓度环晶颗粒，侵蚀度上升速度加快，请注意！"),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 文本信息 */
	private String desc ; 

	private TextMsgEnum(int id, String name, String desc) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static TextMsgEnum get(int id) {
		TextMsgEnum[] values = TextMsgEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【TextMsgEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static TextMsgEnum getNullable(int id) {
		TextMsgEnum[] values = TextMsgEnum.values();
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
