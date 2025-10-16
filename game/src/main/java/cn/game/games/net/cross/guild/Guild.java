package cn.game.games.net.cross.guild;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.core.util.AsyncUtils;
import cn.game.games.cache.entity.GuildJoin;
import cn.game.games.cache.entity.GuildData;
import cn.game.games.core.SimplePlayer;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.GuildBasicConfig;
import cn.game.protocol.generated.config.GuildPermissionsConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.GuildBasicManager;
import cn.game.protocol.generated.manager.GuildPermissionsManager;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.GuildMsg;
import cn.game.protocol.protobuf.GuildMsg.GuildSharedInfo;
import cn.game.protocol.protobuf.GuildMsg.GuildShowInfo;
import cn.game.util.DateUtil;
import cn.game.util.GameUtil;
import cn.game.util.JsonUtil;
import cn.game.util.RedisUtil;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;

/**
 * @ClassName Guild
 *
 * @description:  公会对象
 * @author: ly
 * @create: 2025-02-05 14:45 @Version 1.0
 */
public class Guild {
	private static final Logger LOGGER = LoggerFactory.getLogger(Guild.class);
	
	/**数据库 t_guild 表的数据*/
	private GuildData data;
	/**公会模块的数据 */
	private GuildModuleData module;
	/**保存数据到数据库的时间戳*/
	private long saveDataTimer;

	public Guild() {
	}

	public Guild(GuildData data) {
		this.data = data;
		module = JsonUtil.parseObjectWithType(this.data.getModules());
		module.registerAllModuleEventHandler();
	}

	public void init(String name,String notice,String declaration,int icon,int joinType, long newGuildId, long createPlayerId) {
		SimplePlayer creator = PlayerHelper.getSimplePlayer(createPlayerId);

		module = new GuildModuleData();
		saveDataTimer = System.currentTimeMillis() + GuildConstants.SAVE_ZONG_MEN_DATA_TIMER;
		// 初始化 Guild 对象
		data = new GuildData();
		data.setName(name);
		data.setId(newGuildId);
		data.setLv((byte) 1);
		data.setIcon(icon == 0 ? GlobalConst.GuildIconRes : icon);
		data.setNotice(StringUtils.isEmpty(notice) ? GlobalConst.GuildGonggao : notice);
		data.setNotification(StringUtils.isEmpty(declaration) ? GlobalConst.GuildXuanyan : declaration);
		data.setCreateTime(DateUtil.getTimeByPattern(new Date(), DateUtil.pattern_en));
		data.setExp(0);
		data.setServerId(creator.getServerId());

		// 初始化各个模块
		module = new GuildModuleData();
		module.init(newGuildId,joinType,creator.getName());
		module.afterInit(this);
		module.registerAllModuleEventHandler();

		module.handleEvent(GuildConstants.GuildEvenType.ZONG_MEN_CREATE, this, createPlayerId, creator.getName());
		module.handleEvent(GuildConstants.GuildEvenType.ZONG_MEN_LEVEL_UP, this, getLv());
		joinGuild(createPlayerId, GuildConstants.ZONG_MEN_POSITION_ZONG_ZHU);
	}

	public boolean joinGuild(long joinPlayerId, int position) {
		
		GuildJoin guildJoin = new GuildJoin();
		guildJoin.setPlayerId(joinPlayerId);
		guildJoin.setGuildId(getId());
		guildJoin.setCreateTime(System.currentTimeMillis());
		Future<@Nullable Object> insert = guildJoin.insert(); 
		try {
			@Nullable
			Object await = AsyncUtils.await(insert);
		} catch (Exception e) {
			// 可能重复加入
			LOGGER.warn(joinPlayerId + " joinGuild failed",e);
			return false ; 
		} 
		
		GuildMember member = new GuildMember(joinPlayerId, position);
		module.addMember(member, this);
		module.handleEvent(GuildConstants.GuildEvenType.JOIN_ZONG_MEN, this, member, member.getName());
		return true ; 
	}

	/** 
	 * 判断一个公会是否可以自动加入
	 * 一般需要人数没满，并且设置了自动加入
	 * @return
	 */
	public boolean canAutoJoin() {
		return !isFull() && isAutoJoin();
	}

	public GuildData getData() {
		return data;
	}

	public void setData(GuildData data) {
		this.data = data;
	}

	public long getSaveDataTimer() {
		return saveDataTimer;
	}

	public void setSaveDataTimer(long saveDataTimer) {
		this.saveDataTimer = saveDataTimer;
	}

	public void updateModuleData() {
		data.setModules(JsonUtil.toJsonStringWithType(module));
	}

	public long getId() {
		return data.getId();
	}

	public int getLv() {
		return data.getLv();
	}

	public void setLv(int lv) {
		this.data.setLv((byte) lv);
	}

	public String getName() {
		return data.getName();
	}

	public void setName(String name) {
		data.setName(name);
	}

	public void setIcon(int icon) {
		data.setIcon(icon);
	}

	public int getIcon() {
		return data.getIcon();
	}

	public void setNotice(String notice) {
		data.setNotice(notice);
	}

	public String getNotice() {
		return data.getNotice();
	}

	public int getExp() {
		return data.getExp();
	}

	public void setExp(int exp) {
		data.setExp(exp);
	}

	public long getCreateTimer() {
		return DateUtil.parseDate(data.getCreateTime()).getTime();
	}

	public void handleEvent(GuildConstants.GuildEvenType guildEvenType, Object... params) {
		module.handleEvent(guildEvenType, this, params);
	}

	public SimpleGuild toSimpleGuild() {
		SimpleGuild simpleGuild = new SimpleGuild();
		simpleGuild.setId(data.getId());
		simpleGuild.setLv(data.getLv());
		simpleGuild.setName(data.getName());
		simpleGuild.setIcon(data.getIcon());
		simpleGuild.setTotalPower((int) module.getTotalPower());
		simpleGuild.setNum(module.menMemberMap.size());
		simpleGuild.setIsAutoJoin(getModule().setting.getAutoJoin());
		simpleGuild.getApplyPidList().addAll(module.applyList);
		simpleGuild.setDeclaration(data.getNotification());
		simpleGuild.setCreatorName(module.getCreatorName());
		simpleGuild.setMasterName(module.getMasterMember().getName());
		return simpleGuild;
	}

	public GuildMsg.GuildShowInfo toShowProto() {
		GuildShowInfo.Builder showInfoBuilder = GuildShowInfo.newBuilder();
		showInfoBuilder.setSimpleInfo(toSimpleGuild().toProto());
		module.menMemberMap.forEach((pid, member) -> {
			showInfoBuilder.addMembers(member.toProto().build());
		});
		return showInfoBuilder.build();
	}

	/** 
	 * 工会成员间可以看到的共享数据
	 * @return
	 */
	public GuildMsg.GuildSharedInfo toSharedProto() {
		GuildSharedInfo.Builder builder = GuildSharedInfo.newBuilder();
		builder.setExp(getExp());
		builder.setBargain(module.bargain.toProto());

		// 封装 GuildSetting
		GuildMsg.GuildSettingProto.Builder settingProto = module.setting.toProto();
		settingProto.setNotice(data.getNotice());

		builder.setSetting(settingProto.build());
		builder.setLiveness(module.liveness);
		// 申请列表，先都发下去
		List<SimplePlayer> simplePlayers = RedisLocalCache.getInstance()
				.multiGet(CacheType.PLAYER_SIMPLE, GameUtil.transformToStringArray(module.applyList));
		for (SimplePlayer simplePlayer : simplePlayers) {
			if (simplePlayer != null) {
				builder.addApplyPlayerList(simplePlayer.toSimplePlayerInfo());
			}
		}
		return builder.build();
	}

	public GuildMsg.GuildServiceInfo toProto() {
		GuildMsg.GuildServiceInfo.Builder builder = GuildMsg.GuildServiceInfo.newBuilder();
		builder.setShowInfo(toShowProto());
		builder.setSharedInfo(toSharedProto());
		return builder.build();
	}

	public long callTotalPower() {
		return module.getTotalPower();
	}

	public boolean isHasMember(long playerId) {
		return module.menMemberMap.containsKey(playerId);
	}

	public boolean isFull() {
		GuildBasicConfig basicConfig = GuildBasicManager.instance().get(getLv());
		return module.menMemberMap.size() >= basicConfig.NumberMax;
	}
	public boolean isApplyFull() {
		return module.applyList.size() >= GlobalConst.GuildApplicationNum;
	}

	public boolean hasApply(long playerId) {
		return module.applyList.contains(playerId);
	}
	public boolean hasApply() {
		return module.applyList.size() > 0;
	}

	public void applyJoin(long playerId) {
		module.applyList.add(playerId);
	}

	public boolean isAutoJoin() {
		return module.setting.getAutoJoin() == 1;
	}

	public GuildMember getMember(long playerId) {
		return module.menMemberMap.get(playerId);
	}

	// 解散公会
	public void dissolveGuild() {

		// 删除所有玩家
		module.removeAllMember();
		// 删除公会排行榜
		RankService.getInstance().removeRankAsync(RankType.Guild,data.getServerId(), getId());
		// 删除公会名称 id 映射
		delGuildNameIdRedisData();
		// 删除公会 simple 对象
		RedisLocalCache.getInstance().deleteAsync(CacheType.ZONG_MEN_SIMPLE_DATA.key(getId()));
		// 删除数据库公会
		DAO.delete(data);
		GuildManager.getInstance().delGuild(data.getId());
		GuildManager.log.info("解散公会成功 id:" + getId() + " name:" + getName() + "");
	}

	void delGuildNameIdRedisData() {
		RedisUtil.delete(GuildHelper.getNameKey(getName()));
	}

	public GuildModuleData getModule() {
		return module;
	}

	public void quitGuild(GuildMember member, String playerName) {
		module.removeMember(member.getPlayerId(),0);
		module.handleEvent(GuildConstants.GuildEvenType.QUIT_ZONG_MEN, this, playerName);
	}

	public int getPositionMemberNum(int position) {
		return (int) module.menMemberMap.values().stream().filter(member -> member.position == position).count();
	}

	public void addExp(int addExp) {
		int totalExp = getExp() + addExp;
		GuildBasicConfig basicConfig = GuildBasicManager.instance().get(getLv());
		GuildBasicConfig nextConfig = GuildBasicManager.instance().getNullable(getLv() + 1);
		if (basicConfig == null) {
			return; // 配置错误
		}
		while (totalExp >= basicConfig.Exp) {
			totalExp -= basicConfig.Exp;
			nextConfig = GuildBasicManager.instance().getNullable(getLv() + 1);
			if (nextConfig == null) {
				break;
			}
			setLv(getLv() + 1);
			basicConfig = GuildBasicManager.instance().getNullable(getLv());
			if (basicConfig == null) {
				break;
			}
			handleEvent(GuildConstants.GuildEvenType.ZONG_MEN_LEVEL_UP, this, getLv());
		}
		setExp(totalExp);
	}

	public void addGuildAsset(long playerId, int id, int num) {
		if (id == Asset.GuildPoint.ID) {// 公会活跃度，这个目前不会直接增加了
			module.setLiveness(module.liveness + num);
		} else if (id == Asset.GuildExp.ID) {// 公会经验
			addExp(num);
		}  else if (id == Asset.GuildContribute.ID) {// 个人贡献
			GuildMember member = getMember(playerId); 
			if (member != null) {
				member.addcontribution(num);
				// 成员加贡献的时候，同时增加公会活跃度
				module.setLiveness(module.liveness + num);
				// 同时增加仙会经验
				addExp(num);
			}
		}else {
			throw new IllegalArgumentException("不支持的公会资产类型: " + id);
		}
	}

	/** 
	 * 增加成员
	 * @param targetPidList
	 * @param playerName
	 */
	public void addMember(List<Long> targetPidList) {
		List<Long> joinPidList = new ArrayList<>();
		for (Long targetPid : targetPidList) {
			if (isFull()) {
				break;
			}
			// 删除申请记录
			module.removeApply(targetPid);
			// 加入公会
			boolean joinGuild = joinGuild(targetPid, GuildConstants.ZONG_MEN_POSITION_BANG_ZHONG);
			if (joinGuild) {
				joinPidList.add(targetPid);
				MailHelper.sendPromptMail(targetPid,4,"恭喜加入："+getName()); 
			}
		}
		// 通知被加入的玩家 加入公会
		GuildHelper.broadcastNotifyMsgToPlayer(
				GuildMsg.GuildJoinPush_40000044.newBuilder().setGuild(toSimpleGuild().toProto()).build(),
				PbProtocol.GuildJoinPush_40000044, joinPidList);
		// 更新公会战斗力排行榜
		GuildManager.getInstance().saveGuildTotalPowerRank(this);
	}
	
	/**
	 * 审批 同意 加入
	 *
	 * @param targetPidListList 被加入的列表
	 * @param playerName 审批人
	 */
	public void addMemberAuth(List<Long> targetPidList, String playerName) {
		addMember(targetPidList);
		
	}

	// 删除申请记录
	public void removeApplyAuth(List<Long> targetPidListList, String playerName) {
		targetPidListList.forEach(targetPid -> {
			module.removeApply(targetPid);
		});
	}

	// 踢人
	public void kickMember(List<Long> targetPidListList, String playerName) {
		PlayerManager.getInstance().batchGetSimplePlayerListFromRedisAsync(targetPidListList).onSuccess(res -> {
			res.forEach(simplePlayer -> {
				module.removeMember(simplePlayer.getId(),1);
				handleEvent(GuildConstants.GuildEvenType.ZONG_MEN_KICK_MEMBER, playerName, simplePlayer.getName());
			});
			// 更新公会战斗力排行榜
			GuildManager.getInstance().saveGuildTotalPowerRank(this);
		}).onFailure(err -> {
			err.printStackTrace();
		});
	}

	/**
	 * 检查宗主转让
	 * - 自动转让宗主规则：
	 *   - 宗主3天未上线则进行自动转让宗主（GuildSuzerainTransfer）；
	 *   - 自动转让：职位最高且在3天内上线的玩家，同一职位转让至贡献度最高的玩家，贡献度相同则转让给战力最高的玩家；
	 */
	public void checkZongZhuTransfer(long now) {
		List<GuildMember> memberList = new ArrayList<>(module.menMemberMap.values());
		GuildMember masterMember = module.getMasterMember(); 
		SimplePlayer simplePlayerMaster = PlayerHelper.getSimplePlayer(masterMember.getPlayerId()); 
		if (DateUtil.diffDays(now, simplePlayerMaster.getLastLoginTimer()) < GlobalConst.GuildSuzerainTransfer) {
			return ; 
		}

		if (memberList.size() <= 1) {// 公会没人
			return;
		}
		
		memberList.sort((m1, m2) -> {
			if (m1.position == m2.position) {
				if (m1.getWeekContribution() == m2.getWeekContribution()) {
					SimplePlayer simplePlayer1 = PlayerHelper.getSimplePlayer(m1.getPlayerId());
					SimplePlayer simplePlayer2 = PlayerHelper.getSimplePlayer(m2.getPlayerId());
					return (int) (simplePlayer2.lastLoginTimer - simplePlayer1.lastLoginTimer) ;
				}
				return m1.getWeekContribution() - m2.getWeekContribution();
			}
			return m1.position < m2.position ? 1 : -1;
		});
		GuildMember zongZhu = memberList.get(0);
		
		// 被转让的人
		GuildMember targetZongZhu = null;
		for (int i = 1; i < memberList.size(); i++) {
			SimplePlayer simplePlayer = PlayerHelper.getSimplePlayer(masterMember.getPlayerId()); 
			long lastLoginTimer =  simplePlayer.lastLoginTimer; 
			if (DateUtil.diffDays(now, lastLoginTimer) < GlobalConst.GuildSuzerainTransfer) {
				targetZongZhu = memberList.get(i);
				break;
			}
		}
		if (targetZongZhu == null) {
			return;
		}
		// 转让宗主
		zongZhuTransfer(zongZhu, targetZongZhu);
	}

	public void zongZhuTransfer(GuildMember zongZhu, GuildMember targetZongZhu) {
		zongZhu.setPosition(GuildConstants.ZONG_MEN_POSITION_BANG_ZHONG);
		String promt = "您的职位变更为" + "<color=#FB4141>{0}</color>" ; 
		GuildPermissionsConfig guildPermissionsConfig = GuildPermissionsManager.instance().get(GuildConstants.ZONG_MEN_POSITION_ZONG_ZHU); 
		MailHelper.sendPromptMail(targetZongZhu.getPlayerId(),5,MessageFormat.format(promt, guildPermissionsConfig.Name)) ; 
		handleEvent(GuildConstants.GuildEvenType.ZONG_MEN_POSITION_CHANGE, zongZhu.playerId,
				GuildConstants.ZONG_MEN_POSITION_ZONG_ZHU, zongZhu.position);
		int targetOldPosition = targetZongZhu.position;
		targetZongZhu.setPosition(GuildConstants.ZONG_MEN_POSITION_ZONG_ZHU);
		handleEvent(GuildConstants.GuildEvenType.ZONG_MEN_POSITION_CHANGE, targetZongZhu.playerId, targetOldPosition,
				targetZongZhu.position);
	}

}
