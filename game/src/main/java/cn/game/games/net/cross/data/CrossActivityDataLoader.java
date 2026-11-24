package cn.game.games.net.cross.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.db.GenericDataLoader;
import cn.game.games.cache.entity.GameActivity;
import cn.game.games.net.cross.activity.service.CrossActivityService;
import cn.game.games.net.data.mapper.GameActivityMapper;

/**
 * 跨服活动数据分布式加载器
 * 
 * @author shuyongqiang
 */
@Component
public class CrossActivityDataLoader implements GenericDataLoader<GameActivity, Long> {

	@Autowired
	private GameActivityMapper gameActivityMapper;

	@Override
	public List<GameActivity> getBatch(Long lastId, int limit) {
		return gameActivityMapper.getBatchCursor(lastId != null ? lastId : 0L, limit);
	}

	@Override
	public void processData(List<GameActivity> data) {
		CrossActivityService.getInstance().loadActivitiesFromDb(data);
	}

	@Override
	public Long getLastIdOfBatch(Long lastId, int limit) {
		return gameActivityMapper.getLastIdOfBatch(lastId != null ? lastId : 0L, limit);
	}

	@Override
	public DistributedObjectType getDistributedObjectType() {
		return DistributedObjectType.CROSS_ACTIVITY;
	}

	@Override
	public List<Long> getBatchIdCursor(Long lastId, int limit) {
		return gameActivityMapper.getBatchIdCursor(lastId != null ? lastId : 0L, limit);
	}

	@Override
	public GameActivity load(Long id) {
		return gameActivityMapper.selectByPrimaryKey(id);
	}
}
