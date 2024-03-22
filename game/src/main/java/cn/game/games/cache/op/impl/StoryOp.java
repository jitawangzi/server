package cn.game.games.cache.op.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import cn.game.games.cache.entity.Story;
import cn.game.games.cache.op.face.IStoryOp;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.StoryMapper;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.StoryConfig;
import cn.game.protocol.generated.manager.StoryManager;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

public class StoryOp extends BasePlayerModule implements IStoryOp {

	private Map<Integer, Story> storys;

	@Override
	public void init() {
		storys = new HashMap<Integer, Story>();
	}

	@Override
	public void initLoadData(List<Story> storys) {
		for (Story story : storys) {
			this.storys.put(story.getStory(), story);
		}
	}

	@Override
	public boolean update(int id, int count, boolean finish) {
		Story story = get(id);
		if (story == null) {
			story = Story.valueOf(playerId, id);
			story.setStartConditionCount(count);
			story.setFinish(finish);
			DAO.insert(StoryMapper.class, story);
			this.storys.put(id, story);
		} else {
			if (story.getFinish()) {
				StoryConfig storyConfig = StoryManager.getInstance().getStoryConfig(id);
				if (!storyConfig.getRepeat()) { 
					return false; 
				}
			}
			story.setFinish(finish);
			if (count > 0) {
				story.setStartConditionCount(story.getStartConditionCount() + count);
			}
			DAO.update(StoryMapper.class, story);
		}
		return true;
	}

	@Override
	public Story get(int id) {
		return this.storys.get(id);
	}

	@Override
	public boolean finish(int id) {

		Story story = get(id); 
		if (story == null || story.getFinish()) {
			return false ; 
		}
		update(id, 0, true);
		return true;

	}

	@Override
	public Collection<Story> list() {
		return this.storys.values();
	}

	@Override
	public boolean isFinish(List<Integer> ids) {
		for (Integer id : ids) {
			Story story = get(id); 
			if (story == null || !story.getFinish()) {
				return false; 
			}
		}
		return true;
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleEvent(GameEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void initFromDb(ListIterator<?> iterator) {
		// TODO Auto-generated method stub

	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}
}
