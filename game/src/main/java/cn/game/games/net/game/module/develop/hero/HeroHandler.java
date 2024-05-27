package cn.game.games.net.game.module.develop.hero;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Hero;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.HeroBreakConfig;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.config.HeroLvConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.HeroBreakManager;
import cn.game.protocol.generated.manager.HeroLvManager;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BaseMsg.ItemInfo;
import cn.game.protocol.protobuf.HeroMsg.HeroBattleRequest_16000005;
import cn.game.protocol.protobuf.HeroMsg.HeroBattleResponse_16000006;
import cn.game.protocol.protobuf.HeroMsg.HeroConflateRequest_16000003;
import cn.game.protocol.protobuf.HeroMsg.HeroConflateResponse_16000004;
import cn.game.protocol.protobuf.HeroMsg.HeroFreeDayRentRequest_16000030;
import cn.game.protocol.protobuf.HeroMsg.HeroFreeDayRentResponse_16000031;
import cn.game.protocol.protobuf.HeroMsg.HeroLevelResetRequest_16000007;
import cn.game.protocol.protobuf.HeroMsg.HeroLevelResetResponse_16000008;
import cn.game.protocol.protobuf.HeroMsg.HeroQualityResetRequest_16000011;
import cn.game.protocol.protobuf.HeroMsg.HeroQualityResetResponse_16000012;
import cn.game.protocol.protobuf.HeroMsg.HeroUpLevelBatchRequest_16000023;
import cn.game.protocol.protobuf.HeroMsg.HeroUpLevelBatchResponse_16000024;
import cn.game.protocol.protobuf.HeroMsg.HeroUpLevelMaxRequest_16000021;
import cn.game.protocol.protobuf.HeroMsg.HeroUpLevelMaxResponse_16000022;
import cn.game.protocol.protobuf.HeroMsg.HeroUpLevelRequest_16000001;
import cn.game.protocol.protobuf.HeroMsg.HeroUpLevelResponse_16000002;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

@Component
public class HeroHandler extends BaseHandler {

	@Override
	protected int getModule() {
		return 0x16;
	}

	@Override
	protected void inititialize() {

		putInvoker(PbProtocol.HeroUpLevelRequest_16000001, this::upLevel);
		putInvoker(PbProtocol.HeroUpLevelMaxRequest_16000021, this::upLevelMax);
		putInvoker(PbProtocol.HeroUpLevelBatchRequest_16000023, this::upLevelBatch);
		putInvoker(PbProtocol.HeroConflateRequest_16000003, this::conflate);
		putInvoker(PbProtocol.HeroBattleRequest_16000005, this::battle);
		putInvoker(PbProtocol.HeroLevelResetRequest_16000007, this::levelReset);
		putInvoker(PbProtocol.HeroQualityResetRequest_16000011, this::qualityReset);
		putInvoker(PbProtocol.HeroFreeDayRentRequest_16000030, this::freeDayRent);
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

	private void freeDayRent(NetClient client, Object message) {
		HeroFreeDayRentRequest_16000030 req = (HeroFreeDayRentRequest_16000030) message;
		HeroFreeDayRentResponse_16000031.Builder resp = HeroFreeDayRentResponse_16000031.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		HeroModule heroModule = player.getHeroModule();
		List<Long> freeDayHeros = heroModule.getFreeDayHeros();
		if (!freeDayHeros.isEmpty()) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.repeat_request.getId());
			return;
		}
		heroModule.refreshFreeDayHero();
		for (Long uid : freeDayHeros) {
			resp.addHeros(heroModule.get(uid).toHeroInfo());
		}
		client.sendProtocol(resp.build());
	}

	private void upLevelBatch(NetClient client, Object message) {
		HeroUpLevelBatchRequest_16000023 req = (HeroUpLevelBatchRequest_16000023) message;
		HeroUpLevelBatchResponse_16000024.Builder resp = HeroUpLevelBatchResponse_16000024.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		if (!player.isFuncOpen(InitialUI.CardLv)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}

		HeroModule heroModule = player.getHeroModule();
		Collection<Hero> heros = heroModule.list();
		if (heros == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
			return;
		}
		int itemId = GlobalConst.HeroLvItem;
		int itemCount = 0;
		int moneyId = Asset.gold.ID;
		int moneyCount = 0;

		Set<Hero> updateHeros = new HashSet<Hero>();
		int loopCount = 0;
//		int upCount = 0;
		loop: while (true) {
			if (loopCount >= 10000) {
				throw new RuntimeException("maybe infinite loop，loopCount: " + loopCount);
			}
			for (Hero hero : heros) {
				int heroMaxLevel = HeroHelper.getHeroMaxLevel(hero);
				int curLevel = hero.getLevel();
				if (HeroHelper.isAllHeroMaxLevel(heros)) {
					break loop;
				}
				if (curLevel >= heroMaxLevel) {
					continue;
				}
				HeroLvConfig heroLvConfig = HeroLvManager.instance().get(curLevel);
				if (!player.isEnough(itemId, itemCount + heroLvConfig.LvConsumeItem) || !player.isEnough(moneyId, moneyCount + heroLvConfig.LvConsumeMoney)) {
					break loop;
				}
				heroLvConfig = HeroLvManager.instance().getNullable(curLevel + 1);
				if (heroLvConfig == null) {
					continue;
				}
				itemCount += heroLvConfig.LvConsumeItem;
				moneyCount += heroLvConfig.LvConsumeMoney;
				hero.setLevel(curLevel + 1);
				updateHeros.add(hero);
//				upCount++;
				player.handleEvent(EventTypeEnum.HeroLevelUp, hero);
			}
			loopCount++;
		}

		List<Entry<Integer, Integer>> deleteItems = new ArrayList<>(2);
		deleteItems.add(new AbstractMap.SimpleEntry(moneyId, moneyCount));
		deleteItems.add(new AbstractMap.SimpleEntry(itemId, itemCount));
		PlayerHelper.delResources(player, deleteItems, OpType.HeroLevelUp);

		for (Hero entry : updateHeros) {
			resp.addHeros(entry.toHeroLevelInfo());
		}
//		if (upCount > 0) {
//			player.handleEvent(EventTypeEnum.HeroLevelUp, upCount);
//		}
		client.sendProtocol(resp.build());
	}
	private void upLevelMax(NetClient client, Object message) {
		HeroUpLevelMaxRequest_16000021 req = (HeroUpLevelMaxRequest_16000021) message;
		HeroUpLevelMaxResponse_16000022.Builder resp = HeroUpLevelMaxResponse_16000022.newBuilder();
		long uid = Long.parseLong(req.getUid());
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		if (!player.isFuncOpen(InitialUI.CardLv)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}

		HeroModule heroModule = player.getHeroModule();
		Hero hero = heroModule.get(uid);
		if (hero == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
			return;
		}
		int heroMaxLevel = HeroHelper.getHeroMaxLevel(hero);
		int curLevel = hero.getLevel();
		int maxLevel = curLevel;
		int itemId = GlobalConst.HeroLvItem;
		int itemCount = 0;
		int moneyId = Asset.gold.ID;
		int moneyCount = 0;
		for (int level = curLevel;; level++) {
			if (maxLevel >= heroMaxLevel) {
				break;
			}
			HeroLvConfig heroLvConfig = HeroLvManager.instance().get(curLevel);
			if (!player.isEnough(itemId, itemCount + heroLvConfig.LvConsumeItem) || !player.isEnough(moneyId, moneyCount + heroLvConfig.LvConsumeMoney)) {
				break;
			}
			heroLvConfig = HeroLvManager.instance().getNullable(level + 1);
			if (heroLvConfig == null) {
				break;
			}
			itemCount += heroLvConfig.LvConsumeItem;
			moneyCount += heroLvConfig.LvConsumeMoney;
			maxLevel = level + 1;
			player.handleEvent(EventTypeEnum.HeroLevelUp, hero);

		}
		if (maxLevel != curLevel) {
			hero.setLevel(maxLevel);
			List<Entry<Integer, Integer>> deleteItems = new ArrayList<>(2);
			deleteItems.add(new AbstractMap.SimpleEntry(moneyId,moneyCount)) ; 
			deleteItems.add(new AbstractMap.SimpleEntry(itemId, itemCount));
			PlayerHelper.delResources(player, deleteItems, OpType.HeroLevelUp);
		}

		resp.setLevel(maxLevel);
		client.sendProtocol(resp.build());
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
		int level = hero.getLevel();

		int itemCount = 0;
		int money = 0;
		HeroLvConfig heroLvConfig;
		for (int i = 1; i < level; i++) {
			heroLvConfig = HeroLvManager.instance().get(i);
			itemCount += heroLvConfig.LvConsumeItem;
			money += heroLvConfig.LvConsumeMoney;
		}
		PlayerHelper.addResources(player, GlobalConst.HeroLvItem, itemCount, OpType.HeroLvReset);
		PlayerHelper.addResources(player, Asset.gold.ID, money, OpType.HeroLvReset);

		hero.setLevel(1);
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
		HeroConfig heroConfig = HeroManager.instance().get(hero.getConfigId());
		int quality = hero.getQuality();
		int star = hero.getStar();
		int sameIdHeros = 0;

		Map<Integer, Integer> itemsMap = new HashMap<Integer, Integer>();
		for (int i = 1; i < star; i++) {
			HeroBreakConfig breakConfig = HeroBreakManager.instance().getUIInitialQualityStar(quality, i);
			sameIdHeros += breakConfig.SameConsumeNum;
			
			int omniItemID = getOmniItemID(heroConfig, breakConfig); 
			int itemCount = breakConfig.CareerConsumeNum ; 
			itemsMap.compute(omniItemID, (k, v) -> v == null ? itemCount : v + itemCount);
		}
//		List<HeroInfo> heroInfos = new ArrayList<>();
//		List<RewardInfo> resources = PlayerHelper.addResources(player, heroConfig.ID, sameIdHeros, OpType.HeroQualityReset);
//		for (RewardInfo rewardInfo : resources) {
//			heroInfos.add(rewardInfo.getRole());
//		}
		List<ItemInfo> itemInfos = new ArrayList<>();
		for (Entry<Integer, Integer> entry : itemsMap.entrySet()) {
			List<RewardInfo> resources2 = PlayerHelper.addResources(player, entry.getKey(), entry.getValue(), OpType.HeroQualityReset);
			for (RewardInfo rewardInfo : resources2) {
				itemInfos.add(rewardInfo.getItem());
			}
		}
		hero.setStar(1);
//		resp.addAllHeros(heroInfos);
		resp.addAllItems(itemInfos);

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
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_data_not_found.getId());
			return;
		}
		Set<Long> battleHeros = heroModule.getBattleHeros();
		if (battleHeros.contains(uid)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.repeat_request.getId());
			return;
		}
		// 日租卡检查。
		List<Long> freeDayHeros = heroModule.getFreeDayHeros();
		if (freeDayHeros.contains(uid)) {
			for (Long bid : battleHeros) {
				if (freeDayHeros.contains(bid)) {
					client.sendProtocol(resp.build(), ErrorMsgEnum.hero_day_rent_max.getId());
					return;
				}
			}
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
		player.handleEvent(EventTypeEnum.HeroBattle, hero);
		battleHeros.add(uid);
		client.sendProtocol(resp.build());
	}
	private void conflate(NetClient client, Object message) {
		HeroConflateRequest_16000003 req = (HeroConflateRequest_16000003) message;
		HeroConflateResponse_16000004.Builder resp = HeroConflateResponse_16000004.newBuilder();
		String uid = req.getUid();
		List<String> consumedUidList = req.getConsumedUidList();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		if (!player.isFuncOpen(InitialUI.CardBreak)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}

		HeroModule heroModule = player.getHeroModule();
		Hero hero = heroModule.get(Long.parseLong(uid));
		if (hero == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
			return;
		}
		for (String string : consumedUidList) {
			if (string.equals(uid)) {
				client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
				return;
			}
		}

		HeroConfig heroConfig = HeroManager.instance().get(hero.getConfigId());
		// 先检查能不能往下突破
		HeroBreakConfig nextQualityStarConfig = HeroBreakManager.instance().getUIInitialQualityStar(hero.getQuality(), hero.getStar() + 1);
		if (nextQualityStarConfig == null) {
			nextQualityStarConfig = HeroBreakManager.instance().getUIInitialQualityStar(hero.getQuality() + 1, 1);
		}
		if (nextQualityStarConfig == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.hero_break_max.getId());
			return;
		}
		if (nextQualityStarConfig.InitialQuality > heroConfig.BreakQuality) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.hero_break_max.getId());
			return;
		}
		// 检查资源
		HeroBreakConfig qualityStarConfig = HeroBreakManager.instance().getUIInitialQualityStar(hero.getQuality(), hero.getStar());

//		HeroQualityConfig heroQualityConfig = HeroQualityManager.instance().get(heroConfig.Quality);
//		HeroConflateConfig heroConflateConfig = HeroConflateManager.instance().get(heroConfig.ID);
		boolean check = checkStarConsume(player, hero, consumedUidList, qualityStarConfig);
		if (!check) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
			return;
		}
		for (String string : consumedUidList) {
			player.getHeroModule().del(Long.parseLong(string), OpType.HeroConflate);
		}
		hero.setStar(nextQualityStarConfig.Star);
		hero.setQuality(nextQualityStarConfig.InitialQuality);

		// 英雄突破，奖励固定元宝
		PlayerHelper.addResources(player, Asset.gold.ID, GlobalConst.HeroBookAward, OpType.HeroConflate);

		player.handleEvent(EventTypeEnum.HeroBreak);

		resp.setHero(hero.toHeroInfo());
		client.sendProtocol(resp.build());
	}

	private boolean checkStarConsume(Player player, Hero hero, List<String> consumedUidList, HeroBreakConfig qualityStarConfig) {

		HeroModule heroModule = player.getHeroModule();
		HeroConfig heroConfig = HeroManager.instance().get(hero.getConfigId());
//		HeroSourceConfig heroSourceConfig = HeroSourceManager.instance().get(heroConfig.HeroSourceID);
//		HeroQualityConfig heroQualityConfig = HeroQualityManager.instance().get(heroConfig.Quality);
		int OmniItemID = getOmniItemID(heroConfig, qualityStarConfig);
		int needHeroCount = qualityStarConfig.CareerConsumeNum + qualityStarConfig.SameConsumeNum;
		int OmniItemCount = needHeroCount - consumedUidList.size();
		boolean isItemEnough = player.isEnough(OmniItemID, OmniItemCount);
		if (!isItemEnough) {
			return false;
		}

		List<Long> sameHerosUsedList = new ArrayList<>();
		if (qualityStarConfig.SameConsumeNum > 0) {
			Collection<Hero> sameHeros = heroModule.getByConfigId(hero.getConfigId());
			if (sameHeros.size() < qualityStarConfig.SameConsumeNum) {
				return false;
			}
			for (String uid : consumedUidList) {
				Hero hero2 = heroModule.get(Long.parseLong(uid));
				if (hero2 == null) {
					return false;
				}
				if (hero2.getConfigId() == hero.getConfigId()) {
					sameHerosUsedList.add(hero2.getId());
					if (sameHerosUsedList.size() == qualityStarConfig.SameConsumeNum) {
						break;
					}
				}
			}
			if (sameHerosUsedList.size() != qualityStarConfig.SameConsumeNum) {
				return false;
			}
		}
		if (qualityStarConfig.CareerConsumeNum > 0) {
			// 检查同职业的卡时，需要先排除已经当做同名卡的
//			int sameCareerCount = 0;
			for (String uid : consumedUidList) {
				if (sameHerosUsedList.contains(Long.parseLong(uid))) {
					continue;
				}
				Hero hero2 = heroModule.get(Long.parseLong(uid));
				HeroConfig heroConfig2 = HeroManager.instance().get(hero2.getConfigId());
				if (heroConfig2.Career != heroConfig.Career) {
					return false;
				}
			}
		}

		// 在这里先把万能耗材扣了,之后只扣卡
		PlayerHelper.delResources(player, OmniItemID, OmniItemCount, null, false);
		return true;
	}

	private int getOmniItemID(HeroConfig heroConfig, HeroBreakConfig qualityStarConfig) {
		return qualityStarConfig.RebirthReturnItem[heroConfig.Career - 1];
	}

	private void upLevel(NetClient client, Object message) {
		HeroUpLevelRequest_16000001 req = (HeroUpLevelRequest_16000001) message;
		HeroUpLevelResponse_16000002.Builder resp = HeroUpLevelResponse_16000002.newBuilder();
		long uid = Long.parseLong(req.getUid());
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		if (!player.isFuncOpen(InitialUI.CardLv)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}

		HeroModule heroModule = player.getHeroModule();
		Hero hero = heroModule.get(uid);
		if (hero == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
			return;
		}
		if (hero.getLevel() >= HeroHelper.getHeroMaxLevel(hero)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.hero_level_max.getId());
			return;
		}

		HeroLvConfig nextConfig = HeroLvManager.instance().getNullable(hero.getLevel() + 1);
		if (nextConfig == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.hero_level_max.getId());
			return;
		}
		HeroLvConfig curConfig = HeroLvManager.instance().getNullable(hero.getLevel());
		if (!player.isEnough(GlobalConst.HeroLvItem, curConfig.LvConsumeItem) || !player.isEnough(Asset.gold.ID, curConfig.LvConsumeMoney)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.resource_not_enough.getId());
			return;
		}

		PlayerHelper.delResources(player, GlobalConst.HeroLvItem, curConfig.LvConsumeItem, OpType.HeroLevelUp);
		PlayerHelper.delResources(player, Asset.gold.ID, curConfig.LvConsumeMoney, OpType.HeroLevelUp);
		hero.setLevel(hero.getLevel() + 1);
//		hero.update();
		player.handleEvent(EventTypeEnum.HeroLevelUp, hero);

		client.sendProtocol(resp.build());
	}
}

