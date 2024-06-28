package cn.game.games.net.game.module.develop.hccommon;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.player.VarConstant;
import cn.game.games.net.game.module.player.VarModule;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.protobuf.HCCommonMsg.HCBattleSpeedAdsRequest_28000020;
import cn.game.protocol.protobuf.HCCommonMsg.HCBattleSpeedAdsResponse_28000021;
import cn.game.protocol.protobuf.PbProtocol;

@Component
public class HCCommonHandler extends BaseHandler {

	@Override
	protected int getModule() {
		return 0x28;
	}

	@Override
	protected void inititialize() {
		putInvoker(PbProtocol.HCBattleSpeedAdsRequest_28000020, this::adsBattleSpeed);

	}

	private void adsBattleSpeed(NetClient client, Object message) {
		HCBattleSpeedAdsRequest_28000020 req = (HCBattleSpeedAdsRequest_28000020) message;
		HCBattleSpeedAdsResponse_28000021.Builder resp = HCBattleSpeedAdsResponse_28000021.newBuilder();

		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		VarModule varModule = player.getVarModule(); 
		HCCommonModule hcCommonModule = player.getHCCommonModule(); 
		int count = varModule.addVar(VarConstant.BATTLE_SPEED_ADS_COUNT);
		if (count >= GlobalConst.CombatSpeedAdCnt) {
			hcCommonModule.setBattleSpeedUnlock(true);
			varModule.clearVar(VarConstant.BATTLE_SPEED_ADS_COUNT);
		}
		player.handleEvent(EventTypeEnum.WatchAds);
		client.sendProtocol(resp.build());
	}
}

