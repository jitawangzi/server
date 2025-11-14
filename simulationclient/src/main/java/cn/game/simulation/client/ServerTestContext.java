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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import javax.net.ssl.SSLException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;

import cn.game.core.net.pressure.GlobalMessageStatistics;
import cn.game.games.net.game.manager.ActivityStateManager;
import cn.game.protocol.generated.helper.ManagerHelper;
import cn.game.protocol.protobuf.PlayerMsg.PlayerLogoutRequest_01000003;
import cn.game.simulation.test.base.ServerTest;
import cn.game.simulation.util.CSVMessagesReader;
import cn.game.simulation.util.CSVMessagesReader.CSVMessage;
import cn.game.util.SpringContextLoader;
import cn.game.util.log.LoggerManager;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Promise;

/**    
 * 压测使用
 * 2024年11月19日 11:33:29
 * @author SYQ
 */
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
	public static int msgGroup;
	public static int singleMessage;

	// 并发登录数
	public static int loginConcurrency = 8;

	public static boolean init = false;

	public static volatile boolean run = true;

	public static ConcurrentLinkedQueue<Client> clients = new ConcurrentLinkedQueue<Client>();

	private static List<String> sourceIps;

	// 用于控制登录速率的信号量
	private static Semaphore loginRateLimiter;

	// 用于并发登录的线程池
	private static ExecutorService loginExecutor;

	// 记录登录统计信息
	private static final AtomicInteger loginSuccessCount = new AtomicInteger(0);
	private static final AtomicInteger loginFailCount = new AtomicInteger(0);
	private static volatile boolean loginCompleted = false;

	public static void main(String args[]) throws Exception {
		Client.exitOnClientClose = false;

		// 仅用于定位，生产环境谨慎使用内部API
//		try {
//			sun.misc.Signal.handle(new sun.misc.Signal("TERM"), sig -> System.err.println("Java caught SIGTERM"));
//			sun.misc.Signal.handle(new sun.misc.Signal("INT"), sig -> System.err.println("Java caught SIGINT"));
//			sun.misc.Signal.handle(new sun.misc.Signal("HUP"), sig -> System.err.println("Java caught SIGHUP"));
//		} catch (Throwable t) {
//			System.err.println("Signal handlers not installed: " + t);
//		}

		String filePath = System.getProperty("user.dir") + "/messages.csv";
		CSVMessagesReader.read(filePath);
		ManagerHelper.init();
		ActivityStateManager.getInstance().start();

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
		if (args.length != 8) {
			System.err.println("args length must be 8");
			System.exit(0);
		}
		singleMessage = Integer.parseInt(args[0]);
		botIdStart = Integer.parseInt(args[1]);
		System.setProperty("botIdStart", botIdStart + "");
		botCount = Integer.parseInt(args[2]);
		loginInterval = Integer.parseInt(args[3]);
		sendInterval = Integer.parseInt(args[4]);
		botSendInterval = Integer.parseInt(args[5]);
		if (botSendInterval < 200) {
			System.err.println("botSendInterval must greater than 200");
			System.exit(0);
		}
		messageStatisticsInterval = Integer.parseInt(args[6]);
		botRunTimeMax = Integer.parseInt(args[7]);
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
	            long startTime = System.currentTimeMillis();
	            System.out.println("开始优雅关闭流程...");
	            
	            try {
	                run = false;
	                
	                // 1) 关闭登录线程池
	                shutdownLoginExecutor();
	                
	                // 2) 并发退出所有客户端
	                logoutAllClients();
	                
	                // 3) 统计与收尾
	                GlobalMessageStatistics.getInstance().calculateStatisticsAndSaveResult(clients);
	                
	                long totalTime = System.currentTimeMillis() - startTime;
	                System.out.println("优雅关闭流程完成,总耗时: " + totalTime + "ms" + ",保存玩家数："+ clients.size());
	                
	            } catch (Exception e) {
	                System.err.println("关闭流程异常: " + e.getMessage());
	                e.printStackTrace();
	            }
	        }
	    });
	}

	/**
	 * 关闭登录线程池
	 */
	private static void shutdownLoginExecutor() {
	    if (loginExecutor == null || loginExecutor.isShutdown()) {
	        return;
	    }
	    
	    long start = System.currentTimeMillis();
	    loginExecutor.shutdown();
	    
	    try {
	        if (!loginExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
	            loginExecutor.shutdownNow();
	        }
	        System.out.println("登录线程池关闭完成,耗时: " + (System.currentTimeMillis() - start) + "ms");
	    } catch (InterruptedException e) {
	        loginExecutor.shutdownNow();
	        Thread.currentThread().interrupt();
	    }
	}

	/**
	 * 并发退出所有客户端
	 */
	private static void logoutAllClients() {
	    final int CONCURRENT_LOGOUT = 8;           // 同时退出的客户端数
	    final long OVERALL_TIMEOUT_SECONDS = 300;  // 整体超时时间
	    final long RESPONSE_TIMEOUT_SECONDS = 10;  // 单个客户端响应超时
	    
	    if (clients == null || clients.isEmpty()) {
	        System.out.println("没有需要退出的客户端");
	        return;
	    }
	    
	    long startTime = System.currentTimeMillis();
	    System.out.println("开始退出 " + clients.size() + " 个客户端,并发度: " + CONCURRENT_LOGOUT);
	    
	    // 使用信号量控制并发数
	    Semaphore semaphore = new Semaphore(CONCURRENT_LOGOUT);
	    CountDownLatch latch = new CountDownLatch(clients.size());
	    
	    AtomicInteger successCount = new AtomicInteger(0);
	    AtomicInteger failCount = new AtomicInteger(0);
	    AtomicInteger timeoutCount = new AtomicInteger(0);
	    
	    // 为每个客户端创建退出任务
	    ExecutorService logoutExecutor = Executors.newFixedThreadPool(CONCURRENT_LOGOUT);
	    
	    for (Client client : clients) {
	        logoutExecutor.submit(() -> {
	            try {
	                // 获取信号量(阻塞直到有可用槽位)
	                semaphore.acquire();
	                
	                // 执行退出逻辑
	                boolean success = logoutSingleClient(client, RESPONSE_TIMEOUT_SECONDS);
	                
	                if (success) {
	                    successCount.incrementAndGet();
	                } else {
	                    timeoutCount.incrementAndGet();
	                }
	                
	            } catch (Exception e) {
	                failCount.incrementAndGet();
	                System.err.println("客户端退出异常: " + e.getMessage());
	            } finally {
	                semaphore.release(); // 释放信号量,允许下一个客户端开始退出
	                latch.countDown();
	            }
	        });
	    }
	    
	    // 等待所有客户端完成或整体超时
	    try {
	        boolean completed = latch.await(OVERALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
	        
	        if (!completed) {
	            System.err.println("退出流程整体超时(" + OVERALL_TIMEOUT_SECONDS + "秒)");
	        }
	        
	    } catch (InterruptedException e) {
	        Thread.currentThread().interrupt();
	        System.err.println("退出流程被中断");
	    } finally {
	        logoutExecutor.shutdown();
	        try {
	            logoutExecutor.awaitTermination(5, TimeUnit.SECONDS);
	        } catch (InterruptedException e) {
	            logoutExecutor.shutdownNow();
	        }
	    }
	    
	    long totalTime = System.currentTimeMillis() - startTime;
	    System.out.println(String.format(
	        "客户端退出完成 - 总数:%d, 成功:%d, 超时:%d, 失败:%d, 耗时:%dms",
	        clients.size(), successCount.get(), timeoutCount.get(), failCount.get(), totalTime
	    ));
	}

	/**
	 * 退出单个客户端
	 * @param client 客户端
	 * @param timeoutSeconds 超时时间(秒)
	 * @return 是否成功收到服务器响应
	 */
	private static boolean logoutSingleClient(Client client, long timeoutSeconds) {
	    try {
	        // 发送退出请求
	        ChannelFuture future = client.sendProtocol(PlayerLogoutRequest_01000003.getDefaultInstance());
	        
	        if (future == null) {
	            System.err.println("发送退出请求失败: future为null");
	            return false;
	        }
	        
	        // 等待发送完成
	        if (!future.await(2, TimeUnit.SECONDS)) {
	            System.err.println("发送退出请求超时");
	            return false;
	        }
	        
	        // 轮询等待服务器响应
	        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000;
	        
	        while (System.currentTimeMillis() < deadline) {
	            if (client.isLastMessageReturn()) {
	                return true; // 收到响应
	            }
	            
	            // 短暂休眠避免CPU空转
	            Thread.sleep(20);
	        }
	        
	        // 超时未收到响应
	        return false;
	        
	    } catch (InterruptedException e) {
	        Thread.currentThread().interrupt();
	        return false;
	    } catch (Exception e) {
	        System.err.println("退出客户端异常: " + e.getMessage());
	        return false;
	    }
	}

	public static void run() throws Exception {

		Map<String, ServerTest> beansRead = SpringContextLoader.getContext().getBeansOfType(ServerTest.class, true, true);
		Map<String, ServerTest> beansMap = new HashMap<String, ServerTest>();
		beansRead.forEach((k, v) -> {
			beansMap.put(k.substring(0, k.length() - 4).toLowerCase(), v);
		});

		// 初始化登录相关组件
		initLoginComponents();

		System.out.println();
		System.out.println("start to run bots,id start : " + botIdStart + " , count : " + botCount);
		System.out.println("login concurrency: " + loginConcurrency + ", login interval: " + loginInterval + "ms");

		// 异步启动并发登录，不等待完成
		startConcurrentLogin();

		// 启动登录进度监控线程
		startLoginProgressMonitor();

		long startTime = System.currentTimeMillis();

		long lastStatisticsTime = System.currentTimeMillis();
		long lastSendTime = System.currentTimeMillis();
		Iterator<Client> iterator = clients.iterator();

		// 主循环：边登录边发送请求
		while (run) {
			try {
				if (botRunTimeMax > 0 && System.currentTimeMillis() - startTime > botRunTimeMax * 60 * 1000) {
					// 到运行时间上限，该停止了
					System.out.println("time to stop, runtime=" + (System.currentTimeMillis() - startTime) / 1000 / 60
							+ "min, botRunTimeMax=" + botRunTimeMax + "min");
					run = false;
				}
				if (System.currentTimeMillis() - lastStatisticsTime > messageStatisticsInterval * 60 * 1000) {
					Thread.sleep(5000); // 先等待一下回复消息
					GlobalMessageStatistics.getInstance().calculateStatisticsAndSaveResult(clients);

					lastStatisticsTime = System.currentTimeMillis();
				}
				  // 全局发送节流
				if (System.currentTimeMillis() - lastSendTime < sendInterval) {
					Thread.sleep(1);
					continue;
				}

				// 如果还没有任何客户端，等待一下
				if (clients.isEmpty()) {
					Thread.sleep(100);
					continue;
				}
		        if (iterator.hasNext()) {
		            Client client = iterator.next();

		            // 对该 client 在本轮就地尝试直到发出或确定无可发
		            boolean	sent = trySendOneMessage(beansMap, client);
		            if (sent) {
		                lastSendTime = System.currentTimeMillis();
		            }
		            // 无论 sent 与否，都继续迭代到下一个 client
		        } else {
		            iterator = clients.iterator();
		        }
			} catch (Throwable e) {
				logger.error("main loop error", e);
			}
		}
		System.out.println("Main loop exited. run=" + run);
	}
	
	/** 
	 * 
	 * @param beansMap
	 * @param client
	 * @return  是否成功发送了消息
	 */
	private static boolean trySendOneMessage(Map<String, ServerTest> beansMap,Client client) {
	    // 只处理已初始化
	    if (!client.getInit()) {
	        return false;
	    }

	    // 若上条消息未返回，优先尝试重发；返回 true 表示已经发出一次，本轮结束
	    if (!client.isLastMessageReturn()) {
	        boolean resent = client.resendLastMessage();
	        if (resent) {
	            return true;
	        }
	        // 未重发成功则继续尝试新消息
	    }

	    // 发送频率限制：如果该 client 还未到间隔，直接返回本 client 本轮不发
	    if (client.getLastSendMessageTime() > 0
	            && System.currentTimeMillis() - client.getLastSendMessageTime() < botSendInterval) {
	        return false;
	    }

	    // 在本轮内不断尝试找到可发的一条消息
	    int guard = 0; // 防止异常情况下死循环
	    final int maxProbe = 256;

	    while (guard++ < maxProbe) {
	        CSVMessage randomMessage;

	        if (singleMessage > 0) {
	            randomMessage = CSVMessagesReader.randomGroupMessage(msgGroup, client.sendingGroupIndex);
	        } else {
	            randomMessage = CSVMessagesReader.randomGroupMessage(client.sendingGroup, client.sendingGroupIndex);
	        }

	        if (randomMessage == null) {
	            // 当前组消息用尽，重置组与索引，然后继续下一轮随机
	            client.sendingGroup = 0;
	            client.sendingGroupIndex = 0;
	            continue;
	        }

	        ServerTest serverTest = beansMap.get(randomMessage.msgName.toLowerCase());
	        if (serverTest == null) {
	            logger.warn("test message not found : " + randomMessage);
	            // 找不到，推进到组内下一条
	            client.sendingGroupIndex++;
	            continue;
	        }

	        Message messageObj = null;
	        try {
	            messageObj = serverTest.getMessagePressure(client);
	        } catch (Exception e) {
	            // 构造失败，推进到组内下一条继续
	            client.sendingGroupIndex++;
	            logger.error("getMessagePressure error, ServerTest :  " + serverTest.getClass().getSimpleName(), e);
	            continue;
	        }

	        if (messageObj == null) {
	            // 条件不满足，推进下一条
	            client.sendingGroupIndex++;
	            continue;
	        }

	        // 找到可发消息，立即发送并记录组与索引
	        client.sendProtocol(messageObj);
	        client.sendingGroup = randomMessage.group;
	        client.sendingGroupIndex++;
	        return true;
	    }
	    // 超过探测次数仍未发出，应该有错误
	    throw new IllegalStateException("Unable to send message after " + maxProbe + " attempts for client " + client);
	}

	
	/**
	 * 初始化登录相关组件
	 */
	private static void initLoginComponents() {
		// 创建线程池，核心线程数为并发登录数，最大线程数为并发登录数的2倍
		loginExecutor = new ThreadPoolExecutor(loginConcurrency, loginConcurrency * 2, 60L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<>(botCount), new ThreadFactory() {
					private final AtomicInteger counter = new AtomicInteger(0);

					@Override
					public Thread newThread(Runnable r) {
						Thread thread = new Thread(r, "LoginThread-" + counter.incrementAndGet());
						thread.setDaemon(true);
						return thread;
					}
				}, new ThreadPoolExecutor.CallerRunsPolicy());

		// 初始化速率限制器，控制登录频率
		// 每秒最多允许 1000/loginInterval 个登录请求
		int permitsPerSecond = Math.max(1, 1000 / loginInterval);
		loginRateLimiter = new Semaphore(permitsPerSecond);

		// 启动速率限制器补充线程
		startRateLimiterRefiller(permitsPerSecond);
	}

	/**
	 * 启动速率限制器补充线程
	 */
	private static void startRateLimiterRefiller(int permitsPerSecond) {
		Thread refiller = new Thread(() -> {
			while (run && !loginCompleted) {
				try {
					Thread.sleep(1000);
					// 每秒补充许可
					int availablePermits = loginRateLimiter.availablePermits();
					if (availablePermits < permitsPerSecond) {
						loginRateLimiter.release(permitsPerSecond - availablePermits);
					}
				} catch (InterruptedException e) {
					break;
				}
			}
		}, "RateLimiterRefiller");
		refiller.setDaemon(true);
		refiller.start();
	}

	/**
	 * 异步启动并发登录（不阻塞主线程）
	 */
	private static void startConcurrentLogin() {
		final AtomicReference<Iterator<String>> ipIteratorRef = new AtomicReference<>();
		if (sourceIps != null && !sourceIps.isEmpty()) {
			ipIteratorRef.set(sourceIps.iterator());
		}

		long overallStartTime = System.currentTimeMillis();

		// 在后台线程中提交所有登录任务
		Thread loginScheduler = new Thread(() -> {
			for (int i = botIdStart; i < botIdStart + botCount; i++) {
				final int botId = i;

				try {
					// 获取速率限制许可
					loginRateLimiter.acquire();

					// 提交登录任务到线程池
					loginExecutor.submit(() -> {
						try {
							long loginTimeStart = System.currentTimeMillis();

							Client client = new Client(botId + "", "", serverId, version);

							// 同步登陆账号服务器
							client.loginPassportProto(loginServerUrl);

							String sourceIp = null;
							Iterator<String> ipIterator = ipIteratorRef.get();
							if (ipIterator != null) {
								synchronized (ipIteratorRef) {
									ipIterator = ipIteratorRef.get();
									if (ipIterator != null) {
										if (!ipIterator.hasNext()) {
											ipIterator = sourceIps.iterator();
											ipIteratorRef.set(ipIterator);
										}
										sourceIp = ipIterator.next();
									}
								}
							}

							// 异步登陆游戏服务器
							client.loginGateway(gateServerIp, gateServerPort, sourceIp);
							clients.add(client);

							int currentSuccess = loginSuccessCount.incrementAndGet();

							long loginCost = System.currentTimeMillis() - loginTimeStart;
							if (currentSuccess % 100 == 0) {
								System.out.println(String.format("Login progress: %d/%d, success: %d, fail: %d, last login cost: %dms",
										currentSuccess + loginFailCount.get(), botCount, currentSuccess, loginFailCount.get(), loginCost));
							}

						} catch (Exception e) {
							int currentFail = loginFailCount.incrementAndGet();
							System.err.println("Bot " + botId + " login failed: " + e.getMessage());
							if (currentFail <= 10) { // 只打印前10个失败的详细堆栈
								e.printStackTrace();
							}
						}
					});
				} catch (InterruptedException e) {
					System.err.println("Login scheduler interrupted");
					break;
				}
			}

			long totalTime = System.currentTimeMillis() - overallStartTime;
			loginCompleted = true;
			System.out.println(String.format("All login tasks submitted. Total submission time: %dms", totalTime));

		}, "LoginScheduler");
		loginScheduler.setDaemon(true);
		loginScheduler.start();
	}

	/**
	 * 启动登录进度监控线程
	 */
	private static void startLoginProgressMonitor() {
		Thread monitor = new Thread(() -> {
			long startTime = System.currentTimeMillis();
			while (!loginCompleted && run) {
				try {
					Thread.sleep(5000);
					int success = loginSuccessCount.get();
					int fail = loginFailCount.get();
					int total = success + fail;
					long readyCount = clients.stream().filter(Client::getInit).count();

					System.out.println(String.format(
							"[Login Monitor] Total: %d/%d (%.1f%%), Success: %d, Fail: %d, Ready: %d, Elapsed: %ds", total, botCount,
							(total * 100.0 / botCount), success, fail, readyCount, (System.currentTimeMillis() - startTime) / 1000));

					if (total >= botCount) {
						System.out.println(String.format("[Login Monitor] All login attempts completed! Success: %d, Fail: %d, Ready: %d",
								success, fail, readyCount));
						break;
					}
				} catch (InterruptedException e) {
					break;
				}
			}
		}, "LoginProgressMonitor");
		monitor.setDaemon(true);
		monitor.start();
	}

	public static void send(Client client, Message message)
			throws URISyntaxException, InterruptedException, UnknownHostException, SSLException {

		if (login) {
			client.loginPassport(loginServerUrl);
			client.loginGateway(gateServerIp, gateServerPort);
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

		readEnv(initialProp);

		SpringContextLoader.loadWithFile(new String[] { args });

		init = true;
		return true;
	}

	public static void readEnv(Properties initialProp) {

		passportUsername = initialProp.getProperty("user.name");
		pwd = initialProp.getProperty("user.pwd");
		gateServerIp = initialProp.getProperty("game.server.ip");
		gateServerPort = initialProp.getProperty("game.server.port") == null ? 0
				: Integer.parseInt(initialProp.getProperty("game.server.port"));
		loginServerUrl = initialProp.getProperty("login.server.url");

		logbackFile = initialProp.getProperty("logbackFile");
		args = initialProp.getProperty("argsFile");
		login = Boolean.parseBoolean(initialProp.getProperty("login"));

		serverId = initialProp.getProperty("game.server.id");
		version = initialProp.getProperty("game.server.version");
		initLogback = Boolean.parseBoolean(initialProp.getProperty("initLogback"));
		URL resource = Thread.currentThread().getContextClassLoader().getResource(args);
		args = resource.getPath();
		botIdStart = Integer.parseInt(initialProp.getProperty("botIdStart"));
		System.setProperty("botIdStart", botIdStart + "");

		botCount = Integer.parseInt(initialProp.getProperty("botCount"));
		singleMessage = Integer.parseInt(initialProp.getProperty("singleMessage"));
		loginInterval = Integer.parseInt(initialProp.getProperty("loginInterval"));
		sendInterval = Integer.parseInt(initialProp.getProperty("sendInterval"));
		botSendInterval = Integer.parseInt(initialProp.getProperty("botSendInterval"));
		messageStatisticsInterval = Integer.parseInt(initialProp.getProperty("messageStatisticsInterval"));
		botRunTimeMax = Integer.parseInt(initialProp.getProperty("botRunTimeMax"));
		msgGroup = initialProp.getProperty("msgGroup") == null ? 0 : Integer.parseInt(initialProp.getProperty("msgGroup"));

		// 读取登录并发数配置
		loginConcurrency = initialProp.getProperty("loginConcurrency") == null ? 8
				: Integer.parseInt(initialProp.getProperty("loginConcurrency"));

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