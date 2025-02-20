package cn.game.games.net.game.module.union;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson2.JSON;

public class UnionService {
	private final RedissonClient redisson;
	private static final String UNION_INFO_KEY = "union:info:";
	private static final String UNION_COUNTER_KEY = "union:counter:";
	private static final String UNION_MEMBER_KEY = "union:member:";
	private static final String UNION_LOG_KEY = "union:log:";
	private static final String UNION_LOCK_KEY = "union:lock:";

	@Autowired
	public UnionService(RedissonClient redisson) {
		this.redisson = redisson;
	}

	// 创建工会
	public boolean createUnion(String unionId, UnionInfo info) {
		RLock lock = redisson.getLock(UNION_LOCK_KEY + unionId);
		try {
			if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
				try {
					// 检查工会是否已存在
					if (redisson.getBucket(UNION_INFO_KEY + unionId).isExists()) {
						return false;
					}

					// 保存基本信息
					redisson.getBucket(UNION_INFO_KEY + unionId).set(JSON.toJSONString(info));

					// 初始化计数器
					RMap<String, Object> counterMap = redisson.getMap(UNION_COUNTER_KEY + unionId);
					counterMap.put("activity", 0);
					counterMap.put("resources", 0);
					counterMap.put("memberCount", 1);

					// 添加会长为成员
					UnionMember leader = new UnionMember();
					leader.setMemberId(info.getLeaderId());
					leader.setPosition(1);
					leader.setJoinTime(System.currentTimeMillis());
					addMember(unionId, leader);

					// 记录创建日志
					addLog(unionId, createUnionLog(unionId, info.getLeaderId(), "创建工会"));

					return true;
				} finally {
					lock.unlock();
				}
			}
			return false;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Failed to acquire lock", e);
		}
	}

	// 更新工会基本信息
	public boolean updateUnionInfo(String unionId, UnionInfo updateInfo) {
		RLock lock = redisson.getLock(UNION_LOCK_KEY + unionId);
		try {
			if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
				try {
					UnionInfo currentInfo = getUnionInfo(unionId);
					if (currentInfo == null) {
						return false;
					}

					// 更新基本信息
					if (updateInfo.getName() != null) {
						currentInfo.setName(updateInfo.getName());
					}
					if (updateInfo.getNotice() != null) {
						currentInfo.setNotice(updateInfo.getNotice());
					}
					// ... 其他字段更新

					redisson.getBucket(UNION_INFO_KEY + unionId).set(JSON.toJSONString(currentInfo));

					// 记录更新日志
//					addLog(unionId, createUpdateLog(unionId, updateInfo));

					return true;
				} finally {
					lock.unlock();
				}
			}
			return false;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Failed to acquire lock", e);
		}
	}

	// 成员管理相关方法
	public boolean addMember(String unionId, UnionMember member) {
		RLock lock = redisson.getLock(UNION_LOCK_KEY + unionId);
		try {
			if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
				try {
					RMap<String, String> memberMap = redisson.getMap(UNION_MEMBER_KEY + unionId);

					// 检查是否已是成员
					if (memberMap.containsKey(member.getMemberId())) {
						return false;
					}

					// 检查成员数量限制
					RMap<String, Object> counterMap = redisson.getMap(UNION_COUNTER_KEY + unionId);
					long memberCount = Long.parseLong(counterMap.get("memberCount").toString());
					if (memberCount >= getMaxMemberCount(unionId)) {
						return false;
					}

					// 添加成员
					memberMap.put(member.getMemberId(), JSON.toJSONString(member));
					counterMap.addAndGet("memberCount", 1);

					// 记录日志
					addLog(unionId, createMemberLog(unionId, member.getMemberId(), "加入工会"));

					return true;
				} finally {
					lock.unlock();
				}
			}
			return false;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Failed to acquire lock", e);
		}
	}

	public boolean removeMember(String unionId, String memberId, String operatorId) {
		RLock lock = redisson.getLock(UNION_LOCK_KEY + unionId);
		try {
			if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
				try {
					RMap<String, String> memberMap = redisson.getMap(UNION_MEMBER_KEY + unionId);

					// 检查成员是否存在
					String memberJson = memberMap.get(memberId);
					if (memberJson == null) {
						return false;
					}

					UnionMember member = JSON.parseObject(memberJson, UnionMember.class);

					// 检查权限
					if (!checkRemovePermission(unionId, operatorId, member)) {
						return false;
					}

					// 移除成员
					memberMap.remove(memberId);
					redisson.getMap(UNION_COUNTER_KEY + unionId).addAndGet("memberCount", -1);

					// 记录日志
					addLog(unionId, createMemberLog(unionId, memberId, "离开工会"));

					return true;
				} finally {
					lock.unlock();
				}
			}
			return false;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Failed to acquire lock", e);
		}
	}

	// 计数器操作
//	public long incrementActivity(String unionId, long delta) {
//		return redisson.getMap(UNION_COUNTER_KEY + unionId).addAndGet("activity", delta);
//	}

//	public long addResource(String unionId, long amount) {
//		return redisson.getMap(UNION_COUNTER_KEY + unionId).addAndGet("resources", amount);
//	}

	public boolean deductResource(String unionId, long amount) {
		RLock lock = redisson.getLock(UNION_LOCK_KEY + unionId);
		try {
			if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
				try {
					RMap<String, Object> counterMap = redisson.getMap(UNION_COUNTER_KEY + unionId);
					long currentResource = Long.parseLong(counterMap.get("resources").toString());

					if (currentResource >= amount) {
						counterMap.put("resources", currentResource - amount);

						// 记录资源变动日志
						addLog(unionId, createResourceLog(unionId, "resources", -amount));

						return true;
					}
					return false;
				} finally {
					lock.unlock();
				}
			}
			return false;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Failed to acquire lock", e);
		}
	}

	// 日志管理
	public void addLog(String unionId, UnionLog log) {
		RList<String> logList = redisson.getList(UNION_LOG_KEY + unionId);
		logList.add(JSON.toJSONString(log));

		// 保持最近1000条日志
		if (logList.size() > 1000) {
			logList.remove(0);
		}
	}

	public List<UnionLog> getRecentLogs(String unionId, int limit) {
		RList<String> logList = redisson.getList(UNION_LOG_KEY + unionId);
		List<String> recentLogs = logList.readAll();

		// 返回最近的日志
		return recentLogs.stream()
				.map(log -> JSON.parseObject(log, UnionLog.class))
				.sorted((a, b) -> Long.compare(b.getCreateTime(), a.getCreateTime()))
				.limit(limit)
				.collect(Collectors.toList());
	}

	// 查询完整信息
//	public CompletableFuture<UnionFullInfo> getUnionFullInfoAsync(String unionId) {
//		CompletableFuture<UnionInfo> basicInfoFuture = CompletableFuture.supplyAsync(() -> getUnionInfo(unionId));
//
//		CompletableFuture<Map<String, Object>> countersFuture = CompletableFuture
//				.supplyAsync(() -> redisson.getMap(UNION_COUNTER_KEY + unionId).readAllMap());
//
//		CompletableFuture<List<UnionMember>> membersFuture = CompletableFuture.supplyAsync(() -> getMembers(unionId));
//
//		CompletableFuture<List<UnionLog>> logsFuture = CompletableFuture.supplyAsync(() -> getRecentLogs(unionId, 50));
//
//		return CompletableFuture.allOf(basicInfoFuture, countersFuture, membersFuture, logsFuture).thenApply(v -> {
//			UnionFullInfo fullInfo = new UnionFullInfo();
//			fullInfo.setBasicInfo(basicInfoFuture.join());
//			fullInfo.setCounters(countersFuture.join());
//			fullInfo.setMembers(membersFuture.join());
//			fullInfo.setRecentLogs(logsFuture.join());
//			return fullInfo;
//		});
//	}

	// 工具方法
	private UnionInfo getUnionInfo(String unionId) {
		String json = (String) redisson.getBucket(UNION_INFO_KEY + unionId).get();
		return json != null ? JSON.parseObject(json, UnionInfo.class) : null;
	}

	private List<UnionMember> getMembers(String unionId) {
		RMap<String, String> memberMap = redisson.getMap(UNION_MEMBER_KEY + unionId);
		return memberMap.readAllValues().stream().map(json -> JSON.parseObject(json, UnionMember.class)).collect(Collectors.toList());
	}

	private int getMaxMemberCount(String unionId) {
		UnionInfo info = getUnionInfo(unionId);
		return info.getLevel() * 10 + 50; // 示例：基础50人，每级增加10人
	}

	private boolean checkRemovePermission(String unionId, String operatorId, UnionMember targetMember) {
		UnionMember operator = JSON.parseObject((String) redisson.getMap(UNION_MEMBER_KEY + unionId).get(operatorId), UnionMember.class);

		if (operator == null) {
			return false;
		}

		// 会长可以删除任何人
		if (operator.getPosition() == 1) {
			return true;
		}

		// 副会长可以删除普通成员
		return operator.getPosition() == 2 && targetMember.getPosition() == 3;
	}

	private UnionLog createUnionLog(String unionId, String operatorId, String content) {
		UnionLog log = new UnionLog();
		log.setLogId(UUID.randomUUID().toString());
		log.setUnionId(unionId);
		log.setOperatorId(operatorId);
		log.setContent(content);
		log.setCreateTime(System.currentTimeMillis());
		return log;
	}

	private UnionLog createMemberLog(String unionId, String memberId, String action) {
		UnionLog log = createUnionLog(unionId, memberId, action);
		log.setLogType(1);
		return log;
	}

	private UnionLog createResourceLog(String unionId, String resourceType, long amount) {
		UnionLog log = createUnionLog(unionId, "SYSTEM", String.format("%s变动：%d", resourceType, amount));
		log.setLogType(2);
		Map<String, Object> extra = new HashMap<>();
		extra.put("resourceType", resourceType);
		extra.put("amount", amount);
		log.setExtraInfo(extra);
		return log;
	}

	// 工会基本信息
	public static class UnionInfo {
		// 基本不变的一些基础字段
		private long unionId;
		private String name;
		private int level;
		private String leaderId; // 会长ID
		private String notice; // 公告
		private long createTime;
		// 可能频繁修改的字段，需要单独更新的
		private Map<String, Object> extraProperties; // 扩展字段

		public long getUnionId() {
			return unionId;
		}

		public void setUnionId(long unionId) {
			this.unionId = unionId;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getLevel() {
			return level;
		}

		public void setLevel(int level) {
			this.level = level;
		}

		public String getLeaderId() {
			return leaderId;
		}

		public void setLeaderId(String leaderId) {
			this.leaderId = leaderId;
		}

		public String getNotice() {
			return notice;
		}

		public void setNotice(String notice) {
			this.notice = notice;
		}

		public long getCreateTime() {
			return createTime;
		}

		public void setCreateTime(long createTime) {
			this.createTime = createTime;
		}

		public Map<String, Object> getExtraProperties() {
			return extraProperties;
		}

		public void setExtraProperties(Map<String, Object> extraProperties) {
			this.extraProperties = extraProperties;
		}


		// getters, setters, toString...
	}

	// 工会成员信息
	public static class UnionMember {
		private String memberId;
		private String name;
		private int position; // 职位：1-会长，2-副会长，3-普通成员
		private long joinTime;
		private long lastActiveTime;

		public String getMemberId() {
			return memberId;
		}

		public void setMemberId(String memberId) {
			this.memberId = memberId;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getPosition() {
			return position;
		}

		public void setPosition(int position) {
			this.position = position;
		}

		public long getJoinTime() {
			return joinTime;
		}

		public void setJoinTime(long joinTime) {
			this.joinTime = joinTime;
		}

		public long getLastActiveTime() {
			return lastActiveTime;
		}

		public void setLastActiveTime(long lastActiveTime) {
			this.lastActiveTime = lastActiveTime;
		}


		// getters, setters, toString...
	}

	// 工会完整信息
	public static class UnionFullInfo {
		private UnionInfo basicInfo;
		private Map<String, Object> counters; // 计数器数据
		private List<UnionMember> members; // 成员列表
		private List<UnionLog> recentLogs; // 最近日志

		public UnionInfo getBasicInfo() {
			return basicInfo;
		}

		public void setBasicInfo(UnionInfo basicInfo) {
			this.basicInfo = basicInfo;
		}

		public Map<String, Object> getCounters() {
			return counters;
		}

		public void setCounters(Map<String, Object> counters) {
			this.counters = counters;
		}

		public List<UnionMember> getMembers() {
			return members;
		}

		public void setMembers(List<UnionMember> members) {
			this.members = members;
		}

		public List<UnionLog> getRecentLogs() {
			return recentLogs;
		}

		public void setRecentLogs(List<UnionLog> recentLogs) {
			this.recentLogs = recentLogs;
		}

		// getters, setters, toString...
	}

	// 工会日志
	public static class UnionLog {
		private String logId;
		private String unionId;
		private int logType; // 日志类型：1-成员变动，2-资源变动，3-等级变动...
		private String operatorId;// 操作者ID
		private String content; // 日志内容
		private long createTime;
		private Map<String, Object> extraInfo; // 额外信息

		public String getLogId() {
			return logId;
		}

		public void setLogId(String logId) {
			this.logId = logId;
		}

		public String getUnionId() {
			return unionId;
		}

		public void setUnionId(String unionId) {
			this.unionId = unionId;
		}

		public int getLogType() {
			return logType;
		}

		public void setLogType(int logType) {
			this.logType = logType;
		}

		public String getOperatorId() {
			return operatorId;
		}

		public void setOperatorId(String operatorId) {
			this.operatorId = operatorId;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public long getCreateTime() {
			return createTime;
		}

		public void setCreateTime(long createTime) {
			this.createTime = createTime;
		}

		public Map<String, Object> getExtraInfo() {
			return extraInfo;
		}

		public void setExtraInfo(Map<String, Object> extraInfo) {
			this.extraInfo = extraInfo;
		}

	}
}