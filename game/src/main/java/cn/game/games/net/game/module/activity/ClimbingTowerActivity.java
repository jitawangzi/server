package cn.game.games.net.game.module.activity;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import cn.game.core.task.TaskManager;
import cn.game.games.cache.entity.ClimbingTower;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.manager.ActivityStateManager;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.GameConstants;
import cn.game.protocol.generated.config.PromotionRulesConfig;
import cn.game.protocol.generated.config.SettlementAwardConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.manager.PromotionRulesManager;
import cn.game.protocol.generated.manager.SettlementAwardManager;
import cn.game.protocol.protobuf.ActivityMsg.ActivityInfo;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**
 * 爬塔活动，需要细分一下开始阶段
 * 
 * @date 2021年6月9日 下午12:14:38
 * @author SYQ
 */
@ActivityType(type = ActivityTypeEnum.ClimbingTower)
public class ClimbingTowerActivity extends ActivityBase {

	/**分组  levelid- groupid- 组员*/
	private final Map<Integer, Multimap<Integer, ClimbingTower>> groupInfo = new HashMap<>();

	/**玩家是否可以参加活动*/
	private volatile boolean ready = false;

	private static final Comparator<ClimbingTower> TOWER_SCORE_RANK = new ScoreComparator();

	@Override
	public List<RewardInfo> receive(int id) {
		return null;
	}

	@Override
	public ActivityInfo buildActivityInfo() {
		return null;
	}
	@Override
	public void setEvents(EventTypeEnum[] events) {

	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	@Override
	public void startUp() {

		group();
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime end = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), GameConstants.TOWER_STARHOUR, GameConstants.TOWER_STARMINITE, 0);

		Duration duration = Duration.between(now, end);
		long millis = duration.toMillis();//相差毫秒数
		TaskManager.getInstance().scheduleGeneral(new Runnable() {
			@Override
			public void run() {
				setReady(true);
			}
		}, millis);

	}

	public void group() {
		groupInfo.clear();
		// 这里按段位初始化20人小组，玩家还不能参加任务，开启一个定时任务，标记玩家可以参加活动
		Map<Long, ClimbingTower> playerClimbingTowerData = ActivityStateManager.getInstance().getPlayerClimbingTowerData();
		if (playerClimbingTowerData.size() == 0) {
			return;
		}
		//List<ClimbingTower> collect = playerClimbingTowerData.values().stream().collect(Collectors.toList());
		List<ClimbingTower> collect = new ArrayList<>(playerClimbingTowerData.values());
		Collections.shuffle(collect);
		for (ClimbingTower tower : collect) {
			Multimap<Integer, ClimbingTower> integerListMap = groupInfo.get(tower.getLevel());
			if (integerListMap == null) {
				integerListMap = ArrayListMultimap.create();
				groupInfo.put(tower.getLevel(), integerListMap);
			}
			int groupId = integerListMap.size() / GameConstants.GROUPNUM_MAX + 1;
			tower.setGroupId(groupId);
			integerListMap.put(groupId, tower);
			//离线玩家保存组信息
			updateClimbingTower(tower);
		}
	}


	@Override
	public void shutDown() {
		this.setReady(false);
		// 重新计算段位  发奖励
		groupInfo.values().forEach(e -> {
			e.asMap().values().forEach(v -> Collections.sort((List<ClimbingTower>) v, TOWER_SCORE_RANK));
		} );

		for (Map.Entry<Integer, Multimap<Integer, ClimbingTower>> entry : groupInfo.entrySet()) {
			PromotionRulesConfig config = PromotionRulesManager.getInstance().getPromotionRulesConfig(entry.getKey());
			int riseInRank = config.getRiseInRank();
			int reduceInRank = config.getReduceInRank();

			for (Collection<ClimbingTower> climbingTowers : entry.getValue().asMap().values()) {
				int j = 1;
				for (ClimbingTower climbingTower : climbingTowers) {
					if (climbingTower.getScore() > 0 && j < riseInRank) {
						//升级
						climbingTower.setLevel(climbingTower.getLevel() + 1);
						//更库
						updateClimbingTower(climbingTower);
					}
					if (j > reduceInRank) {
						//降级
						climbingTower.setLevel(climbingTower.getLevel() - 1);
						//更库
						updateClimbingTower(climbingTower);
					}
					j++;
					SettlementAwardConfig settlementAwardConfig = SettlementAwardManager.getInstance().getSettlementAwardConfig(climbingTower.getLevel());
					List<Map.Entry<Integer, Integer>> reward = settlementAwardConfig.getReward();
					//邮箱发奖
					MailHelper.sendMailMultiLanguage(climbingTower.getPlayerId(), 208011, 208009, 208010, MailHelper.SYSTEM, reward);

				}
			}
		}
	}

	private void updateClimbingTower(ClimbingTower climbingTower) {
		int maxLevel = SettlementAwardManager.getInstance().list().size();
		int curLevel = climbingTower.getLevel();
		if (curLevel > maxLevel) {
			climbingTower.setLevel(maxLevel);
		}
		if (curLevel < 1) {
			climbingTower.setLevel(1);
		}
		
		//离线玩家更新爬塔数据
		long playerId = climbingTower.getPlayerId();
		if (!GameClientManager.getInstance().isOnline(playerId)) {
//			DAO.updateSelective(ClimbingTowerMapper.class, climbingTower);
		}
	}
	
	/** 销毁/重置玩家爬塔数据 */
	@Override
	public void destroy() {
		for (Map.Entry<Integer, Multimap<Integer, ClimbingTower>> entry : groupInfo.entrySet()) {
			for (Collection<ClimbingTower> climbingTowers : entry.getValue().asMap().values()) {
				for (ClimbingTower climbingTower : climbingTowers) {
					boolean noBattle  = climbingTower.getScore() == 0;
					boolean noSee = climbingTower.getBattleLevel().equals("");
					if (noBattle && noSee) {
						continue;
					}
					climbingTower.setScore(0);
					climbingTower.setLayer(1);
					climbingTower.setLayerFinish(false);
					climbingTower.setScoreTime(0L);
					climbingTower.setBattleLevel("");
					climbingTower.setGroupId(0);
					updateClimbingTower(climbingTower);
				}
			}
		}
	}
	
	
	public Collection<ClimbingTower> getGroup(int level, int groupId) {
		return groupInfo.get(level).get(groupId);
	}

	public Map<Integer, Multimap<Integer, ClimbingTower>> getGroupInfo() {
		return groupInfo;
	}


	private static class ScoreComparator implements Comparator<ClimbingTower>, Serializable {

		@Override
		public int compare(ClimbingTower o1, ClimbingTower o2) {
			if (o2.getScore() > o1.getScore()) {
				return 1;
			} else if (o2.getScore() < o1.getScore()) {
				return -1;
			}
			if (o2.getScoreTime() > o1.getScoreTime()) {
				return -1;
			} else if (o2.getScoreTime() < o1.getScoreTime()) {
				return 1;
			}
			return (int)(o1.getPlayerId() - o2.getPlayerId());
		}
	}

}
