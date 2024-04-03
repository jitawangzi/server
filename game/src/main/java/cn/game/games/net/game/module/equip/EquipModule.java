package cn.game.games.net.game.module.equip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.ProtocolStringList;

import cn.game.core.util.IdUtil;
import cn.game.games.cache.entity.Equip;
import cn.game.games.cache.op.impl.BuffOp;
import cn.game.games.cache.op.impl.RoleOp;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.EquipMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.manager.GameConstants;
import cn.game.games.net.game.module.item.AbstractItemNoStackModule;
import cn.game.games.net.game.module.item.ItemModule;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.EquipmentBuffConfig;
import cn.game.protocol.generated.config.EquipmentConfig;
import cn.game.protocol.generated.config.EquipmentEnumConfig;
import cn.game.protocol.generated.config.OldBuffConfig;
import cn.game.protocol.generated.config.OldGlobalConst;
import cn.game.protocol.generated.config.RoleConfig;
import cn.game.protocol.generated.enume.GoodsTypeEnum;
import cn.game.protocol.generated.manager.EquipmentBuffManager;
import cn.game.protocol.generated.manager.EquipmentEnumManager;
import cn.game.protocol.generated.manager.EquipmentManager;
import cn.game.protocol.generated.manager.OldBuffManager;
import cn.game.protocol.generated.manager.RoleManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.BaseMsg.EquipInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.Rnd;
/**    
 * 装备模块
 * @date 2024年2月19日 上午10:55:53
 * @author SYQ
 */
public class EquipModule extends AbstractItemNoStackModule<Equip> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay };


	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		}
	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
		return new Class<?>[] { EquipMapper.class };
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.Equipment;
	}

	@Override
	public RewardInfo toRewardInfo(Equip equip) {
		return RewardInfo.newBuilder().setEquip(EquipInfo.newBuilder().setId(equip.getConfigId())).build();
	}

	@Override
	public Equip newInstance() {
		return new Equip();
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}
	
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
	public void setInstanceAfter(Equip item) {

	}

//	@Override
//	public void del(long id) {
//		Equip remove = equips.remove(id);
//		DAO.delete(EquipMapper.class, remove.getId());
//	}

//	@Override
//	public Collection<Equip> list() {
//		return equips.values();
//	}

	public void toRepository(long uid) {
		Equip equip = get(uid);
		equip.setStorage(Equip.IN_REPOSITORY);
	}

	public void toBag(long uid) {
		Equip equip = get(uid);
		equip.setStorage(Equip.IN_BAG);
	}

	public ErrorMsgEnum toBag(ProtocolStringList equipUids) {
		ItemModule itemModule = player.getModule(ItemModule.class);
//		int idle = itemModule.getBagIdleSize();
//		if (equipUids.size() > idle) {
//			return ErrorMsgEnum.item_bag_capacity_not_enough;
//		}

		for (String uidStr : equipUids) {
			long uid = StringUtils.isEmpty(uidStr) ? 0 : Long.parseLong(uidStr);
			Equip equip = get(uid);
			if (equip == null) {
				return ErrorMsgEnum.illegal_request;
			}
		}
		for (String uidStr : equipUids) {
			long uid = StringUtils.isEmpty(uidStr) ? 0 : Long.parseLong(uidStr);
			toBag(uid);
//			itemModule.addBagEquip(uid);
		}
		return ErrorMsgEnum.ok;
	}

	public int equip(long uid, int roleId, int slot) {
		ItemModule itemModule = player.getModule(ItemModule.class);
		Equip newEquip = get(uid);
		if (newEquip == null) {
			return ErrorMsgEnum.player_check_error.getId();
		}
		if (newEquip.getRoleId() != 0) {
			// 不能选择其他角色已经装备的
			return ErrorMsgEnum.player_check_error.getId();
		}
		EquipmentConfig equipmentConfig = EquipmentManager.getInstance().getEquipmentConfig(newEquip.getDictId());

		RoleConfig roleConfig = RoleManager.getInstance().getRoleConfig(roleId);
		List<Integer> equipmentSlot = roleConfig.getEquipmentSlot();
		if (equipmentSlot == null || equipmentSlot.size() == 0) {
			return ErrorMsgEnum.config_data_not_found.getId();
		}
		if (slot <= 0 || slot > equipmentSlot.size()) {
			return ErrorMsgEnum.illegal_request.getId();
		}
		RoleOp roleOp = player.getModule(RoleOp.class);
		if (roleOp.get(roleId) == null) {
			return ErrorMsgEnum.player_check_error.getId();
		}
		Map<Byte, Equip> equipMap = null ;
		if (equipMap == null) {
			equipMap = new HashMap<>();
		}
		if (equipmentConfig.getType() != equipmentSlot.get(slot - 1)) {
			// 类型不对
			return ErrorMsgEnum.illegal_request.getId();
		}
		// 身上的装备
		Equip bodyEquip = equipMap.get((byte) slot);
		BuffOp buffOp = player.getModule(BuffOp.class);
		if (bodyEquip == null) {// 装备
			if (equipmentConfig.getRoleId().size() != 0 && !equipmentConfig.getRoleId().contains(roleId)) {
				return ErrorMsgEnum.illegal_request.getId();
			}
			if (equipmentConfig.getOccupation().size() != 0
					&& !equipmentConfig.getOccupation().contains(roleConfig.getOccupation())) {
				return ErrorMsgEnum.illegal_request.getId();
			}
			if (equipmentConfig.getCamp().size() != 0 && !equipmentConfig.getCamp().contains(roleConfig.getCamp())) {
				return ErrorMsgEnum.illegal_request.getId();
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

			equipMap.put((byte) slot, newEquip);
//			this.role_equips.put(roleId, equipMap);

		} else {// 替换
			if (bodyEquip.getId().equals(uid)) {
				return ErrorMsgEnum.illegal_request.getId();
			}
			EquipmentConfig config = EquipmentManager.getInstance().getEquipmentConfig(bodyEquip.getDictId());
			if (equipmentConfig.getType() != config.getType()) {
				return ErrorMsgEnum.illegal_request.getId();
			}

			// 背包里装备增减
			bodyEquip.setStorage(Equip.IN_REPOSITORY);
			
			bodyEquip.setRoleId(0);
			bodyEquip.setPos((byte) 0);
			
			newEquip.setStorage(Equip.IN_ROLE);
			newEquip.setRoleId(roleId);
			newEquip.setPos((byte) slot);
			
//			this.equips.put(bodyEquip.getId(), bodyEquip);

//			this.role_equips.get(roleId).put(newEquip.getPos(), newEquip);

			//移除之前的buff
			buffOp.remove(roleId, bodyEquip.getBuffList());
		}
		//添加buff
		newEquip.getBuffList().forEach(e -> buffOp.add(e, Arrays.asList(roleId)));
		return ErrorMsgEnum.ok.getId();
	}

//	public boolean teardown(int roleId, int slot) {
//		Map<Byte, Equip> byteEquipMap = this.role_equips.get(roleId);
//		if (byteEquipMap == null) {
//			return false;
//		}
//		Equip equip = byteEquipMap.get((byte) slot);
//		if (equip == null) {
//			return false;
//		}
//		// 装备不在角色身上
//		if (equip.getStorage() != Equip.IN_ROLE) {
//			return false;
//		}
//		
//		ItemModule itemModule = player.getModule(ItemModule.class);
//		equip.setRoleId(0);
//		equip.setPos((byte) 0);
//		byteEquipMap.remove((byte) slot);
//		equip.setStorage(Equip.IN_REPOSITORY);
//
//		//移除buff
//		BuffOp buffOp = player.getModule(BuffOp.class);
//		buffOp.remove(equip.getBuffList());
//
//		return true;
//	}

//	public int equipStrength(int roleId, int slot) {
//		Map<Byte, Equip> byteEquipMap = this.role_equips.get(roleId);
//		if (byteEquipMap == null || byteEquipMap.size() == 0) {
//			return ErrorMsgEnum.player_check_error.getId();
//		}
//		Equip equip = byteEquipMap.get((byte) slot);
//		if (equip == null) {
//			return ErrorMsgEnum.player_check_error.getId();
//		}
//		int level = equip.getStrength();
//		//装备最大等级受科技树影响
//		EquipmentStrengthenCostConfig config = EquipmentStrengthenCostManager.getInstance()
//				.getEquipmentStrengthenCostConfig(level + 1);
//		if (!PlayerHelper.delResources(playerId, config.getStandardExp(), ResourceConsumeEnum.EquipmentStrengthen)) {
//			return ErrorMsgEnum.resource_not_enough.getId();
//		}
//		// 4、7、10级时分别解锁一个词缀
//		EquipmentConfig equipmentConfig = EquipmentManager.getInstance().getEquipmentConfig(equip.getDictId());
//		List<List<Integer>> buffs = equipmentConfig.getBuffs();
//		List<Integer> equipUnlockBuffPromotionLevel = OldGlobalConst.EquipUnlockBuffPromotionLevel;
//		BuffOp buffOp = player.getModule(BuffOp.class);
//		if (level + 1 == equipUnlockBuffPromotionLevel.get(0)) {
//			buffs.get(0).forEach(e-> {
//				equip.addBuff(e);
//				buffOp.add(e, Arrays.asList(roleId));
//			});
//		} else if (level + 1 == equipUnlockBuffPromotionLevel.get(1)) {
//			buffs.get(1).forEach(e-> {
//				equip.addBuff(e);
//				buffOp.add(e, Arrays.asList(roleId));
//			});
//		} else if (level + 1 == equipUnlockBuffPromotionLevel.get(2)) {
//			buffs.get(2).forEach(e-> {
//				equip.addBuff(e);
//				buffOp.add(e, Arrays.asList(roleId));
//			});
//		}
//		//触发事件
//		EventHelper.handleEvent(playerId, new GameEvent(EventTypeEnum.FixedEquipmentLevel, update.getStrength()));
//		return ErrorMsgEnum.ok.getId();
//	}

//	public List<BaseMsg.EquipInfo> bulidEquipList(int roleId) {
//		Map<Byte, Equip> integerEquipMap = this.role_equips.get(roleId);
//		List<BaseMsg.EquipInfo> equipInfos = new ArrayList<>();
//		for (Equip equip : integerEquipMap.values()) {
//			equipInfos.add(PbBuilder.buildEquipInfo(equip));
//		}
//		return equipInfos;
//	}

//	public Map<Byte, Equip> getRoleEquipByRoleId(int roleId) {
//		return this.role_equips.get(roleId);
//	}

//	public Map<Integer, Integer> getEquipAttrValByType(int roleId, int valueType) {
//		Map<Byte, Equip> roleEquipByRoleId = getRoleEquipByRoleId(roleId);
//		if (roleEquipByRoleId == null) {
//			return new HashMap<>();
//		}
//		Map<Integer, Integer> attrMap = new HashMap<>();
//		roleEquipByRoleId.values().forEach(equip -> {
//			EquipmentConfig equipmentConfig = EquipmentManager.getInstance().getEquipmentConfig(equip.getConfigId());
//			if (valueType == GameConstants.EQUIP_FIXEDVAL) {// 固定属性
//				List<Map.Entry<Integer, Integer>> attribute = equipmentConfig.getAttribute();
//				attribute.forEach(e -> {
//					attrMap.merge(e.getKey(), e.getValue(), Integer::sum);
//				});
//				List<EquipmentAttributteTemplateConfig> typelevelList = EquipmentAttributteTemplateManager.getInstance()
//						.getTypestrengthList(equipmentConfig.getAttributeTemplateType(), equip.getDictId());
//				if (typelevelList != null && typelevelList.size() > 0) {
//					typelevelList.get(0).getAttribute().forEach(e -> {
//						attrMap.merge(e.getKey(), e.getValue(), Integer::sum);
//					});
//				}
//			}
//			//List<Integer> buff = equipmentConfig.getBuff();
//			// 添加buff属性 ----buff中有固定属性和百分比属性
//			//addBuffAttr(buff, valueType, attrMap);
//			//addBuffAttr(equip.getBuffList(), valueType, attrMap);
//		});
//		return attrMap;
//	}

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
	public void deleteByIds(List<Long> list) {
		if (list == null || list.size() == 0) {
			return;
		}
		for (long id : list) {
			removeCache(get(id));
		}
		DAO.execute(EquipMapper.class, MapperConstant.deleteByIds, list);
	}

//	public void addRoleFixedEquip(int equipDictId, int roleId, int slot) {
//		EquipmentConfig equipmentConfig = EquipmentManager.getInstance().getEquipmentConfigNullable(equipDictId);
//		if (equipmentConfig == null) {
//			return;
//		}
//		if (equipmentConfig.getType() != GameConstants.FIXED_EQUIP) {
//			return;
//		}
//		Equip insert = new Equip();
//		insert.setPlayerId(playerId);
////		insert.setDictId(equipDictId);
////		insert.setStrength((byte) 0);
////		insert.setExp(0);
//		long id = IdUtil.getId();
//		insert.setId(id);
//		RoleConfig roleConfig = RoleManager.getInstance().getRoleConfig(roleId);
//		List<Integer> equipmentSlot = roleConfig.getEquipmentSlot();
//		if (equipmentSlot == null || equipmentSlot.size() == 0) {
//			return;
//		}
//		if (slot <= 0 || slot > equipmentSlot.size()) {
//			return;
//		}
//		Map<Byte, Equip> equipMap = this.role_equips.get(roleId);
//		if (equipMap == null) {
//			equipMap = new HashMap<>();
//		}
//		if (equipmentConfig.getType() != equipmentSlot.get(slot - 1)) {
//			// 类型不对
//			return;
//		}
//		Equip equip = equipMap.get((byte) slot);
//		if (equip != null) {
//			return;
//		}
//		insert.setRoleId(roleId);
//		insert.setPos((byte) slot);
//		insert.setStorage(Equip.IN_ROLE);
//		equipmentConfig.getBuff().forEach(e-> insert.addBuff(e));
//
//		equipMap.put((byte) slot, insert);
//	}


}
