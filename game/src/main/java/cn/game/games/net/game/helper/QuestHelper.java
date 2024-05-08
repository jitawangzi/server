package cn.game.games.net.game.helper;

import cn.game.games.cache.entity.Quest;
import cn.game.games.util.PbBuilder;
import cn.game.protocol.generated.config.QuestConfig;
import cn.game.protocol.generated.enume.QuestTypeEnum;
import cn.game.protocol.generated.manager.AchievementMissionManager;
import cn.game.protocol.generated.manager.QuestManager;
import cn.game.protocol.protobuf.BaseMsg.UpdateType;
import cn.game.protocol.protobuf.QuestMsg.QuestPush_20200008;

public class QuestHelper {

	/** 初始状态，任务不能接，不能看*/
	public static final byte INIT = 0;
	/** 可以看到任务, 不能接，不能开始 */
	public static final byte SHOW = 1;
	/** 可以接取任务 */
	public static final byte CAN_ACCEPT = 2;
	/** 已接 */
	public static final byte ACCEPTED = 3;
	/** 条件完成(可交付/可领奖) */
	public static final byte CAN_GIVEWARD = 4;
	/** 已交付(已领奖) */
	public static final byte REWARDED = 5;
	/** 失效 */
	public static final byte DISABLED = -1;

	public static boolean canReceive(Quest quest) {
		return quest != null && quest.getState() == CAN_GIVEWARD;
	}

	public static boolean watch(Quest quest){
		return quest.getState() == ACCEPTED;
	}
	public static boolean showBranch(byte state) {
		return state == ACCEPTED || state == CAN_GIVEWARD;
	}

	public static boolean isStart(Quest quest) {
		return quest.getState() == INIT;
	}
	public static boolean isFinished(Quest quest) {
		return quest != null && (quest.getState() == CAN_GIVEWARD || quest.getState() == REWARDED);
	}
	/** 
	 * 判断一个任务，是否处在活跃状态（接取后，等待条件达成）
	 * @param quest
	 * @return
	 */
	public static boolean isActive(Quest quest) {
		return quest != null && (quest.getState() == ACCEPTED);
	}
	/**
	 * @Description 任务是否自动通过邮件发送奖励
	 * @param id
	 * @return
	 */
	public static boolean autoRewardUseMail(int id) {
		QuestConfig missionConfig = QuestHelper.getQuestConfig(id);
		QuestTypeEnum type = missionConfig.getType();
//		return type == QuestTypeEnum.Challenge;
		return false;
	}

	/**
	 * @Description 获取任务配置文件
	 * @param id
	 * @return
	 */
	public static QuestConfig getQuestConfig(int id) {
		int type = id / 10000;
		QuestConfig missionConfig = null;
		switch (QuestTypeEnum.get(type)) {
			case Achievement:
				missionConfig = AchievementMissionManager.getInstance().getAchievementMissionConfig(id);
				break;
//			case MainLine:
//				missionConfig = MainlineMissionManager.getInstance().getMainlineMissionConfig(id);
//				break;
//			case ExploreMainLine:
//			case Explore:
//				missionConfig = ExploreMissionManager.getInstance().getExploreMissionConfig(id);
//				break;
			default:
				missionConfig = QuestManager.getInstance().getMissionConfig(id);
				break;
		}
		return missionConfig;
	}
	/**
	 * @Description 获取任务配置文件
	 * @param id
	 * @return
	 */
	public static QuestConfig getMissionConfigOrNull(int id) {
		QuestConfig missionConfig = QuestManager.getInstance().getMissionConfigNullable(id);
		if (missionConfig == null) {
//			missionConfig = MainlineMissionManager.getInstance().getMainlineMissionConfigNullable(id);
		}
//		if (missionConfig == null) {
//			missionConfig = ExploreMissionManager.getInstance().getExploreMissionConfigNullable(id);
//		}
		if (missionConfig == null) {
			missionConfig = AchievementMissionManager.getInstance().getAchievementMissionConfigNullable(id);
		}
		return missionConfig;
	}
	
	/**
	 * @Description 判断执行任务的条件命令
	 * @param quest
	 * @param cond
	 */
	public static void conditionCmd(Quest quest, int cond) {
		//  目前只有主线和支线的任务，带命令
		QuestConfig missionConfig = QuestHelper.getQuestConfig(quest.getId());
		if (missionConfig != null) {
//			if (missionConfig instanceof MainlineMissionConfig) {
//				MainlineMissionConfig mainlineMissionConfig = (MainlineMissionConfig) missionConfig;
//				if (!mainlineMissionConfig.getConditionalCommand().isEmpty()) {
//					Condition require = quest.getRequire(cond);
//					if (require != null) {
//						int cmd = mainlineMissionConfig.getConditionalCommand().get(require.getIndex());
//						if (cmd != 0) {
//							PlayerHelper.command(quest.getPlayerId(), cmd);
//						}
//					}
//				}
//			}
		}
	}

	public static void notifyQuestChange(Quest quest, UpdateType type) {
		long playerId = quest.getPlayerId();
		QuestPush_20200008 msg = QuestPush_20200008.newBuilder().setType(type).setQuest(PbBuilder.buildQuestInfo(quest))
				.build();
		PlayerHelper.sendProtocol(playerId, msg);
	}

	/** 
	 * 更新任务基本数据到数据库，不包括条件参数
	 * @param quest
	 */
	public static void updateBase(Quest quest) {
		quest.update();
	}

	/** 
	 * 更新任务条件数据到数据库，不包括任务基本数据
	 * @param quest
	 */
	public static void updateParams(Quest quest) {

		quest.toParams();
		Quest update = new Quest();
		update.setPlayerId(quest.getPlayerId());
		update.setId(quest.getId());
		update.setParams(quest.getParams());
		update.updateWithBlobs();
	}
}
