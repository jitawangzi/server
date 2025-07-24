package cn.game.simulation.client.handler;

import java.util.List;
import org.springframework.stereotype.Component;
import cn.game.core.net.client.NetClient;
import cn.game.games.net.game.handler.GameBaseHandler;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.protobuf.BaseMsg.SimplePlayerInfo;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenActiveRewardResponse_40000046;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenBargainBuyResponse_40000063;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenBargainResponse_40000061;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenBountyAcceptResponse_40000071;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenBountyBattleEndResponse_40000079;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenBountyBattleReportAttacker;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenBountyBattleReportResponse_4000007b;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenBountyBattleStartResponse_40000077;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenBountyMissionProto;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenBountyPlayerProto;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenBountyPlayerResponse_4000007d;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenBountyProto;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenBountyTargetRefreshResponse_40000075;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenBuyShopResponse_40000048;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenGetMyApplyZongMenIdListResponse_40000056;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenGmTestResponse_40000054;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenInfoProto;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenLogProto;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenQuickJoinResponse_40000066;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenRankInfo;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenRankListResponse_40000081;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenRankWorshipResponse_40000083;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenShopProto;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenSimpleInfoProto;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenTaskProto;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenUpdateMemberFightPowerResponse_40000052;
import cn.game.protocol.protobuf.ZongMenMsg.applyJoinZongMenResponse_40000008;
import cn.game.protocol.protobuf.ZongMenMsg.createZongMenResponse_40000006;
import cn.game.protocol.protobuf.ZongMenMsg.dissolveZongMenResponse_40000012;
import cn.game.protocol.protobuf.ZongMenMsg.findZongMenResponse_40000004;
import cn.game.protocol.protobuf.ZongMenMsg.getZongMenActivityResponse_40000034;
import cn.game.protocol.protobuf.ZongMenMsg.getZongMenInfoResponse_40000022;
import cn.game.protocol.protobuf.ZongMenMsg.getZongMenListResponse_40000002;
import cn.game.protocol.protobuf.ZongMenMsg.getZongMenLogResponse_40000026;
import cn.game.protocol.protobuf.ZongMenMsg.getZongMenShopResponse_40000028;
import cn.game.protocol.protobuf.ZongMenMsg.getZongMenTaskResponse_40000032;
import cn.game.protocol.protobuf.ZongMenMsg.quitZongMenResponse_40000018;
import cn.game.protocol.protobuf.ZongMenMsg.setZongMenMemberPositionResponse_40000016;
import cn.game.protocol.protobuf.ZongMenMsg.setZongMenSettingResponse_40000014;
import cn.game.protocol.protobuf.ZongMenMsg.updateMemberAuthResponse_40000042;
import cn.game.protocol.protobuf.ZongMenMsg.updateZongMenAssetResponse_40000038;
import cn.game.simulation.client.Client;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenBountyRewardResponse_40000073;

@Component
public class ClientZongMenHandler extends GameBaseHandler {

    @Override
    protected int getModule() {
        return 0x40;
    }

    @Override
    protected InitialUI getInitialUI() {
        return InitialUI.Guild;
    }

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.getZongMenListResponse_40000002, this::getList);
        putInvoker(PbProtocol.findZongMenResponse_40000004, this::find);
        putInvoker(PbProtocol.createZongMenResponse_40000006, this::create);
        putInvoker(PbProtocol.applyJoinZongMenResponse_40000008, this::applyJoin);
        putInvoker(PbProtocol.dissolveZongMenResponse_40000012, this::dissolve);
        putInvoker(PbProtocol.setZongMenSettingResponse_40000014, this::setSetting);
        putInvoker(PbProtocol.setZongMenMemberPositionResponse_40000016, this::setMemberPosition);
        putInvoker(PbProtocol.quitZongMenResponse_40000018, this::quit);
        putInvoker(PbProtocol.getZongMenInfoResponse_40000022, this::getInfo);
        putInvoker(PbProtocol.getZongMenLogResponse_40000026, this::getLog);
        putInvoker(PbProtocol.getZongMenShopResponse_40000028, this::getShop);
        putInvoker(PbProtocol.getZongMenTaskResponse_40000032, this::getTask);
        putInvoker(PbProtocol.getZongMenActivityResponse_40000034, this::getActivity);
        putInvoker(PbProtocol.updateZongMenAssetResponse_40000038, this::updateAsset);
        putInvoker(PbProtocol.updateMemberAuthResponse_40000042, this::updateMemberAuth);
        putInvoker(PbProtocol.ZongMenActiveRewardResponse_40000046, this::activeReward);
        putInvoker(PbProtocol.ZongMenBuyShopResponse_40000048, this::buyShop);
        putInvoker(PbProtocol.ZongMenBargainResponse_40000061, this::bargain);
        putInvoker(PbProtocol.ZongMenBargainBuyResponse_40000063, this::bargainBuy);
        putInvoker(PbProtocol.ZongMenBountyTargetRefreshResponse_40000075, this::bountyTargetRefresh);
        putInvoker(PbProtocol.ZongMenBountyBattleStartResponse_40000077, this::bountyBattleStart);
        putInvoker(PbProtocol.ZongMenBountyBattleEndResponse_40000079, this::bountyBattleEnd);
        putInvoker(PbProtocol.ZongMenBountyBattleReportResponse_4000007b, this::bountyBattleReport);
        putInvoker(PbProtocol.ZongMenBountyPlayerResponse_4000007d, this::bountyPlayer);
        putInvoker(PbProtocol.ZongMenRankListResponse_40000081, this::rankList);
        putInvoker(PbProtocol.ZongMenRankWorshipResponse_40000083, this::rankWorship);
        putInvoker(PbProtocol.ZongMenUpdateMemberFightPowerResponse_40000052, this::updateMemberFightPower);
        putInvoker(PbProtocol.ZongMenGmTestResponse_40000054, this::gmTest);
        putInvoker(PbProtocol.ZongMenGetMyApplyZongMenIdListResponse_40000056, this::getMyApplyIdList);
        putInvoker(PbProtocol.ZongMenQuickJoinResponse_40000066, this::quickJoin);
        putInvoker(PbProtocol.ZongMenBountyAcceptResponse_40000071, this::bountyAccept);
        putInvoker(PbProtocol.ZongMenBountyRewardResponse_40000073, this::bountyReward);
    }

    private void getList(NetClient netClient, Object message) {
        getZongMenListResponse_40000002 resp = (getZongMenListResponse_40000002) message;
        List<ZongMenSimpleInfoProto> zongMenListList = resp.getZongMenListList();
        int total = resp.getTotal();
        int page = resp.getPage();
        Client client = (Client) netClient;
    }

    private void find(NetClient netClient, Object message) {
        findZongMenResponse_40000004 resp = (findZongMenResponse_40000004) message;
        ZongMenInfoProto zongMen = resp.getZongMen();
        Client client = (Client) netClient;
    }

    private void create(NetClient netClient, Object message) {
        createZongMenResponse_40000006 resp = (createZongMenResponse_40000006) message;
        ZongMenInfoProto zongMen = resp.getZongMen();
        Client client = (Client) netClient;
    }

    private void applyJoin(NetClient netClient, Object message) {
        applyJoinZongMenResponse_40000008 resp = (applyJoinZongMenResponse_40000008) message;
        ZongMenInfoProto zongMen = resp.getZongMen();
        Client client = (Client) netClient;
    }

    private void dissolve(NetClient netClient, Object message) {
        dissolveZongMenResponse_40000012 resp = (dissolveZongMenResponse_40000012) message;
        boolean result = resp.getResult();
        Client client = (Client) netClient;
    }

    private void setSetting(NetClient netClient, Object message) {
        setZongMenSettingResponse_40000014 resp = (setZongMenSettingResponse_40000014) message;
        boolean result = resp.getResult();
        Client client = (Client) netClient;
    }

    private void setMemberPosition(NetClient netClient, Object message) {
        setZongMenMemberPositionResponse_40000016 resp = (setZongMenMemberPositionResponse_40000016) message;
        boolean result = resp.getResult();
        int position = resp.getPosition();
        int targetPid = resp.getTargetPid();
        Client client = (Client) netClient;
    }

    private void quit(NetClient netClient, Object message) {
        quitZongMenResponse_40000018 resp = (quitZongMenResponse_40000018) message;
        boolean result = resp.getResult();
        Client client = (Client) netClient;
    }

    private void getInfo(NetClient netClient, Object message) {
        getZongMenInfoResponse_40000022 resp = (getZongMenInfoResponse_40000022) message;
        ZongMenInfoProto info = resp.getInfo();
        Client client = (Client) netClient;
    }

    private void getLog(NetClient netClient, Object message) {
        getZongMenLogResponse_40000026 resp = (getZongMenLogResponse_40000026) message;
        List<ZongMenLogProto> logListList = resp.getLogListList();
        Client client = (Client) netClient;
    }

    private void getShop(NetClient netClient, Object message) {
        getZongMenShopResponse_40000028 resp = (getZongMenShopResponse_40000028) message;
        ZongMenShopProto shopList = resp.getShopList();
        Client client = (Client) netClient;
    }

    private void getTask(NetClient netClient, Object message) {
        getZongMenTaskResponse_40000032 resp = (getZongMenTaskResponse_40000032) message;
        ZongMenTaskProto taskList = resp.getTaskList();
        Client client = (Client) netClient;
    }

    private void getActivity(NetClient netClient, Object message) {
        getZongMenActivityResponse_40000034 resp = (getZongMenActivityResponse_40000034) message;
        Client client = (Client) netClient;
    }

    private void updateAsset(NetClient netClient, Object message) {
        updateZongMenAssetResponse_40000038 resp = (updateZongMenAssetResponse_40000038) message;
        Client client = (Client) netClient;
    }

    private void updateMemberAuth(NetClient netClient, Object message) {
        updateMemberAuthResponse_40000042 resp = (updateMemberAuthResponse_40000042) message;
        boolean result = resp.getResult();
        Client client = (Client) netClient;
    }

    private void activeReward(NetClient netClient, Object message) {
        ZongMenActiveRewardResponse_40000046 resp = (ZongMenActiveRewardResponse_40000046) message;
        List<Integer> rewardIndexListList = resp.getRewardIndexListList();
        List<RewardInfo> dropsList = resp.getDropsList();
        Client client = (Client) netClient;
    }

    private void buyShop(NetClient netClient, Object message) {
        ZongMenBuyShopResponse_40000048 resp = (ZongMenBuyShopResponse_40000048) message;
        List<RewardInfo> dropsList = resp.getDropsList();
        Client client = (Client) netClient;
    }

    private void bargain(NetClient netClient, Object message) {
        ZongMenBargainResponse_40000061 resp = (ZongMenBargainResponse_40000061) message;
        int count = resp.getCount();
        Client client = (Client) netClient;
    }

    private void bargainBuy(NetClient netClient, Object message) {
        ZongMenBargainBuyResponse_40000063 resp = (ZongMenBargainBuyResponse_40000063) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void bountyTargetRefresh(NetClient netClient, Object message) {
        ZongMenBountyTargetRefreshResponse_40000075 resp = (ZongMenBountyTargetRefreshResponse_40000075) message;
        List<ZongMenBountyPlayerProto> playersList = resp.getPlayersList();
        Client client = (Client) netClient;
    }

    private void bountyBattleStart(NetClient netClient, Object message) {
        ZongMenBountyBattleStartResponse_40000077 resp = (ZongMenBountyBattleStartResponse_40000077) message;
        Client client = (Client) netClient;
    }

    private void bountyBattleEnd(NetClient netClient, Object message) {
        ZongMenBountyBattleEndResponse_40000079 resp = (ZongMenBountyBattleEndResponse_40000079) message;
        Client client = (Client) netClient;
    }

    private void bountyBattleReport(NetClient netClient, Object message) {
        ZongMenBountyBattleReportResponse_4000007b resp = (ZongMenBountyBattleReportResponse_4000007b) message;
        List<ZongMenBountyBattleReportAttacker> reportsList = resp.getReportsList();
        Client client = (Client) netClient;
    }

    private void bountyPlayer(NetClient netClient, Object message) {
        ZongMenBountyPlayerResponse_4000007d resp = (ZongMenBountyPlayerResponse_4000007d) message;
        SimplePlayerInfo player = resp.getPlayer();
        ZongMenBountyMissionProto mission = resp.getMission();
        Client client = (Client) netClient;
    }

    private void rankList(NetClient netClient, Object message) {
        ZongMenRankListResponse_40000081 resp = (ZongMenRankListResponse_40000081) message;
        int rank = resp.getRank();
        String score = resp.getScore();
        List<ZongMenRankInfo> playersList = resp.getPlayersList();
        List<Integer> extList = resp.getExtList();
        Client client = (Client) netClient;
    }

    private void rankWorship(NetClient netClient, Object message) {
        ZongMenRankWorshipResponse_40000083 resp = (ZongMenRankWorshipResponse_40000083) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void updateMemberFightPower(NetClient netClient, Object message) {
        ZongMenUpdateMemberFightPowerResponse_40000052 resp = (ZongMenUpdateMemberFightPowerResponse_40000052) message;
        boolean result = resp.getResult();
        Client client = (Client) netClient;
    }

    private void gmTest(NetClient netClient, Object message) {
        ZongMenGmTestResponse_40000054 resp = (ZongMenGmTestResponse_40000054) message;
        boolean result = resp.getResult();
        Client client = (Client) netClient;
    }

    private void getMyApplyIdList(NetClient netClient, Object message) {
        ZongMenGetMyApplyZongMenIdListResponse_40000056 resp = (ZongMenGetMyApplyZongMenIdListResponse_40000056) message;
        List<Integer> zongMenIdListList = resp.getZongMenIdListList();
        Client client = (Client) netClient;
    }

    private void quickJoin(NetClient netClient, Object message) {
        ZongMenQuickJoinResponse_40000066 resp = (ZongMenQuickJoinResponse_40000066) message;
        ZongMenInfoProto info = resp.getInfo();
        Client client = (Client) netClient;
    }

    private void bountyAccept(NetClient netClient, Object message) {
        ZongMenBountyAcceptResponse_40000071 resp = (ZongMenBountyAcceptResponse_40000071) message;
        Client client = (Client) netClient;
    }

    private void bountyReward(NetClient netClient, Object message) {
        ZongMenBountyRewardResponse_40000073 resp = (ZongMenBountyRewardResponse_40000073) message;
        int event = resp.getEvent();
        List<RewardInfo> eventRewardsList = resp.getEventRewardsList();
        List<RewardInfo> bountyRewardsList = resp.getBountyRewardsList();
        ZongMenBountyProto bounty = resp.getBounty();
        Client client = (Client) netClient;
    }
}
