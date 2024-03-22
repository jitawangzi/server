package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.util.IdUtil;
import cn.game.games.cache.op.impl.BuffOp;
import cn.game.games.core.event.EventHandler;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.BuffHelper;
import cn.game.games.net.game.helper.EventHelper;
import cn.game.games.net.game.helper.RoleHelper;
import cn.game.games.net.game.manager.GameConstants;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.buff.BuffEndEventHandler;
import cn.game.games.net.game.module.buff.BuffRoundEventHandler;
import cn.game.games.net.game.module.buff.BuffUseEventHandler;
import cn.game.protocol.generated.config.OldBuffConfig;
import cn.game.protocol.generated.enume.AttributeSubTypeEnum;
import cn.game.protocol.generated.enume.AttributeTypeEnum;
import cn.game.protocol.generated.enume.BuffMomentEnum;
import cn.game.protocol.generated.enume.EffectEnum;
import cn.game.protocol.generated.enume.EffectTargetEnum;
import cn.game.protocol.generated.enume.EffectTargetTypeEnum;
import cn.game.protocol.generated.manager.OldBuffManager;
import cn.game.protocol.protobuf.BaseMsg.UpdateType;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.games.cache.base.DbEntity;

public class Buff implements Serializable, DbEntity {


	/**
	 * @mbg.generated
	 */
	private Long id;

	/**
	 * BuffConfig表id
	 * @mbg.generated
	 */
	private Integer buffId;

	/**
	 * @mbg.generated
	 */
	private Long playerId;

	/**
	 * 剩余生效次数
	 * @mbg.generated
	 */
	private Integer useNum;

	/**
	 * 剩余层数
	 * @mbg.generated
	 */
	private Integer level;

	/**
	 * 剩余回合数
	 * @mbg.generated
	 */
	private Integer round;

	/**
	 * buff作用目标id集合
	 * @mbg.generated
	 */
	private Long target;

	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @mbg.generated
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @mbg.generated
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getBuffId() {
		return buffId;
	}

	/**
	 * @mbg.generated
	 */
	public void setBuffId(Integer buffId) {
		this.buffId = buffId;
	}

	/**
	 * @mbg.generated
	 */
	public Long getPlayerId() {
		return playerId;
	}

	/**
	 * @mbg.generated
	 */
	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getUseNum() {
		return useNum;
	}

	/**
	 * @mbg.generated
	 */
	public void setUseNum(Integer useNum) {
		this.useNum = useNum;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getLevel() {
		return level;
	}

	/**
	 * @mbg.generated
	 */
	public void setLevel(Integer level) {
		this.level = level;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getRound() {
		return round;
	}

	/**
	 * @mbg.generated
	 */
	public void setRound(Integer round) {
		this.round = round;
	}

	/**
	 * @mbg.generated
	 */
	public Long getTarget() {
		return target;
	}

	/**
	 * @mbg.generated
	 */
	public void setTarget(Long target) {
		this.target = target;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.BuffMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}

	/** 通过这个buff获取的奖励，临时保存 */
	private transient List<RewardInfo> rewards;
	
	public static transient final Logger buffLog = LoggerFactory.getLogger("buffLog");
	
	public List<RewardInfo> getRewards() {
		return rewards;
	}

	public void setRewards(List<RewardInfo> rewards) {
		this.rewards = rewards;
	}
	/**level 属性弃用了*/
	public Buff() {
	}

	private EventHandler useEventHandler = new BuffUseEventHandler(this);
	private EventHandler roundEventHandler = new BuffRoundEventHandler(this);
	private EventHandler endEventHandler = new BuffEndEventHandler(this);

	public void init(Player player) {
		player.registerEventHandler(BuffHelper.getUseEventTypeEnum(buffId), useEventHandler);
		player.registerEventHandler(BuffHelper.getRoundEventTypeEnum(buffId), roundEventHandler);
		player.registerEventHandler(BuffHelper.getEndEventTypeEnum(buffId), endEventHandler);
		// 重置角色属性
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buffId);
		EffectTargetTypeEnum type = EffectTargetTypeEnum.get(buffConfig.getTargetType().getType());
		if (type == EffectTargetTypeEnum.Role || type == EffectTargetTypeEnum.ExploreRole) {
			int idParam = buffConfig.getIdParam();
			int numParam = buffConfig.getNumParam();
			int mode = buffConfig.getNumTypeParam();
			EffectEnum effectType = buffConfig.getEffectType();
			if (effectType != EffectEnum.ChangeRoleAttribute) {
				return;
			}
			// idParam == 0代表多属性改变
			if (idParam == 0) {
				int[] extParam = buffConfig.getExtParam();
				for (int attrId : extParam) {
					AttributeTypeEnum attrType = RoleHelper.getAttributeType(attrId);
					AttributeSubTypeEnum subType = RoleHelper.getSubType(attrId);
					buffLog.info("player[{}],buff[{}],修改角色[{}][{}][{}],模式[{}],值[{}],即将重置角色属性", playerId, buffId, target,
							attrType.getDesc(), subType.getDesc(), GameConstants.modeString(mode), numParam);
				}
			} else {
				AttributeTypeEnum attrType = RoleHelper.getAttributeType(idParam);
				AttributeSubTypeEnum subType = RoleHelper.getSubType(idParam);
				buffLog.info("player[{}],buff[{}],修改角色[{}][{}][{}],模式[{}],值[{}],即将重置角色属性", playerId, buffId, target,
						attrType.getDesc(), subType.getDesc(), GameConstants.modeString(mode), numParam);
			}

			EventHelper.handleEvent(playerId, new GameEvent(EventTypeEnum.BuffChange, Arrays.asList(this.target)));
		}
	}
	public void end() {
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buffId);
		// buff在结束时生效,此类buff(配置useType，配置roundPara)
		int useType = buffConfig.getUseType();
		if (useType == BuffMomentEnum.End.getId()) {
			buffLog.info("player[{}],buff[{}],即将即将执行结束效果", playerId, buffId);
			BuffHelper.addBuffEffect(playerId, buffId, Arrays.asList(target));
		}
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		BuffOp buffOp = player.getModule(BuffOp.class);
		buffOp.remove(this.buffId, this.target);

	}
	
	/** 注销事件处理器 */
	public void unregisterEventHandler() {
		EventHelper.unregisterEventHandler(playerId, BuffHelper.getUseEventTypeEnum(buffId), useEventHandler);
		EventHelper.unregisterEventHandler(playerId, BuffHelper.getRoundEventTypeEnum(buffId), roundEventHandler);
		EventHelper.unregisterEventHandler(playerId, BuffHelper.getEndEventTypeEnum(buffId), endEventHandler);
	}

	/** 执行持续效果 
	 * @param param */
	public void continuousEffect(int... param) {
		BuffHelper.addBuffEffect(playerId, buffId, Arrays.asList(target), param);
		if (useNum == 999) {
			return;
		}
		if (pause()) {
			return;
		}
		useNum--;
		if (useNum <= 0) {
			end();
			return;
		}
		BuffHelper.pushBuffUpdate(this, UpdateType.UPDATE);
	}

	public void consumeRound() {
		if (round == 999) {
			return;
		}
		if (pause()) {
			return;
		}
		round--;
		if (round <= 0) {
			end();
			return;
		}
		BuffHelper.pushBuffUpdate(this, UpdateType.UPDATE);
	}
	
	// 是否是暂停状态
	private boolean pause() {
		int roleConfigId = 0;
		
//		LineupOp lineupOp = player.getModule(LineupOp.class);
		OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(this.buffId);
		EffectTargetEnum targetType = buffConfig.getTargetType();
		if (targetType == EffectTargetEnum.ExploreRole) {
			int roleUid = this.getTarget().intValue();

		} else if (targetType == EffectTargetEnum.Role) {
			roleConfigId = this.getTarget().intValue();
			
		} else {
			return false;
		}
		
//		List<Integer> lineupRoles = lineupOp.getLineupRoleIds(GlobalConst.exploreTeamId);
//		if (!lineupRoles.contains(roleConfigId)) {
//			return true;
//		}
		return false;
	}
	
	public static Buff valueOf(int buffId, long playerId, long target, List<RewardInfo> buffRewards) {
		Buff buff = new Buff();
		buff.setBuffId(buffId);
		buff.setPlayerId(playerId);
		buff.setTarget(target);
		buff.setRewards(buffRewards);
		buff.setId(IdUtil.getId());
		return buff;
	}

}