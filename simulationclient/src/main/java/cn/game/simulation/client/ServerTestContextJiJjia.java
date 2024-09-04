package cn.game.simulation.client;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;

import cn.game.simulation.test.base.ServerTest;
import cn.game.simulation.util.EnvConfig;
import cn.game.util.SpringContextLoader;
import io.netty.util.concurrent.Promise;
import io.vertx.core.impl.ConcurrentHashSet;

public class ServerTestContextJiJjia {
	public static Logger logger = LoggerFactory.getLogger(ServerTestContextJiJjia.class);

	public static boolean init = false;

	public static String loginServerUrl;
	public static String gateServerIp;
	public static int gateServerPort;
	public static String passportUsername;
	public static String pwd;
	public static String logbackFile;
	public static String args;
	public static boolean login;
	public static String serverId;
	public static String version;

	public static int botIdStart;
	public static int botCount;
	public static int loginCountPerSecond;
	public static int sendInterval;

	public static Properties initialProp;

	public static void main(String args[]) throws Exception {
		test();
	}

	public static void test() throws Exception {
		int clientSize = 500;
		int packetPerClient = 10;

		init();
		Map<String, ServerTest> beans = SpringContextLoader.getContext().getBeansOfType(ServerTest.class, true, true);
//		String[] beanNamesForType = SpringContextLoader.getContext().getBeanNamesForType(ServerTest.class);
		final List<ServerTest> list = new ArrayList<>();
//		for (String name : beanNamesForType) {
//			list.add((ServerTest) SpringContextLoader.getContext().getBean(name));
//		}
		list.addAll(beans.values());
		ExecutorService executorService = Executors.newFixedThreadPool(100) ; 

//		final List<Client> clients = new ArrayList<>(clientSize);
		ConcurrentHashSet<Client> clients = new ConcurrentHashSet<Client>();

		final CountDownLatch latch = new CountDownLatch(clientSize);
		long createStart = System.currentTimeMillis() ; 
		for (int i = 0; i < clientSize; i++) {
			Thread.sleep(1);
			executorService.execute(() -> {

				try {
					Client client = new Client();
					client.loginPassport(loginServerUrl);
					client.loginGateway(gateServerIp, gateServerPort);

//					client.sendProtocolAfterInit(list.get(Rnd.nextInt(list.size())).getMessage(client));

//					synchronized (clients) {
//						clients.add(client);
//						latch.countDown();
//					}

					clients.add(client);
					latch.countDown();

				} catch (Exception e) {

					e.printStackTrace();

				}

			});

		}
		latch.await(120, TimeUnit.SECONDS);

		System.err.println(" 开始检查客户端是否都初始化好了");
		logger.warn(" 开始检查客户端是否都初始化好了");
		while (true) {
			boolean init = true;
			for (Client client : clients) {
				if (!client.getInit()) {
					init = false;
					break;
				}
			}
			if (init) {
				break;
			}

		}
		
		logger.warn("准备创建[{}]个客户端，耗时[{}]毫秒",clientSize,System.currentTimeMillis() - createStart);

		System.err.println("准备开始发包了 :" + clients.size());
		logger.warn("准备开始发包了,实际创建客户端数量[{}] " ,clients.size());
//		Thread.sleep(3000);

		long start = System.currentTimeMillis();
//		long second = 120 * 1000;
		
//		int sendCountAfterCreate = 0 ; 

//		while (true) {
//
//			for (Client client : clients) {
//
//				client.sendProtocolAfterInit(list.get(Rnd.nextInt(list.size())).getMessage(client));
//				Thread.sleep(1);
//				sendCountAfterCreate ++ ; 
//
//			}
//			if ((System.currentTimeMillis() - start) > second) {
//				break;
//			}
//			// Thread.sleep(50);
//		}
		
		int expectedSendCount = clients.size() * packetPerClient;
		for (int i = 0; i < packetPerClient; i++) {
			
			for (Client client : clients) {
				
//				client.sendProtocolAfterInit(list.get(Rnd.nextInt(list.size())).getMessage(client));
//				client.sendProtocolAfterInit(new ExploreEnterRequest_52000801Test().getMessage(client));
				Thread.sleep(1);
			}
		}
		long time = System.currentTimeMillis() - start ; 
		// 先简单等待返回包
		Thread.sleep(10000);

		int sendCount = 0 ; 
		int recvCount = 0 ; 
		for (Client client : clients) {
			sendCount += client.sendCount.get() ; 
			recvCount += client.recvCount.get() ; 
		}
		// 包含登录，创建角色
		logger.warn("实际总发包数量 :" + sendCount);
		// 包含resp、push
		logger.warn("实际总收包数量 :" + recvCount);
		
		logger.warn("创建角色数量[{}]后,预计发包数量[{}]发包持续时间[{}]毫秒",clients.size(),expectedSendCount,time);
		System.exit(0);

	}

	public static void send(Client client, Message message) throws URISyntaxException, InterruptedException, UnknownHostException, SSLException {

		if (login) {
			client.loginPassport(loginServerUrl);
			client.loginGateway(gateServerIp, gateServerPort);
//			client.sendProtocolAfterInit(message, true);
			client.sendProtocol(message);
		} else {
			Promise<Client> connect = client.connect(gateServerIp, gateServerPort, false);
			connect.addListener(r -> {
				client.sendProtocol(message);
			});
		}
	}

	public static void send(Client client, Supplier<Message> supplier) throws Exception {

		if (login) {
			client.loginPassportProto(loginServerUrl);
			client.loginGateway(gateServerIp, gateServerPort);
			client.sendProtocolAfterInit(supplier, true);
		} else {
			Promise<Client> connect = client.connect(gateServerIp, gateServerPort, false);
			connect.addListener(r -> {
				client.sendProtocol(supplier.get());
			});
		}
	}

	public static void sendMessages(Client client, Supplier<List<Message>> supplier)
			throws URISyntaxException, InterruptedException, UnknownHostException, SSLException {

		if (login) {
			client.loginPassport(loginServerUrl);
			client.loginGateway(gateServerIp, gateServerPort);
			List<Message> list = supplier.get();
			for (Message message : list) {
				client.sendProtocolAfterInit(message, true);
			}
		} else {
			Promise<Client> connect = client.connect(gateServerIp, gateServerPort, false);
			connect.addListener(r -> {
				List<Message> list = supplier.get();
				for (Message message : list) {
					client.sendProtocol(message);
				}
			});
		}
	}

	public static boolean init() throws Exception {
		if (init)
			return true;

		initialProp = new Properties();
		ClassLoader loader = Thread.currentThread().getClass().getClassLoader();
		if (loader == null) {
			loader = ServerTestContextJiJjia.class.getClassLoader();
		}
		InputStream inputStream = loader.getResourceAsStream("env.properties");
		initialProp.load(inputStream);
		inputStream.close();

		EnvConfig.init(initialProp);

		SpringContextLoader.main(new String[] { args });

		init = true;
		return true;
	}

}
