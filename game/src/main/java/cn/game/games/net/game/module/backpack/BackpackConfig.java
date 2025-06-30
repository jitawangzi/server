package cn.game.games.net.game.module.backpack;

/**
 * 背包配置类
 */
public class BackpackConfig {
    private int initialCapacity;
    private int maxCapacity;
    private int maxStackSize;
    
    public BackpackConfig(int initialCapacity, int maxCapacity, int maxStackSize) {
        this.initialCapacity = initialCapacity;
        this.maxCapacity = maxCapacity;
        this.maxStackSize = maxStackSize;
    }
    
    public int getInitialCapacity() {
        return initialCapacity;
    }
    
    public int getMaxCapacity() {
        return maxCapacity;
    }
    
    public int getMaxStackSize() {
        return maxStackSize;
    }
}

