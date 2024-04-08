package cn.game.games.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;

import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.base.DbEntity;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.db.DbTask;
import cn.game.util.SpringContextLoader;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

public class DAO {
	private static final Logger log = LoggerFactory.getLogger("dbLog");

	private static LinkedBlockingQueue<DbTask> dbTasksQueue = new LinkedBlockingQueue<DbTask>();
	private static volatile boolean pauseUpdateDb = false;

	public static Future<@Nullable Object> insert(Class<?> mapper, DbEntity arg) {
		return execute(mapper, MapperConstant.insert, arg);
	}

	public static Future<@Nullable Object> insertBatch(Class<?> mapper, List<? extends DbEntity> list) {
		return execute(mapper, MapperConstant.insertBatch, list);
	}

	public static Future<@Nullable Object> insertOrUpdate(Class<?> mapper, DbEntity arg) {
		return execute(mapper, MapperConstant.insertOrUpdate, arg);
	}

	public static Future<@Nullable Object> insertSelective(Class<?> mapper, DbEntity arg) {
		return execute(mapper, MapperConstant.insertSelective, arg);
	}

	/**
	 * @Description 更新整行数据,注意不包含blob字段
	 * @param mapper
	 * @param arg
	 * @return 
	 */
	public static Future<@Nullable Object> update(Class<?> mapper, DbEntity arg) {
		return execute(mapper, MapperConstant.updateByPrimaryKey, arg);

//		GameDataPush_7d00000a.Builder builder = GameDataPush_7d00000a.newBuilder();
//		builder.setMapperClass(mapper.getName());
//		builder.setMethod(MapperConstant.updateByPrimaryKey);
//		builder.setArg(UnsafeByteOperations.unsafeWrap(KryoUtils.serializeClassAndObject(arg)));
//		RocketMQRpcClient.send(GameServer.getInstance().getServerId(ServerType.Data), builder.build());

	}

	/**
	 * @Description 更新整行数据，包含blob字段。
	 * @param mapper
	 * @param arg
	 * @return 
	 */
	public static Future<@Nullable Object> updateWithBLOBs(Class<?> mapper, DbEntity arg) {
		return execute(mapper, MapperConstant.updateByPrimaryKeySelective, arg);
	}

	/**
	 * @Description 更新部分字段，可以包含blob
	 * @param mapper
	 * @param arg
	 * @return 
	 */
	public static Future<@Nullable Object> updateSelective(Class<?> mapper, DbEntity arg) {
		return execute(mapper, MapperConstant.updateByPrimaryKeySelective, arg);

	}

	public static Future<@Nullable Object> delete(Class<?> mapper, Object... arg) {
		return execute(mapper, MapperConstant.deleteByPrimaryKey, arg);
	}

	public static Future<@Nullable Object> deleteBatch(Class<?> mapper, List<? extends DbEntity> list) {
		return execute(mapper, MapperConstant.deleteBatch, list);
	}

	/**
	 * 同步执行数据库操作,少用
	 * @param mapper
	 * @param method
	 * @param arg
	 */
	public static Object executeSync(Class<?> mapper, String method, Object... args) {
//		DAO.execute(mapper, method, arg);
		return invoke(mapper, method, args);
	}

	/** 
	 * 一次性执行多个任务，注意这些任务都是在一个线程顺序执行的。 
	 * @param tasks
	 * @return
	 */
	public static Future<List<Object>> execute(List<DbTask> tasks) {
		Future<List<Object>> future = VxHolder.vertx.executeBlocking(promise -> {
			List<Object> ret = new ArrayList<>();
			for (DbTask dbTask : tasks) {
				Object result = invoke(dbTask.getMapper(), dbTask.getMethod(), dbTask.getArg());
				ret.add(result);
			}
			promise.complete(ret);
		}, false);
		return future;
	}

	public static Future<@Nullable Object> execute(Class<?> mapperClass, String method, Object... args) {
//		if (pauseUpdateDb) {
//			dbTasksQueue.add(new DbTask(mapperClass, method, args));
//			return Future.succeededFuture();
//		} else {
//			if (!dbTasksQueue.isEmpty()) {
//
//			}
//		}
		Future<@Nullable Object> future = VxHolder.vertx.executeBlocking(promise -> {
			Object result;
			try {
				result = invoke(mapperClass, method, args);
				promise.complete(result);
			} catch (Throwable e) {
				promise.fail(e);
			}
		}, false);
		future.onFailure(r -> {
			r.printStackTrace();
			log.error("db execute error", r);
		});
		return future;
	}

	public static Future<List<Object>> update(List<DbTask> tasks) {

//		if (pauseUpdateDb) {
//			dbTasksQueue.addAll(tasks);
//			return Future.succeededFuture();
//		}

		Future<List<Object>> future = VxHolder.vertx.executeBlocking(promise -> {
			List<Object> ret = new ArrayList<>();
			for (DbTask dbTask : tasks) {
				Object result = invoke(dbTask.getMapper(), dbTask.getMethod(), dbTask.getArg());
				ret.add(result);
			}
			promise.complete(ret);
		}, false);
		future.onFailure(r -> {
			r.printStackTrace();
			log.error("db update error", r);
		});
		return future;
	}

	public static Object invoke(Class<?> mapperClass, String method, Object... args) {
		Object targetObject = SpringContextLoader.getContext().getBean(mapperClass);
		Method method2 = MapperConstant.getMethod(mapperClass, method);
		Object result = ReflectionUtils.invokeMethod(method2, targetObject, args);
		return result;
	}

	public static void listenPauseUpdateDb() {

		com.ctrip.framework.apollo.Config config = ConfigService.getAppConfig();
		pauseUpdateDb = config.getBooleanProperty("pauseUpdateDb", false);
		config.addChangeListener(new ConfigChangeListener() {
			@Override
			public void onChange(ConfigChangeEvent changeEvent) {
				for (String key : changeEvent.changedKeys()) {
					ConfigChange change = changeEvent.getChange(key);
					if (key.equalsIgnoreCase("pauseUpdateDb")) {
						boolean pause = Boolean.parseBoolean(change.getNewValue());
						if (pause) {
							pauseUpdateDb = true;
						} else {
							// 清空队列任务
							saveAndClearCacheTask();
						}
						break;
					}
					System.out.println(String.format("Found change - key: %s, oldValue: %s, newValue: %s, changeType: %s",
									change.getPropertyName(), change.getOldValue(), change.getNewValue(),
									change.getChangeType()));
				}
			}
		});
	}

	private static void saveAndClearCacheTask() {
		DbTask task;
		List<Future> futures = new ArrayList<>();
		while ((task = dbTasksQueue.poll()) != null) {
			Future<@Nullable Object> updateFutrue = execute(task.getMapper(), task.getMethod(), task.getArg());
			futures.add(updateFutrue);
		}
		CompositeFuture.join(futures).onSuccess(r -> {
			pauseUpdateDb = false;
		}).onFailure(e -> {
			log.error("UpdateDb task error", e);
		});
	}

}
