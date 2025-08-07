package cn.game.games.net.cross.remote;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.cache.id.IdCache;
import cn.game.core.db.GenericDataLoader;
import cn.game.games.net.cross.zongmen.ZongMenBargain;
import cn.game.games.net.cross.zongmen.ZongMen;
import cn.game.games.net.cross.zongmen.ZongMenManager;
import cn.game.games.net.cross.zongmen.ZongMenMember;
import cn.game.protocol.generated.config.GuildBargainConfig;
import cn.game.protocol.generated.manager.GuildBargainManager;
import cn.game.util.SpringContextLoader;
import io.vertx.core.Future;

@Component
public class CrossServerImpl implements CrossServerInterface {
	
	private static final Logger	log	= LoggerFactory.getLogger(CrossServerImpl.class);

	@Override
	public <T, ID extends Number> int loadDataDistributed(Class<? extends GenericDataLoader<T, ID>> loaderClass, ID lastId, int limit) {
		GenericDataLoader<T, ID> loader = SpringContextLoader.getContext().getBean(loaderClass);
		List<T> list = loader.getBatch(lastId, limit);
		loader.processData(list);
		return list.size();
	}


	@Override
	public <T, ID extends Number> int loadDataDistributed(Class<? extends GenericDataLoader<T, ID>> loaderClass, ID id) {
		GenericDataLoader<T, ID> loader = SpringContextLoader.getContext().getBean(loaderClass);
		T data = loader.load(id);
		loader.processData(List.of(data));
		return 1;
	}

	@Override
	public boolean isObjectInCurrentServer(DistributedObjectType type, long objectId) {
		return IdCache.getManager(type).isObjectInCurrentServer(objectId);
	}

}
