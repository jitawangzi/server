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
	Passport(100101,1001,"Passport",4,0,"通行证","Texture/Lobby/main_icon_passport_02"),
	/**  */
	Passport1(1001011,100101,"Passport1",4,0,"通行证1",""),
	/**  */
	Supervalue(100102,1001,"Supervalue",4,0,"超值","Texture/Lobby/main_icon_chaozhi"),
	/**  */
	Letter(100105,1001,"Letter",4,0,"信件","Texture/Lobby/main_icon_mail"),
	/**  */
	Task(100106,1001,"Task",4,0,"任务","Texture/Lobby/main_icon_quest"),
	/**  */
	Ranking(100107,1001,"Ranking",5,0,"排行","Texture/Lobby/main_icon_rank"),
	/**  */
	PleaseGod(100108,1001,"PleaseGod",2,0,"请神","Texture/Lobby/main_icon_qingshen"),
	/**  */
	RandomBox(100109,1001,"RandomBox",3,0,"随机宝箱","Texture/Lobby/main_icon_baoxiang"),
	/**  */
	ChapterBox(100110,1001,"ChapterBox",3,0,"章节宝箱","Texture/Lobby/main_icon_baoxiang"),
	/**  */
	HangingUpp(100111,1001,"HangingUpp",5,0,"挂机","Texture/Lobby/main_icon_guaji"),
	/**  */
	Build(100112,1001,"Build",30,0,"建造","Texture/common/main_icon_build"),
	/**  */
	CardMain(2001,0,"CardMain",2,0,"神将",""),
	/**  */
	CardLv(200101,2001,"CardLv",2,0,"神将升级",""),
	/**  */
	CardBreak(200102,2001,"CardBreak",2,0,"神将突破",""),
	/**  */
	CardTreasure(200103,2001,"CardTreasure",30,0,"神将法宝",""),
	/**  */
	CardBook(200104,2001,"CardBook",10,0,"神将图鉴",""),
	/**  */
	ThreeWorld(3001,0,"ThreeWorld",10,0,"三界",""),
	/**  */
	Consciousness(300101,3001,"Consciousness",10,0,"神元",""),
	/**  */
	Experience(4001,0,"Experience",10,0,"历练",""),
	/**  */
	Travel(400101,4001,"Travel",10,0,"游历",""),
	/**  */
	Elite(400102,4001,"Elite",30,0,"精英挑战",""),
	/**  */
	Boss(400103,4001,"Boss",30,0,"BOSS挑战",""),
	/**  */
	MiniGame(400104,4001,"MiniGame",30,0,"小游戏",""),
	/**  */
	DaoXinMoLi(400105,4001,"DaoXinMoLi",30,0,"道心磨砺",""),
	/**  */
	DaoXinLLiLian(4001051,400105,"DaoXinLLiLian",30,0,"道心历练",""),
	/**  */
	XinMoShiLian(4001052,400105,"XinMoShiLian",30,0,"心魔试炼",""),
	/**  */
	YaoWangBiePao(400106,4001,"YaoWangBiePao",30,0,"妖王别跑",""),
	/**  */
	ShiLuoZhenJing(400107,4001,"ShiLuoZhenJing",30,0,"失落真经",""),
	/**  */
	Welfare(5001,0,"Welfare",10,0,"福利",""),
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
