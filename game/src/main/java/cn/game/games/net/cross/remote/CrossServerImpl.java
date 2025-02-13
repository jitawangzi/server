package cn.game.games.net.cross.remote;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.db.GenericDataLoader;
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
}
