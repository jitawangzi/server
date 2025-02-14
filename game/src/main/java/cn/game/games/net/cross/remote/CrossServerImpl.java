package cn.game.games.net.cross.remote;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.db.GenericDataLoader;
import cn.game.games.cache.id.IdCache;
import cn.game.games.net.cross.zongmen.ZongMenBargain;
import cn.game.games.net.cross.zongmen.ZongMenInfo;
import cn.game.games.net.cross.zongmen.ZongMenManager;
import cn.game.protocol.generated.config.GuildBargainConfig;
import cn.game.protocol.generated.manager.GuildBargainManager;
import cn.game.util.SpringContextLoader;

public class CrossServerImpl implements CrossServerInterface {
	
	private static final Logger	log	= LoggerFactory.getLogger(CrossServerImpl.class);

	@Override
	public int loadDataDistributed(Class<? extends GenericDataLoader> loaderClass, int offset, int limit) {
		GenericDataLoader<?> loader = SpringContextLoader.getContext().getBean(loaderClass);
		List list = loader.getBatch(offset, limit);
		loader.processData(list);
		return list.size();
	}

	@Override
	public boolean isObjectInCurrentServer(DistributedObjectType type, long objectId) {
		return IdCache.getManager(type).isObjectInCurrentServer(objectId);
	}

	@Override
	public io.vertx.core.Future<Integer> zongmenBargainPrice(long zongmenId) {
		ZongMenInfo zongMenInfo = ZongMenManager.getInstance().getZongMenInfo(zongmenId);
		ZongMenBargain bargain = zongMenInfo.getModule().getBargain();
		GuildBargainConfig guildBargainConfig = GuildBargainManager.instance().get(bargain.getBargainItemId());
		int bargainTotalNum = bargain.getBargainTotalNum();
		int price = guildBargainConfig.Price[1] - bargainTotalNum;
		return io.vertx.core.Future.succeededFuture(price);
	}
}
