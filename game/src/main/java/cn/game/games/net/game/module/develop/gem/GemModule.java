package cn.game.games.net.game.module.develop.gem;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.item.AbstractItemNoStackModule;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public class GemModule extends AbstractItemNoStackModule<Gem> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE };


	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {
		}
	}

	@Override
	public void setInstanceAfter(Gem instance) {
		// 随机宝石属性
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.Gem;
	}

	@Override
	public RewardInfo toRewardInfo(Gem obj) {
		return RewardInfo.newBuilder().setGem(obj.toGemInfo()).build();
	}

	@Override
	public Gem newInstance() {
		return new Gem();
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		for (Gem obj : list()) {
			builder.addGems(obj.toGemInfo());
		}
	}

	@Override
	public void checkConfig(int id) {
		// TODO Auto-generated method stub

	}
}
