package cn.game.protocol.generated.enume;

/**
 * buff生效时机枚举
 * 
 * 工具生成的，不要手动修改
 */
public enum BuffMomentEnum{

	/** 探索中经过一回合 */
	ExploreRound(1,"ExploreRound","探索中经过一回合"),
	/** 战斗 */
	BattleEnd(2,"BattleEnd","战斗"),
	/** 地图 */
	ExploreMapEnd(3,"ExploreMapEnd","地图"),
	/** 区域 */
	ExploreLevelEnd(4,"ExploreLevelEnd","区域"),
	/** 探索结束 */
	ExploreEnd(5,"ExploreEnd","探索结束"),
	/** 复活 */
	ExploreRoleResurrection(6,"ExploreRoleResurrection","复活"),
	/** 探索行动 */
	ExplorePlayerRound(7,"ExplorePlayerRound","探索行动"),
	/** 中立和怪物回合 */
	ExploreNpcRound(8,"ExploreNpcRound","中立和怪物回合"),
	/** 战斗中损失生命值 */
	ExploreWoundedInBattle(9,"ExploreWoundedInBattle","战斗中损失生命值"),
	/** 战斗胜利 */
	ExploreBattleWin(10,"ExploreBattleWin","战斗胜利"),
	/** 探索中购买道具 */
	ExploreExploreStoreBuy(11,"ExploreExploreStoreBuy","探索中购买道具"),
	/** 获取晶矿结晶 */
	ExploreMaterialReward(12,"ExploreMaterialReward","获取晶矿结晶"),
	/** 宝箱、遗骸获得物品 */
	ExploreBoxAndRemainsReward(13,"ExploreBoxAndRemainsReward","宝箱、遗骸获得物品"),
	/** 探索中获得金币 */
	ExploreGetCoin(14,"ExploreGetCoin","探索中获得金币"),
	/** 开始（添加） */
	Start(21,"Start","开始（添加）"),
	/** 结束（移除） */
	End(22,"End","结束（移除）"),
	/** 探索开始 */
	ExploreStart(23,"ExploreStart","探索开始"),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 时机描述 */
	private String desc ; 

	private BuffMomentEnum(int id, String name, String desc) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static BuffMomentEnum get(int id) {
		BuffMomentEnum[] values = BuffMomentEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【BuffMomentEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static BuffMomentEnum getNullable(int id) {
		BuffMomentEnum[] values = BuffMomentEnum.values();
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
