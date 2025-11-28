package cn.game.games.net.game.module.ginseng;

import java.security.PrivateKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.game.games.core.log.GameLogger;
import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.handler.GameBaseHandler;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.GameConstants;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.battle.BattleModule;
import cn.game.games.net.game.module.develop.hero.HeroModule;
import cn.game.games.net.game.module.item.ItemModule;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.RSGFetterConfig;
import cn.game.protocol.generated.config.RSGRewardConfig;
import cn.game.protocol.generated.config.RSGTreeLvConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.RSGFetterManager;
import cn.game.protocol.generated.manager.RSGRewardManager;
import cn.game.protocol.generated.manager.RSGTreeLvManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeBugRequest_39000005;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeBugResponse_39000006;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeEnergyRequest_39000021;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeEnergyResponse_39000022;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeFertilizationRequest_39000011;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeFertilizationResponse_39000012;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeHandCardRequest_39000025;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeHandCardResponse_39000026;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeHangUpRequest_39000015;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeHangUpResponse_39000016;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeHarvestRequest_39000013;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeHarvestResponse_39000014;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeHeroRequest_39000017;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeHeroResponse_39000018;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeInfoRequest_39000001;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeInfoResponse_39000002;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeInsecticidesRequest_39000007;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeInsecticidesResponse_39000008;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeWateringRequest_39000003;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeWateringResponse_39000004;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;
import cn.game.util.GameUtil;
import cn.game.util.IntMapWrapper;
import cn.game.util.Rnd;

@Component
public class GinsengTreeHandler extends GameBaseHandler {

    @Override
    protected int getModule() {
        return 0x39;
    }

    @Override
    protected InitialUI getInitialUI() {
        return InitialUI.RSGTree;
    }

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.GinsengTreeInfoRequest_39000001, this::info);
        putInvoker(PbProtocol.GinsengTreeWateringRequest_39000003, this::watering);
        putInvoker(PbProtocol.GinsengTreeBugRequest_39000005, this::bug);
        putInvoker(PbProtocol.GinsengTreeInsecticidesRequest_39000007, this::insecticides);
        putInvoker(PbProtocol.GinsengTreeFertilizationRequest_39000011, this::fertilization);
        putInvoker(PbProtocol.GinsengTreeHarvestRequest_39000013, this::harvest);
        putInvoker(PbProtocol.GinsengTreeHangUpRequest_39000015, this::hangUp);
        putInvoker(PbProtocol.GinsengTreeHeroRequest_39000017, this::hero);
        putInvoker(PbProtocol.GinsengTreeEnergyRequest_39000021, this::energy);
        putInvoker(PbProtocol.GinsengTreeHandCardRequest_39000025, this::handCard);
    }

    private void info(NetClient client, Object message) {
        GinsengTreeInfoRequest_39000001 req = (GinsengTreeInfoRequest_39000001) message;
        GinsengTreeInfoResponse_39000002 defaultInstance = GinsengTreeInfoResponse_39000002.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        //		if (!player.isFuncOpen(InitialUI.RSGTree)) {
        //			client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.ID);
        //			return;
        //		}
        GinsengTreeInfoResponse_39000002.Builder resp = GinsengTreeInfoResponse_39000002.newBuilder();
        GinsengTreeModule module = player.getModule(GinsengTreeModule.class);
        resp.setTreeInfo(module.buildGinsengTreeInfo());
        client.sendProtocol(resp.build());
    }

    private void watering(NetClient client, Object message) {
        GinsengTreeWateringRequest_39000003 req = (GinsengTreeWateringRequest_39000003) message;
        GinsengTreeWateringResponse_39000004 defaultInstance = GinsengTreeWateringResponse_39000004.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.RSGTree)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.ID);
            return;
        }
        GinsengTreeWateringResponse_39000004.Builder resp = GinsengTreeWateringResponse_39000004.newBuilder();
        GinsengTreeModule module = player.getModule(GinsengTreeModule.class);
        int waterTimes = module.getWaterTimes();
        int[] cost =GameUtil.getArrayCost(GlobalConst.RSGTreeWaterCost, waterTimes); 
        PlayerHelper.delResources(player, cost, OpType.GinsengTreeWarter);
        module.setWaterTimes(waterTimes + 1);
        List<RewardInfo> expReward = module.addExp(); 
        if (expReward != null) {
			resp.addAllRewards(expReward); 
		}
        // 浇水奖励
        List<RewardInfo> resources = PlayerHelper.addResources(player, GlobalConst.RSGTreeWaterLeave, OpType.GinsengTreeWarter);
        resp.addAllRewards(resources);
        GameLogger.RSGTreeGrow(player, 1, 1);
        client.sendProtocol(resp.build());
    }

    private void bug(NetClient client, Object message) {
        GinsengTreeBugRequest_39000005 req = (GinsengTreeBugRequest_39000005) message;
        GinsengTreeBugResponse_39000006 defaultInstance = GinsengTreeBugResponse_39000006.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.RSGTree)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.ID);
            return;
        }
        GinsengTreeBugResponse_39000006.Builder resp = GinsengTreeBugResponse_39000006.newBuilder();
        GinsengTreeModule module = player.getModule(GinsengTreeModule.class);
        int bugs = module.getBugs();
        if (bugs <= 0) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.player_check_error.ID);
            return;
        }
        module.removeBug();
        List<RewardInfo> resources = PlayerHelper.addResources(player, GlobalConst.RSGTreeInsecticideLeave, OpType.GinsengTreeBug);
        resp.addAllRewards(resources);
        client.sendProtocol(resp.build());
        GameLogger.RSGTreeGrow(player, 2, 1);
    }

    private void insecticides(NetClient client, Object message) {
        GinsengTreeInsecticidesRequest_39000007 req = (GinsengTreeInsecticidesRequest_39000007) message;
        GinsengTreeInsecticidesResponse_39000008 defaultInstance = GinsengTreeInsecticidesResponse_39000008.getDefaultInstance();
        int count = req.getCount();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        player.checkClientRequestCount(count);
        if (!player.isFuncOpen(InitialUI.RSGTree)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.ID);
            return;
        }
        GinsengTreeInsecticidesResponse_39000008.Builder resp = GinsengTreeInsecticidesResponse_39000008.newBuilder();
        GinsengTreeModule module = player.getModule(GinsengTreeModule.class);
        int insecticidesTimes = module.getInsecticidesTimes();
//        if (insecticidesTimes + count >= GlobalConst.RSGTreeInsecticideMax) {
//            client.sendProtocol(defaultInstance, ErrorMsgEnum.times_limit.ID);
//            return;
//        }
        int[] cost = GameUtil.arrayMultiple(GlobalConst.RSGTreeInsecticidePrice, count);
        PlayerHelper.delResources(player, cost, OpType.GinsengTreeInsecticidesBug);
        module.setInsecticidesTimes(insecticidesTimes + count);
        int bugs = module.getBugs(); 
        module.clearBugs();
        int insecticidesEndTime = module.getInsecticidesEndTime() == 0 ? DateUtil.currentTimeSeconds() : module.getInsecticidesEndTime();
        module.setInsecticidesEndTime(insecticidesEndTime + GlobalConst.RSGTreeInsecticideTime * count);
        
        for (int i = 0; i < bugs; i++) {
        	List<RewardInfo> resources = PlayerHelper.addResources(player, GlobalConst.RSGTreeInsecticideLeave, OpType.GinsengTreeBug);
        	resp.addAllRewards(resources);
		}
        resp.setTreeInfo(module.buildGinsengTreeInfo());
        client.sendProtocol(resp.build());
        GameLogger.RSGTreeGrow(player, 3, count);
    }

    private void fertilization(NetClient client, Object message) {
        GinsengTreeFertilizationRequest_39000011 req = (GinsengTreeFertilizationRequest_39000011) message;
        GinsengTreeFertilizationResponse_39000012 defaultInstance = GinsengTreeFertilizationResponse_39000012.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.RSGTree)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.ID);
            return;
        }
        GinsengTreeFertilizationResponse_39000012.Builder resp = GinsengTreeFertilizationResponse_39000012.newBuilder();
        GinsengTreeModule module = player.getModule(GinsengTreeModule.class);
        PlayerHelper.delResources(player, 212001, 1, OpType.GinsengTreeInsectic);
     
        List<RewardInfo> expReward = module.addExp(); 
        if (expReward != null) {
			resp.addAllRewards(expReward); 
		}
        
        List<RewardInfo> resources = PlayerHelper.addResources(player, GlobalConst.RSGTreeFertilizerLeave, OpType.GinsengTreeInsectic);
        resp.addAllRewards(resources);
        // 减少果实时间
        module.fertilization();
        resp.setTreeInfo(module.buildGinsengTreeInfo());
        client.sendProtocol(resp.build());
        GameLogger.RSGTreeGrow(player, 4, 1);
    }

    private void harvest(NetClient client, Object message) {
        GinsengTreeHarvestRequest_39000013 req = (GinsengTreeHarvestRequest_39000013) message;
        int pos = req.getPos();
        GinsengTreeHarvestResponse_39000014 defaultInstance = GinsengTreeHarvestResponse_39000014.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.RSGTree)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.ID);
            return;
        }
        GinsengTreeHarvestResponse_39000014.Builder resp = GinsengTreeHarvestResponse_39000014.newBuilder();
        GinsengTreeModule module = player.getModule(GinsengTreeModule.class);
        IntMapWrapper map = module.getFruitMap();
        if (!map.hasValue(pos) || map.getValue(pos) > DateUtil.currentTimeSeconds()) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.player_check_error.ID);
            return;
        }
        map.remove(pos);
        if (module.isFirstFruit()) {// 第一次特殊奖励
        	List<RewardInfo> reward = PlayerHelper.addReward(player, 1030002, OpType.GinsengTreeHarvest); 
        	resp.addAllRewards(reward); 
        	module.setFirstFruit(false);
		}else {
			  List<Integer> heroIdList = module.getHeroIdList();
		        // 基础奖励
		        RSGTreeLvConfig rsgTreeLvConfig = RSGTreeLvManager.instance().get(player.getLevel(Asset.RSGTreeExp));
		        int[][] baseReward = GameUtil.arrayZoomBy10k(rsgTreeLvConfig.Reward, heroIdList.size() * 100);
		        int rewardReduceBugCount = module.getRewardReduceBugCount(); 
		        if (rewardReduceBugCount > 0) {
		            baseReward = GameUtil.arrayZoomBy10k(baseReward, -rewardReduceBugCount * GlobalConst.RSGTreeBugRewardReduce);
				}
		        List<RewardInfo> baseRewardList = PlayerHelper.addResources(player, baseReward, OpType.GinsengTreeHarvest);
		        resp.addAllRewards(baseRewardList);
		        // 手操卡加成的默认奖励
		        Map<Integer, Integer> fetterMap = new HashMap<>();
		        if (!heroIdList.isEmpty()) {
		            List<RSGFetterConfig> list = RSGFetterManager.instance().list();
		            for (RSGFetterConfig rsgFetterConfig : list) {
		                if (GameUtil.containsAll(heroIdList, rsgFetterConfig.HeroList)) {
		                    fetterMap.put(rsgFetterConfig.RSGRewardID, rsgFetterConfig.AddRewardWeight);
		                }
		            }
		        }
		        List<RSGRewardConfig> list = RSGRewardManager.instance().list();
//		        int rewardCount = Rnd.randomInRange(GlobalConst.RSGTreeRewardNum);
		        int rewardCount = rsgTreeLvConfig.CardNum;
		        for (int i = 0; i < rewardCount; i++) {
		            RSGRewardConfig rsgRewardConfig = null;
		            if (fetterMap.isEmpty()) {
		                rsgRewardConfig = Rnd.randomElement(list);
		            } else {
		                int index = Rnd.randomIndex(list, e -> {
		                    int weigetAdd = 0;
		                    if (!fetterMap.isEmpty()) {
		                        weigetAdd = fetterMap.getOrDefault(e.ID, 0);
		                    }
		                    return e.RewardWeight + weigetAdd;
		                });
		                rsgRewardConfig = list.get(index);
		            }
		            List<RewardInfo> resources = PlayerHelper.addResources(player, rsgRewardConfig.RewardID, OpType.GinsengTreeHarvest);
		            resp.addAllRewards(resources);
		        }
		}
        if (map.getMap().isEmpty()) {
        	module.setNextFruitTime(0);
        	module.startFruitTask();
		}
        client.sendProtocol(resp.build());
    }

    private void hangUp(NetClient client, Object message) {
        GinsengTreeHangUpRequest_39000015 req = (GinsengTreeHangUpRequest_39000015) message;
        GinsengTreeHangUpResponse_39000016 defaultInstance = GinsengTreeHangUpResponse_39000016.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.RSGTree)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.ID);
            return;
        }
        GinsengTreeHangUpResponse_39000016.Builder resp = GinsengTreeHangUpResponse_39000016.newBuilder();
        GinsengTreeModule module = player.getModule(GinsengTreeModule.class);
        //		int hangUpStartTime = module.getHangUpStartTime();
        module.setHangUpStartTime(DateUtil.currentTimeSeconds());
        int minutes = module.calcHangUpReward();
        if (module.getHangUpRandomRewardMap().getMap().isEmpty()) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.request_parameter_error.ID);
            return;
        }
        // 随机奖励
        IntMapWrapper rewardMap = new IntMapWrapper();
        rewardMap.getMap().putAll(module.getHangUpRandomRewardMap().getMap());
        // 固定奖励
        int level = player.getLevel(Asset.RSGTreeExp);
        RSGTreeLvConfig rsgTreeLvConfig = RSGTreeLvManager.instance().get(level);
        int[][] rewardArray = GameUtil.arrayMultiple(rsgTreeLvConfig.Reward, minutes);
        for (int[] is : rewardArray) {
            rewardMap.add(is[0], is[1]);
        }
        // 所有奖励做一个英雄数量加成
        int heroSize = module.getHeroIdList().size();
        if (heroSize > 0) {
            rewardMap.getMap().replaceAll((k, v) -> v + ((int) (heroSize / 10000f) * v));
        }
        List<RewardInfo> resources = PlayerHelper.addResources(player, rewardMap.getMap(), OpType.GinsengTreeHangUp);
        resp.addAllRewards(resources);
        module.getHangUpRandomRewardMap().clear();
        client.sendProtocol(resp.build());
    }

    private void hero(NetClient client, Object message) {
        GinsengTreeHeroRequest_39000017 req = (GinsengTreeHeroRequest_39000017) message;
        int heroId = req.getHeroId();
        GinsengTreeHeroResponse_39000018 defaultInstance = GinsengTreeHeroResponse_39000018.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.RSGTree)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.ID);
            return;
        }
        GinsengTreeModule module = player.getModule(GinsengTreeModule.class);
        HeroModule heroModule = player.getHeroModule();
        if (!heroModule.has(heroId)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.player_check_error.ID);
            return;
        }
        List<Integer> heroIdList = module.getHeroIdList();
        if (heroIdList.contains(heroId)) {
            heroIdList.remove(Integer.valueOf(heroId));
        } else {
            heroIdList.add(heroId);
        }
        client.sendProtocol(defaultInstance);
    }

    private void energy(NetClient client, Object message) {
        GinsengTreeEnergyRequest_39000021 req = (GinsengTreeEnergyRequest_39000021) message;
        int count = req.getCount();
        GinsengTreeEnergyResponse_39000022 defaultInstance = GinsengTreeEnergyResponse_39000022.getDefaultInstance();
        GinsengTreeEnergyResponse_39000022.Builder resp = GinsengTreeEnergyResponse_39000022.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        BattleModule battleModule = player.getBattleModule();
        List<Integer> storeStaminas = battleModule.getStoreStaminas();
        if (storeStaminas.isEmpty()) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.player_check_error.ID);
            return;
        }
        int energyCount = storeStaminas.size();
        if (count > energyCount) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.times_limit.ID);
            return;
        }
        if (count > 0) {
            energyCount = count;
            for (int i = 0; i < count; i++) {
                storeStaminas.removeFirst();
            }
        } else {
            storeStaminas.clear();
        }
        List<RewardInfo> resources = PlayerHelper.addResources(player, Asset.playerEnergy.ID, energyCount * GlobalConst.RSGTreeEnergyFruitAdd, OpType.GinsengTree);
        resp.addAllRewards(resources);
        client.sendProtocol(resp.build());
    }

    private void handCard(NetClient client, Object message) {
        GinsengTreeHandCardRequest_39000025 req = (GinsengTreeHandCardRequest_39000025) message;
        List<Integer> idList = req.getIdList();
        GinsengTreeHandCardResponse_39000026 defaultInstance = GinsengTreeHandCardResponse_39000026.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        GinsengTreeModule module = player.getModule(GinsengTreeModule.class);
        ItemModule itemModule = player.getItemModule(); 
        for (Integer id : idList) {
			if (!itemModule.has(id)) {
				player.fail(ErrorMsgEnum.player_data_not_found);
			}
		}
        List<Integer> handCardList = module.getHandCardList(); 
        handCardList.clear(); 
        handCardList.addAll(idList);
        
        client.sendProtocol(defaultInstance);
    }
}
