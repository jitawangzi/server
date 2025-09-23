package cn.game.games.net.cross.guild.service;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import cn.game.core.base.ServerContext;
import cn.game.core.exception.LogicException;
import cn.game.core.net.remote.RemoteProxy;
import cn.game.games.net.cross.guild.Guild;
import cn.game.games.net.cross.guild.GuildBargain;
import cn.game.games.net.cross.guild.GuildConstants;
import cn.game.games.net.cross.guild.GuildManager;
import cn.game.games.net.cross.guild.GuildMember;
import cn.game.games.net.cross.guild.GuildSetting;
import cn.game.games.net.cross.guild.dto.MemberAuthRequest;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.cross.guild.dto.GuildSettingRequest;
import cn.game.protocol.generated.config.GuildBargainConfig;
import cn.game.protocol.generated.config.GuildPermissionsConfig;
import cn.game.protocol.generated.manager.GuildBargainManager;
import cn.game.protocol.generated.manager.GuildPermissionsManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.GuildMsg.GuildServiceInfo;
import cn.game.protocol.protobuf.GuildMsg.GuildShowInfo;
import io.vertx.core.Future;

/**    
 * 公会服务 - 公会系统的核心业务逻辑服务
 * 2025年8月1日 13:33:44
 * @author SYQ
 */
@Component
public class GuildService implements RemoteProxy, GuildServiceInterface {
	private static final GuildServiceInterface INSTANCE  = new GuildService();
	private static final Logger log = LoggerFactory.getLogger(GuildService.class);

	protected GuildService() {
	}

	public static GuildServiceInterface getInstance() {
		return INSTANCE;
	}
	@Bean
	public static GuildServiceInterface guildService() {
		log.info("Spring is getting the bean from static @Bean factory method.");
		return INSTANCE;
	}
    
	/** 失败处理：抛出业务异常 */
	private void fail(ErrorMsgEnum errorMsgEnum) {
		throw new LogicException(errorMsgEnum.ID);
	}

	@Override
	public Future<GuildServiceInfo> createGuild(long createPlayerId, String name, String notice, String declaration, int icon,int joinType) {
		return GuildManager.getInstance().createGuild(createPlayerId,name,notice,declaration,icon,joinType).map(r -> {
			return r.toProto();
		});
	}

	/**
	 * 获取公会信息
	 * @param guildId 公会ID
	 * @param playerId 玩家ID
	 * @return 公会信息
	 */
	@Override
	public Guild getGuild(long guildId) {
		Guild guildInfo = GuildManager.getInstance().getGuild(guildId);
		if (guildInfo == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}
		return guildInfo;
	}
	
	@Override
	public GuildShowInfo getGuildShowInfo(long guildId) {
		Guild guild = GuildManager.getInstance().getGuild(guildId);
		if (guild == null) {
			return null; 
		}
		return guild.toShowProto();
	}
	@Override
	public String getGuildName(long guildId) {
		Guild guild = GuildManager.getInstance().getGuild(guildId);
		if (guild == null) {
			return null; 
		}
		return guild.getName();
	}
	@Override
	public GuildServiceInfo getGuildAllInfoForMember(long guildId) {
		Guild guild = GuildManager.getInstance().getGuild(guildId);
		if (guild == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}
		return guild.toProto();
	}

	/**
	 * 申请加入公会
	 * @param guildId 公会ID
	 * @param playerId 玩家ID
	 * @param playerName 玩家名称
	 * @param power 战斗力
	 * @return 公会信息
	 */
	@Override
	public GuildServiceInfo applyJoinGuild(long guildId, long playerId) {
		Guild guildInfo = GuildManager.getInstance().getGuild(guildId);
		if (guildInfo == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}
		if (guildInfo.isHasMember(playerId)) {
			fail(ErrorMsgEnum.zong_men_player_apply_has);
		}
		if (guildInfo.isFull()) {
			fail(ErrorMsgEnum.zong_men_full);
		}
		if (guildInfo.hasApply(playerId)) {
			fail(ErrorMsgEnum.zong_men_apply_exist);
		}
		if (guildInfo.isApplyFull()) {
			fail(ErrorMsgEnum.zong_men_apply_max);
		}
		// 开启自动加入 则直接加入公会
		if (guildInfo.isAutoJoin() && !guildInfo.isFull()) {
			boolean joinGuild = guildInfo.joinGuild(playerId, GuildConstants.ZONG_MEN_POSITION_BANG_ZHONG);
			if (joinGuild) {
				return guildInfo.toProto(); 
			}
		} else if (guildInfo.getModule().setting.getAutoJoin() == 2) {
			guildInfo.applyJoin(playerId);
		} else {
			fail(ErrorMsgEnum.zong_men_not_allow_join);
		}
		return null;
	}

	/**
	 * 解散公会
	 * @param guildId 公会ID
	 * @param playerId 操作玩家ID
	 * @return 是否成功
	 */
	@Override
	public void dissolveGuild(long guildId, long playerId) {
		Guild guildInfo = GuildManager.getInstance().getGuild(guildId);
		if (guildInfo == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}
		GuildMember member = guildInfo.getMember(playerId);
		if (member.getPosition() != GuildConstants.ZONG_MEN_POSITION_ZONG_ZHU) {
			fail(ErrorMsgEnum.zong_men_permission_not_enough);
		}
		guildInfo.dissolveGuild();
	}

	/**
	 * 设置公会配置
	 * @param guildId 公会ID
	 * @param request 设置请求
	 * @return 是否成功
	 */
	@Override
	public boolean setGuildSetting(long guildId, GuildSettingRequest request) {
		Guild guildInfo = GuildManager.getInstance().getGuild(guildId);
		if (guildInfo == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}

		GuildSetting setting = guildInfo.getModule().setting;
		GuildMember member = guildInfo.getMember(request.getOperatorId());
		GuildPermissionsConfig permissionsConfig = GuildPermissionsManager.instance().get(member.position);

		// 修改公会名称
		if (!StringUtils.isEmpty(request.getName())) {
			if (!permissionsConfig.Rename) {
				fail(ErrorMsgEnum.zong_men_permission_not_enough);
			}
			 if (!setting.changeGuildName(guildInfo, request.getName(), request.getOperatorName())) {
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
			setting.changeNotice(guildInfo, request.getNotice(), request.getOperatorName());
		}
		if (!StringUtils.isEmpty(request.getDeclaration())) {
			if (!permissionsConfig.Manifesto) {
				fail(ErrorMsgEnum.zong_men_permission_not_enough);
			}
			setting.changeDeclaration(guildInfo, request.getDeclaration(), request.getOperatorName());
		}
		if (request.getIcon() != 0 && setting.unlockIconMap.containsKey(request.getIcon())) {
			if (!permissionsConfig.Icon) {
				fail(ErrorMsgEnum.zong_men_permission_not_enough);
			}
			setting.changeIcon(guildInfo, request.getIcon());
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
	 * @param guildId 公会ID
	 * @param operatorId 操作者ID
	 * @param targetPlayerId 目标玩家ID
	 * @param position 新职位
	 * @return 是否成功
	 */
	@Override
	public void setMemberPosition(long guildId, long operatorId, long targetPlayerId, int position) {
		System.out.println("收到公会设置成员职位请求: guildId=" + guildId + ", operatorId=" + operatorId + ", targetPlayerId=" + targetPlayerId
				+ ", position=" + position);
		Guild guildInfo = GuildManager.getInstance().getGuild(guildId);
		if (guildInfo == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}
		GuildMember operator = guildInfo.getMember(operatorId);
		GuildMember targetMember = guildInfo.getMember(targetPlayerId);
		if (operator == null || targetMember == null) {
			fail(ErrorMsgEnum.zong_men_player_member_not_exist);
		}
		GuildPermissionsConfig permissionsConfig = GuildPermissionsManager.instance().get(operator.position);
		if (!permissionsConfig.Posts) {
			fail(ErrorMsgEnum.zong_men_permission_not_enough);
		}
		// 目标职位人数检查
		int targetPositionNum = guildInfo.getPositionMemberNum(position);
		GuildPermissionsConfig targetPermissionsConfig = GuildPermissionsManager.instance().get(position);
		if (targetPositionNum >= targetPermissionsConfig.Number && position != GuildConstants.ZONG_MEN_POSITION_ZONG_ZHU) {
			fail(ErrorMsgEnum.zong_men_position_member_num_not_enough);
		}
		if (operator == targetMember || position == targetMember.getPosition()) {
			fail(ErrorMsgEnum.request_parameter_error);
		}

		if (position == GuildConstants.ZONG_MEN_POSITION_ZONG_ZHU) {
			// 转让宗主
			guildInfo.zongZhuTransfer(operator, targetMember);
		} else {
			int oldPosition = targetMember.getPosition();
			targetMember.setPosition(position);
			guildInfo.handleEvent(GuildConstants.GuildEvenType.ZONG_MEN_POSITION_CHANGE, targetMember.playerId, oldPosition,
					position);
		}
	}

	/**
	 * 退出公会
	 * @param guildId 公会ID
	 * @param playerId 玩家ID
	 * @param playerName 玩家名称
	 * @return 是否成功
	 */
	@Override
	public void quitGuild(long guildId, long playerId, String playerName) {
		Guild guildInfo = GuildManager.getInstance().getGuild(guildId);
		if (guildInfo == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}
		GuildMember member = guildInfo.getMember(playerId);
		if (member == null) {
			fail(ErrorMsgEnum.zong_men_player_member_not_exist);
		}
		// 对宗主的处理
		if (member.getPosition() == GuildConstants.ZONG_MEN_POSITION_ZONG_ZHU) {
			// 公会没人了 直接解散
			if (guildInfo.getModule().menMemberMap.size() <= 1) {
				guildInfo.dissolveGuild();
			} else {
				// 公会有人存在 则不可退出 需要先把宗主转让出去
				fail(ErrorMsgEnum.zong_men_permission_not_enough);
			}
		} else {
			guildInfo.quitGuild(member, playerName);
		}
	}

	/**
	 * 成员权限管理
	 * @param guildId 公会ID
	 * @param request 权限请求
	 * @return 是否成功
	 */
	@Override
	public void updateMemberAuth(long guildId, MemberAuthRequest request) {
		Guild guildInfo = GuildManager.getInstance().getGuild(guildId);
		if (guildInfo == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}
		GuildMember operator = guildInfo.getMember(request.getOperatorId());
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
			if ((request.getOptType() == 1 || request.getOptType() == 2) && !guildInfo.hasApply(targetPid)) {
				fail(ErrorMsgEnum.request_parameter_error);
			}
			if (request.getOptType() == 3 && !guildInfo.isHasMember(targetPid)) {
				fail(ErrorMsgEnum.zong_men_player_member_not_exist);
			}
		}

		if (request.getOptType() == 1 && guildInfo.isFull()) {
			fail(ErrorMsgEnum.zong_men_full);
		}

		// 执行操作
		switch (request.getOptType()) {
		case 1: // 审批同意添加成员
			guildInfo.addMemberAuth(request.getTargetPlayerIds(), request.getOperatorName());
			break;
		case 2: // 审批拒绝添加成员
			guildInfo.removeApplyAuth(request.getTargetPlayerIds(), request.getOperatorName());
			break;
		case 3: // 踢人
			guildInfo.kickMember(request.getTargetPlayerIds(), request.getOperatorName());
			break;
		}
	}

	@Override
	public Future<?> addGuildAsset(long guildId, long playerId, int assetId, int value) {
		Guild guildInfo = GuildManager.getInstance().getGuild(guildId);
		if (guildInfo == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}
		guildInfo.addGuildAsset(playerId, assetId, value);
		
		return Future.succeededFuture();
	}
	@Override
	public Future<?> addMemberContribute(long guildId, long playerId, int value) {
		Guild guildInfo = GuildManager.getInstance().getGuild(guildId);
		if (guildInfo == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}
		GuildMember member = guildInfo.getMember(playerId); 
		if (member != null) {
			member.addcontribution(value);
		}
		return Future.succeededFuture();
	}

	/**
	 * 公会砍价
	 * @param guildId 公会ID
	 * @param playerId 玩家ID
	 * @return 砍价次数
	 */
	@Override
	public int[] bargain(long guildId, long playerId) {
		Guild guildInfo = GuildManager.getInstance().getGuild(guildId);
		if (guildInfo == null) {
			fail(ErrorMsgEnum.zong_men_not_exist);
		}
		GuildMember member = guildInfo.getMember(playerId);
		if (member == null) {
			fail(ErrorMsgEnum.zong_men_player_member_not_exist);
		}

		GuildBargain bargain = guildInfo.getModule().getBargain();
		int bargainCount = bargain.performBargain(playerId,guildInfo.getLv());
		return new int[] {bargain.getBargainItemId(), bargainCount};
	}

	@Override
	public GuildServiceInfo randomJoin(long playerId) {
		String serverId = PlayerHelper.getServerId(playerId); 
		Collection<Guild> allGuild = GuildManager.getInstance().getAllGuild(); 
		for (Guild guild : allGuild) {
            if (guild.getData().getServerId().equals(serverId) && guild.canAutoJoin()) {
                return ServerContext.getInstance().getProcessor().process(guild.getId(), () -> {
                	if (guild.isFull()) {
                		return null ; 
					}
                	guild.addMember(List.of(playerId)); 
                	return guild.toProto() ; 
                }) ; 
            }
		}
		return null ; 
	}
	

	@Override
	public int[] getBargainPrice(long guildId) {
		Guild guildInfo = GuildManager.getInstance().getGuild(guildId);
		GuildBargain bargain = guildInfo.getModule().getBargain();
		GuildBargainConfig guildBargainConfig = GuildBargainManager.instance().get(bargain.getBargainItemId());
		int bargainTotalNum = bargain.getBargainTotalNum();
		int price = guildBargainConfig.Price[1] - bargainTotalNum;
		return new int[] {guildBargainConfig.ID,price};
	}

	@Override
	public Future<?> donate(long guildId, long playerId, int donateType) {
		Guild guild = GuildManager.getInstance().getGuild(guildId);
		GuildMember member = guild.getMember(playerId); 
		if (member != null) {
			member.setTotalDonateCount(member.getTotalDonateCount() + 1);
		}
		return Future.succeededFuture();
	}

	@Override
	public boolean hasPendingApplication(long guildId, long playerId) {
		Guild guild = GuildManager.getInstance().getGuild(guildId);
		GuildMember member = guild.getMember(playerId); 
		GuildPermissionsConfig guildPermissionsConfig = GuildPermissionsManager.instance().get(member.position); 
		if (!guildPermissionsConfig.Approval) {
			return false; 
		}
		return guild.hasApply();
	}
}