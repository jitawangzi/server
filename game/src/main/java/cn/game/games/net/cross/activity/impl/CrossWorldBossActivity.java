package cn.game.games.net.cross.activity.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import cn.game.core.base.ServerContext;
import cn.game.games.net.common.module.activity.DefaultActivityScopeResolver;
import cn.game.games.net.cross.activity.CrossActivityBase;
import cn.game.games.net.cross.activity.dto.CrossActivityActionResult;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.AcceptanceResetRequest;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.AcceptanceResetResponse;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.AttackRequest;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.AttackResponse;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.ClaimKillRewardRequest;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.ClaimKillRewardResponse;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.ClaimMilestoneRequest;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.ClaimMilestoneResponse;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.QueryDetailRequest;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.QueryDetailResponse;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.QueryRankRequest;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.QueryRankResponse;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.RankEntryPayload;
import cn.game.games.net.cross.activity.service.CrossActivityService;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.helper.PlayerSaveCoordinator;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.award.Goods;
import cn.game.protocol.generated.config.ActivityCrossWorldBossConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.manager.ActivityCrossWorldBossManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.ActivityMsg.ActivityState;
import cn.game.util.DateUtil;
import cn.game.util.JsonUtil;

/**
 * 跨服世界Boss Cross 权威领域实体。
 * <p>
 * 职责边界：按战区 {@code scopeKey} 在 {@code activityUid} 单邮箱内串行维护全战区 Boss 血量、里程碑解锁、
 * 唯一最后一击归属、伤害排行榜及周期结束锁定结算发邮；严禁在此向 Game 发起任何同步阻塞 {@code await} 调用。
 * </p>
 *
 * @author shuyongqiang
 */
@ActivityType(type = ActivityTypeEnum.CrossWorldBoss)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrossWorldBossActivity extends CrossActivityBase {

	/** 跨服世界Boss实体日志器 */
	private static final Logger log = LoggerFactory.getLogger(CrossWorldBossActivity.class);

	/** 动作常量：查询 Boss 详情与本人战区进度 */
	public static final String ACTION_QUERY_DETAIL = "query_detail";
	/** 动作常量：上报攻打伤害并扣血 */
	public static final String ACTION_ATTACK = "attack";
	/** 动作常量：校验领取战区血量阶段里程碑奖励 */
	public static final String ACTION_CLAIM_MILESTONE = "claim_milestone";
	/** 动作常量：确认或断线补领最后一击击杀奖励 */
	public static final String ACTION_CLAIM_KILL_REWARD = "claim_kill_reward";
	/** 动作常量：分页查询本战区伤害排行榜 */
	public static final String ACTION_QUERY_RANK = "query_rank";
	/** 动作常量：非生产环境受控验收重置 */
	public static final String ACTION_ACCEPTANCE_RESET = "acceptance_reset";

	/** 验收重置模式：恢复沙盒战区 Boss 初始满血态 */
	public static final String RESET_MODE_BOSS = "reset_sandbox_boss";
	/** 验收重置模式：清除指定沙盒玩家伤害记录 */
	public static final String RESET_MODE_CLEAR_PLAYER = "clear_player";
	/** 验收重置模式：推进沙盒战区 Boss 血量至目标里程碑阈值 */
	public static final String RESET_MODE_SET_MILESTONE = "set_damage_for_milestone";

	/** 奖励类型常量：战区血量阶段里程碑奖 */
	public static final int REWARD_TYPE_MILESTONE = 1;
	/** 奖励类型常量：最后一击击杀奖 */
	public static final int REWARD_TYPE_KILL = 2;

	/** 血量削减万分比计算分母 */
	private static final int MILESTONE_DENOMINATOR = 10000;
	/** 排行榜单页最大查询条数 */
	private static final int MAX_RANK_PAGE_SIZE = 100;

	/** 伤害榜排序比较器：伤害降序，同伤害先达成时间升序，再按 playerId 升序 */
	private static final Comparator<CrossBossDamageRecord> DAMAGE_RANK_COMPARATOR = (a, b) -> {
		int cmp = Long.compare(b.getDamage(), a.getDamage());
		if (cmp != 0) {
			return cmp;
		}
		cmp = Long.compare(a.getUpdateTime(), b.getUpdateTime());
		if (cmp != 0) {
			return cmp;
		}
		return Long.compare(a.getPlayerId(), b.getPlayerId());
	};

	/** 可注入异步发邮 SPI（默认绑定 MailHelper 异步投递） */
	@JsonIgnore
	private static volatile CrossWorldBossMailSender mailSender = MailHelper::sendMail;

	/** Boss 最大血量 */
	protected long maxHp;
	/** Boss 当前剩余血量 */
	protected long currentHp;
	/** 战区累计造成有效伤害 */
	protected long totalDamage;
	/** Boss 是否已被击杀 */
	protected boolean killed;
	/** 最后一击击杀者玩家 ID（未击杀时为 0） */
	protected long killerPlayerId;
	/** 最后一击击杀者昵称 */
	protected String killerName = "";
	/** 最后一击击杀者所属逻辑服 ID */
	protected String killerServerId = "";
	/** 击杀时间戳（毫秒） */
	protected long killTime;
	/** 最后一击击杀奖是否已领取或已确认发放 */
	protected boolean killRewardClaimed;
	/** 战区已解锁的里程碑 ID 列表（1-based） */
	protected List<Integer> unlockedMilestoneIds = new ArrayList<>();
	/** 玩家伤害记录映射（playerId -> 累计伤害档案） */
	protected Map<Long, CrossBossDamageRecord> damageMap = new HashMap<>();
	/** 活动结束时锁定的最终排行榜快照 */
	protected List<CrossBossRankEntry> finalRankList = new ArrayList<>();
	/** 排行榜结算邮件是否已完成落盘锁定的幂等标记 */
	protected boolean rankSettled;

	/** 瞬态运行期排序榜缓存 */
	@JsonIgnore
	private transient List<CrossBossRankEntry> sortedRankCache = new ArrayList<>();
	/** 瞬态运行期玩家名次索引缓存（playerId -> 1-based rank） */
	@JsonIgnore
	private transient Map<Long, Integer> playerRankIndexCache = new HashMap<>();
	/** 瞬态排行榜缓存脏标记 */
	@JsonIgnore
	private transient boolean rankCacheDirty = true;

	/**
	 * 构造跨服世界Boss实体（保留无参构造以兼容 Jackson 多态反序列化）
	 */
	public CrossWorldBossActivity() {
	}

	/**
	 * 注入自定义异步发邮器（供单元测试拦截断言邮件次数与附件内容）
	 *
	 * @param sender 自定义发邮 SPI；传入 null 时恢复默认 {@link MailHelper#sendMail}
	 */
	public static void setMailSender(CrossWorldBossMailSender sender) {
		mailSender = (sender != null ? sender : MailHelper::sendMail);
	}

	/**
	 * 恢复默认异步发邮器为 {@link MailHelper#sendMail}
	 */
	public static void resetMailSender() {
		mailSender = MailHelper::sendMail;
	}

	/**
	 * 新建活动实例启动初始化：从配置读取最大血量并重置全部战区战斗状态
	 *
	 * @throws IllegalStateException 缺失对应 {@link ActivityCrossWorldBossConfig} 配置时抛出
	 */
	@Override
	protected void onStart() {
		ActivityCrossWorldBossConfig cfg = ActivityCrossWorldBossManager.instance().getNullable(this.id);
		if (cfg == null) {
			log.error("CrossWorldBoss 启动初始化缺少配置: configId={}, uid={}, scopeKey={}", id, uid, scopeKey);
			throw new IllegalStateException("CrossWorldBoss 启动初始化缺少配置: configId=" + id);
		}
		this.maxHp = cfg.maxHp;
		this.currentHp = cfg.maxHp;
		this.totalDamage = 0L;
		this.killed = false;
		this.killerPlayerId = 0L;
		this.killerName = "";
		this.killerServerId = "";
		this.killTime = 0L;
		this.killRewardClaimed = false;
		this.unlockedMilestoneIds = new ArrayList<>();
		this.damageMap = new HashMap<>();
		this.finalRankList = new ArrayList<>();
		this.rankSettled = false;
		this.sortedRankCache = new ArrayList<>();
		this.playerRankIndexCache = new HashMap<>();
		this.rankCacheDirty = true;
	}

	/**
	 * 从数据库恢复反序列化后的重建与防御自愈逻辑：
	 * 1. 对历史或缺省集合字段补齐空容器；
	 * 2. 若 {@code maxHp <= 0} 则回补静态配置或兜底 1 防止里程碑除零异常；
	 * 3. 重建排行榜瞬态索引；
	 * 4. 若处于 {@code CLOSE} 状态但尚未完成结算落盘（{@code !rankSettled}），记录告警并补触发 {@link #onEnd()}。
	 */
	@Override
	public void afterLoadFromDb() {
		ensureCollectionsInitialized();
		if (this.killerName == null) {
			this.killerName = "";
		}
		if (this.killerServerId == null) {
			this.killerServerId = "";
		}

		// 防御 maxHp 为 0 导致后续里程碑比例计算出现除零异常
		if (this.maxHp <= 0L) {
			ActivityCrossWorldBossConfig cfg = ActivityCrossWorldBossManager.instance().getNullable(this.id);
			if (cfg != null && cfg.maxHp > 0L) {
				this.maxHp = cfg.maxHp;
				if (this.currentHp <= 0L && !this.killed && this.totalDamage == 0L) {
					this.currentHp = cfg.maxHp;
				}
			} else {
				log.warn("CrossWorldBoss 冷恢复检测到非法 maxHp 且无可用配置，兜底设为 1: configId={}, uid={}, scopeKey={}, maxHp={}",
						id, uid, scopeKey, maxHp);
				this.maxHp = 1L;
			}
		}

		if (!this.finalRankList.isEmpty()) {
			rebuildRankCacheFromFinalList();
		} else {
			this.rankCacheDirty = true;
			ensureRankCacheUpToDate();
		}

		// 若历史停服在 end() 写库失败后重启，状态已为 CLOSE 但 rankSettled 仍为 false，此处补触发一次幂等结算
		if (this.state == ActivityState.CLOSE_VALUE && !this.rankSettled) {
			log.warn("CrossWorldBoss 加载恢复检测到 CLOSE 状态尚未完成结算，触发自愈补结算: configId={}, uid={}, scopeKey={}",
					id, uid, scopeKey);
			onEnd();
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
	 * 在活动邮箱内单线程串行执行玩家业务动作
	 *
	 * @param playerId    玩家 ID
	 * @param actionType  动作类型（见 {@code ACTION_*} 常量）
	 * @param payloadJson 动作 JSON 请求载荷
	 * @return 执行结果 DTO（含错误码或结果 JSON 载荷）
	 */
	@Override
	public CrossActivityActionResult executePlayerAction(long playerId, String actionType, String payloadJson) {
		if (StringUtils.isBlank(actionType)) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID, "动作类型不能为空");
		}
		ensureCollectionsInitialized();
		switch (actionType) {
		case ACTION_QUERY_DETAIL:
			return handleQueryDetail(playerId, payloadJson);
		case ACTION_ATTACK:
			return handleAttack(playerId, payloadJson);
		case ACTION_CLAIM_MILESTONE:
			return handleClaimMilestone(playerId, payloadJson);
		case ACTION_CLAIM_KILL_REWARD:
			return handleClaimKillReward(playerId, payloadJson);
		case ACTION_QUERY_RANK:
			return handleQueryRank(playerId, payloadJson);
		case ACTION_ACCEPTANCE_RESET:
			return handleAcceptanceReset(playerId, payloadJson);
		default:
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID,
					"不支持的跨服世界Boss动作: " + actionType);
		}
	}

	/**
	 * 活动结束进入领奖展示期：锁定战区最终伤害榜，先强制持久化 {@code rankSettled = true}，再异步逐人发送排名邮件。
	 * <p>
	 * 若落盘失败则回滚 {@code rankSettled = false} 并中止发邮，避免宕机重启漏发或重复发邮。
	 * </p>
	 */
	@Override
	protected void onEnd() {
		ensureCollectionsInitialized();
		if (this.rankSettled) {
			return;
		}

		// 锁定并固化最终排行榜快照（CLOSE 期后续查榜直接读取此锁定结果）
		List<CrossBossRankEntry> lockedRank = buildSortedRankEntries();
		this.finalRankList = lockedRank;
		rebuildRankCacheFromFinalList();

		// 先置幂等标记并强制同步落盘，落盘失败则回滚标志并中止发邮，等待补偿重试
		try {
			this.rankSettled = true;
			markDirty();
			CrossActivityService.getInstance().flushActivityInMailbox(this);
		} catch (Exception e) {
			this.rankSettled = false;
			markDirty();
			log.error("跨服世界Boss结算落盘失败，已回滚 rankSettled 并中止发邮: uid={}, scopeKey={}", uid, scopeKey, e);
			return;
		}

		ActivityCrossWorldBossConfig cfg = ActivityCrossWorldBossManager.instance().getNullable(this.id);
		if (cfg == null) {
			log.error("跨服世界Boss结算发邮缺少配置: configId={}, uid={}, scopeKey={}", id, uid, scopeKey);
			return;
		}

		// 单向异步发送排名结算邮件，严禁阻塞等待 Game 返回
		for (CrossBossRankEntry entry : this.finalRankList) {
			if (entry.getDamage() < cfg.minParticipateDamage) {
				continue;
			}
			int rangeIdx = findRankRangeIndex(cfg.rankRanges, entry.getRank());
			if (rangeIdx < 0 || cfg.rankRewards == null || rangeIdx >= cfg.rankRewards.length) {
				continue;
			}
			List<Goods> goodsList = buildGoodsList(cfg.rankRewards[rangeIdx]);
			if (goodsList.isEmpty()) {
				continue;
			}
			try {
				mailSender.sendMail(entry.getPlayerId(), cfg.rankMailId, goodsList, true);
			} catch (Exception e) {
				log.error("跨服世界Boss排名邮件发送异常: uid={}, playerId={}, rank={}",
						uid, entry.getPlayerId(), entry.getRank(), e);
			}
		}
	}

	/**
	 * 处理查询活动详情动作（允许 START 与 CLOSE 期，不标记脏数据）
	 */
	private CrossActivityActionResult handleQueryDetail(long playerId, String payloadJson) {
		if (!isReadableState()) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.not_open.ID, "活动未处于可查询状态");
		}
		QueryDetailRequest req = parsePayload(playerId, ACTION_QUERY_DETAIL, payloadJson, QueryDetailRequest.class);
		if (req == null) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID, "查询详情参数非法");
		}
		long effectivePlayerId = playerId > 0 ? playerId : req.getPlayerId();
		ensureRankCacheUpToDate();

		CrossBossDamageRecord rec = damageMap.get(effectivePlayerId);
		long myDamage = rec != null ? rec.getDamage() : 0L;
		int myRank = playerRankIndexCache.getOrDefault(effectivePlayerId, 0);

		QueryDetailResponse resp = new QueryDetailResponse();
		resp.setState(this.state);
		resp.setMaxHp(this.maxHp);
		resp.setCurrentHp(this.currentHp);
		resp.setTotalDamage(this.totalDamage);
		resp.setKilled(this.killed);
		resp.setKillerPlayerId(this.killerPlayerId);
		resp.setKillerName(this.killerName);
		resp.setKillerServerId(this.killerServerId);
		resp.setKillTime(this.killTime);
		resp.setKillRewardClaimed(this.killRewardClaimed);
		resp.setUnlockedMilestoneIds(new ArrayList<>(this.unlockedMilestoneIds));
		resp.setMyTotalDamage(myDamage);
		resp.setMyRank(myRank);
		resp.setParticipantCount(this.damageMap.size());
		return CrossActivityActionResult.success(JsonUtil.toJsonString(resp));
	}

	/**
	 * 处理玩家攻打动作：严格校验 START 状态、未击杀与单次伤害上限，原子扣血、解锁里程碑与判定唯一最后一击
	 */
	private CrossActivityActionResult handleAttack(long playerId, String payloadJson) {
		if (this.state != ActivityState.START_VALUE) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.not_open.ID, "活动不在攻打期");
		}
		if (this.killed || this.currentHp <= 0L) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.cross_world_boss_killed.ID, "跨服世界Boss已被击杀");
		}

		ActivityCrossWorldBossConfig cfg = ActivityCrossWorldBossManager.instance().getNullable(this.id);
		if (cfg == null) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.config_data_not_found.ID, "跨服世界Boss配置缺失");
		}

		AttackRequest req = parsePayload(playerId, ACTION_ATTACK, payloadJson, AttackRequest.class);
		if (req == null) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID, "攻打参数非法");
		}
		long damage = req.getDamage();
		if (damage <= 0L || damage > cfg.maxSingleDamage) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID,
					"单次伤害越界: damage=" + damage);
		}

		long effectivePlayerId = playerId > 0 ? playerId : req.getPlayerId();
		if (effectivePlayerId <= 0L) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID, "玩家ID非法");
		}
		long now = DateUtil.currentTimeMillis();
		long actualDamage = Math.min(damage, this.currentHp);
		this.currentHp -= actualDamage;
		this.totalDamage += actualDamage;

		// 累计个人伤害并刷新达成时间戳，确保同伤害按先达成时刻排序
		CrossBossDamageRecord record = damageMap.computeIfAbsent(effectivePlayerId, idKey -> {
			CrossBossDamageRecord created = new CrossBossDamageRecord();
			created.setPlayerId(idKey);
			return created;
		});
		if (StringUtils.isNotEmpty(req.getPlayerName())) {
			record.setPlayerName(req.getPlayerName());
		}
		if (StringUtils.isNotEmpty(req.getServerId())) {
			record.setServerId(req.getServerId());
		}
		record.setDamage(record.getDamage() + actualDamage);
		record.setUpdateTime(now);
		if (actualDamage > record.getMaxSingleHit()) {
			record.setMaxSingleHit(actualDamage);
		}
		this.rankCacheDirty = true;

		boolean isKillShot = false;
		if (this.currentHp <= 0L) {
			this.currentHp = 0L;
			this.killed = true;
			this.killerPlayerId = effectivePlayerId;
			this.killerName = record.getPlayerName();
			this.killerServerId = record.getServerId();
			this.killTime = now;
			this.killRewardClaimed = false;
			isKillShot = true;
		}

		unlockMilestonesByHp(cfg);
		ensureRankCacheUpToDate();
		markDirty();

		// 击杀瞬间强制同步落盘击杀者归属与零血量状态，防止宕机丢击杀事实
		if (isKillShot) {
			try {
				CrossActivityService.getInstance().flushActivityInMailbox(this);
			} catch (Exception e) {
				log.error("跨服世界Boss击杀瞬间强刷落盘失败，保留脏标等待补偿: uid={}, killerPlayerId={}",
						uid, killerPlayerId, e);
			}
		}

		AttackResponse resp = new AttackResponse();
		resp.setActualDamage(actualDamage);
		resp.setRemainingHp(this.currentHp);
		resp.setMaxHp(this.maxHp);
		resp.setKilled(this.killed);
		resp.setKillShot(isKillShot);
		resp.setKillerPlayerId(this.killerPlayerId);
		resp.setKillerName(this.killerName);
		resp.setKillerServerId(this.killerServerId);
		resp.setKillTime(this.killTime);
		resp.setMyTotalDamage(record.getDamage());
		resp.setMyRank(playerRankIndexCache.getOrDefault(effectivePlayerId, 0));
		resp.setUnlockedMilestoneIds(new ArrayList<>(this.unlockedMilestoneIds));
		return CrossActivityActionResult.success(JsonUtil.toJsonString(resp));
	}

	/**
	 * 处理战区血量阶段里程碑领取校验动作（允许 START 与 CLOSE 期）
	 */
	private CrossActivityActionResult handleClaimMilestone(long playerId, String payloadJson) {
		if (!isReadableState()) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.not_open.ID, "活动未处于可领奖状态");
		}
		ActivityCrossWorldBossConfig cfg = ActivityCrossWorldBossManager.instance().getNullable(this.id);
		if (cfg == null) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.config_data_not_found.ID, "跨服世界Boss配置缺失");
		}

		ClaimMilestoneRequest req = parsePayload(playerId, ACTION_CLAIM_MILESTONE, payloadJson, ClaimMilestoneRequest.class);
		if (req == null) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID, "领取里程碑参数非法");
		}
		int milestoneId = req.getMilestoneId();
		if (cfg.milestoneThresholds == null || milestoneId < 1 || milestoneId > cfg.milestoneThresholds.length) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID,
					"非法的里程碑 ID: " + milestoneId);
		}
		if (!this.unlockedMilestoneIds.contains(milestoneId)) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.condition_check_error.ID, "目标里程碑尚未解锁");
		}

		long effectivePlayerId = playerId > 0 ? playerId : req.getPlayerId();
		if (effectivePlayerId <= 0L) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID, "玩家ID非法");
		}
		CrossBossDamageRecord rec = damageMap.get(effectivePlayerId);
		long myDamage = rec != null ? rec.getDamage() : 0L;
		if (myDamage < cfg.minParticipateDamage) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.condition_check_error.ID,
					"个人累计伤害未达最低参与门槛");
		}

		ClaimMilestoneResponse resp = new ClaimMilestoneResponse();
		resp.setMilestoneId(milestoneId);
		resp.setMyTotalDamage(myDamage);
		resp.setUnlockedMilestoneIds(new ArrayList<>(this.unlockedMilestoneIds));
		return CrossActivityActionResult.success(JsonUtil.toJsonString(resp));
	}

	/**
	 * 处理最后一击击杀奖励的直发幂等确认或断线补领动作
	 */
	private CrossActivityActionResult handleClaimKillReward(long playerId, String payloadJson) {
		if (!isReadableState()) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.not_open.ID, "活动未处于可领奖状态");
		}
		ClaimKillRewardRequest req = parsePayload(playerId, ACTION_CLAIM_KILL_REWARD, payloadJson,
				ClaimKillRewardRequest.class);
		if (req == null) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID, "击杀奖励请求参数非法");
		}
		long effectivePlayerId = playerId > 0 ? playerId : req.getPlayerId();
		if (effectivePlayerId <= 0L) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID, "玩家ID非法");
		}

		if (req.isConfirmOnly()) {
			if (this.killed && this.killerPlayerId == effectivePlayerId && !this.killRewardClaimed) {
				this.killRewardClaimed = true;
				markDirty();
				try {
					CrossActivityService.getInstance().flushActivityInMailbox(this);
				} catch (Exception e) {
					this.killRewardClaimed = false;
					markDirty();
					log.warn("确认击杀奖励直发状态强刷落盘失败，已保留待补领状态: uid={}, playerId={}",
							uid, effectivePlayerId, e);
				}
			}
			ClaimKillRewardResponse resp = new ClaimKillRewardResponse();
			resp.setKillRewardClaimed(this.killRewardClaimed);
			resp.setKillerPlayerId(this.killerPlayerId);
			return CrossActivityActionResult.success(JsonUtil.toJsonString(resp));
		}

		if (!this.killed) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.condition_check_error.ID, "Boss 尚未被击杀");
		}
		if (this.killerPlayerId != effectivePlayerId) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.cross_world_boss_not_killer.ID,
					"非最后一击击杀者不可领取");
		}
		if (this.killRewardClaimed) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.repeat_request.ID, "最后一击奖励已领取");
		}

		this.killRewardClaimed = true;
		markDirty();
		try {
			CrossActivityService.getInstance().flushActivityInMailbox(this);
		} catch (Exception e) {
			this.killRewardClaimed = false;
			markDirty();
			log.error("补领击杀奖励落盘失败，已回滚 killRewardClaimed: uid={}, playerId={}", uid, effectivePlayerId, e);
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_remote_server.ID, "击杀奖励确认持久化失败");
		}

		ClaimKillRewardResponse resp = new ClaimKillRewardResponse();
		resp.setKillRewardClaimed(true);
		resp.setKillerPlayerId(this.killerPlayerId);
		return CrossActivityActionResult.success(JsonUtil.toJsonString(resp));
	}

	/**
	 * 处理分页查询本战区伤害排行榜动作（只读，不标脏）
	 */
	private CrossActivityActionResult handleQueryRank(long playerId, String payloadJson) {
		if (!isReadableState()) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.not_open.ID, "活动未处于可查询状态");
		}
		QueryRankRequest req = parsePayload(playerId, ACTION_QUERY_RANK, payloadJson, QueryRankRequest.class);
		if (req == null) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID, "查询排行参数非法");
		}
		int page = req.getPage();
		int pageSize = req.getPageSize();
		if (page < 1 || pageSize < 1 || pageSize > MAX_RANK_PAGE_SIZE) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID,
					"分页参数非法: page=" + page + ", pageSize=" + pageSize);
		}

		long effectivePlayerId = playerId > 0 ? playerId : req.getPlayerId();
		ensureRankCacheUpToDate();

		int totalCount = sortedRankCache.size();
		long fromIndexLong = (long) (page - 1) * pageSize;
		List<RankEntryPayload> pageEntries = new ArrayList<>();
		if (fromIndexLong < totalCount) {
			int fromIndex = (int) fromIndexLong;
			int toIndex = (int) Math.min((long) totalCount, fromIndexLong + pageSize);
			for (int i = fromIndex; i < toIndex; i++) {
				CrossBossRankEntry entry = sortedRankCache.get(i);
				pageEntries.add(new RankEntryPayload(entry.getRank(), entry.getPlayerId(),
						entry.getPlayerName(), entry.getServerId(), entry.getDamage()));
			}
		}

		CrossBossDamageRecord rec = damageMap.get(effectivePlayerId);
		QueryRankResponse resp = new QueryRankResponse();
		resp.setTotalCount(totalCount);
		resp.setPage(page);
		resp.setPageSize(pageSize);
		resp.setEntries(pageEntries);
		resp.setMyRank(playerRankIndexCache.getOrDefault(effectivePlayerId, 0));
		resp.setMyDamage(rec != null ? rec.getDamage() : 0L);
		return CrossActivityActionResult.success(JsonUtil.toJsonString(resp));
	}

	/**
	 * 处理非生产环境受控验收重置动作（生产环境硬拦截）
	 */
	private CrossActivityActionResult handleAcceptanceReset(long playerId, String payloadJson) {
		if (ServerContext.getInstance().getRunMode() == null || ServerContext.getInstance().getRunMode().isProduction()) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.production_gm_not_allow.ID, "生产环境禁止验收重置");
		}
		AcceptanceResetRequest req = parsePayload(playerId, ACTION_ACCEPTANCE_RESET, payloadJson,
				AcceptanceResetRequest.class);
		if (req == null) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID, "验收重置参数非法");
		}
		long effectivePlayerId = playerId > 0 ? playerId : req.getPlayerId();
		boolean isSandboxScope = DefaultActivityScopeResolver.SANDBOX_SCOPE_KEY.equals(this.scopeKey);
		boolean isSandboxPlayer = PlayerSaveCoordinator.isSandboxPlayerId(effectivePlayerId);
		if (!isSandboxScope && !isSandboxPlayer) {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID,
					"仅允许对沙盒隔离战区或沙盒玩家执行验收重置");
		}

		ActivityCrossWorldBossConfig cfg = ActivityCrossWorldBossManager.instance().getNullable(this.id);
		String mode = req.getMode();
		if (RESET_MODE_BOSS.equals(mode)) {
			long resetMaxHp = (cfg != null && cfg.maxHp > 0L) ? cfg.maxHp : Math.max(1L, this.maxHp);
			this.maxHp = resetMaxHp;
			this.currentHp = resetMaxHp;
			this.totalDamage = 0L;
			this.killed = false;
			this.killerPlayerId = 0L;
			this.killerName = "";
			this.killerServerId = "";
			this.killTime = 0L;
			this.killRewardClaimed = false;
			this.unlockedMilestoneIds.clear();
			this.damageMap.clear();
			this.finalRankList.clear();
			this.rankSettled = false;
			this.state = ActivityState.START_VALUE;
			this.sortedRankCache.clear();
			this.playerRankIndexCache.clear();
			this.rankCacheDirty = false;
			markDirty();
		} else if (RESET_MODE_CLEAR_PLAYER.equals(mode)) {
			this.damageMap.remove(effectivePlayerId);
			this.rankCacheDirty = true;
			ensureRankCacheUpToDate();
			markDirty();
		} else if (RESET_MODE_SET_MILESTONE.equals(mode)) {
			if (cfg == null) {
				return CrossActivityActionResult.fail(ErrorMsgEnum.config_data_not_found.ID, "跨服世界Boss配置缺失");
			}
			int targetThreshold = req.getTargetThreshold();
			if (targetThreshold <= 0 && cfg.milestoneThresholds != null
					&& req.getMilestoneId() >= 1 && req.getMilestoneId() <= cfg.milestoneThresholds.length) {
				targetThreshold = cfg.milestoneThresholds[req.getMilestoneId() - 1];
			}
			if (targetThreshold <= 0 || targetThreshold > MILESTONE_DENOMINATOR) {
				return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID,
						"目标里程碑阈值非法: " + targetThreshold);
			}

			long effectiveMaxHp = Math.max(1L, this.maxHp);
			long targetReduction = BigInteger.valueOf(effectiveMaxHp)
					.multiply(BigInteger.valueOf(targetThreshold))
					.add(BigInteger.valueOf(MILESTONE_DENOMINATOR - 1L))
					.divide(BigInteger.valueOf(MILESTONE_DENOMINATOR))
					.longValue();
			targetReduction = Math.min(effectiveMaxHp, Math.max(1L, targetReduction));

			this.state = ActivityState.START_VALUE;
			this.currentHp = Math.max(0L, effectiveMaxHp - targetReduction);
			this.totalDamage = Math.max(this.totalDamage, targetReduction);
			if (this.currentHp == 0L) {
				this.killed = true;
				if (this.killerPlayerId <= 0L) {
					this.killerPlayerId = effectivePlayerId;
					this.killerName = req.getPlayerName();
					this.killerServerId = req.getServerId();
					this.killTime = DateUtil.currentTimeMillis();
				}
			} else {
				// 回退至未击杀血量时同步清理旧击杀者归属与领奖标记，避免状态自相矛盾
				this.killed = false;
				this.killerPlayerId = 0L;
				this.killerName = "";
				this.killerServerId = "";
				this.killTime = 0L;
				this.killRewardClaimed = false;
			}

			long minPlayerDamage = Math.max(1L, cfg.minParticipateDamage);
			CrossBossDamageRecord rec = this.damageMap.computeIfAbsent(effectivePlayerId, idKey -> {
				CrossBossDamageRecord created = new CrossBossDamageRecord();
				created.setPlayerId(idKey);
				return created;
			});
			if (StringUtils.isNotEmpty(req.getPlayerName())) {
				rec.setPlayerName(req.getPlayerName());
			}
			if (StringUtils.isNotEmpty(req.getServerId())) {
				rec.setServerId(req.getServerId());
			}
			if (rec.getDamage() < minPlayerDamage) {
				rec.setDamage(minPlayerDamage);
			}
			rec.setUpdateTime(DateUtil.currentTimeMillis());

			// 先清空已解锁里程碑再按目标血量重算，避免保留高于目标血量阈值的高阶里程碑
			this.unlockedMilestoneIds.clear();
			unlockMilestonesByHp(cfg);
			this.rankCacheDirty = true;
			ensureRankCacheUpToDate();
			markDirty();
		} else {
			return CrossActivityActionResult.fail(ErrorMsgEnum.request_parameter_error.ID,
					"不支持的验收重置模式: " + mode);
		}
		log.info("跨服世界Boss沙盒验收重置: uid={}, scopeKey={}, playerId={}, mode={}, currentHp={}, killed={}",
				uid, scopeKey, effectivePlayerId, mode, currentHp, killed);

		AcceptanceResetResponse resp = new AcceptanceResetResponse();
		resp.setMode(mode);
		resp.setState(this.state);
		resp.setCurrentHp(this.currentHp);
		resp.setMaxHp(this.maxHp);
		resp.setKilled(this.killed);
		resp.setUnlockedMilestoneIds(new ArrayList<>(this.unlockedMilestoneIds));
		CrossBossDamageRecord myRec = this.damageMap.get(effectivePlayerId);
		resp.setMyTotalDamage(myRec != null ? myRec.getDamage() : 0L);
		return CrossActivityActionResult.success(JsonUtil.toJsonString(resp));
	}

	/**
	 * 按当前剩余血量与击杀态解锁达标里程碑（使用 BigInteger 防止大血量乘法溢出）
	 *
	 * @param cfg 跨服世界Boss静态配置
	 */
	private void unlockMilestonesByHp(ActivityCrossWorldBossConfig cfg) {
		if (cfg == null || cfg.milestoneThresholds == null) {
			return;
		}
		long effectiveMaxHp = Math.max(1L, this.maxHp);
		long reducedHp = Math.max(0L, effectiveMaxHp - this.currentHp);
		long ratioPermyriad = BigInteger.valueOf(reducedHp)
				.multiply(BigInteger.valueOf(MILESTONE_DENOMINATOR))
				.divide(BigInteger.valueOf(effectiveMaxHp))
				.longValue();

		for (int i = 0; i < cfg.milestoneThresholds.length; i++) {
			int milestoneId = i + 1;
			if ((this.killed || ratioPermyriad >= cfg.milestoneThresholds[i])
					&& !this.unlockedMilestoneIds.contains(milestoneId)) {
				this.unlockedMilestoneIds.add(milestoneId);
			}
		}
	}

	/**
	 * 确保瞬态排行榜缓存处于最新状态；处于已结算终态时直接沿用锁定的 {@code finalRankList}
	 */
	private void ensureRankCacheUpToDate() {
		ensureCollectionsInitialized();
		if (this.state == ActivityState.CLOSE_VALUE && this.rankSettled && !this.finalRankList.isEmpty()) {
			if (this.rankCacheDirty || this.sortedRankCache.isEmpty()) {
				rebuildRankCacheFromFinalList();
			}
			return;
		}
		if (!this.rankCacheDirty) {
			return;
		}
		List<CrossBossRankEntry> sorted = buildSortedRankEntries();
		this.sortedRankCache = sorted;
		this.playerRankIndexCache.clear();
		for (CrossBossRankEntry entry : sorted) {
			this.playerRankIndexCache.put(entry.getPlayerId(), entry.getRank());
		}
		this.rankCacheDirty = false;
	}

	/**
	 * 从已锁定的 {@code finalRankList} 重建瞬态排序缓存与玩家名次索引
	 */
	private void rebuildRankCacheFromFinalList() {
		this.sortedRankCache = new ArrayList<>(this.finalRankList);
		this.playerRankIndexCache.clear();
		for (CrossBossRankEntry entry : this.finalRankList) {
			this.playerRankIndexCache.put(entry.getPlayerId(), entry.getRank());
		}
		this.rankCacheDirty = false;
	}

	/**
	 * 基于当前 {@code damageMap} 构建按规则排好序的 1-based 排行榜条目列表
	 *
	 * @return 排序后的排行榜条目列表
	 */
	private List<CrossBossRankEntry> buildSortedRankEntries() {
		List<CrossBossDamageRecord> records = new ArrayList<>(this.damageMap.values());
		records.sort(DAMAGE_RANK_COMPARATOR);
		List<CrossBossRankEntry> entries = new ArrayList<>(records.size());
		for (int i = 0; i < records.size(); i++) {
			CrossBossDamageRecord rec = records.get(i);
			CrossBossRankEntry entry = new CrossBossRankEntry();
			entry.setRank(i + 1);
			entry.setPlayerId(rec.getPlayerId());
			entry.setPlayerName(rec.getPlayerName());
			entry.setServerId(rec.getServerId());
			entry.setDamage(rec.getDamage());
			entries.add(entry);
		}
		return entries;
	}

	/**
	 * 匹配名次所在的配置区间索引
	 *
	 * @param rankRanges 区间二维数组
	 * @param rank       玩家名次（1-based）
	 * @return 匹配的区间下标，未命中返回 -1
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
	 * 将扁平偶数数组 {@code [itemId, count, ...]} 逐对构造为 {@link Goods} 列表（规避 Goods.valueOf(int[][]) 缺陷）
	 *
	 * @param pairs 扁平道具数量对数组
	 * @return 构造好的奖励附件列表
	 */
	private static List<Goods> buildGoodsList(int[] pairs) {
		if (pairs == null || pairs.length < 2) {
			return new ArrayList<>();
		}
		List<Goods> goodsList = new ArrayList<>(pairs.length / 2);
		for (int j = 0; j + 1 < pairs.length; j += 2) {
			int itemId = pairs[j];
			int count = pairs[j + 1];
			if (itemId > 0 && count > 0) {
				goodsList.add(new Goods(itemId, count));
			}
		}
		return goodsList;
	}

	/**
	 * 反序列化动作 JSON 载荷；空载荷返回默认实例，格式错误时记录 warn 日志并返回 null
	 */
	private <T> T parsePayload(long playerId, String actionType, String payloadJson, Class<T> clazz) {
		if (StringUtils.isBlank(payloadJson)) {
			try {
				return clazz.getDeclaredConstructor().newInstance();
			} catch (Exception e) {
				log.warn("跨服世界Boss创建默认动作载荷失败: uid={}, playerId={}, actionType={}",
						uid, playerId, actionType, e);
				return null;
			}
		}
		try {
			return JsonUtil.parseObject(payloadJson, clazz);
		} catch (Exception e) {
			log.warn("跨服世界Boss解析动作载荷失败: uid={}, playerId={}, actionType={}",
					uid, playerId, actionType, e);
			return null;
		}
	}

	/**
	 * 判断当前是否处于允许只读查询与领奖的活动状态
	 */
	private boolean isReadableState() {
		return this.state == ActivityState.START_VALUE || this.state == ActivityState.CLOSE_VALUE;
	}

	/**
	 * 兜底初始化反序列化后可能为空的集合与瞬态缓存容器
	 */
	private void ensureCollectionsInitialized() {
		if (this.unlockedMilestoneIds == null) {
			this.unlockedMilestoneIds = new ArrayList<>();
		}
		if (this.damageMap == null) {
			this.damageMap = new HashMap<>();
		}
		if (this.finalRankList == null) {
			this.finalRankList = new ArrayList<>();
		}
		if (this.sortedRankCache == null) {
			this.sortedRankCache = new ArrayList<>();
		}
		if (this.playerRankIndexCache == null) {
			this.playerRankIndexCache = new HashMap<>();
		}
	}

	public long getMaxHp() {
		return maxHp;
	}

	public void setMaxHp(long maxHp) {
		this.maxHp = maxHp;
	}

	public long getCurrentHp() {
		return currentHp;
	}

	public void setCurrentHp(long currentHp) {
		this.currentHp = currentHp;
	}

	public long getTotalDamage() {
		return totalDamage;
	}

	public void setTotalDamage(long totalDamage) {
		this.totalDamage = totalDamage;
	}

	public boolean isKilled() {
		return killed;
	}

	public void setKilled(boolean killed) {
		this.killed = killed;
	}

	public long getKillerPlayerId() {
		return killerPlayerId;
	}

	public void setKillerPlayerId(long killerPlayerId) {
		this.killerPlayerId = killerPlayerId;
	}

	public String getKillerName() {
		return killerName;
	}

	public void setKillerName(String killerName) {
		this.killerName = killerName != null ? killerName : "";
	}

	public String getKillerServerId() {
		return killerServerId;
	}

	public void setKillerServerId(String killerServerId) {
		this.killerServerId = killerServerId != null ? killerServerId : "";
	}

	public long getKillTime() {
		return killTime;
	}

	public void setKillTime(long killTime) {
		this.killTime = killTime;
	}

	public boolean isKillRewardClaimed() {
		return killRewardClaimed;
	}

	public void setKillRewardClaimed(boolean killRewardClaimed) {
		this.killRewardClaimed = killRewardClaimed;
	}

	public List<Integer> getUnlockedMilestoneIds() {
		return unlockedMilestoneIds;
	}

	public void setUnlockedMilestoneIds(List<Integer> unlockedMilestoneIds) {
		this.unlockedMilestoneIds = unlockedMilestoneIds != null ? unlockedMilestoneIds : new ArrayList<>();
	}

	public Map<Long, CrossBossDamageRecord> getDamageMap() {
		return damageMap;
	}

	public void setDamageMap(Map<Long, CrossBossDamageRecord> damageMap) {
		this.damageMap = damageMap != null ? damageMap : new HashMap<>();
		this.rankCacheDirty = true;
	}

	public List<CrossBossRankEntry> getFinalRankList() {
		return finalRankList;
	}

	public void setFinalRankList(List<CrossBossRankEntry> finalRankList) {
		this.finalRankList = finalRankList != null ? finalRankList : new ArrayList<>();
	}

	public boolean isRankSettled() {
		return rankSettled;
	}

	public void setRankSettled(boolean rankSettled) {
		this.rankSettled = rankSettled;
	}

	/**
	 * 获取当前参与造成伤害的玩家总数
	 *
	 * @return 参与玩家数
	 */
	@JsonIgnore
	public int getParticipantCount() {
		return this.damageMap == null ? 0 : this.damageMap.size();
	}

	/**
	 * 玩家累计伤害档案（non-final 嵌入式 POJO，兼容 Jackson 多态类型序列化）
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class CrossBossDamageRecord {
		/** 玩家 ID */
		private long playerId;
		/** 玩家昵称 */
		private String playerName = "";
		/** 玩家所属逻辑区服 ID */
		private String serverId = "";
		/** 本期累计伤害 */
		private long damage;
		/** 最近一次有效增伤时间戳（毫秒，同分排序基准） */
		private long updateTime;
		/** 历史单次最高伤害记录 */
		private long maxSingleHit;

		/**
		 * 默认无参构造
		 */
		public CrossBossDamageRecord() {
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
			this.playerName = playerName != null ? playerName : "";
		}

		public String getServerId() {
			return serverId;
		}

		public void setServerId(String serverId) {
			this.serverId = serverId != null ? serverId : "";
		}

		public long getDamage() {
			return damage;
		}

		public void setDamage(long damage) {
			this.damage = damage;
		}

		public long getUpdateTime() {
			return updateTime;
		}

		public void setUpdateTime(long updateTime) {
			this.updateTime = updateTime;
		}

		public long getMaxSingleHit() {
			return maxSingleHit;
		}

		public void setMaxSingleHit(long maxSingleHit) {
			this.maxSingleHit = maxSingleHit;
		}
	}

	/**
	 * 跨服世界Boss排行榜条目（non-final 嵌入式 POJO，兼容 Jackson 多态类型序列化）
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class CrossBossRankEntry {
		/** 名次（从 1 开始） */
		private int rank;
		/** 玩家 ID */
		private long playerId;
		/** 玩家昵称 */
		private String playerName = "";
		/** 玩家所属逻辑区服 ID */
		private String serverId = "";
		/** 本期累计伤害 */
		private long damage;

		/**
		 * 默认无参构造
		 */
		public CrossBossRankEntry() {
		}

		public int getRank() {
			return rank;
		}

		public void setRank(int rank) {
			this.rank = rank;
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
			this.playerName = playerName != null ? playerName : "";
		}

		public String getServerId() {
			return serverId;
		}

		public void setServerId(String serverId) {
			this.serverId = serverId != null ? serverId : "";
		}

		public long getDamage() {
			return damage;
		}

		public void setDamage(long damage) {
			this.damage = damage;
		}
	}
}
