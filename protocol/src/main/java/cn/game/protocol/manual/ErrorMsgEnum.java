package cn.game.protocol.manual;

/**
 * 错误类型
 */
public enum ErrorMsgEnum {

	// #系统级错误，1-49
	/** 正常 */
	ok(0, "正常"),
	/** 未知错误 */
	unknown(1, "未知错误"),
	/** 功能暂时不能使用。 */
	module_disabled(2, "功能暂时不能使用。"),
	/** 暂时不能登录 */
	login_forbidden(3, "暂时不能登录"),
	/** 重连失败，需要重新登录 */
	reconnect_fail(4, "重连失败，需要重新登录"),
	/** 需要重新登录 */
	need_login(5, "需要重新登录"),
	/** 版本不匹配 */
	version_mismatch(6, "版本不匹配"),
	/**当前服务器状态不允许进入 */
	server_status(7, "当前服务器状态不允许进入"),
	/** 没有获取到玩家锁 */
	player_lock(8, "没有获取到玩家锁"),
	/** redis操作失败 */
	redis_fail(9, "redis操作失败"),
	/** 远程服务器请求失败，可能多种原因 */
	request_remote_server(10, "远程服务器请求失败，可能多种原因"),
	/** 不在线/不在当前服务器 */
	not_online(11, "不在线/不在当前服务器"),
	session_not_exist(12, "session错误，需要先登陆"),
	requests_too_frequent(13, "请求太频繁"),
	async_request_fail(14, "异步请求失败"),
	/** 特别注意这个错误只是 客户端使用，服务器不用这个错误码，只是给客户端定义的。  */
	request_timeout(15, "请求超时没有返回数据"),
	disable_ios_pay(16, "当前的客户端版本，禁用ios支付"),
	gm_cmd_not_exist(18, "不存在的gm指令"),

	// #通用错误 50 - 99
	/** 玩家数据校验错误。 */
	player_check_error(50, "玩家数据校验错误。"),
	/** 非法的重复操作，例如重复领取某任务奖励等。 */
	repeat_request(51, "非法的重复操作，例如重复领取某任务奖励等。"),
	/** 请求参数校验错误 */
	request_parameter_error(52, "请求参数校验错误，例如合法值是1、2、3，却发了0"),
	/** 请求参数为null */
	request_parameter_null(53, "请求参数为null"),
	/** 玩家数据不存在 */
	player_data_not_found(54, "玩家数据不存在"),
	/** 条件校验错误 */
	condition_check_error(55, "条件校验错误"),
	/** 配置表数据找不到 */
	config_data_not_found(56, "配置表数据找不到"),
	/** 等级不足 */
	level_not_enough(57, "等级不足"),
	/** 一般是时间未到之类未开启 */
	not_open(58, "一般是时间未到之类未开启"),
	/** 功能尚未开启  */
	func_not_open(59, "功能尚未开启"),
	/** 资源不足 */
	resource_not_enough(60, "资源不足"),
	/** 次数不足 */
	times_limit(61, "次数不足"),
	free_times_limit(62, "免费次数不足"),
	cd_time_error(63, "未到冷却时间"),
	level_limit(64, "等级已经到上限，可能是等级，星级等"),
	player_not_exist(65, "玩家不存在"),
	player_not_online(66, "玩家不在线"),
	operation_too_fast(67, "请求太快，太频繁了"),
	/** 非法请求，一般是客户端不够条件进行当前操作 */
	illegal_request(68, "非法请求，一般是客户端不够条件进行当前操作，例如功能未开启，等级不足等等"),
	not_watch_ads(69, "需要先看广告才能进行该操作"),

	/** 前置条件校验错误 */
	pre_condition_check_error(70, "前置条件校验错误"),
	/** 没有该福利/特权，不能进行此项操作。 */
	welfare_check_error(71, "没有该福利/特权，不能进行此项操作。"),
	// 100+ 业务错误。

	/** 创建角色名字重复 */
	player_name_repeat(100, "创建角色名字重复"),
	/** 前置关卡没有通关 */
	BattleLevel_pre(106, "前置关卡没有通关"),
	/** 购买超上限 */
	buy_over_limit(108, "购买超上限"),
	/** 玩家名字不合法  */
	player_name_illegal(109, "玩家名字不合法"),
	/** 不能出售 */
	not_sale(114, "不能出售"),
	/** 不能使用 */
	not_use(115, "不能使用"),

	/** 名字不合法 */
	not_name(118, "名字不合法"),
	/** 公告不合法 */
	not_notice(119, "公告不合法"),
	/** 可能没有这个玩家 */
	player_not_found(127, "可能没有这个玩家"),
	/** 该玩家不是好友 */
	player_not_friend(129, "该玩家不是好友"),
	/** 已经被拉黑，私聊消息被拒收 */
	black_friend_chat_not(130, "已经被拉黑，私聊消息被拒收"),
	/** 不能跟已经被拉黑的好友说话 */
	black_friend_not_send(131, "不能跟已经被拉黑的好友说话"),
	/** 初始化异常 */
	init_error(135, "初始化异常"),
	/** 前置剧情没有完成 */
	story_pre_not_finish(145, "前置剧情没有完成"),
	/** 该名称已存在 */
	name_exist(166, "该名称已存在"),
	/** 服务器没有处理对应的红点类型 */
	red_point_not_support(170, "服务器没有处理对应的红点类型"),


	// 英雄
	hero_level_max(180, "英雄等级到达上限"),
	hero_break_max(181, "英雄突破到达上限"),

	payment_order_create_fail(360, "充值订单创建失败。"),

	// 商店，月卡
	/** 商品不存在 */
	shop_item_not_exist(370, "商品不存在"),
	/** 商品购买次数达到上限 */
	shop_item_buy_count_max(371, "商品购买次数达到上限"),

	month_card_repeated(372, "月卡重复购买"),
	month_card_not_exist(373, "月卡不存在"),
	month_card_condition(375, "月卡没有达到购买条件"),

	shop_gift_condition(378, "礼包没有达到购买条件"),
	shop_gift_repeated(379, "礼包重复购买"),

	fundpass_not_buy(380, "没有购买通行证"),
	hero_day_rent_max(390, "日租卡最多能上一个"),
	stamina_expire(391, "体力过期了"),
	vip_free_gift_has_reward(392, "VIP 每日免费礼包已经领取过了"),
	vip_gift_has_reward(393, "VIP一次性礼包不可重复购买"),
	vip_gift_unlock(394, "VIP等级不足一次性礼包未解锁"),
	activity_lei_chong_has_reward(395, "该累充活动奖励已经领取过了"),
	activity_task_not_finish(396, "该活动任务未完成不可领取"),
	activity_not_found(397, "该活动不存在"),
	da_dao_not_play(398, "当前时间不可进行大道争锋活动"),
	da_dao_play_num_not_enough(399, "今日大道争锋挑战次数已经用完"),
	da_dao_not_found_target_Player(400, "大道争锋要挑战的目标不存在"),
	da_dao_in_battle(401, "大道争锋要挑战正在进行中，不可再次挑战"),
	da_dao_refresh_is_max(402, "大道争锋刷新次数已经达到最大"),
	da_dao_free_refresh_is_max(403, "大道争锋免费刷新次数已经用完"),
	we_chat_context_check_fail(404, "输入的文字包含屏蔽字，请检查之后在输入"),
	login_fail_player_is_forbid(405,"玩家被封号 不可登录"),
	chat_fail_player_is_forbid(406,"玩家被禁言 不可聊天"),
	xian_shi_li_bao_not_found(407,"购买的限时礼包不存在"),
	xian_shi_li_bao_time_is_fail(408,"限时礼包已经过期，不可购买"),
	xian_shi_li_bao_buy_num_is_max(409, "限时礼包已经购买过了，不可重复购买"),
	xian_shi_li_bao_buy_fail(410, "购买限时礼包失败");

	/** id */
	public int ID;
	/** 描述 */
	private String desc;

	private ErrorMsgEnum(int id, String desc) {
		this.ID = id;
		this.desc = desc;
	}

	public int getId() {
		return this.ID;
	}
	public String getDesc() {
		return this.desc;
	}
}
