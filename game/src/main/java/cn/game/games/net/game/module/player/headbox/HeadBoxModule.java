package cn.game.games.net.game.module.player.headbox;

import java.util.Set;

import cn.game.games.cache.entity.Item;
import cn.game.games.core.GoodsModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.item.AbstractItemOnlyOneModule;
import cn.game.games.net.game.module.player.IdConstant;
import cn.game.games.net.game.module.player.figure.Figure;
import cn.game.protocol.generated.config.HeadBoxConfig;
import cn.game.protocol.generated.config.PlayerFigureConfig;
import cn.game.protocol.generated.manager.HeadBoxManager;
import cn.game.protocol.generated.manager.PlayerFigureManager;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.DateUtil;

/**    
 * 这里只是为了可以给头像框奖励
 * 2024年8月22日 下午4:16:56
 * @author SYQ
 */
public class HeadBoxModule extends AbstractItemOnlyOneModule<HeadBox> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE };
	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(PlayerEvent event) {

		switch (event.getType()) {

		case PLAYER_CREATE: {

			idItems.forEach((k, v) -> {
				player.getData().setHeadFrame(v.getConfigId());
				return;
			});

			break;
		}
		}

	}
	@Override
	public void setInstanceExt(HeadBox item) {
		HeadBoxConfig config = HeadBoxManager.instance().get(item.getConfigId());
		if (config.Time > 0) {
			item.setExpiredTime(DateUtil.currentTimeSeconds() + config.Time);
		}
	}

	@Override
	public void checkConfig(int id) {
//		HeadBoxManager.instance().get
	}

	@Override
	public HeadBox newInstance() {
		return new HeadBox();
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.HeadBox;
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		idItems.forEach((k, v) -> {
			builder.putFigureMap(k, v.getExpiredTime() > 0 ? DateUtil.currentTimeSeconds() - v.getExpiredTime() : 0);
		});
	}

	@Override
	public Item addRepeated(int itemId) {
		HeadBoxConfig config = HeadBoxManager.instance().get(itemId);
		HeadBox headBox = get(itemId);
		headBox.setExpiredTime(headBox.getExpiredTime() + config.Time);
		return headBox;
	}
}
