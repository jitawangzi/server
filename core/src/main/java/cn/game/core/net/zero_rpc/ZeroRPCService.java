package cn.game.core.net.zero_rpc;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
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

import com.google.common.util.concurrent.MoreExecutors;

import cn.game.core.net.process.Processor;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.core.net.transport.Command;
import cn.game.core.net.transport.Result;
import cn.game.util.ByteHelp;
import cn.game.util.KryoUtils;

/**
 * @Description 服务端用pull接收任务，处理后发送给一个线程，pub
 * 2016年11月17日 上午10:06:17
 * @author SYQ
 * @param <T>
 */
public class ZeroRPCService<T> implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(ZeroRPCService.class);

	private T wrappedService;
	private ZContext context;
	private Socket receiver;
//	private Socket sender;
	private String pubAddr;
	private String pullAddr;
	private String serverId;
	private String subAddr;
	private String routerAddr;
//	private static ExecutorService executorService = Executors.newCachedThreadPool();
	private static ExecutorService executorService = Executors.newFixedThreadPool(100);
	private Processor processor;

	private ZmqSender zmqSender;


	public ZeroRPCService(T wrappedService) {
		this.wrappedService = wrappedService;
	}

	public void init() {
		this.context = new ZContext();

		if (!StringUtils.isEmpty(pubAddr)) {
//			ZmqSender.getInstance().bind(context, pubAddr);
//			zmqSender = new ZmqSender();
			new Thread(zmqSender.bind(context, pubAddr), "zmq-pub").start();
			log.info("ZeroRPCService pub  bind addr【{}】,serverId [{}] ", pubAddr, serverId);
		}

		if (!StringUtils.isEmpty(pullAddr)) {
			receiver = context.createSocket(ZMQ.PULL);
			receiver.bind(pullAddr);
			log.info("ZeroRPCService pull  bind addr【{}】,serverId [{}] ", pullAddr, serverId);
		}

		if (!StringUtils.isEmpty(subAddr)) {
			receiver = context.createSocket(ZMQ.SUB);
			this.receiver.subscribe(serverId.getBytes());
			this.receiver.subscribe("all".getBytes());
			receiver.connect(subAddr);
			log.info("ZeroRPCService sub addr【{}】,serverId [{}] ", subAddr, serverId);
		}
		if (!StringUtils.isEmpty(routerAddr)) {
//			ZmqSender.getInstance().connect(context, routerAddr);

//			zmqSender = new ZmqSender();
			new Thread(zmqSender.connect(context, routerAddr), "zmq-router").start();
//			new Thread(ZmqSender.getInstance(), "zmq-router").start();
			log.info("ZeroRPCService router addr【{}】,serverId [{}] ", routerAddr, serverId);
		}

//		TaskManager.getInstance().scheduleGeneral(() -> {
//			ZMsg zMsg = new ZMsg();
//			zMsg.add("");
//			zMsg.add(new byte[] { 99 });
//			zMsg.add(new byte[] { serverType });
//			zMsg.add(pullAddr);
//			ZeroPublisher.getInstance().pubMessage(zMsg);
//
//		}, Config.heart);

		Thread thread = new Thread(this);
		thread.setName("zmq receiver");
		thread.start();
	}

	public ZeroRPCService(String pullAddr, String pubAddr) {
		this.pullAddr = pullAddr;
		this.pubAddr = pubAddr;

	}
	public ZeroRPCService(T wrappedService, String pullAddr, String pubAddr, String serverId, Processor processor,
			ZmqSender zmqSender) {
		this.wrappedService = wrappedService;
		this.pullAddr = pullAddr;
		this.pubAddr = pubAddr;
		this.serverId = serverId;
		this.processor = processor;
		this.zmqSender = zmqSender;
	}
	public ZeroRPCService(T wrappedService, String subAddr, String routerAddr, String serverId, Processor processor, Boolean game,
			ZmqSender zmqSender) {
		this.wrappedService = wrappedService;
		this.subAddr = subAddr;
		this.routerAddr = routerAddr;
		this.serverId = serverId;
		this.processor = processor;
		this.zmqSender = zmqSender;
	}

	class DataPushHandler implements IZLoopHandler {

		private Object invoke(Command command) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

			if (command.getArgs() != null && command.getArgs().length > 0) {
				Method m = wrappedService.getClass().getMethod(command.getMethodName(), command.getClazz());
				return m.invoke(wrappedService, command.getArgs());
			} else {
				Method m = wrappedService.getClass().getMethod(command.getMethodName());
				return m.invoke(wrappedService);
			}
		}

		@Override
		public int handle(ZLoop loop, PollItem item, Object arg) {
			final ZMsg recvMsg = ZMsg.recvMsg(item.getSocket());

			if (log.isDebugEnabled()) {
				log.debug("ZeroRPCService  Socket type [{}] receive msg [{}]", item.getSocket().getType(), recvMsg);
			}
			// 另起线程执行操作
			executorService.submit(() -> {

				try {

					ZFrame recvServerIdFrame = recvMsg.poll();
					ZFrame sendServerIdFrame = recvMsg.poll();
					ZFrame typeFrame = recvMsg.poll();
					ZFrame idFrame = recvMsg.poll();
					ZFrame dataFrame = recvMsg.poll();

					if (typeFrame.getData()[0] == ZeroMQRpcClient.PUSH_SUB[0]) {
						Serializable result = null;
						try {
							Command command = KryoUtils.deserialize(dataFrame.getData(), Command.class);
							result = (Serializable) invoke(command);

						} catch (Throwable e) {
							log.error("", e);
							e.printStackTrace();
							result = e;
						}

						if (idFrame != null && idFrame.getData().length > 0) {

							Result resp = new Result(result);
							ZMsg msg = new ZMsg();
							msg.add(sendServerIdFrame);
							msg.add(recvServerIdFrame);
							msg.add(ZeroMQRpcClient.PUSH_SUB);
							msg.add(idFrame);
							msg.add(KryoUtils.serialize(resp));
							zmqSender.pubMessage(msg);
						}
					} else if (typeFrame.getData()[0] == ZeroMQRpcClient.PUB_SUB[0]) {
						// 接收的消息，不返回
						int id = ByteHelp.makeInt(idFrame.getData());
						ProtobufProtocol protocol = new ProtobufProtocol();
//						protocol.setMsgID(id);
//						protocol.setData(dataFrame.getData());

						processor.process(null, protocol);

					}

				} catch (Exception e) {
					log.error("ZeroRPCService error : ", e);
				}

			});

			return 0;

		}

	}

	@Override
	public void run() {

		ZLoop zLoop = new ZLoop();
		PollItem item = new PollItem(receiver, Poller.POLLIN);
		DataPushHandler handler = new DataPushHandler();
		zLoop.addPoller(item, handler, null);
		zLoop.start();

	}
	
	public boolean shutdown() {
		
		log.info("{} start shutdown.... ",this.getClass().getSimpleName());
		context.close();  
		boolean ret = MoreExecutors.shutdownAndAwaitTermination(executorService, 60, TimeUnit.SECONDS);
		log.info("Thread[{}]  Class[{}] shutdown ret[{}]", Thread.currentThread().getName(), this.getClass().getSimpleName(), ret);
		
		return ret;
	}

}
