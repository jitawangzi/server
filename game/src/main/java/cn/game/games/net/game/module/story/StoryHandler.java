package cn.game.games.net.game.module.story;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.StoryMsg.StoryFinishRequest_14000003;
import cn.game.protocol.protobuf.StoryMsg.StoryFinishResponse_14000004;
import cn.game.protocol.protobuf.StoryMsg.StoryStartRequest_14000001;
import cn.game.protocol.protobuf.StoryMsg.StoryStartResponse_14000002;

@Component
public class StoryHandler extends BaseHandler {

    @Override
    protected int getModule() {
		return 0x14;
    }

    @Override
    protected void inititialize() {

		putInvoker(PbProtocol.StoryStartRequest_14000001, this::start);
		putInvoker(PbProtocol.StoryFinishRequest_14000003, this::finish);
    }

	private void start(NetClient client, Object message) {
		StoryStartRequest_14000001 request = (StoryStartRequest_14000001) message;
		StoryStartResponse_14000002.Builder resp = StoryStartResponse_14000002.newBuilder();

		int id = request.getId();
		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);

//		StoryConfig storyConfig = StoryManager.getInstance().getStoryConfig(id);

//		boolean checkCondition = PlayerHelper.checkCondition(player, storyConfig.getCondition());
//		if (!checkCondition) {
//			client.sendProtocol(resp.build(), ErrorMsgEnum.story_unlock_condition_err.getId());
//			return;
//		}
//		StoryModule storyModule = player.getModule(StoryModule.class);

//		if (!storyModule.isFinish(storyConfig.getStoryCondition())) {
//			client.sendProtocol(resp.build(), ErrorMsgEnum.story_pre_not_finish.getId());
//			return;
//		}
//		boolean update = storyModule.update(id, 1, false);
//		if (!update) {
//			client.sendProtocol(resp.build(), ErrorMsgEnum.story_start_repeated.getId());
//			return;
//		}

//		Story story = storyModule.get(id);

//		client.sendProtocol(resp.setStory(story.toStoryInfo()));

	}
	private void finish(NetClient client, Object message) {
		StoryFinishRequest_14000003 request = (StoryFinishRequest_14000003) message;
		StoryFinishResponse_14000004.Builder resp = StoryFinishResponse_14000004.newBuilder();
		int id = request.getId();
		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		StoryModule storyModule = player.getModule(StoryModule.class);
		boolean finish = storyModule.finish(id);
		if (!finish) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_check_error.getId());
			return;
		}
//		StoryConfig storyConfig = StoryManager.getInstance().getStoryConfig(id);

//		List<RewardInfo> addResources = PlayerHelper.addResources(player, storyConfig.getReward());
//		resp.addAllRewards(addResources);
//		PlayerHelper.command(playerId, storyConfig.getCommandList());
		client.sendProtocol(resp.build());
	}
}
