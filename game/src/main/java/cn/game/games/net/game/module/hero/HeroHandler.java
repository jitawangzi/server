package cn.game.games.net.game.module.hero;

import java.util.List;
import java.util.Map.Entry;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Hero;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.config.HeroConflateConfig;
import cn.game.protocol.generated.manager.HeroConflateManager;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.ResourceConsumeEnum;
import cn.game.protocol.protobuf.HeroMsg.HeroConflateRequest_16000003;
import cn.game.protocol.protobuf.HeroMsg.HeroConflateResponse_16000004;
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
	}

	private void conflate(NetClient client, Object message) {
		HeroConflateRequest_16000003 req = (HeroConflateRequest_16000003) message;
		HeroConflateResponse_16000004.Builder resp = HeroConflateResponse_16000004.newBuilder();
		long uid = req.getUid();
		List<Long> consumedUidList = req.getConsumedUidList();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		HeroModule heroModule = player.getHeroModule();
		Hero hero = heroModule.get(uid);
		if (hero == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
			return;
		}
		HeroConfig heroConfig = HeroManager.instance().get(hero.getConfigId());
		HeroConflateConfig heroConflateConfig = HeroConflateManager.instance().get(heroConfig.ID);
		// 检查是否有对应的耗材等。
		int powerfulCostIdCount = 0;
		boolean check = true;
		for (int i = 0; i < consumedUidList.size(); i++) {
			long consumedUid = consumedUidList.get(i);
			if (consumedUid == 0) {
				powerfulCostIdCount++;
				continue;
			}
			Hero consumedHero = heroModule.get(consumedUid);
			HeroConfig consumedHeroConfig = HeroManager.instance().get(consumedHero.getConfigId());
			int costType = 0;
			int[] costParam = null;
			if (i == 0) {
				costType = heroConflateConfig.costType1;
				costParam = heroConflateConfig.costParam1;
			} else if (i == 1) {
				costType = heroConflateConfig.costType2;
				costParam = heroConflateConfig.costParam2;
			} else if (i == 2) {
				costType = heroConflateConfig.costType3;
				costParam = heroConflateConfig.costParam3;
			} else {
				throw new IllegalArgumentException("合成数量不对了 ： " + i);
			}

			if (costType == 1) {
				if (consumedHeroConfig.quality != costParam[0] || consumedHeroConfig.Career != costParam[1]) {
					check = false;
					break;
				}
			} else if (costType == 2) {
				if (consumedHeroConfig.ID != costParam[0]) {
					check = false;
					break;
				}
			}
		}
		if (!check) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
			return;
		}
		// 万能id TODO
		int powerfulCostId = 1;
		if (!PlayerHelper.delResources(player.getPlayerId(), powerfulCostId, powerfulCostIdCount,
				ResourceConsumeEnum.HeroConflate)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
			return;
		}
		// 扣除耗材
		for (Long consumedUid : consumedUidList) {
			if (consumedUid == 0) {
				continue;
			}
			heroModule.del(consumedUid, ResourceConsumeEnum.HeroConflate);
		}
		// 合成

		if (heroConflateConfig.type == 1) {
			hero.setConfigId(heroConflateConfig.target[0]);

		} else {
			throw new IllegalArgumentException("合成类型还没支持: " + heroConflateConfig.type);
		}
		hero.setLevel(1);
		hero.setStar(0);
		hero.update();
		resp.setHero(hero.toHeroInfo());
		client.sendProtocol(resp.build());
	}
	private void upLevel(NetClient client, Object message) {
		HeroUpLevelRequest_16000001 req = (HeroUpLevelRequest_16000001) message;
		HeroUpLevelResponse_16000002.Builder resp = HeroUpLevelResponse_16000002.newBuilder();
		long uid = req.getUid();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		HeroModule heroModule = player.getHeroModule();
		Hero hero = heroModule.get(uid);
		if (hero == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
			return;
		}
		HeroConfig heroConfig = HeroManager.instance().get(hero.getConfigId()); 
		if (hero.getLevel() >= heroConfig.levelMax) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.hero_level_max.getId());
			return;
		}

		List<Entry<Integer, Integer>> cost = HeroHelper.calcUpLevelCost(hero.getLevel(), heroConfig.Career);

		boolean delResources = PlayerHelper.delResources(player.getPlayerId(), cost, ResourceConsumeEnum.HeroLevelUp);
		if (!delResources) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.resource_not_enough.getId());
			return;
		}
		hero.setLevel(hero.getLevel() + 1);
		hero.update();

		client.sendProtocol(resp.build());
	}
}

