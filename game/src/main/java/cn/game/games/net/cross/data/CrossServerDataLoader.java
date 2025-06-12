package cn.game.games.net.cross.data;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.game.core.base.ActiveServerListManager;
import cn.game.core.base.ServerContext;
import cn.game.core.base.ServerInstanceListener;
import cn.game.core.db.GenericDataLoader;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.task.SchedulerService;
import cn.game.core.util.AsyncUtils;
import cn.game.games.net.cross.CrossServer;
import cn.game.games.net.cross.remote.CrossServerInterface;
import cn.game.util.Config;
import cn.game.util.ServerType;
import cn.game.util.log.LoggerType;
import cn.game.util.reflect.ClassHelper;
import io.vertx.core.Future;

@Component
public class CrossServerDataLoader<T, ID extends Number> implements ServerInstanceListener {
	private static final int pageSize = 20;
	@Autowired
	private List<GenericDataLoader<T, ID>> loaders;

	@PostConstruct
	public void init() {
		ActiveServerListManager.getInstance().addServerInstanceListener(this);
	}

	public synchronized void load() {
		if (ServerContext.getInstance().getServerType() == ServerType.Cross && ServerContext.getInstance().isLeader()) {
			if (loaders != null) {
				for (GenericDataLoader<T, ID> loader : loaders) {
					// 获取ID的实际类型
					Class<?> idType = ClassHelper.getGenericParameterType(loader, 1);
					ID lastId;
					// 根据类型初始化lastId
					if (idType == Long.class) {
						lastId = (ID) Long.valueOf(0L);
					} else if (idType == Integer.class) {
						lastId = (ID) Integer.valueOf(0);
					} else {
						throw new RuntimeException("不支持的ID类型: " + idType.getName());
					}
					while (true) {
						@SuppressWarnings("unchecked")
						Class<? extends GenericDataLoader<T, ID>> loaderClass = (Class<? extends GenericDataLoader<T, ID>>) loader
								.getClass();
						CrossServerInterface crossServerInterface = CrossServer.getInstance().getCrossServerInterface();
						crossServerInterface.loadDataDistributed(loaderClass, lastId, pageSize);
						// 更新lastId为当前处理的最后一条记录的ID
						lastId = loader.getLastIdOfBatch(lastId, pageSize);
						if (lastId == null) {
							break;
						}

					}
				}
			}
		}

	}

	public synchronized void reload() {
		VxHolder.vertx.executeBlocking(() -> {
			if (ServerContext.getInstance().getServerType() == ServerType.Cross && ServerContext.getInstance().isLeader()) {
				if (loaders != null) {
					for (GenericDataLoader<T, ID> loader : loaders) {
						// 获取ID的实际类型
						Class<?> idType = ClassHelper.getGenericParameterType(loader, 1);
						ID lastId;
						// 根据类型初始化lastId
						if (idType == Long.class) {
							lastId = (ID) Long.valueOf(0L);
						} else if (idType == Integer.class) {
							lastId = (ID) Integer.valueOf(0);
						} else {
							throw new RuntimeException("不支持的ID类型: " + idType.getName());
						}
						@SuppressWarnings("unchecked")
						Class<? extends GenericDataLoader<T, ID>> loaderClass = (Class<? extends GenericDataLoader<T, ID>>) loader
								.getClass();
						List<CrossServerInterface> allCrossServerInterface = CrossServer.getInstance().getAllCrossServerInterface();
						load(allCrossServerInterface, loader, loaderClass, lastId);

					}
				}
			}
			return null;
		});

	}

	private void load(List<CrossServerInterface> allCrossServerInterface, GenericDataLoader<T, ID> loader,
			Class<? extends GenericDataLoader<T, ID>> loaderClass, ID lastId) {

		List<ID> ids = loader.getBatchIdCursor(lastId, pageSize);
		if (ids == null || ids.isEmpty()) {
			return;
		}
		lastId = ids.get(ids.size() - 1);
		ID tmpId = lastId;
		Future<HashSet<Long>> future = AsyncUtils.sequentialCollect(allCrossServerInterface, crossServerInterface -> {
			return crossServerInterface.getManagedIdsInRange(loader.getDistributedObjectType(), ids.get(0), ids.get(ids.size() - 1));
		}, (t1, t2) -> {
			t2.addAll((HashSet<Long>) t1);
			return (HashSet<Long>) t2;
		}, r -> {
			return r.size() == ids.size();
		}, new HashSet<Long>());
		future.onComplete(r -> {
			VxHolder.vertx.executeBlocking(() -> {
				if (r.failed()) {
					LoggerType.Stdout.logger.error("获取分布式对象ID失败", r.cause());
				} else {
					ids.removeAll(r.result());
					for (ID id : ids) {
						CrossServer.getInstance().getCrossServerInterface(id.longValue()).loadDataDistributed(loaderClass, id);
					}
				}
				load(allCrossServerInterface, loader, loaderClass, tmpId);
				return null;
			});
		});
	}

	@Override
	public void onServerJoin(String serverId, ServerType serverType) {

	}

	@Override
	public void onServerLeave(String serverId, ServerType serverType) {
		if (serverType != ServerType.Cross) {
			return;
		}
		// TODO 优化一个等待周期，多个加载操作合并为一次
		ScheduledFuture<?> scheduleTask = SchedulerService.getInstance().scheduleTask(() -> {
			load();
			// 等待redis中的id--serverId自然过期再重新加载数据
		}, Config.DEFAULT_REDIS_DISTRIBUTED_OBJECT_EXPIRE_SECONDS + 10, TimeUnit.SECONDS);

	}

}
