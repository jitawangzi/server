package cn.game.core.base;

import cn.game.util.ServerType;

public interface ServerInstanceListener {

	void onServerJoin(String serverId, ServerType serverType);

	void onServerLeave(String serverId, ServerType serverType);
}