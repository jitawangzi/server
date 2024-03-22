package cn.game.games.cache.op.face;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import cn.game.games.cache.entity.Equip;
import cn.game.games.cache.base.ICacheOp;
import cn.game.protocol.protobuf.BaseMsg;

public interface IEquipOp {

	// 初始数据,
	public int initLoadData(List<Equip> equips);

	public Equip add(int configId);

//	public Equip add(int configId, boolean updateDb);

	public Collection<Equip> list();

	public Equip get(long equipId);

	public void update(Equip equip);

	public void del(long id);

//	public boolean delete(List<Long> ids);

	public Equip insert(Equip equip);

//	public Equip addExp(Equip equip, int exp);

//	public int coinCost(Equip equip, int exp);

//	public int wear(long id, int roleId, EquipMsg.EquipWearResp_03000004.Builder resp);

//	public Equip getEquip(int roleId);

//	public void initAttr(Equip equip);

//	public int getCountByLevel(int level);

//	public int getCountByQuarlity(int quarlity);

	public void toRepository(long uid);

	public void toBag(long uid);

	boolean isExist(long uid);

//	public int replace(int roleId,long uid);

	public void updateSelective(Equip equip);

	public List<BaseMsg.EquipInfo> bulidEquipList(int roleId);

	public boolean teardown(int roleId, int slot);

	public int equip(long uid, int roleId, int slot);

	/**
	 * 根据结果类型获取角色各个属性对应的值
	 * @param roleId
	 * @param valueType
	 * @return
	 */
	public Map<Integer,Integer> getEquipAttrValByType(int roleId, int valueType);

	/**
	 * 生成一个装备
	 * @param configId
	 * @return
	 */
	public Equip gen(int configId);

	/**
	 * 获得/添加装备
	 * @param equip
	 * @param storage 所在位置(背包/仓库/身上)
	 * @return
	 */
	public boolean add(Equip equip, byte storage);

	//public int checkStrengthenChangeList(Equip equip, List<Map.Entry<Integer, Integer>> levelupCost, List<Long> list);

	public Map<Long, Equip> getEquips();

	public void deleteByIds(List<Long> list);

	public void delExploreEquip();

	/**
	 * 探索后重置初始装备
	 * @param role_init_equips
	 */
	void resetRoleEquips(Map<Integer, Map<Byte, Long>> role_init_equips);

	/**
	 * 升级
	 * @param roleId
	 * @param slot
	 * @return
	 */
	public int equipStrength(int roleId, int slot);

	public void updateEquip(long uid, int roleId, byte pos);

	public Map<Byte, Equip> getRoleEquipByRoleId(int roleId);

	/**
	 * 添加固有装备
	 * @param equipDictId
	 * @param roleId
	 * @param slot
	 */
	public void addRoleFixedEquip(int equipDictId, int roleId, int slot);

}
