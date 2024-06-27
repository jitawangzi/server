package cn.game.games.net.game.module.develop;

import java.util.List;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.protocol.generated.config.QuestConfig;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.enume.QuestTypeEnum;
import cn.game.protocol.generated.manager.QuestManager;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

/**    
 * 一些玩家级别的养成数据
 * @date 2024年6月25日 下午6:48:42
 * @author SYQ
 */
public class DevelopModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay,
			EventTypeEnum.LoginFinish, EventTypeEnum.Reconnect, EventTypeEnum.LevelUp, EventTypeEnum.ResourceRemove, EventTypeEnum.FuncOpen };

	/** 天道修为等级 */
	private int heavenlyDaoLevel;


	public int getHeavenlyDaoLevel() {
		return heavenlyDaoLevel;
	}

	public void setHeavenlyDaoLevel(int heavenlyDaoLevel) {
		this.heavenlyDaoLevel = heavenlyDaoLevel;
	}
	@Override
	public void buildPlayerAllInfo(Builder builder) {
		builder.setHeavenlyDaoLevel(heavenlyDaoLevel);

	}
	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		case LoginFinish: {
			break;
		}
//		case PLAYER_CREATE: {
//			PlayerManager.getInstance().online(playerId, ServerContext.getInstance().getServerId());
//			break;
//		}
		case FuncOpen: {
			InitialUI func = event.getParameter(0);
			if (func == InitialUI.HeavenlyDaoCultivation) {
				heavenlyDaoLevel = 1;
				QuestModule questModule = player.getQuestModule();
				List<QuestConfig> groupList = QuestManager.instance().getTypeList(QuestTypeEnum.HeavenlyDao.ID);
				for (QuestConfig questConfig : groupList) {
					questModule.open(questConfig.ID, false);
				}
			}
			break;
		}
		case NewDay: {
//			isFirstLoign = true;
			break;
		}
		}
	}

	@Override
	protected int getInitOrder() {
		return INIT_PRIORITY_HIGH;
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void initFromDbAfter() {

	}

}
