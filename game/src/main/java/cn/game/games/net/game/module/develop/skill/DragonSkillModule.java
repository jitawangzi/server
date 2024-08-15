package cn.game.games.net.game.module.develop.skill;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.item.AbstractItemNoStackModule;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.protobuf.BaseMsg.DragonSkillInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.ObjUtil;

public class DragonSkillModule extends AbstractItemNoStackModule<DragonSkill> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE };
	private long DragonSkillUid;

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		}
	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
		return null;
	}

	@Override
	public void setInstanceAfter(DragonSkill hero) {
		ObjUtil.setDefaultValue(hero);
		hero.setLevel(1);
		if (this.DragonSkillUid == 0) {
			this.DragonSkillUid = hero.getId();
		}
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.DragonSkill;
	}

	@Override
	public RewardInfo toRewardInfo(DragonSkill hero) {
		return RewardInfo.newBuilder().setDragonSkill(DragonSkillInfo.newBuilder().setId(hero.getConfigId()).setLevel(hero.getLevel())).build();
	}

	@Override
	public DragonSkill newInstance() {
		return new DragonSkill();
	}

	public DragonSkill getCurDragonSkill() {
		return get(DragonSkillUid);
	}
	@Override
	public void buildPlayerAllInfo(Builder builder) {
		for (DragonSkill hero : list()) {
			builder.putDragonSkills(hero.getConfigId(), hero.getLevel());
		}
	}

	@Override
	public void checkConfig(int id) {
		// TODO Auto-generated method stub

	}
}
