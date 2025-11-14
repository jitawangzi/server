package cn.game.games.util;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import cn.game.games.cache.entity.Equip;
import cn.game.games.cache.entity.ForbidAccount;
import cn.game.games.cache.entity.Mail;
import cn.game.games.cache.entity.Member;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.Union;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.SimplePlayer;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.manager.UnionManager;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.mail.MailRankInfo;
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
import cn.game.protocol.protobuf.BaseMsg.SimplePlayerInfo;
import cn.game.protocol.protobuf.GmMsg.ForbidAccountInfo;
import cn.game.protocol.protobuf.MailMsg;
import cn.game.protocol.protobuf.MailMsg.MailInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo;
import cn.game.protocol.protobuf.QuestMsg.QuestChallengeGroupInfo;
import cn.game.protocol.protobuf.QuestMsg.QuestInfo;
import cn.game.protocol.protobuf.RewardMsg;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.RewardMsg.RewardPush_55000501;
import cn.game.util.DateUtil;
import cn.game.util.JsonUtil;
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

	public static List<SimplePlayerInfo> buildSimplePlayerInfos(List<SimplePlayer> players) {
		List<SimplePlayerInfo> list = new ArrayList<BaseMsg.SimplePlayerInfo>();
		for (SimplePlayer simplePlayer : players) {
			list.add(simplePlayer.toSimplePlayerInfo());
		}
		return list;
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
		builder.setExpireTime(mail.getExpireTime());
		builder.setReceive(mail.getReceive());
		builder.setSee(mail.getSee());
		builder.setSender(mail.getSender());
		builder.setTime(mail.getCreateTime());
		builder.setTitle(mail.getTitle());
		builder.setType(mail.getType());
		builder.setId(mailId);
        if( mail.getAttachmentList()!=null) {
			List<GoodsInfo> collect = mail.getAttachmentList().stream().map(PbBuilder::buildGoodsInfo).collect(toList());
			builder.addAllAttachments(collect);
		}
		if (mail.getType() == 3) {
			List<MailRankInfo> mailRankInfos = JsonUtil.parseObjectWithType(mail.getContent());
			mailRankInfos.forEach(mailRankInfo ->
			{
				builder.addRanks(MailMsg.MailRankInfo.newBuilder()
						.setFigure(mailRankInfo.getFigure())
						.setName(mailRankInfo.getName())
						.setRankId(mailRankInfo.getRankId())
						.setPlayerId(String.valueOf(mailRankInfo.getPlayerId()))
						.build());
			});
		}
		return builder.build();
	}

	public static GoodsInfo buildGoodsInfo(Goods goods) {
		return GoodsInfo.newBuilder().setId(goods.getId()).setCount(goods.getCount()).build();
	}

	public static GoodsInfo buildGoodsInfo(int id, long count) {
		return GoodsInfo.newBuilder().setId(id).setCount((int) count).build();
	}
	public static GoodsInfo buildGoodsUpdateInfo(int id, long count) {
		return GoodsInfo.newBuilder().setId(id).setCount((int) count).build();
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
			builder.setType(f.getType());
//			builder.setEndTime(f.getEndTime());)
//			builder.setUnblockTime(DateUtil.getTimeByPattern(f.getUnblockTime()));

			res.add(builder.build());
		}
		return res;
	}
}
