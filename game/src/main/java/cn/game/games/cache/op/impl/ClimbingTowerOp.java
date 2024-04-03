package cn.game.games.cache.op.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import cn.game.games.cache.entity.ClimbingTower;
import cn.game.games.cache.op.face.IClimbTowerOp;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.manager.ActivityStateManager;
import cn.game.games.net.game.manager.GameConstants;
import cn.game.games.net.game.module.activity.ActivityBase;
import cn.game.games.net.game.module.activity.ClimbingTowerActivity;
import cn.game.protocol.generated.config.OldGlobalConst;
import cn.game.protocol.generated.config.TowerLevelConfig;
import cn.game.protocol.generated.manager.TowerLevelManager;
import cn.game.protocol.manual.OldErrorMsgEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.Rnd;

public class ClimbingTowerOp extends BasePlayerModule implements IClimbTowerOp {

	private ClimbingTower climbingTower;

	@Override
	public void init() {

	}

	@Override
	public boolean loadDataFromCache() {
		ClimbingTower climbingTower = ActivityStateManager.getInstance().getClimbingTowerDataById(this.playerId);
		if (climbingTower != null) {
			this.climbingTower = climbingTower;
			return true;
		}

		return false;
	}

	@Override
	public void save() {
		if (climbingTower == null) {
			return;
		}
//		DAO.update(ClimbingTowerMapper.class, climbingTower);
	}

	@Override
	public boolean reset(int groupId) {
		climbingTower.setGroupId(groupId);
		return true;
	}

	@Override
	public boolean addMoney(int add) {
		if (add <= 0) {
			return false;
		}
		int newMoney = climbingTower.getMoney() + add;
		climbingTower.setMoney(newMoney);
		return true;
	}

	@Override
	public boolean delMoney(int del) {
		if (del <= 0) {
			return false;
		}
		int newMoney = climbingTower.getMoney() - del;
		climbingTower.setMoney(newMoney);
		return true;
	}

	@Override
	public boolean moneyEnough(int num) {
		if (num <= 0) {
			return false;
		}
		return climbingTower.getMoney() >= num;
	}

	@Override
	public boolean setMoney(int value) {
		if (value < 0) {
			return false;
		}
		climbingTower.setMoney(value);
		return true;
	}

	@Override
	public int getScore() {

		return climbingTower.getScore();
	}

	@Override
	public int getLevel() {

		return climbingTower.getLevel();
	}

	@Override
	public int getLayer() {

		return climbingTower.getLayer();
	}

	@Override
	public int getGroupId() {

		return climbingTower.getGroupId();
	}

	@Override
	public int getMoney() {

		return climbingTower.getMoney();
	}

	@Override
	public boolean addScore(int add) {
		if (add <= 0) {
			return false;
		}
		int newScore = getScore() + add;
		climbingTower.setScore(newScore);
		return true;
	}

	@Override
	public boolean addClearanceScore() {
		int curLayer = getLayer();
		TowerLevelConfig towerLevelConfig = TowerLevelManager.getInstance().getTowerLevelConfig(curLayer);
		int add = towerLevelConfig.getClearanceScore();
		addScore(add);
		return true;
	}

	@Override
	public boolean addLayer(int add) {
		if (add <= 0) {
			return false;
		}
		climbingTower.setScoreTime(System.currentTimeMillis());
		int curLayer = getLayer();
		Collection<TowerLevelConfig> list = TowerLevelManager.getInstance().list();
		int newLayer = curLayer + add;
		if (newLayer > list.size()) {
			climbingTower.setLayer(list.size());
			climbingTower.setLayerFinish(true);
			return true;
		}
		climbingTower.setLayer(newLayer);
		climbingTower.setLayerFinish(false);
		return true;
	}

	@Override
	public boolean shuffleBattle() {
		int curLayer = getLayer();
		TowerLevelConfig towerLevelConfig = TowerLevelManager.getInstance().getTowerLevelConfig(curLayer);
		List<Integer> battleLevels = towerLevelConfig.getBattleLevel();
		int battleCnt = OldGlobalConst.climbingTowerEachLayerBattleCount;
		if (battleLevels.size() < battleCnt) {
			throw new IllegalArgumentException("【TowerLevel】表的battleLevel配置数量不足");
		}
		Set<Integer> set = new HashSet<>(battleCnt);
		for (int i = 0; i < Rnd.RANDOM_MAX; i++) {
			int index = Rnd.get(0, battleLevels.size() - 1);
			set.add(battleLevels.get(index));

			if (set.size() == battleCnt) {
				climbingTower.setBattles(set);
				return true;
			}
		}

		if (set.size() != battleCnt) {
			throw new IllegalArgumentException("ClimbTowerOp shuffleBattle()方法有问题");
		}
		return false;
	}

	@Override
	public boolean isAllLayerFinish() {
		Collection<TowerLevelConfig> list = TowerLevelManager.getInstance().list();
		boolean isTop = list.size() == getLayer();
		boolean topFinish = climbingTower.getLayerFinish();
		return isTop && topFinish;
	}

	@Override
	public void battleWin() {
		addClearanceScore();
		addLayer(1);
		if (!isAllLayerFinish()) {
			shuffleBattle();
		}
	}

	@Override
	public boolean isJoined() {
		return this.climbingTower != null && climbingTower.getGroupId() != 0;
	}

	@Override
	public boolean isInitialized() {
		return this.climbingTower != null;
	}

	public int[] getBattles() {
		int[] battles = this.climbingTower.getBattles();
		if (battles.length == 0) {
			shuffleBattle();
		}
		return this.climbingTower.getBattles();
	}

	@Override
	public int checkBattle(int battleLevelId) {
		int state = checkState();
		if (state != 0) {
			return state;
		}
		int checkStart = checkBattleStart();
		if (checkStart != 0) {
			return checkStart;
		}
		if (isAllLayerFinish()) {
			return OldErrorMsgEnum.climbing_tower_all_pass.getId();
		}
		//如果没有初始化,先初始化
		getBattles();
		return climbingTower.checkBattle(battleLevelId);
	}
	
	@Override
	public int checkState() {
		// 未开启
		boolean isOpen = ActivityStateManager.getInstance().isOpen(GameConstants.TOWER_ACTIVITYID);
		if (!isOpen) {
			System.err.println("climbing_tower_not_open");
			return OldErrorMsgEnum.climbing_tower_not_open.getId();
		}
		// 未参加
		if (!isJoined()) {
			return OldErrorMsgEnum.climbing_tower_not_join.getId();
		}

		return 0;
	}

	@Override
	public int checkBattleStart() {
		ActivityBase activityBase = ActivityStateManager.getInstance().activityOp.get(GameConstants.TOWER_ACTIVITYID);
		if (!(activityBase instanceof ClimbingTowerActivity)) {
			return OldErrorMsgEnum.unknown.getId();
		}
		// 爬塔活动不能战斗
		ClimbingTowerActivity climbingTowerActivity = (ClimbingTowerActivity) activityBase;
		if (!climbingTowerActivity.isReady()) {
			return OldErrorMsgEnum.climbing_tower_not_start.getId();
		}
		return 0;
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleEvent(GameEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void initFromDb(ListIterator<?> iterator) {
		// TODO Auto-generated method stub


	}
	@Override
	public void initFromDbAfter() {

	};

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}

}
