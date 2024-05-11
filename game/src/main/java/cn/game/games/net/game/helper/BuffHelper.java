package cn.game.games.net.game.helper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;

import cn.game.games.cache.entity.Buff;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.Role;
import cn.game.games.cache.op.impl.BuffOp;
import cn.game.games.cache.op.impl.RoleOp;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.GameConstants;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.buff.BuffValue;
import cn.game.games.util.PbBuilder;
import cn.game.protocol.generated.config.OldBuffConfig;
import cn.game.protocol.generated.enume.AttributeSubTypeEnum;
import cn.game.protocol.generated.enume.AttributeTypeEnum;
import cn.game.protocol.generated.enume.BuffTypeEnum;
import cn.game.protocol.generated.enume.EffectEnum;
import cn.game.protocol.generated.enume.EffectTargetEnum;
import cn.game.protocol.generated.enume.EffectTargetTypeEnum;
import cn.game.protocol.generated.manager.OldBuffManager;
import cn.game.protocol.manual.ResourceConsumeEnum;
import cn.game.protocol.protobuf.BaseMsg.UpdateType;
import cn.game.protocol.protobuf.BuffMsg.BuffPush_0100010b;
import cn.game.protocol.protobuf.BuffMsg.BuffShowPush_0100010d;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.GameUtil;
import cn.game.util.Rnd;

public class BuffHelper {
	private static final Logger buffLog = LoggerFactory.getLogger("buffLog");

	/**
	 * 获取buff监听的回合使用(触发持续效果)事件类型
	 * 
	 * @param buffId
	 * @return
	 */
	public static EventTypeEnum getUseEventTypeEnum(int buffId) {
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buffId);
		int useType = buffConfig.getUseType();
		if (useType == 0) {
			return null;
		} else {
			return getEventTypeEnum(useType);
		}
	}

	/**
	 * 获取buff监听的回合计数事件类型
	 * 
	 * @param buffId
	 * @return
	 */
	public static EventTypeEnum getRoundEventTypeEnum(int buffId) {
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buffId);
		// 产生buff对象的buff,roundPara一定有值
		int param = buffConfig.getRoundType();
		return getEventTypeEnum(param);
	}

	private static EventTypeEnum getEventTypeEnum(int param) {
		if (param == 1)
			return EventTypeEnum.ExploreRound;
		if (param == 2)
			return EventTypeEnum.BattleEnd;
		if (param == 3)
			return EventTypeEnum.ExploreMapEnd;
		if (param == 4)
			return EventTypeEnum.ExploreLevelEnd;
		if (param == 5)
			return EventTypeEnum.ExploreEnd;
		if (param == 6)
			return EventTypeEnum.ExploreRoleResurrection;
		if (param == 7)
			return EventTypeEnum.ExplorePlayerRound;
		if (param == 8)
			return EventTypeEnum.ExploreNpcRound;
		if (param == 9)
			return EventTypeEnum.ExploreWoundedInBattle;
		if (param == 10)
			return EventTypeEnum.ExploreBattleWin;
		if (param == 11)
			return EventTypeEnum.ExploreExploreStoreBuy;
		if (param == 12)
			return EventTypeEnum.ExploreMaterialReward;
		if (param == 13)
			return EventTypeEnum.ExploreBoxAndRemainsReward;
		if (param == 14)
			return EventTypeEnum.ExploreGetCoin;
		if (param == 22)
			return EventTypeEnum.Ignore;
		if (param == 23)
			return EventTypeEnum.ExploreStart;
		if (param == 888)
			return EventTypeEnum.Ignore;

		throw new IllegalArgumentException(" buff event para error : " + param);
	}

	/**
	 * 获取buff监听的结束事件类型
	 * 
	 * @param buffId
	 * @return
	 */
	public static EventTypeEnum getEndEventTypeEnum(int buffId) {
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buffId);
		int[] endPara = buffConfig.getEndPara();
		if (endPara.length == 0) {
			return null;
		}

		int param = endPara[0];
		if (param == 1)
			return EventTypeEnum.SupplyLessThanOneValue;
		if (param == 2)
			return EventTypeEnum.ExploreMapEnd;

		throw new IllegalArgumentException(" buff end para error : " + param);

	}

	/**
	 * 给玩家增加一个buff
	 * 
	 * @param playerId
	 * @param id                                    BuffConfig表id
	 * @param targetIds，添加buff时，指定目标，如果不指定目标，则为null
	 * @return 作用的目标id
	 */
	public static List<Buff> addBuff(long playerId, int id, List<Long> targetIds) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		BuffOp buffOp = player.getModule(BuffOp.class);
		return buffOp.add(id, targetIds);
	}

	public static List<Buff> addBuff(long playerId, int id, List<Long> targetIds, boolean notify) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		BuffOp buffOp = player.getModule(BuffOp.class);
		return buffOp.add(id, targetIds);
	}

//	/** 
//	 * 对目标增加多个buff效果
//	 * @param playerId
//	 * @param targetIds 目标id 
//	 */
//	public static List<RewardItem> addBuffEffect(long playerId, int buff, List<? extends Number> targetIds,
//			boolean notify) {
//		List<RewardItem> ret = new ArrayList<>();
//		List<RewardItem> rewards = addBuffEffect(playerId, buff, targetIds, notify);
//		if (rewards != null) {
//			ret.addAll(rewards);
//		}
//
//		return ret;
//	}

	/*public static List<RewardItem> addBuffEffect(long playerId, int buff, List<Long> targetIds, int... param) {
		return addBuffEffect(playerId, buff, targetIds, param);
	}*/

	/**
	 * 给玩家使用一个效果
	 * 
	 * @param playerId
	 * @param targetIds 效果作用的目标，比如玩家，某些角色等。
	 * @return 获得的奖励
	 */
	public static List<RewardInfo> addBuffEffect(long playerId, int buff, List<? extends Number> targetIds,
			int... param) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buff);
		EffectTargetEnum targetType = buffConfig.getTargetType();
		EffectEnum effectType = buffConfig.getEffectType();
		int idParam = buffConfig.getIdParam();
		int numParam = buffConfig.getNumParam();
		// 改变模式 0-绝对值,1-百分比,2-当前百分比
		int mode = buffConfig.getNumTypeParam();
		int[] extParam = buffConfig.getExtParam();

		RoleOp roleOp = player.getModule(RoleOp.class);
		BuffOp buffOp = player.getModule(BuffOp.class);

		switch (effectType) {
		case AddOrDelGoods: {
			if (numParam > 0) {
				// 获取奖励倍数
				int bei = getBuffRewardMultiple(playerId);
				buffLog.info("player[{}],buff[{}]效果,加资源前奖励倍数[{}]", playerId, buff, bei);
				numParam = numParam * bei;
				List<RewardInfo> addResources = PlayerHelper.addResources(player, idParam, numParam);
				buffLog.info("player[{}],buff[{}]效果,添加了资源[{}][{}]", playerId, buff, idParam, numParam);
				/*if (notify) {
					RewardPush_55000501.Builder pushR = RewardPush_55000501.newBuilder();
					pushR.addAllRewards(PbBuilder.buildRewardInfo(addResources));
					GameClientManager.getInstance().noticeOne(pushR, playerId);
				}*/
				return addResources;

			} else {

				PlayerHelper.delResources(player, idParam, Math.abs(numParam), mode, ResourceConsumeEnum.Effect);
				buffLog.info("player[{}],buff[{}]效果,扣除了资源[{}][{}]", playerId, buff, idParam, Math.abs(numParam));
			}

		}
			break;

		case ChangeRoleAttribute: {
			changeRoleAttr(idParam, numParam, mode, targetIds, playerId, buff);
			break;
		}
		case ChangeRandomRoleAttribute: {
			randomChangeRoleAttr(targetIds, playerId, buff);
			break;
		}

		case ChangeRoleAttributeToplimit:
			break;

		case RemoveBuffByType: {
			int removeType = idParam;
			EffectTargetTypeEnum effectTargetTypeEnum = EffectTargetTypeEnum.get(targetType.getType());
			buffLog.info("player[{}],buff[{}]效果,准备移除目标[{}]身上,类型为[{}]的buff", playerId, buff, targetIds.toString(), removeType);
			for (Number exploreRoleId : targetIds) {
				buffOp.remove(effectTargetTypeEnum, removeType, exploreRoleId.intValue());
			}
			break;
		}
		case RemoveSeriouslyInjured: {
			List<Role> roles = new ArrayList<>();
			for (Number exploreRoleId : targetIds) {
				Role role = roleOp.get(exploreRoleId.intValue());
//				重伤状态废弃了				
//				if (role.getState() == RoleState.INJURY_VALUE) {
//					role.setState((byte) RoleState.NORMAL_VALUE);
//					roles.add(role);
//					buffLog.info("player[{}],buff[{}]效果,移除了角色[{}]的重伤状态", playerId, buff, role.getId());
//				}
			}
//			RoleUpdatePush_02001003.Builder response = RoleUpdatePush_02001003.newBuilder();
//			response.addAllUpdates(PbBuilder.buildRoleUpdateInfo(roles));
//			GameClientManager.getInstance().noticeOne(response.build(), playerId);
			break;
		}
		case GetRandomGoods: {
			List<Integer> reward = Rnd.randomSubArray(extParam, numParam);
			List<RewardInfo> res = new ArrayList<>();
			int num = 1;
			for (int id : reward) {
				List<RewardInfo> addResources = PlayerHelper.addResources(player, id, num);
				buffLog.info("player[{}],buff[{}]效果,给玩家随机增加了资源[{}][{}]个", playerId, buff, id, num);
				res.addAll(addResources);
			}
			return res;
		}
		case AddBuff: {
			for (int buffId : extParam) {
				buffLog.info("player[{}],buff[{}]效果,准备给玩家加buff[{}]", playerId, buff, buffId);
				addBuff(playerId, buffId, null);
			}
			break;
		}
		case AddBuffWoundedInBattle:
			// 符合条件的角色id通过param传过来
			List<Long> exploreRoleIds = GameUtil.transform(param);
			for (int buffId : extParam) {
				buffLog.info("player[{}],buff[{}]效果,角色[{}]战斗中损失了属性值,准备给角色加buff[{}]", playerId, buff, exploreRoleIds.toString(), buffId);
				addBuff(playerId, buffId, exploreRoleIds);
			}
			break;

		case RecoveryAttributeLossInBattle:
			int attId = param[0];
			int loss = param[1];
			int recovery = loss * (numParam / 100);
			// 恢复战斗中损失的属性
			changeRoleAttr(attId, recovery, BuffValue.CHANGE_BY_VALUE, targetIds, playerId, buff);
			// 额外恢复san值
			changeRoleAttr(AttributeTypeEnum.san.getId(), extParam[0], BuffValue.CHANGE_BY_VALUE, targetIds, playerId, buff);
			break;


		case ExplorationSpirit:
			for (int buffId : extParam) {
				buffLog.info("player[{}]buff[{}]效果,从宝箱、遗骸获得物品时准备增加奖励翻倍buff[{}]", playerId, buff, buffId);
				addBuff(playerId, buffId, null);
			}
			break;

		case ViolenceMining:
			for (int buffId : extParam) {
				buffLog.info("player[{}]buff[{}]效果,从晶矿获取结晶时,准备有概率翻倍,增加奖励翻倍buff[{}]", playerId, buff, buffId);
				addBuff(playerId, buffId, null);
			}
			break;


//		case AddBattleLevel:
//			BattleLevelStartPush_13000100.Builder push = BattleLevelStartPush_13000100.newBuilder();
//			push.setType(DungeonTypeEnum.ExploreBattle.getId());
//			push.setDungeonId(BattleExplorectiveType.Buff_VALUE);
//			push.setId(idParam);
//			PlayerHelper.sendProtcol(playerId, push.build());
//			buffLog.info("player[{}]buff[{}]效果,推送一场战斗[{}]", playerId, buff, idParam);
//			break;

		case RestoreLife:
			break;
			
		case RemoveBuff:
			for (Number targetId : targetIds) {
				buffOp.remove(idParam, targetId.intValue());
				buffLog.info("player[{}]buff[{}]效果:移除了target[{}]的buff[{}]", playerId, buff, targetId.intValue(), idParam);
			}
			break;
			
		case Null:
		case ShopDiscount:
		case ArmoryDiscount:
		case ChangeShopNumbers:
		case DelItemShopNumbers:
		case BuildingEfficency:
		case AddRoleAllAttributeToplimit:
		case DelRoleAllAttributeToplimit:
		case StopMove:
		case ContaminationStop:
		case ExtraRecoveryAttributeInStateRecovery:
		case ExtraSupplyConsumeInStateRecovery:
		case RecoveryStopInStateRecovery:
		case RecoveryEpWoundedInBattleByPercent:
		case SanConsumeStopInMap:
		case SanConsumeStopInBattle:
		case ShowMonster:
		case ChangeExploreView:
		case MapStoreDiscount:
		case AddExpInBattle:
		case OnlySkip:
		case OnlyView:
		// 读属性值的时候(比如san>60 可以进行某些操作)，检查有没有这种效果的buff
		case AddRoleTemporaryAttribute: 
			break;
			
		default:
			throw new IllegalArgumentException("效果类型错误 ：  " + effectType);
		}
		return null;
	}

	/**
	 * 随机改变目标属性
	 * @param targetIds
	 * @param playerId
	 * @param buff
	 */
	private static void randomChangeRoleAttr(List<? extends Number> targetIds, long playerId, int buff) {
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buff);
		int attrId = buffConfig.getIdParam();
		int[] extParam = buffConfig.getExtParam();
		int min = extParam[0];
		int max = extParam[1];
		int num = Rnd.get(min, max);
		if (num == 0) {
			return;
		}
		int mode = buffConfig.getNumTypeParam();
		for (Number targetId : targetIds) {
			int roleId = targetId.intValue();
			changeRoleAttr(attrId, num, mode, roleId, playerId);
		}
	}

	/**
	 * buff效果加成
	 * @param playerId
	 * @param targetId
	 * @param buff 当前执行的buff
	 * @return 加成后的buff效果值
	 */
	private static int changeBuffEffect(long playerId, int targetId, int buff, int effcet) {
		return effcet;
	}

	/**
	 * 获取buff增益后的值
	 * @param playerId
	 * @param type  目标小类型
	 * @param targetId 目标id
	 * @param buff
	 * @param effcet 增益后的值
	 * @return
	 */
	private static int getBuffGain(long playerId, EffectTargetTypeEnum type, int targetId, int buff, int effcet) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		BuffOp buffOp = player.getModule(BuffOp.class);
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buff);
		BuffValue buffValue = buffOp.getBuffValue(type, targetId, EffectEnum.ChangeBuffEffect);
		BuffTypeEnum buffType = buffConfig.getType();
		int buffTypeId = buffType.getId();

		int value = buffValue.getValue(BuffValue.CHANGE_BY_VALUE, buffTypeId);
		effcet += value;
		
		int precent = buffValue.getValue(BuffValue.CHANGE_BY_PANEL_PERCENT, buffTypeId);
		int add = 0;
		if (precent > 0) {
			add = Math.round(effcet * (precent / 100f));
			effcet += add;
		} else if (precent < 0) {
			add = Math.round(effcet * (precent / 100f));
			effcet -= add;
		}
		
		return effcet;
	}

	private static void changeRoleAttr(int attrId, int num, int mode, List<? extends Number> targetIds, long playerId, int buff) {
		for (Number targetId : targetIds) {
			int roleId = targetId.intValue();
			// buff增益,让某类buff更强或更弱
			num = changeBuffEffect(playerId, roleId, buff, num);
			changeRoleAttr(attrId, num, mode, roleId, playerId);
		}
	}

	/**
	 * 修改角色属性
	 * 
	 * @param attrId   属性小id
	 * @param value    改变数量
	 * @param mode     改变模式
	 * @param roleId
	 * @param playerId
	 */
	private static void changeRoleAttr(int attrId, int value, int mode, int roleId, long playerId) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		RoleOp roleOp = player.getModule(RoleOp.class);
		// 全属性修改,修改的是属性上限
		if (attrId == 0) {
			return;
		}
		AttributeTypeEnum attrType = RoleHelper.getAttributeType(attrId);
		AttributeSubTypeEnum subType = RoleHelper.getSubType(attrId);

		roleOp.changeAttr(roleId, attrType.getId(), subType.getId(), mode, value, true);
		buffLog.info("player[{}],buff效果,修改角色[{}][{}][{}],模式[{}],值[{}]", playerId, roleId, attrType.getDesc(), subType.getDesc(), GameConstants.modeString(mode), value);
	}


	/**
	 * 获取buff奖励倍数
	 * 
	 * @param playerId
	 * @return
	 */
	public static int getBuffRewardMultiple(long playerId) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		BuffOp buffOp = player.getModule(BuffOp.class);
		boolean have = buffOp.hasEffectType(EffectTargetTypeEnum.Explore, playerId, EffectEnum.RewardDouble);
		int bei = 1;
		if (have) {
			BuffValue buffValue = buffOp.getBuffValue(EffectTargetTypeEnum.Explore, playerId, EffectEnum.RewardDouble);
			bei = buffValue.getValueDefault();
		}
		return bei;
	}

	/**
	 * 获取buff的目标id集合 优先用配置表中指定id，没有根据类型计g算id
	 * 
	 * @param playerId
	 * @param buffId
	 * @return
	 */
	public static List<? extends Number> getTargetIds(long playerId, int buffId) {
		List<Long> ret = new ArrayList<>();
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buffId);
		if (buffConfig.getTarget() > 0) {
			ret.add((long) buffConfig.getTarget());
			return ret;
		}
		EffectTargetEnum type = buffConfig.getTargetType();
		switch (type) {
		case Null:
			break;
		case PlayerResouce:
			break;

		case ExploreRole:
			break;

		case SingleBuilding:
			break;
		case SingleStore:
			ret.add((long) buffConfig.getIdParam());
			break;
		case ExploreAttribute:
		case Explore:
			ret.add(playerId);
			break;

		default:
			break;
		}
		return ret;

	}

	/**
	 * 推送buff展示信息 (一般用于客户端显示buff效果)
	 * 
	 * @param buff
	 */
	public static void pushBuffShow(long playerId, List<Buff> buffs) {
		Iterator<Buff> iterator = buffs.iterator();
		while (iterator.hasNext()) {
			Buff buff = iterator.next();
			if (notSync(buff.getBuffId())) {
				iterator.remove();
			}
		}
		if (buffs.isEmpty()) {
			return;
		}
		BuffShowPush_0100010d.Builder push = BuffShowPush_0100010d.newBuilder();
		push.setBuffShow(PbBuilder.buildBuffShowInfo(buffs));
		GameClientManager.getInstance().noticeOne(push, playerId);
	}

	/**
	 * 推送buff变化
	 * 
	 * @param buff
	 * @param type
	 */
	public static void pushBuffUpdate(Buff buff, UpdateType type) {
		if (notSync(buff.getBuffId())) {
			return;
		}
		long playerId = buff.getPlayerId();
		BuffPush_0100010b.Builder push = BuffPush_0100010b.newBuilder();
		push.setType(type);
		push.setBuff(PbBuilder.buildBuffInfo(buff));
		PlayerHelper.sendProtocol(playerId, push);
	}
	
	/**
	 * buff不通知客户端
	 * @param buffId
	 * @return
	 */
	public static boolean notSync(int buffId) {
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buffId);
		boolean notSync = buffConfig.getNotSync();
		return notSync;
	}

	/**
	 * 转换buff作用目标 用于把通用角色id转换成两个探索角色id
	 * 
	 * @param playerId
	 * @param buffId
	 * @param targetIds 通用角色ids
	 * @return
	 */
	public static List<Long> convertTargetIds(long playerId, int buffId, List<Long> targetIds) {
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buffId);
		// 目标类型是探索角色,才需要转换成探索角色id
		if (buffConfig.getTargetType() != EffectTargetEnum.ExploreRole) {
			return targetIds;
		}

		List<Long> res = new ArrayList<>();
//		for (Long roleId : targetIds) {
//			if (ExploreHelper.isExploreRole(roleId.intValue())) {
//				continue;
//			}
//			// 是通用角色id
//			res.add(Long.valueOf(ExploreHelper.makeExploreRoleId(roleId.intValue(), true)));
//			res.add(Long.valueOf(ExploreHelper.makeExploreRoleId(roleId.intValue(), false)));
//		}
		return res;
	}

	/**
	 * 删除不在探索编队角色的buff
	 * 
	 * @param playerId
	 */
	public static void removeNotInExploreRoleBuff(long playerId) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		BuffOp buffOp = player.getModule(BuffOp.class);

		Set<Integer> roleUids = new HashSet<>();
		List<Long> dels = new ArrayList<>();
		Multimap<Long, Buff> buffs = buffOp.getBuffs(EffectTargetTypeEnum.Role);
		for (Long target : buffs.keySet()) {
			// 角色id不超过int
//			int id = target.intValue();
//			if (!roleUids.contains(id) && !roleIds.contains(id)) {
//				dels.add(target);
//			}
		}
		for (Long del : dels) {
			buffOp.remove(EffectTargetTypeEnum.Role, del);
		}

	}

	/**
	 * 探索单位是否有某种buff效果
	 * 
	 * @param playerId
	 * @param target
	 * @param effectEnum
	 * @return
	 */
	public static boolean exploreObjHasBuffEffect(long playerId, long target, EffectEnum effectEnum) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		BuffOp buffOp = player.getModule(BuffOp.class);
//		boolean isPlayer = ExploreMapDataManager.isPlayerObject(target);
//		if (isPlayer) {
//			return buffOp.hasEffectType(EffectTargetTypeEnum.Team, target, effectEnum);
//		}
		return buffOp.hasEffectType(EffectTargetTypeEnum.ExploreNpc, target, effectEnum);
	}
	
	/** 只有开始效果 */
	public static boolean onlyStartEffect(int buffId) {
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buffId);
		int useType = buffConfig.getUseType();
		int roundType = buffConfig.getRoundType();
		int round = buffConfig.getRound();
		// 不配置useType，不配置roundType, 只有开始效果
		if (useType == 0 && roundType == 0) {
			return true;
		}
		if (useType == 0 && roundType == 888 && round == 0) {
			return true;
		}
		return false;
	}
	
	/** 后端忽略buff */
	public static boolean ignore(int buffId) {
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buffId);
		EffectTargetEnum targetType = buffConfig.getTargetType();
		EffectEnum effectType = buffConfig.getEffectType();
		if (effectType == EffectEnum.OnlyView) {
			return false;
		}
		return targetType == EffectTargetEnum.Null || effectType == EffectEnum.Null;
	}

	/** 是否跨区域删除buff */
	public static boolean crossExploreLevelDel(int buffId) {
		EventTypeEnum roundType = BuffHelper.getRoundEventTypeEnum(buffId);
		switch (roundType) {
			case ExploreRound:
			case ExploreMapEnd:
			case ExplorePlayerRound:
				return true;
	
			default:
				return false;
		}
	}

}
