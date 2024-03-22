package cn.game.core.net.socket.controller;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.socket.handler.Handler;

public interface Dispatcher {

	public abstract void put(int module, Handler handler);

	public abstract void dispatch(NetClient client, IProtocol protocol);

}
