package cn.game.games.net.game.manager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.cache.entity.DataFixLog;
import cn.game.games.cache.entity.Player;
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
				ShiLuoZhenJingBattle battle = player.getChapterModule().getBattle(DungeonTypeEnum.ShiLuoZhenJing);
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

			LingPoBattle lingPoBattle = player.getChapterModule().getBattle(DungeonTypeEnum.LingPo);
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
							RankService.getInstance().setScoreAsync(targetServer, rankType, rankEntry.getPlayerId(), rankEntry.getScore());
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
		List<DataFixLog> selectByFixName = bean.selectByFixName(fixName);
		return !selectByFixName.isEmpty();
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
}

//自定义注解，用于标记修正方法
@Retention(RetentionPolicy.RUNTIME) // 注解在运行时可用
@Target(ElementType.METHOD) // 仅可用于方法
@interface DataFix {
	String description() default ""; // 修正方法的描述

	boolean deprecated() default false; // 是否标记为废弃, 默认为否,如果执行过了，可以手动标记为true
}
