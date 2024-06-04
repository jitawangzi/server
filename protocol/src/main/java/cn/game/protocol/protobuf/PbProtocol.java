package cn.game.protocol.protobuf;
import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.google.protobuf.InvalidProtocolBufferException;

import cn.game.protocol.parser.ProtocolParser;

public class PbProtocol implements ProtocolParser {

	public static Map<Integer, Parser<?>> parsersMap = new HashMap<Integer, Parser<?>>();
	public static Map<String, Integer> nameIdMap = new HashMap<String, Integer>();
	
	private static PbProtocol instance = new PbProtocol();

	private PbProtocol() {
	};
	public static PbProtocol getInstance() {
		return instance;
	}

	public final static int ActivityListRequest_11000001 = 0x11000001;    //import "HeroMsg.proto" ;  
	public final static int ActivityListResponse_11000002 = 0x11000002;    
	public final static int ActivityStatePush_11100006 = 0x11100006;    
	public final static int ActivityFirstChargeRequest_11000007 = 0x11000007;    
	public final static int ActivityFirstChargeResponse_11000008 = 0x11000008;    //活动状态改变推送，看具体情况选择性推送  
	public final static int ActivityFirstChargeBuyRequest_11000010 = 0x11000010;    
	public final static int ActivityFirstChargeBuyResponse_11000011 = 0x11000011;    
	public final static int ActivityFirstChargeRewardRequest_11000012 = 0x11000012;    //首冲活动  
	public final static int ActivityFirstChargeRewardResponse_11000013 = 0x11000013;    
	public final static int ActivitySevenDaysCarnivalRequest_11000020 = 0x11000020;    //领取首冲礼包奖励  
	public final static int ActivitySevenDaysCarnivalResponse_11000021 = 0x11000021;    
	public final static int ActivitySevenDaysSigninInfoRequest_11000024 = 0x11000024;    
	public final static int ActivitySevenDaysSigninInfoResponse_11000025 = 0x11000025;    //七日狂欢  
	public final static int ActivitySevenDaysSigninRequest_11000026 = 0x11000026;    
	public final static int ActivitySevenDaysSigninResponse_11000027 = 0x11000027;    
	public final static int BattleFieldStartRequest_13000001 = 0x13000001;    
	public final static int BattleFieldStartResponse_13000002 = 0x13000002;    
	public final static int BattleFieldEndRequest_13000003 = 0x13000003;    //英雄属性数据  
	public final static int BattleFieldEndResponse_13000004 = 0x13000004;    
	public final static int BattleRougeRefreshRequest_13000005 = 0x13000005;    
	public final static int BattleRougeRefreshResponse_13000006 = 0x13000006;    
	public final static int BattleRewardRequest_13000022 = 0x13000022;    
	public final static int BattleRewardResponse_13000023 = 0x13000023;    
	public final static int BattleSweepRequest_13000024 = 0x13000024;    
	public final static int BattleSweepResponse_13000025 = 0x13000025;    
	public final static int BattlePatrolRewardRequest_13000044 = 0x13000044;    
	public final static int BattlePatrolRewardResponse_13000045 = 0x13000045;    
	public final static int BattleStaminaRequest_13000050 = 0x13000050;    
	public final static int BattleStaminaResponse_13000051 = 0x13000051;    
	public final static int BattleDaoHeartRequest_13000055 = 0x13000055;    
	public final static int BattleDaoHeartResponse_13000056 = 0x13000056;    
	public final static int BattleDaoHeartSweepRequest_13000060 = 0x13000060;    
	public final static int BattleDaoHeartSweepResponse_13000061 = 0x13000061;    
	public final static int BattleDaoHeartSweepBatchRequest_13000062 = 0x13000062;    
	public final static int BattleDaoHeartSweepBatchResponse_13000063 = 0x13000063;    
	public final static int BattleDaoHeartSweepRequest_13000064 = 0x13000064;    
	public final static int BattleDaoHeartSweepResponse_13000065 = 0x13000065;    
	public final static int BattleDaoHeartSweepRequest_13000066 = 0x13000066;    
	public final static int BattleDaoHeartSweepResponse_13000067 = 0x13000067;    
	public final static int BattleDayChallengeReceiveActivePointRequest_13000070 = 0x13000070;    
	public final static int BattleDayChallengeReceiveActivePointResponse_13000071 = 0x13000071;    
	public final static int DragonUnlockRequest_17000001 = 0x17000001;    
	public final static int DragonUnlockResponse_17000002 = 0x17000002;    //龙解锁  
	public final static int DragonStarUpRequest_17000003 = 0x17000003;    
	public final static int DragonStarUpResponse_17000004 = 0x17000004;    
	public final static int DragonSkillUpRequest_17000005 = 0x17000005;    
	public final static int DragonSkillUpResponse_17000006 = 0x17000006;    
	public final static int DrawListRequest_37000001 = 0x37000001;    
	public final static int DrawListResponse_37000002 = 0x37000002;    
	public final static int DrawRequest_37000003 = 0x37000003;    
	public final static int DrawResponse_37000004 = 0x37000004;    
	public final static int EquipmentWearRequest_09000001 = 0x09000001;    
	public final static int EquipmentWearResponse_09000002 = 0x09000002;    
	public final static int EquipmentTeardownRequest_09000003 = 0x09000003;    
	public final static int EquipmentTeardownResponse_09000004 = 0x09000004;    
	public final static int EquipmentPartStrengthRequest_09000007 = 0x09000007;    
	public final static int EquipmentPartStrengthResponse_09000008 = 0x09000008;    //装备部位强化  
	public final static int EquipmentPartBreakthroughRequest_09000011 = 0x09000011;    
	public final static int EquipmentPartBreakthroughResponse_09000012 = 0x09000012;    
	public final static int SwordStarUpRequest_09000013 = 0x09000013;    
	public final static int SwordStarUpResponse_09000014 = 0x09000014;    
	public final static int FashionStarUpRequest_09000015 = 0x09000015;    
	public final static int FashionStarUpResponse_09000016 = 0x09000016;    
	public final static int GemWearRequest_10000001 = 0x10000001;    
	public final static int GemWearResponse_10000002 = 0x10000002;    
	public final static int GemTeardownRequest_10000003 = 0x10000003;    
	public final static int GemTeardownResponse_10000004 = 0x10000004;    
	public final static int GemLockRequest_10000005 = 0x10000005;    
	public final static int GemLockResponse_10000006 = 0x10000006;    
	public final static int GemComposeRequest_10000007 = 0x10000007;    
	public final static int GemComposeResponse_10000008 = 0x10000008;    
	public final static int GmShutdownServerRequest_77000001 = 0x77000001;    
	public final static int GmShutdownServerResponse_77000002 = 0x77000002;    
	public final static int GmForbidAccountListRequest_77000003 = 0x77000003;    
	public final static int GmForbidAccountListResponse_77000004 = 0x77000004;    
	public final static int GmForbidAccountRequest_77000005 = 0x77000005;    
	public final static int GmForbidAccountResponse_77000006 = 0x77000006;    
	public final static int GmUnblockAccountRequest_77000007 = 0x77000007;    
	public final static int GmUnblockAccountResponse_77000008 = 0x77000008;    
	public final static int GmPlayerLogoutRequest_77000009 = 0x77000009;    
	public final static int GmPlayerLogouttResponse_7700000a = 0x7700000a;    //请求解封  
	public final static int HeroUpLevelRequest_16000001 = 0x16000001;    
	public final static int HeroUpLevelResponse_16000002 = 0x16000002;    
	public final static int HeroUpLevelMaxRequest_16000021 = 0x16000021;    
	public final static int HeroUpLevelMaxResponse_16000022 = 0x16000022;    //英雄一键升级,自动升级到最高级。  
	public final static int HeroUpLevelBatchRequest_16000023 = 0x16000023;    
	public final static int HeroUpLevelBatchResponse_16000024 = 0x16000024;    
	public final static int HeroConflateRequest_16000003 = 0x16000003;    
	public final static int HeroConflateResponse_16000004 = 0x16000004;    
	public final static int HeroBattleRequest_16000005 = 0x16000005;    //被消耗的英雄唯一id,如果使用了万能耗材，万能耗材不用填写这个值  
	public final static int HeroBattleResponse_16000006 = 0x16000006;    
	public final static int HeroLevelResetRequest_16000007 = 0x16000007;    
	public final static int HeroLevelResetResponse_16000008 = 0x16000008;    
	public final static int HeroQualityResetRequest_16000011 = 0x16000011;    
	public final static int HeroQualityResetResponse_16000012 = 0x16000012;    
	public final static int HeroFreeDayRentRequest_16000030 = 0x16000030;    
	public final static int HeroFreeDayRentResponse_16000031 = 0x16000031;    //英雄品质重置  
	public final static int HeroFreeDayRentChooseRequest_16000032 = 0x16000032;    
	public final static int HeroFreeDayRentChooseResponse_16000033 = 0x16000033;    
	public final static int ItemUseRequest_0b000003 = 0x0b000003;    
	public final static int ItemUseResponse_0b000004 = 0x0b000004;    
	public final static int MailListRequest_12000001 = 0x12000001;    
	public final static int MailListResponse_12000002 = 0x12000002;    
	public final static int MailSeeRequest_12000003 = 0x12000003;    
	public final static int MailSeeResponse_12000004 = 0x12000004;    //查看未读邮件  
	public final static int MailReceiveRequest_12000005 = 0x12000005;    
	public final static int MailReceiveResponse_12000006 = 0x12000006;    
	public final static int MailDeleteRequest_12000007 = 0x12000007;    
	public final static int MailDeleteResponse_12000008 = 0x12000008;    
	public final static int MergeEquipmentWearRequest_23000001 = 0x23000001;    //import "BaseMsg.proto" ;  
	public final static int MergeEquipmentWearResponse_23000002 = 0x23000002;    
	public final static int MergeEquipmentTeardownRequest_23000003 = 0x23000003;    
	public final static int MergeEquipmentTeardownResponse_23000004 = 0x23000004;    
	public final static int MergeEquipmentPartStrengthRequest_23000007 = 0x23000007;    
	public final static int MergeEquipmentPartStrengthResponse_23000008 = 0x23000008;    //装备升级  
	public final static int PlayerLoginRequest_01000001 = 0x01000001;    
	public final static int PlayerLoginResponse_01000002 = 0x01000002;    //string deviceId = 25;   设备唯一标识：MAC或者UUID或者IMEI，这里小游戏变为账号id ，即微信的openid  
	public final static int PlayerLogoutPush_01100030 = 0x01100030;    
	public final static int PlayerNameRequest_01000011 = 0x01000011;    
	public final static int PlayerNameResponse_01000012 = 0x01000012;    //退出登录(客户端登录多个账号，或者服务器关闭等)  
	public final static int PlayerHeadRequest_01000013 = 0x01000013;    
	public final static int PlayerHeadResponse_01000014 = 0x01000014;    
	public final static int PlayerHeadFrameRequest_01000015 = 0x01000015;    
	public final static int PlayerHeadFrameResponse_01000016 = 0x01000016;    
	public final static int PlayerGenderRequest_01000017 = 0x01000017;    
	public final static int PlayerGenderResponse_01000018 = 0x01000018;    
	public final static int PlayerReconnecRequest_01000065 = 0x01000065;    
	public final static int PlayerReconnecResponse_01000066 = 0x01000066;    
	public final static int PlayerHeartbeatRequest_01000005 = 0x01000005;    
	public final static int PlayerHeartbeatResponse_01000006 = 0x01000006;    
	public final static int PlayerBriefInfoRequest_01000007 = 0x01000007;    
	public final static int PlayerBriefInfoResponse_01000008 = 0x01000008;    
	public final static int PlayerBriefInfoOtherRequest_01000009 = 0x01000009;    
	public final static int PlayerBriefInfoOtherResponse_0100000a = 0x0100000a;    
	public final static int PlayerShowRequest_01000039 = 0x01000039;    
	public final static int PlayerShowResponse_0100003a = 0x0100003a;    
	public final static int PlayerAlchemyRequest_01000040 = 0x01000040;    
	public final static int PlayerAlchemyResponse_01000041 = 0x01000041;    
	public final static int PlayerResetPush_01100016 = 0x01100016;    //炼金请求  
	public final static int PlayerErrorPush_01000099 = 0x01000099;    
	public final static int ItemUseRequest_01000050 = 0x01000050;    
	public final static int ItemUseResponse_01000051 = 0x01000051;    
	public final static int PlayerCloudBoxPush_01100040 = 0x01100040;    
	public final static int PlayerCloudBoxRequest_01000042 = 0x01000042;    
	public final static int PlayerCloudBoxResponse_01000043 = 0x01000043;    
	public final static int PlayerExpLevelPush_01100050 = 0x01100050;    
	public final static int QuestListRequest_20000001 = 0x20000001;    
	public final static int QuestListResponse_20000002 = 0x20000002;    
	public final static int QuestReceiveRequest_20000004 = 0x20000004;    
	public final static int QuestReceiveResponse_20000005 = 0x20000005;    
	public final static int QuestReceiveActivePointRequest_20000008 = 0x20000008;    
	public final static int QuestReceiveActivePointResponse_20000009 = 0x20000009;    
	public final static int QuestGroupPush_20100008 = 0x20100008;    
	public final static int QuestPush_20200008 = 0x20200008;    
	public final static int QuestListAllRequest_20000051 = 0x20000051;    
	public final static int QuestListAllResponse_20000052 = 0x20000052;    //带积分奖励的任务，奖励领取情况  
	public final static int QuestChallengeGroupRequest_20000020 = 0x20000020;    
	public final static int QuestChallengeGroupResponse_20000021 = 0x20000021;    
	public final static int QuestChallengeGroupDetailRequest_20000022 = 0x20000022;    
	public final static int QuestChallengeGroupDetailResponse_20000023 = 0x20000023;    //查看某类型挑战组任务----暂时用不到  
	public final static int QuestConditionCompletePush_20500001 = 0x20500001;    
	public final static int QuestRewardPush_20600008 = 0x20600008;    
	public final static int QuestAcceptRequest_20000026 = 0x20000026;    
	public final static int QuestAcceptResponse_20000027 = 0x20000027;    //一个任务条件完成的时候，推送此协议 ----暂时用不到  
	public final static int QuestBranchPriorityRequest_20000028 = 0x20000028;    
	public final static int QuestBranchPriorityResponse_20000029 = 0x20000029;    
	public final static int QuestBranchPriorityPush_20300000 = 0x20300000;    
	public final static int QuestUpdateRequest_20000030 = 0x20000030;    //设置优先显示的支线组  ----暂时用不到  
	public final static int QuestUpdateResponse_20000031 = 0x20000031;    
	public final static int QuestChooseRewardRequest_20000033 = 0x20000033;    
	public final static int QuestChooseRewardResponse_20000034 = 0x20000034;    
	public final static int RewardPush_55000501 = 0x55000501;    
	public final static int SpendPush_55001501 = 0x55001501;    
	public final static int RewardShowPush_55002501 = 0x55002501;    
	public final static int GamePlayerOnlinePush_7d000010 = 0x7d000010;    
	public final static int GamePlayerPush_7d000011 = 0x7d000011;    
	public final static int GamePlayerPush_7d000100 = 0x7d000100;    
	public final static int GamePlayerLogoutRequest_7d000101 = 0x7d000101;    
	public final static int GamePlayerLogoutResponse_7d000102 = 0x7d000102;    
	public final static int GameTestRequest_7d000500 = 0x7d000500;    
	public final static int GameTestResponse_7d000501 = 0x7d000501;    
	public final static int ServerStatusRequest_7d000901 = 0x7d000901;    //测试的  
	public final static int ServerStatusResponse_7d000902 = 0x7d000902;    
	public final static int GamePlayerRequest_7d000015 = 0x7d000015;    
	public final static int GamePlayerResponse_7d000016 = 0x7d000016;    
	public final static int GameStatusPublish_7d000017 = 0x7d000017;    
	public final static int LoginPlayerUidRequest_7d000018 = 0x7d000018;    //向某个服务器的玩家发送消息让玩家处理，处理后返回  
	public final static int LoginPlayerUidResponse_7d000019 = 0x7d000019;    
	public final static int PaymentOrderCreateRequest_7d000020 = 0x7d000020;    
	public final static int PaymentOrderCreateResponse_7d000021 = 0x7d000021;    
	public final static int PaymentOrderShipRequest_7d000022 = 0x7d000022;    
	public final static int PaymentOrderShipResponse_7d000023 = 0x7d000023;    //请求创建支付订单  
	public final static int GameCrossForwardPush_7d000002 = 0x7d000002;    
	public final static int CrossGameForwardPush_7d000003 = 0x7d000003;    
	public final static int GameCrossPlayerBroadcast_7d000005 = 0x7d000005;    
	public final static int GameCrossBroadcast_7d000008 = 0x7d000008;    
	public final static int GameDataPush_7d00000a = 0x7d00000a;    
	public final static int GameDataPushBatch_7d00000b = 0x7d00000b;    
	public final static int GameDataPushBatch2_7d00000c = 0x7d00000c;    //Game向Cross发数据，将里面的消息广播给指定Game，或者所有Game  
	public final static int ShopItemListRequest_15000001 = 0x15000001;    
	public final static int ShopItemListResponse_15000002 = 0x15000002;    
	public final static int ShopItemBuyRequest_15000003 = 0x15000003;    
	public final static int ShopItemBuyResponse_15000004 = 0x15000004;    //购买商品  
	public final static int ShopHeishiRefreshRequest_15000005 = 0x15000005;    
	public final static int ShopHeishiRefreshResponse_15000006 = 0x15000006;    
	public final static int MonthCardBuyRequest_15000010 = 0x15000010;    
	public final static int MonthCardBuyResponse_15000011 = 0x15000011;    
	public final static int MonthCardBuyRewardRequest_15000012 = 0x15000012;    
	public final static int MonthCardBuyRewardResponse_15000013 = 0x15000013;    
	public final static int MonthCardDayRewardRequest_15000014 = 0x15000014;    
	public final static int MonthCardDayRewardResponse_15000015 = 0x15000015;    
	public final static int MonthCardDoubleBonusRequest_15000016 = 0x15000016;    
	public final static int MonthCardDoubleBonusResponse_15000017 = 0x15000017;    
	public final static int ShopChapterPacksBuyRequest_15000020 = 0x15000020;    
	public final static int ShopChapterPacksBuyResponse_15000021 = 0x15000021;    
	public final static int ShopRechargeRequest_15000022 = 0x15000022;    
	public final static int ShopRechargeResponse_15000023 = 0x15000023;    
	public final static int PaymentOrderPush_15010020 = 0x15010020;    
	public final static int ShopFundPassBuyRequest_15000030 = 0x15000030;    
	public final static int ShopFundPassBuyResponse_15000031 = 0x15000031;    
	public final static int ShopFundPassRewardRequest_15000032 = 0x15000032;    
	public final static int ShopFundPassRewardResponse_15000033 = 0x15000033;    
	public final static int ShopBoxOpenRequest_15000040 = 0x15000040;    //领取通行证奖励  
	public final static int ShopBoxOpenResponse_15000041 = 0x15000041;    
	public final static int StoryStartRequest_14000001 = 0x14000001;    
	public final static int StoryStartResponse_14000002 = 0x14000002;    
	public final static int StoryFinishRequest_14000003 = 0x14000003;    
	public final static int StoryFinishResponse_14000004 = 0x14000004;    //剧情结束  
	public final static int TestGmCmdRequest_6f000001 = 0x6f000001;    
	public final static int TestGmCmdResponse_6f000002 = 0x6f000002;    //gm指令  
	public final static int TestAddItemRequest_6f000008 = 0x6f000008;    //item 增加物品或者资源 ,参数1 为表id 参数2为数量  
	public final static int TestAddItemResponse_6f000009 = 0x6f000009;    
	public final static int TestMissionFinishRequest_6f000022 = 0x6f000022;    
	public final static int TestMissionFinishResponse_6f000023 = 0x6f000023;    
	public final static int TestRequest_6f000020 = 0x6f000020;    
	public final static int TestResponse_6f000021 = 0x6f000021;    
	public final static int TestMessageRequest_6f000080 = 0x6f000080;    //只是测试  
	public final static int TestMessageResponse_6f000081 = 0x6f000081;    	

	static {	
		parsersMap.put(ActivityListRequest_11000001, cn.game.protocol.protobuf.ActivityMsg.ActivityListRequest_11000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ActivityListResponse_11000002, cn.game.protocol.protobuf.ActivityMsg.ActivityListResponse_11000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ActivityStatePush_11100006, cn.game.protocol.protobuf.ActivityMsg.ActivityStatePush_11100006.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ActivityFirstChargeRequest_11000007, cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRequest_11000007.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ActivityFirstChargeResponse_11000008, cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeResponse_11000008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ActivityFirstChargeBuyRequest_11000010, cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeBuyRequest_11000010.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ActivityFirstChargeBuyResponse_11000011, cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeBuyResponse_11000011.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ActivityFirstChargeRewardRequest_11000012, cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardRequest_11000012.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ActivityFirstChargeRewardResponse_11000013, cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardResponse_11000013.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ActivitySevenDaysCarnivalRequest_11000020, cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysCarnivalRequest_11000020.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ActivitySevenDaysCarnivalResponse_11000021, cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysCarnivalResponse_11000021.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ActivitySevenDaysSigninInfoRequest_11000024, cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysSigninInfoRequest_11000024.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ActivitySevenDaysSigninInfoResponse_11000025, cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysSigninInfoResponse_11000025.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ActivitySevenDaysSigninRequest_11000026, cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysSigninRequest_11000026.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ActivitySevenDaysSigninResponse_11000027, cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysSigninResponse_11000027.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleFieldStartRequest_13000001, cn.game.protocol.protobuf.BattleMsg.BattleFieldStartRequest_13000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleFieldStartResponse_13000002, cn.game.protocol.protobuf.BattleMsg.BattleFieldStartResponse_13000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleFieldEndRequest_13000003, cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleFieldEndResponse_13000004, cn.game.protocol.protobuf.BattleMsg.BattleFieldEndResponse_13000004.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleRougeRefreshRequest_13000005, cn.game.protocol.protobuf.BattleMsg.BattleRougeRefreshRequest_13000005.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleRougeRefreshResponse_13000006, cn.game.protocol.protobuf.BattleMsg.BattleRougeRefreshResponse_13000006.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleRewardRequest_13000022, cn.game.protocol.protobuf.BattleMsg.BattleRewardRequest_13000022.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleRewardResponse_13000023, cn.game.protocol.protobuf.BattleMsg.BattleRewardResponse_13000023.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleSweepRequest_13000024, cn.game.protocol.protobuf.BattleMsg.BattleSweepRequest_13000024.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleSweepResponse_13000025, cn.game.protocol.protobuf.BattleMsg.BattleSweepResponse_13000025.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattlePatrolRewardRequest_13000044, cn.game.protocol.protobuf.BattleMsg.BattlePatrolRewardRequest_13000044.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattlePatrolRewardResponse_13000045, cn.game.protocol.protobuf.BattleMsg.BattlePatrolRewardResponse_13000045.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleStaminaRequest_13000050, cn.game.protocol.protobuf.BattleMsg.BattleStaminaRequest_13000050.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleStaminaResponse_13000051, cn.game.protocol.protobuf.BattleMsg.BattleStaminaResponse_13000051.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleDaoHeartRequest_13000055, cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartRequest_13000055.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleDaoHeartResponse_13000056, cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartResponse_13000056.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleDaoHeartSweepRequest_13000060, cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepRequest_13000060.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleDaoHeartSweepResponse_13000061, cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepResponse_13000061.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleDaoHeartSweepBatchRequest_13000062, cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepBatchRequest_13000062.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleDaoHeartSweepBatchResponse_13000063, cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepBatchResponse_13000063.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleDaoHeartSweepRequest_13000064, cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepRequest_13000064.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleDaoHeartSweepResponse_13000065, cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepResponse_13000065.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleDaoHeartSweepRequest_13000066, cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepRequest_13000066.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleDaoHeartSweepResponse_13000067, cn.game.protocol.protobuf.BattleMsg.BattleDaoHeartSweepResponse_13000067.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleDayChallengeReceiveActivePointRequest_13000070, cn.game.protocol.protobuf.BattleMsg.BattleDayChallengeReceiveActivePointRequest_13000070.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleDayChallengeReceiveActivePointResponse_13000071, cn.game.protocol.protobuf.BattleMsg.BattleDayChallengeReceiveActivePointResponse_13000071.getDefaultInstance()
				.getParserForType());
		parsersMap.put(DragonUnlockRequest_17000001, cn.game.protocol.protobuf.DragonMsg.DragonUnlockRequest_17000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(DragonUnlockResponse_17000002, cn.game.protocol.protobuf.DragonMsg.DragonUnlockResponse_17000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(DragonStarUpRequest_17000003, cn.game.protocol.protobuf.DragonMsg.DragonStarUpRequest_17000003.getDefaultInstance()
				.getParserForType());
		parsersMap.put(DragonStarUpResponse_17000004, cn.game.protocol.protobuf.DragonMsg.DragonStarUpResponse_17000004.getDefaultInstance()
				.getParserForType());
		parsersMap.put(DragonSkillUpRequest_17000005, cn.game.protocol.protobuf.DragonMsg.DragonSkillUpRequest_17000005.getDefaultInstance()
				.getParserForType());
		parsersMap.put(DragonSkillUpResponse_17000006, cn.game.protocol.protobuf.DragonMsg.DragonSkillUpResponse_17000006.getDefaultInstance()
				.getParserForType());
		parsersMap.put(DrawListRequest_37000001, cn.game.protocol.protobuf.DrawMsg.DrawListRequest_37000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(DrawListResponse_37000002, cn.game.protocol.protobuf.DrawMsg.DrawListResponse_37000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(DrawRequest_37000003, cn.game.protocol.protobuf.DrawMsg.DrawRequest_37000003.getDefaultInstance()
				.getParserForType());
		parsersMap.put(DrawResponse_37000004, cn.game.protocol.protobuf.DrawMsg.DrawResponse_37000004.getDefaultInstance()
				.getParserForType());
		parsersMap.put(EquipmentWearRequest_09000001, cn.game.protocol.protobuf.EquipMsg.EquipmentWearRequest_09000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(EquipmentWearResponse_09000002, cn.game.protocol.protobuf.EquipMsg.EquipmentWearResponse_09000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(EquipmentTeardownRequest_09000003, cn.game.protocol.protobuf.EquipMsg.EquipmentTeardownRequest_09000003.getDefaultInstance()
				.getParserForType());
		parsersMap.put(EquipmentTeardownResponse_09000004, cn.game.protocol.protobuf.EquipMsg.EquipmentTeardownResponse_09000004.getDefaultInstance()
				.getParserForType());
		parsersMap.put(EquipmentPartStrengthRequest_09000007, cn.game.protocol.protobuf.EquipMsg.EquipmentPartStrengthRequest_09000007.getDefaultInstance()
				.getParserForType());
		parsersMap.put(EquipmentPartStrengthResponse_09000008, cn.game.protocol.protobuf.EquipMsg.EquipmentPartStrengthResponse_09000008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(EquipmentPartBreakthroughRequest_09000011, cn.game.protocol.protobuf.EquipMsg.EquipmentPartBreakthroughRequest_09000011.getDefaultInstance()
				.getParserForType());
		parsersMap.put(EquipmentPartBreakthroughResponse_09000012, cn.game.protocol.protobuf.EquipMsg.EquipmentPartBreakthroughResponse_09000012.getDefaultInstance()
				.getParserForType());
		parsersMap.put(SwordStarUpRequest_09000013, cn.game.protocol.protobuf.EquipMsg.SwordStarUpRequest_09000013.getDefaultInstance()
				.getParserForType());
		parsersMap.put(SwordStarUpResponse_09000014, cn.game.protocol.protobuf.EquipMsg.SwordStarUpResponse_09000014.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FashionStarUpRequest_09000015, cn.game.protocol.protobuf.EquipMsg.FashionStarUpRequest_09000015.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FashionStarUpResponse_09000016, cn.game.protocol.protobuf.EquipMsg.FashionStarUpResponse_09000016.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GemWearRequest_10000001, cn.game.protocol.protobuf.GemMsg.GemWearRequest_10000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GemWearResponse_10000002, cn.game.protocol.protobuf.GemMsg.GemWearResponse_10000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GemTeardownRequest_10000003, cn.game.protocol.protobuf.GemMsg.GemTeardownRequest_10000003.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GemTeardownResponse_10000004, cn.game.protocol.protobuf.GemMsg.GemTeardownResponse_10000004.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GemLockRequest_10000005, cn.game.protocol.protobuf.GemMsg.GemLockRequest_10000005.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GemLockResponse_10000006, cn.game.protocol.protobuf.GemMsg.GemLockResponse_10000006.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GemComposeRequest_10000007, cn.game.protocol.protobuf.GemMsg.GemComposeRequest_10000007.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GemComposeResponse_10000008, cn.game.protocol.protobuf.GemMsg.GemComposeResponse_10000008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmShutdownServerRequest_77000001, cn.game.protocol.protobuf.GmMsg.GmShutdownServerRequest_77000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmShutdownServerResponse_77000002, cn.game.protocol.protobuf.GmMsg.GmShutdownServerResponse_77000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmForbidAccountListRequest_77000003, cn.game.protocol.protobuf.GmMsg.GmForbidAccountListRequest_77000003.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmForbidAccountListResponse_77000004, cn.game.protocol.protobuf.GmMsg.GmForbidAccountListResponse_77000004.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmForbidAccountRequest_77000005, cn.game.protocol.protobuf.GmMsg.GmForbidAccountRequest_77000005.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmForbidAccountResponse_77000006, cn.game.protocol.protobuf.GmMsg.GmForbidAccountResponse_77000006.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmUnblockAccountRequest_77000007, cn.game.protocol.protobuf.GmMsg.GmUnblockAccountRequest_77000007.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmUnblockAccountResponse_77000008, cn.game.protocol.protobuf.GmMsg.GmUnblockAccountResponse_77000008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmPlayerLogoutRequest_77000009, cn.game.protocol.protobuf.GmMsg.GmPlayerLogoutRequest_77000009.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmPlayerLogouttResponse_7700000a, cn.game.protocol.protobuf.GmMsg.GmPlayerLogouttResponse_7700000a.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HeroUpLevelRequest_16000001, cn.game.protocol.protobuf.HeroMsg.HeroUpLevelRequest_16000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HeroUpLevelResponse_16000002, cn.game.protocol.protobuf.HeroMsg.HeroUpLevelResponse_16000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HeroUpLevelMaxRequest_16000021, cn.game.protocol.protobuf.HeroMsg.HeroUpLevelMaxRequest_16000021.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HeroUpLevelMaxResponse_16000022, cn.game.protocol.protobuf.HeroMsg.HeroUpLevelMaxResponse_16000022.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HeroUpLevelBatchRequest_16000023, cn.game.protocol.protobuf.HeroMsg.HeroUpLevelBatchRequest_16000023.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HeroUpLevelBatchResponse_16000024, cn.game.protocol.protobuf.HeroMsg.HeroUpLevelBatchResponse_16000024.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HeroConflateRequest_16000003, cn.game.protocol.protobuf.HeroMsg.HeroConflateRequest_16000003.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HeroConflateResponse_16000004, cn.game.protocol.protobuf.HeroMsg.HeroConflateResponse_16000004.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HeroBattleRequest_16000005, cn.game.protocol.protobuf.HeroMsg.HeroBattleRequest_16000005.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HeroBattleResponse_16000006, cn.game.protocol.protobuf.HeroMsg.HeroBattleResponse_16000006.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HeroLevelResetRequest_16000007, cn.game.protocol.protobuf.HeroMsg.HeroLevelResetRequest_16000007.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HeroLevelResetResponse_16000008, cn.game.protocol.protobuf.HeroMsg.HeroLevelResetResponse_16000008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HeroQualityResetRequest_16000011, cn.game.protocol.protobuf.HeroMsg.HeroQualityResetRequest_16000011.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HeroQualityResetResponse_16000012, cn.game.protocol.protobuf.HeroMsg.HeroQualityResetResponse_16000012.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HeroFreeDayRentRequest_16000030, cn.game.protocol.protobuf.HeroMsg.HeroFreeDayRentRequest_16000030.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HeroFreeDayRentResponse_16000031, cn.game.protocol.protobuf.HeroMsg.HeroFreeDayRentResponse_16000031.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HeroFreeDayRentChooseRequest_16000032, cn.game.protocol.protobuf.HeroMsg.HeroFreeDayRentChooseRequest_16000032.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HeroFreeDayRentChooseResponse_16000033, cn.game.protocol.protobuf.HeroMsg.HeroFreeDayRentChooseResponse_16000033.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ItemUseRequest_0b000003, cn.game.protocol.protobuf.ItemMsg.ItemUseRequest_0b000003.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ItemUseResponse_0b000004, cn.game.protocol.protobuf.ItemMsg.ItemUseResponse_0b000004.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MailListRequest_12000001, cn.game.protocol.protobuf.MailMsg.MailListRequest_12000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MailListResponse_12000002, cn.game.protocol.protobuf.MailMsg.MailListResponse_12000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MailSeeRequest_12000003, cn.game.protocol.protobuf.MailMsg.MailSeeRequest_12000003.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MailSeeResponse_12000004, cn.game.protocol.protobuf.MailMsg.MailSeeResponse_12000004.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MailReceiveRequest_12000005, cn.game.protocol.protobuf.MailMsg.MailReceiveRequest_12000005.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MailReceiveResponse_12000006, cn.game.protocol.protobuf.MailMsg.MailReceiveResponse_12000006.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MailDeleteRequest_12000007, cn.game.protocol.protobuf.MailMsg.MailDeleteRequest_12000007.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MailDeleteResponse_12000008, cn.game.protocol.protobuf.MailMsg.MailDeleteResponse_12000008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MergeEquipmentWearRequest_23000001, cn.game.protocol.protobuf.MergeEquipMsg.MergeEquipmentWearRequest_23000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MergeEquipmentWearResponse_23000002, cn.game.protocol.protobuf.MergeEquipMsg.MergeEquipmentWearResponse_23000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MergeEquipmentTeardownRequest_23000003, cn.game.protocol.protobuf.MergeEquipMsg.MergeEquipmentTeardownRequest_23000003.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MergeEquipmentTeardownResponse_23000004, cn.game.protocol.protobuf.MergeEquipMsg.MergeEquipmentTeardownResponse_23000004.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MergeEquipmentPartStrengthRequest_23000007, cn.game.protocol.protobuf.MergeEquipMsg.MergeEquipmentPartStrengthRequest_23000007.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MergeEquipmentPartStrengthResponse_23000008, cn.game.protocol.protobuf.MergeEquipMsg.MergeEquipmentPartStrengthResponse_23000008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerLoginRequest_01000001, cn.game.protocol.protobuf.PlayerMsg.PlayerLoginRequest_01000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerLoginResponse_01000002, cn.game.protocol.protobuf.PlayerMsg.PlayerLoginResponse_01000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerLogoutPush_01100030, cn.game.protocol.protobuf.PlayerMsg.PlayerLogoutPush_01100030.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerNameRequest_01000011, cn.game.protocol.protobuf.PlayerMsg.PlayerNameRequest_01000011.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerNameResponse_01000012, cn.game.protocol.protobuf.PlayerMsg.PlayerNameResponse_01000012.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerHeadRequest_01000013, cn.game.protocol.protobuf.PlayerMsg.PlayerHeadRequest_01000013.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerHeadResponse_01000014, cn.game.protocol.protobuf.PlayerMsg.PlayerHeadResponse_01000014.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerHeadFrameRequest_01000015, cn.game.protocol.protobuf.PlayerMsg.PlayerHeadFrameRequest_01000015.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerHeadFrameResponse_01000016, cn.game.protocol.protobuf.PlayerMsg.PlayerHeadFrameResponse_01000016.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerGenderRequest_01000017, cn.game.protocol.protobuf.PlayerMsg.PlayerGenderRequest_01000017.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerGenderResponse_01000018, cn.game.protocol.protobuf.PlayerMsg.PlayerGenderResponse_01000018.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerReconnecRequest_01000065, cn.game.protocol.protobuf.PlayerMsg.PlayerReconnecRequest_01000065.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerReconnecResponse_01000066, cn.game.protocol.protobuf.PlayerMsg.PlayerReconnecResponse_01000066.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerHeartbeatRequest_01000005, cn.game.protocol.protobuf.PlayerMsg.PlayerHeartbeatRequest_01000005.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerHeartbeatResponse_01000006, cn.game.protocol.protobuf.PlayerMsg.PlayerHeartbeatResponse_01000006.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerBriefInfoRequest_01000007, cn.game.protocol.protobuf.PlayerMsg.PlayerBriefInfoRequest_01000007.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerBriefInfoResponse_01000008, cn.game.protocol.protobuf.PlayerMsg.PlayerBriefInfoResponse_01000008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerBriefInfoOtherRequest_01000009, cn.game.protocol.protobuf.PlayerMsg.PlayerBriefInfoOtherRequest_01000009.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerBriefInfoOtherResponse_0100000a, cn.game.protocol.protobuf.PlayerMsg.PlayerBriefInfoOtherResponse_0100000a.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerShowRequest_01000039, cn.game.protocol.protobuf.PlayerMsg.PlayerShowRequest_01000039.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerShowResponse_0100003a, cn.game.protocol.protobuf.PlayerMsg.PlayerShowResponse_0100003a.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerAlchemyRequest_01000040, cn.game.protocol.protobuf.PlayerMsg.PlayerAlchemyRequest_01000040.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerAlchemyResponse_01000041, cn.game.protocol.protobuf.PlayerMsg.PlayerAlchemyResponse_01000041.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerResetPush_01100016, cn.game.protocol.protobuf.PlayerMsg.PlayerResetPush_01100016.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerErrorPush_01000099, cn.game.protocol.protobuf.PlayerMsg.PlayerErrorPush_01000099.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ItemUseRequest_01000050, cn.game.protocol.protobuf.PlayerMsg.ItemUseRequest_01000050.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ItemUseResponse_01000051, cn.game.protocol.protobuf.PlayerMsg.ItemUseResponse_01000051.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerCloudBoxPush_01100040, cn.game.protocol.protobuf.PlayerMsg.PlayerCloudBoxPush_01100040.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerCloudBoxRequest_01000042, cn.game.protocol.protobuf.PlayerMsg.PlayerCloudBoxRequest_01000042.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerCloudBoxResponse_01000043, cn.game.protocol.protobuf.PlayerMsg.PlayerCloudBoxResponse_01000043.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerExpLevelPush_01100050, cn.game.protocol.protobuf.PlayerMsg.PlayerExpLevelPush_01100050.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestListRequest_20000001, cn.game.protocol.protobuf.QuestMsg.QuestListRequest_20000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestListResponse_20000002, cn.game.protocol.protobuf.QuestMsg.QuestListResponse_20000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestReceiveRequest_20000004, cn.game.protocol.protobuf.QuestMsg.QuestReceiveRequest_20000004.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestReceiveResponse_20000005, cn.game.protocol.protobuf.QuestMsg.QuestReceiveResponse_20000005.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestReceiveActivePointRequest_20000008, cn.game.protocol.protobuf.QuestMsg.QuestReceiveActivePointRequest_20000008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestReceiveActivePointResponse_20000009, cn.game.protocol.protobuf.QuestMsg.QuestReceiveActivePointResponse_20000009.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestGroupPush_20100008, cn.game.protocol.protobuf.QuestMsg.QuestGroupPush_20100008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestPush_20200008, cn.game.protocol.protobuf.QuestMsg.QuestPush_20200008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestListAllRequest_20000051, cn.game.protocol.protobuf.QuestMsg.QuestListAllRequest_20000051.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestListAllResponse_20000052, cn.game.protocol.protobuf.QuestMsg.QuestListAllResponse_20000052.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestChallengeGroupRequest_20000020, cn.game.protocol.protobuf.QuestMsg.QuestChallengeGroupRequest_20000020.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestChallengeGroupResponse_20000021, cn.game.protocol.protobuf.QuestMsg.QuestChallengeGroupResponse_20000021.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestChallengeGroupDetailRequest_20000022, cn.game.protocol.protobuf.QuestMsg.QuestChallengeGroupDetailRequest_20000022.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestChallengeGroupDetailResponse_20000023, cn.game.protocol.protobuf.QuestMsg.QuestChallengeGroupDetailResponse_20000023.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestConditionCompletePush_20500001, cn.game.protocol.protobuf.QuestMsg.QuestConditionCompletePush_20500001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestRewardPush_20600008, cn.game.protocol.protobuf.QuestMsg.QuestRewardPush_20600008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestAcceptRequest_20000026, cn.game.protocol.protobuf.QuestMsg.QuestAcceptRequest_20000026.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestAcceptResponse_20000027, cn.game.protocol.protobuf.QuestMsg.QuestAcceptResponse_20000027.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestBranchPriorityRequest_20000028, cn.game.protocol.protobuf.QuestMsg.QuestBranchPriorityRequest_20000028.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestBranchPriorityResponse_20000029, cn.game.protocol.protobuf.QuestMsg.QuestBranchPriorityResponse_20000029.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestBranchPriorityPush_20300000, cn.game.protocol.protobuf.QuestMsg.QuestBranchPriorityPush_20300000.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestUpdateRequest_20000030, cn.game.protocol.protobuf.QuestMsg.QuestUpdateRequest_20000030.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestUpdateResponse_20000031, cn.game.protocol.protobuf.QuestMsg.QuestUpdateResponse_20000031.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestChooseRewardRequest_20000033, cn.game.protocol.protobuf.QuestMsg.QuestChooseRewardRequest_20000033.getDefaultInstance()
				.getParserForType());
		parsersMap.put(QuestChooseRewardResponse_20000034, cn.game.protocol.protobuf.QuestMsg.QuestChooseRewardResponse_20000034.getDefaultInstance()
				.getParserForType());
		parsersMap.put(RewardPush_55000501, cn.game.protocol.protobuf.RewardMsg.RewardPush_55000501.getDefaultInstance()
				.getParserForType());
		parsersMap.put(SpendPush_55001501, cn.game.protocol.protobuf.RewardMsg.SpendPush_55001501.getDefaultInstance()
				.getParserForType());
		parsersMap.put(RewardShowPush_55002501, cn.game.protocol.protobuf.RewardMsg.RewardShowPush_55002501.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GamePlayerOnlinePush_7d000010, cn.game.protocol.protobuf.ServerMsg.GamePlayerOnlinePush_7d000010.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GamePlayerPush_7d000011, cn.game.protocol.protobuf.ServerMsg.GamePlayerPush_7d000011.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GamePlayerPush_7d000100, cn.game.protocol.protobuf.ServerMsg.GamePlayerPush_7d000100.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GamePlayerLogoutRequest_7d000101, cn.game.protocol.protobuf.ServerMsg.GamePlayerLogoutRequest_7d000101.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GamePlayerLogoutResponse_7d000102, cn.game.protocol.protobuf.ServerMsg.GamePlayerLogoutResponse_7d000102.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GameTestRequest_7d000500, cn.game.protocol.protobuf.ServerMsg.GameTestRequest_7d000500.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GameTestResponse_7d000501, cn.game.protocol.protobuf.ServerMsg.GameTestResponse_7d000501.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ServerStatusRequest_7d000901, cn.game.protocol.protobuf.ServerMsg.ServerStatusRequest_7d000901.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ServerStatusResponse_7d000902, cn.game.protocol.protobuf.ServerMsg.ServerStatusResponse_7d000902.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GamePlayerRequest_7d000015, cn.game.protocol.protobuf.ServerMsg.GamePlayerRequest_7d000015.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GamePlayerResponse_7d000016, cn.game.protocol.protobuf.ServerMsg.GamePlayerResponse_7d000016.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GameStatusPublish_7d000017, cn.game.protocol.protobuf.ServerMsg.GameStatusPublish_7d000017.getDefaultInstance()
				.getParserForType());
		parsersMap.put(LoginPlayerUidRequest_7d000018, cn.game.protocol.protobuf.ServerMsg.LoginPlayerUidRequest_7d000018.getDefaultInstance()
				.getParserForType());
		parsersMap.put(LoginPlayerUidResponse_7d000019, cn.game.protocol.protobuf.ServerMsg.LoginPlayerUidResponse_7d000019.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PaymentOrderCreateRequest_7d000020, cn.game.protocol.protobuf.ServerMsg.PaymentOrderCreateRequest_7d000020.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PaymentOrderCreateResponse_7d000021, cn.game.protocol.protobuf.ServerMsg.PaymentOrderCreateResponse_7d000021.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PaymentOrderShipRequest_7d000022, cn.game.protocol.protobuf.ServerMsg.PaymentOrderShipRequest_7d000022.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PaymentOrderShipResponse_7d000023, cn.game.protocol.protobuf.ServerMsg.PaymentOrderShipResponse_7d000023.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GameCrossForwardPush_7d000002, cn.game.protocol.protobuf.ServerMsg.GameCrossForwardPush_7d000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(CrossGameForwardPush_7d000003, cn.game.protocol.protobuf.ServerMsg.CrossGameForwardPush_7d000003.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GameCrossPlayerBroadcast_7d000005, cn.game.protocol.protobuf.ServerMsg.GameCrossPlayerBroadcast_7d000005.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GameCrossBroadcast_7d000008, cn.game.protocol.protobuf.ServerMsg.GameCrossBroadcast_7d000008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GameDataPush_7d00000a, cn.game.protocol.protobuf.ServerMsg.GameDataPush_7d00000a.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GameDataPushBatch_7d00000b, cn.game.protocol.protobuf.ServerMsg.GameDataPushBatch_7d00000b.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GameDataPushBatch2_7d00000c, cn.game.protocol.protobuf.ServerMsg.GameDataPushBatch2_7d00000c.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ShopItemListRequest_15000001, cn.game.protocol.protobuf.ShopMsg.ShopItemListRequest_15000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ShopItemListResponse_15000002, cn.game.protocol.protobuf.ShopMsg.ShopItemListResponse_15000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ShopItemBuyRequest_15000003, cn.game.protocol.protobuf.ShopMsg.ShopItemBuyRequest_15000003.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ShopItemBuyResponse_15000004, cn.game.protocol.protobuf.ShopMsg.ShopItemBuyResponse_15000004.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ShopHeishiRefreshRequest_15000005, cn.game.protocol.protobuf.ShopMsg.ShopHeishiRefreshRequest_15000005.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ShopHeishiRefreshResponse_15000006, cn.game.protocol.protobuf.ShopMsg.ShopHeishiRefreshResponse_15000006.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MonthCardBuyRequest_15000010, cn.game.protocol.protobuf.ShopMsg.MonthCardBuyRequest_15000010.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MonthCardBuyResponse_15000011, cn.game.protocol.protobuf.ShopMsg.MonthCardBuyResponse_15000011.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MonthCardBuyRewardRequest_15000012, cn.game.protocol.protobuf.ShopMsg.MonthCardBuyRewardRequest_15000012.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MonthCardBuyRewardResponse_15000013, cn.game.protocol.protobuf.ShopMsg.MonthCardBuyRewardResponse_15000013.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MonthCardDayRewardRequest_15000014, cn.game.protocol.protobuf.ShopMsg.MonthCardDayRewardRequest_15000014.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MonthCardDayRewardResponse_15000015, cn.game.protocol.protobuf.ShopMsg.MonthCardDayRewardResponse_15000015.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MonthCardDoubleBonusRequest_15000016, cn.game.protocol.protobuf.ShopMsg.MonthCardDoubleBonusRequest_15000016.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MonthCardDoubleBonusResponse_15000017, cn.game.protocol.protobuf.ShopMsg.MonthCardDoubleBonusResponse_15000017.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ShopChapterPacksBuyRequest_15000020, cn.game.protocol.protobuf.ShopMsg.ShopChapterPacksBuyRequest_15000020.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ShopChapterPacksBuyResponse_15000021, cn.game.protocol.protobuf.ShopMsg.ShopChapterPacksBuyResponse_15000021.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ShopRechargeRequest_15000022, cn.game.protocol.protobuf.ShopMsg.ShopRechargeRequest_15000022.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ShopRechargeResponse_15000023, cn.game.protocol.protobuf.ShopMsg.ShopRechargeResponse_15000023.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PaymentOrderPush_15010020, cn.game.protocol.protobuf.ShopMsg.PaymentOrderPush_15010020.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ShopFundPassBuyRequest_15000030, cn.game.protocol.protobuf.ShopMsg.ShopFundPassBuyRequest_15000030.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ShopFundPassBuyResponse_15000031, cn.game.protocol.protobuf.ShopMsg.ShopFundPassBuyResponse_15000031.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ShopFundPassRewardRequest_15000032, cn.game.protocol.protobuf.ShopMsg.ShopFundPassRewardRequest_15000032.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ShopFundPassRewardResponse_15000033, cn.game.protocol.protobuf.ShopMsg.ShopFundPassRewardResponse_15000033.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ShopBoxOpenRequest_15000040, cn.game.protocol.protobuf.ShopMsg.ShopBoxOpenRequest_15000040.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ShopBoxOpenResponse_15000041, cn.game.protocol.protobuf.ShopMsg.ShopBoxOpenResponse_15000041.getDefaultInstance()
				.getParserForType());
		parsersMap.put(StoryStartRequest_14000001, cn.game.protocol.protobuf.StoryMsg.StoryStartRequest_14000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(StoryStartResponse_14000002, cn.game.protocol.protobuf.StoryMsg.StoryStartResponse_14000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(StoryFinishRequest_14000003, cn.game.protocol.protobuf.StoryMsg.StoryFinishRequest_14000003.getDefaultInstance()
				.getParserForType());
		parsersMap.put(StoryFinishResponse_14000004, cn.game.protocol.protobuf.StoryMsg.StoryFinishResponse_14000004.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestGmCmdRequest_6f000001, cn.game.protocol.protobuf.TestMsg.TestGmCmdRequest_6f000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestGmCmdResponse_6f000002, cn.game.protocol.protobuf.TestMsg.TestGmCmdResponse_6f000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestAddItemRequest_6f000008, cn.game.protocol.protobuf.TestMsg.TestAddItemRequest_6f000008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestAddItemResponse_6f000009, cn.game.protocol.protobuf.TestMsg.TestAddItemResponse_6f000009.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestMissionFinishRequest_6f000022, cn.game.protocol.protobuf.TestMsg.TestMissionFinishRequest_6f000022.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestMissionFinishResponse_6f000023, cn.game.protocol.protobuf.TestMsg.TestMissionFinishResponse_6f000023.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestRequest_6f000020, cn.game.protocol.protobuf.TestMsg.TestRequest_6f000020.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestResponse_6f000021, cn.game.protocol.protobuf.TestMsg.TestResponse_6f000021.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestMessageRequest_6f000080, cn.game.protocol.protobuf.TestMsg.TestMessageRequest_6f000080.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestMessageResponse_6f000081, cn.game.protocol.protobuf.TestMsg.TestMessageResponse_6f000081.getDefaultInstance()
				.getParserForType());

		nameIdMap.put("ActivityListRequest_11000001", 0x11000001);
		nameIdMap.put("ActivityListResponse_11000002", 0x11000002);
		nameIdMap.put("ActivityStatePush_11100006", 0x11100006);
		nameIdMap.put("ActivityFirstChargeRequest_11000007", 0x11000007);
		nameIdMap.put("ActivityFirstChargeResponse_11000008", 0x11000008);
		nameIdMap.put("ActivityFirstChargeBuyRequest_11000010", 0x11000010);
		nameIdMap.put("ActivityFirstChargeBuyResponse_11000011", 0x11000011);
		nameIdMap.put("ActivityFirstChargeRewardRequest_11000012", 0x11000012);
		nameIdMap.put("ActivityFirstChargeRewardResponse_11000013", 0x11000013);
		nameIdMap.put("ActivitySevenDaysCarnivalRequest_11000020", 0x11000020);
		nameIdMap.put("ActivitySevenDaysCarnivalResponse_11000021", 0x11000021);
		nameIdMap.put("ActivitySevenDaysSigninInfoRequest_11000024", 0x11000024);
		nameIdMap.put("ActivitySevenDaysSigninInfoResponse_11000025", 0x11000025);
		nameIdMap.put("ActivitySevenDaysSigninRequest_11000026", 0x11000026);
		nameIdMap.put("ActivitySevenDaysSigninResponse_11000027", 0x11000027);
		nameIdMap.put("BattleFieldStartRequest_13000001", 0x13000001);
		nameIdMap.put("BattleFieldStartResponse_13000002", 0x13000002);
		nameIdMap.put("BattleFieldEndRequest_13000003", 0x13000003);
		nameIdMap.put("BattleFieldEndResponse_13000004", 0x13000004);
		nameIdMap.put("BattleRougeRefreshRequest_13000005", 0x13000005);
		nameIdMap.put("BattleRougeRefreshResponse_13000006", 0x13000006);
		nameIdMap.put("BattleRewardRequest_13000022", 0x13000022);
		nameIdMap.put("BattleRewardResponse_13000023", 0x13000023);
		nameIdMap.put("BattleSweepRequest_13000024", 0x13000024);
		nameIdMap.put("BattleSweepResponse_13000025", 0x13000025);
		nameIdMap.put("BattlePatrolRewardRequest_13000044", 0x13000044);
		nameIdMap.put("BattlePatrolRewardResponse_13000045", 0x13000045);
		nameIdMap.put("BattleStaminaRequest_13000050", 0x13000050);
		nameIdMap.put("BattleStaminaResponse_13000051", 0x13000051);
		nameIdMap.put("BattleDaoHeartRequest_13000055", 0x13000055);
		nameIdMap.put("BattleDaoHeartResponse_13000056", 0x13000056);
		nameIdMap.put("BattleDaoHeartSweepRequest_13000060", 0x13000060);
		nameIdMap.put("BattleDaoHeartSweepResponse_13000061", 0x13000061);
		nameIdMap.put("BattleDaoHeartSweepBatchRequest_13000062", 0x13000062);
		nameIdMap.put("BattleDaoHeartSweepBatchResponse_13000063", 0x13000063);
		nameIdMap.put("BattleDaoHeartSweepRequest_13000064", 0x13000064);
		nameIdMap.put("BattleDaoHeartSweepResponse_13000065", 0x13000065);
		nameIdMap.put("BattleDaoHeartSweepRequest_13000066", 0x13000066);
		nameIdMap.put("BattleDaoHeartSweepResponse_13000067", 0x13000067);
		nameIdMap.put("BattleDayChallengeReceiveActivePointRequest_13000070", 0x13000070);
		nameIdMap.put("BattleDayChallengeReceiveActivePointResponse_13000071", 0x13000071);
		nameIdMap.put("DragonUnlockRequest_17000001", 0x17000001);
		nameIdMap.put("DragonUnlockResponse_17000002", 0x17000002);
		nameIdMap.put("DragonStarUpRequest_17000003", 0x17000003);
		nameIdMap.put("DragonStarUpResponse_17000004", 0x17000004);
		nameIdMap.put("DragonSkillUpRequest_17000005", 0x17000005);
		nameIdMap.put("DragonSkillUpResponse_17000006", 0x17000006);
		nameIdMap.put("DrawListRequest_37000001", 0x37000001);
		nameIdMap.put("DrawListResponse_37000002", 0x37000002);
		nameIdMap.put("DrawRequest_37000003", 0x37000003);
		nameIdMap.put("DrawResponse_37000004", 0x37000004);
		nameIdMap.put("EquipmentWearRequest_09000001", 0x09000001);
		nameIdMap.put("EquipmentWearResponse_09000002", 0x09000002);
		nameIdMap.put("EquipmentTeardownRequest_09000003", 0x09000003);
		nameIdMap.put("EquipmentTeardownResponse_09000004", 0x09000004);
		nameIdMap.put("EquipmentPartStrengthRequest_09000007", 0x09000007);
		nameIdMap.put("EquipmentPartStrengthResponse_09000008", 0x09000008);
		nameIdMap.put("EquipmentPartBreakthroughRequest_09000011", 0x09000011);
		nameIdMap.put("EquipmentPartBreakthroughResponse_09000012", 0x09000012);
		nameIdMap.put("SwordStarUpRequest_09000013", 0x09000013);
		nameIdMap.put("SwordStarUpResponse_09000014", 0x09000014);
		nameIdMap.put("FashionStarUpRequest_09000015", 0x09000015);
		nameIdMap.put("FashionStarUpResponse_09000016", 0x09000016);
		nameIdMap.put("GemWearRequest_10000001", 0x10000001);
		nameIdMap.put("GemWearResponse_10000002", 0x10000002);
		nameIdMap.put("GemTeardownRequest_10000003", 0x10000003);
		nameIdMap.put("GemTeardownResponse_10000004", 0x10000004);
		nameIdMap.put("GemLockRequest_10000005", 0x10000005);
		nameIdMap.put("GemLockResponse_10000006", 0x10000006);
		nameIdMap.put("GemComposeRequest_10000007", 0x10000007);
		nameIdMap.put("GemComposeResponse_10000008", 0x10000008);
		nameIdMap.put("GmShutdownServerRequest_77000001", 0x77000001);
		nameIdMap.put("GmShutdownServerResponse_77000002", 0x77000002);
		nameIdMap.put("GmForbidAccountListRequest_77000003", 0x77000003);
		nameIdMap.put("GmForbidAccountListResponse_77000004", 0x77000004);
		nameIdMap.put("GmForbidAccountRequest_77000005", 0x77000005);
		nameIdMap.put("GmForbidAccountResponse_77000006", 0x77000006);
		nameIdMap.put("GmUnblockAccountRequest_77000007", 0x77000007);
		nameIdMap.put("GmUnblockAccountResponse_77000008", 0x77000008);
		nameIdMap.put("GmPlayerLogoutRequest_77000009", 0x77000009);
		nameIdMap.put("GmPlayerLogouttResponse_7700000a", 0x7700000a);
		nameIdMap.put("HeroUpLevelRequest_16000001", 0x16000001);
		nameIdMap.put("HeroUpLevelResponse_16000002", 0x16000002);
		nameIdMap.put("HeroUpLevelMaxRequest_16000021", 0x16000021);
		nameIdMap.put("HeroUpLevelMaxResponse_16000022", 0x16000022);
		nameIdMap.put("HeroUpLevelBatchRequest_16000023", 0x16000023);
		nameIdMap.put("HeroUpLevelBatchResponse_16000024", 0x16000024);
		nameIdMap.put("HeroConflateRequest_16000003", 0x16000003);
		nameIdMap.put("HeroConflateResponse_16000004", 0x16000004);
		nameIdMap.put("HeroBattleRequest_16000005", 0x16000005);
		nameIdMap.put("HeroBattleResponse_16000006", 0x16000006);
		nameIdMap.put("HeroLevelResetRequest_16000007", 0x16000007);
		nameIdMap.put("HeroLevelResetResponse_16000008", 0x16000008);
		nameIdMap.put("HeroQualityResetRequest_16000011", 0x16000011);
		nameIdMap.put("HeroQualityResetResponse_16000012", 0x16000012);
		nameIdMap.put("HeroFreeDayRentRequest_16000030", 0x16000030);
		nameIdMap.put("HeroFreeDayRentResponse_16000031", 0x16000031);
		nameIdMap.put("HeroFreeDayRentChooseRequest_16000032", 0x16000032);
		nameIdMap.put("HeroFreeDayRentChooseResponse_16000033", 0x16000033);
		nameIdMap.put("ItemUseRequest_0b000003", 0x0b000003);
		nameIdMap.put("ItemUseResponse_0b000004", 0x0b000004);
		nameIdMap.put("MailListRequest_12000001", 0x12000001);
		nameIdMap.put("MailListResponse_12000002", 0x12000002);
		nameIdMap.put("MailSeeRequest_12000003", 0x12000003);
		nameIdMap.put("MailSeeResponse_12000004", 0x12000004);
		nameIdMap.put("MailReceiveRequest_12000005", 0x12000005);
		nameIdMap.put("MailReceiveResponse_12000006", 0x12000006);
		nameIdMap.put("MailDeleteRequest_12000007", 0x12000007);
		nameIdMap.put("MailDeleteResponse_12000008", 0x12000008);
		nameIdMap.put("MergeEquipmentWearRequest_23000001", 0x23000001);
		nameIdMap.put("MergeEquipmentWearResponse_23000002", 0x23000002);
		nameIdMap.put("MergeEquipmentTeardownRequest_23000003", 0x23000003);
		nameIdMap.put("MergeEquipmentTeardownResponse_23000004", 0x23000004);
		nameIdMap.put("MergeEquipmentPartStrengthRequest_23000007", 0x23000007);
		nameIdMap.put("MergeEquipmentPartStrengthResponse_23000008", 0x23000008);
		nameIdMap.put("PlayerLoginRequest_01000001", 0x01000001);
		nameIdMap.put("PlayerLoginResponse_01000002", 0x01000002);
		nameIdMap.put("PlayerLogoutPush_01100030", 0x01100030);
		nameIdMap.put("PlayerNameRequest_01000011", 0x01000011);
		nameIdMap.put("PlayerNameResponse_01000012", 0x01000012);
		nameIdMap.put("PlayerHeadRequest_01000013", 0x01000013);
		nameIdMap.put("PlayerHeadResponse_01000014", 0x01000014);
		nameIdMap.put("PlayerHeadFrameRequest_01000015", 0x01000015);
		nameIdMap.put("PlayerHeadFrameResponse_01000016", 0x01000016);
		nameIdMap.put("PlayerGenderRequest_01000017", 0x01000017);
		nameIdMap.put("PlayerGenderResponse_01000018", 0x01000018);
		nameIdMap.put("PlayerReconnecRequest_01000065", 0x01000065);
		nameIdMap.put("PlayerReconnecResponse_01000066", 0x01000066);
		nameIdMap.put("PlayerHeartbeatRequest_01000005", 0x01000005);
		nameIdMap.put("PlayerHeartbeatResponse_01000006", 0x01000006);
		nameIdMap.put("PlayerBriefInfoRequest_01000007", 0x01000007);
		nameIdMap.put("PlayerBriefInfoResponse_01000008", 0x01000008);
		nameIdMap.put("PlayerBriefInfoOtherRequest_01000009", 0x01000009);
		nameIdMap.put("PlayerBriefInfoOtherResponse_0100000a", 0x0100000a);
		nameIdMap.put("PlayerShowRequest_01000039", 0x01000039);
		nameIdMap.put("PlayerShowResponse_0100003a", 0x0100003a);
		nameIdMap.put("PlayerAlchemyRequest_01000040", 0x01000040);
		nameIdMap.put("PlayerAlchemyResponse_01000041", 0x01000041);
		nameIdMap.put("PlayerResetPush_01100016", 0x01100016);
		nameIdMap.put("PlayerErrorPush_01000099", 0x01000099);
		nameIdMap.put("ItemUseRequest_01000050", 0x01000050);
		nameIdMap.put("ItemUseResponse_01000051", 0x01000051);
		nameIdMap.put("PlayerCloudBoxPush_01100040", 0x01100040);
		nameIdMap.put("PlayerCloudBoxRequest_01000042", 0x01000042);
		nameIdMap.put("PlayerCloudBoxResponse_01000043", 0x01000043);
		nameIdMap.put("PlayerExpLevelPush_01100050", 0x01100050);
		nameIdMap.put("QuestListRequest_20000001", 0x20000001);
		nameIdMap.put("QuestListResponse_20000002", 0x20000002);
		nameIdMap.put("QuestReceiveRequest_20000004", 0x20000004);
		nameIdMap.put("QuestReceiveResponse_20000005", 0x20000005);
		nameIdMap.put("QuestReceiveActivePointRequest_20000008", 0x20000008);
		nameIdMap.put("QuestReceiveActivePointResponse_20000009", 0x20000009);
		nameIdMap.put("QuestGroupPush_20100008", 0x20100008);
		nameIdMap.put("QuestPush_20200008", 0x20200008);
		nameIdMap.put("QuestListAllRequest_20000051", 0x20000051);
		nameIdMap.put("QuestListAllResponse_20000052", 0x20000052);
		nameIdMap.put("QuestChallengeGroupRequest_20000020", 0x20000020);
		nameIdMap.put("QuestChallengeGroupResponse_20000021", 0x20000021);
		nameIdMap.put("QuestChallengeGroupDetailRequest_20000022", 0x20000022);
		nameIdMap.put("QuestChallengeGroupDetailResponse_20000023", 0x20000023);
		nameIdMap.put("QuestConditionCompletePush_20500001", 0x20500001);
		nameIdMap.put("QuestRewardPush_20600008", 0x20600008);
		nameIdMap.put("QuestAcceptRequest_20000026", 0x20000026);
		nameIdMap.put("QuestAcceptResponse_20000027", 0x20000027);
		nameIdMap.put("QuestBranchPriorityRequest_20000028", 0x20000028);
		nameIdMap.put("QuestBranchPriorityResponse_20000029", 0x20000029);
		nameIdMap.put("QuestBranchPriorityPush_20300000", 0x20300000);
		nameIdMap.put("QuestUpdateRequest_20000030", 0x20000030);
		nameIdMap.put("QuestUpdateResponse_20000031", 0x20000031);
		nameIdMap.put("QuestChooseRewardRequest_20000033", 0x20000033);
		nameIdMap.put("QuestChooseRewardResponse_20000034", 0x20000034);
		nameIdMap.put("RewardPush_55000501", 0x55000501);
		nameIdMap.put("SpendPush_55001501", 0x55001501);
		nameIdMap.put("RewardShowPush_55002501", 0x55002501);
		nameIdMap.put("GamePlayerOnlinePush_7d000010", 0x7d000010);
		nameIdMap.put("GamePlayerPush_7d000011", 0x7d000011);
		nameIdMap.put("GamePlayerPush_7d000100", 0x7d000100);
		nameIdMap.put("GamePlayerLogoutRequest_7d000101", 0x7d000101);
		nameIdMap.put("GamePlayerLogoutResponse_7d000102", 0x7d000102);
		nameIdMap.put("GameTestRequest_7d000500", 0x7d000500);
		nameIdMap.put("GameTestResponse_7d000501", 0x7d000501);
		nameIdMap.put("ServerStatusRequest_7d000901", 0x7d000901);
		nameIdMap.put("ServerStatusResponse_7d000902", 0x7d000902);
		nameIdMap.put("GamePlayerRequest_7d000015", 0x7d000015);
		nameIdMap.put("GamePlayerResponse_7d000016", 0x7d000016);
		nameIdMap.put("GameStatusPublish_7d000017", 0x7d000017);
		nameIdMap.put("LoginPlayerUidRequest_7d000018", 0x7d000018);
		nameIdMap.put("LoginPlayerUidResponse_7d000019", 0x7d000019);
		nameIdMap.put("PaymentOrderCreateRequest_7d000020", 0x7d000020);
		nameIdMap.put("PaymentOrderCreateResponse_7d000021", 0x7d000021);
		nameIdMap.put("PaymentOrderShipRequest_7d000022", 0x7d000022);
		nameIdMap.put("PaymentOrderShipResponse_7d000023", 0x7d000023);
		nameIdMap.put("GameCrossForwardPush_7d000002", 0x7d000002);
		nameIdMap.put("CrossGameForwardPush_7d000003", 0x7d000003);
		nameIdMap.put("GameCrossPlayerBroadcast_7d000005", 0x7d000005);
		nameIdMap.put("GameCrossBroadcast_7d000008", 0x7d000008);
		nameIdMap.put("GameDataPush_7d00000a", 0x7d00000a);
		nameIdMap.put("GameDataPushBatch_7d00000b", 0x7d00000b);
		nameIdMap.put("GameDataPushBatch2_7d00000c", 0x7d00000c);
		nameIdMap.put("ShopItemListRequest_15000001", 0x15000001);
		nameIdMap.put("ShopItemListResponse_15000002", 0x15000002);
		nameIdMap.put("ShopItemBuyRequest_15000003", 0x15000003);
		nameIdMap.put("ShopItemBuyResponse_15000004", 0x15000004);
		nameIdMap.put("ShopHeishiRefreshRequest_15000005", 0x15000005);
		nameIdMap.put("ShopHeishiRefreshResponse_15000006", 0x15000006);
		nameIdMap.put("MonthCardBuyRequest_15000010", 0x15000010);
		nameIdMap.put("MonthCardBuyResponse_15000011", 0x15000011);
		nameIdMap.put("MonthCardBuyRewardRequest_15000012", 0x15000012);
		nameIdMap.put("MonthCardBuyRewardResponse_15000013", 0x15000013);
		nameIdMap.put("MonthCardDayRewardRequest_15000014", 0x15000014);
		nameIdMap.put("MonthCardDayRewardResponse_15000015", 0x15000015);
		nameIdMap.put("MonthCardDoubleBonusRequest_15000016", 0x15000016);
		nameIdMap.put("MonthCardDoubleBonusResponse_15000017", 0x15000017);
		nameIdMap.put("ShopChapterPacksBuyRequest_15000020", 0x15000020);
		nameIdMap.put("ShopChapterPacksBuyResponse_15000021", 0x15000021);
		nameIdMap.put("ShopRechargeRequest_15000022", 0x15000022);
		nameIdMap.put("ShopRechargeResponse_15000023", 0x15000023);
		nameIdMap.put("PaymentOrderPush_15010020", 0x15010020);
		nameIdMap.put("ShopFundPassBuyRequest_15000030", 0x15000030);
		nameIdMap.put("ShopFundPassBuyResponse_15000031", 0x15000031);
		nameIdMap.put("ShopFundPassRewardRequest_15000032", 0x15000032);
		nameIdMap.put("ShopFundPassRewardResponse_15000033", 0x15000033);
		nameIdMap.put("ShopBoxOpenRequest_15000040", 0x15000040);
		nameIdMap.put("ShopBoxOpenResponse_15000041", 0x15000041);
		nameIdMap.put("StoryStartRequest_14000001", 0x14000001);
		nameIdMap.put("StoryStartResponse_14000002", 0x14000002);
		nameIdMap.put("StoryFinishRequest_14000003", 0x14000003);
		nameIdMap.put("StoryFinishResponse_14000004", 0x14000004);
		nameIdMap.put("TestGmCmdRequest_6f000001", 0x6f000001);
		nameIdMap.put("TestGmCmdResponse_6f000002", 0x6f000002);
		nameIdMap.put("TestAddItemRequest_6f000008", 0x6f000008);
		nameIdMap.put("TestAddItemResponse_6f000009", 0x6f000009);
		nameIdMap.put("TestMissionFinishRequest_6f000022", 0x6f000022);
		nameIdMap.put("TestMissionFinishResponse_6f000023", 0x6f000023);
		nameIdMap.put("TestRequest_6f000020", 0x6f000020);
		nameIdMap.put("TestResponse_6f000021", 0x6f000021);
		nameIdMap.put("TestMessageRequest_6f000080", 0x6f000080);
		nameIdMap.put("TestMessageResponse_6f000081", 0x6f000081);
	}	
	@Override
	public Message parseFrom(int msgID,byte[] data){
		Parser<?> parser = parsersMap.get(msgID);  
		if (parser == null) {
			throw new IllegalArgumentException("message not found : "+msgID);
		}
		try {
			return (Message) parser.parseFrom(data);
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
	}
	@Override	
	public int getMsgId(String name){
		Integer id = nameIdMap.get(name);
		if (id == null) { 
			throw new IllegalArgumentException("message id not found : " + name);
		}
		return id ;
	}
}
