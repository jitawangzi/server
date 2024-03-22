package cn.game.core.net.zero_rpc;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZLoop;
import org.zeromq.ZLoop.IZLoopHandler;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.alibaba.fastjson.JSON;

import cn.game.util.SpringContextLoader;


/**
 * @Description 服务端用pull接收任务，处理后发送给一个线程，pub
 * @date 2016年11月17日 上午10:06:17
 * @author SYQ
 * @param <T>
 */
@Deprecated
public class DbTaskReceiver<T> implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(DbTaskReceiver.class);

	private ZContext context;
	private Socket receiver;
	private String publishAddr;
	private String pullAddr;
	private ResultPublish publisher;

	private static ExecutorService executorService = Executors.newCachedThreadPool();

	
	public void init() {
		this.context = new ZContext();
		receiver = context.createSocket(ZMQ.PULL);
		receiver.bind(pullAddr);

		Thread thread = new Thread(this);
		thread.setName("zmq receiver");
		thread.start();

	}

	public DbTaskReceiver(String pullAddr, String pubAddr) {
		this.pullAddr = pullAddr;
		this.publishAddr = pubAddr;

	}

	class DataPushHandler implements IZLoopHandler {

		private Object invoke(String mapperClass, String method, Object args) {

			StopWatch watch = new StopWatch();
			watch.start();
			Class<?> clazz = null;
			Object ret = null;

			try {
				clazz = Class.forName(mapperClass);
				Object mapper = SpringContextLoader.getContext().getBean(clazz);
				System.out.println(clazz.getSimpleName());
				if (args == null) {
					Method m = mapper.getClass().getDeclaredMethod(method);
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
						ret = m.invoke(mapper, objects);

					} else {
						Method m = mapper.getClass().getDeclaredMethod(method, args.getClass());
						ret = m.invoke(mapper, args);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				log.error("调用mapper:" + clazz.getSimpleName() + "异常Method:" + method + 
						"args:" + JSON.toJSONString(args),e);
//				try {
//					MailUtil.reportException(e.getMessage());
//				} catch (MessagingException e1) {
//					e1.printStackTrace();
//				}

				throw new RuntimeException(e.getMessage(), e.getCause());
			}
			watch.stop();
			log.debug("调用mapper[{}]method[{}]args[{}]timeConsuming[{}]ret[{}]", new Object[] { clazz.getSimpleName(),
					method, JSON.toJSONString(args), watch.getTime(), JSON.toJSONString(ret) });
			return ret;

		}

		@Override
		public int handle(ZLoop loop, PollItem item, Object arg) {
			final ZMsg recvMsg = ZMsg.recvMsg(item.getSocket());

			// 另起线程执行操作
			executorService.submit(new Runnable() {

				@Override
				public void run() {
					try {
						ZFrame idFrame = recvMsg.poll();
						
//						String mapperClass = new String(recvMsg.poll().getData());
//						String method = new String(recvMsg.poll().getData());
//						Object args = SerializationUtils.deserialize(recvMsg.poll().getData());
//						// log.debug("传递过来的方法： " + command.getMethodName());
//						Object object = invoke(mapperClass, method, args);

						
						Serializable result = null ; 
						int size = recvMsg.size() ; 
						// 一个任务是4帧数据
						if (size>4) {// 多任务
							result = new ArrayList<>() ; 
							ZFrame mapperFrame = recvMsg.poll() ; 
							while(mapperFrame!=null&&mapperFrame.hasData()){
								String mapperClass = new String(mapperFrame.getData()) ; 
								String method = new String(recvMsg.poll().getData());

								Object args = org.apache.commons.lang3.SerializationUtils.deserialize(recvMsg.poll().getData());
								Object object = invoke(mapperClass, method, args);
								((ArrayList)result).add(object) ; 
								mapperFrame = recvMsg.poll() ;
							}
							
						}else {
							
							String mapperClass = new String(recvMsg.poll().getData());
							String method = new String(recvMsg.poll().getData());
							Object args = SerializationUtils.deserialize(recvMsg.poll().getData());
							result = (Serializable) invoke(mapperClass, method, args);
						}
						
						if (idFrame != null && idFrame.hasData()) {

							ZMsg msg = new ZMsg();
							msg.add(idFrame);
							msg.add(SerializationUtils.serialize(result));
							publisher.addResult(msg);
						}

					} catch (Throwable e) {
						log.error("", e);
						e.printStackTrace();
					}
				}
			});

			return 0;

		}

	}

	public void start() {

		this.publisher = new ResultPublish();
		publisher.bind(context, publishAddr);
		new Thread(publisher, "zmq-pub").start();

		ZLoop zLoop = new ZLoop();
		PollItem item = new PollItem(receiver, Poller.POLLIN);
		DataPushHandler handler = new DataPushHandler();
		zLoop.addPoller(item, handler, null);
		zLoop.start();

	}

	private static class ResultPublish implements Runnable {

		private static final ResultPublish instance = new ResultPublish();
		private Socket pusher;

		public void addResult(ZMsg msg) {
			this.queue.add(msg);
		}

		private ResultPublish() {
		};

		private BlockingQueue<ZMsg> queue = new LinkedBlockingQueue<ZMsg>();

		public void bind(ZContext context, String address) {
			pusher = context.createSocket(ZMQ.PUB);
			pusher.bind(address);

		}

		@Override
		public void run() {

			while (!Thread.currentThread().isInterrupted()) {
				ZMsg poll = null;
				try {
					poll = this.queue.poll(10, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (poll != null) {
					poll.send(pusher);
				}
			}
		}

	}

	@Override
	public void run() {
		start();
	}

}
