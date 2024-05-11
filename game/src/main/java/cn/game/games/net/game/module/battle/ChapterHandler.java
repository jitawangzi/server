package cn.game.games.net.game.module.battle;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Chapter;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.develop.AttrModule;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.BattleFieldConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.PatrolConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.BattleFieldManager;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.generated.manager.PatrolManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.ResourceConsumeEnum;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndResponse_13000004;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldStartRequest_13000001;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldStartResponse_13000002;
import cn.game.protocol.protobuf.BattleMsg.BattlePatrolRewardRequest_13000044;
import cn.game.protocol.protobuf.BattleMsg.BattlePatrolRewardResponse_13000045;
import cn.game.protocol.protobuf.BattleMsg.BattleRewardRequest_13000022;
import cn.game.protocol.protobuf.BattleMsg.BattleRewardResponse_13000023;
import cn.game.protocol.protobuf.BattleMsg.BattleRougeRefreshRequest_13000005;
import cn.game.protocol.protobuf.BattleMsg.BattleRougeRefreshResponse_13000006;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;

@Component
public class ChapterHandler extends BaseHandler {

	@Override
	protected int getModule() {
		return 0x13;
	}
	@Override
	protected void inititialize() {

		putInvoker(PbProtocol.BattleFieldStartRequest_13000001, (client, message) -> start(client, message));
		putInvoker(PbProtocol.BattleFieldEndRequest_13000003, (client, message) -> end(client, message));
//		putInvoker(PbProtocol.BattleFieldSweepRequest_13000005, (client, message) -> sweep(client, message));
//		putInvoker(PbProtocol.BattleChapterRewardRequest_13000022, (client, message) -> reward(client, message));
//		putInvoker(PbProtocol.ExploreActRewardRequest_13000020, (client, message) -> exploreActReward(client, message));
		putInvoker(PbProtocol.BattleRewardRequest_13000022, (client, message) -> chapterReward(client, message));
		putInvoker(PbProtocol.BattleRougeRefreshRequest_13000005, (client, message) -> rougeRefresh(client, message));
		putInvoker(PbProtocol.BattlePatrolRewardRequest_13000044, this::patrolReward);

	}

	protected void empty(NetClient client, Object message) {
		BattleFieldStartRequest_13000001 req = (BattleFieldStartRequest_13000001) message;
		BattleFieldStartResponse_13000002.Builder resp = BattleFieldStartResponse_13000002.newBuilder();


		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		ChapterModule chapterModule = player.getModule(ChapterModule.class);

		client.sendProtocol(resp);
	}

	private void patrolReward(NetClient client, Object message) {
		BattlePatrolRewardRequest_13000044 request = (BattlePatrolRewardRequest_13000044) message;
		boolean isFast = request.getIsFast();
		boolean advertising = request.getAdvertising();
		BattlePatrolRewardResponse_13000045.Builder resp = BattlePatrolRewardResponse_13000045.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		if (!player.isFuncOpen(InitialUI.HangingUpp)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}
//		int seconds = DateUtil.currentTimeSeconds() - playerModule.getLastPatrolRewardTime();
		if (advertising && !isFast) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
			return;
		}

		if (isFast) {
			// 快速巡逻次数处理
			if (advertising) {
				int quickPatrolCount = chapterModule.getAdPatrolCount();
				if (quickPatrolCount >= GlobalConst.AdPatrolCnt) {
					client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
					return;
				}
				// 消耗
				if (!PlayerHelper.delResources(player, GlobalConst.QuickPatrolConsume, ResourceConsumeEnum.None)) {
					client.sendProtocol(resp, ErrorMsgEnum.resource_not_enough.getId());
					return;
				}
				chapterModule.setAdPatrolCount(quickPatrolCount + 1);
				player.handleEvent(EventTypeEnum.WatchAds);

			} else {
				int quickPatrolCount = chapterModule.getQuickPatrolCount();
				if (quickPatrolCount >= GlobalConst.QuickPatrolCnt) {
					client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
					return;
				}
				// 消耗
				if (!PlayerHelper.delResources(player, GlobalConst.QuickPatrolConsume, ResourceConsumeEnum.None)) {
					client.sendProtocol(resp, ErrorMsgEnum.resource_not_enough.getId());
					return;
				}
				chapterModule.setQuickPatrolCount(quickPatrolCount + 1);
			}
		}
		// 巡逻时间
		int minute = 0;
		int hours = 0;

		if (isFast) {
			minute = GlobalConst.QuickPatrolDuration / 60;
			hours = minute / 60;
		} else {
			int seconds = DateUtil.currentTimeSeconds() - chapterModule.getLastPatrolRewardTime();
			if (seconds >= GlobalConst.MaximumPatrolDuration) {
				seconds = GlobalConst.MaximumPatrolDuration;
			}
			minute = seconds / 60;
			hours = minute / 60;
		}

		PatrolConfig patrolConfig = PatrolManager.instance().get(chapterModule.getMainBattleHighest());

		int exp = patrolConfig.IncomeEXP * minute;
		int gold = patrolConfig.IncomeGold * minute;

		PlayerHelper.addResources(player, Asset.playerExp.ID, exp);
		PlayerHelper.addResources(player, Asset.gold.ID, gold);
		for (int i = 0; i < hours; i++) {
			List<RewardInfo> reward = PlayerHelper.addReward(player, patrolConfig.IncomeRandomID);
			resp.addAllRewards(reward);
		}
		resp.setExp(exp);
		resp.setGold(gold);
		if (!isFast) {
			chapterModule.setPatrolRewardTime();
		}
		player.handleEvent(EventTypeEnum.Patrol, isFast);

		client.sendProtocol(resp);
	}

	protected void rougeRefresh(NetClient client, Object message) {
		BattleRougeRefreshRequest_13000005 req = (BattleRougeRefreshRequest_13000005) message;
		BattleRougeRefreshResponse_13000006.Builder resp = BattleRougeRefreshResponse_13000006.newBuilder();

		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		int freeRougeTimes = chapterModule.getFreeRougeTimes();
		if (freeRougeTimes < 3) {
			chapterModule.setFreeRougeTimes(freeRougeTimes + 1);
		} else { // TODO 看广告， 和最大次数

		}
		client.sendProtocol(resp);
	}

	/*protected void exploreActReward(NetClient client, Object message) {
	
		ExploreActRewardRequest_13000020 req = (ExploreActRewardRequest_13000020) message;
		ExploreActRewardResponse_13000021.Builder resp = ExploreActRewardResponse_13000021.newBuilder();
	
		int id = req.getId();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		long playerId = player.getPlayerId();
		chapterModule chapterModule = PlayerCacheFactory.getCache(playerId, chapterModule.class);
		boolean pass = chapterModule.isExploreActPass(id);
		if (!pass) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}
		ExploreAct exploreAct = chapterModule.getExploreAct(id);
		if (exploreAct != null && exploreAct.getReward()) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}
	
		boolean reward = chapterModule.exploreActReward(id);
		if (!reward) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}
		ExploreActConfig exploreActConfig = ExploreActManager.getInstance().getExploreActConfig(id);
	
		List<RewardItem> addRewards = PlayerHelper.addRewards(playerId, exploreActConfig.getProgressRewardId());
		resp.addAllReward(PbBuilder.buildRewardInfo(addRewards));
		client.sendProtocol(resp);
	
	}
	*/
	protected void chapterReward(NetClient client, Object message) {
	
		BattleRewardRequest_13000022 req = (BattleRewardRequest_13000022) message;
		BattleRewardResponse_13000023.Builder resp = BattleRewardResponse_13000023.newBuilder();
	
		int id = req.getId();
		int index = req.getIndex();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());

		if (!player.isFuncOpen(InitialUI.ChapterBox)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}
		long playerId = player.getPlayerId();
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		boolean pass = chapterModule.isExploreChapterPass(id);
		if (!pass) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}
		Chapter chapter = chapterModule.getChapter(id);
		List<Integer> rewards = chapter.getRewards();
		if (rewards.contains(index)) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}
		int minute =   chapter.getBattleTime() / 60 ; 
		BattleConfig battleConfig = BattleManager.instance().get(id);
		
		if (index == 0 && minute < battleConfig.BattleBoxTrigger[0]) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}
		if (index == 1 && minute < battleConfig.BattleBoxTrigger[1]) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}
		if (index == 2 && !chapter.getPass()) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}
		List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.BattleBoxRandomId[index]);
		rewards.add(index);
	
		resp.addAllReward(reward);

		client.sendProtocol(resp);
	
	}
	
	/*protected void reward(NetClient client, Object message) {
	
		BattleChapterRewardRequest_13000009 req = (BattleChapterRewardRequest_13000009) message;
		BattleChapterRewardResponse_1300000a.Builder resp = BattleChapterRewardResponse_1300000a.newBuilder();
	
		int chapterId = req.getId();
		int index = req.getIndex();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		long playerId = player.getPlayerId();
		chapterModule chapterModule = PlayerCacheFactory.getCache(playerId, chapterModule.class);
		Chapter chapter = chapterModule.getChapter(chapterId);
		BattleChapterConfig chapterConfig = BattleChapterManager.getInstance().getBattleChapterConfig(chapterId);
	
		int allStar = chapterModule.getStars(chapterId);
	
		List<Integer> stars = chapterConfig.getStar();
		int ret = -1;
		for (int i = 0; i < stars.size(); i++) {
			if (allStar >= stars.get(i)) {
				ret = i;
			}
		}
		if (ret < 0 || index > ret) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}
	
		Integer rewards = chapter.getRewards();
	
		boolean one = ByteHelp.isOne(rewards, index);
		if (one) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}
	
		int rewardId = chapterConfig.getStarRewardId().get(ret);
		List<RewardItem> addRewards = PlayerHelper.addRewards(playerId, rewardId);
		resp.addAllReward(PbBuilder.buildRewardInfo(addRewards));
		chapter.setRewards(ByteHelp.modifyBit(rewards, index));
		chapterModule.updateChapter(chapter);
	
		client.sendProtocol(resp);
	
	}*/
	protected void start(NetClient client, Object message) {
		BattleFieldStartRequest_13000001 req = (BattleFieldStartRequest_13000001) message;
		BattleFieldStartResponse_13000002.Builder resp = BattleFieldStartResponse_13000002.newBuilder();

		int dungeonId = req.getTypeId();
		int id = req.getFieldId();
		int type = req.getType();
//		String uidString = req.getUid();
//		long uid = StringUtils.isEmpty(uidString) ? 0 : Long.parseLong(uidString);
		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		
		BattleConfig battleConfig = BattleManager.instance().get(dungeonId);
		BattleFieldConfig levelConfig = BattleFieldManager.instance().get(id);
		/*		if (battleConfig.BattleFieldID != id) {
					client.sendProtocol(resp, ErrorMsgEnum.request_parameter_error.getId());
					return;
				}*/

		/*		int[] openDay = battleConfig.openDay;
				if (openDay.length > 0) {
					int dayOfWeek = DateUtil.getDayOfWeek();
					if (!GameUtil.contains(openDay, dayOfWeek)) {
						client.sendProtocol(resp, ErrorMsgEnum.not_open.getId());
						return;
					}
				}*/
		if (battleConfig.preBattle > 0 && !chapterModule.isBattlePass(battleConfig.preBattle)) {
			client.sendProtocol(resp, ErrorMsgEnum.BattleLevel_pre.getId());
			return;
		}
		if (!chapterModule.checkChapterTimes(dungeonId)) {
			client.sendProtocol(resp, ErrorMsgEnum.times_limit.getId());
			return;
		}

//		if (!PlayerHelper.checkCondition(playerId, battleConfig.enterCondtion)) {
//			client.sendProtocol(resp, ErrorMsgEnum.condition_check_error.getId());
//			return;
//		}

		if (!PlayerHelper.delResources(player, battleConfig.cost, ResourceConsumeEnum.None)) {
			client.sendProtocol(resp, ErrorMsgEnum.resource_not_enough.getId());
			return;
		}
//		long randomSeed = System.currentTimeMillis() ; 
		IBattleHandler battleHandler = BattleFactory.getBattleHandler(type);
		int errorCode = battleHandler.battleStart(playerId, type, dungeonId, id, 0, 0);
		if (errorCode == 0) {
			// 设置当前在打的关卡数据
			chapterModule.setAttackingData(0, type, dungeonId, id, 0, 0);
			// 触发事件
			player.handleEvent(EventTypeEnum.BattleStart, battleConfig.ID, 0);
		}
		//添加怪物图鉴
//		List<Integer> monsterSequence = levelConfig.getMonsterSequence();
//		for (Integer monster : monsterSequence) {
//			MonsterGroupConfig monsterGroupConfig = MonsterGroupManager.getInstance().getMonsterGroupConfig(monster);
//			List<Integer> monsterList = monsterGroupConfig.getMonsterList();
//			if (monsterList == null || monsterList.size() == 0) {
//				continue;
//			}
//			for (int i = 0; i < monsterList.size(); i++) {
//				MonsterConfig monsterConfig = MonsterManager.getInstance().getMonsterConfig(monsterList.get(i));
//				int landforms = monsterConfig.getLandforms();
//				if (landforms != -1) {
//					PlayerExt playerExt = PlayerManager.getInstance().getPlayerExt(playerId);
//					playerExt.addIllustrate(BuildingMsg.IllustrateType.MONSTER_VALUE, monsterList.get(i));
//				}
//			}
//		}

//		resp.setRandomSeed(randomSeed + "");
		AttrModule module = player.getModule(AttrModule.class); 
		module.calcAllAttr();
		resp.setAttrs(module.buildBattleAttrs());
		client.sendProtocol(resp, errorCode);
	}

	protected void end(NetClient client, Object message) {
		BattleFieldEndRequest_13000003 req = (BattleFieldEndRequest_13000003) message;
		BattleFieldEndResponse_13000004.Builder resp = BattleFieldEndResponse_13000004.newBuilder();
		boolean win = req.getWin(); 
//		String uidString = req.getUid();
//		long uid = StringUtils.isEmpty(uidString) ? 0 : Long.parseLong(uidString);
		long playerId = client.getPlayerId();
		int hpPercent = req.getHpPercent();
		int killMonsterCount = req.getKillMonsterCount();
		int killMonsterBossCount = req.getKillMonsterBossCount();

		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		int attackingId = chapterModule.getAttackingId();
		int attackingType = chapterModule.getAttackingType();
		long attackingUid = chapterModule.getAttackingUid();
		int attackingDungeonId = chapterModule.getAttackingDungeonId();
		int lineupId = chapterModule.getLineupId();
		if (attackingId == 0 || attackingType == 0) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}

//		List<BattleReportItemInfo> report = req.getReport().getItemsList();
		IBattleHandler battleHandler = BattleFactory.getBattleHandler(attackingType);
		int errorCode = battleHandler.battleEnd(playerId, req, resp);
		if (errorCode > 0) {
			client.sendProtocol(resp, errorCode);
			return;
		}
//		BattleLevelConfig levelConfig = BattleLevelManager.getInstance().getBattleLevelConfig(0);
//		int apCost = levelConfig.getEnergyExpend();

		// 检查体力消耗 探索的战斗不需要
//		if (type != DungeonTypeEnum.ExploreBattle.getId()) {
//			if (levelConfig != null && apCost > 0) {
//				PlayerHelper.delResources(player.getPlayerId(), ResourceEnum.Brawn.getId(),
//						levelConfig.getEnergyExpend(), ResourceConsumeEnum.OriginUnlock);
//			}
//		}
		
//		if (attackingType != DungeonTypeEnum.ExploreBattle.getId()) {
//			// 除了探索战斗，其他的战斗基本都用到了BattleField，计算体力消耗等等
//			if (win) {
//				List<Map.Entry<Integer, Integer>> starLevelReward = levelConfig.getStarLevelReward();
//				if (!starLevelReward.isEmpty()) {
//					//星级奖励 只有首次可以领
//					BattleLevel BattleField = chapterModule.getBattleLevel(id);
//					List<Integer> currstars = ByteHelp.binary1List(BattleField == null ? 0 : BattleField.getStar());
//					List<Map.Entry<Integer, Integer>> starRewards = new ArrayList<>();
//					for (Integer star : starList) {
//						if (!currstars.contains(star)) {
//							starRewards.add(starLevelReward.get(star));
//						}
//					}
//					resp.addAllStarRewards(PlayerHelper.addResources(player, starRewards));
//				}
//				
//				player.handleEvent(new GameEvent(EventTypeEnum.Level, player, attackingId, 5, 5));
//			} else {// 失败会返回一半体力,向下取
////				int energyExpend = levelConfig.getEnergyExpend();
////				if (energyExpend > 0) {
////					int lose = energyExpend / 2;
////					PlayerHelper.addResources(player, ResourceEnum.Brawn.getId()0, energyExpend - lose);
////					apCost = lose;
////				}
//			}
////			addExp(resp, player, lineupId, apCost);
//		} else { //探索战斗
//			if (win) {
//				//exploreOp.calcExploreBattleResource(levelConfig, lineupId, resp);
//				//加晋升点
//			}
//		}
		//基础奖励
//		resp.addAllCommonRewards(PlayerHelper.addResources(client.getPlayerId(), levelConfig.getBaseReward()));
		//随机奖励
//		int[] randomReward = levelConfig.getRandomReward();
//		Set<Pair<Integer, Integer>> rewards = new HashSet<>();
//		for (Integer reward : randomReward) {
//			List<RandomRewardConfig> randomGroupIdItemsByGroupId = RandomUtil.randomGroupIdItemsByGroupId(reward);
//			for (RandomRewardConfig config : randomGroupIdItemsByGroupId) {
//				rewards.add(new Pair<Integer, Integer>(config.getItemId(), config.getNum()));
//			}
//		}
////		List<RewardInfo> rewardItems = PlayerHelper.addResources(client.getPlayerId(), rewards);
////		resp.addAllRandomRewards(rewardItems);

		player.handleEvent(EventTypeEnum.BattleEnd, attackingDungeonId, attackingId, win, killMonsterCount, killMonsterBossCount);
		chapterModule.setAttackingData(0, 0, 0, 0, 0, 0);
		
		client.sendProtocol(resp);

	}
	
	/*private void addExp(BattleFieldEndResponse_13000004.Builder resp, Player player, int lineupId, int apCost) {
		if (apCost == 0) {
			return ; 
		}
		// 消耗体力就加经验
		long playerId = player.getPlayerId();
		int level = player.getLevel();
		int playerExp = BattleHelper.calcPlayerExp(level, apCost);
		PlayerHelper.addExp(player, playerExp);
		int coin = BattleHelper.calcCoin(level, apCost);
		PlayerHelper.addResources(player, ResourceEnum.Coin.getId(), coin);
	
		resp.setCoin(coin);
		resp.setPlayerExp(playerExp);
	}
	
	protected void sweep(NetClient client, Object message) {
		BattleFieldSweepRequest_13000005 req = (BattleFieldSweepRequest_13000005) message;
		BattleFieldSweepResponse_13000006.Builder resp = BattleFieldSweepResponse_13000006.newBuilder();
		int level = req.getId();
		int times = req.getTimes();
		int type = req.getType();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		chapterModule chapterModule = PlayerCacheFactory.getCache(player.getPlayerId(), chapterModule.class);
		BattleField level2 = chapterModule.getBattleField(level);
		BattleFieldConfig levelConfig = BattleFieldManager.getInstance().getBattleFieldConfig(level);
		if (levelConfig == null) {
			client.sendProtocol(resp, ErrorMsgEnum.config_data_not_found.getId());
			return;
		}
		if (level2 == null || ByteHelp.binary1Count(level2.getStar()) < levelConfig.getStarLevelCondition().size()) {
	
			client.sendProtocol(resp, ErrorMsgEnum.unknown.getId());
			return;
		}
	
		List<Entry<Integer, Integer>> levelDrop = levelConfig.getBaseReward();
	
		RoleOp heroOp = PlayerCacheFactory.getCache(player.getPlayerId(), RoleOp.class);
	
		LineupOp lineupOp = PlayerCacheFactory.getCache(player.getPlayerId(), LineupOp.class); //List<Integer> heroIds = lineupOp.getCurRoleIds();
		List<Role> heros = new ArrayList<>();
		//添加奖励
		if (type == DungeonTypeEnum.RoutineTraining.getId()) {
			if (player.getTrainingRewardTimes() > 0) {
				player.setTrainingRewardTimes(player.getTrainingRewardTimes() - 1);
				List<RewardItem> rewardItems = PlayerHelper.addResources(client.getPlayerId(), levelConfig.getSpecialReward());
				resp.addAllSpecialRewards(PbBuilder.buildRewardInfo(rewardItems));
			}
		}
		resp.addAllCommonRewards(PbBuilder.buildRewardInfo(PlayerHelper.addResources(client.getPlayerId(), levelConfig.getBaseReward())));
	
		client.sendProtocol(resp);
	}*/

}
