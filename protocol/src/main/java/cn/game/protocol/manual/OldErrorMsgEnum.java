package cn.game.protocol.manual;

/**
 * 错误类型
 * 
 * 工具生成的，不要手动修改
 */
public enum OldErrorMsgEnum{

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

	/** 玩家数据校验错误。 */
	player_check_error(50,"player_check_error","玩家数据校验错误。"),
	/** 铜币不足 */
	coin_not_enough(51,"coin_not_enough","铜币不足"),
	/** 资源不足 */
	resource_not_enough(52,"resource_not_enough","资源不足"),
	/** 玩家数据不存在 */
	player_data_not_found(53,"player_data_not_found","玩家数据不存在"),
	/** 配置表数据找不到 */
	config_data_not_found(54,"config_data_not_found","配置表数据找不到"),
	/** 元宝不足 */
	gold_not_enough(55,"gold_not_enough","元宝不足"),
	/** 等级不足 */
	player_level_not_enough(56,"player_level_not_enough","等级不足"),
	/** 请求参数校验错误 */
	request_parameter_error(57,"request_parameter_error","请求参数校验错误"),
	/** 玩家名字不合法  */
	player_name_illegal(60, "player_name_illegal", "玩家名字不合法"),
	/** 创建角色名字重复 */
	player_name_repeat(100,"player_name_repeat","创建角色名字重复"),
	/** 重复装备。 */
	equip_repeat(101,"equip_repeat","重复装备。"),
	/** 装备位置不匹配。 */
	equippos_not_match(102,"equippos_not_match","装备位置不匹配。"),
	/** 阵营数据不一致 */
	camp_not_fit(103,"camp_not_fit","阵营数据不一致"),
	/** 等级已达上限 */
	level_reach_limit(104,"level_reach_limit","等级已达上限"),
	/** 未解锁 */
	unlock(105,"unlock","未解锁"),
	/** 前置关卡没有通关 */
	BattleLevel_pre(106,"BattleLevel_pre","前置关卡没有通关"),
	/** 进阶训练进关卡职业不匹配 */
	routinetranin_profession_not_match(107,"routinetranin_profession_not_match","进阶训练进关卡职业不匹配"),
	/** 购买超上限 */
	buy_over_limit(108,"buy_over_limit","购买超上限"),
	/** 非法请求 */
	illegal_request(109,"illegal_request","非法请求"),
	/** 奖励已领取 */
	reward_have_received(110,"reward_have_received","奖励已领取"),
	/** 通行证未启用 */
	battlepass_not_buy(111,"battlepass_not_buy","通行证未启用"),
	/** 次数不足 */
	count_not(112,"count_not","次数不足"),
	/** 好感度等级奖励已领取 */
	love_lv_reaward_not(113,"love_lv_reaward_not","好感度等级奖励已领取"),
	/** 不能出售 */
	not_sale(114,"not_sale","不能出售"),
	/** 不能使用 */
	not_use(115,"not_use","不能使用"),
	/** 群组不存在 */
	not_group(116,"not_group","群组不存在"),
	/** 没有权限做此操作 */
	not_jurisdiction(117,"not_jurisdiction","没有权限做此操作"),
	/** 名字不合法 */
	not_name(118,"not_name","名字不合法"),
	/** 公告不合法 */
	not_notice(119,"not_notice","公告不合法"),
	/** 创建群组数量已达最大 */
	max_count_create(120,"max_count_create","创建群组数量已达最大"),
	/** 玩家被邀请入群列表已满 */
	max_group_appliaction(121,"max_group_appliaction","玩家被邀请入群列表已满"),
	/** 群组人数已满 */
	max_group_count(122,"max_group_count","群组人数已满"),
	/** 玩家已在群组里 */
	not_player_group(123,"not_player_group","玩家已在群组里"),
	/** 玩家的群组数已满 */
	not_player_group_count(124,"not_player_group_count","玩家的群组数已满"),
	/** 玩法类型与阵容id不匹配 */
	lineupid_dungeontype_not_match(125,"lineupid_dungeontype_not_match","玩法类型与阵容id不匹配"),
	/** 阵容类型不存在 */
	lineuptype_not_exist(126,"lineuptype_not_exist","阵容类型不存在"),
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
	/** 还没有准备好SAN值充能，请等待 */
	spirit_receivetime_notfit(133,"spirit_receivetime_notfit","还没有准备好SAN值充能，请等待"),
	/** 重复操作 */
	repeat(134,"repeat","重复操作"),
	/** 初始化异常 */
	init_error(135,"init_error","初始化异常"),
	/** 探索精神值不足 */
	san_not(136,"san_not","探索精神值不足"),
	/** 交互失败 */
	interactive_not(137,"interactive_not","交互失败"),
	/** 活性物质不足 */
	energy_not(138,"energy_not","活性物质不足"),
	/** 怪物交互非法 */
	monster_not(139,"monster_not","怪物交互非法"),
	/** 未在避难所不可设置阵容 */
	sale_room_not(140,"sale_room_not","未在避难所不可设置阵容"),
	/** 已领取或当前避难所无宝箱 */
	sale_box_not(141,"sale_box_not","已领取或当前避难所无宝箱"),
	/** 上阵队员不能为空 */
	role_empty(142,"role_empty","上阵队员不能为空"),
	/** 玩家等级不足 */
	level_not_enough(143,"level_not_enough","玩家等级不足"),
	/** 合成条件不满足 */
	compose_condition_not_found(144,"compose_condition_not_found","合成条件不满足"),
	/** 前置剧情没有完成 */
	story_pre_not_finish(145,"story_pre_not_finish","前置剧情没有完成"),
	/** 解锁条件验证失败 */
	story_unlock_condition_err(146,"story_unlock_condition_err","解锁条件验证失败"),
	/** 不允许重复开始剧情 */
	story_start_repeated(147,"story_start_repeated","不允许重复开始剧情"),
	/** 主线交互等级不足 */
	mainline_not_lv(148,"mainline_not_lv","主线交互等级不足"),
	/** 主线交互物CD未到 */
	mainline_not_time(149,"mainline_not_time","主线交互物CD未到"),
	/** 主线交互物不可重复交互 */
	mainline_not_repeat(150,"mainline_not_repeat","主线交互物不可重复交互"),
	/** 主线交互物交互条件不足 */
	mainline_not_condition(151,"mainline_not_condition","主线交互物交互条件不足"),
	/** 主线命令执行失败 */
	mainline_command_not(152,"mainline_command_not","主线命令执行失败"),
	/** 不在地图中 */
	mainline_not_in_map(153,"mainline_not_in_map","不在地图中"),
	/** 交互的门不在当前地图 */
	mainline_door_map_error(154,"mainline_door_map_error","交互的门不在当前地图"),
	/** 不符合主线地图解锁条件 */
	mainline_map_unlock_error(155,"mainline_map_unlock_error","不符合主线地图解锁条件"),
	/** 阵容人数达上限 */
	lineupNum_reach_limit(156,"lineupNum_reach_limit","阵容人数达上限"),
	/** 阵容位置校验错误 */
	lineupPos_check_error(157,"lineupPos_check_error","阵容位置校验错误"),
	/** 阵容数据重复 */
	lineup_data_repeat(158,"lineup_data_repeat","阵容数据重复"),
	/** 不符合主线地图解锁条件 */
	mainline_not_interactive_item(159,"mainline_not_interactive_item","不符合主线地图解锁条件"),
	/** 层地图已销毁 */
	explore_not_objects(160,"explore_not_objects","层地图已销毁"),
	/** 要交互的怪物根据id找不到怪物 */
	interactive_not_id(161,"interactive_not_id","要交互的怪物根据id找不到怪物"),
	/** 要交互的位置为空 */
	interactive_not_object(162,"interactive_not_object","要交互的位置为空"),
	/** 要交互的怪物攻击不到玩家 */
	interactive_not_role(163,"interactive_not_role","要交互的怪物攻击不到玩家"),
	/** 位置错误 */
	explore_position_error(164,"explore_position_error","位置错误"),
	/** 解锁失败 */
	lock_error(165,"lock_error","解锁失败"),
	/** 该名称已存在 */
	name_exist(166,"name_exist","该名称已存在"),
	/** 已达最大卡组数量 */
	strategyCardGroup_max(167,"strategyCardGroup_max","已达最大卡组数量"),
	/** 长度达上限 */
	length_max(168,"length_max","长度达上限"),
	/** 探索内不能行动 */
	explore_not_action(169,"explore_not_action","探索内不能行动"),
	/** 探索地图更新了，建议重新进入探索。 */
	explore_map_version_update(170,"explore_map_version_update","探索地图更新了，建议重新进入探索。"),
	/** 打boss阶段不可以行动 */
	explore_boss_time_not(171,"explore_boss_time_not","打boss阶段不可以行动"),
	/** 要交互的对象不存在 */
	explore_interactive_object_not(172,"explore_interactive_object_not","要交互的对象不存在"),
	/**  */
	climbing_tower_not_open(200,"climbing_tower_not_open",""),
	/**  */
	climbing_tower_not_start(201,"climbing_tower_not_start",""),
	/**  */
	climbing_tower_not_join(202,"climbing_tower_not_join",""),
	/**  */
	climbing_tower_is_end(203,"climbing_tower_is_end",""),
	/**  */
	climbing_tower_all_pass(204,"climbing_tower_all_pass",""),
	/** 槽位不足 */
	equip_slot_not_enough(220,"equip_slot_not_enough","槽位不足"),
	/** 经验值已满 */
	exp_max(221,"exp_max","经验值已满"),
	/** 装备不在背包内 */
	equip_not_in_bag(222,"equip_not_in_bag","装备不在背包内"),
	/** 背包道具不足 */
	item_bag_item_not_enough(240,"item_bag_item_not_enough","背包道具不足"),
	/** 仓库道具不足 */
	item_repository_item_not_enough(241,"item_repository_item_not_enough","仓库道具不足"),
	/** 背包容量不足 */
	item_bag_capacity_not_enough(242,"item_bag_capacity_not_enough","背包容量不足"),
	/** 角色被锁定,无法操作 */
	role_lock(270,"role_lock","角色被锁定,无法操作"),
	/** cost值不足 */
	gift_card_cost_not_enough(330,"gift_card_cost_not_enough","cost值不足"),
	/** 礼物卡未解锁 */
	gift_card_not_unlock(331,"gift_card_not_unlock","礼物卡未解锁"),
	/** 礼物卡已使用 */
	gift_card_is_used(332,"gift_card_is_used","礼物卡已使用"),
	/** 礼物卡仅限自己使用 */
	gift_card_only_yourself(333,"gift_card_only_yourself","礼物卡仅限自己使用"),
	/** 商店需要刷新 */
	store_need_refresh(370,"store_need_refresh","商店需要刷新"),
	/** 事件还未完成，不能重复开启 */
	event_not_finish(400,"event_not_finish","事件还未完成，不能重复开启"),
	/** 事件已经完成，不能重复开启 */
	event_repeat_error(401,"event_repeat_error","事件已经完成，不能重复开启"),
	/** 事件不存在 */
	event_not_exist(402,"event_not_exist","事件不存在"),
	/** 事件阶段没有按照顺序更新 */
	event_stage_error(403,"event_stage_error","事件阶段没有按照顺序更新"),
	/** 阶段没有全部完成，不能结束事件 */
	event_finish_stage_error(404,"event_finish_stage_error","阶段没有全部完成，不能结束事件"),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 描述 */
	private String desc ; 

	private OldErrorMsgEnum(int id, String name, String desc) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static OldErrorMsgEnum get(int id) {
		OldErrorMsgEnum[] values = OldErrorMsgEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【ErrorMsgEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static OldErrorMsgEnum getNullable(int id) {
		OldErrorMsgEnum[] values = OldErrorMsgEnum.values();
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
