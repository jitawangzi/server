package cn.game.games.net.cross.activity;

import java.util.Optional;

/**
 * 跨服活动发现目录接口
 * 维护 (configId, scopeKey) -> activeUid 的映射
 * 
 * @author shuyongqiang
 */
public interface CrossActivityDirectory {

	/**
	 * 获取当前活跃的活动 UID
	 * 
	 * @param configId 配置 ID
	 * @param scopeKey 作用域键（如 "all" 或 serverId）
	 * @return 活跃 UID
	 */
	Optional<Long> getActiveUid(int configId, String scopeKey);

	/**
	 * 注册或更新活跃活动 UID
	 * 
	 * @param configId 配置 ID
	 * @param scopeKey 作用域键
	 * @param activityUid 活动 UID
	 */
	void putActive(int configId, String scopeKey, long activityUid);

	/**
	 * CAS 安全移除活跃活动 UID（仅当当前记录与 expectedUid 匹配时移除）
	 * 
	 * @param configId 配置 ID
	 * @param scopeKey 作用域键
	 * @param expectedUid 期望的活动 UID
	 * @return 是否成功移除
	 */
	boolean removeIfMatch(int configId, String scopeKey, long expectedUid);
}
