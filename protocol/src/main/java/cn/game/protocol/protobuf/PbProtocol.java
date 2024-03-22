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
	public final static int FriendListRequest_30000001 = 0x30000001;    //请求好友列表  
	public final static int FriendListResponse_30000002 = 0x30000002;    //好友数据  
	public final static int FriendBlackListRequest_30000051 = 0x30000051;    //请求黑名单列表  
	public final static int FriendBlackListResponse_30000052 = 0x30000052;    
	public final static int FriendApplyListRequest_30000053 = 0x30000053;    //请求好友申请列表  
	public final static int FriendApplyListResponse_30000054 = 0x30000054;    
	public final static int FriendRecommendRequest_30000003 = 0x30000003;    //搜索符合条件的推荐好友，换一批也是此条协议,刷新有时间间隔限制  
	public final static int FriendRecommendResponse_30000004 = 0x30000004;    //推荐好友列表  
	public final static int FriendSearchRequest_30000020 = 0x30000020;    //按账号id搜索玩家  
	public final static int FriendSearchResponse_30000021 = 0x30000021;    //搜索出来的玩家  
	public final static int FriendApplyRequest_30000005 = 0x30000005;    //申请成为对方好友或者批量申请好友，从黑名单里加好友也用这个协议  
	public final static int FriendApplyResponse_30000006 = 0x30000006;    
	public final static int FriendApplicationRequest_30000007 = 0x30000007;    //处理好友申请,包含批量处理  
	public final static int FriendApplicationResponse_30000008 = 0x30000008;    
	public final static int FriendDeleteRequest_30000009 = 0x30000009;    //删除好友,从黑名单里删除也用这个协议  
	public final static int FriendDeleteResponse_3000000a = 0x3000000a;    
	public final static int FriendBlackRequest_30000010 = 0x30000010;    //将玩家加入到黑名单  
	public final static int FriendBlackResponse_30000011 = 0x30000011;    
	public final static int FriendAttentionRequest_30000030 = 0x30000030;    //关注好友  
	public final static int FriendAttentionResponse_30000031 = 0x30000031;    
	public final static int FriendshipRequest_30000012 = 0x30000012;    //赠送好友友情点  
	public final static int FriendshipResponse_30000013 = 0x30000013;    
	public final static int FriendshipReceiveRequest_30000014 = 0x30000014;    //领取好友赠送的友情点，包含一键领取  
	public final static int FriendshipReceiveResponse_30000015 = 0x30000015;    
	public final static int FriendGiftRequest_30000016 = 0x30000016;    //送好友礼物  
	public final static int FriendGiftResponse_30000017 = 0x30000017;    
	public final static int FriendApplyPush_30000022 = 0x30000022;    //玩家收到其他服务器的好友申请  
	public final static int FriendAddPush_30000023 = 0x30000023;    //增加其他服务器的好友  
	public final static int FriendDelPush_30000024 = 0x30000024;    //删除其他服务器的好友  
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
	public final static int DeprecatedItemSaleRequest_0b000001 = 0x0b000001;    //出售  
	public final static int DeprecatedItemSaleResponse_0b000002 = 0x0b000002;    
	public final static int DeprecatedItemUseRequest_0b000003 = 0x0b000003;    //使用道具  
	public final static int DeprecatedItemUseResponse_0b000004 = 0x0b000004;    //使用道具后可能会给资源，道具等。  
	public final static int DecomposeRequest_0b000005 = 0x0b000005;    //分解  
	public final static int DecomposeResponse_0b000006 = 0x0b000006;    
	public final static int EquipLockRequest_0b000007 = 0x0b000007;    //锁定  
	public final static int EquipLockResponse_0b000008 = 0x0b000008;    
	public final static int ItemDiscardRequest_0b000103 = 0x0b000103;    //丢弃道具请求  
	public final static int ItemDiscardResponse_0b000104 = 0x0b000104;    //丢弃道具响应  
	public final static int ItemUseRequest_0b000107 = 0x0b000107;    //使用道具请求  
	public final static int ItemUseResponse_0b000108 = 0x0b000108;    //使用道具响应  
	public final static int ItemBagSortRequest_0b000113 = 0x0b000113;    //一键整理背包请求  
	public final static int ItemBagSortResponse_0b000114 = 0x0b000114;    //一键整理背包响应  
	public final static int ItemBagGridPush_0b000320 = 0x0b000320;    //背包格子推送  
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
	public final static int PlayerBuyPowerItemRequest_01000022 = 0x01000022;    //请求购买体力物品  
	public final static int PlayerBuyPowerItemResponse_01000023 = 0x01000023;    //服务器返回购买结果  
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
	public final static int AdvertiseWatchFinishRequest_15000030 = 0x15000030;    //广告观看完毕  
	public final static int AdvertiseWatchFinishResponse_15000031 = 0x15000031;    
	public final static int TestGmCmdRequest_6f000001 = 0x6f000001;    //gm指令  
	public final static int TestGmCmdResponse_6f000002 = 0x6f000002;    
	public final static int TestAddItemRequest_6f000008 = 0x6f000008;    //获取游戏中的各种物品  
	public final static int TestAddItemResponse_6f000009 = 0x6f000009;    
	public final static int TestMissionFinishRequest_6f000022 = 0x6f000022;    //直接完成任务  
	public final static int TestMissionFinishResponse_6f000023 = 0x6f000023;    
	public final static int TestStoryFinishRequest_6f000024 = 0x6f000024;    //直接完成剧情  
	public final static int TestStoryFinishResponse_6f000025 = 0x6f000025;    
	public final static int TestMapTransmitRequest_6f000026 = 0x6f000026;    //传送到某地图  
	public final static int TestMapTransmitResponse_6f000027 = 0x6f000027;    
	public final static int TestExplorePosPush_6f000088 = 0x6f000088;    //修改玩家在地图上的位置  
	public final static int TestMailRequest_6f000010 = 0x6f000010;    //测试发送邮件  
	public final static int TestMailResponse_6f000011 = 0x6f000011;    
	public final static int TestRequest_6f000020 = 0x6f000020;    //只是测试  
	public final static int TestResponse_6f000021 = 0x6f000021;    
	public final static int TestDbRequest_6f000041 = 0x6f000041;    //  
	public final static int TestDbResponse_6f000042 = 0x6f000042;    
	public final static int TestAddExploreItemRequest_6f000030 = 0x6f000030;    //挑战副本 获取各种物品  
	public final static int TestAddExploreItemResponse_6f000031 = 0x6f000031;    
	public final static int TestAddOrDelBagItemRequest_6f000032 = 0x6f000032;    //请求增加或减少背包道具  
	public final static int TestAddOrDelBagItemResponse_6f000033 = 0x6f000033;    //响应背包道具  
	public final static int TestExploreMapRequest_6f000035 = 0x6f000035;    //查看某人探索地图数据  
	public final static int TestExploreMapResponse_6f000036 = 0x6f000036;    
	public final static int TestExploreDeleteMonsterRequest_6f000037 = 0x6f000037;    //清除当前探索地图所有怪物  
	public final static int TestExploreDeleteMonsterResponse_6f000038 = 0x6f000038;    //清除怪物响应(响应之前推送 ExploreFloorObjectDeleteByIdPush_52100907)  
	public final static int TestExploreDeleteObjectRequest_6f000039 = 0x6f000039;    //清除探索地图某位置的物体  
	public final static int TestExploreDeleteObjectResponse_6f00003a = 0x6f00003a;    
	public final static int TestExploreAddBarrierRequest_6f000050 = 0x6f000050;    //探索指定位置添加障碍  
	public final static int TestExploreAddBarrierResponse_6f000051 = 0x6f000051;    
	public final static int TestExploreFixRoleHp1000Request_6f000052 = 0x6f000052;    //恢复探索角色血量到最大值  
	public final static int TestExploreFixRoleHp1000Response_6f000053 = 0x6f000053;    
	public final static int TestMessageRequest_6f000080 = 0x6f000080;    //模拟测试某玩家发送协议  
	public final static int TestMessageResponse_6f000081 = 0x6f000081;    
	public final static int TestAddBuffRequest_6f00003b = 0x6f00003b;    //测试添加buff  
	public final static int TestAddBuffResponse_6f00003c = 0x6f00003c;    
	public final static int TestEnterPhaseTwoRequest_6f000091 = 0x6f000091;    //直接进入探索第二阶段  
	public final static int TestEnterPhaseTwoResponse_6f000092 = 0x6f000092;    
	public final static int TestExploreLevelGoalsFinishRequest_6f000093 = 0x6f000093;    //直接完成探索区域目标  
	public final static int TestExploreLevelGoalsFinishResponse_6f000094 = 0x6f000094;    
	public final static int TestCommandRequest_6f000095 = 0x6f000095;    //测试执行命令  
	public final static int TestCommandResponse_6f000096 = 0x6f000096;    
	public final static int TestRoleAttributeRequest_6f000100 = 0x6f000100;    //查看角色相关属性，方便前后端比对  
	public final static int TestRoleAttributeResponse_6f000101 = 0x6f000101;    
	public final static int TestGameEventTriggerRequest_6f000105 = 0x6f000105;    
	public final static int TestGameEventTriggerResponse_6f000106 = 0x6f000106;    
	public final static int TestGenObjectRequest_6f000107 = 0x6f000107;    
	public final static int TestGenObjectResponse_6f000108 = 0x6f000108;    
	public final static int TestStoreDiscountResponse_6f00010a = 0x6f00010a;    
	public final static int TestRolePromotionRequest_6f00010b = 0x6f00010b;    //增加角色晋升等级。  
	public final static int TestRolePromotionResponse_6f00010c = 0x6f00010c;    
	public final static int TestRoleUnlockOccupationTalentNodeRequest_6f00010d = 0x6f00010d;    //解锁所有天赋节点  
	public final static int TestRoleUnlockOccupationTalentNodeResponse_6f00010e = 0x6f00010e;    	

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
		parsersMap.put(FriendListRequest_30000001, cn.game.protocol.protobuf.FriendMsg.FriendListRequest_30000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendListResponse_30000002, cn.game.protocol.protobuf.FriendMsg.FriendListResponse_30000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendBlackListRequest_30000051, cn.game.protocol.protobuf.FriendMsg.FriendBlackListRequest_30000051.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendBlackListResponse_30000052, cn.game.protocol.protobuf.FriendMsg.FriendBlackListResponse_30000052.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendApplyListRequest_30000053, cn.game.protocol.protobuf.FriendMsg.FriendApplyListRequest_30000053.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendApplyListResponse_30000054, cn.game.protocol.protobuf.FriendMsg.FriendApplyListResponse_30000054.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendRecommendRequest_30000003, cn.game.protocol.protobuf.FriendMsg.FriendRecommendRequest_30000003.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendRecommendResponse_30000004, cn.game.protocol.protobuf.FriendMsg.FriendRecommendResponse_30000004.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendSearchRequest_30000020, cn.game.protocol.protobuf.FriendMsg.FriendSearchRequest_30000020.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendSearchResponse_30000021, cn.game.protocol.protobuf.FriendMsg.FriendSearchResponse_30000021.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendApplyRequest_30000005, cn.game.protocol.protobuf.FriendMsg.FriendApplyRequest_30000005.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendApplyResponse_30000006, cn.game.protocol.protobuf.FriendMsg.FriendApplyResponse_30000006.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendApplicationRequest_30000007, cn.game.protocol.protobuf.FriendMsg.FriendApplicationRequest_30000007.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendApplicationResponse_30000008, cn.game.protocol.protobuf.FriendMsg.FriendApplicationResponse_30000008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendDeleteRequest_30000009, cn.game.protocol.protobuf.FriendMsg.FriendDeleteRequest_30000009.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendDeleteResponse_3000000a, cn.game.protocol.protobuf.FriendMsg.FriendDeleteResponse_3000000a.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendBlackRequest_30000010, cn.game.protocol.protobuf.FriendMsg.FriendBlackRequest_30000010.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendBlackResponse_30000011, cn.game.protocol.protobuf.FriendMsg.FriendBlackResponse_30000011.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendAttentionRequest_30000030, cn.game.protocol.protobuf.FriendMsg.FriendAttentionRequest_30000030.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendAttentionResponse_30000031, cn.game.protocol.protobuf.FriendMsg.FriendAttentionResponse_30000031.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendshipRequest_30000012, cn.game.protocol.protobuf.FriendMsg.FriendshipRequest_30000012.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendshipResponse_30000013, cn.game.protocol.protobuf.FriendMsg.FriendshipResponse_30000013.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendshipReceiveRequest_30000014, cn.game.protocol.protobuf.FriendMsg.FriendshipReceiveRequest_30000014.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendshipReceiveResponse_30000015, cn.game.protocol.protobuf.FriendMsg.FriendshipReceiveResponse_30000015.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendGiftRequest_30000016, cn.game.protocol.protobuf.FriendMsg.FriendGiftRequest_30000016.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendGiftResponse_30000017, cn.game.protocol.protobuf.FriendMsg.FriendGiftResponse_30000017.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendApplyPush_30000022, cn.game.protocol.protobuf.FriendMsg.FriendApplyPush_30000022.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendAddPush_30000023, cn.game.protocol.protobuf.FriendMsg.FriendAddPush_30000023.getDefaultInstance()
				.getParserForType());
		parsersMap.put(FriendDelPush_30000024, cn.game.protocol.protobuf.FriendMsg.FriendDelPush_30000024.getDefaultInstance()
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
		parsersMap.put(DeprecatedItemSaleRequest_0b000001, cn.game.protocol.protobuf.ItemMsg.DeprecatedItemSaleRequest_0b000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(DeprecatedItemSaleResponse_0b000002, cn.game.protocol.protobuf.ItemMsg.DeprecatedItemSaleResponse_0b000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(DeprecatedItemUseRequest_0b000003, cn.game.protocol.protobuf.ItemMsg.DeprecatedItemUseRequest_0b000003.getDefaultInstance()
				.getParserForType());
		parsersMap.put(DeprecatedItemUseResponse_0b000004, cn.game.protocol.protobuf.ItemMsg.DeprecatedItemUseResponse_0b000004.getDefaultInstance()
				.getParserForType());
		parsersMap.put(DecomposeRequest_0b000005, cn.game.protocol.protobuf.ItemMsg.DecomposeRequest_0b000005.getDefaultInstance()
				.getParserForType());
		parsersMap.put(DecomposeResponse_0b000006, cn.game.protocol.protobuf.ItemMsg.DecomposeResponse_0b000006.getDefaultInstance()
				.getParserForType());
		parsersMap.put(EquipLockRequest_0b000007, cn.game.protocol.protobuf.ItemMsg.EquipLockRequest_0b000007.getDefaultInstance()
				.getParserForType());
		parsersMap.put(EquipLockResponse_0b000008, cn.game.protocol.protobuf.ItemMsg.EquipLockResponse_0b000008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ItemDiscardRequest_0b000103, cn.game.protocol.protobuf.ItemMsg.ItemDiscardRequest_0b000103.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ItemDiscardResponse_0b000104, cn.game.protocol.protobuf.ItemMsg.ItemDiscardResponse_0b000104.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ItemUseRequest_0b000107, cn.game.protocol.protobuf.ItemMsg.ItemUseRequest_0b000107.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ItemUseResponse_0b000108, cn.game.protocol.protobuf.ItemMsg.ItemUseResponse_0b000108.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ItemBagSortRequest_0b000113, cn.game.protocol.protobuf.ItemMsg.ItemBagSortRequest_0b000113.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ItemBagSortResponse_0b000114, cn.game.protocol.protobuf.ItemMsg.ItemBagSortResponse_0b000114.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ItemBagGridPush_0b000320, cn.game.protocol.protobuf.ItemMsg.ItemBagGridPush_0b000320.getDefaultInstance()
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
		parsersMap.put(PlayerBuyPowerItemRequest_01000022, cn.game.protocol.protobuf.PlayerMsg.PlayerBuyPowerItemRequest_01000022.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerBuyPowerItemResponse_01000023, cn.game.protocol.protobuf.PlayerMsg.PlayerBuyPowerItemResponse_01000023.getDefaultInstance()
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
		parsersMap.put(TestStoryFinishRequest_6f000024, cn.game.protocol.protobuf.TestMsg.TestStoryFinishRequest_6f000024.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestStoryFinishResponse_6f000025, cn.game.protocol.protobuf.TestMsg.TestStoryFinishResponse_6f000025.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestMapTransmitRequest_6f000026, cn.game.protocol.protobuf.TestMsg.TestMapTransmitRequest_6f000026.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestMapTransmitResponse_6f000027, cn.game.protocol.protobuf.TestMsg.TestMapTransmitResponse_6f000027.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestExplorePosPush_6f000088, cn.game.protocol.protobuf.TestMsg.TestExplorePosPush_6f000088.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestMailRequest_6f000010, cn.game.protocol.protobuf.TestMsg.TestMailRequest_6f000010.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestMailResponse_6f000011, cn.game.protocol.protobuf.TestMsg.TestMailResponse_6f000011.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestRequest_6f000020, cn.game.protocol.protobuf.TestMsg.TestRequest_6f000020.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestResponse_6f000021, cn.game.protocol.protobuf.TestMsg.TestResponse_6f000021.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestDbRequest_6f000041, cn.game.protocol.protobuf.TestMsg.TestDbRequest_6f000041.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestDbResponse_6f000042, cn.game.protocol.protobuf.TestMsg.TestDbResponse_6f000042.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestAddExploreItemRequest_6f000030, cn.game.protocol.protobuf.TestMsg.TestAddExploreItemRequest_6f000030.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestAddExploreItemResponse_6f000031, cn.game.protocol.protobuf.TestMsg.TestAddExploreItemResponse_6f000031.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestAddOrDelBagItemRequest_6f000032, cn.game.protocol.protobuf.TestMsg.TestAddOrDelBagItemRequest_6f000032.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestAddOrDelBagItemResponse_6f000033, cn.game.protocol.protobuf.TestMsg.TestAddOrDelBagItemResponse_6f000033.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestExploreMapRequest_6f000035, cn.game.protocol.protobuf.TestMsg.TestExploreMapRequest_6f000035.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestExploreMapResponse_6f000036, cn.game.protocol.protobuf.TestMsg.TestExploreMapResponse_6f000036.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestExploreDeleteMonsterRequest_6f000037, cn.game.protocol.protobuf.TestMsg.TestExploreDeleteMonsterRequest_6f000037.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestExploreDeleteMonsterResponse_6f000038, cn.game.protocol.protobuf.TestMsg.TestExploreDeleteMonsterResponse_6f000038.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestExploreDeleteObjectRequest_6f000039, cn.game.protocol.protobuf.TestMsg.TestExploreDeleteObjectRequest_6f000039.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestExploreDeleteObjectResponse_6f00003a, cn.game.protocol.protobuf.TestMsg.TestExploreDeleteObjectResponse_6f00003a.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestExploreAddBarrierRequest_6f000050, cn.game.protocol.protobuf.TestMsg.TestExploreAddBarrierRequest_6f000050.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestExploreAddBarrierResponse_6f000051, cn.game.protocol.protobuf.TestMsg.TestExploreAddBarrierResponse_6f000051.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestExploreFixRoleHp1000Request_6f000052, cn.game.protocol.protobuf.TestMsg.TestExploreFixRoleHp1000Request_6f000052.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestExploreFixRoleHp1000Response_6f000053, cn.game.protocol.protobuf.TestMsg.TestExploreFixRoleHp1000Response_6f000053.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestMessageRequest_6f000080, cn.game.protocol.protobuf.TestMsg.TestMessageRequest_6f000080.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestMessageResponse_6f000081, cn.game.protocol.protobuf.TestMsg.TestMessageResponse_6f000081.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestAddBuffRequest_6f00003b, cn.game.protocol.protobuf.TestMsg.TestAddBuffRequest_6f00003b.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestAddBuffResponse_6f00003c, cn.game.protocol.protobuf.TestMsg.TestAddBuffResponse_6f00003c.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestEnterPhaseTwoRequest_6f000091, cn.game.protocol.protobuf.TestMsg.TestEnterPhaseTwoRequest_6f000091.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestEnterPhaseTwoResponse_6f000092, cn.game.protocol.protobuf.TestMsg.TestEnterPhaseTwoResponse_6f000092.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestExploreLevelGoalsFinishRequest_6f000093, cn.game.protocol.protobuf.TestMsg.TestExploreLevelGoalsFinishRequest_6f000093.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestExploreLevelGoalsFinishResponse_6f000094, cn.game.protocol.protobuf.TestMsg.TestExploreLevelGoalsFinishResponse_6f000094.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestCommandRequest_6f000095, cn.game.protocol.protobuf.TestMsg.TestCommandRequest_6f000095.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestCommandResponse_6f000096, cn.game.protocol.protobuf.TestMsg.TestCommandResponse_6f000096.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestRoleAttributeRequest_6f000100, cn.game.protocol.protobuf.TestMsg.TestRoleAttributeRequest_6f000100.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestRoleAttributeResponse_6f000101, cn.game.protocol.protobuf.TestMsg.TestRoleAttributeResponse_6f000101.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestGameEventTriggerRequest_6f000105, cn.game.protocol.protobuf.TestMsg.TestGameEventTriggerRequest_6f000105.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestGameEventTriggerResponse_6f000106, cn.game.protocol.protobuf.TestMsg.TestGameEventTriggerResponse_6f000106.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestGenObjectRequest_6f000107, cn.game.protocol.protobuf.TestMsg.TestGenObjectRequest_6f000107.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestGenObjectResponse_6f000108, cn.game.protocol.protobuf.TestMsg.TestGenObjectResponse_6f000108.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestStoreDiscountResponse_6f00010a, cn.game.protocol.protobuf.TestMsg.TestStoreDiscountResponse_6f00010a.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestRolePromotionRequest_6f00010b, cn.game.protocol.protobuf.TestMsg.TestRolePromotionRequest_6f00010b.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestRolePromotionResponse_6f00010c, cn.game.protocol.protobuf.TestMsg.TestRolePromotionResponse_6f00010c.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestRoleUnlockOccupationTalentNodeRequest_6f00010d, cn.game.protocol.protobuf.TestMsg.TestRoleUnlockOccupationTalentNodeRequest_6f00010d.getDefaultInstance()
				.getParserForType());
		parsersMap.put(TestRoleUnlockOccupationTalentNodeResponse_6f00010e, cn.game.protocol.protobuf.TestMsg.TestRoleUnlockOccupationTalentNodeResponse_6f00010e.getDefaultInstance()
				.getParserForType());

		nameIdMap.put("ActivityListRequest_11000001", 0x11000001);
		nameIdMap.put("ActivityListResponse_11000002", 0x11000002);
		nameIdMap.put("ActivityInfoRequest_11000003", 0x11000003);
		nameIdMap.put("ActivityInfoResponse_11000004", 0x11000004);
		nameIdMap.put("ActivityStatePush_11100006", 0x11100006);
		nameIdMap.put("FriendListRequest_30000001", 0x30000001);
		nameIdMap.put("FriendListResponse_30000002", 0x30000002);
		nameIdMap.put("FriendBlackListRequest_30000051", 0x30000051);
		nameIdMap.put("FriendBlackListResponse_30000052", 0x30000052);
		nameIdMap.put("FriendApplyListRequest_30000053", 0x30000053);
		nameIdMap.put("FriendApplyListResponse_30000054", 0x30000054);
		nameIdMap.put("FriendRecommendRequest_30000003", 0x30000003);
		nameIdMap.put("FriendRecommendResponse_30000004", 0x30000004);
		nameIdMap.put("FriendSearchRequest_30000020", 0x30000020);
		nameIdMap.put("FriendSearchResponse_30000021", 0x30000021);
		nameIdMap.put("FriendApplyRequest_30000005", 0x30000005);
		nameIdMap.put("FriendApplyResponse_30000006", 0x30000006);
		nameIdMap.put("FriendApplicationRequest_30000007", 0x30000007);
		nameIdMap.put("FriendApplicationResponse_30000008", 0x30000008);
		nameIdMap.put("FriendDeleteRequest_30000009", 0x30000009);
		nameIdMap.put("FriendDeleteResponse_3000000a", 0x3000000a);
		nameIdMap.put("FriendBlackRequest_30000010", 0x30000010);
		nameIdMap.put("FriendBlackResponse_30000011", 0x30000011);
		nameIdMap.put("FriendAttentionRequest_30000030", 0x30000030);
		nameIdMap.put("FriendAttentionResponse_30000031", 0x30000031);
		nameIdMap.put("FriendshipRequest_30000012", 0x30000012);
		nameIdMap.put("FriendshipResponse_30000013", 0x30000013);
		nameIdMap.put("FriendshipReceiveRequest_30000014", 0x30000014);
		nameIdMap.put("FriendshipReceiveResponse_30000015", 0x30000015);
		nameIdMap.put("FriendGiftRequest_30000016", 0x30000016);
		nameIdMap.put("FriendGiftResponse_30000017", 0x30000017);
		nameIdMap.put("FriendApplyPush_30000022", 0x30000022);
		nameIdMap.put("FriendAddPush_30000023", 0x30000023);
		nameIdMap.put("FriendDelPush_30000024", 0x30000024);
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
		nameIdMap.put("DeprecatedItemSaleRequest_0b000001", 0x0b000001);
		nameIdMap.put("DeprecatedItemSaleResponse_0b000002", 0x0b000002);
		nameIdMap.put("DeprecatedItemUseRequest_0b000003", 0x0b000003);
		nameIdMap.put("DeprecatedItemUseResponse_0b000004", 0x0b000004);
		nameIdMap.put("DecomposeRequest_0b000005", 0x0b000005);
		nameIdMap.put("DecomposeResponse_0b000006", 0x0b000006);
		nameIdMap.put("EquipLockRequest_0b000007", 0x0b000007);
		nameIdMap.put("EquipLockResponse_0b000008", 0x0b000008);
		nameIdMap.put("ItemDiscardRequest_0b000103", 0x0b000103);
		nameIdMap.put("ItemDiscardResponse_0b000104", 0x0b000104);
		nameIdMap.put("ItemUseRequest_0b000107", 0x0b000107);
		nameIdMap.put("ItemUseResponse_0b000108", 0x0b000108);
		nameIdMap.put("ItemBagSortRequest_0b000113", 0x0b000113);
		nameIdMap.put("ItemBagSortResponse_0b000114", 0x0b000114);
		nameIdMap.put("ItemBagGridPush_0b000320", 0x0b000320);
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
		nameIdMap.put("PlayerBuyPowerItemRequest_01000022", 0x01000022);
		nameIdMap.put("PlayerBuyPowerItemResponse_01000023", 0x01000023);
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
		nameIdMap.put("AdvertiseWatchFinishRequest_15000030", 0x15000030);
		nameIdMap.put("AdvertiseWatchFinishResponse_15000031", 0x15000031);
		nameIdMap.put("TestGmCmdRequest_6f000001", 0x6f000001);
		nameIdMap.put("TestGmCmdResponse_6f000002", 0x6f000002);
		nameIdMap.put("TestAddItemRequest_6f000008", 0x6f000008);
		nameIdMap.put("TestAddItemResponse_6f000009", 0x6f000009);
		nameIdMap.put("TestMissionFinishRequest_6f000022", 0x6f000022);
		nameIdMap.put("TestMissionFinishResponse_6f000023", 0x6f000023);
		nameIdMap.put("TestStoryFinishRequest_6f000024", 0x6f000024);
		nameIdMap.put("TestStoryFinishResponse_6f000025", 0x6f000025);
		nameIdMap.put("TestMapTransmitRequest_6f000026", 0x6f000026);
		nameIdMap.put("TestMapTransmitResponse_6f000027", 0x6f000027);
		nameIdMap.put("TestExplorePosPush_6f000088", 0x6f000088);
		nameIdMap.put("TestMailRequest_6f000010", 0x6f000010);
		nameIdMap.put("TestMailResponse_6f000011", 0x6f000011);
		nameIdMap.put("TestRequest_6f000020", 0x6f000020);
		nameIdMap.put("TestResponse_6f000021", 0x6f000021);
		nameIdMap.put("TestDbRequest_6f000041", 0x6f000041);
		nameIdMap.put("TestDbResponse_6f000042", 0x6f000042);
		nameIdMap.put("TestAddExploreItemRequest_6f000030", 0x6f000030);
		nameIdMap.put("TestAddExploreItemResponse_6f000031", 0x6f000031);
		nameIdMap.put("TestAddOrDelBagItemRequest_6f000032", 0x6f000032);
		nameIdMap.put("TestAddOrDelBagItemResponse_6f000033", 0x6f000033);
		nameIdMap.put("TestExploreMapRequest_6f000035", 0x6f000035);
		nameIdMap.put("TestExploreMapResponse_6f000036", 0x6f000036);
		nameIdMap.put("TestExploreDeleteMonsterRequest_6f000037", 0x6f000037);
		nameIdMap.put("TestExploreDeleteMonsterResponse_6f000038", 0x6f000038);
		nameIdMap.put("TestExploreDeleteObjectRequest_6f000039", 0x6f000039);
		nameIdMap.put("TestExploreDeleteObjectResponse_6f00003a", 0x6f00003a);
		nameIdMap.put("TestExploreAddBarrierRequest_6f000050", 0x6f000050);
		nameIdMap.put("TestExploreAddBarrierResponse_6f000051", 0x6f000051);
		nameIdMap.put("TestExploreFixRoleHp1000Request_6f000052", 0x6f000052);
		nameIdMap.put("TestExploreFixRoleHp1000Response_6f000053", 0x6f000053);
		nameIdMap.put("TestMessageRequest_6f000080", 0x6f000080);
		nameIdMap.put("TestMessageResponse_6f000081", 0x6f000081);
		nameIdMap.put("TestAddBuffRequest_6f00003b", 0x6f00003b);
		nameIdMap.put("TestAddBuffResponse_6f00003c", 0x6f00003c);
		nameIdMap.put("TestEnterPhaseTwoRequest_6f000091", 0x6f000091);
		nameIdMap.put("TestEnterPhaseTwoResponse_6f000092", 0x6f000092);
		nameIdMap.put("TestExploreLevelGoalsFinishRequest_6f000093", 0x6f000093);
		nameIdMap.put("TestExploreLevelGoalsFinishResponse_6f000094", 0x6f000094);
		nameIdMap.put("TestCommandRequest_6f000095", 0x6f000095);
		nameIdMap.put("TestCommandResponse_6f000096", 0x6f000096);
		nameIdMap.put("TestRoleAttributeRequest_6f000100", 0x6f000100);
		nameIdMap.put("TestRoleAttributeResponse_6f000101", 0x6f000101);
		nameIdMap.put("TestGameEventTriggerRequest_6f000105", 0x6f000105);
		nameIdMap.put("TestGameEventTriggerResponse_6f000106", 0x6f000106);
		nameIdMap.put("TestGenObjectRequest_6f000107", 0x6f000107);
		nameIdMap.put("TestGenObjectResponse_6f000108", 0x6f000108);
		nameIdMap.put("TestStoreDiscountResponse_6f00010a", 0x6f00010a);
		nameIdMap.put("TestRolePromotionRequest_6f00010b", 0x6f00010b);
		nameIdMap.put("TestRolePromotionResponse_6f00010c", 0x6f00010c);
		nameIdMap.put("TestRoleUnlockOccupationTalentNodeRequest_6f00010d", 0x6f00010d);
		nameIdMap.put("TestRoleUnlockOccupationTalentNodeResponse_6f00010e", 0x6f00010e);
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
