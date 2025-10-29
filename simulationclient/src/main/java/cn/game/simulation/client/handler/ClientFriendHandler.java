package cn.game.simulation.client.handler;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.protocol.protobuf.BaseMsg.SimplePlayerInfo;
import cn.game.protocol.protobuf.FriendMsg.FriendAddPush_30000023;
import cn.game.protocol.protobuf.FriendMsg.FriendApplicationResponse_30000008;
import cn.game.protocol.protobuf.FriendMsg.FriendApplyListResponse_30000054;
import cn.game.protocol.protobuf.FriendMsg.FriendApplyPush_30000022;
import cn.game.protocol.protobuf.FriendMsg.FriendApplyResponse_30000006;
import cn.game.protocol.protobuf.FriendMsg.FriendBlackListResponse_30000052;
import cn.game.protocol.protobuf.FriendMsg.FriendBlackResponse_30000011;
import cn.game.protocol.protobuf.FriendMsg.FriendDelPush_30000024;
import cn.game.protocol.protobuf.FriendMsg.FriendDeleteResponse_3000000a;
import cn.game.protocol.protobuf.FriendMsg.FriendGiftPush_30000028;
import cn.game.protocol.protobuf.FriendMsg.FriendGiftReceiveResponse_30000015;
import cn.game.protocol.protobuf.FriendMsg.FriendGiftResponse_30000013;
import cn.game.protocol.protobuf.FriendMsg.FriendInfo;
import cn.game.protocol.protobuf.FriendMsg.FriendListResponse_30000002;
import cn.game.protocol.protobuf.FriendMsg.FriendRecommendResponse_30000004;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.simulation.client.Client;

@Component
public class ClientFriendHandler extends BaseHandler {

    @Override
    protected int getModule() {
        return 0x30;
    }

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.FriendRecommendResponse_30000004, this::recommendList);
        putInvoker(PbProtocol.FriendBlackListResponse_30000052, this::blackList);
        putInvoker(PbProtocol.FriendApplyListResponse_30000054, this::applyList);
        putInvoker(PbProtocol.FriendApplyResponse_30000006, this::apply);
        putInvoker(PbProtocol.FriendApplicationResponse_30000008, this::application);
        putInvoker(PbProtocol.FriendDeleteResponse_3000000a, this::delete);
        putInvoker(PbProtocol.FriendBlackResponse_30000011, this::black);
        putInvoker(PbProtocol.FriendGiftResponse_30000013, this::gift);
        putInvoker(PbProtocol.FriendGiftReceiveResponse_30000015, this::giftReceive);
        putInvoker(PbProtocol.FriendApplyPush_30000022, this::applyPush);
        putInvoker(PbProtocol.FriendAddPush_30000023, this::addPush);
        putInvoker(PbProtocol.FriendDelPush_30000024, this::delPush);
        putInvoker(PbProtocol.FriendGiftPush_30000028, this::giftPush);
        putInvoker(PbProtocol.FriendListResponse_30000002, this::list);
    }

    protected void recommendList(NetClient client, Object message) {
        Client client2 = (Client) client;
        FriendRecommendResponse_30000004 response = (FriendRecommendResponse_30000004) message;
        List<SimplePlayerInfo> playersList = response.getPlayersList();
        client2.recommendList = playersList;
    }

    private void list(NetClient netClient, Object message) {
        FriendListResponse_30000002 resp = (FriendListResponse_30000002) message;
        List<FriendInfo> friendsList = resp.getFriendsList();
        Client client = (Client) netClient;
        client.friendsList = friendsList; 
    }

    private void blackList(NetClient netClient, Object message) {
        FriendBlackListResponse_30000052 resp = (FriendBlackListResponse_30000052) message;
        List<SimplePlayerInfo> playersList = resp.getPlayersList();
        Client client = (Client) netClient;
        client.blackList = playersList.stream().map(r-> r.getId()).collect(toList()); 
    }

    private void applyList(NetClient netClient, Object message) {
        FriendApplyListResponse_30000054 resp = (FriendApplyListResponse_30000054) message;
        List<SimplePlayerInfo> playersList = resp.getPlayersList();
        Client client = (Client) netClient;
        client.applicationList = playersList.stream().map(r-> r.getId()).collect(toList()); 

    }

    private void apply(NetClient netClient, Object message) {
        FriendApplyResponse_30000006 resp = (FriendApplyResponse_30000006) message;
        Client client = (Client) netClient;
    }

    private void application(NetClient netClient, Object message) {
        FriendApplicationResponse_30000008 resp = (FriendApplicationResponse_30000008) message;
        List<String> friendIdsList = resp.getFriendIdsList();
        Client client = (Client) netClient;
    }

    private void delete(NetClient netClient, Object message) {
        FriendDeleteResponse_3000000a resp = (FriendDeleteResponse_3000000a) message;
        Client client = (Client) netClient;
    }

    private void black(NetClient netClient, Object message) {
        FriendBlackResponse_30000011 resp = (FriendBlackResponse_30000011) message;
        Client client = (Client) netClient;
    }

    private void gift(NetClient netClient, Object message) {
        FriendGiftResponse_30000013 resp = (FriendGiftResponse_30000013) message;
        Client client = (Client) netClient;
    }

    private void giftReceive(NetClient netClient, Object message) {
        FriendGiftReceiveResponse_30000015 resp = (FriendGiftReceiveResponse_30000015) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void applyPush(NetClient netClient, Object message) {
        FriendApplyPush_30000022 resp = (FriendApplyPush_30000022) message;
        long playerId = resp.getPlayerId();
        long applyPlayerId = resp.getApplyPlayerId();
        String applyPlayerServer = resp.getApplyPlayerServer();
        Client client = (Client) netClient;
    }

    private void addPush(NetClient netClient, Object message) {
        FriendAddPush_30000023 resp = (FriendAddPush_30000023) message;
        long playerId = resp.getPlayerId();
        long friendId = resp.getFriendId();
        String friendServer = resp.getFriendServer();
        Client client = (Client) netClient;
    }

    private void delPush(NetClient netClient, Object message) {
        FriendDelPush_30000024 resp = (FriendDelPush_30000024) message;
        long playerId = resp.getPlayerId();
        long friendId = resp.getFriendId();
        Client client = (Client) netClient;
    }

    private void giftPush(NetClient netClient, Object message) {
        FriendGiftPush_30000028 resp = (FriendGiftPush_30000028) message;
        long sendPlayerId = resp.getSendPlayerId();
        long recvPlayerId = resp.getRecvPlayerId();
        Client client = (Client) netClient;
    }

}
