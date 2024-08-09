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

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.cache.entity.BattleLevel;
import cn.game.games.cache.entity.Chapter;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.BattleLevelMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.BattleChapterConfig;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.BattleLevelConfig;
import cn.game.protocol.generated.config.PatrolConfig;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.BattleChapterManager;
import cn.game.protocol.generated.manager.BattleLevelManager;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.generated.manager.PatrolManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.protobuf.BattleMsg.DayChallengeInfo;
import cn.game.protocol.protobuf.BattleMsg.PatrolInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.ByteHelp;
import cn.game.util.DateUtil;
import cn.game.util.IntMapWrapper;

/**    
 * 战役、章
 * 2024年4月12日 下午4:29:43
 * @author SYQ
 */
public class ChapterModule extends BasePlayerModule  {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay, EventTypeEnum.LoginFinish,
			EventTypeEnum.FuncOpen, EventTypeEnum.ChapterFirstWin };

	private static final int[] REWARD_HOURS = { 6, 12, 18, 22 };

	/** 主线战役 */
	private Map<Integer, Chapter> chapters = new HashMap<>();;

	@Deprecated
	@JsonIgnore
	/** 打过的关卡数据,这里是统一的关卡id，关卡可能包括剧情关卡，普通关卡，战旗关卡等，id按段区分。 */
	private Map<Integer, BattleLevel> levels;

	/** 战役次数 */
	private IntMapWrapper dailyCount = new IntMapWrapper();

	/** 主线战役最高id,已通关的 */
	private int mainBattleHighest;
	/** 每日免费肉鸽刷新次数 */
	private int freeRougeTimes;

	/** 最后一次领取巡逻奖励的时间 */
	private int lastPatrolRewardTime;
	/** 每天的快速巡逻次数 */
	private int quickPatrolCount;
	/** 每天的广告巡逻次数 */
	private int adPatrolCount;
	/** 每天看广告并分享获得多倍战斗奖励的次数。 */
	private int battleRewardMultipleTimes;

	/** 上一次战斗获得的奖励，后续双倍用 */
	@JsonIgnore
	private List<RewardInfo> lastBattleRewards;

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
	/** 每日分享复活次数 */
	private int shareReliveCount;
	/** 每日广告复活次数 */
	private int adReliveCount;
	/** 单次战斗复活次数 */
	@JsonIgnore
	private int reliveCountPerBattle;

	/** 每日挑战数据 */
	@JsonIgnore
	@Deprecated
	private BattleDayChallenge dayChallenge = new BattleDayChallenge();
	@Deprecated
	@JsonIgnore
	private Map<Integer, DaoHeartBattle> daoBattleMap = new HashMap<Integer, DaoHeartBattle>();
	@Deprecated
	@JsonIgnore
	/** 梦魇秘境 */
	private MengYanMiJingBattle mengYanMiJingBattle;
	@JsonIgnore
	@Deprecated
	/** 灵魄之战 */
	private LingPoBattle lingPoBattle;

	/** 阵容数据，玩法-> 阵容顺序->阵容里面的角色 */
	private Map<Integer, Map<Integer, List<String>>> lineupMaps = new HashMap<Integer, Map<Integer, List<String>>>();

	/** 所有的战斗相关玩法数据 */
	private Map<Integer, IBattleHandler> battlesMap = new HashMap<Integer, IBattleHandler>();

	/** 
	 * 
	 * @param type
	 * @param lineupId  ，一般从0开始。 
	 * @param lineup
	 */
	public void updateLineup(int type, int lineupId, List<String> heroUids) {
		Map<Integer, List<String>> map = lineupMaps.get(type);
		if (map == null) {
			map = new HashMap<Integer, List<String>>();
			lineupMaps.put(type, map);
		}
		map.put(lineupId, new ArrayList<String>(heroUids));
	}

	public Map<Integer, List<String>> getLineups(int type) {
		return lineupMaps.get(type);
	}
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


	/** 
	 * 某战役是否完成过
	 * @param battleId
	 * @return
	 */
	public boolean isBattlePass(int battleId) {
		BattleConfig battleConfig = BattleManager.instance().get(battleId);
		int battleType = battleConfig.BattleType;
		if (battleType == DungeonTypeEnum.BattleChapter.getId()) {
			Chapter chapter = this.chapters.get(battleId);
			return chapter != null && chapter.getPass() != null && chapter.getPass();
		}
		if (battleType == DungeonTypeEnum.DaoHeart.getId() || battleType == DungeonTypeEnum.XinMo.getId() || battleType == DungeonTypeEnum.YaoWang.getId()) {
			DaoHeartBattle daoHeartBattle = getBattle(battleType);
			return daoHeartBattle != null && BattleHelper.isComplete(daoHeartBattle.getCompleteBattleId(), battleId);
		}
		if (battleType == DungeonTypeEnum.MengYanMiJing.getId()) {
			MengYanMiJingBattle battle = getBattle(battleType);
			return battle != null && BattleHelper.isComplete(battle.getCompleteBattleId(), battleId);
		}
		if (battleType == DungeonTypeEnum.ShiLuoZhenJing.getId()) {
			ShiLuoZhenJingBattle battle = getBattle(battleType);
			return battle != null && BattleHelper.isComplete(battle.getCompleteBattleId(), battleId);
		}
		return false;
	}

	/** 
	 * 某合成战役是否完成过
	 * @param battleId
	 * @return
	 */
	public boolean isHCBattlePass(int battleId) {
		Chapter chapter = this.chapters.get(battleId);
		return chapter != null && chapter.getPass();
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
	 * 计算巡逻n小时金币,当前在哪一关，也就是已经通关的下一关。 
	 * @param hours
	 * @return
	 */
	public int calcPatrolGold(int hours) {
		PatrolConfig patrolConfig = PatrolManager.instance().get(getFightMainBattleId());
		return patrolConfig.IncomeGold * 60 * hours;
	}

	/** 
	 * 获取可以打的，最新的战役id，最新解锁的，还没通关的
	 * @param type  {@link BattleConfig#BattleType} 
	 * @return
	 */
	public int getFightBattleId(int type) {
		int battleId = 0;
		List<BattleConfig> battleTypeList = BattleManager.instance().getBattleTypeList(type);
		for (BattleConfig battleConfig : battleTypeList) {
			if (battleConfig.preBattle == mainBattleHighest) {
				battleId = battleConfig.ID;
				break;
			}
		}
		return battleId;
	}

	/** 
	 * 获取主线战役可以打的最新的战役id
	 * @return
	 */
	public int getFightMainBattleId() {
		return getFightBattleId(DungeonTypeEnum.BattleChapter.getId());
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

	public int getAttackingId() {
		return this.id;
	}

	public List<RewardInfo> getLastBattleRewards() {
		return lastBattleRewards;
	}

	public void setLastBattleRewards(List<RewardInfo> lastBattleRewards) {
		this.lastBattleRewards = lastBattleRewards;
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

	public int getBattleRewardMultipleTimes() {
		return battleRewardMultipleTimes;
	}

	public void setBattleRewardMultipleTimes(int battleRewardMultipleTimes) {
		this.battleRewardMultipleTimes = battleRewardMultipleTimes;
	}

	public int getShareReliveCount() {
		return shareReliveCount;
	}

	public void setShareReliveCount(int shareReliveCount) {
		this.shareReliveCount = shareReliveCount;
	}

	public int getAdReliveCount() {
		return adReliveCount;
	}

	public void setAdReliveCount(int adReliveCount) {
		this.adReliveCount = adReliveCount;
	}
	public int getReliveCountPerBattle() {
		return reliveCountPerBattle;
	}

	public void setReliveCountPerBattle(int reliveCountPerBattle) {
		this.reliveCountPerBattle = reliveCountPerBattle;
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
		this.shareReliveCount = 0;
		this.adReliveCount = 0;
		this.battleRewardMultipleTimes = 0;
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
			MainBattle battle = new MainBattle();
			battle.setPlayer(player);
			battlesMap.put(DungeonTypeEnum.BattleChapter.getId(), battle);
			break;
		}
		case ChapterFirstWin: {
			LingPoBattle lingPoBattle = getBattle(DungeonTypeEnum.LingPo);
			if (lingPoBattle != null) {
				lingPoBattle.updateBattleId();
			}
			break;
		}
		case FuncOpen: {
			InitialUI func = event.getParameter(0);
			if (func == InitialUI.HangingUpp) {
				setPatrolRewardTime();
			} else if (func == InitialUI.DaoXinLLiLian) {
				DaoHeartBattle daoHeartBattle = new DaoHeartBattle(DungeonTypeEnum.DaoHeart.getId());
				daoHeartBattle.setPlayer(player);
				battlesMap.put(DungeonTypeEnum.DaoHeart.getId(), daoHeartBattle);
			} else if (func == InitialUI.XinMoShiLian) {
				DaoHeartBattle daoHeartBattle = new DaoHeartBattle(DungeonTypeEnum.XinMo.getId());
				daoHeartBattle.setPlayer(player);
				battlesMap.put(DungeonTypeEnum.XinMo.getId(), daoHeartBattle);
			} else if (func == InitialUI.YaoWangBiePao) {
				DaoHeartBattle daoHeartBattle = new DaoHeartBattle(DungeonTypeEnum.YaoWang.getId());
				daoHeartBattle.setPlayer(player);
				battlesMap.put(DungeonTypeEnum.YaoWang.getId(), daoHeartBattle);
			} else if (func == InitialUI.NightmareRealm) {
				MengYanMiJingBattle mengYanMiJingBattle = new MengYanMiJingBattle();
				mengYanMiJingBattle.init();
				mengYanMiJingBattle.setPlayer(player);
				battlesMap.put(DungeonTypeEnum.MengYanMiJing.getId(), mengYanMiJingBattle);

			} else if (func == InitialUI.SpiritBattle) {
				LingPoBattle lingPoBattle = new LingPoBattle();
				lingPoBattle.setPlayer(player);
				lingPoBattle.reset();
				battlesMap.put(DungeonTypeEnum.LingPo.getId(), lingPoBattle);

			} else if (func == InitialUI.ShiLuoZhenJing) {
				ShiLuoZhenJingBattle battle = new ShiLuoZhenJingBattle();
				battle.setPlayer(player);
				battle.reset();
				battlesMap.put(DungeonTypeEnum.ShiLuoZhenJing.getId(), battle);
			}
			break;
		}
		default:
			break;
		}
	}

	public <T extends IBattleHandler> T getBattle(DungeonTypeEnum type) {
		return (T) battlesMap.get(type.getId());
	}

	public <T extends IBattleHandler> T getBattle(int type) {
		return (T) battlesMap.get(type);
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
		this.battlesMap.forEach((k, v) -> {
			v.setPlayer(player);
		});
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
		builder.setPatrol(buildPatrolInfo());

		builder.addAllStoreStaminas(storeStaminas);
		builder.setMergeSweepTimes(daySweepCount);
		builder.setBattleRewardMultipleTimes(battleRewardMultipleTimes);
		builder.setShareReliveCount(shareReliveCount);
		builder.setAdReliveCount(adReliveCount);

		BattleDayChallenge dayChallenge = getBattle(DungeonTypeEnum.DayChallenge);
		if (dayChallenge != null) {
			DayChallengeInfo.Builder dayBuilder = DayChallengeInfo.newBuilder();
			dayBuilder.setBattleId(dayChallenge.getBattleId());
			dayBuilder.setBattleTimes(dayChallenge.getBattleTimes());
			dayBuilder.addAllRandomBuff(dayChallenge.getRandomBuff());
			dayBuilder.addAllRewardIndex(dayChallenge.getRewardIndex());
			builder.setMergeDayChallenge(dayBuilder.build());
		}
		builder.setBattleType(type) ; 
		builder.setBattleId(dungeonId);

	}

	public PatrolInfo buildPatrolInfo() {
		return PatrolInfo.newBuilder().setAdPatrolCount(adPatrolCount).setQuickPatrolCount(quickPatrolCount).setRewardTime(lastPatrolRewardTime).build();
	}
}
