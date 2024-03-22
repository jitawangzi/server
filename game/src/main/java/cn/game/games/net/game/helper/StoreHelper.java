package cn.game.games.net.game.helper;

import java.util.Iterator;
import java.util.Map.Entry;

import com.google.common.collect.Multimap;

import cn.game.games.cache.entity.Buff;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.op.impl.BuffOp;
import cn.game.games.cache.op.impl.StoreOp;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.buff.BuffValue;
import cn.game.protocol.generated.config.OldBuffConfig;
import cn.game.protocol.generated.config.StoreConfig;
import cn.game.protocol.generated.config.StoreGiftConfig;
import cn.game.protocol.generated.enume.EffectEnum;
import cn.game.protocol.generated.enume.EffectTargetTypeEnum;
import cn.game.protocol.generated.manager.OldBuffManager;
import cn.game.protocol.generated.manager.StoreGiftManager;
import cn.game.protocol.generated.manager.StoreManager;
import cn.game.protocol.protobuf.StoreMsg.StoreType;

public class StoreHelper {

	/** 普通礼包,对应StoreGift表的礼包类型 */
	public static final int common = 1;
	/** 月卡,对应StoreGift表的礼包类型 */
	public static final int monthCard = 2;
	/** 按次销售的礼包,对应StoreGift表的礼包类型 */
	public static final int sellByFrequency = 3;

	/** 不刷新 */
	public static final int refreshByNo = 0;
	/** StoreGift StoreToken 商品刷新规则，按月刷新 */
	public static final int refreshByMonth = 1;
	/** StoreGift StoreToken 商品刷新规则，按周刷新 */
	public static final int refreshByWeek = 2;
	/** StoreGift StoreToken 商品刷新规则，按日刷新 */
	public static final int refreshByDay = 3;

	/**
	 * 按规则，刷新代币商店/礼包购买次数
	 * 
	 * @param playerId
	 * @param refreshRule 1-月;2-周;3-天
	 */
	public static void refresh(long playerId, int refreshRule) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		StoreOp storeOp = player.getModule(StoreOp.class);

		Iterator<Entry<Integer, Integer>> it = storeOp.getId_buyCnt().entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, Integer> id_cnt = it.next();
			int id = id_cnt.getKey();
			StoreConfig goodsConf = StoreManager.getInstance().getStoreConfigNullable(id);
			StoreGiftConfig giftConf = StoreGiftManager.getInstance().getStoreGiftConfigNullable(id);
			if (goodsConf != null && giftConf != null) {
				throw new IllegalArgumentException("【Store】表和【StoreGift】表的id重复");
			}
			if (goodsConf == null && giftConf == null) {
				it.remove();
				storeOp.delete(id);
			}
			if (goodsConf != null && goodsConf.getLimitParameters1() == refreshRule) {
				it.remove();
				storeOp.delete(id);
				continue;
			}
			if (giftConf != null && giftConf.getLimitParameters1() == refreshRule) {
				it.remove();
				storeOp.delete(id);
			}
		}
	}
	
	/**
	 * 获取商店折扣(每个折扣/100后相乘,最后四舍五入保留两位小数)
	 * @param playerId 
	 * @param type 商店类型
	 * @return 
	 */
	public static float getDiscount(long playerId, StoreType type) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		BuffOp buffOp = player.getModule(BuffOp.class);
		long storeId = (long) type.getNumber();
		float discountFinal = 1f;
		Multimap<Long, Buff> buffs = buffOp.getBuffs(EffectTargetTypeEnum.Shop);
		for (Buff buff : buffs.get(storeId)) {
			OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buff.getBuffId());
			int discount = buffConfig.getNumParam();
			discountFinal *= (discount / 100f);
		}
//		finalDiscount = (float)(Math.round(finalDiscount * 100.0) / 100.0);
//		BigDecimal bigDecimal = new BigDecimal(discountFinal);
//		discountFinal = bigDecimal.setScale(2, RoundingMode.HALF_UP).floatValue();
		return discountFinal;
	}
	
	/**
	 * 额外增加的获取库存数量
	 * @param playerId 
	 * @param type 商店类型
	 * @return
	 */
	public static int getExtraAdd(long playerId, StoreType type) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		BuffOp buffOp = player.getModule(BuffOp.class);
		BuffValue buffValue = buffOp.getBuffValue(EffectTargetTypeEnum.Shop, type.getNumber(), EffectEnum.ChangeShopNumbers);
		int res = buffValue.getValueDefault(type.getNumber());
		return res;
	}
	

}
