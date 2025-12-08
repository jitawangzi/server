package cn.game.games.net.game.module.player.headbox;

import cn.game.games.cache.entity.Item;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.item.AbstractItemOnlyOneModule;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.HeadBoxConfig;
import cn.game.protocol.generated.manager.HeadBoxManager;
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
		expired(item, config.Time);
	}

	@Override
	public void checkConfig(int id) {
//		HeadBoxManager.instance().get
	}
	
	@Override
	public void delItemAfter(Item item, OpType... args) {
		if (player.getData().getHeadFrame() == item.getConfigId()) {
			for (int[] array : GlobalConst.initItems) {
				if (array[0] == GoodsTypeEnum.HeadBox.getId()) {
					int headBoxId = array[1];
					HeadBox headBox = get(headBoxId);
					if (headBox != null) {
						player.getData().setHeadFrame(headBoxId);
						return;
					}
					
				}
			}
		}
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
			builder.putHeadBoxMap(k, v.getExpiredTime());
		});
	}

	@Override
	public Item addRepeated(int itemId) {
		HeadBoxConfig config = HeadBoxManager.instance().get(itemId);
		HeadBox headBox = get(itemId);
		expired(headBox, config.Time);
		return headBox;
	}
}
