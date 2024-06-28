package cn.game.games.net.game.module.develop.hccommon;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.HCHeroArousalConfig;
import cn.game.protocol.generated.config.HCHeroConfig;
import cn.game.protocol.generated.manager.HCHeroArousalManager;
import cn.game.protocol.generated.manager.HCHeroManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.HCHeroMsg.HCHeroCompositeRequest_26000007;
import cn.game.protocol.protobuf.HCHeroMsg.HCHeroCompositeResponse_26000008;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

@Component
public class HCCommonHandler extends BaseHandler {

	@Override
	protected int getModule() {
		return 0x28;
	}

	@Override
	protected void inititialize() {
//		putInvoker(PbProtocol.hc, this::adsBattleSpeed);

	}

	private void adsBattleSpeed(NetClient client, Object message) {
		HCHeroCompositeRequest_26000007 req = (HCHeroCompositeRequest_26000007) message;
		HCHeroCompositeResponse_26000008.Builder resp = HCHeroCompositeResponse_26000008.newBuilder();
		int id = req.getId();
		HCHeroConfig heroConfig = HCHeroManager.instance().getNullable(id);
		if (heroConfig == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.config_data_not_found.getId());
			return;
		}
		HCHeroArousalConfig arousalConfig = HCHeroArousalManager.instance().getUIHeroIDStar(id, 0);
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		if (!PlayerHelper.delResources(player, arousalConfig.StarCost, OpType.HCHeroComposite)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.resource_not_enough.getId());
			return;
		}
		List<RewardInfo> resources = PlayerHelper.addResources(player, id, 1);
		if (!resources.isEmpty()) {
			resp.setHero(resources.get(0).getHcHero());
		}
		client.sendProtocol(resp.build());
	}
}

