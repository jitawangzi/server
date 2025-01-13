package cn.game.games.net.game.module.develop.hero.skin;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.currency.Currency;
import cn.game.games.net.game.module.currency.CurrencyModule;
import cn.game.games.net.game.module.item.AbstractItemIdModule;
import cn.game.games.net.game.module.player.IdConstant;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.HeroSkinConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.HeroSkinManager;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public class HeroSkinModule extends AbstractItemIdModule<HeroSkin> {
	private static EventTypeEnum[] events = new EventTypeEnum[] {};
	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void checkConfig(int id) {
		HeroSkinManager.instance().get(id);
	}

	@Override
	public HeroSkin newInstance() {
		return new HeroSkin();
	}

	@Override
	public RewardInfo toRewardInfo(HeroSkin reward) {
		return RewardInfo.newBuilder().setHeroSkin(reward.getConfigId()).build();
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.HeroSkin;
	}

	@Override
	public IdConstant getIdType() {
		return IdConstant.HERO_SKIN;
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {

	}

	@Override
	public void handleEvent(GameEvent event) {

		switch (event.getType()) {

		case PLAYER_CREATE: {
			break;
		}
		}
	}

	@Override
	public Object addRepeated(int itemId) {
		HeroSkinConfig heroSkinConfig = HeroSkinManager.instance().get(itemId);
		int count = GlobalConst.SkinBreakdown.get(heroSkinConfig.Quality);
		CurrencyModule currencyModule = player.getCurrencyModule(); 
		Currency currency = currencyModule.add(Asset.diamond.ID, count, OpType.HeroSkinBreakdown);
		return currency;
	}

}
