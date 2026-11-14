package cn.game.games.net.game.handler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.game.games.net.game.gm.ConfigQueryService;
import cn.game.games.net.game.gm.GmDispatcher;
import cn.game.games.net.game.gm.GmResult;
import cn.game.games.net.game.gm.GmSchemaCatalog;
import cn.game.games.net.game.gm.GmSecurityPolicy;
import cn.game.games.net.game.gm.PlayerInspectService;
import cn.game.games.net.game.gm.ProtoCatalogService;
import cn.game.games.cache.entity.*;
import cn.game.games.net.game.module.battle.*;
import cn.game.games.net.game.module.develop.equip.EquipModule;
import cn.game.games.net.game.module.develop.equip.EquipPart;
import cn.game.games.net.game.module.guarantee.Guarantee;
import cn.game.games.net.game.module.guarantee.GuaranteeModule;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.protocol.generated.config.*;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.GuaranteeTypeEnum;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.*;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.cache.id.IdCache;
import cn.game.core.event.ServerEventTypeEnum;
import cn.game.core.exception.LogicException;
import cn.game.core.net.client.LogoutType;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.process.Processor;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.core.SimplePlayer;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.cross.guild.service.GuildServiceInterface;
import cn.game.games.net.cross.remote.CrossServerInterface;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.QuestHelper;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.helper.TestHelper;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.develop.AttrModule;
import cn.game.games.net.game.module.develop.DevelopModule;
import cn.game.games.net.game.module.develop.attr.AttrCalcType;
import cn.game.games.net.game.module.develop.attr.PlayerAttrCalc;
import cn.game.games.net.game.module.develop.hero.HeroModule;
import cn.game.games.net.game.module.draw.DrawModule;
import cn.game.games.net.game.module.item.ItemModule;
import cn.game.games.net.game.module.quest.Quest;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.games.net.game.remote.GameServerInterface;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.enume.QuestTypeEnum;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PlayerMsg.PlayerLogoutResponse_01000004;
import cn.game.protocol.protobuf.PlayerMsg.PlayerResetPush_01100016;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.RewardMsg.RewardPush_55000501;
import cn.game.protocol.protobuf.TestMsg;
import cn.game.protocol.protobuf.TestMsg.TestAddItemRequest_6f000008;
import cn.game.protocol.protobuf.TestMsg.TestAddItemResponse_6f000009;
import cn.game.protocol.protobuf.TestMsg.TestClearResourceAndItemRequest_6f000040;
import cn.game.protocol.protobuf.TestMsg.TestClearResourceAndItemResponse_6f000041;
import cn.game.protocol.protobuf.TestMsg.TestConfigCheckRequest_6f000090;
import cn.game.protocol.protobuf.TestMsg.TestConfigCheckResponse_6f000091;
import cn.game.protocol.protobuf.TestMsg.TestMessageRequest_6f000080;
import cn.game.protocol.protobuf.TestMsg.TestMessageResponse_6f000081;
import cn.game.protocol.protobuf.TestMsg.TestMissionFinishRequest_6f000022;
import cn.game.protocol.protobuf.TestMsg.TestMissionFinishResponse_6f000023;
import cn.game.protocol.protobuf.TestMsg.TestPlayerAssetDataRequest_6f000028;
import cn.game.protocol.protobuf.TestMsg.TestPlayerAssetDataResponse_6f000029;
import cn.game.protocol.protobuf.TestMsg.TestPlayerDeleteRequest_6f000044;
import cn.game.protocol.protobuf.TestMsg.TestPlayerDeleteResponse_6f000045;
import cn.game.protocol.protobuf.TestMsg.TestPlayerLogoutRequest_6f000042;
import cn.game.protocol.protobuf.TestMsg.TestPlayerLogoutResponse_6f000043;
import cn.game.protocol.protobuf.TestMsg.TestRunRequest_6f000020;
import cn.game.util.Config;
import cn.game.util.DateUtil;
import cn.game.util.FloatMapWrapper;
import cn.game.util.IntMapWrapper;
import cn.game.util.LinuxTimeShift;
import cn.game.util.LinuxTimeShift.PreviewResult;
import cn.game.util.ObjUtil;
import cn.game.util.RedisUtil;
import cn.game.util.ServerType;
import cn.game.util.SpringContextLoader;
import io.vertx.core.Future;
import cn.game.protocol.protobuf.TestMsg.TestGmSchemaRequest_6f000100;
import cn.game.protocol.protobuf.TestMsg.TestGmSchemaResponse_6f000101;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.protobuf.TestMsg.TestGmExecRequest_6f000102;
import cn.game.protocol.protobuf.TestMsg.TestGmExecResponse_6f000103;
import cn.game.protocol.protobuf.TestMsg.TestConfigQueryRequest_6f000104;
import cn.game.protocol.protobuf.TestMsg.TestConfigQueryResponse_6f000105;
import cn.game.protocol.protobuf.TestMsg.TestProtoCatalogRequest_6f000106;
import cn.game.protocol.protobuf.TestMsg.TestProtoCatalogResponse_6f000107;
import cn.game.protocol.protobuf.TestMsg.TestInspectPlayerRequest_6f000108;
import cn.game.protocol.protobuf.TestMsg.TestInspectPlayerResponse_6f000109;

@Component
public class TestHandler extends GameBaseHandler {

    // protected Logger gamerecvLog = LoggerFactory.getLogger("gamerecvLog");
    protected Logger gamesendLog = LoggerFactory.getLogger("gamesendLog");

    protected Logger logger = LoggerFactory.getLogger(TestHandler.class);

    /** 0x6f测试协议共用的JSON序列化与反序列化Mapper */
    private static final ObjectMapper GM_JSON_MAPPER = new ObjectMapper();

    @Autowired(required = false)
    private GmDispatcher gmDispatcher;

    @Autowired(required = false)
    private ConfigQueryService configQueryService;

    @Autowired(required = false)
    private ProtoCatalogService protoCatalogService;

    @Autowired(required = false)
    private PlayerInspectService playerInspectService;

    @Override
    protected int getModule() {
        return 0x6f;
    }

    @Override
    protected void inititialize() {
        // 这个先保留，紧急情况下可以使用。
        putInvoker(PbProtocol.TestPlayerLogoutRequest_6f000042, this::playerLogout);
        // 先保留给客户端使用
        putInvoker(PbProtocol.TestPlayerAssetDataRequest_6f000028, this::assetData);
        if (GmSecurityPolicy.currentRunMode().isProduction()) {
            return;
        }
        logger.warn("===================================test command is enable =================================");
        putInvoker(PbProtocol.TestGmCmdRequest_6f000001, this::gmCmd);
        putInvoker(PbProtocol.TestAddItemRequest_6f000008, this::addItem);
        putInvoker(PbProtocol.TestRunRequest_6f000020, this::test);
        putInvoker(PbProtocol.TestMissionFinishRequest_6f000022, this::finishMission);
        putInvoker(PbProtocol.TestClearResourceAndItemRequest_6f000040, this::clearResourceAndItem);
        putInvoker(PbProtocol.TestPlayerDeleteRequest_6f000044, this::playerDelete);
        putInvoker(PbProtocol.TestMessageRequest_6f000080, this::message);
        putInvoker(PbProtocol.TestConfigCheckRequest_6f000090, this::configCheck);
        putInvoker(PbProtocol.TestGmSchemaRequest_6f000100, this::gmSchema);
        putInvoker(PbProtocol.TestGmExecRequest_6f000102, this::gmExec);
        putInvoker(PbProtocol.TestConfigQueryRequest_6f000104, this::configQuery);
        putInvoker(PbProtocol.TestProtoCatalogRequest_6f000106, this::protoCatalog);
        putInvoker(PbProtocol.TestInspectPlayerRequest_6f000108, this::inspectPlayer);
        if (SpringContextLoader.getContext() != null) {
            GmDispatcher dispatcher = resolveGmDispatcher();
            if (!dispatcher.hasRegisteredGroupBeans()) {
                logger.error("TestHandler初始化完成但GmDispatcher尚未注册任何@GmGroup动作Bean");
            }
        }
    }

    private void gmCmd(NetClient client, Object message) {
        TestMsg.TestGmCmdRequest_6f000001 req = (TestMsg.TestGmCmdRequest_6f000001) message;
        TestMsg.TestGmCmdResponse_6f000002.Builder resp = TestMsg.TestGmCmdResponse_6f000002.newBuilder();
        String cmd = req.getCmd();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        // 复用已有测试协议回传实际处理节点，不进入修改玩家状态的 GM 分发。
        if (cmd.equals("runtimeIdentity") || cmd.startsWith("runtimeIdentity ")) {
            if (GmSecurityPolicy.currentRunMode().isProduction()) {
                client.sendProtocol(resp.build(), ErrorMsgEnum.production_gm_not_allow.getId());
                return;
            }
            if (playerId <= 0 || player == null || !player.isOnline() || player.getGameClient() != client) {
                client.sendProtocol(resp.build(), ErrorMsgEnum.need_login.getId());
                return;
            }
            try {
                String[] identityArgs = cmd.split(" ", -1);
                if (identityArgs.length != 2) {
                    throw new IllegalArgumentException("RUNTIME_CHALLENGE_INVALID");
                }
                Map<String, String> identity = cn.game.games.core.runtime.RuntimeIdentity.observe(identityArgs[1]);
                identity.put("playerId", Long.toString(playerId));
                if (player.getData() != null && player.getData().getServerId() != null) {
                    identity.put("logicServerId", player.getData().getServerId());
                }
                resp.setDebugInfo(GM_JSON_MAPPER.writeValueAsString(identity));
                client.sendProtocol(resp.build());
            } catch (Exception e) {
                logger.warn("玩家运行身份观察失败 playerId={}", playerId, e);
                client.sendProtocol(resp.build(), ErrorMsgEnum.unknown.getId());
            }
            return;
        }
        if (GmSecurityPolicy.currentRunMode().isProduction()) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.production_gm_not_allow.getId());
            return;
        }
        GmResult gmResult = resolveGmDispatcher().dispatchText(player, cmd);
        resp.setDebugInfo(toJson(gmResult));
        if (!gmResult.isOk()) {
            int errCode = gmResult.getErrorCode() > 0 ? gmResult.getErrorCode() : ErrorMsgEnum.unknown.getId();
            client.sendProtocol(resp.build(), errCode);
            return;
        }
        client.sendProtocol(resp.build());
    }

    /*
		// 角色晋升1
		private void rolePromotion(NetClient client, Object message) {
			TestMsg.TestRolePromotionRequest_6f00010b req = (TestMsg.TestRolePromotionRequest_6f00010b) message;
			TestMsg.TestRolePromotionResponse_6f00010c.Builder resp = TestMsg.TestRolePromotionResponse_6f00010c.newBuilder();
			int roleId = req.getRoleId();
			int level = req.getLevel();
			long playerId = client.getPlayerId(); Player player = PlayerManager.getInstance().getPlayer(playerId);
			RoleOp roleOp = player.getModule(RoleOp.class);
			Role role = roleOp.get(roleId);
			for (int i = role.getPromotionLevel() + 1; i <= level; i++) {
				roleOp.changeRolePromotionLevel(roleId, i);
			}
			client.sendProtocol(resp.build());
		}
	
		// 直接完成探索区域目标,方便进入局间
	
		private void triggerEvent(NetClient client, Object message) {
			TestMsg.TestGameEventTriggerRequest_6f000105 req = (TestMsg.TestGameEventTriggerRequest_6f000105) message;
			TestMsg.TestGameEventTriggerResponse_6f000106.Builder resp = TestMsg.TestGameEventTriggerResponse_6f000106.newBuilder();
			long playerId = client.getPlayerId(); Player player = PlayerManager.getInstance().getPlayer(playerId);
			int id = req.getId();
			GameEventConfig gameEventConfig = GameEventManager.getInstance().getGameEventConfig(id);
			int[] ids = gameEventConfig.getGenarateIds();
	
			client.sendProtocol(resp.build());
		}
		private void getRoleAttribute(NetClient client, Object message) {
			TestMsg.TestRoleAttributeRequest_6f000100 req = (TestMsg.TestRoleAttributeRequest_6f000100) message;
			TestMsg.TestRoleAttributeResponse_6f000101.Builder resp = TestMsg.TestRoleAttributeResponse_6f000101.newBuilder();
			long playerId = client.getPlayerId(); Player player = PlayerManager.getInstance().getPlayer(playerId);
			int id = req.getId();
			int uid = req.getUid();
			int attrType = req.getAttrType();
			PropertyOp propertyOp = player.getModule(PropertyOp.class);
			propertyOp.init();
			TestMsg.TestRoleAttrValue.Builder value = TestMsg.TestRoleAttrValue.newBuilder();
			value.setAttrSubType(AttributeSubTypeEnum.cur.getId());
			RoleOp roleOp = player.getModule(RoleOp.class);
			Role role = roleOp.get(uid);
			value.setValue(role.getHp());
			resp.addAttrs(value);
	
			value.setAttrSubType(AttributeSubTypeEnum.curTotal.getId());
			value.setValue(role.getHpCurMax());
			resp.addAttrs(value);
	
			value.setAttrSubType(AttributeSubTypeEnum.total.getId());
			int attrMax = roleOp.getAttrMax(role, AttributeTypeEnum.hp, AttributeSubTypeEnum.total);
			value.setValue(attrMax);
			resp.addAttrs(value);
	
			client.sendProtocol(resp.build());
		}
	
		private void command(NetClient client, Object message) {
			TestCommandRequest_6f000095 req = (TestCommandRequest_6f000095) message;
			int command = req.getCommand();
			PlayerHelper.command(client.getPlayerId(), command);
		}
		private void bagTest(NetClient client, Object message) {
			long playerId = client.getPlayerId(); Player player = PlayerManager.getInstance().getPlayer(playerId);
			TestAddOrDelBagItemRequest_6f000032 request = (TestAddOrDelBagItemRequest_6f000032) message;
			TestAddOrDelBagItemResponse_6f000033.Builder response = TestAddOrDelBagItemResponse_6f000033.newBuilder();
			ItemModule itemModule = player.getModule(ItemModule.class);
	
			int id = request.getId();
			int count = request.getCount();
	
			response.setBag(PbBuilder.buildBagInfo(playerId));
			client.sendProtocol(response.build());
	
		}
	
		private void finishStory(NetClient client, Object message) {
			TestStoryFinishRequest_6f000024 request = (TestStoryFinishRequest_6f000024) message;
			TestStoryFinishResponse_6f000025.Builder response = TestStoryFinishResponse_6f000025.newBuilder();
			List<Integer> idList = request.getIdList();
			long playerId = client.getPlayerId(); Player player = PlayerManager.getInstance().getPlayer(playerId);
			StoryOp storyOp = player.getModule(StoryOp.class);
	
			for (Integer id : idList) {
				storyOp.update(id, 10, false);
	
				StoryConfig storyConfig = StoryManager.getInstance().getStoryConfig(id);
	
				boolean finish = storyOp.finish(id);
				if (finish) {
					List<RewardInfo> addResources = PlayerHelper.addResources(player, storyConfig.getReward());
					response.addAllResource(addResources);
					PlayerHelper.command(playerId, storyConfig.getCommandList());
				}
	
			}
			client.sendProtocol(response.build());
	
		}*/
    private void clearResourceAndItem(NetClient client, Object message) {
        TestClearResourceAndItemRequest_6f000040 request = (TestClearResourceAndItemRequest_6f000040) message;
        TestClearResourceAndItemResponse_6f000041.Builder response = TestClearResourceAndItemResponse_6f000041.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        player.getCurrencyModule().getCurrencyMap().clear();
        player.getItemModule().getId_items().clear();
        client.sendProtocol(response.build());
    }

    private void assetData(NetClient client, Object message) {
        TestPlayerAssetDataRequest_6f000028 request = (TestPlayerAssetDataRequest_6f000028) message;
        TestPlayerAssetDataResponse_6f000029.Builder response = TestPlayerAssetDataResponse_6f000029.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        response.putAllAssets(player.getCurrencyModule().getCurrencyMap().getMap());
        for (Item item : player.getItemModule().list()) {
            response.addItems(item.toItemInfo());
        }
        client.sendProtocol(response.build());
    }

    private void finishMission(NetClient client, Object message) {
        TestMissionFinishRequest_6f000022 request = (TestMissionFinishRequest_6f000022) message;
        TestMissionFinishResponse_6f000023.Builder response = TestMissionFinishResponse_6f000023.newBuilder();
        List<Integer> idList = request.getIdList();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        QuestModule questOp = player.getQuestModule();
        Map<Integer, Quest> group = questOp.getGroup(QuestTypeEnum.MainLine);
        Quest mainQuest = null;
        if (group != null) {
            for (Entry<Integer, Quest> entry : group.entrySet()) {
                mainQuest = entry.getValue();
                break;
            }
        }
        List<RewardInfo> rewardItems = new ArrayList<>();
        int curId = mainQuest != null ? mainQuest.getId() : 0;
        for (int i = 0; i < idList.size(); i++) {
            Integer id = idList.get(i);
            if (id < curId) {
                continue;
            }
            if (id == curId) {
                mainQuest.setState(QuestHelper.CAN_GIVEWARD);
                List<RewardInfo> list = questOp.receive(mainQuest.getId());
                if (list != null) {
                    rewardItems.addAll(list);
                }
                if (group != null) {
                    for (Entry<Integer, Quest> entry : group.entrySet()) {
                        mainQuest = entry.getValue();
                        break;
                    }
                    curId = mainQuest != null ? mainQuest.getId() : 0;
                }
            } else if (id > curId) {
            }
        }
        response.addAllResource(rewardItems);
        client.sendProtocol(response.build());
    }

    protected void test(NetClient client, Object message) {
        //    	ServerContext.getInstance().fireEvent(ServerEventTypeEnum.NewDay, null);
        TestRunRequest_6f000020 req = (TestRunRequest_6f000020) message;
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        ItemModule itemModule = player.getItemModule();
        List<Hero> battleHeroList = player.getHeroModule().getBattleHeroList();
        String playerServerId = IdCache.getPlayerServerId(240201789);
        System.out.println(playerServerId);
        //		RFuture<Object> async = RedisUtil.getAsync(CacheType.PLAYER_SIMPLE.key(240202120));
        //		System.out.println("当前线程：" + Thread.currentThread().getName());
        //		async.onComplete((r, e) -> {
        //			System.out.println("redis回调线程：" + Thread.currentThread().getName());
        //			if (e == null) {
        //				System.out.println("异步获取缓存成功：" + r);
        //			} else {
        //				System.out.println("异步获取缓存失败：" + e);
        //			}
        //		});
        RFuture<SimplePlayer> asyncc = RedisUtil.getAsync(CacheType.PLAYER_SIMPLE.key(240202120));
        System.out.println("初始线程：" + Thread.currentThread().getName());
        asyncc.onComplete((r, e) -> {
            System.out.println("异步获取缓存线程：" + Thread.currentThread().getName());
            if (e == null) {
                System.out.println("异步获取缓存成功：" + r);
            } else {
                System.out.println("异步获取缓存失败：" + e);
            }
        });
        System.out.println("初始vertx线程：" + Thread.currentThread().getName());
        VxHolder.runAfterRedisAsyncOperation(r -> {
            System.out.println("执行逻辑线程：" + Thread.currentThread().getName());
            System.out.println("获取缓存结果：" + r);
            return Future.succeededFuture(r);
        }, asyncc);
        //		System.out.println("当前vertx线程：" + Thread.currentThread().getName());
        //		Future<String> f = VxHolder.runWithLock(() -> {
        //			System.out.println("执行逻辑线程：" + Thread.currentThread().getName());
        //
        //			RFuture<String> asyncc = RedisUtil.getAsync(CacheType.PLAYER_SIMPLE.key(240202120));
        //			return Future.fromCompletionStage(asyncc);
        //		}, CacheType.PLAYER_SIMPLE.key(240202120));
        //		f.onComplete(r -> {
        //			System.out.println("vertx redis onComplete 回调线程：" + Thread.currentThread().getName());
        //			if (r.succeeded()) {
        //				System.out.println("获取锁成功：" + r.result());
        //			} else {
        //				System.out.println("获取锁失败：" + r.cause());
        //			}
        //		});
        //        Future<ItemModule> requestRemoteServer = VxHolder.requestRemoteServer("game_test", new ObjectProtocol(PbProtocol.ServerObjectTestRequest_7d000033, itemModule));
        //        requestRemoteServer.onComplete(r -> {
        //            if (r.succeeded()) {
        //                ItemModule result = r.result();
        //                System.err.println("返回值： " + result);
        //                System.err.println("返回值： " + result.getId_items());
        //            } else {
        //                System.err.println("失败");
        //            }
        //        });
        //		Future<Long> future = GameServer.getInstance().getRemoteLoginServerInterface(CallType.LoadBalancer).getUid2("323323");
        //		future.onComplete(r -> {
        //			System.out.println(r);
        //		});
        //		testcalcPower(player);
        //		RankModule rankModule = player.getModule(RankModule.class);
        //		rankModule.updateHeroCombatRank();
        //		PlayerHelper.modifyPlayer(240201720, pp -> {
        //
        //			PlayerHelper.addResources(pp, Asset.diamond.ID, 999, OpType.Test);
        //
        //			return true;
        //		});
        //		drawTest2(player);
        //		drawTest(player);
        //		CommonLogger.error("what the fuck by common logger");
        //		log.error("what the fuck by log");
        //		player.getQuestModule().addConditionCount(ConditionTypeEnum.ChapterFinish, 3, 1, 2);
        //		System.exit(0);
    }

    private void testcalcPower(Player player) {
        // 神将属性
        Map<Long, FloatMapWrapper> heroAttrs = new HashMap<Long, FloatMapWrapper>();
        HeroModule heroModule = player.getHeroModule();
        AttrModule attrModule = player.getAttrModule();
        Collection<Hero> list = heroModule.getBattleHeroList();
        System.out.println();
        System.out.println();
        PlayerAttrCalc playerAttrCalc = attrModule.getPlayerAttrCalcMap().get(AttrCalcType.HeroBook);
        playerAttrCalc.reCalcAttr();
        System.out.println("图鉴增加的属性： " + playerAttrCalc.getAttrMap().getMap());
        System.out.println("图鉴增加的战力： " + BattleHelper.calcCombat(playerAttrCalc.getAttrMap()));
        System.out.println();
        System.out.println();
        int heavenlyDaoLevel = player.getDevelopModule().getHeavenlyDaoLevel();
        System.out.println("天道修为等级： " + heavenlyDaoLevel);
        playerAttrCalc = attrModule.getPlayerAttrCalcMap().get(AttrCalcType.HeavenlyDao);
        playerAttrCalc.reCalcAttr();
        System.out.println("天道修为增加的属性： " + playerAttrCalc.getAttrMap().getMap());
        System.out.println("天道修为增加的战力： " + BattleHelper.calcCombat(playerAttrCalc.getAttrMap()));
        System.out.println();
        System.out.println();
        playerAttrCalc = attrModule.getPlayerAttrCalcMap().get(AttrCalcType.Potential);
        playerAttrCalc.reCalcAttr();
        System.out.println("修炼增加的属性： " + playerAttrCalc.getAttrMap().getMap());
        System.out.println("修炼增加的战力： " + BattleHelper.calcCombat(playerAttrCalc.getAttrMap()));
        System.out.println();
        System.out.println();
        playerAttrCalc = attrModule.getPlayerAttrCalcMap().get(AttrCalcType.QiankunMirror);
        playerAttrCalc.reCalcAttr();
        System.out.println("乾坤镜增加的属性： " + playerAttrCalc.getAttrMap().getMap());
        System.out.println("乾坤镜增加的战力： " + BattleHelper.calcCombat(playerAttrCalc.getAttrMap()));
        System.out.println();
        System.out.println();
        playerAttrCalc = attrModule.getPlayerAttrCalcMap().get(AttrCalcType.FairyFriend);
        playerAttrCalc.reCalcAttr();
        System.out.println("仙友增加的属性： " + playerAttrCalc.getAttrMap().getMap());
        System.out.println("仙友加增的战力： " + BattleHelper.calcCombat(playerAttrCalc.getAttrMap()));
        System.out.println();
        System.out.println();
        playerAttrCalc = attrModule.getPlayerAttrCalcMap().get(AttrCalcType.Secretscript);
        playerAttrCalc.reCalcAttr();
        System.out.println("神通增加的属性： " + playerAttrCalc.getAttrMap().getMap());
        System.out.println("神通增加的战力： " + BattleHelper.calcCombat(playerAttrCalc.getAttrMap()));
        System.out.println();
        System.out.println();
        float totalCombat = 0;
        for (Hero hero : list) {
            //			if (hero.getLevel() == 1) {
            //				continue; ti
            //			}
            HeroConfig heroConfig = HeroManager.instance().get(hero.getConfigId());
            System.out.println("hero id : " + hero.getConfigId() + " name : " + heroConfig.name + " level : " + hero.getLevel());
            System.out.println("基本属性： " + BattleHelper.makeHeroAttr(hero));
            FloatMapWrapper heroAttr = BattleHelper.makeHeroAttr(hero);
            float heroCombat = BattleHelper.calcCombat(heroAttr);
            System.out.println("单英雄不算外围战力： " + heroCombat);
            System.out.println();
            heroAttrs.put(hero.getId(), heroAttr);
            totalCombat += heroCombat;
        }
        System.out.println();
        AttrModule module = player.getModule(AttrModule.class);
        module.calcAllAttr();
        float wwCombat = BattleHelper.calcCombat(module.getPlayerAttrMap());
        System.out.println("所有外围增加的战力： " + wwCombat);
        totalCombat += wwCombat * list.size();
        System.err.println("总战力： " + totalCombat);
    }

    private void drawTest(Player player) {
        RandomGivenConfig randomGivenConfig = RandomGivenManager.instance().get(300001);
        int r4 = 0, r5 = 0, r6 = 0;
        int lp = 50;
        int count = 100;
        for (int j = 0; j < lp; j++) {
            for (int i = 0; i < count; i++) {
                List<RewardInfo> reward = PlayerHelper.addReward(player, 300001, OpType.None);
                HeroConfig heroConfig = HeroManager.instance().get(reward.get(0).getRole().getConfigId());
                if (heroConfig.InitialQuality == 4) {
                    r4++;
                } else if (heroConfig.InitialQuality == 5) {
                    r5++;
                } else if (heroConfig.InitialQuality == 6) {
                    r6++;
                }
                //				if (i % 10 == 0) {
                //					System.out.println(MessageFormat.format("第{0}次十连抽结果,紫:{1} 金:{2} 红:{3}", i / 10, r4, r5, r6));
                //				}
            }
        }
        System.out.println(MessageFormat.format("循环{0}次，每次{1}连抽结果,紫:{2} 金:{3} 红:{4}", lp, count, r4, r5, r6));
    }

    private void drawTest2(Player player) {
        int r3 = 0, r4 = 0, r5 = 0, r6 = 0;
        int lp = 100;
        int count = 100;
        int drawCount = 1;
        DrawModule module = player.getModule(DrawModule.class);
        for (int j = 0; j < lp; j++) {
            for (int i = 0; i < count; i++) {
                List<List<RewardInfo>> draw = module.draw(2, drawCount, false);
                for (List<RewardInfo> list : draw) {
                    for (RewardInfo list2 : list) {
                        HeroConfig heroConfig = HeroManager.instance().get(list2.getRole().getConfigId());
                        if (heroConfig.InitialQuality == 4) {
                            r4++;
                        } else if (heroConfig.InitialQuality == 5) {
                            r5++;
                        } else if (heroConfig.InitialQuality == 6) {
                            r6++;
                        } else if (heroConfig.InitialQuality == 3) {
                            r3++;
                        }
                    }
                }
                //				if (i % 10 == 0) {
                //					System.out.println(MessageFormat.format("第{0}次十连抽结果,紫:{1} 金:{2} 红:{3}", i / 10, r4, r5, r6));
                //				}
            }
        }
        System.out.println(MessageFormat.format("{0}次抽卡结果,蓝:{1} 紫:{2} 金:{3} 红:{4}", lp * count, r3, r4, r5, r6));
    }

    protected void addItem(NetClient client, Object message) {
        TestAddItemRequest_6f000008 req = (TestAddItemRequest_6f000008) message;
        TestAddItemResponse_6f000009.Builder resp = TestAddItemResponse_6f000009.newBuilder();
        if (!Config.gmOpen) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.unknown.getId());
            return;
        }
        int id = req.getId();
        int count = req.getCount();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        if (id == 10000001) {
            DevelopModule developModule = player.getDevelopModule();
            developModule.setHeavenlyDaoLevel(count);
            client.sendProtocol(resp.build(), 0);
            return;
        } else if (id == 10000002) {
            BattleModule battleModule = player.getBattleModule();
            battleModule.setMainBattleHighest(count);
            BattleConfig battleConfig = BattleManager.instance().getNullable(count);
            while (battleConfig != null) {
                battleModule.addChapter(battleConfig.ID);
                Chapter chapter = battleModule.getChapter(battleConfig.ID);
                chapter.setBattleTime(30);
                chapter.setPass(true);
                battleConfig = BattleManager.instance().getNullable(battleConfig.preBattle);
            }
            return;
        }
        List<RewardInfo> items = TestHelper.addItems(player, id, count);
        resp.addAllResource(items);
        client.sendProtocol(resp.build());
    }

    protected void ssit(NetClient client, Object message) {
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        //		FriendBaseInfo buildFriendBaseInfo = PbBuilder.buildFriendBaseInfo(player);
        //		client.sendProtocol(buildFriendBaseInfo);
    }

    private void testMessage(NetClient client, Object message) {
        TestMessageRequest_6f000080 request = (TestMessageRequest_6f000080) message;
        long playerId = Long.valueOf(request.getPlayerId());
        Processor processor = (Processor) SpringContextLoader.getContext().getBean("processor");
        int msgId = request.getId();
        Message m = PbProtocol.getInstance().parseFrom(msgId, request.toByteArray());
        ProtobufProtocol protocol = new ProtobufProtocol(msgId, m);
        GameClient gameClient = GameClientManager.getInstance().getGameClientByPlayer(playerId);
        processor.process(gameClient, protocol);
    }

    private void playerLogout(NetClient client, Object message) {
        TestPlayerLogoutRequest_6f000042 req = (TestPlayerLogoutRequest_6f000042) message;
        long playerId = req.getPlayerId();
        TestPlayerLogoutResponse_6f000043 defaultInstance = TestPlayerLogoutResponse_6f000043.getDefaultInstance();
        GameClient gameClientByPlayer = GameClientManager.getInstance().getGameClientByPlayer(playerId);
        if (gameClientByPlayer != null) {
            // 保存数据
            Future<?> logout = GameClientManager.getInstance().logout((GameClient) gameClientByPlayer, LogoutType.TestRequest);
            logout.onComplete(r -> {
                gameClientByPlayer.sendProtocol(PlayerLogoutResponse_01000004.getDefaultInstance());
            });
        } else {
            client.sendProtocol(defaultInstance);
        }
    }

    private void playerDelete(NetClient client, Object message) {
        TestPlayerDeleteRequest_6f000044 req = (TestPlayerDeleteRequest_6f000044) message;
        long playerId = req.getPlayerId();
        TestPlayerDeleteResponse_6f000045 defaultInstance = TestPlayerDeleteResponse_6f000045.getDefaultInstance();
        TestHelper.deletePlayerData(playerId);
        client.sendProtocol(defaultInstance);
    }

    private void message(NetClient client, Object message) {
        TestMessageRequest_6f000080 req = (TestMessageRequest_6f000080) message;
        long playerId = req.getPlayerId();
        int id = req.getId();
        ByteString data = req.getData();
        TestMessageResponse_6f000081 defaultInstance = TestMessageResponse_6f000081.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        client.sendProtocol(defaultInstance);
    }

    private void configCheck(NetClient client, Object message) {
        TestConfigCheckRequest_6f000090 req = (TestConfigCheckRequest_6f000090) message;
        TestConfigCheckResponse_6f000091 defaultInstance = TestConfigCheckResponse_6f000091.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        // 给资源
        TestHelper.setMaxCurrency(player, OpType.Test);
        // 所有道具给一遍。
        Collection<ItemConfig> items = ItemManager.instance().list();
        for (ItemConfig itemConfig : items) {
            try {
                PlayerHelper.addResources(player, itemConfig.ID, 100, OpType.None);
            } catch (Exception e) {
                throw new IllegalAccessError("itemConfig id 添加失败： " + itemConfig.ID);
            }
        }
        // 所有的奖励给一遍
        Collection<RandomGivenConfig> list = RandomGivenManager.instance().list();
        for (RandomGivenConfig randomGivenConfig : list) {
            try {
                PlayerHelper.addReward(player, randomGivenConfig.ID, OpType.None);
            } catch (Exception e) {
                System.err.println("randomGivenConfig id 添加失败： " + randomGivenConfig.ID);
                //				throw new IllegalAccessError("randomGivenConfig id 添加失败： " + randomGivenConfig.ID);
            }
        }
        BattleHandler chapterHandler = new BattleHandler();
        // 所有关卡打一遍
        Map<Integer, List<BattleConfig>> battleTypes = BattleManager.instance().getBattleTypes();
        battleTypes.forEach((k, v) -> {
            int battleId = 0;
            while (true) {
                int battleTmp = battleId;
                for (BattleConfig config : v) {
                    if (config.preBattle == battleId) {
                        battleId = config.ID;
                        break;
                    }
                }
                if (battleTmp == battleId) {
                    break;
                }
                cn.game.protocol.protobuf.BattleMsg.BattleFieldStartRequest_13000001.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleFieldStartRequest_13000001.newBuilder();
                builder.setType(k);
                builder.setTypeId(battleId);
                try {
                    chapterHandler.start(client, builder.build());
                    cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003.Builder builderEnd = cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003.newBuilder();
                    builderEnd.setHpPercent(100);
                    builderEnd.setKillMonsterCount(3000);
                    builderEnd.setBattleTime(3);
                    builderEnd.setWin(true);
                    builderEnd.setDamage(500000);
                    chapterHandler.end(client, builderEnd.build());
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new IllegalAccessError("关卡战斗失败，id： " + battleId);
                }
            }
        });
        //		Iterator<Entry<Integer, List<BattleConfig>>> iterator = battleTypes.entrySet().iterator();
        //		while (iterator.hasNext()) {
        //			int battleId = 0;
        //			BattleConfig battleConfig = null;
        //			Map.Entry<java.lang.Integer, java.util.List<cn.game.protocol.generated.config.BattleConfig>> entry = (Map.Entry<java.lang.Integer, java.util.List<cn.game.protocol.generated.config.BattleConfig>>) iterator
        //					.next();
        //			int type = entry.getKey();
        //			List<BattleConfig> battleConfigs = entry.getValue();
        //			for (BattleConfig config : battleConfigs) {
        //				if (config.preBattle == battleId) {
        //					battleId = config.ID;
        //					battleConfig =
        //				}
        //			}
        //
        //			cn.game.protocol.protobuf.BattleMsg.BattleFieldStartRequest_13000001.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleFieldStartRequest_13000001
        //					.newBuilder();
        //			builder.setType(type);
        //		}
        client.sendProtocol(defaultInstance);
    }

    private void playerAssetData(NetClient client, Object message) {
        TestPlayerAssetDataRequest_6f000028 req = (TestPlayerAssetDataRequest_6f000028) message;
        TestPlayerAssetDataResponse_6f000029 defaultInstance = TestPlayerAssetDataResponse_6f000029.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        TestPlayerAssetDataResponse_6f000029.Builder resp = TestPlayerAssetDataResponse_6f000029.newBuilder();
        client.sendProtocol(resp.build());
    }

    private void gmSchema(NetClient client, Object message) {
        TestGmSchemaRequest_6f000100 req = (TestGmSchemaRequest_6f000100) message;
        TestGmSchemaResponse_6f000101.Builder resp = TestGmSchemaResponse_6f000101.newBuilder();
        if (GmSecurityPolicy.currentRunMode().isProduction()) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.production_gm_not_allow.getId());
            return;
        }
        resp.setSchemaVersion(GmResult.SCHEMA_VERSION);
        try {
            GmSchemaCatalog catalog = resolveGmDispatcher().getSchemaCatalog(GmSecurityPolicy.currentRunMode(), req.getGroupFilter());
            resp.setSchemaVersion(catalog.getSchemaVersion());
            resp.setSchemaJson(toJson(catalog));
        } catch (Exception e) {
            logger.warn("获取GM Schema失败 groupFilter={}", req.getGroupFilter(), e);
            resp.setSchemaJson(toJson(GmResult.fail(ErrorMsgEnum.unknown, "获取GM Schema异常: " + e.getMessage())));
        }
        client.sendProtocol(resp.build());
    }

    private void gmExec(NetClient client, Object message) {
        TestGmExecRequest_6f000102 req = (TestGmExecRequest_6f000102) message;
        TestGmExecResponse_6f000103.Builder resp = TestGmExecResponse_6f000103.newBuilder();
        if (GmSecurityPolicy.currentRunMode().isProduction()) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.production_gm_not_allow.getId());
            return;
        }
        long rawTargetId = req.getTargetPlayerId();
        long targetPlayerId = rawTargetId > 0 ? rawTargetId : client.getPlayerId();
        String traceId = req.getTraceId() != null && !req.getTraceId().isBlank() ? req.getTraceId() : UUID.randomUUID().toString();
        GmResult gmResult;
        if (rawTargetId < 0) {
            targetPlayerId = rawTargetId;
            gmResult = GmResult.fail(ErrorMsgEnum.request_parameter_error, "target_player_id不能为负数")
                    .withTraceId(traceId);
        } else {
            try {
                Map<String, Object> params = parseJsonObjectMap(req.getParamsJson());
                gmResult = resolveGmDispatcher().dispatchAction(req.getAction(), targetPlayerId, params, traceId, false);
            } catch (IllegalArgumentException e) {
                logger.warn("解析TestGmExec参数失败 action={} paramsJson={}", req.getAction(), req.getParamsJson(), e);
                gmResult = GmResult.fail(ErrorMsgEnum.gm_cmd_param, "params_json必须是合法的JSON对象: " + e.getMessage())
                        .withTraceId(traceId);
            } catch (Exception e) {
                logger.warn("执行TestGmExec异常 action={} targetPlayerId={}", req.getAction(), targetPlayerId, e);
                gmResult = GmResult.fail(ErrorMsgEnum.unknown, "执行GM动作异常: " + e.getMessage())
                        .withTraceId(traceId);
            }
        }
        int seq = client instanceof GameClient gameClient ? gameClient.getCurMessageSeq() : 0;
        int reqMsgId = PbProtocol.TestGmExecRequest_6f000102;
        String reqMsgName = PbProtocol.idNameMap != null
                ? PbProtocol.idNameMap.getOrDefault(reqMsgId, "TestGmExecRequest_6f000102")
                : "TestGmExecRequest_6f000102";
        resp.setResultJson(toJson(gmResult));
        resp.setPlayerId(targetPlayerId);
        resp.setMsgId(reqMsgId);
        resp.setMsgName(reqMsgName);
        resp.setSeq(seq);
        resp.setTimestampMs(DateUtil.currentTimeMillis());
        resp.setTraceId(gmResult.getTraceId() != null && !gmResult.getTraceId().isBlank() ? gmResult.getTraceId() : traceId);
        client.sendProtocol(resp.build());
    }

    private void configQuery(NetClient client, Object message) {
        TestConfigQueryRequest_6f000104 req = (TestConfigQueryRequest_6f000104) message;
        TestConfigQueryResponse_6f000105.Builder resp = TestConfigQueryResponse_6f000105.newBuilder();
        if (GmSecurityPolicy.currentRunMode().isProduction()) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.production_gm_not_allow.getId());
            return;
        }
        resp.setSchemaVersion(GmResult.SCHEMA_VERSION);
        String op = req.getOp() == null ? "" : req.getOp().trim();
        try {
            Object payload;
            if ("tables".equalsIgnoreCase(op) || "listTables".equalsIgnoreCase(op)) {
                List<Map<String, Object>> tables = resolveConfigQueryService().listTables();
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("schemaVersion", GmResult.SCHEMA_VERSION);
                result.put("ok", true);
                result.put("total", tables.size());
                result.put("tables", tables);
                payload = result;
            } else if ("list".equalsIgnoreCase(op)) {
                Map<String, String> filters = parseStringMap(req.getFilterJson());
                Integer page = req.getPage() > 0 ? req.getPage() : 1;
                Integer pageSize = req.getPageSize() > 0 ? req.getPageSize() : ConfigQueryService.DEFAULT_PAGE_SIZE;
                Map<String, Object> pageResult = resolveConfigQueryService().listRows(req.getTable(), filters, page, pageSize);
                pageResult.put("ok", true);
                payload = pageResult;
            } else if ("get".equalsIgnoreCase(op)) {
                if (req.getId() < 0) {
                    payload = GmResult.fail(ErrorMsgEnum.request_parameter_error, "id不能为负数");
                } else {
                    String key = extractLookupKey(req.getFilterJson());
                    Integer id = req.getId() > 0 ? req.getId() : null;
                    Object row = resolveConfigQueryService().getRow(req.getTable(), id, key);
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("schemaVersion", GmResult.SCHEMA_VERSION);
                    result.put("ok", true);
                    result.put("table", req.getTable());
                    result.put("data", row);
                    payload = result;
                }
            } else {
                payload = GmResult.fail(ErrorMsgEnum.request_parameter_error, "不支持的config查询op: " + op);
            }
            resp.setResultJson(toJson(payload));
        } catch (LogicException e) {
            logger.warn("TestConfigQuery业务参数或数据异常 op={} table={}", op, req.getTable(), e);
            resp.setResultJson(toJson(GmResult.fail(e.getErrorCode(), resolveErrorName(e.getErrorCode()), e.getMessage())));
        } catch (Exception e) {
            logger.warn("TestConfigQuery执行异常 op={} table={}", op, req.getTable(), e);
            resp.setResultJson(toJson(GmResult.fail(ErrorMsgEnum.request_parameter_error, "配置查询异常: " + e.getMessage())));
        }
        client.sendProtocol(resp.build());
    }

    private void protoCatalog(NetClient client, Object message) {
        TestProtoCatalogRequest_6f000106 req = (TestProtoCatalogRequest_6f000106) message;
        TestProtoCatalogResponse_6f000107.Builder resp = TestProtoCatalogResponse_6f000107.newBuilder();
        if (GmSecurityPolicy.currentRunMode().isProduction()) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.production_gm_not_allow.getId());
            return;
        }
        resp.setSchemaVersion(GmResult.SCHEMA_VERSION);
        try {
            int rawModuleFilter = req.getModuleFilter();
            if (rawModuleFilter < 0 || rawModuleFilter > 0xFF) {
                resp.setCatalogJson(toJson(GmResult.fail(ErrorMsgEnum.request_parameter_error, "module_filter必须在0到255之间")));
            } else {
                String moduleFilter = rawModuleFilter > 0 ? String.format("0x%02x", rawModuleFilter & 0xFF) : null;
                Map<String, Object> catalog = resolveProtoCatalogService().listCatalog(moduleFilter, null, req.getIncludeSchemas(), true);
                resp.setCatalogJson(toJson(catalog));
            }
        } catch (Exception e) {
            logger.warn("查询协议目录异常 moduleFilter={}", req.getModuleFilter(), e);
            resp.setCatalogJson(toJson(GmResult.fail(ErrorMsgEnum.unknown, "查询协议目录异常: " + e.getMessage())));
        }
        client.sendProtocol(resp.build());
    }

    private void inspectPlayer(NetClient client, Object message) {
        TestInspectPlayerRequest_6f000108 req = (TestInspectPlayerRequest_6f000108) message;
        TestInspectPlayerResponse_6f000109.Builder resp = TestInspectPlayerResponse_6f000109.newBuilder();
        if (GmSecurityPolicy.currentRunMode().isProduction()) {
            client.sendProtocol(resp.build(), ErrorMsgEnum.production_gm_not_allow.getId());
            return;
        }
        resp.setSchemaVersion(GmResult.SCHEMA_VERSION);
        try {
            long rawTargetId = req.getTargetPlayerId();
            if (rawTargetId < 0) {
                resp.setResultJson(toJson(GmResult.fail(ErrorMsgEnum.request_parameter_error, "target_player_id不能为负数")));
            } else {
                long targetPlayerId = rawTargetId > 0 ? rawTargetId : client.getPlayerId();
                GmResult inspectResult = resolvePlayerInspectService().inspectPlayer(targetPlayerId, req.getModulesList(), req.getScope(), null);
                resp.setResultJson(toJson(inspectResult));
            }
        } catch (Exception e) {
            logger.warn("执行玩家只读自省异常 targetPlayerId={}", req.getTargetPlayerId(), e);
            resp.setResultJson(toJson(GmResult.fail(ErrorMsgEnum.unknown, "执行玩家只读自省异常: " + e.getMessage())));
        }
        client.sendProtocol(resp.build());
    }

    private GmDispatcher resolveGmDispatcher() {
        if (gmDispatcher != null) {
            return gmDispatcher;
        }
        GmDispatcher singleton = GmDispatcher.getInstance();
        if (!singleton.hasRegisteredGroupBeans() && SpringContextLoader.getContext() != null) {
            singleton.initFromSpring();
            if (!singleton.hasRegisteredGroupBeans()) {
                logger.error("Spring上下文已存在但GmDispatcher兜底扫描后仍无@GmGroup动作Bean");
            }
        }
        return singleton;
    }

    private ConfigQueryService resolveConfigQueryService() {
        if (configQueryService == null) {
            configQueryService = new ConfigQueryService();
        }
        return configQueryService;
    }

    private ProtoCatalogService resolveProtoCatalogService() {
        if (protoCatalogService == null) {
            protoCatalogService = new ProtoCatalogService();
        }
        return protoCatalogService;
    }

    private PlayerInspectService resolvePlayerInspectService() {
        if (playerInspectService == null) {
            playerInspectService = new PlayerInspectService();
        }
        return playerInspectService;
    }

    private Map<String, Object> parseJsonObjectMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            Map<String, Object> parsed = GM_JSON_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {
            });
            return parsed != null ? parsed : Collections.emptyMap();
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private Map<String, String> parseStringMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        Map<String, Object> raw = parseJsonObjectMap(json);
        Map<String, String> result = new LinkedHashMap<>();
        for (Entry<String, Object> entry : raw.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                result.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return result;
    }

    private String extractLookupKey(String filterJson) {
        if (filterJson == null || filterJson.isBlank()) {
            return null;
        }
        String trimmed = filterJson.trim();
        if (trimmed.startsWith("{")) {
            Map<String, Object> map = parseJsonObjectMap(trimmed);
            Object keyObj = map.get("key");
            return keyObj != null ? String.valueOf(keyObj) : null;
        }
        return trimmed;
    }

    private static String resolveErrorName(int errorCode) {
        for (ErrorMsgEnum err : ErrorMsgEnum.values()) {
            if (err.getId() == errorCode) {
                return err.name();
            }
        }
        return "LOGIC_ERROR";
    }

    private String toJson(Object value) {
        try {
            return GM_JSON_MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            logger.warn("序列化0x6f测试响应JSON失败", e);
            return "{\"schemaVersion\":\"1.0\",\"ok\":false,\"errorCode\":-1,\"errorName\":\"JSON_SERIALIZE_ERROR\",\"summary\":\"JSON序列化失败\"}";
        }
    }
}
