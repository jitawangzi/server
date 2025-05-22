package cn.game.games.net.game.module.quest.require.tag;

import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.battle.IRoleBattleAction;
import cn.game.games.net.game.module.quest.AbstractCondition;

/** 
* 默认角色标签条件
* 2022年11月4日 下午5:30:29 
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
	public boolean checkEventParam(PlayerEvent event) {
		Object obj = event.getParameter(0);
		if (obj instanceof IRoleBattleAction && getUpdateCount((IRoleBattleAction) obj) > 0) {
			return true;
		}
		return false;
	}
	
	@Override
	public void updateRequireCount(PlayerEvent event) {
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
