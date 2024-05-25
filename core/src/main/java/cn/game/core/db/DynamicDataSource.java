package cn.game.core.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.zaxxer.hikari.HikariConfig;

import cn.game.core.net.vertx.VxHolder;

/**    
 * 多数据源实现，主要用来获取多个shardingsphere连接，同时只会连接一个。 
 * @date 2022年6月21日 下午3:49:24
 * @author SYQ
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

	private Map<Object, Object> targetDataSources;
	private List<String> targetDataSourcesList;
	private String nodePath;
	private String serverId;
	private CuratorFramework curator;
	// 只是为了先初始化VxHolder
	@Autowired
	private VxHolder vxHolder;
	/** 尽量复用单数据源的配置 */
	@Autowired
	private HikariConfig hikariConfig;
	private String currentDataSourceKey;

	public DynamicDataSource(String nodePath, String serverId) {
		this.nodePath = nodePath;
		this.serverId = serverId;
	}

	/** 
	 * 从zk节点中读取数据源ip和端口，结合jdbc的配置，生成多数据源
	 * @throws Exception
	 */
	public void init() throws Exception {
		targetDataSources = new HashMap<>();
		targetDataSourcesList = new ArrayList<>();
		CuratorFramework curatorFramework = VxHolder.getZookeeperClusterManager().getCuratorFramework();
		this.curator = curatorFramework;
		List<String> shardingNodes = curator.getChildren().forPath(nodePath);
		if (shardingNodes.isEmpty()) {
			throw new IllegalArgumentException("DynamicDataSource init error , no data sources found ! ");
		}
		for (String shardingNode : shardingNodes) {
			byte[] forPath = curator.getData().forPath(nodePath + "/" + shardingNode);
			shardingNode = new String(forPath);
			addDataSource(shardingNode);
		}
		PathChildrenCache childrenCache = new PathChildrenCache(curator, nodePath, true);

		childrenCache.getListenable().addListener(new PathChildrenCacheListener() {

			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
				String data = new String(event.getData().getData());
				switch (event.getType()) {
					case CHILD_ADDED:
						addDataSource(data);
						break;
					case CHILD_REMOVED:
						removeDataSource(data);
						break;
					default:
						break;
				}
				logger.info("sharding data source event :  " + event);
			}
		});
		childrenCache.start(PathChildrenCache.StartMode.NORMAL);
	}

	@Override
	public void afterPropertiesSet() {
		try {
			init();
		} catch (Exception e) {
			throw new RuntimeException(" DynamicDataSource init failed !", e);
		}
		super.afterPropertiesSet();
	}

	private void addDataSource(String shardingNode) {
		String addr = extractedIpAndPort(shardingNode);
		Object object = targetDataSources.get(addr);
		if (object != null) {
			logger.warn("add exist dataSource ： " + shardingNode);
			return;
		}
		
		// 替换数据库连接中的ip和端口。 
		String url = hikariConfig.getDataSourceProperties().getProperty("url");
		int startIndex = url.indexOf("//");
		int endIndex = url.lastIndexOf("/");
		String ipAndPort = url.substring(startIndex + 2, endIndex);
		url = url.replace(ipAndPort, addr);

		hikariConfig.getDataSourceProperties().setProperty("url", url);
		// 生成数据源
		com.zaxxer.hikari.HikariDataSource dataSource = new com.zaxxer.hikari.HikariDataSource(hikariConfig);
		targetDataSources.put(addr, dataSource);
		targetDataSourcesList.add(addr);
		setTargetDataSources(targetDataSources);
		super.afterPropertiesSet();
		determineDataSourceKey();
	}
	/** 
	 * 从zookeeper节点数据里提取出shardingsphere的地址(ip:port 格式)
	 * @param shardingNode
	 * @return
	 */
	private String extractedIpAndPort(String shardingNode) {

		Pattern pattern = Pattern.compile("(\\d{1,3}\\.){3}\\d{1,3}@(\\d{1,5})");
		Matcher matcher = pattern.matcher(shardingNode);
		if (!matcher.find()) {
			throw new IllegalArgumentException("shardingNode addr  error : " + shardingNode);
		}
		String ip = matcher.group(0).split("@")[0];
		String port = matcher.group(0).split("@")[1];
		return ip + ":" + port;

	}
	private void removeDataSource(String shardingNode) {
		String addr = extractedIpAndPort(shardingNode);
		targetDataSources.remove(addr);
		targetDataSourcesList.remove(addr);
		setTargetDataSources(targetDataSources);
		super.afterPropertiesSet();
		determineDataSourceKey();
	}
	/** 
	 * 选择当前连接的数据源，设置key，获取数据源时用到。 
	 */
	private void determineDataSourceKey() {
		int index = Math.abs(serverId.hashCode()) % targetDataSourcesList.size();
		this.currentDataSourceKey = targetDataSourcesList.get(index);
	}
	@Override
	protected Object determineCurrentLookupKey() {
		return currentDataSourceKey;
	}

}
