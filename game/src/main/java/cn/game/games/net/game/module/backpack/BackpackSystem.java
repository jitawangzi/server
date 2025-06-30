package cn.game.games.net.game.module.backpack;

import java.util.HashMap;
import java.util.Map;

import cn.game.games.cache.entity.Item;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;

/**
 * 背包系统
 */
public class BackpackSystem {
    private long playerId;
    private Map<BackpackType, Backpack> backpacks;
    
    public BackpackSystem(long playerId) {
        this.playerId = playerId;
        this.backpacks = new HashMap<>();
        
        // 初始化背包
        initBackpacks();
    }
    
    /**
     * 初始化背包
     */
    private void initBackpacks() {
        // 创建装备背包 (初始容量30, 最大容量100, 最大堆叠数1)
        backpacks.put(BackpackType.EQUIPMENT, new Backpack(playerId, BackpackType.EQUIPMENT, 
                new BackpackConfig(30, 100, 1)));
        
        // 创建材料背包 (初始容量40, 最大容量200, 最大堆叠数999)
        backpacks.put(BackpackType.MATERIAL, new Backpack(playerId, BackpackType.MATERIAL, 
                new BackpackConfig(40, 200, 999)));
        
        // 创建任务背包 (初始容量20, 最大容量50, 最大堆叠数50)
        backpacks.put(BackpackType.QUEST, new Backpack(playerId, BackpackType.QUEST, 
                new BackpackConfig(20, 50, 50)));
    }
    
    /**
     * 获取指定类型的背包
     */
    public Backpack getBackpack(BackpackType type) {
        return backpacks.get(type);
    }
    
    /**
     * 扩展背包容量
     */
    public boolean expandBackpack(BackpackType type, int additionalSlots) {
        Backpack backpack = backpacks.get(type);
        if (backpack == null) {
            return false;
        }
        
        return backpack.expand(additionalSlots);
    }
    
    /**
     * 添加物品
     */
    public boolean addItem(Item item, OpType opType) {
        BackpackType targetType = getTargetBackpackType(item);
        Backpack backpack = backpacks.get(targetType);
        
        if (backpack == null) {
            return false;
        }
        
        return backpack.addItem(item, opType);
    }
    
    /**
     * 根据物品类型确定目标背包
     */
    private BackpackType getTargetBackpackType(Item item) {
		if (item.getType() == GoodsTypeEnum.Equipment.getId()) {
            return BackpackType.EQUIPMENT;
		} else if (item.getType() == GoodsTypeEnum.Item.getId()) {
            return BackpackType.MATERIAL;
		} else if (item.getType() == GoodsTypeEnum.Skin.getId()) {
            return BackpackType.QUEST;
        } else {
            // 默认放入材料背包
            return BackpackType.MATERIAL;
        }
    }
    
    /**
     * 移除物品
     */
    public boolean removeItem(BackpackType type, int slot, int count, OpType opType) {
        Backpack backpack = backpacks.get(type);
        if (backpack == null) {
            return false;
        }
        
        return backpack.removeItem(slot, count, opType);
    }
    
    /**
     * 移动物品
     */
    public boolean moveItem(BackpackType type, int fromSlot, int toSlot) {
        Backpack backpack = backpacks.get(type);
        if (backpack == null) {
            return false;
        }
        
        return backpack.moveItem(fromSlot, toSlot);
    }
    
    /**
     * 交换物品
     */
    public boolean swapItems(BackpackType type, int slot1, int slot2) {
        Backpack backpack = backpacks.get(type);
        if (backpack == null) {
            return false;
        }
        
        return backpack.swapItems(slot1, slot2);
    }
    
    /**
     * 拆分物品
     */
    public boolean splitItem(BackpackType type, int slot, int count) {
        Backpack backpack = backpacks.get(type);
        if (backpack == null) {
            return false;
        }
        
        return backpack.splitItem(slot, count);
    }
    
    /**
     * 合并物品
     */
    public boolean mergeItems(BackpackType type, int sourceSlot, int targetSlot) {
        Backpack backpack = backpacks.get(type);
        if (backpack == null) {
            return false;
        }
        
        return backpack.mergeItems(sourceSlot, targetSlot);
    }
    
    /**
     * 锁定物品
     */
    public boolean lockItem(BackpackType type, int slot) {
        Backpack backpack = backpacks.get(type);
        if (backpack == null) {
            return false;
        }
        
        return backpack.lockSlot(slot);
    }
    
    /**
     * 解锁物品
     */
    public boolean unlockItem(BackpackType type, int slot) {
        Backpack backpack = backpacks.get(type);
        if (backpack == null) {
            return false;
        }
        
        return backpack.unlockSlot(slot);
    }
    
    /**
     * 整理背包
     */
    public void sortBackpack(BackpackType type) {
        Backpack backpack = backpacks.get(type);
        if (backpack != null) {
            backpack.sortItems();
        }
    }
    
    /**
     * 整理所有背包
     */
    public void sortAllBackpacks() {
        for (Backpack backpack : backpacks.values()) {
            backpack.sortItems();
        }
    }
}

