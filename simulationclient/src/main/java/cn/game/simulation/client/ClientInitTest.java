package cn.game.simulation.client;

import java.net.URL;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.protobuf.Message;

import cn.game.protocol.protobuf.TestMsg.TestGmCmdRequest_6f000001;
import cn.game.util.SpringContextLoader;
import cn.game.util.log.LoggerManager;

/**    
 * 批量初始化一些测试账号，使用gm 的 init命令
 * 2025年10月23日 11:48:40
 * @author SYQ
 */
public class ClientInitTest {

	private static int idStart = 99998100 ; 
	private static int count = 10 ; 
	private static String serverId = "SYQ" ; 
//	private static String serverId = "game_test" ; 
//	private static String serverId = "xy_game_1" ; 
	private static String loginServerUrl =  "http://test:9390" ;
	// #西游QA外网
//	private static String loginServerUrl =  "https://partyqaloginml.changyou.com:9390" ;
	// 西游正式外网
//	private static String loginServerUrl =  "https://partyloginml.changyou.com:9390" ;
	
	private static boolean keepalive = true; 
	
	public static ConcurrentLinkedQueue<Client> clients = new ConcurrentLinkedQueue<Client>();
	
	public static void main(String args[]) throws Exception {
		ClientInitTest test = new ClientInitTest();
		test.start();
	}

	public void start() throws Exception {
		Client.exitOnClientClose = false;

		LoggerManager.init();
		
		URL resource = Thread.currentThread().getContextClassLoader().getResource("applicationContext-gameserver.xml");
		SpringContextLoader.loadWithFile(new String[] { resource.getPath()});
		if (keepalive) {
			startHeartbeat();
		}
		run();
	}

	public void run() throws Exception {
		for (int i = idStart; i < idStart + count; i++) {
			try {
				Client client = new Client(i + "","", serverId, "1.0.0.1");
				client.loginPassportProto(loginServerUrl);
				client.loginGateway("", 0, "");
				client.waitInit();
				client.sendProtocol(message());
				if (!keepalive) {
					client.close();
				}
				clients.add(client);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private Message message() {
		TestGmCmdRequest_6f000001.Builder builder = TestGmCmdRequest_6f000001.newBuilder(); 
		builder.setCmd("init"); 
		return builder.build();
	}

	private static void startHeartbeat() {
		Thread thread = new Thread(() -> {
			while (true) {
				try {
					for (Client client : clients) {
						client.heartbeat();
						Thread.sleep(1);
					}
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}
	
}
