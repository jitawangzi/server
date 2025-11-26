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
import org.redisson.client.protocol.ScoredEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.ServerContext;
import cn.game.games.cache.entity.DataFixLog;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.net.data.mapper.DataFixLogMapper;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.QuestHelper;
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
		String[] serverIds = new String[] {"server1","server2","server3","server4"}; 
		RankType[] rankTypes = new RankType[] {RankType.Battle, RankType.Level, RankType.LingShanWenChan,
				 RankType.GemTowerMain,RankType.GemTowerIce,RankType.GemTowerThunder,RankType.GemTowerFire,RankType.GemTowerPoison,RankType.XiangYaoFuMo};
		for (String serverId : serverIds) {
			for (RankType rankType : rankTypes) {
				  String key = RankService.getInstance().getKey(serverId, rankType);
				  log.info("开始修复排行榜数据: {}", key);
			        RScoredSortedSet<Long> rankSet = RedisUtil.getRedis().getScoredSortedSet(key);
			        
			        // 1. 读取所有数据 (如果数据量极大，建议分批处理，但几千几万条直接读没问题)
			        Collection<ScoredEntry<Long>> allEntries = rankSet.entryRange(0, -1);

			        if (allEntries.isEmpty()) {
			            log.info("排行榜 {} 为空，无需修复。", key);
			            return;
			        }

			        Map<Long, Double> updates = new HashMap<>();
			        int count = 0;

			        for (ScoredEntry<Long> entry : allEntries) {
			            long playerId = entry.getValue();
			            double oldScore = entry.getScore();

			            // 2. 转换分数
			            double newScore = convertOldToNew(oldScore);

			            // 3. 存入待更新Map
			            updates.put(playerId, newScore);
			            
			            count++;
			            if (count % 100 == 0) {
			                log.info("已处理 {} 条数据...", count);
			            }
			        }

			        // 4. 批量写回 Redis (覆盖旧值)
			        // addAll 相比一个一个 add 效率更高
			        rankSet.addAll(updates);
			        log.info("修复完成！Key: {}, 共更新 {} 条数据。", key, updates.size());
			}
		}
	}

    /**
     * 核心算法：旧分数 -> 绝对时间 -> 新分数
     */
    private double convertOldToNew(double oldCombinedScore) {
		// ============ 旧参数 (用于反推时间) ============
		long OLD_BASE_END_TIME = 4093726323L;
		double OLD_FACTOR = 1.0E-15;

		// ============ 新参数 (用于生成新数据) ============
		long NEW_BASE_END_TIME = 1893427200L; // 2030-01-01
		double NEW_FACTOR = 5.0E-9;
		
        // 步骤 A: 提取整数部分 (玩家真实分数)
        long rawScore = (long) oldCombinedScore;

        // 步骤 B: 提取旧的小数部分
        // 注意：直接减可能会有极微小的精度误差，但在 E-15 级别通常可控
        double oldFraction = oldCombinedScore - rawScore;

        // 步骤 C: 反推达成该分数时的“绝对时间戳”
        // 旧公式: fraction = (OLD_END - achievedTime) * OLD_FACTOR
        // 变形: achievedTime = OLD_END - (fraction / OLD_FACTOR)
        
        // 这里的计算需要非常小心，因为 oldFraction 非常小
        long timeDeltaOld = (long) (oldFraction / OLD_FACTOR);
        long achievedTime = OLD_BASE_END_TIME - timeDeltaOld;

        // 校验反推的时间是否合理 (比如是否在 2020年-2025年之间)
        // 如果数据异常(比如纯整数没有小数)，achievedTime 会变成 2099年
        // 我们可以做一个修正，如果时间不合理，就按当前时间算
        long now = System.currentTimeMillis() / 1000;
        if (achievedTime > now + 86400 || achievedTime < 1577836800L) { // 2020-01-01
            log.warn("检测到异常或无时间戳的数据: score={}, 推导时间={}. 重置为当前时间。", oldCombinedScore, achievedTime);
            achievedTime = now;
        }

        // 步骤 D: 使用新参数计算新分数
        // 新公式: score + (NEW_END - achievedTime) * NEW_FACTOR
        long newTimeDelta = NEW_BASE_END_TIME - achievedTime;
        
        // 防止新时间差为负数 (如果达成时间超过了2030年，虽然理论上不可能)
        if (newTimeDelta < 0) {
            newTimeDelta = 0;
        }

        return rawScore + (newTimeDelta * NEW_FACTOR);
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
			return ; 
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
						totalFailed ++ ;
						log.error("refreshSimplePlayers error for playerId: " + playerData.getPlayerId() +  e.getMessage());
						if (e.getMessage() != null && e.getMessage().contains("json 反序列化异常")) {
							PlayerHelper.deletePlayerData(playerData.getPlayerId());
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
