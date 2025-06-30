package cn.game.games.net.game.module.backpack;

import cn.game.games.cache.entity.Item;

/**
 * 背包格子类
 */
public class ItemSlot {
    private Item item;
    private boolean locked;
    
    public ItemSlot() {
        this.item = null;
        this.locked = false;
    }
    
    public Item getItem() {
        return item;
    }
    
    public void setItem(Item item) {
        this.item = item;
    }
    
    public boolean isLocked() {
        return locked;
    }
    
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    
    public boolean isEmpty() {
        return item == null;
    }
}

