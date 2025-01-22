package cn.game.games.net.game.module.develop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.protocol.generated.config.PotentialConfig;
import cn.game.protocol.generated.config.QuestConfig;
import cn.game.protocol.generated.config.RescueConfig;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.enume.QuestTypeEnum;
import cn.game.protocol.generated.manager.PotentialManager;
import cn.game.protocol.generated.manager.QuestManager;
import cn.game.protocol.generated.manager.RescueManager;
import cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.IntMapWrapper;

/**    
 * 一些玩家级别的养成数据
 * 2024年6月25日 下午6:48:42
 * @author SYQ
 */
public class DevelopModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay,
			EventTypeEnum.LoginFinish, EventTypeEnum.Reconnect, EventTypeEnum.LevelUp, EventTypeEnum.ResourceRemove, EventTypeEnum.FuncOpen };

	/** 天道修为等级 */
	private int heavenlyDaoLevel;
	/**  修炼等级。 属性id->等级 */
	private IntMapWrapper potentiaLvMap = new IntMapWrapper();
	/**  */
	private Map<Integer, Boolean> isPotentiaBreakMap = new HashMap<Integer, Boolean>();
	/** 潜力修炼的突破等级 */
	private IntMapWrapper potentiaBreakLevelMap = new IntMapWrapper();

	private QianKunMirrorInfo.Builder qiankunMirrorBuilder = QianKunMirrorInfo.newBuilder();

	public int getHeavenlyDaoLevel() {
		return heavenlyDaoLevel;
	}

	public void setHeavenlyDaoLevel(int heavenlyDaoLevel) {
		this.heavenlyDaoLevel = heavenlyDaoLevel;
	}

	public IntMapWrapper getPotentiaLvMap() {
		return potentiaLvMap;
	}

	public boolean isPotentiaBreak(int id) {
		Boolean ret = isPotentiaBreakMap.get(id);
		return ret == null ? false : ret;
	}

	public void setIsPotentiaBreak(int id, boolean value) {
		this.isPotentiaBreakMap.put(id, value);
	}

	public QianKunMirrorInfo.Builder getQiankunMirrorBuilder() {
		return qiankunMirrorBuilder;
	}

	public IntMapWrapper getPotentiaBreakLevelMap() {
		return potentiaBreakLevelMap;
	}

	/** 
	 * 获取修炼等级
	 * @param atrrId 属性id
	 * @param type 1 潜力 2强援
	 * @return
	 */
	public int getCultivationLv(int atrrId, int type) {
		int value = potentiaLvMap.getValue(atrrId);
		if (value == 0) {
			if (type == 1) {
				PotentialConfig config = DevelopHelper.getPotentialConfig(atrrId, value);
				if (PlayerHelper.checkCondition(player, config.PotentialUnlock)) {
					potentiaLvMap.setValue(config.PotentialMark, 1);
					value = 1;
				}
			} else if (type == 2) {
				RescueConfig config = DevelopHelper.getRescueConfig(atrrId, value);
				if (PlayerHelper.checkCondition(player, config.RescueUnlock)) {
					potentiaLvMap.setValue(config.RescueMark, 1);
					value = 1;
				}
			}

		}
		return value;
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		builder.setHeavenlyDaoLevel(heavenlyDaoLevel);
		builder.putAllPotentialLvMap(potentiaLvMap.getMap());
		builder.putAllPotentialBreak(isPotentiaBreakMap);
		builder.putAllPotentiaBreakLevelMap(potentiaBreakLevelMap.getMap());
		builder.setQianKunMirrorInfo(qiankunMirrorBuilder);

	}
	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		case LoginFinish: {
			if (player.getLevel() >= InitialUI.HeavenlyDaoCultivation.DisplayLevel) {
				initTianDao();
			}
			break;
		}
		case FuncOpen: {
			InitialUI func = event.getParameter(0);
			if (func == InitialUI.HeavenlyDaoCultivation) {
				initTianDao();
			} else if (func == InitialUI.Consciousness) {
				// 初始修炼等级
				Map<Integer, List<PotentialConfig>> potentialMarks = PotentialManager.instance().getPotentialMarks();
				potentialMarks.forEach((k, v) -> {
					PotentialConfig potential = DevelopHelper.getPotentialConfig(v, 1);
					if (PlayerHelper.checkCondition(player, potential.PotentialUnlock)) {
						potentiaLvMap.setValue(potential.PotentialMark, 1);
					}
				});
			} else if (func == InitialUI.HuDaoQiangYuan) {
				Map<Integer, List<RescueConfig>> rescueMarks = RescueManager.instance().getRescueMarks();
				rescueMarks.forEach((k, v) -> {
					RescueConfig potential = DevelopHelper.getRescueConfig(v, 1);
					if (PlayerHelper.checkCondition(player, potential.RescueUnlock)) {
						potentiaLvMap.setValue(potential.RescueMark, 1);
					}
				});
			}
			break;
		}
		case NewDay: {
//			isFirstLoign = true;
			break;
		}
		}
	}

	private void initTianDao() {
		if (heavenlyDaoLevel >= 1) {
			return;
		}
		heavenlyDaoLevel = 1;
		QuestModule questModule = player.getQuestModule();
		List<QuestConfig> groupList = QuestManager.instance().getTypeList(QuestTypeEnum.HeavenlyDao.ID);
		for (QuestConfig questConfig : groupList) {
			questModule.open(questConfig.ID, false);
		}
	}


	@Override
	protected int getInitOrder() {
		return INIT_PRIORITY_HIGH;
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

}
