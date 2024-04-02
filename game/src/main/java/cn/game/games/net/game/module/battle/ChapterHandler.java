package cn.game.games.net.game.module.battle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.base.PlayerCacheFactory;
import cn.game.games.cache.entity.BattleLevel;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.op.impl.ChapterOp;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.util.RandomUtil;
import cn.game.protocol.generated.config.BattleLevelConfig;
import cn.game.protocol.generated.config.RandomRewardConfig;
import cn.game.protocol.generated.enume.DungeonTypeEnum;
import cn.game.protocol.generated.manager.BattleLevelManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.BattleMsg.BattleChapterRewardRequest_13000022;
import cn.game.protocol.protobuf.BattleMsg.BattleChapterRewardResponse_13000023;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndResponse_13000004;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldStartRequest_13000001;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.ByteHelp;
import cn.game.util.Pair;

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
		putInvoker(PbProtocol.BattleChapterRewardRequest_13000022, (client, message) -> chapterReward(client, message));

	}

	/*protected void exploreActReward(NetClient client, Object message) {
	
		ExploreActRewardRequest_13000020 req = (ExploreActRewardRequest_13000020) message;
		ExploreActRewardResponse_13000021.Builder resp = ExploreActRewardResponse_13000021.newBuilder();
	
		int id = req.getId();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		long playerId = player.getPlayerId();
		ChapterOp chapterOp = PlayerCacheFactory.getCache(playerId, ChapterOp.class);
		boolean pass = chapterOp.isExploreActPass(id);
		if (!pass) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}
		ExploreAct exploreAct = chapterOp.getExploreAct(id);
		if (exploreAct != null && exploreAct.getReward()) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}
	
		boolean reward = chapterOp.exploreActReward(id);
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
	
		BattleChapterRewardRequest_13000022 req = (BattleChapterRewardRequest_13000022) message;
		BattleChapterRewardResponse_13000023.Builder resp = BattleChapterRewardResponse_13000023.newBuilder();
	
		int id = req.getId();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		long playerId = player.getPlayerId();
		ChapterOp chapterOp = PlayerCacheFactory.getCache(playerId, ChapterOp.class);
		boolean pass = chapterOp.isExploreChapterPass(id);
		if (!pass) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}
	
//		ExploreChapterComplete exploreChapter = chapterOp.getExploreChapter(id);
//		if (exploreChapter != null && exploreChapter.getReward()) {
//			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
//			return;
//		}
//	
//		boolean reward = chapterOp.exploreChapterReward(id);
//		if (!reward) {
//			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
//			return;
//		}
//		ExploreChapterConfig exploreChapterConfig = ExploreChapterManager.getInstance().getExploreChapterConfig(id);
//	
//		List<RewardItem> addRewards = PlayerHelper.addRewards(playerId, exploreChapterConfig.getProgressRewardId());
//		resp.addAllReward(PbBuilder.buildRewardInfo(addRewards));
		client.sendProtocol(resp);
	
	}
	
	/*protected void reward(NetClient client, Object message) {
	
		BattleChapterRewardRequest_13000009 req = (BattleChapterRewardRequest_13000009) message;
		BattleChapterRewardResponse_1300000a.Builder resp = BattleChapterRewardResponse_1300000a.newBuilder();
	
		int chapterId = req.getId();
		int index = req.getIndex();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		long playerId = player.getPlayerId();
		ChapterOp chapterOp = PlayerCacheFactory.getCache(playerId, ChapterOp.class);
		Chapter chapter = chapterOp.getChapter(chapterId);
		BattleChapterConfig chapterConfig = BattleChapterManager.getInstance().getBattleChapterConfig(chapterId);
	
		int allStar = chapterOp.getStars(chapterId);
	
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
		chapterOp.updateChapter(chapter);
	
		client.sendProtocol(resp);
	
	}*/
	protected void start(NetClient client, Object message) {
		BattleFieldStartRequest_13000001 req = (BattleFieldStartRequest_13000001) message;
		BattleFieldEndResponse_13000004.Builder resp = BattleFieldEndResponse_13000004.newBuilder();

		int dungeonId = req.getDungeonId();
		int id = req.getId();
		int type = req.getType();
		String uidString = req.getUid();
		long uid = StringUtils.isEmpty(uidString) ? 0 : Long.parseLong(uidString);
		long playerId = client.getPlayerId();
		ChapterOp chapterOp = PlayerCacheFactory.getCache(playerId, ChapterOp.class);
		
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		BattleLevelConfig levelConfig = BattleLevelManager.getInstance().getBattleLevelConfig(id);

		// 检查体力消耗 探索的战斗不需要
	/*	if(type != DungeonTypeEnum.ExploreBattle.getId()) {
			if (levelConfig != null && levelConfig.getEnergyExpend() > 0 && !PlayerHelper.isEnough(player.getId(), ResourceEnum.Brawn
					.getId(), levelConfig.getEnergyExpend())) {

				client.sendProtocol(resp, ErrorMsgEnum.resource_not_enough.getId());
				return;
			}
		}*/
		long randomSeed = System.currentTimeMillis() ; 
		IBattleHandler battleHandler = BattleFactory.getBattleHandler(type);
		int errorCode = battleHandler.battleStart(playerId, type, dungeonId, id, 0, uid);
		if (errorCode == 0) {
			// 设置当前在打的关卡数据
			chapterOp.setAttackingData(0, type, dungeonId, id, uid,randomSeed);
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
		//触发事件
		player.handleEvent(EventTypeEnum.BattleStart, levelConfig.getId(), 0);
//		resp.setRandomSeed(randomSeed + "");
		client.sendProtocol(resp, errorCode);
	}

	protected void end(NetClient client, Object message) {
		BattleFieldEndRequest_13000003 req = (BattleFieldEndRequest_13000003) message;
		BattleFieldEndResponse_13000004.Builder resp = BattleFieldEndResponse_13000004.newBuilder();
		int type = req.getType();
		int dungeonId = req.getDungeonId();
		int id = req.getId();
		boolean win = req.getWin(); 
		String uidString = req.getUid();
		long uid = StringUtils.isEmpty(uidString) ? 0 : Long.parseLong(uidString);
		long playerId = client.getPlayerId();
		List<Integer> starList = req.getStarList();
		// 战报数据 [
//		  { "roleId": 410201, "hp": 10, "ep": 10, "san": 10 ,"uid" : 1000410201},
//		  { "roleId": 410202, "hp": 10, "ep": 10, "san": 10 ,"uid" : 2000410202}
//		]
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		ChapterOp chapterOp = PlayerCacheFactory.getCache(playerId, ChapterOp.class);
		int attackingId = chapterOp.getAttackingId();
		int attackingType = chapterOp.getAttackingType();
		long attackingUid = chapterOp.getAttackingUid();
		int lineupId = chapterOp.getLineupId();
		if (attackingId == 0 || attackingId != id || attackingType != type || uid != attackingUid) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}

//		List<BattleReportItemInfo> report = req.getReport().getItemsList();
		IBattleHandler battleHandler = BattleFactory.getBattleHandler(attackingType);
		int errorCode = battleHandler.battleEnd(playerId, win, starList, resp);
		if (errorCode > 0) {
			client.sendProtocol(resp, errorCode);
			return;
		}
		BattleLevelConfig levelConfig = BattleLevelManager.getInstance().getBattleLevelConfig(id);

		int apCost = levelConfig.getEnergyExpend();

		// 检查体力消耗 探索的战斗不需要
//		if (type != DungeonTypeEnum.ExploreBattle.getId()) {
//			if (levelConfig != null && apCost > 0) {
//				PlayerHelper.delResources(player.getPlayerId(), ResourceEnum.Brawn.getId(),
//						levelConfig.getEnergyExpend(), ResourceConsumeEnum.OriginUnlock);
//			}
//		}
		
		if (attackingType != DungeonTypeEnum.ExploreBattle.getId()) {
			// 除了探索战斗，其他的战斗基本都用到了BattleField，计算体力消耗等等
			if (win) {
				List<Map.Entry<Integer, Integer>> starLevelReward = levelConfig.getStarLevelReward();
				if (!starLevelReward.isEmpty()) {
					//星级奖励 只有首次可以领
					BattleLevel BattleField = chapterOp.getBattleLevel(id);
					List<Integer> currstars = ByteHelp.binary1List(BattleField == null ? 0 : BattleField.getStar());
					List<Map.Entry<Integer, Integer>> starRewards = new ArrayList<>();
					for (Integer star : starList) {
						if (!currstars.contains(star)) {
							starRewards.add(starLevelReward.get(star));
						}
					}
					resp.addAllStarRewards(PlayerHelper.addResources(playerId, starRewards));
				}
				
				player.handleEvent(new GameEvent(EventTypeEnum.Level, player, attackingId, 5, 5));
			} else {// 失败会返回一半体力,向下取
//				int energyExpend = levelConfig.getEnergyExpend();
//				if (energyExpend > 0) {
//					int lose = energyExpend / 2;
//					PlayerHelper.addResources(playerId, ResourceEnum.Brawn.getId()0, energyExpend - lose);
//					apCost = lose;
//				}
			}
//			addExp(resp, player, lineupId, apCost);
		} else { //探索战斗
			if (win) {
				//exploreOp.calcExploreBattleResource(levelConfig, lineupId, resp);
				//加晋升点
			}
		}
		//基础奖励
		resp.addAllCommonRewards(PlayerHelper.addResources(client.getPlayerId(), levelConfig.getBaseReward()));
		//随机奖励
		int[] randomReward = levelConfig.getRandomReward();
		Set<Pair<Integer, Integer>> rewards = new HashSet<>();
		for (Integer reward : randomReward) {
			List<RandomRewardConfig> randomGroupIdItemsByGroupId = RandomUtil.randomGroupIdItemsByGroupId(reward);
			for (RandomRewardConfig config : randomGroupIdItemsByGroupId) {
				rewards.add(new Pair<Integer, Integer>(config.getItemId(), config.getNum()));
			}
		}
		List<RewardInfo> rewardItems = PlayerHelper.addResources(client.getPlayerId(), rewards);
		resp.addAllRandomRewards(rewardItems);

		player.handleEvent(new GameEvent(EventTypeEnum.BattleEnd, lineupId, win, levelConfig.getId()));
		chapterOp.setAttackingData(0, 0, 0, 0, 0, 0);
		
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
		PlayerHelper.addResources(playerId, ResourceEnum.Coin.getId(), coin);
	
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
		ChapterOp chapterOp = PlayerCacheFactory.getCache(player.getPlayerId(), ChapterOp.class);
		BattleField level2 = chapterOp.getBattleField(level);
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
