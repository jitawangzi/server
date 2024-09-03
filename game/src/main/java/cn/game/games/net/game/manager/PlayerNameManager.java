package cn.game.games.net.game.manager;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.redisson.api.RFuture;
import org.redisson.api.RSet;
import org.redisson.api.RSetAsync;

import cn.game.core.cache.CacheType;
import cn.game.util.RedisUtil;


/**    
 * 分布式环境下，借助redis实现用户名唯一。
 * 2024年8月30日 上午11:52:03
 * @author SYQ
 */
public class PlayerNameManager {
	private static final int SHARD_COUNT = 64; // 使用64个分片
	private static PlayerNameManager instance = new PlayerNameManager();

	private PlayerNameManager() {
	};


	public RFuture<Boolean> isUsernameTaken(String username) {
		String key = getUsernameKey(username);
		RSetAsync<String> set = RedisUtil.getRedis().getSet(key);
		return set.containsAsync(username);
	}

	public RFuture<Boolean> tryCreateUser(String username) {
		String key = getUsernameKey(username);
		RSetAsync<String> set = RedisUtil.getRedis().getSet(key);
		return set.addAsync(username);
	}

	private String getUsernameKey(String username) {
		int shard = Math.abs(username.hashCode()) % SHARD_COUNT;
		return CacheType.SET_ALL_NAME.key(shard);
	}

	public boolean addExistingUsername(String username) {
		String key = getUsernameKey(username);
		RSet<String> set = RedisUtil.getRedis().getSet(key);
		return set.add(username);
	}

	/** 
	 * 用于初始化或迁移数据的方法
	 * @param usernames
	 */
	public void addExistingUsernames(Collection<String> usernames) {
		if (usernames.isEmpty()) {
			return;
		}
		// 按分片对用户名进行分组
		Map<String, List<String>> shardedUsernames = usernames.stream().collect(Collectors.groupingBy(this::getUsernameKey));

		// 为每个分片创建一个异步添加操作
		List<RFuture<Boolean>> futures = shardedUsernames.entrySet().stream().map(entry -> {
			RSetAsync<String> set = RedisUtil.getRedis().getSet(entry.getKey());
			return set.addAllAsync(entry.getValue());
		}).collect(Collectors.toList());

		// 等待所有异步操作完成
		CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
		allFutures.join();
	}

	/** 
	 * 用于获取随机用户名的方法
	 * @return
	 */
	public RFuture<String> getRandomUsername() {
		int randomShard = ThreadLocalRandom.current().nextInt(SHARD_COUNT);
		String key = CacheType.SET_ALL_NAME.key(randomShard);
		RSetAsync<String> set = RedisUtil.getRedis().getSet(key);
		return set.randomAsync();
	}
}