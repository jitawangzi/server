package cn.game.core.net.zero_rpc;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

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
import org.zeromq.ZMsg;

import cn.game.core.net.process.Processor;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.core.net.transport.Result;
import cn.game.core.task.TaskManager;
import cn.game.util.ByteHelp;
import cn.game.util.KryoUtils;

public class RemoteCallManager implements Runnable {
	private static Logger log = LoggerFactory.getLogger(RemoteCallManager.class);

	private ZContext context;
	private ZMQ.Socket receiver;
	private ZMQ.Socket router;
	private String[] subAddr;
	private String serverId;
	private String routerAddr;

	private Processor processor;

	private static RemoteCallManager instance = new RemoteCallManager();

	private Map<Integer, CompletableFuture<byte[]>> waitingFutures = new ConcurrentHashMap<>();
	private Map<Integer, Consumer<?>> cbs = new ConcurrentHashMap<>();

	private AtomicInteger id = new AtomicInteger(1);

	public static RemoteCallManager getInstance() {
		return instance;
	}

	public void set(ZContext context, String subAddr, String routerAddr, String serverId, Processor processor) {
		this.context = context;
		if (!StringUtils.isEmpty(subAddr)) {
			this.subAddr = subAddr.split("\\|");
		}
		this.routerAddr = routerAddr;
		this.serverId = serverId;
		this.processor = processor;
	}

	private RemoteCallManager() {
	};

	public void connect() {

		if (subAddr != null) {
			this.receiver = context.createSocket(ZMQ.SUB);
			this.receiver.subscribe(serverId.getBytes());
			this.receiver.subscribe("all".getBytes());
			for (String addr : subAddr) {
				receiver.connect(addr);
			}
		}
		if (!StringUtils.isEmpty(routerAddr)) {
			this.router = context.createSocket(ZMQ.ROUTER);
			this.router.setIdentity(serverId.getBytes());
			this.router.bind(routerAddr);
		}
	}

	public int getId() {
		return id.getAndIncrement();
	}

	public void regRemoteCall(int id, CompletableFuture<byte[]> object) {
		
		waitingFutures.put(id, object);
	}

	public void regRemoteCallback(int id, Consumer callBackTask) {

		cbs.putIfAbsent(id, callBackTask);
	}

	@Override
	public void run() {

		connect();
		ResultHandler handler = new ResultHandler();

		ZLoop zLoop = new ZLoop();
		if (receiver != null) {
			PollItem poolItem = new PollItem(receiver, Poller.POLLIN);
			zLoop.addPoller(poolItem, handler, null);
		}
		if (router != null) {
			PollItem poolItem2 = new PollItem(router, Poller.POLLIN);
			zLoop.addPoller(poolItem2, handler, null);
		}
		zLoop.start();

	}

	class ResultHandler implements IZLoopHandler {

		@Override
		public int handle(ZLoop loop, PollItem item, Object arg) {

//			System.err.println("收到返回的结果了");
			ZMsg msg = ZMsg.recvMsg(item.getSocket());
			if (log.isDebugEnabled()) {
				log.debug("RemoteCallManager  Socket type [{}] receive msg [{}]", item.getSocket().getType(), msg);
			}
			ZFrame myServerAddr = msg.poll();
			ZFrame remoteServerAddr = msg.poll();
			ZFrame typeFrame = msg.poll();
			ZFrame idFrame = msg.poll(); // 回调的id，或者消息id
			if (idFrame == null) { 
				return 0; 
			}

			ZFrame dataFrame = msg.poll();
			if (dataFrame == null) { 
				return 0;
			}
			byte msgType = typeFrame.getData()[0];
			try {

				if (msgType == 1) { // 处理远程调用返回的消息
					int id = ByteHelp.makeInt(idFrame.getData());
					CompletableFuture<byte[]> future = RemoteCallManager.getInstance().waitingFutures.remove(id);
					if (future != null) {// 同步等待的消息返回了，线程继续执行
						future.complete(dataFrame.getData());
					} else { // 执行异步回调
						Consumer cb = RemoteCallManager.getInstance().cbs.remove(id);
						if (cb != null) {

							TaskManager.getInstance().addMainTask(() -> {
								cb.accept(KryoUtils.deserialize(dataFrame.getData(), Result.class).getResult());
							});
						} else {
							// 可能调用方的server id 配置错误，弄成相同的了 
							log.warn("远程回调方法不存在,id:" + id);
						}
					}
				} else if (msgType == 0) { // 单纯发布订阅的消息
					int id = ByteHelp.makeInt(idFrame.getData());
					ProtobufProtocol protocol = new ProtobufProtocol();
//					protocol.setMsgID(id);
//					protocol.setData(dataFrame.getData());
					processor.process(null, protocol);
				} else if (msgType == 2) { // 同步消息
					int id = ByteHelp.makeInt(idFrame.getData());
					CompletableFuture<byte[]> future = RemoteCallManager.getInstance().waitingFutures.get(id);
					if (future != null) {// 同步等待的消息返回了，线程继续执行
						future.complete(dataFrame.getData());
					}
				} else if (msgType == 99) { // 发布的心跳消息,先按这个类型来处理，不按消息id分发了
					byte serverType = dataFrame.getData()[0];
//					processor.process(null, protocol);
				}
			} catch (Exception e) {
				log.error("远程回调方法出现异常： ", e);
			}
			return 0;
		}

	}

}
