package cn.game.core.zookeeper;

/**
 * 统一管理 ZK 路径规则。
 * 典型规则：/basePath/{id}
 */
public interface PathPolicy {
    String basePath();
    String pathForId(String id);
    String idFromPath(String path);

    static PathPolicy simple(String basePath) {
        return new SimplePathPolicy(basePath);
    }
}

