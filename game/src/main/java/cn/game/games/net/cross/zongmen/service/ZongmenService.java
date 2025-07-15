package cn.game.games.net.cross.zongmen.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.cache.CacheType;
import cn.game.core.exception.LogicException;
import cn.game.games.net.cross.zongmen.ZongMenBargain;
import cn.game.games.net.cross.zongmen.ZongMenConstants;
import cn.game.games.net.cross.zongmen.ZongMenInfo;
import cn.game.games.net.cross.zongmen.ZongMenManager;
import cn.game.games.net.cross.zongmen.ZongMenMember;
import cn.game.games.net.cross.zongmen.ZongMenSetting;
import cn.game.games.net.cross.zongmen.dto.CreateZongmenRequest;
import cn.game.games.net.cross.zongmen.dto.MemberAuthRequest;
import cn.game.games.net.cross.zongmen.dto.ZongmenOperationResult;
import cn.game.games.net.cross.zongmen.dto.ZongmenSettingRequest;
import cn.game.protocol.generated.config.GuildPermissionsConfig;
import cn.game.protocol.generated.config.ShopItemConfig;
import cn.game.protocol.generated.config.ZongmenStoreConfig;
import cn.game.protocol.generated.manager.GuildPermissionsManager;
import cn.game.protocol.generated.manager.ShopItemManager;
import cn.game.protocol.generated.manager.ZongmenStoreManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.util.LockUtil;
import io.vertx.core.Future;

/**
 * @ClassName ZongmenService
 * @description: 宗门服务 - 宗门系统的核心业务逻辑服务
 * @author: ly
 * @create: 2025-02-08 14:45 @Version 1.0
 */
public class ZongmenService {
	private static final ZongmenService INSTANCE = new ZongmenService();
	private static final Logger log = LoggerFactory.getLogger(ZongmenService.class);

	private ZongmenService() {
	}

	public static ZongmenService getInstance() {
		return INSTANCE;
	}

	/**
	 * 创建宗门
	 * @param request 创建宗门请求
	 * @return 操作结果
	 */
	public Future<ZongmenOperationResult<ZongMenInfo>> createZongmen(CreateZongmenRequest request) {
		boolean createLock = LockUtil.tryLockNoWaitSync(3, CacheType.ZONG_MEN_CREATE_LOCK.key(request.getName()));
		if (!createLock) {
			return Future.succeededFuture(ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_name_repeat, "宗门名称重复"));
		}

		return ZongMenManager.getInstance()
				.createZongMen(null, request.getName(), request.getCreatePlayerId(), request.getCreatePlayerName(), request.getPower(),
						request.getServerId())
				.map(zongMenInfo -> {
					if (zongMenInfo != null) {
						return ZongmenOperationResult.success(zongMenInfo);
					} else {
						return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_create_failed, "创建宗门失败");
					}
				});
	}

	/**
	 * 获取宗门信息
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @return 操作结果
	 */
	public ZongmenOperationResult<ZongMenInfo> getZongmenInfo(long zongMenId, long playerId) {
		ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
		if (zongMenInfo == null || zongMenInfo.getMember(playerId) == null) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_not_exist, "宗门不存在");
		}
		return ZongmenOperationResult.success(zongMenInfo);
	}

	/**
	 * 申请加入宗门
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @param playerName 玩家名称
	 * @param power 战斗力
	 * @return 操作结果
	 */
	public ZongmenOperationResult<ZongMenInfo> applyJoinZongmen(long zongMenId, long playerId, String playerName, int power) {
		ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
		if (zongMenInfo == null) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_not_exist, "宗门不存在");
		}
		if (zongMenInfo.isHasMember(playerId)) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_player_apply_has, "已经是宗门成员");
		}
		if (zongMenInfo.isFull()) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_full, "宗门已满");
		}
		if (zongMenInfo.hasApply(playerId)) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_apply_exist, "已提交申请");
		}

		// 开启自动加入 则直接加入宗门
		if (zongMenInfo.isAutoJoin()) {
			zongMenInfo.joinZongMen(playerId, playerName, power, ZongMenConstants.ZONG_MEN_POSITION_BANG_ZHONG);
		} else if (zongMenInfo.getModule().setting.getAutoJoin() == 2) {
			zongMenInfo.applyJoin(playerId);
		} else {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_not_allow_join, "宗门不允许加入");
		}

		return ZongmenOperationResult.success(zongMenInfo);
	}

	/**
	 * 解散宗门
	 * @param zongMenId 宗门ID
	 * @param playerId 操作玩家ID
	 * @return 操作结果
	 */
	public ZongmenOperationResult<Boolean> dissolveZongmen(long zongMenId, long playerId) {
		ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
		if (zongMenInfo == null) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_not_exist, "宗门不存在");
		}

		ZongMenMember member = zongMenInfo.getMember(playerId);
		if (member.getPosition() != ZongMenConstants.ZONG_MEN_POSITION_ZONG_ZHU) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_permission_not_enough, "权限不足");
		}

		zongMenInfo.dissolveZongMen();
		return ZongmenOperationResult.success(true);
	}

	/**
	 * 设置宗门配置
	 * @param zongMenId 宗门ID
	 * @param request 设置请求
	 * @return 操作结果
	 */
	public Future<ZongmenOperationResult<Boolean>> setZongmenSetting(long zongMenId, ZongmenSettingRequest request) {
		ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
		if (zongMenInfo == null) {
			return Future.succeededFuture(ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_not_exist, "宗门不存在"));
		}

		ZongMenSetting setting = zongMenInfo.getModule().setting;
		ZongMenMember member = zongMenInfo.getMember(request.getOperatorId());
		GuildPermissionsConfig permissionsConfig = GuildPermissionsManager.instance().get(member.position);

		// 修改宗门名称
		if (!StringUtils.isEmpty(request.getName())) {
			if (!permissionsConfig.Rename) {
				return Future.succeededFuture(ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_permission_not_enough, "权限不足"));
			}
			return setting.changeZongmenName(zongMenInfo, request.getName(), request.getOperatorName())
					.map(success -> success ? ZongmenOperationResult.success(true)
							: ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_name_repeat, "宗门名称重复"));
		}

		// 其他设置修改
		if (!StringUtils.isEmpty(request.getWx())) {
			setting.changeWx(request.getWx());
		}
		if (!StringUtils.isEmpty(request.getNotice())) {
			if (!permissionsConfig.Notice) {
				return Future.succeededFuture(ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_permission_not_enough, "权限不足"));
			}
			setting.changeNotice(zongMenInfo, request.getNotice(), request.getOperatorName());
		}
		if (!StringUtils.isEmpty(request.getDeclaration())) {
			if (!permissionsConfig.Manifesto) {
				return Future.succeededFuture(ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_permission_not_enough, "权限不足"));
			}
			setting.changeDeclaration(zongMenInfo, request.getDeclaration(), request.getOperatorName());
		}
		if (request.getIcon() != 0 && setting.unlockIconMap.containsKey(request.getIcon())) {
			if (!permissionsConfig.Icon) {
				return Future.succeededFuture(ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_permission_not_enough, "权限不足"));
			}
			setting.changeIcon(zongMenInfo, request.getIcon());
		}
		if (request.getAutoJoin() == 1 || request.getAutoJoin() == 2 || request.getAutoJoin() == 3) {
			setting.setAutoJoin(request.getAutoJoin());
		}
		if (request.getTianDaoLevel() != 0) {
			setting.setTianDaoLevel(request.getTianDaoLevel());
		}

		return Future.succeededFuture(ZongmenOperationResult.success(true));
	}

	/**
	 * 设置成员职位
	 * @param zongMenId 宗门ID
	 * @param operatorId 操作者ID
	 * @param targetPlayerId 目标玩家ID
	 * @param position 新职位
	 * @return 操作结果
	 */
	public ZongmenOperationResult<Boolean> setMemberPosition(long zongMenId, long operatorId, long targetPlayerId, int position) {
		ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
		if (zongMenInfo == null) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_not_exist, "宗门不存在");
		}

		ZongMenMember operator = zongMenInfo.getMember(operatorId);
		ZongMenMember targetMember = zongMenInfo.getMember(targetPlayerId);
		if (operator == null || targetMember == null) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_player_member_not_exist, "成员不存在");
		}

		GuildPermissionsConfig permissionsConfig = GuildPermissionsManager.instance().get(operator.position);
		if (!permissionsConfig.Posts) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_permission_not_enough, "权限不足");
		}

		// 目标职位人数检查
		int targetPositionNum = zongMenInfo.getPositionMemberNum(position);
		GuildPermissionsConfig targetPermissionsConfig = GuildPermissionsManager.instance().get(position);
		if (targetPositionNum >= targetPermissionsConfig.Number && position != ZongMenConstants.ZONG_MEN_POSITION_ZONG_ZHU) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_position_member_num_not_enough, "该职位人数已满");
		}

		if (operator == targetMember || position == targetMember.getPosition()) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.request_parameter_error, "参数错误");
		}

		if (position == ZongMenConstants.ZONG_MEN_POSITION_ZONG_ZHU) {
			// 转让宗主
			zongMenInfo.zongZhuTransfer(operator, targetMember);
		} else {
			int oldPosition = targetMember.getPosition();
			targetMember.setPosition(position);
			zongMenInfo.handleEvent(ZongMenConstants.ZongMenEvenType.ZONG_MEN_POSITION_CHANGE, targetMember.playerId, oldPosition,
					position);
		}

		return ZongmenOperationResult.success(true);
	}

	/**
	 * 退出宗门
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @param playerName 玩家名称
	 * @return 操作结果
	 */
	public ZongmenOperationResult<Boolean> quitZongmen(long zongMenId, long playerId, String playerName) {
		ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
		if (zongMenInfo == null) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_not_exist, "宗门不存在");
		}

		ZongMenMember member = zongMenInfo.getMember(playerId);
		if (member == null) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_player_member_not_exist, "不是宗门成员");
		}

		// 对宗主的处理
		if (member.getPosition() == ZongMenConstants.ZONG_MEN_POSITION_ZONG_ZHU) {
			// 宗门没人了 直接解散
			if (zongMenInfo.getModule().menMemberMap.size() <= 1) {
				zongMenInfo.dissolveZongMen();
			} else {
				// 宗门有人存在 则不可退出 需要先把宗主转让出去
				return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_permission_not_enough, "宗主需要先转让职位才能退出");
			}
		} else {
			zongMenInfo.quitZongMen(member, playerName);
		}

		return ZongmenOperationResult.success(true);
	}

	/**
	 * 成员权限管理
	 * @param zongMenId 宗门ID
	 * @param request 权限请求
	 * @return 操作结果
	 */
	public ZongmenOperationResult<Boolean> updateMemberAuth(long zongMenId, MemberAuthRequest request) {
		ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
		if (zongMenInfo == null) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_not_exist, "宗门不存在");
		}

		ZongMenMember operator = zongMenInfo.getMember(request.getOperatorId());
		GuildPermissionsConfig permissionsConfig = GuildPermissionsManager.instance().get(operator.position);

		// 权限检查
		if ((request.getOptType() == 1 || request.getOptType() == 2) && !permissionsConfig.Approval) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_permission_not_enough, "权限不足");
		}
		if (request.getOptType() == 3 && !permissionsConfig.Rename) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_permission_not_enough, "权限不足");
		}

		// 数据校验
		for (long targetPid : request.getTargetPlayerIds()) {
			if ((request.getOptType() == 1 || request.getOptType() == 2) && !zongMenInfo.hasApply(targetPid)) {
				return ZongmenOperationResult.failure(ErrorMsgEnum.request_parameter_error, "申请不存在");
			}
			if (request.getOptType() == 3 && !zongMenInfo.isHasMember(targetPid)) {
				return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_player_member_not_exist, "成员不存在");
			}
		}

		if (request.getOptType() == 1 && zongMenInfo.isFull()) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_full, "宗门已满");
		}

		// 执行操作
		switch (request.getOptType()) {
		case 1: // 审批同意添加成员
			zongMenInfo.addMemberAuth(request.getTargetPlayerIds(), request.getOperatorName());
			break;
		case 2: // 审批拒绝添加成员
			zongMenInfo.removeApplyAuth(request.getTargetPlayerIds(), request.getOperatorName());
			break;
		case 3: // 踢人
			zongMenInfo.kickMember(request.getTargetPlayerIds(), request.getOperatorName());
			break;
		}

		return ZongmenOperationResult.success(true);
	}

	/**
	 * 更新宗门资产
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @param assetId 资产ID
	 * @param value 资产值
	 * @return 操作结果
	 */
	public ZongmenOperationResult<Boolean> updateZongmenAsset(long zongMenId, long playerId, int assetId, int value) {
		ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
		if (zongMenInfo == null) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_not_exist, "宗门不存在");
		}

		zongMenInfo.addZongMenAsset(playerId, assetId, value);
		return ZongmenOperationResult.success(true);
	}

	/**
	 * 领取宗门活跃度奖励
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @param indexList 奖励索引列表
	 * @return 操作结果
	 */
	public ZongmenOperationResult<Boolean> receiveActiveReward(long zongMenId, long playerId, List<Integer> indexList) {
		ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
		if (zongMenInfo == null) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_not_exist, "宗门不存在");
		}

		ZongMenMember member = zongMenInfo.getMember(playerId);
		if (member == null) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_player_member_not_exist, "不是宗门成员");
		}

		for (int index : indexList) {
			if (member.getRewardLivenessIndexList().contains(index)) {
				return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_active_reward_already_get, "奖励已领取");
			}
		}

		member.getRewardLivenessIndexList().addAll(indexList);
		return ZongmenOperationResult.success(true);
	}

	/**
	 * 购买宗门商店物品
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @param playerLv 玩家等级
	 * @param itemId 物品ID
	 * @param count 购买数量
	 * @return 操作结果
	 */
	public ZongmenOperationResult<Boolean> buyShopItem(long zongMenId, long playerId, int playerLv, int itemId, int count) {
		ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
		if (zongMenInfo == null) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_not_exist, "宗门不存在");
		}

		ZongMenMember member = zongMenInfo.getMember(playerId);
		if (member == null) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_player_member_not_exist, "不是宗门成员");
		}

		ZongmenStoreConfig config = ZongmenStoreManager.instance().getNullable(itemId);
		if (config == null) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.config_data_not_found, "配置数据未找到");
		}
		if (playerLv < config.LevelUnlock) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.level_not_enough, "等级不足");
		}

		ShopItemConfig itemConfig = ShopItemManager.instance().getNullable(config.Item);
		if (itemConfig == null) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.config_data_not_found, "配置数据未找到");
		}

		if (itemConfig.ShopItemQuota != 0 && member.buyShopItemNumMap.getOrDefault(itemConfig, 0) + count > itemConfig.ShopItemQuota) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.shop_item_buy_count_max, "购买数量超过限制");
		}

		if (!zongMenInfo.isEnoughAsset(itemConfig.PurchaseParameter, count, member)) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.resource_not_enough, "资源不足");
		}

		zongMenInfo.costAsset(itemConfig.PurchaseParameter, count, member);
		member.addShopItemNum(itemId, count);
		return ZongmenOperationResult.success(true);
	}

	/**
	 * 宗门砍价
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @return 操作结果
	 */
	public ZongmenOperationResult<Integer> bargain(long zongMenId, long playerId) {
		ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
		if (zongMenInfo == null) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_not_exist, "宗门不存在");
		}

		ZongMenMember member = zongMenInfo.getMember(playerId);
		if (member == null) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_player_member_not_exist, "不是宗门成员");
		}
		if (member.isBargain) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.repeat_request, "已经砍价过了");
		}

		ZongMenBargain bargain = zongMenInfo.getModule().getBargain();
		int bargainCount = bargain.performBargain(member);

		return ZongmenOperationResult.success(bargainCount);
	}

	/**
	 * 砍价购买
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @return 操作结果
	 */
	public ZongmenOperationResult<Boolean> buyBargain(long zongMenId, long playerId) {
		ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
		if (zongMenInfo == null) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_not_exist, "宗门不存在");
		}

		ZongMenMember member = zongMenInfo.getMember(playerId);
		if (member == null) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_player_member_not_exist, "不是宗门成员");
		}
		if (!member.isBargain) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_player_not_bargain, "尚未砍价");
		}

		member.setBargainBuy(true);
		return ZongmenOperationResult.success(true);
	}

	/**
	 * 更新成员战斗力
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @param fightPower 战斗力
	 * @return 操作结果
	 */
	public ZongmenOperationResult<Boolean> updateMemberFightPower(long zongMenId, long playerId, int fightPower) {
		ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
		if (zongMenInfo == null) {
			return ZongmenOperationResult.success(false); // 静默失败
		}

		ZongMenMember member = zongMenInfo.getMember(playerId);
		if (member == null) {
			return ZongmenOperationResult.success(false); // 静默失败
		}

		member.setPower(fightPower);
		return ZongmenOperationResult.success(true);
	}

	/**
	 * 更新贡献度
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @param value 贡献度值
	 * @return 操作结果
	 */
	public ZongmenOperationResult<Boolean> updateContributeValue(long zongMenId, long playerId, int value) {
		ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongMenId);
		if (zongMenInfo == null) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_not_exist, "宗门不存在");
		}

		ZongMenMember member = zongMenInfo.getMember(playerId);
		if (member == null) {
			return ZongmenOperationResult.failure(ErrorMsgEnum.zong_men_player_member_not_exist, "不是宗门成员");
		}

		member.setTotalContribution(value);
		return ZongmenOperationResult.success(true);
	}

	private void fail(ErrorMsgEnum errorMsgEnum) {
		throw new LogicException(errorMsgEnum.ID);
	}
}