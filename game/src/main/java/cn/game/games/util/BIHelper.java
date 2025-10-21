package cn.game.games.util;

import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.entity.Hero;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.game.helper.ItemHelper;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.util.DateUtil;

public class BIHelper {

	public static void start() {
		VxHolder.vertx.setPeriodic(DateUtil.MINUTE_MILLIS, r -> {
			GameLogger.heart();
		});
	}

	public static void resourceUpdate(Player player, int id, long value, OpType opType, boolean isAdd) {

		int goodsType = ItemHelper.getGoodsType(id);
		if (goodsType == GoodsTypeEnum.Resource.getId()) {
			if (id == Asset.gold.ID || id == Asset.diamond.ID|| id == Asset.GuildContribute.ID|| id == Asset.GuildCoin.ID) {
				GameLogger.money(player, id, value, opType, isAdd);
			}
		} else {
			GameLogger.item(player, id, (int) value, opType, isAdd);
		}
	}

	public static void heroraise(Player player, Hero hero, long value) {

	}

}
