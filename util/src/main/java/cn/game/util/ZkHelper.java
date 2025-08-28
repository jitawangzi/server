package cn.game.util;

import java.util.concurrent.TimeUnit;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;

import io.vertx.core.json.JsonObject;

public class ZkHelper {
	private static final Logger log = LoggerFactory.getLogger(ZkHelper.class);
	private static volatile boolean inited = false;
	public static CuratorFramework curator;
	static {
		try {
			init();
		} catch (Exception e) {
			log.error("zookeeper 初始化失败", e);
			throw new ExceptionInInitializerError("zookeeper 初始化失败: " + e.getMessage());
		}
	}

	private static void init(JsonObject conf) throws InterruptedException {

		RetryPolicy retryPolicy = new ExponentialBackoffRetry(
				conf.getJsonObject("retry", new JsonObject()).getInteger("initialSleepTime", 1000),
				conf.getJsonObject("retry", new JsonObject()).getInteger("maxTimes", 5),
				conf.getJsonObject("retry", new JsonObject()).getInteger("intervalTimes", 10000));

		// Read the zookeeper hosts from a system variable
		String hosts = System.getProperty("vertx.zookeeper.hosts");
		if (hosts == null) {
			hosts = conf.getString("zookeeperHosts", "127.0.0.1");
		}
		log.info("Zookeeper hosts set to " + hosts);
		curator = CuratorFrameworkFactory.builder().connectString(hosts)
				.namespace(conf.getString("rootPath", "io.vertx"))
				.sessionTimeoutMs(conf.getInteger("sessionTimeout", 20000))
				.connectionTimeoutMs(conf.getInteger("connectTimeout", 3000)).retryPolicy(retryPolicy).build();

		curator.start();
		int count = 0;
		while (curator.getState() != CuratorFrameworkState.STARTED) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				if (curator.getState() != CuratorFrameworkState.STARTED) {
					throw new IllegalArgumentException(
							"zookeeper client being interrupted while starting,CuratorFramework init failed ");
				}
			}
			if (count++ >= 100) {
				throw new IllegalArgumentException("CuratorFramework init failed ");
			}
		}
		curator.blockUntilConnected(30, TimeUnit.SECONDS);  
	}

	private static void init() throws InterruptedException {
		if (inited) {
			return;
		}
		inited = true;
		Config zkConfig = ConfigService.getConfig("zookeeper");
		String content = zkConfig.getProperty("zk", null);
		JsonObject conf = new JsonObject(content);
		init(conf);
	}

	/** 
	 * 从节点路径中提取节点名字
	 * @param nodePath
	 * @return
	 */
	public static String getNodeNameFromPath(String nodePath) {
		// 在实际情况中，您可能需要根据节点路径的格式进行适当的解析
		String[] pathSegments = nodePath.split("/");
		return pathSegments[pathSegments.length - 1];
	}
}
