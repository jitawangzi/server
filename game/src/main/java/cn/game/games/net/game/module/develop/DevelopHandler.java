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
import cn.game.protocol.generated.config.RescueConfig;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.HeavenlyDaoManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.DevelopMsg.DevelopHeavenlyDaoLvUpRequest_25000010;
import cn.game.protocol.protobuf.DevelopMsg.DevelopHeavenlyDaoLvUpResponse_25000011;
import cn.game.protocol.protobuf.DevelopMsg.DevelopPotentialBreakRequest_25000003;
import cn.game.protocol.protobuf.DevelopMsg.DevelopPotentialBreakResponse_25000004;
import cn.game.protocol.protobuf.DevelopMsg.DevelopPotentialLvUpRequest_25000001;
import cn.game.protocol.protobuf.DevelopMsg.DevelopPotentialLvUpResponse_25000002;
import cn.game.protocol.protobuf.DevelopMsg.DevelopRescueLvUpRequest_25000007;
import cn.game.protocol.protobuf.DevelopMsg.DevelopRescueLvUpResponse_25000008;
import cn.game.protocol.protobuf.PbProtocol;

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

