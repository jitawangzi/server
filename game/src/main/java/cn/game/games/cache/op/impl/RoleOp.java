package cn.game.games.cache.op.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import cn.game.core.util.IdUtil;
import cn.game.core.util.IdUtil.IdType;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerExt;
import cn.game.games.cache.entity.Role;
import cn.game.games.cache.entity.RoleAction;
import cn.game.games.cache.entity.RoleTagQuest;
import cn.game.games.cache.op.face.IRoleOp;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.RoleActionBattleMapper;
import cn.game.games.net.data.mapper.RoleActionMapper;
import cn.game.games.net.data.mapper.RoleMapper;
import cn.game.games.net.data.mapper.RoleTagQuestMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.helper.BuffHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.RoleHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.award.RewardItem;
import cn.game.games.net.game.module.buff.BuffValue;
import cn.game.games.net.game.module.item.ItemModule;
import cn.game.games.net.game.module.prop.RolePropFromType;
import cn.game.games.net.game.module.role.RoleActionType;
import cn.game.games.net.game.module.role.RoleConstant;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.FriendlyConfig;
import cn.game.protocol.generated.config.MercenaryNameConfig;
import cn.game.protocol.generated.config.OccupationTalentNodeConfig;
import cn.game.protocol.generated.config.OldBuffConfig;
import cn.game.protocol.generated.config.OldGlobalConst;
import cn.game.protocol.generated.config.OldSkillConfig;
import cn.game.protocol.generated.config.RoleConfig;
import cn.game.protocol.generated.config.RoleExpConfig;
import cn.game.protocol.generated.config.RoleRisingStarConfig;
import cn.game.protocol.generated.config.RoleTagConfig;
import cn.game.protocol.generated.enume.AttributeSubTypeEnum;
import cn.game.protocol.generated.enume.AttributeTypeEnum;
import cn.game.protocol.generated.enume.OldConditionTypeEnum;
import cn.game.protocol.generated.enume.RoleTagEnum;
import cn.game.protocol.generated.manager.FriendlyManager;
import cn.game.protocol.generated.manager.MercenaryNameManager;
import cn.game.protocol.generated.manager.OccupationTalentNodeManager;
import cn.game.protocol.generated.manager.OldBuffManager;
import cn.game.protocol.generated.manager.OldSkillManager;
import cn.game.protocol.generated.manager.RoleExpManager;
import cn.game.protocol.generated.manager.RoleManager;
import cn.game.protocol.generated.manager.RoleRisingStarManager;
import cn.game.protocol.generated.manager.RoleTagManager;
import cn.game.protocol.manual.OldErrorMsgEnum;
import cn.game.protocol.manual.ResourceConsumeEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.ByteHelp;
import cn.game.util.MapUtil;
import cn.game.util.Rnd;

public class RoleOp extends BasePlayerModule implements IRoleOp {

	private transient EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.ExploreMapEnter };

	// id => Role,探索角色也存放在这里，方便更新探索角色血量。
	private Map<Integer, Role> id_roles = new HashMap<>();;
	// roleId=> type -> actions
	private Map<Integer, Multimap<Integer, RoleAction>> roleActions;
	// 开启的不同职业的天赋技能 职业id=> skillid -> buffid
	private Map<Integer, Multimap<Integer, Integer>> occTalentSkills;
	// roleId=>tagId-> roleQuest
	private Map<Integer, Map<Integer, RoleTagQuest>> roleTagQuests;

	@Override
	public Class<?>[] defaultDbMapperClass() {
		return new Class[] { RoleMapper.class };
	}

	@Override
	public int initLoadData(List<Role> roles) {
		// 职业技能天赋
		PlayerExt playerExt = PlayerManager.getInstance().getPlayer(playerId).getExt();
		List<Integer> occTalentNodes = playerExt.getOccTalentNodes();
		occTalentNodes.forEach(e -> putOccTalentSkill(e));

		if (roles == null) {
			return 0;
		}
		for (Role role : roles) {
			id_roles.put(role.getDictId(), role);
			role.load();
		}

//		for (RoleAction roleAction : roleActions) {
//			Multimap<Integer, RoleAction> ac = getRoleActionMap(roleAction.getRoleId());
//			ac.put(roleAction.getType(), roleAction);
//		}
//		for (UserTag uidTag : uidTags) {
//			int tagId = uidTag.getTagId();
//			RoleTagConfig roleTagConfig = RoleTagManager.getInstance().getRoleTagConfig(tagId);
//			// TODO
//			int roleId = 0;
//			Role role = get(roleId);
//			role.addTag(tagId);
//		}
//		for (RoleTagQuest tagQuest : roleTagQuests) {
//			Map<Integer, RoleTagQuest> roleTagQuestMap = getRoleTagQuestMap(tagQuest.getRoleDictId());
//			roleTagQuestMap.put(tagQuest.getTagId(), tagQuest);
//			tagQuest.initCondition();
//		}

		return id_roles.size();
	}

	protected void initFromDb(ListIterator<?> iterator) {
		List<Role> roles = (List<Role>) iterator.next();
		for (Role role : roles) {
			id_roles.put(role.getDictId(), role);
			role.load();
		}

	}
	@Override
	public void initFromDbAfter() {

	};
	private Map<Integer, RoleTagQuest> getRoleTagQuestMap(int roleDictId) {
		Map<Integer, RoleTagQuest> map = this.roleTagQuests.get(roleDictId);
		if (map == null) {
			map = new HashMap<>();
			this.roleTagQuests.put(roleDictId, map);
		}
		return map;
	}

	private Multimap<Integer, RoleAction> getRoleActionMap(int roleId) {
		Multimap<Integer, RoleAction> ac = this.roleActions.get(roleId);
		if (ac == null) {
			ac = ArrayListMultimap.create();
			this.roleActions.put(roleId, ac);
		}
		return ac;
	}

	@Override
	public void updateSelective(Role role) {

		DAO.execute(RoleMapper.class,
				MapperConstant.updateByPrimaryKeySelective, role);
	}

	@Override
	public void update(Role role) {
		// 探索角色在探索数据中更新,共有属性保存到role
		role.save();
		DAO.execute(RoleMapper.class, MapperConstant.updateByPrimaryKey,
				role);
	}

	@Override
	public void updateWithBlob(Role role) {
		DAO.execute(RoleMapper.class,
				MapperConstant.updateByPrimaryKeyWithBLOBs, role);
	}

	@Override
	public void del(int id) {

		long uid = get(id).getId();
		DAO.execute(RoleMapper.class, MapperConstant.deleteByPrimaryKey,
				uid);
		this.id_roles.remove(id);
	}

	@Override
	public void add(int id, Role role) {
		this.id_roles.put(id, role);
	}

	@Override
	public RewardItem newRole(int roleId) {
		RoleConfig roleConfig = RoleManager.getInstance().getRoleConfig(roleId);
		if (roleConfig == null) {
			throw new IllegalArgumentException("role id 不存在" + roleId);
		}
//		Player player =  PlayerManager.getInstance().getPlayer(playerId);
		// 已经有的伙伴转化为碎片
		RoleOp roleOp = player.getModule(RoleOp.class);
		if (exist(roleId)) {

			Role r = roleOp.get(roleId);
			int fragment = roleConfig.getStarFragment();
			int star = r.getStar();
			RoleRisingStarConfig roleRisingStarConfig = RoleRisingStarManager.getInstance()
					.getRoleRisingStarConfig(star);
			int count = roleRisingStarConfig.getCost();
			ItemModule itemModule = player.getModule(ItemModule.class);
//			itemModule.addToRepository(fragment, count);
			return RewardItem.valueOf(fragment, count);
		}
		Role role = new Role();

		role.setDictId(roleId);
		role.setExp(0);
		role.setLevel(1);
		role.setStar(roleConfig.getStar());
		role.setBreakLevel(0);
		role.setPlayerId(playerId);
		role.setIntimacy(0);
		role.setIntimacyLevel((byte) 1);
		role.setPromise(false);
		role.setMechalevel(0);
		role.setGetTime(System.currentTimeMillis());
		role.setSkin(0);
		role.setSoulweaponLevel(1);

		role.setEp(roleConfig.getEp());
		role.setSan(0);
		role.setState((byte) 0);
		role.setSkillPoint(0);
		role.setLineupId(0);
		role.setIsLock(false);

		role.setExploreTime(0L);
		role.setPromotionLevel((byte) 1);
		role.setPromotionPoint(0);
		
		// 初始化主动技能
		// 普通攻击
		int normalSkill = roleConfig.getNormalSkill().get(0);
		role.addSkills(normalSkill);
		// EP技能
		int epSkill = roleConfig.getEpSkill().get(0);
		role.addSkills(epSkill);
		// AP技能
		int apSkill = roleConfig.getApSkill().get(0);
		role.addSkills(apSkill);

		// 初始化使用中的技能
		role.addSkillsUsed(normalSkill);
		role.addSkillsUsed(epSkill);
		role.addSkillsUsed(apSkill);
		// 被动技能,默认装备(格式是list,但只会有一个元素)
		List<Integer> passiveSkillList = roleConfig.getPassiveSkillList();
		role.addSkillsUsed(passiveSkillList.get(0));
		
		id_roles.put(role.getDictId(), role);
		// 添加固有装备
		List<Integer> equipmentId = roleConfig.getEquipmentId();
//		for (int i = 0; i < equipmentId.size(); i++) {
//			EquipModule equipOp = player.getModule(EquipModule.class);
//			equipOp.addRoleFixedEquip(equipmentId.get(i), roleId, i + 1);
//		}

		if (passiveSkillList.size() > 0) {
			role.addSkillsUsed(passiveSkillList.get(0));
		}

		// 前端显示用
		// role.addSkills(roleConfig.getTalentSkill());
		// 初始化礼物卡
		role.setGiftCardUsed((byte) 0);
		// 解锁礼物卡
		byte flag = 0;
		List<List<Integer>> cards = roleConfig.getGiftCards();
		for (int index = 0; index < cards.size(); index++) {
			List<Integer> list = cards.get(index);
			int friendly = list.get(2);
			if (friendly == 0) {
				flag = (byte) ByteHelp.modifyBit(flag, index);
			}
		}
		role.setGiftCardUnlock(flag);

		// 如果是佣兵随机名字
		if (roleConfig.getType() == 1) {
			List<MercenaryNameConfig> nameIdList = MercenaryNameManager.getInstance().getNameIdList(roleId);
			if (nameIdList != null && nameIdList.size() > 0) {
				int index = Rnd.get(0, nameIdList.size() - 1);
				role.setNameId(nameIdList.get(index).getId());
			}

			List<RoleTagConfig> typeoccupationList = RoleTagManager.getInstance().getType3List(RoleTagEnum.SAN);
			if (typeoccupationList != null && typeoccupationList.size() > 0) {
				int index = Rnd.randomWeighableIndex(typeoccupationList);
				RoleTagConfig roleTagConfig = typeoccupationList.get(index);
				role.addTag(roleTagConfig.getId());
			}
		} else {
			role.setNameId(0);
			role.addTag(roleConfig.getSanValueTag());
		}
		
		// 增加基础标签
		List<RoleTagConfig> typeoccupationList = RoleTagManager.getInstance().getType3List(RoleTagEnum.BASE);
		if (typeoccupationList != null && typeoccupationList.size() > 0) {
			List<Integer> index = Rnd.randomWeighableIndexsNonRepeating(typeoccupationList, 2);
			for (Integer i : index) {
				RoleTagConfig roleTagConfig = typeoccupationList.get(i);
				role.addTag(roleTagConfig.getId());
			}
		}
		
		// TODO 如果有可继承标签，就继承。
		UserOp userOp = player.getModule(UserOp.class);
		List<Integer> tagIds = userOp.getTagIds();
		for (Integer integer : tagIds) {
			RoleTagConfig roleTagConfig = RoleTagManager.getInstance().getRoleTagConfig(integer);
//			if (roleTagConfig) {
//				
//			}
		}
		// 初始化历程标签事件
		typeoccupationList = RoleTagManager.getInstance().getTypeList(3);
//		if (typeoccupationList != null && typeoccupationList.size() > 0) {
//			for (RoleTagConfig roleTagConfig : typeoccupationList) {
//				RoleTagQuest tagQuest = RoleTagQuest.valueOf(this.playerId, roleId, roleTagConfig.getId());
//				Map<Integer, RoleTagQuest> roleTagQuestMap = getRoleTagQuestMap(roleId);
//				roleTagQuestMap.put(roleTagConfig.getId(), tagQuest);
//				tagQuest.initCondition();
//				insert(tagQuest);
//			}
//		}
		
		// 初始天赋树,暂时没有天赋树了
//		List<OccupationLevelTreeConfig> roleIdList = OccupationLevelTreeManager.getInstance().getRoleIdList(roleId);
//
//		for (OccupationLevelTreeConfig occupationLevelTreeConfig : roleIdList) {
//			if (occupationLevelTreeConfig.getLevelLimit() <= role.getLevel()) {
//				role.addTreeId(occupationLevelTreeConfig.getId());
//			}
//		}

		

		PropertyOp propertyOp = player.getModule(PropertyOp.class);
		int hp = propertyOp.getProperty(role.getDictId(), AttributeTypeEnum.hp);
		role.setHp(hp);
		role.setHpCurMax(hp);

		long id = IdUtil.getIdAutoIncrease(IdType.HERO);
		role.setId(id);

		role.save();
		DAO.execute(RoleMapper.class, MapperConstant.insert, role);

		player.handleEvent(new GameEvent(EventTypeEnum.Role, roleConfig.getId()));

		return RewardItem.valueOf(role);
	}

	/**
	 * 检查历程类标签成就,达到上限停止积累 
	 * 历程标签上限6个
	 * 历程标签的子分类有数量限制
	 * @param roleDictId
	 * @param tagId
	 * @return
	 */
	public boolean checkTagAchieve(int roleDictId, int tagId) {
		RoleTagConfig tagConfig = RoleTagManager.getInstance().getRoleTagConfig(tagId);
		int num = getTagNum(roleDictId, tagConfig.getType());
		// 历程标签最多6个
		if (num >= 6) {
			System.err.println("历程类标签达到上限,停止积累");
			stopAll(roleDictId, 3);
			return true;
		}
		// 每种标签有数量限制
		RoleTagEnum type3 = tagConfig.getType3();
		int max = type3.getMax();
		num = getTagNum(roleDictId, tagConfig.getType3());
		if (num >= max) {
			System.err.println(type3+"类标签达到上限,停止积累");
			stopAll(roleDictId, type3);
			return true;
		}
		return false;
	}

	/**
	 * 停止积累标签成就
	 * @param roleDictId
	 * @param type3
	 */
	private void stopAll(int roleDictId, RoleTagEnum type3) {
		Map<Integer, RoleTagQuest> map = this.roleTagQuests.get(roleDictId);
		List<Integer> dels = new ArrayList<>();
		for (RoleTagQuest quest : map.values()) {
			Integer tagId = quest.getTagId();
			RoleTagConfig tagConfig = RoleTagManager.getInstance().getRoleTagConfig(tagId);
			if (tagConfig.getType3() == type3) {
				stop(quest);
				dels.add(tagId);
			}
		}
		for (Integer tag : dels) {
			map.remove(tag);
		}
	}

	/**
	 * 停止积累标签成就
	 * @param roleDictId
	 * @param tagType
	 */
	private void stopAll(int roleDictId, int tagType) {
		Map<Integer, RoleTagQuest> map = this.roleTagQuests.get(roleDictId);
		List<Integer> dels = new ArrayList<>();
		for (RoleTagQuest quest : map.values()) {
			Integer tagId = quest.getTagId();
			RoleTagConfig tagConfig = RoleTagManager.getInstance().getRoleTagConfig(tagId);
			if (tagConfig.getType() == tagType) {
				stop(quest);
				dels.add(tagId);
			}
		}
		for (Integer tag : dels) {
			map.remove(tag);
		}
	}

	private void stop(RoleTagQuest quest) {
		quest.unregEvent();
		delete(quest);
	}

	private int getTagNum(int roleDictId, int type) {
		int res = 0;
		Role role = this.id_roles.get(roleDictId);
		List<Integer> tagList = role.getTagList();
		for (int tagId : tagList) {
			RoleTagConfig tagConfig = RoleTagManager.getInstance().getRoleTagConfig(tagId);
			if (tagConfig.getType() == type) {
				res++;
			}
		}
		return res;
	}

	private int getTagNum(int roleDictId, RoleTagEnum roleTagEnum) {
		int res = 0;
		Role role = this.id_roles.get(roleDictId);
		List<Integer> tagList = role.getTagList();
		for (int tagId : tagList) {
			RoleTagConfig tagConfig = RoleTagManager.getInstance().getRoleTagConfig(tagId);
			if (tagConfig.getType3() == roleTagEnum) {
				res++;
			}
		}
		return res;
	}

	@Override
	public Collection<Role> list() {

		return this.id_roles.values();
	}

	@Override
	public boolean exists(int configId) {

		for (Role role : this.id_roles.values()) {
			if (role.getDictId() == configId) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Role addExp(Role role, int exp) {

		Player player = PlayerManager.getInstance().getPlayer(playerId);

		int curExp = role.getExp() + exp;

		RoleExpConfig expConfig = RoleExpManager.getInstance().getRoleExpConfig(role.getLevel());
		while (expConfig != null && curExp >= expConfig.getExp() && role.getLevel() < player.getData().getLevel()
				&& role.getLevel() < OldGlobalConst.roleLevelMax) {

			role.setExp(curExp - expConfig.getExp());
			role.setLevel(role.getLevel() + 1);
			curExp = role.getExp();
			expConfig = RoleExpManager.getInstance().getRoleExpConfig(role.getLevel());

			
			player.handleEvent(new GameEvent(EventTypeEnum.RoleLevelUp, null, role.getDictId()));

		}
		role.setExp(curExp);
		update(role);
		return role;
	}

	@Override
	public Role addLevel(int id, int level) {

		Role role = get(id);
		if (role.getLevel() >= OldGlobalConst.roleLevelMax) {
			return role;
		}

		
		player.handleEvent(new GameEvent(EventTypeEnum.RoleLevelUp, null, role.getDictId()));
		role.setLevel(role.getLevel() + level);
		update(role);
		return role;
	}

	@Override
	public Role addExp(int roleId, int exp) {

		Role role = this.id_roles.get(roleId);
		if (role == null) {
			return null;
		}
		return addExp(role, exp);
	}

	@Override
	public int size() {

		return this.id_roles.size();
	}

	@Override
	public Role get(int configId) {
		Role role = this.id_roles.get(configId);
		if (role == null) {
			throw new IllegalArgumentException("Role not exist  ： " + configId);
		}
		return role;
	}

	@Override
	public boolean exist(int configId) {
		Role role = this.id_roles.get(configId);
		return role != null;
	}

	@Override
	public Role starLevelup(int id) {

		return null;
	}

	@Override
	public int getCountByLevel(int level) {
		int count = 0;
		for (Role e : this.id_roles.values()) {
			if (e.getLevel() >= level) {
				count++;
			}
		}
		return count;
	}

	@Override
	public int getCountByStar(int star) {
		int count = 0;
		for (Role e : id_roles.values()) {
			RoleConfig roleConfig = RoleManager.getInstance().getRoleConfig(e.getDictId());
//			if (roleConfig.getRoleCat() == 2) {
//				continue;
//			}
			if (e.getStar() >= star) {
				count++;
			}
		}
		return count;
	}

	@Override
	public int getCountByBreakLevel(int breakLevel) {
		int count = 0;
		for (Role e : this.id_roles.values()) {
			if (e.getBreakLevel() >= breakLevel) {
				count++;
			}
		}
		return count;
	}

	@Override
	public Role addIntimacy(int roleId, int intimacy) {
		Role role = get(roleId);
		int curIntimacy = role.getIntimacy() + intimacy;

		FriendlyConfig friendlyConfig = FriendlyManager.getInstance().getFriendlyConfig(role.getIntimacyLevel());
		FriendlyConfig nextConfig = FriendlyManager.getInstance().getFriendlyConfig(role.getIntimacyLevel() + 1);
		while (friendlyConfig != null && curIntimacy >= friendlyConfig.getFriendlyValue() && nextConfig != null) {

			role.setIntimacy(curIntimacy - friendlyConfig.getFriendlyValue());
			role.setIntimacyLevel((byte) (role.getIntimacyLevel() + (byte) 1));
			curIntimacy = role.getIntimacy();
			friendlyConfig = nextConfig;
			nextConfig = FriendlyManager.getInstance().getFriendlyConfig(role.getIntimacyLevel() + 1);
//			
//			player.handleEvent(new GameEvent(EventTypeEnum.LevelUp, null, role.getDictId()));
		}
		role.setIntimacy(curIntimacy);
		update(role);
		return role;
	}

	@Override
	public boolean addAllRoleIntimacy(int intimacy) {
		for (int roleId : id_roles.keySet()) {
			addIntimacy(roleId, intimacy);
		}

		return true;
	}

	@Override
	public int getAttr(Role role, AttributeTypeEnum type, AttributeSubTypeEnum subType) {
		/*
		 * Role role = get(confId); if (role == null) { return -1; }
		 */

		switch (type) {
		case hp:
			if (subType == AttributeSubTypeEnum.cur) {
				return role.getHp();
			} else if (subType == AttributeSubTypeEnum.curTotal) {
				return role.getHpCurMax();
			}
//
//		case ep:
//			return role.getEp();

		case san:
			return role.getSan();

		default:
			return -1;
		}
	}

	@Override
	public int getAttrMax(Role role, AttributeTypeEnum type) {
		return getAttrMax(role, type, AttributeSubTypeEnum.cur);
	}
	
	@Override
	public int getAttrMax(Role role, AttributeTypeEnum type, AttributeSubTypeEnum subType) {
		/*
		 * Role role = get(confId); if (role == null) { return -1; }
		 */
		PropertyOp propertyOp = player.getModule(PropertyOp.class);
		int roleId = role.getDictId();
		int max = -1;

		switch (type) {

		case hp:
			if (subType == AttributeSubTypeEnum.cur) {
				return role.getHpCurMax();
				
			} else if (subType == AttributeSubTypeEnum.curTotal) {
				return propertyOp.getProperty(roleId, AttributeTypeEnum.hp);

			} else if (subType == AttributeSubTypeEnum.total) {
				return propertyOp.getProperty(roleId, type);
			}

//		case ep:
//			max = RoleManager.getInstance().getRoleConfig(role.getDictId()).getEp();
//			return max;

		case san:
			if (subType == AttributeSubTypeEnum.cur) {
				return role.getSanCurMax();
			} else{
				return propertyOp.getProperty(roleId, AttributeTypeEnum.san);
			}

		default:
			return -1;
		}
	}
	
	@Override
	public boolean setAttr(int roleId, AttributeTypeEnum type, int value, boolean updateDb) {
		return setAttr(roleId, type, AttributeSubTypeEnum.cur, value, updateDb);
	}
	
	@Override
	public boolean setAttr(int roleId, AttributeTypeEnum type, AttributeSubTypeEnum subType, int value, boolean updateDb) {
		Role role = get(roleId);

		int max = getAttrMax(role, type, subType);
		if (value > max) {
			value = max;
		}
		if (value < 0) {
			value = 0;
		}
		switch (type) {
		case hp:
			if (subType == AttributeSubTypeEnum.curTotal) {
				log.debug(playerId + ",RoleOp setHpCurMax()," + roleId + ",hpCurMax=" + value);
				role.setHpCurMax(value);
				break;
			}
			BuffOp buffOp = player.getModule(BuffOp.class);
			
			Byte state = role.getState();
			/*	if (value == 0) {
					if (state == (byte) RoleState.NORMAL_VALUE) {
						// 置为濒死状态
						role.setState((byte) RoleState.NEAR_DEATH_VALUE);
						role.setHp(1);
						// 增加一层创伤buff
						
					} else {
						// 置为死亡状态
						role.setState((byte) RoleState.DIE_VALUE);
						role.setHp(0);
						role.setSan(0);
					} 
					
				} else {
					role.setHp(value);
					if (state == RoleState.NEAR_DEATH_VALUE && value > 1) {
						role.setState((byte) RoleState.NORMAL_VALUE);
					}
				}*/
			break;

//			case ep:
//				role.setEp(value);
//				break;

		case san:
			if (subType == AttributeSubTypeEnum.cur) {
				
				role.setSan(value);
				if(value == 0){
				}
				
			}else if (subType == AttributeSubTypeEnum.curTotal) {
				role.setSanCurMax(value);
			}
			break;

		default:
			return false;
		}
		if (updateDb) {
			update(role);
		}
		return true;
	}

	@Override
	public boolean incrAttr(int roleId, AttributeTypeEnum type, float incr, boolean updateDb) {
		return changeAttr(roleId, type.getId(), AttributeSubTypeEnum.cur.getId(), BuffValue.CHANGE_BY_VALUE, incr, updateDb);
	}

	@Override
	public boolean decrAttr(int roleId, AttributeTypeEnum type, float decr, boolean updateDb) {
		return changeAttr(roleId, type.getId(), AttributeSubTypeEnum.cur.getId(), BuffValue.CHANGE_BY_VALUE, -decr, updateDb);
	}

	@Override
	public boolean changeAttr(int roleId, AttributeTypeEnum type, float change, boolean updateDb) {
		return changeAttr(roleId, type.getId(), AttributeSubTypeEnum.cur.getId(), BuffValue.CHANGE_BY_VALUE, change, updateDb);
	}
	
	@Override
	public boolean changeAttrByPercent(int roleId, AttributeTypeEnum type, float percentChange, boolean updateDb) {
		return changeAttr(roleId, type.getId(), AttributeSubTypeEnum.cur.getId(), BuffValue.CHANGE_BY_MAX_PERCENT, percentChange, updateDb);
	}
	
	@Override
	public boolean changeAttr(int roleId, AttributeTypeEnum type, int mode, float change, boolean updateDb) {
		return changeAttr(roleId, type.getId(), AttributeSubTypeEnum.cur.getId(), mode, change, updateDb);
	}

	@Override
	public boolean changeAttr(int roleId, int attrId, int attrSubId, int mode, float change, boolean updateDb) {
		Role role = get(roleId);
		AttributeTypeEnum attrType = AttributeTypeEnum.get(attrId);
		AttributeSubTypeEnum attrSubType = AttributeSubTypeEnum.get(attrSubId);
		
		int attr = getAttr(role, attrType, attrSubType);
		if (attr == -1) {
			return false;
		}
		
		int max = getAttrMax(role, attrType, attrSubType);
		change = calcChange(roleId, attrId, attr, max, mode, change);
		
		// 血量特殊处理 要走污染值公式

		int newValue;
		int newAttr = (newValue = Math.round(attr + change)) > max ? max : newValue;
		setAttr(roleId, attrType, attrSubType, newAttr, updateDb);
		if (attr != newAttr) {
//			exploreOp.pushExploreAttrRole(role);
		}
		return true;
	}

	/**
	 * 计算按百分比改变的值
	 * @param roleId
	 * @param attrId 	大属性id
	 * @param attrValue 属性值
	 * @param max 		属性最大值
	 * @param mode
	 * @param change
	 * @return
	 */
	private float calcChange(int roleId, int attrId, int attrValue, int max, int mode, float change) {
		PropertyOp propertyOp = player.getModule(PropertyOp.class);
		
		switch (mode) {
			case BuffValue.CHANGE_BY_VALUE:
				return change;
				
			case BuffValue.CHANGE_BY_PANEL_PERCENT:
				int panelValue = propertyOp.getPanelValue(roleId, attrId);
				change = Math.round(panelValue * (change / 100f));
				return change;
				
			case BuffValue.CHANGE_BY_CUR_PERCENT:
				change = Math.round(attrValue * (change / 100f));
				return change;
				
			case BuffValue.CHANGE_BY_BODY_PERCENT:
				int bodyValue = propertyOp.getRolePropTypeEnumValue(roleId, RolePropFromType.BASE, attrId);
				change =  Math.round(bodyValue * (change / 100f));
				return change;
				
			case BuffValue.CHANGE_BY_MAX_PERCENT:
				change = Math.round(max * (change / 100f));
				return change;

			default:
				throw new IllegalArgumentException("未定义的数值改变模式:" + mode);
		}

	}

	@Override
	public boolean setAllRoleAttrByPercent(AttributeTypeEnum type, float percent, boolean updateDb) {
		if (percent < 0 || percent > 100) {
			return false;
		}

		boolean succ = false;
		for (Role role : id_roles.values()) {
			succ = setRoleAttrByPercent(role.getDictId(), type, percent, updateDb);
		}

		if (succ) {
			return true;
		}
		return false;

	}

	/**
	 * 获取将要加的按百分比的值
	 * @param role
	 * @param type
	 * @param percent
	 * @return
	 */
	public int getAttrNewValue(Role role, AttributeTypeEnum type, float percent) {
		int max = getAttrMax(role, type, AttributeSubTypeEnum.cur);
		// 比例
		float proportion = percent / 100F;
		int newValue = Math.round(max * proportion);
		return newValue;
	}

	@Override
	public boolean setRoleAttrByPercent(int confId, AttributeTypeEnum type, float percent, boolean updateDb) {
		if (percent < 0 || percent > 100) {
			return false;
		}
		Role role = get(confId);
		if (role == null) {
			return false;
		}
		int newValue = getAttrNewValue(role, type, percent);

		return setAttr(confId, type, AttributeSubTypeEnum.cur, newValue, updateDb);
	}

	@Override
	public boolean changeAllRoleAttr(AttributeTypeEnum type, float change, boolean updateDb) {

		for (Role role : id_roles.values()) {
			changeAttr(role.getDictId(), type, change, updateDb);
		}
		return true;
	}

	@Override
	public boolean changeAllRoleAttrByPercent(AttributeTypeEnum type, float percent, boolean updateDb) {
		if (percent < -100) {
			percent = -100;
		}
		if (percent > 100) {
			percent = 100;
		}
		for (Role role : id_roles.values()) {
			int confId = role.getDictId();
			int max = getAttrMax(role, type, AttributeSubTypeEnum.cur);
			int cur = getAttr(role, type, AttributeSubTypeEnum.cur);
			// 比例
			float proportion = percent / 100F;
			int value = Math.round(proportion * max + cur);

			setAttr(confId, type, AttributeSubTypeEnum.cur, value, updateDb);
		}

		return true;
	}

	@Override
	public boolean changeRoleLineupAttrByPercent(AttributeTypeEnum type, int lineupId, boolean isAdd, float percent) {
		if (percent < -100) {
			percent = -100;
		}
		if (percent > 100) {
			percent = 100;
		}
		float finalPercent = percent;
		return true;
	}

	@Override
	public boolean changeRoleLineupAttr(AttributeTypeEnum type, int lineupId, boolean isAdd, float value) {
		return true;
	}

	@Override
	public boolean changeAttr(int roleId, List<List<Integer>> effects, boolean updateDb) {
		Role role = get(roleId);
		if (role == null) {
			return false;
		}
		for (List<Integer> effect : effects) {
			int attrId = effect.get(0);
			int mode = effect.get(1);
			int value = effect.get(2);
			changeAttr(roleId, attrId, AttributeSubTypeEnum.cur.getId(), mode, value, false);
		}
		if (updateDb) {
			update(role);
		}
		return true;
	}

	@Override
	public void updateRoleAction(int roleId, RoleActionType actionType, OldConditionTypeEnum type, int subType,
			int... args) {
		Multimap<Integer, RoleAction> roleActionMap = getRoleActionMap(roleId);
		Collection<RoleAction> collection = roleActionMap.get(type.getId());
		RoleAction ac = queryAction(collection, type.getId(), subType, args);
		boolean insert = ac == null;
		Class mapper = null;
		if (actionType == RoleActionType.Battle) {
			mapper = RoleActionBattleMapper.class;
			if (insert) {
//				ac = new RoleActionBattle(playerId, roleId, type.getId(), subType, args);
			}
		} else if (actionType == RoleActionType.Common) {
			mapper = RoleActionMapper.class;
			if (insert) {
//				ac = new RoleAction(playerId, roleId, type.getId(), subType, args);
			}
		}
		if (insert) {
//			DAO.insert(mapper, ac);
			roleActionMap.put(type.getId(), ac);
		} else {
			ac.setCount(ac.getCount() + 1);
//			DAO.update(mapper, ac);
		}
	}

	private RoleAction queryAction(Collection<RoleAction> collection, int type, int subType, int... args) {

		for (RoleAction ac : collection) {
			if (ac.getType() == type && ac.getSubType() == subType) {
//				if (ac instanceof RoleActionBattle) {
//					RoleActionBattle acb = (RoleActionBattle) ac;
//					if (acb.getRoleType() == args[0]) {
//						return acb;
//					}
//
//				} else {
//					return ac;
//				}
			}

		}
		return null;
	}

	/**
	 * 角色升级,按照权重随机增加属性点
	 * 
	 * @param roleId
	 * @return 本次升级增加的属性点 key:属性枚举表id ; value:增加的属性点
	 */
	public Map<Integer, Integer> levelUpAddRandomAttrPoint(int roleId) {
		Role role = get(roleId);
		RoleConfig roleConfig = RoleManager.getInstance().getRoleConfig(roleId);
		List<Entry<Integer, Integer>> attributeWeight = roleConfig.getAttributeWeight();
		Map<Integer, Integer> res = new HashMap<>(attributeWeight.size());
		// 要分配上的属性的权重和
		int weightSum = 0;
		for (Entry<Integer, Integer> entry : attributeWeight) {
			weightSum += entry.getValue();
		}
		// 按权重分配属性点数
		for (int i = 0; i < roleConfig.getAttributePoint(); i++) {
			int random = Rnd.get(0, weightSum);
			int sum = 0;
			for (Entry<Integer, Integer> entry : attributeWeight) {
				Integer attrId = entry.getKey();
				Integer value = entry.getValue();
				sum += value;
				if (sum >= random) {
					MapUtil.addItemCounts(res, attrId, 1);
					break;
				}
			}
		}
		role.addAttrPoint(res);
		return res;
	}

	@Override
	public int getRoleAttrPoint(int roleId, AttributeTypeEnum type) {
		Role role = get(roleId);
		if (role == null) {
			return 0;
		}
		Map<Integer, Integer> attrPointAddMap = role.getAttrPointAddMap();
		Integer point = attrPointAddMap.get(type.getId());

		return point == null ? 0 : point;
	}

	@Override
	public void handleEvent(GameEvent event) {

		switch (event.getType()) {
		default:
			break;
		}
	}

	@Override
	public boolean hasTag(int roleId, RoleTagEnum type) {
		Role role = get(roleId);
		List<Integer> tagList = role.getTagList();
		for (Integer tagId : tagList) {
			RoleTagConfig tagConfig = RoleTagManager.getInstance().getRoleTagConfig(tagId);
			if (tagConfig.getType3() == type) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int getTagAvailable(int roleId, RoleTagEnum type) {
		Role role = get(roleId);
		List<Integer> tagList = role.getTagList();
		RoleConfig roleConfig = RoleManager.getInstance().getRoleConfig(roleId);
		List<RoleTagConfig> envTags = RoleTagManager.getInstance().getType3List(type);
		List<RoleTagConfig> canGet = new ArrayList<>();
		tagloop: for (RoleTagConfig tagConfig : envTags) {
			if (tagConfig.getType() == 3) { // 历程标签
				int maxCount = RoleHelper.getRoleTagCount(tagConfig.getType2());
				int id = tagConfig.getType3().getId();
				int type2Count = 0;
				for (Integer tid : tagList) {
					if (id == tid) {
						continue tagloop;
					}
					RoleTagConfig own = RoleTagManager.getInstance().getRoleTagConfig(tid);
					if (own.getType3() == tagConfig.getType3()) {
						continue tagloop;
					}
					if (own.getType2() == tagConfig.getType2()) {
						type2Count++;
						if (type2Count >= maxCount) {
							continue tagloop;
						}
					}
				}
			}
			boolean check = PlayerHelper.checkCondition(playerId, tagConfig.getCondition(), new GameEvent(roleId));
			if (check) {
				canGet.add(tagConfig);
			}
		}
		if (canGet.isEmpty()) {
			return 0;
		}
		return canGet.get(Rnd.randomWeighableIndex(canGet)).getId();
	}

	@Override
	public void addTag(int roleId, int tagId) {
		if (tagId == 0) {
			return;
		}
		Role role = get(roleId);
		boolean flag = role.addTag(tagId);
		if (!flag) {
			return;
		}

		RoleTagConfig roleTagConfig = RoleTagManager.getInstance().getRoleTagConfig(tagId);
		if (roleTagConfig.getType3() == RoleTagEnum.INHERIT) {

		} else {

			Role update = Role.valueOf(role.getId());
			update.setTags(role.getTags());
			DAO.updateSelective(update);
		}

//		PlayerHelper.sendProtcol(playerId,
//				RoleTagPush_02001001.newBuilder().setRoleId(role.getDictId()).setTagId(tagId).build());
	}

	public void update(RoleTagQuest quest) {
		DAO.execute(RoleTagQuestMapper.class,
				MapperConstant.updateByPrimaryKeyWithBLOBs, quest);
	}

	public void insert(RoleTagQuest quest) {
		DAO.execute(RoleTagQuestMapper.class, MapperConstant.insert,
				quest);
	}

	public void delete(RoleTagQuest quest) {
		DAO.execute(RoleTagQuestMapper.class,
				MapperConstant.deleteByPrimaryKey, quest);
	}

	@Override
	public boolean checkActionCount(int roleId, RoleActionType actionType, OldConditionTypeEnum type, int subType,
			int count, int... args) {
		Multimap<Integer, RoleAction> actions = this.roleActions.get(roleId);
		if (actions == null) {
			return false;
		}
		Collection<RoleAction> collection = actions.get(type.getId());
		for (RoleAction roleAction : collection) {
			if (actionType == RoleActionType.Battle) {
//				RoleActionBattle acb = (RoleActionBattle) roleAction;

			} else if (actionType == RoleActionType.Common) {
				if (roleAction.getSubType() == subType) {
					if (roleAction.getCount() >= count) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void exploreEndResetRole() {
		List<Integer> dels = new ArrayList<>();
		for (Role role : id_roles.values()) {

			// 本体
			role.setLevel(1);// 等级
			role.setSan(getAttrMax(role, AttributeTypeEnum.san, AttributeSubTypeEnum.cur));// SAN值

			role.getAttrPointAddMap().clear();// 属性点
			role.setAttrPointAdd("");// 属性点
			role.setSkillPoint(0);// 技能点
			role.resetSkill();// 重置技能
			role.resetGiftCard();// 重置礼物卡

			update(role);
		}

		for (int roleUid : dels) {
			id_roles.remove(roleUid);
		}
	}

	@Override
	public int getTalentSkill(int roleId) {
		Role role = get(roleId);
		if (role == null) {
			return -1;
		}
		Set<Integer> skills = role.getSkillsUsedSet();
		for (int skillId : skills) {
			OldSkillConfig skillConfig = OldSkillManager.getInstance().getSkillConfig(skillId);
			if (skillConfig.getType() == RoleConstant.TALENT_SKILL) {
				return skillId;
			}
		}
		return 0;
	}

	@Override
	public int occupationTalentUnlock(int id) {
		OccupationTalentNodeConfig config = OccupationTalentNodeManager.getInstance().getOccupationTalentNodeConfig(id);
		int preNode = config.getPreNode();
		PlayerExt playerExt = PlayerManager.getInstance().getPlayer(playerId).getExt();
		List<Integer> occTalentNodes = playerExt.getOccTalentNodes();
		if (occTalentNodes.contains(id)) {
			return OldErrorMsgEnum.illegal_request.getId();
		}
		if (preNode != 0 && !occTalentNodes.contains(preNode)) {
			return OldErrorMsgEnum.unlock.getId();
		}
		List<Map.Entry<Integer, Integer>> cost = config.getCost();
		if (!PlayerHelper.delResources(playerId, cost, ResourceConsumeEnum.OccupationTalentUnlock)) {
			return OldErrorMsgEnum.resource_not_enough.getId();
		}
		// 开启天赋技能
		if (!putOccTalentSkill(id)) {
			return OldErrorMsgEnum.lock_error.getId();
		}

		playerExt.addOccTalentNode(id);

		PlayerExt update = PlayerExt.valueOf(playerId);
		update.setOcctalentNode(playerExt.getOcctalentNode());
		DAO.updateSelective(update);

		return OldErrorMsgEnum.ok.getId();
	}

	private boolean putOccTalentSkill(int talentNodeId) {
		OccupationTalentNodeConfig config = OccupationTalentNodeManager.getInstance()
				.getOccupationTalentNodeConfig(talentNodeId);
		int occupation = config.getOccupation();
		int[] unlockTalentSkill = config.getUnlockTalentSkill();
		Multimap<Integer, Integer> integerMultimap = occTalentSkills.get(occupation);
		if (integerMultimap == null) {
			integerMultimap = ArrayListMultimap.create();
			occTalentSkills.put(occupation, integerMultimap);
		}
		OldSkillConfig skillConfig = OldSkillManager.getInstance().getSkillConfig(unlockTalentSkill[0]);
		if (occupation != skillConfig.getOccupation()) {
			return false;
		}
		List<Integer> strengthenBuff = skillConfig.getStrengthenBuff();
		if (unlockTalentSkill[1] == -1) {
			integerMultimap.put(unlockTalentSkill[0], unlockTalentSkill[1]);
		} else {
			OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(strengthenBuff.get(unlockTalentSkill[1]));
			integerMultimap.put(unlockTalentSkill[0], buffConfig.getId());
			list().forEach(role-> {
				int talentSkill = getTalentSkill(role.getDictId());
				if (unlockTalentSkill[0] == talentSkill) {
					List<Long> buffTargets = new ArrayList<>();
					buffTargets.add(Long.valueOf(role.getDictId()));
					BuffHelper.addBuff(playerId, buffConfig.getId(), buffTargets, true);
				}
			});
		}
		return true;
	}

	public Multimap<Integer, Integer> getOccTalentSkillsById(int occuptionId) {
		return this.occTalentSkills.get(occuptionId);
	}

	public Map<Integer, Multimap<Integer, Integer>> getOccTalentSkills() {
		return occTalentSkills;
	}

	@Override
	public boolean useGiftCard(int roleDictId, int giftId) {
		Role role = get(roleDictId);
		if (role == null) {
			return false;
		}
		// 礼物卡未解锁
		if (!role.isGiftCardUnlock(giftId)) {
			return false;
		}
		// 礼物卡已经使用了
		if (role.isGiftCardUsed(giftId)) {
			return false;
		}
		int location = RoleHelper.getGiftCardLocation(roleDictId, giftId);

		if (location == -1) {
			return false;
		}
		int modifyBit = ByteHelp.modifyBit(role.getGiftCardUsed(), location);
		role.setGiftCardUsed((byte) modifyBit);

		Role update = new Role();
		update.setId(role.getId());
		update.setGiftCardUsed(role.getGiftCardUsed());
		updateSelective(update);

		return true;
	}

	@Override
	public Set<Integer> unlockGiftCard(int roleId, int friendId, byte friendly) {
		Set<Integer> res = new HashSet<>();

		Role role = get(roleId);
		RoleConfig roleConfig = RoleManager.getInstance().getRoleConfigNullable(roleId);
		List<List<Integer>> giftCards = roleConfig.getGiftCards();
		for (int i = 0; i < giftCards.size(); i++) {
			List<Integer> list = giftCards.get(i);
			// 礼物卡:好友角色id:好感度
			int cardId = list.get(0);
			int targetFriendId = list.get(1);
			int targetFriendly = list.get(2);

			if (friendId != targetFriendId) {
				continue;
			}
			if (friendly < targetFriendly) {
				continue;
			}
			// 已经解锁过了
			int index = i - RoleConstant.GIFT_CARD_DEFAULT_UNLOCK;
			if (ByteHelp.isOne(role.getGiftCardUnlock(), index)) {
				continue;
			}
			int modifyBit = ByteHelp.modifyBit(role.getGiftCardUnlock(), index);
			role.setGiftCardUnlock((byte) modifyBit);
			res.add(cardId);
		}
		return res;
	}

	@Override
	public void resetGiftCardUse() {
		for (Role role : id_roles.values()) {
			if (role.getGiftCardUsed() == 0) {
				continue;
			}
			role.setGiftCardUsed((byte) 0);
			Role update = new Role();
			update.setId(role.getId());
			update.setGiftCardUsed((byte) 0);
			updateSelective(update);
		}
	}

	/**
	 *
	 * @param roleDictId
	 * @param canUsedOrUnlock 可以使用的 canUsed true 已经解锁的 unlock false
	 * @return
	 */
	private List<Integer> getGiftCard(int roleDictId, boolean canUsedOrUnlock) {
		List<Integer> res = new ArrayList<>();
		Role role = get(roleDictId);
		if (role == null) {
			return res;
		}
		RoleConfig roleConfing = RoleManager.getInstance().getRoleConfig(roleDictId);
		List<List<Integer>> cards = roleConfing.getGiftCards();
		for (int i = 0; i < cards.size(); i++) {
			List<Integer> list = cards.get(i);
			// 礼物卡:角色id:好感度
			int cardId = list.get(0);
			if (canUsedOrUnlock) {
				boolean canUse = !ByteHelp.isOne(role.getGiftCardUsed(), i);
				if (!canUse) {
					continue;
				}
			}
			// 默认解锁的卡可以用
			if (i < RoleConstant.GIFT_CARD_DEFAULT_UNLOCK) {
				res.add(cardId);
				continue;
			}
			// 需要解锁的卡可以用
			if (ByteHelp.isOne(role.getGiftCardUnlock(), i - RoleConstant.GIFT_CARD_DEFAULT_UNLOCK)) {
				res.add(cardId);
			}
		}
		return res;
	}

	/**
	 * 获取角色可以使用的礼物卡
	 * 
	 * @return
	 */
	@Override
	public List<Integer> getCanUsedGiftCard(int roleDictId) {
		return getGiftCard(roleDictId, true);
	}

	@Override
	public List<Integer> getUnlockGiftCard(int roleDictId) {
		return getGiftCard(roleDictId, false);
	}

	@Override
	public void setRoleExploreTime(List<Integer> roleIds) {
		roleIds.forEach(e -> {
			Role role = this.get(e);
			role.setExploreTime(System.currentTimeMillis());
			Role updateRole = new Role();
			updateRole.setId(role.getId());
			updateRole.setExploreTime(role.getExploreTime());
			updateSelective(updateRole);
		});
	}

	/**
	 * 晋升分别在?,?,?级别时解锁一个新技能
	 * @param role
	 * @return 解锁的技能
	 */
	public int promotionAddNewSkill(Role role) {
		int level = role.getPromotionLevel();
		int skillId = -1;
		RoleConfig roleConfig = RoleManager.getInstance().getRoleConfig(role.getDictId());
		List<Integer> levels = OldGlobalConst.RoleUnlockSkillPromotionLevel;
		if (level == levels.get(0)) {
			skillId = roleConfig.getNormalSkill().get(1);
		} else if (level == levels.get(1)) {
			// EP技能
			skillId = roleConfig.getEpSkill().get(1);
		} else if (level == levels.get(2)) {
			// AP技能
			skillId = roleConfig.getApSkill().get(1);
		}
		if (skillId != -1) {
			role.addSkills(skillId);
		}
		return skillId;
	}

	@Override
	public void rolePromotion() {
	}

	@Override
	public void addPromotionPoint(int promotionPoint) {
		if (promotionPoint == 0) {
			return;
		}
		ChapterOp chapterOp = player.getModule(ChapterOp.class);
	}

	@Override
	public void addPromotionPoint(List<Integer> roleIdList, int promotionPoint) {
		//List<RoleMsg.RolePromotionInfo> outRolePromotionInfos = new ArrayList<>();
//		roleIdList.forEach(e -> {
//			Role role = this.id_roles.get(e);
//			if (role.getState() != (byte) RoleMsg.RoleState.DIE_VALUE) {
				//role.setPromotionPoint(role.getPromotionPoint() + promotionPoint);
//				ExploreOp exploreOp = player.getModule(ExploreOp.class);
//				if(exploreOp.getExploreChapter() != null) {
//					HashMap<Integer, Integer> promotionPointMap = exploreOp.getExploreChapter().getPromotionPointMap();
//					promotionPointMap.put(e, (promotionPointMap.get(e) == null ? 0 : promotionPointMap.get(e)) + promotionPoint);
//				}
//				Role update = new Role();
//				update.setPromotionPoint(role.getPromotionPoint());
//				update.setId(role.getId());
//				DAO.updateSelective(RoleMapper.class, update);
//
//				RoleMsg.RolePromotionInfo.Builder info = RoleMsg.RolePromotionInfo.newBuilder();
//				info.setRoleId(e);
//				info.setPromotionLevel(role.getPromotionLevel());
//				info.setPromotionPoint(role.getPromotionPoint());
//				outRolePromotionInfos.add(info.build());
//			}
//		});
//		RoleMsg.RolePromotionInfoPush_52011130.Builder response = RoleMsg.RolePromotionInfoPush_52011130.newBuilder();
//		response.addAllPromotionInfos(outRolePromotionInfos);
//		GameClientManager.getInstance().noticeOne(response.build(), this.playerId);
	}

	@Override
	public boolean hasCampRole(int camp) {
		Set<Entry<Integer, Role>> entrySet = id_roles.entrySet();
		for (Entry<Integer, Role> entry : entrySet) {
			RoleConfig roleConfig = RoleManager.getInstance().getRoleConfig(entry.getValue().getDictId());
			if (roleConfig.getCamp() == camp) {
				return true;
			}
		}
		return false;
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}

}
