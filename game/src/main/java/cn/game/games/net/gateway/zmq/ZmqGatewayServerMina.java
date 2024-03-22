package cn.game.games.net.gateway.zmq;

import java.util.Random;

import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class ZmqGatewayServerMina {

	
	public static void main(String args[]) throws Exception {

		Random random = new Random(47);

		Context context = ZMQ.context(1);
		Socket socket = context.socket(ZMQ.ROUTER);
		Socket pub = context.socket(ZMQ.PUB);
		socket.bind("tcp://*:3333");
		pub.bind("tcp://*:6789");

		Thread.sleep(3000);

		long start = System.currentTimeMillis(); 
		
		for (;;) {

			if (random.nextBoolean()) {
				socket.sendMore("A");
				socket.sendMore("");
				socket.send("A msg");

			} else {

				socket.sendMore("B");
				socket.sendMore("");
				socket.send("B msg");

			}
			
			ZMsg msg = ZMsg.recvMsg(socket,1) ; 
			if (msg!=null) {
//				msg.pop() ; 
//				msg.pop() ; 
//				ZFrame pop = msg.pop(); 
				
				System.out.println("���ط��յ����ذ�msg�� "+msg);
			}
			

			// socket.send("") ;
			Thread.sleep(5000);

		}
		// socket.sendMore("A") ;
		// socket.send("stop") ;
		// socket.send("") ;

		// socket.close();
		// context.term();

	}
}
