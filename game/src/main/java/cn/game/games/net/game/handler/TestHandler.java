package cn.game.games.net.game.handler;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.core.base.ServerContext;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.process.Processor;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Hero;
import cn.game.games.cache.entity.Item;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.helper.ItemHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.QuestHelper;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.develop.AttrModule;
import cn.game.games.net.game.module.develop.DevelopModule;
import cn.game.games.net.game.module.develop.attr.AttrCalcType;
import cn.game.games.net.game.module.develop.attr.PlayerAttrCalc;
import cn.game.games.net.game.module.develop.hero.HeroModule;
import cn.game.games.net.game.module.draw.DrawModule;
import cn.game.games.net.game.module.quest.Quest;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.config.ItemConfig;
import cn.game.protocol.generated.config.RandomGivenConfig;
import cn.game.protocol.generated.config.SoulPetConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.QuestTypeEnum;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.generated.manager.ItemManager;
import cn.game.protocol.generated.manager.RandomGivenManager;
import cn.game.protocol.generated.manager.SoulPetManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.TestMsg;
import cn.game.protocol.protobuf.TestMsg.TestAddItemRequest_6f000008;
import cn.game.protocol.protobuf.TestMsg.TestAddItemResponse_6f000009;
import cn.game.protocol.protobuf.TestMsg.TestMessageRequest_6f000080;
import cn.game.protocol.protobuf.TestMsg.TestMissionFinishRequest_6f000022;
import cn.game.protocol.protobuf.TestMsg.TestMissionFinishResponse_6f000023;
import cn.game.protocol.protobuf.TestMsg.TestPlayerAssetDataRequest_6f000028;
import cn.game.protocol.protobuf.TestMsg.TestPlayerAssetDataResponse_6f000029;
import cn.game.protocol.protobuf.TestMsg.TestRequest_6f000020;
import cn.game.util.Config;
import cn.game.util.DateUtil;
import cn.game.util.IntMapWrapper;
import cn.game.util.ObjUtil;
import cn.game.util.SpringContextLoader;

@Component
public class TestHandler extends BaseHandler {

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

//		putInvoker(PbProtocol.TestDbInsertRequest_7f000001, new Invoker() {
//			@Override
//			public void invoke(NetClient client, Object message) throws InvalidProtocolBufferException {
//				testDBinsert(client, message);
//			}
//
//		});
		if (ServerContext.getInstance().getRunMode().isProduction()) {
			return;
		}
		logger.warn("===================================test command is enable =================================");
		putInvoker(PbProtocol.TestGmCmdRequest_6f000001, this::gmCmd);
		putInvoker(PbProtocol.TestAddItemRequest_6f000008, this::addItem);
//		putInvoker(PbProtocol.TestMailRequest_6f000010, this::mail);
		putInvoker(PbProtocol.TestRequest_6f000020, this::test);
//		putInvoker(PbProtocol.TestDbRequest_6f000041, this::testDBinsert);
		putInvoker(PbProtocol.TestMissionFinishRequest_6f000022, this::finishMission);
		putInvoker(PbProtocol.TestPlayerAssetDataRequest_6f000028, this::assetData);
//		putInvoker(PbProtocol.TestStoryFinishRequest_6f000024, this::finishStory);

//		putInvoker(PbProtocol.TestAddOrDelBagItemRequest_6f000032, this::bagTest);

//		putInvoker(PbProtocol.TestMessageRequest_6f000080, this::testMessage);

//		putInvoker(PbProtocol.TestCommandRequest_6f000095, this::command);
//		putInvoker(PbProtocol.TestRoleAttributeRequest_6f000100, this::getRoleAttribute);
//		putInvoker(PbProtocol.TestGameEventTriggerRequest_6f000105, this::triggerEvent);
//		putInvoker(PbProtocol.TestRolePromotionRequest_6f00010b, this::rolePromotion);
//		putInvoker(PbProtocol.TestRoleUnlockOccupationTalentNodeRequest_6f00010d, this::unlockOccupationTalentNode);
	}

	//角色晋升
	private void gmCmd(NetClient client, Object message) {
		TestMsg.TestGmCmdRequest_6f000001 req = (TestMsg.TestGmCmdRequest_6f000001) message;
		TestMsg.TestGmCmdResponse_6f000002.Builder resp = TestMsg.TestGmCmdResponse_6f000002.newBuilder();
		String cmd = req.getCmd();
		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		String[] params = cmd.split(" ");

		switch (params[0]) {
		case "item": {

			break;
		}
		default:
			client.sendProtocol(resp.build(), ErrorMsgEnum.unknown.getId());
			break;
		}
		client.sendProtocol(resp.build());
	}

	/*
		// 角色晋升
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
	private void assetData(NetClient client, Object message) {
		TestPlayerAssetDataRequest_6f000028 request = (TestPlayerAssetDataRequest_6f000028) message;
		TestPlayerAssetDataResponse_6f000029.Builder response = TestPlayerAssetDataResponse_6f000029.newBuilder();

		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());

		response.putAllAssets(player.getCurrencyModule().getCurrencyMap().getMap()) ; 
		
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
		TestRequest_6f000020 req = (TestRequest_6f000020) message;
		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		testcalcPower(player);
//		List<RewardInfo> resources = PlayerHelper.addResources(player, 200032, 10);
//		for (RewardInfo rewardInfo : resources) {
//			System.out.println(rewardInfo);
//		}
//		PayItem payItem = new PayItem();
//		payItem.setOrderId(System.currentTimeMillis());
//		payItem.setRmb(30);
//		payItem.setPayType(PayType.MonthCard);
//		payItem.setPayId(1);
//		GameLogger.recharge(player, payItem);

//		GameClientManager.getInstance().logout((GameClient) client);
//		SecretscriptModule module = player.getModule(SecretscriptModule.class);
//		module.add(101, OpType.None);
//		GameClientManager.getInstance().logout((GameClient)client); 
//		List<Goods> list = new ArrayList<Goods>(); 
//		list.add(new Goods(1,66666)) ; 
//		MailHelper.sendMail(playerId, "", "", "content", MailHelper.SYSTEM, list);
//		Collection<Hero> list = player.getHeroModule().list();
//		PlayerHelper.addReward(player, 20011, OpType.None);
//		player.handleEvent(EventTypeEnum.CostItem, Asset.diamond.ID, 3000);
//		Iterator<Hero> iterator = list.iterator();
		long uid = 0;
//		while (iterator.hasNext()) {
//			Hero hero = (Hero) iterator.next();
//			if (hero.getConfigId() == 354001) {
//				uid = hero.getId();
//				break;
//			}
//		}

//		player.getHeroModule().del(uid, OpType.None);
//		drawTest2(player);
//		long uid = 0;
//		for (Hero hero : list) {
//			uid = hero.getId();
//			break;
//		}
//		player.getHeroModule().del(uid, OpType.None);

//		drawTest(player);


//		CommonLogger.error("what the fuck by common logger");
//		log.error("what the fuck by log");
//		GameLogger.heart();


//		player.getQuestModule().addConditionCount(ConditionTypeEnum.ChapterFinish, 3, 1, 2);

//		AttrModule module = player.getModule(AttrModule.class);
//		module.calcAllAttr();
//		module.buildBattleAttrs();
//		PlayerHelper.addResources(player, 610001, 1);
//		System.out.println();
//		System.out.println();
//		PlayerHelper.refresh(player);
//		for (int i = 0; i < 100000; i++) {
//			player.getData().setLevel(1001 + i);
//			DAO.update(PlayerDataMapper.class, player.getData());
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}

//		System.exit(0);
//		QuestOp questOp = PlayerCacheFactory.getCache(client.getPlayerId(), QuestOp.class);
//		questOp.open(47101, false);
//
//		PlayerHelper.addResources(client.getPlayerId(), ResourceEnum.Gold.getId(), 500);
//		IGCVersion_1046930.GetServerVersion(r -> {
//			System.out.println("执行成功" + r);
//		}, e -> {
//			System.out.println("执行失败");
//			e.printStackTrace();
//		});
//		ExploreOp exploreOp = player.getModule(ExploreOp.class);
//		IExploreObjectContainer objectContainer = exploreOp.getObjectContainer();
//		BuffOp buffOp = PlayerCacheFactory.getCache(client.getPlayerId(), BuffOp.class);

//		List<Integer> target = new ArrayList<>();
//		target.add((int) client.getPlayerId());
//		buffOp.add(501050, target, false);
//		BuffValue buffValue = buffOp.getBuffValue(EffectTargetTypeEnum.AllTeam, client.getPlayerId(),
//				EffectEnum.RecoveryStopInStateRecovery);
//		System.out.println(buffValue.getValue(BuffValue.CHANGE_BY_VALUE, 1));

//		
//		System.gc();
//		long playerId = client.getPlayerId(); Player player = PlayerManager.getInstance().getPlayer(playerId);
//
//		long other = playerId - 1;
//
//		System.out.println("当前线程： " + Thread.currentThread());
//		Context context = VxHolder.vertx.getOrCreateContext();
//
//		GameClient otherClient = GameClientManager.getInstance().getGameClientByPlayer(other);
//		if (otherClient != null) {
//
//			Promise<Object> promise = Promise.promise();
//
//			otherClient.getContext().runOnContext(v -> {
//				System.out.println("当前线程： " + Thread.currentThread());
//
//				Player otherPlayer = PlayerManager.getInstance().getPlayer(other);
//				otherPlayer.setCoin(3900L);
//				promise.complete(true);
//			});
//			Future<Object> future = promise.future();
//			future.onComplete(r -> {
////				context.runOnContext(v -> {
//
//					System.out.println("当前线程： " + Thread.currentThread());
//					System.out.println(r);
////				});
//			});

//			res.addListener(r -> {
//				Boolean boolean1 = (Boolean) r.get();
//				TaskManager.getInstance().addMainTask(() -> {
//					System.out.println(boolean1);
//					if (boolean1) {
//						System.out.println(Thread.currentThread().getName() + " 进行后续操作");
//					}
//				});
//			});
	}

	private void testcalcPower(Player player) {
		// 神将属性
		Map<Long, IntMapWrapper> heroAttrs = new HashMap<Long, IntMapWrapper>();
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

		for (Hero hero : list) {
//			if (hero.getLevel() == 1) {
//				continue; ti
//			}
			HeroConfig heroConfig = HeroManager.instance().get(hero.getConfigId());
			System.out.println("hero id : " + hero.getConfigId() + " name : " + heroConfig.name + " level : " + hero.getLevel());
			System.out.println("基本属性： " + BattleHelper.makeHeroAttr2(hero));
			IntMapWrapper heroAttr = BattleHelper.makeHeroAttr2(hero);
			System.out.println("单英雄不算外围战力： " + BattleHelper.calcCombat(heroAttr));
			System.out.println();

			heroAttrs.put(hero.getId(), heroAttr);
		}
		System.out.println();
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

	/*
	protected void mail(NetClient client, Object message) {
		TestMailRequest_6f000010 req = (TestMailRequest_6f000010) message;
		
		int receiveId = req.getReceiveId();
		List<GoodsInfo> attachmentsList = req.getAttachmentsList();
		List<Goods> attachmentList = new ArrayList<Goods>();
		for (GoodsInfo goodsInfo : attachmentsList) {
		Goods g = new Goods();
		g.setId(goodsInfo.getId());
		g.setCount(goodsInfo.getCount());
		attachmentList.add(g);
		}
		MailHelper.sendMail(receiveId, req.getSender(), req.getTitle(), req.getContent(), (byte) 0, attachmentList);
		}*/


	protected void addItem(NetClient client, Object message) {
		TestAddItemRequest_6f000008 req = (TestAddItemRequest_6f000008) message;
		TestAddItemResponse_6f000009.Builder resp = TestAddItemResponse_6f000009.newBuilder();

		if (!Config.gmOpen) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.unknown.getId());
			return;
		}

		List<RewardInfo> allRewards = new ArrayList<>();
		List<RewardInfo> rewardItems = null;
		List<RewardInfo> buildRewardInfo = null;
		int id = req.getId();
		int count = req.getCount();
		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		if (id == 10000001) {
			DevelopModule developModule = player.getDevelopModule();
			developModule.setHeavenlyDaoLevel(count);
			client.sendProtocol(resp.build(), 0);
			return;
		}
		int goodsType = ItemHelper.getGoodsType(id);
		int error = 0;

		try {

			if (count == 0) {
				if (goodsType == 0) {
					goodsType = (byte) id;
				}
				boolean typeCheck = false;
				for (GoodsTypeEnum rewardInfo : GoodsTypeEnum.values()) {
					if (rewardInfo.getId() == goodsType) {
						typeCheck = true;
						break;
					}
				}
				if (!typeCheck) {
					client.sendProtocol(resp.build(), ErrorMsgEnum.config_data_not_found.getId());
					return;
				}
				if (goodsType == GoodsTypeEnum.Resource.getId()) {
					for (Asset resourceEnum : Asset.values()) {
//						if (resourceEnum.getType() == 2 && !inExplore) {
//							continue;
//						}
						rewardItems = PlayerHelper.addResources(player, resourceEnum.ID, 1000000, OpType.Test);
						allRewards.addAll(rewardItems);
					}

				} else if (goodsType == GoodsTypeEnum.Item.getId()) {
					Collection<ItemConfig> list = ItemManager.instance().list();
					for (ItemConfig e : list) {
						rewardItems = PlayerHelper.addResources(player, e.ID, 999, OpType.Test);
						allRewards.addAll(rewardItems);
					}

				} else if (goodsType == GoodsTypeEnum.Hero.getId()) {
					Collection<HeroConfig> list = HeroManager.instance().list();
					for (HeroConfig e : list) {
						rewardItems = PlayerHelper.addResources(player, e.ID, 10, OpType.Test);
						allRewards.addAll(rewardItems);
					}
				} else if (goodsType == GoodsTypeEnum.Pet.getId()) {
					Collection<SoulPetConfig> list = SoulPetManager.instance().list();
					for (SoulPetConfig e : list) {
						rewardItems = PlayerHelper.addResources(player, e.ID, 10, OpType.Test);
						allRewards.addAll(rewardItems);
					}
				} else {

					List<RewardInfo> tmp = PlayerHelper.addResources(player, id, count, OpType.Test);
					allRewards.addAll(tmp);
				}
			} else {
				rewardItems = PlayerHelper.addResources(player, id, count, OpType.Test);
				allRewards.addAll(rewardItems);
			}

			buildRewardInfo = allRewards;
			resp.addAllResource(buildRewardInfo);

		} catch (IllegalArgumentException e) {
			log.error("", e);
			error = ErrorMsgEnum.config_data_not_found.getId();
		} catch (Exception e) {
			log.error("", e);
			error = ErrorMsgEnum.unknown.getId();
		}
		client.sendProtocol(resp.build(), error);
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

					player.setRefreshDay(DateUtil.getDay(0));

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
}
