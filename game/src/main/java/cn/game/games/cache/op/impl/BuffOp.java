package cn.game.games.cache.op.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import cn.game.games.cache.entity.Buff;
import cn.game.games.cache.op.face.IBuffOp;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventHandler;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.BuffHelper;
import cn.game.games.net.game.helper.EventHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.GameConstants;
import cn.game.games.net.game.module.buff.BuffValue;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.EventOptionConfig;
import cn.game.protocol.generated.config.OldBuffConfig;
import cn.game.protocol.generated.enume.BuffTypeEnum;
import cn.game.protocol.generated.enume.EffectEnum;
import cn.game.protocol.generated.enume.EffectTargetEnum;
import cn.game.protocol.generated.enume.EffectTargetTypeEnum;
import cn.game.protocol.generated.manager.EventOptionManager;
import cn.game.protocol.generated.manager.OldBuffManager;
import cn.game.protocol.protobuf.BaseMsg.UpdateType;
import cn.game.protocol.protobuf.BuffMsg;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.Pair;

public class BuffOp extends BasePlayerModule implements IBuffOp {
	private static final Logger buffLog = LoggerFactory.getLogger("buffLog");
	private Multimap<Long, Buff>[] buffArrayMap = new Multimap[EffectTargetTypeEnum.values().length];
	private transient EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.ExploreEnd, EventTypeEnum.ExploreLevelEnd };
	private transient EventHandler eventHandler;

	@Override
	public void initAfter() {
		for (int i = 0; i < buffArrayMap.length; i++) {
			buffArrayMap[i] = HashMultimap.create();
		}
		regEvent();
	}

	private void regEvent() {
		// 探索结束时清理探索buff
		eventHandler = new EventHandler() {
			@Override
			public void handleEvent(GameEvent event) {
				EventTypeEnum type = event.getType();
				if (type == EventTypeEnum.ExploreEnd) {
					interruptAndDestory(1);
				}else if(type == EventTypeEnum.ExploreLevelEnd){
					removeCrossExploreLevelBuff();
				}
			}

			@Override
			public EventTypeEnum[] getEventTypes() {
				// TODO Auto-generated method stub
				return null;
			}
		};
		player.registerEventHandler(events, eventHandler);
	}

	@Override
	public void initLoadData(List<Buff> buffList) {
		for (Buff buff : buffList) {
			putBuff(buff.getTarget(), buff);
			buff.init(player);
		}
	}

	@Override
	public void save() {
		for (Multimap<Long, Buff> multimap : buffArrayMap) {
			for (Buff buff : multimap.values()) {
				buff.update();
			}
		}
	}

	@Override
	public List<Buff> add(int buffId, List<? extends Number> targetIdList) {
		List<Buff> ret = new ArrayList<>();
		if (buffId == 0) {
			buffLog.info("player[{}],buff异常,正在增加buffId为0的buff", playerId);
			return ret;
		}
		// 后端忽略的buff
		if (BuffHelper.ignore(buffId)) {
			return ret;
		}
		// 获得buff作用目标
		List<Long> targetIds = getTargets(buffId, targetIdList);
		// 处理buff抵消
		counteract(buffId, targetIds);
		// 过滤掉免疫此buff的target
		filterImmune(buffId, targetIds);

		List<RewardInfo> buffRewards = new ArrayList<>();
		// 只有开始效果, 立即生效一次
		if (BuffHelper.onlyStartEffect(buffId)) {
			buffLog.info("player[{}],buff[{}]只有开始效果, 立即生效一次,目标[{}]", playerId, buffId, targetIds.toString());
			buffRewards = BuffHelper.addBuffEffect(playerId, buffId, targetIds);
		}
		// 处理buff堆叠
		stack(buffId, targetIds);
		// buff目标为空,只是用buff携带奖励或展示
		if (targetIds.isEmpty()) {
			return emptyBuff(buffId, ret, buffRewards);
		}

		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buffId);
		int useType = buffConfig.getUseType();
		int useNum = buffConfig.getUseNum();
		int round = buffConfig.getRound();
		for (long target : targetIds) {
			Buff buff = Buff.valueOf(buffId, playerId, target, buffRewards);
			// 只有开始效果,不产生buff对象,直接返回
			if (BuffHelper.onlyStartEffect(buffId)) {
				ret.add(buff);
				return ret;
			}
			// 持续生效的buff,设置回合数
			buff.setRound(round);
			if (useType == 0) {
				buff.setUseNum(1);
			} else {
				buff.setUseNum(useNum);
			}

			buff.init(player);
			putBuff(target, buff);

			DAO.insert(buff);
			ret.add(buff);
		}


		BuffHelper.pushBuffShow(playerId, ret);

		for (Buff buff : ret) {
			BuffHelper.pushBuffUpdate(buff, UpdateType.ADD);
		}
		EventHelper.handleEvent(playerId, new GameEvent(EventTypeEnum.BuffChange, targetIds));

		return ret;
	}

	private List<Buff> emptyBuff(int buffId, List<Buff> ret, List<RewardInfo> buffRewards) {
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buffId);
		EffectTargetEnum targetType = buffConfig.getTargetType();
		if (targetType == EffectTargetEnum.Null || targetType == EffectTargetEnum.PlayerResouce) {
			Buff buff = new Buff();
			buff.setBuffId(buffId);
			buff.setRewards(buffRewards);
			ret.add(buff);
			return ret;
		}
		return ret;
	}

	/** 处理buff目标 */
	private List<Long> getTargets(int id, List<? extends Number> targetIdList) {
		if (targetIdList == null) {
			targetIdList = new ArrayList<>();
		}
		buffLog.info("player[{}],准备增加buff[{}],目标[{}]", playerId, id, targetIdList.toString());

		// 优先使用buff表设定目标
		List<? extends Number> defaultTargets = BuffHelper.getTargetIds(playerId, id);
		if (!defaultTargets.isEmpty()) {
			targetIdList = defaultTargets;
		}
		List<Long> targetIds = new ArrayList<>();
		for (Number long1 : targetIdList) {
			targetIds.add(long1.longValue());
		}
		buffLog.info("player[{}],准备增加buff[{}],获取到目标[{}]", playerId, id, targetIds.toString());
		return targetIds;
	}

	private void stack(int buffId, List<Long> targetIds) {
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buffId);
		int useNum = buffConfig.getUseNum();
		int round = buffConfig.getRound();
		EffectTargetEnum targetType = buffConfig.getTargetType();
		// 已有的buff更新回合数
		Buff oldBuff = null;
		// buff叠加类型
		int addType = buffConfig.getAddType();
		int addLimit = buffConfig.getAddLimit();
		// 对于不同叠加类型buff,按规则处理,targetId从targetIds中移除。以免与下面根据targetId添加buff的逻辑冲突
		Iterator<? extends Number> iterator = targetIds.iterator();
		while (iterator.hasNext()) {
			long tid = iterator.next().longValue();
			oldBuff = getOldBuff(tid, buffId);
			if (oldBuff == null) {
				continue;
			}
			if (addType == GameConstants.NEW_BUFF_COVER) {
				iterator.remove();
				oldBuff.setUseNum(useNum);
				oldBuff.setRound(round);
				buffLog.info("player[{}],buff叠加:新buff覆盖老buff[{}],新useNum[{}],新round[{}]", playerId, buffId, useNum, round);
				BuffHelper.pushBuffUpdate(oldBuff, UpdateType.UPDATE);

			} else if (addType == GameConstants.NEW_BUFF_NOT_ADD) {
				iterator.remove();
				buffLog.info("player[{}],buff叠加:有老buff在新buff不添加[{}]", playerId, buffId);
				
			} else if (addType == GameConstants.NEW_BUFF_INSERT) {
				EffectTargetTypeEnum effectTargetTypeEnum = EffectTargetTypeEnum.get(targetType.getType());
				int hasCount = this.getBuffCount(effectTargetTypeEnum, tid, buffId);
				if (hasCount >= addLimit) {
					iterator.remove();
					buffLog.info("player[{}],buff叠加:老buff,新buff共存,此buff已达最大获取数量[{}],不再添加[{}]", playerId, addLimit, buffId);
				}
				buffLog.info("player[{}],buff叠加:老buff,新buff共存", playerId, buffId);
			}else {
				throw new IllegalArgumentException("未知的buff叠加类型:" + addType);
			}
		}
	}

	/**
	 * 抵消buff 移除目标身上可以被抵消的buff,并从targetIds里移除目标
	 * 
	 * @param newBuffId 准备加的buff
	 * @param targetIds
	 */
	private void counteract(int newBuffId, List<? extends Number> targetIds) {
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(newBuffId);
		EffectTargetTypeEnum effectTargetTypeEnum = EffectTargetTypeEnum.get(buffConfig.getTargetType().getType());
		int newCounteractType = buffConfig.getType().getCounteractType();

		Set<Integer> delBuffs = new HashSet<>();
		Iterator<? extends Number> iterator = targetIds.iterator();
		while (iterator.hasNext()) {
			long targetId = iterator.next().longValue();
			Collection<Buff> buffs = buffArrayMap[effectTargetTypeEnum.ordinal()].get(targetId);
			for (Buff buff : buffs) {
				OldBuffConfig targetBuffConfig = OldBuffManager.getInstance().getBuffConfig(buff.getBuffId());
				int counteractType = targetBuffConfig.getType().getCounteractType();
				if (counteractType == 0) {
					continue;
				}
				if (counteractType == newCounteractType) {
					delBuffs.add(buff.getBuffId());
				}
			}
			// 移除身上可以被抵消的buff
			for (Integer buffId : delBuffs) {
				this.remove(buffId, targetId);
				buffLog.info("player[{}],添加buff[{}]时,身上的老buff[{}]被抵消了", playerId, newBuffId, buffId);
			}
			// 移除这个目标,新buff就不会加到这个目标上
			if (!delBuffs.isEmpty()) {
				iterator.remove();
				buffLog.info("player[{}],目标[{}],将不再添加buff[{}],因为被抵消消耗掉了", playerId, targetIds.toString(), newBuffId);
				delBuffs.clear();
			}
		}
	}

	/**
	 * 过滤免疫
	 * 
	 * @param buffId
	 * @param targetIds
	 */
	private void filterImmune(int buffId, List<Long> targetIds) {
		Iterator<Long> it = targetIds.iterator();
		// 过滤掉有免疫状态的target
		while (it.hasNext()) {
			long targetId = it.next();
			if (isImmune(buffId, targetId)) {
				buffLog.info("player[{}],目标[{}],身上有buff免疫[{}],将不添加buff[{}]", playerId, targetIds.toString(), buffId,
						buffId);
				it.remove();
			}
		}
	}

	/**
	 * 是否免疫某buff
	 * 
	 * @param buffId
	 * @param targetId
	 * @return
	 */
	private boolean isImmune(int buffId, long targetId) {
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buffId);
		BuffTypeEnum buffType = buffConfig.getType();
		int buffTypeId = buffType.getId();

		EffectTargetEnum targetType = buffConfig.getTargetType();
		EffectTargetTypeEnum effectTargetTypeEnum = EffectTargetTypeEnum.get(targetType.getType());
		BuffValue buffValue = this.getBuffValue(effectTargetTypeEnum, targetId, EffectEnum.ImmuneBuffByType);
		boolean immune = buffValue.hasValueDefault(buffTypeId);
		return immune;
	}

	private void putBuff(long tid, Buff buff) {

		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buff.getBuffId());
		EffectTargetTypeEnum effectTargetTypeEnum = EffectTargetTypeEnum.get(buffConfig.getTargetType().getType());
		buffArrayMap[effectTargetTypeEnum.ordinal()].put(tid, buff);
	}

	private Buff getOldBuff(long tid, int bufId) {
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(bufId);
		EffectTargetTypeEnum effectTargetTypeEnum = EffectTargetTypeEnum.get(buffConfig.getTargetType().getType());

		Collection<Buff> collection = buffArrayMap[effectTargetTypeEnum.ordinal()].get(tid);
		for (Buff buff : collection) {
			if (buff.getBuffId() == bufId) {
				return buff;
			}
		}
		return null;
	}

	@Override
	public List<Buff> add(List<Integer> ids, List<? extends Number> targetIds) {
		List<Buff> res = new ArrayList<>();
		for (int buff : ids) {
			List<Buff> b = add(buff, targetIds);
			res.addAll(b);
		}
		return res;
	}

	@Override
	public List<Buff> add(int[] ids, List<? extends Number> targetIds) {
		List<Buff> res = new ArrayList<>();
		for (int buff : ids) {
			List<Buff> b = add(buff, targetIds);
			res.addAll(b);
		}
		return res;
	}

	@Override
	public void remove(EffectTargetTypeEnum targetType) {
		Multimap<Long, Buff> multimap = buffArrayMap[targetType.ordinal()];
		for (Buff buff : multimap.values()) {
			buff.unregisterEventHandler();
			DAO.delete(buff);
			BuffHelper.pushBuffUpdate(buff, UpdateType.DELETE);
			printDelLog(buff);
		}

		if (targetType == EffectTargetTypeEnum.Role) {
			Set<Long> targets = new HashSet<>();
			for (Buff buff : multimap.values()) {
				List<? extends Number> targetIdList = BuffHelper.getTargetIds(playerId, buff.getBuffId());
				List<Long> targetIds = new ArrayList<>();
				for (Number long1 : targetIdList) {
					targetIds.add(long1.longValue());
				}
				targetIds = BuffHelper.convertTargetIds(playerId, buff.getBuffId(), targetIds);
				targets.addAll(targetIds);
			}
			EventHelper.handleEvent(playerId, new GameEvent(EventTypeEnum.BuffChange, new ArrayList<>(targets)));
		}
		multimap.clear();
	}

	@Override
	public void remove(List<Integer> buffId) {
		for (int buff : buffId) {
			remove(buff);
		}
	}
	@Override
	public void remove(int... ids) {
		for (int buff : ids) {
			remove(buff);
		}
	}
	
	@Override
	public void remove(long targetId, List<Integer> buffIds) {
		for (Integer buffId : buffIds) {
			remove(buffId, targetId);
		}
	}

	@Override
	public void remove(Integer buffId) {
		List<? extends Number> targetIdList = BuffHelper.getTargetIds(playerId, buffId);
		List<Long> targetIds = new ArrayList<>();
		for (Number long1 : targetIdList) {
			targetIds.add(long1.longValue());
		}
		targetIds = BuffHelper.convertTargetIds(playerId, buffId, targetIds);
		targetIds.forEach(e -> remove(buffId, e));
		EventHelper.handleEvent(playerId, new GameEvent(EventTypeEnum.BuffChange, targetIds));
	}

	@Override
	public void remove(int buffId, long target) {
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buffId);
		EffectTargetTypeEnum effectTargetTypeEnum = EffectTargetTypeEnum.get(buffConfig.getTargetType().getType());
		Multimap<Long, Buff> multimap = buffArrayMap[effectTargetTypeEnum.ordinal()];
		Iterator<Buff> iterator = multimap.get(target).iterator();
		while (iterator.hasNext()) {
			Buff buff = iterator.next();
			if (buff.getBuffId() == buffId) {
				iterator.remove();
				buff.unregisterEventHandler();
				DAO.delete(buff);
				BuffHelper.pushBuffUpdate(buff, UpdateType.DELETE);
				printDelLog(buff);
			}
		}
		List<Long> targets = new ArrayList<>();
		targets.add(target);
		EventHelper.handleEvent(playerId, new GameEvent(EventTypeEnum.BuffChange, targets));
	}

	@Override
	public void remove(EffectTargetTypeEnum effectTargetTypeEnum, int buffType, long target) {
		Multimap<Long, Buff> multimap = buffArrayMap[effectTargetTypeEnum.ordinal()];
		Iterator<Buff> iterator = multimap.get(target).iterator();
		while (iterator.hasNext()) {
			Buff buff = iterator.next();
			OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buff.getBuffId());
			if (buffConfig.getType().getId() == buffType) {
				iterator.remove();
				buff.unregisterEventHandler();
				DAO.delete(buff);
				BuffHelper.pushBuffUpdate(buff, UpdateType.DELETE);
				printDelLog(buff);
//				buffLog.info("player[{}],移除目标[{}]身上,类型为[{}],id为[{}]的buff", playerId, target, buffType, buff.getBuffId());
			}
		}
		List<Long> targets = new ArrayList<>();
		targets.add(target);
		EventHelper.handleEvent(playerId, new GameEvent(EventTypeEnum.BuffChange, targets));
	}

	@Override
	public void remove(EffectTargetTypeEnum targetType, long target) {
		Multimap<Long, Buff> multimap = buffArrayMap[targetType.ordinal()];
		Collection<Buff> removeAll = multimap.removeAll(target);
		for (Buff buff : removeAll) {
			buff.unregisterEventHandler();
			DAO.delete(buff);
			BuffHelper.pushBuffUpdate(buff, UpdateType.DELETE);
			printDelLog(buff);
		}
		List<Long> targets = new ArrayList<>();
		targets.add(target);
		EventHelper.handleEvent(playerId, new GameEvent(EventTypeEnum.BuffChange, targets));
	}

	@Override
	public BuffValue getBuffValue(EffectTargetTypeEnum targetType, long id, EffectEnum effectType) {
		// Map<buff表numTypeParam, Map<effect表的idParam, effect表的numParam>>
		Map<Integer, Map<Integer, Integer>> ret = new HashMap<>();
		BuffValue buffValue = new BuffValue(ret);

		Multimap<Long, Buff> multimap = buffArrayMap[targetType.ordinal()];
		for (Buff buff : multimap.get(id)) {
			mergeEffectValue(ret, buff.getBuffId(), effectType);
		}
		return buffValue;
	}

	@Override
	public Multimap<Long, Buff> getBuffs(EffectTargetTypeEnum targetType) {

		return buffArrayMap[targetType.ordinal()];
	}

	@Override
	public int getBuffCount(EffectTargetTypeEnum targetType, long targetId, int buffId) {
		Multimap<Long, Buff> multimap = buffArrayMap[targetType.ordinal()];
		int res = 0;
		for (Buff buff : multimap.get(targetId)) {
			if (buff.getBuffId() == buffId) {
				res++;
			}
		}
		return res;
	}

	private boolean hasEffectType(int buffId, EffectEnum effectType) {
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buffId);
		if (buffConfig.getEffectType() == effectType) {
			return true;
		}

//		int[] bufeffect = buffConfig.getContinuousEffect();
//		for (int i : bufeffect) {
//			EffectConfig effectConfig = EffectManager.getInstance().getEffectConfig(i);
//			if (effectConfig.getEffectType() == effectType) {
//				return true;
//			}
//		}
		return false;
	}

	@Override
	public boolean hasEffectType(EffectTargetTypeEnum targetType, long targetId, EffectEnum effectType) {
		Collection<Buff> collection = buffArrayMap[targetType.ordinal()].get(targetId);
		for (Buff buff : collection) {
			if (hasEffectType(buff.getBuffId(), effectType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasBuff(long targetId, int buffId) {
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buffId);
		EffectTargetTypeEnum targetType = EffectTargetTypeEnum.get(buffConfig.getTargetType().getType());
		Collection<Buff> collection = buffArrayMap[targetType.ordinal()].get(targetId);
		for (Buff buff : collection) {
			if (buff.getBuffId() == buffId) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean hasBuffType(EffectTargetTypeEnum targetType, long targetId, int buffTypeId) {
		Collection<Buff> collection = buffArrayMap[targetType.ordinal()].get(targetId);
		for (Buff buff : collection) {
			OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buff.getBuffId());
			BuffTypeEnum type = buffConfig.getType();
			if (type.getId() == buffTypeId) {
				return true;
			}
		}
		return false;
	}

	private Map<Integer, Map<Integer, Integer>> mergeEffectValue(Map<Integer, Map<Integer, Integer>> ret, int buffId,
			EffectEnum effectType) {
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buffId);

		if (buffConfig.getEffectType() == effectType) {
			Map<Integer, Integer> integerIntegerMap = ret.get(buffConfig.getNumTypeParam());
			if (integerIntegerMap == null) {
				integerIntegerMap = new HashMap<>();
				ret.put(buffConfig.getNumTypeParam(), integerIntegerMap);
			}
			integerIntegerMap.merge(buffConfig.getIdParam(), buffConfig.getNumParam(), (o, n) -> o + n);
		}
		return ret;
	}

	@Override
	public void interruptAndDestory(int type) {
		for (Multimap<Long, Buff> multimap : buffArrayMap) {
			Iterator<Buff> iterator = multimap.values().iterator();
			while (iterator.hasNext()) {
				Buff buff = iterator.next();
				OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buff.getBuffId());
				if (buffConfig.getTargetType().getDestroyType() == type) {
					iterator.remove();
					DAO.delete(buff);
					BuffHelper.pushBuffUpdate(buff, UpdateType.DELETE);
					printDelLog(buff);
				}
			}
		}
	}

	@Override
	public void addEvent(int eventId) {
		List<EventOptionConfig> eventIdList = EventOptionManager.getInstance().getEventIdList(eventId);
		if (eventIdList.size() == 1) {
			// 直接添加buff
			List<Buff> buffs = PlayerHelper.chooseEventOption(player, eventId, eventIdList.get(0).getId(), true);
			Map<Integer, Integer> addResources = new HashMap<>();
			for (Buff buff : buffs) {
				OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buff.getBuffId());
				EffectEnum effectType = buffConfig.getEffectType();
				if (effectType == EffectEnum.AddOrDelGoods) {
					addResources.put(buffConfig.getIdParam(), buffConfig.getNumParam());
				}
			}
			// 触发事件
			EventHelper.handleEvent(playerId, new GameEvent(EventTypeEnum.ExploreGetResources, addResources));

		} else {

			/*		PlayerExt playerExt = PlayerManager.getInstance().getPlayer(playerId).getExt();
					playerExt.addEventId(eventId);
					PlayerExt update = PlayerExt.valueOf(playerId);
					update.setEventIds(playerExt.getEventIds());
					DAO.updateSelective(update);*/

			BuffMsg.EventPush_01001100.Builder response = BuffMsg.EventPush_01001100.newBuilder();
			response.setEventId(eventId);
			GameClientManager.getInstance().noticeOne(response.build(), this.playerId);
		}
	}

	@Override
	public Buff get(int buffId, long target) {
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buffId);
		EffectTargetTypeEnum effectTargetTypeEnum = EffectTargetTypeEnum.get(buffConfig.getTargetType().getType());
		Multimap<Long, Buff> multimap = buffArrayMap[effectTargetTypeEnum.ordinal()];
		Iterator<Buff> iterator = multimap.get(target).iterator();
		while (iterator.hasNext()) {
			Buff buff = iterator.next();
			if (buff.getBuffId() == buffId) {
				return buff;
			}
		}
		return null;
	}
	
	@Override
	public void removeCrossExploreLevelBuff() {
		remove(EffectTargetTypeEnum.ExploreNpc);
		for (int i = 0; i < buffArrayMap.length; i++) {
			Multimap<Long, Buff> multimap = buffArrayMap[i];

			List<Pair<Long, Buff>> dels = new ArrayList<>();
			Map<Long, Collection<Buff>> asMap = multimap.asMap();
			for (Long targetId : asMap.keySet()) {
				Collection<Buff> buffs = asMap.get(targetId);
				for (Buff buff : buffs) {
					boolean del = BuffHelper.crossExploreLevelDel(buff.getBuffId());
					if (del) {
						dels.add(new Pair<Long, Buff>(targetId, buff));
					}
				}
			}
			for (Pair<Long, Buff> pair : dels) {
				Long targetId = pair.first;
				Buff buff = pair.second;
				multimap.remove(targetId, buff);
				buff.unregisterEventHandler();
				DAO.delete(buff);
				BuffHelper.pushBuffUpdate(buff, UpdateType.DELETE);
				printDelLog(buff);
				if (i + 1 == EffectTargetTypeEnum.Role.getId()) {
					EventHelper.handleEvent(playerId, new GameEvent(EventTypeEnum.BuffChange, Arrays.asList(targetId)));
				}
			}
		}
	}
	
	private void printDelLog(Buff buff) {
		int buffId = buff.getBuffId();
		long target = buff.getTarget();
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buffId);
//		EffectTargetTypeEnum type = EffectTargetTypeEnum.get(buffConfig.getTargetType().getType());
		EffectEnum effect = buffConfig.getEffectType();
 		int idParam = buffConfig.getIdParam();
		int numParam = buffConfig.getNumParam();
		int mode = buffConfig.getNumTypeParam();
//		AttributeTypeEnum attrType = RoleHelper.getAttributeType(idParam);
//		AttributeSubTypeEnum subType = RoleHelper.getSubType(idParam);
		buffLog.info("player[{}],删除了buff[{}],目标[{}],效果[{}],idParam[{}],模式[{}],值[{}]", playerId, buffId, target,
				effect.getDesc(), idParam, GameConstants.modeString(mode), numParam);
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleEvent(GameEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void initFromDb(ListIterator<?> iterator) {
		// TODO Auto-generated method stub

	}
	@Override
	public void initFromDbAfter() {

	};

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}

}
