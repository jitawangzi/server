package cn.game.games.net.game.module.develop.secretscript;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.SecretscriptBreakConfig;
import cn.game.protocol.generated.config.SecretscriptConfig;
import cn.game.protocol.generated.config.SecretscriptLvConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.SecretscriptBreakManager;
import cn.game.protocol.generated.manager.SecretscriptLvManager;
import cn.game.protocol.generated.manager.SecretscriptManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.SecretscriptMsg.SecretscriptBreakUpRequest_38000005;
import cn.game.protocol.protobuf.SecretscriptMsg.SecretscriptBreakUpResponse_38000006;
import cn.game.protocol.protobuf.SecretscriptMsg.SecretscriptFusionPvPRequest_38000011;
import cn.game.protocol.protobuf.SecretscriptMsg.SecretscriptFusionPvPResponse_38000012;
import cn.game.protocol.protobuf.SecretscriptMsg.SecretscriptFusionRequest_38000007;
import cn.game.protocol.protobuf.SecretscriptMsg.SecretscriptFusionResponse_38000008;
import cn.game.protocol.protobuf.SecretscriptMsg.SecretscriptUpLevelRequest_38000003;
import cn.game.protocol.protobuf.SecretscriptMsg.SecretscriptUpLevelResponse_38000004;

@Component
public class SecretscriptHandler extends BaseHandler {

    @Override
    protected int getModule() {
        return 0x38;
    }

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.SecretscriptUpLevelRequest_38000003, this::upLevel);
        putInvoker(PbProtocol.SecretscriptBreakUpRequest_38000005, this::breakUp);
        putInvoker(PbProtocol.SecretscriptFusionRequest_38000007, this::fusion);
        putInvoker(PbProtocol.SecretscriptFusionPvPRequest_38000011, this::fusionPvP);
    }

    private void upLevel(NetClient client, Object message) {
        SecretscriptUpLevelRequest_38000003 req = (SecretscriptUpLevelRequest_38000003) message;
        int id = req.getId();
        SecretscriptUpLevelResponse_38000004 defaultInstance = SecretscriptUpLevelResponse_38000004.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.Avatar)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        SecretscriptModule module = player.getModule(SecretscriptModule.class);
        Secretscript secretscript = module.get(id);
        if (secretscript == null) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.resource_not_enough.getId());
            return;
        }
        SecretscriptConfig config = SecretscriptManager.instance().getUISecretscriptMarkSecretscriptStar(id, secretscript.getStar());
        int maxLevel = config.SecretscriptLvMax;
        if (secretscript.getLevel() >= maxLevel) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.level_limit.getId());
            return;
        }
        SecretscriptLvConfig lvConfig = SecretscriptLvManager.instance().getUIInitialQualityLv(config.SecretscriptQuality, secretscript.getLevel());
        SecretscriptLvConfig nextLvConfig = SecretscriptLvManager.instance().getUIInitialQualityLv(config.SecretscriptQuality, secretscript.getLevel() + 1);
        if (nextLvConfig == null) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.level_limit.getId());
            return;
        }
		int costId = 0;
		if (config.SecretscriptType == 1) {
			costId = 207001;
		} else if (config.SecretscriptType == 2) {
			costId = 207002;
		} else if (config.SecretscriptType == 3) {
			costId = 207003;
		} else if (config.SecretscriptType == 4) {
			costId = 207004;
		} else if (config.SecretscriptType == 5) {
			costId = 207005;
		} else {
			throw new IllegalArgumentException("神通类型错误: " + config.SecretscriptType);
		}
        List<SimpleEntry<Integer, Integer>> costEntries = new ArrayList<>();
		costEntries.add(new SimpleEntry<>(costId, lvConfig.LvConsumeSpecialItem));
        costEntries.add(new SimpleEntry<>(207008, lvConfig.LvConsumeNormalItem));
        costEntries.add(new SimpleEntry<>(Asset.gold.ID, lvConfig.LvConsumeMoney));
        if (!PlayerHelper.delResources(player, costEntries, OpType.Secretscript)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.resource_not_enough.getId());
            return;
        }
        secretscript.setLevel(secretscript.getLevel() + 1);
        client.sendProtocol(defaultInstance);
    }

    private void breakUp(NetClient client, Object message) {
        SecretscriptBreakUpRequest_38000005 req = (SecretscriptBreakUpRequest_38000005) message;
        int id = req.getId();
        SecretscriptBreakUpResponse_38000006 defaultInstance = SecretscriptBreakUpResponse_38000006.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.Avatar)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        SecretscriptModule module = player.getModule(SecretscriptModule.class);
        Secretscript secretscript = module.get(id);
        int star = secretscript == null ? 0 : secretscript.getStar();
        SecretscriptConfig config = SecretscriptManager.instance().getUISecretscriptMarkSecretscriptStar(id, star);
        SecretscriptBreakConfig breakConfig = SecretscriptBreakManager.instance().getUIInitialQualityStar(config.SecretscriptQuality, star);
        int itemId = config.FragmentID;
        int itemCount = breakConfig.BreakConsumeSpecialItem;
        long count = player.getItemModule().getCount(itemId);
        int specialItemCount = (int) (itemCount - count);
        if (specialItemCount < 0) {
            specialItemCount = 0;
        }
        if (specialItemCount > 0 && breakConfig.BreakConsumeSpecialItem1 == 0) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.resource_not_enough.getId());
            return;
        }
        if (specialItemCount > 0 && player.getItemModule().getCount(breakConfig.BreakConsumeSpecialItem1) < specialItemCount) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.resource_not_enough.getId());
            return;
        }
        PlayerHelper.delResources(player, itemId, itemCount, OpType.Secretscript);
        PlayerHelper.delResources(player, breakConfig.BreakConsumeSpecialItem1, specialItemCount, OpType.Secretscript);
        if (secretscript == null) {
            module.add(id, OpType.Secretscript);
        } else {
            secretscript.setStar(star + 1);
        }
        client.sendProtocol(defaultInstance);
    }

    private void fusion(NetClient client, Object message) {
        SecretscriptFusionRequest_38000007 req = (SecretscriptFusionRequest_38000007) message;
        int id = req.getId();
        int pos = req.getPos();
        SecretscriptFusionResponse_38000008 defaultInstance = SecretscriptFusionResponse_38000008.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.Avatar)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        SecretscriptModule module = player.getModule(SecretscriptModule.class);
        Secretscript secretscript = module.get(id);
        if (secretscript == null) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.player_data_not_found.getId());
            return;
        }
        Map<Integer, Integer> secretscriptPosMap = module.getSecretscriptPosMap();
        if (pos == 0) {
            secretscriptPosMap.remove(id);
        } else {
            secretscriptPosMap.put(id, pos);
        }
        client.sendProtocol(defaultInstance);
    }

    private void fusionPvP(NetClient client, Object message) {
        SecretscriptFusionPvPRequest_38000011 req = (SecretscriptFusionPvPRequest_38000011) message;
        Map<Integer, Integer> secretscriptMapMap = req.getSecretscriptMapMap();
        SecretscriptFusionPvPResponse_38000012 defaultInstance = SecretscriptFusionPvPResponse_38000012.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.Avatar)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }
       /* if (req.getSecretscriptMapMap().size() != 5){
            client.sendProtocol(defaultInstance, ErrorMsgEnum.request_parameter_error.getId());
            return;
        }*/
        SecretscriptModule module = player.getModule(SecretscriptModule.class);
        for(int id : req.getSecretscriptMapMap().keySet()){
            Secretscript secretscript = module.get(id);
            if (secretscript == null) {
//                client.sendProtocol(defaultInstance, ErrorMsgEnum.player_data_not_found.getId());
//                return;
                log.error("fusionPvP error secretscriptMapMap key:{} not found ",id);
            }
        }
        module.setPvPSecretscriptMap(secretscriptMapMap);
        client.sendProtocol(defaultInstance);
    }
}
