package cn.game.games.net.game.module.develop;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.stereotype.Component;

import com.alibaba.druid.util.StringUtils;

import cn.game.core.net.client.NetClient;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.handler.GameBaseHandler;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.QuestHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.develop.defenceline.DefenceSkin;
import cn.game.games.net.game.module.develop.defenceline.DefenceSkinModule;
import cn.game.games.net.game.module.quest.Quest;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.protocol.generated.config.DefenceLevelUpConfig;
import cn.game.protocol.generated.config.DefenceSkinStarUpConfig;
import cn.game.protocol.generated.config.HeavenlyDaoConfig;
import cn.game.protocol.generated.config.PotentialConfig;
import cn.game.protocol.generated.config.QiankunMirrorLvConfig;
import cn.game.protocol.generated.config.RescueConfig;
import cn.game.protocol.generated.config.SpiritualAttrConfig;
import cn.game.protocol.generated.config.SpiritualQualityConfig;
import cn.game.protocol.generated.config.SpiritualRootConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.DefenceLevelUpManager;
import cn.game.protocol.generated.manager.DefenceSkinStarUpManager;
import cn.game.protocol.generated.manager.HeavenlyDaoManager;
import cn.game.protocol.generated.manager.QiankunMirrorLvManager;
import cn.game.protocol.generated.manager.SpiritualAttrManager;
import cn.game.protocol.generated.manager.SpiritualQualityManager;
import cn.game.protocol.generated.manager.SpiritualRootManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BaseMsg.AttrGrowInfo;
import cn.game.protocol.protobuf.DevelopMsg.DevelopHeavenlyDaoLvUpRequest_25000010;
import cn.game.protocol.protobuf.DevelopMsg.DevelopHeavenlyDaoLvUpResponse_25000011;
import cn.game.protocol.protobuf.DevelopMsg.DevelopPotentialBreakRequest_25000003;
import cn.game.protocol.protobuf.DevelopMsg.DevelopPotentialBreakResponse_25000004;
import cn.game.protocol.protobuf.DevelopMsg.DevelopPotentialLvUpRequest_25000001;
import cn.game.protocol.protobuf.DevelopMsg.DevelopPotentialLvUpResponse_25000002;
import cn.game.protocol.protobuf.DevelopMsg.DevelopRescueLvUpRequest_25000007;
import cn.game.protocol.protobuf.DevelopMsg.DevelopRescueLvUpResponse_25000008;
import cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorInfo.Builder;
import cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorReplaceRequest_25000022;
import cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorReplaceResponse_25000023;
import cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorReversalRequest_25000020;
import cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorReversalResponse_25000021;
import cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorSpiritualRootUnlockRequest_25000024;
import cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorSpiritualRootUnlockResponse_25000025;
import cn.game.protocol.protobuf.DevelopMsg.SpiritualInfo;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.Rnd;
import cn.game.protocol.protobuf.DevelopMsg.DefenceLevelUpRequest_25000031;
import cn.game.protocol.protobuf.DevelopMsg.DefenceLevelUpResponse_25000032;
import cn.game.protocol.protobuf.DevelopMsg.DefenceSkinStarUpRequest_25000033;
import cn.game.protocol.protobuf.DevelopMsg.DefenceSkinStarUpResponse_25000034;
import cn.game.protocol.protobuf.DevelopMsg.DefenceSkinChangeRequest_25000035;
import cn.game.protocol.protobuf.DevelopMsg.DefenceSkinChangeResponse_25000036;

@Component
public class DevelopHandler extends GameBaseHandler {

    @Override
    protected int getModule() {
        return 0x25;
    }

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.DevelopHeavenlyDaoLvUpRequest_25000010, this::heavenlyDaoLvUp);
        putInvoker(PbProtocol.DevelopPotentialLvUpRequest_25000001, this::potentialLvUp);
        putInvoker(PbProtocol.DevelopPotentialBreakRequest_25000003, this::potentialBreak);
        putInvoker(PbProtocol.DevelopRescueLvUpRequest_25000007, this::rescueLvUp);
        putInvoker(PbProtocol.QianKunMirrorReversalRequest_25000020, this::qianKunMirrorReversal);
        putInvoker(PbProtocol.QianKunMirrorReplaceRequest_25000022, this::qianKunMirrorReplace);
        putInvoker(PbProtocol.QianKunMirrorSpiritualRootUnlockRequest_25000024, this::spiritualRootUnlock);
        putInvoker(PbProtocol.DefenceLevelUpRequest_25000031, this::defenceLevelUp);
        putInvoker(PbProtocol.DefenceSkinStarUpRequest_25000033, this::defenceSkinStarUp);
        putInvoker(PbProtocol.DefenceSkinChangeRequest_25000035, this::defenceSkinChange);
    }

    private void empty(NetClient client, Object message) {
        QianKunMirrorReversalRequest_25000020 req = (QianKunMirrorReversalRequest_25000020) message;
        QianKunMirrorReversalResponse_25000021.Builder resp = QianKunMirrorReversalResponse_25000021.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.QiankunMirror)) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
            return;
        }
        DevelopModule developModule = player.getDevelopModule();
        client.sendProtocol(resp.build());
    }

    private void spiritualRootUnlock(NetClient client, Object message) {
        QianKunMirrorSpiritualRootUnlockRequest_25000024 req = (QianKunMirrorSpiritualRootUnlockRequest_25000024) message;
        QianKunMirrorSpiritualRootUnlockResponse_25000025 resp = QianKunMirrorSpiritualRootUnlockResponse_25000025.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.QiankunMirror)) {
            client.sendProtocol(resp, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        int id = req.getId();
        DevelopModule developModule = player.getDevelopModule();
        Builder qiankunMirrorBuilder = developModule.getQiankunMirrorBuilder();
        List<Integer> spiritualRootIdsList = qiankunMirrorBuilder.getSpiritualRootIdsList();
        SpiritualRootConfig spiritualRootConfig = SpiritualRootManager.instance().get(id);
        if (spiritualRootConfig.preID > 0 && !spiritualRootIdsList.contains(spiritualRootConfig.preID)) {
            client.sendProtocol(resp, ErrorMsgEnum.pre_condition_check_error.getId());
            return;
        }
        if (spiritualRootIdsList.contains(id)) {
            client.sendProtocol(resp, ErrorMsgEnum.repeat_request.getId());
            return;
        }
        PlayerHelper.delResources(player, spiritualRootConfig.PurpleConsume, OpType.QianKunMirror);
        int removeIndex = -1;
        for (int i = 0; i < qiankunMirrorBuilder.getSpiritualRootIdsCount(); i++) {
            if (qiankunMirrorBuilder.getSpiritualRootIds(i) == spiritualRootConfig.preID) {
                removeIndex = i;
                break;
            }
        }
        if (removeIndex > -1) {
            qiankunMirrorBuilder.setSpiritualRootIds(removeIndex, id);
        } else {
            qiankunMirrorBuilder.addSpiritualRootIds(id);
        }
        // TODO 属性计算
        client.sendProtocol(resp);
    }

    private void qianKunMirrorReplace(NetClient client, Object message) {
        QianKunMirrorReplaceRequest_25000022 req = (QianKunMirrorReplaceRequest_25000022) message;
        QianKunMirrorReplaceResponse_25000023.Builder resp = QianKunMirrorReplaceResponse_25000023.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.QiankunMirror)) {
            client.sendProtocol(resp, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        DevelopModule developModule = player.getDevelopModule();
        Builder qiankunMirrorBuilder = developModule.getQiankunMirrorBuilder();
        //		SpiritualInfo spiritualReplace = qiankunMirrorBuilder.getSpiritualReplace();
        if (!qiankunMirrorBuilder.hasSpiritualReplace()) {
            client.sendProtocol(resp, ErrorMsgEnum.player_data_not_found.getId());
            return;
        }
        SpiritualInfo spiritualRemove = null;
        if (req.getReplace()) {
            int startPos = qiankunMirrorBuilder.getStartPos();
            spiritualRemove = qiankunMirrorBuilder.getSpiritual(startPos);
            qiankunMirrorBuilder.setSpiritual(startPos, qiankunMirrorBuilder.getSpiritualReplace());
        } else {
            spiritualRemove = qiankunMirrorBuilder.getSpiritualReplace();
        }
        qiankunMirrorBuilder.clearSpiritualReplace();
        QiankunMirrorLvConfig qiankunMirrorLvConfig = QiankunMirrorLvManager.instance().get(player.getLevel(Asset.QiankunMirrorExp));
        int randomIndex = Rnd.randomIndex(qiankunMirrorLvConfig.DecompositionItemProbability);
        int itemId = qiankunMirrorLvConfig.DecompositionItem[randomIndex];
        int itemCount = qiankunMirrorLvConfig.DecompositionItemNum[randomIndex];
        SpiritualQualityConfig spiritualQualityConfig = SpiritualQualityManager.instance().get(spiritualRemove.getQuality());
        itemCount *= spiritualQualityConfig.DecompositionItemQuality / 10000f;
        List<RewardInfo> resources = PlayerHelper.addResources(player, itemId, itemCount, OpType.QianKunMirror);
        resp.addAllRewards(resources);
        int pos = qiankunMirrorBuilder.getStartPos();
        int nextPos = pos + 1 > 5 ? 0 : pos + 1;
        qiankunMirrorBuilder.setStartPos(nextPos);
        client.sendProtocol(resp);
    }

    private void qianKunMirrorReversal(NetClient client, Object message) {
        QianKunMirrorReversalRequest_25000020 req = (QianKunMirrorReversalRequest_25000020) message;
        QianKunMirrorReversalResponse_25000021.Builder resp = QianKunMirrorReversalResponse_25000021.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.QiankunMirror)) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
            return;
        }
        DevelopModule developModule = player.getDevelopModule();
        Builder qiankunMirrorBuilder = developModule.getQiankunMirrorBuilder();
        PlayerHelper.delResources(player, Asset.QiankunSpiritualEssence.ID, 1, OpType.QianKunMirror);
        int pos = qiankunMirrorBuilder.getStartPos();
        int nextPos = pos + 1 > 5 ? 0 : pos + 1;
        int level = player.getLevel(Asset.QiankunMirrorExp);
        QiankunMirrorLvConfig qiankunMirrorLvConfig = QiankunMirrorLvManager.instance().get(level);
        int randomLevel = level + qiankunMirrorLvConfig.SpiritLv[Rnd.randomIndex(qiankunMirrorLvConfig.SpiritLvProbability)];
        int randomQuality = qiankunMirrorLvConfig.SpiritQuality[Rnd.randomIndex(qiankunMirrorLvConfig.SpiritQualityProbability)];
        SpiritualQualityConfig spiritualQualityConfig = SpiritualQualityManager.instance().get(randomQuality);
        int affixCount = spiritualQualityConfig.SpiritQualityaAffixNum[Rnd.randomIndex(spiritualQualityConfig.SpiritQualityaAffixNumProbability)];
        cn.game.protocol.protobuf.DevelopMsg.SpiritualInfo.Builder spiritualBuilder = SpiritualInfo.newBuilder();
        for (int i = 1; i <= affixCount; i++) {
            List<SpiritualAttrConfig> spiritPositionAttrGroupList = SpiritualAttrManager.instance().getSpiritPositionAttrGroupList(pos + 1, i);
            int randomIndex = Rnd.randomIndex(spiritPositionAttrGroupList, r -> r.Weight);
            SpiritualAttrConfig spiritualAttrConfig = spiritPositionAttrGroupList.get(randomIndex);
            spiritualBuilder.addAtts(AttrGrowInfo.newBuilder().setId(spiritualAttrConfig.AttributeId).setStartValue(spiritualAttrConfig.AttrMin).setGrowValue(spiritualAttrConfig.AttrUpgrade));
        }
        spiritualBuilder.setLevel(randomLevel);
        spiritualBuilder.setQuality(randomQuality);
        List<SpiritualInfo> spiritualList = qiankunMirrorBuilder.getSpiritualList();
        if (spiritualList.size() == pos) {
            qiankunMirrorBuilder.addSpiritual(spiritualBuilder.build());
            qiankunMirrorBuilder.setStartPos(nextPos);
        } else {
            qiankunMirrorBuilder.setSpiritualReplace(spiritualBuilder);
        }
        player.getCurrencyModule().addExp(Asset.QiankunMirrorExp.ID, 1);
        resp.setSpiritual(spiritualBuilder.build());
        client.sendProtocol(resp.build());
    }

    private void potentialLvUp(NetClient client, Object message) {
        DevelopPotentialLvUpRequest_25000001 req = (DevelopPotentialLvUpRequest_25000001) message;
        DevelopPotentialLvUpResponse_25000002.Builder resp = DevelopPotentialLvUpResponse_25000002.newBuilder();
        int id = req.getId();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.Consciousness)) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
            return;
        }
        DevelopModule developModule = player.getDevelopModule();
        int level = developModule.getCultivationLv(id, 1);
        if (level == 0) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
            return;
        }
        PotentialConfig nextConfig = DevelopHelper.getPotentialConfig(id, level + 1);
        if (nextConfig == null) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.level_limit.getId());
            return;
        }
        PotentialConfig potentialConfig = DevelopHelper.getPotentialConfig(id, level);
        // 当前等级需要突破后才能继续升级
        if (level % potentialConfig.PotentialBreak[0][0] == 0 && !developModule.isPotentiaBreak(id)) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.illegal_request.getId());
            return;
        }
        List<Entry<Integer, Integer>> consumeList = new ArrayList<Map.Entry<Integer, Integer>>();
        for (int[] consume : potentialConfig.PotentialConsume) {
            int calcPotentialConsumeValue = DevelopHelper.calcPotentialConsumeValue(consume[1], consume[2], consume[3], level);
            consumeList.add(new AbstractMap.SimpleEntry(consume[0], calcPotentialConsumeValue));
        }
        PlayerHelper.delResources(player, consumeList, OpType.PotentialLvUp);
        developModule.getPotentiaLvMap().add(id, 1);
        developModule.setIsPotentiaBreak(id, false);
        player.handleEvent(EventTypeEnum.QianLi);
        client.sendProtocol(resp.build());
    }

    private void potentialBreak(NetClient client, Object message) {
        DevelopPotentialBreakRequest_25000003 req = (DevelopPotentialBreakRequest_25000003) message;
        DevelopPotentialBreakResponse_25000004.Builder resp = DevelopPotentialBreakResponse_25000004.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.Consciousness)) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
            return;
        }
        int id = req.getId();
        DevelopModule developModule = player.getDevelopModule();
        int level = developModule.getCultivationLv(id, 1);
        if (level == 0) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
            return;
        }
        PotentialConfig potentialConfig = DevelopHelper.getPotentialConfig(id, level);
        if (level % potentialConfig.PotentialBreak[0][0] != 0 || developModule.isPotentiaBreak(id)) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
            return;
        }
        List<Entry<Integer, Integer>> consumeList = new ArrayList<Map.Entry<Integer, Integer>>();
        for (int[] consume : potentialConfig.PotentialBreak) {
            consumeList.add(new AbstractMap.SimpleEntry(consume[1], consume[2]));
        }
        PlayerHelper.delResources(player, consumeList, OpType.PotentialBreak);
        developModule.setIsPotentiaBreak(id, true);
        developModule.getPotentiaBreakLevelMap().add(id, 1);
        client.sendProtocol(resp.build());
    }

    private void rescueLvUp(NetClient client, Object message) {
        DevelopRescueLvUpRequest_25000007 req = (DevelopRescueLvUpRequest_25000007) message;
        DevelopRescueLvUpResponse_25000008.Builder resp = DevelopRescueLvUpResponse_25000008.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        int id = req.getId();
        if (!player.isFuncOpen(InitialUI.HuDaoQiangYuan)) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
            return;
        }
        DevelopModule developModule = player.getDevelopModule();
        int level = developModule.getCultivationLv(id, 2);
        RescueConfig nextConfig = DevelopHelper.getRescueConfig(id, level + 1);
        if (nextConfig == null) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.level_limit.getId());
            return;
        }
        RescueConfig config = DevelopHelper.getRescueConfig(id, level);
        if (!PlayerHelper.checkCondition(player, config.RescueUnlock)) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.condition_check_error.getId());
            return;
        }
        List<Entry<Integer, Integer>> consumeList = new ArrayList<Map.Entry<Integer, Integer>>();
        for (int[] consume : config.RescueConsume) {
            int calcPotentialConsumeValue = DevelopHelper.calcPotentialConsumeValue(consume[1], consume[2], consume[3], level);
            consumeList.add(new AbstractMap.SimpleEntry(consume[0], calcPotentialConsumeValue));
        }
        PlayerHelper.delResources(player, consumeList, OpType.RescueLvUp);
        developModule.getPotentiaLvMap().add(id, 1);
        player.handleEvent(EventTypeEnum.QiangYuan);
        client.sendProtocol(resp.build());
    }

    private void heavenlyDaoLvUp(NetClient client, Object message) {
        DevelopHeavenlyDaoLvUpRequest_25000010 req = (DevelopHeavenlyDaoLvUpRequest_25000010) message;
        DevelopHeavenlyDaoLvUpResponse_25000011.Builder resp = DevelopHeavenlyDaoLvUpResponse_25000011.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.HeavenlyDaoCultivation)) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
            return;
        }
        DevelopModule developModule = player.getDevelopModule();
        int heavenlyDaoLevel = developModule.getHeavenlyDaoLevel();
        HeavenlyDaoConfig nextConfig = HeavenlyDaoManager.instance().getNullable(heavenlyDaoLevel + 1);
        if (nextConfig == null) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
            return;
        }
//        HeavenlyDaoConfig heavenlyDaoConfig = HeavenlyDaoManager.instance().get(heavenlyDaoLevel);
        int[] taskID = nextConfig.TaskID;
        QuestModule questModule = player.getQuestModule();
        boolean isAllTaskReceived = true;
        for (int tid : taskID) {
            Quest quest = questModule.get(tid);
            if (quest != null && quest.getState() < QuestHelper.REWARDED) {
                isAllTaskReceived = false;
                break;
            }
        }
        if (!isAllTaskReceived) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
            return;
        }
        resp.addAllRewards(PlayerHelper.addResources(player, nextConfig.Reward, OpType.TianDaoLvUp)); 
        developModule.setHeavenlyDaoLevel(heavenlyDaoLevel + 1);
        player.handleEvent(EventTypeEnum.CultivatesImmortals);
        client.sendProtocol(resp.build());
    }

    private void defenceLevelUp(NetClient client, Object message) {
        DefenceLevelUpRequest_25000031 req = (DefenceLevelUpRequest_25000031) message;
        DefenceLevelUpResponse_25000032 defaultInstance = DefenceLevelUpResponse_25000032.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        int defenceLevel = player.getDevelopModule().getDefenceLevel();
        DefenceLevelUpConfig curDefenceLevelUpConfig = DefenceLevelUpManager.instance().getNullable(defenceLevel);
        DefenceLevelUpConfig nextDefenceLevelUpConfig = DefenceLevelUpManager.instance().getNullable(defenceLevel + 1);
        if (nextDefenceLevelUpConfig == null) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.level_limit.ID);
            return;
        }
        PlayerHelper.delResources(player, curDefenceLevelUpConfig.Material, curDefenceLevelUpConfig.Point, OpType.DefenceLevelUp);
        player.getDevelopModule().setDefenceLevel(defenceLevel + 1);
        player.handleEvent(EventTypeEnum.DefenceLevelUp,defenceLevel + 1);
        client.sendProtocol(defaultInstance);
    }

    private void defenceSkinStarUp(NetClient client, Object message) {
        DefenceSkinStarUpRequest_25000033 req = (DefenceSkinStarUpRequest_25000033) message;
        List<String> ConsumedUidsList = req.getConsumedUidsList();
        DefenceSkinStarUpResponse_25000034 defaultInstance = DefenceSkinStarUpResponse_25000034.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        
        DefenceSkinModule module = player.getModule(DefenceSkinModule.class); 
        long starUpUid = Long.parseLong(req.getStarUpUid());
        DefenceSkin defenceSkin = module.get(starUpUid);
		if (defenceSkin == null) {
			client.sendProtocol(defaultInstance, ErrorMsgEnum.player_data_not_found.getId());
			return;
		}
		DefenceSkinStarUpConfig curConfig= DefenceSkinStarUpManager.instance().getUIDefenceSkinIDStar(defenceSkin.getConfigId(), defenceSkin.getStar()); 
		DefenceSkinStarUpConfig nextConfig = DefenceSkinStarUpManager.instance().getUIDefenceSkinIDStar(defenceSkin.getConfigId(), defenceSkin.getStar()+ 1); 
		if (nextConfig == null) {
			client.sendProtocol(defaultInstance, ErrorMsgEnum.level_limit.getId());
			return;
		}
		int consumeCount = curConfig.LvUp ; 
		List<Long> ConsumedUidsListLong = new ArrayList<>();
		for (String uid : ConsumedUidsList) {
			long tempId = Long.parseLong(uid); 
	        DefenceSkin defenceSkinConsume = module.get(tempId);
			if (defenceSkinConsume == null) {
				client.sendProtocol(defaultInstance, ErrorMsgEnum.player_data_not_found.getId());
				return;
			}
			if (defenceSkinConsume.getConfigId() != defenceSkin.getConfigId()) {
				client.sendProtocol(defaultInstance, ErrorMsgEnum.illegal_request.getId());
				return;
			}
			ConsumedUidsListLong.add(tempId); 
		}
		if (ConsumedUidsList.size() < consumeCount) {
			client.sendProtocol(defaultInstance, ErrorMsgEnum.illegal_request.getId());
			return;
		}
		if (ConsumedUidsList.size() > consumeCount) {
			ConsumedUidsListLong= ConsumedUidsListLong.subList(0, consumeCount); 
		}
		boolean delBatch = module.delBatch(ConsumedUidsListLong, OpType.DefenceSkinStarUp);
		if (!delBatch) {
			client.sendProtocol(defaultInstance, ErrorMsgEnum.player_data_not_found.getId());
			return;
		}
		
		defenceSkin.setStar(defenceSkin.getStar() + 1);
        client.sendProtocol(defaultInstance);
    }

	private void defenceSkinChange(NetClient client, Object message) {
		DefenceSkinChangeRequest_25000035 req = (DefenceSkinChangeRequest_25000035) message;
		String uid = req.getUid();
		DefenceSkinChangeResponse_25000036 defaultInstance = DefenceSkinChangeResponse_25000036.getDefaultInstance();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		DefenceSkinModule module = player.getModule(DefenceSkinModule.class);
		long longId = StringUtils.isEmpty(uid) ? 0 : Long.parseLong(uid);
		if (longId > 0) {
			DefenceSkin defenceSkin = module.get(longId);
			if (defenceSkin == null) {
				client.sendProtocol(defaultInstance, ErrorMsgEnum.player_data_not_found.getId());
				return;
			}
		}
		module.setCurUseId(longId);
		client.sendProtocol(defaultInstance);
	}
}
