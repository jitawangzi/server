package cn.game.games.net.cross.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.db.GenericDataLoader;
import cn.game.games.cache.entity.ZongmenData;
import cn.game.games.net.cross.zongmen.ZongMenManager;
import cn.game.games.net.data.mapper.ZongmenDataMapper;

@Component
public class ZongmenDataLoader implements GenericDataLoader<ZongmenData, Long> {

	@Autowired
	private ZongmenDataMapper zongmenMapper;

	@Override
	public List<ZongmenData> getBatch(Long lastId, int limit) {
		return zongmenMapper.getBatchCursor(lastId, limit);
	}

	@Override
	public void processData(List<ZongmenData> data) {
		ZongMenManager.getInstance().loadZongmenList(data);
	}

	@Override
	public Long getLastIdOfBatch(Long lastId, int limit) {
		return zongmenMapper.getLastIdOfBatch(lastId, limit);
	}

	@Override
	public DistributedObjectType getDistributedObjectType() {
		return DistributedObjectType.ZONGMEN;
	}

	@Override
	public List<Long> getBatchIdCursor(Long lastId, int limit) {
		return zongmenMapper.getBatchIdCursor(lastId, limit);
	}

	@Override
	public ZongmenData load(Long id) {
		return zongmenMapper.selectByPrimaryKey(id);
	}

}