package cn.game.games.net.cross.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.game.core.db.GenericDataLoader;
import cn.game.games.cache.entity.Zongmen;
import cn.game.games.net.cross.zongmen.ZongMenManager;
import cn.game.games.net.data.mapper.ZongmenMapper;

@Component
public class ZongmenDataLoader implements GenericDataLoader<Zongmen, Long> {

	@Autowired
	private ZongmenMapper zongmenMapper;

	@Override
	public List<Zongmen> getBatch(Long lastId, int limit) {
		return zongmenMapper.getBatchCursor(lastId, limit);
	}

	@Override
	public void processData(List<Zongmen> data) {
		ZongMenManager.getInstance().loadZongmenList(data);
	}

	@Override
	public Long getLastIdOfBatch(Long lastId, int limit) {
		return zongmenMapper.getLastIdOfBatch(lastId, limit);
	}

}