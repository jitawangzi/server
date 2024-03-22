package cn.game.games.net.game.module.currency;

import cn.game.games.cache.entity.Item;

public class Currency extends Item {

	public Currency() {
	}

	public Currency(int id, long count) {
		super();
		setConfigId(id);
		setCount(count);
	}

}
