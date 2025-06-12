package cn.game.core.net.remote;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.cache.id.GenericDistributedIDManager;
import cn.game.core.cache.id.IdCache;
import io.vertx.core.Future;

public interface RemoteServerInterface extends RemoteProxy {
	default boolean isObjectInCurrentServer(DistributedObjectType type, long objectId) {
		return false;
	}

	default public <ID extends Number> Future<Set<ID>> getManagedIdsInRange(DistributedObjectType type, ID minId, ID maxId) {
		Set<ID> result = new HashSet<>();
		GenericDistributedIDManager manager = IdCache.getManager(type);
		Collection<Long> allIds = manager.getAllIds();
		// 从本节点内存中查找在指定范围内的已管理ID
		for (Long id : allIds) {
			if (compareIds(id, minId) >= 0 && compareIds(id, maxId) <= 0) {
				result.add((ID) id);
			}
		}
		return Future.succeededFuture(result);
	}

	// 比较两个ID的大小，支持各种数字类型
	private <ID extends Number> int compareIds(ID id1, ID id2) {
		if (id1 instanceof Long) {
			return ((Long) id1).compareTo((Long) id2);
		} else if (id1 instanceof Integer) {
			return ((Integer) id1).compareTo((Integer) id2);
		} else {
			// 支持其他数字类型...
			throw new UnsupportedOperationException("不支持的ID类型比较");
		}
	}

}
