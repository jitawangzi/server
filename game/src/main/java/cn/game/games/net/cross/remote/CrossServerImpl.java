package cn.game.games.net.cross.remote;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.cache.entity.Zongmen;
import cn.game.games.net.cross.zongmen.ZongMenManager;
import cn.game.games.net.data.mapper.ZongmenMapper;
import cn.game.util.SpringContextLoader;

public class CrossServerImpl implements CrossServerInterface {
	
	private static final Logger	log	= LoggerFactory.getLogger(CrossServerImpl.class);

	@Override
	public void loadZongmen(int offset, int limit) {
		ZongmenMapper mapper = SpringContextLoader.getContext().getBean(ZongmenMapper.class);
		List<Zongmen> list = mapper.getBatch(offset, limit);
		ZongMenManager.getInstance().loadZongmenList(list);
	}

}
