package cn.game.games.net.game.zmq;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.Socket;


public class ZmqPairSender implements Runnable{
	
	private ZContext context ; 
	private String addr ; 
	private BlockingQueue<ZMsg> queue = new LinkedBlockingDeque<ZMsg>();
	
	private static ZmqPairSender instance = new ZmqPairSender() ; 
	
	public static ZmqPairSender getInstance(){
		return instance ; 
	}
	public ZmqPairSender init (ZContext context,String addr) {
		this.context = context; 
		this.addr = addr ; 
		return this ; 
	}
	private  ZmqPairSender() {
	}
    public void put(ZMsg msg) throws InterruptedException{
        queue.put(msg);
    }
  
	  
	@Override
	public void run() {

		Socket pair = context.createSocket(ZMQ.PAIR); 
		pair.connect(addr);
		
		while (!Thread.currentThread().isInterrupted()) {
			ZMsg poll = null;
			try {
				poll = this.queue.poll(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();  
			}
			if (poll != null) {
				poll.send(pair);
			}
		}
		
	}

}
