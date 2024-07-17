package cn.game.games.net.game.db;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import cn.game.core.net.transport.Command;
import cn.game.core.net.transport.Result;
import cn.game.core.net.zero_rpc.RemoteCallManager;
import cn.game.util.ByteHelp;
import cn.game.util.KryoUtils;

/**   
 * 执行数据库任务
 * 2017年4月14日 下午3:39:32
 * @author SYQ
 */
public class DbTaskManager implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(DbTaskManager.class);

	public static final DbTaskManager instance = new DbTaskManager();
	private volatile boolean shutdown;
	private LinkedBlockingQueue<ZMsg> tasks = new LinkedBlockingQueue<ZMsg>();
	private ZContext context;
	private String addr;

	public static DbTaskManager getInstance() {
		return instance;
	}

	private DbTaskManager() {

	}

	public DbTaskManager set(ZContext context, String addr) {
		this.context = context;
		this.addr = addr;
		return this;
	}

	/**
	 * 
	 * @param mapperClass Mapper接口
	 * @param method	接口中的方法
	 * @param args		方法参数
	 * @param caller	回掉对象（异步操作数据库返回结果后，还有后续逻辑的）
	 * @param callbackMothed	回掉方法
	 * @param sync	是否阻塞调用，一般为false
	 * @return
	 */
	public <T> T addTask(Class<?> mapperClass, String method, Serializable args, Object caller, String callbackMothed, boolean sync) {

		ZMsg msg = new ZMsg();

		msg.add(mapperClass.getName());
		msg.add(method);
		msg.add(KryoUtils.serialize(args));

		return sendTask(msg, caller, callbackMothed, sync);

	}

	/**
	 * 
	 * @param methodName 方法名
	 * @param clazz	方法参数类型
	 * @param args	方法参数
	 * @param caller  回掉对象
	 * @param callbackMothed  回掉方法
	 * @param sync
	 * @return
	 */
	public <T> T addTask(String methodName, Class<?>[] clazz, Object[] args, Object caller, String callbackMothed, boolean sync) {

		ZMsg msg = new ZMsg();
		Command command = new Command(methodName, clazz, args);
		msg.add(KryoUtils.serialize(command));

		return sendTask(msg, caller, callbackMothed, sync);

	}

	public <T> T addMultiTask(List<DbTask> tasks, Object caller, String callbackMothed, boolean sync) {

		ZMsg msg = new ZMsg();

		for (DbTask task : tasks) {
			msg.add(task.getMapper().getName());
			msg.add(task.getMethod());
			msg.add(KryoUtils.serialize((Serializable) task.getArg()));
		}
		return sendTask(msg, caller, callbackMothed, sync);
	}

	/**
	 * 废弃了
	 * @param msg
	 * @param caller
	 * @param callbackMothed
	 * @param sync
	 * @return
	 */
	private <T> T sendTask(ZMsg msg, Object caller, String callbackMothed, boolean sync) {

		int id = 0;
		if (sync) {

			id = RemoteCallManager.getInstance().getId();
//			RemoteCallManager.getInstance().regRemoteCall(id, Thread.currentThread());
			msg.addFirst(ByteHelp.toByteArray(id));

		} else {
			if (caller == null) {
				msg.addFirst("");

			} else {
				id = RemoteCallManager.getInstance().getId();
				msg.addFirst(ByteHelp.toByteArray(id));
				RemoteCallManager.getInstance().regRemoteCallback(id, null);

			}

		}

		try {
			this.tasks.put(msg);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (caller != null || !sync) {
			return null;
		}

		LockSupport.parkNanos(5000000000L);

		byte[] result = null;
		if (result == null) {
			log.error("远程调用无返回结果： Zmsg[{}]", msg.toString());
			return null;
		}
		Result t = KryoUtils.deserialize(result, Result.class);

		return (T) t.getResult();

	}

	/**
	 * 默认的非阻塞无回掉方法的数据库任务
	 * @param mapperClass
	 * @param method
	 * @param args
	 * @return
	 */
	public <T> T addTask(Class<?> mapperClass, String method, Serializable args) {

		return addTask(mapperClass, method, args, null, null, false);
	}

	/**
	 * 无回调，可选择同步或异步的数据库任务
	 * @param mapperClass
	 * @param method
	 * @param args
	 * @param sync
	 * @return
	 */
	public <T> T addTask(Class<?> mapperClass, String method, Serializable args, boolean sync) {

		return addTask(mapperClass, method, args, null, null, sync);
	}

	public void shutdown() {
		this.shutdown = true;
	}

	@Override
	public void run() {

		Socket pusher = context.createSocket(ZMQ.PUSH);
		pusher.connect(addr);

		while (!shutdown) {

			try {

				ZMsg msg = tasks.poll(1000, TimeUnit.MILLISECONDS);
				if (msg == null) {
					continue;
				}
				msg.send(pusher);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

}
