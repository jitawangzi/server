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
import cn.game.protocol.protobuf.PetMsg.PetBreakRequest_19000005;
import cn.game.protocol.protobuf.PetMsg.PetBreakResponse_19000006;
import cn.game.protocol.protobuf.PetMsg.PetCompositeRequest_19000001;
import cn.game.protocol.protobuf.PetMsg.PetCompositeResponse_19000002;
import cn.game.protocol.protobuf.PetMsg.PetRefineRequest_19000007;
import cn.game.protocol.protobuf.PetMsg.PetRefineResponse_19000008;
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

		putInvoker(PbProtocol.PetBattleRequest_19000011, this::battle);
		putInvoker(PbProtocol.PetBondsActivateRequest_19000013, this::bondsActivate);
		putInvoker(PbProtocol.PetBondsUpLevelRequest_19000015, this::bondsUpLevel);
		putInvoker(PbProtocol.PetBreakRequest_19000005, this::breakpet);
		putInvoker(PbProtocol.PetCompositeRequest_19000001, this::composite);
		putInvoker(PbProtocol.PetRefineRequest_19000007, this::refine);
		putInvoker(PbProtocol.PetUpLevelRequest_19000003, this::upLevel);
	}

	private void upLevel(NetClient client, Object message) {
		PetUpLevelRequest_19000003 req = (PetUpLevelRequest_19000003) message;
		PetUpLevelResponse_19000004.Builder resp = PetUpLevelResponse_19000004.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		if (!player.isFuncOpen(InitialUI.SoulPets)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}
		int id = req.getId();
		PetModule petModule = player.getPetModule();

		client.sendProtocol(resp.build());
	}

	private void refine(NetClient client, Object message) {
		PetRefineRequest_19000007 req = (PetRefineRequest_19000007) message;
		PetRefineResponse_19000008.Builder resp = PetRefineResponse_19000008.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		if (!player.isFuncOpen(InitialUI.SoulPets)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}
		PetModule petModule = player.getPetModule();

		client.sendProtocol(resp.build());
	}

	private void composite(NetClient client, Object message) {
		PetCompositeRequest_19000001 req = (PetCompositeRequest_19000001) message;
		PetCompositeResponse_19000002.Builder resp = PetCompositeResponse_19000002.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		if (!player.isFuncOpen(InitialUI.SoulPets)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}
		PetModule petModule = player.getPetModule();

		client.sendProtocol(resp.build());
	}

	private void breakpet(NetClient client, Object message) {
		PetBreakRequest_19000005 req = (PetBreakRequest_19000005) message;
		PetBreakResponse_19000006.Builder resp = PetBreakResponse_19000006.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		if (!player.isFuncOpen(InitialUI.SoulPets)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}
		PetModule petModule = player.getPetModule();

		client.sendProtocol(resp.build());
	}

	private void bondsUpLevel(NetClient client, Object message) {
		PetBondsUpLevelRequest_19000015 req = (PetBondsUpLevelRequest_19000015) message;
		PetBondsUpLevelResponse_19000016.Builder resp = PetBondsUpLevelResponse_19000016.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		if (!player.isFuncOpen(InitialUI.SoulPets)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}
		PetModule petModule = player.getPetModule();

		client.sendProtocol(resp.build());
	}

	private void bondsActivate(NetClient client, Object message) {
		PetBondsActivateRequest_19000013 req = (PetBondsActivateRequest_19000013) message;
		PetBondsActivateResponse_19000014.Builder resp = PetBondsActivateResponse_19000014.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		if (!player.isFuncOpen(InitialUI.SoulPets)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}
		PetModule petModule = player.getPetModule();

		client.sendProtocol(resp.build());
	}
	private void battle(NetClient client, Object message) {
		PetBattleRequest_19000011 req = (PetBattleRequest_19000011) message;
		PetBattleResponse_19000012.Builder resp = PetBattleResponse_19000012.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		if (!player.isFuncOpen(InitialUI.SoulPets)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}
		PetModule petModule = player.getPetModule();

		client.sendProtocol(resp.build());
	}

}

