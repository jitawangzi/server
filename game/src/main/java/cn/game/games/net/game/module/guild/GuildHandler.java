package cn.game.games.net.game.module.guild;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.google.protobuf.Message;
import cn.game.core.base.ServerContext;
import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.cross.guild.GuildHelper;
import cn.game.games.net.cross.guild.SimpleGuild;
import cn.game.games.net.cross.guild.service.GuildServiceInterface;
import cn.game.games.net.game.GameServer;
import cn.game.games.net.game.handler.GameBaseHandler;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.award.RewardHelper;
import cn.game.games.net.game.module.rank.RankEntry;
import cn.game.games.net.game.module.rank.RankHelper;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.GuildBargainConfig;
import cn.game.protocol.generated.config.GuildDonateConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.GuildBargainManager;
import cn.game.protocol.generated.manager.GuildDonateManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.GuildCrossMsg.GuildMsgPush_41000045;
import cn.game.protocol.protobuf.GuildCrossMsg.GuildMsgResponse_41000046;
import cn.game.protocol.protobuf.GuildMsg;
import cn.game.protocol.protobuf.GuildMsg.GuildAllInfo;
import cn.game.protocol.protobuf.GuildMsg.GuildApplyJoinResponse_40000008;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyAcceptRequest_40000070;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyAcceptResponse_40000071;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyBattleEndRequest_40000078;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyBattleEndResponse_40000079;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyBattleReportRequest_4000007a;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyBattleReportResponse_4000007b;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyBattleStartRequest_40000076;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyBattleStartResponse_40000077;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyPlayerRequest_4000007c;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyPlayerResponse_4000007d;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyRewardRequest_40000072;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyRewardResponse_40000073;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyTargetRefreshRequest_40000074;
import cn.game.protocol.protobuf.GuildMsg.GuildBountyTargetRefreshResponse_40000075;
import cn.game.protocol.protobuf.GuildMsg.GuildCreateResponse_40000006;
import cn.game.protocol.protobuf.GuildMsg.GuildDonateRequest_40000067;
import cn.game.protocol.protobuf.GuildMsg.GuildDonateResponse_40000068;
import cn.game.protocol.protobuf.GuildMsg.GuildMemberPositionSetResponse_40000016;
import cn.game.protocol.protobuf.GuildMsg.GuildRankList;
import cn.game.protocol.protobuf.GuildMsg.GuildRankListRequest_40000081;
import cn.game.protocol.protobuf.GuildMsg.GuildRankListResponse_40000082;
import cn.game.protocol.protobuf.GuildMsg.GuildServiceInfo;
import cn.game.protocol.protobuf.GuildMsg.GuildShowInfo;
import cn.game.protocol.protobuf.GuildMsg.GuildSimpleInfo;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.IntMapWrapper;
import cn.game.util.ServerType;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import cn.game.protocol.protobuf.GuildMsg.GuildGVECardDataRequest_40000083;
import cn.game.protocol.protobuf.GuildMsg.GuildGVECardDataResponse_40000084;
import cn.game.protocol.protobuf.GuildMsg.GuildGVEOpenCardRequest_40000085;
import cn.game.protocol.protobuf.GuildMsg.GuildGVEOpenCardResponse_40000086;
import cn.game.protocol.protobuf.GuildMsg.GuildGVEOpenMapRequest_40000087;
import cn.game.protocol.protobuf.GuildMsg.GuildGVEOpenMapResponse_40000088;
import cn.game.protocol.protobuf.GuildMsg.GuildGVEBuyTicketRequest_40000090;
import cn.game.protocol.protobuf.GuildMsg.GuildGVEBuyTicketResponse_40000091;

@Component
public class GuildHandler extends GameBaseHandler {

    static Logger log = LoggerFactory.getLogger(GuildHandler.class);

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.GuildListRequest_40000001, this::guildList);
        putInvoker(PbProtocol.GuildFindRequest_40000003, this::findGuild);
        putInvoker(PbProtocol.GuildCreateRequest_40000005, this::createGuild);
        putInvoker(PbProtocol.GuildApplyJoinRequest_40000007, this::applyJoinGuild);
        putInvoker(PbProtocol.GuildDissolveRequest_40000011, this::dissolveGuild);
        putInvoker(PbProtocol.GuildSettingRequest_40000013, this::setGuildSetting);
        putInvoker(PbProtocol.GuildMemberPositionSetRequest_40000015, this::setGuildMemberPosition);
        putInvoker(PbProtocol.GuildQuitRequest_40000017, this::quitGuild);
        putInvoker(PbProtocol.GuildInfoRequest_40000021, this::getGuildInfo);
        putInvoker(PbProtocol.GuildLogRequest_40000025, this::getGuildLogs);
        putInvoker(PbProtocol.GuildMemberAuthRequest_40000041, this::updateMemberAuth);
        putInvoker(PbProtocol.GuildBargainRequest_40000060, this::bargain);
        putInvoker(PbProtocol.GuildBargainBuyRequest_40000062, this::buyBargain);
        putInvoker(PbProtocol.GuildQuickJoinRequest_40000065, this::quickJoinGuild);
        putInvoker(PbProtocol.GuildBountyAcceptRequest_40000070, this::bountyAccept);
        putInvoker(PbProtocol.GuildBountyTargetRefreshRequest_40000074, this::bountyTargetRefresh);
        putInvoker(PbProtocol.GuildBountyBattleStartRequest_40000076, this::bountyBattleStart);
        putInvoker(PbProtocol.GuildBountyBattleEndRequest_40000078, this::bountyBattleEnd);
        putInvoker(PbProtocol.GuildBountyBattleReportRequest_4000007a, this::bountyBattleReport);
        putInvoker(PbProtocol.GuildBountyPlayerRequest_4000007c, this::bountyPlayer);
        putInvoker(PbProtocol.GuildBountyRewardRequest_40000072, this::bountyReward);
        putInvoker(PbProtocol.GuildDonateRequest_40000067, this::donate);
        putInvoker(PbProtocol.GuildRankListRequest_40000081, this::rankList);
        putInvoker(PbProtocol.GuildGVECardDataRequest_40000083, this::gVECardData);
        putInvoker(PbProtocol.GuildGVEOpenCardRequest_40000085, this::gVEOpenCard);
        putInvoker(PbProtocol.GuildGVEOpenMapRequest_40000087, this::gVEOpenMap);
        putInvoker(PbProtocol.GuildGVEBuyTicketRequest_40000090, this::gVEBuyTicket);
    }

    @Override
    protected int getModule() {
        return 0x40;
    }

    @Override
    protected InitialUI getInitialUI() {
        return InitialUI.Guild;
    }

    // 一键快速加入公会
    private void quickJoinGuild(NetClient client, Object o) {
        GuildMsg.GuildQuickJoinRequest_40000065 req = (GuildMsg.GuildQuickJoinRequest_40000065) o;
        GuildMsg.GuildQuickJoinResponse_40000066.Builder res = GuildMsg.GuildQuickJoinResponse_40000066.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        GuildModule guildModule = player.getGuildModule();
        if (guildModule.getDisbandCount() > 0 && System.currentTimeMillis() < guildModule.getNextJoinTimer()) {
            client.sendProtocol(res.build(), ErrorMsgEnum.cd_time_error.ID);
            return;
        }
        if (player.getGuildId() > 0) {
            client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_exist.ID);
            return;
        }
        GuildServiceInfo joined = null;
        List<GuildServiceInterface> allServerInterface = ServerHelper.getAllServerInterface(ServerType.Cross, GuildServiceInterface.class);
        for (GuildServiceInterface guildServiceInterface : allServerInterface) {
            GuildServiceInfo randomJoin = guildServiceInterface.randomJoin(player.getPlayerId());
            if (randomJoin != null) {
                joined = randomJoin;
                break;
            }
        }
        if (joined != null) {
            GuildSimpleInfo simpleInfo = joined.getShowInfo().getSimpleInfo();
            guildModule.join(simpleInfo.getId());
            GuildAllInfo allInfo = GuildHelper.buildAllInfo(joined, player.getPlayerId());
            res.setGuild(allInfo);
        }
        client.sendProtocol(res.build());
    }

    private void bargain(NetClient client, Object o) {
        GuildMsg.GuildBargainRequest_40000060 req = (GuildMsg.GuildBargainRequest_40000060) o;
        GuildMsg.GuildBargainResponse_40000061.Builder res = GuildMsg.GuildBargainResponse_40000061.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (player.getGuildId() < 0) {
            client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_not_exist.ID);
            return;
        }
        GuildModule guildModule = player.getGuildModule();
        long joinTime = guildModule.getJoinTime();
        if (guildModule.getDisbandCount() > 0 && (System.currentTimeMillis() - joinTime) / 1000 < GlobalConst.GuildBargainCD) {
            client.sendProtocol(res.build(), ErrorMsgEnum.cd_time_error.ID);
            return;
        }
        int bargainCount = guildModule.getBargainCount();
        if (bargainCount >= GlobalConst.GuildBargainMax) {
            client.sendProtocol(res.build(), ErrorMsgEnum.times_limit.ID);
            return;
        }
        // 先扣次数
        log.info("guild bargain BIBIBI 1");
        PlayerHelper.delResources(player, Asset.GuildBargain.ID, 1, OpType.GuildBargain);
        GuildServiceInterface guildProxy = ServerHelper.getGuildProxy(player.getGuildId());
        int[] ret = guildProxy.bargain(player.getGuildId(), player.getPlayerId());
        GuildBargainConfig guildBargainConfig = GuildBargainManager.instance().get(ret[0]);
        log.info("guild bargain BIBIBI 2");
        List<RewardInfo> resources = PlayerHelper.addResources(player, guildBargainConfig.BargainReward, OpType.GuildBargain);
        res.addAllRewards(resources);
        res.setCount(ret[1]);
        log.info("guild bargain BIBIBI 3");
        player.handleEvent(EventTypeEnum.GuildBargain);
        log.info("guild bargain BIBIBI 4");
        guildModule.setBargainCount(bargainCount + 1);
        client.sendProtocol(res.build());
        log.info("guild bargain BIBIBI 5");
        GameLogger.guildBargain(player, player.getGuildId(), ret[2], ret[1], guildBargainConfig.Price[1] - ret[1]);
    }

    private void buyBargain(NetClient client, Object o) {
        GuildMsg.GuildBargainBuyRequest_40000062 req = (GuildMsg.GuildBargainBuyRequest_40000062) o;
        GuildMsg.GuildBargainBuyResponse_40000063.Builder res = GuildMsg.GuildBargainBuyResponse_40000063.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (player.getGuildId() == 0) {
            client.sendProtocol(res.build(), ErrorMsgEnum.illegal_request.ID);
            return;
        }
        log.info("guild buyBargain BIBIBI 1");
        GuildServiceInterface guildProxy = ServerHelper.getGuildProxy(player.getGuildId());
        int[] bargainPrice = guildProxy.getBargainPrice(player.getGuildId());
        log.info("guild buyBargain BIBIBI 2");
        GuildBargainConfig guildBargainConfig = GuildBargainManager.instance().get(bargainPrice[0]);
        PlayerHelper.delResources(player, guildBargainConfig.Price[0], bargainPrice[1], OpType.GuildBargain);
        List<RewardInfo> rewards = PlayerHelper.addResources(player, guildBargainConfig.Item, OpType.GuildBargain);
        res.addAllRewards(rewards);
        GuildModule guildModule = player.getGuildModule();
        guildModule.setBargainBuy(true);
        client.sendProtocol(res.build());
        log.info("guild buyBargain BIBIBI 3");
        GameLogger.guildBargainPurchase(player, guildBargainConfig.ID, bargainPrice[1]);
    }

    private void updateMemberAuth(NetClient client, Object o) {
        GuildMsg.GuildMemberAuthRequest_40000041 req = (GuildMsg.GuildMemberAuthRequest_40000041) o;
        GuildMsg.GuildMemberAuthResponse_40000042.Builder res = GuildMsg.GuildMemberAuthResponse_40000042.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (player.getGuildId() == 0) {
            client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_not_exist.ID);
            return;
        }
        int optType = req.getOptType();
        // 不能审批自己
        List<Integer> targetPidListList = req.getTargetPidListList();
        if (targetPidListList.contains(player.getPlayerId())) {
            client.sendProtocol(res.build(), ErrorMsgEnum.request_parameter_error.ID);
            return;
        }
        if (optType != 1 && optType != 2 && optType != 3) {
            client.sendProtocol(res.build(), ErrorMsgEnum.request_parameter_error.ID);
            return;
        }
        List<Long> targetPidList = targetPidListList.stream().map(i -> i.longValue()).collect(Collectors.toList());
        GuildServiceInterface guildProxy = ServerHelper.getGuildProxy(player.getGuildId());
        guildProxy.updateMemberAuth(player.getGuildId(), player.getPlayerId(), player.getPlayerName(), optType, targetPidList);
        client.sendProtocol(res.setResult(true).build());
    }

    private void getGuildLogs(NetClient client, Object o) {
        GuildMsg.GuildLogRequest_40000025 req = (GuildMsg.GuildLogRequest_40000025) o;
        GuildMsg.GuildLogResponse_40000026.Builder res = GuildMsg.GuildLogResponse_40000026.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (player.getGuildId() == 0) {
            client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_not_exist.ID);
            return;
        }
        autoForwardGuildServer(client, res, req, null);
    }

    private void getGuildInfo(NetClient client, Object o) {
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        GuildMsg.GuildInfoResponse_40000022.Builder res = GuildMsg.GuildInfoResponse_40000022.newBuilder();
        GuildMsg.GuildInfoRequest_40000021 req = (GuildMsg.GuildInfoRequest_40000021) o;
        if (player.getGuildId() == 0) {
            client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_not_exist.ID);
            return;
        }
        GuildServiceInterface guildProxy = ServerHelper.getGuildProxy(player.getGuildId());
        GuildServiceInfo guildAllInfoForMember = guildProxy.getGuildAllInfoForMember(player.getGuildId());
        GuildAllInfo allInfo = GuildHelper.buildAllInfo(guildAllInfoForMember, player.getPlayerId());
        res.setInfo(allInfo);
        client.sendProtocol(res.build());
    }

    private void autoForwardGuildServer(NetClient client, Message.Builder res, Message req, Function<Message, Void> successCallBack) {
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (player.getGuildId() == 0) {
            client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_not_exist.ID);
            return;
        }
        sendMsgToGuildServer(player, req, player.getPlayerName()).onSuccess(callBack -> {
            if (callBack.errorCode != ErrorMsgEnum.ok.ID) {
                client.sendProtocol(res.build(), callBack.errorCode);
            } else {
                if (successCallBack != null) {
                    successCallBack.apply(callBack.response);
                } else {
                    client.sendProtocol(callBack.response);
                }
            }
        }).onFailure(err -> {
            err.printStackTrace();
            client.sendProtocol(res, ErrorMsgEnum.zong_men_not_exist.ID);
        });
    }

    private void quitGuild(NetClient client, Object o) {
        GuildMsg.GuildQuitRequest_40000017 req = (GuildMsg.GuildQuitRequest_40000017) o;
        GuildMsg.GuildQuitResponse_40000018.Builder res = GuildMsg.GuildQuitResponse_40000018.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        GuildModule guildModule = player.getGuildModule();
        long guildId = guildModule.getGuildId();
        if (guildId <= 0) {
            player.fail(ErrorMsgEnum.zong_men_not_exist);
        }
        GuildServiceInterface guildProxy = ServerHelper.getGuildProxy(guildId);
        guildProxy.quitGuild(guildId, player.getPlayerId(), player.getPlayerName());
        // 退出成功,在退出的推送协议里处理退出，这里先不处理了
        //        guildModule.quit(0);
        client.sendProtocol(res.setResult(true).build());
    }

    private void setGuildMemberPosition(NetClient client, Object o) {
        GuildMsg.GuildMemberPositionSetRequest_40000015 req = (GuildMsg.GuildMemberPositionSetRequest_40000015) o;
        GuildMsg.GuildMemberPositionSetResponse_40000016.Builder res = GuildMsg.GuildMemberPositionSetResponse_40000016.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        long guildId = player.getGuildId();
        if (guildId == 0) {
            client.sendProtocol(res.build(), ErrorMsgEnum.illegal_request.ID);
            return;
        }
        autoForwardGuildServer(client, res, req, (result) -> {
            GuildMemberPositionSetResponse_40000016 response = (GuildMemberPositionSetResponse_40000016) result;
            client.sendProtocol(response);
            GameLogger.guildMemberPositionChange(player, req.getTargetPid(), response.getOldPosition(), response.getPosition());
            return null;
        });
    }

    private void setGuildSetting(NetClient client, Object o) {
        GuildMsg.GuildSettingRequest_40000013 req = (GuildMsg.GuildSettingRequest_40000013) o;
        GuildMsg.GuildSettingResponse_40000014.Builder res = GuildMsg.GuildSettingResponse_40000014.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (player.getGuildId() == 0) {
            client.sendProtocol(res.build(), ErrorMsgEnum.illegal_request.ID);
            return;
        }
        List<String> checkStrs = new ArrayList<>();
        if (!StringUtils.isEmpty(req.getName())) {
            // - 公会名称：需要花费500元宝（GuildNameRevise），最多输入6个字；
            if (req.getName().length() > GlobalConst.GuildName) {
                client.sendProtocol(res.build(), ErrorMsgEnum.not_name.ID);
                return;
            }
            if (!player.isEnough(GlobalConst.GuildNameRevise[0], GlobalConst.GuildNameRevise[1])) {
                client.sendProtocol(res.build(), ErrorMsgEnum.resource_not_enough.ID);
                return;
            }
            // 检查公会名称是否重复
            boolean checkGuildNameRepeat = GuildHelper.checkGuildNameRepeat(req.getName());
            if (checkGuildNameRepeat) {
                client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_name_repeat.ID);
                return;
            }
            checkStrs.add(req.getName());
        }
        if (!StringUtils.isEmpty(req.getNotice())) {
            checkStrs.add(req.getNotice());
        }
        if (!StringUtils.isEmpty(req.getDeclaration())) {
            checkStrs.add(req.getDeclaration());
        }
        if (!StringUtils.isEmpty(req.getWx())) {
            checkStrs.add(req.getWx());
        }
        // 非法字符串检测
        List<CompletableFuture<Boolean>> checkComplatableList = new ArrayList<>();
        for (String str : checkStrs) {
            checkComplatableList.add((CompletableFuture<Boolean>) PlayerHelper.checkContextData(player, str).toCompletionStage());
        }
        CompletableFuture.allOf(checkComplatableList.toArray(new CompletableFuture[0])).thenAcceptAsync(result -> {
            if (checkComplatableList.stream().anyMatch(CompletableFuture::isCompletedExceptionally)) {
                client.sendProtocol(res.build(), ErrorMsgEnum.unknown.ID);
                return;
            }
            for (CompletableFuture<Boolean> future : checkComplatableList) {
                try {
                    if (future.get().booleanValue() == false) {
                        client.sendProtocol(res.build(), ErrorMsgEnum.we_chat_context_check_fail.ID);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    client.sendProtocol(res.build(), ErrorMsgEnum.unknown.ID);
                    return;
                }
            }
            autoForwardGuildServer(client, res, req, message -> {
                if (!StringUtils.isEmpty(req.getName())) {
                    // 公会改名 扣除资源
                    PlayerHelper.delResources(player, GlobalConst.GuildNameRevise, OpType.guildChangeName);
                }
                res.setResult(true);
                client.sendProtocol(res);
                GameLogger.guildInfoChange(player, player.getGuildId(), req.getName() == null ? "null" : req.getName(), req.getIcon());
                return null;
            });
        });
    }

    // 已废弃 不要主动解散公会了
    @Deprecated
    private void dissolveGuild(NetClient client, Object o) {
        GuildMsg.GuildDissolveRequest_40000011 req = (GuildMsg.GuildDissolveRequest_40000011) o;
        GuildMsg.GuildDissolveResponse_40000012.Builder res = GuildMsg.GuildDissolveResponse_40000012.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (player.getGuildId() == 0) {
            client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_not_exist.ID);
            return;
        }
        long guildId = player.getGuildId();
        String guildName = player.getGuildName();
        sendMsgToGuildServer(player, req, "").onSuccess(callBack -> {
            if (callBack.errorCode != ErrorMsgEnum.ok.ID) {
                client.sendProtocol(res.build(), callBack.errorCode);
            } else {
                // 解散公会成功
                GuildModule module = player.getGuildModule();
                module.quit(2);
                module.setDisbandCount(module.getDisbandCount() + 1);
                // 第二次及后续解散时，宗主需要1小时才可加入其它公会（GuildSuzerainCD）；
                if (module.getDisbandCount() > 1) {
                    module.setNextJoinTimer(System.currentTimeMillis() + GlobalConst.GuildSuzerainCD * 1000);
                }
                GameLogger.guildDisband(player, guildId, guildName);
                client.sendProtocol(callBack.response);
            }
        }).onFailure(err -> {
            err.printStackTrace();
            client.sendProtocol(res, ErrorMsgEnum.zong_men_not_exist.ID);
        });
    }

    private void applyJoinGuild(NetClient client, Object o) {
        GuildMsg.GuildApplyJoinRequest_40000007 req = (GuildMsg.GuildApplyJoinRequest_40000007) o;
        GuildMsg.GuildApplyJoinResponse_40000008.Builder res = GuildMsg.GuildApplyJoinResponse_40000008.newBuilder();
        long id = req.getId();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (player.getGuildId() != 0) {
            client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_exist.ID);
            return;
        }
        GuildModule guildModule = player.getGuildModule();
        List<Long> applyJoinList = guildModule.getApplyJoinList();
        //        if (applyJoinList.contains(id)) {
        //            client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_apply_exist.ID);
        //            return;
        //		}
        guildModule.checkJoinCd();
        GuildServiceInterface serviceInterface = GameServer.getInstance().getRemoteCrossServerInterface(GuildServiceInterface.class, DistributedObjectType.GUILD, id);
        GuildServiceInfo guild = serviceInterface.applyJoinGuild(id, player.getPlayerId());
        if (guild != null) {
            GuildSimpleInfo simpleInfo = guild.getShowInfo().getSimpleInfo();
            // 玩家直接加入公会
            guildModule.join(simpleInfo.getId());
            GuildAllInfo allInfo = GuildHelper.buildAllInfo(guild, player.getPlayerId());
            client.sendProtocol(res.setGuild(allInfo).build());
        } else {
            applyJoinList.add(id);
            client.sendProtocol(GuildApplyJoinResponse_40000008.getDefaultInstance());
        }
    }

    private void createGuild(NetClient client, Object o) {
        GuildMsg.GuildCreateRequest_40000005 req = (GuildMsg.GuildCreateRequest_40000005) o;
        GuildMsg.GuildCreateResponse_40000006.Builder res = GuildMsg.GuildCreateResponse_40000006.newBuilder();
        GuildCreateResponse_40000006 defaultInstance = GuildCreateResponse_40000006.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (player.getGuildId() != 0) {
            client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_exist.ID);
            return;
        }
        if (player.getVipLevel() < GlobalConst.GuildCreationVIP) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.level_not_enough.ID);
            return;
        }
        GuildModule guildModule = player.getGuildModule();
        guildModule.checkJoinCd();
        String name = req.getName();
        List<String> checkStrs = new ArrayList<>();
        if (!player.isEnough(GlobalConst.GuildCreationConsume[0], GlobalConst.GuildCreationConsume[1])) {
            client.sendProtocol(res.build(), ErrorMsgEnum.resource_not_enough.ID);
            return;
        }
        if (name == null || name.length() == 0 || name.length() > GlobalConst.GuildName) {
            client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_name_too_long.ID);
            return;
        }
        // 检查公会名称是否重复
        boolean checkGuildNameRepeat = GuildHelper.checkGuildNameRepeat(name);
        if (checkGuildNameRepeat) {
            client.sendProtocol(res.build(), ErrorMsgEnum.zong_men_name_repeat.ID);
            return;
        }
        if (!StringUtils.isEmpty(req.getNotice())) {
            checkStrs.add(req.getNotice());
        }
        if (!StringUtils.isEmpty(req.getDeclaration())) {
            checkStrs.add(req.getDeclaration());
        }
        checkStrs.add(name);
        Future<Boolean> checkFuture = PlayerHelper.checkContextData(player, name);
        // 非法字符串检测
        log.warn("start thread " + Thread.currentThread().getName());
        List<Future<Boolean>> checks = new ArrayList<>(checkStrs.size());
        for (String str : checkStrs) {
            // 应为 Vert.x Future<Boolean>
            Future<Boolean> f = PlayerHelper.checkContextData(player, str);
            checks.add(f);
        }
        checks.add(checkFuture);
        Future.all(checks).compose(cf -> {
            // 验证是否有 false
            for (int i = 0; i < checks.size(); i++) {
                Boolean ok = (Boolean) cf.resultAt(i);
                if (Boolean.FALSE.equals(ok)) {
                    client.sendProtocol(res.build(), ErrorMsgEnum.we_chat_context_check_fail.ID);
                    return Future.failedFuture("context-check-fail");
                }
            }
            // 通过校验，发起远程调用
            GuildServiceInterface guildProxy = ServerHelper.getGuildProxy(0);
            return guildProxy.createGuild(player.getPlayerId(), req.getName(), req.getNotice(), req.getDeclaration(), req.getIcon(), req.getAutoJoin());
        }).onSuccess(r -> {
            ServerContext.getInstance().getProcessor().process(player.getPlayerId(), () -> {
                log.warn("map exec" + Thread.currentThread().getName());
                log.warn("thread=" + Thread.currentThread().getName());
                //    		 log.warn("isEventLoop=" + context.isEventLoopContext());
                //    		 log.warn("isWorker=" + context.isWorkerContext());
                //    		 log.warn("isVirtualThread=" + (context.threadingModel() == ThreadingModel.VIRTUAL_THREAD ? "true":"false"));
                // 创建公会成功
                // 创建公会成功的业务逻辑
                PlayerHelper.delResources(player, GlobalConst.GuildCreationConsume, OpType.guildChangeName);
                GuildSimpleInfo simpleInfo = r.getShowInfo().getSimpleInfo();
                guildModule.join(simpleInfo.getId());
                GuildAllInfo allInfo = GuildHelper.buildAllInfo(r, player.getPlayerId());
                res.setGuild(allInfo);
                client.sendProtocol(res);
                GameLogger.guildCreate(player, simpleInfo.getId(), simpleInfo.getName(), simpleInfo.getIcon());
            });
        }).onFailure(err -> {
            // 如果是校验未通过，前面已返回相应协议；这里兜底
            player.handleFail(err);
        });
    }

    private void findGuild(NetClient client, Object o) {
        GuildMsg.GuildFindRequest_40000003 req = (GuildMsg.GuildFindRequest_40000003) o;
        GuildMsg.GuildFindResponse_40000004.Builder res = GuildMsg.GuildFindResponse_40000004.newBuilder();
        long guildId = req.getId();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (guildId <= 0) {
            player.fail(ErrorMsgEnum.request_parameter_error, "guildId <= 0");
        }
        GuildServiceInterface serviceInterface = GameServer.getInstance().getRemoteCrossServerInterface(GuildServiceInterface.class, DistributedObjectType.GUILD, guildId);
        GuildShowInfo guildShowInfo = serviceInterface.getGuildShowInfo(guildId);
        if (guildShowInfo != null && player.getServerId().equals(guildShowInfo.getSimpleInfo().getServerId())) {
            res.setGuild(guildShowInfo);
        }
        client.sendProtocol(res.build());
    }

    private void guildList(NetClient client, Object o) {
        GuildMsg.GuildListRequest_40000001 req = (GuildMsg.GuildListRequest_40000001) o;
        GuildMsg.GuildListResponse_40000002.Builder res = GuildMsg.GuildListResponse_40000002.newBuilder();
        final int page = req.getPage();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        RankType rankType = RankType.Guild;
        res.setPage(page);
        // 公会总数
        CompletionStage<Integer> rankSizeStage = RankService.getInstance().getRankSizeAsync(player.getServerId(), rankType);
        // 查询指定页码的战斗力公会数据
        CompletionStage<List<RankEntry>> guildListStage = RankService.getInstance().getTopNAsync(player.getServerId(), rankType, 30);
        // 获取公会 simpleGuild 列表
        guildListStage.thenCombine(rankSizeStage, (guildRankList, rankSize) -> {
            List<Long> guildIdList = guildRankList.stream().map(RankEntry::getId).collect(Collectors.toList());
            GuildHelper.getSimpleGuildListAsync(guildIdList).thenAccept(list -> {
                int readRankSize = rankSize;
                if (list != null) {
                    int i = 0;
                    for (Object simpleGuild : list) {
                        if (simpleGuild != null) {
                            i++;
                            res.addGuildList(((SimpleGuild) simpleGuild).toProto());
                        } else {
                            readRankSize--;
                        }
                        if (i >= 20) {
                            break;
                        }
                    }
                }
                res.setTotal(readRankSize);
                client.sendProtocol(res.build());
            }).exceptionally(err -> {
                client.sendProtocol(res.build(), ErrorMsgEnum.unknown.getId());
                err.printStackTrace();
                return null;
            });
            return null;
        }).exceptionally(err -> {
            client.sendProtocol(res.build(), ErrorMsgEnum.unknown.getId());
            err.printStackTrace();
            return null;
        });
    }

    /**
     * GuildCallbackMsg 公会 RPC 回调 消息
     */
    public static class GuildCallbackMsg {

        /**
         * 错误码
         */
        int errorCode;

        /**
         * 消息
         */
        Message response;

        public GuildCallbackMsg(int errorCode, Message response) {
            this.errorCode = errorCode;
            this.response = response;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(int errorCode) {
            this.errorCode = errorCode;
        }

        public Message getResponse() {
            return response;
        }

        public void setResponse(Message response) {
            this.response = response;
        }
    }

    private void bountyAccept(NetClient client, Object message) {
        GuildBountyAcceptRequest_40000070 req = (GuildBountyAcceptRequest_40000070) message;
        GuildBountyAcceptResponse_40000071 defaultInstance = GuildBountyAcceptResponse_40000071.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        client.sendProtocol(defaultInstance);
    }

    private void bountyTargetRefresh(NetClient client, Object message) {
        GuildBountyTargetRefreshRequest_40000074 req = (GuildBountyTargetRefreshRequest_40000074) message;
        GuildBountyTargetRefreshResponse_40000075 defaultInstance = GuildBountyTargetRefreshResponse_40000075.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        GuildBountyTargetRefreshResponse_40000075.Builder resp = GuildBountyTargetRefreshResponse_40000075.newBuilder();
        client.sendProtocol(resp.build());
    }

    private void bountyBattleStart(NetClient client, Object message) {
        GuildBountyBattleStartRequest_40000076 req = (GuildBountyBattleStartRequest_40000076) message;
        String playerId = req.getPlayerId();
        GuildBountyBattleStartResponse_40000077 defaultInstance = GuildBountyBattleStartResponse_40000077.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        client.sendProtocol(defaultInstance);
    }

    private void bountyBattleEnd(NetClient client, Object message) {
        GuildBountyBattleEndRequest_40000078 req = (GuildBountyBattleEndRequest_40000078) message;
        String playerId = req.getPlayerId();
        boolean isWin = req.getIsWin();
        GuildBountyBattleEndResponse_40000079 defaultInstance = GuildBountyBattleEndResponse_40000079.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        client.sendProtocol(defaultInstance);
    }

    private void bountyBattleReport(NetClient client, Object message) {
        GuildBountyBattleReportRequest_4000007a req = (GuildBountyBattleReportRequest_4000007a) message;
        boolean isGuild = req.getIsGuild();
        GuildBountyBattleReportResponse_4000007b defaultInstance = GuildBountyBattleReportResponse_4000007b.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        GuildBountyBattleReportResponse_4000007b.Builder resp = GuildBountyBattleReportResponse_4000007b.newBuilder();
        client.sendProtocol(resp.build());
    }

    private void bountyPlayer(NetClient client, Object message) {
        GuildBountyPlayerRequest_4000007c req = (GuildBountyPlayerRequest_4000007c) message;
        String playerId = req.getPlayerId();
        GuildBountyPlayerResponse_4000007d defaultInstance = GuildBountyPlayerResponse_4000007d.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        GuildBountyPlayerResponse_4000007d.Builder resp = GuildBountyPlayerResponse_4000007d.newBuilder();
        client.sendProtocol(resp.build());
    }

    private void bountyReward(NetClient client, Object message) {
        GuildBountyRewardRequest_40000072 req = (GuildBountyRewardRequest_40000072) message;
        GuildBountyRewardResponse_40000073 defaultInstance = GuildBountyRewardResponse_40000073.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        GuildBountyRewardResponse_40000073.Builder resp = GuildBountyRewardResponse_40000073.newBuilder();
        client.sendProtocol(resp.build());
    }

    private void donate(NetClient client, Object message) {
        GuildDonateRequest_40000067 req = (GuildDonateRequest_40000067) message;
        int id = req.getId();
        GuildDonateResponse_40000068 defaultInstance = GuildDonateResponse_40000068.getDefaultInstance();
        GuildDonateResponse_40000068.Builder resp = GuildDonateResponse_40000068.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (player.getGuildId() == 0) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.illegal_request.ID);
            return;
        }
        GuildDonateConfig guildDonateConfig = GuildDonateManager.instance().get(id);
        IntMapWrapper donateMap = player.getGuildModule().getDonateMap();
        if (donateMap.getValue(id) >= guildDonateConfig.DayCount) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.times_limit.ID);
            return;
        }
        player.pay(guildDonateConfig.Price, OpType.GuildDonate);
        List<RewardInfo> resources = PlayerHelper.addResources(player, guildDonateConfig.Reward, OpType.GuildDonate);
        resp.addAllRewards(resources);
        donateMap.add(id);
        player.handleEvent(EventTypeEnum.GuildDonate, id);
        client.sendProtocol(resp);
        long guildContribute = RewardHelper.getRewardCount(resources, Asset.GuildContribute);
        GameLogger.GuildDonate(player, id, donateMap.getValue(id), (int) guildContribute, player.getPlayerId());
    }

    private void rankList(NetClient client, Object message) {
        GuildRankListRequest_40000081 req = (GuildRankListRequest_40000081) message;
        int page = req.getPage();
        int pageSize = req.getPageSize();
        GuildRankListResponse_40000082 defaultInstance = GuildRankListResponse_40000082.getDefaultInstance();
        GuildRankListResponse_40000082.Builder resp = GuildRankListResponse_40000082.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (pageSize > 100) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.request_parameter_error.ID);
            return;
        }
        CompletionStage<GuildRankList> rankInfo = RankHelper.getGuildRankList(player, page, pageSize);
        rankInfo.thenAccept(r -> {
            resp.setRankList(r);
            client.sendProtocol(resp.build());
        }).exceptionally(player::handleFailFunction);
    }

    public static Future<GuildCallbackMsg> sendMsgToGuildServer(long guildId, Player player, Message req, String... params) {
        // 封装公会请求
        GuildMsgPush_41000045.Builder serverReq = GuildMsgPush_41000045.newBuilder();
        // 设置请求参数
        // 公会ids
        serverReq.setGuildId(guildId);
        // 玩家id
        serverReq.setPlayerId(player.getPlayerId());
        // 请求消息
        serverReq.setData(req.toByteString());
        // TODO 上下文设置 后续日志记录
        if (params != null && params.length > 0) {
            for (String param : params) {
                serverReq.addParams(param);
            }
        }
        int reqMsgId = PbProtocol.getInstance().getMsgId(req.getClass().getSimpleName());
        serverReq.setMsgId(reqMsgId);
        // 回包id
        final int responseMsgId = reqMsgId + 1;
        Promise<GuildCallbackMsg> future = Promise.promise();
        // 异步RPC请求
        Future<GuildMsgResponse_41000046> rpcFuture;
        if (player.getGuildId() == 0) {
            // 公会不存在 创建公会 随机找一个节点
            rpcFuture = VxHolder.requestRemoteServer(ServerType.Cross, serverReq.build());
        } else {
            rpcFuture = VxHolder.requestRemoteServer(GuildHelper.getServerIdByGuildId(player.getGuildId()), serverReq.build());
        }
        //            rpcFuture = VxHolder.requestRemoteServer("LY_ZONG_MEN", serverReq.build());
        log.info(String.format("sendMsgToGuildServer pid:%d, msgId:%d, guildId:%d, req:%s", player.getPlayerId(), reqMsgId, guildId, req));
        // 请求成功
        // 请求成功
        rpcFuture.onSuccess(result -> {
            if (result != null) {
                log.info(String.format("sendMsgToGuildServerCallBack msgId:%d %s, errorCode:%d, pid:%d ", result.getMsgId(), req.getClass().getSimpleName(), result.getErrorCode(), result.getPlayerId()));
                GuildMsgResponse_41000046 serverResponse = (GuildMsgResponse_41000046) result;
                if (serverResponse.getErrorCode() == ErrorMsgEnum.ok.ID) {
                    Message response = PbProtocol.getInstance().parseFrom(responseMsgId, serverResponse.getData().toByteArray());
                    log.info(String.format("response:%s", response));
                    // 异步请求成功 封装 proto 信息和错误码 回调
                    future.complete(new GuildCallbackMsg(serverResponse.getErrorCode(), response));
                } else {
                    // 异步请求失败 封装 错误码 回调
                    future.complete(new GuildCallbackMsg(serverResponse.getErrorCode(), null));
                }
            } else {
                future.complete(new GuildCallbackMsg(ErrorMsgEnum.unknown.ID, null));
            }
        }).onFailure(err -> {
            future.complete(new GuildCallbackMsg(ErrorMsgEnum.unknown.ID, null));
            err.printStackTrace();
        });
        return future.future();
    }

    public static Future<GuildCallbackMsg> sendMsgToGuildServer(Player player, Message req, String... params) {
        return sendMsgToGuildServer(player.getGuildId(), player, req, params);
    }

    private void gVECardData(NetClient client, Object message) {
        GuildGVECardDataRequest_40000083 req = (GuildGVECardDataRequest_40000083) message;
        GuildGVECardDataResponse_40000084 defaultInstance = GuildGVECardDataResponse_40000084.getDefaultInstance();
        GuildGVECardDataResponse_40000084.Builder resp = GuildGVECardDataResponse_40000084.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        client.sendProtocol(resp.build());
    }

    private void gVEOpenCard(NetClient client, Object message) {
        GuildGVEOpenCardRequest_40000085 req = (GuildGVEOpenCardRequest_40000085) message;
        int cardId = req.getCardId();
        GuildGVEOpenCardResponse_40000086 defaultInstance = GuildGVEOpenCardResponse_40000086.getDefaultInstance();
        GuildGVEOpenCardResponse_40000086.Builder resp = GuildGVEOpenCardResponse_40000086.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        client.sendProtocol(resp.build());
    }

    private void gVEOpenMap(NetClient client, Object message) {
        GuildGVEOpenMapRequest_40000087 req = (GuildGVEOpenMapRequest_40000087) message;
        GuildGVEOpenMapResponse_40000088 defaultInstance = GuildGVEOpenMapResponse_40000088.getDefaultInstance();
        GuildGVEOpenMapResponse_40000088.Builder resp = GuildGVEOpenMapResponse_40000088.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        client.sendProtocol(resp.build());
    }

    private void gVEBuyTicket(NetClient client, Object message) {
        GuildGVEBuyTicketRequest_40000090 req = (GuildGVEBuyTicketRequest_40000090) message;
        GuildGVEBuyTicketResponse_40000091 defaultInstance = GuildGVEBuyTicketResponse_40000091.getDefaultInstance();
        GuildGVEBuyTicketResponse_40000091.Builder resp = GuildGVEBuyTicketResponse_40000091.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        client.sendProtocol(resp.build());
    }
}
