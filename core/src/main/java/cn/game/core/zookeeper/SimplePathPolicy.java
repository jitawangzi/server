package cn.game.core.zookeeper;

final class SimplePathPolicy implements PathPolicy {
    private final String basePath;

    SimplePathPolicy(String basePath) {
        this.basePath = normalize(basePath);
    }

    @Override
    public String basePath() {
        return basePath;
    }

    @Override
    public String pathForId(String id) {
        return basePath + "/" + id;
    }

    @Override
    public String idFromPath(String path) {
        if (path == null) return null;
        int idx = path.lastIndexOf('/');
        return (idx >= 0 && idx < path.length() - 1) ? path.substring(idx + 1) : null;
    }

    private static String normalize(String p) {
        if (p == null || p.isEmpty()) return "/";
        return p.startsWith("/") ? p : ("/" + p);
    }
}

