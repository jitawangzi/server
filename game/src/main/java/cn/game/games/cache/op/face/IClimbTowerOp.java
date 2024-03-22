package cn.game.games.cache.op.face;

public interface IClimbTowerOp {

	boolean loadDataFromCache();

	void save();

	boolean reset(int groupId);

	int getScore();

	boolean addScore(int add);

	int getLevel();

	int getLayer();

	boolean addLayer(int add);
	
	int getGroupId();

	int getMoney();

	boolean addMoney(int add);

	boolean delMoney(int del);

	boolean moneyEnough(int num);
	
	boolean setMoney(int value);

	int checkBattle(int battleLevelId);
	/**
	 * 加通关分
	 */
	boolean addClearanceScore();

	boolean shuffleBattle();
	
	boolean isAllLayerFinish();

	void battleWin();
	/**
	 * 是否参加过
	 * @return
	 */
	boolean isJoined();
	/**
	 * 是否已经初始化(数据库有数据)
	 * @return
	 */
	boolean isInitialized();
	/**
	 * 检查爬塔活动状态
	 * @return
	 */
	int checkState();
	/**
	 * 检查爬塔是否可以开始战斗
	 * @return
	 */
	int checkBattleStart();

	
	
	

}
