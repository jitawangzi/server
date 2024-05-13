package cn.game.games.net.game.module.draw;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.DrawConfig;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.DrawManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.ResourceConsumeEnum;
import cn.game.protocol.protobuf.DrawMsg.DrawListRequest_37000001;
import cn.game.protocol.protobuf.DrawMsg.DrawListResponse_37000002;
import cn.game.protocol.protobuf.DrawMsg.DrawRequest_37000003;
import cn.game.protocol.protobuf.DrawMsg.DrawResponse_37000004;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;

@Component
public class DrawHandler extends BaseHandler {
	@Override
	protected int getModule() {
		return 0x37;
	}

	@Override
	protected void inititialize() {
		putInvoker(PbProtocol.DrawListRequest_37000001, this::page);
		putInvoker(PbProtocol.DrawRequest_37000003, this::draw);
	}

	private void page(NetClient client, Object message) {
		DrawListRequest_37000001 req = (DrawListRequest_37000001) message;
		DrawListResponse_37000002.Builder resp = DrawListResponse_37000002.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		DrawModule drawModule = player.getModule(DrawModule.class);

		if (!player.isFuncOpen(InitialUI.PleaseGod)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}

		resp.setDraw(drawModule.buildDrawInfo(req.getId()));

		client.sendProtocol(resp.build());
	}

	private void draw(NetClient client, Object message) {
		DrawRequest_37000003 req = (DrawRequest_37000003) message;
		DrawResponse_37000004.Builder resp = DrawResponse_37000004.newBuilder();
		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		if (!player.isFuncOpen(InitialUI.PleaseGod)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}
		DrawModule drawModule = player.getModule(DrawModule.class);
		boolean ten = req.getTen();
		boolean freeOnce = req.getFreeOnce();
		int id = req.getId();
		DrawConfig drawConfig = DrawManager.instance().get(id);
		if (freeOnce) {
			int nextFreeTime = drawModule.getNextFreeTime(id);
			if (nextFreeTime != 0 && nextFreeTime < DateUtil.currentTimeSeconds()) {
				client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
				return;
			}
			if (ten) {
				client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
				return;
			}
		} else {
			boolean delResources = PlayerHelper.delResources(player, ten ? drawConfig.DrawConsumeId[1] : drawConfig.DrawConsumeId[0], ResourceConsumeEnum.Draw);
			if (!delResources) {
				client.sendProtocol(resp, ErrorMsgEnum.resource_not_enough.getId());
				return;
			}
		}
		int count = ten ? 10 : 1;
		List<List<RewardInfo>> allRewards = drawModule.draw(id, count, freeOnce);
		int gold = drawConfig.DrawMoney * count;

		player.handleEvent(EventTypeEnum.Draw, count);

		resp.addAllRewards(allRewards.get(0));
		resp.addAllHeros(allRewards.get(1));
		resp.setGold(gold);
		resp.setDraw(drawModule.buildDrawInfo(id));

		client.sendProtocol(resp.build());
	}

}
