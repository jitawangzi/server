package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.battle.ChapterModule;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

/**
 * 完成指定关卡
 * 
 */
@ConditionType(type = ConditionTypeEnum.PlayerLevel)
public class LevelCondition extends AbstractCondition {

	public LevelCondition() {

	}

	@Override
	public void setEvents() {
		super.events = new EventTypeEnum[] { EventTypeEnum.Level };
	}

	@Override
	public boolean isAchieve() {

		if (super.isAchieve()) { 
			return true; 
		}
		ChapterModule chapterOp = player.getModule(ChapterModule.class);
//		ChapterOp chapterOp = player.getModule(ChapterOp.class);
		boolean pass = chapterOp.isBattleLevelPass(getRequireCount());
//		if (pass && finishCount == 0) {
//			finishCount = 1;
//		}
		return pass;
	}
	@Override
	public boolean checkEventParam(GameEvent event) {
		int level = event.getIntParameter(0);
		if (level == getRequireCount()) { return true; }
		return false;
	}
}
