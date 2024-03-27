package cn.game.games.core;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.cache.base.DbEntity;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventHandler;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.db.DbTask;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo;

public abstract class BasePlayerModule implements Comparable<BasePlayerModule>, EventHandler {
	protected transient Logger log = LoggerFactory.getLogger(this.getClass());

	protected transient Player player;
	protected transient long playerId;
	protected transient Class<?>[] defaultDbMapperClass;
	transient int tableCount = 0;
	/**
	 * 是否初始化过
	 */
	private transient boolean initial = false;

	public BasePlayerModule() {
	}

	public Player getPlayer() {
		return this.player;
	}

	public abstract void buildPlayerAllInfo(PlayerAllInfo.Builder builder);

	/**
	 * 初始化方法逻辑，默认不实现，如有逻辑需要自行实现
	 */
	public final void playerInit(Player player) {
		if (!this.initial) {
			this.player = player;
			if (this instanceof EventHandler) {
				player.registerEventHandler((EventHandler) this);
			}
			initAfter(player);
			initial = true;
		}
	}

	/**
	 * 需要执行一些数据转换的操作
	 */
	public void beforeSave() {
	}

	protected void initAfter(Player player) {

	};

	public void init() {
	};

	public final void initDefault(Player player) {
		if (!this.initial) {
			setDefaultDbMapperClass();
			if (this instanceof EventHandler) {
				player.registerEventHandler((EventHandler) this);
			}
			init();
			initAfter(player);
			initial = true;
		}
	}

	private void setDefaultDbMapperClass() {
		this.defaultDbMapperClass = defaultDbMapperClass();
	}

	/**
	 * 模块初始化顺序，数字小的在前
	 * @return
	 */
	protected int getInitOrder() {
		return 10000;
	}

	/**
	 * 初始化前置模块，优先级最高。 
	 * @return
	 */
	protected Set<Class<? extends BasePlayerModule>> getPreClasses() {
		return Collections.emptySet();
	}

	public final void loadFromDb(ListIterator<?> iterator) {
		int nextIndex = iterator.nextIndex();
		initFromDb(iterator);
		initFromDbAfter();
		int nextIndexAfter = iterator.nextIndex();
		if (nextIndexAfter - nextIndex != tableCount) {
			String error = MessageFormat.format("op[{0}]tableCount count[{1}] Iterator count[{2}]",
					this.getClass().getSimpleName(), tableCount, (nextIndexAfter - nextIndex));
			throw new IllegalArgumentException(error);
		}
	}

	public final void initDbTasks(List<DbTask> dbTasks) {
		int beforeSize = dbTasks.size();
		defaultDbTasks(dbTasks);
		int afterSize = dbTasks.size();
		tableCount = afterSize - beforeSize;
	}


	public abstract Class<?>[] defaultDbMapperClass();

	/**
	 * 从数据库中初始化数据,子类需要实现,这里只是将数据从db加载到内存，数据的进一步初始化，
	 * 应该在{@link #initFromDbAfter()}里完成，例如初始化任务的事件监听、登陆时检查过期数据等。 
	 * @param iterator
	 */
	protected abstract void initFromDb(ListIterator<?> iterator);

	/** 
	 * 业务数据从数据库载入后的自定义初始化流程
	 */
	public void initFromDbAfter() {

	};

	public void defaultDbTasks(List<DbTask> dbTasks) {
		if (defaultDbMapperClass != null) {
			for (int i = 0; i < defaultDbMapperClass.length; i++) {
				dbTasks.add(new DbTask(defaultDbMapperClass[i], MapperConstant.selectByPlayerId, player.getData().getPlayerId()));
			}
		}
	}

	/** 
	 * 自动保存数据的模块复写此方法
	 * @param entities,将要保存的DbEntity添加到这个List中
	 */
	public void autoSaveTasks(List<DbEntity> entities) {

	}

	public void setPlayer(Player player) {
		this.player = player;
		this.playerId = player.getPlayerId();
	}

	@Override
	public String toString() {
		return super.toString() + "[playerId=" + player.getData().getPlayerId() + "]";
	}

	@Override
	public int compareTo(BasePlayerModule o) {
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

	public boolean isComplete() {
		return true; 
	}
//	@Override
//	public EventTypeEnum[] getEventTypes() {
//		return null;
//	}
//
//	@Override
//	public void handleEvent(GameEvent event) {
//	}

}
