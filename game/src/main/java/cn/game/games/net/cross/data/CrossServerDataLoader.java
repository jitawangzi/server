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

@Component
public class CrossServerDataLoader<T> implements ServerInstanceListener {
	private static final int pageSize = 100;
	@Autowired
	private List<GenericDataLoader<T>> loaders;

	@PostConstruct
	public void init() {
		ActiveServerListManager.getInstance().addServerInstanceListener(this);
	}

	public synchronized void load() {
		if (ServerContext.getInstance().getServerType() == ServerType.Cross && ServerContext.getInstance().isLeader()) {
			if (loaders != null) {
				for (GenericDataLoader<T> loader : loaders) {
					Long lastId = 0L; // 初始ID值
					List<T> batch;
					while (true) {
						// 查询一批数据
						batch = (List<T>) loader.getBatch(lastId, pageSize);
						if (batch == null || batch.isEmpty()) {
							break;
						}
						CrossServerInterface crossServerInterface = CrossServer.getInstance().getCrossServerInterface();
						crossServerInterface.loadDataDistributed(loader.getClass(), lastId, pageSize);

						// 更新lastId为当前处理的最后一条记录的ID
						lastId = loader.getLastId(batch.get(batch.size() - 1));
						// TODO 查了两遍数据库，后续优化一下

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
