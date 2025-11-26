package cn.game.games.net.game.module.battle;

import static java.util.stream.Collectors.toList;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.cache.entity.BattleLevel;
import cn.game.games.cache.entity.Chapter;
import cn.game.games.cache.entity.EquiptowerHelp;
import cn.game.games.cache.entity.Hero;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.data.mapper.BattleLevelMapper;
import cn.game.games.net.data.mapper.EquiptowerHelpMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.module.develop.hero.HeroModule;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.enume.QuestTypeEnum;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.protobuf.BattleMsg.BattleLineupInfo;
import cn.game.protocol.protobuf.BattleMsg.DayChallengeInfo;
import cn.game.protocol.protobuf.BattleMsg.LineupInfo;
import cn.game.protocol.protobuf.BattleMsg.PatrolInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;
import cn.game.util.IntMapWrapper;

/**    
 * 战役、章
 * 2024年4月12日 下午4:29:43
 * @author SYQ
 */
public class BattleModule extends BasePlayerModule  {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay, EventTypeEnum.LoginFinish,
			 EventTypeEnum.Relogin,	EventTypeEnum.FuncOpen, EventTypeEnum.ChapterFirstWin, EventTypeEnum.BattleStart, EventTypeEnum.BattleEnd, EventTypeEnum.GetItem   };

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
	private int attackingType;
	private int attackingId;
	private int attackingSubId;
	/** 子玩法的唯一id */
	@JsonIgnore
	private long attackingUid;
	private int lineupId;
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
	/** 每日广告全部肉鸽选择次数 */
	private int adRogueCount;
	/** 单次战斗复活次数 */
	@JsonIgnore
	private int reliveCountPerBattle;
	/** 单次战斗全部肉鸽选择次数*/
	@JsonIgnore
	private int adRogueCountPerBattle;

	/** 战斗选择的强援技能id */
	private int rescueSkillId;

	/** 阵容数据，玩法-> 阵容顺序->阵容里面的角色 */
	private Map<Integer, Map<Integer, List<String>>> lineupMaps = new HashMap<Integer, Map<Integer, List<String>>>();
	/** 当前使用的阵容，玩法-> 阵容顺序*/
	private Map<Integer, Integer> lineupChooseMaps = new HashMap<Integer, Integer>();

	/** 所有的战斗相关玩法数据 */
	private Map<Integer, IBattleHandler> battlesMap = new HashMap<Integer, IBattleHandler>();

	/** 领取过章奖励的  battleId */
	private List<Integer> battleChapterRewards = new ArrayList<>();
	
	/** 玩法类型--> 连续失败次数记录 */
	private IntMapWrapper consecutiveFailures = new IntMapWrapper();
	
	/** 本局内手操卡使用时没有被扣掉的次数 */
	private IntMapWrapper handCardUseCount = new IntMapWrapper() ;

	/** 
	 * 
	 * @param type
	 * @param lineupId  ，一般从0开始。 
	 */
	public void updateLineup(int type, int lineupId, List<String> heroUids) {
		Map<Integer, List<String>> map = lineupMaps.get(type);
		if (map == null) {
			map = new HashMap<Integer, List<String>>();
			lineupMaps.put(type, map);
		}
		map.put(lineupId, new ArrayList<String>(heroUids));

		if (type == DungeonTypeEnum.CHAPTER_TYPE_DA_DAO.getId()){
			player.getOfflineBattleModule().joinPlay();
		}
		player.handleEvent(EventTypeEnum.LineupUpdate,type);

	}

	public void updateLineupChoose(int type, int seq) {
		lineupChooseMaps.put(type, seq);
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
			return battle != null && BattleHelper.isComplete(battle.getMaxBattleId(), battleId);
		}
		if (battleType == DungeonTypeEnum.ShiLuoZhenJing.getId()) {
			ShiLuoZhenJingBattle battle = getBattle(battleType);
			return battle != null && BattleHelper.isComplete(battle.getCompleteBattleId(), battleId);
		}
		if (battleType == DungeonTypeEnum.XiangYaoFuMo.getId()) {
			XiangYaoFuMoBattle battle = getBattle(battleType);
			return battle != null && BattleHelper.isComplete(battle.getLastCompleteBattleId(), battleId);
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
		return battleId == 0 ? mainBattleHighest : battleId;
	}

	/** 
	 * 获取主线战役可以打的最新的战役id
	 * @return
	 */
	public int getFightMainBattleId() {
		return getFightBattleId(DungeonTypeEnum.BattleChapter.getId());
	}

	public void setAttackingData(int lineupId, int type, int id, int subId, long uid, long randomSeed) {
		this.attackingType = type;
		this.attackingId = id;
		this.attackingSubId = subId;
		this.attackingUid = uid;
		this.lineupId = lineupId;
		this.randomSeed = randomSeed;
	}

	public boolean isBattleStarted() {
		return this.attackingSubId > 0;
	}

	public int getLineupId() {
		return this.lineupId;
	}


	public List<RewardInfo> getLastBattleRewards() {
		return lastBattleRewards;
	}

	public void setLastBattleRewards(List<RewardInfo> lastBattleRewards) {
		this.lastBattleRewards = lastBattleRewards;
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
		return this.attackingType;
	}
	public int getAttackingId() {
		return this.attackingId;
	}
	public int getAttackingSubId() {
		return this.attackingSubId;
	}

	public long getAttackingUid() {
		return this.attackingUid;
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

	public boolean addStoreStaminas(int time) {
		if (storeStaminas.size() >= GlobalConst.RSGTreeEnergyFruitMax) {
			return false;
		}
		storeStaminas.add(time);
		return true;
	}

	public void updateStoreStaminas() {
		long offlineTime = player.getData().getOfflineTime(); 
		if (offlineTime == 0) {
			offlineTime = DateUtil.currentTimeMillis(); 
		}
		Instant instant = Instant.ofEpochMilli(offlineTime);

		LocalDateTime lastOnlineTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
		LocalDateTime currentOnlineTime = LocalDateTime.now();

		List<LocalDateTime> rewardTimes = calculateRewardTimes(lastOnlineTime, currentOnlineTime);

		for (LocalDateTime rewardTime : rewardTimes) {
			if (rewardTime.isAfter(lastOnlineTime) && rewardTime.isBefore(currentOnlineTime)) {
				boolean ret = addStoreStaminas((int) DateUtil.toEpochSecond(rewardTime));
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
			for (int hour : GlobalConst.RSGTreeEnergyHour) {
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

	public List<Integer> getBattleChapterRewards() {
		return battleChapterRewards;
	}

	public int getAdRogueCount() {
		return adRogueCount;
	}

	public void setAdRogueCount(int adRogueCount) {
		this.adRogueCount = adRogueCount;
	}

	public int getAdRogueCountPerBattle() {
		return adRogueCountPerBattle;
	}

	public void setAdRogueCountPerBattle(int adRogueCountPerBattle) {
		this.adRogueCountPerBattle = adRogueCountPerBattle;
	}

	public int getRescueSkillId() {
		return rescueSkillId;
	}

	public void setRescueSkillId(int rescueSkillId) {
		this.rescueSkillId = rescueSkillId;
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

		battlesMap.forEach((k, v) -> {
			v.newDay();
		});
	}
	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {
		case NewDay: {
			newDay();
			break;
		}
		case NewWeek: {
			PVEVPBattle p=getBattle(DungeonTypeEnum.PVEVPBattle);
			p.newWeek();
			DaShengXunShanBattle d=getBattle(DungeonTypeEnum.MountainBattle);
			d.newWeek();
			break;
		}
		case SystemTimeChange: {
			updateStoreStaminas();
			break;
		}
		case LoginFinish: {
			updateStoreStaminas();
			battlesMap.forEach((k, v) -> {
				v.onLogin();
			});
			// 如果没有主阵容的，初始化一个
			Map<Integer, List<String>> map = lineupMaps.get(DungeonTypeEnum.BattleChapter.getId());
			if (map == null || map.isEmpty()) {
				Set<Long> uids = player.getHeroModule().getUid_items().keySet();
				List<String> uidStrings = uids
						.stream()
						.map(String::valueOf)
						.limit(8)
						.collect(toList());
				updateLineup(DungeonTypeEnum.BattleChapter.getId(), 0, uidStrings);
				updateLineupChoose(DungeonTypeEnum.BattleChapter.getId(), 0);
            }
			break;
		}
		case Relogin: {
			battlesMap.forEach((k, v) -> {
				v.reLogin();
			});
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
			int battleId = event.getIntParameter(0); 
			if (battleId == 19902) {
				GameLogger.serverEvent(player, 10022);
			}
			break;
		}
		case BattleStart: {
			this.reliveCountPerBattle = 0;
			this.adRogueCountPerBattle = 0;
			this.handCardUseCount.clear();
			break;
		}
		case BattleEnd: {
			BattleConfig battleConfig = BattleManager.instance().get(event.get(0)); 
			boolean isWin = event.getBoolParameter(2); 
			if (!isWin) {
				consecutiveFailures.add(battleConfig.BattleType) ; 
			}else {
				consecutiveFailures.remove(battleConfig.BattleType) ; 
			}
			this.handCardUseCount.clear();
			break;
		}
		case GetItem: {
			int itemId = event.getParameter(0);
			int itemCount = event.getParameter(1);
			if(itemId == 101002) {
				long curCount = player.getCurrencyModule().get(101002).getCount() ; 
				EquipTowerBattle towerBattle = getBattle(DungeonTypeEnum.EquipTower);
				towerBattle.setRank(curCount);
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
				battlesMap.put(battle.getType(), battle);
			} else if (func == InitialUI.WorldBoss) {
				WorldBossBattle battle = new WorldBossBattle();
				battle.setPlayer(player);
				battlesMap.put(battle.getType(), battle);
			} else if (func == InitialUI.DemonsBoss) {
				XiangYaoFuMoBattle battle = new XiangYaoFuMoBattle();
				battle.setPlayer(player);
				battlesMap.put(battle.getType(), battle);
			} else if (func == InitialUI.Lingshan) {
				LingShanWenChanBattle battle = new LingShanWenChanBattle();
				battle.setPlayer(player);
				battlesMap.put(battle.getType(), battle);
			}else if (func == InitialUI.DragonTreasure) {
				TowerBattle battle = new TowerBattle();
				battle.setPlayer(player);
				battle.InitTowerBattle();
				battlesMap.put(battle.getType(), battle);
			}else if (func == InitialUI.EquipTower) {
				EquipTowerBattle battle = new EquipTowerBattle();
				battle.setPlayer(player);
				battle.initEquipBattle();
				player.getQuestModule().refreshQuest(QuestTypeEnum.EquipTower);
				battlesMap.put(battle.getType(), battle);
			}
			else if (func == InitialUI.DaSheng) {
				PVEVPBattle battle = new PVEVPBattle();
				battle.setPlayer(player);
				battle.initPvevpBattle();
				battlesMap.put(battle.getType(), battle);
			}	else if (func == InitialUI.PatrollMountain) {
				DaShengXunShanBattle battle = new DaShengXunShanBattle();
				battle.setPlayer(player);
				battle.initMountainData();
				battlesMap.put(battle.getType(), battle);
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
		// 先不过期。 
		return now - time > DateUtil.DAY_SECONDS * 7000;
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		builder.setFreeRougeTimes(this.freeRougeTimes);

		for (Entry<Integer, Chapter> entry : chapters.entrySet()) {
			Chapter value = entry.getValue();
			builder.addBattles(value.toBattleInfo());
		}
		builder.setPatrol(buildPatrolInfo());

//		builder.addAllStoreStaminas(storeStaminas);
		builder.setMergeSweepTimes(daySweepCount);
		builder.setBattleRewardMultipleTimes(battleRewardMultipleTimes);
		builder.setShareReliveCount(shareReliveCount);
		builder.setAdReliveCount(adReliveCount);
		builder.addAllBattleChapterIds(battleChapterRewards);

		BattleDayChallenge dayChallenge = getBattle(DungeonTypeEnum.DayChallenge);
		if (dayChallenge != null) {
			DayChallengeInfo.Builder dayBuilder = DayChallengeInfo.newBuilder();
			dayBuilder.setBattleId(dayChallenge.getBattleId());
			dayBuilder.setBattleTimes(dayChallenge.getBattleTimes());
			dayBuilder.addAllRandomBuff(dayChallenge.getRandomBuff());
			dayBuilder.addAllRewardIndex(dayChallenge.getRewardIndex());
			builder.setMergeDayChallenge(dayBuilder.build());
		}
		builder.setBattleType(attackingType);
		builder.setBattleId(attackingId);
		builder.setRescueSkillId(rescueSkillId);

		lineupMaps.forEach((k, v) -> {
			BattleLineupInfo.Builder lineupbuilder = BattleLineupInfo.newBuilder();
			lineupbuilder.setBattleType(k);
			v.forEach((k1, v1) -> {
				lineupbuilder.addLineups(LineupInfo.newBuilder().setSeq(k1).addAllHeroUid(v1));
			});
			lineupbuilder.setUseSeq(lineupChooseMaps.getOrDefault(k, 0));
			builder.addBattleLineups(lineupbuilder.build());
		});
	}

	public PatrolInfo buildPatrolInfo() {
		return PatrolInfo.newBuilder().setAdPatrolCount(adPatrolCount).setQuickPatrolCount(quickPatrolCount).setRewardTime(lastPatrolRewardTime).build();
	}


	@Override
	public Class<?>[] defaultDbMapperClass() {
		return new Class<?>[] { EquiptowerHelpMapper.class };
	}

	@Override
	public boolean alwaysStoreDataInStandaloneTable() {
		return true;
	}

	/**
	 * 战报
	 */
	private transient Map<Long, EquiptowerHelp> helpData = new HashMap<>();
	@Override
	protected void initFromDb(ListIterator<?> iterator) {
		List<EquiptowerHelp> equiptowerHelps = (List<EquiptowerHelp>) iterator.next();
		for (EquiptowerHelp help : equiptowerHelps) {
			this.helpData.put(help.getId(),help) ;
		}
		// 检查过期的
		int deltime =( int)(DateUtil.getDayTimeBySet(0, 0, 0)/1000);
		List<Long> deleteIds = new ArrayList<>();
		for (EquiptowerHelp help  : this.helpData.values()) {
			if (deltime > help.getExpiredTime()) {
				deleteIds.add(help.getId());
				help.delete();
			}
		}
		for (Long id : deleteIds) {
			helpData.remove( id);
		}
	}

    public Map<Long, EquiptowerHelp> getHelpData() {
        return helpData;
    }
    /** 
     * 获取某类型战斗连续失败次数
     * @param type
     * @return
     */
    public int getConsecutiveFailures(int type) {
    	if (type == 0) {
			return consecutiveFailures.sum(); 
		}
    	return consecutiveFailures.getValue(type);
    }
    public List<Hero> getDefaultLineupHeroes() {
    	Map<Integer, List<String>> map = this.lineupMaps.get(DungeonTypeEnum.BattleChapter.getId()); 
    	Integer orDefault = lineupChooseMaps.getOrDefault(DungeonTypeEnum.BattleChapter.getId(), 0); 
    	List<String> list = map.get(orDefault);
    	HeroModule heroModule = player.getHeroModule(); 
    	List<Hero> heros = new ArrayList<>();
    	
    	for (String uid : list) {
    		Hero hero = heroModule.get(Long.parseLong(uid)); 
    		heros.add(hero);
    	}
    	return heros;
    }
    
	public IntMapWrapper getHandCardUseCount() {
		return handCardUseCount;
	}

	@Override
	public int processOrder() {
		return EVENT_PROCESS_ORDER_HIGH;
	}
}
