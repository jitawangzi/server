package cn.game.games.net.game.gm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.game.games.core.event.PlayerEventHandler;

import cn.game.core.exception.LogicException;
import cn.game.core.util.AsyncUtils;
import cn.game.games.net.game.module.account.Account;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.PlayerWriteGuard;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.util.CborSnappyUtil;
import cn.game.util.DateUtil;

/**
 * 非生产玩家快照管理服务与真实冷重载 (DC-15, BR-14, EX-09, EX-10, AC-13, DR-07, DR-08, DR-09)
 * 为每位玩家维护最多 20 个命名快照 (LRU 淘汰)；
 * 提供 save/restore/list/delete 及带有前置落盘校验的 flushAndColdReload。
 *
 * @author shuyongqiang
 */
public class PlayerSnapshotService {

	private static final Logger log = LoggerFactory.getLogger(PlayerSnapshotService.class);
	private static final PlayerSnapshotService INSTANCE = new PlayerSnapshotService();

	public static PlayerSnapshotService getInstance() {
		return INSTANCE;
	}

	private final ConcurrentHashMap<Long, LinkedHashMap<String, PlayerSnapshotEntry>> playerSnapshots = new ConcurrentHashMap<>();

	private PlayerSnapshotService() {}

	/**
	 * 保存玩家当前状态命名快照
	 */
	public Map<String, Object> save(Player player, String snapshotName) {
		if (GmSecurityPolicy.currentRunMode().isProduction()) {
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID, "生产环境禁止执行玩家快照操作");
		}
		if (player == null) {
			throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "目标玩家不存在");
		}
		String name = (snapshotName != null && !snapshotName.isBlank()) ? snapshotName.trim() : "default";

		byte[] compressedModules = CborSnappyUtil.compress(player.getModules());
		Map<String, Object> scalarSummary = extractScalars(player);

		PlayerSnapshotEntry entry = new PlayerSnapshotEntry(
				name,
				DateUtil.currentTimeMillis(),
				compressedModules != null ? compressedModules.length : 0,
				compressedModules,
				scalarSummary);

		LinkedHashMap<String, PlayerSnapshotEntry> map = playerSnapshots.computeIfAbsent(player.getPlayerId(), k -> new LinkedHashMap<>(24, 0.75f, true));
		synchronized (map) {
			map.put(name, entry);
			while (map.size() > 20) {
				Iterator<String> it = map.keySet().iterator();
				if (it.hasNext()) {
					it.next();
					it.remove();
				}
			}
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("playerId", player.getPlayerId());
		result.put("snapshotName", name);
		result.put("createdAt", entry.getCreatedAtVirtualMs());
		result.put("byteSize", entry.getByteSize());
		result.put("scalarSummary", scalarSummary);
		return result;
	}

	/**
	 * 还原玩家命名快照
	 */
	public Map<String, Object> restore(Player player, String snapshotName) {
		if (GmSecurityPolicy.currentRunMode().isProduction()) {
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID, "生产环境禁止执行玩家快照操作");
		}
		if (player == null) {
			throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "目标玩家不存在");
		}
		String name = (snapshotName != null && !snapshotName.isBlank()) ? snapshotName.trim() : "default";

		LinkedHashMap<String, PlayerSnapshotEntry> map = playerSnapshots.get(player.getPlayerId());
		PlayerSnapshotEntry entry = null;
		if (map != null) {
			synchronized (map) {
				entry = map.get(name);
			}
		}
		if (entry == null) {
			throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "快照不存在: " + name);
		}

		final PlayerSnapshotEntry snapshot = entry;
		PlayerWriteGuard.run(player, () -> {
			if (player.getModules() != null) {
				for (BasePlayerModule oldModule : player.getModules().values()) {
					if (oldModule instanceof PlayerEventHandler handler) {
						player.getPlayerEventBus().unregister(handler);
					}
				}
			}
			HashMap<String, BasePlayerModule> restoredModules = CborSnappyUtil.decompress(snapshot.getCompressedModules());
			if (restoredModules != null) {
				player.initModule(restoredModules);
				for (BasePlayerModule module : player.getModuleSorted()) {
					module.initFromDbAfter();
				}
			}
			applyScalars(player, snapshot.getScalarSummary());
		});

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("playerId", player.getPlayerId());
		result.put("snapshotName", name);
		result.put("restoredModulesCount", player.getModules().size());
		result.put("restoredAt", DateUtil.currentTimeMillis());
		return result;
	}

	/**
	 * 列出玩家所有已保存快照元数据
	 */
	public List<Map<String, Object>> list(long playerId) {
		if (GmSecurityPolicy.currentRunMode().isProduction()) {
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID, "生产环境禁止执行玩家快照操作");
		}
		List<Map<String, Object>> list = new ArrayList<>();
		LinkedHashMap<String, PlayerSnapshotEntry> map = playerSnapshots.get(playerId);
		if (map != null) {
			synchronized (map) {
				for (PlayerSnapshotEntry entry : map.values()) {
					Map<String, Object> meta = new LinkedHashMap<>();
					meta.put("snapshotName", entry.getSnapshotName());
					meta.put("createdAt", entry.getCreatedAtVirtualMs());
					meta.put("byteSize", entry.getByteSize());
					list.add(meta);
				}
			}
		}
		return list;
	}

	/**
	 * 删除单个或全部快照
	 */
	public Map<String, Object> delete(long playerId, String snapshotName, boolean all) {
		if (GmSecurityPolicy.currentRunMode().isProduction()) {
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID, "生产环境禁止执行玩家快照操作");
		}
		int deletedCount = 0;
		LinkedHashMap<String, PlayerSnapshotEntry> map = playerSnapshots.get(playerId);
		if (map != null) {
			synchronized (map) {
				if (all) {
					deletedCount = map.size();
					map.clear();
				} else if (snapshotName != null && !snapshotName.isBlank()) {
					if (map.remove(snapshotName.trim()) != null) {
						deletedCount = 1;
					}
				}
			}
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("playerId", playerId);
		result.put("deletedCount", deletedCount);
		return result;
	}

	/**
	 * 强制落盘并冷重载全新 Player 实例 (DC-15, DR-09)
	 */
	public Map<String, Object> flushAndColdReload(long playerId) {
		if (GmSecurityPolicy.currentRunMode().isProduction()) {
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID, "生产环境禁止执行强制落盘与冷重载");
		}
		Player oldPlayer = PlayerManager.getInstance().getPlayer(playerId);
		if (oldPlayer == null) {
			throw new LogicException(ErrorMsgEnum.player_not_online.ID, "目标玩家不在线: " + playerId);
		}

		int oldIdentityHash = System.identityHashCode(oldPlayer);
		Account oldAccount = oldPlayer.getAccount();
		GameClient oldClient = oldPlayer.getGameClient();

		// 步骤 1：先等待落盘保存完成；若失败立即中止，绝不驱逐在线玩家 (EX-10, DR-09)
		try {
			AsyncUtils.await(PlayerHelper.saveClientCache(playerId), 10, TimeUnit.SECONDS);
		} catch (Throwable t) {
			log.error("强制落盘保存失败，中止冷重载 playerId={}", playerId, t);
			throw new LogicException(ErrorMsgEnum.unknown.ID, "强制落盘保存失败，中止冷重载: " + t.getMessage());
		}

		// 步骤 2：确认落盘成功后，从在线列表移除旧实例并取消定时器 (DEFECT-3)
		Player removed = PlayerManager.getInstance().deletePlayer(oldPlayer);
		if (removed == null) {
			log.error("在线旧玩家实例驱逐失败，中止冷重载 playerId={}", playerId);
			throw new LogicException(ErrorMsgEnum.unknown.ID, "在线旧玩家实例驱逐失败，中止冷重载: " + playerId);
		}
		oldPlayer.cancelAllTimer();

		// 步骤 3：从数据库冷重载全新 Player 实例并恢复会话状态
		Player newPlayer = null;
		try {
			newPlayer = AsyncUtils.await(PlayerHelper.loadPlayerFromDb(playerId), 10, TimeUnit.SECONDS);
		} catch (Throwable t) {
			log.error("从DB冷重载玩家异常 playerId={}", playerId, t);
			throw new LogicException(ErrorMsgEnum.unknown.ID, "从DB冷重载玩家异常: " + t.getMessage());
		}
		if (newPlayer == null) {
			throw new LogicException(ErrorMsgEnum.player_not_exist.ID, "从DB冷重载玩家返回空: " + playerId);
		}

		newPlayer.setOnline(true);
		newPlayer.setIslogining(false);
		if (oldAccount != null) {
			newPlayer.setAccount(oldAccount);
		}
		if (oldClient != null) {
			oldClient.setPlayerId(playerId);
			newPlayer.setGameClient(oldClient);
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("playerId", playerId);
		result.put("oldIdentityHash", oldIdentityHash);
		result.put("newIdentityHash", System.identityHashCode(newPlayer));
		result.put("reloadedFromDb", true);
		return result;
	}

	private Map<String, Object> extractScalars(Player player) {
		Map<String, Object> scalars = new LinkedHashMap<>();
		PlayerData data = player.getData();
		if (data != null) {
			scalars.put("level", data.getLevel());
			scalars.put("exp", data.getExp());
			scalars.put("vipLevel", data.getVipLevel());
			scalars.put("vipExpTotal", data.getVipExpTotal());
			scalars.put("fightPower", data.getFightPower());
			scalars.put("refreshDay", data.getRefreshDay());
			scalars.put("offlineTime", data.getOfflineTime());
		}
		return scalars;
	}

	private void applyScalars(Player player, Map<String, Object> scalars) {
		if (scalars == null || player.getData() == null) {
			return;
		}
		PlayerData d = player.getData();
		if (scalars.containsKey("level")) d.setLevel(((Number) scalars.get("level")).intValue());
		if (scalars.containsKey("exp")) d.setExp(((Number) scalars.get("exp")).intValue());
		if (scalars.containsKey("vipLevel")) d.setVipLevel(((Number) scalars.get("vipLevel")).intValue());
		if (scalars.containsKey("vipExpTotal")) d.setVipExpTotal(((Number) scalars.get("vipExpTotal")).intValue());
		if (scalars.containsKey("fightPower")) d.setFightPower(((Number) scalars.get("fightPower")).intValue());
		if (scalars.containsKey("refreshDay")) d.setRefreshDay(((Number) scalars.get("refreshDay")).intValue());
		if (scalars.containsKey("offlineTime")) d.setOfflineTime(((Number) scalars.get("offlineTime")).longValue());
	}

	public static class PlayerSnapshotEntry implements Serializable {
		private static final long serialVersionUID = 1L;

		private final String snapshotName;
		private final long createdAtVirtualMs;
		private final int byteSize;
		private final byte[] compressedModules;
		private final Map<String, Object> scalarSummary;

		public PlayerSnapshotEntry(String snapshotName, long createdAtVirtualMs, int byteSize,
				byte[] compressedModules, Map<String, Object> scalarSummary) {
			this.snapshotName = snapshotName;
			this.createdAtVirtualMs = createdAtVirtualMs;
			this.byteSize = byteSize;
			this.compressedModules = compressedModules;
			this.scalarSummary = scalarSummary != null ? scalarSummary : new LinkedHashMap<>();
		}

		public String getSnapshotName() { return snapshotName; }
		public long getCreatedAtVirtualMs() { return createdAtVirtualMs; }
		public int getByteSize() { return byteSize; }
		public byte[] getCompressedModules() { return compressedModules; }
		public Map<String, Object> getScalarSummary() { return scalarSummary; }
	}
}
