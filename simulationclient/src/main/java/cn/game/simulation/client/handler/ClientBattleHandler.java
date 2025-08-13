package cn.game.simulation.client.handler;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.protocol.protobuf.BaseMsg.EquipTowerHelpRewardInfo;
import cn.game.protocol.protobuf.BaseMsg.MountainMapNodeData;
import cn.game.protocol.protobuf.BaseMsg.PVEVPRecordData;
import cn.game.protocol.protobuf.BaseMsg.PlayerRankInfo;
import cn.game.protocol.protobuf.BaseMsg.SimplePlayerInfo;
import cn.game.protocol.protobuf.BattleMsg;
import cn.game.protocol.protobuf.BattleMsg.BattleBuyPvPTimeResponse_13000122;
import cn.game.protocol.protobuf.BattleMsg.BattleChapterRewardResponse_13000223;
import cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartResponse_13000056;
import cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepBatchResponse_13000063;
import cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepResponse_13000061;
import cn.game.protocol.protobuf.BattleMsg.BattleDayChallengeReceiveActivePointResponse_13000071;
import cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerDataResponse_13000527;
import cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerFindHelpRewardResponse_13000538;
import cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerGetHelpRewardResponse_13000536;
import cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerGetTicketResponse_13000534;
import cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerHelpPlayerResponse_13000532;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndResponse_13000004;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldQuickEndResponse_13000006;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldStartResponse_13000002;
import cn.game.protocol.protobuf.BattleMsg.BattleLineupChooseResponse_13000032;
import cn.game.protocol.protobuf.BattleMsg.BattleLineupInfo;
import cn.game.protocol.protobuf.BattleMsg.BattleLineupResponse_13000049;
import cn.game.protocol.protobuf.BattleMsg.BattleLingShanBuyTimesResponse_13000514;
import cn.game.protocol.protobuf.BattleMsg.BattleLingShanEndResponse_1300051a;
import cn.game.protocol.protobuf.BattleMsg.BattleLingShanFloorSkipResponse_13000518;
import cn.game.protocol.protobuf.BattleMsg.BattleLingShanResponse_13000512;
import cn.game.protocol.protobuf.BattleMsg.BattleLingShanRewardResponse_13000516;
import cn.game.protocol.protobuf.BattleMsg.BattleLostDayRewardResponse_13000204;
import cn.game.protocol.protobuf.BattleMsg.BattleLostInfoResponse_13000202;
import cn.game.protocol.protobuf.BattleMsg.BattleMountainDataResponse_1300053a;
import cn.game.protocol.protobuf.BattleMsg.BattleMountainFinishNodeResponse_13000542;
import cn.game.protocol.protobuf.BattleMsg.BattleMountainMapResetResponse_13000546;
import cn.game.protocol.protobuf.BattleMsg.BattleMountainNextFlooResponse_13000548;
import cn.game.protocol.protobuf.BattleMsg.BattleNightmareRealmBuffUpdateResponse_13000083;
import cn.game.protocol.protobuf.BattleMsg.BattleNightmareRealmQuickResponse_13000085;
import cn.game.protocol.protobuf.BattleMsg.BattleNightmareRealmResponse_13000081;
import cn.game.protocol.protobuf.BattleMsg.BattlePVEVPChallengeResponse_13000553;
import cn.game.protocol.protobuf.BattleMsg.BattlePVEVPDataResponse_1300054a;
import cn.game.protocol.protobuf.BattleMsg.BattlePVEVPRecordResponse_13000551;
import cn.game.protocol.protobuf.BattleMsg.BattlePatrolRewardResponse_13000045;
import cn.game.protocol.protobuf.BattleMsg.BattlePvPEndResponse_13000116;
import cn.game.protocol.protobuf.BattleMsg.BattlePvPInfoResponse_13000118;
import cn.game.protocol.protobuf.BattleMsg.BattlePvPStartResponse_13000114;
import cn.game.protocol.protobuf.BattleMsg.BattlePvPTargetListResponse_13000112;
import cn.game.protocol.protobuf.BattleMsg.BattleReliveResponse_13000011;
import cn.game.protocol.protobuf.BattleMsg.BattleRescueSkillIdResponse_13000058;
import cn.game.protocol.protobuf.BattleMsg.BattleRewardResponse_13000023;
import cn.game.protocol.protobuf.BattleMsg.BattleRogueAdvertiseResponse_13000013;
import cn.game.protocol.protobuf.BattleMsg.BattleRougeRefreshResponse_13000053;
import cn.game.protocol.protobuf.BattleMsg.BattleSecretscriptInfo;
import cn.game.protocol.protobuf.BattleMsg.BattleShareResponse_13000008;
import cn.game.protocol.protobuf.BattleMsg.BattleSpiritualChangeBattleResponse_13000099;
import cn.game.protocol.protobuf.BattleMsg.BattleSpiritualGetPointResponse_13000097;
import cn.game.protocol.protobuf.BattleMsg.BattleSpiritualGetTimesResponse_13000095;
import cn.game.protocol.protobuf.BattleMsg.BattleSpiritualInfo;
import cn.game.protocol.protobuf.BattleMsg.BattleSpiritualInfoResponse_13000091;
import cn.game.protocol.protobuf.BattleMsg.BattleSpiritualReceiveActivePointResponse_13000093;
import cn.game.protocol.protobuf.BattleMsg.BattleStaminaResponse_13000051;
import cn.game.protocol.protobuf.BattleMsg.BattleSweepResponse_13000025;
import cn.game.protocol.protobuf.BattleMsg.BattleTowerDataPush_13100523;
import cn.game.protocol.protobuf.BattleMsg.BattleTowerDataResponse_13000522;
import cn.game.protocol.protobuf.BattleMsg.BattleTowerQuickEndResponse_13100525;
import cn.game.protocol.protobuf.BattleMsg.BattleWorldBossBuyTimesResponse_13000304;
import cn.game.protocol.protobuf.BattleMsg.BattleWorldBossInfoResponse_13000302;
import cn.game.protocol.protobuf.BattleMsg.BattleWorldRewardResponse_13000306;
import cn.game.protocol.protobuf.BattleMsg.BattleXiangYaoChuMoResponse_13000502;
import cn.game.protocol.protobuf.BattleMsg.HCBattleDataResponse_13000103;
import cn.game.protocol.protobuf.BattleMsg.HCBattleDataSaveResponse_13000101;
import cn.game.protocol.protobuf.BattleMsg.HCBattleRewardResponse_13000028;
import cn.game.protocol.protobuf.BattleMsg.HCBattleSweepResponse_13000041;
import cn.game.protocol.protobuf.BattleMsg.LineupInfo;
import cn.game.protocol.protobuf.BattleMsg.PlayerBattleAttrs;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.simulation.client.Client;
import cn.game.protocol.protobuf.BattleMsg.BattleBuyTicketResponse_13000555;
import cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerRecordResponse_13000529;
import cn.game.protocol.protobuf.BattleMsg.BattleMountainBuffBagResponse_1300053c;

@Component
public class ClientBattleHandler extends BaseHandler {

    @Override
    protected int getModule() {
        return 0x13;
    }

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.BattleFieldStartResponse_13000002, this::fieldStart);
        putInvoker(PbProtocol.BattleFieldEndResponse_13000004, this::fieldEnd);
        putInvoker(PbProtocol.BattleFieldQuickEndResponse_13000006, this::fieldQuickEnd);
        putInvoker(PbProtocol.BattleShareResponse_13000008, this::share);
        putInvoker(PbProtocol.BattleReliveResponse_13000011, this::relive);
        putInvoker(PbProtocol.BattleRogueAdvertiseResponse_13000013, this::rogueAdvertise);
        putInvoker(PbProtocol.BattleRewardResponse_13000023, this::reward);
        putInvoker(PbProtocol.BattleChapterRewardResponse_13000223, this::chapterReward);
        putInvoker(PbProtocol.HCBattleRewardResponse_13000028, this::hCReward);
        putInvoker(PbProtocol.BattleSweepResponse_13000025, this::sweep);
        putInvoker(PbProtocol.HCBattleSweepResponse_13000041, this::hCSweep);
        putInvoker(PbProtocol.BattlePatrolRewardResponse_13000045, this::patrolReward);
        putInvoker(PbProtocol.BattleLineupResponse_13000049, this::lineup);
        putInvoker(PbProtocol.BattleLineupChooseResponse_13000032, this::lineupChoose);
        putInvoker(PbProtocol.HCBattleDataSaveResponse_13000101, this::hCDataSave);
        putInvoker(PbProtocol.HCBattleDataResponse_13000103, this::hCData);
        putInvoker(PbProtocol.BattleDaoHeartResponse_13000056, this::daoHeart);
        putInvoker(PbProtocol.BattleDaoHeartSweepResponse_13000061, this::daoHeartSweep);
        putInvoker(PbProtocol.BattleDaoHeartSweepBatchResponse_13000063, this::daoHeartSweepBatch);
        putInvoker(PbProtocol.BattleDaoHeartSweepResponse_13000065, this::daoHeartSweep);
        putInvoker(PbProtocol.BattleDaoHeartSweepResponse_13000067, this::daoHeartSweep);
        putInvoker(PbProtocol.BattleNightmareRealmResponse_13000081, this::nightmareRealm);
        putInvoker(PbProtocol.BattleNightmareRealmBuffUpdateResponse_13000083, this::nightmareRealmBuffUpdate);
        putInvoker(PbProtocol.BattleNightmareRealmQuickResponse_13000085, this::nightmareRealmQuick);
        putInvoker(PbProtocol.BattleDayChallengeReceiveActivePointResponse_13000071, this::dayChallengeReceiveActivePoint);
        putInvoker(PbProtocol.BattleSpiritualInfoResponse_13000091, this::spiritualInfo);
        putInvoker(PbProtocol.BattleSpiritualReceiveActivePointResponse_13000093, this::spiritualReceiveActivePoint);
        putInvoker(PbProtocol.BattleSpiritualGetTimesResponse_13000095, this::spiritualGetTimes);
        putInvoker(PbProtocol.BattleSpiritualGetPointResponse_13000097, this::spiritualGetPoint);
        putInvoker(PbProtocol.BattleSpiritualChangeBattleResponse_13000099, this::spiritualChange);
        putInvoker(PbProtocol.BattleLostInfoResponse_13000202, this::lostInfo);
        putInvoker(PbProtocol.BattleLostDayRewardResponse_13000204, this::lostDayReward);
        putInvoker(PbProtocol.BattleWorldBossInfoResponse_13000302, this::worldBossInfo);
        putInvoker(PbProtocol.BattleWorldBossBuyTimesResponse_13000304, this::worldBossBuyTimes);
        putInvoker(PbProtocol.BattleWorldRewardResponse_13000306, this::worldReward);
        putInvoker(PbProtocol.BattleRougeRefreshResponse_13000053, this::rougeRefresh);
        putInvoker(PbProtocol.BattleStaminaResponse_13000051, this::stamina);
        putInvoker(PbProtocol.BattleRescueSkillIdResponse_13000058, this::rescueSkillId);
        putInvoker(PbProtocol.BattlePvPTargetListResponse_13000112, this::targetPvPListResponse);
        putInvoker(PbProtocol.BattlePvPStartResponse_13000114, this::pvPStart);
        putInvoker(PbProtocol.BattlePvPEndResponse_13000116, this::pvPEnd);
        putInvoker(PbProtocol.BattlePvPInfoResponse_13000118, this::pvPInfo);
        putInvoker(PbProtocol.BattleBuyPvPTimeResponse_13000122, this::buyPvPTime);
        putInvoker(PbProtocol.BattleXiangYaoChuMoResponse_13000502, this::xiangYaoChuMo);
        putInvoker(PbProtocol.BattleLingShanResponse_13000512, this::lingShan);
        putInvoker(PbProtocol.BattleLingShanBuyTimesResponse_13000514, this::lingShanBuyTimes);
        putInvoker(PbProtocol.BattleLingShanRewardResponse_13000516, this::lingShanReward);
        putInvoker(PbProtocol.BattleTowerDataResponse_13000522, this::towerData);
        putInvoker(PbProtocol.BattleTowerDataPush_13100523, this::towerDataPush);
        putInvoker(PbProtocol.BattleTowerQuickEndResponse_13100525, this::towerQuickEnd);
        putInvoker(PbProtocol.BattleEquipTowerHelpPlayerResponse_13000532, this::equipTowerHelpPlayer);
        putInvoker(PbProtocol.BattleEquipTowerGetTicketResponse_13000534, this::equipTowerGetTicket);
        putInvoker(PbProtocol.BattleEquipTowerGetHelpRewardResponse_13000536, this::equipTowerGetHelpReward);
        putInvoker(PbProtocol.BattleEquipTowerFindHelpRewardResponse_13000538, this::equipTowerFindHelpReward);
        putInvoker(PbProtocol.BattleEquipTowerDataResponse_13000527, this::equipTowerData);
        putInvoker(PbProtocol.BattleLingShanFloorSkipResponse_13000518, this::lingShanFloorSkip);
        putInvoker(PbProtocol.BattleLingShanEndResponse_1300051a, this::lingShanEnd);
        putInvoker(PbProtocol.BattleMountainDataResponse_1300053a, this::mountainData);
        putInvoker(PbProtocol.BattleMountainFinishNodeResponse_13000542, this::mountainFinishNode);
        putInvoker(PbProtocol.BattleMountainGetRewardResponse_13000544, this::mountainGetReward);
        putInvoker(PbProtocol.BattleMountainMapResetResponse_13000546, this::mountainMapReset);
        putInvoker(PbProtocol.BattleMountainNextFlooResponse_13000548, this::mountainNextFloo);
        putInvoker(PbProtocol.BattlePVEVPDataResponse_1300054a, this::pVEVPData);
        putInvoker(PbProtocol.BattlePVEVPRecordResponse_13000551, this::pVEVPRecord);
        putInvoker(PbProtocol.BattlePVEVPChallengeResponse_13000553, this::pVEVPChallenge);
        putInvoker(PbProtocol.BattleBuyTicketResponse_13000555, this::buyTicket);
        putInvoker(PbProtocol.BattleEquipTowerRecordResponse_13000529, this::equipTowerRecord);
        putInvoker(PbProtocol.BattleMountainBuffBagResponse_1300053c, this::mountainBuffBag);
    }

    private void fieldStart(NetClient netClient, Object message) {
        BattleFieldStartResponse_13000002 resp = (BattleFieldStartResponse_13000002) message;
        PlayerBattleAttrs attrs = resp.getAttrs();
        Client client = (Client) netClient;
    }

    private void fieldEnd(NetClient netClient, Object message) {
        BattleFieldEndResponse_13000004 resp = (BattleFieldEndResponse_13000004) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void fieldQuickEnd(NetClient netClient, Object message) {
        BattleFieldQuickEndResponse_13000006 resp = (BattleFieldQuickEndResponse_13000006) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void share(NetClient netClient, Object message) {
        BattleShareResponse_13000008 resp = (BattleShareResponse_13000008) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void relive(NetClient netClient, Object message) {
        BattleReliveResponse_13000011 resp = (BattleReliveResponse_13000011) message;
        Client client = (Client) netClient;
    }

    private void rogueAdvertise(NetClient netClient, Object message) {
        BattleRogueAdvertiseResponse_13000013 resp = (BattleRogueAdvertiseResponse_13000013) message;
        Client client = (Client) netClient;
    }

    private void reward(NetClient netClient, Object message) {
        BattleRewardResponse_13000023 resp = (BattleRewardResponse_13000023) message;
        List<RewardInfo> rewardList = resp.getRewardList();
        Client client = (Client) netClient;
    }

    private void chapterReward(NetClient netClient, Object message) {
        BattleChapterRewardResponse_13000223 resp = (BattleChapterRewardResponse_13000223) message;
        List<RewardInfo> rewardList = resp.getRewardList();
        Client client = (Client) netClient;
    }

    private void hCReward(NetClient netClient, Object message) {
        HCBattleRewardResponse_13000028 resp = (HCBattleRewardResponse_13000028) message;
        List<RewardInfo> rewardList = resp.getRewardList();
        Client client = (Client) netClient;
    }

    private void sweep(NetClient netClient, Object message) {
        BattleSweepResponse_13000025 resp = (BattleSweepResponse_13000025) message;
        List<RewardInfo> rewardList = resp.getRewardList();
        Client client = (Client) netClient;
    }

    private void hCSweep(NetClient netClient, Object message) {
        HCBattleSweepResponse_13000041 resp = (HCBattleSweepResponse_13000041) message;
        List<RewardInfo> rewardList = resp.getRewardList();
        Client client = (Client) netClient;
    }

    private void patrolReward(NetClient netClient, Object message) {
        BattlePatrolRewardResponse_13000045 resp = (BattlePatrolRewardResponse_13000045) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        int exp = resp.getExp();
        int gold = resp.getGold();
        Client client = (Client) netClient;
    }

    private void lineup(NetClient netClient, Object message) {
        BattleLineupResponse_13000049 resp = (BattleLineupResponse_13000049) message;
        Client client = (Client) netClient;
    }

    private void lineupChoose(NetClient netClient, Object message) {
        BattleLineupChooseResponse_13000032 resp = (BattleLineupChooseResponse_13000032) message;
        Client client = (Client) netClient;
    }

    private void hCDataSave(NetClient netClient, Object message) {
        HCBattleDataSaveResponse_13000101 resp = (HCBattleDataSaveResponse_13000101) message;
        Client client = (Client) netClient;
    }

    private void hCData(NetClient netClient, Object message) {
        HCBattleDataResponse_13000103 resp = (HCBattleDataResponse_13000103) message;
        String data = resp.getData();
        Client client = (Client) netClient;
    }

    private void daoHeart(NetClient netClient, Object message) {
        BattleDaoHeartResponse_13000056 resp = (BattleDaoHeartResponse_13000056) message;
        int completedId = resp.getCompletedId();
        int nextId = resp.getNextId();
        List<Integer> randomBuffList = resp.getRandomBuffList();
        int freeSweepRemaning = resp.getFreeSweepRemaning();
        int paySweepRemaning = resp.getPaySweepRemaning();
        Client client = (Client) netClient;
    }

    private void daoHeartSweep(NetClient netClient, Object message) {
        BattleDaoHeartSweepResponse_13000061 resp = (BattleDaoHeartSweepResponse_13000061) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void daoHeartSweepBatch(NetClient netClient, Object message) {
        BattleDaoHeartSweepBatchResponse_13000063 resp = (BattleDaoHeartSweepBatchResponse_13000063) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void nightmareRealm(NetClient netClient, Object message) {
        BattleNightmareRealmResponse_13000081 resp = (BattleNightmareRealmResponse_13000081) message;
        List<LineupInfo> lineupsList = resp.getLineupsList();
        List<Integer> randomBuffList = resp.getRandomBuffList();
        int buffRefreshTimes = resp.getBuffRefreshTimes();
        List<Integer> quickRewardIdList = resp.getQuickRewardIdList();
        Map<Integer, Integer> buffIdsMap = resp.getBuffIdsMap();
        int startBattle = resp.getStartBattle();
        boolean canQuickReward = resp.getCanQuickReward();
        Client client = (Client) netClient;
    }

    private void nightmareRealmBuffUpdate(NetClient netClient, Object message) {
        BattleNightmareRealmBuffUpdateResponse_13000083 resp = (BattleNightmareRealmBuffUpdateResponse_13000083) message;
        Client client = (Client) netClient;
    }

    private void nightmareRealmQuick(NetClient netClient, Object message) {
        BattleNightmareRealmQuickResponse_13000085 resp = (BattleNightmareRealmQuickResponse_13000085) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void dayChallengeReceiveActivePoint(NetClient netClient, Object message) {
        BattleDayChallengeReceiveActivePointResponse_13000071 resp = (BattleDayChallengeReceiveActivePointResponse_13000071) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void spiritualInfo(NetClient netClient, Object message) {
        BattleSpiritualInfoResponse_13000091 resp = (BattleSpiritualInfoResponse_13000091) message;
        BattleSpiritualInfo battleSpiritualInfo = resp.getBattleSpiritualInfo();
        Client client = (Client) netClient;
    }

    private void spiritualReceiveActivePoint(NetClient netClient, Object message) {
        BattleSpiritualReceiveActivePointResponse_13000093 resp = (BattleSpiritualReceiveActivePointResponse_13000093) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void spiritualGetTimes(NetClient netClient, Object message) {
        BattleSpiritualGetTimesResponse_13000095 resp = (BattleSpiritualGetTimesResponse_13000095) message;
        Client client = (Client) netClient;
    }

    private void spiritualGetPoint(NetClient netClient, Object message) {
        BattleSpiritualGetPointResponse_13000097 resp = (BattleSpiritualGetPointResponse_13000097) message;
        Client client = (Client) netClient;
    }

    private void spiritualChange(NetClient netClient, Object message) {
        BattleSpiritualChangeBattleResponse_13000099 resp = (BattleSpiritualChangeBattleResponse_13000099) message;
        Client client = (Client) netClient;
    }

    private void lostInfo(NetClient netClient, Object message) {
        BattleLostInfoResponse_13000202 resp = (BattleLostInfoResponse_13000202) message;
        int battleId = resp.getBattleId();
        int battleStage = resp.getBattleStage();
        List<Integer> randomBuffList = resp.getRandomBuffList();
        int historyMaxBattle = resp.getHistoryMaxBattle();
        int historyMaxbattleStage = resp.getHistoryMaxbattleStage();
        boolean historyMaxBattleReward = resp.getHistoryMaxBattleReward();
        Client client = (Client) netClient;
    }

    private void lostDayReward(NetClient netClient, Object message) {
        BattleLostDayRewardResponse_13000204 resp = (BattleLostDayRewardResponse_13000204) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void worldBossInfo(NetClient netClient, Object message) {
        BattleWorldBossInfoResponse_13000302 resp = (BattleWorldBossInfoResponse_13000302) message;
        String cumulativeDamage = resp.getCumulativeDamage();
        String maxDamageToday = resp.getMaxDamageToday();
        int rank = resp.getRank();
        int battleTimes = resp.getBattleTimes();
        int buyTimes = resp.getBuyTimes();
        int rewardId = resp.getRewardId();
        Client client = (Client) netClient;
    }

    private void worldBossBuyTimes(NetClient netClient, Object message) {
        BattleWorldBossBuyTimesResponse_13000304 resp = (BattleWorldBossBuyTimesResponse_13000304) message;
        Client client = (Client) netClient;
    }

    private void worldReward(NetClient netClient, Object message) {
        BattleWorldRewardResponse_13000306 resp = (BattleWorldRewardResponse_13000306) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void rougeRefresh(NetClient netClient, Object message) {
        BattleRougeRefreshResponse_13000053 resp = (BattleRougeRefreshResponse_13000053) message;
        Client client = (Client) netClient;
    }

    private void stamina(NetClient netClient, Object message) {
        BattleStaminaResponse_13000051 resp = (BattleStaminaResponse_13000051) message;
        Client client = (Client) netClient;
    }

    private void rescueSkillId(NetClient netClient, Object message) {
        BattleRescueSkillIdResponse_13000058 resp = (BattleRescueSkillIdResponse_13000058) message;
        Client client = (Client) netClient;
    }

    private void pvPTargetList(NetClient netClient, Object message) {
        BattlePvPTargetListResponse_13000112 resp = (BattlePvPTargetListResponse_13000112) message;
        List<SimplePlayerInfo> targetListList = resp.getTargetListList();
        List<Integer> scoreListList = resp.getScoreListList();
        int freeRefreshNum = resp.getFreeRefreshNum();
        int refreshNum = resp.getRefreshNum();
        Client client = (Client) netClient;
    }

    private void pvPStart(NetClient netClient, Object message) {
        BattlePvPStartResponse_13000114 resp = (BattlePvPStartResponse_13000114) message;
        PlayerBattleAttrs selfAttrs = resp.getSelfAttrs();
        PlayerBattleAttrs targetAttrs = resp.getTargetAttrs();
        BattleLineupInfo targetLineupInfo = resp.getTargetLineupInfo();
        BattleSecretscriptInfo targetSecretscriptInfo = resp.getTargetSecretscriptInfo();
        Client client = (Client) netClient;
    }

    private void pvPEnd(NetClient netClient, Object message) {
        BattlePvPEndResponse_13000116 resp = (BattlePvPEndResponse_13000116) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        int selfScore = resp.getSelfScore();
        int targetScore = resp.getTargetScore();
        Client client = (Client) netClient;
    }

    private void pvPInfo(NetClient netClient, Object message) {
        BattlePvPInfoResponse_13000118 resp = (BattlePvPInfoResponse_13000118) message;
        int num = resp.getNum();
        int settlementDayTimer = resp.getSettlementDayTimer();
        int settlementSeasonTimer = resp.getSettlementSeasonTimer();
        int nextSeasonTimer = resp.getNextSeasonTimer();
        int buyNum = resp.getBuyNum();
        Map<Integer, Integer> secretscriptMapMap = resp.getSecretscriptMapMap();
        int freeRefreshNum = resp.getFreeRefreshNum();
        int refreshNum = resp.getRefreshNum();
        List<SimplePlayerInfo> targetListList = resp.getTargetListList();
        List<Integer> scoreListList = resp.getScoreListList();
        Client client = (Client) netClient;
    }

    private void buyPvPTime(NetClient netClient, Object message) {
        BattleBuyPvPTimeResponse_13000122 resp = (BattleBuyPvPTimeResponse_13000122) message;
        int buyNum = resp.getBuyNum();
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void xiangYaoChuMo(NetClient netClient, Object message) {
        BattleXiangYaoChuMoResponse_13000502 resp = (BattleXiangYaoChuMoResponse_13000502) message;
        int sweepTimes = resp.getSweepTimes();
        int lastCompleteBattleId = resp.getLastCompleteBattleId();
        Client client = (Client) netClient;
    }

    private void lingShan(NetClient netClient, Object message) {
        BattleLingShanResponse_13000512 resp = (BattleLingShanResponse_13000512) message;
        int battleTimes = resp.getBattleTimes();
        int lastCompleteBattleId = resp.getLastCompleteFloor();
        int payTimes = resp.getPayTimes();
        List<Integer> rewardBattleIdsList = resp.getRewardBattleIdsList();
        List<PlayerRankInfo> rankPlayersList = resp.getRankPlayersList();
        Client client = (Client) netClient;
    }

    private void lingShanBuyTimes(NetClient netClient, Object message) {
        BattleLingShanBuyTimesResponse_13000514 resp = (BattleLingShanBuyTimesResponse_13000514) message;
        Client client = (Client) netClient;
    }

    private void lingShanReward(NetClient netClient, Object message) {
        BattleLingShanRewardResponse_13000516 resp = (BattleLingShanRewardResponse_13000516) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void targetPvPListResponse(NetClient netClient, Object o) {
        BattleMsg.BattlePvPTargetListResponse_13000112 res = (BattleMsg.BattlePvPTargetListResponse_13000112) o;
        Client client = (Client) netClient;
        if (res.getScoreListCount() > 0) {
            client.setTargetListResponse(res);
        }
    }

    private void towerData(NetClient netClient, Object message) {
        BattleTowerDataResponse_13000522 resp = (BattleTowerDataResponse_13000522) message;
        int rewardCount = resp.getRewardCount();
        int floorCount = resp.getFloorCount();
        Client client = (Client) netClient;
    }

    private void towerDataPush(NetClient netClient, Object message) {
        BattleTowerDataPush_13100523 resp = (BattleTowerDataPush_13100523) message;
        int rewardCount = resp.getRewardCount();
        int floorCount = resp.getFloorCount();
        int radomBuff = resp.getRadomBuff();
        Map<Integer, Integer> curFloorMap = resp.getCurFloorMap();
        Client client = (Client) netClient;
    }

    private void towerQuickEnd(NetClient netClient, Object message) {
        BattleTowerQuickEndResponse_13100525 resp = (BattleTowerQuickEndResponse_13100525) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        int rewardCount = resp.getRewardCount();
        Client client = (Client) netClient;
    }

    private void equipTowerHelpPlayer(NetClient netClient, Object message) {
        BattleEquipTowerHelpPlayerResponse_13000532 resp = (BattleEquipTowerHelpPlayerResponse_13000532) message;
        List<SimplePlayerInfo> helpPlayerListList = resp.getHelpPlayerListList();
        Client client = (Client) netClient;
    }

    private void equipTowerGetTicket(NetClient netClient, Object message) {
        BattleEquipTowerGetTicketResponse_13000534 resp = (BattleEquipTowerGetTicketResponse_13000534) message;
        int ticketCount = resp.getTicketCount();
        long nextTicketTime = resp.getNextTicketTime();
        RewardInfo reward = resp.getReward();
        Client client = (Client) netClient;
    }

    private void equipTowerGetHelpReward(NetClient netClient, Object message) {
        BattleEquipTowerGetHelpRewardResponse_13000536 resp = (BattleEquipTowerGetHelpRewardResponse_13000536) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void equipTowerFindHelpReward(NetClient netClient, Object message) {
        BattleEquipTowerFindHelpRewardResponse_13000538 resp = (BattleEquipTowerFindHelpRewardResponse_13000538) message;
        List<EquipTowerHelpRewardInfo> helpRewardList = resp.getHelpRewardList();
        Client client = (Client) netClient;
    }

    private void equipTowerData(NetClient netClient, Object message) {
        BattleEquipTowerDataResponse_13000527 resp = (BattleEquipTowerDataResponse_13000527) message;
        int ticketCount = resp.getTicketCount();
        long nextTicketTime = resp.getNextTicketTime();
        int curFloor = resp.getCurFloor();
        Client client = (Client) netClient;
    }

    private void lingShanFloorSkip(NetClient netClient, Object message) {
        BattleLingShanFloorSkipResponse_13000518 resp = (BattleLingShanFloorSkipResponse_13000518) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void lingShanEnd(NetClient netClient, Object message) {
        BattleLingShanEndResponse_1300051a resp = (BattleLingShanEndResponse_1300051a) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void mountainData(NetClient netClient, Object message) {
        BattleMountainDataResponse_1300053a resp = (BattleMountainDataResponse_1300053a) message;
        List<MountainMapNodeData> mapdataList = resp.getMapdataList();
        int curNodeId = resp.getCurNodeId();
        int score = resp.getScore();
        int endTime = resp.getEndTime();
        int hp = resp.getHp();
        Map<Integer, Integer> scoreRewardMap = resp.getScoreRewardMap();
        int scoreMax = resp.getScoreMax();
        int refreshNum = resp.getRefreshNum();
        Client client = (Client) netClient;
    }

    private void mountainFinishNode(NetClient netClient, Object message) {
        BattleMountainFinishNodeResponse_13000542 resp = (BattleMountainFinishNodeResponse_13000542) message;
        int score = resp.getScore();
        int hp = resp.getHp();
        int scoreMax = resp.getScoreMax();
        Client client = (Client) netClient;
    }

    private void mountainGetReward(NetClient netClient, Object message) {
    }

    private void mountainMapReset(NetClient netClient, Object message) {
        BattleMountainMapResetResponse_13000546 resp = (BattleMountainMapResetResponse_13000546) message;
        List<MountainMapNodeData> mapdataList = resp.getMapdataList();
        int curNodeId = resp.getCurNodeId();
        int score = resp.getScore();
        int hp = resp.getHp();
        int refreshNum = resp.getRefreshNum();
        List<Integer> buffBagList = resp.getBuffBagList();
        Client client = (Client) netClient;
    }

    private void mountainNextFloo(NetClient netClient, Object message) {
        BattleMountainNextFlooResponse_13000548 resp = (BattleMountainNextFlooResponse_13000548) message;
        int curNodeId = resp.getCurNodeId();
        Client client = (Client) netClient;
    }

    private void pVEVPData(NetClient netClient, Object message) {
        BattlePVEVPDataResponse_1300054a resp = (BattlePVEVPDataResponse_1300054a) message;
        int challengeCount = resp.getChallengeCount();
        int endTime = resp.getEndTime();
        Client client = (Client) netClient;
    }

    private void pVEVPRecord(NetClient netClient, Object message) {
        BattlePVEVPRecordResponse_13000551 resp = (BattlePVEVPRecordResponse_13000551) message;
        List<PVEVPRecordData> recordListList = resp.getRecordListList();
        Client client = (Client) netClient;
    }

    private void pVEVPChallenge(NetClient netClient, Object message) {
        BattlePVEVPChallengeResponse_13000553 resp = (BattlePVEVPChallengeResponse_13000553) message;
        Client client = (Client) netClient;
    }

    private void buyTicket(NetClient netClient, Object message) {
        BattleBuyTicketResponse_13000555 resp = (BattleBuyTicketResponse_13000555) message;
        int buyCount = resp.getBuyCount();
        Client client = (Client) netClient;
    }

    private void equipTowerRecord(NetClient netClient, Object message) {
        BattleEquipTowerRecordResponse_13000529 resp = (BattleEquipTowerRecordResponse_13000529) message;
        Client client = (Client) netClient;
    }

    private void mountainBuffBag(NetClient netClient, Object message) {
        BattleMountainBuffBagResponse_1300053c resp = (BattleMountainBuffBagResponse_1300053c) message;
        List<Integer> buffBagList = resp.getBuffBagList();
        Client client = (Client) netClient;
    }
}
