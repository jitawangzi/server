package cn.game.games.cache.op.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.google.common.collect.Multimap;

import cn.game.games.cache.base.PlayerCacheFactory;
import cn.game.games.cache.entity.Equip;
import cn.game.games.cache.entity.Role;
import cn.game.games.cache.op.face.IPropertyOp;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.RoleHelper;
import cn.game.games.net.game.module.prop.RolePropFromType;
import cn.game.games.net.game.module.prop.RoleProperty;
import cn.game.protocol.generated.config.OldBuffConfig;
import cn.game.protocol.generated.config.RoleConfig;
import cn.game.protocol.generated.config.RoleTagConfig;
import cn.game.protocol.generated.config.OldSkillConfig;
import cn.game.protocol.generated.enume.AttributeSubTypeEnum;
import cn.game.protocol.generated.enume.AttributeTypeEnum;
import cn.game.protocol.generated.enume.EffectEnum;
import cn.game.protocol.generated.manager.OldBuffManager;
import cn.game.protocol.generated.manager.RoleManager;
import cn.game.protocol.generated.manager.RoleTagManager;
import cn.game.protocol.generated.manager.OldSkillManager;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

public class PropertyOp extends BasePlayerModule implements IPropertyOp {

    private EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.BuffChange };
    /**
     * roleid或ExploreRole的uid-每模块属性计算值
     */
    private Map<Integer, Map<Integer, RoleProperty>> rolePropEffectors;

    /**
     * 面板属性 key为roleid或ExploreRole的uid
     */
    public Map<Integer, RoleProperty> roleProperty;

    @Override
	public void init() {
        rolePropEffectors = new HashMap<>();
        roleProperty = new HashMap<>();
    }

    public void updateProperty(long playerId) {
        RoleOp roleOp = player.getModule(RoleOp.class);
        Collection<Role> list = roleOp.list();
        list.forEach(role -> {
           updateProperty(role.getDictId());
        });
    }

    /**
     * 修改某一模块的属性
     * @param id
     * @param type
     */
    public void updateProperty(int id, RolePropFromType type) {
        //属性影响
        Map<Integer, RoleProperty> roleProperties = getRoleProperties(id);
        RoleProperty property = roleProperties.get(type.index);
        if (property == null) {
            property = new RoleProperty();
        }
        type.effector.effect(property, playerId, id);
    }

    @Override
    public void updateProperty(int id) {
       /* //得到角色模块属性值
        Map<Integer, RoleProperty> roleProperties = getRoleProperties(roleId);
        //基础属性影响
        RolePropFromType.BASE.effector.effect(roleProperties.get(RolePropFromType.BASE.index), playerId, roleId);
        //面板属性影响
        RolePropFromType.PANEL.effector.effect(roleProperties.get(RolePropFromType.PANEL.index), playerId, roleId);*/
        // RoleProperty[] roleProperties = getRoleProperties(role.getDictId());
        Map<Integer, RoleProperty> roleProperties = getRoleProperties(id);
        for (RolePropFromType type : RolePropFromType.values()) {
            //属性影响
            RoleProperty property = roleProperties.get(type.index);
            if (property == null) {
                property = new RoleProperty();
                roleProperties.put(type.index, property);
            }
            type.effector.effect(property, playerId, id);
        }

        updateFinalProperty(id);
    }

    private Map<Integer, RoleProperty> getRoleProperties(int id) {
        this.rolePropEffectors.compute(id,(k,v) -> {
            if (v == null) {
                v = new HashMap<>();
               /* List<RolePropFromType> rolePropFromTypeList = RolePropFromType.getRolePropFromTypeList();
                for (RolePropFromType type : rolePropFromTypeList) {
                    if (type != null) {
                        v.compute(type.getIndex(), (type1,property) -> property = new RoleProperty());
                    }
                }*/
            }
            return v;
        });
        return this.rolePropEffectors.get(id);
    }

    private void updateFinalProperty(int id) {
        this.roleProperty.compute(id, (k,v) -> {
            if (v == null) {
                v = new RoleProperty();
            }
            return v;
        });
        RoleProperty property = this.roleProperty.get(id);
        property.clear();
        Map<Integer, RoleProperty> integerRolePropertyMap = this.rolePropEffectors.get(id);
        for (RoleProperty prop : integerRolePropertyMap.values()) {
            property.add(prop);
        }

    }

	@Override
	public int getProperty(int id, AttributeTypeEnum type) {
		/*RoleProperty roleProp = roleProperty.get(configId);
		if (roleProp != null) {
			return roleProp.get(type.getId());
		} else {
            updateProperty(configId);
        }
		return roleProperty.get(configId).get(type.getId());*/
        RoleProperty roleProp = this.roleProperty.get(id);
        if (roleProp == null) {
            updateProperty(id);
        }
        return this.roleProperty.get(id).get(type.getId());
    }


	@Override
    public int getRolePropTypeEnumValue(int id, RolePropFromType type, int attribteId) {
        Map<Integer, RoleProperty> roleProperties = getRoleProperties(id);
        RoleProperty roleProperty = roleProperties.get(type.index);
        if (roleProperty == null) {
            roleProperty = new RoleProperty();
            type.effector.effect(roleProperty, playerId, id);
        }
        return roleProperty.get(attribteId);
    }

    @Override
    public int getPanelValue(int roleId, int attribteId) {
        //基础属性
        int baseValue = getRolePropTypeEnumValue(roleId, RolePropFromType.BASE, attribteId);
        //装备属性
        int equipValue = getRolePropTypeEnumValue(roleId, RolePropFromType.EQUIP, attribteId);
        //装备加成
        int equipBuff = getEquipBuffValue(roleId, attribteId);

        //建筑属性
        int buildingValue = getRolePropTypeEnumValue(roleId, RolePropFromType.BUILDING, attribteId);
        //天赋技能加成
        int talentBuff = getTalentSkillBuffValue(roleId, attribteId);
        //角色标签
        int tagBuff = getRoleTagBuffValue(roleId, attribteId);
        //意志
        int teamAdditionBuff = getTeamAdditionBuffValue(attribteId);

        return baseValue + equipValue + buildingValue + equipBuff + talentBuff + tagBuff + teamAdditionBuff;
    }

    private int getTeamAdditionBuffValue(int attribteId) {
        int buffAddValue = 0;
        return buffAddValue;
    }

    public int getRoleTagBuffValue(int roleId, int attribteId) {
        RoleOp roleOp = player.getModule(RoleOp.class);
        Role role = roleOp.get(roleId);
        List<Integer> tagList = role.getTagList();
        int tagBuffValue = 0;
        if (tagList != null && tagList.size() > 0) {
            for (Integer tag : tagList) {
                RoleTagConfig roleTagConfig = RoleTagManager.getInstance().getRoleTagConfigNullable(tag);
                //不用计算san标签
                if (roleTagConfig.getType() != 1) {
                    List<Integer> buffList = roleTagConfig.getBuff();
                    for (Integer buff : buffList) {
                        tagBuffValue += getBuffAddValue(attribteId, buff);
                    }
                }
            }

        }
        return tagBuffValue;
    }

    public int getEquipBuffValue(int roleId, int attribteId) {
        EquipOp equipOp = player.getModule(EquipOp.class);
        RoleOp roleOp = player.getModule(RoleOp.class);
        Role role = roleOp.get(roleId);
        Map<Byte, Equip> roleEquipByRoleId = equipOp.getRoleEquipByRoleId(role.getDictId());
        int addBuff = 0;
        if (roleEquipByRoleId != null && roleEquipByRoleId.size() > 0) {
            for (Equip equip : roleEquipByRoleId.values()) {
                List<Integer> buffList = equip.getBuffList();
                if (buffList != null && buffList.size() > 0) {
                    for (Integer buff : buffList) {
                        addBuff += getBuffAddValue(attribteId, buff);
                    }
                }
            }
        }
        return addBuff;
    }

    public int getBuffAddValue(int attribteId, int buffId) {
        int buffValue = 0;
        OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfigNullable(buffId);
        if (buffConfig != null) {
            if ((buffConfig.getEffectType() == EffectEnum.ChangeRoleAttribute || buffConfig.getEffectType() == EffectEnum.RestoreLife) && buffConfig.getNumTypeParam() == 0) {
				int idParam = buffConfig.getIdParam();
				int numParam = buffConfig.getNumParam();
				if (idParam != 0) {
					AttributeTypeEnum attrType = RoleHelper.getAttributeType(idParam);
					AttributeSubTypeEnum subType = RoleHelper.getSubType(idParam);
					if (attrType.getId() == attribteId && subType == AttributeSubTypeEnum.total) {
						buffValue += numParam;
					}
				} else {// 多属性修改
					int[] extParam = buffConfig.getExtParam();
					for (int attrId : extParam) {
						AttributeTypeEnum attrType = RoleHelper.getAttributeType(attrId);
						AttributeSubTypeEnum subType = RoleHelper.getSubType(attrId);
						if (attrType.getId() == attribteId && subType == AttributeSubTypeEnum.total) {
							buffValue += numParam;
						}
					}
				}
            }
        }
        return buffValue;
    }

    public int getTalentSkillBuffValue(int roleId, int attribteId){
        //获取当前role身上的天赋
        RoleOp roleOp = player.getModule(RoleOp.class);
        Role role = roleOp.get(roleId);
        int talentSkill = roleOp.getTalentSkill(role.getDictId());
        RoleConfig roleConfig = RoleManager.getInstance().getRoleConfig(role.getDictId());
        Multimap<Integer, Integer> occTalentSkillsById = roleOp.getOccTalentSkillsById(roleConfig.getOccupation());
        int talentBuff = 0;
        if (occTalentSkillsById != null && occTalentSkillsById.size() > 0) {
            Collection<Integer> integers = occTalentSkillsById.get(talentSkill);
            if (integers == null) {
                return talentBuff;
            }
            for (Integer e : integers) {
                if (e == -1) {
                    OldSkillConfig skillConfig = OldSkillManager.getInstance().getSkillConfig(talentSkill);
                    for (Integer buffId : skillConfig.getBuff()) {
                        talentBuff += getBuffAddValue(attribteId, buffId);
                    }
                } else {
                    talentBuff += getBuffAddValue(attribteId, e);
                }
            }
        }
        return talentBuff;
    }

    @Override
    public void initPropertyById(int id) {
//        if(ExploreHelper.isExploreRole(id)) {
//            clearRolePropertyById(id);
//        } else {
//            clearRolePropertyById(id);
//            int exploreRoleId = ExploreHelper.makeExploreRoleId(id, true);
//            clearRolePropertyById(exploreRoleId);
//        }
    }

    public void clearRolePropertyById(int id) {
        if (this.roleProperty.containsKey(id)) {
            this.roleProperty.remove(id);
        }
        if (this.rolePropEffectors.containsKey(id)) {
            this.rolePropEffectors.remove(id);
        }
    }

    @Override
    public void handleEvent(GameEvent event) {
        switch (event.getType()) {
            case BuffChange: {
                List<Long> targetIds = (List<Long>) event.getParameter(0);
                targetIds.forEach(e -> {
                    RoleOp roleOp = PlayerCacheFactory.getCache(this.playerId, RoleOp.class);
                    int roleId = e.intValue();
                    boolean exist = roleOp.exist(roleId);
                    if (exist) {
                        initPropertyById(roleId);
                    }
                });
            }
            break;
            default:
                break;
        }
    }

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
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
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}
}
