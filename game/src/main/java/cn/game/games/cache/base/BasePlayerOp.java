package cn.game.games.cache.base;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventHandler;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.db.DbTask;

public abstract class BasePlayerOp extends BaseOp implements Comparable<BasePlayerOp>, EventHandler {

	// 当前op操作的角色
	protected long playerId;
	protected Player player;
	protected Class<?>[] defaultDbMapperClass;
	int tableCount = 0;

	public final void initAfter() {
		setDefaultDbMapperClass();
		player.registerEventHandler(this);
	}

	private void setDefaultDbMapperClass() {
		this.defaultDbMapperClass = defaultDbMapperClass();
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return null;
	}

	@Override
	public void handleEvent(GameEvent event) {
	}

	/**
	 * Op 初始化顺序，数字小的在前
	 * @return
	 */
	protected int getInitOrder() {
		return 10000;
	}

	/**
	 * 初始化前置op，优先级最高。 
	 * @return
	 */
	protected Set<Class<? extends BasePlayerModule>> getPreClasses() {
		return Collections.emptySet();
	}


	public final void loadFromDb(ListIterator<?> iterator) {
		int nextIndex = iterator.nextIndex();
		initFromDb(iterator);
		int nextIndexAfter = iterator.nextIndex();
		if (nextIndexAfter - nextIndex != tableCount) {
			String error = MessageFormat.format("op[{0}]tableCount count[{1}] Iterator count[{2}]",
					this.getClass().getSimpleName(), tableCount, (nextIndexAfter - nextIndex));
			throw new IllegalArgumentException(error);
		}
	}

	/**
	 * 从数据库中初始化数据,子类需要实现
	 * @param iterator
	 */
	protected void initFromDb(ListIterator<?> iterator) {

	}

	public final void initDbTasks(List<DbTask> dbTasks) {
		int beforeSize = dbTasks.size();
		defaultDbTasks(dbTasks);
		int afterSize = dbTasks.size();
		tableCount = afterSize - beforeSize;
	}

	public Class<?>[] defaultDbMapperClass() {
		return null;
	}

	public void defaultDbTasks(List<DbTask> dbTasks) {
		if (defaultDbMapperClass != null) {
			for (int i = 0; i < defaultDbMapperClass.length; i++) {
				dbTasks.add(new DbTask(defaultDbMapperClass[i], MapperConstant.selectByPlayerId, playerId));
			}
		}
	}
	public void setPlayer(Player player) {
		this.player = player;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	@Override
	public String toString() {
		return super.toString() + "[playerId=" + playerId + "]";
	}

	public int compareTo(BasePlayerOp o) {
		Set<Class<? extends BasePlayerModule>> preClasses = getPreClasses();
		if (preClasses != null && !preClasses.isEmpty()) {
			if (preClasses.contains(o.getClass())) {
				return 1;
			}
		}
		if (getInitOrder() == o.getInitOrder()) {
			return this.getClass().getSimpleName().compareTo(o.getClass().getSimpleName());
		}
		return getInitOrder() - o.getInitOrder();
	}

}
