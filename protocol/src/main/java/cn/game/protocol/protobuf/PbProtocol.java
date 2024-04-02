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

	public final static int ActivityListRequest_11000001 = 0x11000001;    //查看有哪些展示的活动  
	public final static int ActivityListResponse_11000002 = 0x11000002;    //展示中的活动id列表  
	public final static int ActivityInfoRequest_11000003 = 0x11000003;    //查看某活动数据  
	public final static int ActivityInfoResponse_11000004 = 0x11000004;    //活动数据  
	public final static int ActivityStatePush_11100006 = 0x11100006;    //活动状态改变推送，看具体情况选择性推送  
	public final static int BattleFieldStartRequest_13000001 = 0x13000001;    //开始关卡战斗请求  
	public final static int BattleFieldStartResponse_13000002 = 0x13000002;    //开始关卡战斗返回  
	public final static int BattleFieldEndRequest_13000003 = 0x13000003;    //结束关卡战斗请求  
	public final static int BattleFieldEndResponse_13000004 = 0x13000004;    //结束关卡战斗  
	public final static int BattleChapterRewardRequest_13000022 = 0x13000022;    //领取章通关奖励  
	public final static int BattleChapterRewardResponse_13000023 = 0x13000023;    
	public final static int BattleFieldStartPush_13000100 = 0x13000100;    //战斗开始推送  
	public final static int GmShutdownServerRequest_77000001 = 0x77000001;    //关闭服务器  
	public final static int GmShutdownServerResponse_77000002 = 0x77000002;    //关闭服务器响应  
	public final static int GmForbidAccountListRequest_77000003 = 0x77000003;    //请求封号列表  
	public final static int GmForbidAccountListResponse_77000004 = 0x77000004;    //响应封号列表  
	public final static int GmForbidAccountRequest_77000005 = 0x77000005;    //请求封号  
	public final static int GmForbidAccountResponse_77000006 = 0x77000006;    //响应封号  
	public final static int GmUnblockAccountRequest_77000007 = 0x77000007;    //请求解封  
	public final static int GmUnblockAccountResponse_77000008 = 0x77000008;    //响应解封  
	public final static int GmPlayerLogoutRequest_77000009 = 0x77000009;    //请求踢下线  
	public final static int GmPlayerLogouttResponse_7700000a = 0x7700000a;    //响应踢下线  
	public final static int HeroUpLevelRequest_16000001 = 0x16000001;    //英雄升级  
	public final static int HeroUpLevelResponse_16000002 = 0x16000002;    
	public final static int HeroConflateRequest_16000003 = 0x16000003;    //英雄合成  
	public final static int HeroConflateResponse_16000004 = 0x16000004;    
	public final static int MailListRequest_12000001 = 0x12000001;    //请求邮件列表。  
	public final static int MailListResponse_12000002 = 0x12000002;    //邮件列表数据  
	public final static int MailSeeRequest_12000003 = 0x12000003;    //查看未读邮件  
	public final static int MailSeeResponse_12000004 = 0x12000004;    //  
	public final static int MailReceiveRequest_12000005 = 0x12000005;    //领取邮件奖励  
	public final static int MailReceiveResponse_12000006 = 0x12000006;    //领取邮件奖励返回  
	public final static int MailDeleteRequest_12000007 = 0x12000007;    //删除邮件  
	public final static int MailDeleteResponse_12000008 = 0x12000008;    //删除邮件返回  
	public final static int MissionListRequest_20000001 = 0x20000001;    //查看某类任务数据  
	public final static int MissionListResponse_20000002 = 0x20000002;    //任务数据  
	public final static int MissionReceiveRequest_20000004 = 0x20000004;    //领取任务奖励  
	public final static int MissionReceiveResponse_20000005 = 0x20000005;    
	public final static int MissionActiveRequest_20000006 = 0x20000006;    //查看每日任务活跃奖励领取情况  
	public final static int MissionActiveResponse_20000007 = 0x20000007;    
	public final static int MissionReceiveActiveRequest_20000008 = 0x20000008;    //领取每日任务活跃奖励  
	public final static int MissionReceiveActiveResponse_20000009 = 0x20000009;    
	public final static int MissionChallengeGroupRequest_20000020 = 0x20000020;    //查看某类型挑战组任务  
	public final static int MissionChallengeGroupResponse_20000021 = 0x20000021;    
	public final static int MissionChallengeGroupDetailRequest_20000022 = 0x20000022;    //查看一组任务里的任务数据  
	public final static int MissionChallengeGroupDetailResponse_20000023 = 0x20000023;    
	public final static int MissionGroupPush_20100008 = 0x20100008;    //一组任务状态变化通知，一般是有任务可以领奖时，或者加入了新的任务，会推送这个协议  
	public final static int MissionPush_20200008 = 0x20200008;    //单个任务状态变化通知，一般是有任务可以领奖时，或者加入了新的任务，会推送这个协议  
	public final static int MissionConditionCompletePush_20500001 = 0x20500001;    //一个任务条件完成的时候，推送此协议  
	public final static int MissionRewardPush_20600008 = 0x20600008;    //对于自动交付（领奖）的任务，服务端领奖之后会推送此协议，也代表此任务完成，客户端根据此协议判断是否删除任务  
	public final static int MissionAcceptRequest_20000026 = 0x20000026;    //接取任务请求，一般是和npc，或者特殊的交互物触发  
	public final static int MissionAcceptResponse_20000027 = 0x20000027;    
	public final static int MissionBranchPriorityRequest_20000028 = 0x20000028;    //设置优先显示的支线组  
	public final static int MissionBranchPriorityResponse_20000029 = 0x20000029;    
	public final static int MissionBranchPriorityPush_20300000 = 0x20300000;    //推送优先显示的支线组  
	public final static int MissionUpdateRequest_20000030 = 0x20000030;    //增加任务条件进度数据，客户端发起  
	public final static int MissionUpdateResponse_20000031 = 0x20000031;    
	public final static int MissionChooseRewardRequest_20000033 = 0x20000033;    //领取任务奖励,一般是多个任务奖励，选择其中的一个奖励领取  
	public final static int MissionChooseRewardResponse_20000034 = 0x20000034;    
	public final static int PlayerLoginRequest_01000001 = 0x01000001;    //登陆  
	public final static int PlayerLoginResponse_01000002 = 0x01000002;    //用户登陆,返回游戏数据  
	public final static int PlayerLogoutPush_01100030 = 0x01100030;    //退出登录(客户端登录多个账号，或者服务器关闭等)  
	public final static int PlayerNameRequest_01000011 = 0x01000011;    //修改名字  
	public final static int PlayerNameResponse_01000012 = 0x01000012;    //修改名字返回  
	public final static int PlayerHeadRequest_01000013 = 0x01000013;    //修改头像  
	public final static int PlayerHeadResponse_01000014 = 0x01000014;    //修改头像返回  
	public final static int PlayerHeadFrameRequest_01000015 = 0x01000015;    //修改头像框  
	public final static int PlayerHeadFrameResponse_01000016 = 0x01000016;    //修改头像框返回  
	public final static int PlayerReconnecRequest_01000065 = 0x01000065;    //断线重连，和手机端通用  
	public final static int PlayerReconnecResponse_01000066 = 0x01000066;    //断线重连，和手机端通用  
	public final static int PlayerHeartbeatRequest_01000005 = 0x01000005;    //心跳  
	public final static int PlayerHeartbeatResponse_01000006 = 0x01000006;    //心跳返回  
	public final static int PlayerBriefInfoRequest_01000007 = 0x01000007;    //获取一组玩家简略信息 本服  
	public final static int PlayerBriefInfoResponse_01000008 = 0x01000008;    
	public final static int PlayerBriefInfoOtherRequest_01000009 = 0x01000009;    //获取一组玩家简略信息 查询本服和跨服  
	public final static int PlayerBriefInfoOtherResponse_0100000a = 0x0100000a;    
	public final static int PlayerShowRequest_01000039 = 0x01000039;    //获取一个玩家的名片  
	public final static int PlayerShowResponse_0100003a = 0x0100003a;    
	public final static int PlayerResetPush_01100016 = 0x01100016;    //通知客户端，重置一些本地的数据，大多是一些简单次数之类，对于复杂数据的刷新，通过单独定义协议，服务端推送来刷新  
	public final static int PlayerErrorPush_01000099 = 0x01000099;    //对于客户端的请求，如果服务器处理过程中出现未知异常，导致没有返回对应的响应包时，返回此错误消息  
	public final static int RewardPush_55000501 = 0x55000501;    //奖励推送,客户端收到这个协议之后，将奖励增加到本地。  
	public final static int SpendPush_55001501 = 0x55001501;    //消耗推送，客户端收到这个协议之后，减少本地的物品  
	public final static int RewardShowPush_55002501 = 0x55002501;    //一般需要显示推送获得的奖励时  
	public final static int GamePlayerOnlinePush_7d000010 = 0x7d000010;    //Game服务器之间的玩家在线状态变更通知,Game-->Game  
	public final static int GamePlayerPush_7d000011 = 0x7d000011;    //Game收到推送，将里面的具体消息发给指定的玩家,Game-->Game-->Player  
	public final static int GamePlayerPush_7d000100 = 0x7d000100;    //Game收到推送，将里面的具体消息发给指定的玩家,Game-->Game-->Player  
	public final static int GamePlayerLogoutRequest_7d000101 = 0x7d000101;    //退出某个玩家  
	public final static int GamePlayerLogoutResponse_7d000102 = 0x7d000102;    
	public final static int GameTestRequest_7d000500 = 0x7d000500;    //测试的  
	public final static int GameTestResponse_7d000501 = 0x7d000501;    
	public final static int ServerStatusRequest_7d000901 = 0x7d000901;    //获取在线人数等数据  
	public final static int ServerStatusResponse_7d000902 = 0x7d000902;    
	public final static int GamePlayerRequest_7d000015 = 0x7d000015;    //向某个服务器的玩家发送消息让玩家处理  
	public final static int GamePlayerResponse_7d000016 = 0x7d000016;    //向某个服务器的玩家发送消息让玩家处理，处理后返回  
	public final static int GameStatusPublish_7d000017 = 0x7d000017;    //广播服务器在线人数  
	public final static int LoginPlayerUidRequest_7d000018 = 0x7d000018;    
	public final static int LoginPlayerUidResponse_7d000019 = 0x7d000019;    
	public final static int PaymentOrderCreateRequest_7d000020 = 0x7d000020;    //请求创建支付订单  
	public final static int PaymentOrderCreateResponse_7d000021 = 0x7d000021;    
	public final static int PaymentOrderShipRequest_7d000022 = 0x7d000022;    //通知game，给玩家发货，执行支付后的流程  
	public final static int PaymentOrderShipResponse_7d000023 = 0x7d000023;    
	public final static int GameCrossForwardPush_7d000002 = 0x7d000002;    //Game向Cross发数据，请求转发消息给指定玩家  
	public final static int CrossGameForwardPush_7d000003 = 0x7d000003;    //Game收到Cross推送，将里面的具体消息发给指定的玩家  
	public final static int GameCrossPlayerBroadcast_7d000005 = 0x7d000005;    //Game向Cross发数据，将数据包广播给不同Game服务器的玩家  
	public final static int GameCrossBroadcast_7d000008 = 0x7d000008;    //Game向Cross发数据，将里面的消息广播给指定Game，或者所有Game  
	public final static int GameDataPush_7d00000a = 0x7d00000a;    //Game向Data发数据，执行数据库操作  
	public final static int GameDataPushBatch_7d00000b = 0x7d00000b;    //Game向Data发数据，执行数据库操作  
	public final static int GameDataPushBatch2_7d00000c = 0x7d00000c;    
	public final static int ShopGroupItemListRequest_15000001 = 0x15000001;    //查看商品组里面的商品列表  
	public final static int ShopGroupItemListResponse_15000002 = 0x15000002;    
	public final static int ShopItemBuyRequest_15000003 = 0x15000003;    //购买商品  
	public final static int ShopItemBuyResponse_15000004 = 0x15000004;    
	public final static int MonthCardBuyRequest_15000010 = 0x15000010;    //购买月卡  
	public final static int MonthCardBuyResponse_15000011 = 0x15000011;    
	public final static int MonthCardBuyRewardRequest_15000012 = 0x15000012;    //领取月卡购买奖励，每个月卡只能领取一次  
	public final static int MonthCardBuyRewardResponse_15000013 = 0x15000013;    
	public final static int MonthCardDayRewardRequest_15000014 = 0x15000014;    //领取月卡每日奖励  
	public final static int MonthCardDayRewardResponse_15000015 = 0x15000015;    
	public final static int ShopGiftBuyRequest_15000020 = 0x15000020;    //购买礼包  
	public final static int ShopGiftBuyResponse_15000021 = 0x15000021;    
	public final static int PaymentOrderPush_15010020 = 0x15010020;    //支付订单相关参数，客户端收到这个协议就可以利用里面的参数发起支付了  
	public final static int AdvertiseWatchFinishRequest_15000030 = 0x15000030;    //广告观看完毕  
	public final static int AdvertiseWatchFinishResponse_15000031 = 0x15000031;    
	public final static int TestGmCmdRequest_6f000001 = 0x6f000001;    //gm指令  
	public final static int TestGmCmdResponse_6f000002 = 0x6f000002;    
	public final static int TestAddItemRequest_6f000008 = 0x6f000008;    //获取游戏中的各种物品  
	public final static int TestAddItemResponse_6f000009 = 0x6f000009;    
	public final static int TestMissionFinishRequest_6f000022 = 0x6f000022;    //直接完成任务  
	public final static int TestMissionFinishResponse_6f000023 = 0x6f000023;    
	public final static int TestRequest_6f000020 = 0x6f000020;    //只是测试  
	public final static int TestResponse_6f000021 = 0x6f000021;    
	public final static int TestMessageRequest_6f000080 = 0x6f000080;    //模拟测试某玩家发送协议  
	public final static int TestMessageResponse_6f000081 = 0x6f000081;    	

	static {	
		parsersMap.put(ActivityListRequest_11000001, cn.game.protocol.protobuf.ActivityMsg.ActivityListRequest_11000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ActivityListResponse_11000002, cn.game.protocol.protobuf.ActivityMsg.ActivityListResponse_11000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ActivityInfoRequest_11000003, cn.game.protocol.protobuf.ActivityMsg.ActivityInfoRequest_11000003.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ActivityInfoResponse_11000004, cn.game.protocol.protobuf.ActivityMsg.ActivityInfoResponse_11000004.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ActivityStatePush_11100006, cn.game.protocol.protobuf.ActivityMsg.ActivityStatePush_11100006.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleFieldStartRequest_13000001, cn.game.protocol.protobuf.BattleMsg.BattleFieldStartRequest_13000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleFieldStartResponse_13000002, cn.game.protocol.protobuf.BattleMsg.BattleFieldStartResponse_13000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleFieldEndRequest_13000003, cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleFieldEndResponse_13000004, cn.game.protocol.protobuf.BattleMsg.BattleFieldEndResponse_13000004.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleChapterRewardRequest_13000022, cn.game.protocol.protobuf.BattleMsg.BattleChapterRewardRequest_13000022.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleChapterRewardResponse_13000023, cn.game.protocol.protobuf.BattleMsg.BattleChapterRewardResponse_13000023.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleFieldStartPush_13000100, cn.game.protocol.protobuf.BattleMsg.BattleFieldStartPush_13000100.getDefaultInstance()
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
		parsersMap.put(HeroConflateRequest_16000003, cn.game.protocol.protobuf.HeroMsg.HeroConflateRequest_16000003.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HeroConflateResponse_16000004, cn.game.protocol.protobuf.HeroMsg.HeroConflateResponse_16000004.getDefaultInstance()
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
		parsersMap.put(MissionListRequest_20000001, cn.game.protocol.protobuf.MissionMsg.MissionListRequest_20000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionListResponse_20000002, cn.game.protocol.protobuf.MissionMsg.MissionListResponse_20000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionReceiveRequest_20000004, cn.game.protocol.protobuf.MissionMsg.MissionReceiveRequest_20000004.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionReceiveResponse_20000005, cn.game.protocol.protobuf.MissionMsg.MissionReceiveResponse_20000005.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionActiveRequest_20000006, cn.game.protocol.protobuf.MissionMsg.MissionActiveRequest_20000006.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionActiveResponse_20000007, cn.game.protocol.protobuf.MissionMsg.MissionActiveResponse_20000007.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionReceiveActiveRequest_20000008, cn.game.protocol.protobuf.MissionMsg.MissionReceiveActiveRequest_20000008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionReceiveActiveResponse_20000009, cn.game.protocol.protobuf.MissionMsg.MissionReceiveActiveResponse_20000009.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionChallengeGroupRequest_20000020, cn.game.protocol.protobuf.MissionMsg.MissionChallengeGroupRequest_20000020.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionChallengeGroupResponse_20000021, cn.game.protocol.protobuf.MissionMsg.MissionChallengeGroupResponse_20000021.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionChallengeGroupDetailRequest_20000022, cn.game.protocol.protobuf.MissionMsg.MissionChallengeGroupDetailRequest_20000022.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionChallengeGroupDetailResponse_20000023, cn.game.protocol.protobuf.MissionMsg.MissionChallengeGroupDetailResponse_20000023.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionGroupPush_20100008, cn.game.protocol.protobuf.MissionMsg.MissionGroupPush_20100008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionPush_20200008, cn.game.protocol.protobuf.MissionMsg.MissionPush_20200008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionConditionCompletePush_20500001, cn.game.protocol.protobuf.MissionMsg.MissionConditionCompletePush_20500001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionRewardPush_20600008, cn.game.protocol.protobuf.MissionMsg.MissionRewardPush_20600008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionAcceptRequest_20000026, cn.game.protocol.protobuf.MissionMsg.MissionAcceptRequest_20000026.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionAcceptResponse_20000027, cn.game.protocol.protobuf.MissionMsg.MissionAcceptResponse_20000027.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionBranchPriorityRequest_20000028, cn.game.protocol.protobuf.MissionMsg.MissionBranchPriorityRequest_20000028.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionBranchPriorityResponse_20000029, cn.game.protocol.protobuf.MissionMsg.MissionBranchPriorityResponse_20000029.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionBranchPriorityPush_20300000, cn.game.protocol.protobuf.MissionMsg.MissionBranchPriorityPush_20300000.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionUpdateRequest_20000030, cn.game.protocol.protobuf.MissionMsg.MissionUpdateRequest_20000030.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionUpdateResponse_20000031, cn.game.protocol.protobuf.MissionMsg.MissionUpdateResponse_20000031.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionChooseRewardRequest_20000033, cn.game.protocol.protobuf.MissionMsg.MissionChooseRewardRequest_20000033.getDefaultInstance()
				.getParserForType());
		parsersMap.put(MissionChooseRewardResponse_20000034, cn.game.protocol.protobuf.MissionMsg.MissionChooseRewardResponse_20000034.getDefaultInstance()
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
		parsersMap.put(PlayerResetPush_01100016, cn.game.protocol.protobuf.PlayerMsg.PlayerResetPush_01100016.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerErrorPush_01000099, cn.game.protocol.protobuf.PlayerMsg.PlayerErrorPush_01000099.getDefaultInstance()
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
		parsersMap.put(ShopGroupItemListRequest_15000001, cn.game.protocol.protobuf.ShopMsg.ShopGroupItemListRequest_15000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ShopGroupItemListResponse_15000002, cn.game.protocol.protobuf.ShopMsg.ShopGroupItemListResponse_15000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ShopItemBuyRequest_15000003, cn.game.protocol.protobuf.ShopMsg.ShopItemBuyRequest_15000003.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ShopItemBuyResponse_15000004, cn.game.protocol.protobuf.ShopMsg.ShopItemBuyResponse_15000004.getDefaultInstance()
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
		parsersMap.put(ShopGiftBuyRequest_15000020, cn.game.protocol.protobuf.ShopMsg.ShopGiftBuyRequest_15000020.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ShopGiftBuyResponse_15000021, cn.game.protocol.protobuf.ShopMsg.ShopGiftBuyResponse_15000021.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PaymentOrderPush_15010020, cn.game.protocol.protobuf.ShopMsg.PaymentOrderPush_15010020.getDefaultInstance()
				.getParserForType());
		parsersMap.put(AdvertiseWatchFinishRequest_15000030, cn.game.protocol.protobuf.ShopMsg.AdvertiseWatchFinishRequest_15000030.getDefaultInstance()
				.getParserForType());
		parsersMap.put(AdvertiseWatchFinishResponse_15000031, cn.game.protocol.protobuf.ShopMsg.AdvertiseWatchFinishResponse_15000031.getDefaultInstance()
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
		nameIdMap.put("ActivityInfoRequest_11000003", 0x11000003);
		nameIdMap.put("ActivityInfoResponse_11000004", 0x11000004);
		nameIdMap.put("ActivityStatePush_11100006", 0x11100006);
		nameIdMap.put("BattleFieldStartRequest_13000001", 0x13000001);
		nameIdMap.put("BattleFieldStartResponse_13000002", 0x13000002);
		nameIdMap.put("BattleFieldEndRequest_13000003", 0x13000003);
		nameIdMap.put("BattleFieldEndResponse_13000004", 0x13000004);
		nameIdMap.put("BattleChapterRewardRequest_13000022", 0x13000022);
		nameIdMap.put("BattleChapterRewardResponse_13000023", 0x13000023);
		nameIdMap.put("BattleFieldStartPush_13000100", 0x13000100);
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
		nameIdMap.put("HeroConflateRequest_16000003", 0x16000003);
		nameIdMap.put("HeroConflateResponse_16000004", 0x16000004);
		nameIdMap.put("MailListRequest_12000001", 0x12000001);
		nameIdMap.put("MailListResponse_12000002", 0x12000002);
		nameIdMap.put("MailSeeRequest_12000003", 0x12000003);
		nameIdMap.put("MailSeeResponse_12000004", 0x12000004);
		nameIdMap.put("MailReceiveRequest_12000005", 0x12000005);
		nameIdMap.put("MailReceiveResponse_12000006", 0x12000006);
		nameIdMap.put("MailDeleteRequest_12000007", 0x12000007);
		nameIdMap.put("MailDeleteResponse_12000008", 0x12000008);
		nameIdMap.put("MissionListRequest_20000001", 0x20000001);
		nameIdMap.put("MissionListResponse_20000002", 0x20000002);
		nameIdMap.put("MissionReceiveRequest_20000004", 0x20000004);
		nameIdMap.put("MissionReceiveResponse_20000005", 0x20000005);
		nameIdMap.put("MissionActiveRequest_20000006", 0x20000006);
		nameIdMap.put("MissionActiveResponse_20000007", 0x20000007);
		nameIdMap.put("MissionReceiveActiveRequest_20000008", 0x20000008);
		nameIdMap.put("MissionReceiveActiveResponse_20000009", 0x20000009);
		nameIdMap.put("MissionChallengeGroupRequest_20000020", 0x20000020);
		nameIdMap.put("MissionChallengeGroupResponse_20000021", 0x20000021);
		nameIdMap.put("MissionChallengeGroupDetailRequest_20000022", 0x20000022);
		nameIdMap.put("MissionChallengeGroupDetailResponse_20000023", 0x20000023);
		nameIdMap.put("MissionGroupPush_20100008", 0x20100008);
		nameIdMap.put("MissionPush_20200008", 0x20200008);
		nameIdMap.put("MissionConditionCompletePush_20500001", 0x20500001);
		nameIdMap.put("MissionRewardPush_20600008", 0x20600008);
		nameIdMap.put("MissionAcceptRequest_20000026", 0x20000026);
		nameIdMap.put("MissionAcceptResponse_20000027", 0x20000027);
		nameIdMap.put("MissionBranchPriorityRequest_20000028", 0x20000028);
		nameIdMap.put("MissionBranchPriorityResponse_20000029", 0x20000029);
		nameIdMap.put("MissionBranchPriorityPush_20300000", 0x20300000);
		nameIdMap.put("MissionUpdateRequest_20000030", 0x20000030);
		nameIdMap.put("MissionUpdateResponse_20000031", 0x20000031);
		nameIdMap.put("MissionChooseRewardRequest_20000033", 0x20000033);
		nameIdMap.put("MissionChooseRewardResponse_20000034", 0x20000034);
		nameIdMap.put("PlayerLoginRequest_01000001", 0x01000001);
		nameIdMap.put("PlayerLoginResponse_01000002", 0x01000002);
		nameIdMap.put("PlayerLogoutPush_01100030", 0x01100030);
		nameIdMap.put("PlayerNameRequest_01000011", 0x01000011);
		nameIdMap.put("PlayerNameResponse_01000012", 0x01000012);
		nameIdMap.put("PlayerHeadRequest_01000013", 0x01000013);
		nameIdMap.put("PlayerHeadResponse_01000014", 0x01000014);
		nameIdMap.put("PlayerHeadFrameRequest_01000015", 0x01000015);
		nameIdMap.put("PlayerHeadFrameResponse_01000016", 0x01000016);
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
		nameIdMap.put("PlayerResetPush_01100016", 0x01100016);
		nameIdMap.put("PlayerErrorPush_01000099", 0x01000099);
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
		nameIdMap.put("ShopGroupItemListRequest_15000001", 0x15000001);
		nameIdMap.put("ShopGroupItemListResponse_15000002", 0x15000002);
		nameIdMap.put("ShopItemBuyRequest_15000003", 0x15000003);
		nameIdMap.put("ShopItemBuyResponse_15000004", 0x15000004);
		nameIdMap.put("MonthCardBuyRequest_15000010", 0x15000010);
		nameIdMap.put("MonthCardBuyResponse_15000011", 0x15000011);
		nameIdMap.put("MonthCardBuyRewardRequest_15000012", 0x15000012);
		nameIdMap.put("MonthCardBuyRewardResponse_15000013", 0x15000013);
		nameIdMap.put("MonthCardDayRewardRequest_15000014", 0x15000014);
		nameIdMap.put("MonthCardDayRewardResponse_15000015", 0x15000015);
		nameIdMap.put("ShopGiftBuyRequest_15000020", 0x15000020);
		nameIdMap.put("ShopGiftBuyResponse_15000021", 0x15000021);
		nameIdMap.put("PaymentOrderPush_15010020", 0x15010020);
		nameIdMap.put("AdvertiseWatchFinishRequest_15000030", 0x15000030);
		nameIdMap.put("AdvertiseWatchFinishResponse_15000031", 0x15000031);
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
