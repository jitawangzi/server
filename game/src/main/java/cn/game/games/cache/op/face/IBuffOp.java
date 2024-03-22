package cn.game.games.cache.op.face;

import java.util.List;

import com.google.common.collect.Multimap;

import cn.game.games.cache.entity.Buff;
import cn.game.games.cache.base.ICacheOp;
import cn.game.games.net.game.module.buff.BuffValue;
import cn.game.protocol.generated.enume.EffectEnum;
import cn.game.protocol.generated.enume.EffectTargetTypeEnum;

public interface IBuffOp {

	void initLoadData(List<Buff> buffList);

	void save();

	/** 
	 * 增加一个buff
	 * @param id BuffConfig id
	 * @param targetIds 事先指定的目标id，如果没有指定目标id ，则使用buff中产生的
	 * @return 如果buff只有开始效果，返回id和作用目标；如果持续效果，返回完整buff数据
	 */
	List<Buff> add(int id, List<? extends Number> targetIds);

	/** 
	 *  一次增加多个buff
	 * @param ids
	 * @param targetIds
	 * @return List<Buff>
	 */
	List<Buff> add(List<Integer> ids, List<? extends Number> targetIds);

	List<Buff> add(int[] ids, List<? extends Number> targetIds);

	/**
	 * 删除buff
	 * @param buffId
	 */
	public void remove(List<Integer> buffId);
	
	public void remove(int... ids);

	/**
	 * 删除一个目标的多个buff
	 * @param buffId
	 */
	public void remove(long targetId, List<Integer> buffId);

	/**
	 * 删除buff
	 * @param buffId
	 */
	public void remove(Integer buffId);

	/**
	 * 删除某目标类型的所有buff
	 * @param targetType
	 */
	public void remove(EffectTargetTypeEnum targetType);
	
	/** 
	 * 删除一个buf
	 * @param buffId
	 * @param target
	 */
	void remove(int buffId, long target);

	/** 
	 * 删除某目标的所有buff
	 * @param targetType
	 * @param target
	 */
	void remove(EffectTargetTypeEnum targetType, long target);
	
	/**
	 * 删除某目标的某类buff
	 * @param effectTargetTypeEnum 大目标类型
	 * @param buffType buff表的type字段
	 * @param target
	 */
	void remove(EffectTargetTypeEnum effectTargetTypeEnum, int buffType, long target);

	/** 
	 * 获取buff的加成值
	 * @param targetType 
	 * @param targetId 根据不同的targetType ，可能是角色id，建筑id等等。 
	 * @param effectType 哪种效果的加成
	 * @return 
	 */
	BuffValue getBuffValue(EffectTargetTypeEnum targetType, long targetId, EffectEnum effectType);

	/** 
	 * 获取某种目标类型的buff集合
	 * @return
	 */
	Multimap<Long, Buff> getBuffs(EffectTargetTypeEnum targetType);

	/** 
	 * 判断某目标，是否有某种类型的buff（带持续回合的）
	 * @param targetType 大目标类型
	 * @param targetId
	 * @param effectType  具体要判断的效果类型
	 * @return
	 */
	boolean hasEffectType(EffectTargetTypeEnum targetType, long targetId, EffectEnum effectType);

	/** 
	 * 强制销毁buff，不等正常结束。 
	 * @param type
	 */
	void interruptAndDestory(int type);


	/**
	 * 添加事件
	 * @param eventId
	 */
	public void addEvent(int eventId);
	
	public Buff get(int buffId, long target);

	/**
	 * 获取已有buff数量
	 * @param targetType
	 * @param targetId
	 * @param buffId
	 * @return
	 */
	int getBuffCount(EffectTargetTypeEnum targetType, long targetId, int buffId);

	/**
	 * 是否有某id的buff
	 * @param targetId
	 * @param buffId
	 * @return
	 */
	boolean hasBuff(long targetId, int buffId);
	
	/**
	 * 是否有某类型的buff
	 * @param targetType buff效果大目标类型
	 * @param targetId	 目标id
	 * @param buffTypeId BuffTypeEnum表的id
	 * @return
	 */
	boolean hasBuffType(EffectTargetTypeEnum targetType, long targetId, int buffTypeId);

	/**
	 * 移除不能跨区域的buff
	 */
	void removeCrossExploreLevelBuff();
	
}
