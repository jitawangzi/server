package cn.game.games.net.game.module.develop.defenceline;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.item.AbstractItemNoStackModule;
import cn.game.protocol.generated.manager.DefenceSkinManager;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

public class DefenceSkinModule extends AbstractItemNoStackModule<DefenceSkin> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE };
	private long curUseId = 0;

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {
		case PLAYER_CREATE: {
			break;
		}
		}
	}


	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.DefenceSkin;
	}

	@Override
	public DefenceSkin newInstance() {
		return new DefenceSkin();
	}
	@Override
	public void setInstanceExt(DefenceSkin item) {
		item.setStar(1);
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		for (DefenceSkin obj : list()) {
			builder.addDefenceSkins(obj.toProto()); 
		}
		builder.setDefenceSkinUsed(curUseId + "");     
	}
	

	public void setCurUseId(long curUseId) {
		this.curUseId = curUseId;
	}

	@Override
	public void checkConfig(int id) {
		DefenceSkinManager.instance().get(id); 
	}
}
