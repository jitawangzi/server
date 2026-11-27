package cn.game.games.net.cross.activity.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import cn.game.core.base.ServerContext;
import cn.game.games.net.common.module.activity.DefaultActivityScopeResolver;
import cn.game.games.net.cross.activity.CrossActivityBase;
import cn.game.games.net.cross.activity.dto.CrossActivityActionResult;
import cn.game.games.net.cross.activity.dto.CrossGuildWarPayloads.AcceptanceResetRequest;
import cn.game.games.net.cross.activity.dto.CrossGuildWarPayloads.AcceptanceResetResponse;
import cn.game.games.net.cross.activity.dto.CrossGuildWarPayloads.AttackRequest;
import cn.game.games.net.cross.activity.dto.CrossGuildWarPayloads.AttackResponse;
import cn.game.games.net.cross.activity.dto.CrossGuildWarPayloads.ClaimMilestoneRequest;
import cn.game.games.net.cross.activity.dto.CrossGuildWarPayloads.ClaimMilestoneResponse;
import cn.game.games.net.cross.activity.dto.CrossGuildWarPayloads.GuildRankEntryPayload;
import cn.game.games.net.cross.activity.dto.CrossGuildWarPayloads.MemberRankEntryPayload;
import cn.game.games.net.cross.activity.dto.CrossGuildWarPayloads.QueryDetailRequest;
import cn.game.games.net.cross.activity.dto.CrossGuildWarPayloads.QueryDetailResponse;
import cn.game.games.net.cross.activity.dto.CrossGuildWarPayloads.QueryGuildRankRequest;
import cn.game.games.net.cross.activity.dto.CrossGuildWarPayloads.QueryGuildRankResponse;
import cn.game.games.net.cross.activity.dto.CrossGuildWarPayloads.QueryMemberRankRequest;
import cn.game.games.net.cross.activity.dto.CrossGuildWarPayloads.QueryMemberRankResponse;
import cn.game.games.net.cross.activity.dto.CrossGuildWarPayloads.StrongholdPayload;
import cn.game.games.net.cross.activity.service.CrossActivityService;
import cn.game.games.net.game.helper.ItemHelper;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.helper.PlayerSaveCoordinator;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.award.Goods;
import cn.game.protocol.generated.config.ActivityCrossGuildWarConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.manager.ActivityCrossGuildWarManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.ActivityMsg.ActivityState;
import cn.game.util.DateUtil;
import cn.game.util.JsonUtil;

/**
 * 跨服宗门争霸 Cross 权威领域实体。
 * <p>
 * 职责边界：按战区 {@code scopeKey} 在 {@code activityUid} 单邮箱内串行维护全战区据点归属、宗门与成员战功、
 * 双维排行榜、个人/宗门里程碑达标判定以及 {@code onEnd()} 终场霸主加分与 {@code Mail(45)} 单向异步结算；
 * 严禁访问 Cross {@code Guild} 邮箱、{@code GuildManager} 或向 Game 发起同步阻塞 {@code await} 调用。
 * </p>
 *
 * @author shuyongqiang
 */
@ActivityType(type = ActivityTypeEnum.CrossGuildWar)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrossGuildWarActivity extends CrossActivityBase {

	/** 跨服宗门争霸实体日志器 */
	private static final Logger log = LoggerFactory.getLogger(CrossGuildWarActivity.class);

	/** 动作常量：查询争霸详情与实时据点状态 */
	public static final String ACTION_QUERY_DETAIL = "query_detail";
	/** 动作常量：发起据点出征攻打 */
	public static final String ACTION_ATTACK_STRONGHOLD = "attack_stronghold";
	/** 动作常量：校验领取个人或锁定宗门阶段奖励 */
	public static final String ACTION_CLAIM_MILESTONE = "claim_milestone";
	/** 动作常量：分页查询战区宗门排行榜 */
	public static final String ACTION_QUERY_GUILD_RANK = "query_guild_rank";
	/** 动作常量：分页查询宗门内成员贡献榜 */
	public static final String ACTION_QUERY_MEMBER_RANK = "query_member_rank";
	/** 动作常量：非生产环境受控验收重置与压测数据构造 */
	public static final String ACTION_ACCEPTANCE_RESET = "acceptance_reset";

	/** 验收重置模式：重置沙盒战区据点与榜单状态 */
	public static final String RESET_MODE_WAR = "reset_sandbox_war";
	/** 验收重置模式：清除指定沙盒玩家参战记录 */
	public static final String RESET_MODE_CLEAR_PLAYER = "clear_player";
	/** 验收重置模式：受控设定沙盒玩家/宗门与据点战功以便验收高阶里程碑及终榜结算 */
	public static final String RESET_MODE_SET_SCORES = "set_scores_for_test";

	/** 奖励类型常量：个人战功目标奖 */
	public static final int REWARD_TYPE_PERSONAL = 1;
	/** 奖励类型常量：锁定宗门战功阶段奖 */
	public static final int REWARD_TYPE_GUILD = 2;

	/** 终榜落盘失败后在线补偿重试冷却时间（30 秒） */
	private static final long SETTLE_RETRY_COOLDOWN_MS = 30_000L;
	/** 排行榜单页最大查询条数 */
	private static final int MAX_RANK_PAGE_SIZE = 100;

	/** 战区宗门榜排序比较器：总战功降序，同分先达成时间升序，再按 guildId 升序 */
	private static final Comparator<GuildWarGuildRecord> GUILD_RANK_COMPARATOR = (a, b) -> {
		int cmp = Long.compare(b.getTotalScore(), a.getTotalScore());
		if (cmp != 0) {
			return cmp;
		}
		cmp = Long.compare(a.getUpdateTime(), b.getUpdateTime());
		if (cmp != 0) {
			return cmp;
		}
		return Long.compare(a.getGuildId(), b.getGuildId());
	};

	/** 宗门成员贡献榜排序比较器：战功降序，同分先达成时间升序，再按 playerId 升序 */
	private static final Comparator<GuildWarMemberRecord> MEMBER_RANK_COMPARATOR = (a, b) -> {
		int cmp = Long.compare(b.getScore(), a.getScore());
		if (cmp != 0) {
			return cmp;
		}
		cmp = Long.compare(a.getUpdateTime(), b.getUpdateTime());
		if (cmp != 0) {
			return cmp;
		}
		return Long.compare(a.getPlayerId(), b.getPlayerId());
	};

	/** 据点权威实时状态映射（key: strongholdId） */
	protected Map<Integer, GuildWarStrongholdState> strongholds = new HashMap<>();
	/** 本战区参战宗门战功记录映射（key: guildId） */
	protected Map<Long, GuildWarGuildRecord> guilds = new HashMap<>();
	/** 本战区锁定参战成员记录映射（key: playerId） */
	protected Map<Long, GuildWarMemberRecord> members = new HashMap<>();
	/** 争霸结束锁定后的终榜快照 */
	protected List<GuildWarRankSnapshotEntry> finalGuildRankList = new ArrayList<>();
	/** 终榜是否已加分落盘并完成结算标记（volatile 保障周期调度无锁预检可见性） */
	protected volatile boolean rankSettled;

	/** 瞬态缓存：当前有序宗门排行榜 */
	@JsonIgnore
	private transient List<GuildWarRankSnapshotEntry> sortedGuildRankCache = new ArrayList<>();
	/** 瞬态缓存：guildId -> 1-based 名次索引 */
	@JsonIgnore
	private transient Map<Long, Integer> guildRankIndexCache = new HashMap<>();
	/** 瞬态缓存：宗门排行是否需要重建 */
	@JsonIgnore
	private transient boolean guildRankCacheDirty = true;
	/** 瞬态缓存：按宗门缓存有序成员列表 */
	@JsonIgnore
	private transient Map<Long, List<GuildWarMemberRecord>> sortedGuildMembersCache = new HashMap<>();
	/** 瞬态缓存：待重建成员榜的宗门 ID 集合 */
	@JsonIgnore
	private transient Set<Long> dirtyMemberRankGuildIds = new HashSet<>();
	/** 瞬态冷却：终场持久化失败后下次允许在线补偿重试的时间戳（毫秒） */
	@JsonIgnore
	private transient volatile long nextSettleRetryTime;
	/** 可注入异步发邮 SPI，默认绑定 MailHelper::sendMail */
	@JsonIgnore
	private transient CrossGuildWarMailSender mailSender = MailHelper::sendMail;

	/** 无参构造，供反射创建与 Jackson 反序列化使用 */
	public CrossGuildWarActivity() {
	}

	/**
	 * 注入自定义异步发邮器（供单元测试拦截断言发邮明细）
	 *
	 * @param customMailSender 自定义发邮器，传 null 时回退默认 {@link MailHelper#sendMail}
	 */
	public void setMailSender(CrossGuildWarMailSender customMailSender) {
		this.mailSender = customMailSender != null ? customMailSender : MailHelper::sendMail;
	}

	/**
	 * 还原默认生产异步发邮器
	 */
	public void resetMailSender() {
		this.mailSender = MailHelper::sendMail;
	}

	/**
	 * 是否已完成终场据点加分与排行榜落盘结算
	 *
	 * @return true 表示已完成终场落盘
	 */
	public boolean isRankSettled() {
		return rankSettled;
	}

	/**
	 * 设置终场结算落盘标记（供反序列化恢复使用）
	 *
	 * @param rankSettled true 表示已完成终场落盘
	 */
	public void setRankSettled(boolean rankSettled) {
		this.rankSettled = rankSettled;
	}

	/**
	 * 判断当前时刻是否允许执行终场结算在线补偿重试
	 *
	 * @param now 当前毫秒时间戳
	 * @return 未结算且已过 30 秒冷却期返回 true
	 */
	@JsonIgnore
	public boolean canRetryEndSettlement(long now) {
		return !rankSettled && now >= nextSettleRetryTime;
	}

	/**
	 * 获取当前据点数量（供监控快照使用）
	 *
	 * @return 据点数量
	 */
	@JsonIgnore
	public int getStrongholdCount() {
		return strongholds != null ? strongholds.size() : 0;
	}

	/**
	 * 获取当前参战宗门数量（供监控快照使用）
	 *
	 * @return 参战宗门数量
	 */
	@JsonIgnore
	public int getGuildCount() {
		return guilds != null ? guilds.size() : 0;
	}

	/**
	 * 获取当前参战锁定玩家数量（供监控快照使用）
	 *
	 * @return 参战成员数量
	 */
	@JsonIgnore
	public int getParticipantCount() {
		return members != null ? members.size() : 0;
	}

	/**
	 * 获取终榜快照条数（供监控快照使用）
	 *
	 * @return 终榜条数
	 */
	@JsonIgnore
	public int getFinalRankCount() {
		return finalGuildRankList != null ? finalGuildRankList.size() : 0;
	}

	/**
	 * 活动开启初始化：校验配置并按据点表初始化全部中立据点
	 */
	@Override
	protected void onStart() {
		ActivityCrossGuildWarConfig cfg = ActivityCrossGuildWarManager.instance().getNullable(this.id);
		if (cfg == null) {
			log.error("CrossGuildWar 启动初始化缺少配置: configId={}, uid={}, scopeKey={}", id, uid, scopeKey);
			throw new IllegalStateException("CrossGuildWar 启动初始化缺少配置: configId=" + id);
		}
		ensureCollectionsInitialized();
		this.strongholds.clear();
		this.guilds.clear();
		this.members.clear();
		this.finalGuildRankList.clear();
		this.rankSettled = false;
		this.nextSettleRetryTime = 0L;
		resetTransientCaches();

		initMissingStrongholdsFromConfig(cfg);
		this.guildRankCacheDirty = true;
	}

	/**
	 * 从数据库反序列化恢复后的集合补空、配置补齐与瞬态索引重建
	 */
	@Override
	public void afterLoadFromDb() {
		ensureCollectionsInitialized();
		resetTransientCaches();
		this.nextSettleRetryTime = 0L;

		for (GuildWarGuildRecord guild : this.guilds.values()) {
			if (guild != null) {
				guild.ensureCollections();
				this.dirtyMemberRankGuildIds.add(guild.getGuildId());
			}
		}

		ActivityCrossGuildWarConfig cfg = ActivityCrossGuildWarManager.instance().getNullable(this.id);
		if (cfg != null) {
			initMissingStrongholdsFromConfig(cfg);
		}

		if (!this.finalGuildRankList.isEmpty()) {
			rebuildGuildRankCacheFromFinalList();
		} else {
			this.guildRankCacheDirty = true;
			ensureGuildRankCacheUpToDate();
		}

		if (this.state == ActivityState.CLOSE_VALUE && !this.rankSettled) {
			log.warn("跨服宗门争霸重启恢复检测到 CLOSE 期尚未完成排名发邮结算: uid={}, scopeKey={}", uid, scopeKey);
		}
	}

	/**
	 * 玩家参与准入防御检查（纯只读接口，绝不触发无意义的脏标）
	 *
	 * @param playerId  玩家 ID
	 * @param serverId  玩家逻辑区服 ID
	 * @param extraData 附加参数
	 * @return 活动处于 START 或 CLOSE 且基础准入允许时返回 true，否则返回 false
	 */
	@Override
	public boolean join(long playerId, String serverId, Map<String, Object> extraData) {
		return super.canJoin(playerId)
				&& (this.state == ActivityState.START_VALUE || this.state == ActivityState.CLOSE_VALUE);
	}

	/**
	 * 活动彻底销毁前清理内存瞬态缓存
	 */
	@Override
	protected void onDestroy() {
		resetTransientCaches();
	}

	/**
	 * 争霸期结束结算：归零重算据点霸主加分、锁定终榜、强制同步落盘（失败回滚并设 30 秒冷却），随后异步发送 Mail(45)
	 */
	@Override
	protected void onEnd() {
		ensureCollectionsInitialized();
		if (this.rankSettled) {
			return;
		}

		ActivityCrossGuildWarConfig cfg = ActivityCrossGuildWarManager.instance().getNullable(this.id);
		if (cfg == null) {
			log.error("跨服宗门争霸结算缺少配置: configId={}, uid={}, scopeKey={}", id, uid, scopeKey);
			return;
		}

		// 幂等归零所有宗门的终场据点加分，确保失败重试时不重复叠加
		for (GuildWarGuildRecord guild : this.guilds.values()) {
			if (guild != null) {
				guild.setStrongholdBonusScore(0L);
			}
		}
		for (GuildWarStrongholdState sh : this.strongholds.values()) {
			if (sh == null || sh.getOwnerGuildId() <= 0L) {
				continue;
			}
			GuildWarGuildRecord ownerGuild = this.guilds.get(sh.getOwnerGuildId());
			if (ownerGuild == null) {
				continue;
			}
			long bonus = resolveStrongholdBonusScore(cfg, sh.getStrongholdId());
			if (bonus > 0L) {
				ownerGuild.setStrongholdBonusScore(ownerGuild.getStrongholdBonusScore() + bonus);
			}
		}

		// 冻结终榜快照并更新内存排名索引
		List<GuildWarRankSnapshotEntry> lockedRank = buildSortedGuildRankEntries();
		this.finalGuildRankList = lockedRank;
		rebuildGuildRankCacheFromFinalList();

		// 先置幂等标记并强制同步落盘，落盘失败则回滚标志、设置 30 秒冷却并中止发邮
		try {
			this.rankSettled = true;
			markDirty();
			CrossActivityService.getInstance().flushActivityInMailbox(this);
			this.nextSettleRetryTime = 0L;
		} catch (Exception e) {
			this.rankSettled = false;
			this.nextSettleRetryTime = DateUtil.currentTimeMillis() + SETTLE_RETRY_COOLDOWN_MS;
			markDirty();
			log.error("跨服宗门争霸终榜结算落盘失败，已回滚 rankSettled 并设置30秒补偿重试冷却，中止发邮: uid={}, scopeKey={}",
					uid, scopeKey, e);
			return;
		}

		// 单向异步向达标锁定成员逐人发送终榜结算邮件（严禁回查 Cross Guild 邮箱）
		CrossGuildWarMailSender sender = this.mailSender != null ? this.mailSender : MailHelper::sendMail;
		for (GuildWarRankSnapshotEntry entry : this.finalGuildRankList) {
			int rangeIdx = findRankRangeIndex(cfg.rankRanges, entry.getRank());
			if (rangeIdx < 0 || cfg.rankRewards == null || rangeIdx >= cfg.rankRewards.length) {
				continue;
			}
			List<Goods> goodsList = buildGoodsList(cfg.rankRewards[rangeIdx]);
			if (goodsList.isEmpty()) {
				continue;
			}
			GuildWarGuildRecord guild = this.guilds.get(entry.getGuildId());
			if (guild == null || guild.getMemberIds() == null) {
				continue;
			}
			for (Long memberId : guild.getMemberIds()) {
				if (memberId == null) {
					continue;
				}
				GuildWarMemberRecord member = this.members.get(memberId);
				if (member == null || member.getLockedGuildId() != entry.getGuildId()
						|| member.getScore() < cfg.minParticipateScore) {
					continue;
				}
				try {
					sender.sendMail(member.getPlayerId(), cfg.rankMailId, ItemHelper.copy(goodsList), true);
				} catch (Exception e) {
					log.error("跨服宗门争霸终榜邮件发送异常: uid={}, guildId={}, playerId={}, rank={}",
							uid, entry.getGuildId(), member.getPlayerId(), entry.getRank(), e);
				}
			}
		}
	}

	/**
	 * 补偿触发终榜结算：供 CLOSE 期在线周期任务、跨天或重启恢复调用
	 */
	public void compensateEndSettlement() {
		if (this.state == ActivityState.CLOSE_VALUE && !this.rankSettled) {
			onEnd();
		}
	}

	/**
	 * 在活动邮箱内单线程串行执行玩家业务动作
	 *
	 * @param playerId    玩家 ID
	 * @param actionType  动作类型（见 {@code ACTION_*} 常量）
	 * @param payloadJson 动作 JSON 请求载荷
	 * @return 执行结果 DTO（含错误码或结果 JSON 载荷）
	 */
	@Override
	public CrossActivityActionResult executePlayerAction(long playerId, String actionType, String payloadJson) {
		if (playerId <= 0L) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID, "玩家ID非法");
		}
		ensureCollectionsInitialized();
		if (ACTION_QUERY_DETAIL.equals(actionType)) {
			return handleQueryDetail(playerId, payloadJson);
		}
		if (ACTION_ATTACK_STRONGHOLD.equals(actionType)) {
			return handleAttackStronghold(playerId, payloadJson);
		}
		if (ACTION_CLAIM_MILESTONE.equals(actionType)) {
			return handleClaimMilestone(playerId, payloadJson);
		}
		if (ACTION_QUERY_GUILD_RANK.equals(actionType)) {
			return handleQueryGuildRank(playerId, payloadJson);
		}
		if (ACTION_QUERY_MEMBER_RANK.equals(actionType)) {
			return handleQueryMemberRank(playerId, payloadJson);
		}
		if (ACTION_ACCEPTANCE_RESET.equals(actionType)) {
			return handleAcceptanceReset(playerId, payloadJson);
		}
		return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID,
				"Unsupported CrossGuildWar actionType: " + actionType);
	}

	/**
	 * 处理查询争霸详情动作（允许 START 与 CLOSE 期，不标记脏数据）
	 */
	private CrossActivityActionResult handleQueryDetail(long playerId, String payloadJson) {
		if (!isReadableState()) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.not_open.ID, "活动未开启");
		}
		ActivityCrossGuildWarConfig cfg = ActivityCrossGuildWarManager.instance().getNullable(this.id);
		if (cfg == null) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.config_data_not_found.ID, "活动配置不存在");
		}
		QueryDetailRequest req = parsePayload(payloadJson, QueryDetailRequest.class);
		long currentGuildId = req != null ? req.getCurrentGuildId() : 0L;

		ensureGuildRankCacheUpToDate();

		GuildWarMemberRecord member = this.members.get(playerId);
		long authLockedGuildId = member != null ? member.getLockedGuildId() : 0L;
		long myScore = member != null ? member.getScore() : 0L;
		long effectiveGuildId = authLockedGuildId > 0L ? authLockedGuildId : currentGuildId;
		GuildWarGuildRecord effectiveGuild = effectiveGuildId > 0L ? this.guilds.get(effectiveGuildId) : null;

		QueryDetailResponse resp = new QueryDetailResponse();
		resp.setState(this.state);
		resp.setLockedGuildId(authLockedGuildId);
		resp.setMyScore(myScore);
		resp.setStrongholds(buildStrongholdPayloads(cfg, effectiveGuild));
		resp.setMyGuildSummary(buildGuildSummaryPayload(effectiveGuildId));
		resp.setUnlockedPersonalMilestoneIds(computeUnlockedPersonalMilestones(cfg, myScore));
		resp.setUnlockedGuildMilestoneIds(computeUnlockedGuildMilestones(cfg, authLockedGuildId, myScore));
		resp.setEndTime(this.endTime);
		resp.setDestroyTime(this.destroyTime);
		return CrossActivityActionResult.success(JsonUtil.toJsonString(resp));
	}

	/**
	 * 处理据点出征攻打动作（仅允许 START 期；校验宗门锁定一致性、严格大于易主与同分保权）
	 */
	private CrossActivityActionResult handleAttackStronghold(long playerId, String payloadJson) {
		if (this.state != ActivityState.START_VALUE) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.not_open.ID, "当前非争霸期，不可出征");
		}
		ActivityCrossGuildWarConfig cfg = ActivityCrossGuildWarManager.instance().getNullable(this.id);
		if (cfg == null) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.config_data_not_found.ID, "活动配置不存在");
		}
		AttackRequest req = parsePayload(payloadJson, AttackRequest.class);
		if (req == null) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID, "请求参数不能为空");
		}
		if (req.getCurrentGuildId() <= 0L) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.zong_men_not_exist.ID, "尚未加入宗门");
		}
		GuildWarStrongholdState stronghold = this.strongholds.get(req.getStrongholdId());
		if (stronghold == null) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.cross_guild_war_stronghold_invalid.ID,
					"非法的跨服宗门争霸据点ID");
		}
		if (req.getScore() <= 0L || req.getScore() > cfg.maxSingleScore) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID,
					"战功上报数值超出合法区间");
		}

		// 一经锁定参战宗门，换会后禁止在新宗门继续出征
		GuildWarMemberRecord existingMember = this.members.get(playerId);
		if (existingMember != null && existingMember.getLockedGuildId() > 0L
				&& existingMember.getLockedGuildId() != req.getCurrentGuildId()) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.cross_guild_war_guild_locked.ID,
					"本期已锁定参战宗门，不可在新宗门继续参与");
		}

		long now = DateUtil.currentTimeMillis();
		long currentGuildId = req.getCurrentGuildId();
		long addScore = req.getScore();
		String safePlayerName = StringUtils.isNotBlank(req.getPlayerName())
				? req.getPlayerName() : "玩家#" + playerId;
		String safePlayerServerId = StringUtils.isNotBlank(req.getServerId()) ? req.getServerId() : "";
		String safeGuildName = StringUtils.isNotBlank(req.getGuildName())
				? req.getGuildName() : "宗门#" + currentGuildId;
		String safeGuildServerId = StringUtils.isNotBlank(req.getGuildServerId())
				? req.getGuildServerId() : safePlayerServerId;

		// 维护个人锁定参战与累计战功
		GuildWarMemberRecord member = existingMember;
		if (member == null) {
			member = new GuildWarMemberRecord();
			member.setPlayerId(playerId);
			member.setLockedGuildId(currentGuildId);
			this.members.put(playerId, member);
		} else if (member.getLockedGuildId() <= 0L) {
			member.setLockedGuildId(currentGuildId);
		}
		member.setPlayerName(safePlayerName);
		member.setServerId(safePlayerServerId);
		member.setScore(member.getScore() + addScore);
		member.setUpdateTime(now);

		// 维护锁定宗门基础战功、据点累计分与元数据快照
		GuildWarGuildRecord guild = this.guilds.get(currentGuildId);
		if (guild == null) {
			guild = new GuildWarGuildRecord();
			guild.setGuildId(currentGuildId);
			this.guilds.put(currentGuildId, guild);
		}
		guild.ensureCollections();
		guild.setGuildName(safeGuildName);
		guild.setGuildIcon(req.getGuildIcon());
		guild.setGuildServerId(safeGuildServerId);
		guild.getMemberIds().add(playerId);
		guild.setBaseScore(guild.getBaseScore() + addScore);
		guild.setUpdateTime(now);
		long newGuildStrongholdScore = guild.getStrongholdScores().getOrDefault(req.getStrongholdId(), 0L) + addScore;
		guild.getStrongholdScores().put(req.getStrongholdId(), newGuildStrongholdScore);

		// 判定据点归属：严格超越原霸主分数才易主，同分先达成者保权
		long occupyMinScore = resolveStrongholdOccupyMinScore(cfg, req.getStrongholdId());
		boolean captured = false;
		if (stronghold.getOwnerGuildId() == currentGuildId) {
			stronghold.setOwnerScore(newGuildStrongholdScore);
			stronghold.setOwnerGuildName(safeGuildName);
			stronghold.setOwnerGuildIcon(req.getGuildIcon());
			stronghold.setOwnerGuildServerId(safeGuildServerId);
		} else if (newGuildStrongholdScore >= occupyMinScore && newGuildStrongholdScore > stronghold.getOwnerScore()) {
			stronghold.setOwnerGuildId(currentGuildId);
			stronghold.setOwnerGuildName(safeGuildName);
			stronghold.setOwnerGuildIcon(req.getGuildIcon());
			stronghold.setOwnerGuildServerId(safeGuildServerId);
			stronghold.setOwnerScore(newGuildStrongholdScore);
			stronghold.setOccupyTime(now);
			captured = true;
		}

		this.guildRankCacheDirty = true;
		this.dirtyMemberRankGuildIds.add(currentGuildId);
		ensureGuildRankCacheUpToDate();
		markDirty();

		int guildRank = this.guildRankIndexCache.getOrDefault(currentGuildId, 0);
		AttackResponse resp = new AttackResponse();
		resp.setEffectiveScore(addScore);
		resp.setMyScore(member.getScore());
		resp.setLockedGuildId(member.getLockedGuildId());
		resp.setGuildTotalScore(guild.getTotalScore());
		resp.setGuildRank(guildRank);
		resp.setStrongholdId(req.getStrongholdId());
		resp.setGuildStrongholdScore(newGuildStrongholdScore);
		resp.setOwnerGuildId(stronghold.getOwnerGuildId());
		resp.setOwnerGuildName(stronghold.getOwnerGuildName() != null ? stronghold.getOwnerGuildName() : "");
		resp.setOwnerScore(stronghold.getOwnerScore());
		resp.setCaptured(captured);
		resp.setUnlockedPersonalMilestoneIds(computeUnlockedPersonalMilestones(cfg, member.getScore()));
		resp.setUnlockedGuildMilestoneIds(
				computeUnlockedGuildMilestones(cfg, member.getLockedGuildId(), member.getScore()));
		return CrossActivityActionResult.success(JsonUtil.toJsonString(resp));
	}

	/**
	 * 处理领取阶段奖励校验动作（允许 START 与 CLOSE 期，不标记脏数据）
	 */
	private CrossActivityActionResult handleClaimMilestone(long playerId, String payloadJson) {
		if (!isReadableState()) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.not_open.ID, "活动未开启");
		}
		ActivityCrossGuildWarConfig cfg = ActivityCrossGuildWarManager.instance().getNullable(this.id);
		if (cfg == null) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.config_data_not_found.ID, "活动配置不存在");
		}
		ClaimMilestoneRequest req = parsePayload(payloadJson, ClaimMilestoneRequest.class);
		if (req == null) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID, "请求参数不能为空");
		}
		int rewardType = req.getRewardType();
		int milestoneId = req.getMilestoneId();
		GuildWarMemberRecord member = this.members.get(playerId);
		long authLockedGuildId = member != null ? member.getLockedGuildId() : 0L;

		if (rewardType == REWARD_TYPE_PERSONAL) {
			if (cfg.personalMilestoneThresholds == null
					|| milestoneId < 1 || milestoneId > cfg.personalMilestoneThresholds.length) {
				return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID,
						"个人里程碑档位越界");
			}
			long myScore = member != null ? member.getScore() : 0L;
			if (myScore < cfg.personalMilestoneThresholds[milestoneId - 1]) {
				return CrossActivityActionResult.fail(ErrorMsgEnum.condition_check_error.ID,
						"个人战功尚未达到该阶段里程碑要求");
			}
		} else if (rewardType == REWARD_TYPE_GUILD) {
			if (cfg.guildMilestoneThresholds == null
					|| milestoneId < 1 || milestoneId > cfg.guildMilestoneThresholds.length) {
				return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID,
						"宗门里程碑档位越界");
			}
			if (authLockedGuildId <= 0L) {
				return CrossActivityActionResult.fail(ErrorMsgEnum.cross_guild_war_no_locked_guild.ID,
						"未锁定参赛宗门或尚未参战，不可领取宗门奖励");
			}
			if (req.getExpectedLockedGuildId() > 0L && req.getExpectedLockedGuildId() != authLockedGuildId) {
				return CrossActivityActionResult.fail(ErrorMsgEnum.cross_guild_war_guild_locked.ID,
						"本期已锁定参战宗门，不可在新宗门领取阶段奖励");
			}
			GuildWarGuildRecord lockedGuild = this.guilds.get(authLockedGuildId);
			if (member.getScore() < cfg.minParticipateScore || lockedGuild == null
					|| lockedGuild.getTotalScore() < cfg.guildMilestoneThresholds[milestoneId - 1]) {
				return CrossActivityActionResult.fail(ErrorMsgEnum.condition_check_error.ID,
						"未达宗门阶段奖励参战或战功门槛");
			}
		} else {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID,
					"非法的阶段奖励类型: " + rewardType);
		}

		ClaimMilestoneResponse resp = new ClaimMilestoneResponse();
		resp.setRewardType(rewardType);
		resp.setMilestoneId(milestoneId);
		resp.setLockedGuildId(authLockedGuildId);
		return CrossActivityActionResult.success(JsonUtil.toJsonString(resp));
	}

	/**
	 * 处理分页查询战区宗门排行榜动作（允许 START 与 CLOSE 期，不标记脏数据）
	 */
	private CrossActivityActionResult handleQueryGuildRank(long playerId, String payloadJson) {
		if (!isReadableState()) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.not_open.ID, "活动未开启");
		}
		QueryGuildRankRequest req = parsePayload(payloadJson, QueryGuildRankRequest.class);
		if (req == null || req.getPage() < 1 || req.getPageSize() < 1 || req.getPageSize() > MAX_RANK_PAGE_SIZE) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID,
					"分页参数非法，要求 page>=1 且 1<=pageSize<=100");
		}

		ensureGuildRankCacheUpToDate();

		int totalCount = this.sortedGuildRankCache.size();
		long fromIndexLong = (long) (req.getPage() - 1) * req.getPageSize();
		List<GuildRankEntryPayload> pageEntries = new ArrayList<>();
		if (fromIndexLong >= 0L && fromIndexLong < totalCount) {
			int fromIndex = (int) fromIndexLong;
			int toIndex = (int) Math.min((long) totalCount, fromIndexLong + req.getPageSize());
			for (int i = fromIndex; i < toIndex; i++) {
				pageEntries.add(toGuildRankPayload(this.sortedGuildRankCache.get(i)));
			}
		}

		GuildWarMemberRecord member = this.members.get(playerId);
		long effectiveGuildId = (member != null && member.getLockedGuildId() > 0L)
				? member.getLockedGuildId() : req.getCurrentGuildId();

		QueryGuildRankResponse resp = new QueryGuildRankResponse();
		resp.setTotalCount(totalCount);
		resp.setPage(req.getPage());
		resp.setPageSize(req.getPageSize());
		resp.setEntries(pageEntries);
		resp.setMyGuildEntry(buildGuildSummaryPayload(effectiveGuildId));
		return CrossActivityActionResult.success(JsonUtil.toJsonString(resp));
	}

	/**
	 * 处理分页查询指定宗门成员战功贡献榜动作（允许 START 与 CLOSE 期，不标记脏数据）
	 */
	private CrossActivityActionResult handleQueryMemberRank(long playerId, String payloadJson) {
		if (!isReadableState()) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.not_open.ID, "活动未开启");
		}
		QueryMemberRankRequest req = parsePayload(payloadJson, QueryMemberRankRequest.class);
		if (req == null || req.getPage() < 1 || req.getPageSize() < 1 || req.getPageSize() > MAX_RANK_PAGE_SIZE) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID,
					"分页参数非法，要求 page>=1 且 1<=pageSize<=100");
		}
		if (req.getTargetGuildId() <= 0L) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.cross_guild_war_no_locked_guild.ID,
					"未指定有效宗门且当前尚未锁定参战宗门");
		}

		long targetGuildId = req.getTargetGuildId();
		List<GuildWarMemberRecord> sortedMembers = ensureMemberRankCacheUpToDate(targetGuildId);
		int totalCount = sortedMembers.size();
		long fromIndexLong = (long) (req.getPage() - 1) * req.getPageSize();
		List<MemberRankEntryPayload> pageEntries = new ArrayList<>();
		if (fromIndexLong >= 0L && fromIndexLong < totalCount) {
			int fromIndex = (int) fromIndexLong;
			int toIndex = (int) Math.min((long) totalCount, fromIndexLong + req.getPageSize());
			for (int i = fromIndex; i < toIndex; i++) {
				pageEntries.add(toMemberRankPayload(i + 1, sortedMembers.get(i)));
			}
		}

		MemberRankEntryPayload myMemberEntry = new MemberRankEntryPayload();
		myMemberEntry.setPlayerId(playerId);
		myMemberEntry.setPlayerName("");
		myMemberEntry.setServerId("");
		for (int i = 0; i < sortedMembers.size(); i++) {
			GuildWarMemberRecord record = sortedMembers.get(i);
			if (record.getPlayerId() == playerId) {
				myMemberEntry = toMemberRankPayload(i + 1, record);
				break;
			}
		}

		QueryMemberRankResponse resp = new QueryMemberRankResponse();
		resp.setTargetGuildId(targetGuildId);
		resp.setTotalCount(totalCount);
		resp.setPage(req.getPage());
		resp.setPageSize(req.getPageSize());
		resp.setEntries(pageEntries);
		resp.setMyMemberEntry(myMemberEntry);
		return CrossActivityActionResult.success(JsonUtil.toJsonString(resp));
	}

	/**
	 * 处理非生产环境受控验收重置与测试数据构造动作
	 */
	private CrossActivityActionResult handleAcceptanceReset(long playerId, String payloadJson) {
		if (ServerContext.getInstance().getRunMode() == null
				|| ServerContext.getInstance().getRunMode().isProduction()) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.production_gm_not_allow.ID, "生产环境禁止验收重置");
		}
		AcceptanceResetRequest req = parsePayload(payloadJson, AcceptanceResetRequest.class);
		if (req == null || StringUtils.isBlank(req.getMode())) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID, "重置模式不能为空");
		}
		String mode = req.getMode().trim();
		if (RESET_MODE_WAR.equals(mode)) {
			if (!DefaultActivityScopeResolver.SANDBOX_CGW_SCOPE_KEY.equals(this.scopeKey)) {
				return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID,
						"reset_sandbox_war 仅允许在沙盒战区 group_9998 执行");
			}
			ActivityCrossGuildWarConfig cfg = ActivityCrossGuildWarManager.instance().getNullable(this.id);
			this.strongholds.clear();
			this.guilds.clear();
			this.members.clear();
			this.finalGuildRankList.clear();
			this.rankSettled = false;
			this.nextSettleRetryTime = 0L;
			this.state = ActivityState.START_VALUE;
			resetTransientCaches();
			if (cfg != null) {
				initMissingStrongholdsFromConfig(cfg);
			}
			this.guildRankCacheDirty = true;
			markDirty();
		} else if (RESET_MODE_CLEAR_PLAYER.equals(mode)) {
			if (!PlayerSaveCoordinator.isSandboxPlayerId(playerId)) {
				return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID,
						"clear_player 仅允许沙盒调用方玩家执行");
			}
			long targetId = req.getTargetPlayerId() > 0L ? req.getTargetPlayerId() : playerId;
			if (!PlayerSaveCoordinator.isSandboxPlayerId(targetId)) {
				return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID,
						"clear_player 目标玩家必须为沙盒玩家ID");
			}
			GuildWarMemberRecord removed = this.members.remove(targetId);
			if (removed != null && removed.getLockedGuildId() > 0L) {
				GuildWarGuildRecord guild = this.guilds.get(removed.getLockedGuildId());
				if (guild != null && guild.getMemberIds() != null) {
					guild.getMemberIds().remove(targetId);
					this.dirtyMemberRankGuildIds.add(guild.getGuildId());
				}
			}
			this.guildRankCacheDirty = true;
			markDirty();
		} else if (RESET_MODE_SET_SCORES.equals(mode)) {
			if (!DefaultActivityScopeResolver.SANDBOX_CGW_SCOPE_KEY.equals(this.scopeKey)) {
				return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID,
						"set_scores_for_test 仅允许在沙盒战区 group_9998 执行");
			}
			long targetId = req.getTargetPlayerId() > 0L ? req.getTargetPlayerId() : playerId;
			applyAcceptanceTestScores(req, targetId);
		} else {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID,
					"不支持的验收重置模式: " + mode);
		}

		AcceptanceResetResponse resp = new AcceptanceResetResponse();
		resp.setMode(mode);
		resp.setState(this.state);
		resp.setStrongholdCount(this.strongholds.size());
		resp.setGuildCount(this.guilds.size());
		resp.setParticipantCount(this.members.size());
		return CrossActivityActionResult.success(JsonUtil.toJsonString(resp));
	}

	/**
	 * 为沙盒验收构造受控战功与据点占领状态
	 */
	private void applyAcceptanceTestScores(AcceptanceResetRequest req, long targetPlayerId) {
		long guildId = req.getGuildId() > 0L ? req.getGuildId() : 900_000_001L;
		long targetScore = Math.max(1L, req.getScore());
		long now = DateUtil.currentTimeMillis();
		String guildName = StringUtils.isNotBlank(req.getGuildName()) ? req.getGuildName() : "沙盒验收宗门";

		GuildWarMemberRecord member = this.members.computeIfAbsent(targetPlayerId, id -> {
			GuildWarMemberRecord created = new GuildWarMemberRecord();
			created.setPlayerId(id);
			created.setPlayerName("SandboxPlayer#" + id);
			created.setServerId(DefaultActivityScopeResolver.SANDBOX_CGW_SERVER_ID);
			return created;
		});
		member.setLockedGuildId(guildId);
		member.setScore(targetScore);
		member.setUpdateTime(now);

		GuildWarGuildRecord guild = this.guilds.computeIfAbsent(guildId, id -> {
			GuildWarGuildRecord created = new GuildWarGuildRecord();
			created.setGuildId(id);
			return created;
		});
		guild.ensureCollections();
		guild.setGuildName(guildName);
		guild.setGuildServerId(DefaultActivityScopeResolver.SANDBOX_CGW_SERVER_ID);
		guild.getMemberIds().add(targetPlayerId);
		guild.setBaseScore(targetScore);
		guild.setUpdateTime(now);

		if (req.getStrongholdId() > 0 && this.strongholds.containsKey(req.getStrongholdId())) {
			guild.getStrongholdScores().put(req.getStrongholdId(), targetScore);
			GuildWarStrongholdState sh = this.strongholds.get(req.getStrongholdId());
			sh.setOwnerGuildId(guildId);
			sh.setOwnerGuildName(guildName);
			sh.setOwnerGuildServerId(DefaultActivityScopeResolver.SANDBOX_CGW_SERVER_ID);
			sh.setOwnerScore(targetScore);
			sh.setOccupyTime(now);
		}
		this.guildRankCacheDirty = true;
		this.dirtyMemberRankGuildIds.add(guildId);
		markDirty();
	}

	/**
	 * 确保所有持久化集合字段非空，避免历史或默认反序列化空指针
	 */
	private void ensureCollectionsInitialized() {
		if (this.strongholds == null) {
			this.strongholds = new HashMap<>();
		}
		if (this.guilds == null) {
			this.guilds = new HashMap<>();
		}
		if (this.members == null) {
			this.members = new HashMap<>();
		}
		if (this.finalGuildRankList == null) {
			this.finalGuildRankList = new ArrayList<>();
		}
	}

	/**
	 * 重置全部瞬态缓存容器
	 */
	private void resetTransientCaches() {
		this.sortedGuildRankCache = new ArrayList<>();
		this.guildRankIndexCache = new HashMap<>();
		this.guildRankCacheDirty = true;
		this.sortedGuildMembersCache = new HashMap<>();
		this.dirtyMemberRankGuildIds = new HashSet<>();
	}

	/**
	 * 按配置补齐尚未创建的据点状态对象（不覆盖已有占领记录）
	 */
	private void initMissingStrongholdsFromConfig(ActivityCrossGuildWarConfig cfg) {
		if (cfg.strongholdIds == null) {
			return;
		}
		for (int i = 0; i < cfg.strongholdIds.length; i++) {
			int strongholdId = cfg.strongholdIds[i];
			if (this.strongholds.containsKey(strongholdId)) {
				continue;
			}
			GuildWarStrongholdState state = new GuildWarStrongholdState();
			state.setStrongholdId(strongholdId);
			String name = (cfg.strongholdNames != null && i < cfg.strongholdNames.length)
					? cfg.strongholdNames[i] : "据点#" + strongholdId;
			state.setName(name);
			state.setOwnerGuildName("");
			state.setOwnerGuildServerId("");
			this.strongholds.put(strongholdId, state);
		}
	}

	/**
	 * 判断活动当前是否处于可查询状态（START 或 CLOSE）
	 */
	private boolean isReadableState() {
		return this.state == ActivityState.START_VALUE || this.state == ActivityState.CLOSE_VALUE;
	}

	/**
	 * 刷新战区宗门排行榜瞬态缓存；CLOSE 期已锁榜时直接复用 {@code finalGuildRankList}
	 */
	private void ensureGuildRankCacheUpToDate() {
		if (!this.finalGuildRankList.isEmpty()) {
			if (this.guildRankCacheDirty) {
				rebuildGuildRankCacheFromFinalList();
			}
			return;
		}
		if (!this.guildRankCacheDirty) {
			return;
		}
		List<GuildWarRankSnapshotEntry> entries = buildSortedGuildRankEntries();
		this.sortedGuildRankCache = entries;
		this.guildRankIndexCache = new HashMap<>(entries.size() * 2);
		for (GuildWarRankSnapshotEntry entry : entries) {
			this.guildRankIndexCache.put(entry.getGuildId(), entry.getRank());
		}
		this.guildRankCacheDirty = false;
	}

	/**
	 * 从锁定终榜快照重建瞬态排名缓存
	 */
	private void rebuildGuildRankCacheFromFinalList() {
		this.sortedGuildRankCache = new ArrayList<>(this.finalGuildRankList);
		this.guildRankIndexCache = new HashMap<>(this.finalGuildRankList.size() * 2);
		for (GuildWarRankSnapshotEntry entry : this.finalGuildRankList) {
			this.guildRankIndexCache.put(entry.getGuildId(), entry.getRank());
		}
		this.guildRankCacheDirty = false;
	}

	/**
	 * 构建有序战区宗门榜快照列表
	 */
	private List<GuildWarRankSnapshotEntry> buildSortedGuildRankEntries() {
		Map<Long, Integer> ownedCounts = new HashMap<>();
		for (GuildWarStrongholdState sh : this.strongholds.values()) {
			if (sh != null && sh.getOwnerGuildId() > 0L) {
				ownedCounts.merge(sh.getOwnerGuildId(), 1, Integer::sum);
			}
		}
		List<GuildWarGuildRecord> list = new ArrayList<>(this.guilds.values());
		list.sort(GUILD_RANK_COMPARATOR);
		List<GuildWarRankSnapshotEntry> result = new ArrayList<>(list.size());
		for (int i = 0; i < list.size(); i++) {
			GuildWarGuildRecord guild = list.get(i);
			GuildWarRankSnapshotEntry entry = new GuildWarRankSnapshotEntry();
			entry.setRank(i + 1);
			entry.setGuildId(guild.getGuildId());
			entry.setGuildName(guild.getGuildName() != null ? guild.getGuildName() : "宗门#" + guild.getGuildId());
			entry.setGuildIcon(guild.getGuildIcon());
			entry.setGuildServerId(guild.getGuildServerId() != null ? guild.getGuildServerId() : "");
			entry.setBaseScore(guild.getBaseScore());
			entry.setStrongholdBonusScore(guild.getStrongholdBonusScore());
			entry.setTotalScore(guild.getTotalScore());
			entry.setOwnedStrongholdCount(ownedCounts.getOrDefault(guild.getGuildId(), 0));
			result.add(entry);
		}
		return result;
	}

	/**
	 * 按需重建并获取指定宗门的有序成员贡献列表
	 */
	private List<GuildWarMemberRecord> ensureMemberRankCacheUpToDate(long guildId) {
		if (!this.dirtyMemberRankGuildIds.contains(guildId) && this.sortedGuildMembersCache.containsKey(guildId)) {
			return this.sortedGuildMembersCache.get(guildId);
		}
		GuildWarGuildRecord guild = this.guilds.get(guildId);
		if (guild == null || guild.getMemberIds() == null || guild.getMemberIds().isEmpty()) {
			this.sortedGuildMembersCache.put(guildId, Collections.emptyList());
			this.dirtyMemberRankGuildIds.remove(guildId);
			return Collections.emptyList();
		}
		List<GuildWarMemberRecord> list = new ArrayList<>(guild.getMemberIds().size());
		for (Long memberId : guild.getMemberIds()) {
			if (memberId == null) {
				continue;
			}
			GuildWarMemberRecord member = this.members.get(memberId);
			if (member != null && member.getLockedGuildId() == guildId) {
				list.add(member);
			}
		}
		list.sort(MEMBER_RANK_COMPARATOR);
		this.sortedGuildMembersCache.put(guildId, list);
		this.dirtyMemberRankGuildIds.remove(guildId);
		return list;
	}

	/**
	 * 按配置据点顺序构建全部据点实时状态载荷列表
	 */
	private List<StrongholdPayload> buildStrongholdPayloads(ActivityCrossGuildWarConfig cfg,
			GuildWarGuildRecord effectiveGuild) {
		if (cfg.strongholdIds == null) {
			return Collections.emptyList();
		}
		List<StrongholdPayload> list = new ArrayList<>(cfg.strongholdIds.length);
		for (int strongholdId : cfg.strongholdIds) {
			GuildWarStrongholdState sh = this.strongholds.get(strongholdId);
			StrongholdPayload payload = new StrongholdPayload();
			payload.setStrongholdId(strongholdId);
			if (sh != null) {
				payload.setOwnerGuildId(sh.getOwnerGuildId());
				payload.setOwnerGuildName(sh.getOwnerGuildName() != null ? sh.getOwnerGuildName() : "");
				payload.setOwnerGuildIcon(sh.getOwnerGuildIcon());
				payload.setOwnerGuildServerId(sh.getOwnerGuildServerId() != null ? sh.getOwnerGuildServerId() : "");
				payload.setOwnerScore(sh.getOwnerScore());
			} else {
				payload.setOwnerGuildName("");
				payload.setOwnerGuildServerId("");
			}
			long myGuildScore = (effectiveGuild != null && effectiveGuild.getStrongholdScores() != null)
					? effectiveGuild.getStrongholdScores().getOrDefault(strongholdId, 0L) : 0L;
			payload.setMyGuildScore(myGuildScore);
			list.add(payload);
		}
		return list;
	}

	/**
	 * 检索指定宗门的排行概要载荷；未参战时返回 rank=0 占位条目
	 */
	private GuildRankEntryPayload buildGuildSummaryPayload(long guildId) {
		if (guildId > 0L) {
			Integer rank = this.guildRankIndexCache.get(guildId);
			if (rank != null && rank >= 1 && rank <= this.sortedGuildRankCache.size()) {
				return toGuildRankPayload(this.sortedGuildRankCache.get(rank - 1));
			}
		}
		GuildRankEntryPayload empty = new GuildRankEntryPayload();
		empty.setGuildId(guildId > 0L ? guildId : 0L);
		empty.setGuildName("");
		empty.setGuildServerId("");
		return empty;
	}

	/**
	 * 计算个人已达标里程碑 ID 列表（1-based 升序）
	 */
	private List<Integer> computeUnlockedPersonalMilestones(ActivityCrossGuildWarConfig cfg, long myScore) {
		if (cfg.personalMilestoneThresholds == null || myScore <= 0L) {
			return Collections.emptyList();
		}
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < cfg.personalMilestoneThresholds.length; i++) {
			if (myScore >= cfg.personalMilestoneThresholds[i]) {
				list.add(i + 1);
			}
		}
		return list;
	}

	/**
	 * 计算锁定宗门已达标里程碑 ID 列表（未锁定或个人战功不足 minParticipateScore 时返回空列表）
	 */
	private List<Integer> computeUnlockedGuildMilestones(ActivityCrossGuildWarConfig cfg,
			long lockedGuildId, long myScore) {
		if (lockedGuildId <= 0L || myScore < cfg.minParticipateScore || cfg.guildMilestoneThresholds == null) {
			return Collections.emptyList();
		}
		GuildWarGuildRecord guild = this.guilds.get(lockedGuildId);
		if (guild == null) {
			return Collections.emptyList();
		}
		long totalScore = guild.getTotalScore();
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < cfg.guildMilestoneThresholds.length; i++) {
			if (totalScore >= cfg.guildMilestoneThresholds[i]) {
				list.add(i + 1);
			}
		}
		return list;
	}

	/**
	 * 获取指定据点的占领最低宗门积分门槛
	 */
	private static long resolveStrongholdOccupyMinScore(ActivityCrossGuildWarConfig cfg, int strongholdId) {
		if (cfg.strongholdIds != null && cfg.strongholdOccupyMinScores != null) {
			for (int i = 0; i < cfg.strongholdIds.length; i++) {
				if (cfg.strongholdIds[i] == strongholdId && i < cfg.strongholdOccupyMinScores.length) {
					return cfg.strongholdOccupyMinScores[i];
				}
			}
		}
		return 1L;
	}

	/**
	 * 获取指定据点的终场霸主额外加分
	 */
	private static long resolveStrongholdBonusScore(ActivityCrossGuildWarConfig cfg, int strongholdId) {
		if (cfg.strongholdIds != null && cfg.strongholdBonusScores != null) {
			for (int i = 0; i < cfg.strongholdIds.length; i++) {
				if (cfg.strongholdIds[i] == strongholdId && i < cfg.strongholdBonusScores.length) {
					return cfg.strongholdBonusScores[i];
				}
			}
		}
		return 0L;
	}

	/**
	 * 查找排名所属的配置区间下标
	 */
	private static int findRankRangeIndex(int[][] rankRanges, int rank) {
		if (rankRanges == null || rank <= 0) {
			return -1;
		}
		for (int i = 0; i < rankRanges.length; i++) {
			int[] range = rankRanges[i];
			if (range != null && range.length >= 2 && rank >= range[0] && rank <= range[1]) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 将偶数长度物品对数组转换为 {@link Goods} 列表
	 */
	private static List<Goods> buildGoodsList(int[] pairs) {
		if (pairs == null || pairs.length < 2) {
			return Collections.emptyList();
		}
		List<Goods> list = new ArrayList<>(pairs.length / 2);
		for (int i = 0; i + 1 < pairs.length; i += 2) {
			list.add(new Goods(pairs[i], pairs[i + 1]));
		}
		return list;
	}

	/**
	 * 将宗门排行榜快照条目转换为协议传输载荷
	 */
	private static GuildRankEntryPayload toGuildRankPayload(GuildWarRankSnapshotEntry entry) {
		GuildRankEntryPayload payload = new GuildRankEntryPayload();
		payload.setRank(entry.getRank());
		payload.setGuildId(entry.getGuildId());
		payload.setGuildName(entry.getGuildName() != null ? entry.getGuildName() : "");
		payload.setGuildIcon(entry.getGuildIcon());
		payload.setGuildServerId(entry.getGuildServerId() != null ? entry.getGuildServerId() : "");
		payload.setBaseScore(entry.getBaseScore());
		payload.setStrongholdBonusScore(entry.getStrongholdBonusScore());
		payload.setTotalScore(entry.getTotalScore());
		payload.setOwnedStrongholdCount(entry.getOwnedStrongholdCount());
		return payload;
	}

	/**
	 * 将成员贡献记录转换为协议传输载荷
	 */
	private static MemberRankEntryPayload toMemberRankPayload(int rank, GuildWarMemberRecord record) {
		MemberRankEntryPayload payload = new MemberRankEntryPayload();
		payload.setRank(rank);
		payload.setPlayerId(record.getPlayerId());
		payload.setPlayerName(record.getPlayerName() != null ? record.getPlayerName() : "");
		payload.setServerId(record.getServerId() != null ? record.getServerId() : "");
		payload.setScore(record.getScore());
		return payload;
	}

	/**
	 * 解析动作 JSON 请求载荷；解析失败时记录告警并返回 null
	 */
	private static <T> T parsePayload(String payloadJson, Class<T> clazz) {
		if (StringUtils.isBlank(payloadJson)) {
			return null;
		}
		try {
			return JsonUtil.parseObject(payloadJson, clazz);
		} catch (Exception e) {
			log.warn("解析跨服宗门争霸动作载荷失败: type={}", clazz.getSimpleName(), e);
			return null;
		}
	}

	/**
	 * 据点实时状态持久化 POJO
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class GuildWarStrongholdState {
		/** 据点 ID */
		private int strongholdId;
		/** 据点名称 */
		private String name;
		/** 当前霸主宗门 ID */
		private long ownerGuildId;
		/** 当前霸主宗门名称 */
		private String ownerGuildName;
		/** 当前霸主宗门图标 */
		private int ownerGuildIcon;
		/** 当前霸主宗门区服 ID */
		private String ownerGuildServerId;
		/** 当前霸主在该据点的累计积分 */
		private long ownerScore;
		/** 最近占领易主时间戳 */
		private long occupyTime;

		/** 无参构造 */
		public GuildWarStrongholdState() {
		}

		public int getStrongholdId() {
			return strongholdId;
		}

		public void setStrongholdId(int strongholdId) {
			this.strongholdId = strongholdId;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public long getOwnerGuildId() {
			return ownerGuildId;
		}

		public void setOwnerGuildId(long ownerGuildId) {
			this.ownerGuildId = ownerGuildId;
		}

		public String getOwnerGuildName() {
			return ownerGuildName;
		}

		public void setOwnerGuildName(String ownerGuildName) {
			this.ownerGuildName = ownerGuildName;
		}

		public int getOwnerGuildIcon() {
			return ownerGuildIcon;
		}

		public void setOwnerGuildIcon(int ownerGuildIcon) {
			this.ownerGuildIcon = ownerGuildIcon;
		}

		public String getOwnerGuildServerId() {
			return ownerGuildServerId;
		}

		public void setOwnerGuildServerId(String ownerGuildServerId) {
			this.ownerGuildServerId = ownerGuildServerId;
		}

		public long getOwnerScore() {
			return ownerScore;
		}

		public void setOwnerScore(long ownerScore) {
			this.ownerScore = ownerScore;
		}

		public long getOccupyTime() {
			return occupyTime;
		}

		public void setOccupyTime(long occupyTime) {
			this.occupyTime = occupyTime;
		}
	}

	/**
	 * 宗门战功汇总持久化 POJO
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class GuildWarGuildRecord {
		/** 宗门 ID */
		private long guildId;
		/** 宗门名称 */
		private String guildName;
		/** 宗门图标 */
		private int guildIcon;
		/** 宗门区服 ID */
		private String guildServerId;
		/** 宗门累计基础战功 */
		private long baseScore;
		/** 终场据点霸主加分之和 */
		private long strongholdBonusScore;
		/** 最后战功更新时间戳 */
		private long updateTime;
		/** 各据点累计积分映射 */
		private Map<Integer, Long> strongholdScores = new HashMap<>();
		/** 在本宗门锁定参战的玩家 ID 集合 */
		private Set<Long> memberIds = new HashSet<>();

		/** 无参构造 */
		public GuildWarGuildRecord() {
		}

		/**
		 * 补齐反序列化后可能为 null 的内部集合字段
		 */
		public void ensureCollections() {
			if (this.strongholdScores == null) {
				this.strongholdScores = new HashMap<>();
			}
			if (this.memberIds == null) {
				this.memberIds = new HashSet<>();
			}
		}

		/**
		 * 获取宗门总战功（基础战功 + 终场据点霸主加分）
		 *
		 * @return 宗门总战功
		 */
		@JsonIgnore
		public long getTotalScore() {
			return baseScore + strongholdBonusScore;
		}

		public long getGuildId() {
			return guildId;
		}

		public void setGuildId(long guildId) {
			this.guildId = guildId;
		}

		public String getGuildName() {
			return guildName;
		}

		public void setGuildName(String guildName) {
			this.guildName = guildName;
		}

		public int getGuildIcon() {
			return guildIcon;
		}

		public void setGuildIcon(int guildIcon) {
			this.guildIcon = guildIcon;
		}

		public String getGuildServerId() {
			return guildServerId;
		}

		public void setGuildServerId(String guildServerId) {
			this.guildServerId = guildServerId;
		}

		public long getBaseScore() {
			return baseScore;
		}

		public void setBaseScore(long baseScore) {
			this.baseScore = baseScore;
		}

		public long getStrongholdBonusScore() {
			return strongholdBonusScore;
		}

		public void setStrongholdBonusScore(long strongholdBonusScore) {
			this.strongholdBonusScore = strongholdBonusScore;
		}

		public long getUpdateTime() {
			return updateTime;
		}

		public void setUpdateTime(long updateTime) {
			this.updateTime = updateTime;
		}

		public Map<Integer, Long> getStrongholdScores() {
			return strongholdScores;
		}

		public void setStrongholdScores(Map<Integer, Long> strongholdScores) {
			this.strongholdScores = strongholdScores;
		}

		public Set<Long> getMemberIds() {
			return memberIds;
		}

		public void setMemberIds(Set<Long> memberIds) {
			this.memberIds = memberIds;
		}
	}

	/**
	 * 锁定参战成员贡献持久化 POJO
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class GuildWarMemberRecord {
		/** 玩家 ID */
		private long playerId;
		/** 玩家昵称 */
		private String playerName;
		/** 玩家区服 ID */
		private String serverId;
		/** 本期锁定参战宗门 ID */
		private long lockedGuildId;
		/** 累计个人战功 */
		private long score;
		/** 最后战功更新时间戳 */
		private long updateTime;

		/** 无参构造 */
		public GuildWarMemberRecord() {
		}

		public long getPlayerId() {
			return playerId;
		}

		public void setPlayerId(long playerId) {
			this.playerId = playerId;
		}

		public String getPlayerName() {
			return playerName;
		}

		public void setPlayerName(String playerName) {
			this.playerName = playerName;
		}

		public String getServerId() {
			return serverId;
		}

		public void setServerId(String serverId) {
			this.serverId = serverId;
		}

		public long getLockedGuildId() {
			return lockedGuildId;
		}

		public void setLockedGuildId(long lockedGuildId) {
			this.lockedGuildId = lockedGuildId;
		}

		public long getScore() {
			return score;
		}

		public void setScore(long score) {
			this.score = score;
		}

		public long getUpdateTime() {
			return updateTime;
		}

		public void setUpdateTime(long updateTime) {
			this.updateTime = updateTime;
		}
	}

	/**
	 * 战区宗门排行快照条目持久化 POJO
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class GuildWarRankSnapshotEntry {
		/** 排名（1-based） */
		private int rank;
		/** 宗门 ID */
		private long guildId;
		/** 宗门名称 */
		private String guildName;
		/** 宗门图标 */
		private int guildIcon;
		/** 宗门区服 ID */
		private String guildServerId;
		/** 基础战功 */
		private long baseScore;
		/** 终场据点霸主加分 */
		private long strongholdBonusScore;
		/** 总战功 */
		private long totalScore;
		/** 占领据点数量 */
		private int ownedStrongholdCount;

		/** 无参构造 */
		public GuildWarRankSnapshotEntry() {
		}

		public int getRank() {
			return rank;
		}

		public void setRank(int rank) {
			this.rank = rank;
		}

		public long getGuildId() {
			return guildId;
		}

		public void setGuildId(long guildId) {
			this.guildId = guildId;
		}

		public String getGuildName() {
			return guildName;
		}

		public void setGuildName(String guildName) {
			this.guildName = guildName;
		}

		public int getGuildIcon() {
			return guildIcon;
		}

		public void setGuildIcon(int guildIcon) {
			this.guildIcon = guildIcon;
		}

		public String getGuildServerId() {
			return guildServerId;
		}

		public void setGuildServerId(String guildServerId) {
			this.guildServerId = guildServerId;
		}

		public long getBaseScore() {
			return baseScore;
		}

		public void setBaseScore(long baseScore) {
			this.baseScore = baseScore;
		}

		public long getStrongholdBonusScore() {
			return strongholdBonusScore;
		}

		public void setStrongholdBonusScore(long strongholdBonusScore) {
			this.strongholdBonusScore = strongholdBonusScore;
		}

		public long getTotalScore() {
			return totalScore;
		}

		public void setTotalScore(long totalScore) {
			this.totalScore = totalScore;
		}

		public int getOwnedStrongholdCount() {
			return ownedStrongholdCount;
		}

		public void setOwnedStrongholdCount(int ownedStrongholdCount) {
			this.ownedStrongholdCount = ownedStrongholdCount;
		}
	}
}
