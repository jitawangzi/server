package cn.game.core.cache.id;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import cn.game.core.base.ServerContext;
import cn.game.core.task.SchedulerService;

public class RollingRenewScheduler {
	private final SchedulerService ses = SchedulerService.getInstance();
	private final RedissonLeaseRenewer renewer;
	private final int batchSize;
	private final long tickMillis;
	private final int ttlSeconds;

	// 每类型一个迭代器游标
	private final Map<DistributedObjectType, Iterator<Long>> iterators = new ConcurrentHashMap<>();

	public RollingRenewScheduler(RedissonLeaseRenewer renewer, int batchSize, long tickMillis, int ttlSeconds) {
		this.renewer = renewer;
		this.batchSize = batchSize; // 建议 200~1000
		this.tickMillis = tickMillis; // 建议 100~500ms
		this.ttlSeconds = ttlSeconds; // 建议 3x~4x 覆盖周期
	}

	public void start() {
		ses.scheduleAtFixedRate(this::tickSafe, 0, tickMillis, TimeUnit.MILLISECONDS);
	}

	public void stop() {
		ses.shutdown();
	}

	private void tickSafe() {
		try {
			tick();
		} catch (Throwable t) {
			/* log warn */ }
	}

	private void tick() {
		for (DistributedObjectType t : DistributedObjectType.values()) {
			GenericDistributedIDManager m = getManager(t);
			Iterator<Long> it = iterators.computeIfAbsent(t, k -> snapshotIterator(m.getAllIds()));

			Map<String, String> keyToVal = new LinkedHashMap<>(batchSize);
			while (it.hasNext() && keyToVal.size() < batchSize) {
				long id = it.next();
				String key = m.generateRedisKey(id);
				String expected = ServerContext.getInstance().getServerId();
				keyToVal.put(key, expected);
			}
			if (keyToVal.isEmpty()) {
				// 当前迭代耗尽，重建快照以适配动态集合
				iterators.put(t, snapshotIterator(m.getAllIds()));
				continue;
			}

			renewer.renewBatch(keyToVal, ttlSeconds).whenComplete((res, ex) -> {
				if (ex != null) {
					// TODO: 打点与重试策略（可选：简单丢给下一轮）
				} else {
					// 可选：统计成功率/返回0比例
				}
			});
		}
	}

	private Iterator<Long> snapshotIterator(Collection<Long> ids) {
		return new ArrayList<>(ids).iterator();
	}

	// 你项目里已有的方法，这里占位
	private GenericDistributedIDManager getManager(DistributedObjectType t) {
		return IdCache.getManager(t);
	}
}
