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
	private transient Class<?>[] defaultDbMapperClass;
	private transient String[] defaultSelectMethodName;
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
			setDefaultSelectMethodName();
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

	private void setDefaultSelectMethodName() {
		this.defaultSelectMethodName = defaultSelectMethodName();
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
			String error = MessageFormat.format("PlayerModule[{0}]tableCount count[{1}] Iterator count[{2}]",
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
	 * 这个模块的部分或者所有数据，需要使用独立的表存储数据， 
	 * 则返回对应表的Mapper class 
	 * @return
	 */
	protected Class<?>[] defaultDbMapperClass() {
		return null;
	};
	/** 
	 * 对应mapper class 的查询方法名
	 * 如果不设置，则使用MapperConstant.selectByPlayerId
	 * 如果有自定义的查询方法名，则返回对应的查询方法名数组
	 * @return
	 */
	protected String[] defaultSelectMethodName() {
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
	 * 这个时机在处理跨天逻辑之后
	 * 这里面不要注册事件，只处理业务逻辑
	 */
	public void onLogin() {
	}

	public void defaultDbTasks(List<DbTask> dbTasks) {
		if (defaultDbMapperClass != null) {
			if (defaultSelectMethodName != null && defaultSelectMethodName.length != defaultDbMapperClass.length) {
				throw new IllegalArgumentException("defaultSelectMethodName length must match defaultDbMapperClass length");
			}
			for (int i = 0; i < defaultDbMapperClass.length; i++) {
				dbTasks.add(new DbTask(defaultDbMapperClass[i],
						defaultSelectMethodName == null ? MapperConstant.selectByPlayerId : defaultSelectMethodName[i],
						player.getPlayerId()));
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
	 * 手动配置这个模块的数据(部分或者全部数据)需要使用独立的表来存储数据,
	 * 也可以直接通过复写 {@link #defaultDbMapperClass()} 来代替，可以忽略这个方法。 
	 * 配置为true后，需要手动处理独立的表数据的更新,并且不能序列化这部分的数据，注意声明为transient 或者添加JsonIgnone注解。
	 * 一般方便离线操作的，数据量大的，数据结构和条目比较稳定修改不频繁的，可以设置为true
	 * @return 是否使用了独立的数据库表来存储数据
	 */
	public boolean alwaysStoreDataInStandaloneTable() {
		return this.defaultDbMapperClass != null && this.defaultDbMapperClass.length > 0;
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
