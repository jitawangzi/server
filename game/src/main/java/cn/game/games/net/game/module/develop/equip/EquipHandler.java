package cn.game.games.net.game.module.develop.equip;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.games.cache.entity.Equip;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.handler.GameBaseHandler;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.EquipConfig;
import cn.game.protocol.generated.config.EquipPartLvUpConfig;
import cn.game.protocol.generated.manager.EquipManager;
import cn.game.protocol.generated.manager.EquipPartLvUpManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.EquipMsg.EquipDecomposeRequest_09000005;
import cn.game.protocol.protobuf.EquipMsg.EquipDecomposeResponse_09000006;
import cn.game.protocol.protobuf.EquipMsg.EquipPartStrengthRequest_09000007;
import cn.game.protocol.protobuf.EquipMsg.EquipPartStrengthResponse_09000008;
import cn.game.protocol.protobuf.EquipMsg.EquipTeardownRequest_09000003;
import cn.game.protocol.protobuf.EquipMsg.EquipTeardownResponse_09000004;
import cn.game.protocol.protobuf.EquipMsg.EquipWearRequest_09000001;
import cn.game.protocol.protobuf.EquipMsg.EquipWearResponse_09000002;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**
 * 装备
 */
@Component
public class EquipHandler extends GameBaseHandler {

    @Override
    protected int getModule() {
        return 0x09;
    }

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.EquipWearRequest_09000001, this::wear);
        putInvoker(PbProtocol.EquipTeardownRequest_09000003, this::teardown);
        putInvoker(PbProtocol.EquipPartStrengthRequest_09000007, this::partStrength);
        putInvoker(PbProtocol.EquipDecomposeRequest_09000005, this::decompose);
    }

    private void wear(NetClient client, Object message) {
        EquipWearRequest_09000001 req = (EquipWearRequest_09000001) message;
        long uid = Long.parseLong(req.getUid());
        EquipWearResponse_09000002 defaultInstance = EquipWearResponse_09000002.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		EquipModule module = player.getModule(EquipModule.class);
		Equip equip = module.get(uid);
		if (equip == null) {
			client.sendProtocol(defaultInstance, ErrorMsgEnum.player_data_not_found.ID);
			return; // 装备不存在
		}
		EquipConfig equipConfig = EquipManager.instance().get(equip.getConfigId());

		EquipPart equipPart = module.getEquipPart(equipConfig.pos);
		equipPart.setEquipUid(uid);

        client.sendProtocol(defaultInstance);
    }

    private void teardown(NetClient client, Object message) {
        EquipTeardownRequest_09000003 req = (EquipTeardownRequest_09000003) message;
		long uid = Long.parseLong(req.getUid());
        EquipTeardownResponse_09000004 defaultInstance = EquipTeardownResponse_09000004.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		EquipModule module = player.getModule(EquipModule.class);
		Equip equip = module.get(uid);
		if (equip == null) {
			client.sendProtocol(defaultInstance, ErrorMsgEnum.player_data_not_found.ID);
			return; // 装备不存在
		}
		EquipConfig equipConfig = EquipManager.instance().get(equip.getConfigId());

		EquipPart equipPart = module.getEquipPart(equipConfig.pos);
		equipPart.setEquipUid(0);

        client.sendProtocol(defaultInstance);
    }

    private void partStrength(NetClient client, Object message) {
        EquipPartStrengthRequest_09000007 req = (EquipPartStrengthRequest_09000007) message;
        int type = req.getType();
        EquipPartStrengthResponse_09000008 defaultInstance = EquipPartStrengthResponse_09000008.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());

		EquipModule module = player.getModule(EquipModule.class);
		EquipPart equipPart = module.getEquipPart(type);
		EquipPartLvUpConfig curConfig = EquipPartLvUpManager.instance().getUIPoslevel(type, equipPart.getStrength());
		EquipPartLvUpConfig nextConfig = EquipPartLvUpManager.instance().getUIPoslevel(type, equipPart.getStrength() + 1);

		if (nextConfig == null) {
			client.sendProtocol(defaultInstance, ErrorMsgEnum.level_limit.ID);
			return; // 强化等级已达上限
		}
		PlayerHelper.delResources(player, curConfig.cost, OpType.EquipPartStrength);

		equipPart.setStrength(equipPart.getStrength() + 1);

		client.sendProtocol(defaultInstance);
    }

    private void decompose(NetClient client, Object message) {
        EquipDecomposeRequest_09000005 req = (EquipDecomposeRequest_09000005) message;
        List<String> uidList = req.getUidList();
        EquipDecomposeResponse_09000006 defaultInstance = EquipDecomposeResponse_09000006.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());

        EquipModule module = player.getModule(EquipModule.class);
		List<RewardInfo> allRewardInfos = new ArrayList<>();
		for (String string : uidList) {
			long uid = Long.parseLong(string);
			Equip equip = module.get(uid);
			boolean del = module.del(uid, OpType.EquipDecompose);
			if (del) {
				int[][] decomposeItems = EquipManager.instance().get(equip.getConfigId()).decomposeItems;
				List<RewardInfo> resources = PlayerHelper.addResources(player, decomposeItems, OpType.EquipDecompose);
				allRewardInfos.addAll(resources);
			}
		}
		EquipDecomposeResponse_09000006.Builder resp = EquipDecomposeResponse_09000006.newBuilder();
		resp.addAllReward(allRewardInfos);

		client.sendProtocol(resp.build());
    }
}
