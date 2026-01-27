package cn.game.games.net.game.gm;

import cn.game.games.cache.entity.Player;

@FunctionalInterface
public interface GmCommand {
    void handle(Player player, String[] params);
}
