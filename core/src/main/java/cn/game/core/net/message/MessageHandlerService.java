package cn.game.core.net.message;

import io.vertx.core.eventbus.Message;

public interface MessageHandlerService {
	void handleMessage(Message<Object> message);
    void init();
    boolean shutdown();
}