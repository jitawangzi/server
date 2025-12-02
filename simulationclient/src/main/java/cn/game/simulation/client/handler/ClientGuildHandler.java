package cn.game.simulation.client.handler;

import java.util.List;
import org.springframework.stereotype.Component;
import cn.game.core.net.client.NetClient;
import cn.game.games.net.game.handler.GameBaseHandler;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.protobuf.BaseMsg.SimplePlayerInfo;
import cn.game.protocol.protobuf.GuildMsg.GuildAllInfo;
import cn.game.protocol.protobuf.GuildMsg.GuildApplyJoinResponse_40000008;
import cn.game.protocol.protobuf.GuildMsg.GuildBargainBuyResponse_40000063;
import cn.game.protocol.protobuf.GuildMsg.GuildBargainResponse_40000061;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyAcceptResponse_40000071;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyBattleEndResponse_40000079;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyBattleReportAttacker;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyBattleReportResponse_4000007b;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyBattleStartResponse_40000077;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyMissionProto;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyPlayerProto;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyPlayerResponse_4000007d;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyProto;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyRewardResponse_40000073;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyTargetRefreshResponse_40000075;
import cn.game.protocol.protobuf.GuildMsg.GuildCreateResponse_40000006;
import cn.game.protocol.protobuf.GuildMsg.GuildDissolveResponse_40000012;
import cn.game.protocol.protobuf.GuildMsg.GuildDonateResponse_40000068;
import cn.game.protocol.protobuf.GuildMsg.GuildFindResponse_40000004;
import cn.game.protocol.protobuf.GuildMsg.GuildGVECardData;
import cn.game.protocol.protobuf.GuildMsg.GuildInfoResponse_40000022;
import cn.game.protocol.protobuf.GuildMsg.GuildJoinPush_40000044;
import cn.game.protocol.protobuf.GuildMsg.GuildListResponse_40000002;
import cn.game.protocol.protobuf.GuildMsg.GuildLogProto;
import cn.game.protocol.protobuf.GuildMsg.GuildLogResponse_40000026;
import cn.game.protocol.protobuf.GuildMsg.GuildMemberAuthResponse_40000042;
import cn.game.protocol.protobuf.GuildMsg.GuildMemberInfo;
import cn.game.protocol.protobuf.GuildMsg.GuildMemberPositionSetResponse_40000016;
import cn.game.protocol.protobuf.GuildMsg.GuildQuickJoinResponse_40000066;
import cn.game.protocol.protobuf.GuildMsg.GuildQuitPush_40000024;
import cn.game.protocol.protobuf.GuildMsg.GuildQuitResponse_40000018;
import cn.game.protocol.protobuf.GuildMsg.GuildRankList;
import cn.game.protocol.protobuf.GuildMsg.GuildRankListResponse_40000082;
import cn.game.protocol.protobuf.GuildMsg.GuildSettingResponse_40000014;
import cn.game.protocol.protobuf.GuildMsg.GuildShowInfo;
import cn.game.protocol.protobuf.GuildMsg.GuildSimpleInfo;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.simulation.client.Client;
import cn.game.protocol.protobuf.GuildMsg.GuildApplyProcessedPush_40100001;
import cn.game.protocol.protobuf.GuildMsg.GuildGVECardDataResponse_40000084;
import cn.game.protocol.protobuf.GuildMsg.GuildGVEOpenCardResponse_40000086;
import cn.game.protocol.protobuf.GuildMsg.GuildGVEOpenMapResponse_40000088;
import cn.game.protocol.protobuf.GuildMsg.GuildGVEMapBattleEndPush_40000089;
import cn.game.protocol.protobuf.GuildMsg.GuildGVEMapBattleStartPush_4000008a;
import cn.game.protocol.protobuf.GuildMsg.GuildGVEBuyTicketResponse_40000091;

@Component
public class ClientGuildHandler extends GameBaseHandler {

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
        putInvoker(PbProtocol.GuildListResponse_40000002, this::getList);
        putInvoker(PbProtocol.GuildFindResponse_40000004, this::find);
        putInvoker(PbProtocol.GuildCreateResponse_40000006, this::create);
        putInvoker(PbProtocol.GuildApplyJoinResponse_40000008, this::applyJoin);
        putInvoker(PbProtocol.GuildDissolveResponse_40000012, this::dissolve);
        putInvoker(PbProtocol.GuildSettingResponse_40000014, this::setSetting);
        putInvoker(PbProtocol.GuildMemberPositionSetResponse_40000016, this::setMemberPosition);
        putInvoker(PbProtocol.GuildQuitResponse_40000018, this::quit);
        putInvoker(PbProtocol.GuildInfoResponse_40000022, this::getInfo);
        putInvoker(PbProtocol.GuildLogResponse_40000026, this::getLog);
        putInvoker(PbProtocol.GuildMemberAuthResponse_40000042, this::updateMemberAuth);
        putInvoker(PbProtocol.GuildBargainResponse_40000061, this::bargain);
        putInvoker(PbProtocol.GuildBargainBuyResponse_40000063, this::bargainBuy);
        putInvoker(PbProtocol.GuildQuickJoinResponse_40000066, this::quickJoin);
        putInvoker(PbProtocol.GuildBountyAcceptResponse_40000071, this::bountyAccept);
        putInvoker(PbProtocol.GuildBountyRewardResponse_40000073, this::bountyReward);
        putInvoker(PbProtocol.GuildBountyTargetRefreshResponse_40000075, this::bountyTargetRefresh);
        putInvoker(PbProtocol.GuildBountyBattleStartResponse_40000077, this::bountyBattleStart);
        putInvoker(PbProtocol.GuildBountyBattleEndResponse_40000079, this::bountyBattleEnd);
        putInvoker(PbProtocol.GuildBountyBattleReportResponse_4000007b, this::bountyBattleReport);
        putInvoker(PbProtocol.GuildBountyPlayerResponse_4000007d, this::bountyPlayer);
        putInvoker(PbProtocol.GuildDonateResponse_40000068, this::donate);
        putInvoker(PbProtocol.GuildQuitPush_40000024, this::quitPush);
        putInvoker(PbProtocol.GuildJoinPush_40000044, this::joinPush);
        putInvoker(PbProtocol.GuildRankListResponse_40000082, this::rankList);
        putInvoker(PbProtocol.GuildApplyProcessedPush_40100001, this::applyProcessedPush);
        putInvoker(PbProtocol.GuildGVECardDataResponse_40000084, this::gVECardData);
        putInvoker(PbProtocol.GuildGVEOpenCardResponse_40000086, this::gVEOpenCard);
        putInvoker(PbProtocol.GuildGVEOpenMapResponse_40000088, this::gVEOpenMap);
        putInvoker(PbProtocol.GuildGVEMapBattleEndPush_40000089, this::gVEMapBattleEndPush);
        putInvoker(PbProtocol.GuildGVEMapBattleStartPush_4000008a, this::gVEMapBattleStartPush);
        putInvoker(PbProtocol.GuildGVEBuyTicketResponse_40000091, this::gVEBuyTicket);
    }

    private void getList(NetClient netClient, Object message) {
        GuildListResponse_40000002 resp = (GuildListResponse_40000002) message;
        List<GuildSimpleInfo> guildListList = resp.getGuildListList();
        int total = resp.getTotal();
        int page = resp.getPage();
        Client client = (Client) netClient;
        client.guildIds = guildListList.stream().map(GuildSimpleInfo::getId).toList();
    }

    private void find(NetClient netClient, Object message) {
        GuildFindResponse_40000004 resp = (GuildFindResponse_40000004) message;
        GuildShowInfo guild = resp.getGuild();
        Client client = (Client) netClient;
    }

    private void create(NetClient netClient, Object message) {
        GuildCreateResponse_40000006 resp = (GuildCreateResponse_40000006) message;
        Client client = (Client) netClient;
    }

    private void applyJoin(NetClient netClient, Object message) {
        GuildApplyJoinResponse_40000008 resp = (GuildApplyJoinResponse_40000008) message;
        GuildAllInfo guild = resp.getGuild();
        Client client = (Client) netClient;
    }

    private void dissolve(NetClient netClient, Object message) {
        GuildDissolveResponse_40000012 resp = (GuildDissolveResponse_40000012) message;
        boolean result = resp.getResult();
        Client client = (Client) netClient;
    }

    private void setSetting(NetClient netClient, Object message) {
        GuildSettingResponse_40000014 resp = (GuildSettingResponse_40000014) message;
        boolean result = resp.getResult();
        Client client = (Client) netClient;
    }

    private void setMemberPosition(NetClient netClient, Object message) {
        GuildMemberPositionSetResponse_40000016 resp = (GuildMemberPositionSetResponse_40000016) message;
        boolean result = resp.getResult();
        int position = resp.getPosition();
        int targetPid = resp.getTargetPid();
        Client client = (Client) netClient;
    }

    private void quit(NetClient netClient, Object message) {
        GuildQuitResponse_40000018 resp = (GuildQuitResponse_40000018) message;
        boolean result = resp.getResult();
        Client client = (Client) netClient;
    }

    private void getInfo(NetClient netClient, Object message) {
        GuildInfoResponse_40000022 resp = (GuildInfoResponse_40000022) message;
        GuildAllInfo info = resp.getInfo();
        Client client = (Client) netClient;
        if (info != null) {
            client.guildAllInfo = info;
            client.guildPersonalInfo = info.getPersonalInfo();
            List<GuildMemberInfo> membersList = info.getShowInfo().getMembersList();
            for (GuildMemberInfo guildMemberInfo : membersList) {
                if (Long.parseLong(guildMemberInfo.getSimplePlayer().getId()) == client.getPlayerId()) {
                    client.guildMember = guildMemberInfo;
                    break;
                }
            }
        }
    }

    private void getLog(NetClient netClient, Object message) {
        GuildLogResponse_40000026 resp = (GuildLogResponse_40000026) message;
        List<GuildLogProto> logListList = resp.getLogListList();
        Client client = (Client) netClient;
    }

    private void updateMemberAuth(NetClient netClient, Object message) {
        GuildMemberAuthResponse_40000042 resp = (GuildMemberAuthResponse_40000042) message;
        boolean result = resp.getResult();
        Client client = (Client) netClient;
    }

    private void bargain(NetClient netClient, Object message) {
        GuildBargainResponse_40000061 resp = (GuildBargainResponse_40000061) message;
        int count = resp.getCount();
        Client client = (Client) netClient;
    }

    private void bargainBuy(NetClient netClient, Object message) {
        GuildBargainBuyResponse_40000063 resp = (GuildBargainBuyResponse_40000063) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void quickJoin(NetClient netClient, Object message) {
        GuildQuickJoinResponse_40000066 resp = (GuildQuickJoinResponse_40000066) message;
        GuildAllInfo info = resp.getGuild();
        Client client = (Client) netClient;
        if (info != null) {
            client.guildAllInfo = info;
            client.guildPersonalInfo = info.getPersonalInfo();
            List<GuildMemberInfo> membersList = info.getShowInfo().getMembersList();
            for (GuildMemberInfo guildMemberInfo : membersList) {
                if (Long.parseLong(guildMemberInfo.getSimplePlayer().getId()) == client.getPlayerId()) {
                    client.guildMember = guildMemberInfo;
                    break;
                }
            }
        }
    }

    private void bountyAccept(NetClient netClient, Object message) {
        GuildBountyAcceptResponse_40000071 resp = (GuildBountyAcceptResponse_40000071) message;
        Client client = (Client) netClient;
    }

    private void bountyReward(NetClient netClient, Object message) {
        GuildBountyRewardResponse_40000073 resp = (GuildBountyRewardResponse_40000073) message;
        int event = resp.getEvent();
        List<RewardInfo> eventRewardsList = resp.getEventRewardsList();
        List<RewardInfo> bountyRewardsList = resp.getBountyRewardsList();
        GuildBountyProto bounty = resp.getBounty();
        Client client = (Client) netClient;
    }

    private void bountyTargetRefresh(NetClient netClient, Object message) {
        GuildBountyTargetRefreshResponse_40000075 resp = (GuildBountyTargetRefreshResponse_40000075) message;
        List<GuildBountyPlayerProto> playersList = resp.getPlayersList();
        Client client = (Client) netClient;
    }

    private void bountyBattleStart(NetClient netClient, Object message) {
        GuildBountyBattleStartResponse_40000077 resp = (GuildBountyBattleStartResponse_40000077) message;
        Client client = (Client) netClient;
    }

    private void bountyBattleEnd(NetClient netClient, Object message) {
        GuildBountyBattleEndResponse_40000079 resp = (GuildBountyBattleEndResponse_40000079) message;
        Client client = (Client) netClient;
    }

    private void bountyBattleReport(NetClient netClient, Object message) {
        GuildBountyBattleReportResponse_4000007b resp = (GuildBountyBattleReportResponse_4000007b) message;
        List<GuildBountyBattleReportAttacker> reportsList = resp.getReportsList();
        Client client = (Client) netClient;
    }

    private void bountyPlayer(NetClient netClient, Object message) {
        GuildBountyPlayerResponse_4000007d resp = (GuildBountyPlayerResponse_4000007d) message;
        SimplePlayerInfo player = resp.getPlayer();
        GuildBountyMissionProto mission = resp.getMission();
        Client client = (Client) netClient;
    }

    private void donate(NetClient netClient, Object message) {
        GuildDonateResponse_40000068 resp = (GuildDonateResponse_40000068) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void quitPush(NetClient netClient, Object message) {
        GuildQuitPush_40000024 resp = (GuildQuitPush_40000024) message;
        Client client = (Client) netClient;
    }

    private void joinPush(NetClient netClient, Object message) {
        GuildJoinPush_40000044 resp = (GuildJoinPush_40000044) message;
        List<Integer> targetPidsList = resp.getTargetPidsList();
        GuildSimpleInfo guild = resp.getGuild();
        Client client = (Client) netClient;
    }

    private void rankList(NetClient netClient, Object message) {
        GuildRankListResponse_40000082 resp = (GuildRankListResponse_40000082) message;
        GuildRankList rankList = resp.getRankList();
        Client client = (Client) netClient;
    }

    private void applyProcessedPush(NetClient netClient, Object message) {
        GuildApplyProcessedPush_40100001 resp = (GuildApplyProcessedPush_40100001) message;
        long guildId = resp.getGuildId();
        long playerId = resp.getPlayerId();
        Client client = (Client) netClient;
    }

    private void gVECardData(NetClient netClient, Object message) {
        GuildGVECardDataResponse_40000084 resp = (GuildGVECardDataResponse_40000084) message;
        List<GuildGVECardData> card1List = resp.getCard1List();
        List<GuildGVECardData> card10List = resp.getCard10List();
        List<GuildGVECardData> card100List = resp.getCard100List();
        int myCount1 = resp.getMyCount1();
        int myCount10 = resp.getMyCount10();
        int myCount100 = resp.getMyCount100();
        Client client = (Client) netClient;
    }

    private void gVEOpenCard(NetClient netClient, Object message) {
        GuildGVEOpenCardResponse_40000086 resp = (GuildGVEOpenCardResponse_40000086) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        GuildGVECardData cardData = resp.getCardData();
        int result = resp.getResult();
        Client client = (Client) netClient;
    }

    private void gVEOpenMap(NetClient netClient, Object message) {
        GuildGVEOpenMapResponse_40000088 resp = (GuildGVEOpenMapResponse_40000088) message;
        GuildGVEMap mapdata = resp.getMapdata();
        Client client = (Client) netClient;
    }

    private void gVEMapBattleEndPush(NetClient netClient, Object message) {
        GuildGVEMapBattleEndPush_40000089 resp = (GuildGVEMapBattleEndPush_40000089) message;
        GuildGVEBattleEnd battleEndData = resp.getBattleEndData();
        Client client = (Client) netClient;
    }

    private void gVEMapBattleStartPush(NetClient netClient, Object message) {
        GuildGVEMapBattleStartPush_4000008a resp = (GuildGVEMapBattleStartPush_4000008a) message;
        GuildGVEBattleStart battleStartData = resp.getBattleStartData();
        Client client = (Client) netClient;
    }

    private void gVEBuyTicket(NetClient netClient, Object message) {
        GuildGVEBuyTicketResponse_40000091 resp = (GuildGVEBuyTicketResponse_40000091) message;
        int leftBuyCount = resp.getLeftBuyCount();
        Client client = (Client) netClient;
    }
}
