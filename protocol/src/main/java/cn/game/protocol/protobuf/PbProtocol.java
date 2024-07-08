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

	public final static int ActivityListRequest_11000001 = 0x11000001;    //查看有哪些显示的活动  
	public final static int ActivityListResponse_11000002 = 0x11000002;    //显示的活动列表  
	public final static int ActivityStatePush_11100006 = 0x11100006;    //活动状态改变推送，看具体情况选择性推送  
	public final static int ActivityFirstChargeRequest_11000007 = 0x11000007;    //查看首冲活动数据  
	public final static int ActivityFirstChargeResponse_11000008 = 0x11000008;    //首冲活动  
	public final static int ActivityFirstChargeBuyRequest_11000010 = 0x11000010;    //购买首冲礼包，只能购买有售价的礼包。  
	public final static int ActivityFirstChargeBuyResponse_11000011 = 0x11000011;    
	public final static int ActivityFirstChargeRewardRequest_11000012 = 0x11000012;    //领取首冲礼包奖励  
	public final static int ActivityFirstChargeRewardResponse_11000013 = 0x11000013;    
	public final static int ActivitySevenDaysCarnivalRequest_11000020 = 0x11000020;    //七日狂欢  
	public final static int ActivitySevenDaysCarnivalResponse_11000021 = 0x11000021;    
	public final static int ActivitySevenDaysSigninInfoRequest_11000024 = 0x11000024;    //七日签到,查看七日签到数据  
	public final static int ActivitySevenDaysSigninInfoResponse_11000025 = 0x11000025;    
	public final static int ActivitySevenDaysSigninRequest_11000026 = 0x11000026;    //七日签到,签到领奖  
	public final static int ActivitySevenDaysSigninResponse_11000027 = 0x11000027;    
	public final static int BattleFieldStartRequest_13000001 = 0x13000001;    //开始关卡战斗请求  
	public final static int BattleFieldStartResponse_13000002 = 0x13000002;    //开始关卡战斗返回  
	public final static int BattleFieldEndRequest_13000003 = 0x13000003;    //结束关卡战斗请求  
	public final static int BattleFieldEndResponse_13000004 = 0x13000004;    //结束关卡战斗  
	public final static int BattleShareRequest_13000007 = 0x13000007;    //看广告并分享之后额外获得xx倍奖励  
	public final static int BattleShareResponse_13000008 = 0x13000008;    //看广告并分享之后额外获得xx倍奖励  
	public final static int BattleRewardRequest_13000022 = 0x13000022;    //领取战役  首次胜利奖励	首次半血胜利奖励	首次无损胜利奖励,可以一次领多个战役的。  
	public final static int BattleRewardResponse_13000023 = 0x13000023;    
	public final static int HCBattleRewardRequest_13000027 = 0x13000027;    //合成领取战役  首次胜利奖励	首次半血胜利奖励	首次无损胜利奖励,可以一次领多个战役的。  
	public final static int HCBattleRewardResponse_13000028 = 0x13000028;    
	public final static int BattleSweepRequest_13000024 = 0x13000024;    //关卡扫荡  
	public final static int BattleSweepResponse_13000025 = 0x13000025;    
	public final static int HCBattleSweepRequest_13000040 = 0x13000040;    //合成关卡扫荡  
	public final static int HCBattleSweepResponse_13000041 = 0x13000041;    
	public final static int BattlePatrolRewardRequest_13000044 = 0x13000044;    //领取巡逻奖励  
	public final static int BattlePatrolRewardResponse_13000045 = 0x13000045;    
	public final static int BattleDaoHeartRequest_13000055 = 0x13000055;    //查看道心磨砺 心魔试炼数据  
	public final static int BattleDaoHeartResponse_13000056 = 0x13000056;    
	public final static int BattleDaoHeartSweepRequest_13000060 = 0x13000060;    //扫荡道心磨砺 心魔试炼关卡  
	public final static int BattleDaoHeartSweepResponse_13000061 = 0x13000061;    
	public final static int BattleDaoHeartSweepBatchRequest_13000062 = 0x13000062;    //一键扫荡道心磨砺 心魔试炼关卡  
	public final static int BattleDaoHeartSweepBatchResponse_13000063 = 0x13000063;    
	public final static int BattleDaoHeartSweepRequest_13000064 = 0x13000064;    //查看道心磨砺 心魔试炼通关奖励领取情况  
	public final static int BattleDaoHeartSweepResponse_13000065 = 0x13000065;    
	public final static int BattleDaoHeartSweepRequest_13000066 = 0x13000066;    //领取道心磨砺 心魔试炼关卡通关奖励  
	public final static int BattleDaoHeartSweepResponse_13000067 = 0x13000067;    
	public final static int BattleDayChallengeReceiveActivePointRequest_13000070 = 0x13000070;    //领取每日挑战活跃积分奖励  
	public final static int BattleDayChallengeReceiveActivePointResponse_13000071 = 0x13000071;    
	public final static int BattleRougeRefreshRequest_13000005 = 0x13000005;    //肉鸽刷新,每天前3次免费，第四次看广告。  
	public final static int BattleRougeRefreshResponse_13000006 = 0x13000006;    //肉鸽刷新  
	public final static int BattleStaminaRequest_13000050 = 0x13000050;    //领取体力  
	public final static int BattleStaminaResponse_13000051 = 0x13000051;    
	public final static int ChatRequest_31000001 = 0x31000001;    //请求聊天  
	public final static int ChatResponse_31000002 = 0x31000002;    //请求聊天返回  
	public final static int ChatMessagePush_31010001 = 0x31010001;    //聊天消息推送  
	public final static int ServerChatMessagePush_31000010 = 0x31000010;    //只服务器使用。  
	public final static int DevelopPotentialLvUpRequest_25000001 = 0x25000001;    //潜力修炼，升级  
	public final static int DevelopPotentialLvUpResponse_25000002 = 0x25000002;    
	public final static int DevelopPotentialBreakRequest_25000003 = 0x25000003;    //潜力修炼，突破  
	public final static int DevelopPotentialBreakResponse_25000004 = 0x25000004;    
	public final static int DevelopRescueLvUpRequest_25000007 = 0x25000007;    //强援修炼，升级  
	public final static int DevelopRescueLvUpResponse_25000008 = 0x25000008;    
	public final static int DevelopHeavenlyDaoLvUpRequest_25000010 = 0x25000010;    //天道修为晋升，提升等级。  
	public final static int DevelopHeavenlyDaoLvUpResponse_25000011 = 0x25000011;    
	public final static int DragonUnlockRequest_17000001 = 0x17000001;    //龙解锁  
	public final static int DragonUnlockResponse_17000002 = 0x17000002;    
	public final static int DragonStarUpRequest_17000003 = 0x17000003;    //龙升星  
	public final static int DragonStarUpResponse_17000004 = 0x17000004;    
	public final static int DragonSkillUpRequest_17000005 = 0x17000005;    //龙技能升级  
	public final static int DragonSkillUpResponse_17000006 = 0x17000006;    
	public final static int DrawListRequest_37000001 = 0x37000001;    //请求卡池界面数据  
	public final static int DrawListResponse_37000002 = 0x37000002;    //响应卡池界面  
	public final static int DrawRequest_37000003 = 0x37000003;    //请求抽卡  
	public final static int DrawResponse_37000004 = 0x37000004;    //抽卡结果  
	public final static int EquipmentWearRequest_09000001 = 0x09000001;    //装备 替换  
	public final static int EquipmentWearResponse_09000002 = 0x09000002;    
	public final static int EquipmentTeardownRequest_09000003 = 0x09000003;    //装备卸下  
	public final static int EquipmentTeardownResponse_09000004 = 0x09000004;    
	public final static int EquipmentPartStrengthRequest_09000007 = 0x09000007;    //装备部位强化  
	public final static int EquipmentPartStrengthResponse_09000008 = 0x09000008;    
	public final static int EquipmentPartBreakthroughRequest_09000011 = 0x09000011;    //装备部位突破  
	public final static int EquipmentPartBreakthroughResponse_09000012 = 0x09000012;    
	public final static int SwordStarUpRequest_09000013 = 0x09000013;    //武器升星  
	public final static int SwordStarUpResponse_09000014 = 0x09000014;    
	public final static int FashionStarUpRequest_09000015 = 0x09000015;    //时装升星  
	public final static int FashionStarUpResponse_09000016 = 0x09000016;    
	public final static int GemWearRequest_10000001 = 0x10000001;    //宝石镶嵌 替换  
	public final static int GemWearResponse_10000002 = 0x10000002;    
	public final static int GemTeardownRequest_10000003 = 0x10000003;    //宝石卸下  
	public final static int GemTeardownResponse_10000004 = 0x10000004;    
	public final static int GemLockRequest_10000005 = 0x10000005;    //宝石锁定  
	public final static int GemLockResponse_10000006 = 0x10000006;    
	public final static int GemComposeRequest_10000007 = 0x10000007;    //宝石合成 一键合成  
	public final static int GemComposeResponse_10000008 = 0x10000008;    
	public final static int GmPlayerRequest_77000021 = 0x77000021;    //查看某玩家数据，优先id，如果id没有通过name查询。  
	public final static int GmPlayerResponse_77000022 = 0x77000022;    //查看玩家数据响应  
	public final static int GmOrderRequest_77000025 = 0x77000025;    //查询订单,哪个参数有值用哪个查，如果都没有值  
	public final static int GmOrderResponse_77000026 = 0x77000026;    //订单响应。  
	public final static int GmOrderReimburseRequest_77000027 = 0x77000027;    //补单  
	public final static int GmOrderReimburseResponse_77000028 = 0x77000028;    //补单响应。  
	public final static int GmMailPlayerSendRequest_77000040 = 0x77000040;    //gm给指定玩家发邮件  
	public final static int GmMailPlayerSendResponse_77000041 = 0x77000041;    
	public final static int GmMailServerSendRequest_77000048 = 0x77000048;    //gm给指定服务器发邮件  
	public final static int GmMailServerSendResponse_77000049 = 0x77000049;    
	public final static int GmMailListRequest_77000042 = 0x77000042;    //查看邮件列表  
	public final static int GmMailResponse_77000043 = 0x77000043;    
	public final static int GmMailCheckRequest_77000044 = 0x77000044;    //审核邮件  
	public final static int GmMailCheckResponse_77000045 = 0x77000045;    
	public final static int GmMailDeleteRequest_77000046 = 0x77000046;    //删除邮件  
	public final static int GmMailDeleteResponse_77000047 = 0x77000047;    
	public final static int GmNoticeAddRequest_77000060 = 0x77000060;    //增加公告  
	public final static int GmNoticeAddResponse_77000061 = 0x77000061;    
	public final static int GmNoticeUpdateRequest_77000062 = 0x77000062;    //更新公告  
	public final static int GmNoticeUpdateResponse_77000063 = 0x77000063;    
	public final static int GmNoticeDeleteRequest_77000064 = 0x77000064;    //删除公告  
	public final static int GmNoticeDeleteResponse_77000065 = 0x77000065;    
	public final static int GmNoticeListRequest_77000066 = 0x77000066;    //查看公告列表  
	public final static int GmNoticeListResponse_77000067 = 0x77000067;    
	public final static int GmShutdownServerRequest_77000001 = 0x77000001;    //关闭服务器  
	public final static int GmShutdownServerResponse_77000002 = 0x77000002;    //关闭服务器响应  
	public final static int GmAccountForbidListRequest_77000003 = 0x77000003;    //请求封号列表  
	public final static int GmAccountForbidListResponse_77000004 = 0x77000004;    //响应封号列表  
	public final static int GmAccountForbidRequest_77000005 = 0x77000005;    //请求封号  
	public final static int GmAccountForbidResponse_77000006 = 0x77000006;    //响应封号  
	public final static int GmAccountUnblockRequest_77000007 = 0x77000007;    //请求解封  
	public final static int GmAccountUnblockResponse_77000008 = 0x77000008;    //响应解封  
	public final static int GmOperationRequest_77000071 = 0x77000071;    //请求gm操作记录列表  
	public final static int GmOperationResponse_77000072 = 0x77000072;    //gm操作记录  
	public final static int GmPlayerLogoutRequest_77000009 = 0x77000009;    //请求踢下线  
	public final static int GmPlayerLogouttResponse_7700000a = 0x7700000a;    //响应踢下线  
	public final static int GmPlayerMailRequest_77000010 = 0x77000010;    //gm给指定玩家发邮件,这个协议不用了。  
	public final static int GmPlayerMailResponse_77000011 = 0x77000011;    
	public final static int HCBattleSpeedAdsRequest_28000020 = 0x28000020;    //看广告获得战中倍速  
	public final static int HCBattleSpeedAdsResponse_28000021 = 0x28000021;    
	public final static int HCHeroUpLevelRequest_26000001 = 0x26000001;    //英雄升级  
	public final static int HCHeroUpLevelResponse_26000002 = 0x26000002;    
	public final static int HCHeroStarUpRequest_26000003 = 0x26000003;    //英雄升星，也就是觉醒  
	public final static int HCHeroStarUpResponse_26000004 = 0x26000004;    
	public final static int HCHeroBattleRequest_26000005 = 0x26000005;    //切换使用的英雄  
	public final static int HCHeroBattleResponse_26000006 = 0x26000006;    
	public final static int HCHeroCompositeRequest_26000007 = 0x26000007;    //使用碎片合成英雄  
	public final static int HCHeroCompositeResponse_26000008 = 0x26000008;    
	public final static int HCHeroAdsRequest_26000009 = 0x26000009;    //看广告获得英雄碎片  
	public final static int HCHeroAdsResponse_2600000a = 0x2600000a;    
	public final static int HeroUpLevelRequest_16000001 = 0x16000001;    //英雄升级  
	public final static int HeroUpLevelResponse_16000002 = 0x16000002;    
	public final static int HeroUpLevelMaxRequest_16000021 = 0x16000021;    //英雄一键升级,自动升级到最高级。  
	public final static int HeroUpLevelMaxResponse_16000022 = 0x16000022;    
	public final static int HeroUpLevelBatchRequest_16000023 = 0x16000023;    //批量英雄升级，能升级哪个升级哪个  
	public final static int HeroUpLevelBatchResponse_16000024 = 0x16000024;    
	public final static int HeroConflateRequest_16000003 = 0x16000003;    //英雄合成,也就是升星，突破。当前品质下可以升星，星级升满之后进行突破，改变品质。  
	public final static int HeroConflateResponse_16000004 = 0x16000004;    //合成返回,客户端这里自己把相关的英雄、万能耗材扣掉。  
	public final static int HeroBattleRequest_16000005 = 0x16000005;    //英雄上阵  
	public final static int HeroBattleResponse_16000006 = 0x16000006;    
	public final static int HeroBattleDismissRequest_16000009 = 0x16000009;    //英雄下阵  
	public final static int HeroBattleDismissResponse_1600000a = 0x1600000a;    
	public final static int HeroLevelResetRequest_16000007 = 0x16000007;    //英雄等级重置,返还升级材料  
	public final static int HeroLevelResetResponse_16000008 = 0x16000008;    //客户端看看自己读表返还升级的材料  
	public final static int HeroQualityResetRequest_16000011 = 0x16000011;    //英雄品质重置  
	public final static int HeroQualityResetResponse_16000012 = 0x16000012;    
	public final static int HeroFreeDayRentRequest_16000030 = 0x16000030;    //请求免费英雄日租卡，这个只有在功能开启的时候请求一下，不能重复请求  
	public final static int HeroFreeDayRentResponse_16000031 = 0x16000031;    
	public final static int HeroFreeDayRentChooseRequest_16000032 = 0x16000032;    //选择某个英雄日租卡。  
	public final static int HeroFreeDayRentChooseResponse_16000033 = 0x16000033;    
	public final static int HeroIllustrationsListRequest_16000040 = 0x16000040;    //查看图鉴数据  
	public final static int HeroIllustrationsListResponse_16000041 = 0x16000041;    
	public final static int HeroIllustrationsRewardRequest_16000042 = 0x16000042;    //一键领取英雄的图鉴奖励  
	public final static int HeroIllustrationsRewardResponse_16000043 = 0x16000043;    
	public final static int ItemUseRequest_0b000003 = 0x0b000003;    //使用道具  
	public final static int ItemUseResponse_0b000004 = 0x0b000004;    //使用道具后可能会给资源，道具等。  
	public final static int MailListRequest_12000001 = 0x12000001;    //请求邮件列表。  
	public final static int MailListResponse_12000002 = 0x12000002;    //邮件列表数据  
	public final static int MailSeeRequest_12000003 = 0x12000003;    //查看未读邮件  
	public final static int MailSeeResponse_12000004 = 0x12000004;    //  
	public final static int MailReceiveRequest_12000005 = 0x12000005;    //领取邮件奖励  
	public final static int MailReceiveResponse_12000006 = 0x12000006;    //领取邮件奖励返回  
	public final static int MailDeleteRequest_12000007 = 0x12000007;    //删除邮件  
	public final static int MailDeleteResponse_12000008 = 0x12000008;    //删除邮件返回  
	public final static int MailNewPush_12010001 = 0x12010001;    //新邮件推送  
	public final static int MergeEquipmentWearRequest_23000001 = 0x23000001;    //装备 替换  
	public final static int MergeEquipmentWearResponse_23000002 = 0x23000002;    
	public final static int MergeEquipmentTeardownRequest_23000003 = 0x23000003;    //装备卸下  
	public final static int MergeEquipmentTeardownResponse_23000004 = 0x23000004;    
	public final static int MergeEquipmentPartStrengthRequest_23000007 = 0x23000007;    //装备升级  
	public final static int MergeEquipmentPartStrengthResponse_23000008 = 0x23000008;    
	public final static int PlayerLoginRequest_01000001 = 0x01000001;    //登陆  
	public final static int PlayerLoginResponse_01000002 = 0x01000002;    //用户登陆,返回游戏数据  
	public final static int PlayerLogoutRequest_01000003 = 0x01000003;    //退出登陆  
	public final static int PlayerLogoutResponse_01000004 = 0x01000004;    //退出登陆  
	public final static int PlayerLogoutPush_01100030 = 0x01100030;    //退出登录推送(客户端登录多个账号，或者服务器关闭等)  
	public final static int PlayerNameRequest_01000011 = 0x01000011;    //修改名字  
	public final static int PlayerNameResponse_01000012 = 0x01000012;    //修改名字返回  
	public final static int PlayerHeadRequest_01000013 = 0x01000013;    //修改头像  
	public final static int PlayerHeadResponse_01000014 = 0x01000014;    //修改头像返回  
	public final static int PlayerHeadFrameRequest_01000015 = 0x01000015;    //修改头像框  
	public final static int PlayerHeadFrameResponse_01000016 = 0x01000016;    //修改头像框返回  
	public final static int PlayerGenderRequest_01000017 = 0x01000017;    //修改性别  
	public final static int PlayerGenderResponse_01000018 = 0x01000018;    //修改性别返回  
	public final static int PlayerReconnecRequest_01000065 = 0x01000065;    //断线重连，和手机端通用  
	public final static int PlayerReconnecResponse_01000066 = 0x01000066;    //断线重连，和手机端通用  
	public final static int PlayerHeartbeatRequest_01000005 = 0x01000005;    //心跳  
	public final static int PlayerHeartbeatResponse_01000006 = 0x01000006;    //心跳返回  
	public final static int PlayerErrorPush_01000099 = 0x01000099;    //对于客户端的请求，如果服务器处理过程中出现未知异常，导致没有返回对应的响应包时，返回此错误消息  
	public final static int PlayerCloudBoxPush_01100040 = 0x01100040;    //产生小云宝箱 推送  
	public final static int PlayerCloudBoxRequest_01000042 = 0x01000042;    //领取小云宝箱奖励  
	public final static int PlayerCloudBoxResponse_01000043 = 0x01000043;    
	public final static int PlayerExpLevelPush_01100050 = 0x01100050;    //如果经验和等级有变化的时候，推送这个协议，客户端用这个数据覆盖本地数据。  
	public final static int PlayerGuideRequest_01000060 = 0x01000060;    //保存新手引导步骤  
	public final static int PlayerGuideResponse_01000061 = 0x01000061;    
	public final static int PlayerBriefInfoRequest_01000007 = 0x01000007;    //获取一组玩家简略信息 本服  
	public final static int PlayerBriefInfoResponse_01000008 = 0x01000008;    
	public final static int PlayerBriefInfoOtherRequest_01000009 = 0x01000009;    //获取一组玩家简略信息 查询本服和跨服  
	public final static int PlayerBriefInfoOtherResponse_0100000a = 0x0100000a;    
	public final static int PlayerShowRequest_01000039 = 0x01000039;    //获取一个玩家的名片  
	public final static int PlayerShowResponse_0100003a = 0x0100003a;    
	public final static int PlayerAlchemyRequest_01000040 = 0x01000040;    //炼金请求  
	public final static int PlayerAlchemyResponse_01000041 = 0x01000041;    
	public final static int PlayerResetPush_01100016 = 0x01100016;    //通知客户端，重置一些本地的数据，大多是一些简单次数之类，对于复杂数据的刷新，通过单独定义协议，服务端推送来刷新  
	public final static int NoticeRequest_01000050 = 0x01000050;    //请求公告数据  
	public final static int NoticeResponse_01000051 = 0x01000051;    //公告内容  
	public final static int QuestListRequest_20000001 = 0x20000001;    //查看某类任务数据,一般在任务功能开启时，客户端请求一下。  
	public final static int QuestListResponse_20000002 = 0x20000002;    //任务数据  
	public final static int QuestReceiveRequest_20000004 = 0x20000004;    //领取任务奖励  
	public final static int QuestReceiveResponse_20000005 = 0x20000005;    
	public final static int QuestReceiveActivePointRequest_20000008 = 0x20000008;    //领取任务活跃积分奖励  
	public final static int QuestReceiveActivePointResponse_20000009 = 0x20000009;    
	public final static int QuestGroupPush_20100008 = 0x20100008;    //一组任务状态变化通知，一般是有任务可以领奖时，或者加入了新的任务，会推送这个协议  
	public final static int QuestPush_20200008 = 0x20200008;    //单个任务状态变化通知，一般是任务有变化，或者加入 删除了任务等，会推送这个协议  
	public final static int QuestListAllRequest_20000051 = 0x20000051;    //请求所有任务数据，一般在任务功能开启的时候请求一下。  
	public final static int QuestListAllResponse_20000052 = 0x20000052;    //任务数据  
	public final static int QuestChallengeGroupRequest_20000020 = 0x20000020;    //查看某类型挑战组任务----暂时用不到  
	public final static int QuestChallengeGroupResponse_20000021 = 0x20000021;    
	public final static int QuestChallengeGroupDetailRequest_20000022 = 0x20000022;    //查看一组任务里的任务数据 ----暂时用不到  
	public final static int QuestChallengeGroupDetailResponse_20000023 = 0x20000023;    
	public final static int QuestConditionCompletePush_20500001 = 0x20500001;    //一个任务条件完成的时候，推送此协议 ----暂时用不到  
	public final static int QuestRewardPush_20600008 = 0x20600008;    //对于自动交付（领奖）的任务，服务端领奖之后会推送此协议，也代表此任务完成，客户端根据此协议判断是否删除任务  ----暂时用不到  
	public final static int QuestAcceptRequest_20000026 = 0x20000026;    //接取任务请求，一般是和npc，或者特殊的交互物触发   ----暂时用不到  
	public final static int QuestAcceptResponse_20000027 = 0x20000027;    
	public final static int QuestBranchPriorityRequest_20000028 = 0x20000028;    //设置优先显示的支线组  ----暂时用不到  
	public final static int QuestBranchPriorityResponse_20000029 = 0x20000029;    
	public final static int QuestBranchPriorityPush_20300000 = 0x20300000;    //推送优先显示的支线组  
	public final static int QuestUpdateRequest_20000030 = 0x20000030;    //增加任务条件进度数据，客户端发起  
	public final static int QuestUpdateResponse_20000031 = 0x20000031;    
	public final static int QuestChooseRewardRequest_20000033 = 0x20000033;    //领取任务奖励,一般是多个任务奖励，选择其中的一个奖励领取  
	public final static int QuestChooseRewardResponse_20000034 = 0x20000034;    
	public final static int RewardPush_55000501 = 0x55000501;    //奖励推送,客户端收到这个协议之后，将奖励增加到本地。  
	public final static int SpendPush_55001501 = 0x55001501;    //消耗推送，客户端收到这个协议之后，减少本地的物品  
	public final static int RewardShowPush_55002501 = 0x55002501;    //一般需要显示推送获得的奖励时，看情况使用。  
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
	public final static int GameGmPlayerInfoRequest_7d000050 = 0x7d000050;    //请求玩家gm显示数据  
	public final static int GameGmPlayerInfoResponse_7d000051 = 0x7d000051;    
	public final static int GameCrossForwardPush_7d000002 = 0x7d000002;    //Game向Cross发数据，请求转发消息给指定玩家  
	public final static int CrossGameForwardPush_7d000003 = 0x7d000003;    //Game收到Cross推送，将里面的具体消息发给指定的玩家  
	public final static int GameCrossPlayerBroadcast_7d000005 = 0x7d000005;    //Game向Cross发数据，将数据包广播给不同Game服务器的玩家  
	public final static int GameCrossBroadcast_7d000008 = 0x7d000008;    //Game向Cross发数据，将里面的消息广播给指定Game，或者所有Game  
	public final static int GameDataPush_7d00000a = 0x7d00000a;    //Game向Data发数据，执行数据库操作  
	public final static int GameDataPushBatch_7d00000b = 0x7d00000b;    //Game向Data发数据，执行数据库操作  
	public final static int GameDataPushBatch2_7d00000c = 0x7d00000c;    
	public final static int ShopItemListRequest_15000001 = 0x15000001;    //查看商店里面的商品列表  
	public final static int ShopItemListResponse_15000002 = 0x15000002;    
	public final static int ShopItemBuyRequest_15000003 = 0x15000003;    //购买商品  
	public final static int ShopItemBuyResponse_15000004 = 0x15000004;    
	public final static int ShopHeishiRefreshRequest_15000005 = 0x15000005;    //手动刷新黑市请求  
	public final static int ShopHeishiRefreshResponse_15000006 = 0x15000006;    
	public final static int MonthCardBuyRequest_15000010 = 0x15000010;    //购买月卡  
	public final static int MonthCardBuyResponse_15000011 = 0x15000011;    
	public final static int MonthCardBuyRewardRequest_15000012 = 0x15000012;    //领取月卡购买奖励，每个月卡只能领取一次  
	public final static int MonthCardBuyRewardResponse_15000013 = 0x15000013;    
	public final static int MonthCardDayRewardRequest_15000014 = 0x15000014;    //领取月卡每日奖励  
	public final static int MonthCardDayRewardResponse_15000015 = 0x15000015;    
	public final static int MonthCardDoubleBonusRequest_15000016 = 0x15000016;    //领取双月卡一次性奖励  
	public final static int MonthCardDoubleBonusResponse_15000017 = 0x15000017;    
	public final static int ShopChapterPacksBuyRequest_15000020 = 0x15000020;    //购买章节礼包  
	public final static int ShopChapterPacksBuyResponse_15000021 = 0x15000021;    
	public final static int ShopRechargeRequest_15000022 = 0x15000022;    //充值  
	public final static int ShopRechargeResponse_15000023 = 0x15000023;    
	public final static int PaymentOrderPush_15010020 = 0x15010020;    //支付订单相关参数，客户端收到这个协议就可以利用里面的参数发起支付了  
	public final static int ShopFundPassBuyRequest_15000030 = 0x15000030;    //购买通行证  
	public final static int ShopFundPassBuyResponse_15000031 = 0x15000031;    
	public final static int ShopFundPassRewardRequest_15000032 = 0x15000032;    //领取通行证奖励  
	public final static int ShopFundPassRewardResponse_15000033 = 0x15000033;    
	public final static int ShopBoxOpenRequest_15000040 = 0x15000040;    //==================================开宝箱===============================  
	public final static int ShopBoxOpenResponse_15000041 = 0x15000041;    
	public final static int StoryStartRequest_14000001 = 0x14000001;    //剧情开始  
	public final static int StoryStartResponse_14000002 = 0x14000002;    
	public final static int StoryFinishRequest_14000003 = 0x14000003;    //剧情结束  
	public final static int StoryFinishResponse_14000004 = 0x14000004;    
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
		parsersMap.put(BattleShareRequest_13000007, cn.game.protocol.protobuf.BattleMsg.BattleShareRequest_13000007.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleShareResponse_13000008, cn.game.protocol.protobuf.BattleMsg.BattleShareResponse_13000008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleRewardRequest_13000022, cn.game.protocol.protobuf.BattleMsg.BattleRewardRequest_13000022.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleRewardResponse_13000023, cn.game.protocol.protobuf.BattleMsg.BattleRewardResponse_13000023.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HCBattleRewardRequest_13000027, cn.game.protocol.protobuf.BattleMsg.HCBattleRewardRequest_13000027.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HCBattleRewardResponse_13000028, cn.game.protocol.protobuf.BattleMsg.HCBattleRewardResponse_13000028.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleSweepRequest_13000024, cn.game.protocol.protobuf.BattleMsg.BattleSweepRequest_13000024.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleSweepResponse_13000025, cn.game.protocol.protobuf.BattleMsg.BattleSweepResponse_13000025.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HCBattleSweepRequest_13000040, cn.game.protocol.protobuf.BattleMsg.HCBattleSweepRequest_13000040.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HCBattleSweepResponse_13000041, cn.game.protocol.protobuf.BattleMsg.HCBattleSweepResponse_13000041.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattlePatrolRewardRequest_13000044, cn.game.protocol.protobuf.BattleMsg.BattlePatrolRewardRequest_13000044.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattlePatrolRewardResponse_13000045, cn.game.protocol.protobuf.BattleMsg.BattlePatrolRewardResponse_13000045.getDefaultInstance()
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
		parsersMap.put(BattleRougeRefreshRequest_13000005, cn.game.protocol.protobuf.BattleMsg.BattleRougeRefreshRequest_13000005.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleRougeRefreshResponse_13000006, cn.game.protocol.protobuf.BattleMsg.BattleRougeRefreshResponse_13000006.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleStaminaRequest_13000050, cn.game.protocol.protobuf.BattleMsg.BattleStaminaRequest_13000050.getDefaultInstance()
				.getParserForType());
		parsersMap.put(BattleStaminaResponse_13000051, cn.game.protocol.protobuf.BattleMsg.BattleStaminaResponse_13000051.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ChatRequest_31000001, cn.game.protocol.protobuf.ChatMsg.ChatRequest_31000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ChatResponse_31000002, cn.game.protocol.protobuf.ChatMsg.ChatResponse_31000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ChatMessagePush_31010001, cn.game.protocol.protobuf.ChatMsg.ChatMessagePush_31010001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(ServerChatMessagePush_31000010, cn.game.protocol.protobuf.ChatMsg.ServerChatMessagePush_31000010.getDefaultInstance()
				.getParserForType());
		parsersMap.put(DevelopPotentialLvUpRequest_25000001, cn.game.protocol.protobuf.DevelopMsg.DevelopPotentialLvUpRequest_25000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(DevelopPotentialLvUpResponse_25000002, cn.game.protocol.protobuf.DevelopMsg.DevelopPotentialLvUpResponse_25000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(DevelopPotentialBreakRequest_25000003, cn.game.protocol.protobuf.DevelopMsg.DevelopPotentialBreakRequest_25000003.getDefaultInstance()
				.getParserForType());
		parsersMap.put(DevelopPotentialBreakResponse_25000004, cn.game.protocol.protobuf.DevelopMsg.DevelopPotentialBreakResponse_25000004.getDefaultInstance()
				.getParserForType());
		parsersMap.put(DevelopRescueLvUpRequest_25000007, cn.game.protocol.protobuf.DevelopMsg.DevelopRescueLvUpRequest_25000007.getDefaultInstance()
				.getParserForType());
		parsersMap.put(DevelopRescueLvUpResponse_25000008, cn.game.protocol.protobuf.DevelopMsg.DevelopRescueLvUpResponse_25000008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(DevelopHeavenlyDaoLvUpRequest_25000010, cn.game.protocol.protobuf.DevelopMsg.DevelopHeavenlyDaoLvUpRequest_25000010.getDefaultInstance()
				.getParserForType());
		parsersMap.put(DevelopHeavenlyDaoLvUpResponse_25000011, cn.game.protocol.protobuf.DevelopMsg.DevelopHeavenlyDaoLvUpResponse_25000011.getDefaultInstance()
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
		parsersMap.put(GmPlayerRequest_77000021, cn.game.protocol.protobuf.GmMsg.GmPlayerRequest_77000021.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmPlayerResponse_77000022, cn.game.protocol.protobuf.GmMsg.GmPlayerResponse_77000022.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmOrderRequest_77000025, cn.game.protocol.protobuf.GmMsg.GmOrderRequest_77000025.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmOrderResponse_77000026, cn.game.protocol.protobuf.GmMsg.GmOrderResponse_77000026.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmOrderReimburseRequest_77000027, cn.game.protocol.protobuf.GmMsg.GmOrderReimburseRequest_77000027.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmOrderReimburseResponse_77000028, cn.game.protocol.protobuf.GmMsg.GmOrderReimburseResponse_77000028.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmMailPlayerSendRequest_77000040, cn.game.protocol.protobuf.GmMsg.GmMailPlayerSendRequest_77000040.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmMailPlayerSendResponse_77000041, cn.game.protocol.protobuf.GmMsg.GmMailPlayerSendResponse_77000041.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmMailServerSendRequest_77000048, cn.game.protocol.protobuf.GmMsg.GmMailServerSendRequest_77000048.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmMailServerSendResponse_77000049, cn.game.protocol.protobuf.GmMsg.GmMailServerSendResponse_77000049.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmMailListRequest_77000042, cn.game.protocol.protobuf.GmMsg.GmMailListRequest_77000042.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmMailResponse_77000043, cn.game.protocol.protobuf.GmMsg.GmMailResponse_77000043.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmMailCheckRequest_77000044, cn.game.protocol.protobuf.GmMsg.GmMailCheckRequest_77000044.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmMailCheckResponse_77000045, cn.game.protocol.protobuf.GmMsg.GmMailCheckResponse_77000045.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmMailDeleteRequest_77000046, cn.game.protocol.protobuf.GmMsg.GmMailDeleteRequest_77000046.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmMailDeleteResponse_77000047, cn.game.protocol.protobuf.GmMsg.GmMailDeleteResponse_77000047.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmNoticeAddRequest_77000060, cn.game.protocol.protobuf.GmMsg.GmNoticeAddRequest_77000060.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmNoticeAddResponse_77000061, cn.game.protocol.protobuf.GmMsg.GmNoticeAddResponse_77000061.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmNoticeUpdateRequest_77000062, cn.game.protocol.protobuf.GmMsg.GmNoticeUpdateRequest_77000062.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmNoticeUpdateResponse_77000063, cn.game.protocol.protobuf.GmMsg.GmNoticeUpdateResponse_77000063.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmNoticeDeleteRequest_77000064, cn.game.protocol.protobuf.GmMsg.GmNoticeDeleteRequest_77000064.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmNoticeDeleteResponse_77000065, cn.game.protocol.protobuf.GmMsg.GmNoticeDeleteResponse_77000065.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmNoticeListRequest_77000066, cn.game.protocol.protobuf.GmMsg.GmNoticeListRequest_77000066.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmNoticeListResponse_77000067, cn.game.protocol.protobuf.GmMsg.GmNoticeListResponse_77000067.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmShutdownServerRequest_77000001, cn.game.protocol.protobuf.GmMsg.GmShutdownServerRequest_77000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmShutdownServerResponse_77000002, cn.game.protocol.protobuf.GmMsg.GmShutdownServerResponse_77000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmAccountForbidListRequest_77000003, cn.game.protocol.protobuf.GmMsg.GmAccountForbidListRequest_77000003.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmAccountForbidListResponse_77000004, cn.game.protocol.protobuf.GmMsg.GmAccountForbidListResponse_77000004.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmAccountForbidRequest_77000005, cn.game.protocol.protobuf.GmMsg.GmAccountForbidRequest_77000005.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmAccountForbidResponse_77000006, cn.game.protocol.protobuf.GmMsg.GmAccountForbidResponse_77000006.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmAccountUnblockRequest_77000007, cn.game.protocol.protobuf.GmMsg.GmAccountUnblockRequest_77000007.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmAccountUnblockResponse_77000008, cn.game.protocol.protobuf.GmMsg.GmAccountUnblockResponse_77000008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmOperationRequest_77000071, cn.game.protocol.protobuf.GmMsg.GmOperationRequest_77000071.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmOperationResponse_77000072, cn.game.protocol.protobuf.GmMsg.GmOperationResponse_77000072.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmPlayerLogoutRequest_77000009, cn.game.protocol.protobuf.GmMsg.GmPlayerLogoutRequest_77000009.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmPlayerLogouttResponse_7700000a, cn.game.protocol.protobuf.GmMsg.GmPlayerLogouttResponse_7700000a.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmPlayerMailRequest_77000010, cn.game.protocol.protobuf.GmMsg.GmPlayerMailRequest_77000010.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GmPlayerMailResponse_77000011, cn.game.protocol.protobuf.GmMsg.GmPlayerMailResponse_77000011.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HCBattleSpeedAdsRequest_28000020, cn.game.protocol.protobuf.HCCommonMsg.HCBattleSpeedAdsRequest_28000020.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HCBattleSpeedAdsResponse_28000021, cn.game.protocol.protobuf.HCCommonMsg.HCBattleSpeedAdsResponse_28000021.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HCHeroUpLevelRequest_26000001, cn.game.protocol.protobuf.HCHeroMsg.HCHeroUpLevelRequest_26000001.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HCHeroUpLevelResponse_26000002, cn.game.protocol.protobuf.HCHeroMsg.HCHeroUpLevelResponse_26000002.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HCHeroStarUpRequest_26000003, cn.game.protocol.protobuf.HCHeroMsg.HCHeroStarUpRequest_26000003.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HCHeroStarUpResponse_26000004, cn.game.protocol.protobuf.HCHeroMsg.HCHeroStarUpResponse_26000004.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HCHeroBattleRequest_26000005, cn.game.protocol.protobuf.HCHeroMsg.HCHeroBattleRequest_26000005.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HCHeroBattleResponse_26000006, cn.game.protocol.protobuf.HCHeroMsg.HCHeroBattleResponse_26000006.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HCHeroCompositeRequest_26000007, cn.game.protocol.protobuf.HCHeroMsg.HCHeroCompositeRequest_26000007.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HCHeroCompositeResponse_26000008, cn.game.protocol.protobuf.HCHeroMsg.HCHeroCompositeResponse_26000008.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HCHeroAdsRequest_26000009, cn.game.protocol.protobuf.HCHeroMsg.HCHeroAdsRequest_26000009.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HCHeroAdsResponse_2600000a, cn.game.protocol.protobuf.HCHeroMsg.HCHeroAdsResponse_2600000a.getDefaultInstance()
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
		parsersMap.put(HeroBattleDismissRequest_16000009, cn.game.protocol.protobuf.HeroMsg.HeroBattleDismissRequest_16000009.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HeroBattleDismissResponse_1600000a, cn.game.protocol.protobuf.HeroMsg.HeroBattleDismissResponse_1600000a.getDefaultInstance()
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
		parsersMap.put(HeroIllustrationsListRequest_16000040, cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsListRequest_16000040.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HeroIllustrationsListResponse_16000041, cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsListResponse_16000041.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HeroIllustrationsRewardRequest_16000042, cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsRewardRequest_16000042.getDefaultInstance()
				.getParserForType());
		parsersMap.put(HeroIllustrationsRewardResponse_16000043, cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsRewardResponse_16000043.getDefaultInstance()
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
		parsersMap.put(MailNewPush_12010001, cn.game.protocol.protobuf.MailMsg.MailNewPush_12010001.getDefaultInstance()
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
		parsersMap.put(PlayerLogoutRequest_01000003, cn.game.protocol.protobuf.PlayerMsg.PlayerLogoutRequest_01000003.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerLogoutResponse_01000004, cn.game.protocol.protobuf.PlayerMsg.PlayerLogoutResponse_01000004.getDefaultInstance()
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
		parsersMap.put(PlayerErrorPush_01000099, cn.game.protocol.protobuf.PlayerMsg.PlayerErrorPush_01000099.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerCloudBoxPush_01100040, cn.game.protocol.protobuf.PlayerMsg.PlayerCloudBoxPush_01100040.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerCloudBoxRequest_01000042, cn.game.protocol.protobuf.PlayerMsg.PlayerCloudBoxRequest_01000042.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerCloudBoxResponse_01000043, cn.game.protocol.protobuf.PlayerMsg.PlayerCloudBoxResponse_01000043.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerExpLevelPush_01100050, cn.game.protocol.protobuf.PlayerMsg.PlayerExpLevelPush_01100050.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerGuideRequest_01000060, cn.game.protocol.protobuf.PlayerMsg.PlayerGuideRequest_01000060.getDefaultInstance()
				.getParserForType());
		parsersMap.put(PlayerGuideResponse_01000061, cn.game.protocol.protobuf.PlayerMsg.PlayerGuideResponse_01000061.getDefaultInstance()
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
		parsersMap.put(NoticeRequest_01000050, cn.game.protocol.protobuf.PlayerMsg.NoticeRequest_01000050.getDefaultInstance()
				.getParserForType());
		parsersMap.put(NoticeResponse_01000051, cn.game.protocol.protobuf.PlayerMsg.NoticeResponse_01000051.getDefaultInstance()
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
		parsersMap.put(GameGmPlayerInfoRequest_7d000050, cn.game.protocol.protobuf.ServerMsg.GameGmPlayerInfoRequest_7d000050.getDefaultInstance()
				.getParserForType());
		parsersMap.put(GameGmPlayerInfoResponse_7d000051, cn.game.protocol.protobuf.ServerMsg.GameGmPlayerInfoResponse_7d000051.getDefaultInstance()
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
		nameIdMap.put("BattleShareRequest_13000007", 0x13000007);
		nameIdMap.put("BattleShareResponse_13000008", 0x13000008);
		nameIdMap.put("BattleRewardRequest_13000022", 0x13000022);
		nameIdMap.put("BattleRewardResponse_13000023", 0x13000023);
		nameIdMap.put("HCBattleRewardRequest_13000027", 0x13000027);
		nameIdMap.put("HCBattleRewardResponse_13000028", 0x13000028);
		nameIdMap.put("BattleSweepRequest_13000024", 0x13000024);
		nameIdMap.put("BattleSweepResponse_13000025", 0x13000025);
		nameIdMap.put("HCBattleSweepRequest_13000040", 0x13000040);
		nameIdMap.put("HCBattleSweepResponse_13000041", 0x13000041);
		nameIdMap.put("BattlePatrolRewardRequest_13000044", 0x13000044);
		nameIdMap.put("BattlePatrolRewardResponse_13000045", 0x13000045);
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
		nameIdMap.put("BattleRougeRefreshRequest_13000005", 0x13000005);
		nameIdMap.put("BattleRougeRefreshResponse_13000006", 0x13000006);
		nameIdMap.put("BattleStaminaRequest_13000050", 0x13000050);
		nameIdMap.put("BattleStaminaResponse_13000051", 0x13000051);
		nameIdMap.put("ChatRequest_31000001", 0x31000001);
		nameIdMap.put("ChatResponse_31000002", 0x31000002);
		nameIdMap.put("ChatMessagePush_31010001", 0x31010001);
		nameIdMap.put("ServerChatMessagePush_31000010", 0x31000010);
		nameIdMap.put("DevelopPotentialLvUpRequest_25000001", 0x25000001);
		nameIdMap.put("DevelopPotentialLvUpResponse_25000002", 0x25000002);
		nameIdMap.put("DevelopPotentialBreakRequest_25000003", 0x25000003);
		nameIdMap.put("DevelopPotentialBreakResponse_25000004", 0x25000004);
		nameIdMap.put("DevelopRescueLvUpRequest_25000007", 0x25000007);
		nameIdMap.put("DevelopRescueLvUpResponse_25000008", 0x25000008);
		nameIdMap.put("DevelopHeavenlyDaoLvUpRequest_25000010", 0x25000010);
		nameIdMap.put("DevelopHeavenlyDaoLvUpResponse_25000011", 0x25000011);
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
		nameIdMap.put("GmPlayerRequest_77000021", 0x77000021);
		nameIdMap.put("GmPlayerResponse_77000022", 0x77000022);
		nameIdMap.put("GmOrderRequest_77000025", 0x77000025);
		nameIdMap.put("GmOrderResponse_77000026", 0x77000026);
		nameIdMap.put("GmOrderReimburseRequest_77000027", 0x77000027);
		nameIdMap.put("GmOrderReimburseResponse_77000028", 0x77000028);
		nameIdMap.put("GmMailPlayerSendRequest_77000040", 0x77000040);
		nameIdMap.put("GmMailPlayerSendResponse_77000041", 0x77000041);
		nameIdMap.put("GmMailServerSendRequest_77000048", 0x77000048);
		nameIdMap.put("GmMailServerSendResponse_77000049", 0x77000049);
		nameIdMap.put("GmMailListRequest_77000042", 0x77000042);
		nameIdMap.put("GmMailResponse_77000043", 0x77000043);
		nameIdMap.put("GmMailCheckRequest_77000044", 0x77000044);
		nameIdMap.put("GmMailCheckResponse_77000045", 0x77000045);
		nameIdMap.put("GmMailDeleteRequest_77000046", 0x77000046);
		nameIdMap.put("GmMailDeleteResponse_77000047", 0x77000047);
		nameIdMap.put("GmNoticeAddRequest_77000060", 0x77000060);
		nameIdMap.put("GmNoticeAddResponse_77000061", 0x77000061);
		nameIdMap.put("GmNoticeUpdateRequest_77000062", 0x77000062);
		nameIdMap.put("GmNoticeUpdateResponse_77000063", 0x77000063);
		nameIdMap.put("GmNoticeDeleteRequest_77000064", 0x77000064);
		nameIdMap.put("GmNoticeDeleteResponse_77000065", 0x77000065);
		nameIdMap.put("GmNoticeListRequest_77000066", 0x77000066);
		nameIdMap.put("GmNoticeListResponse_77000067", 0x77000067);
		nameIdMap.put("GmShutdownServerRequest_77000001", 0x77000001);
		nameIdMap.put("GmShutdownServerResponse_77000002", 0x77000002);
		nameIdMap.put("GmAccountForbidListRequest_77000003", 0x77000003);
		nameIdMap.put("GmAccountForbidListResponse_77000004", 0x77000004);
		nameIdMap.put("GmAccountForbidRequest_77000005", 0x77000005);
		nameIdMap.put("GmAccountForbidResponse_77000006", 0x77000006);
		nameIdMap.put("GmAccountUnblockRequest_77000007", 0x77000007);
		nameIdMap.put("GmAccountUnblockResponse_77000008", 0x77000008);
		nameIdMap.put("GmOperationRequest_77000071", 0x77000071);
		nameIdMap.put("GmOperationResponse_77000072", 0x77000072);
		nameIdMap.put("GmPlayerLogoutRequest_77000009", 0x77000009);
		nameIdMap.put("GmPlayerLogouttResponse_7700000a", 0x7700000a);
		nameIdMap.put("GmPlayerMailRequest_77000010", 0x77000010);
		nameIdMap.put("GmPlayerMailResponse_77000011", 0x77000011);
		nameIdMap.put("HCBattleSpeedAdsRequest_28000020", 0x28000020);
		nameIdMap.put("HCBattleSpeedAdsResponse_28000021", 0x28000021);
		nameIdMap.put("HCHeroUpLevelRequest_26000001", 0x26000001);
		nameIdMap.put("HCHeroUpLevelResponse_26000002", 0x26000002);
		nameIdMap.put("HCHeroStarUpRequest_26000003", 0x26000003);
		nameIdMap.put("HCHeroStarUpResponse_26000004", 0x26000004);
		nameIdMap.put("HCHeroBattleRequest_26000005", 0x26000005);
		nameIdMap.put("HCHeroBattleResponse_26000006", 0x26000006);
		nameIdMap.put("HCHeroCompositeRequest_26000007", 0x26000007);
		nameIdMap.put("HCHeroCompositeResponse_26000008", 0x26000008);
		nameIdMap.put("HCHeroAdsRequest_26000009", 0x26000009);
		nameIdMap.put("HCHeroAdsResponse_2600000a", 0x2600000a);
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
		nameIdMap.put("HeroBattleDismissRequest_16000009", 0x16000009);
		nameIdMap.put("HeroBattleDismissResponse_1600000a", 0x1600000a);
		nameIdMap.put("HeroLevelResetRequest_16000007", 0x16000007);
		nameIdMap.put("HeroLevelResetResponse_16000008", 0x16000008);
		nameIdMap.put("HeroQualityResetRequest_16000011", 0x16000011);
		nameIdMap.put("HeroQualityResetResponse_16000012", 0x16000012);
		nameIdMap.put("HeroFreeDayRentRequest_16000030", 0x16000030);
		nameIdMap.put("HeroFreeDayRentResponse_16000031", 0x16000031);
		nameIdMap.put("HeroFreeDayRentChooseRequest_16000032", 0x16000032);
		nameIdMap.put("HeroFreeDayRentChooseResponse_16000033", 0x16000033);
		nameIdMap.put("HeroIllustrationsListRequest_16000040", 0x16000040);
		nameIdMap.put("HeroIllustrationsListResponse_16000041", 0x16000041);
		nameIdMap.put("HeroIllustrationsRewardRequest_16000042", 0x16000042);
		nameIdMap.put("HeroIllustrationsRewardResponse_16000043", 0x16000043);
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
		nameIdMap.put("MailNewPush_12010001", 0x12010001);
		nameIdMap.put("MergeEquipmentWearRequest_23000001", 0x23000001);
		nameIdMap.put("MergeEquipmentWearResponse_23000002", 0x23000002);
		nameIdMap.put("MergeEquipmentTeardownRequest_23000003", 0x23000003);
		nameIdMap.put("MergeEquipmentTeardownResponse_23000004", 0x23000004);
		nameIdMap.put("MergeEquipmentPartStrengthRequest_23000007", 0x23000007);
		nameIdMap.put("MergeEquipmentPartStrengthResponse_23000008", 0x23000008);
		nameIdMap.put("PlayerLoginRequest_01000001", 0x01000001);
		nameIdMap.put("PlayerLoginResponse_01000002", 0x01000002);
		nameIdMap.put("PlayerLogoutRequest_01000003", 0x01000003);
		nameIdMap.put("PlayerLogoutResponse_01000004", 0x01000004);
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
		nameIdMap.put("PlayerErrorPush_01000099", 0x01000099);
		nameIdMap.put("PlayerCloudBoxPush_01100040", 0x01100040);
		nameIdMap.put("PlayerCloudBoxRequest_01000042", 0x01000042);
		nameIdMap.put("PlayerCloudBoxResponse_01000043", 0x01000043);
		nameIdMap.put("PlayerExpLevelPush_01100050", 0x01100050);
		nameIdMap.put("PlayerGuideRequest_01000060", 0x01000060);
		nameIdMap.put("PlayerGuideResponse_01000061", 0x01000061);
		nameIdMap.put("PlayerBriefInfoRequest_01000007", 0x01000007);
		nameIdMap.put("PlayerBriefInfoResponse_01000008", 0x01000008);
		nameIdMap.put("PlayerBriefInfoOtherRequest_01000009", 0x01000009);
		nameIdMap.put("PlayerBriefInfoOtherResponse_0100000a", 0x0100000a);
		nameIdMap.put("PlayerShowRequest_01000039", 0x01000039);
		nameIdMap.put("PlayerShowResponse_0100003a", 0x0100003a);
		nameIdMap.put("PlayerAlchemyRequest_01000040", 0x01000040);
		nameIdMap.put("PlayerAlchemyResponse_01000041", 0x01000041);
		nameIdMap.put("PlayerResetPush_01100016", 0x01100016);
		nameIdMap.put("NoticeRequest_01000050", 0x01000050);
		nameIdMap.put("NoticeResponse_01000051", 0x01000051);
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
		nameIdMap.put("GameGmPlayerInfoRequest_7d000050", 0x7d000050);
		nameIdMap.put("GameGmPlayerInfoResponse_7d000051", 0x7d000051);
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
