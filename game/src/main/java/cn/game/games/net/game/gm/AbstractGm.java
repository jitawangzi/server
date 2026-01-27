package cn.game.games.net.game.gm;

import org.apache.commons.lang3.StringUtils;
import javax.annotation.PostConstruct;

public abstract class AbstractGm {
    
    @PostConstruct
    public abstract void init();

    protected void register(String cmd, GmCommand handler) {
        GmRegistry.register(cmd, handler);
    }

    protected int getInt(String[] params, int index) {
        if (index < params.length && StringUtils.isNumeric(params[index])) {
            return Integer.parseInt(params[index]);
        }
        return 0;
    }
    
    protected long getLong(String[] params, int index) {
        if (index < params.length && StringUtils.isNumeric(params[index])) {
            return Long.parseLong(params[index]);
        }
        return 0L;
    }
    
     protected String getString(String[] params, int index) {
        if (index < params.length) {
            return params[index];
        }
        return "";
    }
}
