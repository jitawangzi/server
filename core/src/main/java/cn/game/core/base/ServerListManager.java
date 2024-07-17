package cn.game.core.base;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;

import cn.game.util.ZkHelper;

/**    
 * 管理某zk地址下的server列表，工具配置的。 
 * 2024年2月1日 下午4:20:41
 * @author SYQ
 */
public class ServerListManager {
	private static final Logger log = LoggerFactory.getLogger(ServerListManager.class);
	private static ServerListManager instance = new ServerListManager();

	public static ServerListManager getInstance() {
		return instance;
	}
	private ServerListManager() {
	}
	
	/**  */
	private Map<String, ServerList> serverListMap = new ConcurrentHashMap<String, ServerList>();

	
	public void start() throws Exception {
		listenServerNode();
	}

	private void listenServerNode() throws Exception {

		CuratorFramework client = ZkHelper.curator;
		Config config = ConfigService.getConfig("zookeeper");
		String path = config.getProperty("game.server.path", "");
		if (StringUtils.isEmpty(path)) {
			throw new IllegalArgumentException("game server list  path not found ");
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
					String serverId = ZkHelper.getNodeNameFromPath(data.getPath());
					switch (event.getType()) {
					case CHILD_ADDED: {
						ServerList server = JSON.parseObject(data.getData(), ServerList.class);
						log.info("配置的Game节点添加：id[{}] {}", serverId, server);
						serverListMap.put(serverId, server);
						break;
					}
					case CHILD_UPDATED: {
						ServerList server = JSON.parseObject(data.getData(), ServerList.class);
						log.info("配置的Game节点更新：id[{}] {}", serverId, server);
						serverListMap.put(serverId, server);
						break;
					}
					case CHILD_REMOVED:
						log.info("配置的Game节点移除：[{}]", serverId);
						// 处理节点移除事件
						serverListMap.remove(serverId);
						break;
					default:
						break;
					}
				}
			}
		});
	}

	public Collection<ServerList> getServerList() {
		return this.serverListMap.values();
	}
}
