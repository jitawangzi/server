package cn.game.games.net.game.module.rank;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.BaseMsg.PlayerRankInfo;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RankMsg.RankInfo;
import cn.game.protocol.protobuf.RankMsg.RankListRequest_35000001;
import cn.game.protocol.protobuf.RankMsg.RankListResponse_35000002;

@Component
public class RankHandler extends BaseHandler {

    @Override
    protected int getModule() {
        return 0x35;
    }

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.RankListRequest_35000001, this::list);
    }

    private void list(NetClient client, Object message) {
        RankListRequest_35000001 req = (RankListRequest_35000001) message;
        int type = req.getType();
        int page = req.getPage();
        int pageSize = req.getPageSize();

        RankListResponse_35000002 defaultInstance = RankListResponse_35000002.getDefaultInstance();
		RankType rankType = RankType.get(type);
		if (pageSize > 100) {
			client.sendProtocol(defaultInstance, ErrorMsgEnum.request_parameter_error.ID);
			return;
		}
		RankListResponse_35000002.Builder resp = RankListResponse_35000002.newBuilder();

        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		RankModule rankModule = player.getModule(RankModule.class);
		CompletionStage<RankEntry> rankAsync = RankService.getInstance().getRankEntryAsync(player.getServerId(), rankType, player.getPlayerId());
		CompletionStage<List<RankEntry>> pageAsync = RankService.getInstance().getPageAsync(player.getServerId(), rankType, page, pageSize);
		CompletionStage<List<PlayerRank>> playerRankAsync = RankService.getInstance().convertToPlayerRankEntries(pageAsync);
		rankAsync.thenCombine(playerRankAsync, (rank, rankEntries) -> {
			RankInfo.Builder rankInfo = RankInfo.newBuilder();
			rankEntries.forEach(entry -> {
				PlayerRankInfo.Builder rb = PlayerRankInfo.newBuilder();

				rb.setRank(entry.getRankEntry().getRank());
				rb.setPlayer(entry.getPlayer().toSimplePlayerInfo());
				long score = entry.getRankEntry().getScore();
				rb.setScore((score < 0 ? 0 : score) + "");
				rankInfo.addPlayers(rb);
			});
			rankInfo.setRank(rank == null ? -1 : rank.getRank());
			rankInfo.setScore(rank != null ? rank.getScore() + "" : rankModule.getScore(rankType));
			resp.setRankInfo(rankInfo);
			client.sendProtocol(resp.build());
			return null;
		}).exceptionally(e -> {
			log.error("", e);
			client.sendProtocol(defaultInstance, ErrorMsgEnum.unknown.ID);
			return null;
		});
    }
}
