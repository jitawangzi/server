package cn.game.games.net.game.module.develop.mergeequip;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.EquipConfig;
import cn.game.protocol.generated.manager.EquipManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.MergeEquipMsg.MergeEquipmentPartStrengthRequest_23000007;
import cn.game.protocol.protobuf.MergeEquipMsg.MergeEquipmentPartStrengthResponse_23000008;
import cn.game.protocol.protobuf.MergeEquipMsg.MergeEquipmentTeardownRequest_23000003;
import cn.game.protocol.protobuf.MergeEquipMsg.MergeEquipmentTeardownResponse_23000004;
import cn.game.protocol.protobuf.MergeEquipMsg.MergeEquipmentWearRequest_23000001;
import cn.game.protocol.protobuf.MergeEquipMsg.MergeEquipmentWearResponse_23000002;
import cn.game.protocol.protobuf.PbProtocol;

@Component
public class MergeEquipHandler extends BaseHandler {

	@Override
	protected int getModule() {
		return 0x23;
	}

	@Override
	protected void inititialize() {
		putInvoker(PbProtocol.MergeEquipmentWearRequest_23000001, this::wear);
		putInvoker(PbProtocol.MergeEquipmentTeardownRequest_23000003, this::tearDown);
		putInvoker(PbProtocol.MergeEquipmentPartStrengthRequest_23000007, this::strength);
	}

	private void wear(NetClient client, Object message) {
		MergeEquipmentWearRequest_23000001 req = (MergeEquipmentWearRequest_23000001) message;
		MergeEquipmentWearResponse_23000002.Builder resp = MergeEquipmentWearResponse_23000002.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		MergeEquipModule module = player.getModule(MergeEquipModule.class);
		List<Integer> equipList = module.getEquipList();
		int id = req.getId();
		int replaceId = req.getReplaceId();

		MergeEquip mergeEquip = module.get(id);
		if (mergeEquip == null) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}

		if (equipList.contains(id)) {
			client.sendProtocol(resp, ErrorMsgEnum.repeat_request.getId());
			return;
		}

		if (replaceId == 0) {
			if (equipList.size() >= 8) {
				client.sendProtocol(resp, ErrorMsgEnum.config_data_not_found.getId());
				return;
			}
			equipList.add(id);
		} else {
			if (!equipList.contains(replaceId)) {
				client.sendProtocol(resp, ErrorMsgEnum.request_parameter_error.getId());
				return;
			}
			for (int i = 0; i < equipList.size(); i++) {
				if (equipList.get(i) == replaceId) {
					equipList.set(i, id);
					break;
				}
			}
		}
		client.sendProtocol(resp.build());
	}

	private void tearDown(NetClient client, Object message) {
		MergeEquipmentTeardownRequest_23000003 req = (MergeEquipmentTeardownRequest_23000003) message;
		MergeEquipmentTeardownResponse_23000004.Builder resp = MergeEquipmentTeardownResponse_23000004.newBuilder();

		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		MergeEquipModule module = player.getModule(MergeEquipModule.class);
		List<Integer> equipList = module.getEquipList();
		int id = req.getId();
		equipList.remove(Integer.valueOf(id));

		client.sendProtocol(resp.build());
	}

	private void strength(NetClient client, Object message) {
		MergeEquipmentPartStrengthRequest_23000007 req = (MergeEquipmentPartStrengthRequest_23000007) message;
		MergeEquipmentPartStrengthResponse_23000008.Builder resp = MergeEquipmentPartStrengthResponse_23000008.newBuilder();

		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		MergeEquipModule module = player.getModule(MergeEquipModule.class);
		List<Integer> equipList = module.getEquipList();
		int id = req.getId();
		MergeEquip mergeEquip = module.get(id);
		if (mergeEquip == null) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}
		EquipConfig equipConfig = EquipManager.instance().getUIEquipGroupEquipLv(mergeEquip.getConfigId(), mergeEquip.getLevel());
		EquipConfig nextEquipConfig = EquipManager.instance().getUIEquipGroupEquipLv(mergeEquip.getConfigId(), mergeEquip.getLevel() + 1);
		if (nextEquipConfig == null) {
			client.sendProtocol(resp, ErrorMsgEnum.request_parameter_error.getId());
			return;
		}
		PlayerHelper.delResources(player, equipConfig.EquipUpgrade, OpType.MergeEquipLvUp);
		mergeEquip.setLevel(mergeEquip.getLevel() + 1);

		client.sendProtocol(resp.build());
	}
}

