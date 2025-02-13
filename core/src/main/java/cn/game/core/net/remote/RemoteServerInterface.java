package cn.game.core.net.remote;

import cn.game.core.cache.id.DistributedObjectType;

public interface RemoteServerInterface extends RemoteProxy {
	default boolean isObjectInCurrentServer(DistributedObjectType type, long objectId) {
		return false;
	}

}
