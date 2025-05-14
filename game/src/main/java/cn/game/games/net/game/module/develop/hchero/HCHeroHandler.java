package cn.game.games.net.game.module.develop.hchero;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.HCHero;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.HCHeroArousalConfig;
import cn.game.protocol.generated.config.HCHeroConfig;
import cn.game.protocol.generated.config.HCHeroUpgradeConfig;
import cn.game.protocol.generated.manager.HCHeroArousalManager;
import cn.game.protocol.generated.manager.HCHeroManager;
import cn.game.protocol.generated.manager.HCHeroUpgradeManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.HCHeroMsg.HCHeroAdsRequest_26000009;
import cn.game.protocol.protobuf.HCHeroMsg.HCHeroAdsResponse_2600000a;
import cn.game.protocol.protobuf.HCHeroMsg.HCHeroBattleRequest_26000005;
import cn.game.protocol.protobuf.HCHeroMsg.HCHeroBattleResponse_26000006;
import cn.game.protocol.protobuf.HCHeroMsg.HCHeroCompositeRequest_26000007;
import cn.game.protocol.protobuf.HCHeroMsg.HCHeroCompositeResponse_26000008;
import cn.game.protocol.protobuf.HCHeroMsg.HCHeroStarUpRequest_26000003;
import cn.game.protocol.protobuf.HCHeroMsg.HCHeroStarUpResponse_26000004;
import cn.game.protocol.protobuf.HCHeroMsg.HCHeroUpLevelRequest_26000001;
import cn.game.protocol.protobuf.HCHeroMsg.HCHeroUpLevelResponse_26000002;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.IntMapWrapper;

@Component
public class HCHeroHandler extends BaseHandler {

	@Override
	protected int getModule() {
		return 0x26;
	}

	@Override
	protected void inititialize() {

		putInvoker(PbProtocol.HCHeroAdsRequest_26000009, this::adsItem);
		putInvoker(PbProtocol.HCHeroBattleRequest_26000005, this::battle);
		putInvoker(PbProtocol.HCHeroStarUpRequest_26000003, this::starUp);
		putInvoker(PbProtocol.HCHeroUpLevelRequest_26000001, this::upLevel);
		putInvoker(PbProtocol.HCHeroCompositeRequest_26000007, this::composite);

	}

	private void composite(NetClient client, Object message) {
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
		PlayerHelper.delResources(player, arousalConfig.StarCost, OpType.HCHeroComposite);
		List<RewardInfo> resources = PlayerHelper.addResources(player, id, 1);
		if (!resources.isEmpty()) {
			resp.setHero(resources.get(0).getHcHero());
		}
		client.sendProtocol(resp.build());
	}
	private void adsItem(NetClient client, Object message) {
		HCHeroAdsRequest_26000009 req = (HCHeroAdsRequest_26000009) message;
		int id = req.getId();
		HCHeroAdsResponse_2600000a.Builder resp = HCHeroAdsResponse_2600000a.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		HCHeroModule heroModule = player.getHCHeroModule();
		IntMapWrapper heroItemTimesMap = heroModule.getHeroItemTimesMap();
		int count = heroItemTimesMap.getValue(id);
		int freeHcHeroItemTimes = heroModule.getFreeHcHeroItemTimes();
		if (count >= GlobalConst.ADStarCnt && freeHcHeroItemTimes >= GlobalConst.ADStarCntShare) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.times_limit.getId());
			return;
		}
		// 给东西
		HCHeroConfig hcHeroConfig = HCHeroManager.instance().get(id);
		if (hcHeroConfig.Visible == 0) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
			return;
		}
		player.handleEvent(EventTypeEnum.WatchAds);
		boolean useHeroSelfCount = count < GlobalConst.ADStarCnt;
		if (useHeroSelfCount) {
			heroItemTimesMap.add(id, 1);
		} else {
			heroModule.setFreeHcHeroItemTimes(freeHcHeroItemTimes + 1);
		}
		int itemCount = useHeroSelfCount?GlobalConst.ADStarPiece:GlobalConst.ADStarPieceShare;
		List<RewardInfo> resources = PlayerHelper.addResources(player, hcHeroConfig.ItemID, itemCount, OpType.HCHeroPieceAds);
		resp.addAllReward(resources);
		client.sendProtocol(resp.build());
	}

	private void starUp(NetClient client, Object message) {
		HCHeroStarUpRequest_26000003 req = (HCHeroStarUpRequest_26000003) message;
		HCHeroStarUpResponse_26000004.Builder resp = HCHeroStarUpResponse_26000004.newBuilder();
		long uid = Long.parseLong(req.getUid());
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		HCHeroModule heroModule = player.getHCHeroModule();
		HCHero hero = heroModule.get(uid);
		if (hero == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_data_not_found.getId());
			return;
		}
		int curStar = hero.getStar();
		int nextStar = hero.getStar() + 1;
		HCHeroArousalConfig curConfig = HCHeroArousalManager.instance().getUIHeroIDStar(hero.getConfigId(), curStar);
		HCHeroArousalConfig nextConfg = HCHeroArousalManager.instance().getUIHeroIDStar(hero.getConfigId(), nextStar);
		if (nextConfg == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.level_limit.getId());
			return;
		}
		PlayerHelper.delResources(player, curConfig.StarCost, OpType.HCHeroStarUp);
		hero.setStar(hero.getStar() + 1);

		client.sendProtocol(resp.build());
	}

	private void battle(NetClient client, Object message) {
		HCHeroBattleRequest_26000005 req = (HCHeroBattleRequest_26000005) message;
		HCHeroBattleResponse_26000006.Builder resp = HCHeroBattleResponse_26000006.newBuilder();
		long uid = Long.parseLong(req.getUid());
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		HCHeroModule heroModule = player.getHCHeroModule();
		HCHero hero = heroModule.get(uid);
		if (hero == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_data_not_found.getId());
			return;
		}
		heroModule.setHCHeroId(uid);
//		player.handleEvent(EventTypeEnum.HeroBattle, hero);
		client.sendProtocol(resp.build());
	}

	private void upLevel(NetClient client, Object message) {
		HCHeroUpLevelRequest_26000001 req = (HCHeroUpLevelRequest_26000001) message;
		HCHeroUpLevelResponse_26000002.Builder resp = HCHeroUpLevelResponse_26000002.newBuilder();
		long uid = Long.parseLong(req.getUid());
		boolean ads = req.getAds();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
//		if (!player.isFuncOpen(InitialUI.CardLv)) {
//			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
//			return;
//		}

		HCHeroModule heroModule = player.getHCHeroModule();
		HCHero hero = heroModule.get(uid);
		if (hero == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
			return;
		}
		HCHeroArousalConfig curConfig = HCHeroArousalManager.instance().getUIHeroIDStar(hero.getConfigId(), hero.getStar());
		int maxLevel = curConfig.LevelMax;
		if (hero.getLevel() >= maxLevel) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.hero_level_max.getId());
			return;
		}
		if (ads) {
			int freeHcHeroUpTimes = heroModule.getFreeHcHeroUpTimes();
			if (freeHcHeroUpTimes >= GlobalConst.ADUpgradCnt) {
				client.sendProtocol(resp.build(), ErrorMsgEnum.times_limit.getId());
				return;
			}
			heroModule.setFreeHcHeroUpTimes(freeHcHeroUpTimes + 1);
		} else {
			HCHeroConfig hcHeroConfig = HCHeroManager.instance().get(hero.getConfigId());
			HCHeroUpgradeConfig upgradeConfig = HCHeroUpgradeManager.instance().getUIQualityLv(hcHeroConfig.Quality, hero.getLevel());
			PlayerHelper.delResources(player, upgradeConfig.UpgradeCost, OpType.HCHeroLvUp);
		}

		hero.setLevel(hero.getLevel() + 1);
//		player.handleEvent(EventTypeEnum.HeroLevelUp, hero);
		client.sendProtocol(resp.build());
	}
}

