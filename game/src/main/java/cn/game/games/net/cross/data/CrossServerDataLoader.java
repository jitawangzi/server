package cn.game.games.net.cross.data;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.game.core.base.ServerContext;
import cn.game.core.db.GenericDataLoader;
import cn.game.games.net.cross.CrossServer;
import cn.game.games.net.cross.remote.CrossServerInterface;
import cn.game.util.ServerType;
import cn.game.util.reflect.ClassHelper;

@Service
public class CrossServerDataLoader {
	@Autowired
	private List<GenericDataLoader> loaders;

	public void load() throws Exception {
		if (ServerContext.getInstance().getServerType() == ServerType.Cross && ServerContext.getInstance().isLeader()) {
			if (loaders != null) {
				for (GenericDataLoader loader : loaders) {
					Object mapper = loader.getMapper();
					Method method = ClassHelper.findMethod(mapper.getClass(), "getTotal");
					int total = (int) method.invoke(mapper);
					int pageSize = 100;
					int totalPages = (total + pageSize - 1) / pageSize;

					for (int page = 0; page < totalPages; page++) {
						int offset = page * pageSize;
						CrossServerInterface crossServerInterface = CrossServer.getInstance().getCrossServerInterface();
						crossServerInterface.loadDataDistributed(loader.getClass(), offset, totalPages);
					}

				}
			}
		}

	}

}
