package cn.game.core.manager;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 游戏实体管理器示例
 * 演示不同类型管理器的使用和功能
 */
public class GameEntityManagerExample {
    // 游戏实体类
    public static class GameEntity {
        private final String id;
        private String name;
        private EntityType type;
		private int level;
        private Map<String, Object> properties = new HashMap<>();
        
        public enum EntityType {
            PLAYER, MONSTER, NPC, ITEM, SKILL
        }
        
        public GameEntity(String id, String name, EntityType type) {
            this.id = id;
            this.name = name;
            this.type = type;
			this.level = 1;
        }
        
		public GameEntity(String id, String name, EntityType type, int level) {
			this.id = id;
			this.name = name;
			this.type = type;
			this.level = level;
		}

        public String getId() { return id; }
        public String getName() { return name; }
        public EntityType getType() { return type; }

		public int getLevel() {
			return level;
		}

		public void setLevel(int level) {
			this.level = level;
		}
        
        public void setProperty(String key, Object value) {
            properties.put(key, value);
        }
        
        public Object getProperty(String key) {
            return properties.get(key);
        }
        
        @Override
        public String toString() {
			return "GameEntity{id='" + id + "', name='" + name + "', type=" + type + ", level=" + level + '}';
		}
	}

	// 基础管理器示例 - 仅实现Manager接口
	public static class BasicEntityManager extends AbstractManagerTemplate<String, GameEntity> {
		private final Map<String, GameEntity> storage = new HashMap<>();

		public BasicEntityManager() {
			super(ManagerConfig.minimal());
		}

		@Override
		protected void doAdd(String id, GameEntity obj) {
			storage.put(id, obj);
		}

		@Override
		protected GameEntity doGet(String id) {
			return storage.get(id);
		}

		@Override
		protected Collection<GameEntity> doGetAll() {
			return storage.values();
		}

		@Override
		protected Collection<String> doGetIdsAll() {
			return storage.keySet();
		}

		@Override
		protected boolean doRemove(String id) {
			return storage.remove(id) != null;
		}

		@Override
		protected void doClear() {
			storage.clear();
		}

		@Override
		protected int getTotalObjectCount() {
			return storage.size();
		}

	}

	// 标签管理器示例 - 实现TaggedManager接口
	public static class TaggedEntityManager extends AbstractTaggedManager<String, GameEntity> {
		private final Map<String, GameEntity> storage = new HashMap<>();

		public TaggedEntityManager() {
			super(ManagerConfig.builder().withEventNotification().build());
		}

		@Override
		protected void doAdd(String id, GameEntity obj) {
			storage.put(id, obj);
		}

		@Override
		protected void doAdd(String id, GameEntity obj, String... tags) {
			storage.put(id, obj);
			updateObjectTags(id, obj, tags);
		}

		@Override
		protected GameEntity doGet(String id) {
			return storage.get(id);
		}

		@Override
		protected Collection<GameEntity> doGetAll() {
			return storage.values();
		}

		@Override
		protected Collection<String> doGetIdsAll() {
			return storage.keySet();
		}

		@Override
		protected boolean doRemove(String id) {
			GameEntity removed = storage.remove(id);
			if (removed != null) {
				// 标签清理由父类处理
				return true;
			}
			return false;
		}

		@Override
		protected void doClear() {
			storage.clear();
			super.idToTags.clear();
			super.tagToObjects.clear();
		}

		@Override
		protected int getTotalObjectCount() {
			return storage.size();
        }

		@Override
		protected Collection<GameEntity> doGetByLabels(String... labels) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected Collection<String> doGetIdsByLabels(String... labels) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected void doAddWithExpiry(String id, GameEntity obj, long expiryTimeMs, String... tags) {
			// TODO Auto-generated method stub

		}

		@Override
		protected void doAddBatch(Map<String, GameEntity> objects, String... tags) {
			// TODO Auto-generated method stub

		}
    }
    
    // 最小化分片存储
    public static class MinimalEntityManager extends AbstractHierarchicalTagManager<String, GameEntity> {
        public MinimalEntityManager() {
            super(ManagerConfig.minimal());
        }
    }
    
    // 带缓存功能的分片存储
    public static class CachedEntityManager extends AbstractHierarchicalTagManager<String, GameEntity> {
        public CachedEntityManager(int cacheSize) {
            super(ManagerConfig.builder()
                    .withCapacityLimit(cacheSize)
                    .withExpiry()
                    .withEviction(EvictionPolicy.LEAST_RECENTLY_USED)
                    .build());
        }
    }
    
    // 完整功能的分片存储
    public static class FullFeaturedEntityManager extends AbstractHierarchicalTagManager<String, GameEntity> {
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
			hasTag(entity.getId(), "zone:" + zoneId) &&
                entity.getType() == type);
        }
    }
    
    public static void main(String[] args) throws Exception {
		// 0. 基础Manager接口示例
		testBasicManager();

		// 1. 标签管理器接口示例
		testTaggedManager();

		// 2. 最小化配置示例
		testMinimalHierarchicalManager();

		// 3. 缓存管理器示例
		testCachedManager();

		// 4. 完整功能管理器示例
		testFullFeaturedManager();

		// 5. 高级层级结构和路径测试
		testAdvancedHierarchicalFeatures();
	}

	/**
	 * 测试基础Manager接口实现
	 */
	private static void testBasicManager() {
		System.out.println("\n========== 基础Manager接口示例 ==========");
		BasicEntityManager basicManager = new BasicEntityManager();

		// 基本的ID-对象映射
		basicManager.add("player1", new GameEntity("player1", "Alice", GameEntity.EntityType.PLAYER));
		basicManager.add("player2", new GameEntity("player2", "Bob", GameEntity.EntityType.PLAYER));

		// 批量添加
		Map<String, GameEntity> batch = new HashMap<>();
		batch.put("weapon1", new GameEntity("weapon1", "Sword", GameEntity.EntityType.ITEM));
		batch.put("weapon2", new GameEntity("weapon2", "Axe", GameEntity.EntityType.ITEM));
		basicManager.addBatch(batch);

		// 查询
		System.out.println("Total entities: " + basicManager.count());
		GameEntity player = basicManager.get("player1");
		System.out.println("Found player: " + player);

		// 使用谓词查找
		Collection<GameEntity> items = basicManager.findByPredicate(entity -> entity.getType() == GameEntity.EntityType.ITEM);
		System.out.println("Found items: " + items.size());

		// 删除
		basicManager.remove("player1");
		System.out.println("Entities after removal: " + basicManager.count());

		// 清理
		basicManager.clear();
		System.out.println("Entities after clear: " + basicManager.count());
	}

	/**
	 * 测试标签管理器接口实现
	 */
	private static void testTaggedManager() {
		System.out.println("\n========== 标签管理器接口示例 ==========");
		TaggedEntityManager taggedManager = new TaggedEntityManager();

		// 添加事件监听器
		taggedManager.addListener(new Manager.ManagerEventListener<GameEntity>() {
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
				// 简化示例，不打印访问事件
			}
		});

		// 添加带标签的对象
		taggedManager.add("sword", new GameEntity("sword", "Steel Sword", GameEntity.EntityType.ITEM), "weapon", "melee", "metal");
		taggedManager.add("bow", new GameEntity("bow", "Wooden Bow", GameEntity.EntityType.ITEM), "weapon", "ranged", "wood");
		taggedManager.add("shield", new GameEntity("shield", "Iron Shield", GameEntity.EntityType.ITEM), "armor", "metal");

		// 按标签查询
		Collection<GameEntity> weapons = taggedManager.getByTags("weapon");
		System.out.println("Weapons count: " + weapons.size());

		Collection<GameEntity> metalItems = taggedManager.getByTags("metal");
		System.out.println("Metal items count: " + metalItems.size());

		// 多标签查询 (AND逻辑)
		Collection<GameEntity> meleeWeapons = taggedManager.getByTags("weapon", "melee");
		System.out.println("Melee weapons count: " + meleeWeapons.size());

		// 获取对象的标签
		Set<String> swordTags = taggedManager.getTags("sword");
		System.out.println("Sword tags: " + swordTags);

		// 判断对象是否有标签
		boolean isWeapon = taggedManager.hasTag("bow", "weapon");
		System.out.println("Is bow a weapon? " + isWeapon);

		// 添加标签
		taggedManager.addTag("shield", "rare");
		System.out.println("Shield tags after adding 'rare': " + taggedManager.getTags("shield"));

		// 移除标签
		taggedManager.removeTag("shield", "metal");
		System.out.println("Shield tags after removing 'metal': " + taggedManager.getTags("shield"));

		// 获取所有标签
		Set<String> allTags = taggedManager.getAllTags();
		System.out.println("All tags: " + allTags);

		// 分页和排序
		for (int i = 1; i <= 10; i++) {
			taggedManager.add("item" + i, new GameEntity("item" + i, "Item " + i, GameEntity.EntityType.ITEM, i), "common");
		}

		Collection<GameEntity> pagedItems = taggedManager.getPagedAndSorted(0, 5, Comparator.comparing(GameEntity::getLevel).reversed(),
				"common");
		System.out.println("Paged items (page 0, size 5, sorted by level desc):");
		for (GameEntity entity : pagedItems) {
			System.out.println(" - " + entity);
		}

		// 移除对象
		taggedManager.remove("sword");
		System.out.println("Weapons count after removing sword: " + taggedManager.getByTags("weapon").size());
	}

	/**
	 * 测试最小层级管理器
	 */
	private static void testMinimalHierarchicalManager() {
		System.out.println("\n========== 最小化层级管理器示例 ==========");
        MinimalEntityManager minimalManager = new MinimalEntityManager();
        
        // 基本的分片存储
        minimalManager.add("item1", new GameEntity("item1", "Sword", GameEntity.EntityType.ITEM), "items", "weapons");
        minimalManager.add("item2", new GameEntity("item2", "Shield", GameEntity.EntityType.ITEM), "items", "armor");
        
        // 获取所有武器
		Collection<GameEntity> weapons = minimalManager.getByTags("weapons");
        System.out.println("Weapons count: " + weapons.size());
        
        // 获取所有物品
		Collection<GameEntity> items = minimalManager.getByTags("items");
        System.out.println("Items count: " + items.size());
        
		// 使用层级路径
		Collection<GameEntity> itemsByPath = minimalManager.getByPath("items");
		System.out.println("Items by path: " + itemsByPath.size());

		// 添加多级路径
		minimalManager.addWithPaths("potion", new GameEntity("potion", "Health Potion", GameEntity.EntityType.ITEM),
				new String[] { "items", "consumables", "health" });

		// 查询子路径
		Collection<GameEntity> consumables = minimalManager.getByPath("items", "consumables");
		System.out.println("Consumables count: " + consumables.size());

		// 获取对象的所有路径
		List<String[]> potionPaths = minimalManager.getLabelPaths("potion");
		System.out.println("Potion has " + potionPaths.size() + " paths");
		for (String[] path : potionPaths) {
			System.out.println(" - Path: " + String.join(" -> ", path));
		}
	}

	/**
	 * 测试缓存管理器
	 */
	private static void testCachedManager() throws InterruptedException {
		System.out.println("\n========== 缓存管理器示例 ==========");
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
        
		Collection<GameEntity> monstersLeft = cacheManager.getByTags("monsters");
        System.out.println("Monsters left after expiry: " + monstersLeft.size());
        
		// 测试容量限制
		System.out.println("Testing capacity limit...");
		cacheManager.setCapacity(20); // 设置更小的容量

		// 添加足够多的对象触发淘汰
		for (int i = 1; i <= 30; i++) {
			cacheManager.add("overflow" + i, new GameEntity("overflow" + i, "Overflow " + i, GameEntity.EntityType.ITEM), "test",
					"overflow");
		}

		System.out.println("Actual entity count after overflow: " + cacheManager.count());
		System.out.println("Capacity enforced: " + (cacheManager.count() <= 20));

		// 关闭缓存管理器
		cacheManager.shutdown();
	}

	/**
	 * 测试完整功能管理器
	 */
	private static void testFullFeaturedManager() {
		System.out.println("\n========== 完整功能管理器示例 ==========");
        FullFeaturedEntityManager fullManager = new FullFeaturedEntityManager();
        
        // 添加事件监听器
		fullManager.addListener(new Manager.ManagerEventListener<GameEntity>() {
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
				// 可用于追踪访问，这里简化不打印
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
		System.out.println("Item tags before: " + fullManager.getTags(testItemId));
		fullManager.addTag(testItemId, "rare");
		System.out.println("Item tags after adding 'rare': " + fullManager.getTags(testItemId));

		// 添加层级路径
		fullManager.addLabelPath(testItemId, "special", "unique", "legendary");

		// 获取对象路径
		List<String[]> itemPaths = fullManager.getLabelPaths(testItemId);
		System.out.println("Item paths:");
		for (String[] path : itemPaths) {
			System.out.println(" - " + String.join(" -> ", path));
		}
        
        // 获取标签统计
		Map<String, Integer> stats = fullManager.getTagStatistics();
		System.out.println("\nTag statistics:");
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        
        // 清理
        fullManager.clear();
        System.out.println("Entities after clear: " + fullManager.count());
        
        // 关闭管理器
        fullManager.shutdown();
    }

	/**
	 * 测试高级层级结构和路径功能
	 */
	private static void testAdvancedHierarchicalFeatures() {
		System.out.println("\n========== 高级层级结构和路径功能测试 ==========");
		AbstractHierarchicalTagManager<String, GameEntity> hierarchyManager = new AbstractHierarchicalTagManager<String, GameEntity>(ManagerConfig.full()) {
		};

		// 创建带有多级路径的对象
		System.out.println("Adding entities with complex paths...");

		// 游戏地区结构
		// 世界 -> 大陆 -> 国家 -> 城市 -> 区域
		String[][] worldPaths = { { "world", "continent1", "kingdom1", "city1", "district1" },
				{ "world", "continent1", "kingdom1", "city1", "district2" }, { "world", "continent1", "kingdom1", "city2" },
				{ "world", "continent1", "kingdom2" }, { "world", "continent2", "empire1", "capital", "palace" },
				{ "world", "continent2", "empire1", "provinces", "north" }, { "world", "continent2", "empire1", "provinces", "south" } };

		// 在每个路径添加不同类型的实体
		int entityCounter = 0;
		for (String[] path : worldPaths) {
			// 添加NPC
			for (int i = 0; i < 2; i++) {
				entityCounter++;
				String npcId = "adv_npc_" + entityCounter;
				hierarchyManager.addWithPaths(npcId, new GameEntity(npcId, "NPC " + entityCounter, GameEntity.EntityType.NPC), path);
			}

			// 添加怪物
			for (int i = 0; i < 3; i++) {
				entityCounter++;
				String monsterId = "adv_monster_" + entityCounter;
				hierarchyManager.addWithPaths(monsterId,
						new GameEntity(monsterId, "Monster " + entityCounter, GameEntity.EntityType.MONSTER), path);
			}

			// 添加物品
			for (int i = 0; i < 2; i++) {
				entityCounter++;
				String itemId = "adv_item_" + entityCounter;
				hierarchyManager.addWithPaths(itemId, new GameEntity(itemId, "Item " + entityCounter, GameEntity.EntityType.ITEM), path);
			}
		}

		// 测试不同层级的路径查询
		testPathQuery(hierarchyManager, "世界总览", "world");
		testPathQuery(hierarchyManager, "大陆1所有实体", "world", "continent1");
		testPathQuery(hierarchyManager, "王国1所有实体", "world", "continent1", "kingdom1");
		testPathQuery(hierarchyManager, "城市1所有实体", "world", "continent1", "kingdom1", "city1");
		testPathQuery(hierarchyManager, "区域1所有实体", "world", "continent1", "kingdom1", "city1", "district1");
		testPathQuery(hierarchyManager, "帝国宫殿实体", "world", "continent2", "empire1", "capital", "palace");

		// 同一个对象放在多个路径下
		System.out.println("\n测试多路径对象：");
		String multiPathId = "multi_path_item";
		GameEntity multiPathItem = new GameEntity(multiPathId, "多路径宝物", GameEntity.EntityType.ITEM);

		// 在多个位置放置同一个物品
		hierarchyManager.addWithPaths(multiPathId, multiPathItem,
				new String[] { "world", "continent1", "kingdom1", "city1", "district1", "treasure" },
				new String[] { "world", "continent2", "empire1", "capital", "palace", "treasure" },
				new String[] { "special", "quest", "reward" });

		// 查看对象的所有路径
		List<String[]> itemPaths = hierarchyManager.getLabelPaths(multiPathId);
		System.out.println("多路径物品的所有路径：");
		for (String[] path : itemPaths) {
			System.out.println(" - " + String.join(" -> ", path));
		}

		// 不同路径查询同一个物品
		System.out.println("王国宝物中是否包含多路径物品："
				+ hierarchyManager.getByPath("world", "continent1", "kingdom1", "city1", "district1", "treasure").contains(multiPathItem));

		System.out.println("宫殿宝物中是否包含多路径物品："
				+ hierarchyManager.getByPath("world", "continent2", "empire1", "capital", "palace", "treasure").contains(multiPathItem));

		System.out.println("任务奖励中是否包含多路径物品：" + hierarchyManager.getByPath("special", "quest", "reward").contains(multiPathItem));

		// 路径操作
		System.out.println("\n路径操作测试：");
		// 添加新路径
		hierarchyManager.addLabelPath(multiPathId, "shop", "special", "rare");

		// 移除路径
		boolean removed = hierarchyManager.removeLabelPath(multiPathId, "special", "quest", "reward");
		System.out.println("移除路径结果：" + removed);

		// 查看更新后的路径
		itemPaths = hierarchyManager.getLabelPaths(multiPathId);
		System.out.println("操作后的多路径物品路径：");
		for (String[] path : itemPaths) {
			System.out.println(" - " + String.join(" -> ", path));
		}

		// 查询所有标签
		Set<String> allTags = hierarchyManager.getAllTags();
		System.out.println("\n系统中的所有标签：");
		System.out.println(allTags);

		hierarchyManager.shutdown();
	}

	/**
	 * 辅助方法：测试路径查询
	 */
	private static void testPathQuery(AbstractHierarchicalTagManager<String, GameEntity> manager, String description, String... path) {
		Collection<GameEntity> entities = manager.getByPath(path);

		// 统计不同类型的实体数量
		Map<GameEntity.EntityType, Integer> typeCounts = new HashMap<>();
		for (GameEntity entity : entities) {
			typeCounts.put(entity.getType(), typeCounts.getOrDefault(entity.getType(), 0) + 1);
		}

		System.out.println("\n" + description + " (" + String.join(" -> ", path) + "):");
		System.out.println("总实体数量: " + entities.size());
		for (Map.Entry<GameEntity.EntityType, Integer> entry : typeCounts.entrySet()) {
			System.out.println(" - " + entry.getKey() + ": " + entry.getValue());
		}
	}
}