package cn.game.games.net.cross.activity.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 跨服宗门争霸跨服邮箱动作请求与响应 JSON DTO 集合。
 * <p>
 * 所有载荷类显式声明为 {@code non-final} 并保留无参构造，兼容 Jackson 序列化与跨进程透传。
 * </p>
 *
 * @author shuyongqiang
 */
public final class CrossGuildWarPayloads {

	/**
	 * 私有构造，禁止实例化工具容器
	 */
	private CrossGuildWarPayloads() {
	}

	/**
	 * 据点实时状态条目载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class StrongholdPayload {
		/** 据点 ID */
		private int strongholdId;
		/** 当前霸主宗门 ID（无人占领为 0） */
		private long ownerGuildId;
		/** 当前霸主宗门名称 */
		private String ownerGuildName;
		/** 当前霸主宗门图标 */
		private int ownerGuildIcon;
		/** 当前霸主宗门区服 ID */
		private String ownerGuildServerId;
		/** 当前霸主在该据点的累计积分 */
		private long ownerScore;
		/** 本宗门/锁定宗门在该据点的累计积分 */
		private long myGuildScore;

		/** 无参构造 */
		public StrongholdPayload() {
		}

		public int getStrongholdId() {
			return strongholdId;
		}

		public void setStrongholdId(int strongholdId) {
			this.strongholdId = strongholdId;
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

		public long getMyGuildScore() {
			return myGuildScore;
		}

		public void setMyGuildScore(long myGuildScore) {
			this.myGuildScore = myGuildScore;
		}
	}

	/**
	 * 战区宗门榜条目载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class GuildRankEntryPayload {
		/** 排名（1-based，未上榜为 0） */
		private int rank;
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
		/** 宗门总战功 */
		private long totalScore;
		/** 占领据点数量 */
		private int ownedStrongholdCount;

		/** 无参构造 */
		public GuildRankEntryPayload() {
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

	/**
	 * 宗门内成员贡献榜条目载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class MemberRankEntryPayload {
		/** 宗门内贡献排名（1-based，未上榜为 0） */
		private int rank;
		/** 玩家 ID */
		private long playerId;
		/** 玩家昵称 */
		private String playerName;
		/** 所属逻辑区服 ID */
		private String serverId;
		/** 累计战功 */
		private long score;

		/** 无参构造 */
		public MemberRankEntryPayload() {
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
			this.playerName = playerName;
		}

		public String getServerId() {
			return serverId;
		}

		public void setServerId(String serverId) {
			this.serverId = serverId;
		}

		public long getScore() {
			return score;
		}

		public void setScore(long score) {
			this.score = score;
		}
	}

	/**
	 * 查询争霸详情请求载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class QueryDetailRequest {
		/** 玩家 ID */
		private long playerId;
		/** 玩家当前所属宗门 ID */
		private long currentGuildId;
		/** 玩家本地缓存的锁定宗门 ID */
		private long lockedGuildId;

		/** 无参构造 */
		public QueryDetailRequest() {
		}

		public long getPlayerId() {
			return playerId;
		}

		public void setPlayerId(long playerId) {
			this.playerId = playerId;
		}

		public long getCurrentGuildId() {
			return currentGuildId;
		}

		public void setCurrentGuildId(long currentGuildId) {
			this.currentGuildId = currentGuildId;
		}

		public long getLockedGuildId() {
			return lockedGuildId;
		}

		public void setLockedGuildId(long lockedGuildId) {
			this.lockedGuildId = lockedGuildId;
		}
	}

	/**
	 * 查询争霸详情响应载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class QueryDetailResponse {
		/** 活动状态 */
		private int state;
		/** 权威锁定参战宗门 ID */
		private long lockedGuildId;
		/** 本人累计战功 */
		private long myScore;
		/** 全部据点实时状态 */
		private List<StrongholdPayload> strongholds = new ArrayList<>();
		/** 本宗门/锁定宗门概要 */
		private GuildRankEntryPayload myGuildSummary;
		/** 已达标个人里程碑 ID 列表 */
		private List<Integer> unlockedPersonalMilestoneIds = new ArrayList<>();
		/** 已达标锁定宗门里程碑 ID 列表 */
		private List<Integer> unlockedGuildMilestoneIds = new ArrayList<>();
		/** 争霸期结束时间戳（毫秒） */
		private long endTime;
		/** 活动彻底销毁时间戳（毫秒） */
		private long destroyTime;

		/** 无参构造 */
		public QueryDetailResponse() {
		}

		public int getState() {
			return state;
		}

		public void setState(int state) {
			this.state = state;
		}

		public long getLockedGuildId() {
			return lockedGuildId;
		}

		public void setLockedGuildId(long lockedGuildId) {
			this.lockedGuildId = lockedGuildId;
		}

		public long getMyScore() {
			return myScore;
		}

		public void setMyScore(long myScore) {
			this.myScore = myScore;
		}

		public List<StrongholdPayload> getStrongholds() {
			return strongholds;
		}

		public void setStrongholds(List<StrongholdPayload> strongholds) {
			this.strongholds = strongholds;
		}

		public GuildRankEntryPayload getMyGuildSummary() {
			return myGuildSummary;
		}

		public void setMyGuildSummary(GuildRankEntryPayload myGuildSummary) {
			this.myGuildSummary = myGuildSummary;
		}

		public List<Integer> getUnlockedPersonalMilestoneIds() {
			return unlockedPersonalMilestoneIds;
		}

		public void setUnlockedPersonalMilestoneIds(List<Integer> unlockedPersonalMilestoneIds) {
			this.unlockedPersonalMilestoneIds = unlockedPersonalMilestoneIds;
		}

		public List<Integer> getUnlockedGuildMilestoneIds() {
			return unlockedGuildMilestoneIds;
		}

		public void setUnlockedGuildMilestoneIds(List<Integer> unlockedGuildMilestoneIds) {
			this.unlockedGuildMilestoneIds = unlockedGuildMilestoneIds;
		}

		public long getEndTime() {
			return endTime;
		}

		public void setEndTime(long endTime) {
			this.endTime = endTime;
		}

		public long getDestroyTime() {
			return destroyTime;
		}

		public void setDestroyTime(long destroyTime) {
			this.destroyTime = destroyTime;
		}
	}

	/**
	 * 据点出征攻打请求载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class AttackRequest {
		/** 玩家 ID */
		private long playerId;
		/** 玩家昵称 */
		private String playerName;
		/** 玩家区服 ID */
		private String serverId;
		/** 玩家当前所属宗门 ID */
		private long currentGuildId;
		/** 宗门名称（Game 侧无锁缓存透传） */
		private String guildName;
		/** 宗门图标 */
		private int guildIcon;
		/** 宗门区服 ID */
		private String guildServerId;
		/** 目标据点 ID */
		private int strongholdId;
		/** 本次上报战功 */
		private long score;

		/** 无参构造 */
		public AttackRequest() {
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

		public long getCurrentGuildId() {
			return currentGuildId;
		}

		public void setCurrentGuildId(long currentGuildId) {
			this.currentGuildId = currentGuildId;
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

		public int getStrongholdId() {
			return strongholdId;
		}

		public void setStrongholdId(int strongholdId) {
			this.strongholdId = strongholdId;
		}

		public long getScore() {
			return score;
		}

		public void setScore(long score) {
			this.score = score;
		}
	}

	/**
	 * 据点出征攻打响应载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class AttackResponse {
		/** 本次有效战功 */
		private long effectiveScore;
		/** 更新后个人累计战功 */
		private long myScore;
		/** 锁定宗门 ID */
		private long lockedGuildId;
		/** 更新后宗门总战功 */
		private long guildTotalScore;
		/** 更新后宗门战区排名 */
		private int guildRank;
		/** 攻打的据点 ID */
		private int strongholdId;
		/** 本宗门在该据点最新累计分 */
		private long guildStrongholdScore;
		/** 据点最新霸主宗门 ID */
		private long ownerGuildId;
		/** 据点最新霸主宗门名称 */
		private String ownerGuildName;
		/** 据点最新霸主积分 */
		private long ownerScore;
		/** 是否首次占领或夺权易主 */
		private boolean captured;
		/** 最新已达标个人里程碑 ID 列表 */
		private List<Integer> unlockedPersonalMilestoneIds = new ArrayList<>();
		/** 最新已达标锁定宗门里程碑 ID 列表 */
		private List<Integer> unlockedGuildMilestoneIds = new ArrayList<>();

		/** 无参构造 */
		public AttackResponse() {
		}

		public long getEffectiveScore() {
			return effectiveScore;
		}

		public void setEffectiveScore(long effectiveScore) {
			this.effectiveScore = effectiveScore;
		}

		public long getMyScore() {
			return myScore;
		}

		public void setMyScore(long myScore) {
			this.myScore = myScore;
		}

		public long getLockedGuildId() {
			return lockedGuildId;
		}

		public void setLockedGuildId(long lockedGuildId) {
			this.lockedGuildId = lockedGuildId;
		}

		public long getGuildTotalScore() {
			return guildTotalScore;
		}

		public void setGuildTotalScore(long guildTotalScore) {
			this.guildTotalScore = guildTotalScore;
		}

		public int getGuildRank() {
			return guildRank;
		}

		public void setGuildRank(int guildRank) {
			this.guildRank = guildRank;
		}

		public int getStrongholdId() {
			return strongholdId;
		}

		public void setStrongholdId(int strongholdId) {
			this.strongholdId = strongholdId;
		}

		public long getGuildStrongholdScore() {
			return guildStrongholdScore;
		}

		public void setGuildStrongholdScore(long guildStrongholdScore) {
			this.guildStrongholdScore = guildStrongholdScore;
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

		public long getOwnerScore() {
			return ownerScore;
		}

		public void setOwnerScore(long ownerScore) {
			this.ownerScore = ownerScore;
		}

		public boolean isCaptured() {
			return captured;
		}

		public void setCaptured(boolean captured) {
			this.captured = captured;
		}

		public List<Integer> getUnlockedPersonalMilestoneIds() {
			return unlockedPersonalMilestoneIds;
		}

		public void setUnlockedPersonalMilestoneIds(List<Integer> unlockedPersonalMilestoneIds) {
			this.unlockedPersonalMilestoneIds = unlockedPersonalMilestoneIds;
		}

		public List<Integer> getUnlockedGuildMilestoneIds() {
			return unlockedGuildMilestoneIds;
		}

		public void setUnlockedGuildMilestoneIds(List<Integer> unlockedGuildMilestoneIds) {
			this.unlockedGuildMilestoneIds = unlockedGuildMilestoneIds;
		}
	}

	/**
	 * 校验领取阶段奖励请求载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ClaimMilestoneRequest {
		/** 玩家 ID */
		private long playerId;
		/** 奖励类型：1=个人，2=宗门 */
		private int rewardType;
		/** 里程碑档位 ID（1-based） */
		private int milestoneId;
		/** Game 侧预期锁定宗门 ID（用于一致性校验） */
		private long expectedLockedGuildId;

		/** 无参构造 */
		public ClaimMilestoneRequest() {
		}

		public long getPlayerId() {
			return playerId;
		}

		public void setPlayerId(long playerId) {
			this.playerId = playerId;
		}

		public int getRewardType() {
			return rewardType;
		}

		public void setRewardType(int rewardType) {
			this.rewardType = rewardType;
		}

		public int getMilestoneId() {
			return milestoneId;
		}

		public void setMilestoneId(int milestoneId) {
			this.milestoneId = milestoneId;
		}

		public long getExpectedLockedGuildId() {
			return expectedLockedGuildId;
		}

		public void setExpectedLockedGuildId(long expectedLockedGuildId) {
			this.expectedLockedGuildId = expectedLockedGuildId;
		}
	}

	/**
	 * 校验领取阶段奖励响应载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ClaimMilestoneResponse {
		/** 奖励类型 */
		private int rewardType;
		/** 里程碑档位 ID */
		private int milestoneId;
		/** 权威锁定宗门 ID */
		private long lockedGuildId;

		/** 无参构造 */
		public ClaimMilestoneResponse() {
		}

		public int getRewardType() {
			return rewardType;
		}

		public void setRewardType(int rewardType) {
			this.rewardType = rewardType;
		}

		public int getMilestoneId() {
			return milestoneId;
		}

		public void setMilestoneId(int milestoneId) {
			this.milestoneId = milestoneId;
		}

		public long getLockedGuildId() {
			return lockedGuildId;
		}

		public void setLockedGuildId(long lockedGuildId) {
			this.lockedGuildId = lockedGuildId;
		}
	}

	/**
	 * 分页查询战区宗门排行榜请求载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class QueryGuildRankRequest {
		/** 玩家 ID */
		private long playerId;
		/** 玩家当前所属宗门 ID */
		private long currentGuildId;
		/** 页码（1-based） */
		private int page;
		/** 每页条数（1~100） */
		private int pageSize;

		/** 无参构造 */
		public QueryGuildRankRequest() {
		}

		public long getPlayerId() {
			return playerId;
		}

		public void setPlayerId(long playerId) {
			this.playerId = playerId;
		}

		public long getCurrentGuildId() {
			return currentGuildId;
		}

		public void setCurrentGuildId(long currentGuildId) {
			this.currentGuildId = currentGuildId;
		}

		public int getPage() {
			return page;
		}

		public void setPage(int page) {
			this.page = page;
		}

		public int getPageSize() {
			return pageSize;
		}

		public void setPageSize(int pageSize) {
			this.pageSize = pageSize;
		}
	}

	/**
	 * 分页查询战区宗门排行榜响应载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class QueryGuildRankResponse {
		/** 上榜宗门总数 */
		private int totalCount;
		/** 当前页码 */
		private int page;
		/** 每页条数 */
		private int pageSize;
		/** 分页列表 */
		private List<GuildRankEntryPayload> entries = new ArrayList<>();
		/** 本锁定/当前宗门条目 */
		private GuildRankEntryPayload myGuildEntry;

		/** 无参构造 */
		public QueryGuildRankResponse() {
		}

		public int getTotalCount() {
			return totalCount;
		}

		public void setTotalCount(int totalCount) {
			this.totalCount = totalCount;
		}

		public int getPage() {
			return page;
		}

		public void setPage(int page) {
			this.page = page;
		}

		public int getPageSize() {
			return pageSize;
		}

		public void setPageSize(int pageSize) {
			this.pageSize = pageSize;
		}

		public List<GuildRankEntryPayload> getEntries() {
			return entries;
		}

		public void setEntries(List<GuildRankEntryPayload> entries) {
			this.entries = entries;
		}

		public GuildRankEntryPayload getMyGuildEntry() {
			return myGuildEntry;
		}

		public void setMyGuildEntry(GuildRankEntryPayload myGuildEntry) {
			this.myGuildEntry = myGuildEntry;
		}
	}

	/**
	 * 分页查询宗门成员贡献榜请求载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class QueryMemberRankRequest {
		/** 玩家 ID */
		private long playerId;
		/** 目标宗门 ID */
		private long targetGuildId;
		/** 页码（1-based） */
		private int page;
		/** 每页条数（1~100） */
		private int pageSize;

		/** 无参构造 */
		public QueryMemberRankRequest() {
		}

		public long getPlayerId() {
			return playerId;
		}

		public void setPlayerId(long playerId) {
			this.playerId = playerId;
		}

		public long getTargetGuildId() {
			return targetGuildId;
		}

		public void setTargetGuildId(long targetGuildId) {
			this.targetGuildId = targetGuildId;
		}

		public int getPage() {
			return page;
		}

		public void setPage(int page) {
			this.page = page;
		}

		public int getPageSize() {
			return pageSize;
		}

		public void setPageSize(int pageSize) {
			this.pageSize = pageSize;
		}
	}

	/**
	 * 分页查询宗门成员贡献榜响应载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class QueryMemberRankResponse {
		/** 实际查询的宗门 ID */
		private long targetGuildId;
		/** 宗门内参战锁定成员总数 */
		private int totalCount;
		/** 当前页码 */
		private int page;
		/** 每页条数 */
		private int pageSize;
		/** 分页列表 */
		private List<MemberRankEntryPayload> entries = new ArrayList<>();
		/** 本人在该宗门的贡献排名条目 */
		private MemberRankEntryPayload myMemberEntry;

		/** 无参构造 */
		public QueryMemberRankResponse() {
		}

		public long getTargetGuildId() {
			return targetGuildId;
		}

		public void setTargetGuildId(long targetGuildId) {
			this.targetGuildId = targetGuildId;
		}

		public int getTotalCount() {
			return totalCount;
		}

		public void setTotalCount(int totalCount) {
			this.totalCount = totalCount;
		}

		public int getPage() {
			return page;
		}

		public void setPage(int page) {
			this.page = page;
		}

		public int getPageSize() {
			return pageSize;
		}

		public void setPageSize(int pageSize) {
			this.pageSize = pageSize;
		}

		public List<MemberRankEntryPayload> getEntries() {
			return entries;
		}

		public void setEntries(List<MemberRankEntryPayload> entries) {
			this.entries = entries;
		}

		public MemberRankEntryPayload getMyMemberEntry() {
			return myMemberEntry;
		}

		public void setMyMemberEntry(MemberRankEntryPayload myMemberEntry) {
			this.myMemberEntry = myMemberEntry;
		}
	}

	/**
	 * 非生产环境受控验收重置请求载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class AcceptanceResetRequest {
		/** 重置模式（reset_sandbox_war / clear_player / set_scores_for_test） */
		private String mode;
		/** 目标沙盒玩家 ID */
		private long targetPlayerId;
		/** 目标宗门 ID */
		private long guildId;
		/** 目标宗门名称 */
		private String guildName;
		/** 目标据点 ID */
		private int strongholdId;
		/** 设定战功值 */
		private long score;

		/** 无参构造 */
		public AcceptanceResetRequest() {
		}

		public String getMode() {
			return mode;
		}

		public void setMode(String mode) {
			this.mode = mode;
		}

		public long getTargetPlayerId() {
			return targetPlayerId;
		}

		public void setTargetPlayerId(long targetPlayerId) {
			this.targetPlayerId = targetPlayerId;
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

		public int getStrongholdId() {
			return strongholdId;
		}

		public void setStrongholdId(int strongholdId) {
			this.strongholdId = strongholdId;
		}

		public long getScore() {
			return score;
		}

		public void setScore(long score) {
			this.score = score;
		}
	}

	/**
	 * 非生产环境受控验收重置响应载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class AcceptanceResetResponse {
		/** 执行的重置模式 */
		private String mode;
		/** 重置后活动状态 */
		private int state;
		/** 当前据点数量 */
		private int strongholdCount;
		/** 当前参战宗门数量 */
		private int guildCount;
		/** 当前参战成员数量 */
		private int participantCount;

		/** 无参构造 */
		public AcceptanceResetResponse() {
		}

		public String getMode() {
			return mode;
		}

		public void setMode(String mode) {
			this.mode = mode;
		}

		public int getState() {
			return state;
		}

		public void setState(int state) {
			this.state = state;
		}

		public int getStrongholdCount() {
			return strongholdCount;
		}

		public void setStrongholdCount(int strongholdCount) {
			this.strongholdCount = strongholdCount;
		}

		public int getGuildCount() {
			return guildCount;
		}

		public void setGuildCount(int guildCount) {
			this.guildCount = guildCount;
		}

		public int getParticipantCount() {
			return participantCount;
		}

		public void setParticipantCount(int participantCount) {
			this.participantCount = participantCount;
		}
	}
}
