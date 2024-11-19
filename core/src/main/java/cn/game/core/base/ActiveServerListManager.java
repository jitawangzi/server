package cn.game.core.base;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;

import cn.game.core.net.vertx.VxHolder;
import cn.game.util.ServerType;
import cn.game.util.ZkHelper;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.NodeInfo;

/**    
 * 某zk地址下的存活的server列表。 
 * 2024年2月1日 下午4:20:41
 * @author SYQ
 */
public class ActiveServerListManager {
	private static final Logger log = LoggerFactory.getLogger(ActiveServerListManager.class);
	private static ActiveServerListManager instance = new ActiveServerListManager();

	public static ActiveServerListManager getInstance() {
		return instance;
	}
	private ActiveServerListManager() {
	}
	
	/**  */
	private Map<String, Set<String>> serverListMap = new ConcurrentHashMap<String, Set<String>>();
	/**  game服务器在线人数,先存这 */
	private Map<String, Integer> playerCountMap = new ConcurrentHashMap<String, Integer>();

	
	public void start(ServerType... serverType) throws Exception {
		for (ServerType serverType2 : serverType) {
			serverListMap.put(serverType2.name(), new ConcurrentHashSet<String>());
		}
		listenServerNode(serverType);
	}

	private void listenServerNode(ServerType... serverType) throws Exception {

		CuratorFramework client = ZkHelper.curator;
		Config config = ConfigService.getConfig("zookeeper");
		String path = config.getProperty("active.server.path", "");
		if (StringUtils.isEmpty(path)) {
			throw new IllegalArgumentException("active server list  path not found ");
		}
		// 创建路径
		client.create().orSetData().creatingParentsIfNeeded().forPath(path);

		// 创建 PathChildrenCache
		PathChildrenCache cache = new PathChildrenCache(client, path, true);
		cache.start();
		// 添加监听器
		cache.getListenable().addListener(new PathChildrenCacheListener() {
			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
				ChildData data = event.getData();
				if (data != null) {
					String nodeId = ZkHelper.getNodeNameFromPath(data.getPath());

					switch (event.getType()) {
					case CHILD_ADDED: {
						addNode(nodeId);
						break;
					}
					case CHILD_UPDATED: {
						addNode(nodeId);
						break;
					}
					case CHILD_REMOVED: {
						removeNode(data.getData());
						break;
					}
					default:
						break;
					}
				}
			}
		});
	}

	private void addNode(String nodeId) {
		// 创建 Promise 对象，用于处理异步结果
		Promise<NodeInfo> promise = Promise.promise();
		VxHolder.getZookeeperClusterManager().getNodeInfo(nodeId, promise);
		// 处理异步结果
		promise.future().onComplete(ar -> {
			if (ar.succeeded()) {
				NodeInfo nodeInfo = ar.result();
				JsonObject server = nodeInfo.metadata();
				if (server == null) {
					return;
				}
				String serverId = server.getString("serverId");
				String serverType = server.getString("serverType");
				addServer(serverId, serverType);
			} else {
				log.warn("failed to get Node Info: " + ar.cause().getMessage());
			}
		});
	}

	private void removeNode(byte[] data) {

		NodeInfo nodeInfo = new NodeInfo();
		nodeInfo.readFromBuffer(0, Buffer.buffer(data));
		JsonObject server = nodeInfo.metadata();
		if (server == null) {
			return;
		}
		String serverId = server.getString("serverId");
		String serverType = server.getString("serverType");
		removeServer(serverId, serverType);
	}

	private void removeServer(String serverId, String serverType) {
		Set<String> set = serverListMap.get(serverType);
		if (set != null) {
			set.remove(serverId);
			log.info("active server node removes ,id[{}] type[{}]", serverId, serverType);
		}
	}

	public void addServer(String serverId, String serverType) {
		Set<String> set = serverListMap.get(serverType);
		if (set != null) {
			set.add(serverId);
			log.info("active server node add ,id[{}] type[{}]", serverId, serverType);
		}
	}

	/** 
	 * 获取某类型的所有活跃服务器id
	 * @param serverType
	 * @return
	 */
	public Set<String> getServerSet(ServerType serverType) {
		Set<String> set = this.serverListMap.get(serverType.name());
		return set == null ? Collections.EMPTY_SET : set;
	}

	public void setPlayerCount(String serverId, int playerCount) {
		this.playerCountMap.put(serverId, playerCount);
	}

	public int getPlayerCount(String serverId) {
		Integer integer = this.playerCountMap.get(serverId);
		return integer == null ? 0 : integer;
	}

}
