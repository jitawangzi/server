package cn.game.games.net.game.module.battle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import cn.game.games.cache.entity.Base;
import cn.game.protocol.protobuf.BaseMsg;
import cn.game.protocol.protobuf.BattleMsg;
import org.springframework.stereotype.Component;
import cn.game.core.net.client.NetClient;
import cn.game.games.cache.entity.Chapter;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.ResultObject;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.handler.GameBaseHandler;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.award.RewardHelper;
import cn.game.games.net.game.module.currency.CurrencyModule;
import cn.game.games.net.game.module.develop.AttrModule;
import cn.game.games.net.game.module.develop.hero.HeroModule;
import cn.game.games.net.game.module.player.pointreward.PointRewardModule;
import cn.game.games.net.game.module.player.pointreward.PointRewardType;
import cn.game.games.net.game.module.pvp.OfflineBattleHandler;
import cn.game.games.net.game.module.rank.RankHelper;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.ConsumeConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.HCBattleConfig;
import cn.game.protocol.generated.config.LingShanConfig;
import cn.game.protocol.generated.config.PatrolConfig;
import cn.game.protocol.generated.config.RichManItemConfig;
import cn.game.protocol.generated.config.WorldBossRewardConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.enume.WelfareTypeEnum;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.generated.manager.ConsumeManager;
import cn.game.protocol.generated.manager.HCBattleManager;
import cn.game.protocol.generated.manager.PatrolManager;
import cn.game.protocol.generated.manager.RichManItemManager;
import cn.game.protocol.generated.manager.WorldBossRewardManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BaseMsg.PlayerRankInfo;
import cn.game.protocol.protobuf.BattleMsg.BattleChapterRewardRequest_13000222;
import cn.game.protocol.protobuf.BattleMsg.BattleChapterRewardResponse_13000223;
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
import cn.game.protocol.protobuf.BattleMsg.BattleFieldQuickEndRequest_13000005;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldQuickEndResponse_13000006;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldStartRequest_13000001;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldStartResponse_13000002;
import cn.game.protocol.protobuf.BattleMsg.BattleLineupChooseRequest_13000031;
import cn.game.protocol.protobuf.BattleMsg.BattleLineupChooseResponse_13000032;
import cn.game.protocol.protobuf.BattleMsg.BattleLineupRequest_13000048;
import cn.game.protocol.protobuf.BattleMsg.BattleLineupResponse_13000049;
import cn.game.protocol.protobuf.BattleMsg.BattleLingShanBuyTimesRequest_13000513;
import cn.game.protocol.protobuf.BattleMsg.BattleLingShanBuyTimesResponse_13000514;
import cn.game.protocol.protobuf.BattleMsg.BattleLingShanRequest_13000511;
import cn.game.protocol.protobuf.BattleMsg.BattleLingShanResponse_13000512;
import cn.game.protocol.protobuf.BattleMsg.BattleLingShanRewardRequest_13000515;
import cn.game.protocol.protobuf.BattleMsg.BattleLingShanRewardResponse_13000516;
import cn.game.protocol.protobuf.BattleMsg.BattleLostDayRewardRequest_13000203;
import cn.game.protocol.protobuf.BattleMsg.BattleLostDayRewardResponse_13000204;
import cn.game.protocol.protobuf.BattleMsg.BattleLostInfoRequest_13000201;
import cn.game.protocol.protobuf.BattleMsg.BattleLostInfoResponse_13000202;
import cn.game.protocol.protobuf.BattleMsg.BattleNightmareRealmBuffUpdateRequest_13000082;
import cn.game.protocol.protobuf.BattleMsg.BattleNightmareRealmBuffUpdateResponse_13000083;
import cn.game.protocol.protobuf.BattleMsg.BattleNightmareRealmQuickRequest_13000084;
import cn.game.protocol.protobuf.BattleMsg.BattleNightmareRealmQuickResponse_13000085;
import cn.game.protocol.protobuf.BattleMsg.BattleNightmareRealmRequest_13000080;
import cn.game.protocol.protobuf.BattleMsg.BattleNightmareRealmResponse_13000081;
import cn.game.protocol.protobuf.BattleMsg.BattlePatrolRewardRequest_13000044;
import cn.game.protocol.protobuf.BattleMsg.BattlePatrolRewardResponse_13000045;
import cn.game.protocol.protobuf.BattleMsg.BattleReliveRequest_13000010;
import cn.game.protocol.protobuf.BattleMsg.BattleReliveResponse_13000011;
import cn.game.protocol.protobuf.BattleMsg.BattleRescueSkillIdRequest_13000057;
import cn.game.protocol.protobuf.BattleMsg.BattleRescueSkillIdResponse_13000058;
import cn.game.protocol.protobuf.BattleMsg.BattleRewardRequest_13000022;
import cn.game.protocol.protobuf.BattleMsg.BattleRewardResponse_13000023;
import cn.game.protocol.protobuf.BattleMsg.BattleRogueAdvertiseRequest_13000012;
import cn.game.protocol.protobuf.BattleMsg.BattleRogueAdvertiseResponse_13000013;
import cn.game.protocol.protobuf.BattleMsg.BattleRougeRefreshRequest_13000052;
import cn.game.protocol.protobuf.BattleMsg.BattleRougeRefreshResponse_13000053;
import cn.game.protocol.protobuf.BattleMsg.BattleShareRequest_13000007;
import cn.game.protocol.protobuf.BattleMsg.BattleShareResponse_13000008;
import cn.game.protocol.protobuf.BattleMsg.BattleSpiritualChangeBattleRequest_13000098;
import cn.game.protocol.protobuf.BattleMsg.BattleSpiritualChangeBattleResponse_13000099;
import cn.game.protocol.protobuf.BattleMsg.BattleSpiritualGetPointRequest_13000096;
import cn.game.protocol.protobuf.BattleMsg.BattleSpiritualGetPointResponse_13000097;
import cn.game.protocol.protobuf.BattleMsg.BattleSpiritualGetTimesRequest_13000094;
import cn.game.protocol.protobuf.BattleMsg.BattleSpiritualGetTimesResponse_13000095;
import cn.game.protocol.protobuf.BattleMsg.BattleSpiritualInfoRequest_13000090;
import cn.game.protocol.protobuf.BattleMsg.BattleSpiritualInfoResponse_13000091;
import cn.game.protocol.protobuf.BattleMsg.BattleSpiritualReceiveActivePointRequest_13000092;
import cn.game.protocol.protobuf.BattleMsg.BattleSpiritualReceiveActivePointResponse_13000093;
import cn.game.protocol.protobuf.BattleMsg.BattleStaminaRequest_13000050;
import cn.game.protocol.protobuf.BattleMsg.BattleStaminaResponse_13000051;
import cn.game.protocol.protobuf.BattleMsg.BattleSweepRequest_13000024;
import cn.game.protocol.protobuf.BattleMsg.BattleSweepResponse_13000025;
import cn.game.protocol.protobuf.BattleMsg.BattleWorldBossBuyTimesRequest_13000303;
import cn.game.protocol.protobuf.BattleMsg.BattleWorldBossBuyTimesResponse_13000304;
import cn.game.protocol.protobuf.BattleMsg.BattleWorldBossInfoRequest_13000301;
import cn.game.protocol.protobuf.BattleMsg.BattleWorldBossInfoResponse_13000302;
import cn.game.protocol.protobuf.BattleMsg.BattleWorldRewardResponse_13000306;
import cn.game.protocol.protobuf.BattleMsg.BattleXiangYaoChuMoResponse_13000502;
import cn.game.protocol.protobuf.BattleMsg.HCBattleDataRequest_13000102;
import cn.game.protocol.protobuf.BattleMsg.HCBattleDataResponse_13000103;
import cn.game.protocol.protobuf.BattleMsg.HCBattleDataSaveRequest_13000100;
import cn.game.protocol.protobuf.BattleMsg.HCBattleDataSaveResponse_13000101;
import cn.game.protocol.protobuf.BattleMsg.HCBattleRewardRequest_13000027;
import cn.game.protocol.protobuf.BattleMsg.HCBattleRewardResponse_13000028;
import cn.game.protocol.protobuf.BattleMsg.HCBattleSweepRequest_13000040;
import cn.game.protocol.protobuf.BattleMsg.HCBattleSweepResponse_13000041;
import cn.game.protocol.protobuf.BattleMsg.LineupInfo;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.BinarySearchUtil;
import cn.game.util.ByteHelp;
import cn.game.util.DateUtil;
import cn.game.util.GameUtil;
import cn.game.protocol.protobuf.BattleMsg.BattleTowerDataRequest_13000521;
import cn.game.protocol.protobuf.BattleMsg.BattleTowerDataResponse_13000522;
import cn.game.protocol.protobuf.BattleMsg.BattleTowerQuickEndRequest_13100524;
import cn.game.protocol.protobuf.BattleMsg.BattleTowerQuickEndResponse_13100525;
import cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerDataRequest_13000526;
import cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerDataResponse_13000527;
import cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerHelpPlayerRequest_13000531;
import cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerHelpPlayerResponse_13000532;
import cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerGetTicketRequest_13000533;
import cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerGetTicketResponse_13000534;
import cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerGetHelpRewardRequest_13000535;
import cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerGetHelpRewardResponse_13000536;
import cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerFindHelpRewardRequest_13000537;
import cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerFindHelpRewardResponse_13000538;
import cn.game.protocol.protobuf.BattleMsg.BattleLingShanFloorSkipRequest_13000517;
import cn.game.protocol.protobuf.BattleMsg.BattleLingShanFloorSkipResponse_13000518;
import cn.game.protocol.protobuf.BattleMsg.BattleLingShanEndRequest_13000519;
import cn.game.protocol.protobuf.BattleMsg.BattleLingShanEndResponse_1300051a;
import cn.game.protocol.protobuf.BattleMsg.BattleMountainDataRequest_13000539;
import cn.game.protocol.protobuf.BattleMsg.BattleMountainDataResponse_1300053a;
import cn.game.protocol.protobuf.BattleMsg.BattleMountainFinishNodeRequest_13000541;
import cn.game.protocol.protobuf.BattleMsg.BattleMountainFinishNodeResponse_13000542;
import cn.game.protocol.protobuf.BattleMsg.BattleMountainGetRewardRequest_13000543;
import cn.game.protocol.protobuf.BattleMsg.BattleMountainGetRewardResponse_13000544;
import cn.game.protocol.protobuf.BattleMsg.BattleMountainMapResetRequest_13000545;
import cn.game.protocol.protobuf.BattleMsg.BattleMountainNextFloorRequest_13000547;
import cn.game.protocol.protobuf.BattleMsg.BattlePVEVPDataRequest_13000549;
import cn.game.protocol.protobuf.BattleMsg.BattlePVEVPDataResponse_1300054a;
import cn.game.protocol.protobuf.BattleMsg.BattlePVEVPRecordRequest_13000550;
import cn.game.protocol.protobuf.BattleMsg.BattlePVEVPRecordResponse_13000551;
import cn.game.protocol.protobuf.BattleMsg.BattlePVEVPChallengeRequest_13000552;
import cn.game.protocol.protobuf.BattleMsg.BattlePVEVPChallengeResponse_13000553;
import cn.game.protocol.protobuf.BattleMsg.BattleMountainMapResetResponse_13000546;
import cn.game.protocol.protobuf.BattleMsg.BattleMountainNextFlooResponse_13000548;
import cn.game.protocol.protobuf.BattleMsg.BattleBuyTicketRequest_13000554;
import cn.game.protocol.protobuf.BattleMsg.BattleBuyTicketResponse_13000555;
import cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerRecordRequest_13000528;
import cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerRecordResponse_13000529;
import cn.game.protocol.protobuf.BattleMsg.BattleMountainBuffBagRequest_1300053b;
import cn.game.protocol.protobuf.BattleMsg.BattleMountainBuffBagResponse_1300053c;

@Component
public class BattleHandler extends GameBaseHandler {

    @Override
    protected int getModule() {
        return 0x13;
    }

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.BattleFieldStartRequest_13000001, (client, message) -> start(client, message));
        putInvoker(PbProtocol.BattleFieldEndRequest_13000003, (client, message) -> end(client, message));
        putInvoker(PbProtocol.BattleFieldQuickEndRequest_13000005, (client, message) -> quickeEnd(client, message));
        putInvoker(PbProtocol.BattleShareRequest_13000007, (client, message) -> rewardMultiple(client, message));
        putInvoker(PbProtocol.BattleRewardRequest_13000022, (client, message) -> chapterReward(client, message));
        putInvoker(PbProtocol.HCBattleRewardRequest_13000027, (client, message) -> hcChapterReward(client, message));
        putInvoker(PbProtocol.BattleRougeRefreshRequest_13000052, (client, message) -> rougeRefresh(client, message));
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
        putInvoker(PbProtocol.BattleLineupRequest_13000048, this::lineup);
        putInvoker(PbProtocol.BattleNightmareRealmRequest_13000080, this::nightmareRealm);
        putInvoker(PbProtocol.BattleNightmareRealmBuffUpdateRequest_13000082, this::nightmareRealmBuff);
        putInvoker(PbProtocol.BattleNightmareRealmQuickRequest_13000084, this::nightmareRealmQuick);
        putInvoker(PbProtocol.BattleReliveRequest_13000010, this::relive);
        putInvoker(PbProtocol.BattleSpiritualInfoRequest_13000090, this::spiritualInfo);
        putInvoker(PbProtocol.BattleSpiritualReceiveActivePointRequest_13000092, this::spiritualReceiveActivePoint);
        putInvoker(PbProtocol.BattleSpiritualGetTimesRequest_13000094, this::spiritualAds);
        putInvoker(PbProtocol.BattleSpiritualGetPointRequest_13000096, this::spiritualGetPoint);
        putInvoker(PbProtocol.BattleSpiritualChangeBattleRequest_13000098, this::spiritualChangeBattle);
        putInvoker(PbProtocol.BattleLostInfoRequest_13000201, this::lostInfo);
        putInvoker(PbProtocol.BattleLostDayRewardRequest_13000203, this::lostDayReward);
        putInvoker(PbProtocol.BattleWorldBossInfoRequest_13000301, this::worldBossInfo);
        putInvoker(PbProtocol.BattleWorldBossBuyTimesRequest_13000303, this::worldBossBuy);
        putInvoker(PbProtocol.BattleWorldRewardRequest_13000305, this::worldBossReward);
        putInvoker(PbProtocol.BattleChapterRewardRequest_13000222, this::battleChapterReward);
        putInvoker(PbProtocol.BattleRogueAdvertiseRequest_13000012, this::rogueAdvertise);
        putInvoker(PbProtocol.BattleRescueSkillIdRequest_13000057, this::rescueSkillId);
        putInvoker(PbProtocol.BattleLineupChooseRequest_13000031, this::lineupChoose);
        // PVP 大道争锋
        putInvoker(PbProtocol.BattlePvPTargetListRequest_13000111, OfflineBattleHandler::searchTargetList);
        putInvoker(PbProtocol.BattlePvPStartRequest_13000113, OfflineBattleHandler::startBattle);
        putInvoker(PbProtocol.BattlePvPEndRequest_13000115, OfflineBattleHandler::endBattle);
        putInvoker(PbProtocol.BattlePvPInfoRequest_13000117, OfflineBattleHandler::getInfo);
        putInvoker(PbProtocol.BattleBuyPvPTimeRequest_13000121, OfflineBattleHandler::buyTime);
        putInvoker(PbProtocol.BattleXiangYaoChuMoRequest_13000501, this::xiangYaoChuMoInfo);
        putInvoker(PbProtocol.BattleLingShanRequest_13000511, this::lingShanInfo);
        putInvoker(PbProtocol.BattleLingShanBuyTimesRequest_13000513, this::lingShanBuyTimes);
        putInvoker(PbProtocol.BattleLingShanRewardRequest_13000515, this::lingShanReward);
        putInvoker(PbProtocol.HCBattleDataSaveRequest_13000100, this::hCDataSave);
        putInvoker(PbProtocol.HCBattleDataRequest_13000102, this::hCData);
        putInvoker(PbProtocol.BattleTowerDataRequest_13000521, this::towerData);
        putInvoker(PbProtocol.BattleTowerQuickEndRequest_13100524, this::towerQuickEnd);
        putInvoker(PbProtocol.BattleEquipTowerDataRequest_13000526, this::equipTowerData);
        putInvoker(PbProtocol.BattleEquipTowerHelpPlayerRequest_13000531, this::equipTowerHelpPlayer);
        putInvoker(PbProtocol.BattleEquipTowerGetTicketRequest_13000533, this::equipTowerGetTicket);
        putInvoker(PbProtocol.BattleEquipTowerGetHelpRewardRequest_13000535, this::equipTowerGetHelpReward);
        putInvoker(PbProtocol.BattleEquipTowerFindHelpRewardRequest_13000537, this::equipTowerFindHelpReward);
        putInvoker(PbProtocol.BattleLingShanFloorSkipRequest_13000517, this::lingShanFloorSkip);
        putInvoker(PbProtocol.BattleLingShanEndRequest_13000519, this::lingShanEnd);
        putInvoker(PbProtocol.BattleMountainDataRequest_13000539, this::mountainData);
        putInvoker(PbProtocol.BattleMountainFinishNodeRequest_13000541, this::mountainFinishNode);
        putInvoker(PbProtocol.BattleMountainGetRewardRequest_13000543, this::mountainGetReward);
        putInvoker(PbProtocol.BattlePVEVPDataRequest_13000549, this::pVEVPData);
        putInvoker(PbProtocol.BattlePVEVPRecordRequest_13000550, this::pVEVPRecord);
        putInvoker(PbProtocol.BattlePVEVPChallengeRequest_13000552, this::pVEVPChallenge);
        putInvoker(PbProtocol.BattleMountainMapResetRequest_13000545, this::mountainMapReset);
        putInvoker(PbProtocol.BattleMountainNextFloorRequest_13000547, this::mountainNextFloor);
        putInvoker(PbProtocol.BattleBuyTicketRequest_13000554, this::buyTicket);
        putInvoker(PbProtocol.BattleEquipTowerRecordRequest_13000528, this::equipTowerRecord);
        putInvoker(PbProtocol.BattleMountainBuffBagRequest_1300053b, this::mountainBuffBag);
    }

    protected void xiangYaoChuMoInfo(NetClient client, Object message) {
        BattleXiangYaoChuMoResponse_13000502.Builder resp = BattleXiangYaoChuMoResponse_13000502.newBuilder();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        BattleModule battleModule = player.getModule(BattleModule.class);
        XiangYaoFuMoBattle battle = battleModule.getBattle(DungeonTypeEnum.XiangYaoFuMo);
        resp.setSweepTimes(battle.getSweepTimes());
        resp.setLastCompleteBattleId(battle.getLastCompleteBattleId());
        client.sendProtocol(resp);
    }

    protected void rescueSkillId(NetClient client, Object message) {
        BattleRescueSkillIdRequest_13000057 req = (BattleRescueSkillIdRequest_13000057) message;
        BattleRescueSkillIdResponse_13000058.Builder resp = BattleRescueSkillIdResponse_13000058.newBuilder();
        int id = req.getId();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        BattleModule battleModule = player.getModule(BattleModule.class);
        battleModule.setRescueSkillId(id);
        client.sendProtocol(resp);
    }

    protected void rogueAdvertise(NetClient client, Object message) {
        BattleRogueAdvertiseRequest_13000012 req = (BattleRogueAdvertiseRequest_13000012) message;
        BattleRogueAdvertiseResponse_13000013.Builder resp = BattleRogueAdvertiseResponse_13000013.newBuilder();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        BattleModule battleModule = player.getModule(BattleModule.class);
        //		int adRogueCount = battleModule.getAdRogueCount();
        //		if (adRogueCount >= GlobalConst.RogueAdvertiseCount) {
        //			client.sendProtocol(resp, ErrorMsgEnum.times_limit.getId());
        //			return;
        //		}
        //		battleModule.setAdReliveCount(battleModule.getAdReliveCount() + 1);
        player.handleEvent(EventTypeEnum.WatchAds);
        client.sendProtocol(resp);
    }

    protected void battleChapterReward(NetClient client, Object message) {
        BattleChapterRewardRequest_13000222 req = (BattleChapterRewardRequest_13000222) message;
        BattleChapterRewardResponse_13000223.Builder resp = BattleChapterRewardResponse_13000223.newBuilder();
        long playerId = client.getPlayerId();
        int id = req.getId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        BattleModule battleModule = player.getModule(BattleModule.class);
        List<Integer> battleChapterRewards = battleModule.getBattleChapterRewards();
        if (battleChapterRewards.contains(id)) {
            client.sendProtocol(resp, ErrorMsgEnum.repeat_request.getId());
            return;
        }
        if (!battleModule.isBattlePass(id)) {
            client.sendProtocol(resp, ErrorMsgEnum.illegal_request.getId());
            return;
        }
        battleChapterRewards.add(id);
        BattleConfig battleConfig = BattleManager.instance().get(id);
        List<RewardInfo> rewards = PlayerHelper.addResources(player, battleConfig.ChapterRewards, OpType.BattleChapterFirstReward);
        resp.addAllReward(rewards);
        client.sendProtocol(resp);
    }

    protected void worldBossReward(NetClient client, Object message) {
        BattleWorldRewardResponse_13000306.Builder resp = BattleWorldRewardResponse_13000306.newBuilder();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        if (!player.isFuncOpen(InitialUI.WorldBoss)) {
            client.sendProtocol(resp, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        BattleModule battleModule = player.getModule(BattleModule.class);
        WorldBossBattle battle = battleModule.getBattle(DungeonTypeEnum.WorldBoss);
        long maxDamageToday = battle.getMaxDamageToday();
        List<WorldBossRewardConfig> list = WorldBossRewardManager.instance().list();
        int canRewardIndex = BinarySearchUtil.findIndexLastLessThanOrEqual(list, maxDamageToday, r -> r.BoxCondition);
        if (canRewardIndex < 0) {
            client.sendProtocol(resp, ErrorMsgEnum.illegal_request.getId());
            return;
        }
        WorldBossRewardConfig rewardConfig = list.get(canRewardIndex);
        if (battle.getRewardId() >= rewardConfig.ID) {
            client.sendProtocol(resp, ErrorMsgEnum.repeat_request.getId());
            return;
        }
        int welfareValue = player.getWelfareValue(WelfareTypeEnum.BossBattleBox);
        int rewardIndex = BinarySearchUtil.findElementIndexByField(list, battle.getRewardId(), r -> r.ID, (r1, r2) -> r1 - r2);
        for (int i = rewardIndex + 1; i <= canRewardIndex; i++) {
            WorldBossRewardConfig config = list.get(i);
            resp.addAllRewards(PlayerHelper.addReward(player, config.RandomGivenID, welfareValue, OpType.WorldBoss));
        }
        battle.setRewardId(rewardConfig.ID);
        client.sendProtocol(resp);
    }

    protected void worldBossBuy(NetClient client, Object message) {
        BattleWorldBossBuyTimesRequest_13000303 req = (BattleWorldBossBuyTimesRequest_13000303) message;
        BattleWorldBossBuyTimesResponse_13000304 resp = BattleWorldBossBuyTimesResponse_13000304.getDefaultInstance();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        if (!player.isFuncOpen(InitialUI.WorldBoss)) {
            client.sendProtocol(resp, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        BattleModule battleModule = player.getModule(BattleModule.class);
        WorldBossBattle battle = battleModule.getBattle(DungeonTypeEnum.WorldBoss);
        if (battle.getBuyTimes() >= GlobalConst.JDTMPayCnt) {
            client.sendProtocol(resp, ErrorMsgEnum.times_limit.getId());
            return;
        }
        PlayerHelper.delResources(player, GlobalConst.JDTMConsume, OpType.WorldBoss);
        battle.setBuyTimes(battle.getBuyTimes() + 1);
        client.sendProtocol(resp);
    }

    protected void worldBossInfo(NetClient client, Object message) {
        BattleWorldBossInfoRequest_13000301 req = (BattleWorldBossInfoRequest_13000301) message;
        BattleWorldBossInfoResponse_13000302.Builder resp = BattleWorldBossInfoResponse_13000302.newBuilder();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        if (!player.isFuncOpen(InitialUI.WorldBoss)) {
            client.sendProtocol(resp, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        BattleModule battleModule = player.getModule(BattleModule.class);
        WorldBossBattle battle = battleModule.getBattle(DungeonTypeEnum.WorldBoss);
        resp.setBattleTimes(battle.getBattleTimes());
        resp.setBuyTimes(battle.getBuyTimes());
        resp.setCumulativeDamage(battle.getCumulativeDamage() + "");
        resp.setMaxDamageToday(battle.getMaxDamageToday() + "");
        resp.setRewardId(battle.getRewardId());
        CompletionStage<Integer> rankAsync = RankService.getInstance().getRankAsync(player.getServerId(), RankType.WorldBoss, playerId);
        rankAsync.whenComplete((rank, t) -> {
            if (t != null) {
                player.handleFailFunction(t);
            } else {
                resp.setRank(rank);
                client.sendProtocol(resp.build());
            }
        });
    }

    protected void lostDayReward(NetClient client, Object message) {
        BattleLostDayRewardRequest_13000203 req = (BattleLostDayRewardRequest_13000203) message;
        BattleLostDayRewardResponse_13000204.Builder resp = BattleLostDayRewardResponse_13000204.newBuilder();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        if (!player.isFuncOpen(InitialUI.ShiLuoZhenJing)) {
            client.sendProtocol(resp, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        BattleModule battleModule = player.getModule(BattleModule.class);
        ShiLuoZhenJingBattle battle = battleModule.getBattle(DungeonTypeEnum.ShiLuoZhenJing);
        if (battle.isHistoryMaxReward()) {
            client.sendProtocol(resp, ErrorMsgEnum.repeat_request.getId());
            return;
        }
        int maxBattleId = battle.getHistoryMaxBattleId();
        if (maxBattleId == 0) {
            client.sendProtocol(resp, ErrorMsgEnum.illegal_request.getId());
            return;
        }
        BattleConfig battleConfig = BattleManager.instance().get(maxBattleId);
        for (int randomId : battleConfig.BattleBoxRandomId) {
            resp.addAllRewards(PlayerHelper.addReward(player, randomId, OpType.ShiLuoZhenJing));
        }
        battle.setHistoryMaxReward(true);
        client.sendProtocol(resp);
    }

    protected void lostInfo(NetClient client, Object message) {
        BattleLostInfoRequest_13000201 req = (BattleLostInfoRequest_13000201) message;
        BattleLostInfoResponse_13000202.Builder resp = BattleLostInfoResponse_13000202.newBuilder();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        if (!player.isFuncOpen(InitialUI.ShiLuoZhenJing)) {
            client.sendProtocol(resp, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        BattleModule battleModule = player.getModule(BattleModule.class);
        ShiLuoZhenJingBattle battle = battleModule.getBattle(DungeonTypeEnum.ShiLuoZhenJing);
        resp.setBattleId(battle.getStartBattleId());
        resp.setBattleStage(battle.getBattleStage());
        resp.setHistoryMaxBattleReward(battle.isHistoryMaxReward());
        resp.setHistoryMaxbattleStage(battle.getHistoryMaxStage());
        resp.setHistoryMaxBattle(battle.getHistoryMaxBattleId());
        resp.addAllRandomBuff(battle.getRandomBuff());
        client.sendProtocol(resp);
    }

    protected void spiritualChangeBattle(NetClient client, Object message) {
        BattleSpiritualChangeBattleRequest_13000098 req = (BattleSpiritualChangeBattleRequest_13000098) message;
        BattleSpiritualChangeBattleResponse_13000099 resp = BattleSpiritualChangeBattleResponse_13000099.getDefaultInstance();
        int battleId = req.getBattleId();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        if (!player.isFuncOpen(InitialUI.SpiritBattle)) {
            client.sendProtocol(resp, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        BattleModule battleModule = player.getModule(BattleModule.class);
        LingPoBattle lingPoBattle = battleModule.getBattle(DungeonTypeEnum.LingPo);
        if (lingPoBattle == null) {
            client.sendProtocol(resp, ErrorMsgEnum.player_data_not_found.getId());
            return;
        }
        //		boolean preBattle = BattleHelper.isPreBattle(lingPoBattle.getBattleId(), battleId);
        //		if (!preBattle) {
        //			client.sendProtocol(resp, ErrorMsgEnum.BattleLevel_pre.getId());
        //			return;
        //		}
        int mainBattleId = player.getBattleModule().getMainBattleHighest();
        BattleConfig battleConfig = BattleManager.instance().get(battleId);
        if (!BattleHelper.isComplete(mainBattleId, battleConfig.preBattle)) {
            client.sendProtocol(resp, ErrorMsgEnum.BattleLevel_pre.getId());
            return;
        }
        // 次数扣元宝。
        int changeBattleTimes = lingPoBattle.getChangeBattleTimes();
        int cost = changeBattleTimes >= GlobalConst.SpiritBattleChangeCost.length ? GlobalConst.SpiritBattleChangeCost[GlobalConst.SpiritBattleChangeCost.length - 1] : GlobalConst.SpiritBattleChangeCost[changeBattleTimes];
        PlayerHelper.delResources(player, Asset.gold.ID, cost, OpType.LingPoBattleChange);
        lingPoBattle.setBattleId(battleId);
        lingPoBattle.setChangeBattleTimes(changeBattleTimes + 1);
        client.sendProtocol(resp);
    }

    protected void spiritualGetPoint(NetClient client, Object message) {
        BattleSpiritualGetPointRequest_13000096 req = (BattleSpiritualGetPointRequest_13000096) message;
        BattleSpiritualGetPointResponse_13000097 resp = BattleSpiritualGetPointResponse_13000097.getDefaultInstance();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        if (!player.isFuncOpen(InitialUI.SpiritBattle)) {
            client.sendProtocol(resp, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        BattleModule battleModule = player.getModule(BattleModule.class);
        LingPoBattle lingPoBattle = battleModule.getBattle(DungeonTypeEnum.LingPo);
        if (lingPoBattle == null) {
            client.sendProtocol(resp, ErrorMsgEnum.player_data_not_found.getId());
            return;
        }
        int cid = Asset.SpiritBattlePoint.ID;
        CurrencyModule currencyModule = player.getCurrencyModule();
        long curCount = currencyModule.getCount(cid);
        int battleId = lingPoBattle.getBattleId();
        if (battleId == 0) {
            client.sendProtocol(resp, ErrorMsgEnum.illegal_request.getId());
            return;
        }
        BattleConfig battleConfig = BattleManager.instance().get(battleId);
        int[] battleBoxTrigger = battleConfig.BattleBoxTrigger;
        int maxCount = battleBoxTrigger[battleBoxTrigger.length - 1];
        // 检查花费
        int cost = (int) Math.ceil(((double) (maxCount - curCount)) / GlobalConst.SpiritBattlePointFull);
        PlayerHelper.delResources(player, Asset.gold.ID, cost, OpType.LingPoBattlePointFull);
        // 补满
        currencyModule.setCount(cid, maxCount);
        client.sendProtocol(resp);
    }

    protected void spiritualAds(NetClient client, Object message) {
        BattleSpiritualGetTimesRequest_13000094 req = (BattleSpiritualGetTimesRequest_13000094) message;
        BattleSpiritualGetTimesResponse_13000095 resp = BattleSpiritualGetTimesResponse_13000095.getDefaultInstance();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        if (!player.isFuncOpen(InitialUI.SpiritBattle)) {
            client.sendProtocol(resp, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        BattleModule battleModule = player.getModule(BattleModule.class);
        LingPoBattle lingPoBattle = battleModule.getBattle(DungeonTypeEnum.LingPo);
        if (lingPoBattle == null) {
            client.sendProtocol(resp, ErrorMsgEnum.player_data_not_found.getId());
            return;
        }
        lingPoBattle.setAdsGetBattleTimes(false);
        player.handleEvent(EventTypeEnum.WatchAds);
        client.sendProtocol(resp);
    }

    protected void spiritualReceiveActivePoint(NetClient client, Object message) {
        BattleSpiritualReceiveActivePointRequest_13000092 req = (BattleSpiritualReceiveActivePointRequest_13000092) message;
        BattleSpiritualReceiveActivePointResponse_13000093.Builder resp = BattleSpiritualReceiveActivePointResponse_13000093.newBuilder();
        List<Integer> index = req.getIndexList();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        if (!player.isFuncOpen(InitialUI.SpiritBattle)) {
            client.sendProtocol(resp, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        PointRewardModule pointRewardModule = player.getPointRewardModule();
        ResultObject resultObject = pointRewardModule.addReward(PointRewardType.LingPo, 0, index.stream().mapToInt(Integer::intValue).toArray());
        if (!resultObject.isOK()) {
            client.sendProtocol(resp, resultObject.getErrorCode());
            return;
        }
        resp.addAllRewards((Iterable<? extends RewardInfo>) resultObject.getValue());
        client.sendProtocol(resp);
    }

    protected void spiritualInfo(NetClient client, Object message) {
        BattleSpiritualInfoRequest_13000090 req = (BattleSpiritualInfoRequest_13000090) message;
        BattleSpiritualInfoResponse_13000091.Builder resp = BattleSpiritualInfoResponse_13000091.newBuilder();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        if (!player.isFuncOpen(InitialUI.SpiritBattle)) {
            client.sendProtocol(resp, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        BattleModule battleModule = player.getModule(BattleModule.class);
        LingPoBattle lingPoBattle = battleModule.getBattle(DungeonTypeEnum.LingPo);
        if (lingPoBattle == null) {
            client.sendProtocol(resp, ErrorMsgEnum.player_data_not_found.getId());
            return;
        }
        resp.setBattleSpiritualInfo(lingPoBattle.buildBattleInfo(player));
        client.sendProtocol(resp);
    }

    protected void relive(NetClient client, Object message) {
        BattleReliveRequest_13000010 req = (BattleReliveRequest_13000010) message;
        BattleReliveResponse_13000011.Builder resp = BattleReliveResponse_13000011.newBuilder();
        int type = req.getType();
        if (type < 1 || type > 3) {
            client.sendProtocol(resp, ErrorMsgEnum.request_parameter_error.getId());
            return;
        }
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        BattleModule battleModule = player.getModule(BattleModule.class);
        if (battleModule.getReliveCountPerBattle() >= 2) {
            client.sendProtocol(resp, ErrorMsgEnum.times_limit.getId());
            return;
        }
        if (type == 1) {
            if (battleModule.getShareReliveCount() >= GlobalConst.ShareResurrection) {
                client.sendProtocol(resp, ErrorMsgEnum.free_times_limit.getId());
                return;
            }
            battleModule.setShareReliveCount(battleModule.getShareReliveCount() + 1);
        } else if (type == 2) {
            if (battleModule.getAdReliveCount() >= GlobalConst.AdResurrection) {
                client.sendProtocol(resp, ErrorMsgEnum.free_times_limit.getId());
                return;
            }
            battleModule.setAdReliveCount(battleModule.getAdReliveCount() + 1);
            player.handleEvent(EventTypeEnum.WatchAds);
        } else if (type == 3) {
            PlayerHelper.delResources(player, GlobalConst.ResurrectionConsume, OpType.Relive);
        }
        battleModule.setReliveCountPerBattle(battleModule.getReliveCountPerBattle() + 1);
        client.sendProtocol(resp);
    }

    protected void nightmareRealmQuick(NetClient client, Object message) {
        BattleNightmareRealmQuickRequest_13000084 req = (BattleNightmareRealmQuickRequest_13000084) message;
        BattleNightmareRealmQuickResponse_13000085.Builder resp = BattleNightmareRealmQuickResponse_13000085.newBuilder();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        if (!player.isFuncOpen(InitialUI.NightmareRealm)) {
            client.sendProtocol(resp, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        BattleModule battleModule = player.getModule(BattleModule.class);
        MengYanMiJingBattle mengYanMiJingBattle = battleModule.getBattle(DungeonTypeEnum.MengYanMiJing);
        List<Integer> rewardBattleIds = mengYanMiJingBattle.getRewardBattleIds();
        int maxSweepBattle = mengYanMiJingBattle.maxSweepBattle();
        List<RewardInfo> rewardsList = new ArrayList<>();
        BattleConfig battleConfig = BattleManager.instance().getNullable(maxSweepBattle);
        if (battleConfig != null) {
            if (battleConfig.ClearGameReward > 0 && !rewardBattleIds.contains(battleConfig.ID)) {
                List<RewardInfo> list = PlayerHelper.addReward(player, battleConfig.ClearGameReward, player.getWelfareValue(WelfareTypeEnum.MengYanQuickBattleAward), OpType.MengYanMiJingSweep);
                rewardsList.addAll(list);
                rewardBattleIds.add(battleConfig.ID);
            }
        }
        //		while (battleConfig != null) {
        //			if (battleConfig.ClearGameReward > 0 && !rewardBattleIds.contains(battleConfig.ID)) {
        //				List<RewardInfo> list = PlayerHelper
        //						.addReward(player, battleConfig.ClearGameReward, player.getWelfareValue(WelfareTypeEnum.MengYanQuickBattleAward),
        //								OpType.MengYanMiJingFirst);
        //				rewardsList.addAll(list);
        //				rewardBattleIds.add(battleConfig.ID);
        //			}
        //			battleConfig = BattleManager.instance().getNullable(battleConfig.preBattle);
        //		}
        resp.addAllRewards(rewardsList);
        client.sendProtocol(resp);
    }

    protected void nightmareRealmBuff(NetClient client, Object message) {
        BattleNightmareRealmBuffUpdateRequest_13000082 req = (BattleNightmareRealmBuffUpdateRequest_13000082) message;
        BattleNightmareRealmBuffUpdateResponse_13000083.Builder resp = BattleNightmareRealmBuffUpdateResponse_13000083.newBuilder();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        if (!player.isFuncOpen(InitialUI.NightmareRealm)) {
            client.sendProtocol(resp, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        BattleModule battleModule = player.getModule(BattleModule.class);
        MengYanMiJingBattle mengYanMiJingBattle = battleModule.getBattle(DungeonTypeEnum.MengYanMiJing);
        int buffRefreshTimes = mengYanMiJingBattle.getBuffRefreshTimes();
        if (buffRefreshTimes <= 0) {
            client.sendProtocol(resp, ErrorMsgEnum.times_limit.getId());
            return;
        }
        Map<Integer, Integer> buffIdsList = req.getBuffIdsMap();
        if (!buffIdsList.isEmpty()) {
            mengYanMiJingBattle.getBuffIdsMap().clear();
            mengYanMiJingBattle.getBuffIdsMap().putAll(buffIdsList);
        }
        mengYanMiJingBattle.setBuffRefreshTimes(buffRefreshTimes - 1);
        client.sendProtocol(resp);
    }

    protected void nightmareRealm(NetClient client, Object message) {
        BattleNightmareRealmRequest_13000080 req = (BattleNightmareRealmRequest_13000080) message;
        BattleNightmareRealmResponse_13000081.Builder resp = BattleNightmareRealmResponse_13000081.newBuilder();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        if (!player.isFuncOpen(InitialUI.NightmareRealm)) {
            client.sendProtocol(resp, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        HeroModule heroModule = player.getHeroModule();
        BattleModule battleModule = player.getModule(BattleModule.class);
        Map<Integer, List<String>> lineups = battleModule.getLineups(9);
        if (lineups != null) {
            lineups.forEach((k, v) -> {
                LineupInfo.Builder lineup = LineupInfo.newBuilder();
                lineup.setSeq(k);
                Iterator<String> iterator = v.iterator();
                while (iterator.hasNext()) {
                    String string = (String) iterator.next();
                    if (heroModule.get(Long.parseLong(string)) == null) {
                        iterator.remove();
                    }
                }
                lineup.addAllHeroUid(v);
                resp.addLineups(lineup);
            });
        }
        MengYanMiJingBattle mengYanMiJingBattle = battleModule.getBattle(DungeonTypeEnum.MengYanMiJing);
        resp.setBuffRefreshTimes(mengYanMiJingBattle.getBuffRefreshTimes());
        resp.setStartBattle(mengYanMiJingBattle.getStartBattleId());
        resp.putAllBuffIds(mengYanMiJingBattle.getBuffIdsMap());
        resp.addAllQuickRewardId(mengYanMiJingBattle.getRewardBattleIds());
        resp.addAllRandomBuff(mengYanMiJingBattle.getRandomBuff());
        boolean canQuick = mengYanMiJingBattle.isCanQuick() && mengYanMiJingBattle.isTodayCanQuick();
        if (!mengYanMiJingBattle.getRewardBattleIds().isEmpty()) {
            canQuick = false;
        }
        resp.setCanQuickReward(canQuick);
        client.sendProtocol(resp);
    }

    protected void lineupChoose(NetClient client, Object message) {
        BattleLineupChooseRequest_13000031 req = (BattleLineupChooseRequest_13000031) message;
        BattleLineupChooseResponse_13000032.Builder resp = BattleLineupChooseResponse_13000032.newBuilder();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        BattleModule battleModule = player.getModule(BattleModule.class);
        int battleType = req.getBattleType();
        int seq = req.getSeq();
        battleModule.updateLineupChoose(battleType, seq);
        client.sendProtocol(resp);
    }

    protected void lineup(NetClient client, Object message) {
        BattleLineupRequest_13000048 req = (BattleLineupRequest_13000048) message;
        BattleLineupResponse_13000049.Builder resp = BattleLineupResponse_13000049.newBuilder();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        BattleModule battleModule = player.getModule(BattleModule.class);
        int battleType = req.getBattleType();
        LineupInfo lineup = req.getLineup();
        battleModule.updateLineup(battleType, lineup.getSeq(), lineup.getHeroUidList());
        client.sendProtocol(resp);
    }

    protected void daoHeartRewardInfo(NetClient client, Object message) {
        BattleDaoHeartSweepRequest_13000064 req = (BattleDaoHeartSweepRequest_13000064) message;
        BattleDaoHeartSweepResponse_13000065.Builder resp = BattleDaoHeartSweepResponse_13000065.newBuilder();
        int type = req.getType();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        BattleModule battleModule = player.getModule(BattleModule.class);
        DaoHeartBattle daoHeartBattle = battleModule.getBattle(type);
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
        BattleModule battleModule = player.getModule(BattleModule.class);
        DaoHeartBattle daoHeartBattle = battleModule.getBattle(type);
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
        OpType opType = type == DungeonTypeEnum.DaoHeart.getId() ? OpType.DaoXinComplete : type == DungeonTypeEnum.XinMo.getId() ? OpType.XinMoComplete : OpType.YaoWangComplete;
        resp.addAllRewards(PlayerHelper.addReward(player, battleConfig.ClearGameReward, opType));
        client.sendProtocol(resp);
    }

    protected void daoHeartSweepBatch(NetClient client, Object message) {
        BattleDaoHeartSweepBatchRequest_13000062 req = (BattleDaoHeartSweepBatchRequest_13000062) message;
        BattleDaoHeartSweepBatchResponse_13000063.Builder resp = BattleDaoHeartSweepBatchResponse_13000063.newBuilder();
        int id = req.getId();
        int type = req.getType();
        boolean pay = req.getPay();
        BattleConfig battleConfig = BattleManager.instance().get(id);
        if (battleConfig == null) {
            client.sendProtocol(resp, ErrorMsgEnum.illegal_request.getId());
            return;
        }
        ;
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        BattleModule battleModule = player.getModule(BattleModule.class);
        DaoHeartBattle daoHeartBattle = battleModule.getBattle(type);
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
        int payRemaning = 0;
        if (pay) {
            int paySweep = daoHeartBattle.getPaySweep();
            payRemaning = daoHeartBattle.getMaxPaySweepCount() - paySweep;
            if (payRemaning > 0) {
                daoHeartBattle.setPaySweep(paySweep + payRemaning);
            }
        }
        int allCount = freeRemaning + payRemaning;
        if (allCount == 0) {
            client.sendProtocol(resp, ErrorMsgEnum.times_limit.getId());
            return;
        }
        if (pay) {
            List<Integer> payList = new ArrayList<>();
            int[][] paySweepCostAll = daoHeartBattle.getPaySweepCostAll();
            for (int i = 0; i < allCount; i++) {
                payList.add(paySweepCostAll[i][0]);
                payList.add(paySweepCostAll[i][1]);
            }
            int[] payArray = new int[payList.size()];
            for (int i = 0; i < payArray.length; i++) {
                payArray[i] = payList.get(i);
            }
            PlayerHelper.delResources(player, payArray, opType);
        }
        for (int i = 0; i < allCount; i++) {
            if (type == DungeonTypeEnum.YaoWang.getId()) {
                int welfareValue = player.getWelfareValue(WelfareTypeEnum.DemonKingFairy);
                List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.SweepReward, welfareValue, opType);
                resp.addAllRewards(reward);
            } else {
                List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.SweepReward, opType);
                resp.addAllRewards(reward);
            }
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
        BattleModule battleModule = player.getModule(BattleModule.class);
        DaoHeartBattle daoHeartBattle = battleModule.getBattle(type);
        if (daoHeartBattle == null) {
            client.sendProtocol(resp, ErrorMsgEnum.illegal_request.getId());
            return;
        }
        if (daoHeartBattle.getCompleteBattleId() != id) {
            client.sendProtocol(resp, ErrorMsgEnum.illegal_request.getId());
            return;
        }
        IBattleHandler battleHandler = battleModule.getBattle(type);
        int errorCode = battleHandler.check(id, 0);
        if (errorCode > 0) {
            client.sendProtocol(resp, errorCode);
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
            PlayerHelper.delResources(player, daoHeartBattle.getPaySweepCost(), opType);
            daoHeartBattle.setPaySweep(daoHeartBattle.getPaySweep() + 1);
        }
        BattleConfig battleConfig = BattleManager.instance().get(id);
        //		WelfareTypeEnum welfareType = type == DungeonTypeEnum.DaoHeart.getId() ? WelfareTypeEnum.DaoHeartFairy
        //				: type == DungeonTypeEnum.XinMo.getId() ? WelfareTypeEnum.InnerDemonsFairy : WelfareTypeEnum.DemonKingFairy;
        //		int welfareValue = player.getWelfareValue(welfareType);
        int welfareValue = 0;
        if (type == DungeonTypeEnum.YaoWang.getId()) {
            welfareValue = player.getWelfareValue(WelfareTypeEnum.DemonKingFairy);
        } else if (type == DungeonTypeEnum.DaoHeart.getId()) {
            welfareValue = player.getWelfareValue(WelfareTypeEnum.DaoHeartFairy);
        } else if (type == DungeonTypeEnum.XinMo.getId()) {
            welfareValue = player.getWelfareValue(WelfareTypeEnum.InnerDemonsFairy);
        }
        List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.SweepReward, welfareValue, opType);
        resp.addAllRewards(reward);
        client.sendProtocol(resp);
    }

    protected void daoHeart(NetClient client, Object message) {
        BattleDaoHeartRequest_13000055 req = (BattleDaoHeartRequest_13000055) message;
        BattleDaoHeartResponse_13000056.Builder resp = BattleDaoHeartResponse_13000056.newBuilder();
        int type = req.getType();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        if (type == DungeonTypeEnum.DaoHeart.getId()) {
            if (!player.isFuncOpen(InitialUI.DaoXinLLiLian)) {
                client.sendProtocol(resp, ErrorMsgEnum.func_not_open.getId());
                return;
            }
        } else if (type == DungeonTypeEnum.XinMo.getId()) {
            if (!player.isFuncOpen(InitialUI.XinMoShiLian)) {
                client.sendProtocol(resp, ErrorMsgEnum.func_not_open.getId());
                return;
            }
        } else if (type == DungeonTypeEnum.YaoWang.getId()) {
            if (!player.isFuncOpen(InitialUI.YaoWangBiePao)) {
                client.sendProtocol(resp, ErrorMsgEnum.func_not_open.getId());
                return;
            }
        }
        BattleModule battleModule = player.getModule(BattleModule.class);
        DaoHeartBattle daoHeartBattle = battleModule.getBattle(type);
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
        BattleModule battleModule = player.getModule(BattleModule.class);
        PointRewardModule pointRewardModule = player.getPointRewardModule();
        BattleDayChallenge battle = battleModule.getBattle(DungeonTypeEnum.DayChallenge);
        if (battle == null) {
            client.sendProtocol(resp, ErrorMsgEnum.illegal_request.getId());
            return;
        }
        ResultObject reward = pointRewardModule.addReward(PointRewardType.DAY_CHALLENGE, battle.getBattleId(), index);
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
        BattleModule battleModule = player.getModule(BattleModule.class);
        if (!battleModule.isBattlePass(id)) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.illegal_request.getId());
            return;
        }
        if (battleModule.getDaySweepCount() >= GlobalConst.SweepNum) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.times_limit.getId());
            return;
        }
        PlayerHelper.delResourcesWithConsume(player, battleConfig.cost, OpType.BattleSweep);
        battleModule.setDaySweepCount(battleModule.getDaySweepCount() + 1);
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
        BattleModule battleModule = player.getModule(BattleModule.class);
        if (!battleModule.isHCBattlePass(id)) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.illegal_request.getId());
            return;
        }
        if (battleModule.getDaySweepCount() >= GlobalConst.SweepNum) {
            player.handleEvent(EventTypeEnum.WatchAds);
        }
        PlayerHelper.delResourcesWithConsume(player, battleConfig.cost, OpType.BattleSweep);
        battleModule.setDaySweepCount(battleModule.getDaySweepCount() + 1);
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
        BattleModule battleModule = player.getModule(BattleModule.class);
        List<Integer> storeStaminas = battleModule.getStoreStaminas();
        if (!storeStaminas.contains(time)) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.player_data_not_found.getId());
            return;
        }
        storeStaminas.remove(Integer.valueOf(time));
        if (battleModule.isStaminaExpire(DateUtil.currentTimeSeconds(), time)) {
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
        int multiple = request.getMultiple(); 
        BattlePatrolRewardResponse_13000045.Builder resp = BattlePatrolRewardResponse_13000045.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleModule battleModule = player.getModule(BattleModule.class);
        if (!player.isFuncOpen(InitialUI.HangingUpp)) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
            return;
        }
        //		int seconds = DateUtil.currentTimeSeconds() - playerModule.getLastPatrolRewardTime();
        if (advertising && !isFast) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
            return;
        }
        if (multiple < 1 || !player.hasWelfare(WelfareTypeEnum.TravelTimeMultiple)) {
        	multiple = 1 ;
		}
        if (isFast) {
            // 快速巡逻次数处理
            if (advertising) {
                int quickPatrolCount = battleModule.getAdPatrolCount();
                if (quickPatrolCount >= GlobalConst.AdPatrolCnt) {
                    client.sendProtocol(resp, ErrorMsgEnum.times_limit.getId());
                    return;
                }
                battleModule.setAdPatrolCount(quickPatrolCount + 1);
                player.handleEvent(EventTypeEnum.WatchAds);
            } else {
                int quickPatrolCount = battleModule.getQuickPatrolCount();
                if (!player.hasWelfare(WelfareTypeEnum.TravelTimeMultiple)) {
                	if (quickPatrolCount >= GlobalConst.QuickPatrolCnt + player.getWelfareValue(WelfareTypeEnum.QuicPatrolCnt)) {
                		client.sendProtocol(resp, ErrorMsgEnum.times_limit.getId());
                		return;
                	}
        		}
                // 消耗
                PlayerHelper.delResourcesWithConsume(player, GlobalConst.QuickPatrolConsume ,multiple, OpType.Patrol);
                battleModule.setQuickPatrolCount(quickPatrolCount + 1);
            }
        }
        PatrolConfig patrolConfig = PatrolManager.instance().get(player.getBattleModule().getFightMainBattleId());

        if (isFast) {
        	for (int i = 0; i < multiple; i++) {
        		List<RewardInfo> reward = PlayerHelper.addReward(player, patrolConfig.SweepRandomID, OpType.Patrol); 
        		resp.addAllRewards(reward); 
			}
        } else {
            // 最大巡逻时间
            // 巡逻时间
            int minute = 0;
            int hours = 0;
            int maxSeconds = GlobalConst.MaximumPatrolDuration;
            int welfareValue = player.getWelfareValue(WelfareTypeEnum.TravelTime);
            if (welfareValue > 0) {
                maxSeconds += welfareValue * 60 * 60;
            }
            int seconds = DateUtil.currentTimeSeconds() - battleModule.getLastPatrolRewardTime();
            if (seconds >= maxSeconds) {
                seconds = maxSeconds;
            }
            minute = seconds / 60;
            hours = minute / 60;
            int exp = BattleHelper.calcPatrolExpAdd(player, minute, true);
            int gold = BattleHelper.calcPatrolGoldAdd(player, minute, true);
            PlayerHelper.addResources(player, Asset.playerExp.ID, exp, OpType.Patrol);
            PlayerHelper.addResources(player, Asset.gold.ID, gold, OpType.Patrol);
            for (int i = 0; i < hours; i++) {
            	List<RewardInfo> reward = PlayerHelper.addReward(player, patrolConfig.IncomeRandomID, OpType.Patrol);
            	resp.addAllRewards(reward);
            }
            resp.setExp(exp);
            resp.setGold(gold);
        }
        if (!isFast) {
            battleModule.setPatrolRewardTime();
        }
        player.handleEvent(EventTypeEnum.Patrol, isFast);
        client.sendProtocol(resp);
    }

    protected void rougeRefresh(NetClient client, Object message) {
        BattleRougeRefreshRequest_13000052 req = (BattleRougeRefreshRequest_13000052) message;
        BattleRougeRefreshResponse_13000053.Builder resp = BattleRougeRefreshResponse_13000053.newBuilder();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        BattleModule battleModule = player.getModule(BattleModule.class);
        int freeRougeTimes = battleModule.getFreeRougeTimes();
        if (freeRougeTimes < 3) {
            battleModule.setFreeRougeTimes(freeRougeTimes + 1);
        } else {
            // TODO 看广告， 和最大次数
        }
        client.sendProtocol(resp);
    }

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
        BattleModule battleModule = player.getModule(BattleModule.class);
        for (int i = 0; i < indexList.size(); i++) {
            int index = indexList.get(i);
            int id = idList.get(i);
            //			boolean pass = battleModule.isExploreChapterPass(id);
            //			if (!pass) {
            //				client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
            //				return;
            //			}
            Chapter chapter = battleModule.getChapter(id);
            if (chapter == null) {
                continue;
            }
            List<Integer> rewards = chapter.getRewards();
            if (rewards.contains(index)) {
                client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
                return;
            }
            int minute = chapter.getHpPercent();
            BattleConfig battleConfig = BattleManager.instance().get(id);
            if (index == 0 && minute < battleConfig.BattleBoxTrigger[0]) {
                client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
                return;
            }
            if (index == 1 && minute < battleConfig.BattleBoxTrigger[1]) {
                client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
                return;
            }
            if (index == 2 && minute < battleConfig.BattleBoxTrigger[2]) {
                client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
                return;
            }
            //			if (index == 2 && !chapter.getPass()) {
            //				client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
            //				return;
            //			}
        }
        for (int i = 0; i < indexList.size(); i++) {
            int index = indexList.get(i);
            int id = idList.get(i);
            BattleConfig battleConfig = BattleManager.instance().get(id);
            Chapter chapter = battleModule.getChapter(id);
            if (chapter == null) {
                continue;
            }
            chapter.getRewards().add(index);
            List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.BattleBoxRandomId[index], OpType.BattleEnd);
            resp.addAllReward(reward);
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
        BattleModule battleModule = player.getModule(BattleModule.class);
        for (int i = 0; i < indexList.size(); i++) {
            int index = indexList.get(i);
            int id = idList.get(i);
            //			boolean pass = battleModule.isExploreChapterPass(id);
            //			if (!pass) {
            //				client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
            //				return;
            //			}
            Chapter chapter = battleModule.getChapter(id);
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
            Chapter chapter = battleModule.getChapter(id);
            chapter.getRewards().add(index);
        }
        client.sendProtocol(resp);
    }

    public void start(NetClient client, Object message) {
        BattleFieldStartRequest_13000001 req = (BattleFieldStartRequest_13000001) message;
        BattleFieldStartResponse_13000002.Builder resp = BattleFieldStartResponse_13000002.newBuilder();
        int type = req.getType();
        int id = req.getTypeId();
        int subId = req.getFieldId();
        long helpPlayerId = req.getHelpPlayerId();
        //		String uidString = req.getUid();
        //		long uid = StringUtils.isEmpty(uidString) ? 0 : Long.parseLong(uidString);
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        BattleModule battleModule = player.getModule(BattleModule.class);
        //		long randomSeed = System.currentTimeMillis() ;
        IBattleHandler battleHandler = battleModule.getBattle(type);
        int errorCode = battleHandler.check(id, subId, helpPlayerId);
        if (errorCode > 0) {
            client.sendProtocol(resp, errorCode);
            return;
        }
        errorCode = battleHandler.battleStart(id, subId);
        if (errorCode == 0) {
            // 设置当前在打的关卡数据
            battleModule.setAttackingData(0, type, id, subId, 0, 0);
            if (battleHandler instanceof HCBattleHandler) {
                // 触发事件
                player.handleEvent(EventTypeEnum.HCBattleStart, id, 0);
            } else if (battleHandler instanceof XiYouBattleHandler) {
                // 触发事件
                player.handleEvent(EventTypeEnum.BattleStart, id, 0);
            }
            AttrModule module = player.getModule(AttrModule.class);
            module.calcAllAttr();
            resp.setAttrs(module.buildBattleAttrs());
        }
        //		resp.setRandomSeed(randomSeed + "");
        client.sendProtocol(resp, errorCode);
    }

    protected void rewardMultiple(NetClient client, Object message) {
        BattleShareRequest_13000007 req = (BattleShareRequest_13000007) message;
        BattleShareResponse_13000008.Builder resp = BattleShareResponse_13000008.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleModule battleModule = player.getModule(BattleModule.class);
        int battleRewardMultipleTimes = battleModule.getBattleRewardMultipleTimes();
        if (battleRewardMultipleTimes >= GlobalConst.Share) {
            client.sendProtocol(resp, ErrorMsgEnum.times_limit.getId());
            return;
        }
        List<RewardInfo> lastBattleRewards = battleModule.getLastBattleRewards();
        if (lastBattleRewards == null) {
            client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
            return;
        }
        battleModule.setBattleRewardMultipleTimes(battleRewardMultipleTimes + 1);
        List<RewardInfo> multipleRewards = PlayerHelper.multipleRewards(player, lastBattleRewards, GlobalConst.ShareCnt - 1);
        resp.addAllRewards(multipleRewards);
        client.sendProtocol(resp);
    }

    public void quickeEnd(NetClient client, Object message) {
        BattleFieldQuickEndRequest_13000005 req = (BattleFieldQuickEndRequest_13000005) message;
        BattleFieldQuickEndResponse_13000006.Builder resp = BattleFieldQuickEndResponse_13000006.newBuilder();
        int type = req.getType();
        int typeId = req.getTypeId();
        int subId = req.getSubId();
        boolean win = req.getWin();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleModule battleModule = player.getModule(BattleModule.class);
        IBattleHandler battleHandler = battleModule.getBattle(type);
        int errorCode = battleHandler.quickEndCheck(typeId, subId);
        if (errorCode > 0) {
            client.sendProtocol(resp, errorCode);
            return;
        }
        ResultObject<List<RewardInfo>> result = battleHandler.quickEnd(typeId, subId, true);
        if (result.getErrorCode() > 0) {
            client.sendProtocol(resp, result.getErrorCode());
            return;
        }
        player.handleEvent(EventTypeEnum.BattleEnd, typeId, subId, win, 0, 0);
        if (result.getValue() != null) {
            resp.addAllRewards(result.getValue());
        }
        client.sendProtocol(resp);
    }

    public void end(NetClient client, Object message) {
        BattleFieldEndRequest_13000003 req = (BattleFieldEndRequest_13000003) message;
        BattleFieldEndResponse_13000004.Builder resp = BattleFieldEndResponse_13000004.newBuilder();
        boolean win = req.getWin();
        //		String uidString = req.getUid();
        //		long uid = StringUtils.isEmpty(uidString) ? 0 : Long.parseLong(uidString);
        long playerId = client.getPlayerId();
        int hpPercent = req.getHpPercent();
        int killMonsterCount = req.getKillMonsterCount();
        int killMonsterBossCount = req.getKillMonsterBossCount();
        List<Integer> richManItemsList = req.getRichManItemsList();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleModule battleModule = player.getModule(BattleModule.class);
        int attackingId = battleModule.getAttackingId();
        int attackingSubId = battleModule.getAttackingSubId();
        int attackingType = battleModule.getAttackingType();
        long attackingUid = battleModule.getAttackingUid();
        int lineupId = battleModule.getLineupId();
        if (attackingType == 0) {
            client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
            return;
        }
        BattleConfig battleConfig = BattleManager.instance().get(attackingId);
        IBattleHandler battleHandler = battleModule.getBattle(attackingType);
        ResultObject<List<RewardInfo>> result = battleHandler.battleEnd(req, resp);
        if (result.getErrorCode() > 0) {
            client.sendProtocol(resp, result.getErrorCode());
            return;
        }
        player.handleEvent(EventTypeEnum.BattleEnd, attackingId, attackingSubId, win, killMonsterCount, killMonsterBossCount);
        List<RewardInfo> allRewards = new ArrayList<>();
        if (result.getValue() != null) {
            allRewards.addAll(result.getValue());
        }
        // 通用奖励
        List<RewardInfo> rewards = PlayerHelper.addReward(player, win ? battleConfig.WinRandom : battleConfig.FailRandom, OpType.BattleEnd);
        if (rewards != null) {
            allRewards.addAll(rewards);
        }
        //
        if (richManItemsList != null && !richManItemsList.isEmpty()) {
            for (Integer rid : richManItemsList) {
                RichManItemConfig richManItemConfig = RichManItemManager.instance().get(rid);
                if (richManItemConfig.ServerOpt == 1) {
                    if (richManItemConfig.OptType == 8) {
                        List<Goods> rewardAddition = RewardHelper.rewardAddition(allRewards, richManItemConfig.ItemParams[0]);
                        if (!rewardAddition.isEmpty()) {
                            List<RewardInfo> resources = PlayerHelper.addResources(player, rewardAddition, OpType.BattleEnd);
                            allRewards.addAll(resources);
                        }
                    }
                }
            }
            for (Integer rid : richManItemsList) {
                RichManItemConfig richManItemConfig = RichManItemManager.instance().get(rid);
                if (richManItemConfig.ServerOpt == 1) {
                    if (richManItemConfig.OptType == 9) {
                        // 返还体力
                        ConsumeConfig consumeConfig = ConsumeManager.instance().get(battleConfig.cost);
                        List<RewardInfo> resources = PlayerHelper.addResources(player, consumeConfig.cost, OpType.BattleEnd);
                        allRewards.addAll(resources);
                    }
                }
            }
        }
        if (req.getWin()) {
            player.handleEvent(EventTypeEnum.ChapterWin, attackingId, attackingSubId);
        }
        battleModule.setAttackingData(0, 0, 0, 0, 0, 0);
        resp.addAllRewards(allRewards);
        battleModule.setLastBattleRewards(allRewards);
        client.sendProtocol(resp);
        //		Chapter chapter = battleModule.getChapter(attackingDungeonId);
        //		GameLogger.pvefight(player, attackingDungeonId, 1, win, req.getBattleTime(), chapter == null ? 1 : chapter.getFinishTimes());
    }

    private void reward(NetClient client, Object message) {
        BattleRewardRequest_13000022 req = (BattleRewardRequest_13000022) message;
        List<Integer> idList = req.getIdList();
        List<Integer> indexList = req.getIndexList();
        BattleRewardResponse_13000023 defaultInstance = BattleRewardResponse_13000023.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleRewardResponse_13000023.Builder resp = BattleRewardResponse_13000023.newBuilder();
        client.sendProtocol(resp.build());
    }

    private void hCDataSave(NetClient client, Object message) {
        HCBattleDataSaveRequest_13000100 req = (HCBattleDataSaveRequest_13000100) message;
        String data = req.getData();
        HCBattleDataSaveResponse_13000101 defaultInstance = HCBattleDataSaveResponse_13000101.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        client.sendProtocol(defaultInstance);
    }

    private void hCData(NetClient client, Object message) {
        HCBattleDataRequest_13000102 req = (HCBattleDataRequest_13000102) message;
        HCBattleDataResponse_13000103 defaultInstance = HCBattleDataResponse_13000103.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        HCBattleDataResponse_13000103.Builder resp = HCBattleDataResponse_13000103.newBuilder();
        client.sendProtocol(resp.build());
    }

    protected void lingShanReward(NetClient client, Object message) {
        BattleLingShanRewardRequest_13000515 req = (BattleLingShanRewardRequest_13000515) message;
        BattleLingShanRewardResponse_13000516.Builder resp = BattleLingShanRewardResponse_13000516.newBuilder();
        int index = req.getIndex();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        BattleModule battleModule = player.getModule(BattleModule.class);
        LingShanWenChanBattle battle = battleModule.getBattle(DungeonTypeEnum.LingShanWenChan);
        long rewardBattleIds = battle.getRewardBattleIds();
        if (ByteHelp.isOne(rewardBattleIds, index)) {
            client.sendProtocol(resp, ErrorMsgEnum.repeat_request.getId());
            return;
        }
        LingShanConfig curFloorConfig = battle.getCurFloorConfig();
        if (curFloorConfig == null) {
            client.sendProtocol(resp, ErrorMsgEnum.illegal_request.getId());
            return;
        }
        if (GlobalConst.LingshanBonueLevel[index] > curFloorConfig.EndFloor) {
            client.sendProtocol(resp, ErrorMsgEnum.condition_check_error.getId());
            return;
        }
        List<RewardInfo> resources = PlayerHelper.addResources(player, GlobalConst.LingshanBonue[index], OpType.LingShanWenChanFloorReward);
        resp.addAllRewards(resources);
        long modifyBit = ByteHelp.modifyBit(rewardBattleIds, index);
        battle.setRewardBattleIds(modifyBit);
        client.sendProtocol(resp);
    }

    protected void lingShanBuyTimes(NetClient client, Object message) {
        BattleLingShanBuyTimesRequest_13000513 req = (BattleLingShanBuyTimesRequest_13000513) message;
        BattleLingShanBuyTimesResponse_13000514.Builder resp = BattleLingShanBuyTimesResponse_13000514.newBuilder();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        BattleModule battleModule = player.getModule(BattleModule.class);
        LingShanWenChanBattle battle = battleModule.getBattle(DungeonTypeEnum.LingShanWenChan);
        int battleTimes = battle.getBattleTimes();
        int payTimes = battle.getPayTimes();
        if (payTimes >= GameUtil.length(GlobalConst.LingshanChallangeCost)) {
            client.sendProtocol(resp, ErrorMsgEnum.times_limit.getId());
            return;
        }
        PlayerHelper.delResources(player, Asset.diamond.ID, GlobalConst.LingshanChallangeCost[battleTimes], OpType.LingShanBuyTimes);
        battle.setPayTimes(payTimes + 1);
        client.sendProtocol(resp);
    }

    protected void lingShanInfo(NetClient client, Object message) {
        BattleLingShanRequest_13000511 req = (BattleLingShanRequest_13000511) message;
        BattleLingShanResponse_13000512.Builder resp = BattleLingShanResponse_13000512.newBuilder();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        BattleModule battleModule = player.getModule(BattleModule.class);
        LingShanWenChanBattle battle = battleModule.getBattle(DungeonTypeEnum.LingShanWenChan);
        resp.setLastCompleteFloor(battle.getLastCompleteFloor());
        resp.setBattleTimes(battle.getBattleTimes());
        resp.setPayTimes(battle.getPayTimes());
        resp.addAllRewardBattleIds(ByteHelp.binary1List(battle.getRewardBattleIds()));
        // 排行榜玩家
        CompletionStage<List<PlayerRankInfo>> rankPagePlayerInfos = RankHelper.getRankPagePlayerInfos(player.getServerId(), RankType.LingShanWenChan, 1, 3);
        rankPagePlayerInfos.thenAccept(r -> {
            resp.addAllRankPlayers(r);
            client.sendProtocol(resp);
        }).exceptionally(player::handleFailFunction);
    }

    private void towerData(NetClient client, Object message) {
        BattleTowerDataRequest_13000521 req = (BattleTowerDataRequest_13000521) message;
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleModule battleModule = player.getModule(BattleModule.class);
        TowerBattle towerBattle = battleModule.getBattle(DungeonTypeEnum.GemTower);
        BattleTowerDataResponse_13000522.Builder resp = BattleTowerDataResponse_13000522.newBuilder();
        resp.setFloorCount(towerBattle.getFloorCount());
        resp.setRadomBuff(towerBattle.getRadomBuff());
        resp.setRewardCount(towerBattle.getRewardCount());
        Map<Integer, Integer> list = towerBattle.getCurFloor();
        list.forEach((k, v) -> resp.putCurFloor(k, v));
        client.sendProtocol(resp.build());
    }

    private void towerQuickEnd(NetClient client, Object message) {
        BattleTowerQuickEndRequest_13100524 req = (BattleTowerQuickEndRequest_13100524) message;
        int battleId = req.getBattleId();
        int quickCount = req.getQuickCount();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleModule battleModule = player.getModule(BattleModule.class);
        TowerBattle towerBattle = battleModule.getBattle(DungeonTypeEnum.GemTower);
        BattleTowerQuickEndResponse_13100525.Builder resp = BattleTowerQuickEndResponse_13100525.newBuilder();
        ResultObject<List<RewardInfo>> result = towerBattle.quickEnd(battleId, quickCount, true);
        if (result.getErrorCode() > 0) {
            client.sendProtocol(resp, result.getErrorCode());
            return;
        }
        //player.handleEvent(EventTypeEnum.BattleEnd, DungeonTypeEnum.GemTower, battleId,quickCount, true, 0, 0);
        if (result.getValue() != null) {
            resp.addAllRewards(result.getValue());
        }
        resp.setRewardCount(towerBattle.getRewardCount());
        client.sendProtocol(resp.build());
    }

    private void equipTowerData(NetClient client, Object message) {
        BattleEquipTowerDataRequest_13000526 req = (BattleEquipTowerDataRequest_13000526) message;
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleModule battleModule = player.getModule(BattleModule.class);
        EquipTowerBattle towerBattle = battleModule.getBattle(DungeonTypeEnum.EquipTower);
        BattleEquipTowerDataResponse_13000527.Builder resp = BattleEquipTowerDataResponse_13000527.newBuilder();
        resp.setTicketCount(towerBattle.getTicketCount());
        resp.setNextTicketTime(towerBattle.getNextGetTicketTime());
        resp.setCurFloor(towerBattle.getCurFloor());
        client.sendProtocol(resp.build());
    }

    private void equipTowerHelpPlayer(NetClient client, Object message) {
        BattleEquipTowerHelpPlayerRequest_13000531 req = (BattleEquipTowerHelpPlayerRequest_13000531) message;
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleModule battleModule = player.getModule(BattleModule.class);
        EquipTowerBattle towerBattle = battleModule.getBattle(DungeonTypeEnum.EquipTower);
        BattleEquipTowerHelpPlayerResponse_13000532.Builder resp = BattleEquipTowerHelpPlayerResponse_13000532.newBuilder();
        towerBattle.getHelpPlayerInfo(req.getFloor());
        towerBattle.getSimplePlayerMap().forEach((k, v) -> {
            if (k == req.getFloor()) {
                v.forEach(p -> {
                    resp.addHelpPlayerList(PlayerHelper.getSimplePlayer(p).toSimplePlayerInfo());
                });
            }
        });
        client.sendProtocol(resp.build());
    }

    private void equipTowerGetTicket(NetClient client, Object message) {
        BattleEquipTowerGetTicketRequest_13000533 req = (BattleEquipTowerGetTicketRequest_13000533) message;
        BattleEquipTowerGetTicketResponse_13000534 defaultInstance = BattleEquipTowerGetTicketResponse_13000534.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleEquipTowerGetTicketResponse_13000534.Builder resp = BattleEquipTowerGetTicketResponse_13000534.newBuilder();
        BattleModule battleModule = player.getModule(BattleModule.class);
        EquipTowerBattle towerBattle = battleModule.getBattle(DungeonTypeEnum.EquipTower);
        List<RewardInfo> getTicket = towerBattle.getTicket();
        resp.setTicketCount(towerBattle.getTicketCount());
        resp.setNextTicketTime(towerBattle.getNextGetTicketTime());
        if (getTicket != null && getTicket.size() > 0) {
            resp.setReward(getTicket.get(0));
        }
        client.sendProtocol(resp.build());
    }

    private void equipTowerGetHelpReward(NetClient client, Object message) {
        BattleEquipTowerGetHelpRewardRequest_13000535 req = (BattleEquipTowerGetHelpRewardRequest_13000535) message;
        List<Long> battleID = req.getBattleIDList();
        List<Integer> floor = req.getFloorList();
        BattleEquipTowerGetHelpRewardResponse_13000536 defaultInstance = BattleEquipTowerGetHelpRewardResponse_13000536.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleModule battleModule = player.getModule(BattleModule.class);
        EquipTowerBattle towerBattle = battleModule.getBattle(DungeonTypeEnum.EquipTower);
        List<RewardInfo> reward = towerBattle.getHelpReward(battleID, floor);
        BattleEquipTowerGetHelpRewardResponse_13000536.Builder resp = BattleEquipTowerGetHelpRewardResponse_13000536.newBuilder();
        resp.addAllRewards(reward);
        client.sendProtocol(resp.build());
    }

    private void equipTowerFindHelpReward(NetClient client, Object message) {
        BattleEquipTowerFindHelpRewardRequest_13000537 req = (BattleEquipTowerFindHelpRewardRequest_13000537) message;
        BattleEquipTowerFindHelpRewardResponse_13000538 defaultInstance = BattleEquipTowerFindHelpRewardResponse_13000538.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleEquipTowerFindHelpRewardResponse_13000538.Builder resp = BattleEquipTowerFindHelpRewardResponse_13000538.newBuilder();
        BattleModule battleModule = player.getModule(BattleModule.class);
        EquipTowerBattle towerBattle = battleModule.getBattle(DungeonTypeEnum.EquipTower);
        towerBattle.getHelpRewardMap().forEach((k, v) -> {
            v.forEach((k1, v1) -> {
                resp.addHelpReward(BaseMsg.EquipTowerHelpRewardInfo.newBuilder().setHelpfloor(k).setHelpReward(v1).setHelpID(k1));
            });
        });
        client.sendProtocol(resp.build());
    }

    private void lingShanFloorSkip(NetClient client, Object message) {
        BattleLingShanFloorSkipRequest_13000517 req = (BattleLingShanFloorSkipRequest_13000517) message;
        int floor = req.getFloor();
        BattleLingShanFloorSkipResponse_13000518 defaultInstance = BattleLingShanFloorSkipResponse_13000518.getDefaultInstance();
        BattleLingShanFloorSkipResponse_13000518.Builder resp = BattleLingShanFloorSkipResponse_13000518.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleModule battleModule = player.getModule(BattleModule.class);
        LingShanWenChanBattle battle = battleModule.getBattle(DungeonTypeEnum.LingShanWenChan);
        List<RewardInfo> rewards = battle.skipFloor(floor);
        resp.addAllRewards(rewards);
        client.sendProtocol(resp.build());
    }

    private void lingShanEnd(NetClient client, Object message) {
        BattleLingShanEndRequest_13000519 req = (BattleLingShanEndRequest_13000519) message;
        BattleLingShanEndResponse_1300051a defaultInstance = BattleLingShanEndResponse_1300051a.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleLingShanEndResponse_1300051a.Builder resp = BattleLingShanEndResponse_1300051a.newBuilder();
        BattleModule battleModule = player.getModule(BattleModule.class);
        LingShanWenChanBattle battle = battleModule.getBattle(DungeonTypeEnum.LingShanWenChan);
        int attackingSubId = battleModule.getAttackingSubId();
        List<RewardInfo> end = battle.end(battle.getLastCompleteFloor(), true);
        if (end != null && !end.isEmpty()) {
            resp.addAllRewards(end);
        }
        client.sendProtocol(resp.build());
    }

    private void mountainData(NetClient client, Object message) {
        BattleMountainDataRequest_13000539 req = (BattleMountainDataRequest_13000539) message;
        BattleMountainDataResponse_1300053a defaultInstance = BattleMountainDataResponse_1300053a.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleMountainDataResponse_1300053a.Builder resp = BattleMountainDataResponse_1300053a.newBuilder();
        BattleModule battleModule = player.getModule(BattleModule.class);
        DaShengXunShanBattle towerBattle = battleModule.getBattle(DungeonTypeEnum.MountainBattle);
        var mapData = towerBattle.getMountainMapData();
        resp.setScore(mapData.getScore());
        resp.setCurNodeId(mapData.getCurNodeId());
        resp.setScoreMax(mapData.getScoreMax());
        resp.setHp(mapData.getHp());
        resp.setRefreshNum(mapData.getRefreshNum());
        resp.setEndTime(mapData.getEndTime());
        resp.setRankId(mapData.getRankId());
        resp.setLevelPro(mapData.getLevelPro());
        mapData.getMapData().forEach((k, v) -> {
            v.forEach(node -> {
                var builder = BaseMsg.MountainMapNodeData.newBuilder()
                        .setNodeID(node.getNodeId())
                        .setNodeType(node.getNodeType())
                        .setEventId(node.getEventId())
                        .setLevelpro(node.getLevelpro())
                        .setNodeStatus(node.getNodeStatus());
                builder.addAllShopId(node.getShopId());
                builder.addAllMonsterId(node.getMonsterIds());
                resp.addMapdata(builder.build());
            });
        });
        mapData.getScoreReward().forEach((k, v) -> {
            resp.putScoreReward(k, v);
        });
         mapData.getBuffBag().forEach((k, v) -> {
            resp.putBuffBag(k, v);
        });
        client.sendProtocol(resp.build());
    }

    private void mountainFinishNode(NetClient client, Object message) {
        BattleMountainFinishNodeRequest_13000541 req = (BattleMountainFinishNodeRequest_13000541) message;
        int nodeId = req.getNodeId();
        List<Integer> param = req.getParamList();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleMountainFinishNodeResponse_13000542.Builder resp = BattleMountainFinishNodeResponse_13000542.newBuilder();
        BattleModule battleModule = player.getModule(BattleModule.class);
        DaShengXunShanBattle towerBattle = battleModule.getBattle(DungeonTypeEnum.MountainBattle);
        towerBattle.finishNode(nodeId, param);
        resp.setScore(towerBattle.getMountainMapData().getScore());
        resp.setScoreMax(towerBattle.getMountainMapData().getScoreMax());
        resp.setHp(towerBattle.getMountainMapData().getHp());
        towerBattle.getMountainMapData().getBuffBag().forEach((k, v) -> {
            resp.putBuffBag(k, v);
        });
        client.sendProtocol(resp.build());
    }

    private void mountainGetReward(NetClient client, Object message) {
        BattleMountainGetRewardRequest_13000543 req = (BattleMountainGetRewardRequest_13000543) message;
        int rewardId = req.getRewardId();
        BattleMountainGetRewardResponse_13000544 defaultInstance = BattleMountainGetRewardResponse_13000544.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleMountainGetRewardResponse_13000544.Builder resp = BattleMountainGetRewardResponse_13000544.newBuilder();
        BattleModule battleModule = player.getModule(BattleModule.class);
        DaShengXunShanBattle daShengXunShanBattle = battleModule.getBattle(DungeonTypeEnum.MountainBattle);
        List<RewardInfo>rewardInfos= daShengXunShanBattle.getReward(rewardId);
        if(rewardInfos!= null) {
            resp.addAllRewards(rewardInfos);
        }
        daShengXunShanBattle.getMountainMapData().getScoreReward().forEach((k, v) -> {
            resp.putScoreReward(k, v);
        });
        client.sendProtocol(resp.build());
    }

    private void mountainMapReset(NetClient client, Object message) {
        BattleMountainMapResetRequest_13000545 req = (BattleMountainMapResetRequest_13000545) message;
        BattleMountainMapResetResponse_13000546 defaultInstance = BattleMountainMapResetResponse_13000546.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleMountainMapResetResponse_13000546.Builder resp = BattleMountainMapResetResponse_13000546.newBuilder();
        BattleModule battleModule = player.getModule(BattleModule.class);
        DaShengXunShanBattle daShengXunShanBattle = battleModule.getBattle(DungeonTypeEnum.MountainBattle);
        daShengXunShanBattle.playerResetMap();
        var mapData = daShengXunShanBattle.getMountainMapData();
        resp.setCurNodeId(mapData.getCurNodeId());
        resp.setRefreshNum(mapData.getRefreshNum());
        resp.setRankId(mapData.getRankId());
        resp.setLevelPro(mapData.getLevelPro());
        mapData.getMapData().forEach((k, v) -> {
            v.forEach(node -> {
                var builder = BaseMsg.MountainMapNodeData.newBuilder()
                        .setNodeID(node.getNodeId())
                        .setNodeType(node.getNodeType())
                        .setEventId(node.getEventId())
                        .setLevelpro(node.getLevelpro())
                        .setNodeStatus(node.getNodeStatus());
                builder.addAllShopId(node.getShopId());
                builder.addAllMonsterId(node.getMonsterIds());
                resp.addMapdata(builder.build());
            });
        });
        client.sendProtocol(resp.build());
    }

    private void mountainNextFloor(NetClient client, Object message) {
        BattleMountainNextFloorRequest_13000547 req = (BattleMountainNextFloorRequest_13000547) message;
        int floor = req.getFloor();
        BattleMountainNextFlooResponse_13000548 defaultInstance = BattleMountainNextFlooResponse_13000548.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleMountainNextFlooResponse_13000548.Builder resp = BattleMountainNextFlooResponse_13000548.newBuilder();
        BattleModule battleModule = player.getModule(BattleModule.class);
        DaShengXunShanBattle daShengXunShanBattle = battleModule.getBattle(DungeonTypeEnum.MountainBattle);
        daShengXunShanBattle. nextFloor(floor);
        resp.setCurNodeId(daShengXunShanBattle.getMountainMapData().getCurNodeId());
        client.sendProtocol(resp.build());
    }
    private void mountainBuffBag(NetClient client, Object message) {
        BattleMountainBuffBagRequest_1300053b req = (BattleMountainBuffBagRequest_1300053b) message;
        BattleMountainBuffBagResponse_1300053c defaultInstance = BattleMountainBuffBagResponse_1300053c.getDefaultInstance();
        BattleMountainBuffBagResponse_1300053c.Builder resp = BattleMountainBuffBagResponse_1300053c.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleModule battleModule = player.getModule(BattleModule.class);
        DaShengXunShanBattle daShengXunShanBattle = battleModule.getBattle(DungeonTypeEnum.MountainBattle);
        var mapData = daShengXunShanBattle.getMountainMapData();
        mapData.getBuffBag().forEach((k, v) -> {
            resp.putBuffBag(k, v);
        });
        client.sendProtocol(resp.build());
    }
    private void pVEVPData(NetClient client, Object message) {
        BattlePVEVPDataRequest_13000549 req = (BattlePVEVPDataRequest_13000549) message;
        BattlePVEVPDataResponse_1300054a defaultInstance = BattlePVEVPDataResponse_1300054a.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleModule battleModule = player.getModule(BattleModule.class);
        PVEVPBattle pvevpBattle = battleModule.getBattle(DungeonTypeEnum.PVEVPBattle);
        BattlePVEVPDataResponse_1300054a.Builder resp = BattlePVEVPDataResponse_1300054a.newBuilder();
        resp.setChallengeCount(pvevpBattle.getTicketCount());
        resp.setEndTime(pvevpBattle.getEndTime());
        resp.setBuyCount(pvevpBattle.getBuyCount());
        client.sendProtocol(resp.build());
    }

    private void pVEVPRecord(NetClient client, Object message) {
        BattlePVEVPRecordRequest_13000550 req = (BattlePVEVPRecordRequest_13000550) message;
        BattlePVEVPRecordResponse_13000551 defaultInstance = BattlePVEVPRecordResponse_13000551.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattlePVEVPRecordResponse_13000551.Builder resp = BattlePVEVPRecordResponse_13000551.newBuilder();
        BattleModule battleModule = player.getModule(BattleModule.class);
        PVEVPBattle pvevpBattle = battleModule.getBattle(DungeonTypeEnum.PVEVPBattle);
        pvevpBattle.getBattleRecordFromRedis();
        pvevpBattle.recordDataList.forEach(v -> {
            BaseMsg.PVEVPRecordData.Builder builder = BaseMsg.PVEVPRecordData.newBuilder();
            builder.setResult(v.result);
            builder.setName(v.name);
            builder.setLevel(v.level);
            builder.setCombatEffectiveness(v.combatEffectiveness);
            builder.setScoreChange(v.scoreChange);
            builder.setHead(v.head);
            builder.setHeadFrame(v.headFrame);
            builder.setBattleTime(v.battleTime);
            builder.setType(v.type);
            builder.setPlayerId(String.valueOf(v.playerId));
            resp.addRecordList(builder);
        });
        client.sendProtocol(resp.build());
    }

    private void pVEVPChallenge(NetClient client, Object message) {
        BattlePVEVPChallengeRequest_13000552 req = (BattlePVEVPChallengeRequest_13000552) message;
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleModule battleModule = player.getModule(BattleModule.class);
        PVEVPBattle pvevpBattle = battleModule.getBattle(DungeonTypeEnum.PVEVPBattle);
        BattleMsg.BattlePVEVPChallengeResponse_13000553.Builder resp = BattleMsg.BattlePVEVPChallengeResponse_13000553.newBuilder();

        var dataLoadingStage =pvevpBattle.getRadomPlayer(req.getType());
        dataLoadingStage.thenAccept(r -> {
                r.forEach((k, v) -> {
                BaseMsg.PlayerRankInfo.Builder rb = BaseMsg.PlayerRankInfo.newBuilder();
                rb.setRank(v.getRankEntry().getRank());
                rb.setPlayer(v.getPlayer().toSimplePlayerInfo());
                long score = v.getRankEntry().getScore();
                rb.setScore((score < 0 ? 0 : score) + "");
                resp.addChallengePlayers(rb);

            } );
            client.sendProtocol(resp.build());})
        .exceptionally(player::handleFailFunction);
    }

    private void buyTicket(NetClient client, Object message) {
        BattleBuyTicketRequest_13000554 req = (BattleBuyTicketRequest_13000554) message;
        BattleBuyTicketResponse_13000555 defaultInstance = BattleBuyTicketResponse_13000555.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleBuyTicketResponse_13000555.Builder resp = BattleBuyTicketResponse_13000555.newBuilder();
        BattleModule battleModule = player.getModule(BattleModule.class);
        PVEVPBattle pvevpBattle = battleModule.getBattle(DungeonTypeEnum.PVEVPBattle);
        pvevpBattle.buyCount();
        resp.setBuyCount(pvevpBattle.getBuyCount());
        resp.setChallengeCount(pvevpBattle.getTicketCount());
        client.sendProtocol(resp.build());
    }

    private void equipTowerRecord(NetClient client, Object message) {
        BattleEquipTowerRecordRequest_13000528 req = (BattleEquipTowerRecordRequest_13000528) message;
        int floor = req.getFloor();
        String record = req.getRecord();
        BattleEquipTowerRecordResponse_13000529 defaultInstance = BattleEquipTowerRecordResponse_13000529.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleModule battleModule = player.getModule(BattleModule.class);
        EquipTowerBattle equipTowerBattle = battleModule.getBattle(DungeonTypeEnum.EquipTower);
        equipTowerBattle.setRecord(floor, record);
        client.sendProtocol(defaultInstance);
    }


}
