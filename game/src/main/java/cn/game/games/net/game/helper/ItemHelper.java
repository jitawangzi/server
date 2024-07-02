package cn.game.games.net.game.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.cache.entity.Item;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.GoodsModule;
import cn.game.games.net.game.module.award.Goods;
import cn.game.protocol.generated.config.BattlePassConfig;
import cn.game.protocol.generated.config.BattlePassPrizeConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.BattlePassManager;
import cn.game.protocol.generated.manager.BattlePassPrizeManager;
import cn.game.protocol.generated.manager.DragonManager;
import cn.game.protocol.generated.manager.DragonSkillManager;
import cn.game.protocol.generated.manager.EquipManager;
import cn.game.protocol.generated.manager.HCHeroManager;
import cn.game.protocol.generated.manager.HeroFashionManager;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.generated.manager.HeroSwordManager;
import cn.game.protocol.generated.manager.ItemManager;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.util.DateUtil;

public class ItemHelper {

	private static final Logger log = LoggerFactory.getLogger(ItemHelper.class);

	public static int getGoodsType(int id) {
		return (id / 100000);
	}

	public static void checkConfig(int id) {
		GoodsTypeEnum goodsTypeEnum = GoodsTypeEnum.get(getGoodsType(id));
		switch (goodsTypeEnum) {
		case Resource: {
			Asset.get(id);
			break;
		}
		case Item: {
			ItemManager.instance().get(id);
			break;
		}
		case Hero: {
			HeroManager.instance().get(id);
			break;
		}
		case Fashion: {
			HeroFashionManager.instance().get(id);
			break;
		}
		case Dragon: {
			DragonManager.instance().get(id);
			break;
		}
		case DragonSkill: {
			DragonSkillManager.instance().get(id);
			break;
		}
		case Sword: {
			HeroSwordManager.instance().get(id);
			break;
		}
		case Merge_Equip: {
			EquipManager.instance().get(id);
			break;
		}
		case HCHero: {
			HCHeroManager.instance().get(id);
			break;
		}
		default:
			throw new IllegalArgumentException("没有检查的奖励id : " + id);
		}
	}
	// public static GoodsTypeEnum getGoodsTypeEnum(int id) {
	// return GoodsTypeEnum.get(getGoodsType(id));
	// }

	public static class ItemChange {

		public static final int ITEM_ADD = 1;// 物品添加

		public static final int ITEM_DEL = 2;// 物品删除

		public static final int ITEM_UPDATE = 3;// 物品改变

		private int op;

		private Item item;

		public ItemChange(int op, Item item) {
			this.op = op;
			this.item = item;
		}

		public static ItemChange valueof(int op, Item item) {
			return new ItemChange(op, item);
		}

//		public ItemMsg.ItemChangeInfo.Builder toBuilder() {
//			ItemMsg.ItemChangeInfo.Builder builder = ItemMsg.ItemChangeInfo.newBuilder();
//			builder.setState(ItemMsg.ChangeState.forNumber(op));
//			RewardMsg.ItemInfo.Builder newBuilder= RewardMsg.ItemInfo.newBuilder();
//			newBuilder.setId(item.getDictId());
//			newBuilder.setCount(item.getCount());
//			builder.setItem(newBuilder);
//			return builder;
//		}

	}

	/**
	 * 获取物品数量
	 * 
	 * @param player
	 * @param id
	 * @return
	 */
	public static long getCount(Player player, int id) {

		GoodsModule goodsModule = player.getGoodsModule(id);
		return goodsModule.getCount(id);
	}

	public static int getBattlePassId() {
		int passid = 0;
		List<BattlePassConfig> collect = BattlePassManager.getInstance().list().stream()
				.filter(e -> DateUtil.between(e.getUpTime(), e.getDownTime())).collect(Collectors.toList());
		if (collect != null && collect.size() > 0) {
			// 设置周期
			passid = collect.get(0).getId();
		}
		return passid;
	}

	/**
	 * battlepass开启天数
	 * @param battlepassId
	 * @return
	 */
	public static int getBattlePassOpenDays(int battlepassId) {
		BattlePassConfig battlePassConfig = BattlePassManager.getInstance().getBattlePassConfigNullable(battlepassId);
		if (battlePassConfig == null) {
			return 0;
		}
		Date upTime = battlePassConfig.getUpTime();
		Date date = new Date(System.currentTimeMillis());
		// day = DateUtil.calcBetweenDays(date, upTime);
		return DateUtil.calcBetweenDays(date, upTime) + 1;
	}

	public static BattlePassPrizeConfig getBattlePassConfig(int battlepassid, int level) {
		int cycleid = battlepassid * 100 + level;
		BattlePassPrizeConfig config = BattlePassPrizeManager.getInstance().getBattlePassPrizeConfigNullable(cycleid);
		/*if (config == null) {
			throw new IllegalArgumentException("battlepass id不存在: " + cycleid);
		}*/
		return config;
	}

	public static List<Map.Entry<Integer, Integer>> getExtraRewardList(List<Map.Entry<Integer, Integer>> rewards,
			float extraNum) {
		if (rewards == null || rewards.size() == 0) {
			return null;
		}
		List<Map.Entry<Integer, Integer>> nowRewards = new ArrayList<>();

		for (Map.Entry<Integer, Integer> reward : rewards) {
			Map.Entry<Integer, Integer> r = new Map.Entry<Integer, Integer>() {
				@Override
				public Integer setValue(Integer value) {
					return null;
				}

				@Override
				public Integer getValue() {
					return Math.round(reward.getValue() * extraNum);
				}

				@Override
				public Integer getKey() {
					return reward.getKey();
				}
			};
			nowRewards.add(r);
		}
		return nowRewards;
	}

	public static List<Goods> copy(List<Goods> list) {
		List<Goods> copy = new ArrayList<>(list.size());
		for (Goods g : list) {
			copy.add(new Goods(g.getId(), g.getCount()));
		}
		return copy;
	}


}
