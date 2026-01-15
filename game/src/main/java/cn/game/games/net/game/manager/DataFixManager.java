package cn.game.games.net.game.manager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.redisson.api.RScoredSortedSet;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.ScoredEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.ServerContext;
import cn.game.core.net.remote.RemoteLoginServerInterface;
import cn.game.core.util.AsyncUtils;
import cn.game.games.cache.entity.DataFixLog;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.net.data.mapper.DataFixLogMapper;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.QuestHelper;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.helper.TestHelper;
import cn.game.games.net.game.module.battle.LingPoBattle;
import cn.game.games.net.game.module.battle.ShiLuoZhenJingBattle;
import cn.game.games.net.game.module.quest.Condition;
import cn.game.games.net.game.module.quest.Quest;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.games.net.game.module.rank.RankEntry;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.protocol.generated.config.QuestConfig;
import cn.game.protocol.generated.enume.QuestTypeEnum;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.QuestManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.util.GameUtil;
import cn.game.util.RedisUtil;
import cn.game.util.SpringContextLoader;
import io.vertx.core.Future;

/**    
 * 数据修正管理器，需要执行的方法使用@DataFix注解标记（或者使用fix开头），
 * 执行后deprecated标记为true，或者删除方法
 * 
 * 不能有重名修正数据方法
 * 2024年12月3日 15:54:12
 * @author SYQ
 */
public class DataFixManager {
	private static Logger log = LoggerFactory.getLogger(DataFixManager.class);

	// 单例实现
	private static DataFixManager instance = new DataFixManager();

	private DataFixManager() {
	}

	public static DataFixManager getInstance() {
		return instance;
	}

	@DataFix(description = "修复失落真经任务", deprecated = true)
	public void fixPlayerQuestSlzj410() {

		Function<Player, Boolean> function = player -> {

			// 修正玩家任务数据
			QuestModule questModule = player.getQuestModule();
			List<Quest> fixList = new ArrayList<>();
			Quest q1 = questModule.get(32602);
			Quest q2 = questModule.get(32903);
			if (q1 != null) {
				fixList.add(q1);
			}
			if (q2 != null) {
				fixList.add(q2);
			}
			boolean fix = false;
			if (!fixList.isEmpty()) {
				ShiLuoZhenJingBattle battle = player.getBattleModule().getBattle(DungeonTypeEnum.ShiLuoZhenJing);
				if (battle != null) {
					for (Quest quest : fixList) {
						List<Condition> requires = quest.getConditionContainer().getRequires();
						Condition condition = requires.get(0);
						if (condition.getRequireId() <= battle.getHistoryMaxBattleId()) {
							condition.addCount(1);
							quest.setState(QuestHelper.CAN_GIVEWARD);
							fix = true;
						}
					}
				}
			}
			return fix;
		};

		PlayerHelper.loadAndProcessPlayers(function);
	}

	@DataFix(description = "修复天道修为新增任务", deprecated = true)
	public void fixPlayerQuestTDAdd() {
		Function<Player, Boolean> function = player -> {
			int heavenlyDaoLevel = player.getDevelopModule().getHeavenlyDaoLevel();
			QuestModule questModule = player.getQuestModule();
			// 所有天道修改任务
			List<QuestConfig> groupList = QuestManager.instance().getTypeList(QuestTypeEnum.HeavenlyDao.ID);
			boolean fix = false;
			for (QuestConfig questConfig : groupList) {
				// 当前天道修为等级的任务
				if (questConfig.OpenCondition == heavenlyDaoLevel) {
					Quest quest = questModule.open(questConfig.ID, false);
					if (quest != null) {
						// 新增了任务
						fix = true;
					}
				}
			}
			return fix;
		};

		PlayerHelper.loadAndProcessPlayers(function);
	}
	
	@DataFix(description = "初始化玩家最近的服务器", deprecated = true)
	public void fixUserServer() {
		RemoteLoginServerInterface remoteLoginInterfaceProxy = ServerHelper.getRemoteLoginInterfaceProxy(); 
		remoteLoginInterfaceProxy.isAvailable(); 
		Function<Player, Boolean> function = player -> {
			PlayerData data = player.getData(); 
			Future<Void> updateUserServer = remoteLoginInterfaceProxy.updateUserServer(data.getServerId(), data.getUid(), data.getPlayerId(), data.getName(), player.getLevel());
			AsyncUtils.await(updateUserServer); 
			return false;
		};
		
		PlayerHelper.loadAndProcessPlayers(function);
	}

	@DataFix(description = "重置灵魄之战数据", deprecated = true)
	public void fixPlayerLPZZReset() {

		Function<Player, Boolean> function = player -> {

			LingPoBattle lingPoBattle = player.getBattleModule().getBattle(DungeonTypeEnum.LingPo);
			if (lingPoBattle == null) {
				return false;
			}
			if (lingPoBattle.getBattleId() == 31001) {
				return false;
			}
			lingPoBattle.setBattleId(31001);
//			lingPoBattle.setBattleTimes(0);
			return true;
		};

		PlayerHelper.loadAndProcessPlayers(function);
	}

	@DataFix(description = "修复带小数时间的积分排名问题", deprecated = false)
	public void fixRankSameScroeWithTime() {
        String[] serverIds = new String[] { "server1", "server2", "server3", "server4" , "server5", "server6"};
        // 请确保 RankType 枚举可用
        RankType[] rankTypes = new RankType[] { RankType.Battle, RankType.Level, RankType.LingShanWenChan, RankType.GemTowerMain,
                RankType.GemTowerIce, RankType.GemTowerThunder, RankType.GemTowerFire, RankType.GemTowerPoison, RankType.XiangYaoFuMo };

        int totalSuccess = 0;
        int totalFail = 0;

        log.info("========== 开始批量修复任务 (策略：旧数据整体 +0.9) ==========");

        for (String serverId : serverIds) {
            for (RankType rankType : rankTypes) {
                String key = null;
                try {
                    key = RankService.getInstance().getKey(serverId, rankType);
                    log.info(">>> [开始] 修复排行榜: {}", key);

                    RScoredSortedSet<String> rankSet = RedisUtil.getRedis().getScoredSortedSet(key, StringCodec.INSTANCE);

                    Collection<ScoredEntry<String>> allEntries;
                    try {
                        allEntries = rankSet.entryRange(0, -1);
                    } catch (Exception e) {
                        log.error("读取排行榜失败: {}", key, e);
                        continue;
                    }

                    if (allEntries == null || allEntries.isEmpty()) {
                        continue;
                    }

                    Map<String, Double> updates = new HashMap<>();
                    List<String> dirtyMembers = new ArrayList<>();

                    for (ScoredEntry<String> entry : allEntries) {
                        String memberRaw = entry.getValue();
                        double oldScore = entry.getScore();

                        // 1. 识别脏数据
                        if (memberRaw.contains("java.lang.Long") || memberRaw.startsWith("[")) {
                            dirtyMembers.add(memberRaw);
                            continue; 
                        }

                        // 2. 解析 PlayerID
                        long playerId;
                        try {
                            playerId = Long.parseLong(memberRaw);
                        } catch (NumberFormatException e) {
                            log.error("无法解析 PlayerID: {}, 跳过", memberRaw);
                            continue;
                        }

                        // 3. 【核心修改】直接给旧分数 +0.9
                        // 逻辑：110021.000006 -> 110021.900006
                        // 这样既保留了微小的历史差异，又把它们推到了新数据(0.89)的前面
                        double newScore = convertOldToNew(oldScore);

                        updates.put(String.valueOf(playerId), newScore);
                    }

                    // 4. 执行清理和更新
                    if (!dirtyMembers.isEmpty()) {
                        rankSet.removeAll(dirtyMembers);
                        log.info("排行榜 [{}] 清理 {} 条脏数据", key, dirtyMembers.size());
                    }

                    if (!updates.isEmpty()) {
                        rankSet.addAll(updates);
                        log.info("排行榜 [{}] 修复 {} 条数据", key, updates.size());
                        totalSuccess++;
                    }

                } catch (Exception e) {
                    log.error("排行榜 {} 处理异常", key, e);
                    totalFail++;
                }
            }
        }
        log.info("========== 修复结束: 成功 {}, 失败 {} ==========", totalSuccess, totalFail);
    }

	/**
     * 迁移算法：
     * 1. 纯整数分数 -> 保持不变
     * 2. 带小数分数 -> +0.9 (为了排在新数据前面)
     */
	private double convertOldToNew(double oldCombinedScore) {
        long rawScore = (long) oldCombinedScore; // 提取整数部分，例如 110021
        double fraction = oldCombinedScore - rawScore; // 提取小数部分

        // 1. 纯整数判断 (精度容错)
        if (fraction < 1.0E-9) {
            return oldCombinedScore;
        } 
        
        // 2. 进位防御 (Safety Net)
        // 如果小数部分 >= 0.1，加 0.9 就会导致整数部分 +1。
        // 这里用 > 0.09 作为阈值是非常安全的。
        if (fraction > 0.09) {
        	log.warn("发现异常数据: " + oldCombinedScore + "，小数部分过大，已强制修正防止进位。");
            // 强制压缩到 0.9 边缘，放弃原有的时间排序，优先保住整数分
            return rawScore + 0.9 + 0.000001; 
        }

        // 3. 正常迁移
        return oldCombinedScore + 0.9;
    }
	/** 
	 * 
	 * ognl '@cn.game.games.net.game.manager.DataFixManager@getInstance().mergeServers(@array{"param1","param2"}, @array{@array{"source1","source2"},@array{"source3","source4"}})'
	 * @param target
	 * @param sources
	 */
	public void mergeServers(String[] target, String[][] sources) {
		if (target.length != sources.length) {
			log.error("目标服务器数量与源服务器数量不匹配");
			return;
		}
		for (int i = 0; i < target.length; i++) {
			String targetServer = target[i];
			String[] sourceServers = sources[i];
			if (GameUtil.contains(sourceServers, targetServer)) {
				log.error("目标服务器与源服务器不能相同");
				return;
			}
			log.info("合并服务器: " + targetServer + " <= " + String.join(",", sourceServers));
			// 合并逻辑
			PlayerHelper.loadAndProcessPlayers(player -> {
				if (GameUtil.contains(sourceServers, player.getData().getServerId())) {
					// 修改玩家的服务器id
					player.getData().setServerId(targetServer);
					PlayerHelper.saveSimplePlayerToRedis(player);
					return true;
				}
				return false;
			});

			// 合并其他数据
			// 排行榜
			for (RankType rankType : RankType.values()) {
				for (String srouceServerId : sourceServers) {
					for (int page = 1;; page++) {
						List<RankEntry> rankEntries = RankService.getInstance().getPage(srouceServerId, rankType, page, 50);
						if (rankEntries.isEmpty()) {
							break;
						}
						for (RankEntry rankEntry : rankEntries) {
							// 放到目标排行榜
							RankService.getInstance().setScoreAsync(targetServer, rankType, rankEntry.getId(), rankEntry.getScore());
						}
					}
					RankService.getInstance().removeRankAsync(rankType, srouceServerId);
				}

			}

		}

	}

	/** 
	 * ognl -x 3 '@cn.game.games.net.game.manager.DataFixManager@getInstance().runtimeFix()'
	 */
	public void runtimeFix() {
		log.info("runtimeFix");
		RankService.getInstance().setNpcToRank();
	}

	/** 
	 * ognl -x 3 '@cn.game.games.net.game.manager.DataFixManager@getInstance().runtimeFixLongArgs(new long[]{100L, 200L, 300L})'
	 * @param args
	 */
	public void runtimeFixLongArgs(long... args) {
		log.info("runtimeFixLongArgs");

	}

	/** 
	 * ognl -x 3 '@cn.game.games.net.game.manager.DataFixManager@getInstance().runtimeFixStringArgs(new String[]{"hello", "world"})'
	 * @param args
	 */
	public void runtimeFixStringArgs(String... args) {
		log.info("runtimeFixStringArgs");

	}

	/** 
	 * ognl -x 3 '@cn.game.games.net.game.manager.DataFixManager@getInstance().runtimeRankReward(new String[]{"server4", "server5"},new int[]{1,2,3,4})'
	 * @param args
	 */
	public void runtimeRankReward(String[] serverIds, int[] rankIds) {

		log.info("runtimeRankReward rank reward for  server [{}]rankIds[{}] ", serverIds, rankIds);

		RankService.getInstance().reward(serverIds, rankIds);
	}

	public void init() {
		initializeDataFixLogTable();
		runAllFixes();
	}

	public void initializeDataFixLogTable() {
		String createTableSql = "CREATE TABLE IF NOT EXISTS data_fix_log (" + "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "fix_name VARCHAR(255) NOT NULL UNIQUE, " + "executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" + ");";

		PlayerDataMapper bean = SpringContextLoader.getContext().getBean(PlayerDataMapper.class);
		bean.executeSql(createTableSql);
	}

	public void runAllFixes() {
		Method[] methods = this.getClass().getDeclaredMethods(); // 获取当前类的所有方法
		List<Method> fixMethods = new ArrayList<>();

		for (Method method : methods) {
			boolean isFixMethodByName = method.getName().startsWith("fix");
			boolean isFixMethodByAnnotation = method.isAnnotationPresent(DataFix.class);
			if (isFixMethodByName || isFixMethodByAnnotation) {
				fixMethods.add(method);
			}
		}
		// 遍历并执行所有修正方法
		for (Method method : fixMethods) {
			DataFix annotation = method.getAnnotation(DataFix.class); // 获取注解（如果有）
			runFix(method, annotation);
		}
	}

	private void runFix(Method method, DataFix annotation) {
		try {
			// 类名+方法名作为唯一标识符
			String fixName = this.getClass().getName() + "." + method.getName();
			if (isFixExecuted(fixName)) {
				log.info("已经执行过修正: " + fixName + (annotation != null ? ", 描述: " + annotation.description() : ""));
				return;
			}

			// 检查注解是否标记为废弃
			if (annotation != null && annotation.deprecated()) {
				log.info("跳过废弃的修正方法: " + fixName + ", 描述: " + annotation.description());
				return;
			}
			// 执行修正逻辑
			log.info("准备执行修正: " + fixName + (annotation != null ? ", 描述: " + annotation.description() : ""));
			method.invoke(this); // 调用修正方法

			// 标记修正为已执行
			markFixExecuted(fixName);
			log.info("修正完成: " + fixName + (annotation != null ? ", 描述: " + annotation.description() : ""));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 
	 * 检查修正是否已执行
	 * @param fixName
	 * @return
	 */
	private boolean isFixExecuted(String fixName) {
		DataFixLogMapper bean = SpringContextLoader.getContext().getBean(DataFixLogMapper.class);
		DataFixLog selectByFixName = bean.selectByFixName(fixName);
		return selectByFixName != null;
	}

	/** 
	 * 标记修正为已执行
	 * @param fixName
	 */
	private void markFixExecuted(String fixName) {
		DataFixLogMapper bean = SpringContextLoader.getContext().getBean(DataFixLogMapper.class);
		DataFixLog log = new DataFixLog();
		log.setFixName(fixName);
		log.setExecutedAt(new Date());
		bean.insert(log);
	}

	/** 
	 * 仅测试删除失效玩家使用
	 */
	public void deleteInactivePlayers() {
		if (ServerContext.getInstance().getRunMode().isProduction()) {
			return;
		}
		int totalFailed = 0;

		try {
			Function<Player, Boolean> function = player -> {
				PlayerHelper.saveSimplePlayerToRedisSync(player);
				// 初始化名字，名字--id
				PlayerNameManager.getInstance().addExistingUsername(player.getData().getName());
				PlayerNameManager.getInstance().saveName2IdSync(player.getData().getName(), player.getData().getPlayerId());
				return false;
			};
			PlayerDataMapper bean = SpringContextLoader.getContext().getBean(PlayerDataMapper.class);
			long lastId = 0;
			bean.getLastIdOfBatch(lastId, 100);
			while (true) {
				List<PlayerData> playerDatas = bean.getBatchCursor(lastId, 100);
				if (playerDatas.isEmpty()) {
					break;
				}
				for (PlayerData playerData : playerDatas) {
					if (PlayerManager.getInstance().isOnline(playerData.getPlayerId())) {
						continue;
					}
					try {
						Player player = new Player(playerData);
						player.setOnline(false);
						function.apply(player);
					} catch (Exception e) {
						totalFailed++;
						log.error("refreshSimplePlayers error for playerId: " + playerData.getPlayerId() + e.getMessage());
						if (e.getMessage() != null && e.getMessage().contains("json 反序列化异常")) {
							TestHelper.deletePlayerData(playerData.getPlayerId());
						}
					}

				}
				lastId = playerDatas.get(playerDatas.size() - 1).getPlayerId();
			}
		} catch (Exception e) {
			throw e;
		} finally {
		}
		System.err.println("总失败数: " + totalFailed);
	}

//自定义注解，用于标记修正方法
	@Retention(RetentionPolicy.RUNTIME) // 注解在运行时可用
	@Target(ElementType.METHOD) // 仅可用于方法
	@interface DataFix {
		String description() default ""; // 修正方法的描述

		boolean deprecated() default false; // 是否标记为废弃, 默认为否,如果执行过了，可以手动标记为true
	}
}
