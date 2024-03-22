package cn.game.games.net.game.module.prop.effector;

import java.util.List;
import java.util.Map;

import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.Role;
import cn.game.games.cache.op.impl.BuffOp;
import cn.game.games.cache.op.impl.EquipOp;
import cn.game.games.cache.op.impl.PropertyOp;
import cn.game.games.cache.op.impl.RoleOp;
import cn.game.games.net.game.helper.RoleHelper;
import cn.game.games.net.game.manager.GameConstants;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.buff.BuffValue;
import cn.game.games.net.game.module.prop.RolePropFromType;
import cn.game.games.net.game.module.prop.RoleProperty;
import cn.game.protocol.generated.config.RoleConfig;
import cn.game.protocol.generated.config.SoulWeaponListConfig;
import cn.game.protocol.generated.enume.AttributeSubTypeEnum;
import cn.game.protocol.generated.enume.AttributeTypeEnum;
import cn.game.protocol.generated.enume.EffectEnum;
import cn.game.protocol.generated.enume.EffectTargetTypeEnum;
import cn.game.protocol.generated.helper.FormulaHelper;
import cn.game.protocol.generated.manager.RoleManager;
import cn.game.protocol.generated.manager.SoulWeaponListManager;

public abstract class PropertyEffectorFactory {

    /**
     * 基础属性
     */
    public static final PropertyEffectorFactory BASE_EFFECTOR = new PropertyEffectorFactory() {

        @Override
        public RoleProperty effect(RoleProperty prop, long playerId, int id) {
            prop.clear();
			Player player = PlayerManager.getInstance().getPlayer(playerId);

            RoleOp roleOp = player.getModule(RoleOp.class);
            Role role = roleOp.get(id);
            RoleConfig roleConfig = RoleManager.getInstance().getRoleConfig(role.getDictId());

            prop.set(AttributeTypeEnum.hp.getId(), Math.round(FormulaHelper.getRoleBodyAttribute(roleConfig.getHp(), role.getPromotionLevel(), roleConfig.getHpGrow())));
            prop.set(AttributeTypeEnum.attack.getId(), Math.round(FormulaHelper.getRoleBodyAttribute(roleConfig.getAttack(), role.getPromotionLevel(), roleConfig.getAttackGrow())));
            prop.set(AttributeTypeEnum.speed.getId(), Math.round(FormulaHelper.getRoleBodyAttribute(roleConfig.getSpeed(), role.getPromotionLevel(), roleConfig.getSpeedGrow())));
            prop.set(AttributeTypeEnum.defense.getId(), Math.round(FormulaHelper.getRoleBodyAttribute(roleConfig.getDefense(), role.getPromotionLevel(), roleConfig.getDefenseGrow())));
            prop.set(AttributeTypeEnum.san.getId(), Math.round(FormulaHelper.getRoleBodyAttribute(roleConfig.getSan(), role.getPromotionLevel(), roleConfig.getSanGrow())));
            return prop;
        }
    };


    /**
     * 灵武属性
     */
    public static final PropertyEffectorFactory SOULWEAPON_EFFECTOR = new PropertyEffectorFactory() {
        @Override
        public RoleProperty effect(RoleProperty prop, long playerId, int id) {
            prop.clear();
            SoulWeaponListConfig config = SoulWeaponListManager.getInstance().getSoulWeaponListConfigNullable(id);
            if (config != null) {
                List<List<Integer>> list = config.getAttribute();
                list.forEach(e -> {
                    if (e.size() < 3) {
                        return;
                    }
                   // prop.add(e.get(0), e.get(1) + e.get(2) * (role.getSoulweaponLevel() - 1));
                });
            }
            return prop;
        }
    };

    /**
     * 装备属性
     */
    public static final PropertyEffectorFactory EQUIP_EFFECTOR = new PropertyEffectorFactory() {
        @Override
        public RoleProperty effect(RoleProperty prop, long playerId, int id) {
			Player player = PlayerManager.getInstance().getPlayer(playerId);

            prop.clear();
            //装备固定属性
            EquipOp equipOp = player.getModule(EquipOp.class);
            Map<Integer, Integer> equipAttrValByType = equipOp.getEquipAttrValByType(id, GameConstants.EQUIP_FIXEDVAL);
            for (Map.Entry<Integer,Integer> entry : equipAttrValByType.entrySet()) {
                prop.add(entry.getKey(), entry.getValue());
            }
            return prop;
        }
    };
    

    /**
     * 影响面板的buff
     */
    public static final PropertyEffectorFactory BUFF_EFFECTOR =  new PropertyEffectorFactory() {

        @Override
        public RoleProperty effect(RoleProperty prop, long playerId, int id) {
            prop.clear();
			int roleDictId = id;

            // 影响角色属性的buff
				addBuffEffect(playerId, prop, roleDictId, EffectTargetTypeEnum.Role, roleDictId);
			return prop;
        }
        
        /**
		 * 按照buff目标身上的属性效果,给角色加成
		 * @param playerId
		 * @param prop			角色的属性
		 * @param roleId		dictId或uid
		 * @param targetType	哪种目标的buff
		 * @param targetId		buff目标id
		 */
		private void addBuffEffect(long playerId, RoleProperty prop, int roleId, EffectTargetTypeEnum targetType, int targetId) {
			Player player = PlayerManager.getInstance().getPlayer(playerId);

			PropertyOp propertyOp = player.getModule(PropertyOp.class);
			BuffOp buffOp = player.getModule(BuffOp.class);
			BuffValue buffValue = buffOp.getBuffValue(targetType, targetId, EffectEnum.ChangeRoleAttribute);
			
			for (AttributeTypeEnum type : AttributeTypeEnum.values()) {
				int attrId = type.getId();
				for (AttributeSubTypeEnum subType : AttributeSubTypeEnum.values()) {
					if (subType != AttributeSubTypeEnum.total) {
						continue;
					}
					int roleAttributeId = RoleHelper.makeRoleAttributeId(type, subType);
					if (roleAttributeId == 101) {
						int test = 1;
					}
					int change = 0;
					if (roleAttributeId == 1601) {
						int test = 1;
					}
					int absoluteValue = buffValue.getValue(BuffValue.CHANGE_BY_VALUE, roleAttributeId);
					int panelPercent = buffValue.getValue(BuffValue.CHANGE_BY_PANEL_PERCENT, roleAttributeId);
					int value3 = buffValue.getValue(BuffValue.CHANGE_BY_CUR_PERCENT, roleAttributeId);
					int bodyPercent = buffValue.getValue(BuffValue.CHANGE_BY_BODY_PERCENT, roleAttributeId);
					int value5 = buffValue.getValue(BuffValue.CHANGE_BY_MAX_PERCENT, roleAttributeId);
					// 按面板百分比改变
					if (panelPercent != 0) {
						int panelValue = propertyOp.getPanelValue(roleId, attrId);
						change = Math.round(panelValue * (panelPercent / 100f));
						prop.add(attrId, change);
					}
					// 按身体百分比改变
					if (bodyPercent != 0) {
						int bodyValue = propertyOp.getRolePropTypeEnumValue(roleId, RolePropFromType.BASE, attrId);
						change = Math.round(bodyValue * (bodyPercent / 100f));
						prop.add(attrId, change);
					}
					// 按绝对值改变
					if (absoluteValue != 0) {
	                    prop.add(attrId, absoluteValue);
					}

				}
			}
		}
    };

    /**
     * 角色升级
     */
    public static final PropertyEffectorFactory LEVELUP_EFFECTOR = new PropertyEffectorFactory() {
        @Override
        public RoleProperty effect(RoleProperty prop, long playerId, int id) {
			Player player = PlayerManager.getInstance().getPlayer(playerId);

            prop.clear();
            RoleOp roleOp = player.getModule(RoleOp.class);
            Role role = roleOp.get(id);
            if (role != null) {
                Map<Integer, Integer> attrPointAddMap = role.getAttrPointAddMap();
                for (Map.Entry<Integer,Integer> entry : attrPointAddMap.entrySet()) {
                    prop.add(entry.getKey(), entry.getValue());
                }
            }
            return prop;
        }
    };


    /**
     * 高级建筑属性
     */
    public static final PropertyEffectorFactory SUPERBUILDING_EFFECTOR = new PropertyEffectorFactory() {
        @Override
        public RoleProperty effect(RoleProperty prop, long playerId, int id) {
            prop.clear();
            RoleConfig roleConfig = RoleManager.getInstance().getRoleConfig(id);
            return prop;
        }
    };

    /**
     * 模块属性影响
     */
    public abstract RoleProperty effect(RoleProperty prop, long playerId, int id);

    /**
     * 计算模型率
     * @param playerId
     * @param roleId
     * @param typeEnum
     * @return
     */
    /*public int calculateModelRate(long playerId, int roleId, AttributeTypeEnum typeEnum) {
        //随机点数
        RoleOp roleOp = player.getModule(RoleOp.class);
        int defensePoint = roleOp.getRoleAttrPoint(roleId, typeEnum);
        int value = Math.round(typeEnum.getModelArgu() * defensePoint * typeEnum.getArgu2() / (typeEnum.getArgu1() - typeEnum.getModelArgu()));
        return value;
    }*/



}
