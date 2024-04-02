package cn.game.games.cache.op.face;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import cn.game.games.cache.entity.Role;
import cn.game.games.net.game.module.award.RewardItem;
import cn.game.games.net.game.module.buff.BuffValue;
import cn.game.games.net.game.module.role.RoleActionType;
import cn.game.protocol.generated.enume.AttributeSubTypeEnum;
import cn.game.protocol.generated.enume.AttributeTypeEnum;
import cn.game.protocol.generated.enume.OldConditionTypeEnum;
import cn.game.protocol.generated.enume.RoleTagEnum;

/**
 * @Description 角色（队员）数据
 * @date 2020年8月24日 下午6:27:10
 * @author SYQ
 */
public interface IRoleOp {

	public int initLoadData(List<Role> roles);

	public void update(Role role);

	public void updateWithBlob(Role role);

	public void del(int id);

	public void add(int id, Role role);

	public RewardItem newRole(int roleId);

	public Collection<Role> list();

	public int size();

	public Role get(int configId);

	public boolean exist(int configId);

	public boolean exists(int configId);

	public Role addExp(Role role, int exp);

	public Role addLevel(int id, int level);

	/**
	 * @Description 增加亲密度
	 * @param role
	 * @param intimacy
	 * @return
	 */
	public Role addIntimacy(int roleId, int intimacy);
	public boolean addAllRoleIntimacy(int intimacy);

	public Role addExp(int roleId, int exp);

	public Role starLevelup(int id);

	public int getCountByLevel(int level);

	public int getCountByStar(int star);

	public int getCountByBreakLevel(int breakLevel);

	public void updateSelective(Role role);
	
	/**
	 * get属性
	 * 
	 * @param role
	 * @param type 属性类型
	 * @param subType 属性子类型
	 * @return
	 */
	int getAttr(Role role, AttributeTypeEnum type, AttributeSubTypeEnum subType);

	/**
	 * 获取属性最大值
	 * @param role
	 * @param type
	 * @return
	 */
	int getAttrMax(Role role, AttributeTypeEnum type);
	
	/**
	 * get最大值
	 * @param role
	 * @param type 属性类型
	 * @param subType 属性子类型
	 * @return
	 */
	int getAttrMax(Role role, AttributeTypeEnum type, AttributeSubTypeEnum subType);

	/**
	 * 设置属性值
	 * @param roleId
	 * @param type
	 * @param value
	 * @param updateDb
	 * @return
	 */
	boolean setAttr(int roleId, AttributeTypeEnum type, int value, boolean updateDb);
	
	/**
	 * 设置属性值
	 * 
	 * @param dictIdOrUid role表id 或者探索角色uid
	 * @param type 属性类型
	 * @param subType 属性子类型
	 * @param value 要设置的值
	 * @param updateDb
	 * @return
	 */
	boolean setAttr(int dictIdOrUid, AttributeTypeEnum type, AttributeSubTypeEnum subType, int value, boolean updateDb);

	/**
	 * 增加属性值
	 * 
	 * @param dictIdOrUid role表id 或者探索角色uid
	 * @param type     属性类型
	 * @param incr     增加的值(正数)
	 * * @param updateDb
	 * @return
	 */
	boolean incrAttr(int dictIdOrUid, AttributeTypeEnum type, float incr, boolean updateDb);

	/**
	 * 减少属性值
	 * 
	 * @param dictIdOrUid role表id 或者探索角色uid
	 * @param type     属性类型
	 * @param decr     减少的值(正数)
	 * @param updateDb
	 * @return
	 */
	boolean decrAttr(int dictIdOrUid, AttributeTypeEnum type, float decr, boolean updateDb);
	
	
	/**
	 * 增加或减少属性值
	 * @param dictIdOrUid role表id
	 * @param type	 属性类型
	 * @param change 正数增加,负数减少
	 * @param updateDb
	 * @return
	 */
	boolean changeAttr(int dictIdOrUid, AttributeTypeEnum type, float change, boolean updateDb);
	
	/**
	 * 增加或减少所有角色属性值
	 * @param type	 属性类型
	 * @param change 正数增加,负数减少
	 * @param updateDb
	 * @return
	 */
	boolean changeAllRoleAttr(AttributeTypeEnum type, float change, boolean updateDb);
	
	
	/**
	 * 按百分比增减所有角色属性值
	 * @param type	 属性类型
	 * @param percent [-100,100]
	 * @param updateDb
	 * @return
	 */
	boolean changeAllRoleAttrByPercent(AttributeTypeEnum type, float percent, boolean updateDb);

	/**
	 * 按百分比设置属性值
	 * 
	 * @param dictIdOrUid role表id 或者探索角色uid
	 * @param type 属性类型
	 * @param percent百分数[0,100]
	 * @param updateDb
	 * @return
	 */
	boolean setRoleAttrByPercent(int dictIdOrUid, AttributeTypeEnum type, float percent, boolean updateDb);
	
	/**
	 * 按百分比设置所有角色属性值
	 * 
	 * @param type
	 * @param percent百分数 [0,100]
	 * @param updateDb
	 * @return
	 */
	boolean setAllRoleAttrByPercent(AttributeTypeEnum type, float percent, boolean updateDb);
	


	/**
	 * 通过百分比改变阵容中角色的属性
	 * @param type 属性类型
	 * @param lineupId 阵容id
	 * @param isAdd 加true,减false
	 * @param percent [0,100]
	 * @return
	 */
	boolean changeRoleLineupAttrByPercent(AttributeTypeEnum type, int lineupId, boolean isAdd, float percent);

	/**
	 * 通过具体数值改变阵容中角色的属性
	 * @param type 属性类型
	 * @param lineupId 阵容id
	 * @param isAdd 加true,减false
	 * @param value 改变的值
	 * @return
	 */
	boolean changeRoleLineupAttr(AttributeTypeEnum type, int lineupId, boolean isAdd, float value);
	
	/**
	 * 
	 * @param dictIdOrUid role表id 或者探索角色uid
	 * @param effects effect效果list
	 *                effect 属性ID|类型({@link BuffValue#CHANGE_BY_MAX_PERCENT})|数值(正数+,负数-)
	 * @param updateDb 是否更库
	 * @return
	 */
	boolean changeAttr(int dictIdOrUid, List<List<Integer>> effects, boolean updateDb);

	/**
	 * 修改角色属性
	 * @param dictIdOrUid role表id 或者探索角色uid
	 * @param type {@link AttributeTypeEnum}
	 * @param mode 通过数值还是百分比 {@link BuffValue#CHANGE_BY_MAX_PERCENT}
	 * @param change 正数+,负数-
	 * @param updateDb 是否更库
	 * @return
	 */
	public boolean changeAttr(int dictIdOrUid, AttributeTypeEnum type, int mode, float change, boolean updateDb);
	
	/**
	 * 
	 * @param dictIdOrUid role表id 或者探索角色uid
	 * @param attrId 属性枚举id
	 * @param attrSubType 属性子类型
	 * @param mode 通过数值还是百分比 {@link BuffValue#CHANGE_BY_MAX_PERCENT}
	 * @param change 正数+,负数-
	 * @param updateDb 是否更库
	 * @return
	 */
	boolean changeAttr(int dictIdOrUid, int attrId, int attrSubType, int mode, float change, boolean updateDb);
	
	/**
	 * 按百分比修改属性
	 * @param dictIdOrUid
	 * @param type
	 * @param percentChange 正数+,负数-
	 * @param updateDb
	 * @return
	 */
	boolean changeAttrByPercent(int dictIdOrUid, AttributeTypeEnum type, float percentChange, boolean updateDb);
	
	/**
	 * 更新角色的行为记录
	 * 
	 * @param roleId
	 * @param type
	 * @param subType
	 * @param actionType
	 * @param args
	 */
	void updateRoleAction(int roleId, RoleActionType actionType, OldConditionTypeEnum type, int subType, int... args);
	
	/**
	 * 获取角色属性点数
	 * @param roleId
	 * @param type
	 * @return
	 */
	public int getRoleAttrPoint(int roleId, AttributeTypeEnum type);

	/**
	 * 某角色是否有指定类型的标签
	 * 
	 * @param roleId
	 * @param type
	 * @return
	 */
	boolean hasTag(int roleId, RoleTagEnum type);
	/**
	 * 可以获得的某类型的标签
	 * 
	 * @param roleId
	 * @param type
	 * @return tagId, 如果是0表示没有可获得的标签
	 */
	int getTagAvailable(int roleId, RoleTagEnum type);
	/**
	 * 获得某标签
	 * 
	 * @param roleId
	 * @param tagId
	 */
	void addTag(int roleId, int tagId);
	
	/**
	 * 检查某角色的某行为数量是否达到指定数量
	 * 
	 * @param roleId
	 * @param type
	 * @param subType
	 * @param count
	 *            需要达到的数量
	 * @return
	 */
	boolean checkActionCount(int roleId, RoleActionType actionType, OldConditionTypeEnum type, int subType, int count, int... args);

	/**
	 * 探索结束,重置角色属性
	 */
	void exploreEndResetRole();

	/**
	 * 获取角色当前装备的天赋技能
	 * @param roleId
	 * @return
	 */
	int getTalentSkill(int roleId);



	/**
	 * 天赋技能解锁
	 * @param id
	 * @return
	 */
	public int occupationTalentUnlock(int id);

	/**
	 * 使用礼物卡
	 * @param roleUid 探索角色uid
	 * @param giftId
	 * @return
	 */
	public boolean useGiftCard(int roleUid, int giftId);

	/**
	 * 解锁礼物卡
	 * @param roleId
	 * @param friendId
	 * @param friendly
	 * @return 解锁的礼物卡id set
	 */
	public Set<Integer> unlockGiftCard(int roleId, int friendId, byte friendly);
	
	
	/**
	 * 重置角色的礼物卡为可以使用
	 */
	public void resetGiftCardUse();

	/**
	 * 获取角色可以使用的礼物卡
	 * @return
	 */
	public List<Integer> getCanUsedGiftCard(int roleDictId);
	
	/**
	 * 获取角色解锁过的礼物卡
	 * @return
	 */
	public List<Integer> getUnlockGiftCard(int roleDictId);

	/**
	 * 设置角色探索时间
	 * @param roleIds
	 */
	public void setRoleExploreTime(List<Integer> roleIds);

	/**
	 * 晋升
	 */
	public void rolePromotion();


	/**
	 * 加晋升点
	 */
	public void addPromotionPoint(int promotionPoint);

	/**
	 * 探索 队伍加晋升点
	 * @param roleIdList
	 */
	public void addPromotionPoint(List<Integer> roleIdList, int promotionPoint);

	/** 
	 * 是否有某个阵营的角色
	 * @param camp
	 * @return
	 */
	public boolean hasCampRole(int camp);


}
