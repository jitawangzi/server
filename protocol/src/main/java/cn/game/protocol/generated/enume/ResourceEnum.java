package cn.game.protocol.generated.enume;

/**
 * 资源枚举表
 * 
 * 工具生成的，不要手动修改
 */
public enum ResourceEnum{

	/** 玩家经验 */
	Exp(10001, "Exp", "玩家经验", "玩家升级经验", "resouce_teamexp", 1),
	/** 体力 */
	Brawn(10003, "Brawn", "体力", "可用于副本消耗", "resouce_power", 1),
	/** 元宝 */
	Gold(10004, "Gold", "元宝", "相当于钻石,外部充值商店的货币", "resouce_gold", 1),
	/** 金币 */
	Coin(10005, "Coin", "金币", "用于主城商店购买道具使用的通用货币", "resouce_coin", 1),
	/** 皮肤券 */
	SkinCoupon(10006, "SkinCoupon", "皮肤券", "通用资源,用于兑换商店资源", "icon_100006", 1),
	/** 勋章 */
	BpExp(10007, "BpExp", "勋章", "通行证升级经验", "icon_100007", 1),
	/** 行动力 */
	ActionPower(10008, "ActionPower", "行动力", "探索玩法消耗", "icon_100007", 1),
	/** 抽卡券 */
	DrawalCoupon(10009, "DrawalCoupon", "抽卡券", "抽卡用", "item_0_drawcoupons", 1),
	/** 爬塔代币 */
	TowerToken(10010, "TowerToken", "爬塔代币", "用于商店兑换奖励使用", "icon_100007", 1),
	/** 背包格子 */
	BagGrid(10011, "BagGrid", "背包格子", "携带背包数量", "resouce_gold", 1),
	/** 队伍经验 */
	TeamExp(10012, "TeamExp", "队伍经验", "用于探索中队伍升级使用的材料", "resouce_team_exp", 2),
	/** 水晶碎片 */
	Crystallize(10021, "Crystallize", "水晶碎片", "可用于升级角色的固定装备", "resouce_crystallize", 2),
	/** 战斗数据 */
	Intelligence(100022,"Intelligence","战斗数据","用于分析室中的职业属性提升的材料","resouce_intelligence",2),
	/** 石墨烯 */
	Graphene(100023,"Graphene","石墨烯","深水重工勋章","resouce_graphene",2),
	/** 碳纤维 */
	CarbonFibre(100024,"CarbonFibre","碳纤维","昨日联盟勋章","resouce_carbonFibre",2),
	/** 寄合质 */
	Zygote(100025,"Zygote","寄合质","群星乐园勋章","resouce_zygote",2),
	/** 钛合金 */
	Titanium(100026,"Titanium","钛合金","伊娜教会勋章","resouce_titanium",2),
	/** 异导物 */
	Heteroderivative(100027,"Heteroderivative","异导物","马戏团勋章","resouce_heteroDerivative",2),
	/** 碳素钢 */
	Steel(100028,"Steel","碳素钢","风铃群落勋章","resouce_steel",2),
	/** 超合金 */
	Superalloy(100029,"Superalloy","超合金","七海神社勋章","resouce_superalloy",2),
	/** 电子元件 */
	Superconducting(100030,"Superconducting","电子元件","用于升级探索续航建筑的属性使用","resouce_superConducting",2),
	/** 碳纳米管 */
	CarbonNanotubes(100031,"CarbonNanotubes","碳纳米管","用于提升续航能力的建筑的消耗材料","resouce_carbonNanotubes",2),
	/** 补给 */
	Supply(100032,"Supply","补给","探索中消耗步数的道具","resouce_supply",2),
	/** 韧性 */
	Toughness(100033,"Toughness","韧性","可在探索中用作强化探索装备的材料","resouce_Toughness",2),
	/** 积分 */
	Integral(100034,"Integral","积分","用于探索中在商店或与npc购买交互时使用","resouce_Integral",2),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 名称 */
	private String nameCn ; 
	/** 描述 */
	private String desc ; 
	/** 图标 */
	private String icon ; 
	/** 类型 */
	private int type ; 

	private ResourceEnum(int id, String name, String nameCn, String desc, String icon, int type) {
		this.id = id; 
		this.name = name; 
		this.nameCn = nameCn; 
		this.desc = desc; 
		this.icon = icon; 
		this.type = type; 
	}
	
	public static ResourceEnum get(int id) {
		ResourceEnum[] values = ResourceEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【ResourceEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static ResourceEnum getNullable(int id) {
		ResourceEnum[] values = ResourceEnum.values();
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
	public String getNameCn(){
		return this.nameCn;
	}
	public String getDesc(){
		return this.desc;
	}
	public String getIcon(){
		return this.icon;
	}
	public int getType(){
		return this.type;
	}
}
