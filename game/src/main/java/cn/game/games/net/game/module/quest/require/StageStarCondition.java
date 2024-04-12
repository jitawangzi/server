package cn.game.games.net.game.module.quest.require;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.battle.ChapterOp;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.OldConditionTypeEnum;

/**
 * 副本达到指定星数
 * 
 */
@ConditionType(type = OldConditionTypeEnum.CumulativeKillMonsters)
public class StageStarCondition extends AbstractCondition {
	public StageStarCondition() {

	}
	@Override
	public void setEvents() {
		super.events = new EventTypeEnum[] { EventTypeEnum.Level };
	}
	@Override
	@JsonIgnore
	public long getFinishCount() {
		if (finishCount == 0) {
			ChapterOp stageOp = player.getModule(ChapterOp.class);
			finishCount = stageOp.getAllStars();
		}
		return finishCount;
	}

	@Override
	public void updateRequireCount(GameEvent event) {

		if (finishCount == 0) {
			ChapterOp stageOp = player.getModule(ChapterOp.class);
			finishCount = stageOp.getAllStars();
		} else {
			int p = event.getIntParameter(0);
			finishCount += p;
		}
	}

	@Override
	public boolean checkEventParam(GameEvent event) {
		return true;
	}

}
