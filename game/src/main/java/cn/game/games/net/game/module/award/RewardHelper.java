package cn.game.games.net.game.module.award;

import cn.game.games.cache.entity.Equip;
import cn.game.games.cache.entity.Hero;
import cn.game.games.cache.entity.Item;
import cn.game.games.net.game.module.currency.Currency;
import cn.game.games.net.game.module.develop.mergeequip.MergeEquip;
import cn.game.games.net.game.module.develop.pet.Pet;
import cn.game.games.net.game.module.develop.secretscript.Secretscript;
import cn.game.games.net.game.module.player.headbox.HeadBox;
import cn.game.protocol.protobuf.BaseMsg.AssetInfo;
import cn.game.protocol.protobuf.BaseMsg.EquipInfo;
import cn.game.protocol.protobuf.BaseMsg.ItemInfo;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public class RewardHelper {
	public static RewardInfo toRewardInfo(Item item) {
		RewardInfo.Builder builder = RewardInfo.newBuilder();

		if (item.getClass() == Item.class) {
			builder.setItem(ItemInfo.newBuilder().setId(item.getConfigId()).setCount(item.getCount().intValue())).build();
			return builder.build();
		}

		if (item instanceof Currency) {
			builder.setAsset(AssetInfo.newBuilder().setId(item.getConfigId()).setCount(item.getCount().intValue())).build();
		} else if (item instanceof MergeEquip) {
			builder.setMergeEquip(((MergeEquip) item).toMergeEquipProto()).build();
		} else if (item instanceof Pet) {
			builder.setPet(((Pet) item).toPetInfo()).build();
		} else if (item instanceof Secretscript) {
			builder.setSecretscript(((Secretscript) item).toProtoInfo()).build();
		} else if (item instanceof Secretscript) {
			builder.setSecretscript(((Secretscript) item).toProtoInfo()).build();
		} else if (item instanceof Equip) {
			Equip equip = (Equip) item;
			builder.setEquip(EquipInfo.newBuilder().setConfigId(equip.getConfigId()).setUid(equip.getId() + "")).build();
		} else if (item instanceof Hero) {
			Hero obj = (Hero) item;
			builder.setRole(obj.toHeroInfo()).build();

		} else if (item instanceof HeadBox) {

			HeadBox obj = (HeadBox) item;
			builder.setHead(obj.getConfigId()).build();
		} else {
			throw new IllegalArgumentException("toRewardInfo not implement, item class is " + item.getClass().getName());
		}
		return builder.build();
	}

}
