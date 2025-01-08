package cn.game.games.net.game.module.develop.hero;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.google.protobuf.ProtocolStringList;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Hero;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.chat.ChatHelper;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.HeroBandBookConfig;
import cn.game.protocol.generated.config.HeroBreakConfig;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.config.HeroLvConfig;
import cn.game.protocol.generated.config.MarqueeConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.HeroBandBookManager;
import cn.game.protocol.generated.manager.HeroBreakManager;
import cn.game.protocol.generated.manager.HeroLvManager;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.generated.manager.MarqueeManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BaseMsg.QualityStar;
import cn.game.protocol.protobuf.HeroMsg.HeroBattleDismissRequest_16000009;
import cn.game.protocol.protobuf.HeroMsg.HeroBattleDismissResponse_1600000a;
import cn.game.protocol.protobuf.HeroMsg.HeroBattleRequest_16000005;
import cn.game.protocol.protobuf.HeroMsg.HeroBattleResponse_16000006;
import cn.game.protocol.protobuf.HeroMsg.HeroBattleUpLevelBatchRequest_16000025;
import cn.game.protocol.protobuf.HeroMsg.HeroBattleUpLevelBatchResponse_16000026;
import cn.game.protocol.protobuf.HeroMsg.HeroConflateInfo;
import cn.game.protocol.protobuf.HeroMsg.HeroConflateRequest_16000003;
import cn.game.protocol.protobuf.HeroMsg.HeroConflateResponse_16000004;
import cn.game.protocol.protobuf.HeroMsg.HeroDisassembleRequest_16000052;
import cn.game.protocol.protobuf.HeroMsg.HeroDisassembleResponse_16000053;
import cn.game.protocol.protobuf.HeroMsg.HeroFragmentComposeRequest_16000050;
import cn.game.protocol.protobuf.HeroMsg.HeroFragmentComposeResponse_16000051;
import cn.game.protocol.protobuf.HeroMsg.HeroFreeDayRentChooseRequest_16000032;
import cn.game.protocol.protobuf.HeroMsg.HeroFreeDayRentChooseResponse_16000033;
import cn.game.protocol.protobuf.HeroMsg.HeroFreeDayRentRequest_16000030;
import cn.game.protocol.protobuf.HeroMsg.HeroFreeDayRentResponse_16000031;
import cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsLevelRewardRequest_16000044;
import cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsLevelRewardResponse_16000045;
import cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsListResponse_16000041;
import cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsRewardRequest_16000042;
import cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsRewardResponse_16000043;
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
import cn.game.util.IntMapWrapper;

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
        putInvoker(PbProtocol.HeroFragmentComposeRequest_16000050, this::fragmentCompose);
        putInvoker(PbProtocol.HeroDisassembleRequest_16000052, this::disassemble);
        putInvoker(PbProtocol.HeroIllustrationsLevelRewardRequest_16000044, this::illustrationsLevelReward);
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

    private void fragmentCompose(NetClient client, Object message) {
        HeroFragmentComposeRequest_16000050 req = (HeroFragmentComposeRequest_16000050) message;
        HeroFragmentComposeResponse_16000051.Builder resp = HeroFragmentComposeResponse_16000051.newBuilder();
        List<Integer> heroIdList = req.getHeroIdList();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        for (Integer heroId : heroIdList) {
            HeroConfig heroConfig = HeroManager.instance().get(heroId);
            if (heroConfig == null) {
                client.sendProtocol(resp.build(), ErrorMsgEnum.config_data_not_found.getId());
                return;
            }
            Integer count = GlobalConst.HeroSynthesisDisassemble.get(heroConfig.InitialQuality);
            if (count == null) {
                client.sendProtocol(resp.build(), ErrorMsgEnum.config_data_not_found.getId());
                return;
            }
            if (!PlayerHelper.delResources(player, heroConfig.Fragment, count, OpType.HeroFragmentCompose)) {
                client.sendProtocol(resp.build(), ErrorMsgEnum.resource_not_enough.getId());
                return;
            }
            MarqueeConfig marqueeConfig = MarqueeManager.instance().get(HeroHelper.getMarqueeId(count));
            if (heroConfig.InitialQuality >= marqueeConfig.Para) {
                String marqueeText = ChatHelper.getHeroMarqueeText(player.getData().getName(), Arrays.asList(heroConfig.name), 1);
                ChatHelper.marquee(marqueeText, player.getServerId());
            }
            List<RewardInfo> resources = PlayerHelper.addResources(player, heroId, 1, OpType.HeroFragmentCompose);
            resp.addAllReward(resources);
        }
        client.sendProtocol(resp.build());
    }

    private void illustrationsReward(NetClient client, Object message) {
        HeroIllustrationsRewardRequest_16000042 req = (HeroIllustrationsRewardRequest_16000042) message;
        HeroIllustrationsRewardResponse_16000043.Builder resp = HeroIllustrationsRewardResponse_16000043.newBuilder();
        int heroId = req.getHeroId();
		HeroConfig heroConfig = HeroManager.instance().get(heroId);
		if (heroConfig == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.config_data_not_found.getId());
			return;
		}
		if (heroConfig.InitialQuality < 6) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
			return;
		}
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        HeroModule heroModule = player.getHeroModule();
		List<Hero> heros = (List<Hero>) heroModule.getByConfigId(heroId);
		if (heros.isEmpty()) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_data_not_found.getId());
			return;
		}
		Hero hero = heros.get(0);
		Map<Integer, QualityStarObj> starsMap = heroModule.getIllustrationsHeroStars();
		QualityStarObj qualityStarObj = starsMap.get(heroId);
		int maxStar = HeroHelper.getMaxStar(hero.getQuality());
		if (qualityStarObj == null) {
			qualityStarObj = new QualityStarObj();
			qualityStarObj.quality = heroConfig.InitialQuality;
			qualityStarObj.star = 1;
			starsMap.put(heroId, qualityStarObj);
		} else {
			if (hero.getQuality() == qualityStarObj.quality) {
				if (qualityStarObj.star >= maxStar) {
					client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
					return;
				}
				if (hero.getStar() <= qualityStarObj.star) {
					client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
					return;
				}
				qualityStarObj.star++;
			} else if (hero.getQuality() > qualityStarObj.quality) {
				if (qualityStarObj.star >= maxStar) {
					qualityStarObj.quality++;
					qualityStarObj.star = 1;
				} else {
					qualityStarObj.star++;
				}
			} else if (hero.getQuality() > qualityStarObj.quality) {
				qualityStarObj.quality++;
				qualityStarObj.star = 1;
			} else {
				client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
				return;
			}
		}
		List<RewardInfo> resources = PlayerHelper.addResources(player, Asset.CatalogPoints.ID, GlobalConst.HeroHandBookEXP,
				OpType.llustrationsReward);
        resp.addAllReward(resources);
        // 给奖励
        client.sendProtocol(resp.build());
    }

    private void illustrationsList(NetClient client, Object message) {
        HeroIllustrationsListResponse_16000041.Builder resp = HeroIllustrationsListResponse_16000041.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        HeroModule heroModule = player.getHeroModule();
		Map<Integer, QualityStarObj> heroStarsMap = heroModule.getIllustrationsHeroStars();
        resp.addAllHeroIds(heroModule.getOwnedHeroIds());
        heroStarsMap.forEach((k, v) -> {
			resp.addHeroStars(QualityStar.newBuilder().setHeroId(k).setQuality(v.quality).setStar(v.star));
		});
		resp.setRewardLevel(heroModule.getIllustrationRewardLevel());
        client.sendProtocol(resp.build());
    }

    private void freeDayRentChoose(NetClient client, Object message) {
        HeroFreeDayRentChooseRequest_16000032 req = (HeroFreeDayRentChooseRequest_16000032) message;
        HeroFreeDayRentChooseResponse_16000033.Builder resp = HeroFreeDayRentChooseResponse_16000033.newBuilder();
        long uid = Long.parseLong(req.getUid());
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        HeroModule heroModule = player.getHeroModule();
        Hero hero = heroModule.get(uid);
        if (hero == null) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
            return;
        }
        boolean contains = heroModule.getFreeDayHeros().contains(uid);
        if (!contains) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
            return;
        }
        heroModule.setFreeDayHeroUid(uid);
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

    // 一键给上阵的英雄升级。优先升等级最低的。
    private void upLevelBattleBatch(NetClient client, Object message) {
        HeroBattleUpLevelBatchRequest_16000025 req = (HeroBattleUpLevelBatchRequest_16000025) message;
        HeroBattleUpLevelBatchResponse_16000026.Builder resp = HeroBattleUpLevelBatchResponse_16000026.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.CardLv)) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
            return;
        }
        HeroModule heroModule = player.getHeroModule();
        List<Hero> heros = heroModule.getBattleHeroList();
        if (heros == null) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
            return;
        }
        Map<Long, Integer> heroOldCombatMap = new HashMap<>();
        Map<Long, Integer> heroOldLevelMap = new HashMap<>();
        heros.forEach(h -> {
            heroOldCombatMap.put(h.getId(), BattleHelper.calcHeroCombat(h));
            heroOldLevelMap.put(h.getId(), h.getLevel());
        });
        // 优先升等级最低的。如果等级相同，则升星级的。如果星级相同，则升品质高的。
        //		Collections.sort(heros, (o2, o1) -> {
        //			if (o1.getLevel() == o2.getLevel()) {
        //				if (o1.getQuality() == o2.getQuality()) {
        //					return o2.getStar() - o1.getStar();
        //				}
        //				return o2.getQuality() - o1.getQuality();
        //			}
        //			return o1.getLevel() - o2.getLevel();
        //		});
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
            //			if (HeroHelper.isAllHeroMaxLevel(heros)) {
            //				break loop;
            //			}
            // 优先升等级最低的。如果等级相同，则升星级的。如果星级相同，则升品质高的。
            Collections.sort(heros, (o1, o2) -> {
                if (o1.getLevel() == o2.getLevel()) {
                    if (o1.getQuality() == o2.getQuality()) {
                        return o2.getStar() - o1.getStar();
                    }
                    return o2.getQuality() - o1.getQuality();
                }
                return o1.getLevel() - o2.getLevel();
            });
            boolean isAllHeroMaxLevel = true;
            boolean isAllHeroItemNotEnough = true;
            for (Hero hero : heros) {
                int heroMaxLevel = HeroHelper.getHeroMaxLevel(hero);
                int curLevel = hero.getLevel();
                if (curLevel >= heroMaxLevel) {
                    continue;
                }
                HeroLvConfig heroLvConfig = HeroLvManager.instance().get(curLevel);
                if (!player.isEnough(itemId, itemCount + heroLvConfig.LvConsumeItem) || !player.isEnough(moneyId, moneyCount + heroLvConfig.LvConsumeMoney)) {
                    continue;
                }
                HeroLvConfig nextHeroLvConfig = HeroLvManager.instance().getNullable(curLevel + 1);
                if (nextHeroLvConfig == null) {
                    continue;
                }
                itemCount += heroLvConfig.LvConsumeItem;
                moneyCount += heroLvConfig.LvConsumeMoney;
                hero.setLevel(curLevel + 1);
                updateHeros.add(hero);
                //				upCount++;
                player.handleEvent(EventTypeEnum.HeroLevelUp, hero);
                isAllHeroMaxLevel = false;
                isAllHeroItemNotEnough = false;
                continue loop;
            }
            if (isAllHeroMaxLevel) {
                break;
            }
            if (isAllHeroItemNotEnough) {
                break;
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
        Map<Long, Integer> heroNewLevelMap = new HashMap<>();
        Map<Long, Integer> heroNewCombatMap = new HashMap<>();
        heros.forEach(h -> {
            heroNewCombatMap.put(h.getId(), BattleHelper.calcHeroCombat(h));
            heroNewLevelMap.put(h.getId(), h.getLevel());
        });
        heroNewLevelMap.forEach((uid, level) -> {
            if (heroOldLevelMap.containsKey(uid) && heroOldLevelMap.get(uid) != level) {
                GameLogger.heroraise(player, heroModule.get(uid), 1, level - heroOldLevelMap.get(uid), level, heroOldCombatMap.get(uid), heroNewCombatMap.get(uid));
            }
        });
        client.sendProtocol(resp.build());
    }

    /**
     * 挨个升级，升一级换下一个。
     * @param client
     * @param message
     */
    @Deprecated
    private void upLevelBattleBatch2(NetClient client, Object message) {
        HeroBattleUpLevelBatchRequest_16000025 req = (HeroBattleUpLevelBatchRequest_16000025) message;
        HeroBattleUpLevelBatchResponse_16000026.Builder resp = HeroBattleUpLevelBatchResponse_16000026.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.CardLv)) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
            return;
        }
        HeroModule heroModule = player.getHeroModule();
        List<Hero> heros = heroModule.getBattleHeroList();
        if (heros == null) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
            return;
        }
        Collections.sort(heros, (o2, o1) -> {
            if (o1.getQuality() == o2.getQuality()) {
                return o1.getStar() - o2.getStar();
            }
            return o1.getQuality() - o2.getQuality();
        });
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
            //			if (HeroHelper.isAllHeroMaxLevel(heros)) {
            //				break loop;
            //			}
            boolean isAllHeroMaxLevel = true;
            boolean isAllHeroItemNotEnough = true;
            for (Hero hero : heros) {
                int heroMaxLevel = HeroHelper.getHeroMaxLevel(hero);
                int curLevel = hero.getLevel();
                if (curLevel >= heroMaxLevel) {
                    continue;
                }
                HeroLvConfig heroLvConfig = HeroLvManager.instance().get(curLevel);
                if (!player.isEnough(itemId, itemCount + heroLvConfig.LvConsumeItem) || !player.isEnough(moneyId, moneyCount + heroLvConfig.LvConsumeMoney)) {
                    continue;
                }
                HeroLvConfig nextHeroLvConfig = HeroLvManager.instance().getNullable(curLevel + 1);
                if (nextHeroLvConfig == null) {
                    continue;
                }
                itemCount += heroLvConfig.LvConsumeItem;
                moneyCount += heroLvConfig.LvConsumeMoney;
                hero.setLevel(curLevel + 1);
                updateHeros.add(hero);
                //				upCount++;
                player.handleEvent(EventTypeEnum.HeroLevelUp, hero);
                isAllHeroMaxLevel = false;
                isAllHeroItemNotEnough = false;
            }
            if (isAllHeroMaxLevel) {
                break;
            }
            if (isAllHeroItemNotEnough) {
                break;
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

    @Deprecated
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
            //			if (HeroHelper.isAllHeroMaxLevel(heros)) {
            //				break loop;
            //			}
            boolean isAllHeroMaxLevel = true;
            for (Hero hero : heros) {
                int heroMaxLevel = HeroHelper.getHeroMaxLevel(hero);
                int curLevel = hero.getLevel();
                if (curLevel >= heroMaxLevel) {
                    continue;
                }
                HeroLvConfig heroLvConfig = HeroLvManager.instance().get(curLevel);
                if (!player.isEnough(itemId, itemCount + heroLvConfig.LvConsumeItem) || !player.isEnough(moneyId, moneyCount + heroLvConfig.LvConsumeMoney)) {
                    break loop;
                }
                HeroLvConfig nextHeroLvConfig = HeroLvManager.instance().getNullable(curLevel + 1);
                if (nextHeroLvConfig == null) {
                    continue;
                }
                itemCount += heroLvConfig.LvConsumeItem;
                moneyCount += heroLvConfig.LvConsumeMoney;
                hero.setLevel(curLevel + 1);
                updateHeros.add(hero);
                //				upCount++;
                player.handleEvent(EventTypeEnum.HeroLevelUp, hero);
                isAllHeroMaxLevel = false;
            }
            if (isAllHeroMaxLevel) {
                break;
            }
            loopCount++;
        }
        List<Entry<Integer, Integer>> deleteItems = new ArrayList<>(2);
        deleteItems.add(new AbstractMap.SimpleEntry(moneyId, moneyCount));
        deleteItems.add(new AbstractMap.SimpleEntry(itemId, itemCount));
        PlayerHelper.delResources(player, deleteItems, OpType.HeroLevelUp);
        //		player.updateOfflineAttrData();
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
        int upLevelMax = req.getMaxLevel();
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
        for (int level = curLevel; ; level++) {
            if (maxLevel >= heroMaxLevel || upLevelMax > 0 && maxLevel >= upLevelMax) {
                break;
            }
            HeroLvConfig heroLvConfig = HeroLvManager.instance().get(level);
            if (!player.isEnough(itemId, itemCount + heroLvConfig.LvConsumeItem) || !player.isEnough(moneyId, moneyCount + heroLvConfig.LvConsumeMoney)) {
                break;
            }
            HeroLvConfig nextHeroLvConfig = HeroLvManager.instance().getNullable(level + 1);
            if (nextHeroLvConfig == null) {
                break;
            }
            itemCount += heroLvConfig.LvConsumeItem;
            moneyCount += heroLvConfig.LvConsumeMoney;
            maxLevel = level + 1;
        }
        if (maxLevel != curLevel) {
            for (int i = curLevel + 1; i <= maxLevel; i++) {
                hero.setLevel(i);
                player.handleEvent(EventTypeEnum.HeroLevelUp, hero);
            }
            //			hero.setLevel(maxLevel);
            List<Entry<Integer, Integer>> deleteItems = new ArrayList<>(2);
            deleteItems.add(new AbstractMap.SimpleEntry(moneyId, moneyCount));
            deleteItems.add(new AbstractMap.SimpleEntry(itemId, itemCount));
            PlayerHelper.delResources(player, deleteItems, OpType.HeroLevelUp);
        }
        resp.setLevel(maxLevel);
        client.sendProtocol(resp.build());
    }

    private void levelReset(NetClient client, Object message) {
        HeroLevelResetRequest_16000007 req = (HeroLevelResetRequest_16000007) message;
        HeroLevelResetResponse_16000008.Builder resp = HeroLevelResetResponse_16000008.newBuilder();
        ProtocolStringList uidList = req.getUidList();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        for (String string : uidList) {
            long uid = Long.parseLong(string);
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
        }
        //		player.updateOfflineAttrData();
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
        int initialQuality = heroConfig.InitialQuality;
        // 返还的碎片
        IntMapWrapper itemsMap = new IntMapWrapper();
        for (; initialQuality <= hero.getQuality(); initialQuality++) {
            int starMax = initialQuality == hero.getQuality() ? hero.getStar() : 10;
            for (int star = 1; star < starMax; star++) {
                HeroBreakConfig breakConfig = HeroBreakManager.instance().getUIInitialQualityStar(initialQuality, star);
                if (breakConfig == null) {
                    break;
                }
                if (breakConfig.SameConsumeNum > 0) {
                    itemsMap.add(heroConfig.Fragment, breakConfig.SameConsumeNum);
                }
                if (breakConfig.CareerConsumeNum > 0) {
                    int OmniItemID = getOmniItemID(heroConfig, breakConfig);
                    itemsMap.add(OmniItemID, breakConfig.CareerConsumeNum);
                }
            }
        }
        List<RewardInfo> rewards = PlayerHelper.addResources(player, itemsMap.getMap(), OpType.HeroQualityReset);
        hero.setStar(1);
        hero.setQuality(heroConfig.InitialQuality);
        resp.setHero(hero.toHeroInfo());
        resp.addAllItems(rewards);
        client.sendProtocol(resp.build());
    }

    private void battleDismiss(NetClient client, Object message) {
        HeroBattleDismissRequest_16000009 req = (HeroBattleDismissRequest_16000009) message;
        HeroBattleDismissResponse_1600000a.Builder resp = HeroBattleDismissResponse_1600000a.newBuilder();
        long uid = Long.parseLong(req.getUid());
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        HeroModule heroModule = player.getHeroModule();
        Hero hero = heroModule.get(uid);
        if (hero == null) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.player_data_not_found.getId());
            return;
        }
        Map<Long, Integer> battleHeros = heroModule.getBattleHeros();
        Integer remove = battleHeros.remove(uid);
        if (remove != null) {
            player.handleEvent(EventTypeEnum.HeroBattleDismiss, hero);
        }
        client.sendProtocol(resp.build());
    }

    private void battle(NetClient client, Object message) {
        HeroBattleRequest_16000005 req = (HeroBattleRequest_16000005) message;
        HeroBattleResponse_16000006.Builder resp = HeroBattleResponse_16000006.newBuilder();
        long uid = Long.parseLong(req.getUid());
        int pos = req.getPos();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        HeroModule heroModule = player.getHeroModule();
        Hero hero = heroModule.get(uid);
        if (hero == null) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.player_data_not_found.getId());
            return;
        }
        if (pos < 1 || pos > 5) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
            return;
        }
        Map<Long, Integer> battleHeros = heroModule.getBattleHeros();
        //		if (battleHeros.containsKey(uid)) {
        //			client.sendProtocol(resp.build(), ErrorMsgEnum.repeat_request.getId());
        //			return;
        //		}
        // 日租卡检查。
        List<Long> freeDayHeros = heroModule.getFreeDayHeros();
        if (freeDayHeros.contains(uid)) {
            for (Long bid : battleHeros.keySet()) {
                if (freeDayHeros.contains(bid)) {
                    client.sendProtocol(resp.build(), ErrorMsgEnum.hero_day_rent_max.getId());
                    return;
                }
            }
        }
        // 有没有同职业的在阵上
        //		int career = HeroHelper.getCareer(hero.getConfigId());
        //		Hero replaceHero = null;
        //		for (Long id : battleHeros) {
        //			Hero tmp = heroModule.get(id);
        //			if (career == HeroHelper.getCareer(tmp.getConfigId())) {
        //				replaceHero = tmp;
        //				break;
        //			}
        //		}
        //		if (replaceHero != null) {
        //			battleHeros.remove(replaceHero.getId());
        //		}
        Iterator<Entry<Long, Integer>> iterator = battleHeros.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<java.lang.Long, java.lang.Integer> entry = (Map.Entry<java.lang.Long, java.lang.Integer>) iterator.next();
            if (entry.getValue() == pos) {
                iterator.remove();
            }
        }
        battleHeros.put(uid, pos);
        player.handleEvent(EventTypeEnum.HeroBattle, hero);
        client.sendProtocol(resp.build());
    }

    private void conflate(NetClient client, Object message) {
        HeroConflateRequest_16000003 req = (HeroConflateRequest_16000003) message;
        HeroConflateResponse_16000004.Builder resp = HeroConflateResponse_16000004.newBuilder();
        List<HeroConflateInfo> heroConflateInfoList = req.getHeroConflateInfoList();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.CardBreak)) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
            return;
        }
        for (HeroConflateInfo heroConflateInfo : heroConflateInfoList) {
            String uid = heroConflateInfo.getUid();
            //			List<String> consumedUidList = heroConflateInfo.getConsumedUidList();
            Map<Integer, Integer> sameProfessionItems = heroConflateInfo.getSameProfessionItemsMap();
            HeroModule heroModule = player.getHeroModule();
            Hero hero = heroModule.get(Long.parseLong(uid));
            if (hero == null) {
                client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
                return;
            }
            if (!PlayerHelper.isEnough(player, sameProfessionItems)) {
                client.sendProtocol(resp.build(), ErrorMsgEnum.resource_not_enough.getId());
                return;
            }
            int sameProfessionItemsCount = 0;
            for (Entry<Integer, Integer> entry : sameProfessionItems.entrySet()) {
                sameProfessionItemsCount += entry.getValue();
            }
            //			for (String string : consumedUidList) {
            //				if (string.equals(uid)) {
            //					client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
            //					return;
            //				}
            //			}
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
            if (sameProfessionItemsCount != qualityStarConfig.CareerConsumeNum) {
                client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
                return;
            }
            if (!PlayerHelper.isEnough(player, heroConfig.Fragment, qualityStarConfig.SameConsumeNum)) {
                client.sendProtocol(resp.build(), ErrorMsgEnum.resource_not_enough.getId());
                return;
            }
            PlayerHelper.delResources(player, heroConfig.Fragment, qualityStarConfig.SameConsumeNum, OpType.HeroConflate);
            PlayerHelper.delResources(player, sameProfessionItems, OpType.HeroConflate);
            // 扣除资源
            //			boolean check = checkStarConsume(player, hero, consumedUidList, qualityStarConfig);
            //			if (!check) {
            //				client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
            //				return;
            //			}
            //			for (String string : consumedUidList) {
            //				player.getHeroModule().del(Long.parseLong(string), OpType.HeroConflate);
            //			}
            int oldStar = hero.getStar();
            int oldQuality = hero.getQuality();
            int beforeCombat = 0;
            hero.setStar(nextQualityStarConfig.Star);
            hero.setQuality(nextQualityStarConfig.InitialQuality);
            int newStar = hero.getStar();
            int newQuality = hero.getQuality();
            int afterCombat = 0;
            if (oldStar != newStar) {
                GameLogger.heroraise(player, hero, 3, 1, newStar, beforeCombat, afterCombat);
            }
            if (oldQuality != newQuality) {
                GameLogger.heroraise(player, hero, 2, 1, newQuality, beforeCombat, afterCombat);
                player.handleEvent(EventTypeEnum.HeroQuality, hero);
            }
            player.handleEvent(EventTypeEnum.HeroBreak, hero.getStar(), hero.getQuality());
            resp.addHero(hero.toHeroInfo());
        }
        client.sendProtocol(resp.build());
    }

    @Deprecated
    private boolean checkStarConsume(Player player, Hero hero, List<String> consumedUidList, HeroBreakConfig qualityStarConfig) {
        HeroModule heroModule = player.getHeroModule();
        HeroConfig heroConfig = HeroManager.instance().get(hero.getConfigId());
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
        PlayerHelper.delResources(player, OmniItemID, OmniItemCount, OpType.HeroConflate, false);
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
        int oldCombat = BattleHelper.calcHeroCombat(hero);
        PlayerHelper.delResources(player, GlobalConst.HeroLvItem, curConfig.LvConsumeItem, OpType.HeroLevelUp);
        PlayerHelper.delResources(player, Asset.gold.ID, curConfig.LvConsumeMoney, OpType.HeroLevelUp);
        hero.setLevel(hero.getLevel() + 1);
        //		player.updateOfflineAttrData();
        //		hero.update();
        player.handleEvent(EventTypeEnum.HeroLevelUp, hero);
        int newCombat = BattleHelper.calcHeroCombat(hero);
        GameLogger.heroraise(player, hero, 1, 1, hero.getLevel(), oldCombat, newCombat);
        client.sendProtocol(resp.build());
    }

    private void disassemble(NetClient client, Object message) {
        HeroDisassembleRequest_16000052 req = (HeroDisassembleRequest_16000052) message;
        String uid = req.getUid();
        HeroDisassembleResponse_16000053 defaultInstance = HeroDisassembleResponse_16000053.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        HeroModule heroModule = player.getHeroModule();
        Hero hero = heroModule.get(Long.parseLong(uid));
        if (hero == null) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.player_check_error.getId());
            return;
        }
        HeroConfig heroConfig = HeroManager.instance().get(hero.getConfigId());
        Integer count = GlobalConst.HeroSynthesisDisassemble.get(heroConfig.InitialQuality);
        if (count == null) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.config_data_not_found.getId());
            return;
        }
        heroModule.del(Long.parseLong(uid), OpType.HeroDisassemble);
        List<RewardInfo> resources = PlayerHelper.addResources(player, heroConfig.Fragment, count, OpType.HeroDisassemble);
        HeroDisassembleResponse_16000053.Builder resp = HeroDisassembleResponse_16000053.newBuilder();
        resp.addAllReward(resources);
        client.sendProtocol(resp.build());
    }

    private void illustrationsLevelReward(NetClient client, Object message) {
        HeroIllustrationsLevelRewardRequest_16000044 req = (HeroIllustrationsLevelRewardRequest_16000044) message;
        HeroIllustrationsLevelRewardResponse_16000045 defaultInstance = HeroIllustrationsLevelRewardResponse_16000045.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		int level = player.getLevel(Asset.CatalogPoints);
		HeroModule heroModule = player.getHeroModule();
		if (heroModule.getIllustrationRewardLevel() >= level) {
			client.sendProtocol(defaultInstance, ErrorMsgEnum.level_limit.getId());
			return;
		}
        HeroIllustrationsLevelRewardResponse_16000045.Builder resp = HeroIllustrationsLevelRewardResponse_16000045.newBuilder();
		heroModule.setIllustrationRewardLevel(heroModule.getIllustrationRewardLevel() + 1);
		HeroBandBookConfig heroBandBookConfig = HeroBandBookManager.instance().get(heroModule.getIllustrationRewardLevel());
		List<RewardInfo> resources = PlayerHelper.addResources(player, heroBandBookConfig.Reward, OpType.HeroIllustrationsLevelReward);
		resp.addAllReward(resources);
        client.sendProtocol(resp.build());
    }
}
