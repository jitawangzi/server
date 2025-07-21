package cn.game.games.core;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.event.EventHandler;
import cn.game.games.cache.base.DbEntity;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.PlayerEventHandler;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.db.DbTask;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo;

public abstract class BasePlayerModule implements Comparable<BasePlayerModule>, PlayerEventHandler {
	protected transient Logger log = LoggerFactory.getLogger(this.getClass());
	// @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	protected transient Player player;
	protected transient long playerId;
	protected transient Class<?>[] defaultDbMapperClass;
	private transient int tableCount = 0;

	protected static final int INIT_PRIORITY_MIDDLE = 1_0000;
	protected static final int INIT_PRIORITY_HIGH = 100;
	protected static final int INIT_PRIORITY_LOW = 100_0000;
	/**
	 * 是否初始化过
	 */
	private transient boolean initial = false;

	public BasePlayerModule() {
	}

	/** 
	 * 登陆后下发的数据，在这里构建
	 * @param builder
	 */
	public abstract void buildPlayerAllInfo(PlayerAllInfo.Builder builder);

	public final void initDefault(Player player) {
		if (!this.initial) {
			this.player = player;
			this.playerId = player.getPlayerId();
			setDefaultDbMapperClass();
			if (this instanceof EventHandler) {
				player.registerEventHandler((PlayerEventHandler) this);
			}
			init();
			initAfter();
			initial = true;
		}
	}

	public void init() {
	};
	protected void initAfter() {

	};

	private void setDefaultDbMapperClass() {
		this.defaultDbMapperClass = defaultDbMapperClass();
	}

	/**
	 * 模块初始化顺序，数字小的在前,默认10000，普通优先级
	 * @return
	 */
	protected int getInitOrder() {
		return INIT_PRIORITY_MIDDLE;
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


	/** 
	 * 单表情况下先不用这个了
	 * @return
	 */
	public Class<?>[] defaultDbMapperClass() {
		return null;
	};

	/**
	 * 从数据库中初始化数据,这里只是将数据从db加载到内存，数据的进一步初始化，
	 * 应该在{@link #initFromDbAfter()}里完成，例如检查过期数据等。 
	 * 一般在多表情况下使用，或者单表用独立表存数据的情况。 
	 * @param iterator
	 */
	protected void initFromDb(ListIterator<?> iterator) {
	}

	/** 
	 * 数据从数据库载入后的进一步初始化流程，主要针对的还是Module数据的初始化，
	 * 例如检查过期数据等，不要写具体的业务逻辑。
	 * 而玩家正常登陆才会触发的一些逻辑，不要写在这里
	 * 可以在{@link #onLogin()}里处理，或者Login、LoginFinish事件  
	 * 
	 * 比如查询离线玩家时，只要进行数据的初始化就可以了
	 *  
	 */
	public void initFromDbAfter() {
	}

	/** 
	 * 玩家登陆后的一些业务逻辑,触发时机和LoginFinish事件一样
	 */
	public void onLogin() {
	}

	public void defaultDbTasks(List<DbTask> dbTasks) {
		if (defaultDbMapperClass != null) {
			for (int i = 0; i < defaultDbMapperClass.length; i++) {
				dbTasks.add(new DbTask(defaultDbMapperClass[i], MapperConstant.selectByPlayerId, player.getPlayerId()));
			}
		}
	}

	/** 
	 * 自动保存数据的模块复写此方法
	 * @param entities,将要保存的DbEntity添加到这个List中
	 */
	public void autoSaveTasks(List<DbEntity> entities) {

	}
	
	/** 
	 * 总是使用独立的数据表来存储数据，只有在单表存储玩家数据时，这个配置才有用，配置单独的数据表来存储玩家数据
	 * 配置为true后，需要手动处理数据的更新,并且不序列化这个模块的数据，注意添加JsonIgnone
	 * 一般方便离线操作的，数据量大的，数据结构和条目比较稳定修改不频繁的，可以设置为true
	 * @return
	 */
	public boolean alwaysStoreDataInStandaloneTable() {
		return false ; 
	}

	@Override
	public String toString() {
		return super.toString() + "[playerId=" + playerId + "]";
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

	/** 
	 * 模块是否开发完毕，如果还在开发中的，返回false，运行时不会初始化
	 * @return
	 */
	public boolean isComplete() {
		return true; 
	}
}
