package cn.game.games.net.game.module.develop.pet;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PetMsg.PetBattleRequest_19000011;
import cn.game.protocol.protobuf.PetMsg.PetBattleResponse_19000012;
import cn.game.protocol.protobuf.PetMsg.PetBondsActivateRequest_19000013;
import cn.game.protocol.protobuf.PetMsg.PetBondsActivateResponse_19000014;
import cn.game.protocol.protobuf.PetMsg.PetBondsUpLevelRequest_19000015;
import cn.game.protocol.protobuf.PetMsg.PetBondsUpLevelResponse_19000016;
import cn.game.protocol.protobuf.PetMsg.PetBreakUpRequest_19000005;
import cn.game.protocol.protobuf.PetMsg.PetBreakUpResponse_19000006;
import cn.game.protocol.protobuf.PetMsg.PetCompositeRequest_19000001;
import cn.game.protocol.protobuf.PetMsg.PetCompositeResponse_19000002;
import cn.game.protocol.protobuf.PetMsg.PetRefineRequest_19000007;
import cn.game.protocol.protobuf.PetMsg.PetRefineResponse_19000008;
import cn.game.protocol.protobuf.PetMsg.PetRefineSaveRequest_19000009;
import cn.game.protocol.protobuf.PetMsg.PetRefineSaveResponse_1900000a;
import cn.game.protocol.protobuf.PetMsg.PetUpLevelRequest_19000003;
import cn.game.protocol.protobuf.PetMsg.PetUpLevelResponse_19000004;

@Component
public class PetHandler extends BaseHandler {

    @Override
    protected int getModule() {
        return 0x19;
    }

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.PetCompositeRequest_19000001, this::composite);
        putInvoker(PbProtocol.PetUpLevelRequest_19000003, this::upLevel);
        putInvoker(PbProtocol.PetBreakUpRequest_19000005, this::breakUp);
        putInvoker(PbProtocol.PetRefineRequest_19000007, this::refine);
        putInvoker(PbProtocol.PetBattleRequest_19000011, this::battle);
        putInvoker(PbProtocol.PetBondsActivateRequest_19000013, this::bondsActivate);
        putInvoker(PbProtocol.PetBondsUpLevelRequest_19000015, this::bondsUpLevel);
        putInvoker(PbProtocol.PetRefineSaveRequest_19000009, this::refineSave);
    }

    private void composite(NetClient client, Object message) {
        PetCompositeRequest_19000001 req = (PetCompositeRequest_19000001) message;
        int id = req.getId();
        PetCompositeResponse_19000002 defaultInstance = PetCompositeResponse_19000002.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.SoulPets)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        client.sendProtocol(defaultInstance);
    }

    private void upLevel(NetClient client, Object message) {
        PetUpLevelRequest_19000003 req = (PetUpLevelRequest_19000003) message;
        int id = req.getId();
        PetUpLevelResponse_19000004 defaultInstance = PetUpLevelResponse_19000004.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.SoulPets)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        client.sendProtocol(defaultInstance);
    }

    private void breakUp(NetClient client, Object message) {
        PetBreakUpRequest_19000005 req = (PetBreakUpRequest_19000005) message;
        int id = req.getId();
        PetBreakUpResponse_19000006 defaultInstance = PetBreakUpResponse_19000006.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.SoulPets)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        PetBreakUpResponse_19000006.Builder resp = PetBreakUpResponse_19000006.newBuilder();
        client.sendProtocol(resp.build());
    }

    private void refine(NetClient client, Object message) {
        PetRefineRequest_19000007 req = (PetRefineRequest_19000007) message;
        int id = req.getId();
        PetRefineResponse_19000008 defaultInstance = PetRefineResponse_19000008.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.SoulPets)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        client.sendProtocol(defaultInstance);
    }

    private void battle(NetClient client, Object message) {
        PetBattleRequest_19000011 req = (PetBattleRequest_19000011) message;
        int id = req.getId();
        PetBattleResponse_19000012 defaultInstance = PetBattleResponse_19000012.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.SoulPets)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        client.sendProtocol(defaultInstance);
    }

    private void bondsActivate(NetClient client, Object message) {
        PetBondsActivateRequest_19000013 req = (PetBondsActivateRequest_19000013) message;
        int id = req.getId();
        PetBondsActivateResponse_19000014 defaultInstance = PetBondsActivateResponse_19000014.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.SoulPets)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        client.sendProtocol(defaultInstance);
    }

    private void bondsUpLevel(NetClient client, Object message) {
        PetBondsUpLevelRequest_19000015 req = (PetBondsUpLevelRequest_19000015) message;
        int id = req.getId();
        PetBondsUpLevelResponse_19000016 defaultInstance = PetBondsUpLevelResponse_19000016.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.SoulPets)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        client.sendProtocol(defaultInstance);
    }

    private void refineSave(NetClient client, Object message) {
        PetRefineSaveRequest_19000009 req = (PetRefineSaveRequest_19000009) message;
        PetRefineSaveResponse_1900000a defaultInstance = PetRefineSaveResponse_1900000a.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.SoulPets)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        client.sendProtocol(defaultInstance);
    }
}
