package cn.game.simulation.client.handler;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.games.net.game.handler.GameBaseHandler;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.protobuf.MailMsg.MailDeleteResponse_12000008;
import cn.game.protocol.protobuf.MailMsg.MailInfo;
import cn.game.protocol.protobuf.MailMsg.MailListResponse_12000002;
import cn.game.protocol.protobuf.MailMsg.MailNewPush_12010001;
import cn.game.protocol.protobuf.MailMsg.MailReceiveResponse_12000006;
import cn.game.protocol.protobuf.MailMsg.MailSeeResponse_12000004;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.simulation.client.Client;

@Component
public class ClientMailHandler extends GameBaseHandler {

    @Override
    protected int getModule() {
        return 0x12;
    }

    @Override
    protected InitialUI getInitialUI() {
        return InitialUI.Letter;
    }

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.MailListResponse_12000002, this::list);
        putInvoker(PbProtocol.MailSeeResponse_12000004, this::see);
        putInvoker(PbProtocol.MailReceiveResponse_12000006, this::receive);
        putInvoker(PbProtocol.MailDeleteResponse_12000008, this::delete);
        putInvoker(PbProtocol.MailNewPush_12010001, this::newPush);
    }

    private void list(NetClient netClient, Object message) {
        MailListResponse_12000002 resp = (MailListResponse_12000002) message;
        List<MailInfo> mailsList = resp.getMailsList();
        Client client = (Client) netClient;
        client.mailsList = mailsList; 
    }

    private void see(NetClient netClient, Object message) {
        MailSeeResponse_12000004 resp = (MailSeeResponse_12000004) message;
        Client client = (Client) netClient;
    }

    private void receive(NetClient netClient, Object message) {
        MailReceiveResponse_12000006 resp = (MailReceiveResponse_12000006) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void delete(NetClient netClient, Object message) {
        MailDeleteResponse_12000008 resp = (MailDeleteResponse_12000008) message;
        Client client = (Client) netClient;
    }

    private void newPush(NetClient netClient, Object message) {
        MailNewPush_12010001 resp = (MailNewPush_12010001) message;
        MailInfo mail = resp.getMail();
        Client client = (Client) netClient;
    }
}
