package cn.game.games.cache.op.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.ProtocolStringList;

import cn.game.core.util.IdUtil;
import cn.game.games.cache.entity.Equip;
import cn.game.games.cache.entity.PlayerExt;
import cn.game.games.cache.op.face.IEquipOp;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.EquipMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.helper.EventHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.GameConstants;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.item.ItemModule;
import cn.game.games.util.DAO;
import cn.game.games.util.PbBuilder;
import cn.game.protocol.generated.config.EquipmentAttributteTemplateConfig;
import cn.game.protocol.generated.config.EquipmentBuffConfig;
import cn.game.protocol.generated.config.EquipmentConfig;
import cn.game.protocol.generated.config.EquipmentEnumConfig;
import cn.game.protocol.generated.config.EquipmentStrengthenCostConfig;
import cn.game.protocol.generated.config.OldGlobalConst;
import cn.game.protocol.generated.config.OldBuffConfig;
import cn.game.protocol.generated.config.RoleConfig;
import cn.game.protocol.generated.manager.EquipmentAttributteTemplateManager;
import cn.game.protocol.generated.manager.EquipmentBuffManager;
import cn.game.protocol.generated.manager.EquipmentEnumManager;
import cn.game.protocol.generated.manager.EquipmentManager;
import cn.game.protocol.generated.manager.EquipmentStrengthenCostManager;
import cn.game.protocol.generated.manager.OldBuffManager;
import cn.game.protocol.generated.manager.RoleManager;
import cn.game.protocol.manual.OldErrorMsgEnum;
import cn.game.protocol.manual.ResourceConsumeEnum;
import cn.game.protocol.protobuf.BaseMsg;
import cn.game.protocol.protobuf.BuildingMsg;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.Rnd;

public class EquipOp extends BasePlayerModule implements IEquipOp {

	private Map<Long, Equip> equips;

	// roleid => <pos, Equip>
	Map<Integer, Map<Byte, Equip>> role_equips;

	@Override
	public void init() {
		equips = new HashMap<>();
		role_equips = new HashMap<>();
	}

	@Override
	public int initLoadData(List<Equip> equips) {
		init();
		equips.forEach(equip -> {
			this.equips.put(equip.getId(), equip);

			if (equip.getRoleId() != 0) {
				Map<Byte, Equip> byteEquipMap = role_equips.computeIfAbsent(equip.getRoleId(), k -> new HashMap<>());
				byteEquipMap.put(equip.getPos(), equip);
			}
		});

		return equips.size();
	}

	@Override
	public Equip gen(int configId) {
		EquipmentConfig equipmentConfig = EquipmentManager.getInstance().getEquipmentConfigNullable(configId);
		if (equipmentConfig == null) {
			return null;
		}
		if (equipmentConfig.getType() == GameConstants.FIXED_EQUIP) {
			return null;
		}
		Equip equip = new Equip();
		equip.setPlayerId(playerId);
//		equip.setDictId(configId);
		equip.setRoleId(0);
		equip.setPos((byte) 0);
//		equip.setStrength((byte) 0);
//		equip.setExp(0);
		equipmentConfig.getBuff().forEach(e-> equip.addBuff(e));

		EquipmentEnumConfig buffConfig = EquipmentEnumManager.getInstance()
				.getEquipmentEnumConfigNullable(equipmentConfig.getQuality());
		// 随机装备效果
		if (buffConfig != null) {
			List<EquipmentBuffConfig> configs = new ArrayList<>();
			List<EquipmentBuffConfig> groupList = EquipmentBuffManager.getInstance()
					.getGroupList(equipmentConfig.getGroup());

			if (groupList != null && groupList.size() > 0) {
				for (EquipmentBuffConfig config : groupList) {
					if (equipmentConfig.getQuality() >= config.getQualityLimit()) {
						configs.add(config);
					}
				}
			}

			// 根据权重随机
			int buffNum = buffConfig.getBuffNum();

			//a.	第一个词缀出现的概率为80%；
			//b.	如果第一个出现，第二个词缀的概率为70%；
			//c.	如果第二个出现，第三个词缀的概率为50%；
			if (buffNum > 0) {
				List<Integer> equipmentAffixProbability = OldGlobalConst.equipmentAffixProbability;
				int i = 0;
				int rnd = buffNum;
				while (rnd > 0) {
					int random = Rnd.get(0, 100);
					if (random > equipmentAffixProbability.get(i)) {
						break;
					}
					rnd--;
					i++;
				}
				buffNum = (i == 0) ? 0 : buffNum - rnd;
			}
			boolean hasLegend = false;
			int total = 0;
			for (int i = 0; i < configs.size(); i++) {
				total += configs.get(i).weight();
			}

			while (configs.size() > 0 && buffNum > 0) {
				int rand = Rnd.nextInt(total);
				int current = 0;
				for (int i = 0; i < configs.size(); i++) {
					if (configs.get(i).getIsLegend() && hasLegend) {
						continue;
					}
					current += configs.get(i).weight();
					if (rand < current) {

						equip.addBuff(configs.get(i).getBuff());
						// 只能随机出一条传说
						if (configs.get(i).getIsLegend()) {
							if (!hasLegend) {
								hasLegend = true;
								// 去除所有传说的权重
								for (EquipmentBuffConfig config : configs) {
									if (config.getIsLegend()) {
										total -= config.weight();
									}
								}
							}
						}
						break;
					}
				}
				buffNum--;
			}
		}
		long id = IdUtil.getId();
		equip.setId(id);
		return equip;
	}

	@Override
	public Equip add(int configId) {
		Equip equip = gen(configId);
		if (equip == null) {
			return null;
		}
		equip.setStorage(Equip.IN_REPOSITORY);
		this.equips.put(equip.getId(), equip);
		insert(equip);
		// 添加装备图鉴
		PlayerExt playerExt = PlayerManager.getInstance().getPlayer(playerId).getExt();
		playerExt.addIllustrate(BuildingMsg.IllustrateType.EQUIPMENT_VALUE, configId);
		return equip;
	}

	@Override
	public boolean add(Equip equip, byte storage) {
		this.equips.put(equip.getId(), equip);
		equip.setStorage(storage);
		insert(equip);
		// 添加装备图鉴
		PlayerExt playerExt = PlayerManager.getInstance().getPlayer(playerId).getExt();
		playerExt.addIllustrate(BuildingMsg.IllustrateType.EQUIPMENT_VALUE, equip.getDictId());
		return true;
	}

	@Override
	public void del(long id) {
		Equip remove = equips.remove(id);
		DAO.delete(EquipMapper.class, remove.getId());
	}

	@Override
	public Collection<Equip> list() {
		return equips.values();
	}

	@Override
	public Equip insert(Equip equip) {
		DAO.insert(EquipMapper.class, equip);
		return null;
	}

	@Override
	public void update(Equip equip) {
		equip.updateWithBlobs();
	}

	@Override
	public void updateSelective(Equip equip) {
		DAO.updateSelective(EquipMapper.class, equip);
	}

	@Override
	public void toRepository(long uid) {
		if (!isExist(uid)) {
			return;
		}
		Equip equip = this.equips.get(uid);
		equip.setStorage(Equip.IN_REPOSITORY);
		update(equip);
	}

	@Override
	public void toBag(long uid) {
		Equip equip = this.equips.get(uid);
		equip.setStorage(Equip.IN_BAG);
		update(equip);
	}

	@Override
	public boolean isExist(long uid) {
		return this.equips.containsKey(uid);
	}

	public boolean checkExist(ProtocolStringList equipUidsList, byte storage) {
		for (String uidStr : equipUidsList) {
			long uid = StringUtils.isEmpty(uidStr) ? 0 : Long.parseLong(uidStr);
			Equip e = this.equips.get(uid);
			if (e == null || e.getStorage() != storage) {
				return false;
			}
		}
		return true;
	}

	public OldErrorMsgEnum toBag(ProtocolStringList equipUids) {
		ItemModule itemModule = player.getModule(ItemModule.class);
//		int idle = itemModule.getBagIdleSize();
//		if (equipUids.size() > idle) {
//			return ErrorMsgEnum.item_bag_capacity_not_enough;
//		}

		for (String uidStr : equipUids) {
			long uid = StringUtils.isEmpty(uidStr) ? 0 : Long.parseLong(uidStr);
			Equip equip = get(uid);
			if (equip == null) {
				return OldErrorMsgEnum.illegal_request;
			}
		}
		for (String uidStr : equipUids) {
			long uid = StringUtils.isEmpty(uidStr) ? 0 : Long.parseLong(uidStr);
			toBag(uid);
//			itemModule.addBagEquip(uid);
		}
		return OldErrorMsgEnum.ok;
	}

	@Override
	public int equip(long uid, int roleId, int slot) {
		ItemModule itemModule = player.getModule(ItemModule.class);
		Equip newEquip = this.equips.get(uid);
		if (newEquip == null) {
			return OldErrorMsgEnum.player_check_error.getId();
		}
		if (newEquip.getRoleId() != 0) {
			// 不能选择其他角色已经装备的
			return OldErrorMsgEnum.player_check_error.getId();
		}
		EquipmentConfig equipmentConfig = EquipmentManager.getInstance().getEquipmentConfig(newEquip.getDictId());

		RoleConfig roleConfig = RoleManager.getInstance().getRoleConfig(roleId);
		List<Integer> equipmentSlot = roleConfig.getEquipmentSlot();
		if (equipmentSlot == null || equipmentSlot.size() == 0) {
			return OldErrorMsgEnum.config_data_not_found.getId();
		}
		if (slot <= 0 || slot > equipmentSlot.size()) {
			return OldErrorMsgEnum.illegal_request.getId();
		}
		RoleOp roleOp = player.getModule(RoleOp.class);
		if (roleOp.get(roleId) == null) {
			return OldErrorMsgEnum.player_check_error.getId();
		}
		Map<Byte, Equip> equipMap = this.role_equips.get(roleId);
		if (equipMap == null) {
			equipMap = new HashMap<>();
		}
		if (equipmentConfig.getType() != equipmentSlot.get(slot - 1)) {
			// 类型不对
			return OldErrorMsgEnum.illegal_request.getId();
		}
		// 身上的装备
		Equip bodyEquip = equipMap.get((byte) slot);
		BuffOp buffOp = player.getModule(BuffOp.class);
		if (bodyEquip == null) {// 装备
			if (equipmentConfig.getRoleId().size() != 0 && !equipmentConfig.getRoleId().contains(roleId)) {
				return OldErrorMsgEnum.illegal_request.getId();
			}
			if (equipmentConfig.getOccupation().size() != 0
					&& !equipmentConfig.getOccupation().contains(roleConfig.getOccupation())) {
				return OldErrorMsgEnum.illegal_request.getId();
			}
			if (equipmentConfig.getCamp().size() != 0 && !equipmentConfig.getCamp().contains(roleConfig.getCamp())) {
				return OldErrorMsgEnum.illegal_request.getId();
			}
			// 不可同时装备两个同ID的饰品
//			for (Equip equip1 : equipMap.values()) {
//				if (equip1.getDictId().equals(newEquip.getDictId())) {
//					return ErrorMsgEnum.illegal_request.getId();
//				}
//			}
			// 在探索中,穿上背包里的装备

			newEquip.setStorage(Equip.IN_ROLE);
			newEquip.setRoleId(roleId);
			newEquip.setPos((byte) slot);
			update(newEquip);

			equipMap.put((byte) slot, newEquip);
			this.role_equips.put(roleId, equipMap);

		} else {// 替换
			if (bodyEquip.getId().equals(uid)) {
				return OldErrorMsgEnum.illegal_request.getId();
			}
			EquipmentConfig config = EquipmentManager.getInstance().getEquipmentConfig(bodyEquip.getDictId());
			if (equipmentConfig.getType() != config.getType()) {
				return OldErrorMsgEnum.illegal_request.getId();
			}

			// 背包里装备增减
			bodyEquip.setStorage(Equip.IN_REPOSITORY);
			
			bodyEquip.setRoleId(0);
			bodyEquip.setPos((byte) 0);
			
			newEquip.setStorage(Equip.IN_ROLE);
			newEquip.setRoleId(roleId);
			newEquip.setPos((byte) slot);
			
			this.equips.put(bodyEquip.getId(), bodyEquip);
			update(bodyEquip);
			update(newEquip);

			this.role_equips.get(roleId).put(newEquip.getPos(), newEquip);

			//移除之前的buff
			buffOp.remove(roleId, bodyEquip.getBuffList());
		}
		//添加buff
		newEquip.getBuffList().forEach(e -> buffOp.add(e, Arrays.asList(roleId)));
		return OldErrorMsgEnum.ok.getId();
	}

	@Override
	public void updateEquip(long uid, int roleId, byte pos) {
		Equip update = new Equip();
		update.setId(uid);
		update.setRoleId(roleId);
		update.setPos(pos);
		update.setPlayerId(playerId);
		updateSelective(update);
	}

	@Override
	public boolean teardown(int roleId, int slot) {
		Map<Byte, Equip> byteEquipMap = this.role_equips.get(roleId);
		if (byteEquipMap == null) {
			return false;
		}
		Equip equip = byteEquipMap.get((byte) slot);
		if (equip == null) {
			return false;
		}
		// 装备不在角色身上
		if (equip.getStorage() != Equip.IN_ROLE) {
			return false;
		}
		
		ItemModule itemModule = player.getModule(ItemModule.class);
		equip.setRoleId(0);
		equip.setPos((byte) 0);
		byteEquipMap.remove((byte) slot);
		equip.setStorage(Equip.IN_REPOSITORY);

		update(equip);
		//移除buff
		BuffOp buffOp = player.getModule(BuffOp.class);
		buffOp.remove(equip.getBuffList());

		return true;
	}

	@Override
	public int equipStrength(int roleId, int slot) {
		Map<Byte, Equip> byteEquipMap = this.role_equips.get(roleId);
		if (byteEquipMap == null || byteEquipMap.size() == 0) {
			return OldErrorMsgEnum.player_check_error.getId();
		}
		Equip equip = byteEquipMap.get((byte) slot);
		if (equip == null) {
			return OldErrorMsgEnum.player_check_error.getId();
		}
		int level = equip.getStrength();
		/*
		int maxLevel = 0;
		for (BuildingPos buildingPos : buildingOp.getBuildingPosList()) {
			int id = buildingPos.getId();
			BuildingConfig buildingConfig = BuildingManager.getInstance().getBuildingConfig(id);
			if (buildingConfig.getFeatureType() == BuildingFeatureEnum.BlackSmith.getId()) {
				// 装备最大等级受建筑等级限制
				maxLevel = buildingPos.getLevel();
			}
		}*/
		//装备最大等级受科技树影响
		EquipmentStrengthenCostConfig config = EquipmentStrengthenCostManager.getInstance()
				.getEquipmentStrengthenCostConfig(level + 1);
		if (!PlayerHelper.delResources(playerId, config.getStandardExp(), ResourceConsumeEnum.EquipmentStrengthen)) {
			return OldErrorMsgEnum.resource_not_enough.getId();
		}
		// 4、7、10级时分别解锁一个词缀
		EquipmentConfig equipmentConfig = EquipmentManager.getInstance().getEquipmentConfig(equip.getDictId());
		List<List<Integer>> buffs = equipmentConfig.getBuffs();
		List<Integer> equipUnlockBuffPromotionLevel = OldGlobalConst.EquipUnlockBuffPromotionLevel;
		BuffOp buffOp = player.getModule(BuffOp.class);
		if (level + 1 == equipUnlockBuffPromotionLevel.get(0)) {
			buffs.get(0).forEach(e-> {
				equip.addBuff(e);
				buffOp.add(e, Arrays.asList(roleId));
			});
		} else if (level + 1 == equipUnlockBuffPromotionLevel.get(1)) {
			buffs.get(1).forEach(e-> {
				equip.addBuff(e);
				buffOp.add(e, Arrays.asList(roleId));
			});
		} else if (level + 1 == equipUnlockBuffPromotionLevel.get(2)) {
			buffs.get(2).forEach(e-> {
				equip.addBuff(e);
				buffOp.add(e, Arrays.asList(roleId));
			});
		}
		Equip update = new Equip();
		update.setPlayerId(playerId);
		update.setId(equip.getId());
		// 修改库
		this.updateSelective(update);
		//触发事件
		EventHelper.handleEvent(playerId, new GameEvent(EventTypeEnum.FixedEquipmentLevel, update.getStrength()));
		return OldErrorMsgEnum.ok.getId();
	}

	@Override
	public List<BaseMsg.EquipInfo> bulidEquipList(int roleId) {
		Map<Byte, Equip> integerEquipMap = this.role_equips.get(roleId);
		List<BaseMsg.EquipInfo> equipInfos = new ArrayList<>();
		for (Equip equip : integerEquipMap.values()) {
			equipInfos.add(PbBuilder.buildEquipInfo(equip));
		}
		return equipInfos;
	}

	@Override
	public Map<Byte, Equip> getRoleEquipByRoleId(int roleId) {
		return this.role_equips.get(roleId);
	}

	@Override
	public Map<Integer, Integer> getEquipAttrValByType(int roleId, int valueType) {
		Map<Byte, Equip> roleEquipByRoleId = getRoleEquipByRoleId(roleId);
		if (roleEquipByRoleId == null) {
			return new HashMap<>();
		}
		Map<Integer, Integer> attrMap = new HashMap<>();
		roleEquipByRoleId.values().forEach(equip -> {
			EquipmentConfig equipmentConfig = EquipmentManager.getInstance().getEquipmentConfig(equip.getConfigId());
			if (valueType == GameConstants.EQUIP_FIXEDVAL) {// 固定属性
				List<Map.Entry<Integer, Integer>> attribute = equipmentConfig.getAttribute();
				attribute.forEach(e -> {
					attrMap.merge(e.getKey(), e.getValue(), Integer::sum);
				});
				List<EquipmentAttributteTemplateConfig> typelevelList = EquipmentAttributteTemplateManager.getInstance()
						.getTypestrengthList(equipmentConfig.getAttributeTemplateType(), equip.getDictId());
				if (typelevelList != null && typelevelList.size() > 0) {
					typelevelList.get(0).getAttribute().forEach(e -> {
						attrMap.merge(e.getKey(), e.getValue(), Integer::sum);
					});
				}
			}
			//List<Integer> buff = equipmentConfig.getBuff();
			// 添加buff属性 ----buff中有固定属性和百分比属性
			//addBuffAttr(buff, valueType, attrMap);
			//addBuffAttr(equip.getBuffList(), valueType, attrMap);
		});
		return attrMap;
	}

	private void addBuffAttr(List<Integer> buff, int valueType, Map<Integer, Integer> attrMap) {
		for (Integer e : buff) {
			OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfigNullable(e);
			if (buffConfig == null) {
				continue;
			}
			int numTypeParam = buffConfig.getNumTypeParam();
			if(numTypeParam == valueType) {
				int id = buffConfig.getIdParam() / 100;
				if (attrMap.get(buffConfig.getIdParam()) == null) {
					attrMap.put(id, buffConfig.getNumParam());
				} else {
					attrMap.put(id, attrMap.get(id) + buffConfig.getNumParam());
				}
			}
		}
	}

	@Override
	public Equip get(long equipId) {
		Equip equip = this.equips.get(equipId);
		return equip;
	}

	@Override
	public Map<Long, Equip> getEquips() {
		return equips;
	}

	@Override
	public void deleteByIds(List<Long> list) {
		if (list == null || list.size() == 0) {
			return;
		}
		for (long id : list) {
			this.equips.remove(id);
		}
		DAO.execute(EquipMapper.class, MapperConstant.deleteByIds, list);
	}

	@Override
	public void delExploreEquip() {
		List<Long> list = new ArrayList<>();
		this.equips.values().forEach(e -> {
			EquipmentConfig config = EquipmentManager.getInstance().getEquipmentConfig(e.getDictId());
			/*
			 * if (config.getKind() == GameConstants.EXPLORE_EQUIP) { list.add(e.getId()); }
			 */
		});
		deleteByIds(list);
	}

	@Override
	public void resetRoleEquips(Map<Integer, Map<Byte, Long>> role_init_equips) {
		// 删除探索装备
		delExploreEquip();
		for (Map.Entry<Integer, Map<Byte, Long>> map : role_init_equips.entrySet()) {
			Map<Byte, Equip> equips = getRoleEquipByRoleId(map.getKey());
			Map<Byte, Long> value = map.getValue();
			for (Map.Entry<Byte, Long> posUid : value.entrySet()) {
				if (equips != null && !equips.containsKey(posUid.getKey())) {// 该位置有装备
					Equip equip = get(posUid.getValue());
					equip.setPos(posUid.getKey());
					equip.setRoleId(map.getKey());
					// 更库
					updateEquip(equip.getId(), equip.getRoleId(), equip.getPos());
					equips.put(posUid.getKey(), equip);
					this.role_equips.put(equip.getRoleId(), equips);
				}
			}
		}
	}

	@Override
	public void addRoleFixedEquip(int equipDictId, int roleId, int slot) {
		EquipmentConfig equipmentConfig = EquipmentManager.getInstance().getEquipmentConfigNullable(equipDictId);
		if (equipmentConfig == null) {
			return;
		}
		if (equipmentConfig.getType() != GameConstants.FIXED_EQUIP) {
			return;
		}
		Equip insert = new Equip();
		insert.setPlayerId(playerId);
//		insert.setDictId(equipDictId);
//		insert.setStrength((byte) 0);
//		insert.setExp(0);
		long id = IdUtil.getId();
		insert.setId(id);
		RoleConfig roleConfig = RoleManager.getInstance().getRoleConfig(roleId);
		List<Integer> equipmentSlot = roleConfig.getEquipmentSlot();
		if (equipmentSlot == null || equipmentSlot.size() == 0) {
			return;
		}
		if (slot <= 0 || slot > equipmentSlot.size()) {
			return;
		}
		Map<Byte, Equip> equipMap = this.role_equips.get(roleId);
		if (equipMap == null) {
			equipMap = new HashMap<>();
		}
		if (equipmentConfig.getType() != equipmentSlot.get(slot - 1)) {
			// 类型不对
			return;
		}
		Equip equip = equipMap.get((byte) slot);
		if (equip != null) {
			return;
		}
		insert.setRoleId(roleId);
		insert.setPos((byte) slot);
		insert.setStorage(Equip.IN_ROLE);
		equipmentConfig.getBuff().forEach(e-> insert.addBuff(e));
		insert(insert);

		equipMap.put((byte) slot, insert);
		this.equips.put(insert.getId(), insert);
		this.role_equips.put(roleId, equipMap);
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

	/*
	 * @Override public int checkStrengthenChangeList(Equip equip,
	 * List<Map.Entry<Integer, Integer>> levelupCost, List<Long> list) { //转化的资源数量
	 * int num = 0; int sumChange = 0; for (long id : list) { if
	 * (!this.equips.containsKey(id)) { return sumChange; } Equip equip1 =
	 * this.equips.get(id); if (equip1.getRoleId() != 0) {//角色装备的不可转化 return
	 * sumChange; } EquipmentConfig config =
	 * EquipmentManager.getInstance().getEquipmentConfig(equip1.getDictId());
	 * ExploreOp exploreOp = player.getModule(ExploreOp.class);
	 * if (exploreOp.isInExplore() && exploreOp.isInExploreCamp()) { if
	 * (config.getKind() != GameConstants.EXPLORE_EQUIP) return 0; } else { if
	 * (config.getKind() == GameConstants.EXPLORE_EQUIP) return 0; } EquipmentConfig
	 * equipmentConfig =
	 * EquipmentManager.getInstance().getEquipmentConfig(equip.getDictId());
	 * if(equipmentConfig.getType() != config.getType()) {//不同部位不可转化 return
	 * sumChange; } List<List<Integer>> strengthenChange =
	 * config.getStrengthenChange(); for (int i = 0; i < strengthenChange.size();
	 * i++) { List<Integer> changeList = strengthenChange.get(i); Map.Entry<Integer,
	 * Integer> entry = levelupCost.get(i); if
	 * (!changeList.get(0).equals(entry.getKey())) {//转化的资源配置不一致 return sumChange; }
	 * sumChange += equip.getExp() + changeList.get(1) + changeList.get(2) *
	 * equip.getStrength(); if (num < list.size() - 1 && sumChange >=
	 * entry.getValue()) { //经验值已满 return ErrorMsgEnum.exp_max.getId(); } } num++; }
	 * return sumChange; }
	 */

	/*
	 * @Override public int equip(long uid, int roleId, int slot) { Equip
	 * replaceEquip = this.equips.get(uid); if (replaceEquip == null) { return
	 * ErrorMsgEnum.player_check_error.getId(); } if (replaceEquip.getStorage() ==
	 * Equip.IN_BAG) { //在背包中 return ErrorMsgEnum.illegal_request.getId(); }
	 * EquipmentConfig equipmentConfig =
	 * EquipmentManager.getInstance().getEquipmentConfig(replaceEquip.getDictId());
	 * if (equipmentConfig == null) { return
	 * ErrorMsgEnum.config_data_not_found.getId(); } ExploreOp exploreOp =
	 * player.getModule(ExploreOp.class); if
	 * (exploreOp.isInExplore() && exploreOp.isInExploreCamp()) { if
	 * (equipmentConfig.getKind() != GameConstants.EXPLORE_EQUIP) return
	 * ErrorMsgEnum.illegal_request.getId(); } else { if (equipmentConfig.getKind()
	 * == GameConstants.EXPLORE_EQUIP) return ErrorMsgEnum.illegal_request.getId();
	 * } RoleConfig roleConfig = RoleManager.getInstance().getRoleConfig(roleId); if
	 * (roleConfig == null) { return ErrorMsgEnum.config_data_not_found.getId(); }
	 * List<Integer> equipmentSlot = roleConfig.getEquipmentSlot(); if
	 * (equipmentSlot == null || equipmentSlot.size() == 0) { return
	 * ErrorMsgEnum.config_data_not_found.getId(); } if (slot <= 0 || slot >
	 * equipmentSlot.size()) { return ErrorMsgEnum.illegal_request.getId(); } if
	 * (!equipmentSlot.contains(equipmentConfig.getType())) { //类型不对 return
	 * ErrorMsgEnum.illegal_request.getId(); } RoleOp roleOp =
	 * player.getModule(RoleOp.class); if (roleOp.get(roleId)
	 * == null) { return ErrorMsgEnum.player_check_error.getId(); } Map<Byte, Equip>
	 * equipMap = this.role_equips.get(roleId); if (equipMap == null) { equipMap =
	 * new HashMap<>(); } if (equipmentConfig.getType() != equipmentSlot.get(slot -
	 * 1)) { //类型不对 return ErrorMsgEnum.illegal_request.getId(); } Equip equip =
	 * equipMap.get((byte)slot); if (equip == null) {//装备 if
	 * (equipmentConfig.getSlot() == 2 &&
	 * equipmentSlot.get(EquipMsg.EquipSlot.ONE_VALUE - 1) !=
	 * equipmentSlot.get(EquipMsg.EquipSlot.TWO_VALUE - 1) &&
	 * equipmentSlot.get(EquipMsg.EquipSlot.TWO_VALUE - 1) !=
	 * equipmentSlot.get(EquipMsg.EquipSlot.THREE_VALUE - 1)) { return
	 * ErrorMsgEnum.config_data_not_found.getId(); } if (equipmentConfig.getSlot()
	 * == 2 && (slot == EquipMsg.EquipSlot.THREE_VALUE || (slot ==
	 * EquipMsg.EquipSlot.ONE_VALUE &&
	 * (equipMap.get((byte)EquipMsg.EquipSlot.ONE_VALUE) != null ||
	 * equipMap.get((byte)EquipMsg.EquipSlot.TWO_VALUE) != null)) ||(slot ==
	 * EquipMsg.EquipSlot.TWO_VALUE &&
	 * (equipMap.get((byte)EquipMsg.EquipSlot.TWO_VALUE) != null ||
	 * equipMap.get((byte)EquipMsg.EquipSlot.THREE_VALUE) != null)))) { return
	 * ErrorMsgEnum.illegal_request.getId(); } if(equipmentConfig.getSlot() == 2 &&
	 * equipmentSlot.get(EquipMsg.EquipSlot.ONE_VALUE - 1) ==
	 * equipmentSlot.get(EquipMsg.EquipSlot.TWO_VALUE - 1) && slot !=
	 * EquipMsg.EquipSlot.ONE_VALUE) { return ErrorMsgEnum.illegal_request.getId();
	 * } if(equipmentConfig.getSlot() == 2 &&
	 * equipmentSlot.get(EquipMsg.EquipSlot.TWO_VALUE - 1) ==
	 * equipmentSlot.get(EquipMsg.EquipSlot.THREE_VALUE - 1) && slot !=
	 * EquipMsg.EquipSlot.TWO_VALUE) { return ErrorMsgEnum.illegal_request.getId();
	 * }
	 * 
	 * int sumSlot = equipMap.values().stream().map(e ->
	 * EquipmentManager.getInstance().getEquipmentConfig(e.getDictId())).mapToInt(
	 * EquipmentConfig::getSlot).sum(); if (equipmentSlot.size() - sumSlot <
	 * equipmentConfig.getSlot()) { //槽位不够 return
	 * ErrorMsgEnum.equip_slot_not_enough.getId(); } //选择的装备其他角色已经装备了，需要卸下 if
	 * (replaceEquip.getRoleId() != 0) {
	 * this.role_equips.get(replaceEquip.getRoleId()).remove((byte)slot); }
	 * replaceEquip.setRoleId(roleId); replaceEquip.setPos((byte) slot);
	 * updateEquip(uid, roleId, (byte) slot);
	 * 
	 * equipMap.put((byte) slot, replaceEquip); this.role_equips.put(roleId,
	 * equipMap);
	 * 
	 * } else {//替换 if (equip.getId().equals(uid)) { return
	 * ErrorMsgEnum.illegal_request.getId(); } EquipmentConfig config =
	 * EquipmentManager.getInstance().getEquipmentConfig(equip.getDictId()); if
	 * (equipmentConfig.getSlot() != config.getSlot()) { return
	 * ErrorMsgEnum.illegal_request.getId(); } if (replaceEquip.getRoleId() != 0) {
	 * //使用中则卸下 this.role_equips.get(replaceEquip.getRoleId()).remove((byte)slot); }
	 * replaceEquip.setRoleId(roleId); replaceEquip.setPos((byte) slot); //将现装备放回仓库
	 * equip.setRoleId(0); equip.setPos((byte) 0); this.equips.put(equip.getId(),
	 * equip); updateEquip(equip.getId(), equip.getRoleId(), equip.getPos());
	 * updateEquip(replaceEquip.getId(), replaceEquip.getRoleId(),
	 * replaceEquip.getPos());
	 * this.role_equips.get(roleId).put(replaceEquip.getPos(), replaceEquip); }
	 * return ErrorMsgEnum.ok.getId(); }
	 */
}
