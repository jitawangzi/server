package cn.game.games.net.cross.data;

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
import cn.game.core.task.SchedulerService;
import cn.game.games.net.cross.CrossServer;
import cn.game.games.net.cross.remote.CrossServerInterface;
import cn.game.util.Config;
import cn.game.util.ServerType;
import cn.game.util.reflect.ClassHelper;

@Component
public class CrossServerDataLoader<T, ID extends Number> implements ServerInstanceListener {
	private static final int pageSize = 2;
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
