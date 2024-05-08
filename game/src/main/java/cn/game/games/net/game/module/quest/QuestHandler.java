package cn.game.games.net.game.module.quest;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerExt;
import cn.game.games.cache.entity.Quest;
import cn.game.games.cache.entity.QuestChallenge;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.QuestHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.util.DAO;
import cn.game.games.util.PbBuilder;
import cn.game.protocol.generated.config.MissionChallengeGroupConfig;
import cn.game.protocol.generated.config.MissionDailyConfig;
import cn.game.protocol.generated.config.QuestConfig;
import cn.game.protocol.generated.enume.QuestTypeEnum;
import cn.game.protocol.generated.manager.MissionChallengeGroupManager;
import cn.game.protocol.generated.manager.MissionDailyManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.QuestMsg.QuestAcceptRequest_20000026;
import cn.game.protocol.protobuf.QuestMsg.QuestAcceptResponse_20000027;
import cn.game.protocol.protobuf.QuestMsg.QuestBranchPriorityRequest_20000028;
import cn.game.protocol.protobuf.QuestMsg.QuestBranchPriorityResponse_20000029;
import cn.game.protocol.protobuf.QuestMsg.QuestChallengeGroupDetailRequest_20000022;
import cn.game.protocol.protobuf.QuestMsg.QuestChallengeGroupDetailResponse_20000023;
import cn.game.protocol.protobuf.QuestMsg.QuestChallengeGroupRequest_20000020;
import cn.game.protocol.protobuf.QuestMsg.QuestChallengeGroupResponse_20000021;
import cn.game.protocol.protobuf.QuestMsg.QuestChooseRewardRequest_20000033;
import cn.game.protocol.protobuf.QuestMsg.QuestChooseRewardResponse_20000034;
import cn.game.protocol.protobuf.QuestMsg.QuestListRequest_20000001;
import cn.game.protocol.protobuf.QuestMsg.QuestListResponse_20000002;
import cn.game.protocol.protobuf.QuestMsg.QuestReceiveActiveRequest_20000008;
import cn.game.protocol.protobuf.QuestMsg.QuestReceiveActiveResponse_20000009;
import cn.game.protocol.protobuf.QuestMsg.QuestReceiveRequest_20000004;
import cn.game.protocol.protobuf.QuestMsg.QuestReceiveResponse_20000005;
import cn.game.protocol.protobuf.QuestMsg.QuestUpdateRequest_20000030;
import cn.game.protocol.protobuf.QuestMsg.QuestUpdateResponse_20000031;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

@Component
public class QuestHandler extends BaseHandler {

	@Override
	protected int getModule() {
		return 0x20;
	}

	@Override
	protected void inititialize() {

		putInvoker(PbProtocol.QuestListRequest_20000001, this::list);
		putInvoker(PbProtocol.QuestReceiveRequest_20000004, this::receive);
		putInvoker(PbProtocol.QuestChooseRewardRequest_20000033, this::chooseReward);
		putInvoker(PbProtocol.QuestReceiveActiveRequest_20000008, this::activeReceive);
		putInvoker(PbProtocol.QuestChallengeGroupRequest_20000020, this::group);
		putInvoker(PbProtocol.QuestChallengeGroupDetailRequest_20000022, this::groupDetail);
		putInvoker(PbProtocol.QuestAcceptRequest_20000026, this::accept);
		putInvoker(PbProtocol.QuestBranchPriorityRequest_20000028, this::branchPriority);
		putInvoker(PbProtocol.QuestUpdateRequest_20000030, this::update);
	}

	protected void update(NetClient client, Object message) {
		QuestUpdateRequest_20000030 req = (QuestUpdateRequest_20000030) message;
		QuestUpdateResponse_20000031.Builder resp = QuestUpdateResponse_20000031.newBuilder();
		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		int id = req.getId();
		int index = req.getIndex();
		int count = req.getCount();

		// 是否前端触发
		QuestConfig missionConfig = QuestHelper.getQuestConfig(id);
		/*		if (!missionConfig.getIsClentUpdate()) {
<<<<<<< HEAD
					client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
=======
					client.sendProtocol(resp, OldErrorMsgEnum.player_check_error.getId());
>>>>>>> branch 'dev' of http://10.1.10.103:3800/project-server/server.git
					return;
				}*/
		QuestModule questOp = player.getModule(QuestModule.class);
		Quest quest = questOp.get(id);
		if (quest == null) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}
		if (quest.getState() < QuestHelper.ACCEPTED) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}
		quest.getConditionContainer().addCount(index, count);
		client.sendProtocol(resp);
	}

	protected void branchPriority(NetClient client, Object message) {
		QuestBranchPriorityRequest_20000028 req = (QuestBranchPriorityRequest_20000028) message;
		QuestBranchPriorityResponse_20000029.Builder resp = QuestBranchPriorityResponse_20000029.newBuilder();
		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		int group = req.getGroup();
		QuestModule questOp = player.getModule(QuestModule.class);
		boolean hasBranchGroup = questOp.hasBranchGroup(group);
		if (!hasBranchGroup) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}
//		PlayerExt playerExt = PlayerManager.getInstance().getPlayer(playerId).getExt();
//		playerExt.setBranchGroup(group);

		PlayerExt update = PlayerExt.valueOf(playerId);
		update.setBranchGroup(group);
		DAO.updateSelective(update);

		client.sendProtocol(resp);
	}
	protected void accept(NetClient client, Object message) {
		QuestAcceptRequest_20000026 req = (QuestAcceptRequest_20000026) message;
		QuestAcceptResponse_20000027.Builder resp = QuestAcceptResponse_20000027.newBuilder();
		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		int id = req.getId();
		QuestModule questOp = player.getModule(QuestModule.class);
		Quest quest = questOp.get(id);
		if (quest == null) { // 接任务之前应该已经有了 
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}
		if (!questOp.canAccept(id)) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}

		questOp.setState(quest, QuestHelper.ACCEPTED);

		resp.setQuest(PbBuilder.buildQuestInfo(quest));
		client.sendProtocol(resp);
	}
	protected void groupDetail(NetClient client, Object message) {
		QuestChallengeGroupDetailRequest_20000022 req = (QuestChallengeGroupDetailRequest_20000022) message;
		QuestChallengeGroupDetailResponse_20000023.Builder resp = QuestChallengeGroupDetailResponse_20000023.newBuilder();
		int id = req.getId();
		
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		QuestModule questOp = player.getModule(QuestModule.class);

		Map<Integer, QuestChallenge> challenges = questOp.getChallenges();
		QuestChallenge questChallenge = challenges.get(id);
		if (questChallenge != null) {
			MissionChallengeGroupConfig mission = MissionChallengeGroupManager.getInstance().getMissionChallengeGroupConfig(id);
			List<Integer> ids = mission.getMissionId();
			for (Integer qid : ids) {
				Quest quest = questOp.get(qid);
				if (quest != null) {
					resp.addQuests(PbBuilder.buildQuestInfo(quest));
				}
			}
		}

		client.sendProtocol(resp);
	}

	protected void group(NetClient client, Object message) {
		QuestChallengeGroupRequest_20000020 req = (QuestChallengeGroupRequest_20000020) message;
		QuestChallengeGroupResponse_20000021.Builder resp = QuestChallengeGroupResponse_20000021.newBuilder();
		int type = req.getType();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		QuestModule questOp = player.getModule(QuestModule.class);
		Map<Integer, QuestChallenge> challenges = questOp.getChallenges();

		List<MissionChallengeGroupConfig> typeList = MissionChallengeGroupManager.getInstance().getTypeList(type);
		for (MissionChallengeGroupConfig config : typeList) {
			QuestChallenge questChallenge = challenges.get(config.getId());
			if (questChallenge != null) {
				resp.addGroups(PbBuilder.buildQuestChallengeGroupInfo(questChallenge));
			}
		}
		client.sendProtocol(resp.build());
	}
	protected void activeReceive(NetClient client, Object message) {
		QuestReceiveActiveRequest_20000008 req = (QuestReceiveActiveRequest_20000008) message;
		QuestReceiveActiveResponse_20000009.Builder resp = QuestReceiveActiveResponse_20000009.newBuilder();
		int index = req.getIndex();
		long playerId = client.getPlayerId(); Player player = PlayerManager.getInstance().getPlayer(playerId);

		QuestModule questOp = player.getModule(QuestModule.class);
		int finishedCount = questOp.getFinishedCount(QuestTypeEnum.Daily);
		/*		PlayerExt playerExt = PlayerManager.getInstance().getPlayer(playerId).getExt();
				Integer quest = playerExt.getQuestActive();
				boolean one = ByteHelp.isOne(playerExt.getQuestActive(), id);
				if (one) {
					client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
					return;
				}*/
		
		List<MissionDailyConfig> list = MissionDailyManager.getInstance().list();
		boolean canReward = false;
		MissionDailyConfig missionDailyConfig = null;
		for (int i = list.size() - 1; i >= 0; i--) {
			MissionDailyConfig config = list.get(i);
			if (finishedCount >= config.getNumber()) {
				if (index == config.getId()) {
					canReward = true;
					missionDailyConfig = config;
					break;
				}
			}
		}
		if (!canReward) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}
		resp.addAllRewards(PlayerHelper.addResources(playerId, missionDailyConfig.getReward()));

//		playerExt.setQuestActive(ByteHelp.modifyBit(quest, id));

		PlayerExt update = PlayerExt.valueOf(playerId);
//		update.setQuestActive(playerExt.getQuestActive());
		DAO.updateSelective(update);

		client.sendProtocol(resp.build());
	}

	protected void list(NetClient client, Object message) {
		QuestListRequest_20000001 req = (QuestListRequest_20000001) message;
		QuestListResponse_20000002.Builder resp = QuestListResponse_20000002.newBuilder();
		int group = req.getType();
		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		resp.addAllQuests(PbBuilder.buildQuestByGroup(playerId, QuestTypeEnum.get(group)));
		if (group == QuestTypeEnum.BranchLine.ID) {
//			PlayerExt playerExt = PlayerManager.getInstance().getPlayer(playerId).getExt();
//			resp.setPriorityBranch(playerExt.getBranchGroup());
		}
//		resp.addAllGroups(PbBuilder.buildQuestStateByGroup(client.getPlayerId()));
//		resp.setType(type);
//		resp.setGroup(group);
		client.sendProtocol(resp);
	}

	protected void chooseReward(NetClient client, Object message) {
		QuestChooseRewardRequest_20000033 req = (QuestChooseRewardRequest_20000033) message;
		QuestChooseRewardResponse_20000034.Builder resp = QuestChooseRewardResponse_20000034.newBuilder();

		int id = req.getId();
		int index = req.getIndex();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		QuestModule questOp = player.getModule(QuestModule.class);

		List<RewardInfo> rewards = questOp.receive(id, index);
		if (rewards.isEmpty()) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}
		resp.addAllRewards(rewards);
		client.sendProtocol(resp);
	}
	protected void receive(NetClient client, Object message) {
		QuestReceiveRequest_20000004 req = (QuestReceiveRequest_20000004) message;
		QuestReceiveResponse_20000005.Builder resp = QuestReceiveResponse_20000005.newBuilder();

		List<Integer> ids = req.getIdsList();
//		int group = req.getGroup();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		QuestModule questOp = player.getModule(QuestModule.class);

//		List<Integer> ret = new ArrayList<>();
//		if (id > 0) {
//			ret.add(id);
//		} else {
//			ret.addAll(questOp.canReceiveIds(group));
//		}
		// 暂时最多领10个
		// if (ret.size() > 10) {
		// ret = ret.subList(0, 10);
		// }
		List<RewardInfo> rewards = questOp.receive(ids);
		if (rewards.isEmpty()) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}
//		resp.addAllIds(ret);
		resp.addAllRewards(rewards);
//		resp.setGroup(group);
		client.sendProtocol(resp);
	}
}
