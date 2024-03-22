package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;

import cn.game.protocol.generated.config.RoleTagConfig;
import cn.game.protocol.generated.config.OldSkillConfig;
import cn.game.protocol.generated.manager.RoleTagManager;
import cn.game.protocol.generated.manager.OldSkillManager;
import cn.game.games.net.game.helper.BuffHelper;
import cn.game.games.net.game.helper.RoleHelper;
import cn.game.games.net.game.module.role.RoleConstant;
import cn.game.util.ByteHelp;
import cn.game.util.MapUtil;
import cn.game.util.StrUtil;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class Role implements Serializable, DbEntity {

	/**
	 * 数据库唯一id
	 * @mbg.generated
	 */
	private Long id;
	/**
	 * 配置表id
	 * @mbg.generated
	 */
	private Integer dictId;
	/**
	 * 玩家id
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 等级
	 * @mbg.generated
	 */
	private Integer level;
	/**
	 * 等级经验
	 * @mbg.generated
	 */
	private Integer exp;
	/**
	 * 突破等级
	 * @mbg.generated
	 */
	private Integer breakLevel;
	/**
	 * 星级
	 * @mbg.generated
	 */
	private Integer star;
	/**
	 * 解锁的源质id
	 * @mbg.generated
	 */
	private String originIds;
	/**
	 * 已经解锁的天赋树id
	 * @mbg.generated
	 */
	private String treeIds;
	/**
	 * 亲密度
	 * @mbg.generated
	 */
	private Integer intimacy;
	/**
	 * 亲密度等级
	 * @mbg.generated
	 */
	private Byte intimacyLevel;
	/**
	 * 是否誓约了
	 * @mbg.generated
	 */
	private Boolean promise;
	/**
	 * 机甲强化等级
	 * @mbg.generated
	 */
	private Integer mechalevel;
	/**
	 * 获取时间
	 * @mbg.generated
	 */
	private Long getTime;
	/**
	 * 当前使用的皮肤id
	 * @mbg.generated
	 */
	private Integer skin;
	/**
	 * 灵武等级
	 * @mbg.generated
	 */
	private Integer soulweaponLevel;
	/**
	 * @mbg.generated
	 */
	private String skills;
	/**
	 * @mbg.generated
	 */
	private String skillsUsed;
	/**
	 * 血量
	 * @mbg.generated
	 */
	private Integer hp;
	/**
	 * 血量当前最大值
	 * @mbg.generated
	 */
	private Integer hpCurMax;
	/**
	 * san值
	 * @mbg.generated
	 */
	private Integer san;
	/**
	 * ep
	 * @mbg.generated
	 */
	private Integer ep;
	/**
	 * 状态：0正常 1重伤 2死亡
	 * @mbg.generated
	 */
	private Byte state;
	/**
	 * 技能点
	 * @mbg.generated
	 */
	private Integer skillPoint;
	/**
	 * 佣兵名字id，MercenaryName.xlsm表id
	 * @mbg.generated
	 */
	private Integer nameId;
	/**
	 * 属性点数增加
	 * @mbg.generated
	 */
	private String attrPointAdd;
	/**
	 * 队列id
	 * @mbg.generated
	 */
	private Integer lineupId;
	/**
	 * 是否锁定(不可养成)
	 * @mbg.generated
	 */
	private Boolean isLock;
	/**
	 * 礼物卡3 4 5栏位解锁情况 二进制1表示已解锁
	 * @mbg.generated
	 */
	private Byte giftCardUnlock;
	/**
	 * 礼物卡使用情况 二进制1表示已使用
	 * @mbg.generated
	 */
	private Byte giftCardUsed;
	/**
	 * 探索时间
	 * @mbg.generated
	 */
	private Long exploreTime;
	/**
	 * 晋升升级
	 * @mbg.generated
	 */
	private Byte promotionLevel;
	/**
	 * 晋升点
	 * @mbg.generated
	 */
	private Integer promotionPoint;
	/**
	 * 角色所有的标签
	 * @mbg.generated
	 */
	private String tags;
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
	public Integer getDictId() {
		return dictId;
	}

	/**
	 * @mbg.generated
	 */
	public void setDictId(Integer dictId) {
		this.dictId = dictId;
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
	public Integer getExp() {
		return exp;
	}

	/**
	 * @mbg.generated
	 */
	public void setExp(Integer exp) {
		this.exp = exp;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getBreakLevel() {
		return breakLevel;
	}

	/**
	 * @mbg.generated
	 */
	public void setBreakLevel(Integer breakLevel) {
		this.breakLevel = breakLevel;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getStar() {
		return star;
	}

	/**
	 * @mbg.generated
	 */
	public void setStar(Integer star) {
		this.star = star;
	}

	/**
	 * @mbg.generated
	 */
	public String getOriginIds() {
		return originIds;
	}

	/**
	 * @mbg.generated
	 */
	public void setOriginIds(String originIds) {
		this.originIds = originIds;
	}

	/**
	 * @mbg.generated
	 */
	public String getTreeIds() {
		return treeIds;
	}

	/**
	 * @mbg.generated
	 */
	public void setTreeIds(String treeIds) {
		this.treeIds = treeIds;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getIntimacy() {
		return intimacy;
	}

	/**
	 * @mbg.generated
	 */
	public void setIntimacy(Integer intimacy) {
		this.intimacy = intimacy;
	}

	/**
	 * @mbg.generated
	 */
	public Byte getIntimacyLevel() {
		return intimacyLevel;
	}

	/**
	 * @mbg.generated
	 */
	public void setIntimacyLevel(Byte intimacyLevel) {
		this.intimacyLevel = intimacyLevel;
	}

	/**
	 * @mbg.generated
	 */
	public Boolean getPromise() {
		return promise;
	}

	/**
	 * @mbg.generated
	 */
	public void setPromise(Boolean promise) {
		this.promise = promise;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getMechalevel() {
		return mechalevel;
	}

	/**
	 * @mbg.generated
	 */
	public void setMechalevel(Integer mechalevel) {
		this.mechalevel = mechalevel;
	}

	/**
	 * @mbg.generated
	 */
	public Long getGetTime() {
		return getTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setGetTime(Long getTime) {
		this.getTime = getTime;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getSkin() {
		return skin;
	}

	/**
	 * @mbg.generated
	 */
	public void setSkin(Integer skin) {
		this.skin = skin;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getSoulweaponLevel() {
		return soulweaponLevel;
	}

	/**
	 * @mbg.generated
	 */
	public void setSoulweaponLevel(Integer soulweaponLevel) {
		this.soulweaponLevel = soulweaponLevel;
	}

	/**
	 * @mbg.generated
	 */
	public String getSkills() {
		return skills;
	}

	/**
	 * @mbg.generated
	 */
	public void setSkills(String skills) {
		this.skills = skills;
	}

	/**
	 * @mbg.generated
	 */
	public String getSkillsUsed() {
		return skillsUsed;
	}

	/**
	 * @mbg.generated
	 */
	public void setSkillsUsed(String skillsUsed) {
		this.skillsUsed = skillsUsed;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getHp() {
		return hp;
	}

	/**
	 * @mbg.generated
	 */
	public void setHp(Integer hp) {
		this.hp = hp;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getHpCurMax() {
		return hpCurMax;
	}

	/**
	 * @mbg.generated
	 */
	public void setHpCurMax(Integer hpCurMax) {
		this.hpCurMax = hpCurMax;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getSan() {
		return san;
	}

	/**
	 * @mbg.generated
	 */
	public void setSan(Integer san) {
		this.san = san;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getEp() {
		return ep;
	}

	/**
	 * @mbg.generated
	 */
	public void setEp(Integer ep) {
		this.ep = ep;
	}

	/**
	 * @mbg.generated
	 */
	public Byte getState() {
		return state;
	}

	/**
	 * @mbg.generated
	 */
	public void setState(Byte state) {
		this.state = state;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getSkillPoint() {
		return skillPoint;
	}

	/**
	 * @mbg.generated
	 */
	public void setSkillPoint(Integer skillPoint) {
		this.skillPoint = skillPoint;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getNameId() {
		return nameId;
	}

	/**
	 * @mbg.generated
	 */
	public void setNameId(Integer nameId) {
		this.nameId = nameId;
	}

	/**
	 * @mbg.generated
	 */
	public String getAttrPointAdd() {
		return attrPointAdd;
	}

	/**
	 * @mbg.generated
	 */
	public void setAttrPointAdd(String attrPointAdd) {
		this.attrPointAdd = attrPointAdd;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getLineupId() {
		return lineupId;
	}

	/**
	 * @mbg.generated
	 */
	public void setLineupId(Integer lineupId) {
		this.lineupId = lineupId;
	}

	/**
	 * @mbg.generated
	 */
	public Boolean getIsLock() {
		return isLock;
	}

	/**
	 * @mbg.generated
	 */
	public void setIsLock(Boolean isLock) {
		this.isLock = isLock;
	}

	/**
	 * @mbg.generated
	 */
	public Byte getGiftCardUnlock() {
		return giftCardUnlock;
	}

	/**
	 * @mbg.generated
	 */
	public void setGiftCardUnlock(Byte giftCardUnlock) {
		this.giftCardUnlock = giftCardUnlock;
	}

	/**
	 * @mbg.generated
	 */
	public Byte getGiftCardUsed() {
		return giftCardUsed;
	}

	/**
	 * @mbg.generated
	 */
	public void setGiftCardUsed(Byte giftCardUsed) {
		this.giftCardUsed = giftCardUsed;
	}

	/**
	 * @mbg.generated
	 */
	public Long getExploreTime() {
		return exploreTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setExploreTime(Long exploreTime) {
		this.exploreTime = exploreTime;
	}

	/**
	 * @mbg.generated
	 */
	public Byte getPromotionLevel() {
		return promotionLevel;
	}

	/**
	 * @mbg.generated
	 */
	public void setPromotionLevel(Byte promotionLevel) {
		this.promotionLevel = promotionLevel;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getPromotionPoint() {
		return promotionPoint;
	}

	/**
	 * @mbg.generated
	 */
	public void setPromotionPoint(Integer promotionPoint) {
		this.promotionPoint = promotionPoint;
	}

	/**
	 * @mbg.generated
	 */
	public String getTags() {
		return tags;
	}

	/**
	 * @mbg.generated
	 */
	public void setTags(String tags) {
		this.tags = tags;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.RoleMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}

	/** 当前san最大值 */
	private int sanCurMax;
	
	public int getSanCurMax() {
		return sanCurMax;
	}

	public void setSanCurMax(int sanCurMax) {
		this.sanCurMax = sanCurMax;
	}

	private transient Map<Integer, Integer> originIdMap = new HashMap<Integer, Integer>();
	/** key：天赋树id value：默认0，1表示满级 */
	private transient Map<Integer, Integer> treeIdMap = new HashMap<Integer, Integer>();
	/**
	 * 已解锁的主动技能
	 * k：技能id(不包括天赋技能,天赋技能是否解锁存储在天赋树)
	 * v：已解锁的强化项列表
	 */
	private transient Map<Integer, List<Integer>> skillsMap = new HashMap<>();
	/** 当前使用的技能 包括天赋技能、主动技能、一个被动技能 */
	private transient Set<Integer> skillsUsedSet = new HashSet<>();
	/** 标签列表 */
	private transient List<Integer> tagList = new ArrayList<>();
	/** key：属性枚举id value：增加的点数 */
	private transient Map<Integer, Integer> attrPointAddMap = new HashMap<>();

	/**
	 * 数据库大字段转成业务对象，从数据库载入之后调用
	 */
	public void load() {
		this.originIdMap = StrUtil.toMap(this.originIds);
		this.treeIdMap = StrUtil.toMap(this.treeIds);
		this.skillsMap = this.parseSkillsMap();
		this.skillsUsedSet = StrUtil.toSet(this.skillsUsed);
		if (this.tags != null) {
			this.tagList = JSON.parseArray(this.tags, Integer.class);
		}
		this.attrPointAddMap = StrUtil.toMap(this.attrPointAdd);
		// TODO 其他新增字段都加到这里
	}
	
	/** 
	 * 业务对象转成数据库存储字段,存储之前需要调用
	 */
	public void save() {
		this.originIds = StrUtil.toString(this.originIdMap);
		this.treeIds = StrUtil.toString(this.treeIdMap);
		this.skills = this.skillsMaptoString();
		this.skillsUsed = StrUtil.toString(this.skillsUsedSet);
		this.tags = JSON.toJSONString(this.tagList);
		this.attrPointAdd = StrUtil.toString(this.attrPointAddMap);
		// TODO 其他新增字段都加到这里
	}

	public Map<Integer, Integer> getAttrPointAddMap() {
		return attrPointAddMap;
	}
	
	/**
	 * 增加属性点数
	 * @param addMap<attrId,add>
	 */
	public void addAttrPoint(Map<Integer, Integer> addMap) {
		MapUtil.addItemCounts(attrPointAddMap, addMap);
	}
	
	/**
	 * 获取技能map
	 * @return
	 */
	public Map<Integer, List<Integer>> getSkillsMap() {
		return skillsMap;
	}
	
	/**
	 * 解析skills字段
	 * @return
	 */
	private Map<Integer, List<Integer>> parseSkillsMap() {
		Map<Integer, List<Integer>> map = new HashMap<>(3);
		if (skills != null && skills.length() > 0) {
			String[] oneSkill = skills.split("\\|");
			for (String s : oneSkill) {
				String[] ids = s.split(":");
				
				int skillId = Integer.parseInt(ids[0]);
				List<Integer> list = new ArrayList<>(ids.length - 1);
				for (int i = 0; i < ids.length - 1; i++) {
					list.add(Integer.parseInt(ids[i + 1]));
				}
				map.put(skillId, list);
			}
		}
		return map;
	}
	
	public Set<Integer> getSkillsUsedSet() {
		return skillsUsedSet;
	}

	public void removeOccTalentSkill(int skillId) {
		removeSkillsUsed(skillId);
	}

	/**
	 * 增加技能强化项
	 * @param skillId
	 * @param strengthId
	 */
	public void addSkillStrengthBuff(int skillId, int strengthId) {
		if (!skillsMap.containsKey(skillId)) {
			skillsMap.put(skillId, new ArrayList<>());
		}
		skillsMap.get(skillId).add(strengthId);
	}

	/**
	 * 增加一个技能
	 * @param skillId
	 */
	public void addSkills(int skillId) {
		if (!skillsMap.containsKey(skillId)) {
			skillsMap.put(skillId, new ArrayList<>());
		}
	}
	
	/**
	 * 增加多个技能
	 * @param skillId
	 */
	public void addSkills(List<Integer> skillIds) {
		for (int id : skillIds) {
			if (!skillsMap.containsKey(id)) {
				skillsMap.put(id, new ArrayList<>());
			}	
		}
	}
	
	public String skillsMaptoString() {
		StringBuilder sb = new StringBuilder();
		for (int skillId : this.skillsMap.keySet()) {
			List<Integer> list = skillsMap.get(skillId);
			sb.append(skillId).append(":");
			for (int strengthenId : list) {
				sb.append(strengthenId).append(":");
			}
			sb.append("|");
		}
		return sb.toString();
	}
	
	public void addSkillsUsed(int id) {
		if (!skillsUsedSet.contains(id)) {
			skillsUsedSet.add(id);
		}
	}
	
	public void removeSkillsUsed(int id) {
		skillsUsedSet.remove(id);
	}

	public Map<Integer, Integer> getTreeList() {
		return treeIdMap;
	}

	public void addTreeId(int id) {
		this.treeIdMap.put(id, 0);
	}
	/**
	 * @Description 天赋树满级
	 * @param id
	 */
	public void setTreeComplete(int id) {
		this.treeIdMap.put(id, 1);
	}

	public Map<Integer, Integer> getOriginMap() {
		return originIdMap;
	}

	public void addOrigin(int id) {
		originIdMap.put(id, 1);
	}
	public void addOriginLevel(int id) {
		Integer level = originIdMap.get(id);
		originIdMap.put(id, level + 1);
	}
	public boolean addTag(int tagId) {
		if (tagId <= 0) {
			return false;
		}
		if (!tagList.contains(tagId)) {
			RoleTagConfig roleTagConfig = RoleTagManager.getInstance().getRoleTagConfig(tagId);
			List<Integer> buffs = roleTagConfig.getBuff();
			for (Integer buffId : buffs) {
				BuffHelper.addBuff(playerId, buffId, Arrays.asList((long) dictId), true);
			}
			tagList.add(tagId);
			return true;
		}
		return false;
	}

	public List<Integer> getTagList() {
		return tagList;
	}
	public static Role valueOf(long uid) {
		Role role = new Role();
		role.id = uid;
		return role;
	}

	/**
	 * 重置角色礼物卡
	 */
	public void resetGiftCard() {
		// 礼物卡一次解锁,永远解锁
//		this.giftCardUnlock = 0;
		this.giftCardUsed = 0;
	}

	/**
	 * 礼物卡是否已解锁
	 * @param giftCardId
	 * @return
	 */
	public boolean isGiftCardUnlock(int giftCardId) {
		int location = RoleHelper.getGiftCardLocation(this.dictId, giftCardId);
		// 0,1,2位置的礼物卡默认解锁
		if (location >= 0 && location <= RoleConstant.GIFT_CARD_DEFAULT_UNLOCK - 1) {
			return true;
		}
		if (location >= RoleConstant.GIFT_CARD_DEFAULT_UNLOCK && ByteHelp.isOne(this.giftCardUnlock, location - RoleConstant.GIFT_CARD_DEFAULT_UNLOCK)) {
			return true;
		}
		return false;
	}

	/**
	 * 礼物卡是否已使用
	 * @param giftCardId
	 * @return
	 */
	public boolean isGiftCardUsed(int giftCardId) {
		int location = RoleHelper.getGiftCardLocation(this.dictId, giftCardId);
		if (location >= 0 && ByteHelp.isOne(this.giftCardUsed, location)) {
			return true;
		}
		return false;
	}

	/**
	 * 重置技能
	 */
	public void resetSkill() {
		for (int skillId : skillsMap.keySet()) {
			OldSkillConfig skillConfig = OldSkillManager.getInstance().getSkillConfig(skillId);
			// 天赋技能的强化项不重置
			if (skillConfig.getType() == RoleConstant.TALENT_SKILL) {
				continue;
			}
			skillsMap.get(skillId).clear();
		}
	}
}