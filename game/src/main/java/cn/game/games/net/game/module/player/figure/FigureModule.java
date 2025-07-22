package cn.game.games.net.game.module.player.figure;

import cn.game.games.cache.entity.Item;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.item.AbstractItemOnlyOneModule;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.PlayerFigureConfig;
import cn.game.protocol.generated.manager.PlayerFigureManager;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.DateUtil;

public class FigureModule extends AbstractItemOnlyOneModule<Figure> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE };
	/** 当前使用的形象id */
	private int figure;

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(PlayerEvent event) {

		switch (event.getType()) {

		case PLAYER_CREATE: {
			// 设置默认的形象
			figure = GlobalConst.BirthDefaultPlayerFigure;
			break;
		}
		default:
			break;
		}

	}

	@Override
	public void checkConfig(int id) {
		PlayerFigureManager.instance().get(id);
	}

	@Override
	public Figure newInstance() {
		return new Figure();
	}

	@Override
	public void setInstanceExt(Figure item) {
		PlayerFigureConfig playerFigureConfig = PlayerFigureManager.instance().get(item.getConfigId());
		if (playerFigureConfig.Times > 0) {
			item.setExpiredTime(DateUtil.currentTimeSeconds() + playerFigureConfig.Times);
		}
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.Figure;
	}


	@Override
	public void buildPlayerAllInfo(Builder builder) {
		idItems.forEach((k, v) -> {
			builder.putFigureMap(k, v.getExpiredTime() > 0 ? DateUtil.currentTimeSeconds() - v.getExpiredTime() : 0);
		});
		builder.setFigure(figure);
	}

	@Override
	public Item addRepeated(int itemId) {
		PlayerFigureConfig playerFigureConfig = PlayerFigureManager.instance().get(itemId);
		Figure figure = get(itemId);
		figure.setExpiredTime(figure.getExpiredTime() + playerFigureConfig.Times);
		return figure;
	}

	@Override
	protected int getInitOrder() {
		return INIT_PRIORITY_LOW;
	}

	public void setFigure(int figure) {
		this.figure = figure;
	}

	public int getFigure() {
		return figure;
	}
	

}
