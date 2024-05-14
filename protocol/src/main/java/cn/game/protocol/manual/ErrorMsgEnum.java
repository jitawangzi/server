package cn.game.protocol.manual;

/**
 * 错误类型
 */
public enum ErrorMsgEnum{

	// #系统级错误，1-49
	/** 正常 */
	ok(0,"ok","正常"),
	/** 未知错误 */
	unknown(1,"unknown","未知错误"),
	/** 功能暂时不能使用。 */
	module_disabled(2,"module_disabled","功能暂时不能使用。"),
	/** 暂时不能登录 */
	login_forbidden(3,"login_forbidden","暂时不能登录"),
	/** 重连失败，需要重新登录 */
	reconnect_fail(4,"reconnect_fail","重连失败，需要重新登录"),
	/** 需要重新登录 */
	need_login(5,"need_login","需要重新登录"),
	/** 版本不匹配 */
	version_mismatch(6, "version_mismatch", "版本不匹配"),
	/**当前服务器状态不允许进入 */
	server_status(7, "server_status", "当前服务器状态不允许进入"),
	/** 没有获取到玩家锁 */
	player_lock(8, "player_lock", "没有获取到玩家锁"),
	/** redis操作失败 */
	redis_fail(9, "redis_fail", "redis操作失败"),
	/** 远程服务器请求失败，可能多种原因 */
	request_remote_server(10, "request_remote_server", "远程服务器请求失败，可能多种原因"),
	/** 不在线/不在当前服务器 */
	not_online(11, "not_online", "不在线/不在当前服务器"),
	session_not_exist(12, "not_online", "session错误，需要先登陆"),

	// #通用错误 50 - 99
	/** 玩家数据校验错误。 */
	player_check_error(50,"player_check_error","玩家数据校验错误。"),
	/** 非法的重复操作，例如重复领取某任务奖励等。 */
	repeat_illegal(51, "repeat", "非法的重复操作，例如重复领取某任务奖励等。"),
	/** 请求参数校验错误 */
	request_parameter_error(52, "request_parameter_error", "请求参数校验错误，例如合法值是1、2、3，却发了0"),
	/** 非法请求，一般是客户端不够条件进行当前操作 */
	illegal_request(53, "illegal_request", "非法请求，一般是客户端不够条件进行当前操作，例如功能未开启，等级不足等等"),

	/** 玩家数据不存在 */
	player_data_not_found(54, "player_data_not_found", "玩家数据不存在"),
	/** 条件校验错误 */
	condition_check_error(55, "condition_check_error", "条件校验错误"),
	/** 配置表数据找不到 */
	config_data_not_found(56, "config_data_not_found", "配置表数据找不到"),
	/** 等级不足 */
	player_level_not_enough(57, "player_level_not_enough", "等级不足"),
	/** 一般是时间未到之类未开启 */
	not_open(58, "not_open", "一般是时间未到之类未开启"),
	/** 功能尚未开启  */
	func_not_open(59, "func_not_open", "功能尚未开启"),
	/** 资源不足 */
	resource_not_enough(60, "resource_not_enough", "资源不足"),
	/** 次数不足 */
	times_limit(61, "times_limit", "次数不足"),

	// 100+ 业务错误。

	/** 创建角色名字重复 */
	player_name_repeat(100,"player_name_repeat","创建角色名字重复"),
	/** 等级已达上限 */
	level_reach_limit(104,"level_reach_limit","等级已达上限"),
	/** 未解锁 */
	unlock(105,"unlock","未解锁"),
	/** 前置关卡没有通关 */
	BattleLevel_pre(106,"BattleLevel_pre","前置关卡没有通关"),
	/** 购买超上限 */
	buy_over_limit(108,"buy_over_limit","购买超上限"),
	/** 玩家名字不合法  */
	player_name_illegal(109, "player_name_illegal", "玩家名字不合法"),
	/** 不能出售 */
	not_sale(114,"not_sale","不能出售"),
	/** 不能使用 */
	not_use(115,"not_use","不能使用"),
	/** 没有权限做此操作 */
	not_jurisdiction(117,"not_jurisdiction","没有权限做此操作"),

	/** 群组人数已满 */
	max_group_count(122, "max_group_count", "群组人数已满"),
	/** 玩家已在群组里 */
	not_player_group(123, "not_player_group", "玩家已在群组里"),
	/** 玩家的群组数已满 */
	not_player_group_count(124, "not_player_group_count", "玩家的群组数已满"),
	/** 群组不存在 */
	not_group(116, "not_group", "群组不存在"),
	store_need_refresh(370, "store_need_refresh", "商店需要刷新"),

	/** 名字不合法 */
	not_name(118,"not_name","名字不合法"),
	/** 公告不合法 */
	not_notice(119,"not_notice","公告不合法"),
	/** 可能没有这个玩家 */
	player_not_found(127,"player_not_found","可能没有这个玩家"),
	/** 玩家不在线 */
	player_not_online(128,"player_not_online","玩家不在线"),
	/** 该玩家不是好友 */
	player_not_friend(129,"player_not_friend","该玩家不是好友"),
	/** 已经被拉黑，私聊消息被拒收 */
	black_friend_chat_not(130,"black_friend_chat_not","已经被拉黑，私聊消息被拒收"),
	/** 不能跟已经被拉黑的好友说话 */
	black_friend_not_send(131,"black_friend_not_send","不能跟已经被拉黑的好友说话"),
	/** 今日购买次数已用完 */
	buy_power_count_not(132,"buy_power_count_not","今日购买次数已用完"),
	/** 初始化异常 */
	init_error(135,"init_error","初始化异常"),
	/** 前置剧情没有完成 */
	story_pre_not_finish(145,"story_pre_not_finish","前置剧情没有完成"),
	/** 该名称已存在 */
	name_exist(166,"name_exist","该名称已存在"),

	// 商店，月卡
	/** 商品不存在 */
	shop_item_not_exist(370, "shop_item_not_exist", "商品不存在"),
	/** 商品购买次数达到上限 */
	shop_item_buy_count_max(371, "shop_item_buy_count_max", "商品购买次数达到上限"),
	month_card_repeated(372, "month_card_repeated", "月卡重复购买"),
	month_card_not_exist(373, "month_card_not_exist", "月卡不存在"),
	month_card_reward_repeated(374, "month_card_reward_repeated", "月卡重复领奖"),
	month_card_condition(375, "month_card_condition", "月卡没有达到购买条件"),
	shop_gift_condition(378, "shop_gift_condition", "礼包没有达到购买条件"),
	shop_gift_repeated(379, "shop_gift_repeated", "礼包重复购买"),

	// 英雄
	hero_level_max(380, "hero_level_max", "英雄等级到达上限"),

	hero_break_max(381, "hero_break_max", "英雄突破到达上限"),

    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 描述 */
	private String desc ; 

	private ErrorMsgEnum(int id, String name, String desc) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static ErrorMsgEnum get(int id) {
		ErrorMsgEnum[] values = ErrorMsgEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【ErrorMsgEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static ErrorMsgEnum getNullable(int id) {
		ErrorMsgEnum[] values = ErrorMsgEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		return null;
	}

	public int getId(){
		return this.id;
	}
	public String getName(){
		return this.name;
	}
	public String getDesc(){
		return this.desc;
	}
}
