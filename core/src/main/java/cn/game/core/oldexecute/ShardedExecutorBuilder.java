package cn.game.core.oldexecute;

/**
 * 分片执行器构建器 - 使用流式API创建执行器
 */
public class ShardedExecutorBuilder {
    private int shardCount = Runtime.getRuntime().availableProcessors() * 4;
    
    /**
     * 设置分片数量
     * @param count 分片数量
     * @return 构建器实例
     */
    public ShardedExecutorBuilder withShardCount(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Shard count must be positive");
        }
        this.shardCount = count;
        return this;
    }
    
    /**
     * 构建分片执行器
     * @return 新的分片执行器实例
     */
    public ShardedExecutor build() {
        return new ShardedExecutor(shardCount);
    }
    
    /**
     * 创建新的构建器实例
     * @return 构建器实例
     */
    public static ShardedExecutorBuilder create() {
        return new ShardedExecutorBuilder();
    }
}

