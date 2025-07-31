package cn.game.games.net.cross.zongmen.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import cn.game.core.exception.LogicException;
import cn.game.core.net.remote.RemoteProxy;
import cn.game.games.net.cross.zongmen.ZongMen;
import cn.game.games.net.cross.zongmen.ZongMenBargain;
import cn.game.games.net.cross.zongmen.ZongMenConstants;
import cn.game.games.net.cross.zongmen.ZongMenManager;
import cn.game.games.net.cross.zongmen.ZongMenMember;
import cn.game.games.net.cross.zongmen.ZongMenSetting;
import cn.game.games.net.cross.zongmen.dto.MemberAuthRequest;
import cn.game.games.net.cross.zongmen.dto.ZongmenSettingRequest;
import cn.game.protocol.generated.config.GuildPermissionsConfig;
import cn.game.protocol.generated.manager.GuildPermissionsManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.ZongMenMsg;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenShowInfo;
import io.vertx.core.Future;

/**
 * @ClassName ZongmenService
 * @description: 宗门服务 - 宗门系统的核心业务逻辑服务
 * @author: ly
 * @create: 2025-02-08 14:45 @Version 1.0
 */
@Component
public class ZongmenService implements RemoteProxy, ZongmenServiceInterface {
	private static final ZongmenServiceInterface INSTANCE  = new ZongmenService();
	private static final Logger log = LoggerFactory.getLogger(ZongmenService.class);

	protected ZongmenService() {
	}

	public static ZongmenServiceInterface getInstance() {
		return INSTANCE;
	}
	@Bean
	public static ZongmenServiceInterface zongmenService() {
		log.info("Spring is getting the bean from static @Bean factory method.");
		return INSTANCE;
	}
    
	/** 失败处理：抛出业务异常 */
	private void fail(ErrorMsgEnum errorMsgEnum) {
		throw new LogicException(errorMsgEnum.ID);
	}

	/**
	 * 创建宗门
	 * @param request 创建宗门请求
	 * @return 新宗门信息
	 */
	@Override
	public Future<ZongMen> createZongmen(ZongMenMsg.createZongMenRequest_40000005 req, long createPlayerId) {
		return ZongMenManager.getInstance().createZongMen(req, createPlayerId);
	}

	/**
	 * 获取宗门信息
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @return 宗门信息
	 */
	@Override
	public ZongMen getZongmen(long zongMenId) {
		ZongMen zongMenInfo = ZongMenManager.getInstance().getZongMen(zongMenId);
		if (zongMenInfo == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}
		return zongMenInfo;
	}
	
	@Override
	public ZongMenShowInfo getZongmenShowInfo(long zongMenId) {
		ZongMen zongMen = ZongMenManager.getInstance().getZongMen(zongMenId);
		if (zongMen == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}
		return zongMen.toShowProto();
	}

	/**
	 * 申请加入宗门
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @param playerName 玩家名称
	 * @param power 战斗力
	 * @return 宗门信息
	 */
	@Override
	public ZongMen applyJoinZongmen(long zongMenId, long playerId, String playerName, int power) {
		ZongMen zongMenInfo = ZongMenManager.getInstance().getZongMen(zongMenId);
		if (zongMenInfo == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}
		if (zongMenInfo.isHasMember(playerId)) {
			fail(ErrorMsgEnum.zong_men_player_apply_has);
		}
		if (zongMenInfo.isFull()) {
			fail(ErrorMsgEnum.zong_men_full);
		}
		if (zongMenInfo.hasApply(playerId)) {
			fail(ErrorMsgEnum.zong_men_apply_exist);
		}

		// 开启自动加入 则直接加入宗门
		if (zongMenInfo.isAutoJoin()) {
			zongMenInfo.joinZongMen(playerId, ZongMenConstants.ZONG_MEN_POSITION_BANG_ZHONG);
		} else if (zongMenInfo.getModule().setting.getAutoJoin() == 2) {
			zongMenInfo.applyJoin(playerId);
		} else {
			fail(ErrorMsgEnum.zong_men_not_allow_join);
		}
		return zongMenInfo;
	}

	/**
	 * 解散宗门
	 * @param zongMenId 宗门ID
	 * @param playerId 操作玩家ID
	 * @return 是否成功
	 */
	@Override
	public void dissolveZongmen(long zongMenId, long playerId) {
		ZongMen zongMenInfo = ZongMenManager.getInstance().getZongMen(zongMenId);
		if (zongMenInfo == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}
		ZongMenMember member = zongMenInfo.getMember(playerId);
		if (member.getPosition() != ZongMenConstants.ZONG_MEN_POSITION_ZONG_ZHU) {
			fail(ErrorMsgEnum.zong_men_permission_not_enough);
		}
		zongMenInfo.dissolveZongMen();
	}

	/**
	 * 设置宗门配置
	 * @param zongMenId 宗门ID
	 * @param request 设置请求
	 * @return 是否成功
	 */
	@Override
	public boolean setZongmenSetting(long zongMenId, ZongmenSettingRequest request) {
		ZongMen zongMenInfo = ZongMenManager.getInstance().getZongMen(zongMenId);
		if (zongMenInfo == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}

		ZongMenSetting setting = zongMenInfo.getModule().setting;
		ZongMenMember member = zongMenInfo.getMember(request.getOperatorId());
		GuildPermissionsConfig permissionsConfig = GuildPermissionsManager.instance().get(member.position);

		// 修改宗门名称
		if (!StringUtils.isEmpty(request.getName())) {
			if (!permissionsConfig.Rename) {
				fail(ErrorMsgEnum.zong_men_permission_not_enough);
			}
			 if (!setting.changeZongmenName(zongMenInfo, request.getName(), request.getOperatorName())) {
				return false ; 
			};
		}

		// 其他设置修改
		if (!StringUtils.isEmpty(request.getWx())) {
			setting.changeWx(request.getWx());
		}
		if (!StringUtils.isEmpty(request.getNotice())) {
			if (!permissionsConfig.Notice) {
				fail(ErrorMsgEnum.zong_men_permission_not_enough);
			}
			setting.changeNotice(zongMenInfo, request.getNotice(), request.getOperatorName());
		}
		if (!StringUtils.isEmpty(request.getDeclaration())) {
			if (!permissionsConfig.Manifesto) {
				fail(ErrorMsgEnum.zong_men_permission_not_enough);
			}
			setting.changeDeclaration(zongMenInfo, request.getDeclaration(), request.getOperatorName());
		}
		if (request.getIcon() != 0 && setting.unlockIconMap.containsKey(request.getIcon())) {
			if (!permissionsConfig.Icon) {
				fail(ErrorMsgEnum.zong_men_permission_not_enough);
			}
			setting.changeIcon(zongMenInfo, request.getIcon());
		}
		if (request.getAutoJoin() == 1 || request.getAutoJoin() == 2 || request.getAutoJoin() == 3) {
			setting.setAutoJoin(request.getAutoJoin());
		}
		if (request.getTianDaoLevel() != 0) {
			setting.setTianDaoLevel(request.getTianDaoLevel());
		}
		return true;
	}

	/**
	 * 设置成员职位
	 * @param zongMenId 宗门ID
	 * @param operatorId 操作者ID
	 * @param targetPlayerId 目标玩家ID
	 * @param position 新职位
	 * @return 是否成功
	 */
	@Override
	public void setMemberPosition(long zongMenId, long operatorId, long targetPlayerId, int position) {
		System.out.println("收到宗门设置成员职位请求: zongMenId=" + zongMenId + ", operatorId=" + operatorId + ", targetPlayerId=" + targetPlayerId
				+ ", position=" + position);
		ZongMen zongMenInfo = ZongMenManager.getInstance().getZongMen(zongMenId);
		if (zongMenInfo == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}
		ZongMenMember operator = zongMenInfo.getMember(operatorId);
		ZongMenMember targetMember = zongMenInfo.getMember(targetPlayerId);
		if (operator == null || targetMember == null) {
			fail(ErrorMsgEnum.zong_men_player_member_not_exist);
		}
		GuildPermissionsConfig permissionsConfig = GuildPermissionsManager.instance().get(operator.position);
		if (!permissionsConfig.Posts) {
			fail(ErrorMsgEnum.zong_men_permission_not_enough);
		}
		// 目标职位人数检查
		int targetPositionNum = zongMenInfo.getPositionMemberNum(position);
		GuildPermissionsConfig targetPermissionsConfig = GuildPermissionsManager.instance().get(position);
		if (targetPositionNum >= targetPermissionsConfig.Number && position != ZongMenConstants.ZONG_MEN_POSITION_ZONG_ZHU) {
			fail(ErrorMsgEnum.zong_men_position_member_num_not_enough);
		}
		if (operator == targetMember || position == targetMember.getPosition()) {
			fail(ErrorMsgEnum.request_parameter_error);
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
	}

	/**
	 * 退出宗门
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @param playerName 玩家名称
	 * @return 是否成功
	 */
	@Override
	public void quitZongmen(long zongMenId, long playerId, String playerName) {
		ZongMen zongMenInfo = ZongMenManager.getInstance().getZongMen(zongMenId);
		if (zongMenInfo == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}
		ZongMenMember member = zongMenInfo.getMember(playerId);
		if (member == null) {
			fail(ErrorMsgEnum.zong_men_player_member_not_exist);
		}
		// 对宗主的处理
		if (member.getPosition() == ZongMenConstants.ZONG_MEN_POSITION_ZONG_ZHU) {
			// 宗门没人了 直接解散
			if (zongMenInfo.getModule().menMemberMap.size() <= 1) {
				zongMenInfo.dissolveZongMen();
			} else {
				// 宗门有人存在 则不可退出 需要先把宗主转让出去
				fail(ErrorMsgEnum.zong_men_permission_not_enough);
			}
		} else {
			zongMenInfo.quitZongMen(member, playerName);
		}
	}

	/**
	 * 成员权限管理
	 * @param zongMenId 宗门ID
	 * @param request 权限请求
	 * @return 是否成功
	 */
	@Override
	public void updateMemberAuth(long zongMenId, MemberAuthRequest request) {
		ZongMen zongMenInfo = ZongMenManager.getInstance().getZongMen(zongMenId);
		if (zongMenInfo == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}
		ZongMenMember operator = zongMenInfo.getMember(request.getOperatorId());
		GuildPermissionsConfig permissionsConfig = GuildPermissionsManager.instance().get(operator.position);

		// 权限检查
		if ((request.getOptType() == 1 || request.getOptType() == 2) && !permissionsConfig.Approval) {
			fail(ErrorMsgEnum.zong_men_permission_not_enough);
		}
		if (request.getOptType() == 3 && !permissionsConfig.Rename) {
			fail(ErrorMsgEnum.zong_men_permission_not_enough);
		}

		// 数据校验
		for (long targetPid : request.getTargetPlayerIds()) {
			if ((request.getOptType() == 1 || request.getOptType() == 2) && !zongMenInfo.hasApply(targetPid)) {
				fail(ErrorMsgEnum.request_parameter_error);
			}
			if (request.getOptType() == 3 && !zongMenInfo.isHasMember(targetPid)) {
				fail(ErrorMsgEnum.zong_men_player_member_not_exist);
			}
		}

		if (request.getOptType() == 1 && zongMenInfo.isFull()) {
			fail(ErrorMsgEnum.zong_men_full);
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
	}

	/**
	 * 更新宗门资产
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @param assetId 资产ID
	 * @param value 资产值
	 * @return 是否成功
	 */
	@Override
	public void updateZongmenAsset(long zongMenId, long playerId, int assetId, int value) {
		ZongMen zongMenInfo = ZongMenManager.getInstance().getZongMen(zongMenId);
		if (zongMenInfo == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}
		zongMenInfo.addZongMenAsset(playerId, assetId, value);
	}

	/**
	 * 领取宗门活跃度奖励
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @param indexList 奖励索引列表
	 * @return 是否成功
	 */
	@Override
	public void receiveActiveReward(long zongMenId, long playerId, List<Integer> indexList) {
		ZongMen zongMenInfo = ZongMenManager.getInstance().getZongMen(zongMenId);
		if (zongMenInfo == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}
		ZongMenMember member = zongMenInfo.getMember(playerId);
		if (member == null) {
			fail(ErrorMsgEnum.zong_men_player_member_not_exist);
		}
		for (int index : indexList) {
			if (member.getRewardLivenessIndexList().contains(index)) {
				fail(ErrorMsgEnum.zong_men_active_reward_already_get);
			}
		}
		member.getRewardLivenessIndexList().addAll(indexList);
	}

	/**
	 * 宗门砍价
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @return 砍价次数
	 */
	@Override
	public int bargain(long zongMenId, long playerId) {
		ZongMen zongMenInfo = ZongMenManager.getInstance().getZongMen(zongMenId);
		if (zongMenInfo == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}
		ZongMenMember member = zongMenInfo.getMember(playerId);
		if (member == null) {
			fail(ErrorMsgEnum.zong_men_player_member_not_exist);
		}
		if (member.isBargain) {
			fail(ErrorMsgEnum.repeat_request);
		}

		ZongMenBargain bargain = zongMenInfo.getModule().getBargain();
		int bargainCount = bargain.performBargain(member);
		return bargainCount;
	}

	/**
	 * 砍价购买
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @return 是否成功
	 */
	@Override
	public void buyBargain(long zongMenId, long playerId) {
		ZongMen zongMenInfo = ZongMenManager.getInstance().getZongMen(zongMenId);
		if (zongMenInfo == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}
		ZongMenMember member = zongMenInfo.getMember(playerId);
		if (member == null) {
			fail(ErrorMsgEnum.zong_men_player_member_not_exist);
		}
		if (!member.isBargain) {
			fail(ErrorMsgEnum.zong_men_player_not_bargain);
		}
		member.setBargainBuy(true);
	}

	/**
	 * 更新成员战斗力
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @param fightPower 战斗力
	 * @return 是否成功
	 */
	@Override
	public boolean updateMemberFightPower(long zongMenId, long playerId, int fightPower) {
		ZongMen zongMenInfo = ZongMenManager.getInstance().getZongMen(zongMenId);
		if (zongMenInfo == null) {
			return false; // 静默失败
		}
		ZongMenMember member = zongMenInfo.getMember(playerId);
		if (member == null) {
			return false; // 静默失败
		}
		return true;
	}

	/**
	 * 更新贡献度
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @param value 贡献度值
	 * @return 是否成功
	 */
	@Override
	public void updateContributeValue(long zongMenId, long playerId, int value) {
		ZongMen zongMenInfo = ZongMenManager.getInstance().getZongMen(zongMenId);
		if (zongMenInfo == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}
		ZongMenMember member = zongMenInfo.getMember(playerId);
		if (member == null) {
			fail(ErrorMsgEnum.zong_men_player_member_not_exist);
		}
		member.setTotalContribution(value);
	}
}