package cn.game.games.cache.base;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.manager.PlayerManager;

/**
 * 角色缓存op工厂
 * 
 * @author abc
 *
 */
public class PlayerCacheFactory {

	private static Logger log = LoggerFactory.getLogger(PlayerCacheFactory.class);

	// playerId => opClass => op
	private static ConcurrentHashMap<Long, ConcurrentHashMap<Class<? extends BasePlayerOp>, BasePlayerOp>> instances = new ConcurrentHashMap<>();


	/**
	 * 获取指定角色的缓存操作实例
	 * 
	 * @param playerId
	 * @param opImplClass
	 * @return
	 */
	public static <T> T getCache(long playerId, Class opImplClass) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		return (T) player.getModule(opImplClass);
		
	}

	public static <T> T getCacheOld(long playerId, Class opImplClass) {
		if (playerId <= 0) {
			throw new IllegalArgumentException("illegal playerId " + playerId);
		}

		ConcurrentHashMap<Class<? extends BasePlayerOp>, BasePlayerOp> m = instances.get(playerId);
		if (m == null) {
			m = new ConcurrentHashMap<>();
			ConcurrentHashMap<Class<? extends BasePlayerOp>, BasePlayerOp> old = instances.putIfAbsent(playerId, m);
			if (old != null) {
				m = old;
			}
		}

		BasePlayerOp op = m.get(opImplClass);
		if (op == null) {
			try {

				op = (BasePlayerOp) opImplClass.getDeclaredConstructor().newInstance();
				op.setPlayerId(playerId);
				op.init();
				BasePlayerOp old = m.putIfAbsent(opImplClass, op);
				if (old != null) {
					op = old;
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("创建cache op失败", e);
			}
		}
		return (T) op;
	}

	/**
	 * 删除一个角色的缓存数据
	 * 
	 * @param playerId
	 */
	public static void removeCache(long playerId) {
		instances.remove(playerId);

	}
	/** 
	 * 设置缓存，只测试使用。 
	 * @param playerId
	 * @param op
	 */
	public static void setCacheTest(long playerId, BasePlayerOp op) {
		ConcurrentHashMap<Class<? extends BasePlayerOp>, BasePlayerOp> concurrentHashMap = instances.get(playerId);
		if (concurrentHashMap == null) {
			concurrentHashMap = new ConcurrentHashMap<>();
			instances.put(playerId, concurrentHashMap);
		}
		concurrentHashMap.put(op.getClass(), op);
	}

	/** 
	 * 获取玩家缓存数据，只测试使用。 
	 * @param playerId
	 * @return
	 */
	public static ConcurrentHashMap<Class<? extends BasePlayerOp>, BasePlayerOp> getCacheTest(long playerId) {
		return instances.get(playerId);
	}
	/** 
	 * 设置玩家缓存数据，只测试使用。 
	 * @param playerId
	 * @param instance
	 * @return
	 */
	public static ConcurrentHashMap<Class<? extends BasePlayerOp>, BasePlayerOp> setCacheTest(long playerId,
			ConcurrentHashMap<Class<? extends BasePlayerOp>, BasePlayerOp> instance) {
		return instances.put(playerId, instance);
	}
}
