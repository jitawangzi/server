package cn.game.games.net.cross.data;

import java.lang.reflect.Method;
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
public class CrossServerDataLoader implements ServerInstanceListener {
	private static final int pageSize = 100;
	@Autowired
	private List<GenericDataLoader> loaders;

	@PostConstruct
	public void init() {
		ActiveServerListManager.getInstance().addServerInstanceListener(this);
	}

	public synchronized void load() {
		if (ServerContext.getInstance().getServerType() == ServerType.Cross && ServerContext.getInstance().isLeader()) {
			if (loaders != null) {
				for (GenericDataLoader loader : loaders) {
					Object mapper = loader.getMapper();
					Method method = ClassHelper.findMethod(mapper.getClass(), "getTotal");
					int total;
					try {
						total = (int) method.invoke(mapper);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					int totalPages = (total + pageSize - 1) / pageSize;

					for (int page = 0; page < totalPages; page++) {
						int offset = page * pageSize;
						CrossServerInterface crossServerInterface = CrossServer.getInstance().getCrossServerInterface();
						crossServerInterface.loadDataDistributed(loader.getClass(), offset, pageSize);
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
