package cn.game.games.core;


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

import cn.game.core.base.ServerContext;
import cn.game.core.base.ServerList;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.util.DateUtil;
import cn.game.util.GameUtil;
import cn.game.util.ZkHelper;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**    
 * 管理GameServer状态 
 * 2024年2月1日 下午4:20:41
 * @author SYQ
 */
public class GameServerStatus {
	private static final Logger log = LoggerFactory.getLogger(GameServerStatus.class);
	private static GameServerStatus instance = new GameServerStatus();

	public static GameServerStatus getInstance() {
		return instance;
	}

	private GameServerStatus() {
	}
	
	private ServerList serverInfo;

	
	public Future<Integer> start() throws Exception {
		return listenGameServerNode();
	}

	private Future<Integer> listenGameServerNode() throws Exception {

		CuratorFramework client = ZkHelper.curator;
		Config config = ConfigService.getConfig("zookeeper");
		String path = config.getProperty("game.server.path", "");
		if (StringUtils.isEmpty(path)) {
			throw new IllegalArgumentException("game server list  path not found ");
		}
//		String serverId = ServerContext.getInstance().getServerId();
//		path += "/" + serverId;
		// 创建路径
//		client.create().orSetData().creatingParentsIfNeeded().forPath(path);
		// 创建 PathChildrenCache
		PathChildrenCache cache = new PathChildrenCache(client, path, true);
		cache.start();
		
		Promise<Integer> promise = Promise.promise();
		// 添加监听器
		cache.getListenable().addListener(new PathChildrenCacheListener() {
			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
				ChildData data = event.getData();
				if (data != null) {
					String serverId = ZkHelper.getNodeNameFromPath(data.getPath());
					String curServerId = ServerContext.getInstance().getServerId();
					if (!serverId.equalsIgnoreCase(curServerId)) {
						return;
					}
					switch (event.getType()) {
					case CHILD_ADDED: {
						ServerList server = JSON.parseObject(data.getData(), ServerList.class);
						log.info("Game节点添加：id[{}] {}", serverId, server);
						serverInfo = server;
						promise.complete(server.getPort());
						break;
					}
					case CHILD_UPDATED: {
						ServerList server = JSON.parseObject(data.getData(), ServerList.class);
						log.info("Game节点更新：id[{}] {}", serverId, server);
						serverInfo = server;
						break;
					}
					case CHILD_REMOVED:
						log.info("Game节点移除：[{}]", serverId);
						// 处理节点移除事件
						serverInfo = null;
						break;
					default:
						break;
					}
				}
			}
		});
		
		return promise.future();
	}

	public int canLogin(String version) {
		if (serverInfo == null || serverInfo.getStatus() != ServerList.STATUS_RUN) {
			return ErrorMsgEnum.server_status.getId();
		}
		boolean equalsVersion = GameUtil.equalsVersion(version, serverInfo.getVersion());
		if (!equalsVersion) {
			return ErrorMsgEnum.version_mismatch.getId();
		}
		return 0;
	}

	public ServerList getServerInfo() {
		return serverInfo;
	}

	/** 
	 * 获取开服到现在多少天了
	 * @return
	 */
	public int getOpenDaysBetweenNow() {
		return DateUtil.diffDays(serverInfo.getServerOpenTime());
	}

	/** 
	 * 获取开服第多少天
	 * @return
	 */
	public int getOpenDays() {
		return getOpenDaysBetweenNow() + 1;
	}

}