package cn.game.games.net.game.module.develop;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.QuestHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.quest.Quest;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.protocol.generated.config.HeavenlyDaoConfig;
import cn.game.protocol.generated.config.PotentialConfig;
import cn.game.protocol.generated.config.QiankunMirrorLvConfig;
import cn.game.protocol.generated.config.RescueConfig;
import cn.game.protocol.generated.config.SpiritualAttrConfig;
import cn.game.protocol.generated.config.SpiritualQualityConfig;
import cn.game.protocol.generated.config.SpiritualRootConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
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

@Component
public class DevelopHandler extends BaseHandler {

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
		boolean delResources = PlayerHelper.delResources(player, spiritualRootConfig.PurpleConsume, OpType.QianKunMirror);
		if (!delResources) {
			client.sendProtocol(resp, ErrorMsgEnum.resource_not_enough.getId());
			return;
		}
		int removeIndex = -1;
		for (int i = 0; i < qiankunMirrorBuilder.getSpiritualRootIdsCount(); i++) {
			if (qiankunMirrorBuilder.getSpiritualRootIds(i) == spiritualRootConfig.preID) {
				removeIndex = i ; 
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
		if (!PlayerHelper.delResources(player, Asset.QiankunSpiritualEssence.ID, 1, OpType.QianKunMirror)) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.resource_not_enough.getId());
            return;
		}
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
			List<SpiritualAttrConfig> spiritPositionAttrGroupList = SpiritualAttrManager
					.instance()
					.getSpiritPositionAttrGroupList(pos + 1, i);
			int randomIndex = Rnd.randomIndex(spiritPositionAttrGroupList, r -> r.Weight);
			SpiritualAttrConfig spiritualAttrConfig = spiritPositionAttrGroupList.get(randomIndex);
			spiritualBuilder.addAtts(AttrGrowInfo.newBuilder().setId(spiritualAttrConfig.AttributeId).setStartValue(spiritualAttrConfig.AttrMin)
					.setGrowValue(spiritualAttrConfig.AttrUpgrade));
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
		if (!PlayerHelper.delResources(player, consumeList, OpType.PotentialLvUp)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.resource_not_enough.getId());
			return;
		}
		developModule.getPotentiaLvMap().add(id, 1);
		developModule.setIsPotentiaBreak(id, false);

		player.handleEvent(EventTypeEnum.Practice);

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
		if (!PlayerHelper.delResources(player, consumeList, OpType.PotentialBreak)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.resource_not_enough.getId());
			return;
		}
		developModule.setIsPotentiaBreak(id, true);
		developModule.getPotentiaBreakLevelMap().add(id, 1);
		client.sendProtocol(resp.build());
	}
	private void rescueLvUp(NetClient client, Object message) {
		DevelopRescueLvUpRequest_25000007 req = (DevelopRescueLvUpRequest_25000007) message;
		DevelopRescueLvUpResponse_25000008.Builder resp = DevelopRescueLvUpResponse_25000008.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());

		int id = req.getId();
		if (!player.isFuncOpen(InitialUI.Consciousness)) {
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
		if (!PlayerHelper.delResources(player, consumeList, OpType.RescueLvUp)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.resource_not_enough.getId());
			return;
		}
		developModule.getPotentiaLvMap().add(id, 1);
		player.handleEvent(EventTypeEnum.Practice);
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
		HeavenlyDaoConfig heavenlyDaoConfig = HeavenlyDaoManager.instance().get(heavenlyDaoLevel);
		int[] taskID = heavenlyDaoConfig.TaskID;
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
		developModule.setHeavenlyDaoLevel(heavenlyDaoLevel + 1);
		player.handleEvent(EventTypeEnum.CultivatesImmortals);
		client.sendProtocol(resp.build());
	}
}

