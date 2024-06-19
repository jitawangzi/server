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
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.item.ItemModule;
import cn.game.protocol.generated.config.BattlePassConfig;
import cn.game.protocol.generated.config.BattlePassPrizeConfig;
import cn.game.protocol.generated.config.ItemConsumablesConfig;
import cn.game.protocol.generated.config.OldGlobalConst;
import cn.game.protocol.generated.config.OldItemConfig;
import cn.game.protocol.generated.config.RewardConfig;
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
import cn.game.protocol.generated.manager.ItemConsumablesManager;
import cn.game.protocol.generated.manager.ItemManager;
import cn.game.protocol.generated.manager.OldItemManager;
import cn.game.protocol.generated.manager.RewardManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
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

	/**
	 * 队伍道具使用校验
	 * @param lineupId
	 * @param itemId
	 * @return
	 */
	public static int teamItemUseCheck(int lineupId, int itemId) {
		ItemConsumablesConfig itemConfig = ItemConsumablesManager.getInstance().getItemConsumablesConfig(itemId);
		if (lineupId != OldGlobalConst.exploreTeamId && lineupId != OldGlobalConst.exploreTeamId) {
			return ErrorMsgEnum.player_check_error.getId();
		}
//		0-无限制
//		1-表世界使用
//		2-里世界使用
		int limit = itemConfig.getTeamLimit();
		if (limit == 1 && lineupId != OldGlobalConst.exploreTeamId) {
			return ErrorMsgEnum.not_use.getId();
		}
		if (limit == 2 && lineupId != OldGlobalConst.exploreTeamId) {
			return ErrorMsgEnum.not_use.getId();
		}

		return 0;
	}

	public static List<Goods> copy(List<Goods> list) {
		List<Goods> copy = new ArrayList<>(list.size());
		for (Goods g : list) {
			copy.add(new Goods(g.getId(), g.getCount()));
		}
		return copy;
	}

	/**
	 * 获取最大堆叠数量
	 * @param itemId
	 * @return
	 */
	public static int getMaxPile(int itemId) {
		OldItemConfig itemConfig = OldItemManager.getInstance().getItemConfig(itemId);
		return itemConfig.getStack();
	}

	/**
	 * 获取排序规则
	 * @param itemId
	 * @return
	 */
	public static int getSort(int itemId) {
		OldItemConfig itemConfig = OldItemManager.getInstance().getItemConfig(itemId);
		return itemConfig.getSortType();
	}

	/**
	 * 检查玩家背包临时奖励,如果存在奖励,推送选取
	 * @param playerId
	 */
	public static void checkBagReward(long playerId) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		ItemModule itemModule = player.getModule(ItemModule.class);
//		itemModule.checkAndPushBagReward();
	}

	/**
	 * 道具是否是自动使用
	 * @param itemId
	 * @return
	 */
	public static boolean isAutoUse(int itemId) {
		OldItemConfig itemConfig = OldItemManager.getInstance().getItemConfigNullable(itemId);
		return itemConfig.getIsAutoUse();
	}

	/**
	 * 自动使用道具
	 * @param playerId
	 * @param itemId
	 * @param count
	 * @param notify 是否通知客户端
	 * @return 获得的奖励
	 */
	public static void autoUse(long playerId, int itemId, int count) {
//		List<RewardItem> res = new ArrayList<>();
		int type = ItemHelper.getGoodsType(itemId);
		if (type != GoodsTypeEnum.Item.getId()) {
			throw new IllegalArgumentException("不是道具 id:" + itemId);
		}
		if (!isAutoUse(itemId)) {
			return;
		}
		ItemConsumablesConfig config = ItemConsumablesManager.getInstance().getItemConsumablesConfigNullable(itemId);
		for (int buffId : config.getBuffIds()) {
			BuffHelper.addBuff(playerId, buffId, null);
		}
		return;
	}

	/**
	 * 根据是否在探索中，判断仓库或背包道具是否足够
	 * @param playerId
	 * @param itemId
	 * @param count
	 * @return
	 */
	public static boolean isEnough(long playerId, int itemId, int count) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		ItemModule itemModule = player.getModule(ItemModule.class);
		return itemModule.isEnough(itemId, count);
	}

	/**
	 * @param playerId
	 * @param itemId
	 * @param count
	 * @return
	 */
	public static boolean delItem(long playerId, int itemId, int count) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		ItemModule itemModule = player.getModule(ItemModule.class);
		return itemModule.del(itemId, count);
	}

	/**
	 * 推送背包格子数据
	 * @param playerId
	 * @param list
	 *//*
		public static void pushBagGrid(long playerId, List<BagGrid> list) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		
		EquipOp equipOp = player.getModule(EquipOp.class);
		ItemBagGridPush_0b000320.Builder builder = ItemBagGridPush_0b000320.newBuilder();
		for (BagGrid bagGrid : list) {
			int gridId = bagGrid.getId();
			int itemId = bagGrid.getItemId();
			int itemCount = bagGrid.getItemCount();
			long equipId = bagGrid.getEquipId();
		
			BagGridInfo.Builder newBuilder = BagGridInfo.newBuilder();
			if (bagGrid.isEmpty()) {
				newBuilder.setId(gridId);
				
			} else if (bagGrid.isItem()) {
				newBuilder.setId(gridId);
				newBuilder.setItem(ItemInfo.newBuilder().setId(itemId).setCount(itemCount));
				
			} else if (bagGrid.isEquip()) {
				Equip equip = equipOp.get(equipId);
				newBuilder.setId(gridId);
				newBuilder.setEquip(PbBuilder.buildEquipInfo(equip));
			}
			builder.addBagGridInfos(newBuilder);
		}
		PlayerHelper.sendProtcol(playerId, builder.build());
		}*/

	/**
	 * @Description 增加奖励
	 * @param playerId
	 * @param rewardIdList
	 *            RewardConfig 的id 集合
	 * @return
	 */
	@Deprecated
	public static List<RewardInfo> addRewards(long playerId, List<Integer> rewardIdList) {
		List<RewardInfo> ret = new ArrayList<>();
		for (Integer id : rewardIdList) {
			ret.addAll(addRewards(playerId, id));
		}
		return ret;
	}

	@Deprecated
	public static List<RewardInfo> addRewards(long playerId, int rewardId) {

		RewardConfig rewardConfig = RewardManager.getInstance().getRewardConfig(rewardId);
		return PlayerHelper.addResources(null, rewardConfig.getInfo(),null);

	}

	@Deprecated
	public static List<RewardInfo> addRandomRewards(long playerId, int rewardId) {

		RewardConfig rewardConfig = RewardManager.getInstance().getRewardConfig(rewardId);
		return PlayerHelper.addResources(null, rewardConfig.getInfo(),null);
	}

}
