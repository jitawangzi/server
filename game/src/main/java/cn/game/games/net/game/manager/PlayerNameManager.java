package cn.game.games.net.game.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.redisson.api.RFuture;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RSetAsync;

import com.alibaba.druid.util.StringUtils;

import cn.game.core.cache.CacheType;
import cn.game.games.cache.entity.PlayerData;
import cn.game.protocol.generated.config.RandomNameConfig;
import cn.game.protocol.generated.manager.RandomNameManager;
import cn.game.util.RedisUtil;
import cn.game.util.Rnd;
import io.vertx.core.Future;
import io.vertx.core.Promise;


/**    
 * 分布式环境下，借助redis实现用户名唯一。
 * 2024年8月30日 上午11:52:03
 * @author SYQ
 */
public class PlayerNameManager {
	private static final int SHARD_COUNT = 16; // 使用16个分片
	private static PlayerNameManager instance = new PlayerNameManager();

	private PlayerNameManager() {
	};


	public static PlayerNameManager getInstance() {
		return instance;
	}

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
		int shard = getShardIndex(username);
		return CacheType.SET_ALL_NAME.key(shard);
	}

	private String getUsernameIdKey(String username) {
		int shard = getShardIndex(username);
		return CacheType.MAP_PLAYER_NAME_ID.key(shard);
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
	 * 创建一个用户名，如果重复了，则添加数字后缀，确保名字创建成功。 
	 * @param username 想要创建的用户名
	 * @return  创建成功的用户名
	 */
	public CompletionStage<String> createUserName(String username) {
		String key = getUsernameKey(username);
		AtomicBoolean retry = new AtomicBoolean(false);
		RSetAsync<String> set = RedisUtil.getRedis().getSet(key);
		RFuture<Boolean> async = set.addAsync(username);
		return async.thenComposeAsync(success -> {
			if (success) {
				return CompletableFuture.completedFuture(username);
			}
			if (!retry.get()) {
				retry.set(true);
				String newName = username + ThreadLocalRandom.current().nextInt(10000);
				return createUserName(newName);
			}
			throw new IllegalStateException("Failed to create unique username :" + username);
		});
	}

	public Future<String> createUserName() {
		return Future.fromCompletionStage(createUserName(randomName()));
	}

	public Future<PlayerData> saveName2Id(PlayerData player) {
		Promise<PlayerData> promise = Promise.promise();

		String key = getUsernameIdKey(player.getName());
		RMap<String, Long> map = RedisUtil.getRedis().getMap(key);
		RFuture<Long> putAsync = map.putAsync(player.getName(), player.getPlayerId());
		putAsync.onComplete((v,t) -> {
            if (v == null && t == null) {
                promise.complete(player);
            } else {
                promise.fail(t);
            }
		});
		return promise.future(); 
	}

	public Future<Long> getPlayerId(String name) {
		if (StringUtils.isEmpty(name)) {
			return Future.succeededFuture();
		}
		String key = getUsernameIdKey(name);
		RMap<String, Long> map = RedisUtil.getRedis().getMap(key);
		return Future.fromCompletionStage(map.getAsync(name));
	}

	public CompletableFuture<Map<String, Long>> getPlayerIds(Set<String> names) {
		// 按分片对用户名进行分组
		Map<Integer, List<String>> shardToNames = names.stream().collect(Collectors.groupingBy(this::getShardIndex));

		// 为每个分片创建一个异步任务
		List<CompletableFuture<Map<String, Long>>> futures = new ArrayList<>();
		for (Map.Entry<Integer, List<String>> entry : shardToNames.entrySet()) {
			int shard = entry.getKey();
			List<String> shardNames = entry.getValue();
			CompletableFuture<Map<String, Long>> future = getPlayerIdsFromShard(shard, new HashSet<>(shardNames));
			futures.add(future);
		}

		// 等待所有异步任务完成并合并结果
		return CompletableFuture
				.allOf(futures.toArray(new CompletableFuture[0]))
				.thenApply(v -> futures
						.stream()
						.map(CompletableFuture::join)
						.flatMap(map -> map.entrySet().stream())
						.collect(Collectors
								.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, // 如果有重复键，保留第一个值
										HashMap::new)));
	}

	private CompletableFuture<Map<String, Long>> getPlayerIdsFromShard(int shard, Set<String> names) {
		String key = CacheType.MAP_PLAYER_NAME_ID.key(shard);
		RMap<String, Long> map = RedisUtil.getRedis().getMap(key);
		RFuture<Map<String, Long>> future = map.getAllAsync(names);
		return future.toCompletableFuture();
	}

	private int getShardIndex(String username) {
		return Math.abs(username.hashCode()) % SHARD_COUNT;
	}

//	public RFuture<Map<String, Long>> getPlayerIds(Set<String> names) {
//		String key = getUsernameIdKey(player.getName());
//		RMap<String, Long> map = RedisUtil.getRedis().getMap(key);
//		return map.getAllAsync(names);
//	}
//
//	public Future<Collection<Long>> getPlayerIdList(Set<String> names) {
//		if (names == null || names.isEmpty()) {
//			return Future.succeededFuture();
//		}
//		String key = getUsernameIdKey(player.getName());
//		RMap<String, Long> map = RedisUtil.getRedis().getMap(key);
//		return Future.fromCompletionStage(map.getAllAsync(names)).map(map2 -> map2.values());
//	}

	private String randomName() {
		List<RandomNameConfig> list = RandomNameManager.instance().list();
		RandomNameConfig randomOne = Rnd.randomOne(list);
		String xing = randomOne.Familyname;
		String name1;
		String name2;
		boolean isMan = Rnd.nextBoolean();
		if (isMan) {
			name1 = Rnd.randomOne(list).Name;
//			name2 = Rnd.randomOne(list).MenName2;
		} else {
			name1 = Rnd.randomOne(list).Name;
//			name2 = Rnd.randomOne(list).WomenName2;
		}
		return xing + name1;
	}

	/** 
	 * 从某个分片中随机几个用户名
	 * @return
	 */
	public RFuture<Set<String>> getRandomUsername(int count) {
		int randomShard = ThreadLocalRandom.current().nextInt(SHARD_COUNT);
		String key = CacheType.SET_ALL_NAME.key(randomShard);
		RSetAsync<String> set = RedisUtil.getRedis().getSet(key);
		return set.randomAsync(count);
	}

	/** 
	 * 从所有分片中随机用户名
	 * @param count
	 * @return
	 */
	public CompletionStage<Set<String>> getRandomUsernameFromAll(int count) {
		Set<String> result = new HashSet<>();
		CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
		int maxAttempts = SHARD_COUNT; // 最大尝试次数
		int attempts = 0;

		// 循环直到获得所需数量的用户名或达到最大尝试次数
		while (result.size() < count && attempts < maxAttempts) {
			int randomShard = Rnd.nextInt(SHARD_COUNT);
			String key = CacheType.SET_ALL_NAME.key(randomShard);
			RSetAsync<String> set = RedisUtil.getRedis().getSet(key);

			// 使用 CompletableFuture 异步获取随机元素
			future = future.thenCompose(v -> set.randomAsync(count - result.size()).thenAccept(randomElements -> result.addAll(randomElements)));
			attempts++;
		}
		return future.thenApply(v -> {
			// 返回的结果集的大小不超过请求的数量
			return result.stream().limit(count).collect(Collectors.toSet());
		});
	}
}