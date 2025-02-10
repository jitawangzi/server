package cn.game.games.net.game.module.player.headportrait;

import cn.game.games.net.game.module.currency.Currency;
import cn.game.games.net.game.module.currency.CurrencyModule;
import cn.game.games.net.game.module.item.AbstractItemIdModule;
import cn.game.games.net.game.module.player.IdConstant;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.HeadPortraitConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.HeadPortraitManager;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**    
 * 这里只是为了可以给头像奖励
 * 2025年1月22日 18:12:15
 * @author SYQ
 */
public class HeadPortraitModule extends AbstractItemIdModule<HeadPortrait>
{

	@Override
	public void checkConfig(int id) {
		HeadPortraitManager.instance().get(id);
	}

	@Override
	public HeadPortrait newInstance() {
		return new HeadPortrait();
	}

	@Override
	public RewardInfo toRewardInfo(HeadPortrait reward) {
		return RewardInfo.newBuilder().setHeadPortrait(reward.getConfigId()).build();
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.HeadPortrait;
	}

	@Override
	public IdConstant getIdType() {
		return IdConstant.HEAD_PORTRAIT;
	}

	@Override
	public Object addRepeated(int itemId) {
		HeadPortraitConfig config = HeadPortraitManager.instance().get(itemId);
		int count = GlobalConst.AvatarDecomposition.get(config.Quality);
		CurrencyModule currencyModule = player.getCurrencyModule();
		Currency currency = currencyModule.add(Asset.diamond.ID, count, OpType.HeadPortraitRepeat);
		return currency;
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {

	}
}
