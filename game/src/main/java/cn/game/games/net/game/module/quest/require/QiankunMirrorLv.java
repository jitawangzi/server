package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.QiankunMirrorLv)
public class QiankunMirrorLv extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.LevelUp };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public QiankunMirrorLv() {

	}
	@Override
	public long getFinishCount() {
		return player.getLevel(Asset.QiankunMirrorExp);
	}

	@Override
	public boolean checkEventParam(GameEvent event) {

		int exp = event.getIntParameter(0);
		int level = event.getIntParameter(1);
		if (exp == Asset.QiankunMirrorExp.ID) {
			return true;
		}
		return false;
	}
}
