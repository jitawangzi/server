package cn.game.core.manager;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ExampleUnionManager {
	// 工会实体
	private static class Union {
		private final int id;
		private String name;
		private int level;

		public Union(int id, String name, int level) {
			this.id = id;
			this.name = name;
			this.level = level;
		}

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public int getLevel() {
			return level;
		}

		public void setLevel(int level) {
			this.level = level;
		}

		@Override
		public String toString() {
			return "Player{id=" + id + ", name='" + name + "', level=" + level + '}';
		}
	}

	// 工会管理器（使用独占存储）
	public static class PlayerManager extends AbstractExclusiveManager<Integer, Union> {
		// 可添加特定于工会管理的额外方法
		public List<Union> getHighLevelPlayers(int minLevel) {
			return (List<Union>) doFindByPredicate(player -> player.getLevel() >= minLevel);
		}

		public void levelUp(int playerId) {
			Union player = doGet(playerId);
			if (player != null) {
				player.setLevel(player.getLevel() + 1);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		// 创建并配置工会管理器
		PlayerManager playerManager = new PlayerManager();
		playerManager.setCapacity(1000);
		playerManager.setEvictionStrategy(AbstractManagerTemplate.EvictionPolicy.LEAST_RECENTLY_USED);

		// 添加事件监听器
		playerManager.addListener(new AbstractManagerTemplate.ManagerEventListener<Union>() {
			@Override
			public void onObjectAdded(Union obj) {
				System.out.println("Player added: " + obj.getName());
			}

			@Override
			public void onObjectRemoved(Union obj) {
				System.out.println("Player removed: " + obj.getName());
			}

			@Override
			public void onObjectAccessed(Union obj) {
				// 可以用于追踪访问模式
			}
		});

		// 添加工会
		playerManager.add(1, new Union(1, "Alice", 10), "active", "vip");
		playerManager.add(2, new Union(2, "Bob", 5), "active", "new");
		playerManager.add(3, new Union(3, "Charlie", 20), "active", "vip", "guild_leader");

		// 添加临时工会（5秒后过期）
		playerManager.addWithExpiry(4, new Union(4, "Dave", 1), 5, TimeUnit.SECONDS, "temporary");

		// 获取指定工会
		Union alice = playerManager.get(1);
		System.out.println("Retrieved: " + alice);

		// 按标签获取工会
		List<Union> vipPlayers = (List<Union>) playerManager.getByLabels("vip");
		System.out.println("VIP players: " + vipPlayers.size());

		// 使用谓词查找
		List<Union> highLevelPlayers = playerManager.getHighLevelPlayers(10);
		System.out.println("High level players: " + highLevelPlayers.size());

		// 分页和排序
		List<Union> result = (List<Union>) playerManager.getPagedAndSorted(0, 10, Comparator.comparingInt(Union::getLevel).reversed(),
				"active");

		System.out.println("Top players by level:");
		for (Union p : result) {
			System.out.println(p);
		}

		// 等待临时工会过期
		System.out.println("Waiting for temporary player to expire...");
		Thread.sleep(6000);

		Union dave = playerManager.get(4);
		System.out.println("Dave still exists: " + (dave != null));

		// 清理
		playerManager.clear();
		System.out.println("Players after clear: " + playerManager.count());

		// 关闭管理器
		playerManager.shutdown();
	}
}