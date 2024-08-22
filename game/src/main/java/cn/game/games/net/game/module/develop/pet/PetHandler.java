package cn.game.games.net.game.module.develop.pet;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Hero;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.HeroMsg.HeroQualityResetRequest_16000011;
import cn.game.protocol.protobuf.HeroMsg.HeroQualityResetResponse_16000012;
import cn.game.protocol.protobuf.PbProtocol;

@Component
public class PetHandler extends BaseHandler {

	@Override
	protected int getModule() {
		return 0x19;
	}

	@Override
	protected void inititialize() {

		putInvoker(PbProtocol.HeroUpLevelRequest_16000001, this::upLevel);
		putInvoker(PbProtocol.HeroUpLevelMaxRequest_16000021, this::upLevelMax);
		putInvoker(PbProtocol.HeroUpLevelBatchRequest_16000023, this::upLevelBatch);
		putInvoker(PbProtocol.HeroBattleUpLevelBatchRequest_16000025, this::upLevelBattleBatch);
		putInvoker(PbProtocol.HeroConflateRequest_16000003, this::conflate);
		putInvoker(PbProtocol.HeroBattleRequest_16000005, this::battle);
		putInvoker(PbProtocol.HeroBattleDismissRequest_16000009, this::battleDismiss);
		putInvoker(PbProtocol.HeroLevelResetRequest_16000007, this::levelReset);
		putInvoker(PbProtocol.HeroQualityResetRequest_16000011, this::qualityReset);
		putInvoker(PbProtocol.HeroFreeDayRentRequest_16000030, this::freeDayRent);
		putInvoker(PbProtocol.HeroFreeDayRentChooseRequest_16000032, this::freeDayRentChoose);
		putInvoker(PbProtocol.HeroIllustrationsListRequest_16000040, this::illustrationsList);
		putInvoker(PbProtocol.HeroIllustrationsRewardRequest_16000042, this::illustrationsReward);
	}

	private void empty(NetClient client, Object message) {
		HeroQualityResetRequest_16000011 req = (HeroQualityResetRequest_16000011) message;
		HeroQualityResetResponse_16000012.Builder resp = HeroQualityResetResponse_16000012.newBuilder();
		long uid = Long.parseLong(req.getUid());
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		HeroModule heroModule = player.getHeroModule();
		Hero hero = heroModule.get(uid);
		if (hero == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
			return;
		}
		client.sendProtocol(resp.build());
	}

}

