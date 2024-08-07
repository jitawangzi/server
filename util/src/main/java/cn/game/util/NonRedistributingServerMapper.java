package cn.game.util;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class NonRedistributingServerMapper {
	private final List<ServerEntry> servers = new ArrayList<>();
	private final ConcurrentHashMap<Long, String> playerServerCache = new ConcurrentHashMap<>();

	private static class ServerEntry {
		String name;
		long additionTimestamp;

		ServerEntry(String name, long additionTimestamp) {
			this.name = name;
			this.additionTimestamp = additionTimestamp;
		}
	}

	public synchronized void addServer(String serverName) {
		long timestamp = System.currentTimeMillis();
		servers.add(new ServerEntry(serverName, timestamp));
		// 按时间戳排序，确保最新的服务器在最后
		servers.sort(Comparator.comparingLong(s -> s.additionTimestamp));
	}

	public synchronized void removeServer(String serverName) {
		servers.removeIf(entry -> entry.name.equals(serverName));
		// 从缓存中移除该服务器的所有玩家
		playerServerCache.entrySet().removeIf(entry -> entry.getValue().equals(serverName));
	}

	public String getServerForPlayer(long playerId) {
		// 首先检查缓存
		String cachedServer = playerServerCache.get(playerId);
		if (cachedServer != null) {
			// 确保服务器仍然存在
			if (servers.stream().anyMatch(s -> s.name.equals(cachedServer))) {
				return cachedServer;
			} else {
				playerServerCache.remove(playerId);
			}
		}

		// 如果缓存中没有或服务器已被移除，重新分配
		String server = assignServer(playerId);
		if (server != null) {
			playerServerCache.put(playerId, server);
		}
		return server;
	}

	private String assignServer(long playerId) {
		if (servers.isEmpty()) {
			return null;
		}

		// 使用玩家ID的哈希值来选择服务器
		int serverIndex = Math.abs(Long.hashCode(playerId)) % servers.size();
		return servers.get(serverIndex).name;
	}

	public List<String> getAllServers() {
		return servers.stream().map(s -> s.name).toList();
	}
}