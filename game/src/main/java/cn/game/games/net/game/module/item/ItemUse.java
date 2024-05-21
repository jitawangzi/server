package cn.game.games.net.game.module.item;

import java.util.List;

import cn.game.games.cache.entity.Player;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.config.ItemConfig;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.generated.manager.ItemManager;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.IndexedEnum;
import cn.game.util.IndexedEnumUtil;

public enum ItemUse implements IndexedEnum {
	HeroChoose(4) {
        @Override
		public List<RewardInfo> use(Player player, int id, int num, int param) {
        	if (num != 1) {
				throw new IllegalArgumentException("请求选择hero道具数量应该是1");
			}
			ItemConfig itemConfig = ItemManager.instance().get(id);
			HeroConfig heroConfig = HeroManager.instance().get(param);
			if (itemConfig.Para != heroConfig.InitialQuality) {
				throw new IllegalArgumentException("请求选择hero 的id 和品质不符。");
			}
			return player.getHeroModule().addReward(param, 1, OpType.ItemChoose);
		}
	};

    private final int index;

    private ItemUse(int index) {
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

	public abstract List<RewardInfo> use(Player player, int id, int num, int param);

    public static final List<ItemUse> lists = IndexedEnumUtil.toIndexes(ItemUse.values());

    public static ItemUse valueOf(int index) {
        return IndexedEnumUtil.valueOf(lists, index);
    }

}
