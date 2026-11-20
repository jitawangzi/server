package cn.game.games.net.game.gm.acceptance;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.BasePlayerModule;
import cn.game.util.Rnd;

/**
 * 沙盒模块快速状态重置器 (DC-04, EX-04)
 * 复用 BasePlayerModule#resetForTest 毫秒级原地重置模块状态，清理临时公会上下文与可控随机源。
 *
 * @author shuyongqiang
 */
public class SandboxResetter {

    private static final Logger log = LoggerFactory.getLogger(SandboxResetter.class);

    /**
     * 重置沙盒玩家的指定或全部模块状态
     *
     * @param player 目标玩家
     * @param moduleNames 模块名称列表（类全名或简单名，不传则重置全部已加载模块）
     */
    public static void resetModules(Player player, String... moduleNames) {
        if (player == null) {
            return;
        }
        Map<String, BasePlayerModule> modules = player.getModules();
        if (modules == null || modules.isEmpty()) {
            return;
        }

        Set<String> filter = new HashSet<>();
        if (moduleNames != null && moduleNames.length > 0) {
            for (String name : moduleNames) {
                if (name != null && !name.isBlank()) {
                    String clean = name.trim().toLowerCase();
                    filter.add(clean);
                    if (clean.endsWith("module")) {
                        filter.add(clean.substring(0, clean.length() - "module".length()));
                    } else {
                        filter.add(clean + "module");
                    }
                }
            }
        }

        for (Map.Entry<String, BasePlayerModule> entry : modules.entrySet()) {
            BasePlayerModule module = entry.getValue();
            if (module == null) {
                continue;
            }
            String key = entry.getKey() != null ? entry.getKey().toLowerCase() : "";
            String fullName = module.getClass().getName().toLowerCase();
            String simpleName = module.getClass().getSimpleName().toLowerCase();
            String strippedName = simpleName.endsWith("module") ? simpleName.substring(0, simpleName.length() - 6) : simpleName;
            if (filter.isEmpty() || filter.contains(key) || filter.contains(fullName) || filter.contains(simpleName) || filter.contains(strippedName)) {
                try {
                    module.resetForTest();
                } catch (Throwable t) {
                    log.warn("重置模块状态警告: playerId={} module={}", player.getPlayerId(), simpleName, t);
                }
            }
        }

        try {
            ConditionSatisfier.setSandboxGuildContext(player, 0L);
        } catch (Throwable t) {
            log.warn("重置沙盒公会上下文警告: playerId={}", player.getPlayerId(), t);
        }

        try {
            Rnd.resetControlledRandom();
        } catch (Throwable t) {
            log.warn("复位可控随机数生成器警告", t);
        }
    }
}
