package cn.game.games.net.game.module.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.data.mapper.ConditionCountMapper;
import cn.game.games.net.data.mapper.QuestMapper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.QuestHelper;
import cn.game.games.net.game.module.player.pointreward.PointRewardModule;
import cn.game.games.net.game.module.player.pointreward.PointRewardType;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.config.QuestConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.enume.QuestTypeEnum;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.generated.manager.QuestManager;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BaseMsg.UpdateType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.QuestMsg.QuestGroupInfo;
import cn.game.protocol.protobuf.QuestMsg.QuestGroupPointRewardInfo;
import cn.game.protocol.protobuf.QuestMsg.QuestGroupPush_20100008;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.IntMapWrapper;
import cn.game.util.StringMapWrapper;

/**
 * @Description
 * 2020年11月19日 下午6:29:08
 * @author SYQ
 */
public class QuestModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay,
			EventTypeEnum.NewWeek, EventTypeEnum.LevelUp, EventTypeEnum.Charge, EventTypeEnum.ChapterWin, EventTypeEnum.BattleEnd, EventTypeEnum.CostItem,
			EventTypeEnum.FuncOpen, EventTypeEnum.WatchAds, EventTypeEnum.HeroBreak, EventTypeEnum.Hero, EventTypeEnum.Patrol,
			EventTypeEnum.Draw, EventTypeEnum.QianLi, EventTypeEnum.QiangYuan, EventTypeEnum.ParticipatePVPStart,
			EventTypeEnum.FairyFriendsTravel, EventTypeEnum.FairyFriendsGift };

	/** 当前激活的任务 ,key1 ： QuestTypeEnum, key2: QuestConfig id */
	private Map<Integer, Map<Integer, Quest>> quests;
//	/** 任务积分宝箱活跃奖励领取情况 */
//	private Map<Integer, List<Integer>> activeRewardMap = new HashMap<Integer, List<Integer>>();

	@JsonIgnore
	// 支线任务保留最后一个任务id
	private Map<Integer, QuestChallenge> challenges;

	/** 一些累计的计数， 类型->数量*/
	private IntMapWrapper cumulativeCountMap = new IntMapWrapper();
	/** 一些累计的计数,类型->数量 ，类型带额外参数的 */
	private StringMapWrapper cumulativeCountExtMap = new StringMapWrapper();

	/*@SuppressWarnings("unchecked")
	@Override
	public Class<?>[] defaultDbMapperClass() {
		return new Class[] { QuestMapper.class, ConditionCountMapper.class };
	}
	
		protected void initFromDb(ListIterator<?> iterator) {
			List<Quest> list = (List<Quest>) iterator.next();
			List<ConditionCount> conditionList = (List<ConditionCount>) iterator.next();
			for (Quest e : list) {
				QuestConfig questConfig = QuestHelper.getQuestConfig(e.getId());
	//			quests[QuestTypeEnum.get(questConfig.Type).ordinal()].put(e.getId(), e);
			}
	
	//		for (ConditionCount conditionCount : conditionList) {
	//			conditionCountMap.put(conditionCount.getConditionType(), conditionCount.getArg1(), conditionCount.getArg2(),
	//					conditionCount.getCount());
	//		}
		}*/

	@Override
	public void initFromDbAfter() {
		for (Map<Integer, Quest> e : this.quests.values()) {
			for (Quest q : e.values()) {
				q.initCondition();
			}
		}
	};

	@Deprecated
	public void update(Quest quest) {
//		QuestHelper.updateBase(quest);
	}

//	public List<Integer> getActiveRewardList(QuestTypeEnum type) {
//		
//		List<Integer> list = activeRewardMap.get(type.ID);
//		if (list == null) {
//			list = new ArrayList<>();
//			activeRewardMap.put(type.ID, list);
//		}
//		return list;
//	}

//	public int checkActiveReceive(QuestTypeEnum type, int index) {
//		QuestPointRewardConfig questPointRewardConfig = QuestPointRewardManager.instance().get(type.ID);
//		if (index >= questPointRewardConfig.Stage.length) {
//			return ErrorMsgEnum.request_parameter_error.getId();
//		}
//		long point = player.getCurrencyModule().getCount(questPointRewardConfig.PointType);
//		List<Integer> activeRewardList = getActiveRewardList(type);
//		if (activeRewardList.contains(index)) {
//			return ErrorMsgEnum.repeat_request.getId();
//		}
//		int needPoint = questPointRewardConfig.Stage[index];
//		if (point < needPoint) {
//			return ErrorMsgEnum.illegal_request.getId();
//		}
//		return 0;
//	}

	public void refreshQuest(QuestTypeEnum type) {
		if (!player.isFuncOpen(InitialUI.Task)) {
			return;
		}
		Map<Integer, Quest> values = quests.get(type.ID);
		List<QuestConfig> missionList = QuestManager.instance().getTypeList(type.ID);
		if (missionList == null) {
			return;
		}
		for (QuestConfig missionConfig : missionList) {
			if (checkOpen(missionConfig)) {
				Quest quest = values.get(missionConfig.ID);
				if (quest == null) {
					open(missionConfig.ID, false);
				} else {
					quest.clear();
					update(quest);
				}
			} else {
				remove(missionConfig.ID);
			}
		}
	}

	/** 
	 * 获取某类型的累计数
	 * @param type
	 * @return
	 */
	public int getCumulativeCount(ConditionTypeEnum type) {
		return this.cumulativeCountMap.getValue(type.ID);
	}

	/** 
	 * 获取某类型的累计数 
	 * @param type
	 * @param ext 额外参数
	 * @return
	 */
	public int getCumulativeCount(ConditionTypeEnum type, int... ext) {
		if (ext.length > 0) {
			return this.cumulativeCountExtMap.getValue(type.ID, ext);
		}
		return getCumulativeCount(type);
	}

	public int getFinishedCount(QuestTypeEnum type) {
//		Map<Integer, Quest> map = this.quests[type.ordinal()];
		Map<Integer, Quest> map = this.quests.get(type.ID);
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
		/*		Collection<QuestConfig> list = QuestManager.instance().getLevelList(level);
				if (list != null) {
					for (QuestConfig e : list) {
						if (!checkOpen(e)) {
							continue;
						}
						open(e, notify);
					}
				}*/
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

	/*	public void initQuest() {
			Collection<MissionChallengeGroupConfig> list = MissionChallengeGroupManager.getInstance().list();
			for (MissionChallengeGroupConfig config : list) {
				if (config.getSign()) {
					addChallenge(config.getId());
				}
			}
		}*/

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
		return finish(id, index);
		/*		// 先执行结束命令
				if (questConfig instanceof MainlineMissionConfig) {
					MainlineMissionConfig missionConfig = (MainlineMissionConfig) questConfig;
					PlayerHelper.command(playerId, missionConfig.getEndCommand());
				}*/
		/*		List<Entry<Integer, Integer>> reward = questConfig.getReward();
				int chooseRewardType = questConfig.getChooseRewardType();
				if (chooseRewardType == 0) {
				} else if (chooseRewardType == 1) {
					Entry<Integer, Integer> entry = reward.get(index);
					reward = new ArrayList<>();
					reward.add(entry);
				} else {
					throw new IllegalArgumentException("chooseRewardType not impl" + chooseRewardType);
				}*/

//		addChallengeScore(id);
	}

	public List<RewardInfo> finish(int id, int index) {

		QuestConfig questConfig = QuestHelper.getQuestConfig(id);
		Quest quest = get(id);
		if (quest == null) {
			return null;
		}
		setState(quest, QuestHelper.REWARDED, true);

		if (questConfig.IsDeleteOnFinish) {
			remove(id);
		} else {
			quest.close();
			update(quest);
		}
		if (questConfig.OpenQuests.length > 0) {
			open(questConfig.OpenQuests, true);
		}
		// 发起完成任务事件
		player.handleEvent(EventTypeEnum.QuestReward, quest.getId());
		return PlayerHelper.addReward(player, questConfig.Reward, OpType.Quest);

	}

	/*	@Deprecated
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
		}*/
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

		for (Map<Integer, Quest> map : quests.values()) {
			for (Quest e : map.values()) {
				if (QuestHelper.canReceive(e)) {
					return true;
				}
			}
		}
//		for (int i = 0; i < quests.size(); i++) {
//			for (Quest e : quests[i].values()) {
//				if (QuestHelper.canReceive(e)) {
//					return true;
//				}
//			}
//		}
		return false;
	}
	public Quest get(int id) {
		QuestConfig questConfig = QuestManager.instance().get(id);
		return quests.get(questConfig.Type).get(id);
	}

	public boolean hasBranchGroup(int branchGroup) {

		Map<Integer, Quest> group = getGroup(QuestTypeEnum.BranchLine);
		for (Integer id : group.keySet()) {
			QuestConfig config = QuestHelper.getQuestConfig(id);
			if (config.Group == branchGroup) {
				return true;
			}
		}
		return false;
	}

	public Map<Integer, Quest> getGroup(QuestTypeEnum missionTypeEnum) {

		return quests.get(missionTypeEnum.ID);
	}

	public Map<Integer, Quest> getGroup(int type) {

		return quests.get(type);
	}

	public Quest open(int id, boolean notify) {
		return open(id, QuestHelper.SHOW, notify);
	}

	public Quest open(int id, byte initState, boolean notify) {
		QuestConfig questConfig = QuestHelper.getQuestConfig(id);

		int group = questConfig.Type;
		if (group == 0) {
			throw new IllegalArgumentException(" quest group is 0 : " + id);
		}
		if (this.quests.get(group).get(id) != null) {
			log.warn(" {} 任务{}重复开启 : ", playerId, questConfig.ID);
			return null;
		}
		Quest quest = new Quest(playerId, id);
		this.quests.get(group).put(id, quest);

		if (notify) {
			PlayerHelper.sendProtocol(playerId,
					QuestGroupPush_20100008.newBuilder().setType(questConfig.Type).build());
			QuestHelper.notifyQuestChange(quest, UpdateType.ADD);
		}
		setState(quest, initState, notify);
		// 检查任务是否可以完成
		quest.checkFinish();

		Quest now = get(quest.getId());
		if (now != null) {
			// 有可能在设置状态时就已经完成删除了
//			now.insert();
		}
		return quest;
	}

	/** 
	 * 关闭某个活动
	 * @param id
	 * @param notify
	 */
	public void close(int id, boolean notify) {
		QuestConfig questConfig = QuestHelper.getQuestConfig(id);

		int group = questConfig.Type;
		Map<Integer, Quest> map = this.quests.get(group);
		if (map != null) {
			Quest remove = map.remove(id);
			if (remove != null) {
				remove.close();
				if (notify) {
					QuestHelper.notifyQuestChange(remove, UpdateType.DELETE);
				}
			}
		}
	}

	public void close(int[] ids, boolean notify) {
		for (int i : ids) {
			close(i, notify);
		}
	}

	public void open(QuestConfig questConfig, boolean notify) {

		open(questConfig.ID, QuestHelper.SHOW, notify);
	}

	public List<Quest> open(List<Integer> ids) {

		List<Quest> list = new ArrayList<>();
		for (int i = 0; i < ids.size(); i++) {
			list.add(open(ids.get(i), true));
		}
		return list;
	}

	public void open(int[] ids, boolean notify) {

		for (int id : ids) {
			open(id, notify);
		}
	}

	public void remove(int id) {
		QuestConfig missionConfig = QuestHelper.getQuestConfig(id);
		Map<Integer, Quest> group = getGroup(missionConfig.Type);

		Quest quest = group.remove(id);
		if (quest != null) {
			quest.close();
//			quest.delete();
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
		/*		if (questConfig.getLevel() > 0 && player.getData().getLevel() >= questConfig.getLevel()) {
		
					if (questConfig.getEndLevel() > 0 && player.getData().getLevel() > questConfig.getEndLevel()) {
						return false;
					}
					return true;
				}*/

//		Quest quest = get(questConfig.getId(), questConfig.getType());
//		if (quest != null) {
//			return false;
//		}
//		if (questConfig.getPreQuest() > 0) {
//			boolean pass = isPass(questConfig.getPreQuest());
//			if (!pass) {
//				return false;
//			}
//		}
		return true;
//		return false;
	}

	public boolean isPass(int id) {
		QuestConfig config = QuestHelper.getQuestConfig(id);
		Quest quest = get(id);
		if (!config.IsDeleteOnFinish) {
			return quest != null && quest.getState() >= QuestHelper.CAN_GIVEWARD;
		}
		if (QuestHelper.isFinished(quest)) {
			return true;
		}
		// 通过id数值判断是否完成；
		if (config.Type == QuestTypeEnum.MainLine.ID) {
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
		} else if (config.Type == QuestTypeEnum.BranchLine.ID) {
			/*		Map<Integer, Quest> group = getGroup(QuestTypeEnum.BranchLine);
					MainlineMissionConfig mainlineMissionConfig = (MainlineMissionConfig) config;
					int groupId = mainlineMissionConfig.Group;
					Set<Entry<Integer, Quest>> entrySet = group.entrySet();
					for (Entry<Integer, Quest> entry : entrySet) {
						int k = entry.getKey();
						Quest v = entry.getValue();
			
						QuestConfig c = QuestHelper.getQuestConfig(k);
						if (c.Group == groupId) {
							if (id < k) {
								return true;
							} else if (id == k) {
								return QuestHelper.isFinished(v);
							} else {
								return false;
							}
						}
			
					}*/
		}

		return false;
	}

	public List<Integer> canReceiveIds(int group) {

		return this.quests.get(group).values().stream().filter(QuestHelper::canReceive).map(q -> q.getId())
				.collect(Collectors.toList());
	}

	public Map<Integer, QuestChallenge> getChallenges() {
		return challenges;
	}

	public boolean canAccept(int id) {

		QuestConfig missionConfig = QuestHelper.getQuestConfig(id);
//		Quest quest = get(id, missionConfig.Type); 
//		if (quest != null) {
//			return false ; 
//		}
		/*		if (player.getData().getLevel() < missionConfig.getLevel()) {
					return false;
				}
				int accessType = missionConfig.getAccessMode().get(0);
				if (accessType == 1)
					return true;
				QuestTypeEnum type = missionConfig.Type;
				if (type == QuestTypeEnum.MainLine || type == QuestTypeEnum.BranchLine) {
					if (!isPass(id)) {
						return true;
					}
				}*/
		return false;
	}

	public void setState(Quest quest, byte state, boolean notify) {
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
				if (notify) {
					QuestHelper.notifyQuestChange(quest, UpdateType.UPDATE);
				}
			}
		}

	}

	private void setState(Quest quest) {
		byte state = quest.getState();
		QuestConfig questConfig = QuestHelper.getQuestConfig(quest.getId());
		switch (state) {
		case QuestHelper.SHOW:
//			if (player.getData().getLevel() >= questConfig.getLevel()) {
				quest.setState(QuestHelper.CAN_ACCEPT);
				setState(quest);
//			}
			break;
		case QuestHelper.CAN_ACCEPT:
//			if (questConfig.getAccessMode().get(0) == 1) {
				quest.setState(QuestHelper.ACCEPTED);
				setState(quest);
//			}
			break;
		case QuestHelper.ACCEPTED:
			quest.initCondition();
			if (questConfig.Type != QuestTypeEnum.Achievement.ID) {
				GameLogger.task(player, quest.getId(), false);
			}
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
			/*			if (questConfig.getModeOfDelivery().get(0) == 1) { // 自动交付（自动领奖）
							List<RewardInfo> receive = receive(quest.getId());
							if (receive != null && !receive.isEmpty()) {
			
								QuestRewardPush_20600008.Builder builder = QuestRewardPush_20600008.newBuilder();
								builder.setId(quest.getId());
								builder.addAllRewards(receive);
								PlayerHelper.sendProtocol(playerId, builder.build());
							}
						}*/
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

	@Override
	public void init() {
		if (quests == null) {
			quests = new HashMap<Integer, Map<Integer, Quest>>();
		}
		for (QuestTypeEnum type : QuestTypeEnum.values()) {
			if (!quests.containsKey(type.ID)) {
				quests.put(type.ID, new HashMap<Integer, Quest>());
			}
		}
		challenges = new HashMap<>();
	}

	public void addCumulativeCount(ConditionTypeEnum type, int count) {
		cumulativeCountMap.add(type.ID, count);
		/*		int id = type.ID;
				int arg1 = args.length > 0 ? args[0] : 0;
				int arg2 = args.length > 1 ? args[1] : 0;
				Integer oldCount = this.conditionCountMap.get(id, arg1, arg2);
				int newCount = oldCount == null ? count : oldCount + count;
				this.conditionCountMap.put(id, arg1, arg2, newCount);*/
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

	public void addCumulativeCount(ConditionTypeEnum type, int count, int... ext) {
		if (ext.length == 0) {
			addCumulativeCount(type, count);
		} else {
			cumulativeCountExtMap.add(type.ID, count, ext);
		}
	}

	public List<QuestGroupInfo> buildAllGroup() {
		List<QuestGroupInfo> list = new ArrayList<>();

		quests.forEach((k, v) -> {
			QuestGroupInfo.Builder groupInfo = QuestGroupInfo.newBuilder();
			groupInfo.setGroup(k);
			v.forEach((kk, vv) -> {
				groupInfo.addQuests(vv.toQuestInfo());
			});
			if (groupInfo.getQuestsCount() > 0) {
				list.add(groupInfo.build());
			}
		});
		return list;

	}
	@Override
	public void buildPlayerAllInfo(Builder builder) {

		builder.addAllQuestGroups(buildAllGroup());

		Map<Integer, List<Integer>> activeRewardTypeMap = player.getPointRewardModule().getActiveRewardTypeMap(PointRewardType.QUEST);

		activeRewardTypeMap.forEach((k, v) -> {
			QuestGroupPointRewardInfo.Builder rewardInfo = QuestGroupPointRewardInfo.newBuilder();
			rewardInfo.setGroup(k);
			rewardInfo.addAllIndex(v);
			builder.addQuestGroupPointRewards(rewardInfo.build());
		});
	}

	public void refreshNewQuest(QuestTypeEnum type, boolean notify) {
		Map<Integer, Quest> group = getGroup(type);
		if (group == null || group.isEmpty()) {
			if (type == QuestTypeEnum.Achievement) {
//				Collection<AchievementMissionConfig> list = AchievementMissionManager.getInstance().list();
//				for (AchievementMissionConfig config : list) {
//					open(config, notify);
//				}
			}
		} else {
			for (Entry<Integer, Quest> entry : group.entrySet()) {
				Quest quest = entry.getValue();
				if (quest.getState() == QuestHelper.SHOW) {
					setState(quest, QuestHelper.CAN_ACCEPT, notify);
				}
			}
		}
	}
	
	/** 
	 * 第一次初始化某种类型的任务
	 * @param type
	 * @param notify
	 */
	private void initQuest(QuestTypeEnum type, boolean notify) {

		Map<Integer, Quest> map = quests.get(type.ID);
		if (!map.isEmpty()) {
			return;
		}
		if (type == QuestTypeEnum.Achievement) {
			Map<Integer, List<QuestConfig>> groups = QuestManager.instance().getGroups();
			groups.forEach((group, list) -> {
				if (!list.isEmpty()) {
					QuestConfig questConfig = list.get(0);
					if (questConfig.Type == QuestTypeEnum.Achievement.ID) {
						open(questConfig.ID, notify);
					}
				}
			});
		} else {
			List<QuestConfig> typeList = QuestManager.instance().getTypeList(type.ID);
			for (QuestConfig questConfig : typeList) {
				open(questConfig.ID, notify);
			}
		}

	}

	private void initQuestFirst() {
		if (!player.isFuncOpen(InitialUI.Task)) {
			return;
		}
		initQuest(QuestTypeEnum.Daily, false);
		initQuest(QuestTypeEnum.Weekly, false);
		initQuest(QuestTypeEnum.Achievement, false);
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	/**
	 * 这里提升任务模块的事件处理优先级：
	 * 比如对于充值事件，需要先处理，增加累计充值数量。 
	 * 而之后的具体某个充值任务，可能会读取累计充值数量，所以需要有先后顺序
	 */
	@Override
	public int processOrder() {
		return 100;
	}
	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {
		case NewWeek: {
			refreshQuest(QuestTypeEnum.Weekly);
			player.getCurrencyModule().setCount(Asset.WeeklyPoint.ID, 0);

			PointRewardModule pointRewardModule = player.getPointRewardModule();
			pointRewardModule.clearActiveRewardList(PointRewardType.QUEST, QuestTypeEnum.Weekly.ID);
			break;
		}
		case NewDay: {
			refreshQuest(QuestTypeEnum.Daily);
			refreshQuest(QuestTypeEnum.Guild);
			player.getCurrencyModule().setCount(Asset.DailyPoint.ID, 0);

			PointRewardModule pointRewardModule = player.getPointRewardModule();
			pointRewardModule.clearActiveRewardList(PointRewardType.QUEST, QuestTypeEnum.Daily.ID);

//			getActiveRewardList(QuestTypeEnum.Daily).clear();

			addCumulativeCount(ConditionTypeEnum.CumulativeLogins, 1);
			break;
		}
		case PLAYER_CREATE: {
			addCumulativeCount(ConditionTypeEnum.CumulativeLogins, 1);
			break;
		}
		case WatchAds: {

			addCumulativeCount(ConditionTypeEnum.WatchAdsCumulation, 1);
			break;
		}
		case FuncOpen: {
			InitialUI func = event.getParameter(0);
			if (func == InitialUI.Task) {
				initQuestFirst();
			}
			break;
		}
		case Charge: {
			addCumulativeCount(ConditionTypeEnum.AccumulatedRecharge, event.getIntParameter(0));
			addCumulativeCount(ConditionTypeEnum.RechargeCnt, 1);
			break;
		}
//		case QianLi: {
//			addCumulativeCount(ConditionTypeEnum.UpgradeAltar, 1);
//			break;
//		}
//		case QiangYuan: {
//			addCumulativeCount(ConditionTypeEnum.UpgradeHuDao, 1);
//			break;
//		}
//		case ParticipatePVPStart: {
//			addCumulativeCount(ConditionTypeEnum.ParticipatePVP, 1);
//			break;
//		}
		case ChapterWin: {
			int id = event.getIntParameter(0);
			// 这个不用了
//			BattleConfig battleConfig = BattleManager.instance().get(id);
//			if (battleConfig.BattleType == 2) {
//				addCumulativeCount(ConditionTypeEnum.EliteFinish, 1);
//			}
			break;
		}
		case BattleEnd: {
			addCumulativeCount(ConditionTypeEnum.KillMonsters, event.getIntParameter(3));
			addCumulativeCount(ConditionTypeEnum.KillBoss, event.getIntParameter(4));
			break;
		}
//		case FairyFriendsTravel: {
//			addCumulativeCount(ConditionTypeEnum.ParticipateFairyFriend, event.getIntParameter(0));
//			break;
//		}
//		case FairyFriendsGift: {
//			addCumulativeCount(ConditionTypeEnum.CumulativeGift, 1);
//			break;
//		}
		case HeroBreak: {
//			int star = event.getIntParameter(0);
			int quality = event.getIntParameter(1);
			addCumulativeCount(ConditionTypeEnum.BreakHeroCumulation, 1);
			addCumulativeCount(ConditionTypeEnum.EarnHeroCumulation, 1, quality);
			break;
		}
		case Hero: {
			int id = event.getIntParameter(0);
			HeroConfig heroConfig = HeroManager.instance().get(id);
			addCumulativeCount(ConditionTypeEnum.EarnHeroCumulation, 1, heroConfig.InitialQuality);
			break;
		}
		case CostItem: {
			int id = event.getIntParameter(0);
			int count = event.getIntParameter(1);
			if (id == Asset.diamond.ID) {
				addCumulativeCount(ConditionTypeEnum.ConsumesDiamonds, count);
			}
			break;
		}
		case Patrol: {
			boolean isFast = event.getBoolParameter(0);
			if (isFast) {
				addCumulativeCount(ConditionTypeEnum.QuickHangUpCumulation, 1);
			}
			break;
		}
		case Draw: {
			int count = event.getIntParameter(0);
			int typeId = event.getIntParameter(1);
			if (typeId == 2) {
				addCumulativeCount(ConditionTypeEnum.SupremeGacha, count);
			}

			break;
		}
		}
	}
}
