package cn.game.core.net.zero_rpc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.alibaba.fastjson.JSON;

import cn.game.core.net.rpc.RpcClient;
import cn.game.core.net.transport.Command;
import cn.game.core.net.transport.Result;
import cn.game.util.ByteHelp;
import cn.game.util.Config;
import cn.game.util.KryoUtils;
import cn.game.util.MailUtil;
import cn.game.util.ServerType;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;

@Deprecated
public class ZeroMQRpcClient implements Runnable, RpcClient {

	private static final Logger log = LoggerFactory.getLogger(ZeroMQRpcClient.class);

	public static final byte[] PUB_SUB = new byte[] { 0 };
	public static final byte[] PUSH_SUB = new byte[] { 1 };
	public static final byte[] REQ_RESP = new byte[] { 2 };

	private volatile boolean shutdown;
	private LinkedBlockingDeque<ZMsg> tasks = new LinkedBlockingDeque<ZMsg>();
	private ZContext context;
	private String destAddr[];
	private String pubAddr;
	/** 发送方服务器id */
	private String serverId;

	/** 接收方服务器id */
	private String recvServerId;

	public ZeroMQRpcClient(ZContext context, String destAddr, String pubAddr, String serverId) {
		this.context = context;
		if (destAddr != null) {
			this.destAddr = destAddr.split("\\|");
		}
		this.pubAddr = pubAddr;
		this.serverId = serverId;
	}

	/**
	 * @Description
	 * @param methodName方法名
	 * @param clazz方法参数类型
	 * @param returnType
	 *            方法返回类型
	 * @param arg方法参数
	 * @param caller回掉对象
	 * @param callbackMothed回掉方法
	 * @param sync,是否同步
	 * @return
	 */
//	public <T> T invoke(String methodName, Class<?>[] clazz, Class<?> returnType, Object[] args, Consumer caller, boolean sync,
//			String serverId) {
//		ZMsg msg = new ZMsg();
//		Command command = new Command(methodName, clazz, args);
//		msg.add(KryoUtils.serialize(command));
//
//		// if (returnType.equals(void.class)) {
//		// sync = false;
//		// }
//		return send(msg, caller, sync, serverId);
//
//	}

	/**
	 * 给指定服务器发送消息
	 * @param message
	 * @param serverId
	 */
	public void sendToRemoteServer(String serverId, int msgId, byte[] byteArray) {
		ZMsg msg = new ZMsg();
		msg.add(serverId);
		msg.add(this.serverId);
		msg.add(PUB_SUB);
		msg.add(ByteHelp.toByteArray(msgId));
		msg.add(byteArray);

		putMessage(msg);
	}

	public void putMessage(ZMsg msg) {
		try {
			this.tasks.put(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private <T> T send(ZMsg msg, Consumer callBackTask, boolean sync, String serverId) {

		int id = 0;
		if (sync) {

			id = RemoteCallManager.getInstance().getId();
			msg.addFirst(ByteHelp.toByteArray(id));

		} else {
			if (callBackTask == null) {
				msg.addFirst("");
			} else {

				id = RemoteCallManager.getInstance().getId();
				msg.addFirst(ByteHelp.toByteArray(id));
				RemoteCallManager.getInstance().regRemoteCallback(id, callBackTask);
			}

		}
		msg.addFirst(PUSH_SUB);
		msg.addFirst(this.serverId);
		msg.addFirst(serverId == null ? "" : serverId);

		if (callBackTask != null || !sync) { 
			putMessage(msg);
			return null; 
		}

		CompletableFuture<byte[]> future = new CompletableFuture<byte[]>();
		RemoteCallManager.getInstance().regRemoteCall(id, future);
		putMessage(msg);
		byte[] result = null;
		try {
			// 默认等待5秒
			result = future.get(Config.remoteCallTimeOut, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
			log.error("", e);
		}
		if (result == null) {
			Object[] array = msg.toArray();
			ZFrame removeLast = (ZFrame) array[4];
			ZFrame idFrame = (ZFrame) array[3];
			Command command = KryoUtils.deserialize(removeLast.getData(), Command.class);
			log.error("远程调用无返回结果： Zmsg command[{}] id[{}] thread[{}]", JSON.toJSONString(command), ByteHelp.makeInt(idFrame.getData()),
					Thread.currentThread().getName());
			try {
				MailUtil.reportException("远程调用无返回结果 : Zmsg command[{}]", JSON.toJSONString(command));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;

		}
		Result deserialize = KryoUtils.deserialize(result, Result.class);
		Object result2 = deserialize.getResult();
		if (result2 instanceof Throwable)
			throw new RuntimeException((Throwable) result2);
		return (T) result2;

	}

	public void shutdown() {
		this.shutdown = true;
	}

	@Override
	public void run() {

		Socket pusher = null;
		if (destAddr != null) {
			pusher = context.createSocket(ZMQ.PUSH);
			for (String addr : destAddr) {
				pusher.connect(addr);
			}
		}
		if (pubAddr != null) {
			pusher = context.createSocket(ZMQ.PUB);
			pusher.bind(pubAddr);
		}

		ZMsg msg = null;
		while (!shutdown) {
			try {
				msg = tasks.poll(5000, TimeUnit.MILLISECONDS);
				if (msg != null) {
					msg.send(pusher);
					if (log.isDebugEnabled()) {
						log.debug("ZeroRPCClient  Socket type [{}] send msg [{}]", pusher.getType(), msg);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public <ZMsg> void send(ZMsg message) {
	}
	@Override
	public boolean checkAllowSync() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> void request(String serverId, T message, Handler<AsyncResult<Message<T>>> replyHandler) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> Future<Message<T>> request(String serverId, T message) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Future<Message<T>> request(ServerType serverType, T message) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void broadcast(ServerType serverType, T message) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void send(String serverId, T message) {
		// TODO Auto-generated method stub

	}

}
