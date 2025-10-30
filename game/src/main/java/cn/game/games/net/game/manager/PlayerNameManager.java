package cn.game.games.net.game.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.redisson.api.RFuture;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RSetAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.util.StringUtils;

import cn.game.core.async.retry.AsyncRetry;
import cn.game.core.async.retry.RetryConfig;
import cn.game.core.cache.CacheType;
import cn.game.games.cache.entity.PlayerData;
import cn.game.protocol.generated.config.RandomNameConfig;
import cn.game.protocol.generated.manager.RandomNameManager;
import cn.game.util.RedisUtil;
import cn.game.util.Rnd;
import io.vertx.core.Future;


/**    
 * 分布式环境下，借助redis实现用户名唯一。
 * 2024年8月30日 上午11:52:03
 * @author SYQ
 */
public class PlayerNameManager {
	private static Logger log = LoggerFactory.getLogger(PlayerNameManager.class);

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
	 * 创建一个用户名，如果重复了，最多重试3次，添加数字后缀，确保名字创建成功。 
	 * @param username 想要创建的用户名
	 * @return  创建成功的用户名
	 */
	public CompletionStage<String> createUserName(String username) {
		return tryCreateUsername(username, 0);
	}

	private CompletionStage<String> tryCreateUsername(String baseUsername, int attempts) {
		if (attempts >= 3) {
			return CompletableFuture.failedFuture(new IllegalStateException("Failed to create unique username: " + baseUsername));
		}
		String attemptUsername = attempts == 0 ? baseUsername : baseUsername + ThreadLocalRandom.current().nextInt(10000);
		String key = getUsernameKey(attemptUsername);
		RSetAsync<String> set = RedisUtil.getRedis().getSet(key);

		return set.addAsync(attemptUsername).thenComposeAsync(success -> {
			if (success) {
				return CompletableFuture.completedFuture(attemptUsername);
			}
			return tryCreateUsername(baseUsername, attempts + 1);
		});
	}

	public CompletionStage<String> createUserNameRetry(String username) {
		RetryConfig config = new RetryConfig.Builder().maxAttempts(3)
				.retryIf(success -> !((Boolean) success)) // 当添加失败时重试
				.build();

		return AsyncRetry.execute(attempt -> {
			String attemptUsername = attempt == 0 ? username : username + ThreadLocalRandom.current().nextInt(10000);
			String key = getUsernameKey(attemptUsername);
			RSetAsync<String> set = RedisUtil.getRedis().getSet(key);
			return set.addAsync(attemptUsername).thenApply(success -> success ? attemptUsername : null);
		}, config).thenCompose(result -> {
			if (result.isSuccess() && result.getData() != null) {
				return CompletableFuture.completedFuture(result.getData());
			}
			return CompletableFuture.failedFuture(new IllegalStateException(
					String.format("Failed to create unique username after %d attempts", result.getAttemptCount())));
		});
	}

	public Future<String> createUserName() {
		return Future.fromCompletionStage(createUserName(randomName()));
	}

	public Future<PlayerData> saveName2Id(PlayerData player) {
		String key = getUsernameIdKey(player.getName());
		RMap<String, Long> map = RedisUtil.getRedis().getMap(key);
		RFuture<Long> putAsync = map.putAsync(player.getName(), player.getPlayerId());
		return Future.fromCompletionStage(putAsync).map(r -> player);
	}

	public RFuture<Long> saveName2Id(String name, long playerId) {
		String key = getUsernameIdKey(name);
		RMap<String, Long> map = RedisUtil.getRedis().getMap(key);
		RFuture<Long> putAsync = map.putAsync(name, playerId);
		return putAsync;
	}

	public Long saveName2IdSync(String name, long playerId) {
		String key = getUsernameIdKey(name);
		RMap<String, Long> map = RedisUtil.getRedis().getMap(key);
		return map.put(name, playerId);
	}

	/** 
	 * 删除某个存在的名字
	 * @param name
	 * @return
	 */
	public CompletionStage<Long> removeName(String name) {
		String key = getUsernameKey(name);
		String keyId = getUsernameIdKey(name);

		RSet<String> set = RedisUtil.getRedis().getSet(key);
		RMap<String, Long> map = RedisUtil.getRedis().getMap(keyId);
		
		return set.removeAsync(name).thenCompose(r -> {
			return map.removeAsync(name);
		}).exceptionally(t -> {
			log.error("removeName: " + name + "error", t);
			return 0L;
		});
	}

	public Future<Long> getPlayerIdAsync(String name) {
		if (StringUtils.isEmpty(name)) {
			return Future.succeededFuture();
		}
		String key = getUsernameIdKey(name);
		RMap<String, Long> map = RedisUtil.getRedis().getMap(key);
		return Future.fromCompletionStage(map.getAsync(name));
	}
	public long getPlayerId(String name) {
		if (StringUtils.isEmpty(name)) {
			return 0;
		}
		String key = getUsernameIdKey(name);
		RMap<String, Long> map = RedisUtil.getRedis().getMap(key);
		Long id = map.get(name);
		return id == null ? 0 : id;
	}

	public CompletableFuture<Map<String, Long>> getPlayerIds(Set<String> names) {
		log.info("getPlayerIds names:{}", names);
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
		RandomNameConfig randomOne = Rnd.randomElement(list);
		String xing = randomOne.Familyname;
		String name1;
		String name2;
		boolean isMan = Rnd.nextBoolean();
		if (isMan) {
			name1 = Rnd.randomElement(list).Name;
//			name2 = Rnd.randomOne(list).MenName2;
		} else {
			name1 = Rnd.randomElement(list).Name;
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
		List<Integer> shards = IntStream.range(0, SHARD_COUNT).boxed().collect(Collectors.toList());
		Collections.shuffle(shards);

		return queryNextShard(shards, 0, count, result);
	}

	private CompletionStage<Set<String>> queryNextShard(List<Integer> shards, int index, int count, Set<String> result) {
		if (result.size() >= count || index >= shards.size()) {
			return CompletableFuture.completedFuture(result.stream().limit(count).collect(Collectors.toSet()));
		}

		int shard = shards.get(index);
		String key = CacheType.SET_ALL_NAME.key(shard);
		RSetAsync<String> set = RedisUtil.getRedis().getSet(key);

		return set.randomAsync(count - result.size()).thenCompose(randomElements -> {
			result.addAll(randomElements);
			return queryNextShard(shards, index + 1, count, result);
		}).exceptionally(e -> {
			log.error("Error fetching random usernames from shard " + shard, e);
			// 直接返回当前结果
			return result.stream().limit(count).collect(Collectors.toSet());
		});
	}

	/** 
	 * 获取所有用户名，只是测试用。 
	 * @return
	 */
	public Set<String> testGetAllName() {
		Set<String> result = new HashSet<>();

		for (int i = 0; i < SHARD_COUNT; i++) {
			String key = CacheType.SET_ALL_NAME.key(i);
			RSet<String> set = RedisUtil.getRedis().getSet(key);
			Set<String> all = set.readAll();
			result.addAll(all);
		}
		return result;
	}

	/** 
	 * 获取所有用户id，只是测试用。 
	 * @return
	 */
	public Set<Long> testGetAllPlayerId() {
		Set<Long> result = new HashSet<>();
		
		for (int i = 0; i < SHARD_COUNT; i++) {
			String key = CacheType.MAP_PLAYER_NAME_ID.key(i);
			RMap<String, Long> map = RedisUtil.getRedis().getMap(key);
			Map<String, Long> allMap = map.readAllMap();
			result.addAll(allMap.values());
		}
		return result;
	}

	public Map<String, Long> testGetAllPlayerIdMap() {
		Map<String, Long> ret = new HashMap<>();

		for (int i = 0; i < SHARD_COUNT; i++) {
			String key = CacheType.MAP_PLAYER_NAME_ID.key(i);
			RMap<String, Long> map = RedisUtil.getRedis().getMap(key);
			Map<String, Long> allMap = map.readAllMap();
			ret.putAll(allMap);
		}
		return ret;
	}

}