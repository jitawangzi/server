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
import cn.game.games.net.game.module.shop.monthcard.MonthCardModule;
import cn.game.protocol.generated.config.DrawConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.DrawManager;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.DrawMsg.DrawHeroInfoRequest_37000011;
import cn.game.protocol.protobuf.DrawMsg.DrawHeroInfoResponse_37000012;
import cn.game.protocol.protobuf.DrawMsg.DrawHeroRecruitRequest_37000015;
import cn.game.protocol.protobuf.DrawMsg.DrawHeroRecruitResponse_37000016;
import cn.game.protocol.protobuf.DrawMsg.DrawHeroRefreshRequest_37000013;
import cn.game.protocol.protobuf.DrawMsg.DrawHeroRefreshResponse_37000014;
import cn.game.protocol.protobuf.DrawMsg.DrawHeroWishRequest_37000005;
import cn.game.protocol.protobuf.DrawMsg.DrawHeroWishResponse_37000006;
import cn.game.protocol.protobuf.DrawMsg.DrawListRequest_37000001;
import cn.game.protocol.protobuf.DrawMsg.DrawListResponse_37000002;
import cn.game.protocol.protobuf.DrawMsg.DrawRequest_37000003;
import cn.game.protocol.protobuf.DrawMsg.DrawResponse_37000004;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;
import cn.game.util.GameUtil;

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
        putInvoker(PbProtocol.DrawHeroWishRequest_37000005, this::heroWish);
        putInvoker(PbProtocol.DrawHeroInfoRequest_37000011, this::heroInfo);
        putInvoker(PbProtocol.DrawHeroRefreshRequest_37000013, this::heroRefresh);
        putInvoker(PbProtocol.DrawHeroRecruitRequest_37000015, this::heroRecruit);
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
        int id = req.getId();
        DrawConfig drawConfig = DrawManager.instance().get(id);
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        if (!player.isFuncOpen(InitialUI.get(drawConfig.OpenLevel))) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
            return;
        }
        DrawModule drawModule = player.getModule(DrawModule.class);
        boolean ten = req.getTen();
        boolean freeOnce = req.getFreeOnce();
        int countReq = req.getCount();
        int drawCount = countReq > 0 ? countReq : ten ? 10 : 1;
        if (drawCount > GlobalConst.SpecialOfferGiftPackRaffle) {
            drawCount = GlobalConst.SpecialOfferGiftPackRaffle;
        }
        List<SimpleEntry<Integer, Integer>> costEntries = new ArrayList<>();
        if (!freeOnce) {
            int costItemId = drawConfig.DrawConsumeId[0];
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
            player.handleEvent(EventTypeEnum.WatchAds);
        } else {
			PlayerHelper.delResources(player, costEntries, OpType.Draw);
        }
        List<List<RewardInfo>> allRewards = drawModule.draw(id, drawCount, freeOnce);
        for (int i = 0; i < drawCount; i++) {
            player.handleEvent(EventTypeEnum.Draw, 1, id);
        }
        resp.addAllRewards(allRewards.get(0));
        resp.addAllHeros(allRewards.get(1));
        //		resp.setGold(gold);
        resp.setDraw(drawModule.buildDrawInfo(id));
        client.sendProtocol(resp.build());
    }

    private void heroWish(NetClient client, Object message) {
        DrawHeroWishRequest_37000005 req = (DrawHeroWishRequest_37000005) message;
        int heroId = req.getHeroId();
        DrawHeroWishResponse_37000006 defaultInstance = DrawHeroWishResponse_37000006.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.PleaseGod)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        boolean battlePass = player.getChapterModule().isBattlePass(GlobalConst.OrientationFree);
        MonthCardModule monthCardModule = player.getModule(MonthCardModule.class);
        if ((monthCardModule.getMonthCard(1) == null || monthCardModule.getMonthCard(2) == null) && !battlePass) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.condition_check_error.ID);
            return;
        }
        if (!GameUtil.contains(GlobalConst.DirectionalDraw, heroId)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.request_parameter_error.ID);
            return;
        }
        HeroManager.instance().get(heroId);
        DrawModule drawModule = player.getModule(DrawModule.class);
        drawModule.setWishHeroId(heroId);
        client.sendProtocol(defaultInstance);
    }

    private void heroInfo(NetClient client, Object message) {
        DrawHeroInfoRequest_37000011 req = (DrawHeroInfoRequest_37000011) message;
        DrawHeroInfoResponse_37000012 defaultInstance = DrawHeroInfoResponse_37000012.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.PleaseGod)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        DrawHeroInfoResponse_37000012.Builder resp = DrawHeroInfoResponse_37000012.newBuilder();
        client.sendProtocol(resp.build());
    }

    private void heroRefresh(NetClient client, Object message) {
        DrawHeroRefreshRequest_37000013 req = (DrawHeroRefreshRequest_37000013) message;
        DrawHeroRefreshResponse_37000014 defaultInstance = DrawHeroRefreshResponse_37000014.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.PleaseGod)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        DrawHeroRefreshResponse_37000014.Builder resp = DrawHeroRefreshResponse_37000014.newBuilder();
        client.sendProtocol(resp.build());
    }

    private void heroRecruit(NetClient client, Object message) {
        DrawHeroRecruitRequest_37000015 req = (DrawHeroRecruitRequest_37000015) message;
        int multiple = req.getMultiple();
        DrawHeroRecruitResponse_37000016 defaultInstance = DrawHeroRecruitResponse_37000016.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.PleaseGod)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }
        DrawHeroRecruitResponse_37000016.Builder resp = DrawHeroRecruitResponse_37000016.newBuilder();
        client.sendProtocol(resp.build());
    }
}
