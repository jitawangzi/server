package cn.game.games.net.game.module.develop.gem;

import java.util.List;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.item.AbstractItemNoStackModule;
import cn.game.protocol.generated.config.GemAttrConfig;
import cn.game.protocol.generated.config.GemConfig;
import cn.game.protocol.generated.manager.GemAttrManager;
import cn.game.protocol.generated.manager.GemManager;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.Rnd;

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
	public void setInstanceExt(Gem instance) {
		// 随机宝石属性
		GemConfig gemConfig = GemManager.instance().get(instance.getConfigId());
		List<GemAttrConfig> posqualityList = GemAttrManager.instance().getPosqualityList(gemConfig.pos, gemConfig.quality);
		GemAttrConfig config = Rnd.randomElement(posqualityList);
		instance.getGemAttrs().put(config.attrId, Rnd.get(config.attrMin, config.attrMax));
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.Gem;
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
		GemManager.instance().get(id);
	}
}
