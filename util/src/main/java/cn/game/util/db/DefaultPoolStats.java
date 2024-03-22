package cn.game.util.db;

import com.zaxxer.hikari.metrics.PoolStats;

public class DefaultPoolStats extends PoolStats {

	public DefaultPoolStats(long timeoutMs) {
		super(timeoutMs);
	}

	@Override
	protected void update() {

	}

}
