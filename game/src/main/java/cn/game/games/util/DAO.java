package cn.game.games.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.base.DbEntity;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.db.DbTask;
import cn.game.util.JsonUtil;
import cn.game.util.SpringContextLoader;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;

/**    
 * 一些数据库操作的方便封装
 * 2024年4月8日 下午6:54:11
 * @author SYQ
 */
public class DAO {
//	private static final Logger log = LoggerFactory.getLogger("dbLog");
	private static final Logger log = LoggerFactory.getLogger("Db");
	// TODO 使用虚拟线程代替vertx worker线程。

	public static Future<@Nullable Object> insert(DbEntity arg) {
		arg.beforeSave();
		return execute(arg.getMapperClass(), MapperConstant.insert, arg);
	}

	public static Future<@Nullable Object> insertOrUpdate(DbEntity arg) {
		arg.beforeSave();
		return execute(arg.getMapperClass(), MapperConstant.insertOrUpdate, arg);
	}

	/** 
	 * 更新整行数据，包含blob字段
	 * @param arg
	 * @return
	 */
	public static Future<@Nullable Object> update(DbEntity arg) {
		return execute(arg.getMapperClass(), MapperConstant.updateByPrimaryKey, arg);
	}

	public static Future<@Nullable Object> delete(DbEntity arg) {

		Class<?> mapperClass = arg.getMapperClass();
		Object primaryKey = arg.primaryKey();
		if (primaryKey.getClass() == Object[].class) {
			return execute(mapperClass, MapperConstant.deleteByPrimaryKey, (Object[]) primaryKey);
		}
		return execute(mapperClass, MapperConstant.deleteByPrimaryKey, primaryKey);
	}


	public static Future<@Nullable Object> insertBatch(Class<?> mapper, List<? extends DbEntity> list) {
		for (DbEntity dbEntity : list) {
			dbEntity.beforeSave();
		}
		return execute(mapper, MapperConstant.insertBatch, list);
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
	public static <T> T executeSync(Class<?> mapper, String method, Object... args) {
		
		return invoke(mapper, method, args);
	}

	/**
	 * 同步执行数据库操作,少用
	 * @param blockingCode 
	 * @throws Exception 
	 */
	public static T executeSync(Callable<T> blockingCode) {
		try {
			return blockingCode.call();
		} catch (Exception e) {
			log.error("Error executing blocking code", e);
			throw new RuntimeException("Error executing blocking code", e);
		}
	}

	/** 
	 * 一次性执行多个任务，注意这些任务都是在一个线程顺序执行的。 
	 * @param tasks
	 * @return
	 */
	public static Future<List<Object>> executeDbTaskList(List<DbTask> tasks) {
		return VxHolder.executeBlockingWithTimeout(() -> {
			List<Object> ret = new ArrayList<>();
			for (DbTask dbTask : tasks) {
				Object result = invoke(dbTask.getMapper(), dbTask.getMethod(), dbTask.getArg());
				ret.add(result);
			}
			return ret;
		});

	}

	public static <T> Future<@Nullable T> execute(Class<?> mapperClass, String method, Object... args) {
		return VxHolder.executeBlockingWithTimeout(() -> (T) invoke(mapperClass, method, args));
	}

	public static <T> Future<@Nullable T> execute(Callable<T> blockingCode) {
		return VxHolder.executeBlockingWithTimeout(blockingCode);
	}

	public static <T> T invoke(Class<?> mapperClass, String method, Object... args) {
		if (log.isDebugEnabled()) {
			log.debug("execute db operation: mapperClass[{}]method[{}]args[{}]", mapperClass.getSimpleName(), method,
					JsonUtil.toJsonString(args));
		}
		long start = System.currentTimeMillis();
		Object targetObject = SpringContextLoader.getContext().getBean(mapperClass);
		Method method2 = MapperConstant.getMethod(mapperClass, method);
		Object result = ReflectionUtils.invokeMethod(method2, targetObject, args);
		long end = System.currentTimeMillis();
		if (end - start > 100) {
			log.warn("Slow DB operation: mapperClass[{}]method[{}]args[{}] took {} ms", mapperClass.getSimpleName(), method,
					JsonUtil.toJsonString(args), (end - start));
		}
		return (T)result;
	}
}
