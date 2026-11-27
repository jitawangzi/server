package cn.game.games.net.cross.activity;

import java.util.List;
import cn.game.games.cache.entity.GameActivity;

/**
 * 跨服活动持久化存储 SPI
 * 
 * 隔离 DAO / GameActivityMapper，支撑生产真实 MySQL 落盘与无外部依赖的高确定性单元测试。
 * 
 * @author shuyongqiang
 */
public interface CrossActivityStore {

	/**
	 * 加载全部跨服活动持久化记录
	 */
	List<GameActivity> loadAll();

	/**
	 * 按配置ID与作用域查找活动记录（用于僵尸行清理与复用判定）
	 */
	List<GameActivity> selectListByScopeAndConfig(String scopeKey, int configId);

	/**
	 * 保存或更新活动记录
	 */
	void save(GameActivity activity);

	/**
	 * 强制保存或更新活动记录；当底层持久化失败时必须抛出异常，严禁静默吞异常
	 *
	 * @param activity 待持久化的跨服活动实体
	 */
	default void saveOrThrow(GameActivity activity) {
		save(activity);
	}

	/**
	 * 按主键 UID 删除活动记录
	 */
	void delete(long activityUid);
}
