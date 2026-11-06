package cn.game.games.core.runtime;

import java.nio.file.Files;
import java.nio.file.Path;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.game.util.GameUtil;
import cn.game.util.ServerType;

/** 部署前从独立制品和配置源生成期望清单，不调用游戏服务身份接口。 */
public final class RuntimeIdentityManifest {
    /** 工具类不创建实例。 */
    private RuntimeIdentityManifest() {}

    /**
     * 使用与待启动服务相同的类路径及 Apollo 定位参数运行。
     * @param args 真实节点名、输出清单绝对路径
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) { throw new IllegalArgumentException("Expected targetId and output path"); }
        ServerType serverType = switch (System.getProperty("app.id", "")) {
            case "game" -> ServerType.Game;
            case "login" -> ServerType.Login;
            default -> throw new IllegalArgumentException("RUNTIME_MANIFEST_APP_UNSUPPORTED");
        };
        // 复用真实入口的节点覆盖规则，避免把 Apollo 默认节点名计入待启动进程的期望摘要。
        String targetId = GameUtil.parseServerId(args, serverType);
        var inventory = new RuntimeIdentityInventory(Path.of(System.getProperty("game.runtimeIdentity.root")), false);
        Files.writeString(Path.of(args[1]), new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(inventory.capture(targetId)));
        System.exit(0);
    }
}
