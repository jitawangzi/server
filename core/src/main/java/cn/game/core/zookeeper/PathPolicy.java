package cn.game.core.zookeeper;

/**
 * 统一管理 ZK 路径规则。
 * 典型规则：/basePath/{id}
 */
public interface PathPolicy {
	
	public static final String DEFAULT_VIRTUAL_SERVER_PATH = "/server/login/game/virtual-servers";
	
    String basePath();
    String pathForId(String id);
    String idFromPath(String path);

    static PathPolicy simple(String basePath) {
        return new SimplePathPolicy(basePath);
    }
}

