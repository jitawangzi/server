package cn.game.protocol.generated.enume;

/**
 * 界面
 * 
 * 工具生成的，不要手动修改
 */
public enum InitialUI{

	/**  */
	Main(1001,0,"Main",1,0,"主界面",""),
	/**  */
	Passport(100101,1001,"Passport",4,0,"通行证",""),
	/**  */
	Passport1(1001011,100101,"Passport1",4,0,"通行证1",""),
	/**  */
	Supervalue(100102,1001,"Supervalue",4,0,"超值",""),
	/**  */
	Firstpayment(100103,1001,"Firstpayment",1,0,"首冲",""),
	/**  */
	Sevendays(100104,1001,"Sevendays",4,0,"7日",""),
	/**  */
	Letter(100105,1001,"Letter",4,0,"信件",""),
	/**  */
	Task(100106,1001,"Task",4,0,"任务",""),
	/**  */
	Ranking(100107,1001,"Ranking",5,0,"排行",""),
	/**  */
	PleaseGod(100108,1001,"PleaseGod",2,0,"请神",""),
	/**  */
	RandomBox(100109,1001,"RandomBox",3,0,"随机宝箱",""),
	/**  */
	ChapterBox(100110,1001,"ChapterBox",3,0,"章节宝箱",""),
	/**  */
	HangingUpp(100111,1001,"HangingUpp",5,0,"挂机",""),
	/**  */
	Build(100112,1001,"Build",10,0,"建造",""),
	/**  */
	CardMain(2001,0,"CardMain",3,0,"神将",""),
	/**  */
	CardLv(200101,2001,"CardLv",3,0,"神将升级",""),
	/**  */
	CardBreak(200102,2001,"CardBreak",5,0,"神将突破",""),
	/**  */
	CardTreasure(200103,2001,"CardTreasure",30,0,"神将法宝",""),
	/**  */
	CardBook(200104,2001,"CardBook",5,0,"神将图鉴",""),
	/**  */
	ThreeWorld(3001,0,"ThreeWorld",1,0,"三界",""),
	/**  */
	Consciousness(300101,3001,"Consciousness",1,0,"神元",""),
	/**  */
	Experience(4001,0,"Experience",1,0,"历练",""),
	/**  */
	Travel(400101,4001,"Travel",1,0,"游历",""),
	/**  */
	Elite(400102,4001,"Elite",30,0,"精英挑战",""),
	/**  */
	Boss(400103,4001,"Boss",30,0,"BOSS挑战",""),
	/**  */
	MiniGame(400104,4001,"MiniGame",30,0,"小游戏",""),
	/**  */
	Welfare(5001,0,"Welfare",1,0,"福利",""),
	/**  */
	ChapterGift(500101,5001,"ChapterGift",1,0,"章节礼包",""),
	/**  */
	Shop(500102,5001,"Shop",1,0,"黑市",""),
	/**  */
	GemChest(500103,5001,"GemChest",30,0,"玉珏宝箱",""),
	/**  */
	Recharge(500104,5001,"Recharge",1,0,"充值",""),
    ;
	/** 界面ID */
	public final int ID ; 
	/** 父ID */
	public final int ParentID ; 
	/** 模块名 */
	public final String Name ; 
	/** 显示等级 */
	public final int DisplayLevel ; 
	/** 锁或隐 0显示 1锁 2隐藏 */
	public final int LockHide ; 
	/** 界面名称 */
	public final String UIName ; 
	/** 图标路径 */
	public final String IconPath ; 

	private InitialUI(int ID, int ParentID, String Name, int DisplayLevel, int LockHide, String UIName, String IconPath) {
		this.ID = ID; 
		this.ParentID = ParentID; 
		this.Name = Name; 
		this.DisplayLevel = DisplayLevel; 
		this.LockHide = LockHide; 
		this.UIName = UIName; 
		this.IconPath = IconPath; 
	}
	
	public static InitialUI get(int id) {
		InitialUI[] values = InitialUI.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].ID == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【InitialUI】枚举表的" + "id【" + id + "】不存在");
	}

	public static InitialUI getNullable(int id) {
		InitialUI[] values = InitialUI.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].ID == id) {
				return values[i];
			}
		}
		return null;
	}

}
