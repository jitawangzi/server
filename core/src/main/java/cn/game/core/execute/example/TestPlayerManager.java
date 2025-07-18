package cn.game.core.execute.example;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import cn.game.core.execute.ExecutionMonitor;
import cn.game.core.execute.TaskExecutionConfig;
import cn.game.core.execute.TaskExecutorFactory;
import cn.game.core.execute.TaskExecutorService;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * 玩家管理器示例，展示如何使用执行框架
 */
public class TestPlayerManager {
    private static final Logger LOGGER = Logger.getLogger(TestPlayerManager.class.getName());
    
    private final TaskExecutorService executorService;
    private final Map<Long, PlayerData> playerCache = new ConcurrentHashMap<>();
    
    public TestPlayerManager(Vertx vertx) {
        // 创建自定义配置的执行服务
        TaskExecutionConfig config = TaskExecutionConfig.builder()
            .maxQueueSize(2000)           // 每个玩家最多2000个待处理任务
            .idleTimeoutMs(1800000)       // 30分钟不活动的玩家会被清理
            .defaultTaskTimeoutMs(5000)   // 默认任务超时5秒
            .build();
        
		this.executorService = TaskExecutorFactory.createExecutor(config);
    }
    
    /**
     * 加载玩家数据
     * @param playerId 玩家ID
     * @return 玩家数据Future
     */
    public Future<PlayerData> loadPlayerData(long playerId) {
        return executorService.execute(
            playerId,
            () -> {
                // 模拟从数据库加载数据
                LOGGER.info("Loading player data for " + playerId);
                PlayerData data = new PlayerData(playerId);
                data.setName("Player-" + playerId);
                data.setLevel(1);
                data.setGold(100);
                
                // 将数据放入缓存
                playerCache.put(playerId, data);
                LOGGER.info("Player data loaded: " + data);
                
                return data;
            },
            false,"LoadPlayerData-" + playerId
        );
    }
    
    /**
     * 同步加载玩家数据（必须在虚拟线程中调用）
     * @param playerId 玩家ID
     * @return 玩家数据
     * @throws Exception 如果加载失败
     */
    public PlayerData loadPlayerDataSync(long playerId) throws Exception {
        return executorService.executeAndAwait(
            playerId,
            () -> {
                // 模拟从数据库加载数据
                LOGGER.info("Loading player data for " + playerId);
                PlayerData data = new PlayerData(playerId);
                data.setName("Player-" + playerId);
                data.setLevel(1);
                data.setGold(100);
                
                // 将数据放入缓存
                playerCache.put(playerId, data);
                LOGGER.info("Player data loaded: " + data);
                
                return data;
            },
            false, "LoadPlayerData-" + playerId,
				 5000 
        );
    }
    
    /**
     * 增加玩家金币
     * @param playerId 玩家ID
     * @param amount 金币数量
     * @return 新的金币数量Future
     */
    public Future<Integer> addGold(long playerId, int amount) {
        return executorService.execute(
            playerId,
            () -> {
                PlayerData data = playerCache.get(playerId);
                if (data == null) {
                    throw new IllegalStateException("Player data not found for " + playerId);
                }
                
                data.setGold(data.getGold() + amount);
                LOGGER.info("Added " + amount + " gold to player " + playerId + ", new total: " + data.getGold());
                
                // 模拟数据库更新
                // ...
                
                return data.getGold();
            },
            "AddGold-" + playerId + "-" + amount
        );
    }
    
    /**
     * 同步增加玩家金币（必须在虚拟线程中调用）
     * @param playerId 玩家ID
     * @param amount 金币数量
     * @return 新的金币数量
     * @throws Exception 如果操作失败
     */
    public int addGoldSync(long playerId, int amount) throws Exception {
        return executorService.executeAndAwait(
            playerId,
            () -> {
                PlayerData data = playerCache.get(playerId);
                if (data == null) {
                    throw new IllegalStateException("Player data not found for " + playerId);
                }
                
                data.setGold(data.getGold() + amount);
                LOGGER.info("Added " + amount + " gold to player " + playerId + ", new total: " + data.getGold());
                
                // 模拟数据库更新
                // ...
                
                return data.getGold();
            },
            false,	"AddGold-" + playerId + "-" + amount,  0
        );
    }
    
    /**
     * 玩家升级
     * @param playerId 玩家ID
     * @return 新的等级Future
     */
    public Future<Integer> levelUp(long playerId) {
        return executorService.execute(
            playerId,
            () -> {
                PlayerData data = playerCache.get(playerId);
                if (data == null) {
                    throw new IllegalStateException("Player data not found for " + playerId);
                }
                
                data.setLevel(data.getLevel() + 1);
                LOGGER.info("Player " + playerId + " leveled up to " + data.getLevel());
                
                // 发放升级奖励
                int bonus = data.getLevel() * 100;
                executorService.execute(playerId, () -> {
                    data.setGold(data.getGold() + bonus);
                    LOGGER.info("Added level-up bonus of " + bonus + " gold to player " + playerId);
                    return null;
                }, "LevelUpReward-" + playerId);
                
                return data.getLevel();
            },
            "LevelUp-" + playerId
        );
    }
    
    /**
     * 同步玩家升级（必须在虚拟线程中调用）
     * @param playerId 玩家ID
     * @return 新的等级
     * @throws Exception 如果操作失败
     */
    public int levelUpSync(long playerId) throws Exception {
        return executorService.executeAndAwait(
            playerId,
            () -> {
                PlayerData data = playerCache.get(playerId);
                if (data == null) {
                    throw new IllegalStateException("Player data not found for " + playerId);
                }
                
                data.setLevel(data.getLevel() + 1);
                LOGGER.info("Player " + playerId + " leveled up to " + data.getLevel());
                
                // 发放升级奖励
                int bonus = data.getLevel() * 100;
                
                // 使用Vertx Future.await同步等待奖励发放完成
                int newGold = executorService.execute(playerId, () -> {
                    data.setGold(data.getGold() + bonus);
                    LOGGER.info("Added level-up bonus of " + bonus + " gold to player " + playerId);
                    return data.getGold();
                }, "LevelUpReward-" + playerId).await();
                
                LOGGER.info("Player " + playerId + " now has " + newGold + " gold after level-up reward");
                
                return data.getLevel();
            },
            false,"LevelUp-" + playerId, 0
        );
    }
    
    /**
     * 保存玩家数据
     * @param playerId 玩家ID
     * @return 成功保存的Future
     */
    public Future<Boolean> savePlayerData(long playerId) {
        return executorService.execute(
            playerId,
            () -> {
                PlayerData data = playerCache.get(playerId);
                if (data == null) {
                    throw new IllegalStateException("Player data not found for " + playerId);
                }
                
                // 模拟长时间数据库操作
                LOGGER.info("Saving player data for " + playerId);
                Thread.sleep(1000);
                LOGGER.info("Player data saved: " + data);
                
                return true;
            },
            false,"SavePlayerData-" + playerId,
            3000 // 3秒超时
        );
    }
    
    /**
     * 同步保存玩家数据（必须在虚拟线程中调用）
     * @param playerId 玩家ID
     * @return 是否成功保存
     * @throws Exception 如果保存失败
     */
    public boolean savePlayerDataSync(long playerId) throws Exception {
        return executorService.executeAndAwait(
            playerId,
            () -> {
                PlayerData data = playerCache.get(playerId);
                if (data == null) {
                    throw new IllegalStateException("Player data not found for " + playerId);
                }
                
                // 模拟长时间数据库操作
                LOGGER.info("Saving player data for " + playerId);
                Thread.sleep(1000);
                LOGGER.info("Player data saved: " + data);
                
                return true;
            },
            false,"SavePlayerData-" + playerId,
            3000 // 3秒超时
        );
    }
    
    /**
     * 关闭玩家管理器
     */
    public void shutdown() {
        executorService.close();
    }
    
    /**
     * 玩家数据类
     */
    public static class PlayerData {
        private final long id;
        private String name;
        private int level;
        private int gold;
        
        public PlayerData(long id) {
            this.id = id;
        }
        
        public long getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public int getLevel() {
            return level;
        }
        
        public void setLevel(int level) {
            this.level = level;
        }
        
        public int getGold() {
            return gold;
        }
        
        public void setGold(int gold) {
            this.gold = gold;
        }
        
        @Override
        public String toString() {
            return "PlayerData{" +
                   "id=" + id +
                   ", name='" + name + '\'' +
                   ", level=" + level +
                   ", gold=" + gold +
                   '}';
        }
    }
    
    /**
     * 示例用法
     */
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        TestPlayerManager playerManager = new TestPlayerManager(vertx);
        
        try {
            // 创建一个虚拟线程执行示例
            Thread.startVirtualThread(() -> {
                try {
                    // 加载几个玩家（同步方式）
                    for (int i = 1; i <= 5; i++) {
                        PlayerData player = playerManager.loadPlayerDataSync(i);
                        System.out.println("Loaded player: " + player);
                    }
                    
                    // 给每个玩家添加一些操作（同步方式）
                    for (int i = 1; i <= 5; i++) {
                        for (int j = 0; j < 3; j++) {
                            int newGold = playerManager.addGoldSync(i, 50);
                            System.out.println("Player " + i + " new gold: " + newGold);
                            
                            int newLevel = playerManager.levelUpSync(i);
                            System.out.println("Player " + i + " new level: " + newLevel);
                        }
                        
                        boolean saved = playerManager.savePlayerDataSync(i);
                        System.out.println("Player " + i + " data saved: " + saved);
                    }
                    
                    // 输出监控信息
                    ExecutionMonitor.MonitorSnapshot snapshot = playerManager.executorService.getMonitor().collectStatistics();
                    LOGGER.info("Final statistics: " + snapshot.totalCompletedTasks + " tasks completed, " +
                               snapshot.totalFailedTasks + " failed, " +
                               snapshot.totalTimeoutTasks + " timed out");
                    
                    // 关闭玩家管理器
                    playerManager.shutdown();
                    vertx.close();
                    
                } catch (Exception e) {
                    LOGGER.severe("Error in example: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
            // 等待虚拟线程完成
            Thread.sleep(10000);
            
        } catch (Exception e) {
            LOGGER.severe("Error in main: " + e.getMessage());
            e.printStackTrace();
            
            playerManager.shutdown();
            vertx.close();
        }
    }
}
