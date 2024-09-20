package cn.game.simulation.client.handler;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.protocol.protobuf.BattleMsg;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.simulation.client.Client;

/**
 * @ClassName ClientChapterHandler
 *
 * @description:
 * @author: ly
 * @create: 2024-09-19 17:18 @Version 1.0
 */
@Component
public class ClientChapterHandler extends BaseHandler {
    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.BattlePvPTargetListResponse_13000112,this::targetPvPListResponse);

    }



    private void targetPvPListResponse(NetClient netClient, Object o) {
        BattleMsg.BattlePvPTargetListResponse_13000112 res = (BattleMsg.BattlePvPTargetListResponse_13000112) o;
        Client client = (Client) netClient;
        if (res.getScoreListCount() > 0){
            client.setTargetListResponse(res);
        }
    }

    @Override
    protected int getModule() {
        return 0x13;
    }
}
