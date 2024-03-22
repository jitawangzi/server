package cn.game.games.net.data.remote;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;

import cn.game.core.task.TaskManager;
import cn.game.games.net.game.db.DbTask;
import cn.game.util.MailUtil;
import cn.game.util.SpringContextLoader;
import io.vertx.core.Future;
import io.vertx.core.Promise;

public class DataGameServerImpl implements DataGameServerInterface {

	private static final Logger log = LoggerFactory.getLogger("dbLog");
	/** 只序列化字段，不调用get()序列化 */
	private static final boolean fieldBased = true;
	private static transient SerializeConfig serializeConfig = new SerializeConfig(fieldBased);
	@Override
	public Object exec(Class<?> mapperClass, String method, Object args) {

		// StopWatch watch = new StopWatch();
		// watch.start();
		Object ret = null;
		long start = System.currentTimeMillis();
		long start1 = start;

		try {
			Object mapper = SpringContextLoader.getContext().getBean(mapperClass);
			if (args == null) {
				Method m = mapper.getClass().getDeclaredMethod(method);
				// dbPre = watch.getTime() ;
				// watch.stop();
				start1 = System.currentTimeMillis() - start1;
				ret = m.invoke(mapper);
			} else {
				if (args.getClass() == Object[].class) {
					Object[] objects = (Object[]) args;
					Class<?>[] cls = new Class[objects.length];
					for (int i = 0; i < cls.length; i++) {
						cls[i] = objects[i].getClass();
					}
					// TODO 缓存优化
					Method m = mapper.getClass().getDeclaredMethod(method, cls);
					// long elapsedMillis = watch.elapsedMillis();
					// log.debug("反射调用时间: "+elapsedMillis) ;
					// dbPre = watch.getTime() ;
					// watch.stop();
					start1 = System.currentTimeMillis() - start1;
					ret = m.invoke(mapper, objects);

				} else {
					Method m = mapper.getClass().getDeclaredMethod(method, args.getClass());
					// dbPre = watch.getTime() ;
					start1 = System.currentTimeMillis() - start1;
					// watch.stop();
					ret = m.invoke(mapper, args);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.error("调用mapper:" + mapperClass.getSimpleName() + " 异常Method:" + method + " args:"
					+ JSON.toJSONString(args, serializeConfig), e);

			TaskManager.getInstance().addBlockTask(() -> {
				try {
					MailUtil.reportException("数据库操作异常", ExceptionUtils.getFullStackTrace(e));
				} catch (UnsupportedEncodingException | MessagingException e1) {
					e1.printStackTrace();
				}
			});

			throw new RuntimeException(e);
		}
		// watch.stop();
		long time = System.currentTimeMillis() - start;
		log.debug("调用mapper[{}]method[{}]args[{}]dbPre[{}]ret[{}]timeConsuming[{}]", new Object[] {
				mapperClass.getSimpleName(), method, JSON.toJSONString(args, serializeConfig), start1, JSON.toJSONString(ret,
						serializeConfig), time });
		return ret;

	}

	@Override
	public Object execMutiTasks(List<DbTask> tasks) {
		long start = System.currentTimeMillis();

		List<Object> list = new ArrayList<>();
		for (DbTask dbTask : tasks) {
			list.add(exec(dbTask.getMapper(), dbTask.getMethod(), dbTask.getArg()));
		}
		log.info("muti task cost[{}] ms", System.currentTimeMillis() - start);

		return list;
	}

	@Override
	public void shutdown() {

		try {
			SpringContextLoader.close();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("spring close err :", e);
		}

		log.info("dataServer shutdown ");
	}

	@Override
	public Future<?> execAsync(Class<?> mapperClass, String method, Object args) {
		Promise<Object> promise = Promise.promise();
		promise.complete(exec(mapperClass, method, args));
		return promise.future();
	}

	@Override
	public Future<List<Object>> execAsync(List<DbTask> tasks) {
		Promise<List<Object>> promise = Promise.promise();
		List<Object> ret = new ArrayList<>();
		for (DbTask dbTask : tasks) {
			ret.add(exec(dbTask.getMapper(), dbTask.getMethod(), dbTask.getArg()));
		}
		promise.complete(ret);
		return promise.future();
	}
}
