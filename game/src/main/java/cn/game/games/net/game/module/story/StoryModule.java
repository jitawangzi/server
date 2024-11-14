package cn.game.games.net.game.module.story;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.game.games.cache.entity.Story;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

public class StoryModule extends BasePlayerModule {

	private Map<Integer, Story> storys = new HashMap<Integer, Story>();

//	public void initLoadData(List<Story> storys) {
//		for (Story story : storys) {
//			this.storys.put(story.getStory(), story);
//		}
//	}

	public boolean update(int id, int count, boolean finish) {
		Story story = get(id);
		if (story == null) {
			story = Story.valueOf(playerId, id);
			story.setStartConditionCount(count);
			story.setFinish(finish);
//			DAO.insert(story);
			this.storys.put(id, story);
		} else {
			if (story.getFinish()) {
//				StoryConfig storyConfig = StoryManager.getInstance().getStoryConfig(id);
//				if (!storyConfig.getRepeat()) { 
//					return false; 
//				}
			}
			story.setFinish(finish);
			if (count > 0) {
				story.setStartConditionCount(story.getStartConditionCount() + count);
			}
//			DAO.update(story);
		}
		return true;
	}

	public Story get(int id) {
		return this.storys.get(id);
	}

	public boolean finish(int id) {

		Story story = get(id); 
		if (story == null || story.getFinish()) {
			return false ; 
		}
		update(id, 0, true);
		return true;

	}

	public Collection<Story> list() {
		return this.storys.values();
	}

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
	public void initFromDbAfter() {

	};
	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}
}
