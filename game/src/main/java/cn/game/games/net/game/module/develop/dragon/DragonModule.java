package cn.game.games.net.game.module.develop.dragon;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.item.AbstractItemNoStackModule;
import cn.game.protocol.generated.config.DragonConfig;
import cn.game.protocol.generated.manager.DragonManager;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BaseMsg.DragonInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.ObjUtil;

public class DragonModule extends AbstractItemNoStackModule<Dragon> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE };
	private long dragonUid;

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
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
	public Class<?>[] defaultDbMapperClass() {
		return null;
	}

	@Override
	public void setInstanceAfter(Dragon hero) {
		ObjUtil.setDefaultValue(hero);
		if (this.dragonUid == 0) {
			this.dragonUid = hero.getId();
		}
		DragonConfig dragonConfig = DragonManager.instance().get(hero.getConfigId());
		if (dragonConfig.DragonConsumeSkillId > 0) {
			player.getDragonSkillModule().add(dragonConfig.DragonConsumeSkillId, OpType.None);
		}

	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.Dragon;
	}

	@Override
	public RewardInfo toRewardInfo(Dragon hero) {
		return RewardInfo.newBuilder().setDragon(DragonInfo.newBuilder().setId(hero.getConfigId()).setStar(hero.getStar())).build();
	}

	@Override
	public Dragon newInstance() {
		return new Dragon();
	}

	public Dragon getCurDragon() {
		return get(dragonUid);
	}
	@Override
	public void buildPlayerAllInfo(Builder builder) {
		for (Dragon hero : list()) {
			builder.putDragons(hero.getConfigId(), hero.getStar());
		}
	}
}
