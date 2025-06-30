package cn.game.games.net.game.module.backpack;

import cn.game.games.cache.entity.Item;
import cn.game.games.cache.entity.ItemNoStack;
import cn.game.protocol.manual.OpType;

import java.util.ArrayList;
import java.util.List;

/**
 * 背包基类
 */
public class Backpack {
    protected long playerId;
    protected BackpackType type;
    protected int capacity;
    protected int maxCapacity;
    protected int maxStackSize;
    protected ItemSlot[] slots;
    
    public Backpack(long playerId, BackpackType type, BackpackConfig config) {
        this.playerId = playerId;
        this.type = type;
        this.capacity = config.getInitialCapacity();
        this.maxCapacity = config.getMaxCapacity();
        this.maxStackSize = config.getMaxStackSize();
        this.slots = new ItemSlot[capacity];
        
        // 初始化所有格子
        for (int i = 0; i < capacity; i++) {
            slots[i] = new ItemSlot();
        }
    }
    
    /**
     * 获取背包类型
     */
    public BackpackType getType() {
        return type;
    }
    
    /**
     * 获取背包容量
     */
    public int getCapacity() {
        return capacity;
    }
    
    /**
     * 获取最大容量
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }
    
    /**
     * 扩展背包容量
     */
    public boolean expand(int additionalSlots) {
        if (additionalSlots <= 0) {
            return false;
        }
        
        int newCapacity = capacity + additionalSlots;
        if (newCapacity > maxCapacity) {
            newCapacity = maxCapacity;
        }
        
        if (newCapacity <= capacity) {
            return false;
        }
        
        ItemSlot[] newSlots = new ItemSlot[newCapacity];
        System.arraycopy(slots, 0, newSlots, 0, capacity);
        
        // 初始化新增格子
        for (int i = capacity; i < newCapacity; i++) {
            newSlots[i] = new ItemSlot();
        }
        
        slots = newSlots;
        capacity = newCapacity;
        return true;
    }
    
    /**
     * 检查背包是否已满
     */
    public boolean isFull() {
        return getEmptySlotCount() == 0;
    }
    
    /**
     * 获取空格子数量
     */
    public int getEmptySlotCount() {
        int count = 0;
        for (int i = 0; i < capacity; i++) {
            if (slots[i].isEmpty()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 获取所有物品
     */
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            if (!slots[i].isEmpty()) {
                items.add(slots[i].getItem());
            }
        }
        return items;
    }
    
    /**
     * 根据格子索引获取物品
     */
    public Item getItemBySlot(int slot) {
        if (slot < 0 || slot >= capacity) {
            return null;
        }
        return slots[slot].getItem();
    }
    
    /**
     * 检查格子是否为空
     */
    public boolean isSlotEmpty(int slot) {
        if (slot < 0 || slot >= capacity) {
            return false;
        }
        return slots[slot].isEmpty();
    }
    
    /**
     * 锁定格子
     */
    public boolean lockSlot(int slot) {
        if (slot < 0 || slot >= capacity || slots[slot].isEmpty()) {
            return false;
        }
        
        slots[slot].setLocked(true);
        return true;
    }
    
    /**
     * 解锁格子
     */
    public boolean unlockSlot(int slot) {
        if (slot < 0 || slot >= capacity) {
            return false;
        }
        
        slots[slot].setLocked(false);
        return true;
    }
    
    /**
     * 检查格子是否被锁定
     */
    public boolean isSlotLocked(int slot) {
        if (slot < 0 || slot >= capacity) {
            return false;
        }
        
        return slots[slot].isLocked();
    }
    
    /**
     * 查找物品在背包中的位置
     */
    public int findItemSlot(int configId) {
        for (int i = 0; i < capacity; i++) {
            if (!slots[i].isEmpty() && slots[i].getItem().getConfigId() == configId) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 查找第一个空格子
     */
    public int findEmptySlot() {
        for (int i = 0; i < capacity; i++) {
            if (slots[i].isEmpty() && !slots[i].isLocked()) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 添加物品
     */
    public boolean addItem(Item item, OpType opType) {
        if (item == null || item.getCount() <= 0) {
            return false;
        }
        
        // 判断物品是否可堆叠
        boolean isStackable = !(item instanceof ItemNoStack);
        
        if (isStackable) {
            // 先尝试堆叠到现有的物品上
            for (int i = 0; i < capacity; i++) {
                if (!slots[i].isEmpty() && !slots[i].isLocked()) {
                    Item existingItem = slots[i].getItem();
                    if (existingItem.getConfigId() == item.getConfigId()) {
                        long currentCount = existingItem.getCount();
                        long addCount = item.getCount();
                        
                        if (currentCount + addCount <= maxStackSize) {
                            // 可以完全堆叠
                            existingItem.setCount(currentCount + addCount);
                            return true;
                        } else {
                            // 部分堆叠
                            existingItem.setCount((long) maxStackSize);
                            item.setCount(currentCount + addCount - maxStackSize);
                            // 继续寻找下一个槽位
                        }
                    }
                }
            }
        }
        
        // 需要新格子存放物品
        long remainingCount = item.getCount();
        
        while (remainingCount > 0) {
            int emptySlot = findEmptySlot();
            if (emptySlot == -1) {
                // 背包已满，无法添加更多物品
                return false;
            }
            
            Item newItem;
            if (isStackable) {
                // 可堆叠物品
                newItem = new Item();
                newItem.setId(item.getId());
                newItem.setPlayerId(playerId);
                newItem.setConfigId(item.getConfigId());
                newItem.setType(item.getType());
                newItem.setCreateTimeMillis(System.currentTimeMillis());
                
                if (remainingCount <= maxStackSize) {
                    newItem.setCount(remainingCount);
                    remainingCount = 0;
                } else {
                    newItem.setCount((long) maxStackSize);
                    remainingCount -= maxStackSize;
                }
            } else {
                // 不可堆叠物品，每个占一个格子
                newItem = item;
                remainingCount = 0;
            }
            
            slots[emptySlot].setItem(newItem);
        }
        
        return true;
    }
    
    /**
     * 移除物品
     */
    public boolean removeItem(int slot, int count, OpType opType) {
        if (slot < 0 || slot >= capacity || slots[slot].isEmpty() || slots[slot].isLocked()) {
            return false;
        }
        
        Item item = slots[slot].getItem();
        
        // 判断物品是否可堆叠
        boolean isStackable = !(item instanceof ItemNoStack);
        
        if (!isStackable && count != 1) {
            // 不可堆叠物品只能一次移除一个
            return false;
        }
        
        if (item.getCount() < count) {
            return false;
        }
        
        item.setCount(item.getCount() - count);
        if (item.getCount() == 0) {
            slots[slot].setItem(null);
        }
        
        return true;
    }
    
    /**
     * 移动物品
     */
    public boolean moveItem(int fromSlot, int toSlot) {
        if (fromSlot < 0 || fromSlot >= capacity || toSlot < 0 || toSlot >= capacity) {
            return false;
        }
        
        if (slots[fromSlot].isEmpty() || slots[fromSlot].isLocked() || slots[toSlot].isLocked()) {
            return false;
        }
        
        if (slots[toSlot].isEmpty()) {
            // 目标格子为空，直接移动
            slots[toSlot].setItem(slots[fromSlot].getItem());
            slots[fromSlot].setItem(null);
            return true;
        }
        
        return false;
    }
    
    /**
     * 交换物品
     */
    public boolean swapItems(int slot1, int slot2) {
        if (slot1 < 0 || slot1 >= capacity || slot2 < 0 || slot2 >= capacity) {
            return false;
        }
        
        if (slots[slot1].isLocked() || slots[slot2].isLocked()) {
            return false;
        }
        
        Item temp = slots[slot1].getItem();
        slots[slot1].setItem(slots[slot2].getItem());
        slots[slot2].setItem(temp);
        return true;
    }
    
    /**
     * 拆分物品
     */
    public boolean splitItem(int slot, int count) {
        if (slot < 0 || slot >= capacity || slots[slot].isEmpty() || slots[slot].isLocked()) {
            return false;
        }
        
        Item item = slots[slot].getItem();
        
        // 判断物品是否可堆叠
        if (item instanceof ItemNoStack) {
            // 不可堆叠物品不能拆分
            return false;
        }
        
        if (item.getCount() <= count || count <= 0) {
            return false;
        }
        
        // 寻找空格子
        int emptySlot = findEmptySlot();
        if (emptySlot == -1) {
            return false;
        }
        
        // 创建新物品
        Item newItem = new Item();
        newItem.setId(item.getId());
        newItem.setPlayerId(playerId);
        newItem.setConfigId(item.getConfigId());
        newItem.setType(item.getType());
        newItem.setCreateTimeMillis(System.currentTimeMillis());
        newItem.setCount((long) count);
        
        // 减少原物品数量
        item.setCount(item.getCount() - count);
        
        // 放入新物品
        slots[emptySlot].setItem(newItem);
        
        return true;
    }
    
    /**
     * 合并物品
     */
    public boolean mergeItems(int sourceSlot, int targetSlot) {
        if (sourceSlot < 0 || sourceSlot >= capacity || targetSlot < 0 || targetSlot >= capacity) {
            return false;
        }
        
        if (slots[sourceSlot].isEmpty() || slots[targetSlot].isEmpty()) {
            return false;
        }
        
        if (slots[sourceSlot].isLocked() || slots[targetSlot].isLocked()) {
            return false;
        }
        
        Item sourceItem = slots[sourceSlot].getItem();
        Item targetItem = slots[targetSlot].getItem();
        
        // 判断物品是否可堆叠
        if (sourceItem instanceof ItemNoStack || targetItem instanceof ItemNoStack) {
            // 不可堆叠物品不能合并
            return false;
        }
        
        // 检查是否是相同类型的物品
        if (sourceItem.getConfigId() != targetItem.getConfigId()) {
            return false;
        }
        
        // 计算合并后的数量
        long totalCount = sourceItem.getCount() + targetItem.getCount();
        
        if (totalCount <= maxStackSize) {
            // 可以完全合并
            targetItem.setCount(totalCount);
            slots[sourceSlot].setItem(null);
        } else {
            // 部分合并
            targetItem.setCount((long) maxStackSize);
            sourceItem.setCount(totalCount - maxStackSize);
        }
        
        return true;
    }
    
    /**
     * 整理背包
     */
    public void sortItems() {
        // 先收集所有物品
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            if (!slots[i].isEmpty() && !slots[i].isLocked()) {
                items.add(slots[i].getItem());
                slots[i].setItem(null);
            }
        }
        
        // 按类型和ID排序
        items.sort((a, b) -> {
            if (a.getType() != b.getType()) {
                return a.getType() - b.getType();
            }
            return a.getConfigId() - b.getConfigId();
        });
        
        // 尝试堆叠相同物品
        for (int i = 0; i < items.size() - 1; i++) {
            Item item = items.get(i);
            if (item instanceof ItemNoStack) {
                continue;
            }
            
            for (int j = i + 1; j < items.size(); j++) {
                Item nextItem = items.get(j);
                if (nextItem instanceof ItemNoStack) {
                    continue;
                }
                
                if (item.getConfigId() == nextItem.getConfigId() && item.getCount() < maxStackSize) {
                    long totalCount = item.getCount() + nextItem.getCount();
                    if (totalCount <= maxStackSize) {
                        // 完全合并
                        item.setCount(totalCount);
                        items.remove(j);
                        j--;
                    } else {
                        // 部分合并
                        item.setCount((long) maxStackSize);
                        nextItem.setCount(totalCount - maxStackSize);
                    }
                }
            }
        }
        
        // 放回背包
        for (Item item : items) {
            int emptySlot = findEmptySlot();
            if (emptySlot != -1) {
                slots[emptySlot].setItem(item);
            }
        }
    }
}

