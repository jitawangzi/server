package cn.game.games.util;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Multimap;

import cn.game.core.base.ServerContext;
import cn.game.games.cache.base.PlayerCacheFactory;
import cn.game.games.cache.entity.BattleLevel;
import cn.game.games.cache.entity.BattleRandomEvent;
import cn.game.games.cache.entity.Buff;
import cn.game.games.cache.entity.Chapter;
import cn.game.games.cache.entity.Chip;
import cn.game.games.cache.entity.ClimbingTower;
import cn.game.games.cache.entity.Equip;
import cn.game.games.cache.entity.ForbidAccount;
import cn.game.games.cache.entity.Friend;
import cn.game.games.cache.entity.Group;
import cn.game.games.cache.entity.GroupMember;
import cn.game.games.cache.entity.Mail;
import cn.game.games.cache.entity.Member;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.Quest;
import cn.game.games.cache.entity.QuestChallenge;
import cn.game.games.cache.entity.Role;
import cn.game.games.cache.entity.Story;
import cn.game.games.cache.entity.Union;
import cn.game.games.cache.op.impl.RoleOp;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.SimplePlayer;
import cn.game.games.net.game.GameServer;
import cn.game.games.net.game.helper.BuffHelper;
import cn.game.games.net.game.helper.ItemHelper;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.manager.UnionManager;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.award.RewardItem;
import cn.game.games.net.game.module.chat.GroupAllInfo;
import cn.game.games.net.game.module.equip.EquipModule;
import cn.game.games.net.game.module.item.ItemModule;
import cn.game.games.net.game.module.quest.Condition;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.games.net.game.module.store.StoreGoods;
import cn.game.protocol.generated.config.OldGlobalConst;
import cn.game.protocol.generated.config.RoleAttributeConfig;
import cn.game.protocol.generated.enume.AttributeSubTypeEnum;
import cn.game.protocol.generated.enume.AttributeTypeEnum;
import cn.game.protocol.generated.enume.GoodsTypeEnum;
import cn.game.protocol.generated.enume.MissionTypeEnum;
import cn.game.protocol.generated.manager.RoleAttributeManager;
import cn.game.protocol.protobuf.BaseMsg;
import cn.game.protocol.protobuf.BaseMsg.EquipInfo;
import cn.game.protocol.protobuf.BaseMsg.GoodsInfo;
import cn.game.protocol.protobuf.BaseMsg.ItemInfo;
import cn.game.protocol.protobuf.BaseMsg.PlayerShowInfo;
import cn.game.protocol.protobuf.BaseMsg.ResourceInfo;
import cn.game.protocol.protobuf.BaseMsg.ResourceInfo.Builder;
import cn.game.protocol.protobuf.BaseMsg.SimplePlayerInfo;
import cn.game.protocol.protobuf.BaseMsg.SkillInfo;
import cn.game.protocol.protobuf.BuffMsg;
import cn.game.protocol.protobuf.BuffMsg.BuffInfo;
import cn.game.protocol.protobuf.ChatMsg.ChatGroupBriefInfo;
import cn.game.protocol.protobuf.ChatMsg.ChatGroupInfo;
import cn.game.protocol.protobuf.ClimbingTowerMsg.TowerPlayerInfo;
import cn.game.protocol.protobuf.FriendMsg.FriendInfo;
import cn.game.protocol.protobuf.FriendMsg.FriendRelationInfo;
import cn.game.protocol.protobuf.GmMsg.ForbidAccountInfo;
import cn.game.protocol.protobuf.MailMsg.MailInfo;
import cn.game.protocol.protobuf.MissionMsg.MissionChallengeGroupInfo;
import cn.game.protocol.protobuf.MissionMsg.MissionInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo;
import cn.game.protocol.protobuf.RewardMsg;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.RewardMsg.RewardPush_55000501;
import cn.game.protocol.protobuf.StoreMsg.StoreGoodsInfo;
import cn.game.protocol.protobuf.StoreMsg.StoreRecommendInfo;
import cn.game.protocol.protobuf.StoryMsg.StoryInfo;
import cn.game.protocol.protobuf.UnionMsg;
import cn.game.util.ByteHelp;
import cn.game.util.DateUtil;
import cn.game.util.FourTuple;
import cn.game.util.Pair;

public class PbBuilder {

	public static PlayerAllInfo buildPlayerInfo(Player player) {
		PlayerAllInfo.Builder builder = PlayerAllInfo.newBuilder();
		Collection<BasePlayerModule> allModule = player.getAllModule();
		for (BasePlayerModule basePlayerModule : allModule) {
			basePlayerModule.buildPlayerAllInfo(builder);
		}
		return builder.build();
	}


	/*public static BagInfo buildBagInfo(long playerId) {
		BagInfo.Builder builder = BagInfo.newBuilder();
		// 背包格子数据
		builder.addAllBagGridInfos(buildBagGridInfo(playerId));
		//(背包已满)待选择的奖励
	//		builder.setReward(buildBagRewardNeedChooseInfo(playerId));
		
		return builder.build();
	}
	
	public static List<BagGridInfo> buildBagGridInfo(long playerId) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
	
		ItemModule itemModule = player.getModule(ItemModule.class);
		EquipOp equipOp = player.getModule(EquipOp.class);
		List<BagGridInfo> res = new ArrayList<>();
		// 背包格子数据
	//		Map<Integer, BagGrid> bagGridMap = itemModule.getBagGridMap();
	//		for (int gridId : bagGridMap.keySet()) {
	//			BagGrid bagGrid = bagGridMap.get(gridId);
	//			int itemId = bagGrid.getItemId();
	//			int itemCount = bagGrid.getItemCount();
	//			long equipId = bagGrid.getEquipId();
	//
	//			BagGridInfo.Builder newBuilder = BagGridInfo.newBuilder();
	//			if (bagGrid.isItem()) {
	//				newBuilder.setId(gridId);
	//				newBuilder.setItem(ItemInfo.newBuilder().setId(itemId).setCount(itemCount));
	//				
	//			} else if (bagGrid.isEquip()) {
	//				Equip equip = equipOp.get(equipId);
	//				newBuilder.setId(gridId);
	//				newBuilder.setEquip(buildEquipInfo(equip));
	//				
	//			} else if(bagGrid.isEmpty()) {
	//				continue;
	//			}
	//			res.add(newBuilder.build());
	//		}
	//		
		return res;
	}*/

	public static EquipInfo buildEquipInfo(Equip e) {
		EquipInfo.Builder builder = EquipInfo.newBuilder();
		builder.setUid(e.getId() + "");
		builder.setId(e.getDictId());
//		builder.setStorage(e.getStorage());
		builder.setRoleId(e.getRoleId());
		builder.setSlot(e.getPos());
		builder.addAllBuffs(e.getBuffList());
//		builder.setStrength(e.getStrength());
		// TODO 其他属性
		return builder.build();
	}

	


	public static List<ResourceInfo> buildResourceInfo(Map<Integer, Integer> map) {
		List<ResourceInfo> list = new ArrayList<>(map.size());
		for (int key : map.keySet()) {
			Builder b = ResourceInfo.newBuilder().setId(key).setCount(map.get(key));
			list.add(b.build());
		}
		return list;
	}

	public static FriendInfo buildFriendInfo(Friend friend) throws Exception {

		FriendInfo.Builder friendBuilder = FriendInfo.newBuilder();

		SimplePlayerInfo simplePlayerInfo = buildSimplePlayerInfo(friend.getFriendId(), friend.getServerId());
		friendBuilder.setPlayer(simplePlayerInfo);

		FriendRelationInfo.Builder relationBuilder = FriendRelationInfo.newBuilder();
		relationBuilder.setIntimate(friend.getIntimate());
		relationBuilder.setIntimateLevel(friend.getIntimateLevel());
		relationBuilder.setRelation(friend.getRelation());

		friendBuilder.setRelation(relationBuilder.build());

//		FriendGiftInfo.Builder giftBuilder = FriendGiftInfo.newBuilder();
//		giftBuilder.setGift(friend.getGift());
//		giftBuilder.setGifted(friend.getGifted());
//		giftBuilder.setReceive(friend.getReceive());
//		friendBuilder.setGiftInfo(giftBuilder.build());

		return friendBuilder.build();

	}

	/**
	 * @Description 根据在线玩家对象构建玩家数据，需要保证参数不能为空
	 * @param player
	 * @return
	 */
	public static SimplePlayerInfo buildSimplePlayerInfo(Player player) {

		Long playerId = player.getData().getPlayerId();
		SimplePlayerInfo.Builder builder = SimplePlayerInfo.newBuilder();
		builder.setId(playerId + "");
		builder.setLevel(player.getData().getLevel());
		builder.setName(player.getData().getName());
		builder.setOnline(GameClientManager.getInstance().isOnline(playerId));
		builder.setOfflineTime((int) (player.getData().getOfflineTime() / 1000));
		builder.setHead(player.getData().getHead());
		builder.setHeadFrame(player.getData().getHeadFrame());
		builder.setServerId(ServerContext.getInstance().getServerId());
		return builder.build();

	}

	public static SimplePlayerInfo buildSimplePlayerInfo(SimplePlayer player) {

		SimplePlayerInfo.Builder builder = SimplePlayerInfo.newBuilder();
		if (player != null) {

			Long playerId = player.getId();
			builder.setId(playerId + "");
			builder.setLevel(player.getLevel());
			builder.setName(player.getName());
			builder.setOnline(player.isOnline());
			builder.setOfflineTime((int) (player.getOfflineTime() / 1000));
			builder.setHead(player.getHead());
			builder.setHeadFrame(player.getHeadFrame());
			builder.setServerId(player.getServerId() == null ? "" : player.getServerId());

		}
		return builder.build();

	}

	public static SimplePlayer buildSimplePlayer(SimplePlayerInfo simplePlayerInfo) {
		SimplePlayer simplePlayer = new SimplePlayer();
		simplePlayer.setId(Long.parseLong(simplePlayerInfo.getId()));
		simplePlayer.setName(simplePlayerInfo.getName());
		simplePlayer.setLevel(simplePlayerInfo.getLevel());
		simplePlayer.setHead(simplePlayerInfo.getHead());
		simplePlayer.setOnline(simplePlayerInfo.getOnline());
		simplePlayer.setOfflineTime(simplePlayerInfo.getOfflineTime());
		simplePlayer.setServerId(simplePlayerInfo.getServerId());
		
		return simplePlayer;
	}
	
	public static SimplePlayerInfo buildSimplePlayerInfo(long playerId, String serverId) throws Exception {

		SimplePlayerInfo.Builder builder = SimplePlayerInfo.newBuilder();

		SimplePlayer simplePlayer = PlayerManager.getInstance().getAndLoadSimplePlayer(playerId, serverId);

		if (simplePlayer != null) {

			builder.setId(playerId + "");
			builder.setLevel(simplePlayer.getLevel());
			builder.setName(simplePlayer.getName());
			builder.setOnline(simplePlayer.isOnline());
			builder.setOfflineTime((int) (simplePlayer.getOfflineTime() / 1000));
			builder.setHead(simplePlayer.getHead());
			builder.setHeadFrame(simplePlayer.getHeadFrame());
			builder.setServerId(serverId);
		}
		return builder.build();

	}
	public static PlayerShowInfo buildPlayerShowInfo(SimplePlayer player) {

		PlayerShowInfo.Builder showInfo = PlayerShowInfo.newBuilder() ; 
		
		showInfo.setGuild(player.getUnionName() == null ? "" : player.getUnionName());
		showInfo.setCombat(player.getCombat());
		showInfo.setPraisedCount(player.getPraisedCount());
		
		return showInfo.build();

	}
	public static List<SimplePlayerInfo> buildSimplePlayerInfos(List<SimplePlayer> players) {
		List<SimplePlayerInfo> list = new ArrayList<BaseMsg.SimplePlayerInfo>();
		for (SimplePlayer simplePlayer : players) {
			list.add(buildSimplePlayerInfo(simplePlayer));
		}
		return list;
	}
	public static SimplePlayerInfo buildSimplePlayerInfo(long playerId) throws Exception {
		SimplePlayerInfo builder;

		if (PlayerManager.getInstance().hasCache(playerId)) {
			Player player = PlayerManager.getInstance().getPlayer(playerId);
			builder = buildSimplePlayerInfo(player);
		} else {
			SimplePlayer simplePlayer = PlayerManager.getInstance().getAndLoadSimplePlayer(playerId);
			builder = buildSimplePlayerInfo(simplePlayer);
		}
		return builder;

	}

	public static ItemInfo buildItemInfo(int k, long v) {

		ItemInfo.Builder itemInfo = ItemInfo.newBuilder();
		itemInfo.setId(k);
		itemInfo.setCount((int) v);
		return itemInfo.build();

	}
	/*
	public static BaseMsg.RoleInfo buildRoleInfo(Role role) {
		EquipOp equipOp = PlayerCacheFactory.getCache(role.getPlayerId(), EquipOp.class);
		BaseMsg.RoleInfo.Builder builder = BaseMsg.RoleInfo.newBuilder();
		builder.setId(role.getDictId());
//		builder.setLevel(role.getLevel());
//		builder.setExp(role.getExp());
		builder.setStar(role.getStar());
		builder.setFriendly(role.getIntimacy());
		builder.setFriendlyLevel(role.getIntimacyLevel());
		builder.setOath(role.getPromise());
		builder.setGetTime((int) (role.getGetTime() / 1000));
		builder.setSkin(role.getSkin());

		Map<Integer, Integer> originMap = role.getOriginMap();
		for (Entry<Integer, Integer> entry : originMap.entrySet()) {
			builder.addOccupationNode(OccupationNodeInfo.newBuilder().setId(entry.getKey()).setLevel(entry.getValue()).build());
		}
		for (Integer treeId : role.getTreeList().keySet()) {
			builder.addOccupationTrees(treeId);
		}
		builder.setMechaLevel(role.getMechalevel());
		
		builder.addAllSkills(role.getSkillsMap().keySet());
		builder.setSkillPoint(role.getSkillPoint());
		builder.addAllAttrPoints(buidRoleAttrInfo(role.getAttrPointAddMap()));
		builder.addAllSkillsUsed(role.getSkillsUsedSet());
		builder.setSoulWeaponLevel(role.getSoulweaponLevel());
		
//		builder.setHp(role.getHp());
//		builder.setEp(role.getEp());
//		builder.setSan(role.getSan());
		builder.setState(role.getState());
		builder.setLock(role.getIsLock());
		builder.setNameId(role.getNameId());
		builder.addAllTagIds(role.getTagList());
		builder.addAllUnlockGiftIds(buildUnlockGiftIds(role));
		builder.setExploreTime((int)(role.getExploreTime() / 1000));
		builder.setPromotionLevel(role.getPromotionLevel());
		builder.setPromotionPoint(role.getPromotionPoint());
		builder.addAllEquips(equipOp.bulidEquipList(role.getDictId()));
		//		builder.addAllGiftIds(role.getGiftList());


//		SkillOp skillOp = PlayerCacheFactory.getCache(hero.getPlayerId(), SkillOp.class);
//
//		List<Skill> skillList = skillOp.list(hero.getDictId());
//		for (Skill skill : skillList) {
//			int id = PlayerHelper.makeId(skill.getId(), skill.getLevel());
//			builder.addSkillId(id);
//		}
		return builder.build();



	}

	private static List<Integer> buildUnlockGiftIds(Role role) {
		RoleOp roleOp = PlayerCacheFactory.getCache(role.getPlayerId(), RoleOp.class);
		return roleOp.getUnlockGiftCard(role.getDictId());
	}


	private static List<SkillInfo> buildAllSkillInfo(Map<Integer, List<Integer>> skillsMap) {
		List<SkillInfo> list = new ArrayList<>(3);
		for (Integer skillId : skillsMap.keySet()) {
			SkillInfo.Builder builder = SkillInfo.newBuilder();
			builder.setId(skillId);
			List<Integer> strengthenIds = skillsMap.get(skillId);
			for (Integer strengthenId : strengthenIds) {
				builder.addStrengthenIds(strengthenId);
			}
			list.add(builder.build());
		}
		return list;
	}

	
		public static BaseMsg.ChipInfo buildChipInfo(Chip chip) {
	
			BaseMsg.ChipInfo.Builder builder = BaseMsg.ChipInfo.newBuilder();
			builder.setUid(String.valueOf(chip.getId()));
			builder.setId(chip.getDictId());
			builder.setRoleId(chip.getRoleId());
			builder.setGrade(chip.getGrade());
			builder.setPosition(chip.getPosition());
	
	//		chip.getCommonAttrList().forEach(e -> {
	//			BaseMsg.ChipCommonAttrInfo.Builder attrinfo = BaseMsg.ChipCommonAttrInfo.newBuilder();
	//			attrinfo.setAttr(e.first);
	//			attrinfo.setInitvalue(e.second);
	//			attrinfo.setGrowthvalue(e.third);
	//			builder.addCommonAttrs(attrinfo);
	//		});
	
	//		builder.addAllAffixAttrs(buildAffixInfo(chip.getAffixAttrList(), BaseMsg.AttrType.OLDATTR));
	
	//		builder.addAllAffixAttrs(buildAffixInfo(chip.getNewaffixAttrList(), BaseMsg.AttrType.NEWATTR));
			builder.setLock(chip.getChecklock() == null || chip.getChecklock() == 0 ? false : true);
			builder.setGetTime((int) (chip.getGetTime() / 1000));
	
			return builder.build();
		}
	
	
		public static List<BaseMsg.ChipAffixAttrInfo> buildAffixInfo(List<FourTuple<Integer,Integer,Integer,Integer>> affixAttr, BaseMsg.AttrType type) {
			List<BaseMsg.ChipAffixAttrInfo> builders = new ArrayList<>();
	
			affixAttr.forEach(e -> {
				BaseMsg.ChipAffixAttrInfo.Builder info = BaseMsg.ChipAffixAttrInfo.newBuilder();
				info.setType(type);
				info.setAttr(e.first);
				info.setValue(e.second);
				info.setMinValue(e.third);
				info.setMaxValue(e.fourth);
				builders.add(info.build());
			});
			return builders;
		}*/

	public static List<RewardInfo> buildRewardInfo(List<RewardItem> list) {

		if (list == null || list.isEmpty()) {
			return new ArrayList<>(0);
		}
		List<RewardInfo> ret = new ArrayList<>(list.size());
		for (RewardItem entry : list) {
			ret.add(buildRewardInfo(entry));
		}
		return ret;
	}

	public static RewardPush_55000501 buildRewardPush(List<RewardInfo> rewardItems) {

		RewardMsg.RewardPush_55000501.Builder builder = RewardMsg.RewardPush_55000501.newBuilder();
		builder.addAllRewards(rewardItems);
		return builder.build();
	}

	public static RewardInfo buildRewardInfo(RewardItem item) {

		RewardInfo.Builder reward = RewardInfo.newBuilder();
		if (item.getId() > 0) {
			int type = ItemHelper.getGoodsType(item.getId());

			if (type == GoodsTypeEnum.Resource.getId()) {
				Builder b = ResourceInfo.newBuilder().setId(item.getId()).setCount(item.getCount());
				reward.setResource(b);

			} else if (type == GoodsTypeEnum.Item.getId()) {
				BaseMsg.ItemInfo.Builder itemInfo = BaseMsg.ItemInfo.newBuilder();
				itemInfo.setId(item.getId());
				itemInfo.setCount(item.getCount());
				reward.setItem(itemInfo);
			}
		}  else if (item.getEquip() != null) {
			reward.setEquip(buildEquipInfo(item.getEquip()));
		}
		return reward.build();

	}

	public static List<RewardInfo> buildRewardInfos(Collection<RewardItem> items) {
		if (items == null || items.isEmpty()) {
			return new ArrayList<>(0);
		}
		return items.stream().map(PbBuilder::buildRewardInfo).collect(toList());
	}

	public static List<MissionInfo> buildQuestByGroup(Long playerId, MissionTypeEnum missionTypeEnum, int type) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		QuestModule questOp = player.getModule(QuestModule.class);
		Map<Integer, Quest> group2 = type == 0 ? questOp.getGroup(missionTypeEnum) : questOp.getCompeteGroup(missionTypeEnum.getId());

		List<MissionInfo> list = new ArrayList<>();

		for (Quest e : group2.values()) {

			list.add(buildMissionInfo(e));
		}
		return list;
	}
	public static List<MissionInfo> buildQuestByGroup(Long playerId, MissionTypeEnum missionTypeEnum) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		QuestModule questOp = player.getModule(QuestModule.class);
		Map<Integer, Quest> group2 = questOp.getGroup(missionTypeEnum);

		List<MissionInfo> list = new ArrayList<>();

		for (Quest e : group2.values()) {

			list.add(buildMissionInfo(e));
		}
		return list;
	}

	public static MissionInfo buildMissionInfo(Quest quest) {

		MissionInfo.Builder questInfo = MissionInfo.newBuilder();
		questInfo.setId(quest.getId());
		List<Condition> questConditionList = quest.getRequires();
		if (questConditionList != null) {
			for (int i = 0; i < questConditionList.size(); i++) {
				long count = questConditionList.get(i).getFinishCount();
				if (count >= Integer.MAX_VALUE) {
					count = Integer.MAX_VALUE;
				}
				questInfo.addFinishCount((int) count);
			}
		}
		questInfo.setState(quest.getState());
		return questInfo.build();
	}

//	public static ActivityInfo buildActivityInfo(int id) {
//		ActivityInfo.Builder builder = ActivityInfo.newBuilder();
//
//		builder.setId(id);
//		// builder.setStartTime(value);
//		// builder.setState(value);
//
//		return builder.build();
//	}

	public static Collection<MailInfo> buildAllMailInfo(Collection<Mail> mails) {

		return mails.stream().map(PbBuilder::buildMailInfo).collect(toList());
	}

	public static MailInfo buildMailInfo(Mail mail) {
		MailInfo.Builder builder = MailInfo.newBuilder();
		builder.setUid(mail.getId() + "");
		builder.setContent(mail.getContent()) ; 
		builder.setExpireTime((int) (mail.getCreateTime() + DateUtil.DAY_SECONDS * 30));
		builder.setReceive(mail.getReceive());
		builder.setSee(mail.getSee());
		builder.setSender(mail.getSender());
		builder.setTime(mail.getCreateTime());
		builder.setTitle(mail.getTitle());
		builder.setType(mail.getType());

		List<GoodsInfo> collect = mail.getAttachmentList().stream().map(PbBuilder::buildGoodsInfo).collect(toList());
		builder.addAllAttachments(collect);

		return builder.build();
	}

	public static GoodsInfo buildGoodsInfo(Goods goods) {
		return GoodsInfo.newBuilder().setId(goods.getId()).setCount(goods.getCount()).build();
	}
	public static GoodsInfo buildGoodsInfo(int id, int count) {
		return GoodsInfo.newBuilder().setId(id).setCount(count).build();
	}

	public static UnionMsg.UnionInfo buildUnionInfo(Union union, long playerId) throws Exception {
		UnionMsg.UnionInfo.Builder info = UnionMsg.UnionInfo.newBuilder();
		Member member = UnionManager.getInstance().getMember(playerId);
		if (member != null) {
			info.setTitle(member.getTitle());
			info.setContribution(member.getContribution().intValue());
		}
		info.setUnion(buildUnionBaseInfo(union, playerId));
		Collection<Member> members = UnionManager.getInstance().getMembers(union.getId());

		for (Member m : members) {
			info.addMembers(buildMemberInfo(m));
		}

//		info.setJournals(buildJournalInfo());
//		info.setApplications(buildApplicationInfo());
		return info.build();
	}
	public static UnionMsg.UnionBaseInfo buildUnionBaseInfo(Union union, long playerId) {
		UnionMsg.UnionBaseInfo.Builder info = UnionMsg.UnionBaseInfo.newBuilder();
		info.setLevel(union.getLevel()) ;
		info.setMasterName(union.getMaster() == null?"" :union.getMaster().getName()) ;
		info.setName(union.getName()) ;
		info.setNotice(union.getNotice()) ;
		info.setUid(union.getId() + "");
		// TODO
		info.setStatus(0);
		return info.build();
	}
	public static UnionMsg.MemberInfo buildMemberInfo(Member member) throws Exception {
		UnionMsg.MemberInfo.Builder builder = UnionMsg.MemberInfo.newBuilder();
		builder.setTitle(member.getTitle());
		builder.setContribution(member.getContribution().intValue());
		builder.setPlayer(buildSimplePlayerInfo(member.getPlayerId()));
		return builder.build();
	}
	

	// List<Pair(goodsId,可买数量)> list ->Collection<StoreMsg.StoreGoodsInfo>
	public static Collection<GoodsInfo> buildStoreGoodsInfo(List<Pair<Integer, Integer>> goodsList1) {
		List<GoodsInfo> goodsInfos = new ArrayList<>();
		for (Pair<Integer, Integer> goods : goodsList1) {
			GoodsInfo.Builder builder = GoodsInfo.newBuilder();
			builder.setId(goods.first);
			builder.setCount(goods.second);
			goodsInfos.add(builder.build());
		}
		Collections.sort(goodsInfos, new Comparator<GoodsInfo>() {
			@Override
			public int compare(GoodsInfo p1, GoodsInfo p2) {
				return p1.getId() - p2.getId();
			}
		});
		return goodsInfos;
	}

	// 推荐商店列表
	public static Collection<StoreRecommendInfo> buildStoreRecommendInfo(
			List<Pair<Integer, List<Integer>>> allRecommends) {
		List<StoreRecommendInfo> storeRecommendInfos = new ArrayList<>();
		for (Pair<Integer, List<Integer>> recommend : allRecommends) {
			StoreRecommendInfo.Builder builder = StoreRecommendInfo.newBuilder();
			builder.setId(recommend.first);
			if (recommend.second != null) {
				for (Integer giftId : recommend.second) {
					builder.addGiftId(giftId);
				}
			}
			storeRecommendInfos.add(builder.build());
		}
		return storeRecommendInfos;
	}
		
	/**
	 * 根据群组消息获取一个群组的简单消息 阻塞 不可主线程调用
	 * 
	 * @param oneGroup
	 * @return
	 */
	public static ChatGroupBriefInfo getGroupBriefInfo(Group oneGroup, List<GroupMember> groupMembers) {
		if(oneGroup == null || groupMembers == null)
			return null;
		
		if(groupMembers.size() < 1)
			return null;
		
		ChatGroupBriefInfo.Builder groupBriefBuilder = ChatGroupBriefInfo.newBuilder();
		groupBriefBuilder.setId(oneGroup.getId().toString());
		groupBriefBuilder.setName(oneGroup.getName());
		groupBriefBuilder.setHeadId(oneGroup.getHeadIcon());
		groupBriefBuilder.setNotice(oneGroup.getNotice());			
		groupBriefBuilder.setMemberCount(groupMembers.size());
		
		int online = 0;
		for (GroupMember groupMember : groupMembers) {
			if(GameServer.getInstance().isLocalServer(groupMember.getPlayerServerId())) {
				Player player = PlayerManager.getInstance().getPlayer(groupMember.getPlayerId());
				if(player == null)
					continue;
				
				online++;
			}else {
				SimplePlayerInfo simplePlayerInfo = PlayerManager.getInstance().getSimpleOtherPlayerInfo(groupMember.getPlayerId().toString(),
						groupMember.getPlayerServerId());
				
				if(simplePlayerInfo.getOnline())
					online++;
			}
		}
		
		groupBriefBuilder.setOnlineCount(online);
		groupBriefBuilder.setManagerId(oneGroup.getManagerId().toString());
		groupBriefBuilder.setServerId(ServerContext.getInstance().getServerId());
		return groupBriefBuilder.build();		
	}
	
	public static ChatGroupBriefInfo getGroupBriefInfo(Group oneGroup) {
		if(oneGroup == null)
			return null;
		
		ChatGroupBriefInfo.Builder groupBriefBuilder = ChatGroupBriefInfo.newBuilder();
		groupBriefBuilder.setId(oneGroup.getId().toString());
		groupBriefBuilder.setName(oneGroup.getName());
		groupBriefBuilder.setHeadId(oneGroup.getHeadIcon());
		groupBriefBuilder.setNotice(oneGroup.getNotice());			
		groupBriefBuilder.setMemberCount(oneGroup.getMemberCount());						
		groupBriefBuilder.setOnlineCount(oneGroup.getOnlineCount());
		groupBriefBuilder.setManagerId(oneGroup.getManagerId().toString());
		groupBriefBuilder.setServerId(oneGroup.getServerId());
		return groupBriefBuilder.build();		
	}
	
	/**
	 * 根据群组消息获取一个群组的简单消息 阻塞 不可主线程调用
	 * 
	 * @param oneGroup
	 * @return
	 */
	public static ChatGroupBriefInfo getGroupBriefInfo(Group oneGroup, int online, int count) {
		ChatGroupBriefInfo.Builder groupBriefBuilder = ChatGroupBriefInfo.newBuilder();
		groupBriefBuilder.setId(oneGroup.getId().toString());
		groupBriefBuilder.setName(oneGroup.getName());
		groupBriefBuilder.setHeadId(oneGroup.getHeadIcon());
		groupBriefBuilder.setNotice(oneGroup.getNotice());			
		groupBriefBuilder.setMemberCount(count);				
		groupBriefBuilder.setOnlineCount(online);
		groupBriefBuilder.setManagerId(oneGroup.getManagerId().toString());
		groupBriefBuilder.setServerId(ServerContext.getInstance().getServerId());
		return groupBriefBuilder.build();		
	}
	
	public static Group getGroup(Group oneGroup, List<GroupMember> groupMembers) {	
		
		int online = 0;
		for (GroupMember groupMember : groupMembers) {
			if(GameServer.getInstance().isLocalServer(groupMember.getPlayerServerId())) {
				Player player = PlayerManager.getInstance().getPlayer(groupMember.getPlayerId());
				if(player == null)
					continue;
				
				online++;
			}else {
				SimplePlayerInfo simplePlayerInfo = PlayerManager.getInstance().getSimpleOtherPlayerInfo(groupMember.getPlayerId().toString(),
						groupMember.getPlayerServerId());
				
				if(simplePlayerInfo.getOnline())
					online++;
			}
		}

		oneGroup.setMemberCount(groupMembers.size());
		oneGroup.setOnlineCount(online);		
		oneGroup.setServerId(ServerContext.getInstance().getServerId());
		return oneGroup;	
	}
	
	public static GroupAllInfo getGroupAllInfo(ChatGroupInfo chatGroupInfo) {
		ChatGroupBriefInfo briefInfo = chatGroupInfo.getBriefInfo();
		Group group = new Group();
		group.setId(Long.parseLong(briefInfo.getId()));
		group.setHeadIcon(briefInfo.getHeadId());
		group.setName(briefInfo.getName());
		group.setNotice(briefInfo.getNotice());
		group.setMemberCount(briefInfo.getMemberCount());
		group.setOnlineCount(briefInfo.getOnlineCount());
		group.setManagerId(Long.parseLong(briefInfo.getManagerId()));
		group.setServerId(briefInfo.getServerId());
		
		List<SimplePlayer> simplePlayers = new ArrayList<SimplePlayer>();
		
		for (SimplePlayerInfo simplePlayerInfo : chatGroupInfo.getPlayerInfosList()) {
			SimplePlayer simplePlayer = buildSimplePlayer(simplePlayerInfo);
			simplePlayers.add(simplePlayer);
		}
		
		GroupAllInfo groupAllInfo = new GroupAllInfo(group, simplePlayers);		
		return groupAllInfo;
	}
	
	public static ChatGroupInfo buildChatGroupInfo(GroupAllInfo groupAllInfo) {
		if(groupAllInfo == null)
			return null;
				
		ChatGroupInfo.Builder chatGroupInfo = ChatGroupInfo.newBuilder();
		ChatGroupBriefInfo.Builder cBuilder = ChatGroupBriefInfo.newBuilder();
		
		cBuilder.setId(groupAllInfo.getGroup().getId().toString());
		cBuilder.setName(groupAllInfo.getGroup().getName());
		cBuilder.setHeadId(groupAllInfo.getGroup().getHeadIcon());
		cBuilder.setNotice(groupAllInfo.getGroup().getNotice());
		cBuilder.setMemberCount(groupAllInfo.getGroup().getMemberCount());
		cBuilder.setOnlineCount(groupAllInfo.getGroup().getOnlineCount());
		cBuilder.setManagerId(groupAllInfo.getGroup().getManagerId().toString());
		cBuilder.setServerId(groupAllInfo.getGroup().getServerId());
		
		List<SimplePlayerInfo> simplePlayerInfos = new ArrayList<BaseMsg.SimplePlayerInfo>();
		for (SimplePlayer simplePlayer : groupAllInfo.getSimplePlayers()) {
			SimplePlayerInfo simplePlayerInfo = buildSimplePlayerInfo(simplePlayer);
			simplePlayerInfos.add(simplePlayerInfo);
		}
		
		chatGroupInfo.setBriefInfo(cBuilder.build());
		chatGroupInfo.addAllPlayerInfos(simplePlayerInfos);
		
		return chatGroupInfo.build();
	}


	public static MissionChallengeGroupInfo buildQuestChallengeGroupInfo(QuestChallenge questChallenge) {
		MissionChallengeGroupInfo.Builder builder = MissionChallengeGroupInfo.newBuilder();
		builder.setId(questChallenge.getId());
		builder.setReward(questChallenge.getFinish());
		builder.setScore(questChallenge.getScore());
		return builder.build();
		
	}

	/*
		public static List<RoleAttrInfo> buildRoleAttrInfo(Role role) {
			List<RoleAttrInfo> list = new ArrayList<>();
			List<RoleAttributeConfig> typesubTypeList = RoleAttributeManager.getInstance().getTypesubTypeList(AttributeTypeEnum.hp, AttributeSubTypeEnum.cur);
			int hpCurId = typesubTypeList.get(0).getId();
			RoleAttrInfo.Builder rHp = RoleAttrInfo.newBuilder();
			rHp.setId(hpCurId);
			rHp.setValue(role.getHp());
			list.add(rHp.build());
	
			typesubTypeList = RoleAttributeManager.getInstance().getTypesubTypeList(AttributeTypeEnum.hp, AttributeSubTypeEnum.curTotal);
			int hpCurMaxId = typesubTypeList.get(0).getId();
			RoleAttrInfo.Builder hpCurMaxInfo = RoleAttrInfo.newBuilder();
			hpCurMaxInfo.setId(hpCurMaxId);
			hpCurMaxInfo.setValue(role.getHpCurMax());
			list.add(hpCurMaxInfo.build());
	
	//		RoleAttrInfo.Builder rEp = RoleAttrInfo.newBuilder();
	//		rEp.setId(AttributeTypeEnum.ep.getId());
	//		rEp.setValue(role.getEp());
	//		list.add(rEp.build());
			
			typesubTypeList = RoleAttributeManager.getInstance().getTypesubTypeList(AttributeTypeEnum.san, AttributeSubTypeEnum.cur);
			int sanCurId = typesubTypeList.get(0).getId();
			RoleAttrInfo.Builder rSan = RoleAttrInfo.newBuilder();
			rSan.setId(sanCurId);
			rSan.setValue(role.getSan());
			list.add(rSan.build());
			
			typesubTypeList = RoleAttributeManager.getInstance().getTypesubTypeList(AttributeTypeEnum.san, AttributeSubTypeEnum.curTotal);
			int sanCurMaxId = typesubTypeList.get(0).getId();
			RoleAttrInfo.Builder sanCurMaxInfo = RoleAttrInfo.newBuilder();
			sanCurMaxInfo.setId(sanCurMaxId);
			sanCurMaxInfo.setValue(role.getSanCurMax());
			list.add(sanCurMaxInfo.build());
			
			return list;
		}
	
		public static StoryInfo buildStoryInfo(Story story) {
			StoryInfo.Builder builder = StoryInfo.newBuilder();
			builder.setId(story.getStory());
			builder.setCount(story.getStartConditionCount());
			builder.setFinish(story.getFinish());
			return builder.build();
		}
	
	
		public static List<TowerPlayerInfo> buildClimbingTowerGroupInfo(List<SimplePlayer> s_players, Collection<ClimbingTower> players) {
			List<TowerPlayerInfo> res = new ArrayList<>(20);
			int index = 0;
			for (ClimbingTower climbingTower : players) {
				SimplePlayer s_player = s_players.get(index++);
				TowerPlayerInfo.Builder builder = TowerPlayerInfo.newBuilder();
				builder.setHead(s_player.head);
				builder.setName(s_player.name);
				builder.setScore(climbingTower.getScore());
				builder.setScoreTime((int) (climbingTower.getScoreTime() / 1000));
				res.add(builder.build());
			}
			return res;
		}
	
	
		public static List<RoleAttrPointInfo> buidRoleAttrInfo(Map<Integer, Integer> map) {
			List<RoleAttrPointInfo> list = new ArrayList<>(4);
			for (Integer key : map.keySet()) {
				RoleAttrPointInfo.Builder builder = RoleAttrPointInfo.newBuilder();
				builder.setId(key);
				builder.setValue(map.get(key));
				list.add(builder.build());
			}
			return list;
		}
		*/
	

	public static List<ForbidAccountInfo> buildForbidAccount(List<ForbidAccount> accounts) {
		List<ForbidAccountInfo> res = new ArrayList<>();
		for (ForbidAccount f : accounts) {
			ForbidAccountInfo.Builder builder = ForbidAccountInfo.newBuilder();
			builder.setPlayerId(f.getPlayerId() + "");
			builder.setName(f.getName());
			builder.setLevel(f.getLevel());
			builder.setReason(f.getReason());
			builder.setUnblockTime(DateUtil.getTimeByPattern(f.getUnblockTime()));

			res.add(builder.build());
		}
		return res;
	}

	public static List<GoodsInfo> buildGoodsInfo(Map<Integer, Integer> map) {
		List<GoodsInfo> list = new ArrayList<>();

		for (Integer key : map.keySet()) {
			GoodsInfo.Builder goodsInfo = GoodsInfo.newBuilder();
			goodsInfo.setId(key);
			goodsInfo.setCount(map.get(key));
			list.add(goodsInfo.build());
		}
		return list;
	}

	public static List<GoodsInfo> buildGoodsInfo(List<Entry<Integer, Integer>> reward) {
		List<GoodsInfo> list = new ArrayList<>();
		for (Map.Entry<Integer, Integer> entry : reward) {
			GoodsInfo.Builder goodsInfo = GoodsInfo.newBuilder();
			goodsInfo.setId(entry.getKey());
			goodsInfo.setCount(entry.getValue());

			list.add(goodsInfo.build());
		}
		return list;
	}

	private static List<BuffMsg.BuffInfo> buildBuffs(Multimap<Long, Buff> buffs) {
		List<BuffMsg.BuffInfo> list = new ArrayList<>();
		buffs.asMap().forEach((k, v) -> {
			for (Buff buff : v) {
				// 不同步buff
				boolean notSync = BuffHelper.notSync(buff.getBuffId());
				if (notSync) {
					continue;
				}
				BuffInfo buffInfo = buildBuffInfo(buff);
				list.add(buffInfo);
			}
		});

		return list;
	}

	public static BuffInfo buildBuffInfo(Buff buff) {
		BuffInfo.Builder build = BuffInfo.newBuilder();
		build.setUid(buff.getId() + "");
		build.setId(buff.getBuffId());
		build.setUse(buff.getUseNum());
		build.setRound(buff.getRound());
		build.setTarget(buff.getTarget().toString());
		return build.build();
	}


	public static BuffMsg.BuffShowInfo buildBuffShowInfo(List<Buff> buffs) {
		BuffMsg.BuffShowInfo.Builder build = BuffMsg.BuffShowInfo.newBuilder();
		for (Buff buff : buffs) {
			build.setBuffId(buff.getBuffId());
			Long target = buff.getTarget();
			if (target != null) {
				build.addTargetIds(target.toString());
			}
			if (buff.getRewards() != null) {
				build.addAllRewards(buff.getRewards());
			}
		}
		return build.build();
	}

	public static List<StoreGoodsInfo> buildStoreGoodsInfos(Collection<StoreGoods> target) {
		List<StoreGoodsInfo> res = new ArrayList<>();
		for (StoreGoods g : target) {
			res.add(buildStoreGoodsInfo(g));
		}
		return res;
	}

	public static StoreGoodsInfo buildStoreGoodsInfo(StoreGoods g) {
		StoreGoodsInfo.Builder build = StoreGoodsInfo.newBuilder();
		build.setUid(g.getUid() + "");
		build.setId(g.getId());
		build.setCount(g.getCount());
		build.setCostId(g.getCostId());
		build.setCostCount(g.getCostCount());
		Equip e = g.getEquip();
		if (e != null) {
			build.setEquip(buildEquipInfo(e));
		}
		return build.build();
	}

	/*
		public static List<RoleUpdateInfo> buildRoleUpdateInfo(List<? extends Role> infos){
			List<RoleUpdateInfo> roleUpdateInfos = new ArrayList<RoleUpdateInfo>();
			for (Role roleInfo : infos) {
				RoleUpdateInfo.Builder builder = RoleUpdateInfo.newBuilder();
				builder.setId(roleInfo.getDictId());
				builder.setStateValue(roleInfo.getState());
				builder.addAllRoleAttrs(buildRoleAttrInfo(roleInfo));
				roleUpdateInfos.add(builder.build());
			}
			return roleUpdateInfos;
		}
			public static PlayerArchiveInfo buildPlayerArchiveInfo(Player player) {
				PlayerArchiveInfo.Builder builder = PlayerArchiveInfo.newBuilder();
				builder.setId(player.getData().getPlayerId().intValue()) ; 
				builder.setName(player.getData().getName()) ; 
				builder.setGameTime(player.getData().getGameTime());
				builder.setOfflineTime((player.getData().getOfflineTime().toString()));
				builder.setLoginTime(DateUtil.parse(player.getData().getLoginDate()).getTime() + "");
				return builder.build();
			}
			public static List<PlayerArchiveInfo> buildPlayerArchiveInfos(List<Player> players) {
				return players.stream().map(PbBuilder::buildPlayerArchiveInfo).collect(toList());
			}
	*/
}
