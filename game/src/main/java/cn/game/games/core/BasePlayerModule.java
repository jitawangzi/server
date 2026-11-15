package cn.game.games.core;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

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
	/** inspectState递归快照最大深度上限 */
	public static final int MAX_INSPECT_DEPTH = 8;
	/** inspectState单容器/单集合最大导出条目上限 */
	public static final int MAX_INSPECT_ELEMENTS = 200;
	/** inspectState禁止深层反射的JDK及底层框架包名前缀 */
	private static final String[] BLOCKED_INSPECT_PACKAGE_PREFIXES = {
			"java.", "javax.", "sun.", "jdk.", "io.netty.", "io.vertx."
	};
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

	/**
	 * 只读返回当前模块内部状态快照，仅基于字段读取，不触发任何懒初始化getter或写库副作用
	 *
	 * @return 模块状态只读快照
	 */
	public Object inspectState() {
		Map<String, Object> snapshot = new LinkedHashMap<>();
		IdentityHashMap<Object, Boolean> activeAncestors = new IdentityHashMap<>();
		activeAncestors.put(this, Boolean.TRUE);
		try {
			for (Class<?> cursor = this.getClass(); cursor != null && cursor != BasePlayerModule.class && cursor != Object.class; cursor = cursor.getSuperclass()) {
				Field[] fields = cursor.getDeclaredFields();
				for (Field field : fields) {
					int modifiers = field.getModifiers();
					if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) || field.isSynthetic()) {
						continue;
					}
					if (Player.class.isAssignableFrom(field.getType()) || Logger.class.isAssignableFrom(field.getType())) {
						continue;
					}
					if (snapshot.containsKey(field.getName())) {
						continue;
					}
					try {
						field.setAccessible(true);
						Object value = field.get(this);
						snapshot.put(field.getName(), toInspectValue(value, 0, activeAncestors));
					} catch (Throwable e) {
						log.warn("读取模块只读快照字段失败 module={} field={}", this.getClass().getSimpleName(), field.getName(), e);
						snapshot.put(field.getName(), Map.of("_inspectError", formatInspectError(e)));
					}
				}
			}
		} finally {
			activeAncestors.remove(this);
		}
		return snapshot;
	}

	/**
	 * 受控只读转换内部对象快照，使用祖先链环检测、深度上限与容器元素上限避免StackOverflowError或内存膨胀
	 *
	 * @param value 原始字段值
	 * @return 可安全序列化的只读快照对象
	 */
	protected final Object toInspectValue(Object value) {
		return toInspectValue(value, 0, new IdentityHashMap<>());
	}

	/**
	 * 按上限截取集合快照并向目标快照Map写入截断标记
	 *
	 * @param <T> 集合元素类型
	 * @param source 源集合
	 * @param itemMapper 单元素只读映射器
	 * @param snapshot 目标快照Map
	 * @param listKey 明细列表键名
	 * @param truncatedKey 截断标记键名
	 * @return 截断后的明细列表
	 */
	protected final <T> List<Object> snapshotBoundedList(Collection<T> source, Function<T, Object> itemMapper,
			Map<String, Object> snapshot, String listKey, String truncatedKey) {
		List<Object> list = new ArrayList<>();
		boolean truncated = false;
		if (source != null) {
			for (T item : source) {
				if (list.size() >= MAX_INSPECT_ELEMENTS) {
					truncated = true;
					break;
				}
				list.add(itemMapper != null ? itemMapper.apply(item) : toInspectValue(item));
			}
		}
		snapshot.put(listKey, list);
		if (truncated) {
			if (truncatedKey != null && !truncatedKey.isBlank()) {
				snapshot.put(truncatedKey, true);
			}
			snapshot.put("truncated", true);
		}
		return list;
	}

	/**
	 * 按上限截取Map快照并向目标快照Map写入截断标记
	 *
	 * @param <K> 源Map键类型
	 * @param <V> 源Map值类型
	 * @param source 源Map
	 * @param keyMapper 键映射器
	 * @param valueMapper 值映射器
	 * @param snapshot 目标快照Map
	 * @param mapKey 明细Map键名
	 * @param truncatedKey 截断标记键名
	 * @return 截断后的明细Map
	 */
	protected final <K, V> Map<Object, Object> snapshotBoundedMap(Map<K, V> source, Function<K, Object> keyMapper,
			Function<V, Object> valueMapper, Map<String, Object> snapshot, String mapKey, String truncatedKey) {
		Map<Object, Object> bounded = new LinkedHashMap<>();
		boolean truncated = false;
		if (source != null) {
			for (Map.Entry<K, V> entry : source.entrySet()) {
				if (bounded.size() >= MAX_INSPECT_ELEMENTS) {
					truncated = true;
					break;
				}
				Object mappedKey = keyMapper != null ? keyMapper.apply(entry.getKey()) : entry.getKey();
				Object mappedVal = valueMapper != null ? valueMapper.apply(entry.getValue()) : toInspectValue(entry.getValue());
				bounded.put(mappedKey, mappedVal);
			}
		}
		snapshot.put(mapKey, bounded);
		if (truncated) {
			if (truncatedKey != null && !truncatedKey.isBlank()) {
				snapshot.put(truncatedKey, true);
			}
			snapshot.put("truncated", true);
		}
		return bounded;
	}

	private Object toInspectValue(Object value, int depth, IdentityHashMap<Object, Boolean> activeAncestors) {
		if (value == null) {
			return null;
		}
		if (depth > MAX_INSPECT_DEPTH) {
			return "<maxDepth>";
		}
		if (value instanceof Boolean || value instanceof Number || value instanceof Character || value instanceof String) {
			return value;
		}
		if (value instanceof Enum<?>) {
			return ((Enum<?>) value).name();
		}
		if (value instanceof Date || value instanceof UUID) {
			return value;
		}
		if (value instanceof TemporalAccessor) {
			return value.toString();
		}
		if (value instanceof AtomicBoolean) {
			return ((AtomicBoolean) value).get();
		}
		if (value instanceof Optional<?>) {
			Optional<?> opt = (Optional<?>) value;
			return opt.isPresent() ? toInspectValue(opt.get(), depth + 1, activeAncestors) : null;
		}
		if (value instanceof Player || value instanceof Logger) {
			return "<skipped:" + value.getClass().getSimpleName() + ">";
		}
		if (activeAncestors.containsKey(value)) {
			return "<circular:" + value.getClass().getSimpleName() + ">";
		}

		activeAncestors.put(value, Boolean.TRUE);
		try {
			if (value instanceof Map<?, ?>) {
				return convertMapSnapshot((Map<?, ?>) value, depth, activeAncestors);
			}
			if (value instanceof Collection<?>) {
				return convertCollectionSnapshot((Collection<?>) value, depth, activeAncestors);
			}
			if (value.getClass().isArray()) {
				return convertArraySnapshot(value, depth, activeAncestors);
			}
			if (isBlockedInspectType(value.getClass())) {
				return "<opaque:" + value.getClass().getSimpleName() + ">";
			}
			return convertPojoSnapshot(value, depth, activeAncestors);
		} catch (Throwable e) {
			log.warn("转换模块字段快照异常 module={} valueType={}", this.getClass().getSimpleName(), value.getClass().getName(), e);
			return Map.of("_inspectError", formatInspectError(e));
		} finally {
			activeAncestors.remove(value);
		}
	}

	private Map<String, Object> convertMapSnapshot(Map<?, ?> map, int depth, IdentityHashMap<Object, Boolean> activeAncestors) {
		Map<String, Object> result = new LinkedHashMap<>();
		int count = 0;
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			if (count >= MAX_INSPECT_ELEMENTS) {
				result.put("_truncated", true);
				result.put("_totalSize", map.size());
				break;
			}
			String key = entry.getKey() == null ? "null" : String.valueOf(entry.getKey());
			try {
				result.put(key, toInspectValue(entry.getValue(), depth + 1, activeAncestors));
			} catch (Throwable e) {
				log.warn("转换Map快照元素异常 module={} key={}", this.getClass().getSimpleName(), key, e);
				result.put(key, Map.of("_inspectError", formatInspectError(e)));
			}
			count++;
		}
		return result;
	}

	private List<Object> convertCollectionSnapshot(Collection<?> collection, int depth,
			IdentityHashMap<Object, Boolean> activeAncestors) {
		List<Object> result = new ArrayList<>();
		int count = 0;
		for (Object item : collection) {
			if (count >= MAX_INSPECT_ELEMENTS) {
				result.add(Map.of("_truncated", true, "_totalSize", collection.size(), "_limit", MAX_INSPECT_ELEMENTS));
				break;
			}
			try {
				result.add(toInspectValue(item, depth + 1, activeAncestors));
			} catch (Throwable e) {
				log.warn("转换集合快照元素异常 module={} index={}", this.getClass().getSimpleName(), count, e);
				result.add(Map.of("_inspectError", formatInspectError(e)));
			}
			count++;
		}
		return result;
	}

	private List<Object> convertArraySnapshot(Object array, int depth, IdentityHashMap<Object, Boolean> activeAncestors) {
		int len = Array.getLength(array);
		int limit = Math.min(len, MAX_INSPECT_ELEMENTS);
		List<Object> result = new ArrayList<>(limit + (len > MAX_INSPECT_ELEMENTS ? 1 : 0));
		for (int i = 0; i < limit; i++) {
			try {
				result.add(toInspectValue(Array.get(array, i), depth + 1, activeAncestors));
			} catch (Throwable e) {
				log.warn("转换数组快照元素异常 module={} index={}", this.getClass().getSimpleName(), i, e);
				result.add(Map.of("_inspectError", formatInspectError(e)));
			}
		}
		if (len > MAX_INSPECT_ELEMENTS) {
			result.add(Map.of("_truncated", true, "_totalSize", len, "_limit", MAX_INSPECT_ELEMENTS));
		}
		return result;
	}

	private Map<String, Object> convertPojoSnapshot(Object pojo, int depth, IdentityHashMap<Object, Boolean> activeAncestors) {
		Map<String, Object> result = new LinkedHashMap<>();
		int fieldCount = 0;
		for (Class<?> cursor = pojo.getClass(); cursor != null && cursor != Object.class; cursor = cursor.getSuperclass()) {
			if (isBlockedInspectType(cursor)) {
				break;
			}
			Field[] fields = cursor.getDeclaredFields();
			for (Field field : fields) {
				int modifiers = field.getModifiers();
				if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) || field.isSynthetic()) {
					continue;
				}
				if (Player.class.isAssignableFrom(field.getType()) || Logger.class.isAssignableFrom(field.getType())) {
					continue;
				}
				if (result.containsKey(field.getName())) {
					continue;
				}
				if (fieldCount >= MAX_INSPECT_ELEMENTS) {
					result.put("_truncated", true);
					return result;
				}
				fieldCount++;
				try {
					field.setAccessible(true);
					Object fv = field.get(pojo);
					result.put(field.getName(), toInspectValue(fv, depth + 1, activeAncestors));
				} catch (Throwable e) {
					log.warn("读取对象快照字段异常 module={} type={} field={}", this.getClass().getSimpleName(),
							pojo.getClass().getSimpleName(), field.getName(), e);
					result.put(field.getName(), Map.of("_inspectError", formatInspectError(e)));
				}
			}
		}
		return result;
	}

	private static boolean isBlockedInspectType(Class<?> type) {
		String name = type.getName();
		for (String prefix : BLOCKED_INSPECT_PACKAGE_PREFIXES) {
			if (name.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	private static String formatInspectError(Throwable e) {
		String message = e.getMessage();
		return e.getClass().getSimpleName() + ": " + (message == null ? "" : message);
	}

	/**
	 * 非生产测试重置当前模块状态，清空容器、原子类归零并重置业务标量
	 *
	 * @return 重置后的最新模块状态快照
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> resetForTest() {
		if (cn.game.games.net.game.gm.GmSecurityPolicy.currentRunMode().isProduction()) {
			throw new cn.game.core.exception.LogicException(cn.game.protocol.manual.ErrorMsgEnum.production_gm_not_allow.ID, "生产环境禁止执行模块重置");
		}
		cn.game.games.net.game.helper.PlayerWriteGuard.Scope guard = null;
		if (player != null) {
			try {
				guard = cn.game.games.net.game.helper.PlayerWriteGuard.begin(player);
			} catch (Throwable e) {
				// 未在PlayerSaveCoordinator登记的轻量测试实例降级直接执行
			}
		}
		try (cn.game.games.net.game.helper.PlayerWriteGuard.Scope ignored = guard) {
			for (Class<?> clazz = this.getClass(); clazz != null && clazz != BasePlayerModule.class; clazz = clazz.getSuperclass()) {
				Field[] fields = clazz.getDeclaredFields();
				for (Field field : fields) {
					int modifiers = field.getModifiers();
					if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) || field.isSynthetic()) {
						continue;
					}
					Class<?> type = field.getType();
					if (Logger.class.isAssignableFrom(type) || Player.class.isAssignableFrom(type)
							|| PlayerEventHandler.class.isAssignableFrom(type)
							|| "cn.game.core.event.PlayerEventDispatcher".equals(type.getName())
							|| "cn.game.games.core.event.PlayerEventHandler".equals(type.getName())) {
						continue;
					}
					try {
						field.setAccessible(true);
						Object val = field.get(this);
						if (val != null) {
							if (val instanceof Collection<?> coll) {
								coll.clear();
							} else if (val instanceof Map<?, ?> map) {
								map.clear();
							} else if (val instanceof cn.game.util.MapWrapper mw) {
								mw.clear();
							} else if (val instanceof cn.game.util.IntMapWrapper imw) {
								imw.clear();
							} else if (val instanceof cn.game.util.FloatMapWrapper fmw) {
								fmw.clear();
							} else if (val instanceof cn.game.util.StringMapWrapper smw) {
								smw.clear();
							} else if (val instanceof java.util.concurrent.atomic.AtomicInteger ai) {
								ai.set(0);
							} else if (val instanceof java.util.concurrent.atomic.AtomicLong al) {
								al.set(0L);
							} else if (val instanceof AtomicBoolean ab) {
								ab.set(false);
							} else if (!Modifier.isFinal(modifiers)) {
								if (type == boolean.class || type == Boolean.class) {
									field.set(this, false);
								} else if (type == byte.class || type == Byte.class) {
									field.set(this, (byte) 0);
								} else if (type == short.class || type == Short.class) {
									field.set(this, (short) 0);
								} else if (type == int.class || type == Integer.class) {
									field.set(this, 0);
								} else if (type == long.class || type == Long.class) {
									field.set(this, 0L);
								} else if (type == float.class || type == Float.class) {
									field.set(this, 0.0f);
								} else if (type == double.class || type == Double.class) {
									field.set(this, 0.0d);
								} else if (type == char.class || type == Character.class) {
									field.set(this, (char) 0);
								} else {
									field.set(this, null);
								}
							}
						}
					} catch (Throwable e) {
						log.warn("resetForTest清理字段异常 class={} field={}", clazz.getSimpleName(), field.getName(), e);
					}
				}
			}
			init();
			initAfter();
			afterResetForTest();
			initFromDbAfter();
			Object inspect = inspectState();
			return inspect instanceof Map ? (Map<String, Object>) inspect : Collections.emptyMap();
		}
	}

	/**
	 * resetForTest 后置处理钩子，子类可覆写恢复默认容器或特殊索引
	 */
	protected void afterResetForTest() {
	}
}
