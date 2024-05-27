package cn.game.games.net.game.module.battle;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.core.util.IdUtil;
import cn.game.games.cache.entity.BattleEventType;
import cn.game.games.cache.entity.BattleLevel;
import cn.game.games.cache.entity.BattleRandomEvent;
import cn.game.games.cache.entity.Chapter;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.BattleEventTypeMapper;
import cn.game.games.net.data.mapper.BattleLevelMapper;
import cn.game.games.net.data.mapper.BattleRandomEventMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.BattleChapterConfig;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.BattleEventConfig;
import cn.game.protocol.generated.config.BattleLevelConfig;
import cn.game.protocol.generated.config.EventRankIntervalConfig;
import cn.game.protocol.generated.config.EventTriggerConfig;
import cn.game.protocol.generated.config.OldGlobalConst;
import cn.game.protocol.generated.config.PatrolConfig;
import cn.game.protocol.generated.manager.BattleChapterManager;
import cn.game.protocol.generated.manager.BattleEventManager;
import cn.game.protocol.generated.manager.BattleLevelManager;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.generated.manager.EventRankIntervalManager;
import cn.game.protocol.generated.manager.EventTriggerManager;
import cn.game.protocol.generated.manager.PatrolManager;
import cn.game.protocol.protobuf.BattleMsg.DayChallengeInfo;
import cn.game.protocol.protobuf.BattleMsg.PatrolInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.ByteHelp;
import cn.game.util.DateUtil;
import cn.game.util.IntMapWrapper;
import cn.game.util.Rnd;

/**    
 * 战役、章
 * @date 2024年4月12日 下午4:29:43
 * @author SYQ
 */
public class ChapterModule extends BasePlayerModule  {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay, EventTypeEnum.LoginFinish };
	private static final int[] REWARD_HOURS = { 6, 12, 18, 22 };

	/** 主线战役 */
	private Map<Integer, Chapter> chapters = new HashMap<>();;

	@Deprecated
	@JsonIgnore
	/** 打过的关卡数据,这里是统一的关卡id，关卡可能包括剧情关卡，普通关卡，战旗关卡等，id按段区分。 */
	private Map<Integer, BattleLevel> levels;
	@Deprecated
	@JsonIgnore
	/** 产生的随机事件，过期没有通过的，或者成功通过的，不在此列表中 */
	private List<BattleRandomEvent> battleRandomEvents;
	@Deprecated
	@JsonIgnore
	/** 各种类型的随机事件，每天产生了多少次 */
	private Map<Integer, Integer> eventTypeMap;

	/** 战役次数 */
	private IntMapWrapper dailyCount = new IntMapWrapper();

	/** 主线战役最高id */
	private int mainBattleHighest;
	/** 每日免费肉鸽刷新次数 */
	private int freeRougeTimes;

	/** 最后一次领取巡逻奖励的时间 */
	private int lastPatrolRewardTime;
	/** 每天的快速巡逻次数 */
	private int quickPatrolCount;
	/** 每天的广告巡逻次数 */
	private int adPatrolCount;

	// 战斗相关数据
	private int type;
	private int dungeonId;
	private int id;
	private int lineupId;
	/** 子玩法的唯一id */
	@JsonIgnore
	private long uid;
	@JsonIgnore
	private long randomSeed;

	/** 储存的体力 （具体产生体力时的时间） */
	private List<Integer> storeStaminas = new ArrayList<>();

	/** 每日扫荡次数 */
	private int daySweepCount;

	/** 每日挑战数据 */
	private BattleDayChallenge dayChallenge = new BattleDayChallenge();


	public void addChapter(int battleId) {
		Chapter chapter = chapters.get(battleId);
		if (chapter == null) {
			chapter = Chapter.valueOf(playerId, battleId);
			chapters.put(chapter.getBattleId(), chapter);
		}
//		DAO.execute(ChapterMapper.class, MapperConstant.insert, chapter);
	}

	public void addChapterTimes(int battleId) {
		BattleConfig battleConfig = BattleManager.instance().get(battleId);
		/*		if (battleConfig.timesLimit.length > 0) {
					if (battleConfig.timesLimit[0] == 1) {
						dailyCount.add(battleConfig.BattleType, 1);
					}
				} else if (battleConfig.timesLimit[0] == 2) {
		
				}*/
	}

	public boolean checkChapterTimes(int battleId) {
		BattleConfig battleConfig = BattleManager.instance().get(battleId);
		boolean ret = true;
		/*		if (battleConfig.timesLimit.length > 0) {
					if (battleConfig.timesLimit[0] == 1) {
						ret = dailyCount.getValue(battleConfig.BattleType) < battleConfig.timesLimit[1];
					}
				} else if (battleConfig.timesLimit[0] == 2) {
		
				}*/
		return ret;
	}


	public boolean isBattlePass(int battleId) {
		Chapter chapter = this.chapters.get(battleId);
		return chapter != null && chapter.getPass();
	}

	public void insertBattleEvent(BattleRandomEvent event) {

//		DAO.execute(BattleRandomEventMapper.class, MapperConstant.insert,
//				event);
	}

	public void removeBattleEvent(long id) {

		for (int i = 0; i < battleRandomEvents.size(); i++) {
			if (battleRandomEvents.get(i).getId() == id) {
				battleRandomEvents.remove(i);
				break;
			}
		}
		DAO.execute(BattleRandomEventMapper.class, MapperConstant.deleteByPrimaryKey,
				id);

		if (battleRandomEvents.size() == OldGlobalConst.randomEventNumMax - 1) {

//			PlayerHelper.randomBattleEvent(playerId);
		}

	}
	public boolean hasBattleEvent(long id) {
		checkBattleEvent();
		for (int i = 0; i < battleRandomEvents.size(); i++) {
			if (battleRandomEvents.get(i).getId() == id) {
				return true;
			}
		}
		return false;
	}
	public BattleRandomEvent getBattleEvent(long id) {
		for (int i = 0; i < battleRandomEvents.size(); i++) {
			if (battleRandomEvents.get(i).getId() == id) { 
				return battleRandomEvents.get(i);
			}
		}
		return null;
	}

	public void updateBattleLevel(BattleLevel level) {
		DAO.execute(BattleLevelMapper.class, MapperConstant.updateByPrimaryKey,
				level);
	}

	public boolean isBattleLevelPass(int levelId) {
		return this.levels.get(levelId) != null;
	}
	public boolean isExploreActPass(int id) {

		return false;
	}

	/** 
	 * 计算巡逻n小时金币
	 * @param hours
	 * @return
	 */
	public int calcPatrolGold(int hours) {
		if (mainBattleHighest == 0) {
			return 0;
		}
		PatrolConfig patrolConfig = PatrolManager.instance().get(mainBattleHighest);
		return patrolConfig.IncomeGold * 60 * hours;
	}

	@Deprecated
	public boolean isChapterPass(int chapterId) {
		BattleChapterConfig battleChapterConfig = BattleChapterManager.getInstance().getBattleChapterConfig(chapterId);
		List<BattleLevelConfig> battleChapterIdList = BattleLevelManager.getInstance().getBattleChapterIdList(battleChapterConfig
				.getId());
		for (BattleLevelConfig config : battleChapterIdList) {
			if (!isBattleLevelPass(config.getId())) { 
				return false; 
			}
		}
		return true;
	}

	public void setAttackingData(int lineupId, int type, int dungeonId, int id, long uid, long randomSeed) {
		this.type = type;
		this.id = id;
		this.dungeonId = dungeonId;
		this.uid = uid;
		this.lineupId = lineupId;
		this.randomSeed = randomSeed;
	}

	public boolean isBattleStarted() {
		return this.id > 0;
	}

	public boolean addBattleLevelPass(int levelId, List<Integer> starList) {
		BattleLevel level = this.levels.get(levelId);
		if (level == null) {
			level = new BattleLevel();
			level.setPlayerId(playerId);
			level.setLevelId(levelId);
			if (starList != null) {
				level.setStar(ByteHelp.modifyBit(0, starList));
			}else {
				level.setStar(0);
			}
			DAO.execute(BattleLevelMapper.class, MapperConstant.insert, level);

			this.levels.put(levelId, level);
			return true;

		} else {
			if (starList != null) {
				int newStar = ByteHelp.modifyBit(level.getStar(), starList);
				if (newStar != level.getStar()) {
					level.setStar(newStar);
				}
				DAO.execute(BattleLevelMapper.class, MapperConstant.updateByPrimaryKey,
						level);
			}
			return false;

		}
	}
	public boolean addBattleLevelPass(int levelId) {
		return addBattleLevelPass(levelId, null);
	}

	public int getLineupId() {
		return this.lineupId;
	}

	public BattleLevel getBattleLevel(int levelId) {
		return this.levels.get(levelId);
	}

	public BattleLevel getBattleLevelAndInit(int levelId) {
		BattleLevel level = this.levels.get(levelId);
		if (level == null) {
			level = new BattleLevel();
			level.setLevelId(levelId);
			level.setPlayerId(playerId);
			level.setStar(0);
			DAO.execute(BattleLevelMapper.class, MapperConstant.insert, level);

			this.levels.put(levelId, level);
		}
		return level;
	}

	public int getAllStars() {

		int ret = 0;
		if (this.levels == null) {
			return ret;
		}
		for (BattleLevel level : this.levels.values()) {
			ret += ByteHelp.binary1Count(level.getStar());
		}
		return ret;
	}

	public BattleDayChallenge getDayChallenge() {
		return dayChallenge;
	}

	public void setDayChallenge(BattleDayChallenge dayChallenge) {
		this.dayChallenge = dayChallenge;
	}

	public int getAttackingId() {
		return this.id;
	}

	public int getStars(int zoneId) {

		int ret = 0;
		List<BattleLevelConfig> battleChapterIdList = BattleLevelManager.getInstance().getBattleChapterIdList(zoneId);
		for (BattleLevelConfig levelConfig : battleChapterIdList) {
			BattleLevel battleLevel = getBattleLevel(levelConfig.getId());
			if (battleLevel != null) {
				ret += ByteHelp.binary1Count(battleLevel.getStar());
			}
		}
		return ret;
	}

	public Chapter getChapter(int chapterId) {
		return this.chapters.get(chapterId);
	}
	public Collection<Chapter> listChapter() {
		return this.chapters.values();
	}

	public Collection<BattleLevel> listBattleLevels() {
		return this.levels.values();
	}

	public int getAttackingType() {
		return this.type;
	}
	public int getAttackingDungeonId() {
		return this.dungeonId;
	}

	public long getAttackingUid() {
		return this.uid;
	}

	public int getFreeRougeTimes() {
		return freeRougeTimes;
	}

	public void setFreeRougeTimes(int freeRougeTimes) {
		this.freeRougeTimes = freeRougeTimes;
	}

	public boolean checkProfession(long playerId, int profession, int lineupId, int type) {

		return true;
	}

	public int nextBattleEventTime() {

		checkBattleEvent();

		if (battleRandomEvents.size() >= OldGlobalConst.randomEventNumMax) {
			return -1; // 不需要产生事件
		}
		Player player = PlayerManager.getInstance().getPlayer(playerId);
//		long lastRandomEventTime = player.getData().getLastRandomEventTime();
//		if (lastRandomEventTime == 0) { 
//			return 0; 
//		}
		int time = (int) (System.currentTimeMillis() - 0);
		int nextTime = OldGlobalConst.randomEventInterval - time;
		return nextTime < 0 ? 0 : nextTime;
	}

	public boolean createBattleEvent() {
		checkBattleEvent();
		if (battleRandomEvents.size() >= OldGlobalConst.randomEventNumMax) {
			return false; 
		}

		Player player = PlayerManager.getInstance().getPlayer(playerId);
		int level = player.getData().getLevel();
//		long lastRandomEventTime = player.getData().getLastRandomEventTime();
		long now = System.currentTimeMillis();
//		if (lastRandomEventTime != 0 && now - lastRandomEventTime < GlobalConst.randomEventInterval) {
//			return false; 
//		}

//		int newChapter = getNewChapter();
//		// 可能现在还不能开章节,就先不产生事件
//		if (newChapter == 0) { 
//			return false;
//		}
		// 随机事件产生的位置
		byte max = 20;
		byte pos = (byte) Rnd.nextInt(max);

		boolean usablePos = false;
		for (byte i = 0; i < max; i++) {
			boolean has = false;
			for (BattleRandomEvent battleRandomEvent : battleRandomEvents) {
				if (battleRandomEvent.getPos() == pos) {
					has = true;
					break;
				}
			}
			if (has) {
				pos++;
				if (pos >= max) {
					pos = 0;
				}
			} else {
				usablePos = true;
				break;
			}
		}
		if (!usablePos) { // 地方不够用了，不能产生新的事件
			return false;
		}
		
		// 找出能产生事件的事件类型
		List<Integer> excludeIds = new ArrayList<Integer>();
		Collection<EventTriggerConfig> triggerList = EventTriggerManager.getInstance().list();
		for (EventTriggerConfig config : triggerList) {
			if (config.getMaxNum() <= 0) {
				continue;
			}
			Integer count = this.eventTypeMap.get(config.getId());
			if (count != null && count >= config.getMaxNum()) {
				excludeIds.add(config.getId());
			}
		}

		int rankLevlId = 0;
		Collection<EventRankIntervalConfig> list = EventRankIntervalManager.getInstance().list();
		for (EventRankIntervalConfig eventRankIntervalConfig : list) {
			if (level >= eventRankIntervalConfig.getMin() && level <= eventRankIntervalConfig.getMax()) {
				rankLevlId = eventRankIntervalConfig.getId();
				break;
			}
		}
		int randomId = BattleHelper.randomBattleEventType(excludeIds);
		int index = randomId * 100 + rankLevlId;
		// 玩家等级，和事件类型，能产生的所有随机事件
		List<BattleEventConfig> rankIntervalList = BattleEventManager.getInstance().getRankIntervalList(index);
		if (rankIntervalList == null || rankIntervalList.isEmpty()) {
			return false; 
		}
		BattleEventConfig battleEventConfig = rankIntervalList.get(Rnd.get(0, rankIntervalList.size() - 1));

		BattleRandomEvent event = new BattleRandomEvent();
		event.setId(IdUtil.getId());
		event.setCreateTime(now);
		event.setPlayerId(playerId);
		event.setChapterId(0);
		event.setRandomId(battleEventConfig.getId());
		event.setPos(pos);

		insertBattleEvent(event);
//		player.getData().setLastRandomEventTime(now);
		battleRandomEvents.add(event);

		// 更新事件类型次数
		int eventType = battleEventConfig.getType();
		Integer count = this.eventTypeMap.get(eventType);
		if (count == null) {
			this.eventTypeMap.put(eventType, 1);
			BattleEventType battleEventType = BattleEventType.valueOf(playerId, eventType, 1);
			DAO.execute(BattleEventTypeMapper.class, MapperConstant.insert,
					battleEventType);
		}else {
			int newCount = count + 1;
			this.eventTypeMap.put(eventType, newCount);

			BattleEventType battleEventType = BattleEventType.valueOf(playerId, eventType, newCount);
			DAO.execute(BattleEventTypeMapper.class, MapperConstant.updateByPrimaryKey,
					battleEventType);
		}

		if (battleRandomEvents.size() < OldGlobalConst.randomEventNumMax) {
//			PlayerHelper.randomBattleEvent(playerId);
		}

		return true;
	}

	public void checkBattleEvent() {

		long now = System.currentTimeMillis() ; 
		for (int i = 0; i < battleRandomEvents.size(); i++) {
			BattleRandomEvent event = battleRandomEvents.get(i);

			if (DateUtil.howLong(TimeUnit.MILLISECONDS, event.getCreateTime(), now) > OldGlobalConst.randomEventTimer) {
				// 如果在打，就不删除了，先保留，等打完再检查一遍
				if (uid == event.getId()) {
					continue;
				}
				removeBattleEvent(event.getId());
			}
		}
	}

	public int getNewChapter() {
		// 当前打过的章节里，如果没有通关的，就是最新章节 
		for (Chapter chapter : this.chapters.values()) {
			List<BattleLevelConfig> battleChapterIdList = BattleLevelManager.getInstance().getBattleChapterIdList(chapter
					.getBattleId());
			for (BattleLevelConfig levelConfig : battleChapterIdList) {
				if (levelConfig == null || levelConfig.getType() != 1) {
					continue;
				}
				boolean battleLevelPass = isBattleLevelPass(levelConfig.getId());
				if (!battleLevelPass) {
					return chapter.getBattleId();
				}
			}
		}
		
		// 如果打过的都通关了，没有打过的，并且可以解锁的，是最新章节
		Collection<BattleChapterConfig> list = BattleChapterManager.getInstance().list();
		for (BattleChapterConfig battleChapterConfig : list) {
			Chapter chapter = this.chapters.get(battleChapterConfig.getId());
			if (chapter != null) {
				continue;
			}
			boolean checkCondition = PlayerHelper.checkCondition(player, battleChapterConfig.getCondition());
			if (checkCondition) { return battleChapterConfig.getId(); }
		}
		
		return 0;
	}

	public boolean addStoreStaminas(int time) {
		if (storeStaminas.size() >= 60) {
			return false;
		}
		storeStaminas.add(time);
		return true;
	}

	public void updateStoreStaminas() {
		Instant instant = Instant.ofEpochMilli(player.getData().getOfflineTime());
		LocalDateTime lastOnlineTime = instant.atZone(ZoneOffset.UTC).toLocalDateTime();
		LocalDateTime currentOnlineTime = LocalDateTime.now();

		List<LocalDateTime> rewardTimes = calculateRewardTimes(lastOnlineTime, currentOnlineTime);

		for (LocalDateTime rewardTime : rewardTimes) {
			if (rewardTime.isAfter(lastOnlineTime) && rewardTime.isBefore(currentOnlineTime)) {
				boolean ret = addStoreStaminas((int) rewardTime.toEpochSecond(ZoneOffset.UTC));
				if (!ret) {
					break;
				}
			}
		}
	}

	private List<LocalDateTime> calculateRewardTimes(LocalDateTime from, LocalDateTime to) {
		List<LocalDateTime> rewardTimes = new ArrayList<>();
		LocalDateTime startOfDay = from.toLocalDate().atStartOfDay();

		for (LocalDateTime date = startOfDay; date.isBefore(to.plusDays(1)); date = date.plusDays(1)) {
			for (int hour : REWARD_HOURS) {
				LocalDateTime rewardTime = date.with(LocalTime.of(hour, 0));
				rewardTimes.add(rewardTime);
			}
		}

		return rewardTimes;
	}

	public List<BattleRandomEvent> listBattleEvents() {
		return this.battleRandomEvents;
	}


	public boolean isExploreChapterPass(int id) {
		Chapter chapter = chapters.get(id);
		return chapter != null && chapter.getPass();
	}

	public boolean exploreActReward(int id) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean exploreChapterReward(int id) {
		// TODO Auto-generated method stub
		return false;
	}

	public int getMainBattleHighest() {
		return mainBattleHighest;
	}

	public void setMainBattleHighest(int mainBattleHighest) {
		this.mainBattleHighest = mainBattleHighest;
	}

	public void setPatrolRewardTime() {
		this.lastPatrolRewardTime = DateUtil.currentTimeSeconds();
	}

	public int getLastPatrolRewardTime() {
		return lastPatrolRewardTime;
	}

	public int getQuickPatrolCount() {
		return quickPatrolCount;
	}

	public void setQuickPatrolCount(int quickPatrolCount) {
		this.quickPatrolCount = quickPatrolCount;
	}

	public int getAdPatrolCount() {
		return adPatrolCount;
	}

	public void setAdPatrolCount(int adPatrolCount) {
		this.adPatrolCount = adPatrolCount;
	}

	public List<Integer> getStoreStaminas() {
		return storeStaminas;
	}

	public int getDaySweepCount() {
		return daySweepCount;
	}

	public void setDaySweepCount(int daySweepCount) {
		this.daySweepCount = daySweepCount;
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	private void newDay() {
		this.freeRougeTimes = 0;
		this.quickPatrolCount = 0;
		this.adPatrolCount = 0;
		this.daySweepCount = 0;

		dayChallenge.reset();
	}
	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		case NewDay: {
			newDay();
			break;
		}
		case LoginFinish: {
			updateStoreStaminas();
			break;
		}
		case PLAYER_CREATE: {
			newDay();
			break;
		}
		default:
			break;
		}
	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
		return null;
	}

	@Override
	protected void initFromDb(ListIterator<?> iterator) {

	}
	@Override
	public void initFromDbAfter() {
		int now = DateUtil.currentTimeSeconds();
		Iterator<Integer> iterator = storeStaminas.iterator();
		while (iterator.hasNext()) {
			Integer time = (Integer) iterator.next();
			if (isStaminaExpire(now, time)) {
				iterator.remove();
			}
		}
	};

	public boolean isStaminaExpire(int now, int time) {
		return now - time > DateUtil.DAY_SECONDS * 7;
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		builder.setFreeRougeTimes(this.freeRougeTimes);

		for (Entry<Integer, Chapter> entry : chapters.entrySet()) {
			Chapter value = entry.getValue();
			builder.addBattles(value.toBattleInfo());
		}
		builder.setPatrol(
				PatrolInfo.newBuilder().setAdPatrolCount(adPatrolCount).setQuickPatrolCount(quickPatrolCount).setRewardTime(lastPatrolRewardTime).build());

		builder.addAllStoreStaminas(storeStaminas);
		builder.setMergeSweepTimes(daySweepCount);

		DayChallengeInfo.Builder dayBuilder = DayChallengeInfo.newBuilder();
		dayBuilder.setBattleId(dayChallenge.getBattleId());
		dayBuilder.setBattleTimes(dayChallenge.getBattleTimes());
		dayBuilder.addAllRandomBuff(dayChallenge.getRandomBuff());
		dayBuilder.addAllRewardIndex(dayChallenge.getRewardIndex());
		builder.setMergeDayChallenge(dayBuilder.build());
	}
}
