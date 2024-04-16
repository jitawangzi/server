package cn.game.games.net.game.module.develop.sword;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.item.AbstractItemNoStackModule;
import cn.game.protocol.generated.enume.GoodsTypeEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public class SwordModule extends AbstractItemNoStackModule<Sword> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE };
	private long SwordUid;

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
	public void setInstanceAfter(Sword hero) {
		if (this.SwordUid == 0) {
			this.SwordUid = hero.getId();
		}
//		SwordConfig SwordConfig = SwordManager.instance().get(hero.getConfigId());
//		if (SwordConfig.SwordConsumeSkillId > 0) {
//			player.getSwordSkillModule().add(SwordConfig.SwordConsumeSkillId);
//		}

	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.Sword;
	}

	@Override
	public RewardInfo toRewardInfo(Sword sword) {
		return RewardInfo.newBuilder().setSword(sword.toHeroSwordInfo()).build();
	}

	@Override
	public Sword newInstance() {
		return new Sword();
	}

	public Sword getCurSword() {
		return get(SwordUid);
	}
	@Override
	public void buildPlayerAllInfo(Builder builder) {
		for (Sword sword : list()) {
			builder.addSwords(sword.toHeroSwordInfo());
		}
	}
}
