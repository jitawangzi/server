package cn.game.simulation.client.handler;

import java.util.List;
import org.springframework.stereotype.Component;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PetMsg.PetBattleResponse_19000012;
import cn.game.protocol.protobuf.PetMsg.PetBondsActivateResponse_19000014;
import cn.game.protocol.protobuf.PetMsg.PetBreakUpResponse_19000006;
import cn.game.protocol.protobuf.PetMsg.PetCompositeResponse_19000002;
import cn.game.protocol.protobuf.PetMsg.PetRefineResponse_19000008;
import cn.game.protocol.protobuf.PetMsg.PetRefineSaveResponse_1900000a;
import cn.game.protocol.protobuf.PetMsg.PetUpLevelResponse_19000004;
import cn.game.simulation.client.Client;
import cn.game.protocol.protobuf.PetMsg.PetBondsUpLevelResponse_19000016;

@Component
public class ClientPetHandler extends BaseHandler {

    @Override
    protected int getModule() {
        return 0x19;
    }

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.PetCompositeResponse_19000002, this::composite);
        putInvoker(PbProtocol.PetUpLevelResponse_19000004, this::upLevel);
        putInvoker(PbProtocol.PetBreakUpResponse_19000006, this::breakUp);
        putInvoker(PbProtocol.PetRefineResponse_19000008, this::refine);
        putInvoker(PbProtocol.PetRefineSaveResponse_1900000a, this::refineSave);
        putInvoker(PbProtocol.PetBattleResponse_19000012, this::battle);
        putInvoker(PbProtocol.PetBondsActivateResponse_19000014, this::bondsActivate);
        putInvoker(PbProtocol.PetBondsUpLevelResponse_19000016, this::bondsUpLevel);
    }

    private void composite(NetClient netClient, Object message) {
        PetCompositeResponse_19000002 resp = (PetCompositeResponse_19000002) message;
        Client client = (Client) netClient;
    }

    private void upLevel(NetClient netClient, Object message) {
        PetUpLevelResponse_19000004 resp = (PetUpLevelResponse_19000004) message;
        Client client = (Client) netClient;
    }

    private void breakUp(NetClient netClient, Object message) {
        PetBreakUpResponse_19000006 resp = (PetBreakUpResponse_19000006) message;
        Client client = (Client) netClient;
    }

    private void refine(NetClient netClient, Object message) {
        PetRefineResponse_19000008 resp = (PetRefineResponse_19000008) message;
        List<Integer> skillsList = resp.getSkillsList();
        Client client = (Client) netClient;
    }

    private void refineSave(NetClient netClient, Object message) {
        PetRefineSaveResponse_1900000a resp = (PetRefineSaveResponse_1900000a) message;
        Client client = (Client) netClient;
    }

    private void battle(NetClient netClient, Object message) {
        PetBattleResponse_19000012 resp = (PetBattleResponse_19000012) message;
        Client client = (Client) netClient;
    }

    private void bondsActivate(NetClient netClient, Object message) {
        PetBondsActivateResponse_19000014 resp = (PetBondsActivateResponse_19000014) message;
        Client client = (Client) netClient;
    }

    private void bondsUpLevel(NetClient netClient, Object message) {
        PetBondsUpLevelResponse_19000016 resp = (PetBondsUpLevelResponse_19000016) message;
        Client client = (Client) netClient;
    }
}
