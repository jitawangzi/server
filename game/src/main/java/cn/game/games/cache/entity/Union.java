package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import cn.game.games.cache.base.DbEntity;
import cn.game.games.net.game.module.award.UnionApply;
import cn.game.protocol.generated.enume.UnionBuildingType;
import cn.game.util.DateUtil;

public class Union implements Serializable, DbEntity {
	/**
	 * @mbg.generated
	 */
	private Long id;
	/**
	 * @mbg.generated
	 */
	private String name;
	/**
	 * @mbg.generated
	 */
	private Integer type;
	/**
	 * @mbg.generated
	 */
	private Integer level;
	/**
	 * @mbg.generated
	 */
	private Integer iconFrame;
	/**
	 * @mbg.generated
	 */
	private Integer iconContent;
	/**
	 * @mbg.generated
	 */
	private String areaName;
	/**
	 * @mbg.generated
	 */
	private String serverName;
	/**
	 * @mbg.generated
	 */
	private String notice;
	/**
	 * @mbg.generated
	 */
	private Long wood;
	/**
	 * @mbg.generated
	 */
	private Long activity;
	/**
	 * @mbg.generated
	 */
	private Integer minApplyLevel;
	/**
	 * @mbg.generated
	 */
	private Long delateTime;
	/**
	 * @mbg.generated
	 */
	private Long createTime;
	/**
	 * @mbg.generated
	 */
	private byte[] members;
	/**
	 * @mbg.generated
	 */
	private byte[] applys;
	/**
	 * @mbg.generated
	 */
	private byte[] buildings;
	/**
	 * @mbg.generated
	 */
	private byte[] skills;
	/**
	 * @mbg.generated
	 */
	private byte[] items;
	/**
	 * @mbg.generated
	 */
	private byte[] upgrading;
	/**
	 * 工会捐献的英雄
	 * @mbg.generated
	 */
	private byte[] donationHeros;
	/**
	 * 工会日志
	 * @mbg.generated
	 */
	private byte[] journals;
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
	public String getName() {
		return name;
	}

	/**
	 * @mbg.generated
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getType() {
		return type;
	}

	/**
	 * @mbg.generated
	 */
	public void setType(Integer type) {
		this.type = type;
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
	public Integer getIconFrame() {
		return iconFrame;
	}

	/**
	 * @mbg.generated
	 */
	public void setIconFrame(Integer iconFrame) {
		this.iconFrame = iconFrame;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getIconContent() {
		return iconContent;
	}

	/**
	 * @mbg.generated
	 */
	public void setIconContent(Integer iconContent) {
		this.iconContent = iconContent;
	}

	/**
	 * @mbg.generated
	 */
	public String getAreaName() {
		return areaName;
	}

	/**
	 * @mbg.generated
	 */
	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	/**
	 * @mbg.generated
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * @mbg.generated
	 */
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	/**
	 * @mbg.generated
	 */
	public String getNotice() {
		return notice;
	}

	/**
	 * @mbg.generated
	 */
	public void setNotice(String notice) {
		this.notice = notice;
	}

	/**
	 * @mbg.generated
	 */
	public Long getWood() {
		return wood;
	}

	/**
	 * @mbg.generated
	 */
	public void setWood(Long wood) {
		this.wood = wood;
	}

	/**
	 * @mbg.generated
	 */
	public Long getActivity() {
		return activity;
	}

	/**
	 * @mbg.generated
	 */
	public void setActivity(Long activity) {
		this.activity = activity;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getMinApplyLevel() {
		return minApplyLevel;
	}

	/**
	 * @mbg.generated
	 */
	public void setMinApplyLevel(Integer minApplyLevel) {
		this.minApplyLevel = minApplyLevel;
	}

	/**
	 * @mbg.generated
	 */
	public Long getDelateTime() {
		return delateTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setDelateTime(Long delateTime) {
		this.delateTime = delateTime;
	}

	/**
	 * @mbg.generated
	 */
	public Long getCreateTime() {
		return createTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	/**
	 * @mbg.generated
	 */
	public byte[] getMembers() {
		return members;
	}

	/**
	 * @mbg.generated
	 */
	public void setMembers(byte[] members) {
		this.members = members;
	}

	/**
	 * @mbg.generated
	 */
	public byte[] getApplys() {
		return applys;
	}

	/**
	 * @mbg.generated
	 */
	public void setApplys(byte[] applys) {
		this.applys = applys;
	}

	/**
	 * @mbg.generated
	 */
	public byte[] getBuildings() {
		return buildings;
	}

	/**
	 * @mbg.generated
	 */
	public void setBuildings(byte[] buildings) {
		this.buildings = buildings;
	}

	/**
	 * @mbg.generated
	 */
	public byte[] getSkills() {
		return skills;
	}

	/**
	 * @mbg.generated
	 */
	public void setSkills(byte[] skills) {
		this.skills = skills;
	}

	/**
	 * @mbg.generated
	 */
	public byte[] getItems() {
		return items;
	}

	/**
	 * @mbg.generated
	 */
	public void setItems(byte[] items) {
		this.items = items;
	}

	/**
	 * @mbg.generated
	 */
	public byte[] getUpgrading() {
		return upgrading;
	}

	/**
	 * @mbg.generated
	 */
	public void setUpgrading(byte[] upgrading) {
		this.upgrading = upgrading;
	}

	/**
	 * @mbg.generated
	 */
	public byte[] getDonationHeros() {
		return donationHeros;
	}

	/**
	 * @mbg.generated
	 */
	public void setDonationHeros(byte[] donationHeros) {
		this.donationHeros = donationHeros;
	}

	/**
	 * @mbg.generated
	 */
	public byte[] getJournals() {
		return journals;
	}

	/**
	 * @mbg.generated
	 */
	public void setJournals(byte[] journals) {
		this.journals = journals;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.UnionMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}

	/** 工会成员 key:playerId */
	private Map<Long, Member> membersMap = new HashMap<Long, Member>();
	/** 玩家向工会发出的申请 key:playerId */
	private Map<Long, UnionApply> applysMap = new HashMap<Long, UnionApply>();
	/** 工会建筑 value: upgradeId */
	private Map<UnionBuildingType, Integer> buildingMap = new HashMap<UnionBuildingType, Integer>();
	/** 正在升级中的建筑,value:升级开始时间 */
	private Map<UnionBuildingType, Long> upgradingMap = new HashMap<UnionBuildingType, Long>();
	/** 工会技能 */
//	private Map<Integer, cn.game.games.cache.entity.Skill> skillMap = new HashMap<Integer, cn.game.games.cache.entity.Skill>();
	// /** 工会道具，可以给会员分配 */
	// private List<UserItem> itemList ;
	/** 第一个key为userId， 第二个key为horseId，Byte为雇佣状态， 默认0为未雇佣，大于0表示雇佣 */
	private Map<Long, Map<Long, Byte>> donationhorseMap = new HashMap<Long, Map<Long, Byte>>();
	// private List<Journal> journalsList = new ArrayList<>() ;

	private LinkedHashMap<String, List<String>> journalsMap = new LinkedHashMap<>();

	private transient Member[] conTodayRanking;
	private transient Member[] conWeekRanking;
	private transient Member[] conHistoryRanking;


	static class conTodayComparator implements Comparator<Member> {

		@Override
		public int compare(Member o1, Member o2) {

			return o1.getContributionToday() > o2.getContributionToday() ? 1 : -1;
		}

	}

	static class conWeekComparator implements Comparator<Member> {

		@Override
		public int compare(Member o1, Member o2) {

			return o1.getContributionWeek() > o2.getContributionWeek() ? 1 : -1;
		}

	}

	static class conHistoryComparator implements Comparator<Member> {

		@Override
		public int compare(Member o1, Member o2) {

			return o1.getContribution() > o2.getContribution() ? 1 : -1;
		}

	}

	public void initRank() {

		conTodayRanking = new Member[this.membersMap.size()];
		conWeekRanking = new Member[this.membersMap.size()];
		conHistoryRanking = new Member[this.membersMap.size()];

		int i = 0;
		for (Member member : this.membersMap.values()) {
			conTodayRanking[i] = member;
			conWeekRanking[i] = member;
			conHistoryRanking[i] = member;
			i++;
		}
		updateRank();

	}

	public void updateRank() {
		Arrays.sort(conTodayRanking, new conTodayComparator());
		Arrays.sort(conWeekRanking, new conWeekComparator());
		Arrays.sort(conHistoryRanking, new conHistoryComparator());

	}

	public void addRank(Member member) {

		conTodayRanking = Arrays.copyOf(conTodayRanking, conTodayRanking.length + 1);
		conWeekRanking = Arrays.copyOf(conWeekRanking, conWeekRanking.length + 1);
		conHistoryRanking = Arrays.copyOf(conHistoryRanking, conHistoryRanking.length + 1);

		conTodayRanking[conTodayRanking.length - 1] = member;
		conWeekRanking[conWeekRanking.length - 1] = member;
		conHistoryRanking[conHistoryRanking.length - 1] = member;

		updateRank();

	}

	public void delRank(Member member) {

		delFromArray(conTodayRanking, member);
		delFromArray(conWeekRanking, member);
		delFromArray(conHistoryRanking, member);

		updateRank();

	}

	private void delFromArray(Member[] members, Member member) {
		int index = 0;
		for (int i = 0; i < members.length; i++) {
			if (members[i].getPlayerId().equals(member.getPlayerId())) {
				index = i;
				break;
			}
		}
		Member[] tmp = new Member[members.length - 1];

		System.arraycopy(members, 0, tmp, 0, index);
		System.arraycopy(members, index + 1, tmp, index, tmp.length - index);
		members = tmp;

	}

	public int getMemberSize(int title) {
		int size = 0;
		for (Member member : this.membersMap.values()) {
			if (member.getTitle() == title) {
				size++;
			}
		}
		return size;
	}
	public Member getMaster() {

//		for (Member member : this.membersMap.values()) {
//			if (member.getTitle() == DictUnionPositionEnum.Leader.v()) {
//				return member;
//			}
//		}
		return null;
	}

	public static Union valueOf(String name, int type, int iconFrame, int iconContent, Map<Long, Member> membersMap,
			Map<UnionBuildingType, Integer> buildingMap) {
		Union union = new Union();
		union.name = name;
		union.type = type;
		union.iconFrame = iconFrame;
		union.iconContent = iconContent;
		union.membersMap = membersMap;
		union.buildingMap = buildingMap;
		union.level = 1;
		union.minApplyLevel = 1;
		union.setCreateTime(System.currentTimeMillis());
		return union;

	}

	public static Union valueOf(long id) {
		Union union = new Union();
		union.id = id;
		return union;

	}

	public void updateDonationhorseState(long userId, long horseId, byte state) {

		Map<Long, Byte> map = this.donationhorseMap.get(userId);
		if (map == null) {
			return;
		}
		if (map.containsKey(horseId)) {
			map.put(horseId, state);
		}

	}

	public Object addDonation(long userId, long horseId) {
		Map<Long, Byte> map = this.donationhorseMap.get(userId);
		if (map == null) {
			map = new HashMap<>();
		}
		map.put(horseId, (byte) 0);
		return this.donationhorseMap.put(userId, map);
	}

	public void addApply(long playerId) {

		this.applysMap.put(playerId, UnionApply.valueOf(playerId));
	}

	public void delDonation(long userId, long horseId) {

		Map<Long, Byte> map = this.donationhorseMap.get(userId);
		if (map != null) {
			map.remove(horseId);
		}

	}

	public void delApply(long playerId) {

		this.applysMap.remove(playerId);

	}

	public void addJournal(String msg) {
		cn.game.games.cache.entity.Journal journal = new cn.game.games.cache.entity.Journal();
		journal.setMsg(msg);
		String day = DateUtil.getTimeByPattern(new Date(), "yyyy年MM月dd日");
		journal.setDay(day);
		String hour = DateUtil.getTimeByPattern(new Date(), "HH时mm分");
		journal.setHour(hour);

		List<String> list = this.journalsMap.get(day);
		if (list == null) {
			list = new ArrayList<>();
			this.journalsMap.put(day, list);
		}
		list.add(hour + "\t" + msg);

	}

	public void addMember(Member member) {

		this.membersMap.put(member.getPlayerId(), member);
	}

	public void delDonationByUserId(long userId) {

		this.donationhorseMap.remove(userId);
	}

	public final Map<Long, Map<Long, Byte>> getDonationhorseMap() {

		return donationhorseMap;
	}

	public final Map<Long, Member> getMembersMap() {

		return membersMap;
	}

	public final void setMembersMap(Map<Long, Member> membersMap) {

		this.membersMap = membersMap;
	}

	public final Map<Long, UnionApply> getApplysMap() {

		return applysMap;
	}

	public final void setApplysMap(Map<Long, UnionApply> applysMap) {

		this.applysMap = applysMap;
	}

	public final Map<UnionBuildingType, Integer> getBuildingMap() {

		return buildingMap;
	}

	public final void setBuildingMap(Map<UnionBuildingType, Integer> buildingMap) {

		this.buildingMap = buildingMap;
	}

	// public final List<UserItem> getItemList() {
	//
	// return itemList;
	// }
	//
	// public final void setItemList(List<UserItem> itemList) {
	//
	// this.itemList = itemList;
	// }



	public final byte[] getMembersBinaryData() {
		return this.members;
	}

//	public final byte[] getApplys() {
//		if (this.applysMap == null || this.applysMap.isEmpty()) {
//			return null;
//		}
//
//		String string = JSON.toJSONString(this.applysMap);
//		;
//		return string.getBytes();
//	}

//	public final void setApplys(byte[] applys) {
//
//		if (applys != null) {
//			String string = new String(applys);
//			this.applysMap = JSON.parseObject(string, new TypeReference<Map<Long, UnionApply>>() {
//			});
//		}
//		if (this.applysMap == null) {
//			this.applysMap = new HashMap<>();
//		}
//	}

	public final byte[] getDonationhorses() {
		if (this.donationhorseMap == null || this.donationhorseMap.isEmpty()) {
			return null;
		}

		String string = JSON.toJSONString(this.donationhorseMap);
		;
		return string.getBytes();
	}

	public final void setDonationhorses(byte[] donationhorses) {

		if (donationhorses != null) {
			String string = new String(donationhorses);
			this.donationhorseMap = JSON.parseObject(string, new TypeReference<Map<Long, Map<Long, Byte>>>() {
			});
		}
		if (this.donationhorseMap == null) {
			this.donationhorseMap = new HashMap<>();
		}
	}

//	public final byte[] getBuildings() {
//		if (this.buildingMap == null || this.buildingMap.isEmpty()) {
//			return null;
//		}
//
//		String string = JSON.toJSONString(this.buildingMap);
//		return string.getBytes();
//	}

//	public final void setBuildings(byte[] buildings) {
//
//		if (buildings != null) {
//			String string = new String(buildings);
//			this.buildingMap = JSON.parseObject(string, new TypeReference<Map<UnionBuildingType, Integer>>() {
//			});
//		}
//		if (this.buildingMap == null) {
//			this.buildingMap = new HashMap<>();
//		}
//	}

//	public final byte[] getSkills() {
//		if (this.skillMap == null || this.skillMap.isEmpty()) {
//			return null;
//		}
//
//		String string = JSON.toJSONString(this.skillMap);
//		;
//		return string.getBytes();
//	}

//	public final void setSkills(byte[] skills) {
//
//		if (skills != null) {
//			String string = new String(skills);
//			this.skillMap = JSON.parseObject(string, new TypeReference<Map<Integer, cn.game.games.cache.entity.Skill>>() {
//			});
//
//		}
//		if (this.skillMap == null) {
//			this.skillMap = new HashMap<>();
//		}
//	}

//	public final byte[] getItems() {
		// if (this.itemList==null||this.itemList.isEmpty())
		// {
		// return null ;
		// }
		//
		// String string = JSON.toJSONString(this.itemList) ; ;
		// return string.getBytes();
//		return null;
//	}

//	public final void setItems(byte[] items) {

		// if (items!=null)
		// {
		// String string = new String(items) ;
		// this.itemList = JSON.parseObject(string, new
		// TypeReference<List<UserItem>>() {});
		//
		// }
		// if (this.itemList==null)
		// {
		// this.itemList = new ArrayList<>() ;
		// }
//	}

//	public byte[] getJournals() {
//
//		if (this.journalsMap == null || this.journalsMap.isEmpty()) {
//			return null;
//		}
//
//		String string = JSON.toJSONString(this.journalsMap);
//		return string.getBytes();
//	}

//	public void setJournals(byte[] journals) {
//
//		if (journals != null) {
//			String string = new String(journals);
//			this.journalsMap = JSON.parseObject(string, new TypeReference<LinkedHashMap<String, List<String>>>() {
//			});
//		}
//		if (this.journalsMap == null) {
//			this.journalsMap = new LinkedHashMap<>();
//		}
//	}

//	public final byte[] getUpgrading() {
//
//		if (this.upgradingMap == null || this.upgradingMap.isEmpty()) {
//			return null;
//		}
//
//		String string = JSON.toJSONString(this.upgradingMap);
//		return string.getBytes();
//	}

//	public final void setUpgrading(byte[] upgrading) {
//
//		if (upgrading != null) {
//			String string = new String(upgrading);
//			this.upgradingMap = JSON.parseObject(string, new TypeReference<Map<UnionBuildingType, Long>>() {
//			});
//		}
//		if (this.upgradingMap == null) {
//			this.upgradingMap = new HashMap<>();
//		}
//	}

	public final Map<UnionBuildingType, Long> getUpgradingMap() {

		return upgradingMap;
	}

	public final void setUpgradingMap(Map<UnionBuildingType, Long> upgradingMap) {

		this.upgradingMap = upgradingMap;
	}

	/**
	 * 获取建筑的升级id
	 * @param type
	 * @return
	 */
	public Integer getUpgradeId(UnionBuildingType type) {
		return this.buildingMap.get(type);
	}


}
