package cn.game.simulation.client;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import javax.net.ssl.SSLException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;

import cn.game.core.net.pressure.GlobalMessageStatistics;
import cn.game.protocol.generated.helper.ManagerHelper;
import cn.game.protocol.protobuf.PlayerMsg.PlayerLogoutRequest_01000003;
import cn.game.simulation.test.base.ServerTest;
import cn.game.simulation.util.CSVMessagesReader;
import cn.game.util.Config;
import cn.game.util.SpringContextLoader;
import cn.game.util.log.LoggerManager;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Promise;

public class ServerTestContext {
	static {
		try {
			LoggerManager.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static Logger logger = LoggerFactory.getLogger(ServerTestContext.class);
	public static Logger netLogger = LoggerFactory.getLogger("Net");
	public static String loginServerUrl;
	public static String gateServerIp;
	public static int gateServerPort;
	public static String passportUsername;
	public static String pwd;
	public static String logbackFile;
	public static String args;
	public static boolean login;
	public static boolean initLogback;
	public static String serverId;
	public static String version;

	public static int botIdStart;
	public static int botCount;
	public static int loginInterval;
	public static int sendInterval;
	public static int messageStatisticsInterval;
	public static int botSendInterval;
	public static int botRunTimeMax;

	public static boolean init = false;

	public static volatile boolean run = true;

	public static ConcurrentLinkedQueue<Client> clients = new ConcurrentLinkedQueue<Client>();

	private static List<String> sourceIps;

	public static void main(String args[]) throws Exception {
		CSVMessagesReader.read();
		ManagerHelper.init();
		init();
		initEnvFromArgs(args);

		addShutdownHook();
		startHeartbeat();

		run();
	}

	private static void initEnvFromArgs(String[] args) {
		if (args == null || args.length == 0) {
			return;
		}
		if (args.length != 7) {
			System.err.println("args length must be 7");
			System.exit(0);
		}
		botIdStart = Integer.parseInt(args[0]);
		System.setProperty("botIdStart", botIdStart + "");
		botCount = Integer.parseInt(args[1]);
		loginInterval = Integer.parseInt(args[2]);
		sendInterval = Integer.parseInt(args[3]);
		botSendInterval = Integer.parseInt(args[4]);
		if (botSendInterval < 200) {
			System.err.println("botSendInterval must greater than 200");
			System.exit(0);
		}
		messageStatisticsInterval = Integer.parseInt(args[5]);
		botRunTimeMax = Integer.parseInt(args[6]);
	}

	private static void startHeartbeat() {
		Thread thread = new Thread(() -> {
			while (true) {
				try {
					for (Client client : clients) {
						if (!client.isLastMessageReturn()) {
							client.resendLastMessage();
							continue;
						}
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

	private static void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					System.err.println("start shutdown hook");
					run = false;
					List<ChannelFuture> futures = new ArrayList<>();
					for (Client client : clients) {
						ChannelFuture channelFuture = client.sendProtocol(PlayerLogoutRequest_01000003.getDefaultInstance());
						if (channelFuture != null) {
							futures.add(channelFuture);
						}
					}
					CompletableFuture<Void>[] completableFutures = futures.stream().map(ServerTestContext::toCompletableFuture)
							.toArray(CompletableFuture[]::new);

					CompletableFuture<Void> allFutures = CompletableFuture.allOf(completableFutures);
					try {
						allFutures.get(30, TimeUnit.SECONDS);
						System.err.println("All players logout requests completed successfully");
					} catch (TimeoutException e) {
						System.err.println("Some logout requests did not complete in time");
					} catch (Exception e) {
						System.err.println("Error occurred while waiting for logout requests");
						e.printStackTrace();
					}

					GlobalMessageStatistics.getInstance()
							.calculateStatisticsAndSaveResult(clients, Config.messageStatisticsInterval * 60 * 1000);
					System.err.println("shutdown hook execution completed");

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private static CompletableFuture<Void> toCompletableFuture(ChannelFuture channelFuture) {
		CompletableFuture<Void> completableFuture = new CompletableFuture<>();
		channelFuture.addListener((ChannelFutureListener) future -> {
			if (future.isSuccess()) {
				completableFuture.complete(null);
			} else {
				completableFuture.completeExceptionally(future.cause());
			}
		});
		return completableFuture;
	}

	public static void run() throws Exception {

		Map<String, ServerTest> beansRead = SpringContextLoader.getContext().getBeansOfType(ServerTest.class, true, true);
		Map<String, ServerTest> beansMap = new HashMap<String, ServerTest>();
		beansRead.forEach((k, v) -> {
//			beansMap.put(WordUtils.capitalize(k).substring(0, k.length() - 4), v);
			beansMap.put(k.substring(0, k.length() - 4).toLowerCase(), v);
		});
		Iterator<String> ipIterator = null;
		if (sourceIps != null) {
			ipIterator = sourceIps.iterator();
		}
		System.out.println();
		System.out.println("start to run bots,id start : " + botIdStart + " , count : " + botCount);

//	     先一起登陆，似乎分散起来更好。
		for (int i = botIdStart; i < botIdStart + botCount; i++) {
			long loginTimeStart = System.currentTimeMillis();
			try {
				Client client = new Client(i + "", i + "", serverId, version);
				client.loginPassportProto(loginServerUrl);
				String sourceIp = null;
				if (ipIterator != null) {
					if (!ipIterator.hasNext()) {
						ipIterator = sourceIps.iterator();
					}
					sourceIp = ipIterator.next();
				}
				client.loginGateway(gateServerIp, gateServerPort, sourceIp);
				clients.add(client);
			} catch (Exception e) {
				e.printStackTrace();
			}
			long loginCost = System.currentTimeMillis() - loginTimeStart;
			if (loginCost < loginInterval) {
				Thread.sleep(loginInterval - loginCost);
			}
		}
		long startTime = System.currentTimeMillis();

		long lastStatisticsTime = System.currentTimeMillis();
		long lastSendTime = System.currentTimeMillis();
		Iterator<Client> iterator = clients.iterator();
		while (run) {

			try {
				if (botRunTimeMax > 0 && System.currentTimeMillis() - startTime > botRunTimeMax * 60 * 1000) {
					// 到运行时间上限，该停止了
					System.err.println(" time to stop");
					System.exit(0);
				}
				if (System.currentTimeMillis() - lastStatisticsTime > messageStatisticsInterval * 60 * 1000) {
					Thread.sleep(5000); // 先等待一下回复消息
					GlobalMessageStatistics.getInstance().calculateStatisticsAndSaveResult(clients, messageStatisticsInterval * 60 * 1000);

					lastStatisticsTime = System.currentTimeMillis();
				}
				if (System.currentTimeMillis() - lastSendTime < sendInterval) {
					Thread.sleep(1);
					continue;
				}
				if (iterator.hasNext()) {
					Client client = iterator.next();
					if (!client.getInit()) {
						continue;
					}
					if (!client.isLastMessageReturn()) {
						client.resendLastMessage();
						continue;
					}
					if (client.getLastSendMessageTime() > 0
							&& System.currentTimeMillis() - client.getLastSendMessageTime() < botSendInterval) {
						continue;
					}
					String randomMessage = CSVMessagesReader.randomMessage();
					ServerTest serverTest = beansMap.get(randomMessage.toLowerCase());
					if (serverTest == null) {
						throw new IllegalArgumentException("test message not found : " + randomMessage);
					}
					Message message = serverTest.getMessage(client);
					if (message != null) {
						client.sendProtocol(message);
						lastSendTime = System.currentTimeMillis();
					}
				} else {
					iterator = clients.iterator();
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
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

		Properties initialProp = new Properties();
		ClassLoader loader = Thread.currentThread().getClass().getClassLoader();
		if (loader == null) {
			loader = ServerTestContext.class.getClassLoader();
		}
		InputStream inputStream = loader.getResourceAsStream("env.properties");
		initialProp.load(inputStream);
		inputStream.close();

//		init(initialProp);
//		LogbackConfig.init(initLogback, logbackFile);
		readEnv(initialProp);

		SpringContextLoader.loadWithFile(new String[] { args });

		init = true;
		return true;
	}

	public static void readEnv(Properties initialProp) {

		passportUsername = initialProp.getProperty("user.name");
		pwd = initialProp.getProperty("user.pwd");
		gateServerIp = initialProp.getProperty("game.server.ip");
		gateServerPort = initialProp.getProperty("game.server.port") == null ? 0 : Integer.parseInt(initialProp.getProperty("game.server.port"));
		loginServerUrl = initialProp.getProperty("login.server.url");

		logbackFile = initialProp.getProperty("logbackFile");
		args = initialProp.getProperty("argsFile");
		login = Boolean.parseBoolean(initialProp.getProperty("login"));

		serverId = initialProp.getProperty("game.server.id");
		version = initialProp.getProperty("game.server.version");
		initLogback = Boolean.parseBoolean(initialProp.getProperty("initLogback"));
//			LogbackConfig.init(initLogback, logbackFile);
		URL resource = Thread.currentThread().getContextClassLoader().getResource(args);
		args = resource.getPath();
		botIdStart = Integer.parseInt(initialProp.getProperty("botIdStart"));
		System.setProperty("botIdStart", botIdStart + "");

		botCount = Integer.parseInt(initialProp.getProperty("botCount"));
		loginInterval = Integer.parseInt(initialProp.getProperty("loginInterval"));
		sendInterval = Integer.parseInt(initialProp.getProperty("sendInterval"));
		botSendInterval = Integer.parseInt(initialProp.getProperty("botSendInterval"));
		messageStatisticsInterval = Integer.parseInt(initialProp.getProperty("messageStatisticsInterval"));
		botRunTimeMax = Integer.parseInt(initialProp.getProperty("botRunTimeMax"));
		String sourceIpsString = initialProp.getProperty("sourceIps");
		if (!StringUtils.isEmpty(sourceIpsString)) {
			sourceIps = new ArrayList<>();
			String[] split = sourceIpsString.split(",");
			for (String ip : split) {
				sourceIps.add(ip);
			}
		}

	}
}
