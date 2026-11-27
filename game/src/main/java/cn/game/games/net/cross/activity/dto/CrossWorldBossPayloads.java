package cn.game.games.net.cross.activity.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 跨服世界Boss动作通信 JSON DTO 集合。
 * <p>
 * 职责边界：定义 Game 与 Cross 之间经 {@code executePlayerAction} 传递的 6 类动作请求与响应结构；
 * 所有内部类均为 {@code non-final} 且保留无参构造，兼容 Jackson 默认类型与容错反序列化，严禁使用 {@code record}。
 * </p>
 *
 * @author shuyongqiang
 */
public final class CrossWorldBossPayloads {

	/**
	 * 私有构造，禁止实例化静态 DTO 容器
	 */
	private CrossWorldBossPayloads() {
	}

	/**
	 * 排行榜条目载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class RankEntryPayload {
		/** 排名（从 1 开始） */
		private int rank;
		/** 玩家 ID */
		private long playerId;
		/** 玩家昵称 */
		private String playerName = "";
		/** 所属逻辑区服 ID */
		private String serverId = "";
		/** 本期累计伤害 */
		private long damage;

		/**
		 * 默认无参构造
		 */
		public RankEntryPayload() {
		}

		/**
		 * 全参构造
		 *
		 * @param rank       排名
		 * @param playerId   玩家 ID
		 * @param playerName 玩家昵称
		 * @param serverId   逻辑区服 ID
		 * @param damage     累计伤害
		 */
		public RankEntryPayload(int rank, long playerId, String playerName, String serverId, long damage) {
			this.rank = rank;
			this.playerId = playerId;
			this.playerName = playerName != null ? playerName : "";
			this.serverId = serverId != null ? serverId : "";
			this.damage = damage;
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

	/**
	 * 查询详情请求载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class QueryDetailRequest {
		/** 玩家 ID */
		private long playerId;
		/** 玩家昵称 */
		private String playerName = "";
		/** 所属逻辑区服 ID */
		private String serverId = "";

		/**
		 * 默认无参构造
		 */
		public QueryDetailRequest() {
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
	}

	/**
	 * 查询详情响应载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class QueryDetailResponse {
		/** Cross 权威活动状态 */
		private int state;
		/** Boss 最大血量 */
		private long maxHp;
		/** Boss 当前剩余血量 */
		private long currentHp;
		/** 战区累计造成伤害 */
		private long totalDamage;
		/** 是否已被击杀 */
		private boolean killed;
		/** 最后一击击杀者 ID */
		private long killerPlayerId;
		/** 最后一击击杀者昵称 */
		private String killerName = "";
		/** 击杀者所属逻辑区服 ID */
		private String killerServerId = "";
		/** 击杀时间戳（毫秒） */
		private long killTime;
		/** 最后一击击杀奖是否已领取或已确认发放 */
		private boolean killRewardClaimed;
		/** 战区已解锁里程碑 ID 列表 */
		private List<Integer> unlockedMilestoneIds = new ArrayList<>();
		/** 本人本期累计伤害 */
		private long myTotalDamage;
		/** 本人当前排名（未上榜为 0） */
		private int myRank;
		/** 本战区参与人数 */
		private int participantCount;

		/**
		 * 默认无参构造
		 */
		public QueryDetailResponse() {
		}

		public int getState() {
			return state;
		}

		public void setState(int state) {
			this.state = state;
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

		public long getMyTotalDamage() {
			return myTotalDamage;
		}

		public void setMyTotalDamage(long myTotalDamage) {
			this.myTotalDamage = myTotalDamage;
		}

		public int getMyRank() {
			return myRank;
		}

		public void setMyRank(int myRank) {
			this.myRank = myRank;
		}

		public int getParticipantCount() {
			return participantCount;
		}

		public void setParticipantCount(int participantCount) {
			this.participantCount = participantCount;
		}
	}

	/**
	 * 攻打 Boss 请求载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class AttackRequest {
		/** 玩家 ID */
		private long playerId;
		/** 玩家昵称 */
		private String playerName = "";
		/** 所属逻辑区服 ID */
		private String serverId = "";
		/** 本次上报伤害 */
		private long damage;

		/**
		 * 默认无参构造
		 */
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

	/**
	 * 攻打 Boss 响应载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class AttackResponse {
		/** 实际有效扣血量 */
		private long actualDamage;
		/** 攻打后 Boss 剩余血量 */
		private long remainingHp;
		/** Boss 最大血量 */
		private long maxHp;
		/** 是否已被击杀 */
		private boolean killed;
		/** 本次是否为唯一最后一击 */
		private boolean isKillShot;
		/** 击杀者 ID */
		private long killerPlayerId;
		/** 击杀者昵称 */
		private String killerName = "";
		/** 击杀者所属区服 ID */
		private String killerServerId = "";
		/** 击杀时间戳（毫秒） */
		private long killTime;
		/** 本人更新后累计伤害 */
		private long myTotalDamage;
		/** 本人更新后排名 */
		private int myRank;
		/** 战区最新已解锁里程碑 ID 列表 */
		private List<Integer> unlockedMilestoneIds = new ArrayList<>();

		/**
		 * 默认无参构造
		 */
		public AttackResponse() {
		}

		public long getActualDamage() {
			return actualDamage;
		}

		public void setActualDamage(long actualDamage) {
			this.actualDamage = actualDamage;
		}

		public long getRemainingHp() {
			return remainingHp;
		}

		public void setRemainingHp(long remainingHp) {
			this.remainingHp = remainingHp;
		}

		public long getMaxHp() {
			return maxHp;
		}

		public void setMaxHp(long maxHp) {
			this.maxHp = maxHp;
		}

		public boolean isKilled() {
			return killed;
		}

		public void setKilled(boolean killed) {
			this.killed = killed;
		}

		public boolean isKillShot() {
			return isKillShot;
		}

		public void setKillShot(boolean killShot) {
			this.isKillShot = killShot;
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

		public long getMyTotalDamage() {
			return myTotalDamage;
		}

		public void setMyTotalDamage(long myTotalDamage) {
			this.myTotalDamage = myTotalDamage;
		}

		public int getMyRank() {
			return myRank;
		}

		public void setMyRank(int myRank) {
			this.myRank = myRank;
		}

		public List<Integer> getUnlockedMilestoneIds() {
			return unlockedMilestoneIds;
		}

		public void setUnlockedMilestoneIds(List<Integer> unlockedMilestoneIds) {
			this.unlockedMilestoneIds = unlockedMilestoneIds != null ? unlockedMilestoneIds : new ArrayList<>();
		}
	}

	/**
	 * 领取里程碑奖励校验请求载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ClaimMilestoneRequest {
		/** 玩家 ID */
		private long playerId;
		/** 里程碑 ID（从 1 开始） */
		private int milestoneId;

		/**
		 * 默认无参构造
		 */
		public ClaimMilestoneRequest() {
		}

		public long getPlayerId() {
			return playerId;
		}

		public void setPlayerId(long playerId) {
			this.playerId = playerId;
		}

		public int getMilestoneId() {
			return milestoneId;
		}

		public void setMilestoneId(int milestoneId) {
			this.milestoneId = milestoneId;
		}
	}

	/**
	 * 领取里程碑奖励校验响应载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ClaimMilestoneResponse {
		/** 校验通过的里程碑 ID */
		private int milestoneId;
		/** 玩家当前累计伤害 */
		private long myTotalDamage;
		/** 战区当前已解锁里程碑 ID 列表 */
		private List<Integer> unlockedMilestoneIds = new ArrayList<>();

		/**
		 * 默认无参构造
		 */
		public ClaimMilestoneResponse() {
		}

		public int getMilestoneId() {
			return milestoneId;
		}

		public void setMilestoneId(int milestoneId) {
			this.milestoneId = milestoneId;
		}

		public long getMyTotalDamage() {
			return myTotalDamage;
		}

		public void setMyTotalDamage(long myTotalDamage) {
			this.myTotalDamage = myTotalDamage;
		}

		public List<Integer> getUnlockedMilestoneIds() {
			return unlockedMilestoneIds;
		}

		public void setUnlockedMilestoneIds(List<Integer> unlockedMilestoneIds) {
			this.unlockedMilestoneIds = unlockedMilestoneIds != null ? unlockedMilestoneIds : new ArrayList<>();
		}
	}

	/**
	 * 确认或补领最后一击击杀奖励请求载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ClaimKillRewardRequest {
		/** 玩家 ID */
		private long playerId;
		/** true=Game当场直发后的幂等确认，false=断线重连主动补领 */
		private boolean confirmOnly;

		/**
		 * 默认无参构造
		 */
		public ClaimKillRewardRequest() {
		}

		public long getPlayerId() {
			return playerId;
		}

		public void setPlayerId(long playerId) {
			this.playerId = playerId;
		}

		public boolean isConfirmOnly() {
			return confirmOnly;
		}

		public void setConfirmOnly(boolean confirmOnly) {
			this.confirmOnly = confirmOnly;
		}
	}

	/**
	 * 确认或补领最后一击击杀奖励响应载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ClaimKillRewardResponse {
		/** 最新击杀奖励领取状态 */
		private boolean killRewardClaimed;
		/** 击杀者玩家 ID */
		private long killerPlayerId;

		/**
		 * 默认无参构造
		 */
		public ClaimKillRewardResponse() {
		}

		public boolean isKillRewardClaimed() {
			return killRewardClaimed;
		}

		public void setKillRewardClaimed(boolean killRewardClaimed) {
			this.killRewardClaimed = killRewardClaimed;
		}

		public long getKillerPlayerId() {
			return killerPlayerId;
		}

		public void setKillerPlayerId(long killerPlayerId) {
			this.killerPlayerId = killerPlayerId;
		}
	}

	/**
	 * 分页查询伤害排行榜请求载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class QueryRankRequest {
		/** 玩家 ID */
		private long playerId;
		/** 页码（从 1 开始） */
		private int page;
		/** 每页条数（1 ~ 100） */
		private int pageSize;

		/**
		 * 默认无参构造
		 */
		public QueryRankRequest() {
		}

		public long getPlayerId() {
			return playerId;
		}

		public void setPlayerId(long playerId) {
			this.playerId = playerId;
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
	 * 分页查询伤害排行榜响应载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class QueryRankResponse {
		/** 上榜总人数 */
		private int totalCount;
		/** 当前页码 */
		private int page;
		/** 每页条数 */
		private int pageSize;
		/** 本页排行条目列表 */
		private List<RankEntryPayload> entries = new ArrayList<>();
		/** 本人排名（未上榜为 0） */
		private int myRank;
		/** 本人累计伤害 */
		private long myDamage;

		/**
		 * 默认无参构造
		 */
		public QueryRankResponse() {
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

		public List<RankEntryPayload> getEntries() {
			return entries;
		}

		public void setEntries(List<RankEntryPayload> entries) {
			this.entries = entries != null ? entries : new ArrayList<>();
		}

		public int getMyRank() {
			return myRank;
		}

		public void setMyRank(int myRank) {
			this.myRank = myRank;
		}

		public long getMyDamage() {
			return myDamage;
		}

		public void setMyDamage(long myDamage) {
			this.myDamage = myDamage;
		}
	}

	/**
	 * 非生产环境受控验收重置请求载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class AcceptanceResetRequest {
		/** 沙盒玩家 ID */
		private long playerId;
		/** 沙盒玩家昵称 */
		private String playerName = "";
		/** 沙盒玩家逻辑服 ID */
		private String serverId = "";
		/** 重置模式：reset_sandbox_boss / clear_player / set_damage_for_milestone */
		private String mode = "";
		/** 目标里程碑 ID（set_damage_for_milestone 使用，1-based） */
		private int milestoneId;
		/** 目标血量削减万分比（若大于 0 则优先于 milestoneId） */
		private int targetThreshold;

		/**
		 * 默认无参构造
		 */
		public AcceptanceResetRequest() {
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

		public String getMode() {
			return mode;
		}

		public void setMode(String mode) {
			this.mode = mode != null ? mode : "";
		}

		public int getMilestoneId() {
			return milestoneId;
		}

		public void setMilestoneId(int milestoneId) {
			this.milestoneId = milestoneId;
		}

		public int getTargetThreshold() {
			return targetThreshold;
		}

		public void setTargetThreshold(int targetThreshold) {
			this.targetThreshold = targetThreshold;
		}
	}

	/**
	 * 非生产环境受控验收重置响应载荷
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class AcceptanceResetResponse {
		/** 执行模式 */
		private String mode = "";
		/** 重置后活动状态 */
		private int state;
		/** 重置后 Boss 当前血量 */
		private long currentHp;
		/** Boss 最大血量 */
		private long maxHp;
		/** 是否处于击杀态 */
		private boolean killed;
		/** 已解锁里程碑 ID 列表 */
		private List<Integer> unlockedMilestoneIds = new ArrayList<>();
		/** 目标玩家当前累计伤害 */
		private long myTotalDamage;

		/**
		 * 默认无参构造
		 */
		public AcceptanceResetResponse() {
		}

		public String getMode() {
			return mode;
		}

		public void setMode(String mode) {
			this.mode = mode != null ? mode : "";
		}

		public int getState() {
			return state;
		}

		public void setState(int state) {
			this.state = state;
		}

		public long getCurrentHp() {
			return currentHp;
		}

		public void setCurrentHp(long currentHp) {
			this.currentHp = currentHp;
		}

		public long getMaxHp() {
			return maxHp;
		}

		public void setMaxHp(long maxHp) {
			this.maxHp = maxHp;
		}

		public boolean isKilled() {
			return killed;
		}

		public void setKilled(boolean killed) {
			this.killed = killed;
		}

		public List<Integer> getUnlockedMilestoneIds() {
			return unlockedMilestoneIds;
		}

		public void setUnlockedMilestoneIds(List<Integer> unlockedMilestoneIds) {
			this.unlockedMilestoneIds = unlockedMilestoneIds != null ? unlockedMilestoneIds : new ArrayList<>();
		}

		public long getMyTotalDamage() {
			return myTotalDamage;
		}

		public void setMyTotalDamage(long myTotalDamage) {
			this.myTotalDamage = myTotalDamage;
		}
	}
}
