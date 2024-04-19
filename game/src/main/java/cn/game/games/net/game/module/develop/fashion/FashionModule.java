package cn.game.games.net.game.module.develop.fashion;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.item.AbstractItemNoStackModule;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.ObjUtil;

public class FashionModule extends AbstractItemNoStackModule<Fashion> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE };
	private long fashionUid;

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
	public void setInstanceAfter(Fashion hero) {
		ObjUtil.setDefaultValue(hero);
		if (this.fashionUid == 0) {
			this.fashionUid = hero.getId();
		}
//		SwordConfig SwordConfig = SwordManager.instance().get(hero.getConfigId());
//		if (SwordConfig.SwordConsumeSkillId > 0) {
//			player.getSwordSkillModule().add(SwordConfig.SwordConsumeSkillId);
//		}

	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.Fashion;
	}

	@Override
	public RewardInfo toRewardInfo(Fashion sword) {
		return RewardInfo.newBuilder().setFashion(sword.toFashionInfo()).build();
	}

	@Override
	public Fashion newInstance() {
		return new Fashion();
	}

	public Fashion getCurSword() {
		return get(fashionUid);
	}
	@Override
	public void buildPlayerAllInfo(Builder builder) {
		for (Fashion sword : list()) {
			builder.addFashions(sword.toFashionInfo());
		}
	}
}
