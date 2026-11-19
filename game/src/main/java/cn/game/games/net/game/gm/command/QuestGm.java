package cn.game.games.net.game.gm.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.gm.AbstractGm;
import cn.game.games.net.game.gm.GmAction;
import cn.game.games.net.game.gm.GmGroup;
import cn.game.games.net.game.gm.GmParam;
import cn.game.games.net.game.gm.GmScope;
import cn.game.games.net.game.gm.GmSecurityTier;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.QuestHelper;
import cn.game.games.net.game.module.quest.Quest;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.protocol.generated.config.QuestConfig;
import cn.game.protocol.generated.enume.QuestTypeEnum;
import cn.game.protocol.generated.manager.QuestManager;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.RewardMsg.RewardPush_55000501;

/**
 * 任务系统调试GM指令分组
 *
 * @author shuyongqiang
 */
@Component
@GmGroup(name = "quest", description = "任务调试指令")
public class QuestGm extends AbstractGm {

	@GmAction(name = "quest", description = "完成指定任务并领奖", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public String quest(Player player,
			@GmParam(description = "任务配置ID") int id) {
		return finish(player, id);
	}

	@GmAction(name = "finish", description = "完成指定任务并领奖", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public String finish(Player player,
			@GmParam(description = "任务配置ID") int id) {
		QuestConfig config = QuestManager.instance().getNullable(id);
		if (config == null) {
			return "任务配置不存在: " + id;
		}
		QuestModule module = player.getQuestModule();
		if (module == null) {
			return "任务模块未初始化";
		}
		Quest quest = module.get(id);
		if (quest == null) {
			// 若尚未开启，先开启任务
			module.open(id, false);
		}
		List<RewardInfo> items = module.finish(id, 0);
		if (items != null && !items.isEmpty() && player.getGameClient() != null) {
			PlayerHelper.sendProtocol(player.getPlayerId(), RewardPush_55000501.newBuilder().addAllRewards(items).build());
		}
		return "任务 " + id + " (类型: " + config.Type + ") 已完成并发放奖励";
	}

	@GmAction(name = "accept", description = "开启/接取指定任务", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public String accept(Player player,
			@GmParam(description = "任务配置ID") int id) {
		QuestConfig config = QuestManager.instance().getNullable(id);
		if (config == null) {
			return "任务配置不存在: " + id;
		}
		QuestModule module = player.getQuestModule();
		if (module == null) {
			return "任务模块未初始化";
		}
		Quest quest = module.open(id, true);
		return quest != null ? ("成功开启任务: " + id + " (类型: " + config.Type + ")") : ("任务已存在或开启失败: " + id);
	}

	@GmAction(name = "reset", description = "重置并重新开启指定任务", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public String reset(Player player,
			@GmParam(description = "任务配置ID") int id) {
		QuestConfig config = QuestManager.instance().getNullable(id);
		if (config == null) {
			return "任务配置不存在: " + id;
		}
		QuestModule module = player.getQuestModule();
		if (module == null) {
			return "任务模块未初始化";
		}
		module.remove(id);
		Quest quest = module.open(id, true);
		return "任务 " + id + " 已重置并重新开启";
	}

	@GmAction(name = "finishAll", description = "完成所有当前进行中的任务", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public String finishAll(Player player,
			@GmParam(description = "任务类型(0表示所有类型, 1主线, 2日常, 3成就等)", required = false, defaultValue = "0") int type) {
		QuestModule module = player.getQuestModule();
		if (module == null) {
			return "任务模块未初始化";
		}
		List<Integer> finishedIds = new ArrayList<>();
		List<RewardInfo> allRewards = new ArrayList<>();
		if (type == 0) {
			for (QuestTypeEnum t : QuestTypeEnum.values()) {
				Map<Integer, Quest> group = module.getGroup(t);
				if (group != null) {
					List<Integer> ids = new ArrayList<>(group.keySet());
					for (int qid : ids) {
						List<RewardInfo> rewards = module.finish(qid, 0);
						if (rewards != null) {
							allRewards.addAll(rewards);
						}
						finishedIds.add(qid);
					}
				}
			}
		} else {
			Map<Integer, Quest> group = module.getGroup(type);
			if (group != null) {
				List<Integer> ids = new ArrayList<>(group.keySet());
				for (int qid : ids) {
					List<RewardInfo> rewards = module.finish(qid, 0);
					if (rewards != null) {
						allRewards.addAll(rewards);
					}
					finishedIds.add(qid);
				}
			}
		}
		if (!allRewards.isEmpty() && player.getGameClient() != null) {
			PlayerHelper.sendProtocol(player.getPlayerId(), RewardPush_55000501.newBuilder().addAllRewards(allRewards).build());
		}
		return "已批量完成 " + finishedIds.size() + " 个任务: " + finishedIds;
	}

	@GmAction(name = "list", description = "查看当前玩家进行中的任务列表", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public Map<String, Object> list(Player player,
			@GmParam(description = "任务类型(0表示所有类型)", required = false, defaultValue = "0") int type) {
		Map<String, Object> res = new LinkedHashMap<>();
		QuestModule module = player.getQuestModule();
		if (module == null) {
			res.put("error", "任务模块未初始化");
			return res;
		}
		List<Map<String, Object>> questList = new ArrayList<>();
		if (type == 0) {
			for (QuestTypeEnum t : QuestTypeEnum.values()) {
				Map<Integer, Quest> group = module.getGroup(t);
				if (group != null) {
					for (Quest q : group.values()) {
						QuestConfig cfg = QuestHelper.getQuestConfig(q.getId());
						questList.add(Map.of(
								"id", q.getId(),
								"type", t.name(),
								"group", cfg != null ? cfg.Group : 0,
								"state", (int) q.getState()
						));
					}
				}
			}
		} else {
			Map<Integer, Quest> group = module.getGroup(type);
			if (group != null) {
				for (Quest q : group.values()) {
					QuestConfig cfg = QuestHelper.getQuestConfig(q.getId());
					questList.add(Map.of(
							"id", q.getId(),
							"type", String.valueOf(type),
							"group", cfg != null ? cfg.Group : 0,
							"state", (int) q.getState()
					));
				}
			}
		}
		res.put("count", questList.size());
		res.put("quests", questList);
		return res;
	}
}
