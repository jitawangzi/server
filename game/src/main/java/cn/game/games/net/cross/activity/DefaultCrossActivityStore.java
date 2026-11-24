package cn.game.games.net.cross.activity;

import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.game.games.cache.entity.GameActivity;
import cn.game.games.net.data.mapper.GameActivityMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.util.DAO;
import cn.game.util.SpringContextLoader;

/**
 * 跨服活动持久化存储生产默认实现（基于 DAO 与 MyBatis GameActivityMapper）
 * 
 * @author shuyongqiang
 */
public class DefaultCrossActivityStore implements CrossActivityStore {

	private static final Logger log = LoggerFactory.getLogger(DefaultCrossActivityStore.class);

	@Override
	public List<GameActivity> loadAll() {
		if (SpringContextLoader.getContext() == null) {
			log.warn("SpringContextLoader not available, loadAll returns empty list");
			return Collections.emptyList();
		}
		try {
			return DAO.executeSync(GameActivityMapper.class, MapperConstant.selectAll);
		} catch (Exception e) {
			log.error("Failed to loadAll from GameActivityMapper", e);
			return Collections.emptyList();
		}
	}

	@Override
	public List<GameActivity> selectListByScopeAndConfig(String scopeKey, int configId) {
		if (SpringContextLoader.getContext() == null) {
			return Collections.emptyList();
		}
		try {
			return DAO.executeSync(GameActivityMapper.class, MapperConstant.selectListByScopeAndConfig,
					new Object[] { scopeKey, configId });
		} catch (Exception e) {
			log.error("Failed to selectListByScopeAndConfig: scopeKey={}, configId={}", scopeKey, configId, e);
			return Collections.emptyList();
		}
	}

	@Override
	public void save(GameActivity activity) {
		if (SpringContextLoader.getContext() == null) {
			return;
		}
		try {
			DAO.executeSync(GameActivityMapper.class, MapperConstant.insertOrUpdate, activity);
		} catch (Exception e) {
			log.error("Failed to save GameActivity uid={}", activity.getId(), e);
		}
	}

	@Override
	public void delete(long activityUid) {
		if (SpringContextLoader.getContext() == null) {
			return;
		}
		try {
			DAO.executeSync(GameActivityMapper.class, MapperConstant.deleteByPrimaryKey, activityUid);
		} catch (Exception e) {
			log.error("Failed to delete GameActivity uid={}", activityUid, e);
		}
	}
}
