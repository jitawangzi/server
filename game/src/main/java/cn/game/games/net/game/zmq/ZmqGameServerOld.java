package cn.game.games.net.game.zmq;

import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import cn.game.util.ByteHelp;

/**
 * @Description 逻辑服的阻塞操作就是操作数据库，所以可以分离出一个数据库Socket，
 *              通过sub套接字来接受回掉，完成剩下的操作，回调前的上下文保存在本线程。
 *              貌似不太适合。
 * 使用dealer来分配网关服消息，处理完毕返回，但是如果想在自定义线程发送消息，则很困难。
 * 想办法让zmq只处理通讯层，不干扰逻辑和架构。
 * @date 2017年3月29日 下午5:12:36
 * @author SYQ
 */
public class ZmqGameServerOld {

	private static ZContext ctx = new ZContext() ; 
	private static String dataServerAddr = "tcp://localhost:7890" ; 
	private static class GameWorker implements Runnable {

		@Override
		public void run() {
			Socket worker = ctx.createSocket(ZMQ.DEALER);
			worker.connect("tcp://localhost:3334");

			Socket sub = ctx.createSocket(ZMQ.SUB);
			sub.subscribe("".getBytes());
			sub.connect("tcp://localhost:6789");
			
			Socket pusher = ctx.createSocket(ZMQ.PUSH);
			pusher.connect(dataServerAddr);
			

			while (!Thread.currentThread().isInterrupted()) {

				PollItem[] items = { new PollItem(worker, ZMQ.Poller.POLLIN), new PollItem(sub, ZMQ.Poller.POLLIN), };
				if (ZMQ.poll(items, 1000) == -1)
					break; // Interrupted

				if (items[0].isReadable()) {

					ZMsg msg = ZMsg.recvMsg(worker);
					ZFrame addr = msg.pop();
					msg.pop();
					ZFrame dataFrame = msg.pop();

					ZMsg response = new ZMsg();
					response.add(addr);
					response.add("".getBytes());

					if (dataFrame == null || !dataFrame.hasData()) {
						System.out.println("gameServer收到空包，测试链接的");
						response.send(worker);
						continue;
					}
					System.out.println("worker : " + Thread.currentThread().getName() + "收到数据包："
							+ new String(dataFrame.getData()));

					response.add(ByteHelp.toByteArray(999));

					response.add("game worker resp");
					response.send(worker);

					System.out.println("worker : " + Thread.currentThread().getName() + "返回Gate");
				}
				if (items[1].isReadable()) {

					ZMsg msg = ZMsg.recvMsg(sub);
					System.out.println("worker " + Thread.currentThread().getName() + "收到返回结果： "
							+ new String(msg.getFirst().getData()));
					msg.destroy();

				}

			}
		}

	}

	public static void main(String args[]) throws Exception {

		String id = "A";
		Context context = ZMQ.context(1);

		Socket socket = context.socket(ZMQ.ROUTER);
		socket.setIdentity(id.getBytes());
		socket.bind("tcp://*:3333");

		Socket dealer = context.socket(ZMQ.DEALER);

		dealer.bind("tcp://*:3334");

		Thread.sleep(1000);

		new Thread(new GameWorker()).start();
		new Thread(new GameWorker()).start();
		new Thread(new GameWorker()).start();
		new Thread(new GameWorker()).start();
		new Thread(new GameWorker()).start();

		System.out.println("GameServer start up");
		ZMQ.proxy(socket, dealer, null);

	}

}
