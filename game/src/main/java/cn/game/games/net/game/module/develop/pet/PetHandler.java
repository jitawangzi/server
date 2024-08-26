package cn.game.games.net.game.module.develop.pet;

import org.springframework.stereotype.Component;

import cn.game.core.net.socket.handler.BaseHandler;

@Component
public class PetHandler extends BaseHandler {

    @Override
    protected int getModule() {
        return 0x19;
    }

    @Override
    protected void inititialize() {
    }
}
