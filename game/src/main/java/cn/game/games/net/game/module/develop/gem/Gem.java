package cn.game.games.net.game.module.develop.gem;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import cn.game.games.cache.base.DbEntity;
import cn.game.games.cache.entity.ItemNoStack;
import cn.game.protocol.protobuf.BaseMsg.GemInfo;

public class Gem extends ItemNoStack implements Serializable, DbEntity {

	private boolean isLock;

	private Map<Integer, Integer> gemAttrs = new HashMap<Integer, Integer>();

	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	public Map<Integer, Integer> getGemAttrs() {
		return gemAttrs;
	}

	public void setGemAttrs(Map<Integer, Integer> gemAttrs) {
		this.gemAttrs = gemAttrs;
	}

	public boolean isLock() {
		return isLock;
	}

	public void setLock(boolean isLock) {
		this.isLock = isLock;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}

	public GemInfo toGemInfo() {
		return GemInfo.newBuilder().setUid(getId() + "").setConfigId(getConfigId()).setIsLock(isLock).putAllAttrs(gemAttrs).build();
	}
}