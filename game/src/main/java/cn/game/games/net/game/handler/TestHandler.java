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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.game.games.net.game.module.guarantee.Guarantee;
import cn.game.games.net.game.module.guarantee.GuaranteeModule;
import cn.game.games.net.game.module.rank.RankService;
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
import cn.game.games.cache.entity.Chapter;
import cn.game.games.cache.entity.Hero;
import cn.game.games.cache.entity.Item;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.SimplePlayer;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.client.GameClient;
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
import cn.game.games.net.game.module.battle.BattleHandler;
import cn.game.games.net.game.module.battle.BattleModule;
import cn.game.games.net.game.module.battle.LingShanWenChanBattle;
import cn.game.games.net.game.module.battle.MengYanMiJingBattle;
import cn.game.games.net.game.module.battle.ShiLuoZhenJingBattle;
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
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.config.ItemConfig;
import cn.game.protocol.generated.config.RandomGivenConfig;
import cn.game.protocol.generated.enume.QuestTypeEnum;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PlayerMsg.PlayerLogoutResponse_01000004;
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
import cn.game.util.SpringContextLoader;
import io.vertx.core.Future;

@Component
public class TestHandler extends GameBaseHandler {

    // protected Logger gamerecvLog = LoggerFactory.getLogger("gamerecvLog");
    protected Logger gamesendLog = LoggerFactory.getLogger("gamesendLog");

    protected Logger logger = LoggerFactory.getLogger(TestHandler.class);

    ExecutorService executorService = Executors.newFixedThreadPool(1280);

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
        if (ServerContext.getInstance().getRunMode().isProduction()) {
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
    }

    //角色晋升
    private void gmCmd(NetClient client, Object message) {
        TestMsg.TestGmCmdRequest_6f000001 req = (TestMsg.TestGmCmdRequest_6f000001) message;
        TestMsg.TestGmCmdResponse_6f000002.Builder resp = TestMsg.TestGmCmdResponse_6f000002.newBuilder();
        String cmd = req.getCmd();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        PlayerEvent params = new PlayerEvent(cmd.split(" "));
        int paramsCount = params.getParams().length;
        // 默认的参数变量
        int p1 = 0;
        int p2 = 0;
        int p3 = 0;
        if (paramsCount > 1) {
            if (StringUtils.isNumeric(params.getParameter(1).toString())) {
                p1 = Integer.parseInt(params.getParameter(1).toString());
            }
        }
        if (paramsCount > 2) {
            if (StringUtils.isNumeric(params.getParameter(2).toString())) {
                p2 = Integer.parseInt(params.getParameter(2).toString());
            }
        }
        if (paramsCount > 3) {
            if (StringUtils.isNumeric(params.getParameter(3).toString())) {
                p3 = Integer.parseInt(params.getParameter(3).toString());
            }
        }
        switch(params.getStringParameter(0).toLowerCase()) {
            case "item":
                {
                    List<RewardInfo> items = TestHelper.addItems(player, p1, p2);
                    client.sendProtocol(RewardPush_55000501.newBuilder().addAllRewards(items).build());
                    break;
                }
            case "hero":
                {
                    List<RewardInfo> resources = PlayerHelper.addResources(player, p1, p2, OpType.Test);
                    client.sendProtocol(RewardPush_55000501.newBuilder().addAllRewards(resources).build());
                    break;
                }
            case "herolv":
            {
                int size=player.getHeroModule().list().size();
                for (int i = 0; i <size ; i++) {
                    var hero = player.getHeroModule().list().stream().toList().get(i);
                    if(hero.getConfigId()==p1)
                    {
                        hero.setLevel(p2);
                    }
                }
                break;
            }
            case "tdlv":
                {
                    // 设置天道修为等级
                    if (p1 == 0) {
                        throw new LogicException(ErrorMsgEnum.gm_cmd_param.ID);
                    }
                    DevelopModule developModule = player.getDevelopModule();
                    developModule.setHeavenlyDaoLevel(p1);
                    break;
                }
            case "zxgk":
                {
                    // 设置主线关卡id
                    if (p1 == 0) {
                        throw new LogicException(ErrorMsgEnum.gm_cmd_param.ID);
                    }
                    if (p1 < 1000) {
						List<BattleConfig> battleTypeList = BattleManager.instance().getBattleTypeList(DungeonTypeEnum.BattleChapter.getId()); 
						p1 = battleTypeList.get(p1 - 1).ID;
					}
                    BattleModule battleModule = player.getBattleModule();
                    battleModule.setMainBattleHighest(p1);
                    BattleConfig battleConfig = BattleManager.instance().getNullable(p1);
                    while (battleConfig != null) {
                        battleModule.addChapter(battleConfig.ID);
                        Chapter chapter = battleModule.getChapter(battleConfig.ID);
                        chapter.setBattleTime(30);
                        chapter.setPass(true);
                        battleConfig = BattleManager.instance().getNullable(battleConfig.preBattle);
                    }
                    // 刷新一下功能开启
                    player.getFuncModule().refreshFuncOpen(); 
                    break;
                }
            case "lingshan":
            {
            	// 设置灵山问禅层数
            	if (p1 == 0) {
            		throw new LogicException(ErrorMsgEnum.gm_cmd_param.ID);
            	}
            	BattleModule battleModule = player.getBattleModule();
            	LingShanWenChanBattle battle = battleModule.getBattle(DungeonTypeEnum.LingShanWenChan); 
            	battle.setLastCompleteFloor(p1);
            	
            	break;
            }
            case "slzj":
                {
                    // 设置失落真经关卡id
                    BattleModule battleModule = player.getBattleModule();
                    ShiLuoZhenJingBattle battle = battleModule.getBattle(DungeonTypeEnum.ShiLuoZhenJing);
                    if (battle != null) {
                        BattleConfig battleConfig = BattleManager.instance().get(p1);
                        if (battleConfig.preBattle > 0) {
                            battle.setCompleteBattleId(battleConfig.preBattle);
                        }
                        battle.setStartBattleId(p1);
                        if (p2 > 0 && p2 <= 10) {
                            battle.setBattleStage(p2);
                        }
                    }
                    break;
                }
            case "mymj":
                {
                    // 设置梦魇秘境关卡id
                    BattleModule battleModule = player.getBattleModule();
                    MengYanMiJingBattle battle = battleModule.getBattle(DungeonTypeEnum.MengYanMiJing);
                    if (battle != null) {
                        BattleConfig battleConfig = BattleManager.instance().get(p1);
                        if (battleConfig.preBattle > 0) {
                            battle.setCompleteBattleId(battleConfig.preBattle);
                            battle.setMaxBattleId(battleConfig.preBattle);
                        }
                        battle.setStartBattleId(p1);
                    }
                    break;
                }
            case "slzjsd":
                {
                    // 设置失落真经手动关卡。
                    BattleModule battleModule = player.getBattleModule();
                    ShiLuoZhenJingBattle battle = battleModule.getBattle(DungeonTypeEnum.ShiLuoZhenJing);
                    if (battle != null) {
                        if (p1 > 0) {
                            BattleConfig battleConfig = BattleManager.instance().get(p1);
                            if (battleConfig.preBattle > 0) {
                                battle.setCompleteBattleId(battleConfig.preBattle);
                                battle.setHistoryMaxBattleId(battleConfig.preBattle);
                            }
                            battle.setStartBattleId(p1);
                        }
                        if (p2 > 0 && p2 < 10) {
                            battle.setBattleStage(p2);
                        } else {
                            battle.setBattleStage(10);
                        }
                        battle.setZhijieshoudong(true);
                    }
                    break;
                }
            case "quest":
                {
                    QuestManager.instance().get(p1);
                    // 完成某个任务
                    QuestModule module = player.getQuestModule();
                    List<RewardInfo> items = module.finish(p1, 0);
                    client.sendProtocol(RewardPush_55000501.newBuilder().addAllRewards(items).build());
                    break;
                }
            case "playerquit":
                {
                    // 把某人退出
                    if (paramsCount == 1) {
                        // 退出所有人
                        GameClientManager.getInstance().logoutAll(LogoutType.GMTestRequest);
                    } else {
                        // 退出某人
                        TestHelper.logoutPlayer(params.getLong(1), LogoutType.GMTestRequest);
                    }
                    break;
                }
            case "playerdel":
                {
                    if (p1 == 0) {
                        throw new LogicException(ErrorMsgEnum.gm_cmd_param.ID);
                    }
                    // 将某人删档
                    PlayerHelper.deletePlayerData(params.getLong(1));
                    break;
                }
            case "itemdel":
                {
                	long pid = p1; 
                	if (pid == 0) {
						pid = playerId; 
					}
                	GameServerInterface playerProxy = ServerHelper.getPlayerProxy(pid); 
                	playerProxy.delResources(pid, p2, p3); 
                    break;
                }
            case "huanfu":
                {
                    String serverId = params.getStringParameter(1);
                    TestHelper.transferServer(playerId, serverId);
                    break;
                }
            case "newday":
                {
                    int nowDay = DateUtil.getDay();
                    player.getData().setRefreshDay(nowDay - 1);
                    PlayerHelper.refreshDay(player);
                    break;
                }
            case "qingshen":
            {
                player.getDrawModule().getHeroRecruit().refresh();
                break;
            }
            case "rankreward":
            {
                RankService.getInstance().reward(p1);
               // player.getDrawModule().getHeroRecruit().refresh();
                break;
            }
            case "gamenewday":
			{
				ServerContext.getInstance().fireEvent(ServerEventTypeEnum.NewDay);
				int nowDay = DateUtil.getDay();
				PlayerManager.getInstance().getAllPlayer().values().forEach(p -> {
					player.getData().setRefreshDay(nowDay - 1);
					PlayerHelper.refreshDay(player);
				});
				break;
			}
            case "super":
            {
                for (Asset resourceEnum : Asset.values()) {
                    PlayerHelper.addResources(player, resourceEnum.ID, 1000000, OpType.Test);
                }
                for (var resourceEnum : HeroManager.instance().list()) {
                    if(resourceEnum.HeroType==1)
                    {
                        List<RewardInfo> resources = PlayerHelper.addResources(player, resourceEnum.ID, 100, OpType.Test);
                    }
                }
                int size=player.getHeroModule().list().size();
                for (int i = 0; i <size ; i++) {
                    var hero = player.getHeroModule().list().stream().toList().get(i);
                    hero.setLevel(100);
                }
                for (var resourceEnum : GemManager.instance().list()) {
                    List<RewardInfo> resources = PlayerHelper.addResources(player, resourceEnum.ID, 100, OpType.Test);
                }
                for (var resourceEnum : EquipManager.instance().list()) {
                    List<RewardInfo> resources = PlayerHelper.addResources(player, resourceEnum.ID, 100, OpType.Test);
                }
                BattleModule battleModule = player.getBattleModule();
                battleModule.setMainBattleHighest(110085);
                BattleConfig battleConfig = BattleManager.instance().getNullable(110085);
                while (battleConfig != null) {
                    battleModule.addChapter(battleConfig.ID);
                    Chapter chapter = battleModule.getChapter(battleConfig.ID);
                    chapter.setBattleTime(30);
                    chapter.setPass(true);
                    battleConfig = BattleManager.instance().getNullable(battleConfig.preBattle);
                }
                break;
            }
            case "init":
            {
                TestHelper.setMaxCurrency(player, OpType.Test);
                break;
            }
            case "ssss":
            {
                File file = new File("D:\\test\\");
                if (!file.exists()) {
                    file.mkdirs(); // 创建目录
                }
                for (int k = 0; k < p2; k++) {
                    try  {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    player.getDrawModule().getHeroRecruit().getDrawHeroCountMap().clear();
                    String fileName = "qualityList"+ DateUtil.currentTimeSeconds() +".txt";
                    // 创建一个文件来保存数据
                    File outputFile = new File(file, fileName);
                    StringBuilder stringBuilder = new StringBuilder();
                    GuaranteeModule guaranteeModule = player.getGuaranteeModule();
                    Guarantee guarantee = guaranteeModule.get(GuaranteeTypeEnum.DrawRefresh);
                    guarantee.setRound(1);
                    guarantee.setStage(1);
                    guarantee.setCount(0);
                    guarantee.setId(1);
                    for (int i = 0; i < p1; i++) {
                        var hero =  player.getDrawModule().getHeroRecruit();
                        hero.refresh();
                        List<Integer> qualityList = new ArrayList<>();
                        for(var h:hero.getDrawHeroInPoolList()) {
                            qualityList.add(h.quality);
                        }
                        qualityList.sort((o1, o2) -> o2-o1);
                        int end= hero.radom31test();

                        qualityList.add( end);
                        for (int j = 0; j < qualityList.size(); j++) {  // 修改了循环条件
                            stringBuilder.append(qualityList.get(j));
                            if(j != qualityList.size()-1) {  // 修改了条件判断
                                stringBuilder.append(":");
                            }
                        }
                        stringBuilder.append("\n");
                        // 将字符串写入文件
                        try (FileWriter writer = new FileWriter(outputFile)) {
                            writer.write(stringBuilder.toString());
                        } catch (IOException e) {
                            logger.error("写入文件失败", e);
                        }
                        player.handleEvent(EventTypeEnum.HeroRecruit);
                    }

                }

               // System.out.println(stringBuilder.toString());
                break;
            }
            case "rankds":
            {
                RankService.getInstance().setScoreAsync(player.getServerId(), RankType.DaShengLeiTaiSeason, player.getPlayerId(), p1);
                RankService.getInstance().setScoreAsync(player.getServerId(), RankType.DaShengLeiTaiDay, player.getPlayerId(),p1);
                break;
            }
            case "time":
            {
            	if (ServerContext.getInstance().getRunMode().isProduction()) {
					throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID) ;
				}
            	
            	String[] timeArgs=  new String[] { params.getStringParameter(1) }; 
            	LocalDateTime now = LocalDateTime.now(); 
            	PreviewResult previewPlannedTime = LinuxTimeShift.previewPlannedTime(timeArgs); 
            	if (previewPlannedTime!=null) {
            		if (previewPlannedTime.crossedDay) {
            			PlayerManager.getInstance().getAllPlayer().values().forEach(p -> {
            				p.getData().setOfflineTime(DateUtil.currentTimeMillis());
            			});
            		}
				}
            	LinuxTimeShift.main(timeArgs); 
            	
            	LocalDateTime nowDateTime = LocalDateTime.now();
            	if (previewPlannedTime!=null) {
            		if (previewPlannedTime.crossedDay) {
            			PlayerManager.getInstance().getAllPlayer().values().forEach(p -> {
            				PlayerHelper.refresh(player);
            			});
            		}
            	}
				PlayerManager.getInstance().getAllPlayer().values().forEach(p -> {
					player.handleEvent(EventTypeEnum.SystemTimeChange);
				});
				ServerContext.getInstance().fireEvent(ServerEventTypeEnum.SystemTimeChange);
            	if (nowDateTime.isAfter(now) && DateUtil.diff(nowDateTime, now, ChronoUnit.WEEKS) > 0) {
    				ServerContext.getInstance().fireEvent(ServerEventTypeEnum.NewWeek);
    				PlayerManager.getInstance().getAllPlayer().values().forEach(p -> {
    					player.handleEvent(EventTypeEnum.NewWeek);
    				});
				}else if (nowDateTime.isAfter(now) && DateUtil.diff(nowDateTime, now, ChronoUnit.MONTHS) > 0) {
					ServerContext.getInstance().fireEvent(ServerEventTypeEnum.NewMonth);
					PlayerManager.getInstance().getAllPlayer().values().forEach(p -> {
						player.handleEvent(EventTypeEnum.NewMonth);
					});
				}else if (nowDateTime.isAfter(now) && DateUtil.diff(nowDateTime, now, ChronoUnit.DAYS) > 0) {
					ServerContext.getInstance().fireEvent(ServerEventTypeEnum.NewDay);
					PlayerManager.getInstance().getAllPlayer().values().forEach(p -> {
						player.handleEvent(EventTypeEnum.NewDay);
					});
				}
            	break;
            }
            default:
                client.sendProtocol(resp.build(), ErrorMsgEnum.gm_cmd_not_exist.getId());
                break;
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

    protected void testDBinsert(NetClient client, Object message) {
        // 如果是顺序执行，每个db操作大概50-60ms之间,100个要5s
        // 线程池执行，100个操作，2800ms，#连接池中保留的最大连接数maxPoolSize = 5
        // 同上，maxPoolSize = 50 ， 使用了800多ms，时间明显减少。或者600多
        // 同上， maxPoolSize = 500 ，结果基本一样。
        // 10 个连接创建玩家 1000 个消耗时间：8256，已经有一些超时的了
        // 100 个连接创建玩家 1000 个消耗时间：4424，时间减少了，没有超时的。
        // 1000 个连接创建玩家 1000 个消耗时间：4946/4265，和上面100个的没什么区别。估计是达到了mysql本身的连接限制
        // 用测试服务器，mysql最大连接配置为10000，连接池10000个连接创建1000个玩家，第一次用了2000多ms，后面几次都是600ms
        //		TestDbInsertRequest_7f000001 request = (TestDbInsertRequest_7f000001) message;
        //		int intarg = request.getTimes();
        int intarg = 10;
        long start = System.currentTimeMillis();
        //		ExecutorService executorService = Executors.newCachedThreadPool();
        final CountDownLatch latch = new CountDownLatch(intarg);
        for (int i = 0; i < intarg; i++) {
            executorService.execute(new Runnable() {

                @Override
                public void run() {
                    //					long id = GameServer.getInstance().nextPlayerId();
                    long id = 0;
                    // TODO Auto-generated method stub
                    PlayerData player = new PlayerData();
                    // player.setSeq(seq) ;
                    player.setGender(true);
                    player.setCreateDate(DateUtil.getStringDate());
                    player.setLevel(1);
                    player.setUid(id);
                    player.setName(id + "");
                    player.setLoginDate(DateUtil.getStringDate());
                    player.setRefreshDay(DateUtil.getDay());
                    ObjUtil.setDefaultValue(player);
                    player.setPlayerId(id);
                    try {
                        DAO.executeSync(PlayerDataMapper.class, MapperConstant.insert, player);
                        latch.countDown();
                    } catch (Throwable e) {
                        e.printStackTrace();
                        return;
                    }
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        log.info("创建玩家 " + intarg + " 个消耗时间：" + (System.currentTimeMillis() - start));
        System.out.println("创建玩家 " + intarg + " 个消耗时间：" + (System.currentTimeMillis() - start));
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
        PlayerHelper.deletePlayerData(playerId);
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
        TestHelper.setMaxCurrency(player,OpType.Test);
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

}
