package cn.game.games.core.event;

public class GlobalEvent extends EventModule {

	public static final GlobalEvent INSTANCE = new GlobalEvent();

	private GlobalEvent() {
	}

	public static GlobalEvent getInstance() {
		return INSTANCE;
	}

}
