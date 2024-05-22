package cn.game.games.net.game.module.develop.equip;

import cn.game.games.cache.entity.Equip;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.EquipMapper;
import cn.game.games.net.game.module.item.AbstractItemNoStackModule;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.protobuf.BaseMsg.EquipInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
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
		return RewardInfo.newBuilder()
				.setEquip(EquipInfo.newBuilder().setConfigId(equip.getConfigId()).setUid(equip.getId() + ""))
				.build();
	}

	@Override
	public Equip newInstance() {
		return new Equip();
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {

	}
	
	@Override
	public void setInstanceAfter(Equip equip) {
//		EquipManager.in
	}

	public int equip(long uid, int roleId, int slot) {

		return 0;
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
