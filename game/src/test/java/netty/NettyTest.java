package netty;

import cn.game.games.core.netty.WebSocketServer;

public class NettyTest {

	public static void main(String[] args) throws Exception {
		WebSocketServer server = new WebSocketServer();
		server.setPORT(80);
		server.start();
	}
}
