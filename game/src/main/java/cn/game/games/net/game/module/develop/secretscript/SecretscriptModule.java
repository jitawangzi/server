package cn.game.games.net.game.module.develop.secretscript;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cn.game.games.cache.entity.Item;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.item.AbstractItemModule;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public class SecretscriptModule extends AbstractItemModule<Secretscript> {
	private static EventTypeEnum[] events = new EventTypeEnum[] {};

	/** 神通融汇 */
	private Map<Integer, Integer> secretscriptPosMap = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> PvPSecretscriptMap = new HashMap<Integer, Integer>();

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {

		case NewDay: {
			break;
		}
		}
	}

	@Override
	public void checkConfig(int id) {
	}

	@Override
	public Item newInstance() {
		return new Secretscript();
	}

	@Override
	public RewardInfo toRewardInfo(Secretscript reward) {
		return RewardInfo.newBuilder().setSecretscript(reward.toProtoInfo()).build();
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.Secretscript;
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		for (Secretscript obj : list()) {
			builder.addSecretscript(obj.toProtoInfo());
		}
		builder.putAllSecretscriptPos(secretscriptPosMap);
	}

	public Map<Integer, Integer> getSecretscriptPosMap() {
		return secretscriptPosMap;
	}

	public void setSecretscriptPosMap(Map<Integer, Integer> secretscriptPosMap) {
		this.secretscriptPosMap = secretscriptPosMap;
	}

	public Collection<Secretscript> getSecretscriptInfos() {
		return list();
	}

	public void setPvPSecretscriptMap(Map<Integer, Integer> secretscriptMapMap) {
		PvPSecretscriptMap.putAll(secretscriptMapMap);
	}

	public Map<Integer, Integer> getPvPSecretscriptMap() {
		return PvPSecretscriptMap;
	}
}
