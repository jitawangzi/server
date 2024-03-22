package cn.game.games.cache.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PlayerEntity extends CacheEntity {

	protected static final Logger log = LoggerFactory.getLogger(PlayerEntity.class);

	public abstract long getPlayerId();

	protected String calPk(Object... fields) {

		StringBuilder b = new StringBuilder();
		b.append(this.getClass().getSimpleName());

		for (Object o : fields) {
			b.append("_").append(o);
		}
		return b.toString();
	}
}
