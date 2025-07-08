package cn.game.games.net.game.module.award;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.TextFormat;

import cn.game.games.cache.entity.Equip;
import cn.game.games.cache.entity.Hero;
import cn.game.games.cache.entity.Item;
import cn.game.games.net.game.module.currency.Currency;
import cn.game.games.net.game.module.develop.gem.Gem;
import cn.game.games.net.game.module.develop.hero.skin.HeroSkin;
import cn.game.games.net.game.module.develop.mergeequip.MergeEquip;
import cn.game.games.net.game.module.develop.pet.Pet;
import cn.game.games.net.game.module.develop.secretscript.Secretscript;
import cn.game.games.net.game.module.player.figure.Figure;
import cn.game.games.net.game.module.player.headbox.HeadBox;
import cn.game.protocol.protobuf.BaseMsg.AssetInfo;
import cn.game.protocol.protobuf.BaseMsg.EquipInfo;
import cn.game.protocol.protobuf.BaseMsg.GemInfo;
import cn.game.protocol.protobuf.BaseMsg.ItemInfo;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public class RewardHelper {
	public static RewardInfo toRewardInfo(Item item) {
		RewardInfo.Builder builder = RewardInfo.newBuilder();
		if (item.getClass() == Item.class) {
			builder.setItem(ItemInfo.newBuilder().setId(item.getConfigId()).setCount(item.getCount().intValue()));
			return builder.build();
		}
		if (item instanceof Currency) {
			builder.setAsset(AssetInfo.newBuilder().setId(item.getConfigId()).setCount(item.getCount().intValue()));
		} else if (item instanceof MergeEquip) {
			builder.setMergeEquip(((MergeEquip) item).toMergeEquipProto());
		} else if (item instanceof Pet) {
			builder.setPet(((Pet) item).toPetInfo());
		} else if (item instanceof Secretscript) {
			builder.setSecretscript(((Secretscript) item).toProtoInfo());
		} else if (item instanceof Secretscript) {
			builder.setSecretscript(((Secretscript) item).toProtoInfo());
		} else if (item instanceof Equip) {
			Equip equip = (Equip) item;
			builder.setEquip(
					EquipInfo.newBuilder().setConfigId(equip.getConfigId()).setUid(equip.getId() + "").putAllAttrs(equip.getEquipAttrs()));
		} else if (item instanceof Hero) {
			Hero obj = (Hero) item;
			builder.setRole(obj.toHeroInfo());
		} else if (item instanceof HeadBox) {
			HeadBox obj = (HeadBox) item;
			builder.setHead(obj.getConfigId());
		} else if (item instanceof HeroSkin) {
			HeroSkin obj = (HeroSkin) item;
			builder.setHeroSkin(obj.getConfigId());
		} else if (item instanceof Figure) {
			Figure obj = (Figure) item;
			builder.setFigure(obj.getConfigId());
		} else if (item instanceof Gem) {
			Gem obj = (Gem) item;
			builder.setGem(GemInfo.newBuilder().setConfigId(obj.getConfigId()).setUid(obj.getId() + "").putAllAttrs(obj.getGemAttrs()));
		} else {
			throw new IllegalArgumentException("toRewardInfo not implement, item class is " + item.getClass().getName());
		}
		return builder.build();
	}

	/**
	 * 合并同id 的资源和道具的数量。
	 * @param rewards
	 */
	public static void mergeRewards(List<RewardInfo> rewards) {
		// 用Map归并id到数量
		Map<Integer, Integer> itemMap = new HashMap<>();
		Map<Integer, Long> assetMap = new HashMap<>();
		List<RewardInfo> others = new ArrayList<>(); // 其他类型的奖励

		for (RewardInfo rewardInfo : rewards) {
			if (rewardInfo.hasItem()) {
				ItemInfo item = rewardInfo.getItem();
				itemMap.put(item.getId(), itemMap.getOrDefault(item.getId(), 0) + item.getCount());
			} else if (rewardInfo.hasAsset()) {
				AssetInfo asset = rewardInfo.getAsset();
				assetMap.put(asset.getId(), assetMap.getOrDefault(asset.getId(), 0L) + asset.getCount());
			} else {
				// 其它类型奖励，直接保留
				others.add(rewardInfo);
			}
		}

		// 清空原列表
		rewards.clear();

		// 添加合并后的 ItemInfo
		for (Map.Entry<Integer, Integer> entry : itemMap.entrySet()) {
			ItemInfo item = ItemInfo.newBuilder().setId(entry.getKey()).setCount(entry.getValue()).build();
			rewards.add(RewardInfo.newBuilder().setItem(item).build());
		}

		// 添加合并后的 AssetInfo
		for (Map.Entry<Integer, Long> entry : assetMap.entrySet()) {
			AssetInfo asset = AssetInfo.newBuilder().setId(entry.getKey()).setCount(entry.getValue()).build();
			rewards.add(RewardInfo.newBuilder().setAsset(asset).build());
		}

		// 添加未合并的其他类型奖励
		rewards.addAll(others);
	}

	/** 
	 * 给已经生成的奖励，增加奖励倍数 
	 * @param rewards
	 * @param addition 万分比奖励加成
	 */
	@Deprecated
	public static void oldRewardAddition(List<RewardInfo> rewards, int addition) {
		if (addition <= 0 || rewards == null || rewards.isEmpty()) {
			return;
		}

		final float multiplier = 1f + addition / 10000f;
		List<RewardInfo> newRewards = new ArrayList<>(rewards.size());

		for (RewardInfo rewardInfo : rewards) {
			RewardInfo.Builder rewardBuilder = RewardInfo.newBuilder();

			if (rewardInfo.hasItem()) {
				ItemInfo item = rewardInfo.getItem();
				ItemInfo.Builder itemBuilder = item.toBuilder();
				int newCount = Math.round(item.getCount() * multiplier);
				itemBuilder.setCount(newCount);
				rewardBuilder.setItem(itemBuilder);
			} else if (rewardInfo.hasAsset()) {
				AssetInfo asset = rewardInfo.getAsset();
				AssetInfo.Builder assetBuilder = asset.toBuilder();
				int newCount = Math.round(asset.getCount() * multiplier);
				assetBuilder.setCount(newCount);
				rewardBuilder.setAsset(assetBuilder);
			} else {
				// 其他类型原样保留
				rewardBuilder.mergeFrom(rewardInfo);
			}

			newRewards.add(rewardBuilder.build());
		}

		// 用新列表替换原列表内容
		rewards.clear();
		rewards.addAll(newRewards);
	}

	public static List<Goods> rewardAddition(List<RewardInfo> rewards, int addition) {
		List<Goods> goodsList = new ArrayList<>();
		if (addition <= 0 || rewards == null || rewards.isEmpty()) {
			return goodsList;
		}

		final float multiplier = addition / 10000f;
		for (RewardInfo rewardInfo : rewards) {

			if (rewardInfo.hasItem()) {
				ItemInfo item = rewardInfo.getItem();
				int newCount = (int) (item.getCount() * multiplier);
				if (newCount > 0) {
					Goods goods = new Goods(item.getId(), newCount);
					goodsList.add(goods);
				}
			} else if (rewardInfo.hasAsset()) {
				AssetInfo asset = rewardInfo.getAsset();
				int newCount = (int) (asset.getCount() * multiplier);
				if (newCount > 0) {
					Goods goods = new Goods(asset.getId(), newCount);
					goodsList.add(goods);
				}
			} else {
				// 其他类型不处理
			}
		}

		return goodsList;
	}

	public static void main(String[] args) {
		// 构造模拟数据
		List<RewardInfo> rewards = new ArrayList<>();
		RewardInfo rewardInfo1 = RewardInfo.newBuilder().setItem(ItemInfo.newBuilder().setId(1).setCount(10)).build();
		RewardInfo rewardInfo2 = RewardInfo.newBuilder().setItem(ItemInfo.newBuilder().setId(2).setCount(20)).build();
		RewardInfo rewardInfo3 = RewardInfo.newBuilder().setItem(ItemInfo.newBuilder().setId(2).setCount(2)).build();
		RewardInfo rewardInfo4 = RewardInfo.newBuilder().setItem(ItemInfo.newBuilder().setId(1).setCount(5)).build();
		RewardInfo rewardInfo5 = RewardInfo.newBuilder().setItem(ItemInfo.newBuilder().setId(3).setCount(7)).build();
		RewardInfo rewardInfo6 = RewardInfo.newBuilder().setItem(ItemInfo.newBuilder().setId(3).setCount(7)).build();
		RewardInfo rewardInfo7 = RewardInfo.newBuilder().setItem(ItemInfo.newBuilder().setId(2).setCount(1)).build();
		RewardInfo rewardInfo8 = RewardInfo.newBuilder().setGem(GemInfo.newBuilder().setUid(2 + "")).build();

		rewards.add(rewardInfo1);
		rewards.add(rewardInfo2);
		rewards.add(rewardInfo3);
		rewards.add(rewardInfo4);
		rewards.add(rewardInfo5);
		rewards.add(rewardInfo6);
		rewards.add(rewardInfo7);
		rewards.add(rewardInfo8);

		System.out.println("原始 rewards:");
		for (RewardInfo r : rewards) {
			System.out.println(TextFormat.shortDebugString(r));
		}

		mergeRewards(rewards);

		System.out.println("\n合并后 rewards:");
		for (RewardInfo r : rewards) {
			System.out.println(TextFormat.shortDebugString(r));
		}
	}
}
