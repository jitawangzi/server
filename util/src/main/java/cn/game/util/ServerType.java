package cn.game.util;

public enum ServerType {

	Login(),
	Gate(),
	Game(),
	Data(),
	Cross(),
	Gm(),
	World(),
	;
	
	private String serverIdKey;

	private ServerType() {
		this.serverIdKey = this.name().toLowerCase() + ".server.id";
	}

	public String getServerIdKey() {
		return serverIdKey;
	}
}
