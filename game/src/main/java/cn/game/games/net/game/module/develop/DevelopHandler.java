package cn.game.games.net.game.module.develop;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.Quest;
import cn.game.games.net.game.helper.QuestHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.protocol.generated.config.HeavenlyDaoConfig;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.HeavenlyDaoManager;
import cn.game.protocol.manual.ErrorMsgEnum;
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
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		client.sendProtocol(resp.build());
	}

	private void potentialBreak(NetClient client, Object message) {
		DevelopPotentialBreakRequest_25000003 req = (DevelopPotentialBreakRequest_25000003) message;
		DevelopPotentialBreakResponse_25000004.Builder resp = DevelopPotentialBreakResponse_25000004.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		client.sendProtocol(resp.build());
	}
	private void rescueLvUp(NetClient client, Object message) {
		DevelopRescueLvUpRequest_25000007 req = (DevelopRescueLvUpRequest_25000007) message;
		DevelopRescueLvUpResponse_25000008.Builder resp = DevelopRescueLvUpResponse_25000008.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
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
		client.sendProtocol(resp.build());
	}
}

