package cn.game.games.util;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import cn.game.core.base.ServerContext;
import cn.game.games.cache.entity.Equip;
import cn.game.games.cache.entity.ForbidAccount;
import cn.game.games.cache.entity.Friend;
import cn.game.games.cache.entity.Mail;
import cn.game.games.cache.entity.Member;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.Union;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.SimplePlayer;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.manager.UnionManager;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.quest.Quest;
import cn.game.games.net.game.module.quest.QuestChallenge;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.protocol.generated.config.MailConfig;
import cn.game.protocol.generated.enume.QuestTypeEnum;
import cn.game.protocol.generated.manager.MailManager;
import cn.game.protocol.protobuf.BaseMsg;
import cn.game.protocol.protobuf.BaseMsg.AssetInfo;
import cn.game.protocol.protobuf.BaseMsg.EquipInfo;
import cn.game.protocol.protobuf.BaseMsg.GoodsInfo;
import cn.game.protocol.protobuf.BaseMsg.ItemInfo;
import cn.game.protocol.protobuf.BaseMsg.PlayerShowInfo;
import cn.game.protocol.protobuf.BaseMsg.SimplePlayerInfo;
import cn.game.protocol.protobuf.FriendMsg.FriendInfo;
import cn.game.protocol.protobuf.FriendMsg.FriendRelationInfo;
import cn.game.protocol.protobuf.GmMsg.ForbidAccountInfo;
import cn.game.protocol.protobuf.MailMsg.MailInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo;
import cn.game.protocol.protobuf.QuestMsg.QuestChallengeGroupInfo;
import cn.game.protocol.protobuf.QuestMsg.QuestInfo;
import cn.game.protocol.protobuf.RewardMsg;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.RewardMsg.RewardPush_55000501;
import cn.game.protocol.protobuf.UnionMsg;
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

	public static EquipInfo buildEquipInfo(Equip e) {
		EquipInfo.Builder builder = EquipInfo.newBuilder();
		builder.setUid(e.getId() + "");
//		builder.setId(e.getDictId());
		// TODO 其他属性
		return builder.build();
	}


	public static List<AssetInfo> buildResourceInfo(Map<Integer, Integer> map) {
		List<AssetInfo> list = new ArrayList<>(map.size());
		for (int key : map.keySet()) {
			list.add(AssetInfo.newBuilder().setId(key).setCount(map.get(key)).build());
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
	 * 根据在线玩家对象构建玩家数据，需要保证参数不能为空
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

	public static RewardPush_55000501 buildRewardPush(List<RewardInfo> rewardItems) {

		RewardMsg.RewardPush_55000501.Builder builder = RewardMsg.RewardPush_55000501.newBuilder();
		builder.addAllRewards(rewardItems);
		return builder.build();
	}



	public static List<QuestInfo> buildQuestByGroup(Long playerId, QuestTypeEnum missionTypeEnum, int type) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		QuestModule questOp = player.getModule(QuestModule.class);
		Map<Integer, Quest> group2 = questOp.getGroup(missionTypeEnum);

		List<QuestInfo> list = new ArrayList<>();

		for (Quest e : group2.values()) {

			list.add(e.toQuestInfo());
		}
		return list;
	}

	public static List<QuestInfo> buildQuestByGroup(Long playerId, QuestTypeEnum missionTypeEnum) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		QuestModule questOp = player.getModule(QuestModule.class);
		Map<Integer, Quest> group2 = questOp.getGroup(missionTypeEnum);

		List<QuestInfo> list = new ArrayList<>();

		for (Quest e : group2.values()) {

			list.add(e.toQuestInfo());
		}
		return list;
	}

	public static Collection<MailInfo> buildAllMailInfo(Collection<Mail> mails) {

		return mails.stream().filter(mail -> !mail.getIsDeleted()).map(PbBuilder::buildMailInfo).collect(toList());
	}

	public static MailInfo buildMailInfo(Mail mail) {
		MailConfig mailConfig = null;
		int mailId = mail.getMailId();
		if (mailId > 0) {
			mailConfig = MailManager.instance().get(mailId);
		}
		MailInfo.Builder builder = MailInfo.newBuilder();
		builder.setUid(mail.getId() + "");
		builder.setContent(mail.getContent()) ; 
		builder.setExpireTime(mailConfig != null ? (int) (mail.getCreateTime() + mailConfig.Expiration) : 0);
		builder.setReceive(mail.getReceive());
		builder.setSee(mail.getSee());
		builder.setSender(mail.getSender());
		builder.setTime(mail.getCreateTime());
		builder.setTitle(mail.getTitle());
		builder.setType(mail.getType());
		builder.setId(mailId);

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

	public static QuestChallengeGroupInfo buildQuestChallengeGroupInfo(QuestChallenge questChallenge) {
		QuestChallengeGroupInfo.Builder builder = QuestChallengeGroupInfo.newBuilder();
		builder.setId(questChallenge.getId());
		builder.setReward(questChallenge.getFinish());
		builder.setScore(questChallenge.getScore());
		return builder.build();
		
	}

	public static List<ForbidAccountInfo> buildForbidAccount(List<ForbidAccount> accounts) {
		List<ForbidAccountInfo> res = new ArrayList<>();
		for (ForbidAccount f : accounts) {
			ForbidAccountInfo.Builder builder = ForbidAccountInfo.newBuilder();
			builder.setPlayerId(f.getPlayerId() + "");
			builder.setName(f.getName());
			builder.setReason(f.getReason());
//			builder.setEndTime(f.getEndTime());)
//			builder.setUnblockTime(DateUtil.getTimeByPattern(f.getUnblockTime()));

			res.add(builder.build());
		}
		return res;
	}

	/*
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
