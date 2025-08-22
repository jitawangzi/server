package cn.game.core.zookeeper.examples;

/**
 * Vert.x 活跃节点映射到的业务模型（仅包含你关心的元数据）。
 */
public class ActiveServerNode {
    public String serverId;
    public String serverType;

    public ActiveServerNode() {}
    public ActiveServerNode(String serverId, String serverType) {
        this.serverId = serverId;
        this.serverType = serverType;
    }
}

