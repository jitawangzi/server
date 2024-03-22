package cn.game.games.net.game.module.quest.require.tag;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.battle.BattleInfo;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.battle.IRoleBattleAction;

/** 
* 默认角色标签条件
* @date 2022年11月4日 下午5:30:29 
* @author YYB 
*/
public abstract class AbstractRoleTagCondition extends AbstractCondition {
	
	/** 角色id */
	protected int roleId;
	
	/**
	 * 获取条件更新数量
	 * @param roleBattleAction
	 * @return
	 */
	public abstract int getUpdateCount(IRoleBattleAction roleBattleAction);
	
	@Override
	public void setEvents() {
		super.events = new EventTypeEnum[] { EventTypeEnum.BattleEnd };		
	}
	
	@Override
	public boolean checkEventParam(GameEvent event) {
		Object obj = event.getParameter(0);
		if (obj instanceof IRoleBattleAction && getUpdateCount((IRoleBattleAction) obj) > 0) {
			return true;
		}
		return false;
	}
	
	@Override
	public void updateRequireCount(GameEvent event) {
		IRoleBattleAction roleBattleAction = (IRoleBattleAction) event.getParameter(0);
		int count = getUpdateCount(roleBattleAction);
		finishCount += count;
	}
	
	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}
	
	public int getRoleId() {
		return this.roleId;
	}
	

}
