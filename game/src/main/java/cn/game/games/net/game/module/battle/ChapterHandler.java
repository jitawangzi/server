package cn.game.games.net.game.module.battle;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Chapter;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.ResultObject;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.develop.AttrModule;
import cn.game.games.net.game.module.player.pointreward.PointRewardModule;
import cn.game.games.net.game.module.player.pointreward.PointRewardType;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.HCBattleConfig;
import cn.game.protocol.generated.config.PatrolConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.enume.WelfareTypeEnum;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.generated.manager.HCBattleManager;
import cn.game.protocol.generated.manager.PatrolManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartRequest_13000055;
import cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartResponse_13000056;
import cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepBatchRequest_13000062;
import cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepBatchResponse_13000063;
import cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepRequest_13000060;
import cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepRequest_13000064;
import cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepRequest_13000066;
import cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepResponse_13000061;
import cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepResponse_13000065;
import cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepResponse_13000067;
import cn.game.protocol.protobuf.BattleMsg.BattleDayChallengeReceiveActivePointRequest_13000070;
import cn.game.protocol.protobuf.BattleMsg.BattleDayChallengeReceiveActivePointResponse_13000071;
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
import cn.game.protocol.protobuf.BattleMsg.BattleShareRequest_13000007;
import cn.game.protocol.protobuf.BattleMsg.BattleShareResponse_13000008;
import cn.game.protocol.protobuf.BattleMsg.BattleStaminaRequest_13000050;
import cn.game.protocol.protobuf.BattleMsg.BattleStaminaResponse_13000051;
import cn.game.protocol.protobuf.BattleMsg.BattleSweepRequest_13000024;
import cn.game.protocol.protobuf.BattleMsg.BattleSweepResponse_13000025;
import cn.game.protocol.protobuf.BattleMsg.HCBattleRewardRequest_13000027;
import cn.game.protocol.protobuf.BattleMsg.HCBattleRewardResponse_13000028;
import cn.game.protocol.protobuf.BattleMsg.HCBattleSweepRequest_13000040;
import cn.game.protocol.protobuf.BattleMsg.HCBattleSweepResponse_13000041;
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
		putInvoker(PbProtocol.BattleShareRequest_13000007, (client, message) -> rewardMultiple(client, message));
//		putInvoker(PbProtocol.BattleFieldSweepRequest_13000005, (client, message) -> sweep(client, message));
//		putInvoker(PbProtocol.BattleChapterRewardRequest_13000022, (client, message) -> reward(client, message));
//		putInvoker(PbProtocol.ExploreActRewardRequest_13000020, (client, message) -> exploreActReward(client, message));
		putInvoker(PbProtocol.BattleRewardRequest_13000022, (client, message) -> chapterReward(client, message));
		putInvoker(PbProtocol.HCBattleRewardRequest_13000027, (client, message) -> hcChapterReward(client, message));
		putInvoker(PbProtocol.BattleRougeRefreshRequest_13000005, (client, message) -> rougeRefresh(client, message));
		putInvoker(PbProtocol.BattlePatrolRewardRequest_13000044, this::patrolReward);
		putInvoker(PbProtocol.BattleStaminaRequest_13000050, this::stamina);
		putInvoker(PbProtocol.BattleSweepRequest_13000024, this::sweep);
		putInvoker(PbProtocol.HCBattleSweepRequest_13000040, this::hcsweep);
		putInvoker(PbProtocol.BattleDayChallengeReceiveActivePointRequest_13000070, this::dayChallengePointReward);
		putInvoker(PbProtocol.BattleDaoHeartRequest_13000055, this::daoHeart);
		putInvoker(PbProtocol.BattleDaoHeartSweepRequest_13000060, this::daoHeartSweep);
		putInvoker(PbProtocol.BattleDaoHeartSweepBatchRequest_13000062, this::daoHeartSweepBatch);
		putInvoker(PbProtocol.BattleDaoHeartSweepRequest_13000066, this::daoHeartReward);
		putInvoker(PbProtocol.BattleDaoHeartSweepRequest_13000064, this::daoHeartRewardInfo);
	}

	protected void empty(NetClient client, Object message) {
		BattleFieldStartRequest_13000001 req = (BattleFieldStartRequest_13000001) message;
		BattleFieldStartResponse_13000002.Builder resp = BattleFieldStartResponse_13000002.newBuilder();


		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		ChapterModule chapterModule = player.getModule(ChapterModule.class);

		client.sendProtocol(resp);
	}

	protected void daoHeartRewardInfo(NetClient client, Object message) {
		BattleDaoHeartSweepRequest_13000064 req = (BattleDaoHeartSweepRequest_13000064) message;
		BattleDaoHeartSweepResponse_13000065.Builder resp = BattleDaoHeartSweepResponse_13000065.newBuilder();
		int type = req.getType();
		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		DaoHeartBattle daoHeartBattle = chapterModule.getDaoHeartBattle(type);
		if (daoHeartBattle != null) {
			resp.addAllId(daoHeartBattle.getRewardBattleIds());
		}

		client.sendProtocol(resp);
	}

	protected void daoHeartReward(NetClient client, Object message) {
		BattleDaoHeartSweepRequest_13000066 req = (BattleDaoHeartSweepRequest_13000066) message;
		BattleDaoHeartSweepResponse_13000067.Builder resp = BattleDaoHeartSweepResponse_13000067.newBuilder();
		int id = req.getId();
		int type = req.getType();

		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		DaoHeartBattle daoHeartBattle = chapterModule.getDaoHeartBattle(type);
		if (daoHeartBattle == null) {
			client.sendProtocol(resp, ErrorMsgEnum.player_data_not_found.getId());
			return;
		}
		List<Integer> rewardBattleIds = daoHeartBattle.getRewardBattleIds();
		if (rewardBattleIds.contains(id)) {
			client.sendProtocol(resp, ErrorMsgEnum.repeat_request.getId());
			return;
		}
		int completeBattleId = daoHeartBattle.getCompleteBattleId();
		if (completeBattleId == 0) {
			client.sendProtocol(resp, ErrorMsgEnum.illegal_request.getId());
			return;
		}
		BattleConfig battleConfig = BattleManager.instance().get(id);
		if (!BattleHelper.isComplete(completeBattleId, id)) {
			client.sendProtocol(resp, ErrorMsgEnum.illegal_request.getId());
			return;
		}
		rewardBattleIds.add(id); 
		OpType opType = type == 2 ? OpType.DaoXinComplete : type == 3 ? OpType.XinMoComplete : OpType.YaoWangComplete;

		resp.addAllRewards(PlayerHelper.addReward(player, battleConfig.ClearGameReward, opType));

		client.sendProtocol(resp);
	}

	protected void daoHeartSweepBatch(NetClient client, Object message) {
		BattleDaoHeartSweepBatchRequest_13000062 req = (BattleDaoHeartSweepBatchRequest_13000062) message;
		BattleDaoHeartSweepBatchResponse_13000063.Builder resp = BattleDaoHeartSweepBatchResponse_13000063.newBuilder();
		int id = req.getId();
		int type = req.getType();
		boolean pay = req.getPay();

		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		DaoHeartBattle daoHeartBattle = chapterModule.getDaoHeartBattle(type);
		if (daoHeartBattle == null) {
			client.sendProtocol(resp, ErrorMsgEnum.illegal_request.getId());
			return;
		}
		if (daoHeartBattle.getCompleteBattleId() != id) {
			client.sendProtocol(resp, ErrorMsgEnum.illegal_request.getId());
			return;
		}
		OpType opType = type == 2 ? OpType.DaoXinSweep : OpType.XinMoSweep;

		int freeRemaning = daoHeartBattle.getMaxFreeSweepCount() - daoHeartBattle.getFreeSweep();
		if (freeRemaning > 0) {
			daoHeartBattle.setFreeSweep(daoHeartBattle.getFreeSweep() + freeRemaning);
		}
		int paySweep = daoHeartBattle.getPaySweep();
		int payRemaning = daoHeartBattle.getMaxPaySweepCount() - paySweep;
		if (payRemaning > 0) {
			daoHeartBattle.setPaySweep(paySweep + payRemaning);
		}
		int allCount = freeRemaning + payRemaning;
		if (allCount == 0) {
			client.sendProtocol(resp, ErrorMsgEnum.times_limit.getId());
			return;
		}
		List<Integer> payList = new ArrayList<>();
		int[][] paySweepCostAll = daoHeartBattle.getPaySweepCostAll();
		for (int i = daoHeartBattle.getPaySweep(); i < paySweepCostAll.length; i++) {
			payList.add(paySweepCostAll[i][0], paySweepCostAll[i][1]);
		}
		int[] payArray = new int[payList.size()];
		for (int i : payArray) {
			payArray[i] = payList.get(i);
		}
		if (!PlayerHelper.delResources(player, payArray, opType)) {
			client.sendProtocol(resp, ErrorMsgEnum.resource_not_enough.getId());
			return;
		}

		BattleConfig battleConfig = BattleManager.instance().get(id);

		for (int i = 0; i < allCount; i++) {
			List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.SweepReward, opType);
			resp.addAllRewards(reward);
		}
		client.sendProtocol(resp);
	}
	protected void daoHeartSweep(NetClient client, Object message) {
		BattleDaoHeartSweepRequest_13000060 req = (BattleDaoHeartSweepRequest_13000060) message;
		BattleDaoHeartSweepResponse_13000061.Builder resp = BattleDaoHeartSweepResponse_13000061.newBuilder();
		int id = req.getId();
		int type = req.getType();

		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		DaoHeartBattle daoHeartBattle = chapterModule.getDaoHeartBattle(type);
		if (daoHeartBattle == null) {
			client.sendProtocol(resp, ErrorMsgEnum.illegal_request.getId());
			return;
		}
		if (daoHeartBattle.getCompleteBattleId() != id) {
			client.sendProtocol(resp, ErrorMsgEnum.illegal_request.getId());
			return;
		}
		OpType opType = type == 2 ? OpType.DaoXinSweep : type == 3 ? OpType.XinMoSweep : OpType.YaoWangSweep;

		int freeRemaning = daoHeartBattle.getMaxFreeSweepCount() - daoHeartBattle.getFreeSweep();
		if (freeRemaning > 0) {
			daoHeartBattle.setFreeSweep(daoHeartBattle.getFreeSweep() + 1);
		} else {
			int payRemaning = daoHeartBattle.getMaxPaySweepCount() - daoHeartBattle.getPaySweep();
			if (payRemaning <= 0) {
				client.sendProtocol(resp, ErrorMsgEnum.times_limit.getId());
				return;
			}
			if (!PlayerHelper.delResources(player, daoHeartBattle.getPaySweepCost(), opType)) {
				client.sendProtocol(resp, ErrorMsgEnum.resource_not_enough.getId());
				return;
			}
			daoHeartBattle.setPaySweep(daoHeartBattle.getPaySweep() + 1);
		}
		
		BattleConfig battleConfig = BattleManager.instance().get(id);
		List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.SweepReward, opType);
		resp.addAllRewards(reward);

		client.sendProtocol(resp);
	}

	protected void daoHeart(NetClient client, Object message) {
		BattleDaoHeartRequest_13000055 req = (BattleDaoHeartRequest_13000055) message;
		BattleDaoHeartResponse_13000056.Builder resp = BattleDaoHeartResponse_13000056.newBuilder();
		int type = req.getType(); 
		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		if (type == 2) {
			if (!player.isFuncOpen(InitialUI.DaoXinLLiLian)) {
				client.sendProtocol(resp, ErrorMsgEnum.func_not_open.getId());
				return;
			}
		} else if (type == 3) {
			if (!player.isFuncOpen(InitialUI.XinMoShiLian)) {
				client.sendProtocol(resp, ErrorMsgEnum.func_not_open.getId());
				return;
			}
		} else if (type == 4) {
			if (!player.isFuncOpen(InitialUI.YaoWangBiePao)) {
				client.sendProtocol(resp, ErrorMsgEnum.func_not_open.getId());
				return;
			}
		}
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		DaoHeartBattle daoHeartBattle = chapterModule.getDaoHeartBattle(type);
		if (daoHeartBattle == null) {
			client.sendProtocol(resp, ErrorMsgEnum.illegal_request.getId());
			return;
		}
		resp.setCompletedId(daoHeartBattle.getCompleteBattleId());
		resp.setNextId(daoHeartBattle.getNextBattleId());
		resp.addAllRandomBuff(daoHeartBattle.getRandomBuff());
		resp.setFreeSweepRemaning(daoHeartBattle.getMaxFreeSweepCount() - daoHeartBattle.getFreeSweep());
		resp.setPaySweepRemaning(daoHeartBattle.getMaxPaySweepCount() - daoHeartBattle.getPaySweep());
		client.sendProtocol(resp);
	}

	protected void dayChallengePointReward(NetClient client, Object message) {
		BattleDayChallengeReceiveActivePointRequest_13000070 req = (BattleDayChallengeReceiveActivePointRequest_13000070) message;
		BattleDayChallengeReceiveActivePointResponse_13000071.Builder resp = BattleDayChallengeReceiveActivePointResponse_13000071.newBuilder();
		int index = req.getIndex(); 
		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		PointRewardModule pointRewardModule = player.getPointRewardModule();
		ResultObject reward = pointRewardModule.addReward(PointRewardType.DAY_CHALLENGE, chapterModule.getDayChallenge().getBattleId(), index);
		if (!reward.isOK()) {
			client.sendProtocol(resp, reward.getErrorCode());
			return;
		}
		resp.addAllRewards((Iterable<? extends RewardInfo>) reward.getValue());
		client.sendProtocol(resp);
	}

	protected void sweep(NetClient client, Object message) {
		BattleSweepRequest_13000024 req = (BattleSweepRequest_13000024) message;
		BattleSweepResponse_13000025.Builder resp = BattleSweepResponse_13000025.newBuilder();
		int id = req.getId();
		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		BattleConfig battleConfig = BattleManager.instance().get(id);
		if (battleConfig == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.config_data_not_found.getId());
			return;
		}
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		if (!chapterModule.isBattlePass(id)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.illegal_request.getId());
			return;
		}
		if (chapterModule.getDaySweepCount() >= GlobalConst.SweepNum) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.times_limit.getId());
			return;
		}
		boolean delResources = PlayerHelper.delResources(player, battleConfig.cost, OpType.BattleSweep);
		if (!delResources) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.resource_not_enough.getId());
			return;
		}
		chapterModule.setDaySweepCount(chapterModule.getDaySweepCount() + 1);
		List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.WinRandom, OpType.BattleSweep);
		resp.addAllReward(reward);
		client.sendProtocol(resp);
	}

	protected void hcsweep(NetClient client, Object message) {
		HCBattleSweepRequest_13000040 req = (HCBattleSweepRequest_13000040) message;
		HCBattleSweepResponse_13000041.Builder resp = HCBattleSweepResponse_13000041.newBuilder();
		int id = req.getId();
		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		HCBattleConfig battleConfig = HCBattleManager.instance().get(id);
		if (battleConfig == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.config_data_not_found.getId());
			return;
		}
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		if (!chapterModule.isBattlePass(id)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.illegal_request.getId());
			return;
		}
		if (chapterModule.getDaySweepCount() >= GlobalConst.SweepNum) {
			player.handleEvent(EventTypeEnum.WatchAds);
		}
		boolean delResources = PlayerHelper.delResources(player, battleConfig.cost, OpType.BattleSweep);
		if (!delResources) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.resource_not_enough.getId());
			return;
		}
		chapterModule.setDaySweepCount(chapterModule.getDaySweepCount() + 1);
		List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.WinRandom, OpType.BattleSweep);
		resp.addAllReward(reward);
		client.sendProtocol(resp);
	}

	protected void stamina(NetClient client, Object message) {
		BattleStaminaRequest_13000050 req = (BattleStaminaRequest_13000050) message;
		BattleStaminaResponse_13000051.Builder resp = BattleStaminaResponse_13000051.newBuilder();
		int time = req.getTime();
		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		List<Integer> storeStaminas = chapterModule.getStoreStaminas();
		if (!storeStaminas.contains(time)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_data_not_found.getId());
			return;
		}
		storeStaminas.remove(Integer.valueOf(time));
		if (chapterModule.isStaminaExpire(DateUtil.currentTimeSeconds(), time)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.stamina_expire.getId());
			return;
		}
		PlayerHelper.addResources(player, Asset.playerEnergy.ID, 30, OpType.StoreStamina);

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
					client.sendProtocol(resp, ErrorMsgEnum.times_limit.getId());
					return;
				}
				// 消耗
				if (!PlayerHelper.delResources(player, GlobalConst.QuickPatrolConsume, OpType.Patrol)) {
					client.sendProtocol(resp, ErrorMsgEnum.resource_not_enough.getId());
					return;
				}
				chapterModule.setAdPatrolCount(quickPatrolCount + 1);
				player.handleEvent(EventTypeEnum.WatchAds);

			} else {
				int quickPatrolCount = chapterModule.getQuickPatrolCount();
				if (quickPatrolCount >= GlobalConst.QuickPatrolCnt + player.getWelfareValue(WelfareTypeEnum.QuicPatrolCnt)) {
					client.sendProtocol(resp, ErrorMsgEnum.times_limit.getId());
					return;
				}
				// 消耗
				if (!PlayerHelper.delResources(player, GlobalConst.QuickPatrolConsume, OpType.Patrol)) {
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

		PatrolConfig patrolConfig = PatrolManager.instance().get(chapterModule.getPatrolBattleId());

		float incomeRate = player.getWelfareValue(WelfareTypeEnum.PatrolIncome);
		float rate = 1 + (incomeRate / 10000);

		int exp = (int) (patrolConfig.IncomeEXP * minute * rate);
		int gold = (int) (patrolConfig.IncomeGold * minute * rate);

		PlayerHelper.addResources(player, Asset.playerExp.ID, exp, OpType.Patrol);
		PlayerHelper.addResources(player, Asset.gold.ID, gold, OpType.Patrol);
		for (int i = 0; i < hours; i++) {
			List<RewardInfo> reward = PlayerHelper.addReward(player, patrolConfig.IncomeRandomID, OpType.Patrol);
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
	
		List<Integer> idList = req.getIdList();
		List<Integer> indexList = req.getIndexList();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());

		if (!player.isFuncOpen(InitialUI.ChapterBox)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}
		long playerId = player.getPlayerId();
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		for (int i = 0; i < indexList.size(); i++) {
			int index = indexList.get(i);
			int id = idList.get(i);
//			boolean pass = chapterModule.isExploreChapterPass(id);
//			if (!pass) {
//				client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
//				return;
//			}
			Chapter chapter = chapterModule.getChapter(id);
			if (chapter == null) {
				continue;
			}
			List<Integer> rewards = chapter.getRewards();
			if (rewards.contains(index)) {
				client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
				return;
			}
			int minute = chapter.getBattleTime() / 60;
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
		}

		for (int i = 0; i < indexList.size(); i++) {
			int index = indexList.get(i);
			int id = idList.get(i);
			BattleConfig battleConfig = BattleManager.instance().get(id);

			List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.BattleBoxRandomId[index], OpType.BattleEnd);

			resp.addAllReward(reward);
			Chapter chapter = chapterModule.getChapter(id);
			chapter.getRewards().add(index);
		}

		client.sendProtocol(resp);
	
	}

	protected void hcChapterReward(NetClient client, Object message) {

		HCBattleRewardRequest_13000027 req = (HCBattleRewardRequest_13000027) message;
		HCBattleRewardResponse_13000028.Builder resp = HCBattleRewardResponse_13000028.newBuilder();

		List<Integer> idList = req.getIdList();
		List<Integer> indexList = req.getIndexList();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());

		if (!player.isFuncOpen(InitialUI.ChapterBox)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}
		long playerId = player.getPlayerId();
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		for (int i = 0; i < indexList.size(); i++) {
			int index = indexList.get(i);
			int id = idList.get(i);
//			boolean pass = chapterModule.isExploreChapterPass(id);
//			if (!pass) {
//				client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
//				return;
//			}
			Chapter chapter = chapterModule.getChapter(id);
			List<Integer> rewards = chapter.getRewards();
			if (rewards.contains(index)) {
				client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
				return;
			}
			int minute = chapter.getBattleTime();
//			BattleConfig battleConfig = BattleManager.instance().get(id);
			HCBattleConfig battleConfig = HCBattleManager.instance().get(id);
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
		}

		for (int i = 0; i < indexList.size(); i++) {
			int index = indexList.get(i);
			int id = idList.get(i);
//			BattleConfig battleConfig = BattleManager.instance().get(id);
//			BattleConfig battleConfig = BattleHelper.getBattleConfig(id);
			HCBattleConfig battleConfig = HCBattleManager.instance().get(id);

			List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.BattleBoxRandomId[index], OpType.BattleEnd);

			resp.addAllReward(reward);
			Chapter chapter = chapterModule.getChapter(id);
			chapter.getRewards().add(index);
		}

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
//		long randomSeed = System.currentTimeMillis() ; 
		IBattleHandler battleHandler = BattleFactory.getBattleHandler(type);
		int errorCode = battleHandler.check(player, type, dungeonId);
		if (errorCode > 0) {
			client.sendProtocol(resp, errorCode);
			return;
		}
		errorCode = battleHandler.battleStart(playerId, type, dungeonId, id, 0, 0);
		if (errorCode == 0) {
			// 设置当前在打的关卡数据
			chapterModule.setAttackingData(0, type, dungeonId, id, 0, 0);
			if (battleHandler instanceof HCBattleHandler) {
				// 触发事件
				player.handleEvent(EventTypeEnum.HCBattleStart, dungeonId, 0);
			} else if (battleHandler instanceof XiYouBattleHandler) {
				// 触发事件
				player.handleEvent(EventTypeEnum.BattleStart, dungeonId, 0);
			}

			AttrModule module = player.getModule(AttrModule.class);
			module.calcAllAttr();
			resp.setAttrs(module.buildBattleAttrs());
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
		client.sendProtocol(resp, errorCode);
	}

	protected void rewardMultiple(NetClient client, Object message) {
		BattleShareRequest_13000007 req = (BattleShareRequest_13000007) message;
		BattleShareResponse_13000008.Builder resp = BattleShareResponse_13000008.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		int battleRewardMultipleTimes = chapterModule.getBattleRewardMultipleTimes();
		if (battleRewardMultipleTimes >= GlobalConst.Share) {
			client.sendProtocol(resp, ErrorMsgEnum.times_limit.getId());
			return;
		}
		List<RewardInfo> lastBattleRewards = chapterModule.getLastBattleRewards();
		if (lastBattleRewards == null) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}
		chapterModule.setBattleRewardMultipleTimes(battleRewardMultipleTimes + 1);
		List<RewardInfo> multipleRewards = PlayerHelper.multipleRewards(player, lastBattleRewards, GlobalConst.ShareCnt - 1);
		resp.addAllRewards(multipleRewards);
		client.sendProtocol(resp);

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
		if (attackingType == 0) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}

//		List<BattleReportItemInfo> report = req.getReport().getItemsList();
		player.handleEvent(EventTypeEnum.BattleEnd, attackingDungeonId, attackingId, win, killMonsterCount, killMonsterBossCount);

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

		chapterModule.setAttackingData(0, 0, 0, 0, 0, 0);
		
		List<RewardInfo> rewardsList = resp.getRewardsList();
		chapterModule.setLastBattleRewards(rewardsList);

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
