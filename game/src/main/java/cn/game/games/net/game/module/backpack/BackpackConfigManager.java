package cn.game.games.net.game.module.backpack;

import java.util.HashMap;
import java.util.Map;

import cn.game.protocol.manual.GoodsTypeEnum;

/**

背包配置管理类

用于配置哪些物品类型使用格子背包
*/
public class BackpackConfigManager {
	private static BackpackConfigManager instance;

// 物品类型到背包类型的映射
	private Map<GoodsTypeEnum, BackpackType> goodsTypeToBackpackType;

// 标记物品类型是否使用格子背包
	private Map<GoodsTypeEnum, Boolean> useGridBackpack;

	private BackpackConfigManager() {
		init();
	}

	public static BackpackConfigManager instance() {
		if (instance == null) {
			instance = new BackpackConfigManager();
		}
		return instance;
	}

	private void init() {
		goodsTypeToBackpackType = new HashMap<>();
		useGridBackpack = new HashMap<>();

		// 配置哪些物品类型使用格子背包
		// 例如：装备、材料、任务物品使用格子背包
		configureGridBackpack(GoodsTypeEnum.Equipment, BackpackType.EQUIPMENT, true);
		configureGridBackpack(GoodsTypeEnum.Item, BackpackType.MATERIAL, true);

		// 其他物品类型使用无限背包
		configureGridBackpack(GoodsTypeEnum.Hero, null, false);
		// ... 添加其他物品类型的配置
	}

	private void configureGridBackpack(GoodsTypeEnum goodsType, BackpackType backpackType, boolean useGrid) {
		goodsTypeToBackpackType.put(goodsType, backpackType);
		useGridBackpack.put(goodsType, useGrid);
	}

	/**
	
	获取物品类型对应的背包类型
	*/
	public BackpackType getBackpackTypeForGoodsType(GoodsTypeEnum goodsType) {
		return goodsTypeToBackpackType.get(goodsType);
	}

	/**
	
	判断物品类型是否使用格子背包
	*/
	public boolean isUsingGridBackpack(GoodsTypeEnum goodsType) {
		Boolean useGrid = useGridBackpack.get(goodsType);
		return useGrid != null && useGrid;
	}
}
