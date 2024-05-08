package cn.game.games.net.game.module.quest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.MultiKeyMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.cache.entity.ConditionCount;
import cn.game.games.cache.entity.Quest;
import cn.game.games.cache.entity.QuestChallenge;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.ConditionCountMapper;
import cn.game.games.net.data.mapper.QuestChallengeMapper;
import cn.game.games.net.data.mapper.QuestMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.helper.EventHelper;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.QuestHelper;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.AchievementMissionConfig;
import cn.game.protocol.generated.config.MainlineMissionConfig;
import cn.game.protocol.generated.config.MissionChallengeGroupConfig;
import cn.game.protocol.generated.config.QuestConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.enume.QuestTypeEnum;
import cn.game.protocol.generated.manager.AchievementMissionManager;
import cn.game.protocol.generated.manager.MissionChallengeGroupManager;
import cn.game.protocol.generated.manager.QuestManager;
import cn.game.protocol.protobuf.BaseMsg.UpdateType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.QuestMsg.QuestGroupPush_20100008;
import cn.game.protocol.protobuf.QuestMsg.QuestRewardPush_20600008;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**
 * @Description
 * @date 2020年11月19日 下午6:29:08
 * @author SYQ
 */
public class QuestModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay,
			EventTypeEnum.NewWeek, EventTypeEnum.LevelUp };

	/** 当前激活的任务 */
	private Map<Integer, Quest>[] quests;
	@JsonIgnore
	/** 已完成的任务 ， 不需要这个了，不用查看历史任务 */
	private Map<Integer, Quest>[] competeQuests;
	@JsonIgnore
	// 支线任务保留最后一个任务id
	private Map<Integer, QuestChallenge> challenges;

	/** 一些累计的计数 */
	private MultiKeyMap<Integer, Integer> conditionCountMap = new MultiKeyMap<Integer, Integer>();

	@SuppressWarnings("unchecked")
	@Override
	public Class<?>[] defaultDbMapperClass() {
		return new Class[] { QuestMapper.class, ConditionCountMapper.class };
	}

	protected void initFromDb(ListIterator<?> iterator) {
		List<Quest> list = (List<Quest>) iterator.next();
		List<ConditionCount> conditionList = (List<ConditionCount>) iterator.next();
		for (Quest e : list) {
//			if (e.getState() == QuestHelper.RECEIVED) {
//				competeQuests[e.getQuestGroup()].put(e.getId(), e);
//			} else {
//				quests[e.getQuestGroup()].put(e.getId(), e);
//			}
			QuestConfig missionConfig = QuestHelper.getQuestConfig(e.getId());
			quests[missionConfig.getType().ordinal()].put(e.getId(), e);
		}

		for (ConditionCount conditionCount : conditionList) {
			conditionCountMap.put(conditionCount.getConditionType(), conditionCount.getArg1(), conditionCount.getArg2(),
					conditionCount.getCount());
		}
	}

	@Override
	public void initFromDbAfter() {
		for (Map<Integer, Quest> e : this.quests) {
			for (Quest q : e.values()) {
				q.initCondition();
			}
		}
	};

	@Deprecated
	public void update(Quest quest) {
//		QuestHelper.updateBase(quest);
	}

	public void refreshQuest(QuestTypeEnum type) {
		Map<Integer, Quest> values = quests[type.ordinal()];
		List<QuestConfig> missionList = QuestManager.getInstance().getTypeList(type);
		if (missionList == null) {
			return;
		}
		for (QuestConfig missionConfig : missionList) {
			if (checkOpen(missionConfig)) {
				Quest quest = values.get(missionConfig.getId());
				if (quest == null) {
					open(missionConfig.getId(), false);
				} else {
					quest.clear();
					update(quest);
				}
			} else {
				remove(missionConfig.getId());
			}
		}
	}

	public int getFinishedCount(QuestTypeEnum type) {
		Map<Integer, Quest> map = this.quests[type.ordinal()];
		int count = 0;
		for (Quest quest : map.values()) {
			if (QuestHelper.isFinished(quest)) {
				count++;
			}
		}
		return count;
	}

	public void refreshNewLevelQuest(int level, boolean notify) {

		// 通用任务，一般根据等级刷新出新任务
		Collection<QuestConfig> list = QuestManager.getInstance().getLevelList(level);
		if (list != null) {
			for (QuestConfig e : list) {
				if (!checkOpen(e)) {
					continue;
				}
				open(e, notify);
			}
		}
		// 主线任务，根据等级刷新任务状态，或者刷新出新任务，注意支线一组只能刷出来一个。
		/*Collection<MainlineMissionConfig> list2 = MainlineMissionManager.getInstance().getLevelList(level);
		if (list2 != null) {
			for (MissionConfig e : list2) {
				if (!checkOpen(e)) {
					continue;
				}
				if (e.getType() == MissionTypeEnum.MainLine) {
					Map<Integer, Quest> group = getGroup(e.getType());
					if (group == null || group.isEmpty()) {
						open(e, notify);
					} else {
						for (Entry<Integer, Quest> entry : group.entrySet()) {
							Quest quest = entry.getValue();
							if (quest.getState() == QuestHelper.SHOW) {
								setState(quest, QuestHelper.CAN_ACCEPT);
							}
						}
					}
				} else if (e.getType() == MissionTypeEnum.BranchLine) {
					Map<Integer, Quest> group = getGroup(e.getType());
					// 进行中的支线组
					Set<Integer> groups = new HashSet<>();
					for (Integer id : group.keySet()) {
						MainlineMissionConfig mainlineMissionConfig = MainlineMissionManager.getInstance().getMainlineMissionConfig(id);
						groups.add(mainlineMissionConfig.getGroupId());
					}
					if (groups.contains(e.getGroupId())) { // 本组有支线了，不开启这个任务
						continue;
					}
					// 找到支线组最小的id开启
					List<MainlineMissionConfig> groupIdList = MainlineMissionManager.getInstance().getGroupIdList(e.getGroupId());
					int minId = Integer.MAX_VALUE;
		
					for (MainlineMissionConfig config : groupIdList) {
						if (config.getId() < minId) {
							minId = config.getId();
						}
					}
					open(minId, notify);
				}
			}
		}*/

	}

	public void initQuest() {
		Collection<MissionChallengeGroupConfig> list = MissionChallengeGroupManager.getInstance().list();
		for (MissionChallengeGroupConfig config : list) {
			if (config.getSign()) {
				addChallenge(config.getId());
			}
		}
	}

	public List<RewardInfo> receive(int id) {
		return receive(id, -1);
	}

	public List<RewardInfo> receive(int id, int index) {

		// 有些任务是领完删除的
		Quest quest = get(id);
		if (!QuestHelper.canReceive(quest)) {
			return null;
		}
		if (!quest.receive()) {
			return null;
		}
		QuestConfig questConfig = QuestHelper.getQuestConfig(id);
		// 先执行结束命令
//		if (questConfig instanceof MainlineMissionConfig) {
//			MainlineMissionConfig missionConfig = (MainlineMissionConfig) questConfig;
//			PlayerHelper.command(playerId, missionConfig.getEndCommand());
//		}

		setState(quest, QuestHelper.REWARDED);
//		competeQuests[quest.getQuestGroup()].put(quest.getId(), quest);
//		quests[quest.getQuestGroup()].remove(quest.getId());

		List<Entry<Integer, Integer>> reward = questConfig.getReward();
		int chooseRewardType = questConfig.getChooseRewardType();
		if (chooseRewardType == 0) {
//			reward = 
		} else if (chooseRewardType == 1) {
			Entry<Integer, Integer> entry = reward.get(index);
			reward = new ArrayList<>();
			reward.add(entry);
		} else {
			throw new IllegalArgumentException("chooseRewardType not impl" + chooseRewardType);
		}

//		addChallengeScore(id);

//		关闭任务
		quest.close();
		// 成就类型的任务，可能需要完成一个在开启一个。

		/*	boolean lastBranch = questConfig.getOpenTaskId().isEmpty();
			// 分支的最后一个任务保留不删除
			if (questConfig.getRefreshType() || lastBranch || questConfig.getType() == MissionTypeEnum.Achievement) {
				quest.close();
				update(quest);
			} else {
				remove(id);
			}
			if (!questConfig.getOpenTaskId().isEmpty()) {
				open(questConfig.getOpenTaskId());
			}
			if (lastBranch) { // 当前分支完成，如果是设置的优先分支，需要修改默认的优先分支
				PlayerExt playerExt = player.getExt();
				int branchGroup = playerExt.getBranchGroup();
				if (questConfig.getGroupId() == branchGroup) {
					setDefaultBranchShow();
				}
			}*/

		// 发起完成任务事件
		EventHelper.handleEvent(playerId, new GameEvent(EventTypeEnum.QuestFinish, quest.getId()));

		/*		if (QuestHelper.autoRewardUseMail(id)) {
					MailHelper.sendMailMultiLanguage(playerId, 208011, 208009, 208010, MailHelper.SYSTEM, reward);
					return null;
				} else {
					return PlayerHelper.addResources(playerId, reward);
				}*/

		return PlayerHelper.addResources(playerId, reward);

	}

	@Deprecated
	public void addChallengeScore(int id) {
		QuestConfig questConfig = QuestHelper.getQuestConfig(id);
		int challengeScore = questConfig.getChallengeScore();
		if (challengeScore > 0) {
			MissionChallengeGroupConfig config = MissionChallengeGroupManager.getInstance().getMissionChallengeGroupConfig(questConfig.getGroupId());
			QuestChallenge questChallenge = this.challenges.get(config.getId());
			questChallenge.setScore(questChallenge.getScore() + challengeScore);
			if (questChallenge.getScore() >= config.getChapterLimit()) {
				if (!questChallenge.getFinish()) {
					questChallenge.setFinish(true);
					DAO.execute(QuestChallengeMapper.class, MapperConstant.updateByPrimaryKey, questChallenge);
					MailHelper.sendMailMultiLanguage(playerId, 208011, 208009, 208010, MailHelper.SYSTEM,
							config.getReward());
					List<Integer> openGroupId = config.getOpenGroupId();
					for (Integer integer : openGroupId) {
						addChallenge(integer);
					}
				}

			}
		}
	}

	public void addChallenge(int group) {
		QuestChallenge add = new QuestChallenge();
		add.setId(group);
		add.setFinish(false);
		add.setPlayerId(playerId);
		add.setScore(0);
		add.setTime((int) System.currentTimeMillis());
		DAO.execute(QuestChallengeMapper.class, MapperConstant.insert, add);
		this.challenges.put(add.getId(), add);

		MissionChallengeGroupConfig config = MissionChallengeGroupManager.getInstance().getMissionChallengeGroupConfig(group);
		List<Integer> missionId = config.getMissionId();
		open(missionId);
	}

	public List<RewardInfo> receive(List<Integer> id) {

		List<RewardInfo> ret = new ArrayList<>();
		for (Integer e : id) {
			List<RewardInfo> receive = receive(e);
			if (receive != null) {
				ret.addAll(receive);
			}
		}
		return ret;
	}

	public boolean hasRed() {

		for (int i = 0; i < quests.length; i++) {
			for (Quest e : quests[i].values()) {
				if (QuestHelper.canReceive(e)) {
					return true;
				}
			}
		}
		return false;
	}

	public Quest get(int id) {
		QuestConfig missionConfig = QuestHelper.getQuestConfig(id);
		return get(id, missionConfig.getType());
	}

	public Quest getCompelete(int id) {

		for (Map<Integer, Quest> e : competeQuests) {
			Quest quest = e.get(id);
			if (quest != null) {
				return quest;
			}
		}
		return null;
	}

	public Quest getCompelete(int id, int group) {

		return competeQuests[group].get(id);
	}

	public Quest get(int id, QuestTypeEnum missionType) {

		return quests[missionType.ordinal()].get(id);
	}

	public boolean hasBranchGroup(int branchGroup) {

		Map<Integer, Quest> group = getGroup(QuestTypeEnum.BranchLine);
		for (Integer id : group.keySet()) {
			QuestConfig config = QuestHelper.getQuestConfig(id);
			if (config.getGroupId() == branchGroup) {
				return true;
			}
		}
		return false;
	}

	public Map<Integer, Quest> getGroup(QuestTypeEnum missionTypeEnum) {

		return quests[missionTypeEnum.ordinal()];
	}

	public Map<Integer, Quest> getCompeteGroup(int group) {

		return competeQuests[group];
	}

	public Quest open(int id, boolean notify) {
		return open(id, QuestHelper.SHOW, notify);
	}

	public Quest open(int id, byte initState, boolean notify) {
		QuestConfig questConfig = QuestHelper.getQuestConfig(id);
		int group = questConfig.getType().ordinal();
		if (this.quests[group].get(id) != null) {
			log.warn(" {} 任务{}重复开启 : ", playerId, questConfig.getId());
			return null;
		}
		Quest quest = new Quest(playerId, id);
		this.quests[group].put(id, quest);

		if (notify) {
			PlayerHelper.sendProtocol(playerId,
					QuestGroupPush_20100008.newBuilder().setType(questConfig.getType().getId()).build());
			QuestHelper.notifyQuestChange(quest, UpdateType.ADD);
		}
		setState(quest, initState);
		// 检查任务是否可以完成
		quest.checkFinish();

		Quest now = get(quest.getId());
		if (now != null) { // 有可能在设置状态时就已经完成删除了
			now.insert();
		}
		return quest;
	}

	public void open(QuestConfig questConfig, boolean notify) {

		open(questConfig.getId(), QuestHelper.SHOW, notify);
	}

	public List<Quest> open(List<Integer> ids) {

		List<Quest> list = new ArrayList<>();
		for (int i = 0; i < ids.size(); i++) {
			list.add(open(ids.get(i), true));
		}
		return list;
	}

	public void remove(int id) {
		QuestConfig missionConfig = QuestHelper.getQuestConfig(id);
		Map<Integer, Quest> group = getGroup(missionConfig.getType());

		Quest quest = group.remove(id);
		if (quest != null) {
			quest.close();
			quest.delete();
			QuestHelper.notifyQuestChange(quest, UpdateType.DELETE);
		}
	}

	public void remove(int[] ids) {

		for (int i : ids) {
			remove(i);
		}
	}

	public void disable(int id, boolean updateDB) {
		Quest quest = get(id);
		if (quest != null) {
			quest.disable();
			if (updateDB) {
				update(quest);
			}
		}
	}

	public void disable(int[] ids) {
		for (int i : ids) {
			disable(i, true);
		}
	}

	public boolean checkOpen(QuestConfig questConfig) {
		if (questConfig.getLevel() > 0 && player.getData().getLevel() >= questConfig.getLevel()) {

			if (questConfig.getEndLevel() > 0 && player.getData().getLevel() > questConfig.getEndLevel()) {
				return false;
			}
			return true;
		}

//		Quest quest = get(questConfig.getId(), questConfig.getType());
//		if (quest != null) {
//			return false;
//		}
//		quest = getCompelete(questConfig.getId(), questConfig.getType());
//		if (quest != null) {
//			return false;
//		}
//		if (questConfig.getPreQuest() > 0) {
//			boolean pass = isPass(questConfig.getPreQuest());
//			if (!pass) {
//				return false;
//			}
//		}
		return false;
	}

	public boolean isPass(int id) {
		QuestConfig config = QuestHelper.getQuestConfig(id);
		Quest quest = get(id, config.getType());
		if (config.getRefreshType()) {
			return quest != null && quest.getState() >= QuestHelper.CAN_GIVEWARD;
		}
		if (QuestHelper.isFinished(quest)) {
			return true;
		}
		// 通过id数值判断是否完成；
		if (config.getType() == QuestTypeEnum.MainLine) {
			Map<Integer, Quest> group = getGroup(QuestTypeEnum.MainLine);
			for (Quest q : group.values()) {
				if (id < q.getId()) {
					return true;
				} else if (id == q.getId()) {
					return QuestHelper.isFinished(q);
				} else {
					return false;
				}
			}
		} else if (config.getType() == QuestTypeEnum.BranchLine) {
			Map<Integer, Quest> group = getGroup(QuestTypeEnum.BranchLine);
			MainlineMissionConfig mainlineMissionConfig = (MainlineMissionConfig) config;
			int groupId = mainlineMissionConfig.getGroupId();
			Set<Entry<Integer, Quest>> entrySet = group.entrySet();
			for (Entry<Integer, Quest> entry : entrySet) {
				int k = entry.getKey();
				Quest v = entry.getValue();

				QuestConfig c = QuestHelper.getQuestConfig(k);
				if (c.getGroupId() == groupId) {
					if (id < k) {
						return true;
					} else if (id == k) {
						return QuestHelper.isFinished(v);
					} else {
						return false;
					}
				}

			}
		}

		return false;
	}

	public Map<Integer, Quest>[] getAllGroup() {
		return this.quests;
	}

	public List<Integer> canReceiveIds(int group) {

		return this.quests[group].values().stream().filter(QuestHelper::canReceive).map(q -> q.getId())
				.collect(Collectors.toList());
	}

	public Map<Integer, QuestChallenge> getChallenges() {
		return challenges;
	}

	public boolean canAccept(int id) {

		QuestConfig missionConfig = QuestHelper.getQuestConfig(id);
//		Quest quest = get(id, missionConfig.getType()); 
//		if (quest != null) {
//			return false ; 
//		}
		if (player.getData().getLevel() < missionConfig.getLevel()) {
			return false;
		}
		int accessType = missionConfig.getAccessMode().get(0);
		if (accessType == 1)
			return true;
		QuestTypeEnum type = missionConfig.getType();
		if (type == QuestTypeEnum.MainLine || type == QuestTypeEnum.BranchLine) {
			if (!isPass(id)) {
				return true;
			}
		}
		return false;
	}

	public void setState(Quest quest, byte state) {
		if (quest == null) {
			return;
		}
		int oldState = quest.getState();
		if (oldState == state) {
			return;
		}
		quest.setState(state);
		setState(quest);
		int newState = quest.getState();
		if (oldState != newState) {
			Quest now = get(quest.getId());
			if (now != null) { // 有可能设置状态时领奖了，这任务已经被删除了，就不在推送这个任务状态了
				update(quest);
				QuestHelper.notifyQuestChange(quest, UpdateType.UPDATE);
			}
		}

	}

	private void setState(Quest quest) {
		byte state = quest.getState();
		QuestConfig missionConfig = QuestHelper.getQuestConfig(quest.getId());
		switch (state) {
		case QuestHelper.SHOW:

			if (player.getData().getLevel() >= missionConfig.getLevel()) {
				quest.setState(QuestHelper.CAN_ACCEPT);
				setState(quest);
			}
			break;
		case QuestHelper.CAN_ACCEPT:
			if (missionConfig.getAccessMode().get(0) == 1) {
				quest.setState(QuestHelper.ACCEPTED);
				setState(quest);
			}
			break;
		case QuestHelper.ACCEPTED:
			quest.initCondition();
			// 执行接取命令，事件
			/*			if (missionConfig instanceof MainlineMissionConfig) {
							MainlineMissionConfig mainlineMissionConfig = (MainlineMissionConfig) missionConfig;
							PlayerHelper.command(playerId, mainlineMissionConfig.getStartCommand());
			
							if (mainlineMissionConfig.getType() == MissionTypeEnum.BranchLine) {
								PlayerExt playerExt = player.getExt();
								if (playerExt.getBranchGroup() == 0) {
									setDefaultBranchShow();
								}
							}
						}*/
			break;
		case QuestHelper.CAN_GIVEWARD:
			quest.unregEvent();
			if (missionConfig.getModeOfDelivery().get(0) == 1) { // 自动交付（自动领奖）
				List<RewardInfo> receive = receive(quest.getId());
				if (receive != null && !receive.isEmpty()) {

					QuestRewardPush_20600008.Builder builder = QuestRewardPush_20600008.newBuilder();
					builder.setId(quest.getId());
					builder.addAllRewards(receive);
					PlayerHelper.sendProtocol(playerId, builder.build());
				}
			}
			break;
		case QuestHelper.REWARDED:

			break;

		default:
			break;
		}
	}

	public void setDefaultBranchShow() {
		// 当前分支完成，如果是设置的优先分支，需要修改默认的优先分支
//		PlayerExt playerExt = player.getExt();

		int idMin = Integer.MAX_VALUE;

		Map<Integer, Quest> group = getGroup(QuestTypeEnum.BranchLine);
		for (Entry<Integer, Quest> entry : group.entrySet()) {
			Quest quest = entry.getValue();
			if (QuestHelper.showBranch(quest.getState())) {
				int qid = quest.getId();
				if (qid < idMin) {
					idMin = qid;
				}
			}
		}
		/*		MissionConfig configMin = QuestHelper.getMissionConfig(idMin);
				int groupUpdate = configMin == null ? 0 : configMin.getGroupId();
				playerExt.setBranchGroup(groupUpdate);
		
				PlayerExt update = PlayerExt.valueOf(playerId);
				update.setBranchGroup(playerExt.getBranchGroup());
				DAO.updateSelective(update);
		
				PlayerHelper.sendProtocol(playerId,
						MissionBranchPriorityPush_20300000.newBuilder().setGroup(playerExt.getBranchGroup()).build());*/

	}

	public void refreshNewQuest(QuestTypeEnum type, boolean notify) {
		Map<Integer, Quest> group = getGroup(type);
		if (group == null || group.isEmpty()) {
			if (type == QuestTypeEnum.Achievement) {
				Collection<AchievementMissionConfig> list = AchievementMissionManager.getInstance().list();
				for (AchievementMissionConfig config : list) {
					open(config, notify);
				}
			}
		} else {
			for (Entry<Integer, Quest> entry : group.entrySet()) {
				Quest quest = entry.getValue();
				if (quest.getState() == QuestHelper.SHOW) {
					setState(quest, QuestHelper.CAN_ACCEPT);
				}
			}
		}
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		case NewWeek: {
			refreshQuest(QuestTypeEnum.Weekly);
			break;
		}
		case NewDay: {
			refreshQuest(QuestTypeEnum.Daily);
			break;
		}
		case PLAYER_CREATE: {
//			questOp.refreshNewQuest();
			refreshNewLevelQuest(player.getData().getLevel(), false);
			initQuest();
			refreshNewQuest(QuestTypeEnum.Achievement, false);
			break;
		}
		case LevelUp: {
			refreshNewLevelQuest(player.getData().getLevel(), true);
			break;
		}
		}
	}

	@Override
	public void init() {
		quests = new HashMap[QuestTypeEnum.values().length];
		for (int i = 0; i < quests.length; i++) {
			quests[i] = new HashMap<>();
		}
		challenges = new HashMap<>();
	}

	public void addConditionCount(ConditionTypeEnum type, int count, int... args) {
		int id = type.ID;
		int arg1 = args.length > 0 ? args[0] : 0;
		int arg2 = args.length > 1 ? args[1] : 0;
		Integer oldCount = this.conditionCountMap.get(id, arg1, arg2);
		int newCount = oldCount == null ? count : oldCount + count;
		this.conditionCountMap.put(id, arg1, arg2, newCount);
		// TODO 似乎这里如果带参数，应该把不带参数的数量也增加一下。
//		if (oldCount == null) {
//			conditionCount = new ConditionCount();
//			conditionCount.setPlayerId(playerId);
//			conditionCount.setConditionType(id);
//			conditionCount.setCount(count);
//			conditionCount.setArg1(arg1);
//			conditionCount.setArg2(arg2);
//			conditionCount.insert();
//			this.conditionCountMap.put(id, arg1, arg2, count);
//		} else {
//			conditionCount.setCount(conditionCount.getCount() + count);
//			conditionCount.update();
//		}
	}

	public MultiKeyMap<Integer, Integer> getConditionCountMap() {
		return conditionCountMap;
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
	}
}
