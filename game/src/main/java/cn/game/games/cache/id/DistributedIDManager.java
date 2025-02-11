package cn.game.games.cache.id;

import java.util.Collection;

import cn.game.core.cache.CacheConfig;
import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.cache.id.GenericDistributedIDManager;
import cn.game.games.net.cross.zongmen.ZongMenManager;
import cn.game.games.net.game.manager.PlayerManager;

public class DistributedIDManager extends GenericDistributedIDManager {

	public DistributedIDManager(DistributedObjectType objectType, CacheConfig cacheConfig) {
		super(objectType, cacheConfig);
	}

	@Override
	public boolean isObjectInCurrentServer(long objectId) {
		// 根据不同对象类型实现具体的检查逻辑
		switch (objectType) {
		case PLAYER:
			return PlayerManager.getInstance().getPlayer(objectId) != null;
		case ZONGMEN:
			return ZongMenManager.getInstance().getZongMenInfo(objectId) != null;
		default:
			throw new IllegalArgumentException("Unsupported object type: " + objectType);
		}
	}

	@Override
	public Collection<Long> getAllIds() {
		// 根据不同对象类型提供id
		switch (objectType) {
		case PLAYER:
			return PlayerManager.getInstance().getAllPlayer().keySet();
		case ZONGMEN:
			return ZongMenManager.getInstance().getAllZongMenIds();
		default:
			throw new IllegalArgumentException("Unsupported object type: " + objectType);
		}
	}
}
