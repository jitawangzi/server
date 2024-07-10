package cn.game.games.net.game.module.draw;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.DrawConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.DrawManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
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
		int drawCount = ten ? 10 : 1;
		DrawConfig drawConfig = DrawManager.instance().get(id);

		List<SimpleEntry<Integer, Integer>> costEntries = new ArrayList<>();
		if (!freeOnce) {
			int costItemId = drawConfig.DrawConsumeId[0];
			if (ten) {
				int costItemCount = drawConfig.DrawConsumeId[1] * drawCount;
				int count = (int) player.getItemModule().getCount(costItemId);
				if (count > 0) {
					costEntries.add(new SimpleEntry(costItemId, count > costItemCount ? costItemCount : count));
				}
				if (count < costItemCount) {
					boolean useAnother = false;
					for (int[] consume : GlobalConst.GachaConsume) {
						if (consume[0] == costItemId) {
							costEntries.add(new SimpleEntry(consume[1], (consume[2] * (costItemCount - count))));
							useAnother = true;
							break;
						}
					}
					if (!useAnother) {
                        client.sendProtocol(resp.build(), ErrorMsgEnum.resource_not_enough.getId());
                        return;
                    }
				}

			}else {
				costEntries.add(new SimpleEntry<>(costItemId, drawConfig.DrawConsumeId[1]));
			}

		}
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
			boolean delResources = PlayerHelper.delResources(player, costEntries, OpType.Draw);
			if (!delResources) {
				client.sendProtocol(resp, ErrorMsgEnum.resource_not_enough.getId());
				return;
			}
		}
		List<List<RewardInfo>> allRewards = drawModule.draw(id, drawCount, freeOnce);
//		int gold = drawConfig.DrawMoney * drawCount;

		player.handleEvent(EventTypeEnum.Draw, drawCount);

		resp.addAllRewards(allRewards.get(0));
		resp.addAllHeros(allRewards.get(1));
//		resp.setGold(gold);
		resp.setDraw(drawModule.buildDrawInfo(id));

		client.sendProtocol(resp.build());
	}

}
