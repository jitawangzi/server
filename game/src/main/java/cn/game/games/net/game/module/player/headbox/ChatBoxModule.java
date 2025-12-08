package cn.game.games.net.game.module.player.headbox;

import cn.game.games.cache.entity.Item;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.item.AbstractItemOnlyOneModule;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

/**
 * 头像框模块
 */
public class ChatBoxModule extends AbstractItemOnlyOneModule<ChatBox> {
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
				player.getData().setTitle(v.getConfigId());
				return;
			});

			break;
		}
		}

	}
	@Override
	public void setInstanceExt(ChatBox item) {
//		TitleConfig config = TitleManager.instance().get(item.getConfigId());
//		if (config.Time > 0) {
//			item.setExpiredTime(DateUtil.currentTimeSeconds() + config.Time);
//		}
	}

	@Override
	public void checkConfig(int id) {
//		HeadBoxManager.instance().get
	}
	
	@Override
	public void delItemAfter(Item item, OpType... args) {
//		if (player.getData().getHeadFrame() == item.getConfigId()) {
//			for (int[] array : GlobalConst.initItems) {
//				if (array[0] == GoodsTypeEnum.HeadBox.getId()) {
//					int headBoxId = array[1];
//					HeadBox headBox = get(headBoxId);
//					if (headBox != null) {
//						player.getData().setHeadFrame(headBoxId);
//						return;
//					}
//
//				}
//			}
//		}
	}

	@Override
	public ChatBox newInstance() {
		return new ChatBox();
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.ChatBox;
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		idItems.forEach((k, v) -> {
			builder.putChatBoxMap(k, v.getExpiredTime());
		});
	}

	@Override
	public Item addRepeated(int itemId) {
//		TitleConfig config = TitleManager.instance().get(itemId);
//		Title title = get(itemId);
//		title.setExpiredTime(title.getExpiredTime() + config.Time);
		return null;
	}
}
