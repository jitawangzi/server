package cn.game.games.net.game.module.develop.hero;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Hero;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.config.HeroQualityConfig;
import cn.game.protocol.generated.config.HeroSourceConfig;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.generated.manager.HeroQualityManager;
import cn.game.protocol.generated.manager.HeroSourceManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.ResourceConsumeEnum;
import cn.game.protocol.protobuf.HeroMsg.HeroBattleRequest_16000005;
import cn.game.protocol.protobuf.HeroMsg.HeroBattleResponse_16000006;
import cn.game.protocol.protobuf.HeroMsg.HeroConflateRequest_16000003;
import cn.game.protocol.protobuf.HeroMsg.HeroConflateResponse_16000004;
import cn.game.protocol.protobuf.HeroMsg.HeroLevelResetRequest_16000007;
import cn.game.protocol.protobuf.HeroMsg.HeroLevelResetResponse_16000008;
import cn.game.protocol.protobuf.HeroMsg.HeroQualityResetRequest_16000011;
import cn.game.protocol.protobuf.HeroMsg.HeroQualityResetResponse_16000012;
import cn.game.protocol.protobuf.HeroMsg.HeroUpLevelRequest_16000001;
import cn.game.protocol.protobuf.HeroMsg.HeroUpLevelResponse_16000002;
import cn.game.protocol.protobuf.PbProtocol;

@Component
public class HeroHandler extends BaseHandler {

	@Override
	protected int getModule() {
		return 0x16;
	}

	@Override
	protected void inititialize() {

		putInvoker(PbProtocol.HeroUpLevelRequest_16000001, this::upLevel);
		putInvoker(PbProtocol.HeroConflateRequest_16000003, this::conflate);
		putInvoker(PbProtocol.HeroBattleRequest_16000005, this::battle);
		putInvoker(PbProtocol.HeroLevelResetRequest_16000007, this::levelReset);
		putInvoker(PbProtocol.HeroQualityResetRequest_16000011, this::qualityReset);
	}

	private void levelReset(NetClient client, Object message) {
		HeroLevelResetRequest_16000007 req = (HeroLevelResetRequest_16000007) message;
		HeroLevelResetResponse_16000008.Builder resp = HeroLevelResetResponse_16000008.newBuilder();
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

	private void qualityReset(NetClient client, Object message) {
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

	private void battle(NetClient client, Object message) {
		HeroBattleRequest_16000005 req = (HeroBattleRequest_16000005) message;
		HeroBattleResponse_16000006.Builder resp = HeroBattleResponse_16000006.newBuilder();
		long uid = Long.parseLong(req.getUid());
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		HeroModule heroModule = player.getHeroModule();
		Hero hero = heroModule.get(uid);
		if (hero == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
			return;
		}
		Set<Long> battleHeros = heroModule.getBattleHeros();
		if (battleHeros.contains(uid)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
			return;
		}
		// 有没有同职业的在阵上
		int career = HeroHelper.getCareer(hero.getConfigId());
		Hero replaceHero = null;
		for (Long id : battleHeros) {
			Hero tmp = heroModule.get(id);
			if (career == HeroHelper.getCareer(tmp.getConfigId())) {
				replaceHero = tmp;
				break;
			}
		}
		if (replaceHero != null) {
			battleHeros.remove(replaceHero.getId());
		}
		battleHeros.add(uid);
		client.sendProtocol(resp.build());
	}
	private void conflate(NetClient client, Object message) {
		HeroConflateRequest_16000003 req = (HeroConflateRequest_16000003) message;
		HeroConflateResponse_16000004.Builder resp = HeroConflateResponse_16000004.newBuilder();
		String uid = req.getUid();
		List<String> consumedUidList = req.getConsumedUidList();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		HeroModule heroModule = player.getHeroModule();
		Hero hero = heroModule.get(Long.parseLong(uid));
		if (hero == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
			return;
		}
//		先检查是升星还是突破。 
		HeroConfig heroConfig = HeroManager.instance().get(hero.getConfigId());
		HeroQualityConfig heroQualityConfig = HeroQualityManager.instance().get(heroConfig.Quality);
//		HeroConflateConfig heroConflateConfig = HeroConflateManager.instance().get(heroConfig.ID);

		// 普通升星
		boolean isStarUp = hero.getStar() < heroQualityConfig.StarMax;

		boolean check = checkStarConsume(player, hero, consumedUidList, isStarUp);
		if (!check) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
			return;
		}
		for (String string : consumedUidList) {
			player.getHeroModule().del(Long.parseLong(string), ResourceConsumeEnum.HeroConflate);
		}
		if (isStarUp) {
			hero.setStar(hero.getStar() + 1);
		}else {
			hero.setConfigId(heroConfig.PromoteTargetID);
			hero.setStar(0);
		}
		resp.setHero(hero.toHeroInfo());
		client.sendProtocol(resp.build());
	}

	private boolean checkStarConsume(Player player, Hero hero, List<String> consumedUidList, boolean isStarUp) {

		HeroConfig heroConfig = HeroManager.instance().get(hero.getConfigId());
//		HeroSourceConfig heroSourceConfig = HeroSourceManager.instance().get(heroConfig.HeroSourceID);
//		HeroQualityConfig heroQualityConfig = HeroQualityManager.instance().get(heroConfig.Quality);
		int OmniItemID = getOmniItemID(heroConfig, isStarUp);
		int OmniItemCount = 0;
		int[][] consume = null;
		if (isStarUp) {
			consume = heroConfig.StarPromoteConsume;
		} else {
			consume = heroConfig.QualityPromoteConsume;
		}

		int[][] starPromoteConsume = consume;
		for (int i = 0; i < starPromoteConsume.length; i++) {
			int type = starPromoteConsume[i][0];
			if (type == 1) { // 按职业和品质扣英雄
				int Career = starPromoteConsume[i][1];
				int quality = starPromoteConsume[i][2];
				int count = starPromoteConsume[i][3];

				if (consumedUidList.size() > count) {
					return false;
				}

				OmniItemCount = count - consumedUidList.size();
				if (OmniItemCount > 0) {
					if (OmniItemID > 0) {
						boolean isItemEnough = player.isEnough(OmniItemID, OmniItemCount);
						if (!isItemEnough) {
							return false;
						}
					} else {
						return false;
					}
				}
				for (String uid : consumedUidList) {
					Hero hero2 = player.getHeroModule().get(Long.parseLong(uid));
					if (hero2 == null) {
						return false;
					}
					HeroConfig heroConfig2 = HeroManager.instance().get(hero2.getConfigId());
					HeroSourceConfig heroSourceConfig2 = HeroSourceManager.instance().get(heroConfig2.HeroSourceID);
					HeroQualityConfig heroQualityConfig2 = HeroQualityManager.instance().get(heroConfig2.Quality);
					if (heroSourceConfig2.Career != Career || heroQualityConfig2.quality != quality) {
						return false;
					}
				}

			} else if (type == 2) {
				// 按英雄id扣除英雄
				int heroId = starPromoteConsume[i][1];
				int count = starPromoteConsume[i][2];

				if (consumedUidList.size() > count) {
					return false;
				}

				OmniItemCount = count - consumedUidList.size();
				if (OmniItemCount > 0) {
					if (OmniItemID > 0) {
						boolean isItemEnough = player.isEnough(OmniItemID, OmniItemCount);
						if (!isItemEnough) {
							return false;
						}
					} else {
						return false;
					}
				}
				for (String uid : consumedUidList) {
					Hero hero2 = player.getHeroModule().get(Long.parseLong(uid));
					if (hero2 == null) {
						return false;
					}
					HeroConfig heroConfig2 = HeroManager.instance().get(hero2.getConfigId());
					if (heroConfig2.ID != heroId) {
						return false;
					}
				}
			}
		}

		// 在这里先把万能耗材扣了
		PlayerHelper.delResources(player, OmniItemID, OmniItemCount, null);
		return true;
	}

	private int getOmniItemID(HeroConfig heroConfig, boolean isStarUp) {
		if (isStarUp) {
			return heroConfig.StarOmniItemID;
		} else {
			return heroConfig.QualityOmniItemID;
		}
	}

	private void upLevel(NetClient client, Object message) {
		HeroUpLevelRequest_16000001 req = (HeroUpLevelRequest_16000001) message;
		HeroUpLevelResponse_16000002.Builder resp = HeroUpLevelResponse_16000002.newBuilder();
		long uid = Long.parseLong(req.getUid());
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		HeroModule heroModule = player.getHeroModule();
		Hero hero = heroModule.get(uid);
		if (hero == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
			return;
		}
		HeroConfig heroConfig = HeroManager.instance().get(hero.getConfigId()); 
		HeroQualityConfig heroQualityConfig = HeroQualityManager.instance().get(heroConfig.Quality);
		if (hero.getLevel() >= heroQualityConfig.LevelMax) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.hero_level_max.getId());
			return;
		}
		HeroSourceConfig heroSourceConfig = HeroSourceManager.instance().get(heroConfig.HeroSourceID);
		List<Entry<Integer, Integer>> cost = HeroHelper.calcUpLevelCost(hero.getLevel(), heroSourceConfig.Career);

		boolean delResources = PlayerHelper.delResources(player, cost, ResourceConsumeEnum.HeroLevelUp);
		if (!delResources) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.resource_not_enough.getId());
			return;
		}
		hero.setLevel(hero.getLevel() + 1);
//		hero.update();
		client.sendProtocol(resp.build());
	}
}

