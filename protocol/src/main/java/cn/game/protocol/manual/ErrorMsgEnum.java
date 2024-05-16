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

	// #通用错误 50 - 99
	/** 玩家数据校验错误。 */
	player_check_error(50, "玩家数据校验错误。"),
	/** 非法的重复操作，例如重复领取某任务奖励等。 */
	repeat_request(51, "非法的重复操作，例如重复领取某任务奖励等。"),
	/** 请求参数校验错误 */
	request_parameter_error(52, "请求参数校验错误，例如合法值是1、2、3，却发了0"),
	/** 非法请求，一般是客户端不够条件进行当前操作 */
	illegal_request(53, "非法请求，一般是客户端不够条件进行当前操作，例如功能未开启，等级不足等等"),
	/** 玩家数据不存在 */
	player_data_not_found(54, "玩家数据不存在"),
	/** 条件校验错误 */
	condition_check_error(55, "条件校验错误"),
	/** 配置表数据找不到 */
	config_data_not_found(56, "配置表数据找不到"),
	/** 等级不足 */
	player_level_not_enough(57, "等级不足"),
	/** 一般是时间未到之类未开启 */
	not_open(58, "一般是时间未到之类未开启"),
	/** 功能尚未开启  */
	func_not_open(59, "功能尚未开启"),
	/** 资源不足 */
	resource_not_enough(60, "资源不足"),
	/** 次数不足 */
	times_limit(61, "次数不足"),

	// 100+ 业务错误。

	/** 创建角色名字重复 */
	player_name_repeat(100, "创建角色名字重复"),
	/** 等级已达上限 */
	level_reach_limit(104, "等级已达上限"),
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

	/** 没有权限做此操作 */
	not_jurisdiction(117, "没有权限做此操作"),
	/** 群组人数已满 */
	max_group_count(122, "群组人数已满"),
	/** 玩家已在群组里 */
	not_player_group(123, "玩家已在群组里"),
	/** 玩家的群组数已满 */
	not_player_group_count(124, "玩家的群组数已满"),
	/** 群组不存在 */
	not_group(116, "群组不存在"),

	/** 名字不合法 */
	not_name(118, "名字不合法"),
	/** 公告不合法 */
	not_notice(119, "公告不合法"),
	/** 可能没有这个玩家 */
	player_not_found(127, "可能没有这个玩家"),
	/** 玩家不在线 */
	player_not_online(128, "玩家不在线"),
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

	// 英雄
	hero_level_max(380, "英雄等级到达上限"),
	hero_break_max(381, "英雄突破到达上限"),

	;

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
