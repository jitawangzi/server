package cn.game.core.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GameEntityManagerExample {
    // 游戏实体类
    public static class GameEntity {
        private final String id;
        private String name;
        private EntityType type;
        private Map<String, Object> properties = new HashMap<>();
        
        public enum EntityType {
            PLAYER, MONSTER, NPC, ITEM, SKILL
        }
        
        public GameEntity(String id, String name, EntityType type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public EntityType getType() { return type; }
        
        public void setProperty(String key, Object value) {
            properties.put(key, value);
        }
        
        public Object getProperty(String key) {
            return properties.get(key);
        }
        
        @Override
        public String toString() {
            return "GameEntity{id='" + id + "', name='" + name + "', type=" + type + '}';
        }
    }
    
    // 最小化分片存储
    public static class MinimalEntityManager extends AbstractShardedManager<String, GameEntity> {
        public MinimalEntityManager() {
            super(ManagerConfig.minimal());
        }
    }
    
    // 带缓存功能的分片存储
    public static class CachedEntityManager extends AbstractShardedManager<String, GameEntity> {
        public CachedEntityManager(int cacheSize) {
            super(ManagerConfig.builder()
                    .withCapacityLimit(cacheSize)
                    .withExpiry()
                    .withEviction(EvictionPolicy.LEAST_RECENTLY_USED)
                    .build());
        }
    }
    
    // 完整功能的分片存储
    public static class FullFeaturedEntityManager extends AbstractShardedManager<String, GameEntity> {
        public FullFeaturedEntityManager() {
            super(ManagerConfig.full());
        }
        
        // 根据实体类型获取所有实体
        public List<GameEntity> getEntitiesByType(GameEntity.EntityType type) {
            return (List<GameEntity>) doFindByPredicate(entity -> entity.getType() == type);
        }
        
        // 获取某个区域内的所有实体
        public List<GameEntity> getEntitiesInZone(String zoneId) {
            return (List<GameEntity>) getByPath("zone", zoneId);
        }
        
        // 获取某个区域内的特定类型实体
        public List<GameEntity> getEntitiesInZoneByType(String zoneId, GameEntity.EntityType type) {
            return (List<GameEntity>) doFindByPredicate(entity -> 
                hasLabel(entity.getId(), "zone:" + zoneId) && 
                entity.getType() == type);
        }
    }
    
    public static void main(String[] args) throws Exception {
        // 1. 最小化配置示例
        System.out.println("=== 最小化配置示例 ===");
        MinimalEntityManager minimalManager = new MinimalEntityManager();
        
        // 基本的分片存储
        minimalManager.add("item1", new GameEntity("item1", "Sword", GameEntity.EntityType.ITEM), "items", "weapons");
        minimalManager.add("item2", new GameEntity("item2", "Shield", GameEntity.EntityType.ITEM), "items", "armor");
        
        // 获取所有武器
        Collection<GameEntity> weapons = minimalManager.getByLabels("weapons");
        System.out.println("Weapons count: " + weapons.size());
        
        // 获取所有物品
        Collection<GameEntity> items = minimalManager.getByLabels("items");
        System.out.println("Items count: " + items.size());
        
        // 2. 缓存管理器示例
        System.out.println("\n=== 缓存管理器示例 ===");
        CachedEntityManager cacheManager = new CachedEntityManager(100);
        
        // 添加临时对象
        System.out.println("Adding temporary entities...");
        for (int i = 1; i <= 10; i++) {
            // 创建玩家，在不同区域
            String zoneId = "zone" + ((i-1) % 3 + 1);
            cacheManager.add(
                "player" + i, 
                new GameEntity("player" + i, "Player " + i, GameEntity.EntityType.PLAYER),
                "players", zoneId, "active"
            );
            
            // 创建怪物，在不同区域，设置5秒过期
            cacheManager.addWithExpiry(
                "monster" + i,
                new GameEntity("monster" + i, "Monster " + i, GameEntity.EntityType.MONSTER),
                5, TimeUnit.SECONDS,
                "monsters", zoneId
            );
        }
        
        // 按路径获取内容
        Collection<GameEntity> zone1Entities = cacheManager.getByPath("zone1");
        System.out.println("Zone 1 entities: " + zone1Entities.size());
        
        Collection<GameEntity> allPlayers = cacheManager.getByPath("players");
        System.out.println("All players: " + allPlayers.size());
        
        // 等待怪物过期
        System.out.println("Waiting for monsters to expire...");
        Thread.sleep(6000);
        
        Collection<GameEntity> monstersLeft = cacheManager.getByLabels("monsters");
        System.out.println("Monsters left after expiry: " + monstersLeft.size());
        
        // 3. 完整功能管理器示例
        System.out.println("\n=== 完整功能管理器示例 ===");
        FullFeaturedEntityManager fullManager = new FullFeaturedEntityManager();
        
        // 添加事件监听器
        fullManager.addListener(new AbstractManagerTemplate.ManagerEventListener<GameEntity>() {
            @Override
            public void onObjectAdded(GameEntity obj) {
                System.out.println("Event: Entity added: " + obj.getName());
            }
            
            @Override
            public void onObjectRemoved(GameEntity obj) {
                System.out.println("Event: Entity removed: " + obj.getName());
            }
            
            @Override
            public void onObjectAccessed(GameEntity obj) {
                // 可用于追踪访问
            }
        });
        
        // 创建复杂的分层结构
        // 区域 -> 类型 -> 实体
        for (int z = 1; z <= 3; z++) {
            String zoneId = "zone" + z;
            
            // 添加NPC到每个区域
            for (int i = 1; i <= 2; i++) {
                String npcId = "npc_" + zoneId + "_" + i;
                fullManager.add(
                    npcId,
                    new GameEntity(npcId, "NPC " + i + " in " + zoneId, GameEntity.EntityType.NPC),
                    zoneId, "npcs"
                );
            }
            
            // 添加物品到每个区域
            for (int i = 1; i <= 3; i++) {
                String itemId = "item_" + zoneId + "_" + i;
                fullManager.add(
                    itemId,
                    new GameEntity(itemId, "Item " + i + " in " + zoneId, GameEntity.EntityType.ITEM),
                    zoneId, "items"
                );
            }
        }
        
        // 测试专用方法
        List<GameEntity> zone2Entities = fullManager.getEntitiesInZone("zone2");
        System.out.println("Zone 2 entities: " + zone2Entities.size());
        
        List<GameEntity> allNpcs = fullManager.getEntitiesByType(GameEntity.EntityType.NPC);
        System.out.println("All NPCs: " + allNpcs.size());
        
        List<GameEntity> zone3Items = fullManager.getEntitiesInZoneByType("zone3", GameEntity.EntityType.ITEM);
        System.out.println("Zone 3 items: " + zone3Items.size());
        
        // 标签操作
        String testItemId = "item_zone1_1";
        System.out.println("Item labels before: " + fullManager.getLabels(testItemId));
		fullManager.addLabelPath(testItemId, "rare");
        System.out.println("Item labels after adding 'rare': " + fullManager.getLabels(testItemId));
        
        // 获取标签统计
        Map<String, Integer> stats = fullManager.getStatistics();
        System.out.println("\nLabel statistics:");
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        
        // 清理
        fullManager.clear();
        System.out.println("Entities after clear: " + fullManager.count());
        
        // 关闭管理器
        cacheManager.shutdown();
        fullManager.shutdown();
    }
}