package cn.game.games.net.game.gm;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.cache.entity.Player;

public class GmRegistry {
    private static final Logger log = LoggerFactory.getLogger(GmRegistry.class);
    private static final Map<String, GmCommand> commands = new HashMap<>();

    public static void register(String cmd, GmCommand handler) {
        commands.put(cmd.toLowerCase(), handler);
    }

    public static boolean dispatch(Player player, String cmd, String[] params) {
        GmCommand handler = commands.get(cmd.toLowerCase());
        if (handler != null) {
            try {
                handler.handle(player, params);
            } catch (Exception e) {
                log.error("GM command execution failed: " + cmd, e);
                throw e;
            }
            return true;
        }
        return false;
    }
}
