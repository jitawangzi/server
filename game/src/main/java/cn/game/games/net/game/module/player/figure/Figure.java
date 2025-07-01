package cn.game.games.net.game.module.player.figure;

import cn.game.games.cache.entity.ItemOnlyOne;

public class Figure extends ItemOnlyOne {

	public Figure() {
	}

	public Figure(int id, long count) {
		super();
		setConfigId(id);
		setCount(count);
	}

}
